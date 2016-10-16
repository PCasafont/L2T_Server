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
import lombok.Getter;

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

	private GridBagLayout layout = new GridBagLayout();

	//Npc Table
	@Getter private PlayerTableModel playerTableModel;
	@Getter private JTable playerTable;

	private int currentSelectedPlayer = -1;

	public PlayerTablePane()
	{
		setLayout(layout);

		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(5, 5, 5, 5);

		JPanel smallPane = new JPanel();
		smallPane.setLayout(layout);

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

		playerTableModel = new PlayerTableModel();
		playerTable = new JTable(playerTableModel);
		playerTable.addMouseListener(new PlayerTableMouseListener(this));
		playerTable.setDefaultRenderer(Object.class, new PlayerTableRenderer(playerTableModel));
		playerTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		playerTable.getSelectionModel().addListSelectionListener(new PlayerSelectionListener());
		playerTable.getColumnModel().getColumn(0).setMaxWidth(100);
		JScrollPane scrollPane = new JScrollPane(playerTable);
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
			if (playerTableModel.updateData())
			{
				getPlayerTable().updateUI();
			}
		});
	}

	public void updateCurrentPlayer()
	{
		updateCurrentPlayer(false);
	}

	public void updateCurrentPlayer(boolean forced)
	{
		if (!forced && currentSelectedPlayer == playerTable.getSelectedRow())
		{
		}
		else
		{
			currentSelectedPlayer = playerTable.getSelectedRow();
		}

		//Player player = World.getInstance().getPlayer((Integer)_playerTableModel.getValueAt(this.playerTable.getSelectedRow(), 0));
	}

	public void setTableSelectByMouseEvent(MouseEvent e)
	{
		int rowNumber = playerTable.rowAtPoint(e.getPoint());
		playerTable.getSelectionModel().setSelectionInterval(rowNumber, rowNumber);
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
