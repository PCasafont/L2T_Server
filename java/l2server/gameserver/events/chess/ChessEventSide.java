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
	private byte id;

	private int startX;
	private int startY;
	private int startZ;

	private L2PcInstance player;

	private int endX;
	private int endY;
	private int endZ;

	private int[][] board;

	private L2Spawn[] pieceSpawns;

	private L2ChessPieceInstance king;

	private boolean canTheKingBeKilled = false;

	private boolean hasWon = false;

	public ChessEventSide(byte id)
	{
		this.id = id;
		this.pieceSpawns = new L2Spawn[16];
		this.board = new int[8][8];
		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 8; j++)
			{
				this.board[i][j] = 0;
			}
		}
		if (id == 0)
		{
			this.startX = -60215;
		}
		else
		{
			this.startX = -59800;
		}
		this.startY = -59595;
		this.startZ = -1932;
	}

	public boolean setPlayer(L2PcInstance playerInstance)
	{
		if (playerInstance == null)
		{
			return false;
		}
		removePlayer();
		this.player = playerInstance;
		this.player.setTeam(getId() + 1);
		//_player.broadcastUserInfo();
		this.endX = this.player.getX();
		this.endY = this.player.getY();
		this.endZ = this.player.getZ();
		this.player.teleToLocation(this.startX, this.startY, this.startZ);
		return true;
	}

	public void removePlayer()
	{
		if (this.player != null)
		{
			this.player.setTeam(0);
			//_player.broadcastUserInfo();
			this.player.teleToLocation(this.endX, this.endY, this.endZ);
			this.player = null;
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
		return this.player != null && this.player.getObjectId() == playerObjectId;
	}

	public byte getId()
	{
		return this.id;
	}

	public L2PcInstance getPlayer()
	{
		return this.player;
	}

	public L2Spawn getPieceSpawn(int i)
	{
		return this.pieceSpawns[i];
	}

	public void setPieceSpawn(L2Spawn spawn, int i)
	{
		this.pieceSpawns[i] = spawn;
	}

	public int[][] getBoard()
	{
		return this.board;
	}

	public void setBoard(int i, int j, int value)
	{
		this.board[i][j] = value;
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
				this.pieceSpawns[i] = new L2Spawn(tmpl);

				this.pieceSpawns[i].setX(baseX + leftX);
				this.pieceSpawns[i].setY(baseY + i * costatCasella - downY);
				this.pieceSpawns[i].setZ(baseZ);
				this.pieceSpawns[i].setHeading(heading);

				SpawnTable.getInstance().addNewSpawn(this.pieceSpawns[i], false);

				this.pieceSpawns[i].stopRespawn();
				this.pieceSpawns[i].doSpawn();
				//_pieceSpawns[i].getNpc().spawnMe(this.pieceSpawns[i].getNpc().getX(), this.pieceSpawns[i].getNpc().getY(), this.pieceSpawns[i].getNpc().getZ());
				L2ChessPieceInstance piece = (L2ChessPieceInstance) this.pieceSpawns[i].getNpc();
				if (piece.getType() == 6)
				{
					this.king = piece;
				}
			}
			for (i = 0; i < 8; i++)
			{
				this.board[i][0] = 1;
				this.board[i][1] = 1;
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
			if (this.pieceSpawns[i] != null)
			{
				this.pieceSpawns[i].getNpc().deleteMe();
				this.pieceSpawns[i].getNpc().deleteMe();
				this.pieceSpawns[i].getNpc().deleteMe();
				this.pieceSpawns[i].stopRespawn();
				SpawnTable.getInstance().deleteSpawn(this.pieceSpawns[i], true);
			}
		}
	}

	public void killPieces()
	{
		int i;
		for (i = 0; i < 16; i++)
		{
			if (this.pieceSpawns[i] != null)
			{
				this.pieceSpawns[i].stopRespawn();
				this.pieceSpawns[i].getNpc().doDie(this.pieceSpawns[i].getNpc());
				SpawnTable.getInstance().deleteSpawn(this.pieceSpawns[i], true);
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
		return this.player != null;
	}

	public ChessEventSide getEnemy()
	{
		if (this.id == 0)
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
		return this.king;
	}

	public void setCanTheKingBeKilled(boolean canTheKingBeKilled)
	{
		this.canTheKingBeKilled = canTheKingBeKilled;
	}

	public boolean canTheKingBeKilled(boolean check)
	{
		if (!check)
		{
			return this.canTheKingBeKilled;
		}
		this.canTheKingBeKilled = false;
		for (int i = 0; i < 8; i++)
		{
			for (L2ChessPieceInstance piece : ChessEvent.getBoard(getId())[i])
			{
				if (piece != null && piece.getType() != 6 && piece.getSide().getId() != getId() &&
						piece.canKillTheKing())
				{
					this.canTheKingBeKilled = true;
				}
			}
		}
		return this.canTheKingBeKilled;
	}

	public boolean hasWon()
	{
		return this.hasWon;
	}

	public void setHasWon(boolean hasWon)
	{
		this.hasWon = hasWon;
	}
}
