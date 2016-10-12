package l2server.gameserver.model.actor.instance;

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.events.chess.ChessEvent;
import l2server.gameserver.events.chess.ChessEvent.ChessState;
import l2server.gameserver.events.chess.ChessEventSide;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.gameserver.templates.chars.L2NpcTemplate;

import java.util.StringTokenizer;

/**
 * @author Pere
 */
public final class L2ChessPieceInstance extends L2MonsterInstance
{
	private ChessEventSide _side;
	private int _type;
	private boolean _firstMove;
	private int _posX;
	private int _posY;

	public L2ChessPieceInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);

		int templ = getNpcId();
		if (templ % 100 < 10)
		{
			setSide(ChessEvent.getSide(0));
		}
		else
		{
			setSide(ChessEvent.getSide(1));
		}

		_type = templ % 10;

		_firstMove = true;

		setIsInvul(true);
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (this != player.getTarget())
		{
			player.setTarget(this);
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			player.sendPacket(new ValidateLocation(this));
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				// Open a chat window on client with the text of the L2NpcInstance
				showChatWindow(player, 0);
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void showChatWindow(L2PcInstance playerInstance, int val)
	{
		if (playerInstance == null)
		{
			return;
		}

		if (ChessEvent.isState(ChessState.STARTED))
		{
			if (playerInstance.isGM() || getSide().getPlayer().getObjectId() == playerInstance.getObjectId() &&
					ChessEvent.turn == getSide().getId())
			{
				String htmFile = "ChessEventMovements.htm";
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
							if (moveType(i, j, true) == 4)
							{
								square = "<font color=\"00FF00\"><a action=\"bypass -h npc_%objectId%_enrocar_" +
										getSide().getPiece(7, 0).getObjectId() + "_" + i + "_" + j +
										"_dreta\">[]</a></font>";
							}
							else if (moveType(i, j, true) == 3)
							{
								square = "<font color=\"00FF00\"><a action=\"bypass -h npc_%objectId%_enrocar_" +
										getSide().getPiece(0, 0).getObjectId() + "_" + i + "_" + j +
										"_esquerra\">[]</a></font>";
							}
							else if (moveType(i, j, true) == 2)
							{
								square = "<font color=\"FF0000\"><a action=\"bypass -h npc_%objectId%_attack_" +
										ChessEvent.getBoard(getSide().getId())[i][j].getObjectId() + "_" + i + "_" + j +
										"\">[]</a></font>";
							}
							else if (moveType(i, j, true) == 1)
							{
								square = "<font color=\"00FF00\"><a action=\"bypass -h npc_%objectId%_moveto_" + i +
										"_" + j + "\">[]</a></font>";
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
					npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
					playerInstance.sendPacket(npcHtmlMessage);
				}
			}
			else if (getSide().getPlayer().getObjectId() == playerInstance.getObjectId() &&
					ChessEvent.turn != getSide().getId())
			{
				say(playerInstance, "You must wait for your turn");
			}
			else if (getSide().getEnemy().getPlayer().getObjectId() == playerInstance.getObjectId())
			{
				say(playerInstance, "I won't obey you!");
			}
			else
			{
				say(playerInstance, "You aren't participating in this game, so don't touch me!");
			}
		}
		playerInstance.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(L2PcInstance playerInstance, String command)
	{
		int side = 1;
		if ((command.startsWith("moveto") || command.startsWith("attack") || command.startsWith("enrocar")) &&
				ChessEvent.isState(ChessState.STARTED) && !ChessEvent.pieceMoving)
		{
			StringTokenizer st = new StringTokenizer(command, "_");
			st.nextToken();
			if (command.startsWith("moveto"))
			{
				move(Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken()), null);
			}
			else if (command.startsWith("attack"))
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

				move(Integer.valueOf(st.nextToken()), Integer.valueOf(st.nextToken()), enemy);
			}
			else if (command.startsWith("enrocar"))
			{
				int rookId = Integer.valueOf(st.nextToken());
				L2ChessPieceInstance rook = null;

				for (int i = 0; i < 8; i++)
				{
					for (int j = 0; j < 8; j++)
					{
						if (ChessEvent.getBoard(side)[i][j] != null &&
								ChessEvent.getBoard(side)[i][j].getObjectId() == rookId)
						{
							rook = ChessEvent.getBoard(side)[i][j];
						}
					}
				}
				int x = Integer.valueOf(st.nextToken());
				int y = Integer.valueOf(st.nextToken());
				String costat = st.nextToken();
				move(x, y, null);
				if (costat.equalsIgnoreCase("dreta"))
				{
					rook.move(x - 1, y, null);
				}
				else
				{
					rook.move(x + 1, y, null);
				}
			}
		}
		else if (ChessEvent.pieceMoving)
		{
			say(playerInstance, "You must wait for the piece to finish its movement!");
		}
	}

	public void move(int i, int j, L2ChessPieceInstance enemy)
	{
		ChessEvent.pieceMoving = true;
		setTurn();
		sayMovement(enemy);

		if (_firstMove)
		{
			_firstMove = false;
		}

		int moveX;
		int moveY;
		int rPosX;
		int rPosY;

		if (getSide().getId() == 0)
		{
			rPosX = _posX;
			rPosY = _posY;
		}
		else
		{
			rPosX = 7 - _posX;
			rPosY = 7 - _posY;
			i = 7 - i;
			j = 7 - j;
		}

		ChessEvent.setBoard(rPosX, rPosY, null);
		ChessEvent.setBoard(i, j, this);
		moveX = (i - rPosX) * 45;
		moveY = (j - rPosY) * 45;

		getSpawn().setX(rPosY * 45 - 60165 + moveY);
		getSpawn().setY(rPosX * 45 - 59752 + moveX);

		if (enemy != null)
		{
			while (getAI().getIntention() != CtrlIntention.AI_INTENTION_MOVE_TO &&
					(Math.abs(getX() - (rPosY * 45 - 60165 + moveY)) > 30 ||
							Math.abs(getY() - (rPosX * 45 - 59752 + moveX)) > 30))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
						new L2CharPosition(rPosY * 45 - 60165 + moveY, rPosX * 45 - 59752 + moveX, -1932, 0));
				while (getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO &&
						(Math.abs(getX() - (rPosY * 45 - 60165 + moveY)) > 30 ||
								Math.abs(getY() - (rPosX * 45 - 59752 + moveX)) > 30))
				{
					sleep();
				}
			}

			enemy.setIsInvul(false);
			enemy.getSpawn().stopRespawn();

			setTarget(enemy);

			if (!isCoreAIDisabled())
			{
				addDamageHate(enemy, 0, 1);
			}

			while (getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK && !enemy.isDead())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, enemy);
				getAI().setAutoAttacking(true);
				doAttack(enemy);
				while (getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
				{
					sleep();
				}
			}
		}

		while (getAI().getIntention() != CtrlIntention.AI_INTENTION_MOVE_TO &&
				(Math.abs(getX() - (rPosY * 45 - 60165 + moveY)) > 5 ||
						Math.abs(getY() - (rPosX * 45 - 59752 + moveX)) > 5))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
					new L2CharPosition(rPosY * 45 - 60165 + moveY, rPosX * 45 - 59752 + moveX, -1932, 0));
			while (getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO)
			{
				sleep();
			}
		}

		if (getSide().getId() == 0)
		{
			_posX = i;
			_posY = j;
		}
		else
		{
			_posX = 7 - i;
			_posY = 7 - j;
		}

		getSide().canTheKingBeKilled(true);

		if (enemy != null && enemy.getType() == 6)
		{
			ChessEvent.calculateRewards(getSide().getPlayer(), getSide().getEnemy().getPlayer());
		}
		else if (canKillTheKing())
		{
			getSide().getEnemy().setCanTheKingBeKilled(true);
			getSide().setHasWon(true);

			for (int k = 0; k < 8; k++)
			{
				for (int l = 0; l < 8; l++)
				{
					L2ChessPieceInstance enemySaving = getSide().getPiece(l, k);
					if (enemySaving != null && enemySaving.getSide() != getSide())
					{
						for (int m = 0; m < 8; m++)
						{
							for (int n = 0; n < 8; n++)
							{
								if (enemySaving.moveType(m, n, true) != 0)
								{
									getSide().setHasWon(false);
								}
							}
						}
					}
				}
			}
			sayMovement(this);
			if (getSide().hasWon())
			{
				ChessEvent.turn = getSide().getId();
			}
		}
		else
		{
			getSide().getEnemy().canTheKingBeKilled(true);
		}

		ChessEvent.pieceMoving = false;
	}

	public int moveType(int i, int j, boolean checkKing)
	{
		if (i == _posX && j == _posY)
		{
			return 0;
		}
		boolean canMove = false;
		int toReturn = 0;
		switch (getType())
		{
			case 1:
				if (i == _posX && j == _posY + 1 && estaBuit(i, j))
				{
					canMove = true;
				}
				if (Math.abs(i - _posX) == 1 && j == _posY + 1 && hiHaEnemic(i, j))
				{
					canMove = true;
				}
				if (_firstMove && i == _posX && j == _posY + 2 && !anyBetween(_posX, _posY, i, j) && estaBuit(i, j))
				{
					canMove = true;
				}
				break;
			case 2:
				if ((i == _posX || j == _posY) && !anyBetween(_posX, _posY, i, j))
				{
					canMove = true;
				}
				break;
			case 3:
				if (Math.abs(i - _posX) == 1 && Math.abs(j - _posY) == 2 ||
						Math.abs(i - _posX) == 2 && Math.abs(j - _posY) == 1)
				{
					canMove = true;
				}
				break;
			case 4:
				if (Math.abs(i - _posX) == Math.abs(j - _posY) && !anyBetween(_posX, _posY, i, j))
				{
					canMove = true;
				}
				break;
			case 5:
				if ((i == _posX || j == _posY || Math.abs(i - _posX) == Math.abs(j - _posY)) &&
						!anyBetween(_posX, _posY, i, j))
				{
					canMove = true;
				}
				break;
			case 6:
				if ((Math.abs(i - _posX) == 1 || Math.abs(j - _posY) == 1) &&
						Math.abs(i - _posX) + Math.abs(j - _posY) <= 2)
				{
					canMove = true;
				}
				if (_firstMove && j == _posY && i == _posX - 2 && getSide().getPiece(0, 0) != null &&
						!getSide().getPiece(0, 0).hasMoved() && !anyBetween(_posX, _posY, 0, 0))
				{
					canMove = true;
					toReturn = 3;
				}
				if (_firstMove && j == _posY && i == _posX + 2 && getSide().getPiece(7, 0) != null &&
						!getSide().getPiece(7, 0).hasMoved() && !anyBetween(_posX, _posY, 7, 0))
				{
					canMove = true;
					toReturn = 4;
				}
				break;
		}
		if (canMove)
		{
			if (hiHaAmic(i, j) || getSide().hasWon() && (!hiHaEnemic(i, j) ||
					getSide().getPiece(i, j).getObjectId() != getSide().getEnemy().getKing().getObjectId()))
			{
				return 0;
			}
			if (checkKing)
			{
				L2ChessPieceInstance piece = getSide().getPiece(i, j);
				int oPosX = _posX;
				int oPosY = _posY;
				_posX = i;
				_posY = j;
				getSide().setPiece(i, j, this);
				getSide().setPiece(oPosX, oPosY, null);
				if (getSide().canTheKingBeKilled(true))
				{
					canMove = false;
				}
				getSide().setPiece(i, j, piece);
				getSide().setPiece(oPosX, oPosY, this);
				_posX = oPosX;
				_posY = oPosY;
			}
			if (canMove)
			{
				if (toReturn != 0)
				{
					return toReturn;
				}
				if (!hiHaEnemic(i, j))
				{
					return 1;
				}
				else
				{
					return 2;
				}
			}
		}
		return 0;
	}

	public boolean anyBetween(int x, int y, int i, int j)
	{
		int k;
		if (i == x)
		{
			if (y > j)
			{
				for (k = y - 1; k > j; k--)
				{
					if (!estaBuit(i, k))
					{
						return true;
					}
				}
			}
			else
			{
				for (k = y + 1; k < j; k++)
				{
					if (!estaBuit(i, k))
					{
						return true;
					}
				}
			}
		}
		else if (y == j)
		{
			if (x > i)
			{
				for (k = x - 1; k > i; k--)
				{
					if (!estaBuit(k, j))
					{
						return true;
					}
				}
			}
			else
			{
				for (k = x + 1; k < i; k++)
				{
					if (!estaBuit(k, j))
					{
						return true;
					}
				}
			}
		}
		else if (Math.abs(i - x) == Math.abs(j - y))
		{
			if (i - x == j - y)
			{
				if (Math.abs(i - x) == i - x)
				{
					for (k = 1; k < i - x; k++)
					{
						if (!estaBuit(x + k, y + k))
						{
							return true;
						}
					}
				}
				else
				{
					for (k = 1; k < x - i; k++)
					{
						if (!estaBuit(x - k, y - k))
						{
							return true;
						}
					}
				}
			}
			else
			{
				if (Math.abs(i - x) == i - x)
				{
					for (k = 1; k < i - x; k++)
					{
						if (!estaBuit(x + k, y - k))
						{
							return true;
						}
					}
				}
				else
				{
					for (k = 1; k < x - i; k++)
					{
						if (!estaBuit(x - k, y + k))
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean estaBuit(int i, int j)
	{
		return getSide().estaBuit(i, j);
	}

	private boolean hiHaAmic(int i, int j)
	{
		return getSide().hiHaAmic(i, j);
	}

	private boolean hiHaEnemic(int i, int j)
	{
		return getSide().hiHaEnemic(i, j);
	}

	public boolean canKillTheKing()
	{
		L2ChessPieceInstance king = getSide().getEnemy().getKing();
		return moveType(7 - king.getPosX(), 7 - king.getPosY(), false) == 2;
	}

	private void sayMovement(L2ChessPieceInstance victim)
	{
		if (getSide().getPlayer() == null || getSide().getEnemy().getPlayer() == null)
		{
			return;
		}
		String message;
		if (victim == null)
		{
			message = "I move a " + getPieceName() + ".";
		}
		else if (victim.getObjectId() == getObjectId())
		{
			if (!getSide().hasWon())
			{
				message = "Check";
			}
			else
			{
				message = "Checkmate";
			}
		}
		else
		{
			if (victim.getType() != 5 && victim.getType() != 6)
			{
				message = "I kill one of your " + victim.getPieceName() + "s with a " + getPieceName() + ".";
			}
			else
			{
				message = "I kill your " + victim.getPieceName() + "s with a " + getPieceName() + ".";
			}
		}
		CreatureSay cs1 = new CreatureSay(getSide().getPlayer().getObjectId(), Say2.TELL,
				"->" + getSide().getEnemy().getPlayer().getName(), message);
		getSide().getPlayer().sendPacket(cs1);
		CreatureSay cs2 =
				new CreatureSay(getSide().getPlayer().getObjectId(), Say2.TELL, getSide().getPlayer().getName(),
						message);
		getSide().getEnemy().getPlayer().sendPacket(cs2);
	}

	public void setTurn()
	{
		ChessEvent.turn = getSide().getEnemy().getId();
	}

	public String getPieceName()
	{
		switch (getType())
		{
			case 1:
				return "pawn";
			case 2:
				return "rook";
			case 3:
				return "knight";
			case 4:
				return "bishop";
			case 5:
				return "queen";
			default:
				return "king";
		}
	}

	private void say(L2PcInstance player, String message)
	{
		player.sendPacket(new CreatureSay(getObjectId(), Say2.TELL, getName(), message));
	}

	@Override
	public boolean isAttackable()
	{
		return false;
	}

	@Override
	public boolean isAutoAttackable(L2Character character)
	{
		return character instanceof L2ChessPieceInstance;
	}

	@Override
	public void onSpawn()
	{
		_posX = (getY() + 59752) / 45;
		_posY = (getX() + 60165) / 45;
		if (getSide().getId() == 1)
		{
			_posX = 7 - _posX;
			_posY = 7 - _posY;
		}
		getSide().setPiece(_posX, _posY, this);
		super.onSpawn();
	}

	@Override
	public boolean isAggressive()
	{
		return false;
	}

	@Override
	public int getAggroRange()
	{
		return 0;
	}

	public boolean hasMoved()
	{
		return !_firstMove;
	}

	public int getType()
	{
		return _type;
	}

	public ChessEventSide getSide()
	{
		return _side;
	}

	public void setSide(ChessEventSide side)
	{
		_side = side;
	}

	public int getPosX()
	{
		return _posX;
	}

	public int getPosY()
	{
		return _posY;
	}

	public int getRPosX()
	{
		if (getSide().getId() == 0)
		{
			return _posX;
		}
		else
		{
			return 7 - _posX;
		}
	}

	public int getRPosY()
	{
		if (getSide().getId() == 0)
		{
			return _posY;
		}
		else
		{
			return 7 - _posY;
		}
	}

	private void sleep()
	{
		try
		{
			Thread.sleep(100);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
