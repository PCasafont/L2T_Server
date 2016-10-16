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
	private int recordId;
	private int socialId;
	private int socialFrame;
	private L2PcTemplate template;
	private int sex;
	private int hairStyle;
	private int hairColor;
	private int face;
	private int necklace = 0;
	private int head = 0;
	private int rHand = 0;
	private int lHand = 0;
	private int gloves = 0;
	private int chest = 0;
	private int pants = 0;
	private int boots = 0;
	private int cloak = 0;
	private int hair1 = 0;
	private int hair2 = 0;

	/*
	 * To create 1 instance:
	 *
		StatsSet ss = new StatsSet();
		ss.set("id", 0);
		ss.set("type", "L2Npc");
		ss.set("name", "");
		L2NpcTemplate t = new L2NpcTemplate(ss);
		new L2StatueInstance(IdFactory.getInstance().getNextId(), t, 0, this.charObjId, this.x, this.y, this.z, 0);
	 */

	public L2StatueInstance(int objectId, L2NpcTemplate template, int recordId, int playerObjId, int x, int y, int z, int heading)
	{
		super(objectId, template);

		this.recordId = recordId;
		this.socialId = 0;
		this.socialFrame = 0;

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
				this.template = CharTemplateTable.getInstance().getTemplate(rs.getInt("templateId"));
				this.sex = rs.getInt("sex");
				this.hairStyle = rs.getInt("hairStyle");
				this.hairColor = rs.getInt("hairColor");
				this.face = rs.getInt("face");
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
						this.necklace = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_HEAD:
						this.head = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_RHAND:
						this.rHand = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_LHAND:
						this.lHand = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_GLOVES:
						this.gloves = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_CHEST:
						this.chest = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_LEGS:
						this.pants = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_FEET:
						this.boots = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_CLOAK:
						this.cloak = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_HAIR:
						this.hair1 = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_HAIR2:
						this.hair2 = rs.getInt("item_id");
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

		if (this.template != null)
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
		return this.recordId;
	}

	public int getSocialId()
	{
		return this.socialId;
	}

	public int getSocialFrame()
	{
		return this.socialFrame;
	}

	public int getClassId()
	{
		return this.template.startingClassId;
	}

	public int getRace()
	{
		return this.template.race.ordinal();
	}

	public int getSex()
	{
		return this.sex;
	}

	public int getHairStyle()
	{
		return this.hairStyle;
	}

	public int getHairColor()
	{
		return this.hairColor;
	}

	public int getFace()
	{
		return this.face;
	}

	public int getNecklace()
	{
		return this.necklace;
	}

	public int getHead()
	{
		return this.head;
	}

	public int getRHand()
	{
		return this.rHand;
	}

	public int getLHand()
	{
		return this.lHand;
	}

	public int getGloves()
	{
		return this.gloves;
	}

	public int getChest()
	{
		return this.chest;
	}

	public int getPants()
	{
		return this.pants;
	}

	public int getBoots()
	{
		return this.boots;
	}

	public int getCloak()
	{
		return this.cloak;
	}

	public int getHair1()
	{
		return this.hair1;
	}

	public int getHair2()
	{
		return this.hair2;
	}
}
