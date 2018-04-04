/*
 
 */

package l2server.gameserver.gui.playertable;

import l2server.Config;
import l2server.gameserver.gui.ServerGui;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.instance.Player;

import javax.swing.table.AbstractTableModel;

/**
 * @author KenM
 */
class PlayerTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	
	private static final String[] columnNames = {"Id", "Name", "Level"};
	
	private Player[] players = new Player[]{};
	
	public PlayerTableModel() {
	}
	
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}
	
	@Override
	public int getRowCount() {
		return players.length;
	}
	
	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		switch (col) {
			case 0:
				return players[row].getObjectId();
			case 1:
				return players[row].getName();
			case 2:
				return players[row].getLevel();
		}
		return "";
	}
	
	public synchronized boolean updateData() {
		Player[] players = new Player[World.getInstance().getAllPlayersCount()];
		World.getInstance().getAllPlayersArray().toArray(players);
		int playerCount = 0;
		int shopCount = 0;
		for (Player player : players) {
			if (player != null && player.isOnline()) {
				if (player.getClient() == null || player.getClient().isDetached()) {
					shopCount++;
				} else {
					playerCount++;
				}
			}
		}
		
		ServerGui.getMainFrame().setTitle(
				"L2 Server [" + Config.SERVER_NAME + "] | Players online: " + playerCount + " | Offline shops: " + shopCount + " | Total: " +
						(playerCount + shopCount));
		if (players.length == players.length && !(players.length > 0 && players[0] == players[0])) {
			return false;
		}
		
		this.players = players;
		return true;
	}
}
