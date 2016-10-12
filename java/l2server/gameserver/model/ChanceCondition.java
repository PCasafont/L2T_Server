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

import l2server.gameserver.templates.StatsSet;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.Arrays;
import java.util.logging.Level;

/**
 * @author kombat
 */
public final class ChanceCondition
{
	public static final int EVT_HIT = 0x000001;
	public static final int EVT_CRIT = 0x000002;
	public static final int EVT_CAST = 0x000004;
	public static final int EVT_PHYSICAL = 0x000008;
	public static final int EVT_MAGIC = 0x000010;
	public static final int EVT_MAGIC_GOOD = 0x000020;
	public static final int EVT_MAGIC_OFFENSIVE = 0x000040;
	public static final int EVT_ATTACKED = 0x000080;
	public static final int EVT_ATTACKED_HIT = 0x000100;
	public static final int EVT_ATTACKED_CRIT = 0x000200;
	public static final int EVT_ATTACKED_SUMMON = 0x000400;
	public static final int EVT_HIT_BY_SKILL = 0x000800;
	public static final int EVT_HIT_BY_OFFENSIVE_SKILL = 0x001000;
	public static final int EVT_HIT_BY_GOOD_MAGIC = 0x002000;
	public static final int EVT_EVADED_HIT = 0x004000;
	public static final int EVT_ON_START = 0x008000;
	public static final int EVT_ON_ACTION_TIME = 0x010000;
	public static final int EVT_ON_EXIT = 0x020000;
	public static final int EVT_KILL = 0x040000;
	public static final int EVT_SHIELD_BLOCK = 0x080000;

	public enum TriggerType
	{
		// You hit an enemy
		ON_HIT(EVT_HIT), // You hit an enemy - was crit
		ON_CRIT(EVT_CRIT), // You cast a skill
		ON_CAST(EVT_CAST), // You cast a skill - it was a physical one
		ON_PHYSICAL(EVT_PHYSICAL), // You cast a skill - it was a magic one
		ON_MAGIC(EVT_MAGIC), // You cast a skill - it was a magic one - good magic
		ON_MAGIC_GOOD(EVT_MAGIC_GOOD), // You cast a skill - it was a magic one - offensive magic
		ON_MAGIC_OFFENSIVE(EVT_MAGIC_OFFENSIVE), //You block an attack with shield
		ON_SHIELD_BLOCK(EVT_SHIELD_BLOCK), // You are attacked by enemy
		ON_ATTACKED(EVT_ATTACKED), // You are attacked by enemy - by hit
		ON_ATTACKED_HIT(EVT_ATTACKED_HIT), // You are attacked by enemy - by hit - was crit
		ON_ATTACKED_CRIT(EVT_ATTACKED_CRIT), // Your summon is attacked by enemy
		ON_ATTACKED_SUMMON(EVT_ATTACKED_SUMMON), // A skill was casted on you
		ON_HIT_BY_SKILL(EVT_HIT_BY_SKILL), // An evil skill was casted on you
		ON_HIT_BY_OFFENSIVE_SKILL(EVT_HIT_BY_OFFENSIVE_SKILL), // A good skill was casted on you
		ON_HIT_BY_GOOD_MAGIC(EVT_HIT_BY_GOOD_MAGIC), // Evading melee attack
		ON_EVADED_HIT(EVT_EVADED_HIT), // Effect only - on start
		ON_START(EVT_ON_START), // Effect only - each second
		ON_ACTION_TIME(EVT_ON_ACTION_TIME), // Effect only - on exit
		ON_EXIT(EVT_ON_EXIT), // You kill an enemy
		ON_KILL(EVT_KILL);

		private final int _mask;

		TriggerType(int mask)
		{
			_mask = mask;
		}

		public final boolean check(int event)
		{
			return (_mask & event) != 0; // Trigger (sub-)type contains event (sub-)type
		}
	}

	private final TriggerType _triggerType;
	private final double _chance;
	private final double _critChance;
	private final int _mindmg;
	private final byte[] _elements;
	private final int[] _activationSkills;
	private final boolean _pvpOnly;

	private ChanceCondition(TriggerType trigger, double chance, int mindmg, byte[] elements, int[] activationSkills, boolean pvpOnly)
	{
		_triggerType = trigger;
		_chance = chance;
		_mindmg = mindmg;
		_elements = elements;
		_pvpOnly = pvpOnly;
		_activationSkills = activationSkills;
		_critChance = -1;
	}

	private ChanceCondition(TriggerType trigger, double chance, double critChance, int mindmg, byte[] elements, int[] activationSkills, boolean pvpOnly)
	{
		_triggerType = trigger;
		_chance = chance;
		_mindmg = mindmg;
		_elements = elements;
		_pvpOnly = pvpOnly;
		_activationSkills = activationSkills;
		_critChance = critChance;
	}

	public static ChanceCondition parse(StatsSet set)
	{
		try
		{
			TriggerType trigger = set.getEnum("chanceType", TriggerType.class, null);
			int chance = set.getInteger("activationChance", -1);
			int critChance = set.getInteger("activationCritChance", -1);
			int mindmg = set.getInteger("activationMinDamage", -1);
			String elements = set.getString("activationElements", null);
			String activationSkills = set.getString("activationSkills", null);
			boolean pvpOnly = set.getBool("pvpChanceOnly", false);

			if (trigger != null)
			{
				return new ChanceCondition(trigger, chance, critChance, mindmg, parseElements(elements),
						parseActivationSkills(activationSkills), pvpOnly);
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "", e);
		}
		return null;
	}

	public static ChanceCondition parse(String chanceType, double chance, double critChance, int mindmg, String elements, String activationSkills, boolean pvpOnly)
	{
		try
		{
			if (chanceType == null)
			{
				return null;
			}

			TriggerType trigger = Enum.valueOf(TriggerType.class, chanceType);

			if (trigger != null)
			{
				return new ChanceCondition(trigger, chance, critChance, mindmg, parseElements(elements),
						parseActivationSkills(activationSkills), pvpOnly);
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "", e);
		}

		return null;
	}

	public static byte[] parseElements(String list)
	{
		if (list == null)
		{
			return null;
		}

		String[] valuesSplit = list.split(",");
		byte[] elements = new byte[valuesSplit.length];
		for (int i = 0; i < valuesSplit.length; i++)
		{
			elements[i] = Byte.parseByte(valuesSplit[i]);
		}

		Arrays.sort(elements);
		return elements;
	}

	public static int[] parseActivationSkills(String list)
	{
		if (list == null)
		{
			return null;
		}

		String[] valuesSplit = list.split(",");
		int[] skillIds = new int[valuesSplit.length];
		for (int i = 0; i < valuesSplit.length; i++)
		{
			skillIds[i] = Integer.parseInt(valuesSplit[i]);
		}

		return skillIds;
	}

	public boolean trigger(int event, int damage, boolean crit, byte element, boolean playable, L2Skill skill)
	{
		if (_pvpOnly && !playable)
		{
			return false;
		}

		if (_elements != null && Arrays.binarySearch(_elements, element) < 0)
		{
			return false;
		}

		if (_activationSkills != null)
		{
			if (skill == null)
			{
				return false;
			}

			if (Arrays.binarySearch(_activationSkills, skill.getId()) < 0)
			{
				return false;
			}
		}

		// if the skill has "activationMinDamage" set to be higher than -1(default)
		// and if "activationMinDamage" is still higher than the recieved damage, the skill wont trigger
		if (_mindmg > -1 && _mindmg > damage)
		{
			return false;
		}

		if (!crit || _critChance == -1)
		{
			return _triggerType.check(event) && (_chance < 0 || Rnd.get(100) < _chance);
		}
		else
		{
			return _triggerType.check(event) && (_critChance < 0 || Rnd.get(100) < _critChance);
		}
	}

	public TriggerType getTriggerType()
	{
		return _triggerType;
	}

	@Override
	public String toString()
	{
		return "Trigger[" + _chance + ";" + _triggerType.toString() + "]";
	}
}
