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

import lombok.Getter;
import l2server.Config;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author chris_00
 */
public class L2CommandChannel
{
	@Getter private final List<L2Party> partys;
	private L2PcInstance commandLeader = null;
	private int channelLvl;

	/**
	 * Creates a New Command Channel and Add the Leaders party to the CC
	 */
	public L2CommandChannel(L2PcInstance leader)
	{
		commandLeader = leader;
		partys = new CopyOnWriteArrayList<>();
		partys.add(leader.getParty());
		channelLvl = leader.getParty().getLevel();
		leader.getParty().setCommandChannel(this);
		leader.getParty()
				.broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_FORMED));
		leader.getParty().broadcastToPartyMembers(new ExOpenMPCC());
	}

	/**
	 * Adds a Party to the Command Channel
	 */
	public void addParty(L2Party party)
	{
		if (party == null)
		{
			return;
		}
		// Update the CCinfo for existing players
		broadcastToChannelMembers(new ExMPCCPartyInfoUpdate(party, 1));

		partys.add(party);
		if (party.getLevel() > channelLvl)
		{
			channelLvl = party.getLevel();
		}
		party.setCommandChannel(this);
		party.broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.JOINED_COMMAND_CHANNEL));
		party.broadcastToPartyMembers(new ExOpenMPCC());
	}

	/**
	 * Removes a Party from the Command Channel
	 */
	public void removeParty(L2Party party)
	{
		if (party == null)
		{
			return;
		}

		partys.remove(party);
		channelLvl = 0;
		for (L2Party pty : partys)
		{
			if (pty.getLevel() > channelLvl)
			{
				channelLvl = pty.getLevel();
			}
		}
		party.setCommandChannel(null);
		party.broadcastToPartyMembers(new ExCloseMPCC());
		if (partys.size() < 2)
		{
			broadcastToChannelMembers(SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_DISBANDED));
			disbandChannel();
		}
		else
		{
			// Update the CCinfo for existing players
			broadcastToChannelMembers(new ExMPCCPartyInfoUpdate(party, 0));
		}
	}

	/**
	 * disbands the whole Command Channel
	 */
	public void disbandChannel()
	{
		if (partys != null)
		{
			for (L2Party party : partys)
			{
				if (party != null)
				{
					removeParty(party);
				}
			}
		}
		partys.clear();
	}

	/**
	 * @return overall membercount of the Command Channel
	 */
	public int getMemberCount()
	{
		int count = 0;
		for (L2Party party : partys)
		{
			if (party != null)
			{
				count += party.getMemberCount();
			}
		}
		return count;
	}

	/**
	 * Broadcast packet to every channelmember
	 */
	public void broadcastToChannelMembers(L2GameServerPacket gsp)
	{
		if (partys != null && !partys.isEmpty())
		{
			for (L2Party party : partys)
			{
				if (party != null)
				{
					party.broadcastToPartyMembers(gsp);
				}
			}
		}
	}

	public void broadcastCSToChannelMembers(CreatureSay gsp, L2PcInstance broadcaster)
	{
		if (partys != null && !partys.isEmpty())
		{
			for (L2Party party : partys)
			{
				if (party != null)
				{
					party.broadcastCSToPartyMembers(gsp, broadcaster);
				}
			}
		}
	}

	/**
	 * @return list of all Members in Command Channel
	 */
	public List<L2PcInstance> getMembers()
	{
		List<L2PcInstance> members = new ArrayList<>();
		for (L2Party party : getPartys())
		{
			members.addAll(party.getPartyMembers());
		}
		return members;
	}

	/**
	 * @return Level of CC
	 */
	public int getLevel()
	{
		return channelLvl;
	}

	/**
	 */
	public void setChannelLeader(L2PcInstance leader)
	{
		commandLeader = leader;
	}

	/**
	 * @return the leader of the Command Channel
	 */
	public L2PcInstance getChannelLeader()
	{
		return commandLeader;
	}

	/**
	 * @param obj
	 * @return true if proper condition for RaidWar
	 */
	public boolean meetRaidWarCondition(L2Object obj)
	{
		if (!(obj instanceof L2Character && ((L2Character) obj).isRaid()))
		{
			return false;
		}
		return getMemberCount() >= Config.LOOT_RAIDS_PRIVILEGE_CC_SIZE;
	}

	public final boolean isInChannel(final L2Party party)
	{
		return partys.contains(party);
	}
}
