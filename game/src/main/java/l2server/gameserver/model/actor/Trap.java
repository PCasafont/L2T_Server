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

package l2server.gameserver.model.actor;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.knownlist.TrapKnownList;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.Quest.TrapAction;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.NpcInfo;
import l2server.gameserver.network.serverpackets.SocialAction;
import l2server.gameserver.taskmanager.DecayTaskManager;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.gameserver.templates.item.WeaponTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author nBd
 */
public class Trap extends Creature {
	private static Logger log = LoggerFactory.getLogger(Trap.class.getName());

	protected static final int TICK = 1000; // 1s

	private boolean isTriggered;
	private final Skill skill;
	private final int lifeTime;
	private int timeRemaining;
	private boolean hasLifeTime;

	public Trap(int objectId, NpcTemplate template, int lifeTime, Skill skill) {
		super(objectId, template);
		setInstanceType(InstanceType.L2Trap);
		setName(template.Name);
		setIsInvul(false);

		isTriggered = false;
		this.skill = skill;
		hasLifeTime = true;
		if (lifeTime != 0) {
			this.lifeTime = lifeTime;
		} else {
			this.lifeTime = 30000;
		}
		timeRemaining = lifeTime;
		if (lifeTime < 0) {
			hasLifeTime = false;
		}

		if (skill != null) {
			ThreadPoolManager.getInstance().scheduleGeneral(new TrapTask(), TICK);
		}
	}

	/**
	 * @see Creature#getKnownList()
	 */
	@Override
	public TrapKnownList getKnownList() {
		return (TrapKnownList) super.getKnownList();
	}

	@Override
	public void initKnownList() {
		setKnownList(new TrapKnownList(this));
	}

	@Override
	public boolean isAutoAttackable(Creature attacker) {
		return !canSee(attacker);
	}

	/**
	 *
	 *
	 */
	public void stopDecay() {
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}

	/**
	 * @see Creature#onDecay()
	 */
	@Override
	public void onDecay() {
		deleteMe();
	}

	public final int getNpcId() {
		return getTemplate().NpcId;
	}

	/**
	 * @see Creature#doDie(Creature)
	 */
	@Override
	public boolean doDie(Creature killer) {
		if (!super.doDie(killer)) {
			return false;
		}

		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}

	@Override
	public void deleteMe() {
		decayMe();
		getKnownList().removeAllKnownObjects();
		super.deleteMe();
	}

	public synchronized void unSummon() {
		if (isVisible() && !isDead()) {
			if (getWorldRegion() != null) {
				getWorldRegion().removeFromZones(this);
			}

			deleteMe();
		}
	}

	/**
	 * @see Creature#getActiveWeaponInstance()
	 */
	@Override
	public Item getActiveWeaponInstance() {
		return null;
	}

	/**
	 * @see Creature#getActiveWeaponItem()
	 */
	@Override
	public WeaponTemplate getActiveWeaponItem() {
		return null;
	}

	/**
	 * @see Creature#getLevel()
	 */
	@Override
	public int getLevel() {
		return getTemplate().Level;
	}

	/**
	 * @see Creature#getTemplate()
	 */
	@Override
	public NpcTemplate getTemplate() {
		return (NpcTemplate) super.getTemplate();
	}

	/**
	 * @see Creature#getSecondaryWeaponInstance()
	 */
	@Override
	public Item getSecondaryWeaponInstance() {
		return null;
	}

	/**
	 * @see Creature#getSecondaryWeaponItem()
	 */
	@Override
	public WeaponTemplate getSecondaryWeaponItem() {
		return null;
	}

	/**
	 * @see Creature#updateAbnormalEffect()
	 */
	@Override
	public void updateAbnormalEffect() {

	}

	public Skill getSkill() {
		return skill;
	}

	public Player getOwner() {
		return null;
	}

	public int getKarma() {
		return 0;
	}

	public byte getPvpFlag() {
		return 0;
	}

	/**
	 * Checks is triggered
	 *
	 * @return True if trap is triggered.
	 */
	public boolean isTriggered() {
		return isTriggered;
	}

	/**
	 * Checks trap visibility
	 *
	 * @param cha - checked character
	 * @return True if character can see trap
	 */
	public boolean canSee(Creature cha) {
		return false;
	}

	/**
	 * Reveal trap to the detector (if possible)
	 *
	 */
	public void setDetected(Creature detector) {
		detector.sendPacket(new NpcInfo(this));
	}

	/**
	 * Check if target can trigger trap
	 *
	 */
	protected boolean checkTarget(Creature target) {
		return getOwner().isAbleToCastOnTarget(target, null, true);
	}

	private class TrapTask implements Runnable {
		@Override
		public void run() {
			try {
				if (!isTriggered) {
					if (hasLifeTime) {
						timeRemaining -= TICK;
						if (timeRemaining < lifeTime - 15000) {
							SocialAction sa = new SocialAction(getObjectId(), 2);
							broadcastPacket(sa);
						}
						if (timeRemaining < 0) {
							switch (getSkill().getTargetType()) {
								case TARGET_AURA:
								case TARGET_FRONT_AURA:
								case TARGET_BEHIND_AURA:
								case TARGET_AROUND_CASTER:
									trigger(Trap.this);
									break;
								default:
									unSummon();
							}
							return;
						}
					}

					for (Creature target : getKnownList().getKnownCharactersInRadius(skill.getSkillRadius())) {
						if (target == getOwner()) {
							continue;
						}

						if (!getOwner().isAbleToCastOnTarget(target, skill, false)) {
							continue;
						}

						trigger(target);
						return;
					}

					ThreadPoolManager.getInstance().scheduleGeneral(new TrapTask(), TICK);
				}
			} catch (Exception e) {
				log.error("", e);
				unSummon();
			}
		}
	}

	/**
	 * Trigger trap
	 *
	 */
	public void trigger(Creature target) {
		isTriggered = true;
		broadcastPacket(new NpcInfo(this));
		setTarget(target);

		if (getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION) != null) {
			for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION)) {
				quest.notifyTrapAction(this, target, TrapAction.TRAP_TRIGGERED);
			}
		}

		ThreadPoolManager.getInstance().scheduleGeneral(new TriggerTask(), 300);
	}

	private class TriggerTask implements Runnable {
		@Override
		public void run() {
			try {
				doCast(skill);
				ThreadPoolManager.getInstance().scheduleGeneral(new UnsummonTask(), skill.getHitTime() + 300);
			} catch (Exception e) {
				e.printStackTrace();
				unSummon();
			}
		}
	}

	private class UnsummonTask implements Runnable {
		@Override
		public void run() {
			unSummon();
		}
	}

	@Override
	public void sendInfo(Player activeChar) {
		if (isTriggered || canSee(activeChar)) {
			activeChar.sendPacket(new NpcInfo(this));
		}
	}

	@Override
	public void broadcastPacket(L2GameServerPacket mov) {
		Collection<Player> plrs = getKnownList().getKnownPlayers().values();
		for (Player player : plrs) {
			if (player != null && (isTriggered || canSee(player))) {
				player.sendPacket(mov);
			}
		}
	}

	@Override
	public void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist) {
		Collection<Player> plrs = getKnownList().getKnownPlayers().values();
		for (Player player : plrs) {
			if (player == null) {
				continue;
			}
			if (isInsideRadius(player, radiusInKnownlist, false, false)) {
				if (isTriggered || canSee(player)) {
					player.sendPacket(mov);
				}
			}
		}
	}
}
