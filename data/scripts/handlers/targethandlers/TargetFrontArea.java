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
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author One
 */
public class TargetFrontArea implements ISkillTargetTypeHandler
{
	public TargetFrontArea()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<L2Character>();

		if (!(target instanceof L2Attackable || target instanceof L2Playable) ||
				// Target is not L2Attackable or L2PlayableInstance
				skill.getCastRange() >= 0 && (target == null || target == activeChar ||
						target.isAlikeDead())) // target is null or self or dead/faking
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return null;
		}

		L2Character cha;

		if (skill.getCastRange() >= 0)
		{
			cha = target;

			if (!onlyFirst)
			{
				targetList.add(cha); // Add target to target list
			}
			else
			{
				return new L2Character[]{cha};
			}
		}
		else
		{
			cha = activeChar;
		}

		boolean effectOriginIsL2PlayableInstance = cha instanceof L2Playable;

		L2PcInstance src = activeChar.getActingPlayer();

		int radius = skill.getSkillRadius();

		boolean srcInArena =
				activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE);

		Collection<L2Object> objs = activeChar.getKnownList().getKnownObjects().values();
		//synchronized (activeChar.getKnownList().getKnownObjects())
		{
			for (L2Object obj : objs)
			{
				if (obj == cha)
				{
					continue;
				}

				if (!(obj instanceof L2Attackable || obj instanceof L2Playable))
				{
					continue;
				}

				target = (L2Character) obj;

				if (!target.isDead() && target != activeChar)
				{
					if (!Util.checkIfInRange(radius, obj, activeChar, true))
					{
						continue;
					}

					if (!((L2Character) obj).isInFrontOf(activeChar))
					{
						continue;
					}

					if (!GeoEngine.getInstance().canSeeTarget(activeChar, obj))
					{
						continue;
					}

					if (src != null) // caster is l2playableinstance and exists
					{
						if (obj instanceof L2PcInstance)
						{
							L2PcInstance trg = (L2PcInstance) obj;

							if (trg == src)
							{
								continue;
							}

							if (src.getParty() != null && trg.getParty() != null &&
									src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
							{
								continue;
							}

							if (trg.isInsideZone(L2Character.ZONE_PEACE))
							{
								continue;
							}

							if (!srcInArena && !(trg.isInsideZone(L2Character.ZONE_PVP) &&
									!trg.isInsideZone(L2Character.ZONE_SIEGE)))
							{
								if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
								{
									continue;
								}

								if (src.getClan() != null && trg.getClan() != null)
								{
									if (src.getClan().getClanId() == trg.getClan().getClanId())
									{
										continue;
									}
								}

								if (!src.checkPvpSkill(obj, skill))
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

							if (src.getParty() != null && trg.getParty() != null &&
									src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
							{
								continue;
							}

							if (!srcInArena && !(trg.isInsideZone(L2Character.ZONE_PVP) &&
									!trg.isInsideZone(L2Character.ZONE_SIEGE)))
							{
								if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
								{
									continue;
								}

								if (src.getClan() != null && trg.getClan() != null)
								{
									if (src.getClan().getClanId() == trg.getClan().getClanId())
									{
										continue;
									}
								}

								if (!src.checkPvpSkill(trg, skill))
								{
									continue;
								}
							}

							if (((L2Summon) obj).isInsideZone(L2Character.ZONE_PEACE))
							{
								continue;
							}
						}
					}
					else
					// Skill user is not L2PlayableInstance
					{
						if (effectOriginIsL2PlayableInstance && // If effect starts at L2PlayableInstance and
								!(obj instanceof L2Playable)) // Object is not L2PlayableInstance
						{
							continue;
						}
					}
					targetList.add((L2Character) obj);
				}
			}
		}

		if (targetList.size() == 0)
		{
			return null;
		}

		return targetList.toArray(new L2Character[targetList.size()]);
	}

	/**
	 */
	@Override
	public Enum<L2SkillTargetType> getTargetType()
	{
		return L2SkillTargetType.TARGET_FRONT_AREA;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetFrontArea());
	}
}
