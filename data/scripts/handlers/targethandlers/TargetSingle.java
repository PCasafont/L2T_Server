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
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2TrapInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.L2SkillTargetDirection;
import l2server.gameserver.templates.skills.L2SkillTargetType;

/**
 * Used by all skills that affects a single target.
 *
 * @author Rewritten by ZaKaX.
 * @author Unhardcodded by nBd.
 */
public class TargetSingle implements ISkillTargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		final L2PcInstance aPlayer = activeChar.getActingPlayer();

		// Traps cant hit rabbits!
		if (activeChar instanceof L2TrapInstance && target instanceof L2MonsterInstance)
		{
			final L2MonsterInstance monster = (L2MonsterInstance) target;

			if (monster.getNpcId() == 155001)
			{
				return null;
			}
		}

		if (skill.getTargetDirection() == L2SkillTargetDirection.ALL_SUMMONS && aPlayer.getSummon(0) != null &&
				!(target instanceof L2Summon))
		{
			target = aPlayer.getSummon(0);
		}

		if (aPlayer != null && (!isReachableTarget(aPlayer, target, skill.getTargetDirection()) ||
				!aPlayer.isAbleToCastOnTarget(target, skill, false)) ||
				activeChar == target && !skill.isUseableOnSelf())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return null;
		}

		return new L2Character[]{target};
	}

	/**
	 * In addition to the usual checks, here we're going to check for specific skill target direction abilities.
	 * For example if the target direction of the skill is set to Dead Players, we'll check here if the target is a player, and if it's dead.
	 * Returning false will result in the target being unreachable. "Incorrect target" is sent if the attack wasn't massive, otherwise it's just skipped.
	 */
	private final boolean isReachableTarget(final L2PcInstance activeChar, final L2Character target, L2SkillTargetDirection targetDirection)
	{
		if (target == null)
		{
			return false;
		}

		switch (targetDirection)
		{
			case DEAD_CLAN_MEMBER:
			{
				if (target instanceof L2Playable)
				{
					final L2PcInstance tPlayer = target.getActingPlayer();
					if (tPlayer.isDead() && activeChar.isInSameClan(tPlayer))
					{
						return true;
					}
				}
				break;
			}
			case DEAD_ALLY_MEMBER:
			{
				if (target instanceof L2Playable)
				{
					final L2PcInstance tPlayer = target.getActingPlayer();
					if (tPlayer.isDead() && activeChar.isInSameAlly(tPlayer))
					{
						return true;
					}
				}
				break;
			}
			case DEAD_PET:
			{
				if (target instanceof L2Summon)
				{
					final L2Summon sTarget = (L2Summon) target;
					if (sTarget.isDead())
					{
						return true;
					}
				}
				break;
			}
			case DEAD_PLAYABLE:
			{
				if (target instanceof L2PcInstance || target instanceof L2PetInstance)
				{
					if (target.isDead())
					{
						return true;
					}
				}
				break;
			}
			case DEAD_MONSTER:
			{
				if (target instanceof L2MonsterInstance)
				{
					final L2MonsterInstance mTarget = (L2MonsterInstance) target;
					if (mTarget.isDead())
					{
						return true;
					}
				}
				break;
			}
			case MONSTERS:
			{
				if (target instanceof L2MonsterInstance)
				{
					final L2MonsterInstance mTarget = (L2MonsterInstance) target;
					if (!mTarget.isDead())
					{
						return true;
					}
				}
				break;
			}
			case UNDEAD:
			{
				if (target instanceof L2MonsterInstance)
				{
					final L2MonsterInstance mTarget = (L2MonsterInstance) target;
					if (!mTarget.isDead() && mTarget.isUndead())
					{
						return true;
					}
				}
				break;
			}
			case ENNEMY_SUMMON:
			{
				if (target instanceof L2Summon)
				{
					final L2Summon sTarget = (L2Summon) target;
					final L2PcInstance sOwner = sTarget.getOwner();
					if (!sTarget.isDead() && sOwner != null && activeChar != sOwner)
					{
						return true;
					}
				}
				break;
			}
			case ALL_SUMMONS:
			{
				if (target instanceof L2Summon)
				{
					final L2Summon sTarget = (L2Summon) target;
					if (!sTarget.isDead() && sTarget.getOwner() == activeChar)
					{
						return true;
					}
				}
				break;
			}
			case ONE_NOT_SUMMONS:
			{
				if (!(target instanceof L2Summon) && !target.isDead())
				{
					return true;
				}
				break;
			}
			case PARTY_ONE:
			{
				if (target instanceof L2Playable)
				{
					final L2PcInstance tPlayer = target.getActingPlayer();
					if (activeChar == tPlayer || activeChar.isInSameParty(tPlayer))
					{
						return true;
					}
				}
				break;
			}
			case PARTY_ONE_NOTME:
			{
				if (target instanceof L2PcInstance)
				{
					final L2PcInstance tPlayer = (L2PcInstance) target;
					if (activeChar != tPlayer && activeChar.isInSameParty(tPlayer))
					{
						return true;
					}
				}
				break;
			}
			case PLAYER:
			{
				if (target instanceof L2Playable)
				{
					final L2PcInstance tPlayer = (L2PcInstance) target;
					if (activeChar != tPlayer)
					{
						return true;
					}
				}
			}
			case DEFAULT:
			{
				if (!target.isDead())
				{
					return true;
				}

				break;
			}
			default:
		}

		return false;
	}

	@Override
	public Enum<L2SkillTargetType> getTargetType()
	{
		return L2SkillTargetType.TARGET_SINGLE;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetSingle());
	}
}
