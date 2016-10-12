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

package l2server.gameserver.stats.skills;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.log.Log;

import java.util.logging.Level;

public class L2SkillTeleport extends L2Skill
{
	private final String _recallType;
	private final Location _loc;

	public L2SkillTeleport(StatsSet set)
	{
		super(set);

		_recallType = set.getString("recallType", "");
		String coords = set.getString("teleCoords", null);
		if (coords != null)
		{
			String[] valuesSplit = coords.split(",");
			_loc = new Location(Integer.parseInt(valuesSplit[0]), Integer.parseInt(valuesSplit[1]),
					Integer.parseInt(valuesSplit[2]));
		}
		else
		{
			_loc = null;
		}
	}

	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if (activeChar instanceof L2PcInstance)
		{
			// Thanks nbd
			if (((L2PcInstance) activeChar).getEvent() != null &&
					!((L2PcInstance) activeChar).getEvent().onEscapeUse(activeChar.getObjectId()))
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (activeChar.isAfraid() || activeChar.isInLove())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (!((L2PcInstance) activeChar).canEscape() || ((L2PcInstance) activeChar).isCombatFlagEquipped())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (((L2PcInstance) activeChar).isInOlympiadMode())
			{
				activeChar.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
				return;
			}
		}

		try
		{
			for (L2Character target : (L2Character[]) targets)
			{
				if (target instanceof L2PcInstance)
				{
					L2PcInstance targetChar = (L2PcInstance) target;

					// Check to see if player is in jail
					if (targetChar.isInJail())
					{
						targetChar.sendMessage("You can not escape from jail.");
						continue;
					}

					if (!targetChar.canEscape() || targetChar.isCombatFlagEquipped())
					{
						continue;
					}

					// Check to see if player is in a duel
					if (targetChar.isInDuel())
					{
						targetChar.sendMessage("You cannot use escape skills during a duel.");
						continue;
					}

					if (targetChar != activeChar)
					{
						if (targetChar.getEvent() != null &&
								!targetChar.getEvent().onEscapeUse(targetChar.getObjectId()))
						{
							continue;
						}

						if (targetChar.isInOlympiadMode())
						{
							continue;
						}

						if (GrandBossManager.getInstance().getZone(targetChar) != null)
						{
							continue;
						}

						if (targetChar.isCombatFlagEquipped())
						{
							continue;
						}
					}
				}
				Location loc = null;
				if (getSkillType() == L2SkillType.TELEPORT)
				{
					if (_loc != null)
					{
						// target is not player OR player is not flying or flymounted
						// TODO: add check for gracia continent coords
						if (!(target instanceof L2PcInstance) ||
								!(target.isFlying() || ((L2PcInstance) target).isFlyingMounted()))
						{
							loc = _loc;
						}
					}
				}
				else
				{
					if (_recallType.equalsIgnoreCase("Castle"))
					{
						loc = MapRegionTable.getInstance()
								.getTeleToLocation(target, MapRegionTable.TeleportWhereType.Castle);
					}
					else if (_recallType.equalsIgnoreCase("ClanHall"))
					{
						loc = MapRegionTable.getInstance()
								.getTeleToLocation(target, MapRegionTable.TeleportWhereType.ClanHall);
					}
					else if (_recallType.equalsIgnoreCase("Fortress"))
					{
						loc = MapRegionTable.getInstance()
								.getTeleToLocation(target, MapRegionTable.TeleportWhereType.Fortress);
					}
					else
					{
						loc = MapRegionTable.getInstance()
								.getTeleToLocation(target, MapRegionTable.TeleportWhereType.Town);
					}
				}
				if (loc != null)
				{
					target.setInstanceId(0);

					final Location tpTo = loc;
					final L2Character chararacter = target;
					ThreadPoolManager.getInstance().scheduleGeneral(() -> chararacter.teleToLocation(tpTo, true), 400);
				}
			}
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "", e);
		}
	}
}
