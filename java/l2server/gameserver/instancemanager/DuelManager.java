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

package l2server.gameserver.instancemanager;

import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Duel;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.log.Log;

import java.util.ArrayList;

public class DuelManager
{

	public static DuelManager getInstance()
	{
		return SingletonHolder._instance;
	}

	// =========================================================
	// Data Field
	private ArrayList<Duel> _duels;
	private int _currentDuelId = 0x90;

	// =========================================================
	// Constructor
	private DuelManager()
	{
		Log.info("Initializing DuelManager");
		_duels = new ArrayList<>();
	}

	// =========================================================
	// Method - Private

	private int getNextDuelId()
	{
		// In case someone wants to run the server forever :)
		if (++_currentDuelId >= 2147483640)
		{
			_currentDuelId = 1;
		}
		return _currentDuelId;
	}

	// =========================================================
	// Method - Public

	public Duel getDuel(int duelId)
	{
		for (Duel duel : _duels)
		{
			if (duel != null && duel.getId() == duelId)
			{
				return duel;
			}
		}
		return null;
	}

	public void addDuel(L2PcInstance playerA, L2PcInstance playerB, int partyDuel)
	{
		if (playerA == null || playerB == null)
		{
			return;
		}

		// return if a player has PvPFlag
		String engagedInPvP = "The duel was canceled because a duelist engaged in PvP combat.";
		if (partyDuel == 1)
		{
			boolean playerInPvP = false;
			for (L2PcInstance temp : playerA.getParty().getPartyMembers())
			{
				if (temp.getPvpFlag() != 0)
				{
					playerInPvP = true;
					break;
				}
			}
			if (!playerInPvP)
			{
				for (L2PcInstance temp : playerB.getParty().getPartyMembers())
				{
					if (temp.getPvpFlag() != 0)
					{
						playerInPvP = true;
						break;
					}
				}
			}
			// A player has PvP flag
			if (playerInPvP)
			{
				for (L2PcInstance temp : playerA.getParty().getPartyMembers())
				{
					temp.sendMessage(engagedInPvP);
				}
				for (L2PcInstance temp : playerB.getParty().getPartyMembers())
				{
					temp.sendMessage(engagedInPvP);
				}
				return;
			}
		}
		else
		{
			if (playerA.getPvpFlag() != 0 || playerB.getPvpFlag() != 0)
			{
				playerA.sendMessage(engagedInPvP);
				playerB.sendMessage(engagedInPvP);
				return;
			}
		}

		Duel duel = new Duel(playerA, playerB, partyDuel, getNextDuelId());
		_duels.add(duel);
	}

	public void removeDuel(Duel duel)
	{
		_duels.remove(duel);
	}

	public void doSurrender(L2PcInstance player)
	{
		if (player == null || !player.isInDuel())
		{
			return;
		}
		Duel duel = getDuel(player.getDuelId());
		duel.doSurrender(player);
	}

	/**
	 * Updates player states.
	 *
	 * @param player - the dieing player
	 */
	public void onPlayerDefeat(L2PcInstance player)
	{
		if (player == null || !player.isInDuel())
		{
			return;
		}
		Duel duel = getDuel(player.getDuelId());
		if (duel != null)
		{
			duel.onPlayerDefeat(player);
		}
	}

	/**
	 * Registers a debuff which will be removed if the duel ends
	 *
	 * @param player
	 */
	public void onBuff(L2PcInstance player, L2Abnormal buff)
	{
		if (player == null || !player.isInDuel() || buff == null)
		{
			return;
		}
		Duel duel = getDuel(player.getDuelId());
		if (duel != null)
		{
			duel.onBuff(player, buff);
		}
	}

	/**
	 * Removes player from duel.
	 *
	 * @param player - the removed player
	 */
	public void onRemoveFromParty(L2PcInstance player)
	{
		if (player == null || !player.isInDuel())
		{
			return;
		}
		Duel duel = getDuel(player.getDuelId());
		if (duel != null)
		{
			duel.onRemoveFromParty(player);
		}
	}

	/**
	 * Broadcasts a packet to the team opposing the given player.
	 *
	 * @param player
	 * @param packet
	 */
	public void broadcastToOppositTeam(L2PcInstance player, L2GameServerPacket packet)
	{
		if (player == null || !player.isInDuel())
		{
			return;
		}
		Duel duel = getDuel(player.getDuelId());
		if (duel == null)
		{
			return;
		}
		if (duel.getPlayerA() == null || duel.getPlayerB() == null)
		{
			return;
		}

		if (duel.getPlayerA() == player)
		{
			duel.broadcastToTeam2(packet);
		}
		else if (duel.getPlayerB() == player)
		{
			duel.broadcastToTeam1(packet);
		}
		else if (duel.isPartyDuel())
		{
			if (duel.getPlayerA().getParty() != null && duel.getPlayerA().getParty().getPartyMembers().contains(player))
			{
				duel.broadcastToTeam2(packet);
			}
			else if (duel.getPlayerB().getParty() != null &&
					duel.getPlayerB().getParty().getPartyMembers().contains(player))
			{
				duel.broadcastToTeam1(packet);
			}
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final DuelManager _instance = new DuelManager();
	}
}
