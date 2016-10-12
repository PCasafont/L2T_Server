package l2server.gameserver.events.chess;

import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.instance.L2ChessPieceInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author Pere
 */
public class ChessEventSide
{
	private byte _id;

	private int _startX;
	private int _startY;
	private int _startZ;

	private L2PcInstance _player;

	private int _endX;
	private int _endY;
	private int _endZ;

	private int[][] _board;

	private L2Spawn[] _pieceSpawns;

	private L2ChessPieceInstance _king;

	private boolean _canTheKingBeKilled = false;

	private boolean _hasWon = false;

	public ChessEventSide(byte id)
	{
		_id = id;
		_pieceSpawns = new L2Spawn[16];
		_board = new int[8][8];
		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 8; j++)
			{
				_board[i][j] = 0;
			}
		}
		if (id == 0)
		{
			_startX = -60215;
		}
		else
		{
			_startX = -59800;
		}
		_startY = -59595;
		_startZ = -1932;
	}

	public boolean setPlayer(L2PcInstance playerInstance)
	{
		if (playerInstance == null)
		{
			return false;
		}
		removePlayer();
		_player = playerInstance;
		_player.setTeam(getId() + 1);
		//_player.broadcastUserInfo();
		_endX = _player.getX();
		_endY = _player.getY();
		_endZ = _player.getZ();
		_player.teleToLocation(_startX, _startY, _startZ);
		return true;
	}

	public void removePlayer()
	{
		if (_player != null)
		{
			_player.setTeam(0);
			//_player.broadcastUserInfo();
			_player.teleToLocation(_endX, _endY, _endZ);
			_player = null;
		}
	}

	public void cleanMe()
	{
		unSpawnPieces();
		removePlayer();
	}

	public void defeatMe()
	{
		killPieces();
		removePlayer();
	}

	public boolean containsPlayer(int playerObjectId)
	{
		return _player != null && _player.getObjectId() == playerObjectId;
	}

	public byte getId()
	{
		return _id;
	}

	public L2PcInstance getPlayer()
	{
		return _player;
	}

	public L2Spawn getPieceSpawn(int i)
	{
		return _pieceSpawns[i];
	}

	public void setPieceSpawn(L2Spawn spawn, int i)
	{
		_pieceSpawns[i] = spawn;
	}

	public int[][] getBoard()
	{
		return _board;
	}

	public void setBoard(int i, int j, int value)
	{
		_board[i][j] = value;
	}

	public void spawnPieces()
	{
		L2NpcTemplate tmpl;

		int baseX = -60165;
		int baseY = -59752;
		int baseZ = -1932;
		int costatCasella = 45;

		int downY;
		int leftX;
		int heading;

		try
		{
			int i;
			for (i = 0; i < 16; i++)
			{
				leftX = 0;
				if (getId() == 0)
				{
					downY = 0;
					if (i == 0 || i == 7)
					{
						tmpl = NpcTable.getInstance().getTemplate(44402);
					}
					else if (i == 1 || i == 6)
					{
						tmpl = NpcTable.getInstance().getTemplate(44403);
					}
					else if (i == 2 || i == 5)
					{
						tmpl = NpcTable.getInstance().getTemplate(44404);
					}
					else if (i == 3)
					{
						tmpl = NpcTable.getInstance().getTemplate(44405);
					}
					else if (i == 4)
					{
						tmpl = NpcTable.getInstance().getTemplate(44406);
					}
					else
					{
						tmpl = NpcTable.getInstance().getTemplate(44401);
						leftX = costatCasella;
						downY = costatCasella * 8;
					}
					heading = 0;
				}
				else
				{
					downY = costatCasella * 8;
					if (i == 8 || i == 15)
					{
						tmpl = NpcTable.getInstance().getTemplate(44412);
					}
					else if (i == 9 || i == 14)
					{
						tmpl = NpcTable.getInstance().getTemplate(44413);
					}
					else if (i == 10 || i == 13)
					{
						tmpl = NpcTable.getInstance().getTemplate(44414);
					}
					else if (i == 11)
					{
						tmpl = NpcTable.getInstance().getTemplate(44415);
					}
					else if (i == 12)
					{
						tmpl = NpcTable.getInstance().getTemplate(44416);
					}
					else
					{
						tmpl = NpcTable.getInstance().getTemplate(44411);
						leftX = -costatCasella;
						downY = 0;
					}
					leftX += costatCasella * 7;
					heading = 32768;
				}
				_pieceSpawns[i] = new L2Spawn(tmpl);

				_pieceSpawns[i].setX(baseX + leftX);
				_pieceSpawns[i].setY(baseY + i * costatCasella - downY);
				_pieceSpawns[i].setZ(baseZ);
				_pieceSpawns[i].setHeading(heading);

				SpawnTable.getInstance().addNewSpawn(_pieceSpawns[i], false);

				_pieceSpawns[i].stopRespawn();
				_pieceSpawns[i].doSpawn();
				//_pieceSpawns[i].getNpc().spawnMe(_pieceSpawns[i].getNpc().getX(), _pieceSpawns[i].getNpc().getY(), _pieceSpawns[i].getNpc().getZ());
				L2ChessPieceInstance piece = (L2ChessPieceInstance) _pieceSpawns[i].getNpc();
				if (piece.getType() == 6)
				{
					_king = piece;
				}
			}
			for (i = 0; i < 8; i++)
			{
				_board[i][0] = 1;
				_board[i][1] = 1;
				getEnemy().setBoard(i, 6, 2);
				getEnemy().setBoard(i, 7, 2);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void unSpawnPieces()
	{
		int i;
		for (i = 0; i < 16; i++)
		{
			if (_pieceSpawns[i] != null)
			{
				_pieceSpawns[i].getNpc().deleteMe();
				_pieceSpawns[i].getNpc().deleteMe();
				_pieceSpawns[i].getNpc().deleteMe();
				_pieceSpawns[i].stopRespawn();
				SpawnTable.getInstance().deleteSpawn(_pieceSpawns[i], true);
			}
		}
	}

	public void killPieces()
	{
		int i;
		for (i = 0; i < 16; i++)
		{
			if (_pieceSpawns[i] != null)
			{
				_pieceSpawns[i].stopRespawn();
				_pieceSpawns[i].getNpc().doDie(_pieceSpawns[i].getNpc());
				SpawnTable.getInstance().deleteSpawn(_pieceSpawns[i], true);
			}
		}
	}

	public boolean estaBuit(int i, int j)
	{
		return getPiece(i, j) == null;
	}

	public boolean hiHaAmic(int i, int j)
	{
		return getPiece(i, j) != null && getPiece(i, j).getSide().getId() == getId();
	}

	public boolean hiHaEnemic(int i, int j)
	{
		return getPiece(i, j) != null && getPiece(i, j).getSide().getId() != getId();
	}

	public boolean containsAPlayer()
	{
		return _player != null;
	}

	public ChessEventSide getEnemy()
	{
		if (_id == 0)
		{
			return ChessEvent.getSide(1);
		}
		else
		{
			return ChessEvent.getSide(0);
		}
	}

	public L2ChessPieceInstance getPiece(int x, int y)
	{
		return ChessEvent.getBoard(getId())[x][y];
	}

	public void setPiece(int x, int y, L2ChessPieceInstance piece)
	{
		ChessEvent.setBoard(x, y, piece, getId());
	}

	public L2ChessPieceInstance getKing()
	{
		return _king;
	}

	public void setCanTheKingBeKilled(boolean canTheKingBeKilled)
	{
		_canTheKingBeKilled = canTheKingBeKilled;
	}

	public boolean canTheKingBeKilled(boolean check)
	{
		if (!check)
		{
			return _canTheKingBeKilled;
		}
		_canTheKingBeKilled = false;
		for (int i = 0; i < 8; i++)
		{
			for (L2ChessPieceInstance piece : ChessEvent.getBoard(getId())[i])
			{
				if (piece != null && piece.getType() != 6 && piece.getSide().getId() != getId() &&
						piece.canKillTheKing())
				{
					_canTheKingBeKilled = true;
				}
			}
		}
		return _canTheKingBeKilled;
	}

	public boolean hasWon()
	{
		return _hasWon;
	}

	public void setHasWon(boolean hasWon)
	{
		_hasWon = hasWon;
	}
}
