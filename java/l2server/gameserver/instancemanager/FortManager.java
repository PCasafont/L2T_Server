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

package l2server.gameserver.instancemanager;

import l2server.Config;
import l2server.gameserver.InstanceListManager;
import l2server.gameserver.model.CombatFlag;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.entity.Fort;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FortManager implements InstanceListManager
{
	private List<Fort> _forts = new ArrayList<>();

	public static FortManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private FortManager()
	{
		load();
	}

	@Override
	public void load()
	{
		Log.info("Initializing FortManager");

		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "forts.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode n : doc.getFirstChild().getChildren())
		{
			if (!n.getName().equalsIgnoreCase("fort"))
			{
				continue;
			}

			int fortId = n.getInt("id");
			if (Config.isServer(Config.TENKAI) && fortId == 113)
			{
				continue;
			}

			String name = n.getString("name");
			int type = n.getInt("type");
			int flagPoleId = n.getInt("flagPoleId");
			Fort fort = new Fort(fortId, name, type, flagPoleId);
			for (XmlNode subNode : n.getChildren())
			{
				if (subNode.getName().equalsIgnoreCase("envoy"))
				{
					int npcId = subNode.getInt("npcId");
					int castleId = subNode.getInt("castleId");
					fort.addEnvoyCastleId(npcId, castleId);
				}
				else if (subNode.getName().equalsIgnoreCase("flag"))
				{
					int itemId = subNode.getInt("itemId");
					int x = subNode.getInt("x");
					int y = subNode.getInt("y");
					int z = subNode.getInt("z");
					fort.getFlags().add(new CombatFlag(x, y, z, 0, itemId));
				}
			}

			_forts.add(fort);
		}
		Log.info("Loaded: " + _forts.size() + " forts");
	}

	public final int findNearestFortIndex(L2Object obj)
	{
		return findNearestFortIndex(obj, Long.MAX_VALUE);
	}

	public final int findNearestFortIndex(L2Object obj, long maxDistance)
	{
		int index = getFortIndex(obj);
		if (index < 0)
		{
			double distance;
			Fort fort;
			for (int i = 0; i < _forts.size(); i++)
			{
				fort = _forts.get(i);
				if (fort == null)
				{
					continue;
				}
				distance = fort.getDistance(obj);
				if (maxDistance > distance)
				{
					maxDistance = (long) distance;
					index = i;
				}
			}
		}
		return index;
	}

	// =========================================================
	// Property - Public
	public final Fort getFortById(int fortId)
	{
		for (Fort f : _forts)
		{
			if (f.getFortId() == fortId)
			{
				return f;
			}
		}
		return null;
	}

	public final Fort getFortByOwner(L2Clan clan)
	{
		for (Fort f : _forts)
		{
			if (f.getOwnerClan() == clan)
			{
				return f;
			}
		}
		return null;
	}

	public final Fort getFort(String name)
	{
		for (Fort f : _forts)
		{
			if (f.getName().equalsIgnoreCase(name.trim()))
			{
				return f;
			}
		}
		return null;
	}

	public final Fort getFort(int x, int y, int z)
	{
		for (Fort f : _forts)
		{
			if (f.checkIfInZone(x, y, z))
			{
				return f;
			}
		}
		return null;
	}

	public final Fort getFort(L2Object activeObject)
	{
		return getFort(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final int getFortIndex(int fortId)
	{
		Fort fort;
		for (int i = 0; i < _forts.size(); i++)
		{
			fort = _forts.get(i);
			if (fort != null && fort.getFortId() == fortId)
			{
				return i;
			}
		}
		return -1;
	}

	public final int getFortIndex(L2Object activeObject)
	{
		return getFortIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final int getFortIndex(int x, int y, int z)
	{
		Fort fort;
		for (int i = 0; i < _forts.size(); i++)
		{
			fort = _forts.get(i);
			if (fort != null && fort.checkIfInZone(x, y, z))
			{
				return i;
			}
		}
		return -1;
	}

	public final List<Fort> getForts()
	{
		return _forts;
	}

	@Override
	public void updateReferences()
	{
	}

	@Override
	public void activateInstances()
	{
		for (final Fort fort : _forts)
		{
			fort.activateInstance();
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final FortManager _instance = new FortManager();
	}
}
