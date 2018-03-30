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

package l2server.gameserver.network.serverpackets;

import l2server.Config;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2MerchantInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.3.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class SellList extends L2GameServerPacket {

	private final L2PcInstance activeChar;
	private final L2MerchantInstance lease;
	private long money;
	private List<L2ItemInstance> selllist = new ArrayList<>();

	public SellList(L2PcInstance player) {
		activeChar = player;
		lease = null;
		money = activeChar.getAdena();
		doLease();
	}

	public SellList(L2PcInstance player, L2MerchantInstance lease) {
		activeChar = player;
		this.lease = lease;
		money = activeChar.getAdena();
		doLease();
	}

	private void doLease() {
		if (lease == null) {
			for (L2ItemInstance item : activeChar.getInventory().getItems()) {
				if (!item.isEquipped() && // Not equipped
						item.isSellable() && // Item is sellable
						(activeChar.getPet() == null || // Pet not summoned or
								item.getObjectId() !=
										activeChar.getPet().getControlObjectId())) // Pet is summoned and not the item that summoned the pet
				{
					selllist.add(item);
					if (Config.DEBUG) {
						Log.fine("item added to selllist: " + item.getItem().getName());
					}
				}
			}
		}
	}

	@Override
	protected final void writeImpl() {
		writeQ(money);
		writeD(lease == null ? 0x00 : 1000000 + lease.getTemplate().NpcId);
		writeH(selllist.size());

		for (L2ItemInstance item : selllist) {
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeQ(item.getCount());
			writeH(item.getItem().getType2());
			writeH(0x00);
			writeQ(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeQ(item.getItem().getSalePrice());

			// T1
			writeH(item.getAttackElementType());
			writeH(item.getAttackElementPower());
			for (byte i = 0; i < 6; i++) {
				writeH(item.getElementDefAttr(i));
			}

			writeH(0x00); // Enchant effect 1
			writeH(0x00); // Enchant effect 2
			writeH(0x00); // Enchant effect 3
		}
	}
}
