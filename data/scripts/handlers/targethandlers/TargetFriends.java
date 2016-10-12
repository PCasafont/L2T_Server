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

import l2server.gameserver.GeoEngine;
import l2server.gameserver.events.instanced.EventTeam;
import l2server.gameserver.handler.ISkillTargetTypeHandler;
import l2server.gameserver.handler.SkillTargetTypeHandler;
import l2server.gameserver.model.L2ClanMember;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.L2SkillTargetDirection;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.templates.skills.L2SkillType;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Used by all skills that affects friendly players.
 *
 * @author Rewritten by ZaKaX.
 * @author Unhardcodded by nBd.
 */
public class TargetFriends implements ISkillTargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		if (skill.isUseableWithoutTarget())
		{
			final ArrayList<L2Character> result = new ArrayList<L2Character>();

			if (activeChar instanceof L2Playable)
			{
				final L2PcInstance aPlayer = activeChar.getActingPlayer();

				// Friendly targets for players...
				if (skill.getTargetDirection() != L2SkillTargetDirection.PARTY_ALL_NOTME)
				{
					result.add(aPlayer);
				}

				final L2Summon aPet = aPlayer.getPet();
				if (aPet != null && !aPet.isDead())
				{
					result.add(aPet);
				}

				for (L2SummonInstance summon : aPlayer.getSummons())
				{
					if (summon.isDead())
					{
						continue;
					}

					result.add(summon);
				}

				if (aPlayer.isInOlympiadMode())
				{
					return result.toArray(new L2Character[result.size()]);
				}

				Collection<L2Character> candidates;
				if (skill.getSkillRadius() > 0)
				{
					candidates = aPlayer.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius());
				}
				else
				{
					candidates = new ArrayList<L2Character>();
					if (aPlayer.isInParty())
					{
						candidates.addAll(aPlayer.getParty().getPartyMembers());
					}
					if (aPlayer.getClan() != null)
					{
						for (L2ClanMember member : aPlayer.getClan().getMembers())
						{
							candidates.add(member.getPlayerInstance());
						}
					}
				}

				for (L2Character obj : candidates)
				{
					if (obj == null)
					{
						continue;
					}

					final L2PcInstance kTarget = obj.getActingPlayer();
					if (kTarget == null || aPlayer == kTarget)
					{
						continue;
					}

					else if (!aPlayer.isAbleToCastOnTarget(kTarget, skill, true))
					{
						continue;
					}

					if (aPlayer.isInOlympiadMode())
					{
						continue;
					}

					if (aPlayer.isPlayingEvent())
					{
						EventTeam playerTeam = aPlayer.getEvent().getParticipantTeam(aPlayer.getObjectId());
						EventTeam targetTeam = aPlayer.getEvent().getParticipantTeam(aPlayer.getObjectId());
						if (playerTeam != targetTeam)
						{
							continue;
						}
					}

					if (skill.getTargetDirection() == L2SkillTargetDirection.PARTY_ALL)
					{
						if (!aPlayer.isInSameParty(kTarget))
						{
							continue;
						}

						// We need the check for cases where player actually isn't in a party.
						//if (result.size() >= 9)
						//	break;
					}
					else if (skill.getTargetDirection() == L2SkillTargetDirection.PARTY_ALL_NOTME)
					{
						if (aPlayer == kTarget || !aPlayer.isInSameParty(kTarget))
						{
							continue;
						}
					}
					else if (skill.getTargetDirection() == L2SkillTargetDirection.PARTY_AND_CLAN)
					{
						if (!aPlayer.isInSameParty(kTarget) && !aPlayer.isInSameClan(kTarget))
						{
							continue;
						}
					}
					else if (skill.getTargetDirection() == L2SkillTargetDirection.CLAN)
					{
						if (!aPlayer.isInSameClan(kTarget))
						{
							continue;
						}
					}
					else if (skill.getTargetDirection() == L2SkillTargetDirection.DEAD_PARTY_MEMBER)
					{
						if (!aPlayer.isInSameParty(kTarget))
						{
							continue;
						}
					}
					else if (skill.getTargetDirection() == L2SkillTargetDirection.DEAD_CLAN_MEMBER)
					{
						if (!aPlayer.isInSameClan(kTarget))
						{
							continue;
						}
					}
					else if (skill.getTargetDirection() == L2SkillTargetDirection.DEAD_PARTY_AND_CLAN_MEMBER)
					{
						if (!aPlayer.isInSameClan(kTarget))
						{
							continue;
						}
					}
					else if (skill.getTargetDirection() == L2SkillTargetDirection.ALLIANCE)
					{
						if (!aPlayer.isInSameAlly(kTarget))
						{
							continue;
						}
					}
					if (!GeoEngine.getInstance().canSeeTarget(aPlayer, kTarget))
					{
						continue;
					}

					result.add(kTarget);
					final L2Summon kPet = kTarget.getPet();
					if (kPet != null && !kPet.isDead())
					{
						result.add(kPet);
					}

					for (L2SummonInstance summon : kTarget.getSummons())
					{
						if (summon.isDead())
						{
							continue;
						}

						result.add(summon);
					}
				}
			}
			else if (activeChar instanceof L2MonsterInstance)
			{
				final L2MonsterInstance aMonster = (L2MonsterInstance) activeChar;

				for (L2Character obj : aMonster.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius()))
				{
					if (!(obj instanceof L2MonsterInstance))
					{
						continue;
					}

					final L2MonsterInstance kMonster = (L2MonsterInstance) obj;

					if (aMonster == kMonster)
					{
						continue;
					}
					else if (aMonster.getFactionId() == null || kMonster.getFactionId() == null)
					{
						continue;
					}
					else if (!aMonster.getFactionId().equals(kMonster.getFactionId()))
					{
						continue;
					}

					result.add(kMonster);
				}
			}

			return result.toArray(new L2Character[result.size()]);
		}
		else
		{
			if (target != null && (GeoEngine.getInstance().canSeeTarget(activeChar, target) ||
					skill.getSkillType() == L2SkillType.SUMMON_FRIEND))
			{
				if (activeChar instanceof L2MonsterInstance)
				{
					final L2MonsterInstance aMonster = (L2MonsterInstance) activeChar;

					if (target instanceof L2MonsterInstance)
					{
						final L2MonsterInstance mTarget = (L2MonsterInstance) target;

						if (aMonster.getFactionId() != null && mTarget.getFactionId() != null &&
								aMonster.getFactionId().equals(mTarget.getFactionId()))
						{
							return new L2Character[]{mTarget};
						}
					}
				}
				else if (activeChar instanceof L2Playable)
				{
					final L2PcInstance aPlayer = activeChar.getActingPlayer();
					if (target instanceof L2Playable)
					{
						final L2PcInstance tPlayer = target.getActingPlayer();

						if (skill.getTargetDirection() == L2SkillTargetDirection.PARTY_ONE)
						{
							if (aPlayer == tPlayer || aPlayer.isInSameParty(tPlayer))
							{
								return new L2Character[]{target};
							}
						}
						else if (skill.getTargetDirection() == L2SkillTargetDirection.PARTY_AND_CLAN)
						{
							if (aPlayer == tPlayer || aPlayer.isInSameParty(tPlayer) || aPlayer.isInSameClan(tPlayer))
							{
								return new L2Character[]{target};
							}
						}
						else if (skill.getTargetDirection() == L2SkillTargetDirection.PARTY_ONE_NOTME)
						{
							if (aPlayer != tPlayer && aPlayer.isInSameParty(tPlayer))
							{
								return new L2Character[]{target};
							}
						}
					}
				}
			}

			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return null;
		}
	}

	@Override
	public Enum<L2SkillTargetType> getTargetType()
	{
		return L2SkillTargetType.TARGET_FRIENDS;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetFriends());
	}
}
