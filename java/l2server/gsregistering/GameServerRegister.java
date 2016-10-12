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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.ResourceBundle;

public class GameServerRegister extends BaseGameServerRegister
{
	private LineNumberReader _in;

	public static void main(String[] args)
	{
		// Backwards compatibility, redirect to the new one
		BaseGameServerRegister.main(args);
	}

	/**
	 * @param bundle
	 */
	public GameServerRegister(ResourceBundle bundle)
	{
		super(bundle);
		load();

		int size = GameServerTable.getInstance().getServerNames().size();
		if (size == 0)
		{
			System.out.println(getBundle().getString("noServerNames"));
			System.exit(1);
		}
	}

	public void consoleUI() throws IOException
	{
		_in = new LineNumberReader(new InputStreamReader(System.in));
		boolean choiceOk = false;
		String choice;

		while (true)
		{
			hr();
			System.out.println("GSRegister");
			System.out.println('\n');
			System.out.println("1 - " + getBundle().getString("cmdMenuRegister"));
			System.out.println("2 - " + getBundle().getString("cmdMenuListNames"));
			System.out.println("3 - " + getBundle().getString("cmdMenuRemoveGS"));
			System.out.println("4 - " + getBundle().getString("cmdMenuRemoveAll"));
			System.out.println("5 - " + getBundle().getString("cmdMenuExit"));

			do
			{
				System.out.print(getBundle().getString("yourChoice") + ' ');
				choice = _in.readLine();
				try
				{
					int choiceNumber = Integer.parseInt(choice);
					choiceOk = true;

					switch (choiceNumber)
					{
						case 1:
							registerNewGS();
							break;
						case 2:
							listGSNames();
							break;
						case 3:
							unregisterSingleGS();
							break;
						case 4:
							unregisterAllGS();
							break;
						case 5:
							System.exit(0);
							break;
						default:
							System.out.printf(getBundle().getString("invalidChoice") + '\n', choice);
							choiceOk = false;
					}
				}
				catch (NumberFormatException nfe)
				{
					System.out.printf(getBundle().getString("invalidChoice") + '\n', choice);
				}
			}
			while (!choiceOk);
		}
	}

	/**
	 *
	 */
	private void hr()
	{
		System.out.println("_____________________________________________________\n");
	}

	/**
	 *
	 */
	private void listGSNames()
	{
		int idMaxLen = 0;
		int nameMaxLen = 0;
		for (Entry<Integer, String> e : GameServerTable.getInstance().getServerNames().entrySet())
		{
			if (e.getKey().toString().length() > idMaxLen)
			{
				idMaxLen = e.getKey().toString().length();
			}
			if (e.getValue().length() > nameMaxLen)
			{
				nameMaxLen = e.getValue().length();
			}
		}
		idMaxLen += 2;
		nameMaxLen += 2;

		String id;
		boolean inUse;
		String gsInUse = getBundle().getString("gsInUse");
		String gsFree = getBundle().getString("gsFree");
		int gsStatusMaxLen = Math.max(gsInUse.length(), gsFree.length()) + 2;
		for (Entry<Integer, String> e : GameServerTable.getInstance().getServerNames().entrySet())
		{
			id = e.getKey().toString();
			System.out.print(id);

			for (int i = id.length(); i < idMaxLen; i++)
			{
				System.out.print(' ');
			}
			System.out.print("| ");

			System.out.print(e.getValue());

			for (int i = e.getValue().length(); i < nameMaxLen; i++)
			{
				System.out.print(' ');
			}
			System.out.print("| ");

			inUse = GameServerTable.getInstance().hasRegisteredGameServerOnId(e.getKey());
			String inUseStr = inUse ? gsInUse : gsFree;
			System.out.print(inUseStr);

			for (int i = inUseStr.length(); i < gsStatusMaxLen; i++)
			{
				System.out.print(' ');
			}
			System.out.println('|');
		}
	}

	/**
	 * @throws IOException
	 */
	private void unregisterAllGS() throws IOException
	{
		if (yesNoQuestion(getBundle().getString("confirmRemoveAllText")))
		{
			try
			{
				BaseGameServerRegister.unregisterAllGameServers();
				System.out.println(getBundle().getString("unregisterAllOk"));
			}
			catch (SQLException e)
			{
				showError(getBundle().getString("sqlErrorUnregisterAll"), e);
			}
		}
	}

	private boolean yesNoQuestion(String question) throws IOException
	{

		do
		{
			hr();
			System.out.println(question);
			System.out.println("1 - " + getBundle().getString("yes"));
			System.out.println("2 - " + getBundle().getString("no"));
			System.out.print(getBundle().getString("yourChoice") + ' ');
			String choice;
			choice = _in.readLine();
			switch (choice)
			{
				case "1":
					return true;
				case "2":
					return false;
				default:
					System.out.printf(getBundle().getString("invalidChoice") + '\n', choice);
					break;
			}
		}
		while (true);
	}

	/**
	 * @throws IOException
	 */
	private void unregisterSingleGS() throws IOException
	{
		String line;
		int id = Integer.MIN_VALUE;

		do
		{
			System.out.print(getBundle().getString("enterDesiredId") + ' ');
			line = _in.readLine();
			try
			{
				id = Integer.parseInt(line);
			}
			catch (NumberFormatException e)
			{
				System.out.printf(getBundle().getString("invalidChoice") + '\n', line);
			}
		}
		while (id == Integer.MIN_VALUE);

		String name = GameServerTable.getInstance().getServerNameById(id);
		if (name == null)
		{
			System.out.printf(getBundle().getString("noNameForId") + '\n', id);
		}
		else
		{
			if (GameServerTable.getInstance().hasRegisteredGameServerOnId(id))
			{
				System.out.printf(getBundle().getString("confirmRemoveText") + '\n', id, name);
				try
				{
					BaseGameServerRegister.unregisterGameServer(id);
					System.out.printf(getBundle().getString("unregisterOk") + '\n', id);
				}
				catch (SQLException e)
				{
					showError(getBundle().getString("sqlErrorUnregister"), e);
				}
			}
			else
			{
				System.out.printf(getBundle().getString("noServerForId") + '\n', id);
			}
		}
	}

	private void registerNewGS() throws IOException
	{
		String line;
		int id = Integer.MIN_VALUE;

		do
		{
			System.out.println(getBundle().getString("enterDesiredId"));
			line = _in.readLine();
			try
			{
				id = Integer.parseInt(line);
			}
			catch (NumberFormatException e)
			{
				System.out.printf(getBundle().getString("invalidChoice") + '\n', line);
			}
		}
		while (id == Integer.MIN_VALUE);

		String name = GameServerTable.getInstance().getServerNameById(id);
		if (name == null)
		{
			System.out.printf(getBundle().getString("noNameForId") + '\n', id);
		}
		else
		{
			if (GameServerTable.getInstance().hasRegisteredGameServerOnId(id))
			{
				System.out.println(getBundle().getString("idIsNotFree"));
			}
			else
			{
				try
				{
					BaseGameServerRegister.registerGameServer(id, ".");
				}
				catch (IOException e)
				{
					showError(getBundle().getString("ioErrorRegister"), e);
				}
			}
		}
	}

	/**
	 * @see l2server.gsregistering.BaseGameServerRegister#showError(java.lang.String, java.lang.Throwable)
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
		System.out.println(title + ": " + msg);
	}
}
