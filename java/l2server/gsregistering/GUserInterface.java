package l2server.gsregistering;

import lombok.Getter;
import l2server.images.ImagesTable;
import l2server.loginserver.GameServerTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * @author KenM
 */
public class GUserInterface extends BaseGameServerRegister implements ActionListener
{

	/**
	 *
	 */
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	@Getter private final JFrame frame;

	JTableModel dtm;
	JProgressBar progressBar;

	public JTable gsTable;

	public GUserInterface(ResourceBundle bundle)
	{
		super(bundle);

		frame = new JFrame();
		getFrame().setTitle(getBundle().getString("toolName"));
		getFrame().setSize(600, 400);
		getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getFrame().setLayout(new GridBagLayout());
		GridBagConstraints cons = new GridBagConstraints();

		JMenuBar menubar = new JMenuBar();
		getFrame().setJMenuBar(menubar);

		JMenu fileMenu = new JMenu(getBundle().getString("fileMenu"));

		JMenuItem exitItem = new JMenuItem(getBundle().getString("exitItem"));
		exitItem.addActionListener(this);
		exitItem.setActionCommand("exit");

		fileMenu.add(exitItem);

		JMenu helpMenu = new JMenu(getBundle().getString("helpMenu"));

		JMenuItem aboutItem = new JMenuItem(getBundle().getString("aboutItem"));
		aboutItem.addActionListener(this);
		aboutItem.setActionCommand("about");

		helpMenu.add(aboutItem);

		menubar.add(fileMenu);
		menubar.add(helpMenu);

		JButton btnRegister = new JButton(getBundle().getString("btnRegister"), ImagesTable.getImage("add.png"));
		btnRegister.addActionListener(this);
		btnRegister.setActionCommand("register");
		getFrame().add(btnRegister, cons);

		cons.gridx = 1;
		cons.anchor = GridBagConstraints.LINE_END;
		JButton btnRemoveAll = new JButton(getBundle().getString("btnRemoveAll"), ImagesTable.getImage("cross.png"));
		btnRemoveAll.addActionListener(this);
		btnRemoveAll.setActionCommand("removeAll");
		getFrame().add(btnRemoveAll, cons);

		String name = getBundle().getString("gsName");
		String action = getBundle().getString("gsAction");

		dtm = new JTableModel(new Object[]{"ID", name, action});
		gsTable = new JTable(dtm);
		gsTable.addMouseListener(new JTableButtonMouseListener(gsTable));

		gsTable.getColumnModel().getColumn(0).setMaxWidth(30);

		TableColumn actionCollumn = gsTable.getColumnModel().getColumn(2);
		actionCollumn.setCellRenderer(new ButtonCellRenderer());

		cons.fill = GridBagConstraints.BOTH;
		cons.gridx = 0;
		cons.gridy = 1;
		cons.weighty = 1.0;
		cons.weightx = 1.0;
		cons.gridwidth = 2;
		JLayeredPane layer = new JLayeredPane();
		layer.setLayout(new BoxLayout(layer, BoxLayout.PAGE_AXIS));
		layer.add(new JScrollPane(gsTable), 0);
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);
		layer.add(progressBar, BorderLayout.CENTER, 1);
		//layer.setV
		getFrame().add(layer, cons);

		// maximize, doesn't seem really needed
		//getFrame().setExtendedState(JFrame.MAXIMIZED_BOTH);
		/*
        // Work-around JVM maximize issue on linux
		String osName = System.getProperty("os.name");
		if (osName.equals("Linux"))
		{
		   Toolkit toolkit = Toolkit.getDefaultToolkit();
		   Dimension screenSize = toolkit.getScreenSize();
		   getFrame().setSize(screenSize);
		}
		 */
		refreshAsync();
	}

	public void refreshAsync()
	{
		Runnable r = GUserInterface.this::refreshServers;
		Thread t = new Thread(r, "LoaderThread");
		t.start();
	}

	@Override
	public void load()
	{

		SwingUtilities.invokeLater(() -> progressBar.setVisible(true));

		super.load();

		SwingUtilities.invokeLater(() -> progressBar.setVisible(false));
	}

	/**
	 */
	@Override
	public void showError(String msg, Throwable t)
	{
		String title;
		if (getBundle() != null)
		{
			title = getBundle().getString("error");
			msg += '\n' + getBundle().getString("reason") + ' ' + t.getLocalizedMessage();
		}
		else
		{
			title = "Error";
			msg += "\nCause: " + t.getLocalizedMessage();
		}
		JOptionPane.showMessageDialog(getFrame(), msg, title, JOptionPane.ERROR_MESSAGE);
	}

	protected void refreshServers()
	{
		if (!isLoaded())
		{
			load();
		}

		// load succeeded?
		if (isLoaded())
		{
			SwingUtilities.invokeLater(() ->
			{
				int size = GameServerTable.getInstance().getServerNames().size();
				if (size == 0)
				{
					String title = getBundle().getString("error");
					String msg = getBundle().getString("noServerNames");
					JOptionPane.showMessageDialog(getFrame(), msg, title, JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}
				// reset
				dtm.setRowCount(0);

				for (final int id : GameServerTable.getInstance().getRegisteredGameServers().keySet())
				{
					String name = GameServerTable.getInstance().getServerNameById(id);
					JButton button = new JButton(getBundle().getString("btnRemove"), ImagesTable.getImage("cross.png"));
					button.addActionListener(e ->
					{
						String sid = String.valueOf(id);
						String sname = GameServerTable.getInstance().getServerNameById(id);

						int choice = JOptionPane.showConfirmDialog(getFrame(),
								getBundle().getString("confirmRemoveText").replace("%d", sid).replace("%s", sname),
								getBundle().getString("confirmRemoveTitle"), JOptionPane.YES_NO_OPTION);
						if (choice == JOptionPane.YES_OPTION)
						{
							try
							{
								BaseGameServerRegister.unregisterGameServer(id);
								refreshAsync();
							}
							catch (SQLException e1)
							{
								showError(getBundle().getString("errorUnregister"), e1);
							}
						}
					});
					dtm.addRow(new Object[]{id, name, button});
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();

		switch (cmd)
		{
			case "register":
				RegisterDialog rd = new RegisterDialog(this);
				rd.setVisible(true);
				break;
			case "exit":
				System.exit(0);
			case "about":
				JOptionPane.showMessageDialog(getFrame(),
						getBundle().getString("credits") + "\nhttp://www.l2jserver.com\n\n" +
								getBundle().getString("icons") + "\n\n" + getBundle().getString("language") + '\n' +
								getBundle().getString("translation"), getBundle().getString("aboutItem"),
						JOptionPane.INFORMATION_MESSAGE, ImagesTable.getImage("l2jserverlogo.png"));
				break;
			case "removeAll":
				int choice = JOptionPane.showConfirmDialog(getFrame(), getBundle().getString("confirmRemoveAllText"),
						getBundle().getString("confirmRemoveTitle"), JOptionPane.YES_NO_OPTION);
				if (choice == JOptionPane.YES_OPTION)
				{
					try
					{
						BaseGameServerRegister.unregisterAllGameServers();
						refreshAsync();
					}
					catch (SQLException e1)
					{
						showError(getBundle().getString("errorUnregister"), e1);
					}
				}
				break;
		}
	}

	class ButtonCellRenderer implements TableCellRenderer
	{

		/* (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			return (Component) value;
		}
	}

	/**
	 * Forward mouse-events from table to buttons inside.
	 * Buttons animate properly.
	 *
	 * @author KenM
	 */
	class JTableButtonMouseListener implements MouseListener
	{
		private final JTable table;

		public JTableButtonMouseListener(JTable table)
		{
			this.table = table;
		}

		private void forwardEvent(MouseEvent e)
		{
			TableColumnModel columnModel = table.getColumnModel();
			int column = columnModel.getColumnIndexAtX(e.getX());
			int row = e.getY() / table.getRowHeight();
			Object value;

			if (row >= table.getRowCount() || row < 0 || column >= table.getColumnCount() || column < 0)
			{
				return;
			}

			value = table.getValueAt(row, column);

			if (value instanceof JButton)
			{
				final JButton b = (JButton) value;
				if (e.getID() == MouseEvent.MOUSE_PRESSED)
				{
					b.getModel().setPressed(true);
					b.getModel().setArmed(true);
					table.repaint();
				}
				else if (e.getID() == MouseEvent.MOUSE_RELEASED)
				{
					b.doClick();
				}
			}
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
			forwardEvent(e);
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			forwardEvent(e);
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			forwardEvent(e);
		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
			forwardEvent(e);
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			forwardEvent(e);
		}
	}

	class JTableModel extends DefaultTableModel
	{
		private static final long serialVersionUID = 1L;

		public JTableModel(Object[] columnNames)
		{
			super(columnNames, 0);
		}

		@Override
		public boolean isCellEditable(int row, int column)
		{
			return false;
		}

		@Override
		public Class<?> getColumnClass(int column)
		{
			return getValueAt(0, column).getClass();
		}
	}
}
