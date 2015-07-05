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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import l2server.Config;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

public class PlayerRegenDataTable
{
	private class PlayerRegenData
	{
		public final float HP;
		public final float MP;
		public final float CP;
		
		public PlayerRegenData(float hp, float mp, float cp)
		{
			HP = hp;
			MP = mp;
			CP = cp;
		}
	}
	
	private final Map<Integer, PlayerRegenData> _regenData = new HashMap<Integer, PlayerRegenData>();
	
	public static PlayerRegenDataTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private PlayerRegenDataTable()
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
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "regenData.xml");
		XmlDocument doc = new XmlDocument(file);
		
		for (XmlNode n : doc.getFirstChild().getChildren())
		{
			if (n.getName().equalsIgnoreCase("regen"))
			{
				int level = n.getInt("level");
				float hp = n.getFloat("hp");
				float mp = n.getFloat("mp");
				float cp = n.getFloat("cp");
				_regenData.put(level, new PlayerRegenData(hp, mp, cp));
			}
		}
		Log.info("PlayerRegenData: Loaded data for " + _regenData.size() + " levels.");
	}
	
	public float getHpRegen(int level)
	{
		PlayerRegenData data = _regenData.get(level);
		if (data != null)
			return data.HP;
		
		return 0.0f;
	}
	
	public float getMpRegen(int level)
	{
		PlayerRegenData data = _regenData.get(level);
		if (data != null)
			return data.MP;
		
		return 0.0f;
	}
	
	public float getCpRegen(int level)
	{
		PlayerRegenData data = _regenData.get(level);
		if (data != null)
			return data.CP;
		
		return 0.0f;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final PlayerRegenDataTable _instance = new PlayerRegenDataTable();
	}
}