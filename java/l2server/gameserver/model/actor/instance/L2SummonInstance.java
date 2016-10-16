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
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.network.serverpackets.SetSummonRemainTime;
import l2server.gameserver.stats.skills.L2SkillSummon;
import l2server.gameserver.taskmanager.DecayTaskManager;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.log.Log;

import java.util.concurrent.Future;
import java.util.logging.Level;

public class L2SummonInstance extends L2Summon
{
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
	private L2Skill summonPrice;
	private int summonPoints;
	public int lastLifeTimeCheck; // Following FbiAgent's example to avoid sending useless packets

	private Future<?> summonLifeTask;

	public L2SummonInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill)
	{
		super(objectId, template, owner);
		setInstanceType(InstanceType.L2SummonInstance);
		setShowSummonAnimation(true);

		if (skill != null)
		{
			final L2SkillSummon summonSkill = (L2SkillSummon) skill;
			this.itemConsumeId = summonSkill.getItemConsumeIdOT();
			this.itemConsumeCount = summonSkill.getItemConsumeOT();
			this.itemConsumeSteps = summonSkill.getItemConsumeSteps();
			this.totalLifeTime = summonSkill.getTotalLifeTime();
			this.timeLostIdle = summonSkill.getTimeLostIdle();
			this.timeLostActive = summonSkill.getTimeLostActive();
			int summonPrice = summonSkill.getSummonPrice();
			if (summonPrice != 0)
			{
				this.summonPrice = SkillTable.getInstance().getInfo(summonPrice, 1);
			}
			else
			{
				this.summonPrice = null;
			}
			this.summonPoints = summonSkill.getSummonPoints();
			this.summonSkillId = summonSkill.getId();
		}
		else
		{
			// defaults
			this.itemConsumeId = 0;
			this.itemConsumeCount = 0;
			this.itemConsumeSteps = 0;
			this.totalLifeTime = -1; // infinite
			this.timeLostIdle = 1000;
			this.timeLostActive = 1000;
			this.summonPrice = null;
			this.summonPoints = 0;
			this.summonSkillId = 0;
		}

		this.timeRemaining = this.totalLifeTime;
		this.lastLifeTimeCheck = this.totalLifeTime;

		if (this.itemConsumeId == 0)
		{
			this.nextItemConsumeTime = -1; // do not consume
		}
		else if (this.itemConsumeSteps == 0)
		{
			this.nextItemConsumeTime = -1; // do not consume
		}
		else
		{
			this.nextItemConsumeTime = this.totalLifeTime - this.totalLifeTime / (this.itemConsumeSteps + 1);
		}

		// When no item consume is defined task only need to check when summon life time has ended.
		// Otherwise have to destroy items from owner's inventory in order to let summon live.
		int delay = 1000;

		if (Config.DEBUG && this.itemConsumeCount != 0)
		{
			Log.warning("L2SummonInstance: Item Consume ID: " + this.itemConsumeId + ", Count: " + this.itemConsumeCount +
					", Rate: " + this.itemConsumeSteps + " times.");
		}
		if (Config.DEBUG)
		{
			Log.warning("L2SummonInstance: Task Delay " + delay / 1000 + " seconds.");
		}

		this.summonLifeTask = ThreadPoolManager.getInstance()
				.scheduleGeneralAtFixedRate(new SummonLifetime(getOwner(), this), delay, delay);

		// Restore summon's buffs if there are any stored (Noblesse)
		if (!getOwner().isInOlympiadMode())
		{
			L2Abnormal[] restoreEffects = getOwner().restoreSummonBuffs();
			// Only restore buffs if new summon is same kind as last, else clear stored buffs
			if (restoreEffects != null && getNpcId() == getOwner().getLastSummonId())
			{
				for (L2Abnormal e : restoreEffects)
				{
					if (e.getType() == L2AbnormalType.HIDE)
					{
						continue;
					}

					e.getSkill().getEffects(getOwner(), this);
				}
			}
			else
			{
				getOwner().storeSummonBuffs(null);
			}
		}

		// Give the owner's buffs
		for (L2Abnormal e : getOwner().getAllEffects())
		{
			if (e.getType() == L2AbnormalType.HIDE)
			{
				continue;
			}

			if (e.canBeShared())
			{
				for (L2Abnormal sE : e.getSkill().getEffects(getOwner(), this))
				{
					sE.setFirstTime(e.getTime());
				}
			}
		}
	}

	@Override
	public final int getLevel()
	{
		return getTemplate() != null ? getTemplate().Level : 0;
	}

	@Override
	public int getSummonType()
	{
		return 1;
	}

	public void setExpPenalty(float expPenalty)
	{
		this.expPenalty = expPenalty;
	}

	public float getExpPenalty()
	{
		return this.expPenalty;
	}

	public int getItemConsumeCount()
	{
		return this.itemConsumeCount;
	}

	public int getItemConsumeId()
	{
		return this.itemConsumeId;
	}

	public int getItemConsumeSteps()
	{
		return this.itemConsumeSteps;
	}

	public int getNextItemConsumeTime()
	{
		return this.nextItemConsumeTime;
	}

	public int getTotalLifeTime()
	{
		return this.totalLifeTime;
	}

	public int getTimeLostIdle()
	{
		return this.timeLostIdle;
	}

	public int getTimeLostActive()
	{
		return this.timeLostActive;
	}

	public int getTimeRemaining()
	{
		return this.timeRemaining;
	}

	public int getSummonSkillId()
	{
		return this.summonSkillId;
	}

	public void setNextItemConsumeTime(int value)
	{
		this.nextItemConsumeTime = value;
	}

	public void decNextItemConsumeTime(int value)
	{
		this.nextItemConsumeTime -= value;
	}

	public void decTimeRemaining(int value)
	{
		this.timeRemaining -= value;
	}

	public void addExpAndSp(int addToExp, int addToSp)
	{
		getOwner().addExpAndSp(addToExp, addToSp);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		// Store buffs of summon when it does while having Noblesse Blessing
		if (getOwner() != null && !getOwner().isInOlympiadMode())
		{
			L2Abnormal[] effects = getAllEffects();
			for (L2Abnormal e : effects)
			{
				if (e.getSkill().getId() == 1323 ||
						e.getSkill().getId() == 7096) // Noblesse Blessing and Master's Blessing of Noblesse
				{
					getOwner().storeSummonBuffs(effects);
				}
			}
		}

		if (!super.doDie(killer))
		{
			return false;
		}

		// To prevent players re-summoning their dead summons endlessly
		if (this.summonPoints > 0 && !(getOwner() != null && getOwner().isPlayingEvent() &&
				!getOwner().getEvent().isType(EventType.Survival) &&
				!getOwner().getEvent().isType(EventType.TeamSurvival)))
		{
			DecayTaskManager.getInstance().addDecayTask(this, 5000);
		}

		if (Config.DEBUG)
		{
			Log.warning("L2SummonInstance: " + getTemplate().Name + " (" + getOwner().getName() + ") has been killed.");
		}

		if (this.summonLifeTask != null)
		{
			this.summonLifeTask.cancel(true);
			this.summonLifeTask = null;
		}
		return true;
	}

	/**
	 * Servitors' skills automatically change their level based on the servitor's level.
	 * Until level 70, the servitor gets 1 lv of skill per 10 levels. After that, it is 1
	 * skill level per 5 servitor levels.  If the resulting skill level doesn't exist use
	 * the max that does exist!
	 *
	 * @see l2server.gameserver.model.actor.L2Character#doCast(l2server.gameserver.model.L2Skill)
	 */
	@Override
	public void doCast(L2Skill skill)
	{
		final int petLevel = getLevel();
		int skillLevel = petLevel / 10;
		if (petLevel >= 70)
		{
			skillLevel += (petLevel - 65) / 10;
		}

		// adjust the level for servitors less than lv 10
		if (skillLevel < 1)
		{
			skillLevel = 1;
		}

		L2Skill skillToCast = SkillTable.getInstance().getInfo(skill.getId(), skillLevel);

		if (skillToCast != null)
		{
			super.doCast(skillToCast);
		}
		else
		{
			super.doCast(skill);
		}
	}

	static class SummonLifetime implements Runnable
	{
		private L2PcInstance activeChar;
		private L2SummonInstance summon;

		SummonLifetime(L2PcInstance activeChar, L2SummonInstance newpet)
		{
			this.activeChar = activeChar;
			this.summon = newpet;
		}

		@Override
		public void run()
		{
			if (Config.DEBUG)
			{
				Log.warning("L2SummonInstance: " + this.summon.getTemplate().Name + " (" + this.activeChar.getName() +
						") run task.");
			}

			try
			{
				double oldTimeRemaining = this.summon.getTimeRemaining();
				int maxTime = this.summon.getTotalLifeTime();
				double newTimeRemaining;

				// if pet is attacking
				if (this.summon.isAttackingNow())
				{
					this.summon.decTimeRemaining(this.summon.getTimeLostActive());
				}
				else
				{
					this.summon.decTimeRemaining(this.summon.getTimeLostIdle());
				}
				newTimeRemaining = this.summon.getTimeRemaining();
				// check if the summon's lifetime has ran out
				if (maxTime > 0 && newTimeRemaining < 0)
				{
					this.summon.unSummon(this.activeChar);
				}
				// check if it is time to consume another item
				else if (newTimeRemaining <= this.summon.getNextItemConsumeTime() &&
						oldTimeRemaining > summon.getNextItemConsumeTime())
				{
					this.summon.decNextItemConsumeTime(maxTime / (this.summon.getItemConsumeSteps() + 1));

					// check if owner has enought itemConsume, if requested
					if (this.summon.getItemConsumeCount() > 0 && this.summon.getItemConsumeId() != 0 && !this.summon.isDead() &&
							!this.summon.destroyItemByItemId("Consume", this.summon.getItemConsumeId(),
									this.summon.getItemConsumeCount(), this.activeChar, true))
					{
						this.summon.unSummon(this.activeChar);
					}
				}

				// prevent useless packet-sending when the difference isn't visible.
				/*if ((this.summon.lastShowntimeRemaining - newTimeRemaining) > maxTime / 352)
                {
					this.summon.getOwner().sendPacket(new SetSummonRemainTime(maxTime, (int) newTimeRemaining));
					this.summon.lastShowntimeRemaining = (int) newTimeRemaining;
					this.summon.updateEffectIcons();
				}*/
				if (this.summon.lastLifeTimeCheck > 50)
				{
					this.summon.getOwner().sendPacket(new SetSummonRemainTime(maxTime, (int) newTimeRemaining));
					this.summon.lastLifeTimeCheck = 0;
					if (this.summon.summonPrice != null)
					{
						this.summon.summonPrice.getEffects(this.summon, this.activeChar);
					}
					this.summon.updateEffectIcons();
				}
				this.summon.lastLifeTimeCheck++;
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "Error on player [" + this.activeChar.getName() + "] summon item consume task.", e);
			}
		}
	}

	@Override
	public void unSummon(L2PcInstance owner)
	{
		if (Config.DEBUG)
		{
			Log.warning("L2SummonInstance: " + getTemplate().Name + " (" + owner.getName() + ") unsummoned.");
		}

		if (this.summonLifeTask != null)
		{
			this.summonLifeTask.cancel(true);
			this.summonLifeTask = null;

			if (this.summonPrice != null)
			{
				for (L2Abnormal e : this.owner.getAllEffects())
				{
					if (e.getSkill().getId() == this.summonPrice.getId())
					{
						e.exit();
					}
				}
			}
		}

		super.unSummon(owner);
	}

	@Override
	public boolean destroyItem(String process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		return getOwner().destroyItem(process, objectId, count, reference, sendMessage);
	}

	@Override
	public boolean destroyItemByItemId(String process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		if (Config.DEBUG)
		{
			Log.warning("L2SummonInstance: " + getTemplate().Name + " (" + getOwner().getName() + ") consume.");
		}

		return getOwner().destroyItemByItemId(process, itemId, count, reference, sendMessage);
	}

	@Override
	public byte getAttackElement()
	{
		if (getOwner() == null || !getOwner().getCurrentClass().isSummoner())
		{
			return super.getAttackElement();
		}

		return getOwner().getAttackElement();
	}

	@Override
	public int getAttackElementValue(byte attribute)
	{
		if (getOwner() == null || !getOwner().getCurrentClass().isSummoner() ||
				getOwner().getExpertiseWeaponPenalty() > 0)
		{
			return super.getAttackElementValue(attribute);
		}

		return getOwner().getAttackElementValue(attribute);
	}

	@Override
	public int getDefenseElementValue(byte attribute)
	{
		if (getOwner() == null || !getOwner().getCurrentClass().isSummoner())
		{
			return super.getDefenseElementValue(attribute);
		}

		return getOwner().getDefenseElementValue(attribute);
	}

	@Override
	public boolean isMovementDisabled()
	{
		return super.isMovementDisabled() || !getTemplate().getAIData().canMove();
	}

	/**
	 * Tenkai custom
	 *
	 * @return returns true if the owner of this servitor is in Olympiad mode
	 */
	public boolean isInOlympiadMode()
	{
		return this.owner != null && this.owner.isInOlympiadMode();
	}

	public int getSummonPoints()
	{
		return this.summonPoints;
	}
}
