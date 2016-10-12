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

package l2server.gameserver.network.clientpackets;

import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.*;

import java.util.logging.Logger;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestGMCommand extends L2GameClientPacket
{
	static Logger _log = Logger.getLogger(RequestGMCommand.class.getName());

	private String _targetName;
	private int _command;

	@Override
	protected void readImpl()
	{
		_targetName = readS();
		_command = readD();
		//_unknown  = readD();
	}

	@Override
	protected void runImpl()
	{
		// prevent non gm or low level GMs from vieweing player stuff
		if (!getClient().getActiveChar().isGM() || !getClient().getActiveChar().getAccessLevel().allowAltG())
		{
			return;
		}

		L2PcInstance player = L2World.getInstance().getPlayer(_targetName);
		if (player == null)
		{
			for (L2PcInstance pl : L2World.getInstance().getAllPlayers().values())
			{
				if (pl != null && pl.getName().equalsIgnoreCase(_targetName))
				{
					player = pl;
					break;
				}
			}
		}

		L2Clan clan = ClanTable.getInstance().getClanByName(_targetName);

		// player name was incorrect?
		if (player == null && (clan == null || _command != 6))
		{
			return;
		}

		switch (_command)
		{
			case 1: // player status
			{
				sendPacket(new ExGmViewCharacterInfo(player));
				sendPacket(new GMHennaInfo(player));
				break;
			}
			case 2: // player clan
			{
				if (player.getClan() != null)
				{
					sendPacket(new GMViewPledgeInfo(player.getClan(), player));
				}
				break;
			}
			case 3: // player skills
			{
				sendPacket(new GMViewSkillInfo(player));
				break;
			}
			case 4: // player quests
			{
				sendPacket(new GmViewQuestInfo(player));
				break;
			}
			case 5: // player inventory
			{
				sendPacket(new GMViewItemList(player));
				sendPacket(new GMHennaInfo(player));
				break;
			}
			case 6: // player warehouse
			{
				// gm warehouse view to be implemented
				if (player != null)
				{
					sendPacket(new GMViewWarehouseWithdrawList(player));
				}
				// clan warehouse
				else
				{
					sendPacket(new GMViewWarehouseWithdrawList(clan));
				}
				break;
			}
		}
	}
}
