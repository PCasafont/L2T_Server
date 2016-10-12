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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;

import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Level;

/**
 * @author Kerberos
 */
public final class L2CastleTeleporterInstance extends L2Npc
{
	private boolean _currentTask = false;

	/**
	 * @param template
	 */
	public L2CastleTeleporterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2CastleTeleporterInstance);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		if (actualCommand.equalsIgnoreCase("tele"))
		{
			int delay;
			if (!getTask())
			{
				if (getCastle().getSiege().getIsInProgress() && getCastle().getSiege().getControlTowerCount() == 0)
				{
					delay = 480000;
				}
				else
				{
					delay = 30000;
				}

				setTask(true);
				ThreadPoolManager.getInstance().scheduleGeneral(new oustAllPlayers(), delay);
			}

			String filename = "castleteleporter/MassGK-1.htm";
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player.getHtmlPrefix(), filename);
			player.sendPacket(html);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename;
		if (!getTask())
		{
			if (getCastle().getSiege().getIsInProgress() && getCastle().getSiege().getControlTowerCount() == 0)
			{
				filename = "castleteleporter/MassGK-2.htm";
			}
			else
			{
				filename = "castleteleporter/MassGK.htm";
			}
		}
		else
		{
			filename = "castleteleporter/MassGK-1.htm";
		}

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	void oustAllPlayers()
	{
		getCastle().oustAllPlayers();
	}

	class oustAllPlayers implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				NpcSay cs = new NpcSay(getObjectId(), 1, getNpcId(),
						1000443); // The defenders of $s1 castle will be teleported to the inner castle.
				cs.addStringParameter(getCastle().getName());
				int region = MapRegionTable.getInstance().getMapRegion(getX(), getY());
				Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers().values();
				//synchronized (L2World.getInstance().getAllPlayers())
				{
					for (L2PcInstance player : pls)
					{
						if (region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
						{
							player.sendPacket(cs);
						}
					}
				}
				oustAllPlayers();
				setTask(false);
			}
			catch (NullPointerException e)
			{
				Log.log(Level.WARNING, "" + e.getMessage(), e);
			}
		}
	}

	public boolean getTask()
	{
		return _currentTask;
	}

	public void setTask(boolean state)
	{
		_currentTask = state;
	}
}
