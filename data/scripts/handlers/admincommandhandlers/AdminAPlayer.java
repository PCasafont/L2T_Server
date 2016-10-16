package handlers.admincommandhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.instancemanager.ArtificialPlayersManager;
import l2server.gameserver.model.actor.instance.L2ApInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class handles all commands made for control and manage Artificial Players (APlayers).
 *
 * @author Soul
 */
public class AdminAPlayer implements IAdminCommandHandler
{

	private static final String[] ADMIN_COMMANDS =
			{"admin_spawn_aplayer", "admin_spawn_aparty", "admin_delete_all_aplayers"};

	public static Logger log = Logger.getLogger(AdminAPlayer.class.getName());

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String param = null;

		if (command.startsWith("admin_spawn_aplayer"))
		{
			try
			{
				st.nextToken();
				param = st.nextToken();
			}
			catch (NoSuchElementException nsee)
			{
				activeChar.sendMessage("use: //spawn_aplayer <classId | className>");
			}
		}
		else if (command.equalsIgnoreCase("admin_delete_all_aplayers"))
		{
			for (L2ApInstance aplayer : ArtificialPlayersManager.getInstance().getAllAPlayers())
			{
				if (aplayer != null)
				{
					aplayer.deleteMe();
				}
			}
			activeChar.sendMessage("Kiked all aplayers from the server...!");
		}
		else if (command.startsWith("admin_spawn_aparty"))
		{
			if (st.hasMoreTokens())
			{
				// Generate param-specific party
				List<Integer> classIds = new ArrayList<Integer>();

				st.nextToken(); // spawn_aparty command

				while (st.hasMoreTokens())
				{
					param = st.nextToken();

					if (param.matches("[0-9]*"))
					{
						// It's a class Id
						classIds.add(Integer.parseInt(param));
					}
					else
					{
						int id = getIdFromName(param);

						if (id > 0)
						{
							classIds.add(id);
						}
						//else
						//activeChar.sendMessage("Invalid class name: " + param);
					}
				}

				if (classIds.size() > 1)
				{
					ArtificialPlayersManager.getInstance().createParty(classIds);
					activeChar.sendMessage("APlayer party created.");
					//TODO seria bona idea que mostres el party #
				}
				else
				{
					activeChar.sendMessage("Too few parameters to create a APlayer party.");
				}
			}
			else
			{
				// Generate random-party
				ArtificialPlayersManager.getInstance().createRandomParty();
				activeChar.sendMessage("Random APlayer party manually created.");
			}
		}
		else if (command.startsWith("admin_delete_aparty"))
		{
			// TODO: delete party number #XX
		}
		else if (command.startsWith("admin_recall_aparty"))
		{
			// TODO: recall party #XX to our position
		}
		else if (command.equals("admin_list_aparty"))
		{
			// TODO: list the parties
			/*
             * Aplayer party #XX
			 * Members: [Party[n]]+
			 *
			 * Possible output:
			 *
			 * APlayer party #85
			 * Members: Sigel, Tyrr, Iss, Feoh
			 *
			 * APlayer party #74
			 * Members: Aeore, Iss, Yul, Yul, Feoh
			 */
		}
		else if (command.equals("admin_list_aplayers"))
		{
			// TODO: list the non-party APlayers
            /*
			 * [#X] Name (X,Y,Z)
			 * where X = #APlayer ID (for multiple of same name)
			 *
			 * Possible output:
			 *
			 * APlayer list (non-party)
			 * [#3] Feoh (-2213, 55693, 1200)
			 * [#12] Tyrr (51333, 12, -6552)
			 * ...
			 */
		}

		return true;
	}

	private int getIdFromName(String param)
	{
		// For now, only Awakened support!
		if ("Sigel Knight".contains(param))
		{
			return 139;
		}
		else if ("Tyrr Warrior".contains(param))
		{
			return 140;
		}
		else if ("Othel Rogue".contains(param))
		{
			return 141;
		}
		else if ("Yul Archer".contains(param))
		{
			return 142;
		}
		else if ("Feoh Wizard".contains(param))
		{
			return 143;
		}
		else if ("Iss Enchanter".contains(param))
		{
			return 144;
		}
		else if ("Wynn Summoner".contains(param))
		{
			return 145;
		}
		else if ("Aeore Healer".contains(param))
		{
			return 146;
		}

		return 0; // Maybe some class as default?

		// mirar si el projecte encara esta en java6 D:
		
		/*switch (param.toLowerCase())
		{
			case "sigel":
			case "knight":
				return 139;
			case "tyrr":
			case "warrior":
				return 140;
			case "othel":
			case "rogue":
				return 141;
			case "yul":
			case "archer":
				return 142;
			case "feoh":
			case "wizard":
				return 143;
			case "iss":
			case "enchanter":
				return 144;
			case "wynn":
			case "summoner":
				return 145;
			case "aeore":
			case "healer":
				return 146;
			default:
				return 0;
		}*/
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
