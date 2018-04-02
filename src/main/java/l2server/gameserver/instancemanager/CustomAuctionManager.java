package l2server.gameserver.instancemanager;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.instance.NpcInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Message;
import l2server.gameserver.model.itemcontainer.Mail;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;
import l2server.util.loader.annotations.Load;
import l2server.util.loader.annotations.Reload;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author LasTravel
 * @author Pere
 */

public class CustomAuctionManager {
	private static Logger log = LoggerFactory.getLogger(CustomAuctionManager.class.getName());


	private static final long ADDED_DURATION = 3600000L;

	private enum Currency {
		Adena(57),
		GoldenApiga(9143);

		private final int ItemId;

		Currency(int itemId) {
			ItemId = itemId;
		}
	}

	/**
	 * All the item auction info
	 */
	private class AuctionTemplate {
		private int id;
		private int[] itemId;
		private int count;
		private int repeatTime;
		private int randomRepeatTime;
		private int initialCurrencyId;
		private int highestCurrencyId;
		private long initialPrice;
		private int initialDuration;

		private AuctionTemplate(int auctionId,
		                        int[] itemId,
		                        int count,
		                        int repeatTime,
		                        int randomRepeatTime,
		                        int initialCurrency,
		                        int highestCurrency,
		                        long initialPrice,
		                        int initialDuration,
		                        boolean init) {
			id = auctionId;
			this.itemId = itemId;
			this.count = count;
			this.repeatTime = repeatTime;
			this.randomRepeatTime = randomRepeatTime;
			initialCurrencyId = initialCurrency;
			highestCurrencyId = highestCurrency;
			this.initialPrice = initialPrice;
			this.initialDuration = initialDuration;

			if (init) {
				initialize();
			}
		}

		private void initialize() {
			if (repeatTime == 0) {
				System.out.println("The auction " + id + " will not repeat.");
				return;
			}

			long nextAuction = 0;
			Connection con = null;
			try {
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT lastAuctionCreation FROM `custom_auction_templates` WHERE id = ?");
				statement.setInt(1, id);
				ResultSet rs = statement.executeQuery();
				if (rs.next()) {
					long lastAuctionCreation = rs.getLong("lastAuctionCreation");
					nextAuction = lastAuctionCreation + (initialDuration + repeatTime) * 1000L + Rnd.get(randomRepeatTime) * 1000L -
							System.currentTimeMillis();
					if (nextAuction < 0) {
						nextAuction = 0;
					}
				}
				statement.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				L2DatabaseFactory.close(con);
			}

			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
				@Override
				public void run() {
					startAuction(AuctionTemplate.this.getId());

					Connection con = null;
					try {
						con = L2DatabaseFactory.getInstance().getConnection();
						PreparedStatement statement =
								con.prepareStatement("REPLACE INTO `custom_auction_templates` (id, lastAuctionCreation) VALUES (?, ?)");
						statement.setInt(1, id);
						statement.setLong(2, System.currentTimeMillis());
						statement.execute();
						statement.close();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						L2DatabaseFactory.close(con);
					}

					ThreadPoolManager.getInstance().scheduleGeneral(this, repeatTime * 1000L + Rnd.get(randomRepeatTime) * 1000L);
				}
			}, nextAuction);
		}

		private int getId() {
			return id;
		}

		private int getRandomItemId() {
			return itemId[Rnd.get(itemId.length)];
		}

		private int getCount() {
			return count;
		}

		@SuppressWarnings("unused")
		private int getRepeatTime() {
			return repeatTime;
		}

		private int getInitialCurrencyId() {
			return initialCurrencyId;
		}

		private int getHighestCurrencyId() {
			return highestCurrencyId;
		}

		private long getInitialPrice() {
			return initialPrice;
		}

		private int getInitialDuration() {
			return initialDuration;
		}
	}

	/**
	 * All the current auction item list
	 */
	private class Auction {
		private final int id;
		private final int itemId;
		private final AuctionTemplate template;
		private int currentAuctionOwner;
		private int currentCurrencyId;
		private long currentPrice;
		private ScheduledFuture<?> endAuctionTask;

		private Auction(int auctionId, int itemId, AuctionTemplate info) {
			id = auctionId;
			this.itemId = itemId;
			template = info;

			currentCurrencyId = info.getInitialCurrencyId();
			currentPrice = info.getInitialPrice();

			//Set the end task?
			endAuctionTask = ThreadPoolManager.getInstance().scheduleGeneral(new CurrentAuctionEnd(), info.getInitialDuration() * 1000L);

			Connection con = null;
			try {
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(
						"INSERT INTO `custom_auctions` (id, itemId, templateId, currencyId, currentBid, ownerId, endTime) VALUES (?, ?, ?, ?, ?, ?, ?)");
				statement.setInt(1, id);
				statement.setInt(2, itemId);
				statement.setInt(3, template.getId());
				statement.setInt(4, currentCurrencyId);
				statement.setLong(5, currentPrice);
				statement.setInt(6, currentAuctionOwner);
				statement.setLong(7, (System.currentTimeMillis() + getRemainingTime()) / 1000L);
				statement.execute();
				statement.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				L2DatabaseFactory.close(con);
			}
		}

		private Auction(int auctionId, int itemId, AuctionTemplate info, int currencyId, long currentBid, int ownerId, long remainingTime) {
			id = auctionId;
			this.itemId = itemId;
			template = info;

			currentCurrencyId = currencyId;
			currentPrice = currentBid;
			currentAuctionOwner = ownerId;

			//Set the end task?
			endAuctionTask = ThreadPoolManager.getInstance().scheduleGeneral(new CurrentAuctionEnd(), remainingTime);
		}

		private int getId() {
			return id;
		}

		private int getItemId() {
			return itemId;
		}

		private AuctionTemplate getTemplate() {
			return template;
		}

		@SuppressWarnings("unused")
		private ScheduledFuture<?> getEndTask() {
			return endAuctionTask;
		}

		private String getRemainingTimeString() {
			Long remainingTime = endAuctionTask.getDelay(TimeUnit.MILLISECONDS) / 1000;

			int hours = (int) (remainingTime / 3600);
			int minutes = (int) (remainingTime % 3600 / 60);
			int seconds = (int) (remainingTime % 60);

			return hours + "h " + minutes + "m " + seconds + "s";
		}

		private Long getRemainingTime() {
			return endAuctionTask.getDelay(TimeUnit.MILLISECONDS);
		}

		private String getCurrentOwnerName() {
			if (currentAuctionOwner == 0) {
				return "None";
			}

			return CharNameTable.getInstance().getNameById(currentAuctionOwner);
		}

		private int getCurrentOwnerId() {
			return currentAuctionOwner;
		}

		private int getCurrentCurrency() {
			return currentCurrencyId;
		}

		private long getCurrentPrice() {
			return currentPrice;
		}

		public void setCurrentCurrency(int itemId) {
			currentCurrencyId = itemId;
		}

		private void setOwner(int playerId, long playerBid) {
			currentAuctionOwner = playerId;
			currentPrice = playerBid;

			if (getRemainingTime() < ADDED_DURATION) {
				endAuctionTask.cancel(true);
				endAuctionTask = ThreadPoolManager.getInstance().scheduleGeneral(new CurrentAuctionEnd(), ADDED_DURATION);
			}

			Connection con = null;
			try {
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement =
						con.prepareStatement("UPDATE `custom_auctions` SET currencyId = ?, currentBid = ?, ownerId = ?, endTime = ? WHERE id = ?");
				statement.setInt(1, currentCurrencyId);
				statement.setLong(2, currentPrice);
				statement.setInt(3, currentAuctionOwner);
				statement.setLong(4, (System.currentTimeMillis() + getRemainingTime()) / 1000L);
				statement.setInt(5, id);
				statement.execute();
				statement.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				L2DatabaseFactory.close(con);
			}
		}

		private void endAuction() {
			auctions.remove(id);

			giveItemToPlayer(getCurrentOwnerId(), getItemId(), getTemplate().getCount(), "Congrats! You've won this bid!");

			Connection con = null;
			try {
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("DELETE FROM `custom_auctions` WHERE id = ?");
				statement.setInt(1, id);
				statement.execute();
				statement.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				L2DatabaseFactory.close(con);
			}

			String winnerName = CharNameTable.getInstance().getNameById(getCurrentOwnerId());
			String currencyName = ItemTable.getInstance().getTemplate(getCurrentCurrency()).getName();
			String itemName = ItemTable.getInstance().getTemplate(getItemId()).getName();

			Util.logToFile(winnerName + " won the auction for " + itemName + " by bidding " + getCurrentPrice() + " " + currencyName + ".",
					"Auction_Logs",
					"txt",
					true,
					true);
		}

		private class CurrentAuctionEnd implements Runnable {
			@Override
			public void run() {
				endAuction();
			}
		}
	}

	private static Map<Integer, Auction> auctions = new HashMap<>(); //Current auctions
	private static Map<Integer, AuctionTemplate> auctionTemplates = new HashMap<>();
	//All the auction info

	public final void startAuction(final int templateId) {
		int auctionId = IdFactory.getInstance().getNextId();

		AuctionTemplate auctionTemplate = auctionTemplates.get(templateId);

		int itemId = auctionTemplate.getRandomItemId();

		auctions.put(auctionId, new Auction(auctionId, itemId, auctionTemplate));

		NpcInstance auctionManager = null;
		for (Player player : World.getInstance().getAllPlayers().values()) {
			if (auctionManager == null) {
				auctionManager = Util.getNpcCloseTo(33782, player);
			}

			player.sendPacket(new CreatureSay(auctionManager == null ? 0x00 : auctionManager.getObjectId(),
					1,
					"Jolie",
					"I've got a new item (" + (auctionTemplate.getCount() != 0 ? auctionTemplate.getCount() + "x " : "") +
							ItemTable.getInstance().getTemplate(itemId).getName() + ") up for auctions!" +
							(auctionTemplate.getHighestCurrencyId() == 57 ? " Adenas only!" :
									auctionTemplate.getHighestCurrencyId() == 50009 ? " Raid Hearts only!" : "")));
		}
	}

	public final Auction getAuctionById(final int auctionId) {
		if (!auctions.containsKey(auctionId)) {
			return null;
		}

		return auctions.get(auctionId);
	}

	/**
	 * This is the community page, where we display all the auction item information
	 *
	 */
	public String getAuctionInfo(int playerId) {
		StringBuilder sb = new StringBuilder();

		int count = 0;
		int size = auctions.size();

		if (size == 0) {
			return sb.append(
					"<center><table><tr><td width=200><font color=LEVEL>There are no auctions at this time!</font></td></tr></table></center>")
					.toString();
		}

		sb.append("<table width=700>");

		for (Entry<Integer, Auction> currentAuction : auctions.entrySet()) {
			Auction currentAuctionInfo = currentAuction.getValue();

			if (currentAuctionInfo == null) {
				continue;
			}

			if (count == 0) {
				sb.append("<tr><td>");
			} else {
				sb.append("<td>");
			}

			sb.append("<table height=180 background=L2UI_CH3.refinewnd_back_Pattern><tr><td FIXWIDTH=80></td>"); //<img src=\""+currentAuctionInfio.getItemTemplate().getIcon()+"\" width=32 height=32>
			sb.append("<td><br><table cellpadding=0><tr><td FIXWIDTH=375><font color=LEVEL>" +
					ItemTable.getInstance().getTemplate(currentAuctionInfo.getItemId()).getName() + " (" +
					currentAuctionInfo.getTemplate().getCount() + ")</font>");
			sb.append("</td></tr><tr><td>Remaining Time: </td><td FIXWIDTH=375>" + currentAuctionInfo.getRemainingTimeString() +
					"</td></tr><tr><td>Current Owner:</td><td>" + currentAuctionInfo.getCurrentOwnerName() + "</td></tr>");

			String currencyName = ItemTable.getInstance().getTemplate(currentAuctionInfo.getCurrentCurrency()).getName();

			long maxAdenaPrice = (long) 90 * 1000000000; // 99kkk
			if (currentAuctionInfo.getCurrentCurrency() == 57 && currentAuctionInfo.getCurrentPrice() > maxAdenaPrice) {
				currencyName = "Dreams Coin";
			}

			sb.append("<tr><td>Currency:</td><td>" + currencyName + "</td></tr>");
			sb.append("<tr><td>Current Bid:</td><td>" + currentAuctionInfo.getCurrentPrice() + "</td></tr>");

			int curId = 0;
			for (Currency cur : Currency.values()) {
				if (currentAuctionInfo.getCurrentCurrency() == cur.ItemId) {
					break;
				}

				curId++;
			}

			String options = "";
			for (int index = curId; index < Currency.values().length; index++) {
				options += Currency.values()[index] + ";";
			}

			if (!options.isEmpty() && curId < Currency.values().length) {
				options = options.substring(0, options.length() - 1);
				sb.append("<tr><td>Select:</td><td><combobox width=100 height=17 var=\"plcoin" + currentAuction.getKey() + "\" list=" + options +
						"></td></tr>");
			}

			sb.append("<tr><td>Your bid:</td><td><edit var=\"plbid" + currentAuction.getKey() + "\" width=100 type=number length=14></td></tr>");
			sb.append("<tr><td></td><td><br><button action=\"bypass _bbscustom;action;bid;" + currentAuction.getKey() + "; $plbid" +
					currentAuction.getKey() + " ; $plcoin" + currentAuction.getKey() +
					"\" value=BID! width=105 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></td></tr>");
			sb.append("<tr><td></td><td></td></tr></table><br><br></td><td FIXWIDTH=80></td></tr>");
			sb.append("</table><br>");

			count++;
			size--;

			if (count == 2 || size == 0) {
				sb.append("</td></tr>");
				count = 0;
			} else {
				sb.append("</td>");
			}
		}

		sb.append("</table>");
		return sb.toString();
	}
	
	@Load
	public void load() {
		load(false);
	}
	
	@Reload("auctions")
	public void reload() {
		load(true);
	}

	/**
	 * Load all the shit
	 */
	public void load(boolean reload) {
		if (!Config.ENABLE_CUSTOM_AUCTIONS || Config.isServer(Config.TENKAI)) {
			return;
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File file = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/itemAuction.xml");
		if (!file.exists()) {
			return;
		}

		XmlDocument doc = new XmlDocument(file);
		for (XmlNode d : doc.getChildren()) {
			if (d.getName().equalsIgnoreCase("auction")) {
				int auctionId = d.getInt("id");

				int count = d.getInt("count");
				int repeatTime = d.getInt("repeatTime", 0) * 3600; // It's in hours
				int randomRepeatTime = d.getInt("randomRepeatTime", 0) * 3600; // It's in hours
				int initialCurrencyId = d.getInt("initialCurrencyId");
				int highestCurrencyId = d.getInt("highestCurrencyId", 0);
				int initialPrice = d.getInt("initialPrice", 0);
				int initialDuration = d.getInt("initialDuration") * 3600; // It's in hours

				int[] auctionItems = null;
				if (!d.hasAttribute("itemId")) {
					List<Integer> itemIds = new ArrayList<>();
					for (XmlNode e : d.getChildren("item")) {
						itemIds.add(e.getInt("id"));
					}

					auctionItems = new int[itemIds.size()];
					Iterator<Integer> iter = itemIds.iterator();
					for (int i = 0; iter.hasNext(); i++) {
						auctionItems[i] = iter.next();
					}

					System.out.println("Loaded " + auctionItems.length + " for new format auction.");
				} else {
					String[] itemIds = d.getString("itemId").split(",");
					auctionItems = new int[itemIds.length];
					for (int i = 0; i < auctionItems.length; i++) {
						auctionItems[i] = Integer.parseInt(itemIds[i]);
					}

					System.out.println("Loaded " + auctionItems.length + " for old format auction.");
				}

				AuctionTemplate itemAuction = new AuctionTemplate(auctionId,
						auctionItems,
						count,
						repeatTime,
						randomRepeatTime,
						initialCurrencyId,
						highestCurrencyId,
						initialPrice,
						initialDuration,
						!reload);
				auctionTemplates.put(auctionId, itemAuction);
			}
		}

		log.info("ItemAuction: Loaded: " + auctionTemplates.size() + " auctions!");

		if (!reload) {
			Connection con = null;
			try {
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement =
						con.prepareStatement("SELECT id, itemId, templateId, currencyId, currentBid, ownerId, endTime FROM `custom_auctions`");
				ResultSet rs = statement.executeQuery();

				while (rs.next()) {
					int auctionId = rs.getInt("id");
					long remainingTime = rs.getLong("endTime") * 1000 - System.currentTimeMillis();
					if (remainingTime < ADDED_DURATION) {
						remainingTime = ADDED_DURATION;
					}

					AuctionTemplate template = auctionTemplates.get(rs.getInt("templateId"));
					if (template == null) {
						log.warn("CustomAuctionManager: Found a null template with id:" + rs.getInt("templateId"));
						continue;
					}
					Auction auction = new Auction(auctionId,
							rs.getInt("itemId"),
							template,
							rs.getInt("currencyId"),
							rs.getLong("currentBid"),
							rs.getInt("ownerId"),
							remainingTime);
					auctions.put(auctionId, auction);
				}
				statement.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				L2DatabaseFactory.close(con);
			}
		}
	}

	/**
	 * Called at each player bid try, we do all the checks here to deceide if it's avalid or not
	 *
	 */
	public void tryToBid(Player activeChar, int bidId, long playerBid, String coin) {
		if (activeChar == null) {
			return;
		}

		if (!activeChar.getClient().getFloodProtectors().getTransaction().tryPerformAction("buy")) {
			return;
		}

		if (ThreadPoolManager.getInstance().isShutdown()) {
			activeChar.sendMessage("Item Auction: Can't bid while at shutdown...!");
			return;
		}

		Auction bid = auctions.get(bidId);
		if (bid == null) {
			return;
		}

		//Don't allow overcome the bid if it's the same player TODO IMPORTANT DO NOT DELETE
		if (activeChar.getObjectId() == bid.getCurrentOwnerId() && !activeChar.isGM()) {
			activeChar.sendMessage("You can't overbid your own bid!");
			return;
		}
		if (activeChar.getPrivateStoreType() != 0 || activeChar.isInCrystallize()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			return;
		}

		if (bid.getCurrentOwnerName().equals("Ghost")) {
			return;
		}

		//Check time?
		if (bid.getRemainingTime() < 30000) //The end time should be > than 30 sec, just to avoid errors?
		{
			activeChar.sendMessage("Item Auction: This bid has been ended..!");
			return;
		}

		int currentCurrencyId = bid.getCurrentCurrency();
		long minBid = bid.getCurrentPrice() + bid.getCurrentPrice() * 10 / 100;
		if (minBid <= bid.getCurrentPrice()) {
			minBid = bid.getCurrentPrice() + 1;
		}

		long maxAdenaPrice = (long) 90 * 1000000000; // 99kkk
		if (bid.getCurrentCurrency() == 57 && bid.getCurrentPrice() > maxAdenaPrice) {
			coin = "Dreams";
		}

		if (!coin.isEmpty()) {
			int currencyId = 0;
			Currency playerCurrency = null;

			switch (coin) {
				case "Dreams":
					currencyId = 50002;
					break;
				case "Raid":
					currencyId = 50009;
					break;
				default:
					playerCurrency = Currency.valueOf(coin);
					if (playerCurrency == null) //Possible client hax?
					{
						return;
					}

					currencyId = playerCurrency.ItemId;
					break;
			}

			if (currencyId != currentCurrencyId)//If we have a new currency..
			{
				int highestCurrencyId = bid.getTemplate().getHighestCurrencyId();

				switch (highestCurrencyId) {
					case 57: {
						if (currencyId != 57 && currencyId != 50002 || currencyId == 50002 && bid.getCurrentPrice() < maxAdenaPrice) {
							activeChar.sendMessage("You can only bid Adena for this auction.");
							return;
						}
					}
				}

				if (currencyId == 50002) {
					if (playerBid < 10000) {
						activeChar.sendMessage("Item Auction: The minimum bid for this item should be for: 10.000 Dreams Coin.");
						return;
					}
				} else {
					int index = 0;
					for (Currency cur : Currency.values()) {
						if (currentCurrencyId == cur.ItemId) {
							break;
						}
						index++;
					}

					if (playerCurrency.ordinal() < index && !activeChar.isGM()) {
						//activeChar.sendMessage("Ordinal " + playerCurrency.ordinal());
						//activeChar.sendMessage("Index " + index);
						return;
					}
				}

				currentCurrencyId = currencyId;
				minBid = 1; //?
			}
		}

		if (playerBid < minBid && !activeChar.isGM()) {
			activeChar.sendMessage("Item Auction: The minimum bid for this item should be for: " + minBid + " " +
					ItemTable.getInstance().getTemplate(bid.getCurrentCurrency()).getName());
			return;
		}

		if (!activeChar.destroyItemByItemId("ItemAuction", currentCurrencyId, playerBid, null, true)) {
			return;
		}

		restoreLoserBid(bid.getCurrentOwnerId(), bid.getCurrentCurrency(), bid.getCurrentPrice());

		String oldBidderName = bid.getCurrentOwnerName();

		bid.setCurrentCurrency(currentCurrencyId);//Need do only if is not same but beh
		bid.setOwner(activeChar.getObjectId(), playerBid);

		String message = "";
		String itemName = ItemTable.getInstance().getTemplate(bid.getItemId()).getName();
		String currencyName = bid.getCurrentCurrency() == 57 ? "Adenas" : "Golden Apigas";

		if (bid.getCurrentCurrency() == 50002) {
			currencyName = "Dreams Coin";
		}

		String priceString = "";
		long price = bid.getCurrentPrice();
		long counter = 0;
		while (price > 0) {
			priceString = price % 10 + priceString;
			price /= 10;
			counter++;
			if (counter % 3 == 0) {
				priceString = "," + priceString;
			}
		}

		if (counter % 3 == 0) {
			priceString = priceString.substring(1);
		}

		if (!activeChar.isGM()) {
			if (oldBidderName.equals("None")) {
				message = activeChar.getName() + " placed a bid on " + itemName + " - " + priceString + " " + currencyName + ".";
			} else {
				message =
						activeChar.getName() + " outbided " + oldBidderName + " with a " + priceString + " " + currencyName + " bid for " + itemName +
								".";
			}

			NpcInstance auctionManager = null;
			for (Player player : World.getInstance().getAllPlayers().values()) {
				if (auctionManager == null) {
					auctionManager = Util.getNpcCloseTo(33782, player);
				}

				player.sendPacket(new CreatureSay(auctionManager == null ? 0x00 : auctionManager.getObjectId(), Say2.TRADE, "Jolie", message));
			}
		}
	}

	private void restoreLoserBid(int playerId, int currencyId, long count) {
		giveItemToPlayer(playerId, currencyId, count, "Your bid has been overcome!");
	}

	/**
	 * Used for add the reward item to the player or give back the bids
	 *
	 */
	private void giveItemToPlayer(int playerId, int itemId, long count, String text) {
		if (CharNameTable.getInstance().getNameById(playerId) ==
				null) //Filter if the player exists, needed because we call this when an auction is ended without check if there is a winner
		{
			return;
		}

		//Lets use ALWAYS the mail, it's nice because the player will notice much better that his bid has been overcome
		Message msg = new Message(-1, playerId, false, "ItemAuction", text, 0);

		Mail attachments = msg.createAttachments();

		attachments.addItem("ItemAuction", itemId, count, null, null);

		MailManager.getInstance().sendMessage(msg);
	}

	private CustomAuctionManager() {
	}

	public static CustomAuctionManager getInstance() {
		return SingletonHolder.instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final CustomAuctionManager instance = new CustomAuctionManager();
	}
}
