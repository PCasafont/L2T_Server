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

package l2server.gameserver.model.actor.instance;

import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.CharTemplateTable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.chars.L2PcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Pere
 */
public class L2StatueInstance extends L2Npc
{
	private int _recordId;
	private int _socialId;
	private int _socialFrame;
	private L2PcTemplate _template;
	private int _sex;
	private int _hairStyle;
	private int _hairColor;
	private int _face;
	private int _necklace = 0;
	private int _head = 0;
	private int _rHand = 0;
	private int _lHand = 0;
	private int _gloves = 0;
	private int _chest = 0;
	private int _pants = 0;
	private int _boots = 0;
	private int _cloak = 0;
	private int _hair1 = 0;
	private int _hair2 = 0;

	/*
	 * To create 1 instance:
	 *
		StatsSet ss = new StatsSet();
		ss.set("id", 0);
		ss.set("type", "L2Npc");
		ss.set("name", "");
		L2NpcTemplate t = new L2NpcTemplate(ss);
		new L2StatueInstance(IdFactory.getInstance().getNextId(), t, 0, _charObjId, _x, _y, _z, 0);
	 */

	public L2StatueInstance(int objectId, L2NpcTemplate template, int recordId, int playerObjId, int x, int y, int z, int heading)
	{
		super(objectId, template);

		_recordId = recordId;
		_socialId = 0;
		_socialFrame = 0;

		setInstanceType(InstanceType.L2StatueInstance);

		setIsInvul(true);

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement(
					"SELECT char_name, templateId, sex, hairStyle, hairColor, face FROM characters WHERE charId = ?");
			st.setInt(1, playerObjId);
			ResultSet rs = st.executeQuery();
			if (rs.next())
			{
				setName(rs.getString("char_name"));
				_template = CharTemplateTable.getInstance().getTemplate(rs.getInt("templateId"));
				_sex = rs.getInt("sex");
				_hairStyle = rs.getInt("hairStyle");
				_hairColor = rs.getInt("hairColor");
				_face = rs.getInt("face");
			}
			rs.close();
			st.close();

			st = con.prepareStatement("SELECT loc_data, item_id FROM items WHERE owner_id = ? AND loc = \"PAPERDOLL\"");
			st.setInt(1, playerObjId);
			rs = st.executeQuery();
			while (rs.next())
			{
				switch (rs.getInt("loc_data"))
				{
					case Inventory.PAPERDOLL_NECK:
						_necklace = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_HEAD:
						_head = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_RHAND:
						_rHand = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_LHAND:
						_lHand = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_GLOVES:
						_gloves = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_CHEST:
						_chest = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_LEGS:
						_pants = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_FEET:
						_boots = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_CLOAK:
						_cloak = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_HAIR:
						_hair1 = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_HAIR2:
						_hair2 = rs.getInt("item_id");
						break;
				}
			}
			rs.close();
			st.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		if (_template != null)
		{
			setXYZ(x, y, z);
			setHeading(heading);

			getTemplate().baseWalkSpd = 0;
			getTemplate().baseRunSpd = 0;

			spawnMe();
		}
	}

	public int getRecordId()
	{
		return _recordId;
	}

	public int getSocialId()
	{
		return _socialId;
	}

	public int getSocialFrame()
	{
		return _socialFrame;
	}

	public int getClassId()
	{
		return _template.startingClassId;
	}

	public int getRace()
	{
		return _template.race.ordinal();
	}

	public int getSex()
	{
		return _sex;
	}

	public int getHairStyle()
	{
		return _hairStyle;
	}

	public int getHairColor()
	{
		return _hairColor;
	}

	public int getFace()
	{
		return _face;
	}

	public int getNecklace()
	{
		return _necklace;
	}

	public int getHead()
	{
		return _head;
	}

	public int getRHand()
	{
		return _rHand;
	}

	public int getLHand()
	{
		return _lHand;
	}

	public int getGloves()
	{
		return _gloves;
	}

	public int getChest()
	{
		return _chest;
	}

	public int getPants()
	{
		return _pants;
	}

	public int getBoots()
	{
		return _boots;
	}

	public int getCloak()
	{
		return _cloak;
	}

	public int getHair1()
	{
		return _hair1;
	}

	public int getHair2()
	{
		return _hair2;
	}
}
