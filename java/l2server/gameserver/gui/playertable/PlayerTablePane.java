/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */

package l2server.gameserver.gui.playertable;

import l2server.gameserver.ThreadPoolManager;
import l2server.log.Log;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

/**
 * @author KenM
 */
public class PlayerTablePane extends JPanel
{
	private static final long serialVersionUID = 1L;

	public class ButtonListeners implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent evt)
		{
			//String cmd = evt.getActionCommand();
		}
	}

	private GridBagLayout _layout = new GridBagLayout();

	//Npc Table
	private PlayerTableModel _playerTableModel;
	private JTable _playerTable;

	private int _currentSelectedPlayer = -1;

	public PlayerTablePane()
	{
		setLayout(_layout);

		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(5, 5, 5, 5);

		JPanel smallPane = new JPanel();
		smallPane.setLayout(_layout);

		/*ButtonListeners buttonListeners = new ButtonListeners();

		JButton analyze = new JButton("Check Targeting");
		analyze.addActionListener(buttonListeners);
		analyze.setActionCommand("CheckTargeting");
		smallPane.add(analyze, cons);

		cons.weightx = 0.5;
		cons.weighty = 0.1;
		cons.gridx = 0;
		cons.gridy = 0;
		cons.gridheight = 1;
		cons.anchor = GridBagConstraints.WEST;
		cons.fill = GridBagConstraints.HORIZONTAL;
		add(smallPane, cons);*/

		_playerTableModel = new PlayerTableModel();
		_playerTable = new JTable(_playerTableModel);
		_playerTable.addMouseListener(new PlayerTableMouseListener(this));
		_playerTable.setDefaultRenderer(Object.class, new PlayerTableRenderer(_playerTableModel));
		_playerTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_playerTable.getSelectionModel().addListSelectionListener(new PlayerSelectionListener());
		_playerTable.getColumnModel().getColumn(0).setMaxWidth(100);
		JScrollPane scrollPane = new JScrollPane(_playerTable);
		scrollPane.setMinimumSize(new Dimension(250, 500));
		cons.weightx = 0.5;
		cons.weighty = 0.95;
		cons.gridx = 0;
		cons.gridy = 0;
		cons.gridheight = 1;
		cons.fill = GridBagConstraints.BOTH;
		add(scrollPane, cons);

		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this::updateTable, 10000, 1000);
	}

	public void setSelectedPlayer(int startIndex, int endIndex)
	{
		getPlayerTable().setAutoscrolls(true);
		getPlayerTable().getSelectionModel().setSelectionInterval(startIndex, endIndex);
		getPlayerTable().scrollRectToVisible(getPlayerTable().getCellRect(startIndex, 0, true));
	}

	public void updateTable()
	{
		SwingUtilities.invokeLater(() ->
		{
			if (_playerTableModel.updateData())
			{
				getPlayerTable().updateUI();
			}
		});
	}

	public JTable getPlayerTable()
	{
		return _playerTable;
	}

	public PlayerTableModel getPlayerTableModel()
	{
		return _playerTableModel;
	}

	public void updateCurrentPlayer()
	{
		updateCurrentPlayer(false);
	}

	public void updateCurrentPlayer(boolean forced)
	{
		if (!forced && _currentSelectedPlayer == _playerTable.getSelectedRow())
		{
		}
		else
		{
			_currentSelectedPlayer = _playerTable.getSelectedRow();
		}

		//Player player = World.getInstance().getPlayer((Integer)_playerTableModel.getValueAt(_playerTable.getSelectedRow(), 0));
	}

	public void setTableSelectByMouseEvent(MouseEvent e)
	{
		int rowNumber = _playerTable.rowAtPoint(e.getPoint());
		_playerTable.getSelectionModel().setSelectionInterval(rowNumber, rowNumber);
	}

	public class PlayerSelectionListener implements ListSelectionListener
	{
		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			PlayerTablePane view = PlayerTablePane.this;
			// If cell selection is enabled, both row and column change events are fired
			if (e.getSource() == view.getPlayerTable().getSelectionModel())
			{
				view.updateCurrentPlayer();
			}
		}
	}

	@Override
	public void finalize() throws Throwable
	{
		super.finalize();
		Log.info("Finalized: " + getClass().getSimpleName());
	}
}
