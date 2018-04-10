package l2server.gameserver.instancemanager;

import l2server.Config;
import l2server.DatabasePool;
import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.communitybbs.Manager.CustomCommunityBoard;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Message;
import l2server.gameserver.model.itemcontainer.Mail;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;
import l2server.util.loader.annotations.Load;
import l2server.util.loader.annotations.Reload;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author LasTravel
 * @author Pere
 */

public class TenkaiAuctionManager {
	private static Logger log = LoggerFactory.getLogger(TenkaiAuctionManager.class.getName());

	private static final long ADDED_DURATION = 3600000L;
	
	/**
	 * All the item auction info
	 */
	private class AuctionTemplate {
		private final int id;
		private int[] itemId;
		private int count;
		private int repeatTime;
		private int randomRepeatTime;
		private int initialCurrencyId;
		private long initialPrice;
		private int initialDuration;
		private boolean acceptAllCoins;
		
		private AuctionTemplate(int auctionId,
		                        int[] itemId,
		                        int count,
		                        int repeatTime,
		                        int randomRepeatTime,
		                        int initialCurrency,
		                        long initialPrice,
		                        int initialDuration,
		                        boolean acceptAllCoins) {
			id = auctionId;
			this.itemId = itemId;
			this.count = count;
			this.repeatTime = repeatTime;
			this.randomRepeatTime = randomRepeatTime;
			initialCurrencyId = initialCurrency;
			this.initialPrice = initialPrice;
			this.initialDuration = initialDuration;
			this.acceptAllCoins = acceptAllCoins;
			
			initialize();
		}
		
		private void overrideInfo(int[] itemId,
		                          int count,
		                          int repeatTime,
		                          int randomRepeatTime,
		                          int initialCurrency,
		                          long initialPrice,
		                          int initialDuration,
		                          boolean acceptAllCoins) {
			this.itemId = itemId;
			this.count = count;
			this.repeatTime = repeatTime;
			this.randomRepeatTime = randomRepeatTime;
			initialCurrencyId = initialCurrency;
			this.initialPrice = initialPrice;
			this.initialDuration = initialDuration;
			this.acceptAllCoins = acceptAllCoins;
		}
		
		private void initialize() {
			long nextAuction = 0;//repeatTime * 1000L + Rnd.get(randomRepeatTime) * 1000L;
			Connection con = null;
			try {
				con = DatabasePool.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT lastAuctionCreation FROM `custom_auction_templates` WHERE id = ?");
				statement.setInt(1, id);
				ResultSet rs = statement.executeQuery();
				if (rs.next()) {
					long lastAuctionCreation = rs.getLong("lastAuctionCreation");
					nextAuction = lastAuctionCreation + repeatTime * 1000L + Rnd.get(randomRepeatTime) * 1000L - System.currentTimeMillis();
					if (nextAuction < 0) {
						nextAuction = 0;
					}
				}
				statement.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				DatabasePool.close(con);
			}
			
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
				@Override
				public void run() {
					int auctionId = IdFactory.getInstance().getNextId();
					int itemId = AuctionTemplate.this.getRandomItemId();
					addAuction(new Auction(auctionId, itemId, AuctionTemplate.this));
					
					Connection con = null;
					try {
						con = DatabasePool.getInstance().getConnection();
						PreparedStatement statement =
								con.prepareStatement("REPLACE INTO `custom_auction_templates` (id, lastAuctionCreation) VALUES (?, ?)");
						statement.setInt(1, id);
						statement.setLong(2, System.currentTimeMillis());
						statement.execute();
						statement.close();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						DatabasePool.close(con);
					}
					
					Announcements.getInstance()
							.announceToAll("Item Auction: " + ItemTable.getInstance().getTemplate(itemId).getName() +
									" has been added to the Item Auction (Alt + B)!");
					
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
			if (itemId[0] == 36515) {
				return 800;
			}
			return count;
		}
		
		@SuppressWarnings("unused")
		private int getRepeatTime() {
			return repeatTime;
		}
		
		private int getInitialCurrencyId() {
			return initialCurrencyId;
		}
		
		private long getInitialPrice() {
			return initialPrice;
		}
		
		private int getInitialDuration() {
			return initialDuration;
		}
		
		private boolean getAcceptAllCoins() {
			return acceptAllCoins;
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
				con = DatabasePool.getInstance().getConnection();
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
				DatabasePool.close(con);
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
		
		private long getRemainingTime() {
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
				con = DatabasePool.getInstance().getConnection();
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
				DatabasePool.close(con);
			}
		}
		
		private class CurrentAuctionEnd implements Runnable {
			@Override
			public void run() {
				auctions.remove(id);
				
				String playerName = CharNameTable.getInstance().getNameById(getCurrentOwnerId());
				if (playerName != null) {
					giveItemToPlayer(getCurrentOwnerId(), getItemId(), getTemplate().getCount(), "Congrats! You've won this bid!");
					Util.logToFile("The player: " + CharNameTable.getInstance().getNameById(getCurrentOwnerId()) + " wins the " +
							ItemTable.getInstance().getTemplate(getItemId()).getName() + " bid", "Auctions", true);
				} else {
					Util.logToFile("The " + ItemTable.getInstance().getTemplate(getItemId()).getName() + " auction has ended without winner!",
							"Auctions",
							true);
				}
				
				Connection con = null;
				try {
					con = DatabasePool.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("DELETE FROM `custom_auctions` WHERE id = ?");
					statement.setInt(1, id);
					statement.execute();
					statement.close();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					DatabasePool.close(con);
				}
			}
		}
	}
	
	/**
	 * Currency Types
	 */
	private class CurrencyInfo {
		private int position;
		private String name;
		private int id;
		
		private CurrencyInfo(int position, int id) {
			this.position = position;
			this.id = id;
			name = ItemTable.getInstance().getTemplate(id).getName().replace(" ", "");
		}
		
		private int getPosition() {
			return position;
		}
		
		private String getName() {
			return name;
		}
		
		private int getId() {
			return id;
		}
	}
	
	private static Map<Integer, CurrencyInfo> currencies = new LinkedHashMap<>(); //Currency info
	private static Map<Integer, Auction> auctions = new LinkedHashMap<>(); //Current auctions
	private static Map<Integer, AuctionTemplate> auctionTemplates = new HashMap<>();
	//All the auction info
	
	/**
	 * This is the community page, where we display all the auction item information
	 *
	 */
	public String getAuctionInfo(int playerId, int pageToShow) {
		StringBuilder sb = new StringBuilder();
		
		if (!Config.ENABLE_CUSTOM_AUCTIONS || auctions.isEmpty()) {
			return sb.append(
					"<center><table><tr><td width=200><font color=LEVEL>There are no auctions at this time!</font></td></tr></table></center>")
					.toString();
		}
		
		int maxAuctionsPerPage = 6;
		int auctionsSize = auctions.size();
		int maxPages = auctionsSize / maxAuctionsPerPage;
		if (auctionsSize > maxAuctionsPerPage * maxPages) {
			maxPages++;
		}
		if (pageToShow > maxPages) {
			pageToShow = maxPages;
		}
		int pageStart = maxAuctionsPerPage * pageToShow;
		int pageEnd = auctionsSize;
		if (pageEnd - pageStart > maxAuctionsPerPage) {
			pageEnd = pageStart + maxAuctionsPerPage;
		}
		
		if (maxPages > 1) {
			sb.append(CustomCommunityBoard.getInstance().createPages(pageToShow, maxPages, "_bbscustom;itemAuction;", ";"));
		}
		
		sb.append("<table width=700>");
		
		int tempCount = 0;
		int totalCount = 0;
		int totalEnd = pageEnd - pageStart;
		
		Object[] data = auctions.values().toArray();
		for (int i = pageStart; i < pageEnd; i++) {
			Auction currentAuctionInfo = (Auction) data[i];
			if (currentAuctionInfo == null) {
				continue;
			}
			
			if (tempCount == 0) {
				sb.append("<tr><td>");
			} else {
				sb.append("<td>");
			}
			
			sb.append("<table height=200 background=L2UI_CH3.refinewnd_back_Pattern>");
			sb.append("<tr><td FIXWIDTH=160></td>");
			sb.append("<td><br><br><table border=0 cellpadding=0><tr><td FIXWIDTH=375 align=center><font color=LEVEL>" +
					ItemTable.getInstance().getTemplate(currentAuctionInfo.getItemId()).getName() + " (" +
					currentAuctionInfo.getTemplate().getCount() + ")</font></td></tr></table>");
			sb.append("<table cellpadding=0><tr><td FIXWIDTH=375>");
			sb.append("<tr><td>Remaining Time: </td><td FIXWIDTH=375>" + currentAuctionInfo.getRemainingTimeString() +
					"</td></tr><tr><td>Current Owner:</td><td>" + currentAuctionInfo.getCurrentOwnerName() + "</td></tr>");
			sb.append("<tr><td>Currency:</td><td>" + ItemTable.getInstance().getTemplate(currentAuctionInfo.getCurrentCurrency()).getName() +
					"</td></tr>");
			sb.append("<tr><td>Current Bid:</td><td>" + NumberFormat.getNumberInstance(Locale.US).format(currentAuctionInfo.getCurrentPrice()) +
					"</td></tr>");
			
			String options = "";
			if (!currentAuctionInfo.getTemplate().getAcceptAllCoins()) {
				options += currencies.get(currentAuctionInfo.getTemplate().getInitialCurrencyId()).getName() + ";";
			} else {
				CurrencyInfo currency = currencies.get(currentAuctionInfo.getCurrentCurrency());
				int currentPos = currency.getPosition();
				for (Entry<Integer, CurrencyInfo> b : currencies.entrySet()) {
					if (b.getValue().getPosition() < currentPos) {
						continue;
					}
					options += b.getValue().getName() + ";";
				}
				
				if (!options.isEmpty() && currentPos < currencies.size()) {
					options = options.substring(0, options.length() - 1);
					sb.append(
							"<tr><td>Select:</td><td><combobox width=100 height=17 var=\"plcoin" + currentAuctionInfo.getId() + "\" list=" + options +
									"></td></tr>");
				}
			}
			
			sb.append("<tr><td>Your bid:</td><td><edit var=\"plbid" + currentAuctionInfo.getId() + "\" width=100 type=number length=14></td></tr>");
			sb.append("<tr><td></td><td><br><button action=\"bypass _bbscustom;action;bid;" + currentAuctionInfo.getId() + "; $plbid" +
					currentAuctionInfo.getId() + " ; $plcoin" + currentAuctionInfo.getId() +
					"\" value=BID! width=105 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></td></tr>");
			sb.append("<tr><td></td><td></td></tr></table><br><br></td><td FIXWIDTH=80></td></tr>");
			sb.append("</table><br>");
			
			tempCount++;
			totalCount++;
			
			if (tempCount == 2 || totalCount == totalEnd) {
				sb.append("</td></tr>");
				tempCount = 0;
			} else {
				sb.append("</td>");
			}
		}
		
		sb.append("</table>");
		return sb.toString();
	}
	
	/**
	 * Load all the stuff
	 */
	@Reload("customauctions")
	@Load(dependencies = ItemTable.class)
	private void load() {
		if (!Config.ENABLE_CUSTOM_AUCTIONS || !Config.isServer(Config.TENKAI)) {
			return;
		}
		
		loadTemplates();
		
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
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
					log.warn("TenkaiAuctionManager: Found a null template with id:" + rs.getInt("templateId"));
					continue;
				}
				Auction auction = new Auction(auctionId,
						rs.getInt("itemId"),
						template,
						rs.getInt("currencyId"),
						rs.getLong("currentBid"),
						rs.getInt("ownerId"),
						remainingTime);
				addAuction(auction);
			}
			
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DatabasePool.close(con);
		}
	}
	
	private void loadTemplates() {
		currencies.clear();
		auctionTemplates.clear();
		
		File file = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/itemAuction.xml");
		if (!file.exists()) {
			return;
		}
		
		XmlDocument doc = new XmlDocument(file);
		for (XmlNode d : doc.getChildren()) {
			if (d.getName().equalsIgnoreCase("currency")) {
				int position = d.getInt("position");
				int itemId = d.getInt("itemId");
				currencies.put(itemId, new CurrencyInfo(position, itemId));
			} else if (d.getName().equalsIgnoreCase("auction")) {
				int auctionId = d.getInt("id");
				String[] itemIds = d.getString("itemId").split(",");
				int[] itemId = new int[itemIds.length];
				for (int i = 0; i < itemId.length; i++) {
					itemId[i] = Integer.parseInt(itemIds[i]);
				}
				int count = d.getInt("count");
				int repeatTime = d.getInt("repeatTime") * 3600; // It's in hours
				int randomRepeatTime = d.getInt("randomRepeatTime") * 3600; // It's in hours
				int initialCurrencyId = d.getInt("initialCurrencyId");
				int initialPrice = d.getInt("initialPrice");
				int initialDuration = d.getInt("initialDuration") * 3600; // It's in hours
				boolean acceptAllCoins = d.getBool("acceptAllCoins", true);
				AuctionTemplate itemAuction = auctionTemplates.get(auctionId);
				if (itemAuction != null) {
					itemAuction.overrideInfo(itemId,
							count,
							repeatTime,
							randomRepeatTime,
							initialCurrencyId,
							initialPrice,
							initialDuration,
							acceptAllCoins);
				} else {
					auctionTemplates.put(auctionId,
							new AuctionTemplate(auctionId,
									itemId,
									count,
									repeatTime,
									randomRepeatTime,
									initialCurrencyId,
									initialPrice,
									initialDuration,
									acceptAllCoins));
				}
			}
		}
		
		log.info("ItemAuction: Loaded: " + auctionTemplates.size() + " auctions!");
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
		if (activeChar.getObjectId() == bid.getCurrentOwnerId()) {
			activeChar.sendMessage("You can't overbid your own bid!");
			return;
		}
		if (activeChar.getPrivateStoreType() != 0 || activeChar.isInCrystallize()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			return;
		}
		
		//Check time?
		if (bid.getRemainingTime() < 1000) //The end time should be > than 30 sec, just to avoid errors?
		{
			return;
		}
		
		int currentCurrencyId = bid.getCurrentCurrency();
		long minBid = bid.getCurrentPrice() + bid.getCurrentPrice() * 10 / 100;
		if (minBid <= bid.getCurrentPrice()) {
			minBid = bid.getCurrentPrice() + 1;
		}
		
		if (!coin.isEmpty()) {
			CurrencyInfo playerCurrency = null;
			for (Entry<Integer, CurrencyInfo> i : currencies.entrySet()) {
				if (i.getValue().getName().equalsIgnoreCase(coin)) {
					playerCurrency = i.getValue();
					break;
				}
			}
			if (playerCurrency == null) {
				return;
			}
			
			int currencyId = playerCurrency.getId();
			if (!bid.getTemplate().getAcceptAllCoins() && currencyId != bid.getTemplate().getInitialCurrencyId()) {
				return; //client hack?
			}
			
			if (bid.getTemplate().getAcceptAllCoins() && currencyId != currentCurrencyId)//If we have a new currency..
			{
				if (currencies.get(currencyId).getPosition() < currencies.get(currentCurrencyId).getPosition()) {
					return; //client hack?
				}
				
				currentCurrencyId = currencyId;
				minBid = 1; //?
			}
		}
		
		if (playerBid < minBid) {
			activeChar.sendMessage("Item Auction: The minimum bid for this item should be for: " + minBid + " " +
					ItemTable.getInstance().getTemplate(bid.getCurrentCurrency()).getName());
			return;
		}
		
		if (!activeChar.destroyItemByItemId("ItemAuction", currentCurrencyId, playerBid, null, true)) {
			return;
		}
		
		Util.logToFile("The player: " + activeChar.getName() + " did a bid(" + playerBid + " " +
				ItemTable.getInstance().getTemplate(currentCurrencyId).getName() + ") on " +
				ItemTable.getInstance().getTemplate(bid.getItemId()).getName(), "Auctions", true);
		if (bid.getCurrentOwnerId() != 0) {
			Util.logToFile("The bid from: " + CharNameTable.getInstance().getNameById(bid.getCurrentOwnerId()) + "(" + bid.getCurrentPrice() + " " +
					ItemTable.getInstance().getTemplate(bid.getCurrentCurrency()).getName() + ") has been overcome by " + activeChar.getName() + "(" +
					String.valueOf(playerBid) + " " + ItemTable.getInstance().getTemplate(currentCurrencyId).getName() + ")", "Auctions", true);
		}
		
		restoreLoserBid(bid.getCurrentOwnerId(),
				bid.getCurrentCurrency(),
				bid.getCurrentPrice(),
				activeChar.getName(),
				String.valueOf(playerBid) + " " + ItemTable.getInstance().getTemplate(currentCurrencyId).getName());
		
		bid.setCurrentCurrency(currentCurrencyId);
		bid.setOwner(activeChar.getObjectId(), playerBid);
		
		CustomCommunityBoard.getInstance().parseCmd("bbscustom;itemAuction;0", activeChar);
	}
	
	private void restoreLoserBid(int playerId, int currencyId, long count, String newBidOwner, String newBidInfo) {
		giveItemToPlayer(playerId, currencyId, count, "Your bid has been overcome by " + newBidOwner + " with (" + newBidInfo + ")!");
	}
	
	/**
	 * Used for add the reward item to the player or give back the bids
	 *
	 */
	private void giveItemToPlayer(int playerId, int itemId, long count, String text) {
		//Lets use ALWAYS the mail, it's nice because the player will notice much better that his bid has been overcome
		Message msg = new Message(-1, playerId, false, "ItemAuction", text, 0);
		
		Mail attachments = msg.createAttachments();
		attachments.addItem("ItemAuction", itemId, count, null, null);
		
		MailManager.getInstance().sendMessage(msg);
	}
	
	public int getCurrencyId(String coin) {
		for (Entry<Integer, CurrencyInfo> i : currencies.entrySet()) {
			if (i.getValue().getName().equalsIgnoreCase(coin)) {
				return i.getValue().getId();
			}
		}
		return 0;
	}
	
	public synchronized void addAuction(Auction auction) {
		auctions.put(auction.getId(), auction);
		
		// Stupid naive sort
		Map<Integer, Auction> tempAuctions = new HashMap<>(auctions);
		auctions.clear();
		while (!tempAuctions.isEmpty()) {
			Auction minAuction = null;
			long minTime = Long.MAX_VALUE;
			for (Auction temp : tempAuctions.values()) {
				if (temp.getRemainingTime() < minTime) {
					minAuction = temp;
					minTime = temp.getRemainingTime();
				}
			}
			
			if (minAuction == null) {
				auctions.putAll(tempAuctions);
				break;
			}
			
			tempAuctions.remove(minAuction.getId());
			auctions.put(minAuction.getId(), minAuction);
		}
	}
	
	private TenkaiAuctionManager() {
	}
	
	public static TenkaiAuctionManager getInstance() {
		return SingletonHolder.instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final TenkaiAuctionManager instance = new TenkaiAuctionManager();
	}
}
