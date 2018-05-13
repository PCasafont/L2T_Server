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

package handlers.admincommandhandlers;

import l2server.DatabasePool;
import l2server.gameserver.TradeController;
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.MerchantPriceConfigTable.MerchantPriceConfig;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.model.L2TradeList;
import l2server.gameserver.model.L2TradeList.L2TradeItem;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.MerchantInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.gameserver.templates.item.ItemTemplate;
import l2server.gameserver.templates.skills.SkillType;
import l2server.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * @author terry
 * con.close() change by Zoey76 24/02/2011
 */
public class AdminEditNpc implements IAdminCommandHandler {
	
	private static final int PAGE_LIMIT = 20;

	private static final String[] ADMIN_COMMANDS =
			{"admin_edit_npc", "admin_save_npc", "admin_save_npcs", "admin_showShop", "admin_showShopList", "admin_addShopItem", "admin_delShopItem",
					"admin_editShopItem", "admin_close_window", "admin_show_skilllist_npc", "admin_add_skill_npc", "admin_edit_skill_npc",
					"admin_del_skill_npc", "admin_log_npc_spawn"};

	@Override
	public boolean useAdminCommand(String command, Player activeChar) {
		//TODO: Tokenize and protect arguments parsing. Externalize HTML.
		if (command.startsWith("admin_showShop ")) {
			String[] args = command.split(" ");
			if (args.length > 1) {
				showShop(activeChar, Integer.parseInt(command.split(" ")[1]));
			}
		} else if (command.startsWith("admin_log_npc_spawn")) {
			WorldObject target = activeChar.getTarget();
			if (target instanceof Npc) {
				Npc npc = (Npc) target;
				log.info("('',1," + npc.getNpcId() + "," + npc.getX() + "," + npc.getY() + "," + npc.getZ() + ",0,0," + npc.getHeading() +
						",60,0,0),");
			}
		} else if (command.startsWith("admin_showShopList ")) {
			String[] args = command.split(" ");
			if (args.length > 2) {
				showShopList(activeChar, Integer.parseInt(command.split(" ")[1]), Integer.parseInt(command.split(" ")[2]));
			}
		} else if (command.startsWith("admin_edit_npc ")) {
			try {
				String[] commandSplit = command.split(" ");
				int npcId = Integer.parseInt(commandSplit[1]);
				NpcTemplate npc = NpcTable.getInstance().getTemplate(npcId);
				showNpcProperty(activeChar, npc);
			} catch (Exception e) {
				activeChar.sendMessage("Wrong usage: //edit_npc <npcId>");
			}
		} else if (command.startsWith("admin_addShopItem ")) {
			String[] args = command.split(" ");
			if (args.length > 1) {
				addShopItem(activeChar, args);
			}
		} else if (command.startsWith("admin_delShopItem ")) {
			String[] args = command.split(" ");
			if (args.length > 2) {
				delShopItem(activeChar, args);
			}
		} else if (command.startsWith("admin_editShopItem ")) {
			String[] args = command.split(" ");
			if (args.length > 2) {
				editShopItem(activeChar, args);
			}
		} else if (command.startsWith("admin_save_npc ")) {
			try {
				saveNpcProperty(activeChar, command);
			} catch (StringIndexOutOfBoundsException e) {
			}
		} else if (command.startsWith("admin_save_npcs")) {
			NpcTable.getInstance().save();
		} else if (command.startsWith("admin_show_skilllist_npc ")) {
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			try {
				int npcId = Integer.parseInt(st.nextToken());
				int page = 0;
				if (st.hasMoreTokens()) {
					page = Integer.parseInt(st.nextToken());
				}
				showNpcSkillList(activeChar, npcId, page);
			} catch (Exception e) {
				activeChar.sendMessage("Usage: //show_skilllist_npc <npc_id> <page>");
			}
		} else if (command.startsWith("admin_edit_skill_npc ")) {
			try {
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int npcId = Integer.parseInt(st.nextToken());
				int skillId = Integer.parseInt(st.nextToken());
				if (!st.hasMoreTokens()) {
					showNpcSkillEdit(activeChar, npcId, skillId);
				} else {
					int level = Integer.parseInt(st.nextToken());
					addNpcSkillData(activeChar, npcId, skillId, level);
				}
			} catch (Exception e) {
				activeChar.sendMessage("Usage: //edit_skill_npc <npc_id> <item_id> [<level>]");
			}
		} else if (command.startsWith("admin_add_skill_npc ")) {
			try {
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int npcId = Integer.parseInt(st.nextToken());
				if (!st.hasMoreTokens()) {
					showNpcSkillAdd(activeChar, npcId);
				} else {
					int skillId = Integer.parseInt(st.nextToken());
					int level = Integer.parseInt(st.nextToken());
					addNpcSkillData(activeChar, npcId, skillId, level);
				}
			} catch (Exception e) {
				activeChar.sendMessage("Usage: //add_skill_npc <npc_id> [<skill_id> <level>]");
			}
		} else if (command.startsWith("admin_del_skill_npc ")) {
			try {
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				int npcId = Integer.parseInt(st.nextToken());
				int skillId = Integer.parseInt(st.nextToken());
				deleteNpcSkillData(activeChar, npcId, skillId);
			} catch (Exception e) {
				activeChar.sendMessage("Usage: //del_skill_npc <npc_id> <skill_id>");
			}
		}

		return true;
	}

	private void editShopItem(Player activeChar, String[] args) {
		int tradeListID = Integer.parseInt(args[1]);
		int itemID = Integer.parseInt(args[2]);
		L2TradeList tradeList = TradeController.INSTANCE.getBuyList(tradeListID);

		ItemTemplate item = ItemTable.getInstance().getTemplate(itemID);
		if (tradeList.getPriceForItemId(itemID) < 0) {
			return;
		}

		if (args.length > 3) {
			long price = Long.parseLong(args[3]);
			int order = findOrderTradeList(itemID, tradeList.getPriceForItemId(itemID), tradeListID);

			tradeList.replaceItem(itemID, Long.parseLong(args[3]));
			updateTradeList(itemID, price, tradeListID, order);

			activeChar.sendMessage("Updated price for " + item.getName() + " in Trade List " + tradeListID);
			showShopList(activeChar, tradeListID, 1);
			return;
		}

		final String replyMSG = StringUtil.concat("<html><title>Merchant Shop Item Edit</title><body><center><font color=\"LEVEL\">",
				NpcTable.getInstance().getTemplate(tradeList.getNpcId()).getName(),
				" (",
				String.valueOf(tradeList.getNpcId()),
				") -> ",
				Integer.toString(tradeListID),
				"</font></center><table width=\"100%\"><tr><td>Item</td><td>",
				item.getName(),
				" (",
				Integer.toString(item.getItemId()),
				")",
				"</td></tr><tr><td>Price (",
				String.valueOf(tradeList.getPriceForItemId(itemID)),
				")</td><td><edit var=\"price\" width=80></td></tr></table><center><br><button value=\"Save\" action=\"bypass -h admin_editShopItem ",
				String.valueOf(tradeListID),
				" ",
				String.valueOf(itemID),
				" $price\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><button value=\"Back to Shop List\" action=\"bypass -h admin_showShopList ",
				String.valueOf(tradeListID),
				" 1\"  width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(replyMSG);
		activeChar.sendPacket(adminReply);
	}

	private void delShopItem(Player activeChar, String[] args) {
		int tradeListID = Integer.parseInt(args[1]);
		int itemID = Integer.parseInt(args[2]);
		L2TradeList tradeList = TradeController.INSTANCE.getBuyList(tradeListID);

		if (tradeList.getPriceForItemId(itemID) < 0) {
			return;
		}

		if (args.length > 3) {
			int order = findOrderTradeList(itemID, tradeList.getPriceForItemId(itemID), tradeListID);

			tradeList.removeItem(itemID);
			deleteTradeList(tradeListID, order);

			activeChar.sendMessage("Deleted " + ItemTable.getInstance().getTemplate(itemID).getName() + " from Trade List " + tradeListID);
			showShopList(activeChar, tradeListID, 1);
			return;
		}

		final String replyMSG = StringUtil.concat("<html><title>Merchant Shop Item Delete</title><body><br>Delete entry in trade list ",
				String.valueOf(tradeListID),
				"<table width=\"100%\"><tr><td>Item</td><td>",
				ItemTable.getInstance().getTemplate(itemID).getName(),
				" (",
				Integer.toString(itemID),
				")</td></tr><tr><td>Price</td><td>",
				String.valueOf(tradeList.getPriceForItemId(itemID)),
				"</td></tr></table><center><br><button value=\"Delete\" action=\"bypass -h admin_delShopItem ",
				String.valueOf(tradeListID),
				" ",
				String.valueOf(itemID),
				" 1\"  width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><button value=\"Back to Shop List\" action=\"bypass -h admin_showShopList ",
				String.valueOf(tradeListID),
				" 1\"  width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(replyMSG);
		activeChar.sendPacket(adminReply);
	}

	private void addShopItem(Player activeChar, String[] args) {
		int tradeListID = Integer.parseInt(args[1]);

		L2TradeList tradeList = TradeController.INSTANCE.getBuyList(tradeListID);
		if (tradeList == null) {
			activeChar.sendMessage("TradeList not found!");
			return;
		}

		if (args.length > 3) {
			int order = tradeList.getItems().size() + 1; // last item order + 1
			int itemID = Integer.parseInt(args[2]);
			long price = Long.parseLong(args[3]);

			L2TradeItem newItem = new L2TradeItem(tradeListID, itemID);
			newItem.setPrice(price);
			newItem.setMaxCount(-1);
			tradeList.addItem(newItem);
			boolean stored = storeTradeList(itemID, price, tradeListID, order);

			if (stored) {
				activeChar.sendMessage("Added " + ItemTable.getInstance().getTemplate(itemID).getName() + " to Trade List " + tradeList.getListId());
			} else {
				activeChar.sendMessage(
						"Could not add " + ItemTable.getInstance().getTemplate(itemID).getName() + " to Trade List " + tradeList.getListId() + "!");
			}

			showShopList(activeChar, tradeListID, 1);
			return;
		}

		final String replyMSG = StringUtil.concat(
				"<html><title>Merchant Shop Item Add</title><body><br>Add a new entry in merchantList.<table width=\"100%\"><tr><td>ItemID</td><td><edit var=\"itemID\" width=80></td></tr><tr><td>Price</td><td><edit var=\"price\" width=80></td></tr></table><center><br><button value=\"Add\" action=\"bypass -h admin_addShopItem ",
				String.valueOf(tradeListID),
				" $itemID $price\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><button value=\"Back to Shop List\" action=\"bypass -h admin_showShopList ",
				String.valueOf(tradeListID),
				" 1\"  width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(replyMSG);
		activeChar.sendPacket(adminReply);
	}

	private void showShopList(Player activeChar, int tradeListID, int page) {
		L2TradeList tradeList = TradeController.INSTANCE.getBuyList(tradeListID);
		if (page > tradeList.getItems().size() / PAGE_LIMIT + 1 || page < 1) {
			return;
		}

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(itemListHtml(tradeList, page));
		activeChar.sendPacket(adminReply);
	}

	private String itemListHtml(L2TradeList tradeList, int page) {
		final StringBuilder replyMSG = new StringBuilder();

		int max = tradeList.getItems().size() / PAGE_LIMIT;
		if (tradeList.getItems().size() > PAGE_LIMIT * max) {
			max++;
		}

		StringUtil.append(replyMSG,
				"<html><title>Merchant Shop List Page: ",
				String.valueOf(page),
				" of ",
				Integer.toString(max),
				"</title><body><br><center><font color=\"LEVEL\">",
				NpcTable.getInstance().getTemplate(tradeList.getNpcId()).getName(),
				" (",
				String.valueOf(tradeList.getNpcId()),
				") Shop ID: ",
				Integer.toString(tradeList.getListId()),
				"</font></center><table width=300 bgcolor=666666><tr>");

		for (int x = 0; x < max; x++) {
			int pagenr = x + 1;
			if (page == pagenr) {
				replyMSG.append("<td>Page ");
				replyMSG.append(pagenr);
				replyMSG.append("</td>");
			} else {
				replyMSG.append("<td><a action=\"bypass -h admin_showShopList ");
				replyMSG.append(tradeList.getListId());
				replyMSG.append(" ");
				replyMSG.append(x + 1);
				replyMSG.append("\"> Page ");
				replyMSG.append(pagenr);
				replyMSG.append(" </a></td>");
			}
		}

		replyMSG.append("</tr></table><table width=\"100%\"><tr><td width=150>Item</td><td width=60>Price</td><td width=40>Delete</td></tr>");

		int start = (page - 1) * PAGE_LIMIT;
		int end = Math.min((page - 1) * PAGE_LIMIT + PAGE_LIMIT, tradeList.getItems().size());
		//log.info("page: " + page + "; tradeList.getItems().size(): " + tradeList.getItems().size() + "; start: " + start + "; end: " + end + "; max: " + max);
		for (L2TradeItem item : tradeList.getItems(start, end)) {
			StringUtil.append(replyMSG,
					"<tr><td><a action=\"bypass -h admin_editShopItem ",
					String.valueOf(tradeList.getListId()),
					" ",
					String.valueOf(item.getItemId()),
					"\">",
					ItemTable.getInstance().getTemplate(item.getItemId()).getName(),
					"</a></td><td>",
					String.valueOf(item.getPrice()),
					"</td><td><a action=\"bypass -h admin_delShopItem ",
					String.valueOf(tradeList.getListId()),
					" ",
					String.valueOf(item.getItemId()),
					"\">Delete</a></td></tr>");
		}
		StringUtil.append(replyMSG, "<tr><td><br><br></td><td> </td><td> </td></tr><tr>");

		StringUtil.append(replyMSG,
				"</tr></table><center><br><button value=\"Add Shop Item\" action=\"bypass -h admin_addShopItem ",
				String.valueOf(tradeList.getListId()),
				"\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><button value=\"Close\" action=\"bypass -h admin_close_window\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");

		return replyMSG.toString();
	}

	private void showShop(Player activeChar, int merchantID) {
		List<L2TradeList> tradeLists = TradeController.INSTANCE.getBuyListByNpcId(merchantID);
		if (tradeLists == null) {
			activeChar.sendMessage("Unknown npc template Id: " + merchantID);
			return;
		}

		final StringBuilder replyMSG = new StringBuilder();
		StringUtil.append(replyMSG, "<html><title>Merchant Shop Lists</title><body>");

		if (activeChar.getTarget() instanceof MerchantInstance) {
			MerchantPriceConfig mpc = ((MerchantInstance) activeChar.getTarget()).getMpc();
			StringUtil.append(replyMSG,
					"<br>NPC: ",
					activeChar.getTarget().getName(),
					" (",
					Integer.toString(merchantID),
					") <br>Price Config: ",
					mpc.getName(),
					", ",
					Integer.toString(mpc.getBaseTax()),
					"% / ",
					Integer.toString(mpc.getTotalTax()),
					"%");
		}

		StringUtil.append(replyMSG, "<table width=\"100%\">");

		for (L2TradeList tradeList : tradeLists) {
			if (tradeList != null) {
				StringUtil.append(replyMSG,
						"<tr><td><a action=\"bypass -h admin_showShopList ",
						String.valueOf(tradeList.getListId()),
						" 1\">Merchant List ID ",
						String.valueOf(tradeList.getListId()),
						"</a></td></tr>");
			}
		}

		StringUtil.append(replyMSG,
				"</table><center><br><button value=\"Close\" action=\"bypass -h admin_close_window\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private boolean storeTradeList(int itemID, long price, int tradeListID, int order) {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();

			String table = "merchant_buylists";

			PreparedStatement stmt = con.prepareStatement("INSERT INTO `" + table + "`(`item_id`,`price`,`shop_id`,`order`) VALUES (?,?,?,?)");
			stmt.setInt(1, itemID);
			stmt.setLong(2, price);
			stmt.setInt(3, tradeListID);
			stmt.setInt(4, order);
			stmt.execute();
			stmt.close();
		} catch (Exception e) {
			log.warn("Could not store trade list (" + itemID + ", " + price + ", " + tradeListID + ", " + order + "): " + e);
			return false;
		} finally {
			DatabasePool.close(con);
		}
		return true;
	}

	private void updateTradeList(int itemID, long price, int tradeListID, int order) {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();

			PreparedStatement stmt = con.prepareStatement("UPDATE `merchant_buylists` SET `price` = ? WHERE `shop_id` = ? AND `order` = ?");
			stmt.setLong(1, price);
			stmt.setInt(2, tradeListID);
			stmt.setInt(3, order);
			stmt.close();
		} catch (Exception e) {
			log.warn("Could not update trade list (" + itemID + ", " + price + ", " + tradeListID + ", " + order + "): " + e);
		} finally {
			DatabasePool.close(con);
		}
	}

	private void deleteTradeList(int tradeListID, int order) {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();

			PreparedStatement stmt = con.prepareStatement("DELETE FROM `merchant_buylists` WHERE `shop_id` = ? AND `order` = ?");
			stmt.setInt(1, tradeListID);
			stmt.setInt(2, order);
			stmt.close();
		} catch (Exception e) {
			log.warn("Could not delete trade list (" + tradeListID + ", " + order + "): " + e);
		} finally {
			DatabasePool.close(con);
		}
	}

	private int findOrderTradeList(int itemID, long price, int tradeListID) {
		Connection con = null;
		int order = -1;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement stmt =
					con.prepareStatement("SELECT `order` FROM `merchant_buylists` WHERE `shop_id` = ? AND `item_id` = ? AND `price` = ?");
			stmt.setInt(1, tradeListID);
			stmt.setInt(2, itemID);
			stmt.setLong(3, price);
			ResultSet rs = stmt.executeQuery();

			if (rs.first()) {
				order = rs.getInt("order");
			}

			stmt.close();
			rs.close();
		} catch (Exception e) {
			log.warn("Could not get order for (" + itemID + ", " + price + ", " + tradeListID + "): " + e);
		} finally {
			DatabasePool.close(con);
		}
		return order;
	}

	@Override
	public String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}

	private void showNpcProperty(Player activeChar, NpcTemplate npc) {
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		String content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "admin/editnpc.htm");

		if (content != null) {
			adminReply.setHtml(content);
			adminReply.replace("%npcId%", String.valueOf(npc.NpcId));
			adminReply.replace("%templateId%", String.valueOf(npc.TemplateId));
			adminReply.replace("%name%", npc.Name);
			adminReply.replace("%serverSideName%", npc.ServerSideName == true ? "1" : "0");
			adminReply.replace("%title%", npc.Title);
			adminReply.replace("%serverSideTitle%", npc.ServerSideTitle == true ? "1" : "0");
			adminReply.replace("%collisionRadius%", String.valueOf(npc.getFCollisionRadius()));
			adminReply.replace("%collisionHeight%", String.valueOf(npc.getFCollisionHeight()));
			adminReply.replace("%level%", String.valueOf(npc.Level));
			adminReply.replace("%type%", String.valueOf(npc.Type));
			adminReply.replace("%attackRange%", String.valueOf(npc.getBaseAtkRange()));
			adminReply.replace("%hp%", String.valueOf(npc.getBaseHpMax()));
			adminReply.replace("%mp%", String.valueOf(npc.getBaseMpMax()));
			adminReply.replace("%hpRegen%", String.valueOf(npc.getBaseHpReg()));
			adminReply.replace("%mpRegen%", String.valueOf(npc.getBaseMpReg()));
			adminReply.replace("%str%", String.valueOf(npc.getBaseSTR()));
			adminReply.replace("%con%", String.valueOf(npc.getBaseCON()));
			adminReply.replace("%dex%", String.valueOf(npc.getBaseDEX()));
			adminReply.replace("%int%", String.valueOf(npc.getBaseINT()));
			adminReply.replace("%wit%", String.valueOf(npc.getBaseWIT()));
			adminReply.replace("%men%", String.valueOf(npc.getBaseMEN()));
			adminReply.replace("%exp%", String.valueOf(npc.RewardExp));
			adminReply.replace("%sp%", String.valueOf(npc.RewardSp));
			adminReply.replace("%pAtk%", String.valueOf(npc.getBasePAtk()));
			adminReply.replace("%pDef%", String.valueOf(npc.getBasePDef()));
			adminReply.replace("%mAtk%", String.valueOf(npc.getBaseMAtk()));
			adminReply.replace("%mDef%", String.valueOf(npc.getBaseMDef()));
			adminReply.replace("%pAtkSpd%", String.valueOf(npc.getBasePAtkSpd()));
			adminReply.replace("%aggro%", String.valueOf(npc.AggroRange));
			adminReply.replace("%mAtkSpd%", String.valueOf(npc.getBaseMAtkSpd()));
			adminReply.replace("%rHand%", String.valueOf(npc.RHand));
			adminReply.replace("%lHand%", String.valueOf(npc.LHand));
			adminReply.replace("%enchant%", String.valueOf(npc.EnchantEffect));
			adminReply.replace("%walkSpd%", String.valueOf(npc.getBaseWalkSpd()));
			adminReply.replace("%runSpd%", String.valueOf(npc.getBaseRunSpd()));
			adminReply.replace("%factionId%", npc.getAIData().getClan() == null ? "" : npc.getAIData().getClan());
			adminReply.replace("%factionRange%", String.valueOf(npc.getAIData().getClanRange()));
		} else {
			adminReply.setHtml("<html><head><body>File not found: data/html/admin/editnpc.htm</body></html>");
		}
		activeChar.sendPacket(adminReply);
	}

	private void saveNpcProperty(Player activeChar, String command) {
		String[] commandSplit = command.split(" ");

		if (commandSplit.length < 4) {
			return;
		}

		try {
			int npcId = Integer.valueOf(commandSplit[1]);
			NpcTemplate npc = NpcTable.getInstance().getTemplate(npcId);

			String statToSet = commandSplit[2];
			String value = commandSplit[3];

			if (commandSplit.length > 4) {
				for (int i = 0; i < commandSplit.length - 3; i++) {
					value += " " + commandSplit[i + 4];
				}
			}

			if (statToSet.equals("templateId")) {
				npc.TemplateId = Integer.parseInt(value);
			} else if (statToSet.equals("name")) {
				npc.Name = value;
			} else if (statToSet.equals("serverSideName")) {
				npc.ServerSideName = Boolean.parseBoolean(value);
			} else if (statToSet.equals("title")) {
				npc.Title = value;
			} else if (statToSet.equals("serverSideTitle")) {
				npc.ServerSideTitle = Boolean.parseBoolean(value);
			} else if (statToSet.equals("collisionRadius")) {
				npc.setFCollisionRadius(Float.parseFloat(value));
			} else if (statToSet.equals("collisionHeight")) {
				npc.setFCollisionHeight(Float.parseFloat(value));
			} else if (statToSet.equals("level")) {
				npc.Level = Byte.parseByte(value);
			} else if (statToSet.equals("type")) {
				Class.forName("l2server.gameserver.model.actor.instance." + value + "Instance");
				npc.Type = value;
			} else if (statToSet.equals("attackRange")) {
				npc.setBaseAtkRange(Integer.valueOf(value));
			} else if (statToSet.equals("hp")) {
				npc.setBaseHpMax(Integer.valueOf(value));
			} else if (statToSet.equals("mp")) {
				npc.setBaseMpMax(Integer.valueOf(value));
			} else if (statToSet.equals("hpRegen")) {
				npc.setBaseHpReg(Integer.valueOf(value));
			} else if (statToSet.equals("mpRegen")) {
				npc.setBaseMpReg(Integer.valueOf(value));
			} else if (statToSet.equals("str")) {
				npc.setBaseSTR(Integer.valueOf(value));
			} else if (statToSet.equals("con")) {
				npc.setBaseCON(Integer.valueOf(value));
			} else if (statToSet.equals("dex")) {
				npc.setBaseDEX(Integer.valueOf(value));
			} else if (statToSet.equals("int")) {
				npc.setBaseINT(Integer.valueOf(value));
			} else if (statToSet.equals("wit")) {
				npc.setBaseWIT(Integer.valueOf(value));
			} else if (statToSet.equals("men")) {
				npc.setBaseMEN(Integer.valueOf(value));
			} else if (statToSet.equals("exp")) {
				npc.RewardExp = Long.valueOf(value);
			} else if (statToSet.equals("sp")) {
				npc.RewardSp = Long.valueOf(value);
			} else if (statToSet.equals("pAtk")) {
				npc.setBasePAtk(Integer.valueOf(value));
			} else if (statToSet.equals("pDef")) {
				npc.setBasePDef(Integer.valueOf(value));
			} else if (statToSet.equals("mAtk")) {
				npc.setBaseMAtk(Integer.valueOf(value));
			} else if (statToSet.equals("mDef")) {
				npc.setBaseMDef(Integer.valueOf(value));
			} else if (statToSet.equals("pAtkSpd")) {
				npc.setBasePAtkSpd(Integer.valueOf(value));
			} else if (statToSet.equals("aggro")) {
				npc.AggroRange = Integer.valueOf(value);
				npc.Aggressive = npc.AggroRange > 0;
			} else if (statToSet.equals("mAtkSpd")) {
				npc.setBaseMAtkSpd(Integer.valueOf(value));
			} else if (statToSet.equals("rHand")) {
				npc.RHand = Integer.valueOf(value);
			} else if (statToSet.equals("lHand")) {
				npc.LHand = Integer.valueOf(value);
			} else if (statToSet.equals("walkSpd")) {
				npc.setBaseWalkSpd(Integer.valueOf(value));
			} else if (statToSet.equals("runSpd")) {
				npc.setBaseRunSpd(Integer.valueOf(value));
			}

			showNpcProperty(activeChar, NpcTable.getInstance().getTemplate(npcId));
		} catch (Exception e) {
			activeChar.sendMessage("Could not save npc property!");
			log.warn("Error saving new npc value (" + command + "): " + e);
		}
	}

	private void showNpcSkillList(Player activeChar, int npcId, int page) {
		NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
		if (npcData == null) {
			activeChar.sendMessage("Template id unknown: " + npcId);
			return;
		}

		Map<Integer, Skill> skills = new HashMap<Integer, Skill>();
		if (npcData.getSkills() != null) {
			skills = npcData.getSkills();
		}

		int skillsize = skills.size();

		int MaxSkillsPerPage = PAGE_LIMIT;
		int MaxPages = skillsize / MaxSkillsPerPage;
		if (skillsize > MaxSkillsPerPage * MaxPages) {
			MaxPages++;
		}

		if (page > MaxPages) {
			page = MaxPages;
		}

		int SkillsStart = MaxSkillsPerPage * page;
		int SkillsEnd = skillsize;
		if (SkillsEnd - SkillsStart > MaxSkillsPerPage) {
			SkillsEnd = SkillsStart + MaxSkillsPerPage;
		}

		StringBuffer replyMSG = new StringBuffer("<html><title>Show NPC Skill List</title><body><center><font color=\"LEVEL\">");
		replyMSG.append(npcData.getName());
		replyMSG.append(" (");
		replyMSG.append(npcData.NpcId);
		replyMSG.append("): ");
		replyMSG.append(skillsize);
		replyMSG.append(" skills</font></center><table width=300 bgcolor=666666><tr>");

		for (int x = 0; x < MaxPages; x++) {
			int pagenr = x + 1;
			if (page == x) {
				replyMSG.append("<td>Page ");
				replyMSG.append(pagenr);
				replyMSG.append("</td>");
			} else {
				replyMSG.append("<td><a action=\"bypass -h admin_show_skilllist_npc ");
				replyMSG.append(npcData.NpcId);
				replyMSG.append(" ");
				replyMSG.append(x);
				replyMSG.append("\"> Page ");
				replyMSG.append(pagenr);
				replyMSG.append(" </a></td>");
			}
		}
		replyMSG.append("</tr></table><table width=\"100%\" border=0><tr><td>Skill name [skill id-skill lvl]</td><td>Delete</td></tr>");

		Set<Integer> skillset = skills.keySet();
		Iterator<Integer> skillite = skillset.iterator();
		int skillobj = 0;

		for (int i = 0; i < SkillsStart; i++) {
			if (skillite.hasNext()) {
				skillite.next();
			}
		}

		int cnt = SkillsStart;
		while (skillite.hasNext()) {
			cnt++;
			if (cnt > SkillsEnd) {
				break;
			}

			skillobj = skillite.next();
			replyMSG.append("<tr><td width=240><a action=\"bypass -h admin_edit_skill_npc ");
			replyMSG.append(npcData.NpcId);
			replyMSG.append(" ");
			replyMSG.append(skills.get(skillobj).getId());
			replyMSG.append("\">");
			if (skills.get(skillobj).getSkillType() == SkillType.NOTDONE) {
				replyMSG.append("<font color=\"777777\">" + skills.get(skillobj).getName() + "</font>");
			} else {
				replyMSG.append(skills.get(skillobj).getName());
			}
			replyMSG.append(" [");
			replyMSG.append(skills.get(skillobj).getId());
			replyMSG.append("-");
			replyMSG.append(skills.get(skillobj).getLevel());
			replyMSG.append("]</a></td><td width=60><a action=\"bypass -h admin_del_skill_npc ");
			replyMSG.append(npcData.NpcId);
			replyMSG.append(" ");
			replyMSG.append(skillobj);
			replyMSG.append("\">Delete</a></td></tr>");
		}
		replyMSG.append("</table><br><center><button value=\"Add Skill\" action=\"bypass -h admin_add_skill_npc ");
		replyMSG.append(npcId);
		replyMSG.append(
				"\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><button value=\"Close\" action=\"bypass -h admin_close_window\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void showNpcSkillEdit(Player activeChar, int npcId, int skillId) {
		try {
			StringBuffer replyMSG = new StringBuffer("<html><title>NPC Skill Edit</title><body>");

			NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
			if (npcData == null) {
				activeChar.sendMessage("Template id unknown: " + npcId);
				return;
			}
			if (npcData.getSkills() == null) {
				return;
			}

			Skill npcSkill = npcData.getSkills().get(skillId);

			if (npcSkill != null) {
				replyMSG.append("<table width=\"100%\"><tr><td>NPC: </td><td>");
				replyMSG.append(NpcTable.getInstance().getTemplate(npcId).getName());
				replyMSG.append(" (");
				replyMSG.append(npcId);
				replyMSG.append(")</td></tr><tr><td>Skill: </td><td>");
				replyMSG.append(npcSkill.getName());
				replyMSG.append(" (");
				replyMSG.append(skillId);
				replyMSG.append(")</td></tr><tr><td>Skill Lvl: (");
				replyMSG.append(npcSkill.getLevel());
				replyMSG.append(
						") </td><td><edit var=\"level\" width=50></td></tr></table><br><center><button value=\"Save\" action=\"bypass -h admin_edit_skill_npc ");
				replyMSG.append(npcId);
				replyMSG.append(" ");
				replyMSG.append(skillId);
				replyMSG.append(
						" $level\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br1><button value=\"Back to SkillList\" action=\"bypass -h admin_show_skilllist_npc ");
				replyMSG.append(npcId);
				replyMSG.append("\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>");
			}

			replyMSG.append("</body></html>");

			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(adminReply);
		} catch (Exception e) {
			activeChar.sendMessage("Could not edit npc skills!");
			log.warn("Error while editing npc skills (" + npcId + ", " + skillId + "): " + e);
		}
	}

	private void showNpcSkillAdd(Player activeChar, int npcId) {
		NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);

		StringBuffer replyMSG = new StringBuffer("<html><title>NPC Skill Add</title><body><table width=\"100%\"><tr><td>NPC: </td><td>");
		replyMSG.append(npcData.getName());
		replyMSG.append(" (");
		replyMSG.append(npcData.NpcId);
		replyMSG.append(
				")</td></tr><tr><td>SkillId: </td><td><edit var=\"skillId\" width=80></td></tr><tr><td>Level: </td><td><edit var=\"level\" width=80></td></tr></table><br><center><button value=\"Add Skill\" action=\"bypass -h admin_add_skill_npc ");
		replyMSG.append(npcData.NpcId);
		replyMSG.append(
				" $skillId $level\"  width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br1><button value=\"Back to SkillList\" action=\"bypass -h admin_show_skilllist_npc ");
		replyMSG.append(npcData.NpcId);
		replyMSG.append("\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	private void addNpcSkillData(Player activeChar, int npcId, int skillId, int level) {
		try {
			// skill check
			Skill skillData = SkillTable.getInstance().getInfo(skillId, level);
			if (skillData == null) {
				activeChar.sendMessage("Could not add npc skill: not existing skill id with that level!");
				showNpcSkillAdd(activeChar, npcId);
				return;
			}

			NpcTable.getInstance().getTemplate(npcId).addSkill(skillData);

			showNpcSkillList(activeChar, npcId, 0);
			activeChar.sendMessage("Added skill " + skillId + "-" + level + " to npc id " + npcId + ".");
		} catch (Exception e) {
			activeChar.sendMessage("Could not add npc skill!");
			log.warn("Error while adding a npc skill (" + npcId + ", " + skillId + ", " + level + "): ");
			e.printStackTrace();
		}
	}

	private void deleteNpcSkillData(Player activeChar, int npcId, int skillId) {
		if (npcId <= 0) {
			return;
		}

		try {
			NpcTable.getInstance().getTemplate(npcId).getSkills().remove(skillId);

			showNpcSkillList(activeChar, npcId, 0);
			activeChar.sendMessage("Deleted skill id " + skillId + " from npc id " + npcId + ".");
		} catch (Exception e) {
			activeChar.sendMessage("Could not delete npc skill!");
			log.warn("Error while deleting npc skill (" + npcId + ", " + skillId + "): " + e);
		}
	}
}
