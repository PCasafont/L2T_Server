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

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.funcs.Func;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class EnchantEffect
{
	private final int id;
	private final int rarity;
	private final int slot;

	private int skillId = 0;
	private int skillLevel = 0;
	private final List<Func> funcs = new ArrayList<>();

	public EnchantEffect(int id, int rarity, int slot)
	{
		this.id = id;
		this.rarity = rarity;
		this.slot = slot;
	}

	public void setSkill(int skillId, int skillLevel)
	{
		this.skillId = skillId;
		this.skillLevel = skillLevel;
	}

	public void addFunc(Func func)
	{
		funcs.add(func);
	}

	public int getId()
	{
		return id;
	}

	public int getRarity()
	{
		return rarity;
	}

	public int getSlot()
	{
		return slot;
	}

	public L2Skill getSkill()
	{
		if (skillId == 0)
		{
			return null;
		}

		return SkillTable.getInstance().getInfo(skillId, skillLevel);
	}

	public void applyBonus(L2PcInstance player)
	{
		player.removeStatsOwner(this);
		for (Func f : funcs)
		{
			player.addStatFunc(f);
		}
	}

	public void removeBonus(L2PcInstance player)
	{
		player.removeStatsOwner(this);
	}
}
