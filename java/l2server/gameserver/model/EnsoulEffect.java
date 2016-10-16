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

import lombok.Getter;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.funcs.Func;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class EnsoulEffect
{
	@Getter private final int id;
	@Getter private final String name;
	@Getter private final int group;
	@Getter private final int stage;
	private final List<Func> funcs = new ArrayList<>();

	public EnsoulEffect(int id, String name, int group, int stage)
	{
		this.id = id;
		this.name = name;
		this.group = group;
		this.stage = stage;
	}

	public void addFunc(Func func)
	{
		funcs.add(func);
	}





	public void applyBonus(L2PcInstance player)
	{
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
