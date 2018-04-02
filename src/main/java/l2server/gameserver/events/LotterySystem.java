/*
 * Copyright (C) 2004-2013 L2J Server
 *
 * This file is part of L2J Server.
 *
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.events;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.communitybbs.Manager.CustomCommunityBoard;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.instancemanager.MailManager;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Message;
import l2server.gameserver.model.itemcontainer.Mail;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.util.Rnd;
import l2server.util.loader.annotations.Load;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

/**
 * @author LasTravel
 */

public class LotterySystem {
	private static Logger log = LoggerFactory.getLogger(LotterySystem.class.getName());


	private static Map<Integer, List<Integer>> allNumbers = new HashMap<>();
	private static final String LOAD_LOTTERY = "SELECT `ownerId`, `numbers` FROM `lottery_data`";
	private static final String SAVE_LOTTERY = "INSERT INTO lottery_data(ownerId, numbers) VALUES (?, ?) ON DUPLICATE KEY UPDATE numbers=?";
	private static long collectedCoins = 0;
	protected static ScheduledFuture<?> saveTask;

	public void buyNumber(Player pl, int number) {
		if (pl == null) {
			return;
		}

		if (!pl.getClient().getFloodProtectors().getTransaction().tryPerformAction("buy")) {
			return;
		}

		List<Integer> numbers = new ArrayList<>();
		if (allNumbers.get(pl.getObjectId()) != null) {
			numbers = allNumbers.get(pl.getObjectId());
			if (numbers.contains(number)) {
				pl.sendMessage("You already have this number!");
				return;
			}
		}

		if (pl.getPrivateStoreType() != 0 || pl.isInCrystallize()) {
			pl.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			return;
		}

		if (!pl.destroyItemByItemId("Lottery System", Config.CUSTOM_LOTTERY_PRICE_ITEM_ID, Config.CUSTOM_LOTTERY_PRICE_AMOUNT, pl, true)) {
			return;
		}

		collectedCoins += Config.CUSTOM_LOTTERY_PRICE_AMOUNT;
		numbers.add(number);
		allNumbers.put(pl.getObjectId(), numbers);
		pl.sendMessage("You bought the number " + number + " correctly!");

		CustomCommunityBoard.getInstance().parseCmd("_bbscustom;lottery", pl);

		//Manage few announcements
		long totalReward = collectedCoins * Config.CUSTOM_LOTTERY_REWARD_MULTIPLIER;
		if (totalReward % 100000000 == 0) {
			Announcements.getInstance()
					.announceToAll(
							"Lottery System: The next prize has been reached: " + NumberFormat.getNumberInstance(Locale.US).format(totalReward) +
									" Adena!");
		}
	}

	public void giveRewardsAndReset() {
		int luckyNumber = Rnd.get(1, 99);
		long totalCoins = collectedCoins;

		List<String> winnerNames = new ArrayList<>();
		for (Map.Entry<Integer, List<Integer>> entry : allNumbers.entrySet()) {
			if (entry == null) {
				continue;
			}
			if (entry.getValue().contains(luckyNumber)) {
				String winnerName = CharNameTable.getInstance().getNameById(entry.getKey());
				if (winnerName != null) {
					winnerNames.add(winnerName);
				}
			}
		}

		if (winnerNames.isEmpty()) {
			Announcements.getInstance()
					.announceToAll("Lottery System: The Lottery ends with: " + allNumbers.size() + " participants! " + luckyNumber +
							" was the winner number, no one won the lottery! Let's see if you're luckier the next time!");
		} else {
			long eachReward = totalCoins * Config.CUSTOM_LOTTERY_REWARD_MULTIPLIER / winnerNames.size();
			if (eachReward < 0) //Afaik shouldn't happens never
			{
				log.info("LotterySystem: Smth has been fucked on the reward calculation: " + eachReward);
				eachReward = Config.CUSTOM_LOTTERY_PRICE_AMOUNT;
			}

			for (String name : winnerNames) {
				int charid = CharNameTable.getInstance().getIdByName(name);
				Message msg = new Message(-1,
						charid,
						false,
						"Lottery System",
						"Congrats! You win the lottery with the number " + String.valueOf(luckyNumber) + "!",
						0);

				Mail attachments = msg.createAttachments();
				attachments.addItem("Lottery System", Config.CUSTOM_LOTTERY_PRICE_ITEM_ID, eachReward, null, null);
				MailManager.getInstance().sendMessage(msg);

				log.info("LotterySystem: Player: " + name + ", rewarded with: " + NumberFormat.getNumberInstance(Locale.US).format(eachReward) +
						" Adena!");
			}

			// Announce
			Announcements.getInstance()
					.announceToAll("Lottery System: The Lotery ends with: " + allNumbers.size() + " participants (" +
							totalCoins / Config.CUSTOM_LOTTERY_PRICE_AMOUNT + " numbers bought)! " + luckyNumber + " is the winner number! " +
							winnerNames.size() + " winners has been rewarded with: " + NumberFormat.getNumberInstance(Locale.US).format(eachReward) +
							" Adena!");

			log.info("LotterySystem: " + luckyNumber + " was the winner number, lottery ends with total coins: " + totalCoins + " and " +
					winnerNames.size() + " winners (" + allNumbers.size() + " participants), with: " + eachReward + " coins for each player!");
		}

		log.warn("Lottery System: Cleaning info...!");

		allNumbers.clear();
		collectedCoins = 0;

		truncateTable();
	}

	private void truncateTable() {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = null;
			statement = con.prepareStatement("DELETE FROM lottery_data");
			statement.executeUpdate();
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	@Load
	private void load() {
		if (!Config.ENABLE_CUSTOM_LOTTERY) {
			return;
		}
		log.info("LotterySystem: Loading lottery information..!");
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement(LOAD_LOTTERY);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				List<Integer> numbers = new ArrayList<>();
				StringTokenizer st = new StringTokenizer(rs.getString("numbers"), ",");

				while (st.hasMoreTokens()) {
					numbers.add(Integer.valueOf(st.nextToken().trim()));
				}

				allNumbers.put(rs.getInt("ownerId"), numbers);
				collectedCoins += numbers.size() * Config.CUSTOM_LOTTERY_PRICE_AMOUNT;
			}
			rs.close();
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			L2DatabaseFactory.close(con);
		}

		saveTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this::saveData, 3600000, 3600000);
	}

	public void saveData() {
		if (allNumbers.isEmpty()) {
			return;
		}

		log.debug("LotterySystem: Saving lottery information..!");
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = null;

			for (Map.Entry<Integer, List<Integer>> entry : allNumbers.entrySet()) {
				if (entry == null) {
					continue;
				}

				statement = con.prepareStatement(SAVE_LOTTERY);
				statement.setInt(1, entry.getKey());

				String numbers = "";
				int size = entry.getValue().size();
				for (int a : entry.getValue()) {
					numbers = numbers + String.valueOf(a) + (size > 1 ? ", " : "");
					size -= 1;
				}

				statement.setString(2, numbers);
				statement.setString(3, numbers);
				statement.executeUpdate();
				statement.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	public long getTotalCoins() {
		return collectedCoins;
	}

	public long getTotalPrize() {
		return collectedCoins * Config.CUSTOM_LOTTERY_REWARD_MULTIPLIER;
	}

	public String getAvailableNumbers(Player pl) {
		List<Integer> playerNumbers = allNumbers.get(pl.getObjectId());

		StringBuilder sb = new StringBuilder();
		sb.append("<table width=320>");

		int b = 1;
		for (int i = 1; i <= 100; i++) {
			if (b == 1) {
				sb.append("<tr>");
			}

			if (playerNumbers != null && playerNumbers.contains(i)) {
				sb.append("<td><font color=LEVEL>" + i + "</font></td>");
			} else {
				sb.append("<td><a action=\"bypass _bbscustom;action;buyNumber;" + i + "\">" + i + "</a></td>");
			}

			if (b % 10 == 0) {
				sb.append("</tr>");
				sb.append("<tr></tr><tr></tr>");
				if (i < 100) {
					sb.append("<tr>");
				}
				b = 1;
			} else {
				b++;
			}
		}
		sb.append("</table>");
		return sb.toString();
	}

	private LotterySystem() {
	}

	public static LotterySystem getInstance() {
		return SingletonHolder.instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final LotterySystem instance = new LotterySystem();
	}

	public NpcHtmlMessage parseLotteryPanel(Player pl, NpcHtmlMessage htmlPage) {
		// TODO Auto-generated method stub
		return null;
	}
}
