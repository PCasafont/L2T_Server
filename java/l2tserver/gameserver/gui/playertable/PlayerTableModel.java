/**
 * 
 */
package l2tserver.gameserver.gui.playertable;

import javax.swing.table.AbstractTableModel;

import l2tserver.Config;
import l2tserver.gameserver.gui.ServerGui;
import l2tserver.gameserver.model.L2World;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author KenM
 *
 */
@SuppressWarnings("serial")
class PlayerTableModel extends AbstractTableModel
{
	private static final String[] columnNames =
	{
			"Id",
			"Name",
			"Level"
	};
	
	private L2PcInstance[] _players = new L2PcInstance[]{};
	
	public PlayerTableModel()
	{
	}
	
	public int getColumnCount()
	{
		return columnNames.length;
	}
	
	public int getRowCount()
	{
		return _players.length;
	}
	
	@Override
	public String getColumnName(int col)
	{
		return columnNames[col];
	}
	
	public Object getValueAt(int row, int col)
	{
		switch (col)
		{
		case 0:
			return _players[row].getObjectId();
		case 1:
			return _players[row].getName();
		case 2:
			return _players[row].getLevel();
		}
		return "";
	}
	
	public synchronized boolean updateData()
	{
		L2PcInstance[] players = new L2PcInstance[L2World.getInstance().getAllPlayersCount()];
		L2World.getInstance().getAllPlayersArray().toArray(players);
		int playerCount = 0;
		for (L2PcInstance player : players)
		{
			if (player != null)
				playerCount++;
		}
		
		ServerGui.Frame.setTitle("L2 Server [" + Config.SERVER_NAME + "] (" + playerCount + " players online)");
		if (players.length == _players.length
				&& !(players.length > 0 && players[0] == _players[0]))
			return false;
		
		_players = players;
		return true;
	}
}
