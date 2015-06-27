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
package l2server.gameserver.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import l2server.gameserver.gui.playertable.PlayerTablePane;

/**
 * @author Pere
 */
@SuppressWarnings("serial")
public class AdminTab extends JPanel
{
	private GridBagConstraints _cons = new GridBagConstraints();
	private GridBagLayout _layout = new GridBagLayout();
	private JPanel _leftPanel = new JPanel();
	private JTabbedPane _rightPanel = new JTabbedPane();
	
	public AdminTab()
	{
		setLayout(_layout);
		_cons.fill = GridBagConstraints.HORIZONTAL;
		
		_leftPanel.setLayout(_layout);
		
		_cons.insets = new Insets(5, 5, 5, 5);
		_cons.gridwidth = 3;
		_cons.gridheight = 20;
		_cons.weightx = 1;
		_cons.weighty = 1;
		_cons.gridx = 0;
		_cons.gridy = 2;
		_leftPanel.add(new JLabel(""), _cons);
		_leftPanel.setPreferredSize(new Dimension(235, _leftPanel.getHeight()));

		_cons.fill = GridBagConstraints.BOTH;
		_cons.weightx = 1;
		_cons.weighty = 1;
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, _leftPanel, _rightPanel);
		splitPane.setResizeWeight(0.3);
		splitPane.setDividerLocation(235);
		add(splitPane, _cons);
		
		_rightPanel.addTab("Players", new PlayerTablePane());
	}
}
