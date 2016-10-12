package l2server.gameserver.events.chess;

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.instance.L2ChessPieceInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.StatusUpdate;

/**
 * @author Pere
 */
public class ChessEvent
{
	public enum ChessState
	{
		INACTIVATING, PARTICIPATING, STARTING, STARTED, REWARDING
	}

	private static ChessEventSide[] _sides = new ChessEventSide[2];

	private static L2ChessPieceInstance[][] _board = new L2ChessPieceInstance[8][8];

	private static ChessState _state = ChessState.PARTICIPATING;

	private static L2PcInstance[][] _waitingPlayers = new L2PcInstance[8][2];

	private static int[] _waitingPlayerSideIds = new int[8];

	public static byte turn = (byte) 0;

	public static boolean pieceMoving = false;

	/**
	 * No instance of this class!<br>
	 */
	private ChessEvent()
	{
	}

	public static void init()
	{
		_sides[0] = new ChessEventSide((byte) 0);
		_sides[1] = new ChessEventSide((byte) 1);
		turn = (byte) 0;
		for (int i = 0; i < 8; i++)
		{
			_waitingPlayers[i][0] = null;
			_waitingPlayers[i][1] = null;
			_waitingPlayerSideIds[i] = -1;
		}
	}

	public static void start()
	{
		init();
		setState(ChessState.PARTICIPATING);
	}

	public static boolean startFight(L2PcInstance player1, L2PcInstance player2)
	{
		// Set state to STARTING
		setState(ChessState.STARTING);

		for (int i = 0; i < 8; i++)
		{
			_waitingPlayers[i][0] = null;
			_waitingPlayers[i][1] = null;
			_waitingPlayerSideIds[i] = -1;
		}

		_sides[0].setPlayer(player1);
		_sides[1].setPlayer(player2);

		_sides[0].spawnPieces();
		_sides[1].spawnPieces();

		// Set state STARTED
		setState(ChessState.STARTED);

		return true;
	}

	public static void calculateRewards(L2PcInstance winner, L2PcInstance loser)
	{
		setState(ChessState.REWARDING);
		rewardPlayer(winner);
		stopFight(winner);
		MagicSkillUse MSU = new MagicSkillUse(winner, winner, 2025, 1, 1, 0, 0);
		L2Skill skill = SkillTable.getInstance().getInfo(2025, 1);
		winner.sendPacket(MSU);
		winner.broadcastPacket(MSU);
		winner.useMagic(skill, false, false);
	}

	private static void rewardPlayer(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}

		/*SystemMessage systemMessage = null;

		PcInventory inv = player.getInventory();
		inv.addItem("Tenkai Events", 6392, 1, player, player);
		systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
		systemMessage.addItemName(6392);
		player.sendPacket(systemMessage);*/

		StatusUpdate statusUpdate = new StatusUpdate(player);
		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);

		statusUpdate.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		npcHtmlMessage.setHtml(
				"<html><head><title>Chess Event</title></head><body>You won the game, congratulations!</body></html>");
		player.sendPacket(statusUpdate);
		player.sendPacket(npcHtmlMessage);
	}

	public static void stopFight(L2PcInstance winner)
	{
		// Set state INACTIVATING
		setState(ChessState.INACTIVATING);

		if (winner != null)
		{
			_sides[1 - getParticipantSideId(winner.getObjectId())].defeatMe();
			_sides[getParticipantSideId(winner.getObjectId())].cleanMe();
		}
		else
		{
			_sides[0].defeatMe();
			_sides[1].defeatMe();
		}
		_sides[0].setCanTheKingBeKilled(false);
		_sides[0].setHasWon(false);
		_sides[1].setCanTheKingBeKilled(false);
		_sides[1].setHasWon(false);

		turn = (byte) 0;
		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 8; j++)
			{
				_board[i][j] = null;
			}
		}

		// Set state INACTIVE
		setState(ChessState.PARTICIPATING);
	}

	public static synchronized boolean addWaitingParticipant(L2PcInstance playerInstance, int sideId)
	{
		if (playerInstance == null)
		{
			return false;
		}

		boolean trobat = false;

		int i = 0;

		while (i < 8 && !trobat)
		{
			if (_waitingPlayers[i][0] == null && _waitingPlayers[i][1] == null)
			{
				trobat = true;
			}
			i++;
		}

		_waitingPlayers[i - 1][sideId] = playerInstance;
		_waitingPlayerSideIds[i - 1] = sideId;

		return trobat;
	}

	public static synchronized boolean removeWaitingParticipant(L2PcInstance playerInstance)
	{
		if (playerInstance == null)
		{
			return false;
		}
		boolean trobat = false;
		int i = 0;

		while (i < 8 && !trobat)
		{
			if (_waitingPlayers[i][0] != null && _waitingPlayers[i][0].getObjectId() == playerInstance.getObjectId() ||
					_waitingPlayers[i][1] != null &&
							_waitingPlayers[i][1].getObjectId() == playerInstance.getObjectId())
			{
				_waitingPlayers[i][0] = null;
				_waitingPlayers[i][1] = null;
				_waitingPlayerSideIds[i] = -1;
				trobat = true;
			}
			i++;
		}
		return trobat;
	}

	public static synchronized L2PcInstance challengeWaitingParticipant(L2PcInstance playerInstance, int cellId)
	{
		if (playerInstance == null)
		{
			return null;
		}

		L2PcInstance target = null;

		if (_waitingPlayers[cellId][0] != null)
		{
			_waitingPlayers[cellId][1] = playerInstance;
			target = _waitingPlayers[cellId][0];
		}
		else if (_waitingPlayers[cellId][1] != null)
		{
			_waitingPlayers[cellId][0] = playerInstance;
			target = _waitingPlayers[cellId][1];
		}

		return target;
	}

	public static synchronized L2PcInstance acceptChallengingParticipant(L2PcInstance playerInstance)
	{
		if (playerInstance == null)
		{
			return null;
		}

		L2PcInstance target = null;
		int side = 0;

		int i = 0;

		while (i < 8 && target == null)
		{
			if (_waitingPlayers[i][0] != null && _waitingPlayers[i][0].getObjectId() == playerInstance.getObjectId())
			{
				target = _waitingPlayers[i][1];
			}
			else if (_waitingPlayers[i][1] != null &&
					_waitingPlayers[i][1].getObjectId() == playerInstance.getObjectId())
			{
				target = _waitingPlayers[i][0];
				side = 1;
			}
			i++;
		}

		if (side == 0)
		{
			startFight(playerInstance, target);
		}
		else
		{
			startFight(target, playerInstance);
		}

		return target;
	}

	public static synchronized L2PcInstance rejectChallengingParticipant(L2PcInstance playerInstance)
	{
		if (playerInstance == null)
		{
			return null;
		}

		L2PcInstance target = null;

		int i = 0;

		while (i < 8 && target == null)
		{
			if (_waitingPlayers[i][0] != null && _waitingPlayers[i][0].getObjectId() == playerInstance.getObjectId())
			{
				target = _waitingPlayers[i][1];
				_waitingPlayers[i][1] = null;
			}
			else if (_waitingPlayers[i][1] != null &&
					_waitingPlayers[i][1].getObjectId() == playerInstance.getObjectId())
			{
				target = _waitingPlayers[i][0];
				_waitingPlayers[i][0] = null;
			}
			i++;
		}
		return target;
	}

	public static boolean removeParticipant(int playerObjectId)
	{
		// Get the teamId of the player
		byte teamId = getParticipantSideId(playerObjectId);

		// Check if the player is participant
		if (teamId != -1)
		{
			// Remove the player from team
			_sides[teamId].removePlayer();
			return true;
		}

		return false;
	}

	public static void sysMsgToAllParticipants(String message)
	{
		_sides[0].getPlayer().sendMessage(message);
		_sides[1].getPlayer().sendMessage(message);
	}

	public static void onLogout(L2PcInstance playerInstance)
	{
		if (playerInstance != null &&
				(isState(ChessState.STARTING) || isState(ChessState.STARTED) || isState(ChessState.PARTICIPATING)))
		{
			if (isState(ChessState.STARTED) && isPlayerParticipant(playerInstance.getObjectId()))
			{
				for (L2Abnormal effect : playerInstance.getAllEffects())
				{
					if (effect != null)
					{
						effect.exit();
					}
				}
			}
			removeParticipant(playerInstance.getObjectId());
		}
	}

	public static synchronized void onBypass(String command, L2PcInstance playerInstance)
	{
		if (playerInstance == null || !isState(ChessState.PARTICIPATING))
		{
			return;
		}

		if (command.startsWith("wait") && getParticipantSideId(playerInstance.getObjectId()) == -1)
		{

			int sideId = Integer.valueOf(command.substring(4));

			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);

			if (playerInstance.isCursedWeaponEquipped())
			{
				npcHtmlMessage.setHtml(
						"<html><head><title>Instanced Events</title></head><body>Cursed weapon owners are not allowed to participate.</body></html>");
			}
			else if (playerInstance.getReputation() < 0)
			{
				npcHtmlMessage.setHtml(
						"<html><head><title>Instanced Events</title></head><body>Chaotic players are not allowed to participate.</body></html>");
			}
			else if (addWaitingParticipant(playerInstance, sideId))
			{
				String html =
						"<html><head><title>Instanced Events</title></head><body>Now you are at the waiting list<br>";
				npcHtmlMessage.setHtml(html);
			}
			else
			{
				String html = "<html><head><title>Instanced Events</title></head><body>The waiting list is full.<br>";
				npcHtmlMessage.setHtml(html);
			}

			playerInstance.sendPacket(npcHtmlMessage);
		}
		else if (command.equals("remove_wait"))
		{
			removeWaitingParticipant(playerInstance);

			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);

			npcHtmlMessage.setHtml(
					"<html><head><title>Instanced Events</title></head><body>You have been deleted from the waiting list.</body></html>");
			playerInstance.sendPacket(npcHtmlMessage);
		}
		else if (command.startsWith("challenge"))
		{
			int waitCell = Integer.valueOf(command.substring(9));

			L2PcInstance target = null;
			target = challengeWaitingParticipant(playerInstance, waitCell);

			NpcHtmlMessage npcHtmlMessage1 = new NpcHtmlMessage(0);

			npcHtmlMessage1.setHtml(
					"<html><head><title>Instanced Events</title></head><body>You have challenged the player " +
							target.getName() + ".</body></html>");
			playerInstance.sendPacket(npcHtmlMessage1);

			NpcHtmlMessage npcHtmlMessage2 = new NpcHtmlMessage(0);

			npcHtmlMessage2.setHtml(
					"<html><head><title>Instanced Events</title></head><body>" + playerInstance.getName() +
							" has challenged you for a game. Do you accept?<br>" +
							"To accept talk with the Chess Manager.</body></html>");
			target.sendPacket(npcHtmlMessage2);
		}
		else if (command.equals("accept"))
		{
			L2PcInstance target = null;
			target = acceptChallengingParticipant(playerInstance);

			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);

			npcHtmlMessage.setHtml(
					"<html><head><title>Instanced Events</title></head><body>Your game is about to start. Get ready!</body></html>");
			playerInstance.sendPacket(npcHtmlMessage);
			target.sendPacket(npcHtmlMessage);
		}
		else if (command.equals("reject"))
		{
			L2PcInstance target = null;
			target = rejectChallengingParticipant(playerInstance);

			NpcHtmlMessage npcHtmlMessage1 = new NpcHtmlMessage(0);

			npcHtmlMessage1.setHtml(
					"<html><head><title>Instanced Events</title></head><body>You have rejected " + target.getName() +
							"'s challenge.</body></html>");
			playerInstance.sendPacket(npcHtmlMessage1);

			NpcHtmlMessage npcHtmlMessage2 = new NpcHtmlMessage(0);

			npcHtmlMessage2.setHtml(
					"<html><head><title>Instanced Events</title></head><body>" + playerInstance.getName() +
							" has rejected your challenge.</body></html>");
			target.sendPacket(npcHtmlMessage2);
		}
	}

	private static void setState(ChessState state)
	{
		if (state == ChessState.PARTICIPATING)
		{
			pieceMoving = false;
		}
		_state = state;
	}

	public static boolean isState(ChessState state)
	{
		return _state == state;
	}

	public static byte getParticipantSideId(int playerObjectId)
	{
		return (byte) (_sides[0].containsPlayer(playerObjectId) ? 0 :
				_sides[1].containsPlayer(playerObjectId) ? 1 : -1);
	}

	public static boolean isPlayerParticipant(int playerObjectId)
	{
		if (!isState(ChessState.PARTICIPATING) && !isState(ChessState.STARTING) && !isState(ChessState.STARTED))
		{
			return false;
		}
		return _sides[0].containsPlayer(playerObjectId) || _sides[1].containsPlayer(playerObjectId);
	}

	public static boolean isPlayerWaiting(int playerObjectId)
	{
		for (int i = 0; i < 8; i++)
		{
			if (_waitingPlayers[i][0] != null && _waitingPlayers[i][0].getObjectId() == playerObjectId ||
					_waitingPlayers[i][1] != null && _waitingPlayers[i][1].getObjectId() == playerObjectId)
			{
				return true;
			}
		}
		return false;
	}

	public static boolean isPlayerWaitingLeader(int playerObjectId)
	{
		for (int i = 0; i < 8; i++)
		{
			if (_waitingPlayerSideIds[i] != -1 && _waitingPlayers[i][_waitingPlayerSideIds[i]] != null &&
					_waitingPlayers[i][_waitingPlayerSideIds[i]].getObjectId() == playerObjectId)
			{
				return true;
			}
		}
		return false;
	}

	public static boolean isPlayerChallenged(int playerObjectId)
	{
		for (int i = 0; i < 8; i++)
		{
			if (_waitingPlayers[i][0] != null && _waitingPlayers[i][1] != null && _waitingPlayerSideIds[i] != -1 &&
					_waitingPlayers[i][_waitingPlayerSideIds[i]].getObjectId() == playerObjectId)
			{
				return true;
			}
		}
		return false;
	}

	public static L2PcInstance[][] getWaitingPlayers()
	{
		return _waitingPlayers;
	}

	public static ChessEventSide getSide(int id)
	{
		return _sides[id];
	}

	public static L2ChessPieceInstance[][] getBoard()
	{
		return _board;
	}

	public static L2ChessPieceInstance[][] getBoard(int side)
	{
		if (side == 0)
		{
			return _board;
		}
		else
		{
			L2ChessPieceInstance[][] board = new L2ChessPieceInstance[8][8];
			for (int i = 0; i < 8; i++)
			{
				for (int j = 0; j < 8; j++)
				{
					board[i][j] = _board[7 - i][7 - j];
				}
			}
			return board;
		}
	}

	public static void setBoard(int i, int j, L2ChessPieceInstance piece)
	{
		_board[i][j] = piece;
	}

	public static void setBoard(int i, int j, L2ChessPieceInstance piece, int side)
	{
		if (side != 0)
		{
			i = 7 - i;
			j = 7 - j;
		}
		_board[i][j] = piece;
	}
}
