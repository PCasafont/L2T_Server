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

import l2server.Config;
import l2server.gameserver.Reloadable;
import l2server.gameserver.ReloadableManager;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.L2FlyMove;
import l2server.gameserver.model.L2FlyMove.L2FlyMoveChoose;
import l2server.gameserver.model.L2FlyMove.L2FlyMoveOption;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.L2WorldRegion;
import l2server.gameserver.model.zone.form.ZoneCylinder;
import l2server.gameserver.model.zone.type.L2FlyMoveZone;
import l2server.log.Log;
import l2server.util.Point3D;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;

/**
 * @author Pere
 */
public class FlyMoveTable implements Reloadable
{
	private static FlyMoveTable _instance;

	public static FlyMoveTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new FlyMoveTable();
		}

		return _instance;
	}

	private FlyMoveTable()
	{
		if (!Config.IS_CLASSIC)
		{
			load();
			ReloadableManager.getInstance().register("sayune", this);
		}
	}

	private void load()
	{
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "flyMoves.xml");
		XmlDocument doc = new XmlDocument(file);

		int count = 0;
		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("move"))
					{
						int id = d.getInt("id");
						int x = d.getInt("x");
						int y = d.getInt("y");
						int z = d.getInt("z");

						L2FlyMoveZone zone = new L2FlyMoveZone(id);
						zone.setZone(new ZoneCylinder(x, y, z - 100, z + 200, 40));

						L2FlyMove move = new L2FlyMove(id);

						for (XmlNode moveNode : d.getChildren())
						{
							if (moveNode.getName().equalsIgnoreCase("choose"))
							{
								int at = moveNode.getInt("at");
								L2FlyMoveChoose c = move.new L2FlyMoveChoose(at);
								for (XmlNode optionNode : moveNode.getChildren())
								{
									if (optionNode.getName().equalsIgnoreCase("option"))
									{
										int start = optionNode.getInt("start");
										int end = optionNode.getInt("end");
										int last = optionNode.getInt("last", -1);
										L2FlyMoveOption o = move.new L2FlyMoveOption(start, end, last);

										c.addOption(o);
									}
								}
								move.addChoose(at, c);
							}
							else if (moveNode.getName().equalsIgnoreCase("step"))
							{
								id = moveNode.getInt("id");
								x = moveNode.getInt("x");
								y = moveNode.getInt("y");
								z = moveNode.getInt("z");
								Point3D p = new Point3D(x, y, z);

								move.addStep(id, p);
							}
						}

						zone.setFlyMove(move);
						ZoneManager.getInstance().addZone(zone.getId(), zone);

						// Register the zone into any world region it
						// intersects with...
						int ax, ay, bx, by;
						for (x = 0; x < worldRegions.length; x++)
						{
							for (y = 0; y < worldRegions[x].length; y++)
							{
								ax = x - L2World.OFFSET_X << L2World.SHIFT_BY;
								bx = x + 1 - L2World.OFFSET_X << L2World.SHIFT_BY;
								ay = y - L2World.OFFSET_Y << L2World.SHIFT_BY;
								by = y + 1 - L2World.OFFSET_Y << L2World.SHIFT_BY;

								if (zone.getZone().intersectsRectangle(ax, bx, ay, by))
								{
									if (Config.DEBUG)
									{
										Log.info("Zone (" + move.getId() + ") added to: " + x + " " + y);
									}
									worldRegions[x][y].addZone(zone);
								}
							}
						}
						count++;
					}
				}
			}
		}

		Log.info("FlyMoveTable: Loaded " + count + " fly moves.");
	}

	@Override
	public boolean reload()
	{
		ZoneManager.getInstance().reload();
		load();
		return true;
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return "Sayune reloaded!";
	}
}
