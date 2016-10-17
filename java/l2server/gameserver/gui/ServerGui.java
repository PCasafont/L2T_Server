package l2server.gameserver.gui;

import lombok.Getter;

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

	@Getter private static JTabbedPane tabPane = new JTabbedPane();

	@Getter private static ConsoleTab consoleTab;

	@Getter private static AdminTab adminTab;

	public void init()
	{
		Frame = new JFrame("L2 Server");

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
}
