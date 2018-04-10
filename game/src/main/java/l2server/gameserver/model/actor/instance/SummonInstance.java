/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.model.actor.instance;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.network.serverpackets.SetSummonRemainTime;
import l2server.gameserver.stats.skills.SkillSummon;
import l2server.gameserver.taskmanager.DecayTaskManager;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.gameserver.templates.skills.AbnormalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

public class SummonInstance extends Summon {
	private static Logger log = LoggerFactory.getLogger(SummonInstance.class.getName());

	private float expPenalty = 0; // exp decrease multiplier (i.e. 0.3 (= 30%) for shadow)
	private int itemConsumeId;
	private int itemConsumeCount;
	private int itemConsumeSteps;
	private final int totalLifeTime;
	private final int timeLostIdle;
	private final int timeLostActive;
	private int timeRemaining;
	private int nextItemConsumeTime;
	private int summonSkillId;
	private Skill summonPrice;
	private int summonPoints;
	public int lastLifeTimeCheck; // Following FbiAgent's example to avoid sending useless packets
	
	private Future<?> summonLifeTask;
	
	public SummonInstance(int objectId, NpcTemplate template, Player owner, Skill skill) {
		super(objectId, template, owner);
		setInstanceType(InstanceType.L2SummonInstance);
		setShowSummonAnimation(true);
		
		if (skill != null) {
			final SkillSummon summonSkill = (SkillSummon) skill;
			itemConsumeId = summonSkill.getItemConsumeIdOT();
			itemConsumeCount = summonSkill.getItemConsumeOT();
			itemConsumeSteps = summonSkill.getItemConsumeSteps();
			totalLifeTime = summonSkill.getTotalLifeTime();
			timeLostIdle = summonSkill.getTimeLostIdle();
			timeLostActive = summonSkill.getTimeLostActive();
			int summonPrice = summonSkill.getSummonPrice();
			if (summonPrice != 0) {
				this.summonPrice = SkillTable.getInstance().getInfo(summonPrice, 1);
			} else {
				this.summonPrice = null;
			}
			summonPoints = summonSkill.getSummonPoints();
			summonSkillId = summonSkill.getId();
		} else {
			// defaults
			itemConsumeId = 0;
			itemConsumeCount = 0;
			itemConsumeSteps = 0;
			totalLifeTime = -1; // infinite
			timeLostIdle = 1000;
			timeLostActive = 1000;
			summonPrice = null;
			summonPoints = 0;
			summonSkillId = 0;
		}
		
		timeRemaining = totalLifeTime;
		lastLifeTimeCheck = totalLifeTime;
		
		if (itemConsumeId == 0) {
			nextItemConsumeTime = -1; // do not consume
		} else if (itemConsumeSteps == 0) {
			nextItemConsumeTime = -1; // do not consume
		} else {
			nextItemConsumeTime = totalLifeTime - totalLifeTime / (itemConsumeSteps + 1);
		}
		
		// When no item consume is defined task only need to check when summon life time has ended.
		// Otherwise have to destroy items from owner's inventory in order to let summon live.
		int delay = 1000;
		
		if (Config.DEBUG && itemConsumeCount != 0) {
			log.warn("SummonInstance: Item Consume ID: " + itemConsumeId + ", Count: " + itemConsumeCount + ", Rate: " + itemConsumeSteps +
					" times.");
		}
		if (Config.DEBUG) {
			log.warn("SummonInstance: Task Delay " + delay / 1000 + " seconds.");
		}
		
		summonLifeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new SummonLifetime(getOwner(), this), delay, delay);
		
		// Restore summon's buffs if there are any stored (Noblesse)
		if (!getOwner().isInOlympiadMode()) {
			Abnormal[] restoreEffects = getOwner().restoreSummonBuffs();
			// Only restore buffs if new summon is same kind as last, else clear stored buffs
			if (restoreEffects != null && getNpcId() == getOwner().getLastSummonId()) {
				for (Abnormal e : restoreEffects) {
					if (e.getType() == AbnormalType.HIDE) {
						continue;
					}
					
					e.getSkill().getEffects(getOwner(), this);
				}
			} else {
				getOwner().storeSummonBuffs(null);
			}
		}
		
		// Give the owner's buffs
		for (Abnormal e : getOwner().getAllEffects()) {
			if (e.getType() == AbnormalType.HIDE) {
				continue;
			}
			
			if (e.canBeShared()) {
				for (Abnormal sE : e.getSkill().getEffects(getOwner(), this)) {
					sE.setFirstTime(e.getTime());
				}
			}
		}
	}
	
	@Override
	public final int getLevel() {
		return getTemplate() != null ? getTemplate().Level : 0;
	}
	
	@Override
	public int getSummonType() {
		return 1;
	}
	
	public void setExpPenalty(float expPenalty) {
		this.expPenalty = expPenalty;
	}
	
	public float getExpPenalty() {
		return expPenalty;
	}
	
	public int getItemConsumeCount() {
		return itemConsumeCount;
	}
	
	public int getItemConsumeId() {
		return itemConsumeId;
	}
	
	public int getItemConsumeSteps() {
		return itemConsumeSteps;
	}
	
	public int getNextItemConsumeTime() {
		return nextItemConsumeTime;
	}
	
	public int getTotalLifeTime() {
		return totalLifeTime;
	}
	
	public int getTimeLostIdle() {
		return timeLostIdle;
	}
	
	public int getTimeLostActive() {
		return timeLostActive;
	}
	
	public int getTimeRemaining() {
		return timeRemaining;
	}
	
	public int getSummonSkillId() {
		return summonSkillId;
	}
	
	public void setNextItemConsumeTime(int value) {
		nextItemConsumeTime = value;
	}
	
	public void decNextItemConsumeTime(int value) {
		nextItemConsumeTime -= value;
	}
	
	public void decTimeRemaining(int value) {
		timeRemaining -= value;
	}
	
	public void addExpAndSp(int addToExp, int addToSp) {
		getOwner().addExpAndSp(addToExp, addToSp);
	}
	
	@Override
	public boolean doDie(Creature killer) {
		// Store buffs of summon when it does while having Noblesse Blessing
		if (getOwner() != null && !getOwner().isInOlympiadMode()) {
			Abnormal[] effects = getAllEffects();
			for (Abnormal e : effects) {
				if (e.getSkill().getId() == 1323 || e.getSkill().getId() == 7096) // Noblesse Blessing and Master's Blessing of Noblesse
				{
					getOwner().storeSummonBuffs(effects);
				}
			}
		}
		
		if (!super.doDie(killer)) {
			return false;
		}
		
		// To prevent players re-summoning their dead summons endlessly
		if (summonPoints > 0 && !(getOwner() != null && getOwner().isPlayingEvent() && !getOwner().getEvent().isType(EventType.Survival) &&
				!getOwner().getEvent().isType(EventType.TeamSurvival))) {
			DecayTaskManager.getInstance().addDecayTask(this, 5000);
		}
		
		if (Config.DEBUG) {
			log.warn("SummonInstance: " + getTemplate().Name + " (" + getOwner().getName() + ") has been killed.");
		}
		
		if (summonLifeTask != null) {
			summonLifeTask.cancel(true);
			summonLifeTask = null;
		}
		return true;
	}
	
	/**
	 * Servitors' skills automatically change their level based on the servitor's level.
	 * Until level 70, the servitor gets 1 lv of skill per 10 levels. After that, it is 1
	 * skill level per 5 servitor levels.  If the resulting skill level doesn't exist use
	 * the max that does exist!
	 *
	 * @see Creature#doCast(Skill)
	 */
	@Override
	public void doCast(Skill skill) {
		final int petLevel = getLevel();
		int skillLevel = petLevel / 10;
		if (petLevel >= 70) {
			skillLevel += (petLevel - 65) / 10;
		}
		
		// adjust the level for servitors less than lv 10
		if (skillLevel < 1) {
			skillLevel = 1;
		}
		
		Skill skillToCast = SkillTable.getInstance().getInfo(skill.getId(), skillLevel);
		
		if (skillToCast != null) {
			super.doCast(skillToCast);
		} else {
			super.doCast(skill);
		}
	}
	
	static class SummonLifetime implements Runnable {
		private Player activeChar;
		private SummonInstance summon;
		
		SummonLifetime(Player activeChar, SummonInstance newpet) {
			this.activeChar = activeChar;
			summon = newpet;
		}
		
		@Override
		public void run() {
			if (Config.DEBUG) {
				log.warn("SummonInstance: " + summon.getTemplate().Name + " (" + activeChar.getName() + ") run task.");
			}
			
			try {
				double oldTimeRemaining = summon.getTimeRemaining();
				int maxTime = summon.getTotalLifeTime();
				double newTimeRemaining;
				
				// if pet is attacking
				if (summon.isAttackingNow()) {
					summon.decTimeRemaining(summon.getTimeLostActive());
				} else {
					summon.decTimeRemaining(summon.getTimeLostIdle());
				}
				newTimeRemaining = summon.getTimeRemaining();
				// check if the summon's lifetime has ran out
				if (maxTime > 0 && newTimeRemaining < 0) {
					summon.unSummon(activeChar);
				}
				// check if it is time to consume another item
				else if (newTimeRemaining <= summon.getNextItemConsumeTime() && oldTimeRemaining > summon.getNextItemConsumeTime()) {
					summon.decNextItemConsumeTime(maxTime / (summon.getItemConsumeSteps() + 1));
					
					// check if owner has enought itemConsume, if requested
					if (summon.getItemConsumeCount() > 0 && summon.getItemConsumeId() != 0 && !summon.isDead() &&
							!summon.destroyItemByItemId("Consume", summon.getItemConsumeId(), summon.getItemConsumeCount(), activeChar, true)) {
						summon.unSummon(activeChar);
					}
				}
				
				// prevent useless packet-sending when the difference isn't visible.
				/*if ((summon.lastShowntimeRemaining - newTimeRemaining) > maxTime / 352)
                {
					summon.getOwner().sendPacket(new SetSummonRemainTime(maxTime, (int) newTimeRemaining));
					summon.lastShowntimeRemaining = (int) newTimeRemaining;
					summon.updateEffectIcons();
				}*/
				if (summon.lastLifeTimeCheck > 50) {
					summon.getOwner().sendPacket(new SetSummonRemainTime(maxTime, (int) newTimeRemaining));
					summon.lastLifeTimeCheck = 0;
					if (summon.summonPrice != null) {
						summon.summonPrice.getEffects(summon, activeChar);
					}
					summon.updateEffectIcons();
				}
				summon.lastLifeTimeCheck++;
			} catch (Exception e) {
				log.error("Error on player [" + activeChar.getName() + "] summon item consume task.", e);
			}
		}
	}
	
	@Override
	public void unSummon(Player owner) {
		if (Config.DEBUG) {
			log.warn("SummonInstance: " + getTemplate().Name + " (" + owner.getName() + ") unsummoned.");
		}
		
		if (summonLifeTask != null) {
			summonLifeTask.cancel(true);
			summonLifeTask = null;
			
			if (summonPrice != null) {
				for (Abnormal e : owner.getAllEffects()) {
					if (e.getSkill().getId() == summonPrice.getId()) {
						e.exit();
					}
				}
			}
		}
		
		super.unSummon(owner);
	}
	
	@Override
	public boolean destroyItem(String process, int objectId, long count, WorldObject reference, boolean sendMessage) {
		return getOwner().destroyItem(process, objectId, count, reference, sendMessage);
	}
	
	@Override
	public boolean destroyItemByItemId(String process, int itemId, long count, WorldObject reference, boolean sendMessage) {
		if (Config.DEBUG) {
			log.warn("SummonInstance: " + getTemplate().Name + " (" + getOwner().getName() + ") consume.");
		}
		
		return getOwner().destroyItemByItemId(process, itemId, count, reference, sendMessage);
	}
	
	@Override
	public byte getAttackElement() {
		if (getOwner() == null || !getOwner().getCurrentClass().isSummoner()) {
			return super.getAttackElement();
		}
		
		return getOwner().getAttackElement();
	}
	
	@Override
	public int getAttackElementValue(byte attribute) {
		if (getOwner() == null || !getOwner().getCurrentClass().isSummoner() || getOwner().getExpertiseWeaponPenalty() > 0) {
			return super.getAttackElementValue(attribute);
		}
		
		return getOwner().getAttackElementValue(attribute);
	}
	
	@Override
	public int getDefenseElementValue(byte attribute) {
		if (getOwner() == null || !getOwner().getCurrentClass().isSummoner()) {
			return super.getDefenseElementValue(attribute);
		}
		
		return getOwner().getDefenseElementValue(attribute);
	}
	
	@Override
	public boolean isMovementDisabled() {
		return super.isMovementDisabled() || !getTemplate().getAIData().canMove();
	}
	
	/**
	 * Tenkai custom
	 *
	 * @return returns true if the owner of this servitor is in Olympiad mode
	 */
	public boolean isInOlympiadMode() {
		return owner != null && owner.isInOlympiadMode();
	}
	
	public int getSummonPoints() {
		return summonPoints;
	}
}
