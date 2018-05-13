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

package handlers.voicedcommandhandlers;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.handler.IVoicedCommandHandler;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.TradeList;
import l2server.gameserver.model.TradeList.TradeItem;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.CreatureZone;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.PrivateStoreMsgSell;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.gameserver.templates.item.ItemTemplate;
import l2server.gameserver.util.Util;

import java.util.Map.Entry;

/**
 * @author Pere
 * @author LasTravel
 */

public class Sell implements IVoicedCommandHandler {
	private static final boolean logSellCommand = true;

	private static final String[] VOICED_COMMANDS = {"sell"};

	/**
	 * @see l2server.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, Player, java.lang.String)
	 */
	@Override
	public boolean useVoicedCommand(String command, Player player, String params) {
		if (command.equalsIgnoreCase("sell")) {
			if (!player.getClient().getFloodProtectors().getTransaction().tryPerformAction("buy")) {
				return false;
			}

			if (ThreadPoolManager.getInstance().isShutdown()) {
				return false;
			}

			if (params == null) {
				params = "";
			}

			boolean isSelling = player.getPrivateStoreType() == Player.STORE_PRIVATE_CUSTOM_SELL;
			if (!isSelling && player.getPrivateStoreType() > 0) {
				return false;
			}

			TradeList list = player.getCustomSellList();
			String[] values = params.split(" ");

			//Commands Section
			if (!isSelling) {
				if (params.contains("addItem")) {
					if (values != null) {
						if (values.length == 2) {
							if (!canAddMoreItems(player)) {
								return false;
							}

							int itemObjdId = Integer.parseInt(values[1]);
							Item targetItem = player.getInventory().getItemByObjectId(itemObjdId);
							if (targetItem != null && targetItem.getCount() >= 1) {
								list.addItem(itemObjdId, 1L);
							}
						} else {
							player.sendPacket(new ExShowScreenMessage("Click on the item you want to sell", 5000));
							player.setIsAddSellItem(true);
						}
					}
				} else if (params.contains("addPrice")) {
					if (values != null) {
						if (values.length == 2) {
							if (!canAddMoreItems(player)) {
								return false;
							}

							int itemObjId = Integer.valueOf(values[1]);

							player.sendPacket(new ExShowScreenMessage("Click on the item you want to add as a price", 5000));
							player.setAddSellPrice(itemObjId);
						} else {
							if (values.length == 3) {
								int itemObjId = Integer.parseInt(values[1]);
								int itemId = Integer.parseInt(values[2]);

								ItemTemplate toSell = ItemTable.getInstance().getTemplate(itemId);
								if (toSell != null && toSell.isTradeable()) {
									for (TradeItem item : list.getItems()) {
										if (item == null) {
											continue;
										}
										if (item.getObjectId() == itemObjId) {
											item.getPriceItems().put(toSell, 1L);
											break;
										}
									}
								} else {
									player.sendMessage("Sell: You can't add this item as a price!");
									return false;
								}
							}
						}
					}
				} else if (params.contains("deleteItem")) {
					if (values != null) {
						if (values.length == 2) {
							int objId = Integer.parseInt(values[1]);
							list.removeItem(objId, -1, -1);
						}
					}
				} else if (params.contains("setSellItemCount")) {
					if (values != null) {
						if (values.length == 3) {
							int itemObjId = Integer.parseInt(values[1]);
							long priceCount = Long.parseLong(values[2]);

							if (priceCount < 1) {
								player.sendMessage("Sell: You can't set: " + priceCount + "!");
								return false;
							}

							Item targetItem = player.getInventory().getItemByObjectId(itemObjId);
							if (targetItem != null) {
								if (player.checkItemManipulation(itemObjId, priceCount, "Custom Sell") == null) {
									player.sendMessage("Sell: You don't have enough " + targetItem.getName() + "!");
									return false;
								}

								//TradeItem item = list.getItems()[i];
								for (TradeItem item : list.getItems()) {
									if (item == null) {
										continue;
									}
									if (item.getObjectId() == itemObjId) {
										item.setCount(priceCount);
										break;
									}
								}
							} else {
								player.sendMessage("Sell: Something is wrong...");
								return false;
							}
						}
					}
				} else if (params.contains("setSellPriceCount")) {
					if (values != null) {
						if (values.length == 4) {
							int itemObjId = Integer.parseInt(values[1]);
							int itemId = Integer.parseInt(values[2]);
							Long priceCount = Long.parseLong(values[3]);

							if (priceCount < 1) {
								player.sendMessage("Sell: You can't set " + priceCount + "");
								return false;
							}

							for (TradeItem item : list.getItems()) {
								if (item == null) {
									continue;
								}
								if (item.getObjectId() == itemObjId) {
									for (Entry<ItemTemplate, Long> i : item.getPriceItems().entrySet()) {
										if (i.getKey().getItemId() == itemId) {
											item.getPriceItems().put(i.getKey(), priceCount);
											break;
										}
									}
									break;
								}
							}
						}
					}
				} else if (params.contains("deletePriceItem")) {
					if (values != null) {
						if (values.length == 3) {
							int itemObjId = Integer.parseInt(values[1]);
							int itemId = Integer.parseInt(values[2]);
							ItemTemplate temp = ItemTable.getInstance().getTemplate(itemId);
							if (temp == null) {
								return false;
							}

							for (TradeItem item : list.getItems()) {
								if (item == null) {
									continue;
								}
								if (item.getObjectId() == itemObjId) {
									item.getPriceItems().remove(temp);
									break;
								}
							}
						}
					}
				} else if (params.contains("setMessage")) {
					if (values != null) {
						if (params.length() >= 11) {
							String title = params.substring(11);
							if (title != null && title.length() < 29) {
								list.setTitle(title);
							}
						} else {
							player.sendMessage("Sell: Please set correctly the shop message!");
						}
					}
				} else if (params.contains("delMessage")) {
					list.setTitle(null);
				} else if (params.equalsIgnoreCase("start")) {
					if (list.getItemCount() < 1) {
						player.sendMessage("Sell: You need set at least one item to sell!");
						return false;
					}

					for (TradeItem item : list.getItems()) {
						if (item == null) {
							continue;
						}

						if (item.isEquipped()) {
							player.sendMessage("Sell: You can't sell one item that is equipped!");
							return false;
						}

						if (player.checkItemManipulation(item.getObjectId(), item.getCount(), "Custom Sell") == null) {
							player.sendMessage("Sell: You can't sell: " + item.getItem().getName() + "!");
							return false;
						}

						if (item.getPriceItems().isEmpty()) {
							player.sendMessage("Sell: " + item.getItem().getName() + " doesn't have any price!");
							return false;
						}
					}

					if (!player.getAccessLevel().allowTransaction()) {
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
						return false;
					}

					if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player) || player.isInDuel()) {
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT));
						return false;
					}

					if (player.isInsideZone(CreatureZone.ZONE_NOSTORE)) {
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_PRIVATE_STORE_HERE));
						return false;
					}

					for (Creature c : player.getKnownList().getKnownCharactersInRadius(70)) {
						if (!(c instanceof Player && ((Player) c).getPrivateStoreType() == Player.STORE_PRIVATE_NONE)) {
							player.sendMessage("Sell: Try to put your store a little further from " + c.getName() + ", please.");
							return false;
						}
					}

					isSelling = true;
					player.setPrivateStoreType(Player.STORE_PRIVATE_CUSTOM_SELL);
					player.broadcastUserInfo();
					player.broadcastPacket(new PrivateStoreMsgSell(player));
					player.sitDown();

					if (logSellCommand) {
						String log = player.getName() + " (" + list.getTitle() + ")\n";
						for (TradeItem item : list.getItems()) {
							log += "\t" + item.getItem().getName() + " (max " + item.getCount() + ")\n";
							for (Entry<ItemTemplate, Long> priceItem : item.getPriceItems().entrySet()) {
								log += "\t\t" + priceItem.getKey().getName() + " (" + priceItem.getValue() + ")\n";
							}
						}
						Util.logToFile(log, "sellLog", true);
					}
				}
			} else {
				if (params.equalsIgnoreCase("stop")) {
					player.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					player.standUp();
					player.broadcastUserInfo();
					isSelling = false;
				}
			}

			//Html page section
			StringBuilder sb = new StringBuilder();

			sb.append("<html><body><title>Sell</title>");

			//Title Section
			sb.append("<center><table width=300 bgcolor=666666><tr><td align=center>Shop Message:</td></tr");
			if (list != null && list.getTitle() == null) {
				sb.append("<tr><td align=center><edit var=\"addMes\" width=150 type=char length=16></td></tr>");
				sb.append(
						"<tr><td align=center><button action=\"bypass -h voice .sell setMessage $addMes\" value=\"Add Message!\" width=120 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></td></tr>");
			} else {
				sb.append("<tr><td align=center><font color=LEVEL>" + list.getTitle() + "</font></td></tr>");
				sb.append(
						"<tr><td align=center><button action=\"bypass -h voice .sell delMessage\" value=Delete Description! width=120 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></td></tr>");
			}
			sb.append("</table></center><br>");

			//Items section
			//Add Items
			sb.append("<table width=300>");

			if (isSelling) {
				sb.append(
						"<tr><td align=center><button action=\"bypass -h voice .sell stop\" value=\"Stop!\" width=120 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></td></tr>");
			} else {
				if (list.getItemCount() > 0) {
					sb.append(
							"<tr><td align=center><button action=\"bypass -h voice .sell start\" value=\"Start!\" width=120 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></td></tr>");
				}
			}
			sb.append(
					"<tr><td align=center><button action=\"bypass -h voice .sell addItem\" value=\"Add item to sell!\" width=120 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></td></tr>");
			sb.append("</table>");

			//Current items
			sb.append("<br>");
			sb.append("<table width=300>");
			for (int i = 0; i < list.getItemCount(); i++) {
				TradeItem item = list.getItems()[i];
				if (item == null) {
					continue;
				}

				sb.append("<tr>");
				sb.append("<td width=300>");

				String itemName = item.getItem().getName();
				if (itemName.length() > 30) {
					itemName = itemName.substring(0, 30) + "(1)" + "...";
				} else {
					itemName += "(1)";
				}

				sb.append("<center><table width=300 bgcolor=666666><tr><td FIXWIDTH=300 align=center>Sell</td></tr></table></center>");
				sb.append("<table width=300 bgcolor=E35757><tr><td FIXWIDTH=150>" + itemName +
						"</td><td FIXWIDTH=20><button action=\"bypass -h voice .sell deleteItem " + item.getObjectId() +
						"\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></td></tr></table>");
				sb.append("<table width=270><tr><td><table width=220><tr><td width=100>Max amount: " + item.getCount() +
						" </td><td width=55><edit var=\"count" + item.getObjectId() +
						"\" width=50 type=number length=14></td><td width=30><button action=\"bypass -h voice .sell setSellItemCount " +
						item.getObjectId() + " $count" + item.getObjectId() +
						"\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></td></tr></table></td></tr></table>");

				if (!item.getPriceItems().isEmpty()) {
					sb.append("<br>");
					sb.append("<center><table width=250 bgcolor=666666><tr><td FIXWIDTH=270 align=center>Price per unit</td></tr></table></center>");

					int index = 0;
					for (Entry<ItemTemplate, Long> b : item.getPriceItems().entrySet()) {
						String priceName = b.getKey().getName();
						if (priceName.length() > 35) {
							priceName = priceName.substring(0, 35) + "...";
						}

						sb.append("<center>");
						sb.append("<table width=250 bgcolor=8FBDC5><tr><td FIXWIDTH=230>" + priceName +
								"</td><td FIXWIDTH=20><button action=\"bypass -h voice .sell deletePriceItem " + item.getObjectId() + " " +
								b.getKey().getItemId() +
								"\" value=\" \" width=16 height=16 back=L2UI_CT1.BtnEditDel fore=L2UI_CT1.BtnEditDel_over></td></tr></table>");
						sb.append("<table width=250><tr><td><table width=220><tr><td width=100>Count: " + b.getValue() +
								" </td><td width=55><edit var=\"count" + item.getObjectId() + "-" + index +
								"\" width=50 type=number length=14></td><td width=30><button action=\"bypass -h voice .sell setSellPriceCount " +
								item.getObjectId() + " " + b.getKey().getItemId() + " " + " $count" + item.getObjectId() + "-" + index +
								"\" value=\" \" width=16 height=16 back=L2UI.rightBtn1 fore=L2UI.rightBtn2></td></tr></table></td></tr></table><br>");
						sb.append("</center>");
						index++;
					}
				}
				sb.append("<br><center><table><tr><td align=center><button action=\"bypass -h voice .sell addPrice " + item.getObjectId() +
						"\" value=\"Add price!\" width=120 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></td></tr></table></center><br><br><br>");
				sb.append("</td>");
				sb.append("</tr>");
			}
			sb.append("</table>");
			sb.append("</body></html>");

			player.sendPacket(new NpcHtmlMessage(0, sb.toString()));
		}
		return true;
	}

	private boolean canAddMoreItems(Player player) {
		TradeList list = player.getCustomSellList();
		int count = list.getItems().length;
		for (TradeItem item : list.getItems()) {
			if (item == null) {
				continue;
			}
			count += item.getPriceItems().size();
		}

		if (count + 1 >= 17) {
			player.sendMessage("You can't add more items!");
			return false;
		}
		return true;
	}

	/**
	 * @see l2server.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
	 */
	@Override
	public String[] getVoicedCommandList() {
		return VOICED_COMMANDS;
	}
}
