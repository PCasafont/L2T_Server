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

import l2server.gameserver.Announcements;
import l2server.gameserver.gui.playertable.PlayerTablePane;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

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

		JTextArea  talkadmin = new JTextArea();
		JButton bouton = new JButton("Send");
		bouton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Announcements.getInstance().announceToAll(talkadmin.getText());

			}
		});
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


		infoPanel.add(bouton, cons);




		infoPanel.setPreferredSize(new Dimension(235, infoPanel.getHeight()));

		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1;
		cons.weighty = 1;

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, infoPanel);
		splitPane.setResizeWeight(0.3);
		splitPane.setDividerLocation(535);
		add(splitPane, cons);
		listPanel.add(talkadmin, cons);



	}


}
