/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gsregistering;

import l2server.loginserver.GameServerTable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author KenM
 */
public class RegisterDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	private ResourceBundle bundle;
	@SuppressWarnings("rawtypes")
	private final JComboBox combo;
	private final GUserInterface owner;

	@SuppressWarnings({"rawtypes", "unchecked"})
	public RegisterDialog(final GUserInterface owner) {
		super(owner.getFrame(), true);
		this.owner = owner;
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		bundle = owner.getBundle();
		setTitle(bundle.getString("registerGS"));
		setResizable(false);
		setLayout(new GridBagLayout());
		GridBagConstraints cons = new GridBagConstraints();
		cons.weightx = 0.5;
		cons.weighty = 0.5;
		cons.gridx = 0;
		cons.gridy = 0;
		cons.fill = GridBagConstraints.BOTH;

		final JLabel label = new JLabel(bundle.getString("serverName"));
		this.add(label, cons);

		combo = new JComboBox();
		combo.setEditable(false);
		for (Map.Entry<Integer, String> entry : GameServerTable.getInstance().getServerNames().entrySet()) {
			if (!GameServerTable.getInstance().hasRegisteredGameServerOnId(entry.getKey())) {
				combo.addItem(new ComboServer(entry.getKey(), entry.getValue()));
			}
		}
		cons.gridx = 1;
		cons.gridy = 0;
		this.add(combo, cons);

		cons.gridx = 0;
		cons.gridy = 1;
		cons.gridwidth = 2;
		JTextPane textPane = new JTextPane();
		textPane.setText(bundle.getString("saveHexId"));
		textPane.setEditable(false);
		textPane.setBackground(label.getBackground());
		this.add(textPane, cons);
		cons.gridwidth = 1;

		JButton btnSave = new JButton(bundle.getString("save"));
		btnSave.setActionCommand("save");
		btnSave.addActionListener(this);
		cons.gridx = 0;
		cons.gridy = 2;
		this.add(btnSave, cons);

		JButton btnCancel = new JButton(bundle.getString("cancel"));
		btnCancel.setActionCommand("cancel");
		btnCancel.addActionListener(this);
		cons.gridx = 1;
		cons.gridy = 2;
		this.add(btnCancel, cons);

		final double leftSize = Math.max(label.getPreferredSize().getWidth(), btnSave.getPreferredSize().getWidth());
		final double rightSize = Math.max(combo.getPreferredSize().getWidth(), btnCancel.getPreferredSize().getWidth());

		final double height =
				combo.getPreferredSize().getHeight() + 4 * textPane.getPreferredSize().getHeight() + btnSave.getPreferredSize().getHeight();
		this.setSize((int) (leftSize + rightSize + 30), (int) (height + 20));

		setLocationRelativeTo(owner.getFrame());
	}

	class ComboServer {
		private final int id;
		private final String name;

		public ComboServer(int id, String name) {
			this.id = id;
			this.name = name;
		}

		/**
		 * @return Returns the id.
		 */
		public int getId() {
			return id;
		}

		/**
		 * @return Returns the name.
		 */
		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return getName();
		}
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("save")) {
			ComboServer server = (ComboServer) combo.getSelectedItem();
			int gsId = server.getId();

			JFileChooser fc = new JFileChooser();
			//fc.setS
			fc.setDialogTitle(bundle.getString("hexidDest"));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setFileFilter(new FileFilter() {

				@Override
				public boolean accept(File f) {
					return f.isDirectory();
				}

				@Override
				public String getDescription() {
					return null;
				}
			});
			fc.showOpenDialog(this);

			try {
				GUserInterface.registerGameServer(gsId, fc.getSelectedFile().getAbsolutePath());
				owner.refreshAsync();
				setVisible(false);
			} catch (IOException e1) {
				owner.showError(bundle.getString("ioErrorRegister"), e1);
			}
		} else if (cmd.equals("cancel")) {
			setVisible(false);
		}
	}
}
