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
		setLayout(this.layout);
		this.cons.fill = GridBagConstraints.HORIZONTAL;

		this.infoPanel.setLayout(this.layout);

		this.cons.insets = new Insets(5, 5, 5, 5);
		this.cons.gridwidth = 3;
		this.cons.gridheight = 20;
		this.cons.weightx = 1;
		this.cons.weighty = 1;
		this.cons.gridx = 0;
		this.cons.gridy = 2;
		this.infoPanel.add(new JLabel(""), this.cons);
		this.infoPanel.setPreferredSize(new Dimension(235, this.infoPanel.getHeight()));

		this.cons.fill = GridBagConstraints.BOTH;
		this.cons.weightx = 1;
		this.cons.weighty = 1;
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.listPanel, this.infoPanel);
		splitPane.setResizeWeight(0.3);
		splitPane.setDividerLocation(535);
		add(splitPane, this.cons);
	}
}
