package l2server.gameserver.events.chess;

import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.instance.ChessPieceInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.chars.NpcTemplate;

/**
 * @author Pere
 */
public class ChessEventSide {
	private byte id;

	private int startX;
	private int startY;
	private int startZ;

	private Player player;

	private int endX;
	private int endY;
	private int endZ;

	private int[][] board;

	private L2Spawn[] pieceSpawns;

	private ChessPieceInstance king;

	private boolean canTheKingBeKilled = false;

	private boolean hasWon = false;

	public ChessEventSide(byte id) {
		this.id = id;
		pieceSpawns = new L2Spawn[16];
		board = new int[8][8];
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				board[i][j] = 0;
			}
		}
		if (id == 0) {
			startX = -60215;
		} else {
			startX = -59800;
		}
		startY = -59595;
		startZ = -1932;
	}

	public boolean setPlayer(Player playerInstance) {
		if (playerInstance == null) {
			return false;
		}
		removePlayer();
		player = playerInstance;
		player.setTeam(getId() + 1);
		//player.broadcastUserInfo();
		endX = player.getX();
		endY = player.getY();
		endZ = player.getZ();
		player.teleToLocation(startX, startY, startZ);
		return true;
	}

	public void removePlayer() {
		if (player != null) {
			player.setTeam(0);
			//player.broadcastUserInfo();
			player.teleToLocation(endX, endY, endZ);
			player = null;
		}
	}

	public void cleanMe() {
		unSpawnPieces();
		removePlayer();
	}

	public void defeatMe() {
		killPieces();
		removePlayer();
	}

	public boolean containsPlayer(int playerObjectId) {
		return player != null && player.getObjectId() == playerObjectId;
	}

	public byte getId() {
		return id;
	}

	public Player getPlayer() {
		return player;
	}

	public L2Spawn getPieceSpawn(int i) {
		return pieceSpawns[i];
	}

	public void setPieceSpawn(L2Spawn spawn, int i) {
		pieceSpawns[i] = spawn;
	}

	public int[][] getBoard() {
		return board;
	}

	public void setBoard(int i, int j, int value) {
		board[i][j] = value;
	}

	public void spawnPieces() {
		NpcTemplate tmpl;

		int baseX = -60165;
		int baseY = -59752;
		int baseZ = -1932;
		int costatCasella = 45;

		int downY;
		int leftX;
		int heading;

		try {
			int i;
			for (i = 0; i < 16; i++) {
				leftX = 0;
				if (getId() == 0) {
					downY = 0;
					if (i == 0 || i == 7) {
						tmpl = NpcTable.getInstance().getTemplate(44402);
					} else if (i == 1 || i == 6) {
						tmpl = NpcTable.getInstance().getTemplate(44403);
					} else if (i == 2 || i == 5) {
						tmpl = NpcTable.getInstance().getTemplate(44404);
					} else if (i == 3) {
						tmpl = NpcTable.getInstance().getTemplate(44405);
					} else if (i == 4) {
						tmpl = NpcTable.getInstance().getTemplate(44406);
					} else {
						tmpl = NpcTable.getInstance().getTemplate(44401);
						leftX = costatCasella;
						downY = costatCasella * 8;
					}
					heading = 0;
				} else {
					downY = costatCasella * 8;
					if (i == 8 || i == 15) {
						tmpl = NpcTable.getInstance().getTemplate(44412);
					} else if (i == 9 || i == 14) {
						tmpl = NpcTable.getInstance().getTemplate(44413);
					} else if (i == 10 || i == 13) {
						tmpl = NpcTable.getInstance().getTemplate(44414);
					} else if (i == 11) {
						tmpl = NpcTable.getInstance().getTemplate(44415);
					} else if (i == 12) {
						tmpl = NpcTable.getInstance().getTemplate(44416);
					} else {
						tmpl = NpcTable.getInstance().getTemplate(44411);
						leftX = -costatCasella;
						downY = 0;
					}
					leftX += costatCasella * 7;
					heading = 32768;
				}
				pieceSpawns[i] = new L2Spawn(tmpl);

				pieceSpawns[i].setX(baseX + leftX);
				pieceSpawns[i].setY(baseY + i * costatCasella - downY);
				pieceSpawns[i].setZ(baseZ);
				pieceSpawns[i].setHeading(heading);

				SpawnTable.getInstance().addNewSpawn(pieceSpawns[i], false);

				pieceSpawns[i].stopRespawn();
				pieceSpawns[i].doSpawn();
				//pieceSpawns[i].getNpc().spawnMe(pieceSpawns[i].getNpc().getX(), pieceSpawns[i].getNpc().getY(), pieceSpawns[i].getNpc().getZ());
				ChessPieceInstance piece = (ChessPieceInstance) pieceSpawns[i].getNpc();
				if (piece.getType() == 6) {
					king = piece;
				}
			}
			for (i = 0; i < 8; i++) {
				board[i][0] = 1;
				board[i][1] = 1;
				getEnemy().setBoard(i, 6, 2);
				getEnemy().setBoard(i, 7, 2);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void unSpawnPieces() {
		int i;
		for (i = 0; i < 16; i++) {
			if (pieceSpawns[i] != null) {
				pieceSpawns[i].getNpc().deleteMe();
				pieceSpawns[i].getNpc().deleteMe();
				pieceSpawns[i].getNpc().deleteMe();
				pieceSpawns[i].stopRespawn();
				SpawnTable.getInstance().deleteSpawn(pieceSpawns[i], true);
			}
		}
	}

	public void killPieces() {
		int i;
		for (i = 0; i < 16; i++) {
			if (pieceSpawns[i] != null) {
				pieceSpawns[i].stopRespawn();
				pieceSpawns[i].getNpc().doDie(pieceSpawns[i].getNpc());
				SpawnTable.getInstance().deleteSpawn(pieceSpawns[i], true);
			}
		}
	}

	public boolean estaBuit(int i, int j) {
		return getPiece(i, j) == null;
	}

	public boolean hiHaAmic(int i, int j) {
		return getPiece(i, j) != null && getPiece(i, j).getSide().getId() == getId();
	}

	public boolean hiHaEnemic(int i, int j) {
		return getPiece(i, j) != null && getPiece(i, j).getSide().getId() != getId();
	}

	public boolean containsAPlayer() {
		return player != null;
	}

	public ChessEventSide getEnemy() {
		if (id == 0) {
			return ChessEvent.getSide(1);
		} else {
			return ChessEvent.getSide(0);
		}
	}

	public ChessPieceInstance getPiece(int x, int y) {
		return ChessEvent.getBoard(getId())[x][y];
	}

	public void setPiece(int x, int y, ChessPieceInstance piece) {
		ChessEvent.setBoard(x, y, piece, getId());
	}

	public ChessPieceInstance getKing() {
		return king;
	}

	public void setCanTheKingBeKilled(boolean canTheKingBeKilled) {
		this.canTheKingBeKilled = canTheKingBeKilled;
	}

	public boolean canTheKingBeKilled(boolean check) {
		if (!check) {
			return canTheKingBeKilled;
		}
		canTheKingBeKilled = false;
		for (int i = 0; i < 8; i++) {
			for (ChessPieceInstance piece : ChessEvent.getBoard(getId())[i]) {
				if (piece != null && piece.getType() != 6 && piece.getSide().getId() != getId() && piece.canKillTheKing()) {
					canTheKingBeKilled = true;
				}
			}
		}
		return canTheKingBeKilled;
	}

	public boolean hasWon() {
		return hasWon;
	}

	public void setHasWon(boolean hasWon) {
		this.hasWon = hasWon;
	}
}
