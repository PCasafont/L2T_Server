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
	private final int _id;
	private final String _name;
	private final ArrayList<Point3D> _spawns;
	private final int _globalZ;
	private final int _maxTeamPlayers;
	private final boolean _hill;

	private L2TenkaiEventZone _zone = null;

	public EventLocation(XmlNode node)
	{
		_id = node.getInt("id");
		_name = node.getString("name");
		_globalZ = node.getInt("globalZ");
		_maxTeamPlayers = node.getInt("maxTeamPlayers");
		_hill = node.getBool("hill", false);

		_spawns = new ArrayList<>();
		for (XmlNode subNode : node.getChildren())
		{
			if (subNode.getName().equals("spawn"))
			{
				int x = subNode.getInt("x");
				int y = subNode.getInt("y");
				int z = subNode.getInt("z");
				_spawns.add(new Point3D(x, y, z));
			}
		}
	}

	public int getMaxPlayers()
	{
		return _maxTeamPlayers * _spawns.size();
	}

	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public boolean isHill()
	{
		return _hill;
	}

	public int getGlobalZ()
	{
		return _globalZ;
	}

	public int getTeamCount()
	{
		return _spawns.size();
	}

	public int getMaxTeamPlayers()
	{
		return _maxTeamPlayers;
	}

	public Point3D getSpawn(int id)
	{
		if (id < _spawns.size())
		{
			return _spawns.get(id);
		}

		return new Point3D(0, 0, 0);
	}

	public L2TenkaiEventZone getZone()
	{
		if (_zone == null)
		{
			_zone = ZoneManager.getInstance().getZoneById(_id + L2TenkaiEventZone.BASE_ID, L2TenkaiEventZone.class);
		}

		return _zone;
	}
}
