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
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.instancemanager.ClanHallAuctionManager;
import l2server.gameserver.instancemanager.ClanHallManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.entity.Auction;
import l2server.gameserver.model.entity.Auction.Bidder;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;

import java.text.SimpleDateFormat;
import java.util.*;

import static l2server.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;

public final class L2AuctioneerInstance extends L2Npc
{
	private static final int COND_ALL_FALSE = 0;
	private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static final int COND_REGULAR = 3;

	private Map<Integer, Auction> _pendingAuctions = new HashMap<>();

	public L2AuctioneerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2AuctioneerInstance);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		int condition = validateCondition(player);
		if (condition <= COND_ALL_FALSE)
		{
			//TODO: html
			player.sendMessage("Wrong conditions.");
			return;
		}
		else if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
		{
			String filename = "auction/auction-busy.htm";
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player.getHtmlPrefix(), filename);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
			return;
		}
		else if (condition == COND_REGULAR)
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String actualCommand = st.nextToken(); // Get actual command

			String val = "";
			if (st.countTokens() >= 1)
			{
				val = st.nextToken();
			}

			if (actualCommand.equalsIgnoreCase("auction"))
			{
				if (val.isEmpty())
				{
					return;
				}

				try
				{
					int days = Integer.parseInt(val);
					try
					{
						SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
						long bid = 0;
						if (st.countTokens() >= 1)
						{
							bid = Math.min(Long.parseLong(st.nextToken()), MAX_ADENA);
						}

						Auction a =
								new Auction(player.getClan().getHasHideout(), player.getClan(), days * 86400000L, bid,
										ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getName());
						if (_pendingAuctions.get(a.getId()) != null)
						{
							_pendingAuctions.remove(a.getId());
						}

						_pendingAuctions.put(a.getId(), a);

						String filename = "auction/AgitSale3.htm";
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(player.getHtmlPrefix(), filename);
						html.replace("%x%", val);
						html.replace("%ITEM_NAME%", getCurrencyName());
						html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(a.getEndDate())));
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_MIN%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_DESC%",
								ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getDesc());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_sale2");
						html.replace("%AGIT_CURRENCY%",
								ItemTable.getInstance().getTemplate(Config.CH_BID_ITEMID).getName());
						html.replace("%objectId%", String.valueOf(getObjectId()));
						player.sendPacket(html);
					}
					catch (Exception e)
					{
						player.sendMessage("Invalid bid!");
					}
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction duration!");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("confirmAuction"))
			{
				try
				{
					Auction a = _pendingAuctions.get(player.getClan().getHasHideout());
					a.confirmAuction();
					_pendingAuctions.remove(player.getClan().getHasHideout());
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("bidding"))
			{
				if (val.isEmpty())
				{
					return;
				}

				if (Config.DEBUG)
				{
					Log.warning("bidding show successful");
				}

				try
				{
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
					int auctionId = Integer.parseInt(val);

					if (Config.DEBUG)
					{
						Log.warning("auction test started");
					}

					String filename = "auction/AgitAuctionInfo.htm";
					Auction a = ClanHallAuctionManager.getInstance().getAuction(auctionId);

					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getHtmlPrefix(), filename);
					if (a != null)
					{
						html.replace("%AGIT_NAME%", a.getItemName());
						html.replace("%OWNER_PLEDGE_NAME%", a.getSellerClanName());
						html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
						html.replace("%AGIT_SIZE%", String.valueOf(
								ClanHallManager.getInstance().getClanHallById(a.getItemId()).getGrade() * 10));
						html.replace("%AGIT_LEASE%", String.valueOf(
								ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLease()));
						html.replace("%AGIT_LOCATION%",
								ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation());
						html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(a.getEndDate())));
						html.replace("%AGIT_AUCTION_REMAIN%",
								String.valueOf((a.getEndDate() - System.currentTimeMillis()) / 3600000) + " hours " +
										String.valueOf((a.getEndDate() - System.currentTimeMillis()) / 60000 % 60) +
										" minutes");
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_COUNT%", String.valueOf(a.getBidders().size()));
						html.replace("%AGIT_AUCTION_DESC%",
								ClanHallManager.getInstance().getClanHallById(a.getItemId()).getDesc());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_list");
						html.replace("%AGIT_LINK_BIDLIST%", "bypass -h npc_" + getObjectId() + "_bidlist " + a.getId());
						html.replace("%AGIT_LINK_RE%", "bypass -h npc_" + getObjectId() + "_bid1 " + a.getId());
						html.replace("%AGIT_CURRENCY%", getCurrencyName());
					}
					else
					{
						Log.warning("Auctioneer Auction null for AuctionId : " + auctionId);
					}

					player.sendPacket(html);
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("bid"))
			{
				if (val.isEmpty())
				{
					return;
				}

				try
				{
					int auctionId = Integer.parseInt(val);
					try
					{
						long bid = 0;
						if (st.countTokens() >= 1)
						{
							bid = Math.min(Long.parseLong(st.nextToken()), MAX_ADENA);
						}

						ClanHallAuctionManager.getInstance().getAuction(auctionId).setBid(player, bid);
					}
					catch (Exception e)
					{
						player.sendMessage("Invalid bid!");
					}
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("bid1"))
			{
				if (player.getClan() == null || player.getClan().getLevel() < 2)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUCTION_ONLY_CLAN_LEVEL_2_HIGHER));
					return;
				}

				if (val.isEmpty())
				{
					return;
				}

				if (player.getClan().getAuctionBiddedAt() > 0 &&
						player.getClan().getAuctionBiddedAt() != Integer.parseInt(val) ||
						player.getClan().getHasHideout() > 0)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_SUBMITTED_BID));
					return;
				}

				try
				{
					String filename = "auction/AgitBid1.htm";

					long minimumBid = ClanHallAuctionManager.getInstance().getAuction(Integer.parseInt(val))
							.getHighestBidderMaxBid();
					if (minimumBid == 0)
					{
						minimumBid =
								ClanHallAuctionManager.getInstance().getAuction(Integer.parseInt(val)).getStartingBid();
					}

					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getHtmlPrefix(), filename);
					html.replace("%ITEM_NAME%", getCurrencyName());
					html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_bidding " + val);
					html.replace("%PLEDGE_ADENA%", String.valueOf(
							player.getClan().getWarehouse().getItemByItemId(Config.CH_BID_ITEMID) != null ?
									player.getClan().getWarehouse().getItemByItemId(Config.CH_BID_ITEMID).getCount() :
									0));
					html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(minimumBid));
					html.replace("%AGIT_CURRENCY%", getCurrencyName());
					html.replace("npc_%objectId%_bid", "npc_" + getObjectId() + "_bid " + val);
					player.sendPacket(html);
					return;
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("list"))
			{
				List<Auction> auctions = ClanHallAuctionManager.getInstance().getAuctions();
				SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd");
				/* Limit for make new page, prevent client crash **/
				int limit = 15;
				int start;
				int i = 1;
				double npage = Math.ceil((float) auctions.size() / limit);

				if (val.isEmpty())
				{
					start = 1;
				}
				else
				{
					start = limit * (Integer.parseInt(val) - 1) + 1;
					limit *= Integer.parseInt(val);
				}

				if (Config.DEBUG)
				{
					Log.warning("cmd list: auction test started");
				}

				String items = "";
				items += "<table width=280 border=0><tr>";
				for (int j = 1; j <= npage; j++)
				{
					items += "<td><center><a action=\"bypass -h npc_" + getObjectId() + "_list " + j + "\"> Page " + j +
							" </a></center></td>";
				}

				items += "</tr></table>" + "<table width=280 border=0>";

				for (Auction a : auctions)
				{
					if (a == null)
					{
						continue;
					}

					if (ClanHallManager.getInstance().getClanHallById(a.getItemId()) == null)
					{
						Log.warning("Auction set for non existing clan hall! Id: " + a.getId() + ", item name: " +
								a.getItemName());
						continue;
					}

					if (i > limit)
					{
						break;
					}
					else if (i < start)
					{
						i++;
						continue;
					}
					else
					{
						i++;
					}

					items += "<tr>" + "<td>" +
							ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation() + "</td>" +
							"<td><a action=\"bypass -h npc_" + getObjectId() + "_bidding " + a.getId() + "\">" +
							a.getItemName() + "</a></td>" + "<td>" + format.format(a.getEndDate()) + "</td>" + "<td>" +
							a.getStartingBid() + "</td>" + "</tr>";
				}

				items += "</table>";
				String filename = "auction/AgitAuctionList.htm";

				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(player.getHtmlPrefix(), filename);
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
				html.replace("%itemsField%", items);
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("bidlist"))
			{
				int auctionId = 0;
				if (val.isEmpty())
				{
					if (player.getClan().getAuctionBiddedAt() <= 0)
					{
						return;
					}
					else
					{
						auctionId = player.getClan().getAuctionBiddedAt();
					}
				}
				else
				{
					auctionId = Integer.parseInt(val);
				}

				if (Config.DEBUG)
				{
					Log.warning("cmd bidlist: auction test started");
				}

				String biders = "";
				Map<Integer, Bidder> bidders = ClanHallAuctionManager.getInstance().getAuction(auctionId).getBidders();
				for (Bidder b : bidders.values())
				{
					biders += "<tr>" + "<td>" + b.getClanName() + "</td><td>" + b.getName() + "</td><td>" +
							b.getTimeBid().get(Calendar.YEAR) + "/" + (b.getTimeBid().get(Calendar.MONTH) + 1) + "/" +
							b.getTimeBid().get(Calendar.DATE) + "</td><td>" + b.getBid() + "</td>" + "</tr>";
				}
				String filename = "auction/AgitBidderList.htm";

				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(player.getHtmlPrefix(), filename);
				html.replace("%AGIT_LIST%", biders);
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
				html.replace("%x%", val);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("selectedItems"))
			{
				if (player.getClan() != null && player.getClan().getHasHideout() == 0 &&
						player.getClan().getAuctionBiddedAt() > 0)
				{
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
					String filename = "auction/AgitBidInfo.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getHtmlPrefix(), filename);
					Auction a = ClanHallAuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
					if (a != null)
					{
						html.replace("%ITEM_NAME%", getCurrencyName());
						html.replace("%AGIT_NAME%", a.getItemName());
						html.replace("%OWNER_PLEDGE_NAME%", a.getSellerClanName());
						html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
						html.replace("%AGIT_SIZE%", String.valueOf(
								ClanHallManager.getInstance().getClanHallById(a.getItemId()).getGrade() * 10));
						html.replace("%AGIT_LEASE%", String.valueOf(
								ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLease()));
						html.replace("%AGIT_LOCATION%",
								ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation());
						html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(a.getEndDate())));
						html.replace("%AGIT_AUCTION_REMAIN%",
								String.valueOf((a.getEndDate() - System.currentTimeMillis()) / 3600000) + " hours " +
										String.valueOf((a.getEndDate() - System.currentTimeMillis()) / 60000 % 60) +
										" minutes");
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_MYBID%",
								String.valueOf(a.getBidders().get(player.getClanId()).getBid()));
						html.replace("%AGIT_AUCTION_DESC%",
								ClanHallManager.getInstance().getClanHallById(a.getItemId()).getDesc());
						html.replace("%AGIT_CURRENCY%", getCurrencyName());
						html.replace("%objectId%", String.valueOf(getObjectId()));
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
					}
					else
					{
						Log.warning("Auctioneer Auction null for AuctionBiddedAt : " +
								player.getClan().getAuctionBiddedAt());
					}

					player.sendPacket(html);
					return;
				}
				else if (player.getClan() != null &&
						ClanHallAuctionManager.getInstance().getAuction(player.getClan().getHasHideout()) != null)
				{
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
					String filename = "auction/AgitSaleInfo.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getHtmlPrefix(), filename);
					Auction a = ClanHallAuctionManager.getInstance().getAuction(player.getClan().getHasHideout());
					if (a != null)
					{
						html.replace("%ITEM_NAME%", getCurrencyName());
						html.replace("%AGIT_NAME%", a.getItemName());
						html.replace("%AGIT_OWNER_PLEDGE_NAME%", a.getSellerClanName());
						html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
						html.replace("%AGIT_SIZE%", String.valueOf(
								ClanHallManager.getInstance().getClanHallById(a.getItemId()).getGrade() * 10));
						html.replace("%AGIT_LEASE%", String.valueOf(
								ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLease()));
						html.replace("%AGIT_LOCATION%",
								ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation());
						html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(a.getEndDate())));
						html.replace("%AGIT_AUCTION_REMAIN%",
								String.valueOf((a.getEndDate() - System.currentTimeMillis()) / 3600000) + " hours " +
										String.valueOf((a.getEndDate() - System.currentTimeMillis()) / 60000 % 60) +
										" minutes");
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_BIDCOUNT%", String.valueOf(a.getBidders().size()));
						html.replace("%AGIT_AUCTION_DESC%",
								ClanHallManager.getInstance().getClanHallById(a.getItemId()).getDesc());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
						html.replace("%AGIT_CURRENCY%", getCurrencyName());
						html.replace("%id%", String.valueOf(a.getId()));
						html.replace("%objectId%", String.valueOf(getObjectId()));
					}
					else
					{
						Log.warning("Auctioneer Auction null for getHasHideout : " + player.getClan().getHasHideout());
					}

					player.sendPacket(html);
					return;
				}
				else if (player.getClan() != null && player.getClan().getHasHideout() != 0)
				{
					int ItemId = player.getClan().getHasHideout();
					String filename = "auction/AgitInfo.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getHtmlPrefix(), filename);
					if (ClanHallManager.getInstance().getClanHallById(ItemId) != null)
					{
						html.replace("%ITEM_NAME%", getCurrencyName());
						html.replace("%AGIT_NAME%", ClanHallManager.getInstance().getClanHallById(ItemId).getName());
						html.replace("%AGIT_OWNER_PLEDGE_NAME%", player.getClan().getName());
						html.replace("%OWNER_PLEDGE_MASTER%", player.getClan().getLeaderName());
						html.replace("%AGIT_SIZE%",
								String.valueOf(ClanHallManager.getInstance().getClanHallById(ItemId).getGrade() * 10));
						html.replace("%AGIT_LEASE%",
								String.valueOf(ClanHallManager.getInstance().getClanHallById(ItemId).getLease()));
						html.replace("%AGIT_LOCATION%",
								ClanHallManager.getInstance().getClanHallById(ItemId).getLocation());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
						html.replace("%AGIT_CURRENCY%", getCurrencyName());
						html.replace("%objectId%", String.valueOf(getObjectId()));
					}
					else
					{
						Log.warning("Clan Hall ID NULL : " + ItemId +
								" Can be caused by concurent write in ClanHallManager");
					}

					player.sendPacket(html);
					return;
				}
				else if (player.getClan() != null && player.getClan().getHasHideout() == 0)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_OFFERINGS_OWN_OR_MADE_BID_FOR));
					return;
				}
				else if (player.getClan() == null)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_PARTICIPATE_IN_AN_AUCTION));
					return;
				}
			}
			else if (actualCommand.equalsIgnoreCase("cancelBid"))
			{
				long bid = ClanHallAuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt())
						.getBidders().get(player.getClanId()).getBid();
				String filename = "auction/AgitBidCancel.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(player.getHtmlPrefix(), filename);
				html.replace("%ITEM_NAME%", getCurrencyName());
				html.replace("%AGIT_BID%", String.valueOf(bid));
				html.replace("%AGIT_BID_REMAIN%", String.valueOf((long) (bid * 0.9)));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
				html.replace("%AGIT_CURRENCY%", getCurrencyName());
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("doCancelBid"))
			{
				if (ClanHallAuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()) != null)
				{
					ClanHallAuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt())
							.cancelBid(player.getClanId());
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANCELED_BID));
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("cancelAuction"))
			{
				if (!((player.getClanPrivileges() & L2Clan.CP_CH_AUCTION) == L2Clan.CP_CH_AUCTION))
				{
					String filename = "auction/not_authorized.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getHtmlPrefix(), filename);
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
					return;
				}
				String filename = "auction/AgitSaleCancel.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(player.getHtmlPrefix(), filename);
				html.replace("%ITEM_NAME%", getCurrencyName());
				html.replace("%AGIT_DEPOSIT%",
						String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
				html.replace("%AGIT_CURRENCY%", getCurrencyName());
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("doCancelAuction"))
			{
				if (ClanHallAuctionManager.getInstance().getAuction(player.getClan().getHasHideout()) != null)
				{
					ClanHallAuctionManager.getInstance().getAuction(player.getClan().getHasHideout()).cancelAuction();
					player.sendMessage("Your auction has been canceled");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("sale2"))
			{
				String filename = "auction/AgitSale2.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(player.getHtmlPrefix(), filename);
				html.replace("%ITEM_NAME%", getCurrencyName());
				html.replace("%AGIT_LAST_PRICE%",
						String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_sale");
				html.replace("%AGIT_CURRENCY%", getCurrencyName());
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("sale"))
			{
				if (!((player.getClanPrivileges() & L2Clan.CP_CH_AUCTION) == L2Clan.CP_CH_AUCTION))
				{
					String filename = "auction/not_authorized.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getHtmlPrefix(), filename);
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
					return;
				}
				String filename = "auction/AgitSale1.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(player.getHtmlPrefix(), filename);
				html.replace("%ITEM_NAME%", getCurrencyName());
				html.replace("%AGIT_DEPOSIT%",
						String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
				html.replace("%AGIT_PLEDGE_ADENA%", String.valueOf(
						player.getClan().getWarehouse().getItemByItemId(Config.CH_BID_ITEMID) != null ?
								player.getClan().getWarehouse().getItemByItemId(Config.CH_BID_ITEMID).getCount() : 0));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
				html.replace("%AGIT_CURRENCY%", getCurrencyName());
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("rebid"))
			{
				SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
				if (!((player.getClanPrivileges() & L2Clan.CP_CH_AUCTION) == L2Clan.CP_CH_AUCTION))
				{
					String filename = "auction/not_authorized.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getHtmlPrefix(), filename);
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
					return;
				}
				try
				{
					String filename = "auction/AgitBid2.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getHtmlPrefix(), filename);
					Auction a = ClanHallAuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
					if (a != null)
					{
						html.replace("%ITEM_NAME%", getCurrencyName());
						html.replace("%AGIT_AUCTION_BID%",
								String.valueOf(a.getBidders().get(player.getClanId()).getBid()));
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(a.getEndDate())));
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
						html.replace("%AGIT_CURRENCY%", getCurrencyName());
						html.replace("npc_%objectId%_bid1", "npc_" + getObjectId() + "_bid1 " + a.getId());
					}
					else
					{
						Log.warning("Auctioneer Auction null for AuctionBiddedAt : " +
								player.getClan().getAuctionBiddedAt());
					}

					player.sendPacket(html);
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("location"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(player.getHtmlPrefix(), "auction/location.htm");
				html.replace("%location%", MapRegionTable.getInstance().getClosestTownName(player));
				html.replace("%LOCATION%", getPictureName(player));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("start"))
			{
				showChatWindow(player);
				return;
			}
		}

		super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename = "auction/auction-no.htm";

		int condition = validateCondition(player);
		if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
		{
			filename = "auction/auction-busy.htm"; // Busy because of siege
		}
		else
		{
			filename = "auction/auction.htm";
		}

		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	private int validateCondition(L2PcInstance player)
	{
		if (getCastle() != null && getCastle().getCastleId() > 0)
		{
			if (getCastle().getSiege().getIsInProgress())
			{
				return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
			}
			else
			{
				return COND_REGULAR;
			}
		}

		return COND_ALL_FALSE;
	}

	private String getPictureName(L2PcInstance plyr)
	{
		int nearestTownId = MapRegionTable.getInstance().getMapRegion(plyr.getX(), plyr.getY());
		String nearestTown;

		switch (nearestTownId)
		{
			case 5:
				nearestTown = "GLUDIO";
				break;
			case 6:
				nearestTown = "GLUDIN";
				break;
			case 7:
				nearestTown = "DION";
				break;
			case 8:
				nearestTown = "GIRAN";
				break;
			case 14:
				nearestTown = "RUNE";
				break;
			case 15:
				nearestTown = "GODARD";
				break;
			case 16:
				nearestTown = "SCHUTTGART";
				break;
			default:
				nearestTown = "ADEN";
				break;
		}

		return nearestTown;
	}

	private String getCurrencyName()
	{
		return ItemTable.getInstance().getTemplate(Config.CH_BID_ITEMID).getName();
	}
}
