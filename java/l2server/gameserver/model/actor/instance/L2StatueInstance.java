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
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Pere
 */
public class L2StatueInstance extends L2Npc
{
	@Getter private int recordId;
	@Getter private int socialId;
	@Getter private int socialFrame;
	private L2PcTemplate template;
	@Getter private int sex;
	@Getter private int hairStyle;
	@Getter private int hairColor;
	@Getter private int face;
	@Getter private int necklace = 0;
	@Getter private int head = 0;
	@Getter private int rHand = 0;
	@Getter private int lHand = 0;
	@Getter private int gloves = 0;
	@Getter private int chest = 0;
	@Getter private int pants = 0;
	@Getter private int boots = 0;
	@Getter private int cloak = 0;
	@Getter private int hair1 = 0;
	@Getter private int hair2 = 0;

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
		socialId = 0;
		socialFrame = 0;

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
				sex = rs.getInt("sex");
				hairStyle = rs.getInt("hairStyle");
				hairColor = rs.getInt("hairColor");
				face = rs.getInt("face");
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
						necklace = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_HEAD:
						head = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_RHAND:
						rHand = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_LHAND:
						lHand = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_GLOVES:
						gloves = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_CHEST:
						chest = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_LEGS:
						pants = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_FEET:
						boots = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_CLOAK:
						cloak = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_HAIR:
						hair1 = rs.getInt("item_id");
						break;
					case Inventory.PAPERDOLL_HAIR2:
						hair2 = rs.getInt("item_id");
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




	public int getClassId()
	{
		return template.startingClassId;
	}

	public int getRace()
	{
		return template.race.ordinal();
	}















}
