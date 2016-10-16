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
		this.level = lvl;
		this.name = name.intern();
		this.hp = HP;
		this.hpRegen = HpRegen;
		this.type = type;
		this.group = group;
		this.fishGuts = fish_guts;
		this.gutsCheckTime = guts_check_time;
		this.waitTime = wait_time;
		this.combatTime = combat_time;
	}

	public FishData(FishData copyOf)
	{
		this.id = copyOf.getId();
		this.level = copyOf.getLevel();
		this.name = copyOf.getName();
		this.hp = copyOf.getHP();
		this.hpRegen = copyOf.getHpRegen();
		this.type = copyOf.getType();
		this.group = copyOf.getGroup();
		this.fishGuts = copyOf.getFishGuts();
		this.gutsCheckTime = copyOf.getGutsCheckTime();
		this.waitTime = copyOf.getWaitTime();
		this.combatTime = copyOf.getCombatTime();
	}

	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return this.id;
	}

	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		return this.level;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return this.name;
	}

	public int getHP()
	{
		return this.hp;
	}

	public int getHpRegen()
	{
		return this.hpRegen;
	}

	public int getType()
	{
		return this.type;
	}

	public int getGroup()
	{
		return this.group;
	}

	public int getFishGuts()
	{
		return this.fishGuts;
	}

	public int getGutsCheckTime()
	{
		return this.gutsCheckTime;
	}

	public int getWaitTime()
	{
		return this.waitTime;
	}

	public int getCombatTime()
	{
		return this.combatTime;
	}

	public void setType(int type)
	{
		this.type = type;
	}
}
