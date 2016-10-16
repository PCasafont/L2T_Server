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
import lombok.Setter;

public class FishData
{
	@Getter private int id;
	@Getter private int level;
	@Getter private String name;
	@Getter private int hp;
	@Getter private int hpRegen;
	@Getter @Setter private int type;
	@Getter private int group;
	@Getter private int fishGuts;
	@Getter private int gutsCheckTime;
	@Getter private int waitTime;
	@Getter private int combatTime;

	public FishData(int id, int lvl, String name, int HP, int HpRegen, int type, int group, int fish_guts, int guts_check_time, int wait_time, int combat_time)
	{
		this.id = id;
		level = lvl;
		this.name = name.intern();
		hp = HP;
		hpRegen = HpRegen;
		this.type = type;
		this.group = group;
		fishGuts = fish_guts;
		gutsCheckTime = guts_check_time;
		waitTime = wait_time;
		combatTime = combat_time;
	}

	public FishData(FishData copyOf)
	{
		id = copyOf.getId();
		level = copyOf.getLevel();
		name = copyOf.getName();
		hp = copyOf.getHp();
		hpRegen = copyOf.getHpRegen();
		type = copyOf.getType();
		group = copyOf.getGroup();
		fishGuts = copyOf.getFishGuts();
		gutsCheckTime = copyOf.getGutsCheckTime();
		waitTime = copyOf.getWaitTime();
		combatTime = copyOf.getCombatTime();
	}
}
