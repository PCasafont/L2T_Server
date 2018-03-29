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

public class FishData
{
	private int id;
	private int level;
	private String name;
	private int hp;
	private int hpRegen;
	private int type;
	private int group;
	private int fishGuts;
	private int gutsCheckTime;
	private int waitTime;
	private int combatTime;

	public FishData(int id, int lvl, String name, int HP, int HpRegen, int type, int group, int fish_guts, int guts_check_time, int wait_time, int combat_time)
	{
		this.id = id;
		level = lvl;
		this.name = name.intern();
		this.hp = HP;
		this.hpRegen = HpRegen;
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
		hp = copyOf.getHP();
		hpRegen = copyOf.getHpRegen();
		type = copyOf.getType();
		group = copyOf.getGroup();
		fishGuts = copyOf.getFishGuts();
		gutsCheckTime = copyOf.getGutsCheckTime();
		waitTime = copyOf.getWaitTime();
		combatTime = copyOf.getCombatTime();
	}

	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		return level;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}

	public int getHP()
	{
		return hp;
	}

	public int getHpRegen()
	{
		return hpRegen;
	}

	public int getType()
	{
		return type;
	}

	public int getGroup()
	{
		return group;
	}

	public int getFishGuts()
	{
		return fishGuts;
	}

	public int getGutsCheckTime()
	{
		return gutsCheckTime;
	}

	public int getWaitTime()
	{
		return waitTime;
	}

	public int getCombatTime()
	{
		return combatTime;
	}

	public void setType(int type)
	{
		this.type = type;
	}
}
