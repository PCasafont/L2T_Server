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

package l2server.gameserver.model.zone.type;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.MapRegionTable.TeleportWhereType;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.L2ZoneType;

/**
 * A simple no restart zone
 *
 * @author GKR
 */
public class L2NoRestartZone extends L2ZoneType
{
	private int _restartAllowedTime = 0;
	private boolean _enabled = true;

	public L2NoRestartZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equalsIgnoreCase("EnabledByDefault"))
		{
			_enabled = Boolean.parseBoolean(value);
		}
		else if (name.equalsIgnoreCase("restartAllowedTime"))
		{
			_restartAllowedTime = Integer.parseInt(value);
		}
		else if (name.equalsIgnoreCase("restartTime"))
		{
			// Do nothing.
		}
		else if (name.equalsIgnoreCase("defaultStatus"))
		{
			// Do nothing.
		}
		else if (name.equalsIgnoreCase("instanceId"))
		{
			// Do nothing.
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (!_enabled)
		{
			return;
		}

		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_NORESTART, true);
			L2PcInstance player = (L2PcInstance) character;

			if (player.getZoneRestartLimitTime() > 0 && player.getZoneRestartLimitTime() < System.currentTimeMillis())
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(player), 2000);
			}
			player.setZoneRestartLimitTime(0);
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (!_enabled)
		{
			return;
		}

		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_NORESTART, false);
		}
	}

	@Override
	public void onDieInside(L2Character character, L2Character killer)
	{
		// Do nothing.
	}

	@Override
	public void onReviveInside(L2Character character)
	{
		// Do nothing.
	}

	public int getRestartAllowedTime()
	{
		return _restartAllowedTime;
	}

	public void setRestartAllowedTime(int time)
	{
		_restartAllowedTime = time;
	}

	private static class TeleportTask implements Runnable
	{
		private final L2PcInstance _player;

		public TeleportTask(L2PcInstance player)
		{
			_player = player;
		}

		@Override
		public void run()
		{
			_player.teleToLocation(TeleportWhereType.Town);
		}
	}
}
