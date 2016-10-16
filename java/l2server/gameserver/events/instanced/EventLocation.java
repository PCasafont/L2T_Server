package l2server.gameserver.events.instanced;

import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.zone.type.L2TenkaiEventZone;
import l2server.util.Point3D;
import l2server.util.xml.XmlNode;

import java.util.ArrayList;

/**
 * @author Hidari
 */
public class EventLocation
{
	private final int id;
	private final String name;
	private final ArrayList<Point3D> spawns;
	private final int globalZ;
	private final int maxTeamPlayers;
	private final boolean hill;

	private L2TenkaiEventZone zone = null;

	public EventLocation(XmlNode node)
	{
		this.id = node.getInt("id");
		this.name = node.getString("name");
		this.globalZ = node.getInt("globalZ");
		this.maxTeamPlayers = node.getInt("maxTeamPlayers");
		this.hill = node.getBool("hill", false);

		this.spawns = new ArrayList<>();
		for (XmlNode subNode : node.getChildren())
		{
			if (subNode.getName().equals("spawn"))
			{
				int x = subNode.getInt("x");
				int y = subNode.getInt("y");
				int z = subNode.getInt("z");
				this.spawns.add(new Point3D(x, y, z));
			}
		}
	}

	public int getMaxPlayers()
	{
		return this.maxTeamPlayers * this.spawns.size();
	}

	public int getId()
	{
		return this.id;
	}

	public String getName()
	{
		return this.name;
	}

	public boolean isHill()
	{
		return this.hill;
	}

	public int getGlobalZ()
	{
		return this.globalZ;
	}

	public int getTeamCount()
	{
		return this.spawns.size();
	}

	public int getMaxTeamPlayers()
	{
		return this.maxTeamPlayers;
	}

	public Point3D getSpawn(int id)
	{
		if (id < this.spawns.size())
		{
			return this.spawns.get(id);
		}

		return new Point3D(0, 0, 0);
	}

	public L2TenkaiEventZone getZone()
	{
		if (this.zone == null)
		{
			this.zone = ZoneManager.getInstance().getZoneById(this.id + L2TenkaiEventZone.BASE_ID, L2TenkaiEventZone.class);
		}

		return this.zone;
	}
}
