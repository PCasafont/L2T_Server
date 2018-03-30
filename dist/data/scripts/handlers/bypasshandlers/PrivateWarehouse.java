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
import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.network.serverpackets.SortedWareHouseWithdrawalList.WarehouseListType;

public class PrivateWarehouse implements IBypassHandler {
	private static final String[] COMMANDS = {"withdrawp", "withdrawsortedp", "depositp"};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target) {
		if (target == null) {
			return false;
		}
		
		if (activeChar.isEnchanting()) {
			return false;
		}
		
		try {
			if (command.toLowerCase().startsWith(COMMANDS[0])) // WithdrawP
			{
				if (Config.L2JMOD_ENABLE_WAREHOUSESORTING_PRIVATE) {
					NpcHtmlMessage msg = new NpcHtmlMessage(target.getObjectId());
					msg.setFile(activeChar.getHtmlPrefix(), "mods/WhSortedP.htm");
					msg.replace("%objectId%", String.valueOf(target.getObjectId()));
					activeChar.sendPacket(msg);
				} else {
					showWithdrawWindow(activeChar, null, (byte) 0);
				}
				return true;
			} else if (command.toLowerCase().startsWith(COMMANDS[1])) // WithdrawSortedP
			{
				final String param[] = command.split(" ");
				
				if (param.length > 2) {
					showWithdrawWindow(activeChar, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.getOrder(param[2]));
				} else if (param.length > 1) {
					showWithdrawWindow(activeChar, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.A2Z);
				} else {
					showWithdrawWindow(activeChar, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z);
				}
				return true;
			} else if (command.toLowerCase().startsWith(COMMANDS[2])) // DepositP
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				activeChar.setActiveWarehouse(activeChar.getWarehouse());
				activeChar.tempInventoryDisable();
				
				if (Config.DEBUG) {
					log.fine("Source: L2WarehouseInstance.java; Player: " + activeChar.getName() +
							"; Command: showDepositWindow; Message: Showing items to deposit.");
				}
				
				activeChar.sendPacket(new WareHouseDepositList(activeChar, WareHouseDepositList.PRIVATE));
				return true;
			}
			
			return false;
		} catch (Exception e) {
			log.info("Exception in " + getClass().getSimpleName());
		}
		return false;
	}
	
	private static void showWithdrawWindow(L2PcInstance player, WarehouseListType itemtype, byte sortorder) {
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		
		if (player.getActiveWarehouse().getSize() == 0) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH));
			return;
		}
		
		if (itemtype != null) {
			player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE, itemtype, sortorder));
		} else {
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE));
		}
		
		if (Config.DEBUG) {
			log.fine("Source: L2WarehouseInstance.java; Player: " + player.getName() +
					"; Command: showRetrieveWindow; Message: Showing stored items.");
		}
	}
	
	@Override
	public String[] getBypassList() {
		return COMMANDS;
	}
}
