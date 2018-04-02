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

import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Trap;
import l2server.gameserver.model.olympiad.OlympiadGameManager;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.Quest.TrapAction;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.chars.NpcTemplate;

import java.util.ArrayList;
import java.util.List;

public class TrapInstance extends Trap {
	private Player owner;
	private int level;
	private boolean isInArena = false;
	private final List<Integer> playersWhoDetectedMe = new ArrayList<>();

	/**
	 * @param objectId
	 * @param template
	 * @param owner
	 */
	public TrapInstance(int objectId, NpcTemplate template, Player owner, int lifeTime, Skill skill) {
		super(objectId, template, lifeTime, skill);
		setInstanceType(InstanceType.L2TrapInstance);

		setInstanceId(owner.getInstanceId());

		this.owner = owner;
		level = owner.getLevel();
	}

	public TrapInstance(int objectId, NpcTemplate template, int instanceId, int lifeTime, Skill skill) {
		super(objectId, template, lifeTime, skill);
		setInstanceType(InstanceType.L2TrapInstance);

		setInstanceId(instanceId);

		owner = null;
		if (skill != null) {
			level = skill.getLevelHash();
		} else {
			level = 1;
		}
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public Player getOwner() {
		return owner;
	}

	@Override
	public Player getActingPlayer() {
		return owner;
	}

	@Override
	public void onSpawn() {
		super.onSpawn();
		isInArena = isInsideZone(ZONE_PVP) && !isInsideZone(ZONE_SIEGE);
		playersWhoDetectedMe.clear();
	}

	@Override
	public void deleteMe() {
		if (owner != null) {
			owner.setTrap(null);
			owner = null;
		}
		super.deleteMe();
	}

	@Override
	public void unSummon() {
		if (owner != null) {
			owner.setTrap(null);
			owner = null;
		}
		super.unSummon();
	}

	@Override
	public int getKarma() {
		return owner != null ? owner.getReputation() : 0;
	}

	@Override
	public byte getPvpFlag() {
		return owner != null ? owner.getPvpFlag() : 0;
	}

	@Override
	public void sendDamageMessage(Creature target, int damage, boolean mcrit, boolean pcrit, boolean miss) {
		if (miss || owner == null) {
			return;
		}

		if (owner.isInOlympiadMode() && target instanceof Player && ((Player) target).isInOlympiadMode() &&
				((Player) target).getOlympiadGameId() == owner.getOlympiadGameId()) {
			OlympiadGameManager.getInstance().notifyCompetitorDamage(getOwner(), damage);
		}

		final SystemMessage sm;

		if (target.isInvul(owner) && !(target instanceof NpcInstance)) {
			sm = SystemMessage.getSystemMessage(SystemMessageId.ATTACK_WAS_BLOCKED);
		} else {
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_GAVE_C2_DAMAGE_OF_S3);
			sm.addCharName(this);
			sm.addCharName(target);
			sm.addNumber(damage);
		}

		owner.sendPacket(sm);
	}

	@Override
	public boolean canSee(Creature cha) {
		if (cha != null && playersWhoDetectedMe.contains(cha.getObjectId())) {
			return true;
		}

		if (owner == null || cha == null) {
			return false;
		}
		if (cha == owner) {
			return true;
		}

		if (cha instanceof Player) {
			// observers can't see trap
			if (((Player) cha).inObserverMode()) {
				return false;
			}

			// olympiad competitors can't see trap
			if (owner.isInOlympiadMode() && ((Player) cha).isInOlympiadMode() &&
					((Player) cha).getOlympiadSide() != owner.getOlympiadSide()) {
				return false;
			}
		}

		if (isInArena) {
			return true;
		}

		return owner.isInParty() && cha.isInParty() && owner.getParty().getPartyLeaderOID() == cha.getParty().getPartyLeaderOID();
	}

	@Override
	public void setDetected(Creature detector) {
		if (isInArena) {
			super.setDetected(detector);
			return;
		}
		if (owner != null && owner.getPvpFlag() == 0 && owner.getReputation() == 0) {
			return;
		}

		playersWhoDetectedMe.add(detector.getObjectId());
		if (getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION) != null) {
			for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION)) {
				quest.notifyTrapAction(this, detector, TrapAction.TRAP_DETECTED);
			}
		}
		super.setDetected(detector);
	}

	@Override
	protected boolean checkTarget(Creature target) {
		if (!Skill.checkForAreaOffensiveSkills(this, target, getSkill(), isInArena)) {
			return false;
		}

		// observers
		if (target instanceof Player && ((Player) target).inObserverMode()) {
			return false;
		}

		// olympiad own team and their summons not attacked
		if (owner != null && owner.isInOlympiadMode()) {
			final Player player = target.getActingPlayer();
			if (player != null && player.isInOlympiadMode() && player.getOlympiadSide() == owner.getOlympiadSide()) {
				return false;
			}
		}

		if (isInArena) {
			return true;
		}

		// trap owned by players not attack non-flagged players
		if (owner != null) {
			final Player player = target.getActingPlayer();
			if (target instanceof Attackable) {
				return true;
			}
			if (player == null || player.getPvpFlag() == 0 && player.getReputation() == 0) {
				return false;
			}
		}

		return true;
	}
}
