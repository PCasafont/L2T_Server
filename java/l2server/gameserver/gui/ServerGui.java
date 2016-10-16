package l2server.gameserver.gui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Pere
 */
public class ServerGui
{
	public static JFrame Frame;

	//private JMenuBar menuBar = new JMenuBar();

	//private JMenu fileMenu = new JMenu("File");
	//private JMenu helpMenu = new JMenu("Help");

	//private ActionListener menuListener = new MenuActionListener();

	private static JTabbedPane tabPane = new JTabbedPane();

	private static ConsoleTab consoleTab;

	private static AdminTab adminTab;

	public void init()
	{
		Frame = new JFrame("L2 Server");

		//Menu Bar Items
		//File Menu
		/*JMenuItem itemExit = new JMenuItem("Exit");
        itemExit.setActionCommand("Exit");
		itemExit.addActionListener(this.menuListener);

		this.fileMenu.add(itemExit);

		//Help
		JMenuItem itemAbout = new JMenuItem("About");
		itemAbout.setActionCommand("About");
		itemAbout.addActionListener(this.menuListener);
		this.helpMenu.add(itemAbout);

		this.menuBar.add(this.fileMenu);
		this.menuBar.add(this.helpMenu);
		this.frame.setJMenuBar(this.menuBar);*/

		// Console Tab
		consoleTab = new ConsoleTab(true);
		adminTab = new AdminTab();

		Frame.setLayout(new BorderLayout());
		Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		tabPane.add("Console", consoleTab);
		tabPane.add("Admin", adminTab);

		//build the frame
		Frame.add(tabPane, BorderLayout.CENTER);

		//add the window listeners
		addListeners();

		Frame.setLocation(50, 50);
		Frame.setMinimumSize(new Dimension(930, 700));
		//_frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		Frame.setVisible(true);
	}

	public JTabbedPane getTabPane()
	{
		return tabPane;
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
		return consoleTab;
	}

	public AdminTab getAdminTab()
	{
		return adminTab;
	}
}
