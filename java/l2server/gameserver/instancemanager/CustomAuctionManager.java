package l2server.gameserver.instancemanager;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2NpcInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Message;
import l2server.gameserver.model.itemcontainer.Mail;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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

public class CustomAuctionManager
{
	private static final long ADDED_DURATION = 3600000L;

	private enum Currency
	{
		Adena(57), GoldenApiga(9143);

		private final int ItemId;

		Currency(int itemId)
		{
			ItemId = itemId;
		}
	}

	/**
	 * All the item auction info
	 */
	private class AuctionTemplate
	{
		private int _id;
		private int[] _itemId;
		private int _count;
		private int _repeatTime;
		private int _randomRepeatTime;
		private int _initialCurrencyId;
		private int _highestCurrencyId;
		private long _initialPrice;
		private int _initialDuration;

		private AuctionTemplate(int auctionId, int[] itemId, int count, int repeatTime, int randomRepeatTime, int initialCurrency, int highestCurrency, long initialPrice, int initialDuration, boolean init)
		{
			_id = auctionId;
			_itemId = itemId;
			_count = count;
			_repeatTime = repeatTime;
			_randomRepeatTime = randomRepeatTime;
			_initialCurrencyId = initialCurrency;
			_highestCurrencyId = highestCurrency;
			_initialPrice = initialPrice;
			_initialDuration = initialDuration;

			if (init)
			{
				initialize();
			}
		}

		private void initialize()
		{
			if (_repeatTime == 0)
			{
				System.out.println("The auction " + _id + " will not repeat.");
				return;
			}

			long nextAuction = 0;
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement =
						con.prepareStatement("SELECT lastAuctionCreation FROM `custom_auction_templates` WHERE id = ?");
				statement.setInt(1, _id);
				ResultSet rs = statement.executeQuery();
				if (rs.next())
				{
					long lastAuctionCreation = rs.getLong("lastAuctionCreation");
					nextAuction = lastAuctionCreation + (_initialDuration + _repeatTime) * 1000L +
							Rnd.get(_randomRepeatTime) * 1000L - System.currentTimeMillis();
					if (nextAuction < 0)
					{
						nextAuction = 0;
					}
				}
				statement.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					startAuction(AuctionTemplate.this.getId());

					Connection con = null;
					try
					{
						con = L2DatabaseFactory.getInstance().getConnection();
						PreparedStatement statement = con.prepareStatement(
								"REPLACE INTO `custom_auction_templates` (id, lastAuctionCreation) VALUES (?, ?)");
						statement.setInt(1, _id);
						statement.setLong(2, System.currentTimeMillis());
						statement.execute();
						statement.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					finally
					{
						L2DatabaseFactory.close(con);
					}

					ThreadPoolManager.getInstance()
							.scheduleGeneral(this, _repeatTime * 1000L + Rnd.get(_randomRepeatTime) * 1000L);
				}
			}, nextAuction);
		}

		private int getId()
		{
			return _id;
		}

		private int getRandomItemId()
		{
			return _itemId[Rnd.get(_itemId.length)];
		}

		private int getCount()
		{
			return _count;
		}

		@SuppressWarnings("unused")
		private int getRepeatTime()
		{
			return _repeatTime;
		}

		private int getInitialCurrencyId()
		{
			return _initialCurrencyId;
		}

		private int getHighestCurrencyId()
		{
			return _highestCurrencyId;
		}

		private long getInitialPrice()
		{
			return _initialPrice;
		}

		private int getInitialDuration()
		{
			return _initialDuration;
		}
	}

	/**
	 * All the current auction item list
	 */
	private class Auction
	{
		private final int _id;
		private final int _itemId;
		private final AuctionTemplate _template;
		private int _currentAuctionOwner;
		private int _currentCurrencyId;
		private long _currentPrice;
		private ScheduledFuture<?> _endAuctionTask;

		private Auction(int auctionId, int itemId, AuctionTemplate info)
		{
			_id = auctionId;
			_itemId = itemId;
			_template = info;

			_currentCurrencyId = info.getInitialCurrencyId();
			_currentPrice = info.getInitialPrice();

			//Set the end task?
			_endAuctionTask = ThreadPoolManager.getInstance()
					.scheduleGeneral(new CurrentAuctionEnd(), info.getInitialDuration() * 1000L);

			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(
						"INSERT INTO `custom_auctions` (id, itemId, templateId, currencyId, currentBid, ownerId, endTime) VALUES (?, ?, ?, ?, ?, ?, ?)");
				statement.setInt(1, _id);
				statement.setInt(2, _itemId);
				statement.setInt(3, _template.getId());
				statement.setInt(4, _currentCurrencyId);
				statement.setLong(5, _currentPrice);
				statement.setInt(6, _currentAuctionOwner);
				statement.setLong(7, (System.currentTimeMillis() + getRemainingTime()) / 1000L);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}

		private Auction(int auctionId, int itemId, AuctionTemplate info, int currencyId, long currentBid, int ownerId, long remainingTime)
		{
			_id = auctionId;
			_itemId = itemId;
			_template = info;

			_currentCurrencyId = currencyId;
			_currentPrice = currentBid;
			_currentAuctionOwner = ownerId;

			//Set the end task?
			_endAuctionTask = ThreadPoolManager.getInstance().scheduleGeneral(new CurrentAuctionEnd(), remainingTime);
		}

		private int getId()
		{
			return _id;
		}

		private int getItemId()
		{
			return _itemId;
		}

		private AuctionTemplate getTemplate()
		{
			return _template;
		}

		@SuppressWarnings("unused")
		private ScheduledFuture<?> getEndTask()
		{
			return _endAuctionTask;
		}

		private String getRemainingTimeString()
		{
			Long remainingTime = _endAuctionTask.getDelay(TimeUnit.MILLISECONDS) / 1000;

			int hours = (int) (remainingTime / 3600);
			int minutes = (int) (remainingTime % 3600 / 60);
			int seconds = (int) (remainingTime % 60);

			return hours + "h " + minutes + "m " + seconds + "s";
		}

		private Long getRemainingTime()
		{
			return _endAuctionTask.getDelay(TimeUnit.MILLISECONDS);
		}

		private String getCurrentOwnerName()
		{
			if (_currentAuctionOwner == 0)
			{
				return "None";
			}

			return CharNameTable.getInstance().getNameById(_currentAuctionOwner);
		}

		private int getCurrentOwnerId()
		{
			return _currentAuctionOwner;
		}

		private int getCurrentCurrency()
		{
			return _currentCurrencyId;
		}

		private long getCurrentPrice()
		{
			return _currentPrice;
		}

		public void setCurrentCurrency(int itemId)
		{
			_currentCurrencyId = itemId;
		}

		private void setOwner(int playerId, long playerBid)
		{
			_currentAuctionOwner = playerId;
			_currentPrice = playerBid;

			if (getRemainingTime() < ADDED_DURATION)
			{
				_endAuctionTask.cancel(true);
				_endAuctionTask =
						ThreadPoolManager.getInstance().scheduleGeneral(new CurrentAuctionEnd(), ADDED_DURATION);
			}

			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(
						"UPDATE `custom_auctions` SET currencyId = ?, currentBid = ?, ownerId = ?, endTime = ? WHERE id = ?");
				statement.setInt(1, _currentCurrencyId);
				statement.setLong(2, _currentPrice);
				statement.setInt(3, _currentAuctionOwner);
				statement.setLong(4, (System.currentTimeMillis() + getRemainingTime()) / 1000L);
				statement.setInt(5, _id);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}

		private void endAuction()
		{
			_auctions.remove(_id);

			giveItemToPlayer(getCurrentOwnerId(), getItemId(), getTemplate().getCount(),
					"Congrats! You've won this bid!");

			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("DELETE FROM `custom_auctions` WHERE id = ?");
				statement.setInt(1, _id);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			String winnerName = CharNameTable.getInstance().getNameById(getCurrentOwnerId());
			String currencyName = ItemTable.getInstance().getTemplate(getCurrentCurrency()).getName();
			String itemName = ItemTable.getInstance().getTemplate(getItemId()).getName();

			Util.logToFile(winnerName + " won the auction for " + itemName + " by bidding " + getCurrentPrice() + " " +
					currencyName + ".", "Auction_Logs", "txt", true, true);
		}

		private class CurrentAuctionEnd implements Runnable
		{
			@Override
			public void run()
			{
				endAuction();
			}
		}
	}

	private static Map<Integer, Auction> _auctions = new HashMap<>(); //Current auctions
	private static Map<Integer, AuctionTemplate> _auctionTemplates = new HashMap<>();
	//All the auction info

	public final void startAuction(final int templateId)
	{
		int auctionId = IdFactory.getInstance().getNextId();

		AuctionTemplate auctionTemplate = _auctionTemplates.get(templateId);

		int itemId = auctionTemplate.getRandomItemId();

		_auctions.put(auctionId, new Auction(auctionId, itemId, auctionTemplate));

		L2NpcInstance auctionManager = null;
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			if (auctionManager == null)
			{
				auctionManager = Util.getNpcCloseTo(33782, player);
			}

			player.sendPacket(new CreatureSay(auctionManager == null ? 0x00 : auctionManager.getObjectId(), 1, "Jolie",
					"I've got a new item (" +
							(auctionTemplate.getCount() != 0 ? auctionTemplate.getCount() + "x " : "") +
							ItemTable.getInstance().getTemplate(itemId).getName() + ") up for auctions!" +
							(auctionTemplate.getHighestCurrencyId() == 57 ? " Adenas only!" :
									auctionTemplate.getHighestCurrencyId() == 50009 ? " Raid Hearts only!" : "")));
		}
	}

	public final Auction getAuctionById(final int auctionId)
	{
		if (!_auctions.containsKey(auctionId))
		{
			return null;
		}

		return _auctions.get(auctionId);
	}

	/**
	 * This is the community page, where we display all the auction item information
	 *
	 * @param playerId
	 * @return
	 */
	public String getAuctionInfo(int playerId)
	{
		StringBuilder sb = new StringBuilder();

		int count = 0;
		int size = _auctions.size();

		if (size == 0)
		{
			return sb
					.append("<center><table><tr><td width=200><font color=LEVEL>There are no auctions at this time!</font></td></tr></table></center>")
					.toString();
		}

		sb.append("<table width=700>");

		for (Entry<Integer, Auction> currentAuction : _auctions.entrySet())
		{
			Auction currentAuctionInfo = currentAuction.getValue();

			if (currentAuctionInfo == null)
			{
				continue;
			}

			if (count == 0)
			{
				sb.append("<tr><td>");
			}
			else
			{
				sb.append("<td>");
			}

			sb.append(
					"<table height=180 background=L2UI_CH3.refinewnd_back_Pattern><tr><td FIXWIDTH=80></td>"); //<img src=\""+currentAuctionInfio.getItemTemplate().getIcon()+"\" width=32 height=32>
			sb.append("<td><br><table cellpadding=0><tr><td FIXWIDTH=375><font color=LEVEL>" +
					ItemTable.getInstance().getTemplate(currentAuctionInfo.getItemId()).getName() + " (" +
					currentAuctionInfo.getTemplate().getCount() + ")</font>");
			sb.append("</td></tr><tr><td>Remaining Time: </td><td FIXWIDTH=375>" +
					currentAuctionInfo.getRemainingTimeString() + "</td></tr><tr><td>Current Owner:</td><td>" +
					currentAuctionInfo.getCurrentOwnerName() + "</td></tr>");

			String currencyName =
					ItemTable.getInstance().getTemplate(currentAuctionInfo.getCurrentCurrency()).getName();

			long maxAdenaPrice = (long) 90 * 1000000000; // 99kkk
			if (currentAuctionInfo.getCurrentCurrency() == 57 && currentAuctionInfo.getCurrentPrice() > maxAdenaPrice)
			{
				currencyName = "Dreams Coin";
			}

			sb.append("<tr><td>Currency:</td><td>" + currencyName + "</td></tr>");
			sb.append("<tr><td>Current Bid:</td><td>" + currentAuctionInfo.getCurrentPrice() + "</td></tr>");

			int curId = 0;
			for (Currency cur : Currency.values())
			{
				if (currentAuctionInfo.getCurrentCurrency() == cur.ItemId)
				{
					break;
				}

				curId++;
			}

			String options = "";
			for (int index = curId; index < Currency.values().length; index++)
			{
				options += Currency.values()[index] + ";";
			}

			if (!options.isEmpty() && curId < Currency.values().length)
			{
				options = options.substring(0, options.length() - 1);
				sb.append(
						"<tr><td>Select:</td><td><combobox width=100 height=17 var=\"plcoin" + currentAuction.getKey() +
								"\" list=" + options + "></td></tr>");
			}

			sb.append("<tr><td>Your bid:</td><td><edit var=\"plbid" + currentAuction.getKey() +
					"\" width=100 type=number length=14></td></tr>");
			sb.append("<tr><td></td><td><br><button action=\"bypass _bbscustom;action;bid;" + currentAuction.getKey() +
					"; $plbid" + currentAuction.getKey() + " ; $plcoin" + currentAuction.getKey() +
					"\" value=BID! width=105 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></td></tr>");
			sb.append("<tr><td></td><td></td></tr></table><br><br></td><td FIXWIDTH=80></td></tr>");
			sb.append("</table><br>");

			count++;
			size--;

			if (count == 2 || size == 0)
			{
				sb.append("</td></tr>");
				count = 0;
			}
			else
			{
				sb.append("</td>");
			}
		}

		sb.append("</table>");
		return sb.toString();
	}

	/**
	 * Load all the shit
	 */
	public void load(boolean reload)
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File file = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/itemAuction.xml");
		if (!file.exists())
		{
			return;
		}

		Document doc = null;
		try
		{
			doc = factory.newDocumentBuilder().parse(file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeName().equalsIgnoreCase("list"))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (d.getNodeName().equalsIgnoreCase("auction"))
					{
						int auctionId = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());

						int count = Integer.parseInt(d.getAttributes().getNamedItem("count").getNodeValue());
						int repeatTime = d.getAttributes().getNamedItem("repeatTime") == null ? 0 :
								Integer.parseInt(d.getAttributes().getNamedItem("repeatTime").getNodeValue()) *
										3600; // It's in hours
						int randomRepeatTime = d.getAttributes().getNamedItem("randomRepeatTime") == null ? 0 :
								Integer.parseInt(d.getAttributes().getNamedItem("randomRepeatTime").getNodeValue()) *
										3600; // It's in hours
						int initialCurrencyId =
								Integer.parseInt(d.getAttributes().getNamedItem("initialCurrencyId").getNodeValue());
						int highestCurrencyId = 0;

						if (d.getAttributes().getNamedItem("highestCurrencyId") != null)
						{
							highestCurrencyId = Integer.parseInt(
									d.getAttributes().getNamedItem("highestCurrencyId").getNodeValue());
						}

						int initialPrice =
								Integer.parseInt(d.getAttributes().getNamedItem("initialPrice").getNodeValue());
						int initialDuration =
								Integer.parseInt(d.getAttributes().getNamedItem("initialDuration").getNodeValue()) *
										3600; // It's in hours

						int[] auctionItems = null;
						if (d.getAttributes().getNamedItem("itemId") == null)
						{
							List<Integer> itemIds = new ArrayList<>();
							for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
							{
								if (e.getNodeName().equalsIgnoreCase("item"))
								{
									itemIds.add(Integer.parseInt(e.getAttributes().getNamedItem("id").getNodeValue()));
								}
							}

							auctionItems = new int[itemIds.size()];
							Iterator<Integer> iter = itemIds.iterator();
							for (int i = 0; iter.hasNext(); i++)
							{
								auctionItems[i] = iter.next();
							}

							System.out.println("Loaded " + auctionItems.length + " for new format auction.");
						}
						else
						{
							String[] itemIds = d.getAttributes().getNamedItem("itemId").getNodeValue().split(",");
							auctionItems = new int[itemIds.length];
							for (int i = 0; i < auctionItems.length; i++)
							{
								auctionItems[i] = Integer.parseInt(itemIds[i]);
							}

							System.out.println("Loaded " + auctionItems.length + " for old format auction.");
						}

						AuctionTemplate itemAuction =
								new AuctionTemplate(auctionId, auctionItems, count, repeatTime, randomRepeatTime,
										initialCurrencyId, highestCurrencyId, initialPrice, initialDuration, !reload);
						_auctionTemplates.put(auctionId, itemAuction);
					}
				}
			}
		}

		Log.info("ItemAuction: Loaded: " + _auctionTemplates.size() + " auctions!");

		if (!reload)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(
						"SELECT id, itemId, templateId, currencyId, currentBid, ownerId, endTime FROM `custom_auctions`");
				ResultSet rs = statement.executeQuery();

				while (rs.next())
				{
					int auctionId = rs.getInt("id");
					long remainingTime = rs.getLong("endTime") * 1000 - System.currentTimeMillis();
					if (remainingTime < ADDED_DURATION)
					{
						remainingTime = ADDED_DURATION;
					}

					AuctionTemplate template = _auctionTemplates.get(rs.getInt("templateId"));
					if (template == null)
					{
						Log.warning("CustomAuctionManager: Found a null template with id:" + rs.getInt("templateId"));
						continue;
					}
					Auction auction = new Auction(auctionId, rs.getInt("itemId"), template, rs.getInt("currencyId"),
							rs.getLong("currentBid"), rs.getInt("ownerId"), remainingTime);
					_auctions.put(auctionId, auction);
				}
				statement.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	/**
	 * Called at each player bid try, we do all the checks here to deceide if it's avalid or not
	 *
	 * @param activeChar
	 * @param bidId
	 * @param playerBid
	 */
	public void tryToBid(L2PcInstance activeChar, int bidId, long playerBid, String coin)
	{
		if (activeChar == null)
		{
			return;
		}

		if (!activeChar.getClient().getFloodProtectors().getTransaction().tryPerformAction("buy"))
		{
			return;
		}

		if (ThreadPoolManager.getInstance().isShutdown())
		{
			activeChar.sendMessage("Item Auction: Can't bid while at shutdown...!");
			return;
		}

		Auction bid = _auctions.get(bidId);
		if (bid == null)
		{
			return;
		}

		//Don't allow overcome the bid if it's the same player TODO IMPORTANT DO NOT DELETE
		if (activeChar.getObjectId() == bid.getCurrentOwnerId() && !activeChar.isGM())
		{
			activeChar.sendMessage("You can't overbid your own bid!");
			return;
		}
		if (activeChar.getPrivateStoreType() != 0 || activeChar.isInCrystallize())
		{
			activeChar.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			return;
		}

		if (bid.getCurrentOwnerName().equals("Ghost"))
		{
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
		if (minBid <= bid.getCurrentPrice())
		{
			minBid = bid.getCurrentPrice() + 1;
		}

		long maxAdenaPrice = (long) 90 * 1000000000; // 99kkk
		if (bid.getCurrentCurrency() == 57 && bid.getCurrentPrice() > maxAdenaPrice)
		{
			coin = "Dreams";
		}

		if (!coin.isEmpty())
		{
			int currencyId = 0;
			Currency playerCurrency = null;

			switch (coin)
			{
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

				switch (highestCurrencyId)
				{
					case 57:
					{
						if (currencyId != 57 && currencyId != 50002 ||
								currencyId == 50002 && bid.getCurrentPrice() < maxAdenaPrice)
						{
							activeChar.sendMessage("You can only bid Adena for this auction.");
							return;
						}
					}
				}

				if (currencyId == 50002)
				{
					if (playerBid < 10000)
					{
						activeChar.sendMessage(
								"Item Auction: The minimum bid for this item should be for: 10.000 Dreams Coin.");
						return;
					}
				}
				else
				{
					int index = 0;
					for (Currency cur : Currency.values())
					{
						if (currentCurrencyId == cur.ItemId)
						{
							break;
						}
						index++;
					}

					if (playerCurrency.ordinal() < index && !activeChar.isGM())
					{
						//activeChar.sendMessage("Ordinal " + playerCurrency.ordinal());
						//activeChar.sendMessage("Index " + index);
						return;
					}
				}

				currentCurrencyId = currencyId;
				minBid = 1; //?
			}
		}

		if (playerBid < minBid && !activeChar.isGM())
		{
			activeChar.sendMessage("Item Auction: The minimum bid for this item should be for: " + minBid + " " +
					ItemTable.getInstance().getTemplate(bid.getCurrentCurrency()).getName());
			return;
		}

		if (!activeChar.destroyItemByItemId("ItemAuction", currentCurrencyId, playerBid, null, true))
		{
			return;
		}

		restoreLoserBid(bid.getCurrentOwnerId(), bid.getCurrentCurrency(), bid.getCurrentPrice());

		String oldBidderName = bid.getCurrentOwnerName();

		bid.setCurrentCurrency(currentCurrencyId);//Need do only if is not same but beh
		bid.setOwner(activeChar.getObjectId(), playerBid);

		String message = "";
		String itemName = ItemTable.getInstance().getTemplate(bid.getItemId()).getName();
		String currencyName = bid.getCurrentCurrency() == 57 ? "Adenas" : "Golden Apigas";

		if (bid.getCurrentCurrency() == 50002)
		{
			currencyName = "Dreams Coin";
		}

		String priceString = "";
		long price = bid.getCurrentPrice();
		long counter = 0;
		while (price > 0)
		{
			priceString = price % 10 + priceString;
			price /= 10;
			counter++;
			if (counter % 3 == 0)
			{
				priceString = "," + priceString;
			}
		}

		if (counter % 3 == 0)
		{
			priceString = priceString.substring(1);
		}

		if (!activeChar.isGM())
		{
			if (oldBidderName.equals("None"))
			{
				message = activeChar.getName() + " placed a bid on " + itemName + " - " + priceString + " " +
						currencyName + ".";
			}
			else
			{
				message = activeChar.getName() + " outbided " + oldBidderName + " with a " + priceString + " " +
						currencyName + " bid for " + itemName + ".";
			}

			L2NpcInstance auctionManager = null;
			for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
			{
				if (auctionManager == null)
				{
					auctionManager = Util.getNpcCloseTo(33782, player);
				}

				player.sendPacket(
						new CreatureSay(auctionManager == null ? 0x00 : auctionManager.getObjectId(), Say2.TRADE,
								"Jolie", message));
			}
		}
	}

	private void restoreLoserBid(int playerId, int currencyId, long count)
	{
		giveItemToPlayer(playerId, currencyId, count, "Your bid has been overcome!");
	}

	/**
	 * Used for add the reward item to the player or give back the bids
	 *
	 * @param playerId
	 * @param itemId
	 * @param count
	 */
	private void giveItemToPlayer(int playerId, int itemId, long count, String text)
	{
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

	private CustomAuctionManager()
	{
		load(false);
	}

	public static CustomAuctionManager getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CustomAuctionManager _instance = new CustomAuctionManager();
	}
}
