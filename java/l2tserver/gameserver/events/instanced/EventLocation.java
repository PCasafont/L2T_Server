package l2tserver.gameserver.events.instanced;

import java.util.ArrayList;

import l2tserver.gameserver.instancemanager.ZoneManager;
import l2tserver.gameserver.model.zone.type.L2EventZone;
import l2tserver.util.Point3D;
import l2tserver.util.xml.XmlNode;

/**
 * @author  Hidari
 */
public class EventLocation
{
	private final int _id;
	private final String _name;
	private final ArrayList<Point3D> _spawns;
	private final int _globalZ;
	private final int _maxTeamPlayers;
	private final boolean _hill;
	
	private L2EventZone _zone = null;
	
	public EventLocation(XmlNode node)
	{
		_id = node.getInt("id");
		_name = node.getString("name");
		_globalZ = node.getInt("globalZ");
		_maxTeamPlayers = node.getInt("maxTeamPlayers");
		_hill = node.getBool("hill", false);

		_spawns = new ArrayList<Point3D>();
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
			return _spawns.get(id);
		
		return new Point3D(0, 0, 0);
	}
	
	public L2EventZone getZone()
	{
		if (_zone == null)
			_zone = ZoneManager.getInstance().getZoneById(_id + L2EventZone.BASE_ID, L2EventZone.class);
		
		return _zone;
	}
}
