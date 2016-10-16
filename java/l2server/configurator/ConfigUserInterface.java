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

package l2server.configurator;

import l2server.configurator.ConfigUserInterface.ConfigFile.ConfigComment;
import l2server.configurator.ConfigUserInterface.ConfigFile.ConfigProperty;
import l2server.i18n.LanguageControl;
import l2server.images.ImagesTable;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author KenM
 */
public class ConfigUserInterface extends JFrame implements ActionListener
{
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	private JTabbedPane tabPane = new JTabbedPane();

	@Getter @Setter private List<ConfigFile> configs = new ArrayList<>();

	@Getter @Setter private ResourceBundle bundle;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			// couldn't care less
		}

		final ResourceBundle bundle =
				ResourceBundle.getBundle("configurator.Configurator", Locale.getDefault(), LanguageControl.INSTANCE);

		SwingUtilities.invokeLater(() ->
		{
			ConfigUserInterface cui = new ConfigUserInterface(bundle);
			cui.setVisible(true);
		});
	}

	public ConfigUserInterface(ResourceBundle bundle)
	{
		setBundle(bundle);
		setTitle(bundle.getString("toolName"));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(750, 500);
		setLayout(new GridBagLayout());

		GridBagConstraints cons = new GridBagConstraints();
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.gridx = 0;
		cons.gridy = 0;
		cons.weighty = 0;
		cons.weightx = 1;

		JMenuBar menubar = new JMenuBar();

		JMenu fileMenu = new JMenu(bundle.getString("fileMenu"));
		JMenu helpMenu = new JMenu(bundle.getString("helpMenu"));

		JMenuItem exitItem = new JMenuItem(bundle.getString("exitItem"));
		exitItem.setActionCommand("exit");
		exitItem.addActionListener(this);
		fileMenu.add(exitItem);

		JMenuItem aboutItem = new JMenuItem(bundle.getString("aboutItem"));
		aboutItem.setActionCommand("about");
		aboutItem.addActionListener(this);
		helpMenu.add(aboutItem);

		menubar.add(fileMenu);
		menubar.add(helpMenu);

		setJMenuBar(menubar);

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		toolBar.add(createToolButton("disk.png", bundle.getString("save"), "save"));
		add(toolBar, cons);

		cons.gridy++;
		cons.fill = GridBagConstraints.BOTH;
		cons.weighty = 1;
		loadConfigs();
		buildInterface();
		add(tabPane, cons);
	}

	private JButton createToolButton(String image, String text, String action)
	{
		JButton button = new JButton(text, ImagesTable.getImage(image));
		button.setActionCommand(action);
		button.addActionListener(this);
		return button;
	}

	/**
	 *
	 */
	private void buildInterface()
	{
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
		ToolTipManager.sharedInstance().setInitialDelay(0);
		ToolTipManager.sharedInstance().setReshowDelay(0);

		GridBagConstraints cons = new GridBagConstraints();
		cons.fill = GridBagConstraints.NONE;
		cons.anchor = GridBagConstraints.FIRST_LINE_START;
		cons.insets = new Insets(2, 2, 2, 2);
		for (ConfigFile cf : getConfigs())
		{
			JPanel panel = new JPanel()
			{
				/**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void scrollRectToVisible(Rectangle r)
				{
				}
			};
			panel.setLayout(new GridBagLayout());

			cons.gridy = 0;
			cons.weighty = 0;
			for (ConfigComment cc : cf.getConfigProperties())
			{
				if (!(cc instanceof ConfigProperty))
				{
					continue;
				}

				ConfigProperty cp = (ConfigProperty) cc;
				cons.gridx = 0;

				JLabel keyLabel = new JLabel(cp.getDisplayName() + ':', ImagesTable.getImage("help.png"), JLabel.LEFT);
				String comments = "<b>" + cp.getName() + ":</b><br>" + cp.getComments();
				comments = comments.replace("\r\n", "<br>");
				comments = "<html>" + comments + "</html>";
				keyLabel.setToolTipText(comments);
				cons.weightx = 0;
				panel.add(keyLabel, cons);
				cons.gridx++;

				JComponent valueComponent = cp.getValueComponent();
				valueComponent.setToolTipText(comments);
				cons.weightx = 1;
				panel.add(valueComponent, cons);
				cons.gridx++;
				cons.gridy++;
			}
			cons.gridy++;
			cons.weighty = 1;
			panel.add(new JLabel(), cons); // filler
			tabPane.addTab(cf.getName(), new JScrollPane(panel));
		}
	}

	/**
	 *
	 */
	private void loadConfigs()
	{
		File configsDir = new File("config");
		for (File file : configsDir.listFiles())
		{
			if (file.getName().endsWith(".properties") && file.isFile() && file.canWrite())
			{
				try
				{
					parsePropertiesFile(file);
				}
				catch (IOException e)
				{
					JOptionPane.showMessageDialog(ConfigUserInterface.this,
							getBundle().getString("errorReading") + file.getName(), getBundle().getString("error"),
							JOptionPane.ERROR_MESSAGE);
					System.exit(3);
					// e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param file
	 * @throws IOException
	 */
	private void parsePropertiesFile(File file) throws IOException
	{
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));

		String line;
		StringBuilder commentBuffer = new StringBuilder();
		ConfigFile cf = new ConfigFile(file);
		while ((line = lnr.readLine()) != null)
		{
			line = line.trim();

			if (line.startsWith("#"))
			{
				if (commentBuffer.length() > 0)
				{
					commentBuffer.append("\r\n");
				}
				commentBuffer.append(line.substring(1));
			}
			else if (line.length() == 0)
			{
				// blank line, reset comments
				if (commentBuffer.length() > 0)
				{
					cf.addConfigComment(commentBuffer.toString());
				}
				commentBuffer.setLength(0);
			}
			else if (line.indexOf('=') >= 0)
			{
				String[] kv = line.split("=");
				String key = kv[0].trim();
				String value = "";
				if (kv.length > 1)
				{
					value = kv[1].trim();
				}

				if (line.indexOf('\\') >= 0)
				{
					while ((line = lnr.readLine()) != null && line.indexOf('\\') >= 0)
					{
						value += "\r\n" + line;
					}
					value += "\r\n" + line;
				}

				String comments = commentBuffer.toString();
				commentBuffer.setLength(0); //reset

				cf.addConfigProperty(key, parseValue(value), comments);
			}
		}
		getConfigs().add(cf);
		lnr.close();
	}

	/**
	 * @param value
	 */
	private Object parseValue(String value)
	{
		if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("true"))
		{
			return Boolean.parseBoolean(value);
		}

		/*try
		{
			double parseDouble = Double.parseDouble(value);
			return parseDouble;
		}
		catch (NumberFormatException e)
		{
			// not a double, ignore
		}*/

		// localhost -> 127.0.0.1
		if (value.equals("localhost"))
		{
			value = "127.0.0.1";
		}

		String[] parts = value.split("\\.");
		if (parts.length == 4)
		{
			boolean ok = true;
			for (int i = 0; i < 4 && ok; i++)
			{
				try
				{
					int parseInt = Integer.parseInt(parts[i]);
					if (parseInt < 0 || parseInt > 255)
					{
						ok = false;
					}
				}
				catch (NumberFormatException e)
				{
					ok = false;
				}
			}

			if (ok)
			{
				try
				{
					return InetAddress.getByName(value);
				}
				catch (UnknownHostException e)
				{
					// ignore
				}
			}
		}

		return value;
	}

	static class ConfigFile
	{
		private File file;
		@Getter @Setter private String name;
		private final List<ConfigComment> configs = new ArrayList<>();

		public ConfigFile(File file)
		{
			this.file = file;
			int lastIndex = file.getName().lastIndexOf('.');
			setName(file.getName().substring(0, lastIndex));
		}

		public void addConfigProperty(String name, Object value, ValueType type, String comments)
		{
			configs.add(new ConfigProperty(name, value, type, comments));
		}

		public void addConfigComment(String comment)
		{
			configs.add(new ConfigComment(comment));
		}

		public void addConfigProperty(String name, Object value, String comments)
		{
			addConfigProperty(name, value, ValueType.firstTypeMatch(value), comments);
		}

		public List<ConfigComment> getConfigProperties()
		{
			return configs;
		}

		public void save() throws IOException
		{
			BufferedWriter bufWriter = null;
			try
			{
				bufWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
				for (ConfigComment cc : configs)
				{
					cc.save(bufWriter);
				}
			}
			finally
			{
				if (bufWriter != null)
				{
					bufWriter.close();
				}
			}
		}

		class ConfigComment
		{
			@Getter @Setter private String comments;

			/**
			 * @param comments
			 */
			public ConfigComment(String comments)
			{
				this.comments = comments;
			}

			public void save(Writer writer) throws IOException
			{
				StringBuilder sb = new StringBuilder();
				sb.append('#');
				sb.append(getComments().replace("\r\n", "\r\n#"));
				sb.append("\r\n\r\n");
				writer.write(sb.toString());
			}
		}

		class ConfigProperty extends ConfigComment
		{
			private String propname;
			@Getter private Object value;
			@Getter @Setter private ValueType type;
			private JComponent component;

			/**
			 * @param name
			 * @param value
			 * @param type
			 * @param comments
			 */
			public ConfigProperty(String name, Object value, ValueType type, String comments)
			{
				super(comments);
				if (!type.getType().isAssignableFrom(value.getClass()))
				{
					throw new IllegalArgumentException("Value Instance Type doesn't match the type argument.");
				}
				propname = name;
				this.type = type;
				this.value = value;
			}

			/**
			 * @return Returns the name.
			 */
			public String getName()
			{
				return propname;
			}

			/**
			 * @return Returns the name.
			 */
			public String getDisplayName()
			{
				return unCamelize(propname);
			}

			/**
			 * @param name The name to set.
			 */
			public void setName(String name)
			{
				propname = name;
			}

			/**
			 * @param value The value to set.
			 */
			public void setValue(String value)
			{
				this.value = value;
			}

			public JComponent getValueComponent()
			{
				if (component == null)
				{
					component = createValueComponent();
				}
				return component;
			}

			public JComponent createValueComponent()
			{
				switch (getType())
				{
					case BOOLEAN:
						boolean bool = (Boolean) getValue();
						JCheckBox checkBox = new JCheckBox();
						checkBox.setSelected(bool);
						return checkBox;
					case IPv4:
						return new JIPTextField((Inet4Address) getValue());
					case DOUBLE:
					case INTEGER:
					case STRING:
					default:
						String val = getValue().toString();
						JTextArea textArea = new JTextArea(val);
						textArea.setFont(UIManager.getFont("TextField.font"));
						int rows = 1;
						for (int i = 0; i < val.length(); i++)
						{
							if (val.charAt(i) == '\\')
							{
								rows++;
							}
						}
						textArea.setRows(rows);
						textArea.setColumns(Math.max(val.length() / rows, 20));
						return textArea;
				}
			}

			@Override
			public void save(Writer writer) throws IOException
			{
				String value;
				if (getValueComponent() instanceof JCheckBox)
				{
					value = Boolean.toString(((JCheckBox) getValueComponent()).isSelected());
					value = value.substring(0, 1).toUpperCase() + value.substring(1);
				}
				else if (getValueComponent() instanceof JIPTextField)
				{
					value = ((JIPTextField) getValueComponent()).getText();
				}
				else if (getValueComponent() instanceof JTextArea)
				{
					value = ((JTextArea) getValueComponent()).getText();
				}
				else
				{
					throw new IllegalStateException("Unhandled component value");
				}

				StringBuilder sb = new StringBuilder();
				sb.append('#');
				sb.append(getComments().replace("\r\n", "\r\n#"));
				sb.append("\r\n");
				sb.append(getName());
				sb.append(" = ");
				sb.append(value);
				sb.append("\r\n");
				sb.append("\r\n");
				writer.write(sb.toString());
			}
		}
	}

	public enum ValueType
	{
		BOOLEAN(Boolean.class),
		DOUBLE(Double.class),
		INTEGER(Integer.class),
		IPv4(Inet4Address.class),
		STRING(String.class);

		@Getter private final Class<?> type;

		ValueType(Class<?> type)
		{
			this.type = type;
		}

		public static ValueType firstTypeMatch(Object value)
		{
			for (ValueType vt : ValueType.values())
			{
				if (vt.getType() == value.getClass())
				{
					return vt;
				}
			}
			throw new NoSuchElementException("No match for: " + value.getClass().getName());
		}
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();

		StringBuilder errors = new StringBuilder();

		switch (cmd)
		{
			case "save":
				for (ConfigFile cf : getConfigs())
				{
					try
					{
						cf.save();
					}
					catch (Exception e1)
					{
						e1.printStackTrace();
						errors.append(getBundle().getString("errorSaving") + cf.getName() + ".properties. " +
								getBundle().getString("reason") + e1.getLocalizedMessage() + "\r\n");
					}
				}
				if (errors.length() == 0)
				{
					JOptionPane.showMessageDialog(ConfigUserInterface.this, getBundle().getString("success"), "OK",
							JOptionPane.INFORMATION_MESSAGE);
				}
				else
				{
					JOptionPane.showMessageDialog(ConfigUserInterface.this, errors, getBundle().getString("error"),
							JOptionPane.ERROR_MESSAGE);
					System.exit(2);
				}
				break;
			case "exit":
				System.exit(0);
			case "about":
				JOptionPane.showMessageDialog(ConfigUserInterface.this,
						getBundle().getString("credits") + "\nhttp://www.l2jserver.com\n\n" +
								getBundle().getString("icons") + "\n\n" + getBundle().getString("language") + '\n' +
								getBundle().getString("translation"), getBundle().getString("aboutItem"),
						JOptionPane.INFORMATION_MESSAGE, ImagesTable.getImage("l2jserverlogo.png"));
				break;
		}
	}

	public static String unCamelize(final String keyName)
	{
		Pattern p = Pattern.compile("\\p{Lu}");
		Matcher m = p.matcher(keyName);
		StringBuffer sb = new StringBuffer();
		int last = 0;
		while (m.find())
		{
			if (m.start() != last + 1)
			{
				m.appendReplacement(sb, " " + m.group());
			}
			last = m.start();
		}
		m.appendTail(sb);
		return sb.toString().trim();
	}
}
