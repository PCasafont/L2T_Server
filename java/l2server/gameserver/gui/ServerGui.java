package l2server.gameserver.gui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Pere
 */
public class ServerGui
{
	public static JFrame Frame;

	//private JMenuBar _menuBar = new JMenuBar();

	//private JMenu _fileMenu = new JMenu("File");
	//private JMenu _helpMenu = new JMenu("Help");

	//private ActionListener _menuListener = new MenuActionListener();

	private static JTabbedPane _tabPane = new JTabbedPane();

	private static ConsoleTab _consoleTab;

	private static AdminTab _adminTab;

	public void init()
	{
		Frame = new JFrame("L2 Server");

		//Menu Bar Items
		//File Menu
		/*JMenuItem itemExit = new JMenuItem("Exit");
        itemExit.setActionCommand("Exit");
		itemExit.addActionListener(_menuListener);

		_fileMenu.add(itemExit);

		//Help
		JMenuItem itemAbout = new JMenuItem("About");
		itemAbout.setActionCommand("About");
		itemAbout.addActionListener(_menuListener);
		_helpMenu.add(itemAbout);

		_menuBar.add(_fileMenu);
		_menuBar.add(_helpMenu);
		_frame.setJMenuBar(_menuBar);*/

		// Console Tab
		_consoleTab = new ConsoleTab(true);
		_adminTab = new AdminTab();

		Frame.setLayout(new BorderLayout());
		Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		_tabPane.add("Console", _consoleTab);
		_tabPane.add("Admin", _adminTab);

		//build the frame
		Frame.add(_tabPane, BorderLayout.CENTER);

		//add the window listeners
		addListeners();

		Frame.setLocation(50, 50);
		Frame.setMinimumSize(new Dimension(930, 700));
		//_frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		Frame.setVisible(true);
	}

	public JTabbedPane getTabPane()
	{
		return _tabPane;
	}

	private void addListeners()
	{
		//Window Closing
        /*_frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent event)
			{
				close();
			}
		});*/
	}

	// MenuActions
	/*public class MenuActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent ev)
		{
			String actionCmd = ev.getActionCommand();
			if (actionCmd.equals("Exit"))
			{
				System.exit(0);
			}
		}
	}*/

	public JFrame getMainFrame()
	{
		return Frame;
	}

	public ConsoleTab getConsoleTab()
	{
		return _consoleTab;
	}

	public AdminTab getAdminTab()
	{
		return _adminTab;
	}
}
