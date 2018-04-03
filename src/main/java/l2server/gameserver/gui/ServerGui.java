package l2server.gameserver.gui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Pere
 */
public class ServerGui {
	
	private static JFrame frame;

	//private JMenuBar menuBar = new JMenuBar();

	//private JMenu fileMenu = new JMenu("File");
	//private JMenu helpMenu = new JMenu("Help");

	//private ActionListener menuListener = new MenuActionListener();

	private static JTabbedPane tabPane = new JTabbedPane();

	private static ConsoleTab consoleTab;

	private static AdminTab adminTab;

	public void init() {
		frame = new JFrame("L2 Server");

		//Menu Bar Items
		//File Menu
		/*JMenuItem itemExit = new JMenuItem("Exit");
        itemExit.setActionCommand("Exit");
		itemExit.addActionListener(menuListener);

		fileMenu.add(itemExit);

		//Help
		JMenuItem itemAbout = new JMenuItem("About");
		itemAbout.setActionCommand("About");
		itemAbout.addActionListener(menuListener);
		helpMenu.add(itemAbout);

		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		frame.setJMenuBar(menuBar);*/

		// Console Tab
		consoleTab = new ConsoleTab(true);
		adminTab = new AdminTab();
		
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		tabPane.add("Console", consoleTab);
		tabPane.add("Admin", adminTab);

		//build the frame
		frame.add(tabPane, BorderLayout.CENTER);

		//add the window listeners
		addListeners();
		
		frame.setLocation(50, 50);
		frame.setMinimumSize(new Dimension(930, 700));
		//frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
	}

	public JTabbedPane getTabPane() {
		return tabPane;
	}

	private void addListeners() {
		//Window Closing
        /*frame.addWindowListener(new WindowAdapter()
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
	
	public static JFrame getMainFrame() {
		return frame;
	}
	
	public static ConsoleTab getConsoleTab() {
		return consoleTab;
	}
	
	public static AdminTab getAdminTab() {
		return adminTab;
	}
}
