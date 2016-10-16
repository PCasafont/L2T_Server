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

import l2server.gameserver.gui.playertable.PlayerTablePane;

import javax.swing.*;
import java.awt.*;

/**
 * @author Pere
 */
public class AdminTab extends JPanel
{
	private static final long serialVersionUID = 1L;
	private GridBagConstraints cons = new GridBagConstraints();
	private GridBagLayout layout = new GridBagLayout();
	private JPanel listPanel = new PlayerTablePane();
	private JPanel infoPanel = new JPanel();

	public AdminTab()
	{
		setLayout(layout);
		cons.fill = GridBagConstraints.HORIZONTAL;

		infoPanel.setLayout(layout);

		cons.insets = new Insets(5, 5, 5, 5);
		cons.gridwidth = 3;
		cons.gridheight = 20;
		cons.weightx = 1;
		cons.weighty = 1;
		cons.gridx = 0;
		cons.gridy = 2;
		infoPanel.add(new JLabel(""), cons);
		infoPanel.setPreferredSize(new Dimension(235, infoPanel.getHeight()));

		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1;
		cons.weighty = 1;
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, infoPanel);
		splitPane.setResizeWeight(0.3);
		splitPane.setDividerLocation(535);
		add(splitPane, cons);
	}
}
