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

package l2server.gameserver.model.entity;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.instancemanager.DuelManager;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.log.Log;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;

public class Duel
{

	public static final int DUELSTATE_NODUEL = 0;
	public static final int DUELSTATE_DUELLING = 1;
	public static final int DUELSTATE_DEAD = 2;
	public static final int DUELSTATE_WINNER = 3;
	public static final int DUELSTATE_INTERRUPTED = 4;

	// =========================================================
	// Data Field
	private int duelId;
	@Getter private L2PcInstance playerA;
	@Getter private L2PcInstance playerB;
	private boolean partyDuel;
	private Calendar duelEndTime;
	private int surrenderRequest = 0;
	private int countdown = 4;
	@Getter private boolean finished = false;

	private ArrayList<PlayerCondition> playerConditions;

	public enum DuelResultEnum
	{
		Continue, Team1Win, Team2Win, Team1Surrender, Team2Surrender, Canceled, Timeout
	}

	// =========================================================
	// Constructor
	public Duel(L2PcInstance playerA, L2PcInstance playerB, int partyDuel, int duelId)
	{
		this.duelId = duelId;
		this.playerA = playerA;
		this.playerB = playerB;
		this.partyDuel = partyDuel == 1;

		duelEndTime = Calendar.getInstance();
		if (this.partyDuel)
		{
			duelEndTime.add(Calendar.SECOND, 300);
		}
		else
		{
			duelEndTime.add(Calendar.SECOND, 120);
		}

		playerConditions = new ArrayList<>();

		setFinished(false);

		if (this.partyDuel)
		{
			// increase countdown so that start task can teleport players
			countdown++;
			// inform players that they will be portet shortly
			SystemMessage sm = SystemMessage.getSystemMessage(
					SystemMessageId.IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE);
			broadcastToTeam1(sm);
			broadcastToTeam2(sm);
		}
		// Schedule duel start
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartDuelTask(this), 3000);
	}

	// ===============================================================
	// Nested Class

	public static class PlayerCondition
	{
		@Getter private L2PcInstance player;
		private double hp;
		private double mp;
		private double cp;
		private boolean paDuel;
		private int x, y, z;
		private ArrayList<L2Abnormal> debuffs;

		public PlayerCondition(L2PcInstance player, boolean partyDuel)
		{
			if (player == null)
			{
				return;
			}
			this.player = player;
			hp = this.player.getCurrentHp();
			mp = this.player.getCurrentMp();
			cp = this.player.getCurrentCp();
			paDuel = partyDuel;

			if (paDuel)
			{
				x = this.player.getX();
				y = this.player.getY();
				z = this.player.getZ();
			}
		}

		public void restoreCondition()
		{
			if (player == null)
			{
				return;
			}
			player.setCurrentHp(hp);
			player.setCurrentMp(mp);
			player.setCurrentCp(cp);

			if (paDuel)
			{
				teleportBack();
			}
			if (debuffs != null) // Debuff removal
			{
				for (L2Abnormal temp : debuffs)
				{
					if (temp != null)
					{
						temp.exit();
					}
				}
			}
		}

		public void registerDebuff(L2Abnormal debuff)
		{
			if (debuffs == null)
			{
				debuffs = new ArrayList<>();
			}

			debuffs.add(debuff);
		}

		public void teleportBack()
		{
			if (paDuel)
			{
				player.teleToLocation(x, y, z);
			}
		}

	}

	// ===============================================================
	// Schedule task
	public class ScheduleDuelTask implements Runnable
	{
		private Duel duel;

		public ScheduleDuelTask(Duel duel)
		{
			this.duel = duel;
		}

		@Override
		public void run()
		{
			try
			{
				DuelResultEnum status = duel.checkEndDuelCondition();

				if (status == DuelResultEnum.Canceled)
				{
					// do not schedule duel end if it was interrupted
					setFinished(true);
					duel.endDuel(status);
				}
				else if (status != DuelResultEnum.Continue)
				{
					setFinished(true);
					playKneelAnimation();
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndDuelTask(duel, status), 5000);
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
				}
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "", e);
			}
		}
	}

	public static class ScheduleStartDuelTask implements Runnable
	{
		private Duel duel;

		public ScheduleStartDuelTask(Duel duel)
		{
			this.duel = duel;
		}

		@Override
		public void run()
		{
			try
			{
				// start/continue countdown
				int count = duel.countdown();

				if (count == 4)
				{
					// players need to be teleportet first
					//TODO: stadia manager needs a function to return an unused stadium for duels
					// currently only teleports to the same stadium
					duel.teleportPlayers(-83760, -238825, -3331);

					// give players 20 seconds to complete teleport and get ready (its ought to be 30 on offical..)
					ThreadPoolManager.getInstance().scheduleGeneral(this, 20000);
				}
				else if (count > 0) // duel not started yet - continue countdown
				{
					ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
				}
				else
				{
					duel.startDuel();
				}
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "", e);
			}
		}
	}

	public static class ScheduleEndDuelTask implements Runnable
	{
		private Duel duel;
		private DuelResultEnum result;

		public ScheduleEndDuelTask(Duel duel, DuelResultEnum result)
		{
			this.duel = duel;
			this.result = result;
		}

		@Override
		public void run()
		{
			try
			{
				duel.endDuel(result);
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "", e);
			}
		}
	}

	// ========================================================
	// Method - Private

	/**
	 * Stops all players from attacking.
	 * Used for duel timeout / interrupt.
	 */
	private void stopFighting()
	{
		ActionFailed af = ActionFailed.STATIC_PACKET;
		if (partyDuel)
		{
			for (L2PcInstance temp : playerA.getParty().getPartyMembers())
			{
				temp.abortCast();
				temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				temp.setTarget(null);
				temp.sendPacket(af);
			}
			for (L2PcInstance temp : playerB.getParty().getPartyMembers())
			{
				temp.abortCast();
				temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				temp.setTarget(null);
				temp.sendPacket(af);
			}
		}
		else
		{
			playerA.abortCast();
			playerB.abortCast();
			playerA.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			playerA.setTarget(null);
			playerB.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			playerB.setTarget(null);
			playerA.sendPacket(af);
			playerB.sendPacket(af);
		}
	}

	// ========================================================
	// Method - Public

	/**
	 * Check if a player engaged in pvp combat (only for 1on1 duels)
	 *
	 * @return returns true if a duelist is engaged in Pvp combat
	 */
	public boolean isDuelistInPvp(boolean sendMessage)
	{
		if (partyDuel)
		{
			// Party duels take place in arenas - should be no other players there
			return false;
		}
		else if (playerA.getPvpFlag() != 0 || playerB.getPvpFlag() != 0)
		{
			if (sendMessage)
			{
				String engagedInPvP = "The duel was canceled because a duelist engaged in PvP combat.";
				playerA.sendMessage(engagedInPvP);
				playerB.sendMessage(engagedInPvP);
			}
			return true;
		}
		return false;
	}

	/**
	 * Starts the duel
	 */
	public void startDuel()
	{
		// Save player Conditions
		savePlayerConditions();

		if (playerA == null || playerB == null || playerA.isInDuel() || playerB.isInDuel())
		{
			// clean up
			playerConditions.clear();
			playerConditions = null;
			DuelManager.getInstance().removeDuel(this);
			return;
		}

		if (partyDuel)
		{
			// set isInDuel() state
			// cancel all active trades, just in case? xD
			for (L2PcInstance temp : playerA.getParty().getPartyMembers())
			{
				temp.cancelActiveTrade();
				temp.setIsInDuel(duelId);
				temp.setTeam(1);
				temp.broadcastUserInfo();
				broadcastToTeam2(new ExDuelUpdateUserInfo(temp));
			}
			for (L2PcInstance temp : playerB.getParty().getPartyMembers())
			{
				temp.cancelActiveTrade();
				temp.setIsInDuel(duelId);
				temp.setTeam(2);
				temp.broadcastUserInfo();
				broadcastToTeam1(new ExDuelUpdateUserInfo(temp));
			}

			// Send duel Start packets
			ExDuelReady ready = new ExDuelReady(1);
			ExDuelStart start = new ExDuelStart(1);

			broadcastToTeam1(ready);
			broadcastToTeam2(ready);
			broadcastToTeam1(start);
			broadcastToTeam2(start);
		}
		else
		{
			// set isInDuel() state
			playerA.setIsInDuel(duelId);
			playerA.setTeam(1);
			playerB.setIsInDuel(duelId);
			playerB.setTeam(2);

			// Send duel Start packets
			ExDuelReady ready = new ExDuelReady(0);
			ExDuelStart start = new ExDuelStart(0);

			broadcastToTeam1(ready);
			broadcastToTeam2(ready);
			broadcastToTeam1(start);
			broadcastToTeam2(start);

			broadcastToTeam1(new ExDuelUpdateUserInfo(playerB));
			broadcastToTeam2(new ExDuelUpdateUserInfo(playerA));

			playerA.broadcastUserInfo();
			playerB.broadcastUserInfo();
		}

		// play sound
		PlaySound ps = new PlaySound(1, "B04_S01", 0, 0, 0, 0, 0);
		broadcastToTeam1(ps);
		broadcastToTeam2(ps);

		// start duelling task
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleDuelTask(this), 1000);
	}

	/**
	 * Save the current player condition: hp, mp, cp, location
	 */
	public void savePlayerConditions()
	{
		if (partyDuel)
		{
			for (L2PcInstance temp : playerA.getParty().getPartyMembers())
			{
				playerConditions.add(new PlayerCondition(temp, partyDuel));
			}
			for (L2PcInstance temp : playerB.getParty().getPartyMembers())
			{
				playerConditions.add(new PlayerCondition(temp, partyDuel));
			}
		}
		else
		{
			playerConditions.add(new PlayerCondition(playerA, partyDuel));
			playerConditions.add(new PlayerCondition(playerB, partyDuel));
		}
	}

	/**
	 * Restore player conditions
	 */
	public void restorePlayerConditions(boolean abnormalDuelEnd)
	{
		// update isInDuel() state for all players
		if (partyDuel)
		{
			for (L2PcInstance temp : playerA.getParty().getPartyMembers())
			{
				temp.setIsInDuel(0);
				temp.setTeam(0);
				temp.broadcastUserInfo();
			}
			for (L2PcInstance temp : playerB.getParty().getPartyMembers())
			{
				temp.setIsInDuel(0);
				temp.setTeam(0);
				temp.broadcastUserInfo();
			}
		}
		else
		{
			playerA.setIsInDuel(0);
			playerA.setTeam(0);
			playerA.broadcastUserInfo();
			playerB.setIsInDuel(0);
			playerB.setTeam(0);
			playerB.broadcastUserInfo();
		}

		// if it is an abnormal DuelEnd do not restore hp, mp, cp
		if (abnormalDuelEnd)
		{
			return;
		}

		// restore player conditions
		for (PlayerCondition pc : playerConditions)
		{
			pc.restoreCondition();
		}
	}

	/**
	 * Get the duel id
	 *
	 * @return id
	 */
	public int getId()
	{
		return duelId;
	}

	/**
	 * Returns the remaining time
	 *
	 * @return remaining time
	 */
	public int getRemainingTime()
	{
		return (int) (duelEndTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
	}

	/**
	 * Returns whether this is a party duel or not
	 *
	 * @return is party duel
	 */
	public boolean isPartyDuel()
	{
		return partyDuel;
	}

	public void setFinished(boolean mode)
	{
		finished = mode;
	}


	/**
	 * teleport all players to the given coordinates
	 *
	 * @param x
	 * @param y
	 * @param z
	 */
	public void teleportPlayers(int x, int y, int z)
	{
		//TODO: adjust the values if needed... or implement something better (especially using more then 1 arena)
		if (!partyDuel)
		{
			return;
		}
		int offset = 0;

		for (L2PcInstance temp : playerA.getParty().getPartyMembers())
		{
			temp.teleToLocation(x + offset - 180, y - 150, z);
			offset += 40;
		}
		offset = 0;
		for (L2PcInstance temp : playerB.getParty().getPartyMembers())
		{
			temp.teleToLocation(x + offset - 180, y + 150, z);
			offset += 40;
		}
	}

	/**
	 * Broadcast a packet to the challanger team
	 */
	public void broadcastToTeam1(L2GameServerPacket packet)
	{
		if (playerA == null)
		{
			return;
		}

		if (partyDuel && playerA.getParty() != null)
		{
			for (L2PcInstance temp : playerA.getParty().getPartyMembers())
			{
				temp.sendPacket(packet);
			}
		}
		else
		{
			playerA.sendPacket(packet);
		}
	}

	/**
	 * Broadcast a packet to the challenged team
	 */
	public void broadcastToTeam2(L2GameServerPacket packet)
	{
		if (playerB == null)
		{
			return;
		}

		if (partyDuel && playerB.getParty() != null)
		{
			for (L2PcInstance temp : playerB.getParty().getPartyMembers())
			{
				temp.sendPacket(packet);
			}
		}
		else
		{
			playerB.sendPacket(packet);
		}
	}

	/**
	 * Get the duel winner
	 *
	 * @return winner
	 */
	public L2PcInstance getWinner()
	{
		if (!isFinished() || playerA == null || playerB == null)
		{
			return null;
		}
		if (playerA.getDuelState() == DUELSTATE_WINNER)
		{
			return playerA;
		}
		if (playerB.getDuelState() == DUELSTATE_WINNER)
		{
			return playerB;
		}
		return null;
	}

	/**
	 * Get the duel looser
	 *
	 * @return looser
	 */
	public L2PcInstance getLooser()
	{
		if (!isFinished() || playerA == null || playerB == null)
		{
			return null;
		}
		if (playerA.getDuelState() == DUELSTATE_WINNER)
		{
			return playerB;
		}
		else if (playerB.getDuelState() == DUELSTATE_WINNER)
		{
			return playerA;
		}
		return null;
	}

	/**
	 * Playback the bow animation for all loosers
	 */
	public void playKneelAnimation()
	{
		L2PcInstance looser = getLooser();

		if (looser == null)
		{
			return;
		}

		if (partyDuel && looser.getParty() != null)
		{
			for (L2PcInstance temp : looser.getParty().getPartyMembers())
			{
				temp.broadcastPacket(new SocialAction(temp.getObjectId(), 7));
			}
		}
		else
		{
			looser.broadcastPacket(new SocialAction(looser.getObjectId(), 7));
		}
	}

	/**
	 * Do the countdown and send message to players if necessary
	 *
	 * @return current count
	 */
	public int countdown()
	{
		countdown--;

		if (countdown > 3)
		{
			return countdown;
		}

		// Broadcast countdown to duelists
		SystemMessage sm = null;
		if (countdown > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS);
			sm.addNumber(countdown);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.LET_THE_DUEL_BEGIN);
		}

		broadcastToTeam1(sm);
		broadcastToTeam2(sm);

		return countdown;
	}

	/**
	 * The duel has reached a state in which it can no longer continue
	 */
	public void endDuel(DuelResultEnum result)
	{
		if (playerA == null || playerB == null)
		{
			//clean up
			playerConditions.clear();
			playerConditions = null;
			DuelManager.getInstance().removeDuel(this);
			return;
		}

		// inform players of the result
		SystemMessage sm = null;
		switch (result)
		{
			case Team1Win:
			case Team2Surrender:
				restorePlayerConditions(false);
				// send SystemMessage
				if (partyDuel)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PARTY_HAS_WON_THE_DUEL);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_DUEL);
				}
				sm.addString(playerA.getName());

				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
				break;
			case Team1Surrender:
			case Team2Win:
				restorePlayerConditions(false);
				// send SystemMessage
				if (partyDuel)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PARTY_HAS_WON_THE_DUEL);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_DUEL);
				}
				sm.addString(playerB.getName());

				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
				break;
			case Canceled:
				stopFighting();
				// dont restore hp, mp, cp
				restorePlayerConditions(true);
				//TODO: is there no other message for a canceled duel?
				// send SystemMessage
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE);

				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
				break;
			case Timeout:
				stopFighting();
				// hp,mp,cp seem to be restored in a timeout too...
				restorePlayerConditions(false);
				// send SystemMessage
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE);

				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
				break;
		}

		// Send end duel packet
		ExDuelEnd duelEnd = null;
		if (partyDuel)
		{
			duelEnd = new ExDuelEnd(1);
		}
		else
		{
			duelEnd = new ExDuelEnd(0);
		}

		broadcastToTeam1(duelEnd);
		broadcastToTeam2(duelEnd);

		//clean up
		playerConditions.clear();
		playerConditions = null;
		DuelManager.getInstance().removeDuel(this);
	}

	/**
	 * Did a situation occur in which the duel has to be ended?
	 *
	 * @return DuelResultEnum duel status
	 */
	public DuelResultEnum checkEndDuelCondition()
	{
		// one of the players might leave during duel
		if (playerA == null || playerB == null)
		{
			return DuelResultEnum.Canceled;
		}

		// got a duel surrender request?
		if (surrenderRequest != 0)
		{
			if (surrenderRequest == 1)
			{
				return DuelResultEnum.Team1Surrender;
			}
			else
			{
				return DuelResultEnum.Team2Surrender;
			}
		}
		// duel timed out
		else if (getRemainingTime() <= 0)
		{
			return DuelResultEnum.Timeout;
		}
		// Has a player been declared winner yet?
		else if (playerA.getDuelState() == DUELSTATE_WINNER)
		{
			// If there is a Winner already there should be no more fighting going on
			stopFighting();
			return DuelResultEnum.Team1Win;
		}
		else if (playerB.getDuelState() == DUELSTATE_WINNER)
		{
			// If there is a Winner already there should be no more fighting going on
			stopFighting();
			return DuelResultEnum.Team2Win;
		}

		// More end duel conditions for 1on1 duels
		else if (!partyDuel)
		{
			// Duel was interrupted e.g.: player was attacked by mobs / other players
			if (playerA.getDuelState() == DUELSTATE_INTERRUPTED || playerB.getDuelState() == DUELSTATE_INTERRUPTED)
			{
				return DuelResultEnum.Canceled;
			}

			// Are the players too far apart?
			if (!playerA.isInsideRadius(playerB, 1600, false, false))
			{
				return DuelResultEnum.Canceled;
			}

			// Did one of the players engage in PvP combat?
			if (isDuelistInPvp(true))
			{
				return DuelResultEnum.Canceled;
			}

			// is one of the players in a Siege, Peace or PvP zone?
			if (playerA.isInsideZone(L2Character.ZONE_PEACE) || playerB.isInsideZone(L2Character.ZONE_PEACE) ||
					playerA.isInsideZone(L2Character.ZONE_SIEGE) || playerB.isInsideZone(L2Character.ZONE_SIEGE) ||
					playerA.isInsideZone(L2Character.ZONE_PVP) || playerB.isInsideZone(L2Character.ZONE_PVP))
			{
				return DuelResultEnum.Canceled;
			}
		}

		return DuelResultEnum.Continue;
	}

	/**
	 * Register a surrender request
	 *
	 * @param player
	 */
	public void doSurrender(L2PcInstance player)
	{
		// already recived a surrender request
		if (surrenderRequest != 0)
		{
			return;
		}

		// stop the fight
		stopFighting();

		// TODO: Can every party member cancel a party duel? or only the party leaders?
		if (partyDuel)
		{
			if (playerA.getParty().getPartyMembers().contains(player))
			{
				surrenderRequest = 1;
				for (L2PcInstance temp : playerA.getParty().getPartyMembers())
				{
					temp.setDuelState(DUELSTATE_DEAD);
				}
				for (L2PcInstance temp : playerB.getParty().getPartyMembers())
				{
					temp.setDuelState(DUELSTATE_WINNER);
				}
			}
			else if (playerB.getParty().getPartyMembers().contains(player))
			{
				surrenderRequest = 2;
				for (L2PcInstance temp : playerB.getParty().getPartyMembers())
				{
					temp.setDuelState(DUELSTATE_DEAD);
				}
				for (L2PcInstance temp : playerA.getParty().getPartyMembers())
				{
					temp.setDuelState(DUELSTATE_WINNER);
				}
			}
		}
		else
		{
			if (player == playerA)
			{
				surrenderRequest = 1;
				playerA.setDuelState(DUELSTATE_DEAD);
				playerB.setDuelState(DUELSTATE_WINNER);
			}
			else if (player == playerB)
			{
				surrenderRequest = 2;
				playerB.setDuelState(DUELSTATE_DEAD);
				playerA.setDuelState(DUELSTATE_WINNER);
			}
		}
	}

	/**
	 * This function is called whenever a player was defeated in a duel
	 */
	public void onPlayerDefeat(L2PcInstance player)
	{
		// Set player as defeated
		player.setDuelState(DUELSTATE_DEAD);

		if (partyDuel)
		{
			boolean teamdefeated = true;
			for (L2PcInstance temp : player.getParty().getPartyMembers())
			{
				if (temp.getDuelState() == DUELSTATE_DUELLING)
				{
					teamdefeated = false;
					break;
				}
			}

			if (teamdefeated)
			{
				L2PcInstance winner = playerA;
				if (playerA.getParty().getPartyMembers().contains(player))
				{
					winner = playerB;
				}

				for (L2PcInstance temp : winner.getParty().getPartyMembers())
				{
					temp.setDuelState(DUELSTATE_WINNER);
				}
			}
		}
		else
		{
			if (player != playerA && player != playerB)
			{
				Log.warning("Error in onPlayerDefeat(): player is not part of this 1vs1 duel");
			}

			if (playerA == player)
			{
				playerB.setDuelState(DUELSTATE_WINNER);
			}
			else
			{
				playerA.setDuelState(DUELSTATE_WINNER);
			}
		}
	}

	/**
	 * This function is called whenever a player leaves a party
	 */
	public void onRemoveFromParty(L2PcInstance player)
	{
		// if it isnt a party duel ignore this
		if (!partyDuel)
		{
			return;
		}

		// this player is leaving his party during party duel
		// if hes either playerA or playerB cancel the duel and port the players back
		if (player == playerA || player == playerB)
		{
			for (PlayerCondition pc : playerConditions)
			{
				pc.teleportBack();
				pc.getPlayer().setIsInDuel(0);
			}

			playerA = null;
			playerB = null;
		}
		else
		// teleport the player back & delete his PlayerCondition record
		{
			for (PlayerCondition pc : playerConditions)
			{
				if (pc.getPlayer() == player)
				{
					pc.teleportBack();
					playerConditions.remove(pc);
					break;
				}
			}
			player.setIsInDuel(0);
		}
	}

	public void onBuff(L2PcInstance player, L2Abnormal debuff)
	{
		for (PlayerCondition pc : playerConditions)
		{
			if (pc.getPlayer() == player)
			{
				pc.registerDebuff(debuff);
				return;
			}
		}
	}
}
