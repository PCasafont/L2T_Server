/* This program is free software: you can redistribute it and/or modify it under
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
import l2server.gameserver.model.entity.Fort;
import l2server.log.Log;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Vice - 2008
 * Class managing periodical events with castle
 */
public class FortUpdater implements Runnable
{
	protected static Logger log = Logger.getLogger(FortUpdater.class.getName());
	private L2Clan clan;
	private Fort fort;
	@SuppressWarnings("unused") private int runCount;
	private UpdaterType updaterType;

	public enum UpdaterType
	{
		MAX_OWN_TIME, // gives fort back to NPC clan
		PERIODIC_UPDATE // raise blood oath/supply level
	}

	public FortUpdater(Fort fort, L2Clan clan, int runCount, UpdaterType ut)
	{
		this.fort = fort;
		this.clan = clan;
		this.runCount = runCount;
		updaterType = ut;
	}

	@Override
	public void run()
	{
		try
		{
			switch (updaterType)
			{
				case PERIODIC_UPDATE:
					runCount++;
					if (fort.getOwnerClan() == null || fort.getOwnerClan() != clan)
					{
						return;
					}

					fort.setBloodOathReward(fort.getBloodOathReward() + Config.FS_BLOOD_OATH_COUNT);
					if (fort.getFortState() == 2)
					{
						if (clan.getWarehouse().getAdena() >= Config.FS_FEE_FOR_CASTLE)
						{
							clan.getWarehouse()
									.destroyItemByItemId("FS_fee_for_Castle", 57, Config.FS_FEE_FOR_CASTLE, null, null);
							CastleManager.getInstance().getCastleById(fort.getCastleId())
									.addToTreasuryNoTax(Config.FS_FEE_FOR_CASTLE);
							fort.raiseSupplyLvL();
						}
						else
						{
							fort.setFortState(1, 0);
						}
					}
					fort.saveFortVariables();
					break;
				case MAX_OWN_TIME:
					if (fort.getOwnerClan() == null || fort.getOwnerClan() != clan)
					{
						return;
					}
					if (fort.getOwnedTime() > Config.FS_MAX_OWN_TIME * 3600)
					{
						fort.removeOwner(true);
						fort.setFortState(0, 0);
					}
					break;
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "", e);
		}
	}
}
