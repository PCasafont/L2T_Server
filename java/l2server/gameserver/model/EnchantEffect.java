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
	private final int _id;
	private final int _rarity;
	private final int _slot;

	private int _skillId = 0;
	private int _skillLevel = 0;
	private final List<Func> _funcs = new ArrayList<>();

	public EnchantEffect(int id, int rarity, int slot)
	{
		_id = id;
		_rarity = rarity;
		_slot = slot;
	}

	public void setSkill(int skillId, int skillLevel)
	{
		_skillId = skillId;
		_skillLevel = skillLevel;
	}

	public void addFunc(Func func)
	{
		_funcs.add(func);
	}

	public int getId()
	{
		return _id;
	}

	public int getRarity()
	{
		return _rarity;
	}

	public int getSlot()
	{
		return _slot;
	}

	public L2Skill getSkill()
	{
		if (_skillId == 0)
		{
			return null;
		}

		return SkillTable.getInstance().getInfo(_skillId, _skillLevel);
	}

	public void applyBonus(L2PcInstance player)
	{
		player.removeStatsOwner(this);
		for (Func f : _funcs)
		{
			player.addStatFunc(f);
		}
	}

	public void removeBonus(L2PcInstance player)
	{
		player.removeStatsOwner(this);
	}
}
