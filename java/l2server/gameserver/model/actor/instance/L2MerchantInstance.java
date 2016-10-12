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

import l2server.Config;
import l2server.gameserver.TradeController;
import l2server.gameserver.datatables.MerchantPriceConfigTable;
import l2server.gameserver.datatables.MerchantPriceConfigTable.MerchantPriceConfig;
import l2server.gameserver.model.L2TradeList;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.ExBuyList;
import l2server.gameserver.network.serverpackets.ExSellList;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;

/**
 * This class ...
 *
 * @version $Revision: 1.10.4.9 $ $Date: 2005/04/11 10:06:08 $
 */
public class L2MerchantInstance extends L2NpcInstance
{
	private MerchantPriceConfig _mpc;

	/**
	 * @param template
	 */
	public L2MerchantInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2MerchantInstance);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_mpc = MerchantPriceConfigTable.getInstance().getMerchantPriceConfig(this);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";

		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}

		return "merchant/" + pom + ".htm";
	}

	@Override
	public String getHtmlPath(int npcId, String val)
	{
		String pom = "";
		if (val.isEmpty() || val.equals("0"))
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}

		return "merchant/" + pom + ".htm";
	}

	/**
	 * @return Returns the mpc.
	 */
	public MerchantPriceConfig getMpc()
	{
		return _mpc;
	}

	public final void showBuyWindow(L2PcInstance player, int val)
	{
		double taxRate = 0;

		taxRate = getMpc().getTotalTaxRate();

		player.tempInventoryDisable();

		if (Config.DEBUG)
		{
			Log.fine("Showing buylist");
		}

		L2TradeList list = TradeController.getInstance().getBuyList(val);

		if (list != null && list.getNpcId() == getNpcId())
		{
			player.sendPacket(new ExBuyList(list, player.getAdena(), taxRate));
			player.sendPacket(new ExSellList(player, list, taxRate, false));
		}
		else
		{
			Log.warning("possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
			Log.warning("buylist id:" + val);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
