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
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PlayerStatDataTable
{
	public class PlayerStatData
	{
		public final float HP;
		public final float MP;
		public final float CP;

		public PlayerStatData(float hp, float mp, float cp)
		{
			HP = hp;
			MP = mp;
			CP = cp;
		}
	}

	private final Map<Integer, PlayerStatData> _regenData = new HashMap<>();
	private final Map<Integer, Map<Integer, PlayerStatData>> _classMaxData = new HashMap<>();

	public static PlayerStatDataTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private PlayerStatDataTable()
	{
		parseData();
	}

	public void reload()
	{
		parseData();
	}

	public void parseData()
	{
		_regenData.clear();
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "stats/regenData.xml");
		XmlDocument doc = new XmlDocument(file);
		for (XmlNode n : doc.getFirstChild().getChildren())
		{
			if (!n.getName().equalsIgnoreCase("regen"))
			{
				continue;
			}

			int level = n.getInt("level");
			float hp = n.getFloat("hp");
			float mp = n.getFloat("mp");
			float cp = n.getFloat("cp");
			_regenData.put(level, new PlayerStatData(hp, mp, cp));
		}

		Log.info("PlayerStatData: Loaded regen data for " + _regenData.size() + " levels.");

		_classMaxData.clear();
		file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "stats/classStats.xml");
		doc = new XmlDocument(file);
		for (XmlNode n : doc.getFirstChild().getChildren())
		{
			if (!n.getName().equalsIgnoreCase("class"))
			{
				continue;
			}

			Map<Integer, PlayerStatData> statData = new HashMap<>();
			for (XmlNode statNode : n.getChildren())
			{
				if (!statNode.getName().equalsIgnoreCase("statData"))
				{
					continue;
				}

				int level = statNode.getInt("level");
				float hp = statNode.getFloat("hp");
				float mp = statNode.getFloat("mp");
				float cp = statNode.getFloat("cp");
				statData.put(level, new PlayerStatData(hp, mp, cp));
			}

			String[] classIds = n.getString("id").split(",");
			for (String classId : classIds)
			{
				_classMaxData.put(Integer.parseInt(classId), statData);
			}
		}

		Log.info("PlayerStatData: Loaded class max stat data for " + _classMaxData.size() + " classes.");
	}

	public float getHpRegen(int level)
	{
		PlayerStatData data = _regenData.get(level);
		if (data == null)
		{
			return 0.0f;
		}

		return data.HP;
	}

	public float getMpRegen(int level)
	{
		PlayerStatData data = _regenData.get(level);
		if (data == null)
		{
			return 0.0f;
		}

		return data.MP;
	}

	public float getCpRegen(int level)
	{
		PlayerStatData data = _regenData.get(level);
		if (data == null)
		{
			return 0.0f;
		}

		return data.CP;
	}

	public float getMaxHp(int classId, int level)
	{
		Map<Integer, PlayerStatData> classData = _classMaxData.get(classId);
		if (classData == null)
		{
			return 0.0f;
		}

		PlayerStatData data = classData.get(level);
		if (data == null)
		{
			return 0.0f;
		}

		return data.HP;
	}

	public float getMaxMp(int classId, int level)
	{
		Map<Integer, PlayerStatData> classData = _classMaxData.get(classId);
		if (classData == null)
		{
			return 0.0f;
		}

		PlayerStatData data = classData.get(level);
		if (data == null)
		{
			return 0.0f;
		}

		return data.MP;
	}

	public float getMaxCp(int classId, int level)
	{
		Map<Integer, PlayerStatData> classData = _classMaxData.get(classId);
		if (classData == null)
		{
			return 0.0f;
		}

		PlayerStatData data = classData.get(level);
		if (data == null)
		{
			return 0.0f;
		}

		return data.CP;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final PlayerStatDataTable _instance = new PlayerStatDataTable();
	}
}
