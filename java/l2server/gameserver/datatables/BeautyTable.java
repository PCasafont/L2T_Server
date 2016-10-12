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
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LasTravel
 * @author Pere
 */

public class BeautyTable implements Reloadable
{
	public class BeautyTemplate
	{
		private int _id;
		private Map<Integer, BeautyInfo> _hairStyles = new HashMap<>();
		private Map<Integer, BeautyInfo> _faceStyles = new HashMap<>();
		private Map<Integer, BeautyInfo> _hairColors = new HashMap<>();

		public BeautyTemplate(int id)
		{
			_id = id;
		}

		public int getId()
		{
			return _id;
		}

		public Map<Integer, BeautyInfo> getHairStyles()
		{
			return _hairStyles;
		}

		public Map<Integer, BeautyInfo> getFaceStyles()
		{
			return _faceStyles;
		}

		public Map<Integer, BeautyInfo> getHairColors()
		{
			return _hairColors;
		}
	}

	public class BeautyInfo
	{
		private int _id;
		private int _parentId;
		private int _unk;
		private int _adenaCost;
		private int _ticketCost;

		private BeautyInfo(int id, int parentId, int unk, int adena, int tickets)
		{
			_id = id;
			_parentId = parentId;
			_unk = unk;
			_adenaCost = adena;
			_ticketCost = tickets;
		}

		public int getId()
		{
			return _id;
		}

		public int getParentId()
		{
			return _parentId;
		}

		public int getUnk()
		{
			return _unk;
		}

		public int getAdenaPrice()
		{
			return _adenaCost;
		}

		public int getTicketPrice()
		{
			return _ticketCost;
		}
	}

	private Map<Integer, BeautyTemplate> _beautyTable = new HashMap<>();

	private BeautyTable()
	{
		if (!Config.IS_CLASSIC)
		{
			reload();
			ReloadableManager.getInstance().register("beauty", this);
		}
	}

	@Override
	public boolean reload()
	{
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "beautyShop.xml");

		XmlDocument doc = new XmlDocument(file);
		_beautyTable.clear();
		doc.getChildren().stream().filter(n -> n.getName().equalsIgnoreCase("list")).forEachOrdered(n ->
		{
			BeautyTemplate template = new BeautyTemplate(0);
			for (XmlNode d : n.getChildren())
			{
				boolean isHairStyle = d.getName().equalsIgnoreCase("hairStyle");
				boolean isFaceStyle = d.getName().equalsIgnoreCase("faceStyle");
				boolean isHairColor = d.getName().equalsIgnoreCase("hairColor");
				if (isHairStyle || isFaceStyle || isHairColor)
				{
					int id = d.getInt("id");
					int parentId = d.getInt("parentId", 0);
					int unk = d.getInt("unk");
					int adenaCost = d.getInt("adenaCost");
					int ticketCost = d.getInt("ticketCost");

					BeautyInfo info = new BeautyInfo(id, parentId, unk, adenaCost, ticketCost);

					if (isHairStyle)
					{
						template.getHairStyles().put(id, info);
					}
					else if (isFaceStyle)
					{
						template.getFaceStyles().put(id, info);
					}
					else
					{
						template.getHairColors().put(id, info);
					}
				}
			}

			_beautyTable.put(0, template);

			Log.info("BeautyTable: Loaded " + template.getHairStyles().size() + " hair styles, " +
					template.getFaceStyles().size() + " face styles and " + template.getHairColors().size() +
					" hair colors!");
		});

		return false;
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return "Beauty Table reloaded";
	}

	public BeautyTemplate getTemplate(int id)
	{
		return _beautyTable.get(id);
	}

	public static BeautyTable getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final BeautyTable _instance = new BeautyTable();
	}
}
