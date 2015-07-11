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

import l2tserver.gameserver.handler.IAdminCommandHandler;
import l2tserver.gameserver.instancemanager.GamePlayWatcher;
import l2tserver.gameserver.model.actor.L2Character;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.serverpackets.SpecialCamera;

public class AdminCamera implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_camera"
	};
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		try
		{
			final L2Character target = (L2Character)activeChar.getTarget();
			final String[] com = command.split(" ");
			
			target.broadcastPacket(new SpecialCamera(target.getObjectId(), Integer.parseInt(com[1]),
					Integer.parseInt(com[2]), Integer.parseInt(com[3]), Integer.parseInt(com[4]),
					Integer.parseInt(com[5]), Integer.parseInt(com[6]), Integer.parseInt(com[7]),
					Integer.parseInt(com[8]), Integer.parseInt(com[9])));
		}
		catch (Exception e)
		{
			/*List<Point3D> positions = new FastList<Point3D>();
			for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
			{
				if (player == null)
					continue;
				
				positions.add(player.getPosition().getWorldPosition());
			}

			Point3D a = null;
			Point3D b = null;
			double longest = 0.0;
			for (Point3D p1 : positions)
			{
				for (Point3D p2 : positions)
				{
					double distance = Util.calculateDistance(p1.getX(), p1.getY(), p2.getX(), p2.getY());
					if (distance > longest)
					{
						a = p1;
						b = p2;
						longest = distance;
					}
				}
			}

			Point3D dir = new Point3D(a.getX() - b.getX(), a.getY() - b.getY(), a.getZ() - b.getZ());
			Point3D cross = new Point3D(-dir.getZ(), 0, dir.getX());
			
			Point3D pivot = null;
			double shortest = longest;
			for (Point3D p : positions)
			{
				
			}
			
			int objId = activeChar.getObjectId();
			if (activeChar.getTarget() != null)
				objId = activeChar.getTarget().getObjectId();
			activeChar.sendPacket(new SpecialCamera(objId,
					830,	// Distance
					270,	// Yaw
					10,		// Pitch
					3000,	// Time
					3000,	// Duration
					0,		// Turn
					0,		// Rise
					0,		// WideScreen
					0));*/
			
			L2PcInstance watcher = activeChar;
			if (activeChar.getTarget() instanceof L2PcInstance)
				watcher = (L2PcInstance)activeChar.getTarget();
			
			GamePlayWatcher.getInstance().makeWatcher(watcher);
			
			activeChar.sendMessage("Usage: //camera dist yaw pitch time duration turn rise widescreen unknown");
			return false;
		}
		return true;
	}
	
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}