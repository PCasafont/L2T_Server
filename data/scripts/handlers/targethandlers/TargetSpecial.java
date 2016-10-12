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

import l2server.Config;
import l2server.gameserver.GeoEngine;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.handler.ISkillTargetTypeHandler;
import l2server.gameserver.handler.SkillTargetTypeHandler;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2WorldRegion;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2TrapInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.L2SkillTargetDirection;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Used by skills that has unique target abilities.
 *
 * @author ZaKaX.
 * @author Unhardcodded by nBd.
 */
public class TargetSpecial implements ISkillTargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		final L2PcInstance aPlayer = activeChar.getActingPlayer();

		if (!skill.isUseableWithoutTarget() && target == null)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return null;
		}

		final ArrayList<L2Character> result = new ArrayList<L2Character>();

		final L2Summon aPet = aPlayer.getPet();
		if (aPet != null && !aPet.isDead())
		{
			result.add(aPet);
		}

		if (skill.getTargetDirection() == L2SkillTargetDirection.SUBLIMES)
		{
			result.add(activeChar);
			for (L2Character o : aPlayer.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius()))
			{
				if (!GeoEngine.getInstance().canSeeTarget(aPlayer, o))
				{
					continue;
				}

				final L2PcInstance kTarget = o.getActingPlayer();
				if (kTarget != null)
				{
					if (kTarget == aPlayer)
					{
						continue;
					}

					final L2Summon kPet = kTarget.getPet();
					if (kPet != null)
					{
						if (!isReacheableBySublime(aPlayer, kPet))
						{
							continue;
						}

						result.add(kPet);
					}
				}

				if (isReacheableBySublime(aPlayer, o))
				{
					result.add(o);
				}
			}
		}
		else if (skill.getTargetDirection() == L2SkillTargetDirection.CHAIN_HEAL)
		{
			if (activeChar.getTarget() instanceof L2PcInstance)
			{
				final L2PcInstance targetedCharacter = (L2PcInstance) activeChar.getTarget();

				if (!isReachableByChainHeal(aPlayer, targetedCharacter))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
					return null;
				}

				Collection<L2Character> knownCharacters =
						targetedCharacter.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius());

				for (L2Character o : knownCharacters)
				{
					//activeChar.sendMessage("Checking " + o);
					//if (!GeoEngine.getInstance().canSeeTarget(target, o))
					//	continue;

					final L2PcInstance kTarget = o.getActingPlayer();
					if (kTarget != null)
					{
						if (kTarget == aPlayer)
						{
							continue;
						}

						final L2Summon kPet = kTarget.getPet();
						if (kPet != null)
						{
							if (!isReachableByChainHeal(aPlayer, kPet))
							{
								continue;
							}

							result.add(kPet);
						}
					}

					if (isReachableByChainHeal(targetedCharacter, o))
					{
						result.add(o);
						//activeChar.sendMessage("Adding " + o);
					}
					//else
					//	activeChar.sendMessage(o + " is not reachable");

					if (result.size() > 9)
					{
						break;
					}
				}

				result.add(targetedCharacter);

				if (Util.checkIfInRange(400, aPlayer, targetedCharacter, false))
				{
					result.add(aPlayer);
				}

				return result.toArray(new L2Character[result.size()]);
			}
		}
		else if (skill.getTargetDirection() == L2SkillTargetDirection.PARTY_ANYWHERE)
		{
			if (aPlayer.getParty() != null)
			{
				// FIXME return activeChar.getParty().getPartyMembers();
			}
		}
		else if (skill.getTargetDirection() == L2SkillTargetDirection.INVISIBLE_TRAP)
		{
			final L2WorldRegion region = activeChar.getWorldRegion();
			if (region != null)
			{
				for (final L2Object obj : region.getVisibleObjects().values())
				{
					if (obj instanceof L2TrapInstance &&
							activeChar.isInsideRadius(obj.getX(), obj.getY(), skill.getSkillRadius(), false))
					{
						final L2TrapInstance dTrap = (L2TrapInstance) obj;

						if (dTrap.getOwner() == activeChar /*|| dTrap.isDetected() FIXME */)
						{
							continue;
						}

						result.add(dTrap);
					}
				}
			}
		}

		return result.toArray(new L2Character[result.size()]);
	}

	private final boolean isReacheableBySublime(final L2PcInstance activeChar, final L2Character target)
	{
		if (target.isDead())
		{
			return false;
		}

		if (target instanceof L2Playable)
		{
			final L2PcInstance pTarget = target.getActingPlayer();

			// FIXME if (pTarget.isInvisible())
			//	return false;

			if (activeChar.getDuelId() != 0)
			{
				if (activeChar.getDuelId() != pTarget.getDuelId())
				{
					return false;
				}
			}
			else if (pTarget.getDuelId() != 0)
			{
				return false;
			}
			if (activeChar.isInSameParty(pTarget) || activeChar.isInSameChannel(pTarget) ||
					activeChar.isInSameClan(pTarget) || activeChar.isInSameAlly(pTarget))
			{
				return true;
			}
			if (pTarget.isAvailableForCombat() || pTarget.isInsidePvpZone() || activeChar.isInSameClanWar(pTarget))
			{
				return false;
			}
			if (target.isInsideZone(L2Character.ZONE_TOWN))
			{
				return true;
			}
		}
		else
		{
			return false;
		}

		return true;
	}

	private final boolean isReachableByChainHeal(final L2PcInstance activeChar, final L2Character target)
	{
		if (target.isDead())
		{
			return false;
		}

		if (target instanceof L2Playable)
		{
			final L2PcInstance player = target.getActingPlayer();

			if (player == activeChar)
			{
				return true;
			}
		}

		if (activeChar.isInOlympiadMode())
		{
			return false;
		}

		if (target instanceof L2Playable)
		{
			final L2PcInstance pTarget = target.getActingPlayer();

			if (pTarget.isInOlympiadMode())
			{
				return false;
			}

			if (Config.isServer(Config.TENKAI))
			{
				if (activeChar.isPlayingEvent())
				{
					EventInstance event = activeChar.getEvent();
					if (event != pTarget.getEvent())
					{
						return false;
					}

					if (event.getConfig().isAllVsAll())
					{
						return false;
					}

					if (event.getParticipantTeam(activeChar.getObjectId()) !=
							event.getParticipantTeam(target.getObjectId()))
					{
						return false;
					}

					return true;
				}
			}

			if (activeChar.getDuelId() != 0)
			{
				if (activeChar.getDuelId() != pTarget.getDuelId())
				{
					return false;
				}
			}
			else if (pTarget.getDuelId() != 0)
			{
				return false;
			}

			if (activeChar.isInSameParty(pTarget) || activeChar.isInSameChannel(pTarget) ||
					activeChar.isInSameClan(pTarget) || activeChar.isInSameAlly(pTarget))
			{
				return true;
			}

			//if (pTarget.getPvpFlag() == 0)
			//	return true;
		}

		return false;
	}

	@Override
	public Enum<L2SkillTargetType> getTargetType()
	{
		return L2SkillTargetType.TARGET_SPECIAL;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetSpecial());
	}
}
