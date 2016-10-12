/*

 */

package l2server.gameserver.gui.playertable;

import l2server.Config;
import l2server.gameserver.gui.ServerGui;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import javax.swing.table.AbstractTableModel;

/**
 * @author KenM
 */
class PlayerTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;

	private static final String[] columnNames = {"Id", "Name", "Level"};

	private L2PcInstance[] _players = new L2PcInstance[]{};

	public PlayerTableModel()
	{
	}

	@Override
	public int getColumnCount()
	{
		return columnNames.length;
	}

	@Override
	public int getRowCount()
	{
		return _players.length;
	}

	@Override
	public String getColumnName(int col)
	{
		return columnNames[col];
	}

	@Override
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
		int shopCount = 0;
		for (L2PcInstance player : players)
		{
			if (player != null && player.isOnline())
			{
				if (player.getClient() == null || player.getClient().isDetached())
				{
					shopCount++;
				}
				else
				{
					playerCount++;
				}
			}
		}

		ServerGui.Frame.setTitle(
				"L2 Server [" + Config.SERVER_NAME + "] | Players online: " + playerCount + " | Offline shops: " +
						shopCount + " | Total: " + (playerCount + shopCount));
		if (players.length == _players.length && !(players.length > 0 && players[0] == _players[0]))
		{
			return false;
		}

		_players = players;
		return true;
	}
}
