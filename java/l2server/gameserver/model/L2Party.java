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

package l2server.gameserver.model;

import gnu.trove.TIntIntHashMap;
import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.TimeController;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.DuelManager;
import l2server.gameserver.instancemanager.PartySearchManager;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2ApInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @author nuocnam
 * @version $Revision: 1.6.2.2.2.6 $ $Date: 2005/04/11 19:12:16 $
 */
public class L2Party
{

	//private static final double[] BONUS_EXP_SP = {1, 1.30, 1.39, 1.50, 1.54, 1.58, 1.63, 1.67, 1.71};
	// TODO: Soul: I do believe are correct doubles. It must be:
	// 0%, 10%, 20%, 30%, 40%, 50%, 100%, 110%, 120%
	private static final double[] BONUS_EXP_SP = {1, 1.10, 1.20, 1.30, 1.40, 1.50, 2, 2.10, 2.20};
	//TODO: JIV - unhardcode to some SysString enum (sysstring-e.dat)
	private static final int[] LOOT_SYSSTRINGS = {487, 488, 798, 799, 800};
	private static final int PARTY_POSITION_BROADCAST = 12000;

	public static final byte ITEM_LOOTER = 0;
	public static final byte ITEM_RANDOM = 1;
	public static final byte ITEM_RANDOM_SPOIL = 2;
	public static final byte ITEM_ORDER = 3;
	public static final byte ITEM_ORDER_SPOIL = 4;

	private final CopyOnWriteArrayList<L2PcInstance> _members;
	private boolean _pendingInvitation = false;
	private long _pendingInviteTimeout;
	private int _partyLvl = 0;
	private int _itemDistribution = 0;
	private int _itemLastLoot = 0;
	private L2CommandChannel _commandChannel = null;
	private byte _requestChangeLoot = -1;
	private List<Integer> _changeLootAnswers = null;
	private long _requestChangeLootTimer = 0;
	private Future<?> _checkTask = null;
	private Future<?> _positionBroadcastTask = null;
	private PartyMemberPosition _positionPacket;
	private boolean _disbanding = false;

	/**
	 * The message type send to the party members.
	 */
	public enum messageType
	{
		Expelled, Left, None, Disconnected
	}

	/**
	 * constructor ensures party has always one member - leader
	 *
	 * @param leader
	 */
	public L2Party(L2PcInstance leader, int itemDistribution)
	{
		_members = new CopyOnWriteArrayList<>();
		_itemDistribution = itemDistribution;
		getPartyMembers().add(leader);
		_partyLvl = leader.getLevel();
	}

	/**
	 * returns number of party members
	 *
	 * @return
	 */
	public int getMemberCount()
	{
		return getPartyMembers().size();
	}

	/**
	 * Check if another player can start invitation process
	 *
	 * @return boolean if party waits for invitation respond
	 */
	public boolean getPendingInvitation()
	{
		return _pendingInvitation;
	}

	/**
	 * set invitation process flag and store time for expiration
	 * happens when: player join party or player decline to join
	 */
	public void setPendingInvitation(boolean val)
	{
		_pendingInvitation = val;
		_pendingInviteTimeout =
				TimeController.getGameTicks() + L2PcInstance.REQUEST_TIMEOUT * TimeController.TICKS_PER_SECOND;
	}

	/**
	 * Check if player invitation is expired
	 *
	 * @return boolean if time is expired
	 * @see l2server.gameserver.model.actor.instance.L2PcInstance#isRequestExpired()
	 */
	public boolean isInvitationRequestExpired()
	{
		return !(_pendingInviteTimeout > TimeController.getGameTicks());
	}

	/**
	 * returns all party members
	 *
	 * @return
	 */
	public final CopyOnWriteArrayList<L2PcInstance> getPartyMembers()
	{
		return _members;
	}

	/**
	 * get random member from party
	 *
	 * @return
	 */
	private L2PcInstance getCheckedRandomMember(int ItemId, L2Character target)
	{
		List<L2PcInstance> availableMembers = new ArrayList<>();
		for (L2PcInstance member : getPartyMembers())
		{
			if (member.getInventory().validateCapacityByItemId(ItemId) &&
					Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
			{
				availableMembers.add(member);
			}
		}
		if (!availableMembers.isEmpty())
		{
			return availableMembers.get(Rnd.get(availableMembers.size()));
		}
		else
		{
			return null;
		}
	}

	/**
	 * get next item looter
	 *
	 * @return
	 */
	private L2PcInstance getCheckedNextLooter(int ItemId, L2Character target)
	{
		for (int i = 0; i < getMemberCount(); i++)
		{
			if (++_itemLastLoot >= getMemberCount())
			{
				_itemLastLoot = 0;
			}
			L2PcInstance member;
			try
			{
				member = getPartyMembers().get(_itemLastLoot);
				if (member.getInventory().validateCapacityByItemId(ItemId) &&
						Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
				{
					return member;
				}
			}
			catch (Exception e)
			{
				// continue, take another member if this just logged off
			}
		}

		return null;
	}

	/**
	 * get next item looter
	 *
	 * @return
	 */
	private L2PcInstance getActualLooter(L2PcInstance player, int ItemId, boolean spoil, L2Character target)
	{
		L2PcInstance looter = player;

		switch (_itemDistribution)
		{
			case ITEM_RANDOM:
				if (!spoil)
				{
					looter = getCheckedRandomMember(ItemId, target);
				}
				break;
			case ITEM_RANDOM_SPOIL:
				looter = getCheckedRandomMember(ItemId, target);
				break;
			case ITEM_ORDER:
				if (!spoil)
				{
					looter = getCheckedNextLooter(ItemId, target);
				}
				break;
			case ITEM_ORDER_SPOIL:
				looter = getCheckedNextLooter(ItemId, target);
				break;
		}

		if (looter == null)
		{
			looter = player;
		}
		return looter;
	}

	/**
	 * true if player is party leader
	 *
	 * @param player
	 * @return
	 */
	public boolean isLeader(L2PcInstance player)
	{
		return getLeader().equals(player);
	}

	/**
	 * Returns the Object ID for the party leader to be used as a unique identifier of this party
	 *
	 * @return int
	 */
	public int getPartyLeaderOID()
	{
		return getLeader().getObjectId();
	}

	/**
	 * Broadcasts packet to every party member
	 *
	 * @param msg
	 */
	public void broadcastToPartyMembers(L2GameServerPacket msg)
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null)
			{
				member.sendPacket(msg);
			}
		}
	}

	public void broadcastToPartyMembersNewLeader()
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null)
			{
				member.sendPacket(new PartySmallWindowDeleteAll());
				member.sendPacket(new PartySmallWindowAll(member, this));
				member.broadcastUserInfo();
			}
		}
	}

	public void broadcastCSToPartyMembers(CreatureSay msg, L2PcInstance broadcaster)
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null && !BlockList.isBlocked(member, broadcaster))
			{
				member.sendPacket(msg);
			}
		}
	}

	/**
	 * Send a Server->Client packet to all other L2PcInstance of the Party.<BR><BR>
	 */
	public void broadcastToPartyMembers(L2PcInstance player, L2GameServerPacket msg)
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null && !member.equals(player))
			{
				member.sendPacket(msg);
			}
		}
	}

	/**
	 * adds new member to party
	 *
	 * @param player
	 */
	public synchronized void addPartyMember(L2PcInstance player)
	{
		if (getPartyMembers().contains(player))
		{
			return;
		}
		if (getPartyMembers().size() < 3)
		{
			if (getLeader().isSearchingForParty())
			{
				getLeader().closeWaitingSubstitute();
			}
		}
		if (_requestChangeLoot != -1)
		{
			finishLootRequest(false); // cancel on invite
		}
		//sends new member party window for all members
		//we do all actions before adding member to a list, this speeds things up a little
		player.sendPacket(new PartySmallWindowAll(player, this));

		// sends pets/summons of party members
		for (L2PcInstance pMember : getPartyMembers())
		{
			if (pMember != null)
			{
				if (pMember.getPet() != null)
				{
					player.sendPacket(new ExPartyPetWindowAdd(pMember.getPet()));
				}
				for (L2SummonInstance summon : pMember.getSummons())
				{
					player.sendPacket(new ExPartyPetWindowAdd(summon));
				}
			}
		}

		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_JOINED_S1_PARTY);
		msg.addString(getLeader().getName());
		player.sendPacket(msg);

		msg = SystemMessage.getSystemMessage(SystemMessageId.C1_JOINED_PARTY);
		msg.addString(player.getName());
		broadcastToPartyMembers(msg);
		broadcastToPartyMembers(new PartySmallWindowAdd(player, this));
		if (player.isSearchingForParty())
		{
			player.closeWaitingSubstitute();
		}
		// send the position of all party members to the new party member
		//player.sendPacket(new PartyMemberPosition(this));
		// send the position of the new party member to all party members (except the new one - he knows his own position)
		//broadcastToPartyMembers(player, new PartyMemberPosition(this));

		// if member has pet/summon add it to other as well
		if (player.getPet() != null)
		{
			broadcastToPartyMembers(new ExPartyPetWindowAdd(player.getPet()));
		}
		if (player.getSummons() != null)
		{
			for (L2SummonInstance summon : player.getSummons())
			{
				this.broadcastToPartyMembers(player, new ExPartyPetWindowAdd(summon));
			}
		}

		//add player to party, adjust party level
		getPartyMembers().add(player);
		if (player.getLevel() > _partyLvl)
		{
			_partyLvl = player.getLevel();
		}

		// update partySpelled
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null)
			{
				member.updateEffectIcons(true); // update party icons only
				member.broadcastUserInfo();
				if (member.getPet() != null)
				{
					member.getPet().updateEffectIcons();
				}
				for (L2SummonInstance summon : member.getSummons())
				{
					summon.updateEffectIcons();
				}
			}
		}

		// open the CCInformationwindow
		if (isInCommandChannel())
		{
			player.sendPacket(new ExOpenMPCC());
		}

		if (_positionBroadcastTask == null)
		{
			_positionBroadcastTask = ThreadPoolManager.getInstance()
					.scheduleGeneralAtFixedRate(new PositionBroadcast(), PARTY_POSITION_BROADCAST / 2,
							PARTY_POSITION_BROADCAST);
		}
	}

	/**
	 * Remove player from party
	 * Overloaded method that takes player's name as parameter
	 *
	 * @param name
	 */
	public void removePartyMember(String name, messageType type)
	{
		L2PcInstance player = getPlayerByName(name);

		if (player != null)
		{
			removePartyMember(player, type);
		}
	}

	public synchronized void removePartyMember(L2PcInstance player, messageType type)
	{
		if (getPartyMembers().contains(player))
		{
			boolean isLeader = isLeader(player);
			if (!_disbanding)
			{
				if (getPartyMembers().size() == 2 ||
						isLeader && !Config.ALT_LEAVE_PARTY_LEADER && type != messageType.Disconnected)
				{
					disbandParty();
					return;
				}
			}

			getPartyMembers().remove(player);
			recalculatePartyLevel();
			PartySearchManager psm = PartySearchManager.getInstance();
			if (psm.getWannaToChangeThisPlayer(player.getLevel(), player.getClassId()) != null)
			{
				psm.removeChangeThisPlayer(player);
			}

			if (player.isInDuel())
			{
				DuelManager.getInstance().onRemoveFromParty(player);
			}

			try
			{
				if (player.getFusionSkill() != null)
				{
					player.abortCast();
				}

				for (L2Character character : player.getKnownList().getKnownCharacters())
				{
					if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == player)
					{
						character.abortCast();
					}
				}
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, "", e);
			}

			SystemMessage msg;
			if (type == messageType.Expelled)
			{
				player.sendPacket(SystemMessageId.HAVE_BEEN_EXPELLED_FROM_PARTY);
				msg = SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_EXPELLED_FROM_PARTY);
				msg.addString(player.getName());
				broadcastToPartyMembers(msg);
			}
			else if (type == messageType.Left || type == messageType.Disconnected)
			{
				player.sendPacket(SystemMessageId.YOU_LEFT_PARTY);
				msg = SystemMessage.getSystemMessage(SystemMessageId.C1_LEFT_PARTY);
				msg.addString(player.getName());
				broadcastToPartyMembers(msg);
			}

			player.sendPacket(new PartySmallWindowDeleteAll());
			player.setParty(null);

			broadcastToPartyMembers(new PartySmallWindowDelete(player));
			L2Summon pet = player.getPet();
			if (pet != null)
			{
				broadcastToPartyMembers(new ExPartyPetWindowDelete(pet));
			}
			for (L2SummonInstance summon : player.getSummons())
			{
				broadcastToPartyMembers(new ExPartyPetWindowDelete(summon));
			}

			// Close the CCInfoWindow
			if (isInCommandChannel())
			{
				player.sendPacket(new ExCloseMPCC());
			}

			if (isLeader && getPartyMembers().size() > 1 &&
					(Config.ALT_LEAVE_PARTY_LEADER || type == messageType.Disconnected))
			{
				msg = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_BECOME_A_PARTY_LEADER);
				msg.addString(getLeader().getName());
				broadcastToPartyMembers(msg);
				broadcastToPartyMembersNewLeader();
			}

			if (getPartyMembers().size() == 1)
			{
				if (isInCommandChannel())
				{
					// delete the whole command channel when the party who opened the channel is disbanded
					if (getCommandChannel().getChannelLeader().equals(getLeader()))
					{
						getCommandChannel().disbandChannel();
					}
					else
					{
						getCommandChannel().removeParty(this);
					}
				}
				L2PcInstance leader = getLeader();
				if (leader != null)
				{
					leader.setParty(null);
					if (leader.isInDuel())
					{
						DuelManager.getInstance().onRemoveFromParty(leader);
					}
				}
				if (_checkTask != null)
				{
					_checkTask.cancel(true);
					_checkTask = null;
				}
				if (_positionBroadcastTask != null)
				{
					_positionBroadcastTask.cancel(false);
					_positionBroadcastTask = null;
				}
				_members.clear();
			}
		}
	}

	/**
	 * Disperse a party and sends a message to all its members.
	 */
	public void disbandParty()
	{
		_disbanding = true;
		if (_members != null)
		{
			broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.PARTY_DISPERSED));
			for (L2PcInstance member : _members)
			{
				if (member != null)
				{
					removePartyMember(member, messageType.None);
				}
			}
		}
	}

	/**
	 * Change party leader (used for string arguments)
	 *
	 * @param name
	 */
	public void changePartyLeader(String name)
	{
		L2PcInstance player = getPlayerByName(name);

		if (player != null && !player.isInDuel())
		{
			if (getPartyMembers().contains(player))
			{
				if (isLeader(player))
				{
					player.sendPacket(
							SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF));
				}
				else
				{
					//Swap party members
					L2PcInstance temp;
					int p1 = getPartyMembers().indexOf(player);
					temp = getLeader();
					getPartyMembers().set(0, getPartyMembers().get(p1));
					getPartyMembers().set(p1, temp);

					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_BECOME_A_PARTY_LEADER);
					msg.addString(getLeader().getName());
					broadcastToPartyMembers(msg);
					broadcastToPartyMembersNewLeader();
					if (isInCommandChannel() && temp.equals(_commandChannel.getChannelLeader()))
					{
						_commandChannel.setChannelLeader(getLeader());
						msg = SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_LEADER_NOW_C1);
						msg.addString(_commandChannel.getChannelLeader().getName());
						_commandChannel.broadcastToChannelMembers(msg);
					}
					if (player.isInPartyMatchRoom())
					{
						PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
						room.changeLeader(player);
					}
				}
			}
			else
			{
				player.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER));
			}
		}
	}

	/**
	 * finds a player in the party by name
	 *
	 * @param name
	 * @return
	 */
	private L2PcInstance getPlayerByName(String name)
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member.getName().equalsIgnoreCase(name))
			{
				return member;
			}
		}
		return null;
	}

	/**
	 * distribute item(s) to party members
	 *
	 * @param player
	 * @param item
	 */
	public void distributeItem(L2PcInstance player, L2ItemInstance item)
	{
		/*switch (item.getItemId())
        {
			case 57: // Adena
			case 4355: // Blue Eva
			case 4356: // Gold Einhasad
			case 4357: // Silver Shilen
			case 4358: // Bloody Pa'agrio
			case 5569: // Water Rune
			case 5570: // Water Mantra
			case 5571: // Wind Rune
			case 5572: // Wind Mantra
			case 5573: // Fire Rune
			case 5574: // Fire Mantra
			case 50007: // Raid Soul
			case 50008: // Raid Feather
			case 50560: // Genesis Leaf
				distributeCurrency(player, item.getItemId(), item.getCount(), player);
				ItemTable.getInstance().destroyItem("Party", item, player, null);
				return;
		}*/
		if (item.getCount() > 1)
		{
			distributeCurrency(player, item.getItemId(), item.getCount(), player);
			ItemTable.getInstance().destroyItem("Party", item, player, null);
			return;
		}

		L2PcInstance target = getActualLooter(player, item.getItemId(), false, player);
		target.addItem("Party", item, player, true);

		// Send messages to other party members about reward
		if (item.getCount() > 1)
		{
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_OBTAINED_S3_S2);
			msg.addString(target.getName());
			msg.addItemName(item);
			msg.addItemNumber(item.getCount());
			broadcastToPartyMembers(target, msg);
		}
		else
		{
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_OBTAINED_S2);
			msg.addString(target.getName());
			msg.addItemName(item);
			broadcastToPartyMembers(target, msg);
		}
	}

	/**
	 * distribute item(s) to party members
	 *
	 * @param player
	 * @param item
	 */
	public void distributeItem(L2PcInstance player, L2Attackable.RewardItem item, boolean spoil, L2Attackable target)
	{
		if (item == null)
		{
			return;
		}

		/*switch (item.getItemId())
		{
			case 57: // Adena
			case 5569: // Water Rune
			case 5570: // Water Mantra
			case 5571: // Wind Rune
			case 5572: // Wind Mantra
			case 5573: // Fire Rune
			case 5574: // Fire Mantra
			case 50007: // Raid Soul
			case 50008: // Raid Feather
			case 50560: // Genesis Leaf
				distributeCurrency(player, item.getItemId(), item.getCount(), target);
				return;
		}*/
		if (item.getCount() > 1)
		{
			distributeCurrency(player, item.getItemId(), item.getCount(), target);
			return;
		}

		L2PcInstance looter = getActualLooter(player, item.getItemId(), spoil, target);

		boolean canLootToWarehouse = ItemTable.getInstance().getTemplate(item.getItemId()).isStackable();

		switch (item.getItemId())
		{
			case 5572: // Wind Mantra
			case 5570: // Water Mantra
			case 5574: // Fire Mantra
			case 50007: // Raid Soul
			case 50008: // Raid Feather
			case 50009: // Raid Heart
			case 50560: // Genesis Leaf
				canLootToWarehouse = false;
				break;
		}

		if (canLootToWarehouse && looter.getConfigValue("autoLootStackableToWH") &&
				looter.getWarehouse().validateCapacity(5))
		{
			looter.getWarehouse().addItem(spoil ? "Sweep" : "Party", item.getItemId(), item.getCount(), looter, target);

			SystemMessage s = SystemMessage.getSystemMessage(SystemMessageId.LIGHT_BLUE_CHATBOX_S1);

			s.addString(item.getCount() + " " + ItemTable.getInstance().getTemplate(item.getItemId()).getName() + " " +
					(item.getCount() == 1 ? "was" : "were") + " added to your Warehouse.");
			looter.sendPacket(s);
		}
		else
		{
			looter.addItem(spoil ? "Sweep" : "Party", item.getItemId(), item.getCount(), player, true);
		}

		// Send messages to other aprty members about reward
		if (item.getCount() > 1)
		{
			SystemMessage msg = spoil ? SystemMessage.getSystemMessage(SystemMessageId.C1_SWEEPED_UP_S3_S2) :
					SystemMessage.getSystemMessage(SystemMessageId.C1_OBTAINED_S3_S2);
			msg.addString(looter.getName());
			msg.addItemName(item.getItemId());
			msg.addItemNumber(item.getCount());
			broadcastToPartyMembers(looter, msg);
		}
		else
		{
			SystemMessage msg = spoil ? SystemMessage.getSystemMessage(SystemMessageId.C1_SWEEPED_UP_S2) :
					SystemMessage.getSystemMessage(SystemMessageId.C1_OBTAINED_S2);
			msg.addString(looter.getName());
			msg.addItemName(item.getItemId());
			broadcastToPartyMembers(looter, msg);
		}
	}

	/**
	 * distribute adena to party members
	 */
	public void distributeCurrency(L2PcInstance player, int itemId, long amount, L2Character target)
	{
		// Get all the party members
		List<L2PcInstance> membersList = getPartyMembers();

		boolean rewardPlayer = false;
		// Check the number of party members that must be rewarded
		// (The party member must be in range to receive its reward)
		List<L2PcInstance> toReward = new ArrayList<>();
		for (L2PcInstance member : membersList)
		{
			if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
			{
				continue;
			}

			toReward.add(member);
			if (member == player)
			{
				rewardPlayer = true;
			}
		}

		// Avoid null exceptions, if any
		if (toReward.isEmpty())
		{
			return;
		}

		int toRewardCount = toReward.size();
		// Shuffle the list a bit
		for (int i = 0; i < toRewardCount; i++)
		{
			toReward.add(Rnd.get(toRewardCount), toReward.remove(Rnd.get(toRewardCount)));
		}

		// Make the finder the first one
		if (rewardPlayer)
		{
			toReward.remove(player);
			toReward.add(0, player);
		}

		// Now we can actually distribute the adena reward
		// (Total adena splitted by the number of party members that are in range and must be rewarded)
		long remaining = amount % toRewardCount;
		for (L2PcInstance member : toReward)
		{
			long count = amount / toRewardCount;
			if (remaining-- > 0)
			{
				count++;
			}

			if (itemId == 57)
			{
				member.addAdena("Party", count, player, true);
			}
			else
			{
				member.addItem("Party", itemId, count, player, true);
			}
		}
	}

	/**
	 * Distribute Experience and SP rewards to L2PcInstance Party members in the known area of the last attacker.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the L2PcInstance owner of the L2SummonInstance (if necessary) </li>
	 * <li>Calculate the Experience and SP reward distribution rate </li>
	 * <li>Add Experience and SP to the L2PcInstance </li><BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T GIVE rewards to L2PetInstance</B></FONT><BR><BR>
	 * Exception are L2PetInstances that leech from the owner's XP; they get the exp indirectly, via the owner's exp gain<BR>
	 *
	 * @param xpReward        The Experience reward to distribute
	 * @param spReward        The SP reward to distribute
	 * @param rewardedMembers The list of L2PcInstance to reward
	 */
	public void distributeXpAndSp(long xpReward, int spReward, List<L2Playable> rewardedMembers, int topLvl, int partyDmg, L2Attackable target)
	{
		List<L2Playable> validMembers = getValidMembers(rewardedMembers, topLvl);

		float penalty;
		double sqLevel;
		double preCalculation;

		xpReward *= getExpBonus(validMembers.size());
		spReward *= getSpBonus(validMembers.size());

		double sqLevelSum = 0;
		for (L2Playable character : validMembers)
		{
			sqLevelSum += character.getLevel() * character.getLevel();
		}

		final float vitalityPoints = target.getVitalityPoints(partyDmg) * Config.RATE_PARTY_XP / validMembers.size();
		final boolean useVitalityRate = target.useVitalityRate();

		// Go through the L2PcInstances and L2PetInstances (not L2SummonInstances) that must be rewarded
		synchronized (rewardedMembers)
		{
			for (L2Playable member : rewardedMembers)
			{
				if (member.isDead())
				{
					continue;
				}

				penalty = 0;

				// Pets that leech xp from the owner (like babypets) do not get rewarded directly
				if (member instanceof L2PetInstance)
				{
					if (((L2PetInstance) member).getPetLevelData().getOwnerExpTaken() > 0)
					{
						continue;
					}
					else
					// TODO: This is a temporary fix while correct pet xp in party is figured out
					{
						penalty = 0.85F;
					}
				}

				// Level gap penalty
				if (member instanceof L2PcInstance)
				{
					// The L2SummonInstance penalty
					for (L2SummonInstance summon : ((L2PcInstance) member).getSummons())
					{
						penalty = summon.getExpPenalty();
					}

					if (member.getLevel() + 15 <= topLvl)
					{
						penalty = 1F;
					}
					else if (member.getLevel() + 9 <= topLvl)
					{
						penalty = 0.7F;
					}
				}

				// Calculate and add the EXP and SP reward to the member
				if (validMembers.contains(member) && penalty < 1)
				{
					sqLevel = member.getLevel() * member.getLevel();
					preCalculation = sqLevel / sqLevelSum * (1 - penalty);

					// Add the XP/SP points to the requested party member
					if (!member.isDead())
					{
						long addexp =
								Math.round(member.calcStat(Stats.EXP_RATE, xpReward * preCalculation, null, null));
						int addsp = (int) member.calcStat(Stats.SP_RATE, spReward * preCalculation, null, null);
						if (member instanceof L2PcInstance)
						{
							L2PcInstance pcMember = (L2PcInstance) member;
							if (pcMember.getSkillLevelHash(467) > 0)
							{
								L2Skill skill = SkillTable.getInstance().getInfo(467, pcMember.getSkillLevelHash(467));

								if (skill.getExpNeeded() <= addexp)
								{
									pcMember.absorbSoul(skill, target);
								}
							}
							if (pcMember.getLevel() >= target.getLevel() &&
									pcMember.getLevel() - target.getLevel() < 11 ||
									pcMember.getLevel() < target.getLevel() &&
											target.getLevel() - pcMember.getLevel() < 11)
							{
								if (pcMember.getReputation() < 0)
								{
									pcMember.updateReputationForHunting(addexp, addsp);
								}
								else
								{
									pcMember.addExpAndSp(addexp, addsp, useVitalityRate);
									if (addexp > 0)
									{
										pcMember.updateVitalityPoints(vitalityPoints, true, false);
									}
								}
							}
						}
						else
						{
							member.addExpAndSp(addexp, addsp);
						}
					}
				}
				else
				{
					member.addExpAndSp(0, 0);
				}
			}
		}
	}

	/**
	 * refresh party level
	 */
	public void recalculatePartyLevel()
	{
		int newLevel = 0;
		for (L2PcInstance member : getPartyMembers())
		{
			if (member == null)
			{
				getPartyMembers().remove(member);
				continue;
			}

			if (member.getLevel() > newLevel)
			{
				newLevel = member.getLevel();
			}
		}
		_partyLvl = newLevel;
	}

	private List<L2Playable> getValidMembers(List<L2Playable> members, int topLvl)
	{
		List<L2Playable> validMembers = new ArrayList<>();

		//		Fixed LevelDiff cutoff point
		if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level"))
		{
			for (L2Playable member : members)
			{
				if (topLvl - member.getLevel() <= Config.PARTY_XP_CUTOFF_LEVEL)
				{
					validMembers.add(member);
				}
			}
		}
		//		Fixed MinPercentage cutoff point
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("percentage"))
		{
			int sqLevelSum = 0;
			for (L2Playable member : members)
			{
				sqLevelSum += member.getLevel() * member.getLevel();
			}

			for (L2Playable member : members)
			{
				int sqLevel = member.getLevel() * member.getLevel();
				if (sqLevel * 100 >= sqLevelSum * Config.PARTY_XP_CUTOFF_PERCENT)
				{
					validMembers.add(member);
				}
			}
		}
		//		Automatic cutoff method
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("auto"))
		{
			int sqLevelSum = 0;
			for (L2Playable member : members)
			{
				sqLevelSum += member.getLevel() * member.getLevel();
			}

			int i = members.size() - 1;
			if (i < 1)
			{
				return members;
			}
			if (i >= BONUS_EXP_SP.length)
			{
				i = BONUS_EXP_SP.length - 1;
			}

			for (L2Playable member : members)
			{
				int sqLevel = member.getLevel() * member.getLevel();
				if (sqLevel >= sqLevelSum * (1 - 1 / (1 + BONUS_EXP_SP[i] - BONUS_EXP_SP[i - 1])))
				{
					validMembers.add(member);
				}
			}
		}
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("none"))
		{
			validMembers.addAll(members);
		}
		return validMembers;
	}

	private double getBaseExpSpBonus(int membersCount)
	{
		int i = membersCount - 1;
		if (i < 1)
		{
			return 1;
		}
		if (i >= BONUS_EXP_SP.length)
		{
			i = BONUS_EXP_SP.length - 1;
		}

		return BONUS_EXP_SP[i];
	}

	private double getExpBonus(int membersCount)
	{
		if (membersCount < 2)
		{
			//not is a valid party
			return getBaseExpSpBonus(membersCount);
		}
		else
		{
			return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_XP;
		}
	}

	private double getSpBonus(int membersCount)
	{
		if (membersCount < 2)
		{
			//not is a valid party
			return getBaseExpSpBonus(membersCount);
		}
		else
		{
			return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_SP;
		}
	}

	public int getLevel()
	{
		return _partyLvl;
	}

	public int getLootDistribution()
	{
		return _itemDistribution;
	}

	public boolean isInCommandChannel()
	{
		return _commandChannel != null;
	}

	public L2CommandChannel getCommandChannel()
	{
		return _commandChannel;
	}

	public void setCommandChannel(L2CommandChannel channel)
	{
		_commandChannel = channel;
	}

	public L2PcInstance getLeader()
	{
		try
		{
			return _members.get(0);
		}
		catch (NoSuchElementException e)
		{
			return null;
		}
	}

	public void requestLootChange(byte type)
	{
		if (_requestChangeLoot != -1)
		{
			if (System.currentTimeMillis() > _requestChangeLootTimer)
			{
				finishLootRequest(false); // timeout 45sec, guess
			}
			else
			{
				return;
			}
		}
		_requestChangeLoot = type;
		int additionalTime = L2PcInstance.REQUEST_TIMEOUT * 3000;
		_requestChangeLootTimer = System.currentTimeMillis() + additionalTime;
		_changeLootAnswers = new ArrayList<>();
		_checkTask = ThreadPoolManager.getInstance()
				.scheduleGeneralAtFixedRate(new ChangeLootCheck(), additionalTime + 1000, 5000);
		broadcastToPartyMembers(getLeader(), new ExAskModifyPartyLooting(getLeader().getName(), type));
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REQUESTING_APPROVAL_CHANGE_PARTY_LOOT_S1);
		sm.addSystemString(LOOT_SYSSTRINGS[type]);
		getLeader().sendPacket(sm);
	}

	public synchronized void answerLootChangeRequest(L2PcInstance member, boolean answer)
	{
		if (_requestChangeLoot == -1)
		{
			return;
		}
		if (_changeLootAnswers.contains(member.getObjectId()))
		{
			return;
		}
		if (!answer)
		{
			finishLootRequest(false);
			return;
		}
		_changeLootAnswers.add(member.getObjectId());
		if (_changeLootAnswers.size() >= getMemberCount() - 1)
		{
			finishLootRequest(true);
		}
	}

	private synchronized void finishLootRequest(boolean success)
	{
		if (_requestChangeLoot == -1)
		{
			return;
		}

		if (_checkTask != null)
		{
			_checkTask.cancel(false);
			_checkTask = null;
		}
		if (success)
		{
			broadcastToPartyMembers(new ExSetPartyLooting(1, _requestChangeLoot));
			_itemDistribution = _requestChangeLoot;
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PARTY_LOOT_CHANGED_S1);
			sm.addSystemString(LOOT_SYSSTRINGS[_requestChangeLoot]);
			broadcastToPartyMembers(sm);
		}
		else
		{
			broadcastToPartyMembers(new ExSetPartyLooting(0, (byte) 0));
			broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.PARTY_LOOT_CHANGE_CANCELLED));
		}
		_requestChangeLoot = -1;
		_requestChangeLootTimer = 0;
	}

	private L2Character _target;

	public L2Character getTarget()
	{
		return _target;
	}

	public void think()
	{
		L2PcInstance leader = getLeader();

		List<L2Character> enemies = new ArrayList<>();
		List<L2PcInstance> allies = new ArrayList<>();

		allies.add(leader);

		for (L2Character cha : leader.getKnownList().getKnownCharacters())
		{
			if (!cha.isDead())
			{
				if (leader.isEnemy(cha))
				{
					enemies.add(cha);
				}
				else if (leader.isAlly(cha))
				{
					allies.add((L2PcInstance) cha);
				}
			}
		}

		boolean playerFound = false;
		for (L2Character enemy : enemies)
		{
			if (!(enemy instanceof L2ApInstance))
			{
				playerFound = true;
				break;
			}
		}

		if (playerFound)
		{
			List<L2Character> toIterate = new ArrayList<>(enemies);
			for (L2Character enemy : toIterate)
			{
				if (enemy instanceof L2ApInstance)
				{
					enemies.remove(enemy);
				}
			}
		}

		if (_target != null && _target.isVisible() && !_target.isDead() &&
				leader.getDistanceSq(_target) < 2000 * 2000 && _target.isAutoAttackable(leader) && allies.size() > 0 &&
				(!playerFound && _target instanceof L2ApInstance || !(_target instanceof L2ApInstance)))
		{
			//_target = null;
			return;
		}
		else
		{
			_target = null;
		}

		// Balance check
		/*if (allies.size() * 2 < enemies.size())
		{
			_target = null;
			return;
		}*/

		//System.out.println(enemies);

		for (L2PcInstance healer : allies)
		{
			if (healer.getClassId() == 146)
			{
				for (L2Character enemy : enemies)
				{
					if (enemy.getTarget() == healer && (enemy.isAttackingNow() ||
							enemy.getLastSkillCast() != null && enemy.getLastSkillCast().isOffensive()) &&
							leader.getDistanceSq(enemy) < 2000 * 2000)
					{
						_target = enemy;
						return;
					}
				}
			}
		}

		if (enemies.size() == 0)
		{
			// If there's some member with PvP flag yet, return
			for (L2PcInstance member : _members)
			{
				if (member.getPvpFlag() > 0)
				{
					_target = null;
					return;
				}
			}
			
			/*L2PcInstance mostPvP = L2World.getInstance().getMostPvP(true, true);

			if (mostPvP != null)
			{
				int count = 0;
				Set<Integer> clans = new HashSet<Integer>();
				for (L2PcInstance flagged : mostPvP.getKnownList().getKnownPlayers().values())
				{
					if (flagged.getPvpFlag() > 0 && flagged.isInParty())
					{
						count++;
						clans.add(flagged.getClanId());
					}
				}

				if (count > 5 || clans.size() < 2)
				{
					for (L2PcInstance member : _members)
						member.teleToLocation(mostPvP.getX() + Rnd.get(200), mostPvP.getY() + Rnd.get(200), mostPvP.getZ());
				}
			}*/

			for (L2PcInstance member : _members)
			{
				member.teleToLocation(-24501, 187976, -3975, true);
				member.setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);
				member.startPvPFlag();
			}

			return;
		}

		L2PcInstance worstEnemy = null;
		L2Character worstMob = null;
		double closest = 10000000;
		for (L2Character enemy : enemies)
		{
			//if (!(enemy instanceof L2PcInstance))
			//	continue;

			if (!(enemy instanceof L2PcInstance))
			{
				if (_target == null && enemy.isInCombat() && enemy.getDistanceSq(leader) < closest)
				{
					closest = enemy.getDistanceSq(leader);
					worstMob = enemy;
				}
				continue;
			}

			L2PcInstance pcEnemy = (L2PcInstance) enemy;

			if (worstEnemy == null)
			{
				worstEnemy = pcEnemy;
				continue;
			}

			if (pcEnemy.getClassId() == 146 && worstEnemy.getClassId() != 146)
			{
				worstEnemy = pcEnemy;
				continue;
			}

			if (pcEnemy.getClassId() == 143 && worstEnemy.getClassId() != 143)
			{
				worstEnemy = pcEnemy;
				continue;
			}

			if (pcEnemy.getClassId() == 144 && worstEnemy.getClassId() != 144)
			{
				worstEnemy = pcEnemy;
				continue;
			}

			if (pcEnemy.getClassId() == 142 && worstEnemy.getClassId() != 142)
			{
				worstEnemy = pcEnemy;
			}
		}

		//Log.info(worstEnemy);

		if (worstEnemy == null && _target == null)
		{
			_target = worstMob;
			return;
		}

		_target = worstEnemy;
	}

	private class ChangeLootCheck implements Runnable
	{
		@Override
		public void run()
		{
			if (System.currentTimeMillis() > _requestChangeLootTimer)
			{
				finishLootRequest(false);
			}
		}
	}

	private class PositionBroadcast implements Runnable
	{
		@Override
		public void run()
		{
			if (_positionPacket == null)
			{
				_positionPacket = new PartyMemberPosition(L2Party.this);
			}
			else
			{
				_positionPacket.reuse(L2Party.this);
			}
			broadcastToPartyMembers(_positionPacket);
		}
	}

	private TIntIntHashMap _taggedChars = new TIntIntHashMap();

	public int getTaggedChar(int tagId)
	{
		for (int objId : _taggedChars.keys())
		{
			if (_taggedChars.get(objId) == tagId)
			{
				return objId;
			}
		}
		return 0;
	}

	public int getTag(int charObjId)
	{
		if (!_taggedChars.containsKey(charObjId))
		{
			return 0;
		}
		return _taggedChars.get(charObjId);
	}

	public int tagCharacter(int charObjId, int tagId)
	{
		if (_taggedChars.containsKey(charObjId) && _taggedChars.get(charObjId) == tagId)
		{
			_taggedChars.remove(charObjId);
			return 0;
		}

		for (int charId : _taggedChars.keys())
		{
			if (_taggedChars.get(charId) == tagId)
			{
				_taggedChars.remove(charId);
				broadcastToPartyMembers(new ExTacticalSign(charId, 0));
			}
		}

		_taggedChars.put(charObjId, tagId);
		return tagId;
	}

	public final int indexOfPartyMember(final L2Object player)
	{
		if (!(player instanceof L2PcInstance))
		{
			return -1;
		}

		for (int i = _members.size(); i-- > 0; )
		{
			final L2PcInstance member = _members.get(i);
			if (member != null && member == player)
			{
				return i;
			}
		}

		return -1;
	}

	public final boolean isInParty(final L2Object player)
	{
		return indexOfPartyMember(player) != -1;
	}
}
