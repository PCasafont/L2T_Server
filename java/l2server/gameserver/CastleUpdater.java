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

package l2server.gameserver;

import l2server.Config;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.itemcontainer.ItemContainer;
import l2server.log.Log;

import java.util.logging.Level;

/**
 * Thorgrim - 2005
 * Class managing periodical events with castle
 */
public class CastleUpdater implements Runnable
{
	private L2Clan _clan;
	private int _runCount = 0;

	public CastleUpdater(L2Clan clan, int runCount)
	{
		_clan = clan;
		_runCount = runCount;
	}

	@Override
	public void run()
	{
		try
		{
			// Move current castle treasury to clan warehouse every 2 hour
			ItemContainer warehouse = _clan.getWarehouse();
			if (warehouse != null && _clan.getHasCastle() > 0)
			{
				Castle castle = CastleManager.getInstance().getCastleById(_clan.getHasCastle());
				if (!Config.ALT_MANOR_SAVE_ALL_ACTIONS)
				{
					if (_runCount % Config.ALT_MANOR_SAVE_PERIOD_RATE == 0)
					{
						castle.saveSeedData();
						castle.saveCropData();
						if (Config.DEBUG)
						{
							Log.info("Manor System: all data for " + castle.getName() + " saved");
						}
					}
				}
				CastleUpdater cu = new CastleUpdater(_clan, ++_runCount);
				ThreadPoolManager.getInstance().scheduleGeneral(cu, 3600000);
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "", e);
		}
	}
}
