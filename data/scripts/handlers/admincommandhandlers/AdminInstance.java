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
package handlers.admincommandhandlers;

import java.util.StringTokenizer;

import l2tserver.gameserver.handler.IAdminCommandHandler;
import l2tserver.gameserver.instancemanager.InstanceManager;
import l2tserver.gameserver.model.L2Object;
import l2tserver.gameserver.model.actor.L2Summon;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.model.actor.instance.L2SummonInstance;
import l2tserver.gameserver.model.entity.Instance;
import l2tserver.gameserver.network.serverpackets.NpcHtmlMessage;
import l2tserver.util.Rnd;


/**
 * @author evill33t, GodKratos
 * 
 */
public class AdminInstance implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_setinstance",
		"admin_ghoston",
		"admin_ghostoff",
		"admin_createinstance",
		"admin_destroyinstance",
		"admin_listinstances"
	};
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		// create new instance
		if (command.startsWith("admin_createinstance"))
		{
			String[] parts = command.split(" ");
			if (parts.length < 2)
			{
				activeChar.sendMessage("Example: //createinstance <id> <templatefile> - ids => 300000 are reserved for dynamic instances");
			}
			else
			{
				try
				{
					int id = Integer.parseInt(parts[1]);
					if (InstanceManager.getInstance().createInstanceFromTemplate(id, parts[2]) && id < 300000)
					{
						activeChar.sendMessage("Instance created");
						return true;
					}
					else
					{
						activeChar.sendMessage("Failed to create instance");
						return true;
					}
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Failed loading: " + parts[2]);
					return false;
				}
			}
		}
		else if (command.startsWith("admin_listinstances"))
		{
			String page = "<html><body><title>Instance Panel</title><table width=300>";
			for (Instance temp : InstanceManager.getInstance().getInstances().values())
			{
				if (temp.getName() != null && temp.getId() > 1)
					page += "<tr><td><a action=\"bypass -h admin_move_to "+getStringCords(temp.getId())+"\">Name: "+temp.getName()+" id: "+temp.getId()+"</a></td></tr>";
				else
					activeChar.sendMessage("Id: " + temp.getId() + " Name: " + temp.getName());
			}
			page += "</table></body></html>";
			
			activeChar.sendPacket(new NpcHtmlMessage(0, page));
		}
		else if (command.startsWith("admin_setinstance"))
		{
			try
			{
				int val = Integer.parseInt(st.nextToken());
				if (InstanceManager.getInstance().getInstance(val) == null)
				{
					activeChar.sendMessage("Instance " + val + " doesnt exist.");
					return false;
				}
				else
				{
					L2Object target = activeChar.getTarget();
					if (target == null || target instanceof L2Summon) // Don't separate summons from masters
					{
						activeChar.sendMessage("Incorrect target.");
						return false;
					}
					target.setInstanceId(val);
					if (target instanceof L2PcInstance)
					{
						L2PcInstance player = (L2PcInstance) target;
						player.sendMessage("Admin set your instance to:" + val);
						player.teleToLocation(player.getX(), player.getY(), player.getZ());
						L2Summon pet = player.getPet();
						if (pet != null)
						{
							pet.setInstanceId(val);
							pet.teleToLocation(pet.getX(), pet.getY(), pet.getZ());
							player.sendMessage("Admin set " + pet.getName() + "'s instance to:" + val);
						}
						for (L2SummonInstance summon : ((L2PcInstance)target).getSummons())
						{
							summon.setInstanceId(val);
							summon.teleToLocation(summon.getX(), summon.getY(), summon.getZ());
							player.sendMessage("Admin set " + summon.getName() + "'s instance to:" + val);
						}
					}
					activeChar.sendMessage("Moved " + target.getName() + " to instance " + target.getInstanceId() + ".");
					return true;
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Use //setinstance id");
			}
		}
		else if (command.startsWith("admin_destroyinstance"))
		{
			try
			{
				int val = Integer.parseInt(st.nextToken());
				InstanceManager.getInstance().destroyInstance(val);
				activeChar.sendMessage("Instance destroyed");
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Use //destroyinstance id");
			}
		}
		return true;
	}
	
	private String getStringCords(int instId)
	{
		String cords = "";
		
		int size = InstanceManager.getInstance().getPlayers(instId).size();
		
		L2PcInstance player = null;
		
		if (size > 0)
			player = InstanceManager.getInstance().getPlayers(instId).get(Rnd.get(size));
		
		if (player != null)
		{
			cords += player.getX() + " " + player.getY() + " " + player.getZ() + " " + instId;
		}
		
		return cords;
	}
	
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}