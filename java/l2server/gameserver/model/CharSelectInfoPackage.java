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

package l2server.gameserver.model;

import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.templates.chars.L2PcTemplate;
import lombok.Getter;
import lombok.Setter;

/**
 * Used to Store data sent to Client for Character
 * Selection screen.
 *
 * @version $Revision: 1.2.2.2.2.4 $ $Date: 2005/03/27 15:29:33 $
 */
public class CharSelectInfoPackage
{
	@Getter @Setter private String name;
	@Getter @Setter private int objectId = 0;
	@Getter @Setter private int charId = 0x00030b7a;
	@Getter @Setter private long exp = 0;
	@Getter @Setter private long sp = 0;
	@Getter @Setter private int clanId = 0;
	@Getter private L2PcTemplate template = null;
	@Setter private int classId = 0;
	@Getter @Setter private long deleteTimer = 0L;
	@Getter @Setter private long lastAccess = 0L;
	@Getter @Setter private int face = 0;
	@Getter @Setter private int hairStyle = 0;
	@Getter @Setter private int hairColor = 0;
	@Getter @Setter private int sex = 0;
	@Getter @Setter private int level = 1;
	@Getter @Setter private int maxHp = 0;
	@Getter @Setter private double currentHp = 0;
	@Getter @Setter private int maxMp = 0;
	@Getter @Setter private double currentMp = 0;
	private int[][] paperdoll;
	@Getter private int reputation = 0;
	@Getter private int pkKills = 0;
	private int pvpKills = 0;
	@Getter @Setter private long augmentationId = 0;
	@Getter private int transformId = 0;
	@Getter @Setter private int x = 0;
	@Getter @Setter private int y = 0;
	@Getter @Setter private int z = 0;
	@Setter private boolean showHat = true;
	@Getter @Setter private int vitalityPoints = 0;
	@Getter private int vitalityLevel = 0;

	/**
	 */
	public CharSelectInfoPackage(int objectId, String name)
	{
		setObjectId(objectId);
		this.name = name;
		paperdoll = PcInventory.restoreVisibleInventory(objectId);
	}

	public int getCurrentClass()
	{
		return classId;
	}

	public int getPaperdollObjectId(int slot)
	{
		return paperdoll[slot][0];
	}

	public int getPaperdollItemId(int slot)
	{
		return paperdoll[slot][1];
	}

	public void setTemplate(L2PcTemplate t)
	{
		template = t;
	}

	public int getEnchantEffect()
	{
		if (paperdoll[Inventory.PAPERDOLL_RHAND][2] > 0)
		{
			return paperdoll[Inventory.PAPERDOLL_RHAND][2];
		}
		return paperdoll[Inventory.PAPERDOLL_RHAND][2];
	}

	public void setReputation(int k)
	{
		reputation = k;
	}

	public void setPkKills(int PkKills)
	{
		pkKills = PkKills;
	}

	public void setPvPKills(int PvPKills)
	{
		pvpKills = PvPKills;
	}

	public int getPvPKills()
	{
		return pvpKills;
	}

	public void setTransformId(int id)
	{
		transformId = id;
	}

	public boolean isShowingHat()
	{
		return showHat;
	}
}
