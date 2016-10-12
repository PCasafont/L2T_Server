package l2server.gameserver.model.actor.instance;

import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.events.chess.ChessEvent;
import l2server.gameserver.events.chess.ChessEvent.ChessState;
import l2server.gameserver.model.L2World;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.ConfirmDlg;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;

import java.util.StringTokenizer;

/**
 * @author Pere
 */
public class L2ChessManagerInstance extends L2NpcInstance
{
	public L2ChessManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance playerInstance, String command)
	{
		int side = ChessEvent.getParticipantSideId(playerInstance.getObjectId());

		if (command.equals("observe"))
		{
			playerInstance.enterObserverMode(-60008, -59595, -1850);
		}
		else if (command.startsWith("challenge "))
		{
			L2PcInstance receptor = L2World.getInstance().getPlayer(command.substring(10));
			if (receptor != null && !receptor.isChessChallengeRequest() &&
					receptor.getObjectId() != playerInstance.getObjectId())
			{
				receptor.setChessChallengeRequest(true, playerInstance);
				String confirmText = playerInstance.getName() + " has challenged you to a chess game. Do you accept?";
				ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1.getId()).addString(confirmText);
				receptor.sendPacket(dlg);
				playerInstance.sendMessage("Now you have challenged " + receptor.getName() + " to a chess game.");
			}
			else
			{
				playerInstance.sendMessage("This player could not be found in the world!");
			}
		}
		else if ((command.startsWith("movements") || command.startsWith("moveto") || command.startsWith("attack")) &&
				ChessEvent.isState(ChessState.STARTED) && !ChessEvent.pieceMoving && ChessEvent.turn == side)
		{
			StringTokenizer st = new StringTokenizer(command, "_");
			st.nextToken();
			int pieceId = Integer.valueOf(st.nextToken());
			L2ChessPieceInstance piece = null;

			for (int i = 0; i < 8; i++)
			{
				for (int j = 0; j < 8; j++)
				{
					if (ChessEvent.getBoard(side)[i][j] != null &&
							ChessEvent.getBoard(side)[i][j].getObjectId() == pieceId)
					{
						piece = ChessEvent.getBoard(side)[i][j];
					}
				}
			}

			if (piece != null)
			{
				if (command.startsWith("movements"))
				{
					String htmFile = "tenkai/ChessEventMovements.htm";
					String htmContent = HtmCache.getInstance().getHtm(playerInstance.getHtmlPrefix(), htmFile);

					if (htmContent != null)
					{
						NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
						String board = "<table border=0>";
						String square;

						npcHtmlMessage.setHtml(htmContent);

						for (int j = 7; j >= 0; j--)
						{
							board += "<tr>";
							for (int i = 0; i < 8; i++)
							{
								board += "<td>";
								if (piece.moveType(i, j, true) == 4)
								{
									square = "<font color=\"00FF00\"><a action=\"bypass -h npc_" + getObjectId() +
											"_enrocar_" + piece.getSide().getPiece(7, 0).getObjectId() + "_" + i + "_" +
											j + "_dreta\">[]</a></font>";
								}
								else if (piece.moveType(i, j, true) == 3)
								{
									square = "<font color=\"00FF00\"><a action=\"bypass -h npc_" + getObjectId() +
											"_enrocar_" + piece.getSide().getPiece(0, 0).getObjectId() + "_" + i + "_" +
											j + "_esquerra\">[]</a></font>";
								}
								else if (piece.moveType(i, j, true) == 2)
								{
									square = "<font color=\"FF0000\"><a action=\"bypass -h npc_" + getObjectId() +
											"_attack_" + piece.getObjectId() + "_" +
											ChessEvent.getBoard(side)[i][j].getObjectId() + "_" + i + "_" + j +
											"\">[]</a></font>";
								}
								else if (piece.moveType(i, j, true) == 1)
								{
									square = "<font color=\"00FF00\"><a action=\"bypass -h npc_" + getObjectId() +
											"_moveto_" + piece.getObjectId() + "_" + i + "_" + j + "\">[]</a></font>";
								}
								else
								{
									square = "[]";
								}

								board += square + "</td>";
							}
							board += "</tr>";
						}
						npcHtmlMessage.replace("%board%", board);
						playerInstance.sendPacket(npcHtmlMessage);
					}
				}
				if (command.startsWith("moveto"))
				{
					piece.move(Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken()), null);
				}
				if (command.startsWith("attack"))
				{
					int enemyId = Integer.valueOf(st.nextToken());
					L2ChessPieceInstance enemy = null;

					for (int i = 0; i < 8; i++)
					{
						for (int j = 0; j < 8; j++)
						{
							if (ChessEvent.getBoard(side)[i][j] != null &&
									ChessEvent.getBoard(side)[i][j].getObjectId() == enemyId)
							{
								enemy = ChessEvent.getBoard(side)[i][j];
							}
						}
					}

					piece.move(Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken()), enemy);
				}
			}
		}
		else if (ChessEvent.isState(ChessState.STARTED) && ChessEvent.turn != side)
		{
			playerInstance.sendMessage("You must wait for your turn.");
		}
		else if (ChessEvent.pieceMoving)
		{
			playerInstance.sendMessage("You must wait for the piece to finish it's movement!");
		}
		else
		{
			ChessEvent.onBypass(command, playerInstance);
		}
	}

	@Override
	public void showChatWindow(L2PcInstance playerInstance, int val)
	{
		if (playerInstance == null)
		{
			return;
		}

		if (ChessEvent.isState(ChessState.PARTICIPATING))
		{
			String htmFile = "tenkai/";

			if (!ChessEvent.isPlayerWaiting(playerInstance.getObjectId()))
			{
				htmFile += "ChessEventParticipation.htm";
				String htmContent = HtmCache.getInstance().getHtm(playerInstance.getHtmlPrefix(), htmFile);

				if (htmContent != null)
				{
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

					L2PcInstance player = null;
					String list = "";
					String color = "";

					for (int i = 0; i < 8; i++)
					{
						if (ChessEvent.getWaitingPlayers()[i][0] != null &&
								ChessEvent.getWaitingPlayers()[i][1] == null)
						{
							player = ChessEvent.getWaitingPlayers()[i][0];
							color = "White";
						}
						else if (ChessEvent.getWaitingPlayers()[i][1] != null &&
								ChessEvent.getWaitingPlayers()[i][0] == null)
						{
							player = ChessEvent.getWaitingPlayers()[i][1];
							color = "Black";
						}
						if (player != null)
						{
							list += "<tr><td>" + player.getName() + "</td><td>(" + color +
									")</td><td><a action=\"bypass -h npc_" + getObjectId() + "_challenge" + i +
									"\">Retar!</a></td></tr>";
						}
						player = null;
					}

					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%list%", list);
					npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
					playerInstance.sendPacket(npcHtmlMessage);
				}
			}
			else if (ChessEvent.isPlayerChallenged(playerInstance.getObjectId()))
			{
				htmFile += "ChessEventConfirmChallenge.htm";
				String htmContent = HtmCache.getInstance().getHtm(playerInstance.getHtmlPrefix(), htmFile);

				if (htmContent != null)
				{
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

					L2PcInstance player = null;

					for (int i = 0; i < 8; i++)
					{
						if (ChessEvent.getWaitingPlayers()[i][1] != null &&
								ChessEvent.getWaitingPlayers()[i][0] != null &&
								ChessEvent.getWaitingPlayers()[i][0].getObjectId() == playerInstance.getObjectId())
						{
							player = ChessEvent.getWaitingPlayers()[i][1];
						}
						else if (ChessEvent.getWaitingPlayers()[i][0] != null &&
								ChessEvent.getWaitingPlayers()[i][1] != null &&
								ChessEvent.getWaitingPlayers()[i][1].getObjectId() == playerInstance.getObjectId())
						{
							player = ChessEvent.getWaitingPlayers()[i][0];
						}
					}

					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
					npcHtmlMessage.replace("%challenger%", player.getName());
					playerInstance.sendPacket(npcHtmlMessage);
				}
			}
			else if (ChessEvent.isPlayerWaitingLeader(playerInstance.getObjectId()))
			{
				htmFile += "ChessEventRemoveParticipation.htm";
				String htmContent = HtmCache.getInstance().getHtm(playerInstance.getHtmlPrefix(), htmFile);

				if (htmContent != null)
				{
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
					playerInstance.sendPacket(npcHtmlMessage);
				}
			}
		}

		else if (ChessEvent.isState(ChessState.STARTED))
		{
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
			if (ChessEvent.getSide(0).getPlayer() == null || ChessEvent.getSide(0).getPlayer().isOnline() ||
					ChessEvent.getSide(1).getPlayer() == null || ChessEvent.getSide(1).getPlayer().isOnline())
			{
				//TODO: check him to be around the board!!
				npcHtmlMessage.setHtml(
						"<html><head><title>Instanced Events</title></head><body>The current game has been interrupted due to some of the participants leaving.</body></html>");
				ChessEvent.stopFight(null);
				playerInstance.sendPacket(npcHtmlMessage);
			}
			else if (ChessEvent.isPlayerParticipant(playerInstance.getObjectId()))
			{
				String htmFile = "chess/ChessEventBoard.htm";
				String htmContent = HtmCache.getInstance().getHtm(playerInstance.getHtmlPrefix(), htmFile);

				if (htmContent != null)
				{
					String board = "<table border=0>";
					String square;

					npcHtmlMessage.setHtml(htmContent);

					int side = ChessEvent.getParticipantSideId(playerInstance.getObjectId());

					for (int j = 7; j >= 0; j--)
					{
						board += "<tr>";
						for (int i = 0; i < 8; i++)
						{
							board += "<td>";
							if (ChessEvent.getBoard(side)[i][j] == null)
							{
								square = "[]";
							}
							else if (ChessEvent.getBoard(side)[i][j].getSide().getId() == side)
							{
								square = "<a action=\"bypass -h npc_" + getObjectId() + "_movements_" +
										ChessEvent.getBoard(side)[i][j].getObjectId() + "\">[]</a>";
							}
							else
							{
								square = "<font color=\"FF0000\">[]</font>";
							}

							board += square + "</td>";
						}
						board += "</tr>";
					}
					npcHtmlMessage.replace("%board%", board);
					playerInstance.sendPacket(npcHtmlMessage);
				}
			}
			else
			{
				npcHtmlMessage.setHtml(
						"<html><head><title>Instanced Events</title></head><body>There is a started game already.<br>" +
								"<a action=\"bypass -h npc_" + getObjectId() +
								"_observe\">Observe Match</a></body></html>");
				playerInstance.sendPacket(npcHtmlMessage);
			}
		}

		playerInstance.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
