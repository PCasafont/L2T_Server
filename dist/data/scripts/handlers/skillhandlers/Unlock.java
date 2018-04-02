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

package handlers.skillhandlers;

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.ChestInstance;
import l2server.gameserver.model.actor.instance.DoorInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SocialAction;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.SkillType;
import l2server.util.Rnd;

public class Unlock implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.UNLOCK, SkillType.UNLOCK_SPECIAL};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		WorldObject[] targetList = skill.getTargetList(activeChar);

		if (targetList == null) {
			return;
		}

		for (WorldObject target : targets) {
			if (target instanceof DoorInstance) {
				DoorInstance door = (DoorInstance) target;
				// Check if door in the different instance
				if (activeChar.getInstanceId() != door.getInstanceId()) {
					// Search for the instance
					final Instance inst = InstanceManager.getInstance().getInstance(activeChar.getInstanceId());
					if (inst == null) {
						// Instance not found
						activeChar.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					for (DoorInstance instanceDoor : inst.getDoors()) {
						if (instanceDoor.getDoorId() == door.getDoorId()) {
							// Door found
							door = instanceDoor;
							break;
						}
					}
					// Checking instance again
					if (activeChar.getInstanceId() != door.getInstanceId()) {
						activeChar.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}

				if (!door.isOpenableBySkill() && skill.getSkillType() != SkillType.UNLOCK_SPECIAL || door.getFort() != null) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.UNABLE_TO_UNLOCK_DOOR));
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}

				if (doorUnlock(skill) && !door.getOpen()) {
					door.openMe();
					//if (skill.getAfterEffectId() == 0)
					//door.onOpen();
				} else {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_UNLOCK_DOOR));
				}
			} else if (target instanceof ChestInstance) {
				ChestInstance chest = (ChestInstance) target;
				if (chest.getCurrentHp() <= 0 || chest.isInteracted() || activeChar.getInstanceId() != chest.getInstanceId()) {
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				} else {
					chest.setInteracted();
					if (chestUnlock(skill, chest)) {
						activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
						chest.setSpecialDrop();
						chest.setMustRewardExpSp(false);
						chest.reduceCurrentHp(99999999, activeChar, skill);
					} else {
						activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
						chest.addDamageHate(activeChar, 0, 1);
						chest.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
						if (chestTrap(chest)) {
							chest.chestTrap(activeChar);
						}
					}
				}
			}
		}
	}

	private static boolean doorUnlock(Skill skill) {
		if (skill.getSkillType() == SkillType.UNLOCK_SPECIAL) {
			return Rnd.get(100) < skill.getPower();
		}

		switch (skill.getLevel()) {
			case 0:
				return false;
			case 1:
				return Rnd.get(120) < 30;
			case 2:
				return Rnd.get(120) < 50;
			case 3:
				return Rnd.get(120) < 75;
			default:
				return Rnd.get(120) < 100;
		}
	}

	private static boolean chestUnlock(Skill skill, Creature chest) {
		int chance = 0;
		if (chest.getLevel() > 60) {
			if (skill.getLevel() < 10) {
				return false;
			}

			chance = (skill.getLevel() - 10) * 5 + 30;
		} else if (chest.getLevel() > 40) {
			if (skill.getLevel() < 6) {
				return false;
			}

			chance = (skill.getLevel() - 6) * 5 + 10;
		} else if (chest.getLevel() > 30) {
			if (skill.getLevel() < 3) {
				return false;
			}
			if (skill.getLevel() > 12) {
				return true;
			}

			chance = (skill.getLevel() - 3) * 5 + 30;
		} else {
			if (skill.getLevel() > 10) {
				return true;
			}

			chance = skill.getLevel() * 5 + 35;
		}

		chance = Math.min(chance, 50);
		return Rnd.get(100) < chance;
	}

	private static boolean chestTrap(Creature chest) {
		if (chest.getLevel() > 60) {
			return Rnd.get(100) < 80;
		}
		if (chest.getLevel() > 40) {
			return Rnd.get(100) < 50;
		}
		if (chest.getLevel() > 30) {
			return Rnd.get(100) < 30;
		}
		return Rnd.get(100) < 10;
	}

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
