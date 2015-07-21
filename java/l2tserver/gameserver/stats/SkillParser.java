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
package l2tserver.gameserver.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;

import l2tserver.Config;
import l2tserver.gameserver.datatables.EnchantCostsTable;
import l2tserver.gameserver.model.ChanceCondition;
import l2tserver.gameserver.model.L2Skill;
import l2tserver.gameserver.stats.conditions.Condition;
import l2tserver.gameserver.stats.funcs.Lambda;
import l2tserver.gameserver.templates.StatsSet;
import l2tserver.gameserver.templates.skills.L2AbnormalTemplate;
import l2tserver.gameserver.templates.skills.L2AbnormalType;
import l2tserver.gameserver.templates.skills.L2EffectTemplate;
import l2tserver.gameserver.templates.skills.L2SkillType;
import l2tserver.log.Log;
import l2tserver.util.xml.XmlNode;

/**
 * @author mkizub
 */
public final class SkillParser extends StatsParser
{
	private static enum SkillEnchantBonusType
	{
		ADD,
		ADD_PERCENT
	}
	
	private static final class SkillEnchantBonusData
	{
		public SkillEnchantBonusType type;
		public String[] data;
		public SkillEnchantBonusData(SkillEnchantBonusType t, String[] d)
		{
			type = t;
			data = d;
		}
	}
	
	private StatsSet[] _sets;
	Map<Integer, Map<Integer, StatsSet[]>> _enchantSets = new HashMap<Integer, Map<Integer, StatsSet[]>>();
	private int _currentLevel;
	private int _currentEnchantRoute;
	private int _currentEnchantLevel;
	protected Map<String, String[]> _tables = new HashMap<String, String[]>();
	Map<String, Map<Integer, Map<Integer, SkillEnchantBonusData>>> _enchantTables = new HashMap<String, Map<Integer, Map<Integer, SkillEnchantBonusData>>>();
	
	private Map<Integer, L2Skill> _skills = new HashMap<Integer, L2Skill>();
	
	public SkillParser(XmlNode node)
	{
		super(node);
	}
	
	@Override
	protected StatsSet getStatsSet()
	{
		return _sets[_currentLevel];
	}
	
	protected String getTableValue(String name)
	{
		try
		{
			String[] table = null;
			int level = 0;
			if (_currentEnchantRoute > 0)
			{
				Map<Integer, Map<Integer, SkillEnchantBonusData>> nameMap = _enchantTables.get(name);
				if (nameMap != null)
				{
					Map<Integer, SkillEnchantBonusData> routeMap = nameMap.get(_currentEnchantRoute);
					if (routeMap != null)
					{
						SkillEnchantBonusData routeTable = routeMap.get(_currentLevel);
						if (routeTable != null)
						{
							table = routeTable.data;
							level = _currentEnchantLevel;
							
							// Special calculation
							if (routeTable.type == SkillEnchantBonusType.ADD_PERCENT)
							{
								float multiplier = 1.0f + Float.parseFloat(table[level - 1]) / 100.0f;
								return String.valueOf(Float.parseFloat(_tables.get(name)[_currentLevel - 1]) * multiplier);
							}
						}
					}
				}
			}
			else
			{
				table = _tables.get(name);
				level = _currentLevel;
			}
			
			if (table == null)
			{
				table = _tables.get(name);
				level = table.length;
			}
			
			if (table.length == 0)
				return null;

			if (level > table.length)
				return table[0];
			
			return table[level - 1];
		}
		catch (RuntimeException e)
		{
			Log.log(Level.SEVERE, "Error in table: " + name + " of Skill Id " + _id, e);
			return null;
		}
	}
	
	protected void parseTable(XmlNode n)
	{
		String name = n.getString("name");
		if (name.charAt(0) != '#')
			throw new IllegalArgumentException("Table name must start with #");
		
		String tokenizer = " \t\n\r\f";
		if (name.equals("#name"))
			tokenizer = ";\t\n\r\f";
		StringTokenizer data = new StringTokenizer(n.getText(), tokenizer);
		List<String> array = new ArrayList<String>(data.countTokens());
		while (data.hasMoreTokens())
			array.add(data.nextToken());

		_tables.put(name, array.toArray(new String[array.size()]));
		
		if (!n.getChildren().isEmpty())
		{
			Map<Integer, Map<Integer, SkillEnchantBonusData>> nameMap = new HashMap<Integer, Map<Integer, SkillEnchantBonusData>>();
			for (XmlNode node : n.getChildren())
			{
				if (!node.getName().equalsIgnoreCase("enchantRoute"))
					continue;
				
				data = new StringTokenizer(node.getText(), tokenizer);
				array = new ArrayList<String>(data.countTokens());
				while (data.hasMoreTokens())
					array.add(data.nextToken());
				
				SkillEnchantBonusType type = SkillEnchantBonusType.valueOf(node.getString("type", "ADD"));
				String[] enchRoute = node.getString("id").split(",");
				String[] enchLvl = node.getString("level").split(",");
				
				for (String rt : enchRoute)
				{
					Map<Integer, SkillEnchantBonusData> routeMap = new HashMap<Integer, SkillEnchantBonusData>();
					int route = Integer.parseInt(rt);
					for (String lv : enchLvl)
					{
						int lvl = Integer.parseInt(lv);
						routeMap.put(lvl, new SkillEnchantBonusData(type, array.toArray(new String[array.size()])));
					}
					
					if (nameMap.containsKey(route))
						nameMap.get(route).putAll(routeMap);
					else
						nameMap.put(route, routeMap);
				}
			}
			
			_enchantTables.put(name, nameMap);
		}
	}
	
	@Override
	public void parse() throws RuntimeException
	{
		// Basic data
		int levels = _node.getInt("levels");
		_sets = new StatsSet[levels];
		for (int i = 0; i < levels; i++)
		{
			_sets[i] = new StatsSet();
			_sets[i].set("skill_id", _id);
			_sets[i].set("level", i + 1);
			_sets[i].set("name", _name);
		}
		
		if (_sets.length != levels)
			throw new RuntimeException("Skill id=" + _id + " number of levels missmatch, " + levels + " levels expected");
		
		// Enchant routes
		for (XmlNode n : _node.getChildren())
		{
			if (n.getName().equalsIgnoreCase("enchantRoute") && n.getBool("enabled", true))
			{
				int route = n.getInt("id");
				String[] routeLevels = n.getString("level").split(",");
				int enchantLevels = EnchantCostsTable.getInstance().addNewRouteForSkill(_id, Integer.parseInt(routeLevels[0]), route);
				for (String routeLevel : routeLevels)
				{
					int level = Integer.parseInt(routeLevel);
					Map<Integer, StatsSet[]> levelEnchants = _enchantSets.get(level);
					if (levelEnchants == null)
					{
						levelEnchants = new HashMap<Integer, StatsSet[]>();
						_enchantSets.put(level, levelEnchants);
					}
					
					StatsSet[] enchSets = new StatsSet[enchantLevels];
					for (int i = 0; i < enchantLevels; i++)
					{
						enchSets[i] = new StatsSet();
						enchSets[i].set("skill_id", _id);
						enchSets[i].set("level", level);
						enchSets[i].set("enchantRouteId", route);
						enchSets[i].set("enchantLevel", i + 1);
						enchSets[i].set("name", _name);
					}
					
					levelEnchants.put(route, enchSets);
				}
			}
		}
		
		// Tables
		for (XmlNode n : _node.getChildren())
		{
			if (n.getName().equalsIgnoreCase("table"))
				parseTable(n);
		}
		
		// Sets
		for (XmlNode n : _node.getChildren())
		{
			if (n.getName().equalsIgnoreCase("set"))
			{
				_currentLevel = 1;
				while (_currentLevel <= levels)
				{
					_currentEnchantRoute = 0;
					Map<Integer, StatsSet[]> levelEnchants = _enchantSets.get(_currentLevel);
					parseBeanSet(n, _sets[_currentLevel - 1]);
					
					if (levelEnchants != null)
					{
						for (int route : levelEnchants.keySet())
						{
							_currentEnchantRoute = route;
							_currentEnchantLevel = 1;
							StatsSet[] enchSets = levelEnchants.get(route);
							while (_currentEnchantLevel <= enchSets.length)
							{
								parseBeanSet(n, enchSets[_currentEnchantLevel - 1]);
								_currentEnchantLevel++;
							}
						}
					}
			
					_currentLevel++;
				}
			}
		}
		
		// Creating the skill instances
		for (int i = 0; i < levels; i++)
		{
			_skills.put(i + 1, _sets[i].getEnum("skillType", L2SkillType.class).makeSkill(_sets[i]));
			Map<Integer, StatsSet[]> levelEnchants = _enchantSets.get(i + 1);
			if (levelEnchants != null)
			{
				for (int route : levelEnchants.keySet())
				{
					StatsSet[] enchSets = levelEnchants.get(route);
					for (int j = 0; j < enchSets.length; j++)
					{
						int hash = (i + 1) * 1000000 + route * 1000 + j + 1;
						_skills.put(hash, enchSets[j].getEnum("skillType", L2SkillType.class).makeSkill(enchSets[j]));
					}
				}
			}
		}
		
		// Parsing the <for>s
		_currentLevel = 1;
		while (_currentLevel <= levels)
		{
			_currentEnchantRoute = 0;
			Map<Integer, StatsSet[]> levelEnchants = _enchantSets.get(_currentLevel);
			for (XmlNode n : _node.getChildren())
			{
				if (n.getName().equalsIgnoreCase("for"))
				{
					parseTemplate(n, _skills.get(_currentLevel));
					
					if (levelEnchants != null)
					{
						for (int route : levelEnchants.keySet())
						{
							_currentEnchantRoute = route;
							_currentEnchantLevel = 1;
							StatsSet[] enchSets = levelEnchants.get(route);
							while (_currentEnchantLevel <= enchSets.length)
							{
								int hash = _currentLevel * 1000000 + route * 1000 + _currentEnchantLevel;
								parseTemplate(n, _skills.get(hash));
								_currentEnchantLevel++;
							}
						}
					}
				}
			}
			
			_currentLevel++;
		}
	}
	
	protected void parseBeanSet(XmlNode n, StatsSet set)
	{
		String name = n.getString("name").trim();
		String value = n.getString("val").trim();
		char ch = value.length() == 0 ? ' ' : value.charAt(0);
		if (ch == '#' || ch == '-' || Character.isDigit(ch))
		{
			String val = getValue(value);
			if (val != null)
				set.set(name, getValue(value));
		}
		else
			set.set(name, value);
	}
	
	protected void attachAbnormal(XmlNode n, L2Skill template)
	{
		/**
		 * Keep this values as default ones, DP needs it
		 */
		int duration = 0;
		int count = 1;
		
		if (n.hasAttribute("count"))
		{
			count = Integer.decode(getValue(n.getString("count")));
		}
		if (n.hasAttribute("duration"))
		{
			duration = Integer.decode(getValue(n.getString("duration")));
			if (Config.ENABLE_MODIFY_SKILL_DURATION)
			{
				if (Config.SKILL_DURATION_LIST.containsKey(template.getId()))
				{
					if (template.getLevelHash() < 100)
						duration = Config.SKILL_DURATION_LIST.get(template.getId());
					else if ((template.getLevelHash() >= 100) && (template.getLevelHash() < 140))
						duration += Config.SKILL_DURATION_LIST.get(template.getId());
					else if (template.getLevelHash() > 140)
						duration = Config.SKILL_DURATION_LIST.get(template.getId());
					if (Config.DEBUG)
						Log.info("*** Skill " + template.getName() + " (" + template.getLevelHash() + ") changed duration to " + duration + " seconds.");
				}
			}
		}
		else if (template.getBuffDuration() > 0)
			duration = template.getBuffDuration() / 1000 / count;
		
		boolean self = false;
		if (n.hasAttribute("self"))
		{
			if (Integer.decode(getValue(n.getString("self"))) == 1)
				self = true;
		}
		boolean icon = true;
		if (n.hasAttribute("noicon"))
		{
			if (Integer.decode(getValue(n.getString("noicon"))) == 1)
				icon = false;
		}
		Condition applayCond = parseCondition(n.getFirstChild(), template);
		VisualEffect[] visualEffect = null;
		if (n.hasAttribute("visualEffect"))
		{
			String[] abns = n.getString("visualEffect").split(",[ ]*");
			visualEffect = new VisualEffect[abns.length];
			for (int i = 0; i < abns.length; i++)
				visualEffect[i] = VisualEffect.getByName(getValue(abns[i]));
		}
		String[] stackType = new String[]{};
		if (n.hasAttribute("stackType"))
			stackType = n.getString("stackType").split(",[ ]*");
		
		byte stackLvl = 0;
		if (n.hasAttribute("stackLvl"))
			stackLvl = Byte.parseByte(getValue(n.getString("stackLvl")));
		
		double landRate = -1;
		if (n.hasAttribute("landRate"))
			landRate = Double.parseDouble(getValue(n.getString("landRate")));
		else if (template.getSkillType() == L2SkillType.DEBUFF && template.getPower() > 0.0)
			landRate = template.getPower();
		
		L2AbnormalType type = L2AbnormalType.NONE;
		if (n.hasAttribute("effectType"))
		{
			String typeName = getValue(n.getString("effectType"));
			
			try
			{
				type = Enum.valueOf(L2AbnormalType.class, typeName);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("No effect type found for: " + typeName);
			}
		}
		
		int comboId = 0;
		if (n.hasAttribute("comboId"))
			comboId = Integer.parseInt(getValue(n.getString("comboId")));
		
		L2AbnormalTemplate lt = new L2AbnormalTemplate(applayCond, count, duration, visualEffect, stackType, stackLvl, icon, landRate, type, comboId);
		parseTemplate(n, lt);
		if (self)
			template.attachSelf(lt);
		else
			template.attach(lt);
	}
	
	protected void attachEffect(XmlNode n, L2AbnormalTemplate template)
	{
		String type = getValue(n.getString("type").intern());
		Condition applayCond = parseCondition(n.getFirstChild(), template);
		L2EffectTemplate lt;
		
		final boolean isChanceSkillTrigger = type == "ChanceSkillTrigger";
		int trigId = 0;
		if (n.hasAttribute("triggeredId"))
			trigId = Integer.parseInt(getValue(n.getString("triggeredId")));
		else if (isChanceSkillTrigger)
			throw new NoSuchElementException(type + " requires triggerId");
		
		int trigLvl = 0;
		if (n.hasAttribute("triggeredLevel"))
			trigLvl = Integer.parseInt(getValue(n.getString("triggeredLevel")));
		
		String chanceCond = null;
		if (n.hasAttribute("chanceType"))
			chanceCond = getValue(n.getString("chanceType"));
		else if (isChanceSkillTrigger)
			throw new NoSuchElementException(type + " requires chanceType");
		
		int activationChance = -1;
		if (n.hasAttribute("activationChance"))
			activationChance = Integer.parseInt(getValue(n.getString("activationChance")));
		int activationCritChance = -1;
		if (n.hasAttribute("activationCritChance"))
			activationCritChance = Integer.parseInt(getValue(n.getString("activationCritChance")));
		int activationMinDamage = -1;
		if (n.hasAttribute("activationMinDamage"))
			activationMinDamage = Integer.parseInt(getValue(n.getString("activationMinDamage")));
		String activationElements = null;
		if (n.hasAttribute("activationElements"))
			activationElements = getValue(n.getString("activationElements"));
		String activationSkills = null;
		if (n.hasAttribute("activationSkills"))
			activationSkills = getValue(n.getString("activationSkills"));
		boolean pvpOnly = false;
		if (n.hasAttribute("pvpChanceOnly"))
			pvpOnly = Boolean.parseBoolean(getValue(n.getString("pvpChanceOnly")));
		
		ChanceCondition chance = ChanceCondition.parse(chanceCond, activationChance, activationCritChance, activationMinDamage, activationElements, activationSkills, pvpOnly);
		
		if (chance == null && isChanceSkillTrigger)
			throw new NoSuchElementException("Invalid chance condition: " + chanceCond + " "
					+ activationChance);

		Lambda lambda = getLambda(n, template);
		lt = new L2EffectTemplate(template, applayCond, lambda, type, trigId, trigLvl, chance);
		parseTemplate(n, lt);
		template.attach(lt);
	}
	
	@Override
	protected String getValue(String value)
	{
		// is it a table?
		if (value.charAt(0) == '#')
			return getTableValue(value);
		
		return value;
	}
	
	public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
	}
}
