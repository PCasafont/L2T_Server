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
import l2server.gameserver.handler.ISkillTargetTypeHandler;
import l2server.gameserver.handler.SkillTargetTypeHandler;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author nBd
 */
public class TargetBehindAura implements ISkillTargetTypeHandler
{
	/**
	 */
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<L2Character>();

		int radius = skill.getSkillRadius();

		boolean srcInArena =
				activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE);

		L2PcInstance src = activeChar.getActingPlayer();

		// Go through the L2Character _knownList
		Collection<L2Object> objs = activeChar.getKnownList().getKnownObjects().values();
		//synchronized (activeChar.getKnownList().getKnownObjects())
		{
			for (L2Object obj : objs)
			{
				if (obj instanceof L2Attackable || obj instanceof L2Playable)
				{
					// Don't add this target if this is a Pc->Pc pvp
					// casting and pvp condition not met
					if (obj == activeChar || obj == src || ((L2Character) obj).isDead())
					{
						continue;
					}

					if (src != null)
					{
						if (!((L2Character) obj).isBehind(activeChar))
						{
							continue;
						}

						if (!GeoEngine.getInstance().canSeeTarget(activeChar, obj))
						{
							continue;
						}

						// check if both attacker and target are
						// L2PcInstances and if they are in same party
						if (obj instanceof L2PcInstance)
						{
							@SuppressWarnings("unused") L2PcInstance trg = (L2PcInstance) obj;

							if (!src.checkPvpSkill(obj, skill))
							{
								continue;
							}

							if (src.getParty() != null && ((L2PcInstance) obj).getParty() != null &&
									src.getParty().getPartyLeaderOID() ==
											((L2PcInstance) obj).getParty().getPartyLeaderOID())
							{
								continue;
							}

							if (!srcInArena && !(((L2Character) obj).isInsideZone(L2Character.ZONE_PVP) &&
									!((L2Character) obj).isInsideZone(L2Character.ZONE_SIEGE)))
							{
								if (src.getAllyId() == ((L2PcInstance) obj).getAllyId() && src.getAllyId() != 0)
								{
									continue;
								}

								if (src.getClanId() != 0 && src.getClanId() == ((L2PcInstance) obj).getClanId())
								{
									continue;
								}
							}
						}
						if (obj instanceof L2Summon)
						{
							L2PcInstance trg = ((L2Summon) obj).getOwner();

							if (trg == src)
							{
								continue;
							}

							if (!src.checkPvpSkill(trg, skill))
							{
								continue;
							}

							if (src.getParty() != null && trg.getParty() != null &&
									src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
							{
								continue;
							}

							if (!srcInArena && !(((L2Character) obj).isInsideZone(L2Character.ZONE_PVP) &&
									!((L2Character) obj).isInsideZone(L2Character.ZONE_SIEGE)))
							{
								if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
								{
									continue;
								}

								if (src.getClanId() != 0 && src.getClanId() == trg.getClanId())
								{
									continue;
								}
							}
						}
					}
					else
					// Skill user is not L2PlayableInstance
					{
						if (!(obj instanceof L2Playable) // Target is not L2PlayableInstance
								&& !activeChar.isConfused()) // and caster not confused (?)
						{
							continue;
						}
					}
					if (!Util.checkIfInRange(radius, activeChar, obj, true))
					{
						continue;
					}

					if (onlyFirst == false)
					{
						targetList.add((L2Character) obj);
					}
					else
					{
						return new L2Character[]{(L2Character) obj};
					}
				}
			}
		}
		return targetList.toArray(new L2Character[targetList.size()]);
	}

	/**
	 */
	@Override
	public Enum<L2SkillTargetType> getTargetType()
	{
		// TODO Auto-generated method stub
		return L2SkillTargetType.TARGET_BEHIND_AURA;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetBehindAura());
	}
}
