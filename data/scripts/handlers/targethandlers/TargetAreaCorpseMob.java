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
 * @author nBd
 */
public class TargetAreaCorpseMob implements ISkillTargetTypeHandler
{
	/**
	 */
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<L2Character>();

		if (!(target instanceof L2Attackable) || !target.isDead())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return null;
		}

		if (onlyFirst == false)
		{
			targetList.add(target);
		}
		else
		{
			return new L2Character[]{target};
		}

		boolean srcInArena =
				activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE);

		L2PcInstance src = null;
		if (activeChar instanceof L2PcInstance)
		{
			src = (L2PcInstance) activeChar;
		}

		L2PcInstance trg = null;

		int radius = skill.getSkillRadius();

		Collection<L2Object> objs = activeChar.getKnownList().getKnownObjects().values();
		//synchronized (activeChar.getKnownList().getKnownObjects())
		{
			for (L2Object obj : objs)
			{
				if (!(obj instanceof L2Attackable || obj instanceof L2Playable) || ((L2Character) obj).isDead() ||
						(L2Character) obj == activeChar)
				{
					continue;
				}

				if (!Util.checkIfInRange(radius, target, obj, true))
				{
					continue;
				}

				if (!GeoEngine.getInstance().canSeeTarget(activeChar, obj))
				{
					continue;
				}

				if (obj instanceof L2PcInstance && src != null)
				{
					trg = (L2PcInstance) obj;

					if (src.getParty() != null && trg.getParty() != null &&
							src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
					{
						continue;
					}

					if (trg.isInsideZone(L2Character.ZONE_PEACE))
					{
						continue;
					}

					if (!srcInArena &&
							!(trg.isInsideZone(L2Character.ZONE_PVP) && !trg.isInsideZone(L2Character.ZONE_SIEGE)))
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
				if (obj instanceof L2Summon && src != null)
				{
					trg = ((L2Summon) obj).getOwner();

					if (src.getParty() != null && trg.getParty() != null &&
							src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
					{
						continue;
					}

					if (!srcInArena &&
							!(trg.isInsideZone(L2Character.ZONE_PVP) && !trg.isInsideZone(L2Character.ZONE_SIEGE)))
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
				targetList.add((L2Character) obj);
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
		// TODO Auto-generated method stub
		return L2SkillTargetType.TARGET_AREA_CORPSE_MOB;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetAreaCorpseMob());
	}
}
