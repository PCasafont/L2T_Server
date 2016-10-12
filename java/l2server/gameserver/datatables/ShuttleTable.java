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

package l2server.gameserver.datatables;

import gnu.trove.TIntObjectHashMap;
import l2server.Config;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2ShuttleInstance;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.L2CharTemplate;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;

/**
 * @author Pere
 */
public class ShuttleTable
{
	private TIntObjectHashMap<L2ShuttleInstance> _shuttles = new TIntObjectHashMap<>();

	private static ShuttleTable _instance;

	public static ShuttleTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ShuttleTable();
		}

		return _instance;
	}

	private ShuttleTable()
	{
		readShuttles();
	}

	private void readShuttles()
	{
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "shuttles.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode shuttleNode : n.getChildren())
				{
					if (shuttleNode.getName().equalsIgnoreCase("shuttle"))
					{
						int id = shuttleNode.getInt("id");

						StatsSet npcDat = new StatsSet();
						npcDat.set("npcId", id);
						npcDat.set("level", 0);

						npcDat.set("collisionRadius", 0);
						npcDat.set("collisionHeight", 0);
						npcDat.set("type", "");
						npcDat.set("walkSpd", 300);
						npcDat.set("eunSpd", 300);
						npcDat.set("name", "AirShip");
						npcDat.set("hpMax", 50000);
						npcDat.set("hpReg", 3.e-3f);
						npcDat.set("mpReg", 3.e-3f);
						npcDat.set("pDef", 100);
						npcDat.set("mDef", 100);

						L2ShuttleInstance shuttle =
								new L2ShuttleInstance(IdFactory.getInstance().getNextId(), new L2CharTemplate(npcDat),
										id);
						L2World.getInstance().storeObject(shuttle);

						for (XmlNode stopNode : shuttleNode.getChildren())
						{
							if (stopNode.getName().equalsIgnoreCase("stop"))
							{
								int x = stopNode.getInt("x");
								int y = stopNode.getInt("y");
								int z = stopNode.getInt("z");
								int time = stopNode.getInt("time");
								int doorId = stopNode.getInt("doorId");
								int outerDoorId = stopNode.getInt("outerDoorId");
								int oustX = stopNode.getInt("oustX");
								int oustY = stopNode.getInt("oustY");
								int oustZ = stopNode.getInt("oustZ");

								shuttle.addStop(x, y, z, time, doorId, outerDoorId, oustX, oustY, oustZ);
							}
						}

						shuttle.moveToNextRoutePoint();
						_shuttles.put(id, shuttle);
					}
				}
			}
		}
		Log.info("ShuttleTable: Loaded " + _shuttles.size() + " shuttles.");
	}
}
