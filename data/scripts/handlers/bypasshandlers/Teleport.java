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

package handlers.bypasshandlers;

import l2server.Config;
import l2server.gameserver.Ranked1v1;
import l2server.gameserver.events.PvpZone;
import l2server.gameserver.events.Ranked2v2;
import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.util.Rnd;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class Teleport implements IBypassHandler
{
	private static final String[] COMMANDS = {"teleto", "maintown", "pvpzone"};

	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
	{
		if (target == null)
		{
			return false;
		}

		StringTokenizer st = new StringTokenizer(command, " ");
		st.nextToken();

		if (command.startsWith("teleto")) // Tenkai custom - raw teleport coordinates, only check for TW ward
		{
			if (activeChar.isCombatFlagEquipped())
			{
				activeChar.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD));
				return false;
			}

			if (activeChar.getPvpFlag() > 0)
			{
				activeChar.sendMessage("You can't teleport while flagged!");
				return false;
			}

			int[] coords = new int[3];
			try
			{
				for (int i = 0; i < 3; i++)
				{
					coords[i] = Integer.valueOf(st.nextToken());
				}
				activeChar.teleToLocation(coords[0], coords[1], coords[2]);
				activeChar.setInstanceId(0);
			}
			catch (Exception e)
			{
				_log.warning("L2Teleporter - " + target.getName() + "(" + target.getNpcId() +
						") - failed to parse raw teleport coordinates from html");
				e.printStackTrace();
			}

			return true;
		}
		else if (command.startsWith("pvpzone"))
		{
			boolean parties = st.nextToken().equals("1");
			boolean artificialPlayers = st.nextToken().equals("1");

			if (PvpZone.state == PvpZone.State.FIGHT)
			{
				activeChar.sendMessage("You can't teleport while the pvp zone is opened");
				return false;
			}

			if (!parties && activeChar.isInParty() && !activeChar.isGM())
			{
				activeChar.sendPacket(
						new CreatureSay(0, Say2.TELL, target.getName(), "You can't go there being in a party."));
				return true;
			}

			L2PcInstance mostPvP = L2World.getInstance().getMostPvP(parties, artificialPlayers);
			if (mostPvP != null)
			{
				// Check if the player's clan is already outnumbering the PvP
				if (activeChar.getClan() != null)
				{
					Map<Integer, Integer> clanNumbers = new HashMap<Integer, Integer>();
					int allyId = activeChar.getAllyId();
					if (allyId == 0)
					{
						allyId = activeChar.getClanId();
					}
					clanNumbers.put(allyId, 1);
					for (L2PcInstance known : mostPvP.getKnownList().getKnownPlayers().values())
					{
						int knownAllyId = known.getAllyId();
						if (knownAllyId == 0)
						{
							knownAllyId = known.getClanId();
						}
						if (knownAllyId != 0)
						{
							if (clanNumbers.containsKey(knownAllyId))
							{
								clanNumbers.put(knownAllyId, clanNumbers.get(knownAllyId) + 1);
							}
							else
							{
								clanNumbers.put(knownAllyId, 1);
							}
						}
					}

					int biggestAllyId = 0;
					int biggestAmount = 2;
					for (Entry<Integer, Integer> clanNumber : clanNumbers.entrySet())
					{
						if (clanNumber.getValue() > biggestAmount)
						{
							biggestAllyId = clanNumber.getKey();
							biggestAmount = clanNumber.getValue();
						}
					}

					if (biggestAllyId == allyId)
					{
						activeChar.sendPacket(new CreatureSay(0, Say2.TELL, target.getName(),
								"Sorry, your clan/ally is outnumbering the place already so you can't move there."));
						return true;
					}
				}
				if (PvpZone.players.contains(mostPvP) ||
						PvpZone.fighters.contains(mostPvP) ||
						Ranked1v1.fighters.containsKey(mostPvP))
				{
					activeChar.sendMessage("Sorry, I can't find anyone in flag status right now.");
					return true;
				}
				activeChar.teleToLocation(mostPvP.getX() + Rnd.get(300) - 150, mostPvP.getY() + Rnd.get(300) - 150,
						mostPvP.getZ());
				activeChar.setInstanceId(0);
				activeChar.setProtection(true);
				if (!activeChar.isGM())
				{
					activeChar.setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_PVP_TIME);
					activeChar.startPvPFlag();
				}
			}
			else
			{
				activeChar.sendPacket(new CreatureSay(0, Say2.TELL, target.getName(),
						"Sorry, I can't find anyone in flag status right now."));
			}

			return true;
		}
		return false;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
