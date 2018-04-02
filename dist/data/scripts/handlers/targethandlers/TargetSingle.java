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

package handlers.targethandlers;

import l2server.gameserver.handler.ISkillTargetTypeHandler;
import l2server.gameserver.handler.SkillTargetTypeHandler;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.TrapInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.SkillTargetDirection;
import l2server.gameserver.templates.skills.SkillTargetType;

/**
 * Used by all skills that affects a single target.
 *
 * @author Rewritten by ZaKaX.
 * @author Unhardcodded by nBd.
 */
public class TargetSingle implements ISkillTargetTypeHandler {
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		final Player aPlayer = activeChar.getActingPlayer();

		// Traps cant hit rabbits!
		if (activeChar instanceof TrapInstance && target instanceof MonsterInstance) {
			final MonsterInstance monster = (MonsterInstance) target;

			if (monster.getNpcId() == 155001) {
				return null;
			}
		}

		if (skill.getTargetDirection() == SkillTargetDirection.ALL_SUMMONS && aPlayer.getSummon(0) != null && !(target instanceof Summon)) {
			target = aPlayer.getSummon(0);
		}

		if (aPlayer != null &&
				(!isReachableTarget(aPlayer, target, skill.getTargetDirection()) || !aPlayer.isAbleToCastOnTarget(target, skill, false)) ||
				activeChar == target && !skill.isUseableOnSelf()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return null;
		}

		return new Creature[]{target};
	}

	/**
	 * In addition to the usual checks, here we're going to check for specific skill target direction abilities.
	 * For example if the target direction of the skill is set to Dead Players, we'll check here if the target is a player, and if it's dead.
	 * Returning false will result in the target being unreachable. "Incorrect target" is sent if the attack wasn't massive, otherwise it's just skipped.
	 */
	private final boolean isReachableTarget(final Player activeChar, final Creature target, SkillTargetDirection targetDirection) {
		if (target == null) {
			return false;
		}

		switch (targetDirection) {
			case DEAD_CLAN_MEMBER: {
				if (target instanceof Playable) {
					final Player tPlayer = target.getActingPlayer();
					if (tPlayer.isDead() && activeChar.isInSameClan(tPlayer)) {
						return true;
					}
				}
				break;
			}
			case DEAD_ALLY_MEMBER: {
				if (target instanceof Playable) {
					final Player tPlayer = target.getActingPlayer();
					if (tPlayer.isDead() && activeChar.isInSameAlly(tPlayer)) {
						return true;
					}
				}
				break;
			}
			case DEAD_PET: {
				if (target instanceof Summon) {
					final Summon sTarget = (Summon) target;
					if (sTarget.isDead()) {
						return true;
					}
				}
				break;
			}
			case DEAD_PLAYABLE: {
				if (target instanceof Player || target instanceof PetInstance) {
					if (target.isDead()) {
						return true;
					}
				}
				break;
			}
			case DEAD_MONSTER: {
				if (target instanceof MonsterInstance) {
					final MonsterInstance mTarget = (MonsterInstance) target;
					if (mTarget.isDead()) {
						return true;
					}
				}
				break;
			}
			case MONSTERS: {
				if (target instanceof MonsterInstance) {
					final MonsterInstance mTarget = (MonsterInstance) target;
					if (!mTarget.isDead()) {
						return true;
					}
				}
				break;
			}
			case UNDEAD: {
				if (target instanceof MonsterInstance) {
					final MonsterInstance mTarget = (MonsterInstance) target;
					if (!mTarget.isDead() && mTarget.isUndead()) {
						return true;
					}
				}
				break;
			}
			case ENNEMY_SUMMON: {
				if (target instanceof Summon) {
					final Summon sTarget = (Summon) target;
					final Player sOwner = sTarget.getOwner();
					if (!sTarget.isDead() && sOwner != null && activeChar != sOwner) {
						return true;
					}
				}
				break;
			}
			case ALL_SUMMONS: {
				if (target instanceof Summon) {
					final Summon sTarget = (Summon) target;
					if (!sTarget.isDead() && sTarget.getOwner() == activeChar) {
						return true;
					}
				}
				break;
			}
			case ONE_NOT_SUMMONS: {
				if (!(target instanceof Summon) && !target.isDead()) {
					return true;
				}
				break;
			}
			case PARTY_ONE: {
				if (target instanceof Playable) {
					final Player tPlayer = target.getActingPlayer();
					if (activeChar == tPlayer || activeChar.isInSameParty(tPlayer)) {
						return true;
					}
				}
				break;
			}
			case PARTY_ONE_NOTME: {
				if (target instanceof Player) {
					final Player tPlayer = (Player) target;
					if (activeChar != tPlayer && activeChar.isInSameParty(tPlayer)) {
						return true;
					}
				}
				break;
			}
			case PLAYER: {
				if (target instanceof Playable) {
					final Player tPlayer = (Player) target;
					if (activeChar != tPlayer) {
						return true;
					}
				}
			}
			case DEFAULT: {
				if (!target.isDead()) {
					return true;
				}

				break;
			}
			default:
		}

		return false;
	}

	@Override
	public Enum<SkillTargetType> getTargetType() {
		return SkillTargetType.TARGET_SINGLE;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetSingle());
	}
}
