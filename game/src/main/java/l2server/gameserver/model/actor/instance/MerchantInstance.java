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
import l2server.gameserver.templates.chars.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class ...
 *
 * @version $Revision: 1.10.4.9 $ $Date: 2005/04/11 10:06:08 $
 */
public class MerchantInstance extends NpcInstance {
	private static Logger log = LoggerFactory.getLogger(MerchantInstance.class.getName());

	private MerchantPriceConfig mpc;
	
	public MerchantInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2MerchantInstance);
	}
	
	@Override
	public void onSpawn() {
		super.onSpawn();
		mpc = MerchantPriceConfigTable.getInstance().getMerchantPriceConfig(this);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val) {
		String pom = "";
		
		if (val == 0) {
			pom = "" + npcId;
		} else {
			pom = npcId + "-" + val;
		}
		
		return "merchant/" + pom + ".htm";
	}
	
	@Override
	public String getHtmlPath(int npcId, String val) {
		String pom = "";
		if (val.isEmpty() || val.equals("0")) {
			pom = "" + npcId;
		} else {
			pom = npcId + "-" + val;
		}
		
		return "merchant/" + pom + ".htm";
	}
	
	/**
	 * @return Returns the mpc.
	 */
	public MerchantPriceConfig getMpc() {
		return mpc;
	}
	
	public final void showBuyWindow(Player player, int val) {
		double taxRate = 0;
		
		taxRate = getMpc().getTotalTaxRate();
		
		player.tempInventoryDisable();
		
		if (Config.DEBUG) {
			log.debug("Showing buylist");
		}
		
		L2TradeList list = TradeController.INSTANCE.getBuyList(val);
		
		if (list != null && list.getNpcId() == getNpcId()) {
			player.sendPacket(new ExBuyList(list, player.getAdena(), taxRate));
			player.sendPacket(new ExSellList(player, list, taxRate, false));
		} else {
			log.warn("possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
			log.warn("buylist id:" + val);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
