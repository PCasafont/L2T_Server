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

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.AttackableAI;
import l2server.gameserver.ai.CreatureAI;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.knownlist.DecoyKnownList;
import l2server.gameserver.model.actor.stat.DecoyStat;
import l2server.gameserver.network.serverpackets.CharInfo;
import l2server.gameserver.stats.skills.SkillDecoy;
import l2server.gameserver.taskmanager.DecayTaskManager;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.gameserver.templates.item.WeaponTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

public class DecoyInstance extends Attackable {
	private static Logger log = LoggerFactory.getLogger(DecoyInstance.class.getName());


	private Player owner;
	private int totalLifeTime;
	private int timeRemaining;
	private Future<?> decoyLifeTask;
	private List<Future<?>> skillSpam = new ArrayList<>();
	
	public DecoyInstance(int objectId, NpcTemplate template, Player owner, Skill skill) {
		super(objectId, template);
		this.owner = owner;
		setXYZ(owner.getX(), owner.getY(), owner.getZ());
		setIsInvul(false);
		setInstanceType(InstanceType.L2DecoyInstance);
		if (skill != null) {
			totalLifeTime = ((SkillDecoy) skill).getTotalLifeTime();
		} else {
			totalLifeTime = 20000;
		}
		timeRemaining = totalLifeTime;
		int delay = 1000;
		decoyLifeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new DecoyLifetime(getOwner(), this), delay, delay);
		if (template.getSkills() != null) {
			for (Skill s : template.getSkills().values()) {
				if (s.isActive()) {
					skillSpam.add(ThreadPoolManager.getInstance()
							.scheduleGeneralAtFixedRate(new SkillSpam(this, SkillTable.getInstance().getInfo(s.getId(), s.getLevelHash())),
									2000,
									5000));
				}
			}
		}
		if (getName().equalsIgnoreCase("Clone Attack") && getNpcId() >= 13319 && getNpcId() <= 13322) {
			skillSpam.add(ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new SkillSpam(this, null), 100, 100));
		}
	}
	
	@Override
	public boolean doDie(Creature killer) {
		if (!super.doDie(killer)) {
			return false;
		}
		for (Future<?> spam : skillSpam) {
			spam.cancel(true);
		}
		skillSpam.clear();
		totalLifeTime = 0;
		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}
	
	@Override
	public CreatureAI initAI() {
		setIsRunning(true);
		
		if (getNpcId() >= 13319 && getNpcId() <= 13322) {
			return new AttackableAI(this);
		} else {
			return new CreatureAI(this);
		}
	}
	
	@Override
	public DecoyKnownList getKnownList() {
		return (DecoyKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList() {
		setKnownList(new DecoyKnownList(this));
	}
	
	@Override
	public final DecoyStat getStat() {
		return (DecoyStat) super.getStat();
	}
	
	@Override
	public void initCharStat() {
		setStat(new DecoyStat(this));
	}
	
	static class DecoyLifetime implements Runnable {
		private Player activeChar;
		
		private DecoyInstance decoy;
		
		DecoyLifetime(Player activeChar, DecoyInstance Decoy) {
			this.activeChar = activeChar;
			this.decoy = Decoy;
		}
		
		@Override
		public void run() {
			try {
				double newTimeRemaining;
				decoy.decTimeRemaining(1000);
				newTimeRemaining = decoy.getTimeRemaining();
				if (newTimeRemaining < 0) {
					decoy.unSummon(activeChar);
				}
			} catch (Exception e) {
				log.error("Decoy Error: ", e);
			}
		}
	}
	
	static class SkillSpam implements Runnable {
		private DecoyInstance activeChar;
		
		private Skill skill;
		
		SkillSpam(DecoyInstance activeChar, Skill Hate) {
			this.activeChar = activeChar;
			skill = Hate;
		}
		
		@Override
		public void run() {
			try {
				if (skill != null) {
					activeChar.setTarget(activeChar);
					activeChar.doCast(skill);
				} else if (activeChar.getOwner().getTarget() instanceof Creature) {
					Creature target = (Creature) activeChar.getOwner().getTarget();
					activeChar.addDamageHate(target, 1, 1);
					//activeChar.doAttack(target);
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
			} catch (Throwable e) {
				log.error("Decoy Error: ", e);
			}
		}
	}
	
	public void decTimeRemaining(int value) {
		timeRemaining -= value;
	}
	
	public int getTimeRemaining() {
		return timeRemaining;
	}
	
	public int getTotalLifeTime() {
		return totalLifeTime;
	}
	
	@Override
	public void onSpawn() {
		super.onSpawn();
		getOwner().sendPacket(new CharInfo(this));
	}
	
	@Override
	public void updateAbnormalEffect() {
		Collection<Player> plrs = getKnownList().getKnownPlayers().values();
		//synchronized (getKnownList().getKnownPlayers())
		{
			for (Player player : plrs) {
				if (player != null) {
					player.sendPacket(new CharInfo(this));
				}
			}
		}
	}
	
	public void stopDecay() {
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}
	
	@Override
	public void onDecay() {
		deleteMe(owner);
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker) {
		return owner.isAutoAttackable(attacker);
	}
	
	@Override
	public Item getActiveWeaponInstance() {
		return null;
	}
	
	@Override
	public WeaponTemplate getActiveWeaponItem() {
		return null;
	}
	
	@Override
	public Item getSecondaryWeaponInstance() {
		return null;
	}
	
	@Override
	public WeaponTemplate getSecondaryWeaponItem() {
		return null;
	}
	
	public void deleteMe(Player owner) {
		decayMe();
		getKnownList().removeAllKnownObjects();
		owner.setDecoy(null);
	}
	
	public synchronized void unSummon(Player owner) {
		if (decoyLifeTask != null) {
			decoyLifeTask.cancel(true);
			decoyLifeTask = null;
		}
		for (Future<?> spam : skillSpam) {
			spam.cancel(true);
		}
		skillSpam.clear();
		
		if (isVisible() && !isDead()) {
			if (getWorldRegion() != null) {
				getWorldRegion().removeFromZones(this);
			}
			owner.setDecoy(null);
			decayMe();
			getKnownList().removeAllKnownObjects();
		}
	}
	
	@Override
	public final Player getOwner() {
		return owner;
	}
	
	@Override
	public Player getActingPlayer() {
		return owner;
	}
	
	@Override
	public void sendInfo(Player activeChar) {
		activeChar.sendPacket(new CharInfo(this));
	}
}
