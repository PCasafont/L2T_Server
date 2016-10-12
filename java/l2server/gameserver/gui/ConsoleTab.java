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

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class ConsoleTab extends JPanel
{
	private static final long serialVersionUID = 1L;
	private static final int MSG_STORAGE_LIMIT = 5000;
	private static final int MSG_DISPLAY_LIMIT = 500;

	public enum ConsoleFilter
	{
		Console(null, Color.red, true),
		Errors(Console, new Color(220, 50, 50), true),
		Warnings(Console, new Color(220, 220, 50), true),
		Info(Console, new Color(50, 220, 50), true),
		Chat(null, Color.white, true),
		Announcements(Chat, Color.cyan, true),
		GlobalChat(Chat, new Color(255, 100, 100), true),
		HeroChat(Chat, new Color(63, 137, 249), true),
		ShoutChat(Chat, new Color(255, 130, 0), true, "Region"),
		TradeChat(Chat, new Color(234, 165, 245), true, "Region"),
		AllChat(Chat, Color.white, false, "Region", "Talker"),
		AllyChat(Chat, new Color(120, 255, 120), false, "Ally Name", "Talker"),
		ClanChat(Chat, new Color(120, 120, 255), false, "Clan Name", "Talker"),
		PartyChat(Chat, Color.green, false, "Party Leader", "Talker"),
		WhisperChat(Chat, Color.magenta, false, "Talker", "Listener");

		public final ConsoleFilter parent;
		public final List<ConsoleFilter> children = new ArrayList<>();
		public final Color textColor;
		public final boolean startEnabled;
		public final String[] subFilters;

		ConsoleFilter(ConsoleFilter p, Color tc, boolean se, String... sf)
		{
			parent = p;
			if (parent != null)
			{
				parent.children.add(this);
			}
			textColor = tc;
			startEnabled = se;
			subFilters = sf;
		}
	}

	private static class ConsoleLine
	{
		public final ConsoleFilter filter;
		public final String text;
		public final String[] extra;

		public ConsoleLine(ConsoleFilter f, String t, String... e)
		{
			filter = f;
			text = t;
			extra = e;
		}
	}

	private static int _instanceId = 0;
	private static List<ConsoleTab> _instances = new ArrayList<>();
	private static List<ConsoleLine> _messages = new ArrayList<>();

	private class ConsoleFilterInstance
	{
		public JCheckBox checkBox;
		public JTextField[] textFields;

		public boolean isEnabled()
		{
			return checkBox.isSelected();
		}
	}

	private ConsoleFilterInstance[] _filters = new ConsoleFilterInstance[ConsoleFilter.values().length];
	private JTextPane _textPane;

	public ConsoleTab(boolean main)
	{
		setLayout(new GridBagLayout());
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new GridBagLayout());

		JPanel checkBoxes = new JPanel();
		checkBoxes.setLayout(new GridBagLayout());
		checkBoxes.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Show:"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.FIRST_LINE_START;
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.gridy = 0;
		cons.weighty = 1;
		int checkBoxesGridWidth = 20;
		ConsoleActionListener listener = new ConsoleActionListener();
		ConsoleSubFilterListener filterListener = new ConsoleSubFilterListener();
		for (ConsoleFilter f : ConsoleFilter.values())
		{
			_filters[f.ordinal()] = new ConsoleFilterInstance();
			ConsoleFilterInstance fi = _filters[f.ordinal()];
			int depthLevel = 0;
			ConsoleFilter child = f.parent;
			while (child != null)
			{
				depthLevel++;
				child = child.parent;
			}

			cons.gridx = depthLevel;
			cons.weightx = 1;
			cons.gridwidth = 1;
			fi.checkBox = new JCheckBox();
			fi.checkBox.setActionCommand(f.name());
			fi.checkBox.setSelected(f.startEnabled);
			fi.checkBox.addActionListener(listener);
			checkBoxes.add(fi.checkBox, cons);
			cons.gridx = depthLevel + 1;
			cons.weightx = 10;
			cons.gridwidth = checkBoxesGridWidth - depthLevel - 1;
			checkBoxes.add(new JLabel(f.name()), cons);
			cons.gridy++;
			fi.textFields = new JTextField[f.subFilters.length];
			for (int i = 0; i < f.subFilters.length; i++)
			{
				cons.gridx = depthLevel + 2;
				cons.weightx = 1;
				cons.gridwidth = 1;
				checkBoxes.add(new JLabel(f.subFilters[i] + ":"), cons);
				cons.gridx = depthLevel + 3;
				cons.weightx = 10;
				cons.gridwidth = checkBoxesGridWidth - depthLevel - 3;
				fi.textFields[i] = new JTextField();
				fi.textFields[i].getDocument().addDocumentListener(filterListener);
				checkBoxes.add(fi.textFields[i], cons);
				cons.gridy++;
			}
		}

		// Add checkboxes to the left panel
		cons.anchor = GridBagConstraints.FIRST_LINE_START;
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.insets = new Insets(5, 5, 5, 5);
		cons.gridy = 0;
		cons.gridx = 0;
		leftPanel.add(checkBoxes, cons);

		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1;
		cons.weighty = 1;
		_textPane = new JTextPane();
		_textPane.setBackground(new Color(30, 30, 30));
		JScrollPane console = new JScrollPane(_textPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		cons.weightx = 1;
		cons.weighty = 1;
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, console);
		splitPane.setResizeWeight(0.3);
		splitPane.setDividerLocation(230);
		add(splitPane, cons);

		_instances.add(this);
		if (main)
		{
			// Add new console window button
			JButton button = new JButton("New Console Window");
			button.setActionCommand("newConsoleWindow");
			button.addActionListener(listener);
			cons.anchor = GridBagConstraints.FIRST_LINE_START;
			cons.fill = GridBagConstraints.HORIZONTAL;
			cons.insets = new Insets(5, 5, 5, 5);
			cons.gridy = 1;
			cons.gridx = 0;
			leftPanel.add(button, cons);
		}

		reloadDoc();
	}

	public synchronized static void appendMessage(ConsoleFilter f, String msg, String... extra)
	{
		try
		{
			_messages.add(new ConsoleLine(f, msg, extra));
			while (_messages.size() > MSG_STORAGE_LIMIT)
			{
				_messages.remove(0);
			}

			for (ConsoleTab tab : _instances)
			{
				tab.onAppendMessage(f, msg, extra);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public synchronized void onAppendMessage(ConsoleFilter f, String msg, String... extra)
	{
		ConsoleFilterInstance fi = _filters[f.ordinal()];
		if (!fi.isEnabled())
		{
			return;
		}

		int emptySubFields = 0;
		boolean pass = false;
		for (int i = 0; i < fi.textFields.length; i++)
		{
			if (fi.textFields[i].getText().isEmpty())
			{
				emptySubFields++;
				continue;
			}

			if (extra[i].toLowerCase().contains(fi.textFields[i].getText()))
			{
				pass = true;
			}
		}

		if (!pass && emptySubFields < f.subFilters.length)
		{
			return;
		}

		msg += "\n";
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, f.textColor);

		//aset = sc.addAttribute(aset, StyleConstants.Background, new Color(0, 100, 250));
		if (f == ConsoleFilter.Console || f.parent != null && f.parent == ConsoleFilter.Console)
		{
			aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
			aset = sc.addAttribute(aset, StyleConstants.FontSize, 14);
		}
		else
		{
			aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Verdana");
			aset = sc.addAttribute(aset, StyleConstants.FontSize, 12);
		}
		aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

		Document document = _textPane.getDocument();
		Element root = document.getDefaultRootElement();
		while (root.getElementCount() > MSG_DISPLAY_LIMIT)
		{
			Element line = root.getElement(0);
			int end = line.getEndOffset();
			try
			{
				document.remove(0, end);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		int len = document.getLength();
		_textPane.setCaretPosition(len);
		_textPane.setCharacterAttributes(aset, false);
		_textPane.replaceSelection(msg);

		EventQueue.invokeLater(() ->
		{
			Rectangle visibleRect = _textPane.getVisibleRect();
			if (visibleRect.y + 100 > _textPane.getHeight() - visibleRect.height)
			{
				visibleRect.y = _textPane.getHeight() - visibleRect.height;
				_textPane.scrollRectToVisible(visibleRect);
			}
		});
	}

	public synchronized void reloadDoc()
	{
		_textPane.setText("");

		for (ConsoleLine line : _messages)
		{
			ConsoleFilter f = line.filter;
			ConsoleFilterInstance fi = _filters[f.ordinal()];
			if (!fi.isEnabled())
			{
				continue;
			}

			int emptySubFields = 0;
			boolean pass = false;
			for (int i = 0; i < fi.textFields.length; i++)
			{
				JTextField textField = fi.textFields[i];
				if (textField.getText().isEmpty())
				{
					emptySubFields++;
					continue;
				}

				if (line.extra[i].toLowerCase().contains(textField.getText()))
				{
					pass = true;
				}
			}

			if (!pass && emptySubFields < fi.textFields.length)
			{
				continue;
			}

			String msg = line.text + "\n";
			StyleContext sc = StyleContext.getDefaultStyleContext();
			AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, f.textColor);

			//aset = sc.addAttribute(aset, StyleConstants.Background, new Color(0, 100, 250));
			if (f == ConsoleFilter.Console || f.parent != null && f.parent == ConsoleFilter.Console)
			{
				aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
				aset = sc.addAttribute(aset, StyleConstants.FontSize, 14);
			}
			else
			{
				aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Verdana");
				aset = sc.addAttribute(aset, StyleConstants.FontSize, 12);
			}
			aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

			Document document = _textPane.getDocument();
			Element root = document.getDefaultRootElement();
			while (root.getElementCount() > MSG_DISPLAY_LIMIT)
			{
				Element elem = root.getElement(0);
				int end = elem.getEndOffset();
				try
				{
					document.remove(0, end);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			int len = document.getLength();
			_textPane.setCaretPosition(len);
			_textPane.setCharacterAttributes(aset, false);
			_textPane.replaceSelection(msg);
		}
	}

	private class ConsoleActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent ae)
		{
			if (ae.getActionCommand().equalsIgnoreCase("newConsoleWindow"))
			{
				_instanceId++;
				JFrame extra = new JFrame("Console View #" + _instanceId);
				final ConsoleTab tab = new ConsoleTab(false);
				extra.add(tab);
				extra.addWindowListener(new WindowAdapter()
				{
					@Override
					public void windowClosing(WindowEvent arg0)
					{
						_instances.remove(tab);
					}
				});
				extra.setMinimumSize(new Dimension(900, 600));
				extra.setLocation(100, 100);
				extra.setVisible(true);
				tab.reloadDoc();
				return;
			}

			ConsoleFilter f = ConsoleFilter.valueOf(ae.getActionCommand());
			if (f == null)
			{
				return;
			}

			if (_filters[f.ordinal()].isEnabled())
			{
				for (ConsoleFilter child : f.children)
				{
					_filters[child.ordinal()].checkBox.setEnabled(true);
				}
			}
			else
			{
				for (ConsoleFilter child : f.children)
				{
					_filters[child.ordinal()].checkBox.setSelected(false);
					_filters[child.ordinal()].checkBox.setEnabled(false);
				}
			}

			reloadDoc();
		}
	}

	private class ConsoleSubFilterListener implements DocumentListener
	{
		@Override
		public void changedUpdate(DocumentEvent e)
		{
			reloadDoc();
		}

		@Override
		public void insertUpdate(DocumentEvent e)
		{
			reloadDoc();
		}

		@Override
		public void removeUpdate(DocumentEvent e)
		{
			reloadDoc();
		}
	}
}
