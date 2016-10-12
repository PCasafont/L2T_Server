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

package l2server.gameserver.stats;

import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.model.L2Object.InstanceType;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.base.PlayerState;
import l2server.gameserver.model.base.Race;
import l2server.gameserver.stats.conditions.*;
import l2server.gameserver.stats.conditions.ConditionGameTime.CheckGameTime;
import l2server.gameserver.stats.funcs.*;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.item.L2ArmorType;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.gameserver.templates.skills.L2AbnormalTemplate;
import l2server.log.Log;
import l2server.util.xml.XmlNode;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * @author mkizub
 */
public abstract class StatsParser
{
	static Logger _log = Logger.getLogger(StatsParser.class.getName());

	protected int _id;
	protected String _name;

	protected XmlNode _node;

	StatsParser(XmlNode node)
	{
		_id = node.getInt("id");
		_name = node.getString("name");
		_node = node;
	}

	public abstract void parse() throws RuntimeException;

	protected abstract StatsSet getStatsSet();

	protected void parseTemplate(XmlNode node, Object template)
	{
		XmlNode firstNode = node.getFirstChild();
		if (firstNode == null)
		{
			return;
		}

		if (firstNode.getName().equalsIgnoreCase("cond"))
		{
			Condition condition = parseCondition(firstNode.getFirstChild(), template);
			if (condition != null)
			{
				if (firstNode.hasAttribute("msg"))
				{
					condition.setMessage(firstNode.getString("msg"));
				}
				else if (firstNode.hasAttribute("msgId"))
				{
					condition.setMessageId(Integer.decode(getValue(firstNode.getString("msgId"))));
					if (firstNode.hasAttribute("addName") && Integer.decode(getValue(firstNode.getString("msgId"))) > 0)
					{
						condition.addName();
					}
				}

				if (template instanceof L2Skill)
				{
					((L2Skill) template).attach(condition, false);
				}
				else if (template instanceof L2Item)
				{
					((L2Item) template).attach(condition);
				}
			}
		}
		for (XmlNode n : node.getChildren())
		{
			String nodeType = n.getName();
			// Custom for the critical damage buffs/passives
			//if (nodeType.equalsIgnoreCase("addPercent")
			//		&& n.getString("stat").equalsIgnoreCase("pCritDmg"))
			//	nodeType = "addPercentBase";

			if (nodeType.equalsIgnoreCase("add"))
			{
				attachFunc(n, template, "Add");
			}
			else if (nodeType.equalsIgnoreCase("sub"))
			{
				attachFunc(n, template, "Sub");
			}
			else if (nodeType.equalsIgnoreCase("baseAdd"))
			{
				attachFunc(n, template, "BaseAdd");
			}
			else if (nodeType.equalsIgnoreCase("baseSub"))
			{
				attachFunc(n, template, "BaseSub");
			}
			else if (nodeType.equalsIgnoreCase("addPercent"))
			{
				attachFunc(n, template, "AddPercent");
			}
			else if (nodeType.equalsIgnoreCase("subPercent"))
			{
				attachFunc(n, template, "SubPercent");
			}
			else if (nodeType.equalsIgnoreCase("addPercentBase"))
			{
				attachFunc(n, template, "AddPercentBase");
			}
			else if (nodeType.equalsIgnoreCase("subPercentBase"))
			{
				attachFunc(n, template, "SubPercentBase");
			}
			else if (nodeType.equalsIgnoreCase("mul"))
			{
				attachFunc(n, template, "Mul");
			}
			else if (nodeType.equalsIgnoreCase("baseMul"))
			{
				attachFunc(n, template, "BaseMul");
			}
			else if (nodeType.equalsIgnoreCase("set"))
			{
				attachFunc(n, template, "Set");
			}
			else if (nodeType.equalsIgnoreCase("override"))
			{
				attachFunc(n, template, "Override");
			}
			else if (nodeType.equalsIgnoreCase("enchant"))
			{
				attachFunc(n, template, "Enchant");
			}
			else if (nodeType.equalsIgnoreCase("enchanthp"))
			{
				attachFunc(n, template, "EnchantHp");
			}
			else if (nodeType.equalsIgnoreCase("abnormal"))
			{
				if (!(this instanceof SkillParser) && !(template instanceof L2Skill))
				{
					throw new RuntimeException("Abnormals in something that's not a skill");
				}
				((SkillParser) this).attachAbnormal(n, (L2Skill) template);
			}
			else if (nodeType.equalsIgnoreCase("effect"))
			{
				if (!(this instanceof SkillParser) && !(template instanceof L2AbnormalTemplate))
				{
					throw new RuntimeException("Effects in something that's not an abnormal");
				}
				((SkillParser) this).attachEffect(n, (L2AbnormalTemplate) template);
			}
		}
	}

	protected void attachFunc(XmlNode n, Object template, String name)
	{
		Stats stat = Stats.fromString(n.getString("stat"));
		Lambda lambda = getLambda(n, template);
		Condition applayCond = parseCondition(n.getFirstChild(), template);
		FuncTemplate ft = new FuncTemplate(applayCond, name, stat, lambda);
		if (template instanceof L2Item)
		{
			((L2Item) template).attach(ft);
		}
		else if (template instanceof L2Skill)
		{
			((L2Skill) template).attach(ft);
		}
		else if (template instanceof L2AbnormalTemplate)
		{
			((L2AbnormalTemplate) template).attach(ft);
		}
	}

	protected void attachLambdaFunc(XmlNode n, Object template, LambdaCalc calc)
	{
		String name = n.getName();
		final StringBuilder sb = new StringBuilder(name);
		sb.setCharAt(0, Character.toUpperCase(name.charAt(0)));
		name = sb.toString();
		Lambda lambda = getLambda(n, template);
		FuncTemplate ft = new FuncTemplate(null, name, null, lambda);
		calc.addFunc(ft.getFunc(calc));
	}

	protected Condition parseCondition(XmlNode n, Object template)
	{
		if (n == null)
		{
			return null;
		}
		if (n.getName().equalsIgnoreCase("and"))
		{
			return parseLogicAnd(n, template);
		}
		if (n.getName().equalsIgnoreCase("or"))
		{
			return parseLogicOr(n, template);
		}
		if (n.getName().equalsIgnoreCase("not"))
		{
			return parseLogicNot(n, template);
		}
		if (n.getName().equalsIgnoreCase("player"))
		{
			return parsePlayerCondition(n, template);
		}
		if (n.getName().equalsIgnoreCase("target"))
		{
			return parseTargetCondition(n, template);
		}
		if (n.getName().equalsIgnoreCase("using"))
		{
			return parseUsingCondition(n);
		}
		if (n.getName().equalsIgnoreCase("game"))
		{
			return parseGameCondition(n);
		}
		return null;
	}

	protected Condition parseLogicAnd(XmlNode node, Object template)
	{
		ConditionLogicAnd cond = new ConditionLogicAnd();
		for (XmlNode n : node.getChildren())
		{
			cond.add(parseCondition(n, template));
		}

		if (cond.conditions == null || cond.conditions.length == 0)
		{
			Log.severe("Empty <and> condition in " + _name);
		}
		return cond;
	}

	protected Condition parseLogicOr(XmlNode node, Object template)
	{
		ConditionLogicOr cond = new ConditionLogicOr();
		for (XmlNode n : node.getChildren())
		{
			cond.add(parseCondition(n, template));
		}

		if (cond.conditions == null || cond.conditions.length == 0)
		{
			Log.severe("Empty <or> condition in " + _name);
		}
		return cond;
	}

	protected Condition parseLogicNot(XmlNode node, Object template)
	{
		if (node.getFirstChild() != null)
		{
			return new ConditionLogicNot(parseCondition(node.getFirstChild(), template));
		}

		Log.severe("Empty <not> condition in " + _name);
		return null;
	}

	protected Condition parsePlayerCondition(XmlNode n, Object template)
	{
		Condition cond = null;
		byte[] forces = new byte[2];
		for (Entry<String, String> a : n.getAttributes().entrySet())
		{
			if (a.getKey().equalsIgnoreCase("races"))
			{
				final String[] racesVal = a.getValue().split(",");
				final Race[] races = new Race[racesVal.length];
				for (int r = 0; r < racesVal.length; r++)
				{
					if (racesVal[r] != null)
					{
						races[r] = Race.valueOf(racesVal[r]);
					}
				}
				cond = joinAnd(cond, new ConditionPlayerRace(races));
			}
			else if (a.getKey().equalsIgnoreCase("level"))
			{
				int lvl = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerLevel(lvl));
			}
			else if (a.getKey().equalsIgnoreCase("levelRange"))
			{
				String[] range = getValue(a.getValue()).split(";");
				if (range.length == 2)
				{
					int[] lvlRange = new int[2];
					lvlRange[0] = Integer.decode(getValue(a.getValue()).split(";")[0]);
					lvlRange[1] = Integer.decode(getValue(a.getValue()).split(";")[1]);
					cond = joinAnd(cond, new ConditionPlayerLevelRange(lvlRange));
				}
			}
			else if (a.getKey().equalsIgnoreCase("resting"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.RESTING, val));
			}
			else if (a.getKey().equalsIgnoreCase("flying"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.FLYING, val));
			}
			else if (a.getKey().equalsIgnoreCase("moving"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.MOVING, val));
			}
			else if (a.getKey().equalsIgnoreCase("running"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.RUNNING, val));
			}
			else if (a.getKey().equalsIgnoreCase("standing"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.STANDING, val));
			}
			else if (a.getKey().equalsIgnoreCase("combat"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.COMBAT, val));
			}
			else if (a.getKey().equalsIgnoreCase("behind"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.BEHIND, val));
			}
			else if (a.getKey().equalsIgnoreCase("front"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.FRONT, val));
			}
			else if (a.getKey().equalsIgnoreCase("chaotic"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.CHAOTIC, val));
			}
			else if (a.getKey().equalsIgnoreCase("olympiad"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerState(PlayerState.OLYMPIAD, val));
			}
			else if (a.getKey().equalsIgnoreCase("ishero"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerIsHero(val));
			}
			else if (a.getKey().equalsIgnoreCase("hp"))
			{
				int hp = (int) Float.parseFloat(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerHp(hp));
			}
			else if (a.getKey().equalsIgnoreCase("mp"))
			{
				int hp = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerMp(hp));
			}
			else if (a.getKey().equalsIgnoreCase("cp"))
			{
				int cp = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerCp(cp));
			}
			else if (a.getKey().equalsIgnoreCase("grade"))
			{
				int expIndex = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerGrade(expIndex));
			}
			else if (a.getKey().equalsIgnoreCase("pkCount"))
			{
				int expIndex = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerPkCount(expIndex));
			}
			else if (a.getKey().equalsIgnoreCase("siegezone"))
			{
				int value = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionSiegeZone(value, true));
			}
			else if (a.getKey().equalsIgnoreCase("siegeside"))
			{
				int value = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerSiegeSide(value));
			}
			else if ("battle_force".equalsIgnoreCase(a.getKey()))
			{
				forces[0] = Byte.decode(getValue(a.getValue()));
			}
			else if ("spell_force".equalsIgnoreCase(a.getKey()))
			{
				forces[1] = Byte.decode(getValue(a.getValue()));
			}
			else if (a.getKey().equalsIgnoreCase("charges"))
			{
				int value = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerCharges(value));
			}
			else if (a.getKey().equalsIgnoreCase("souls"))
			{
				int value = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerSouls(value));
			}
			else if (a.getKey().equalsIgnoreCase("weight"))
			{
				int weight = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerWeight(weight));
			}
			else if (a.getKey().equalsIgnoreCase("invSize"))
			{
				int size = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerInvSize(size));
			}
			else if (a.getKey().equalsIgnoreCase("isClanLeader"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerIsClanLeader(val));
			}
			else if (a.getKey().equalsIgnoreCase("clanLeaderOn"))
			{
				cond = joinAnd(cond, new ConditionPlayerClanLeaderIsOn());
			}
			else if (a.getKey().equalsIgnoreCase("insideZoneId"))
			{
				int id = Integer.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerInsideZoneId(id));
			}
			else if (a.getKey().equalsIgnoreCase("onEvent"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerEvent(val));
			}
			else if (a.getKey().equalsIgnoreCase("onSurvivalEvent"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerSurvivalEvent(val));
			}
			else if (a.getKey().equalsIgnoreCase("pledgeClass"))
			{
				int pledgeClass = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerPledgeClass(pledgeClass));
			}
			else if (a.getKey().equalsIgnoreCase("clanHall"))
			{
				StringTokenizer st = new StringTokenizer(a.getValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item)));
				}
				cond = joinAnd(cond, new ConditionPlayerHasClanHall(array));
			}
			else if (a.getKey().equalsIgnoreCase("fort"))
			{
				int fort = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerHasFort(fort));
			}
			else if (a.getKey().equalsIgnoreCase("castle"))
			{
				int castle = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerHasCastle(castle));
			}
			else if (a.getKey().equalsIgnoreCase("sex"))
			{
				int sex = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerSex(sex));
			}
			else if (a.getKey().equalsIgnoreCase("flyMounted"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerFlyMounted(val));
			}
			else if (a.getKey().equalsIgnoreCase("landingZone"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerLandingZone(val));
			}
			else if ("active_effect_id".equalsIgnoreCase(a.getKey()))
			{
				int effect_id = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerActiveEffectId(effect_id));
			}
			else if ("active_effect_id_lvl".equalsIgnoreCase(a.getKey()))
			{
				String val = getValue(a.getValue());
				int effect_id = Integer.decode(getValue(val.split(",")[0]));
				int effect_lvl = Integer.decode(getValue(val.split(",")[1]));
				cond = joinAnd(cond, new ConditionPlayerActiveEffectId(effect_id, effect_lvl));
			}
			else if ("active_effect".equalsIgnoreCase(a.getKey()))
			{
				String val = getValue(a.getValue());
				String effectName = getValue(val.split(",")[0]);
				cond = joinAnd(cond, new ConditionPlayerActiveEffect(effectName));
			}
			else if ("active_skill_id".equalsIgnoreCase(a.getKey()))
			{
				int skill_id = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id));
			}
			else if ("active_skill_id_lvl".equalsIgnoreCase(a.getKey()))
			{
				String val = getValue(a.getValue());
				int skill_id = Integer.decode(getValue(val.split(",")[0]));
				int skill_lvl = Integer.decode(getValue(val.split(",")[1]));
				cond = joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id, skill_lvl));
			}
			else if ("class_id_restriction".equalsIgnoreCase(a.getKey()))
			{
				StringTokenizer st = new StringTokenizer(a.getValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item)));
				}
				cond = joinAnd(cond, new ConditionPlayerClassIdRestriction(array));
			}
			else if ("starts_with_class_name_restriction".equalsIgnoreCase(a.getKey()))
			{
				StringTokenizer st = new StringTokenizer(a.getValue(), ",");
				ArrayList<String> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(getValue(item));
				}
				cond = joinAnd(cond, new ConditionPlayerClassNameStartsWith(array));
			}
			else if (a.getKey().equalsIgnoreCase("subclass"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerSubclass(val));
			}
			else if (a.getKey().equalsIgnoreCase("instanceId"))
			{
				StringTokenizer st = new StringTokenizer(a.getValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item)));
				}
				cond = joinAnd(cond, new ConditionPlayerInstanceId(array));
			}
			else if (a.getKey().equalsIgnoreCase("agathionId"))
			{
				int agathionId = Integer.decode(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerAgathionId(agathionId));
			}
			else if (a.getKey().equalsIgnoreCase("cloakStatus"))
			{
				int val = Integer.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionPlayerCloakStatus(val));
			}
			else if (a.getKey().equalsIgnoreCase("hasPet"))
			{
				StringTokenizer st = new StringTokenizer(a.getValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item)));
				}
				cond = joinAnd(cond, new ConditionPlayerHasPet(array));
			}
			else if (a.getKey().equalsIgnoreCase("hasPet"))
			{
				StringTokenizer st = new StringTokenizer(a.getValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item)));
				}
				cond = joinAnd(cond, new ConditionPlayerHasPet(array));
			}
			else if (a.getKey().equalsIgnoreCase("servitorNpcId"))
			{
				StringTokenizer st = new StringTokenizer(a.getValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item)));
				}
				cond = joinAnd(cond, new ConditionPlayerServitorNpcId(array));
			}
			else if (a.getKey().equalsIgnoreCase("hasSummon"))
			{
				boolean val = Boolean.parseBoolean(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionPlayerHasSummon(val));
			}
			else if (a.getKey().equalsIgnoreCase("npcIdRadius"))
			{
				StringTokenizer st = new StringTokenizer(a.getValue(), ",");
				int npcId = 0;
				int radius = 0;
				if (st.countTokens() > 1)
				{
					npcId = Integer.decode(getValue(st.nextToken().trim()));
					radius = Integer.decode(getValue(st.nextToken().trim()));
				}
				cond = joinAnd(cond, new ConditionPlayerRangeFromNpc(npcId, radius));
			}
		}

		if (forces[0] + forces[1] > 0)
		{
			cond = joinAnd(cond, new ConditionForceBuff(forces));
		}

		if (cond == null)
		{
			Log.severe("Unrecognized <player> condition in " + _name);
		}
		return cond;
	}

	protected Condition parseTargetCondition(XmlNode n, Object template)
	{
		Condition cond = null;
		for (Entry<String, String> a : n.getAttributes().entrySet())
		{
			if (a.getKey().equalsIgnoreCase("hp"))
			{
				int hp = Math.round(Float.parseFloat(getValue(a.getValue())));
				cond = joinAnd(cond, new ConditionTargetHp(hp));
			}
			else if (a.getKey().equalsIgnoreCase("aggro"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionTargetAggro(val));
			}
			else if (a.getKey().equalsIgnoreCase("siegezone"))
			{
				int value = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionSiegeZone(value, false));
			}
			else if (a.getKey().equalsIgnoreCase("level"))
			{
				int lvl = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionTargetLevel(lvl));
			}
			else if (a.getKey().equalsIgnoreCase("playable"))
			{
				cond = joinAnd(cond, new ConditionTargetPlayable());
			}
			else if ("class_id_restriction".equalsIgnoreCase(a.getKey()))
			{
				StringTokenizer st = new StringTokenizer(a.getValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item)));
				}
				cond = joinAnd(cond, new ConditionTargetClassIdRestriction(array));
			}
			else if ("active_effect_id".equalsIgnoreCase(a.getKey()))
			{
				int effect_id = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionTargetActiveEffectId(effect_id));
			}
			else if ("active_effect_id_lvl".equalsIgnoreCase(a.getKey()))
			{
				String val = getValue(a.getValue());
				int effect_id = Integer.decode(getValue(val.split(",")[0]));
				int effect_lvl = Integer.decode(getValue(val.split(",")[1]));
				cond = joinAnd(cond, new ConditionTargetActiveEffectId(effect_id, effect_lvl));
			}
			else if ("active_effect".equalsIgnoreCase(a.getKey()))
			{
				String effectName = getValue(a.getValue());
				cond = joinAnd(cond, new ConditionTargetActiveEffect(effectName));
			}
			else if ("active_skill_id".equalsIgnoreCase(a.getKey()))
			{
				int skill_id = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionTargetActiveSkillId(skill_id));
			}
			else if ("active_skill_id_lvl".equalsIgnoreCase(a.getKey()))
			{
				String val = getValue(a.getValue());
				int skill_id = Integer.decode(getValue(val.split(",")[0]));
				int skill_lvl = Integer.decode(getValue(val.split(",")[1]));
				cond = joinAnd(cond, new ConditionTargetActiveSkillId(skill_id, skill_lvl));
			}
			else if (a.getKey().equalsIgnoreCase("abnormal"))
			{
				int abnormalId = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionTargetAbnormal(abnormalId));
			}
			else if (a.getKey().equalsIgnoreCase("mindistance"))
			{
				float distance = Float.parseFloat(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionMinDistance(Math.round(distance * distance)));
			}
			// used for npc race
			else if ("race_id".equalsIgnoreCase(a.getKey()))
			{
				StringTokenizer st = new StringTokenizer(a.getValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item)));
				}
				cond = joinAnd(cond, new ConditionTargetRaceId(array));
			}
			// used for pc race
			else if (a.getKey().equalsIgnoreCase("races"))
			{
				final String[] racesVal = a.getValue().split(",");
				final Race[] races = new Race[racesVal.length];
				for (int r = 0; r < racesVal.length; r++)
				{
					if (racesVal[r] != null)
					{
						races[r] = Race.valueOf(racesVal[r]);
					}
				}
				cond = joinAnd(cond, new ConditionTargetRace(races));
			}
			else if (a.getKey().equalsIgnoreCase("using"))
			{
				int mask = 0;
				StringTokenizer st = new StringTokenizer(a.getValue(), ",");
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					for (L2WeaponType wt : L2WeaponType.values())
					{
						if (wt.toString().equals(item))
						{
							mask |= wt.mask();
							break;
						}
					}
					for (L2ArmorType at : L2ArmorType.values())
					{
						if (at.toString().equals(item))
						{
							mask |= at.mask();
							break;
						}
					}
				}
				cond = joinAnd(cond, new ConditionTargetUsesWeaponKind(mask));
			}
			else if (a.getKey().equalsIgnoreCase("npcId"))
			{
				StringTokenizer st = new StringTokenizer(a.getValue(), ",");
				ArrayList<Integer> array = new ArrayList<>(st.countTokens());
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(getValue(item)));
				}
				cond = joinAnd(cond, new ConditionTargetNpcId(array));
			}
			else if (a.getKey().equalsIgnoreCase("npcType"))
			{
				String values = getValue(a.getValue()).trim();
				String[] valuesSplit = values.split(",");

				InstanceType[] types = new InstanceType[valuesSplit.length];
				InstanceType type;

				for (int j = 0; j < valuesSplit.length; j++)
				{
					type = Enum.valueOf(InstanceType.class, valuesSplit[j]);
					if (type == null)
					{
						throw new IllegalArgumentException("Instance type not recognized: " + valuesSplit[j]);
					}
					types[j] = type;
				}

				cond = joinAnd(cond, new ConditionTargetNpcType(types));
			}
			else
			{
				Log.severe("Unrecognized <target> " + a.getKey() + " condition in " + _name);
			}
		}
		return cond;
	}

	protected Condition parseUsingCondition(XmlNode n)
	{
		Condition cond = null;
		for (Entry<String, String> a : n.getAttributes().entrySet())
		{
			if (a.getKey().equalsIgnoreCase("kind"))
			{
				int mask = 0;
				StringTokenizer st = new StringTokenizer(a.getValue(), ",");
				while (st.hasMoreTokens())
				{
					int old = mask;
					String item = st.nextToken().trim();
					if (ItemTable._weaponTypes.containsKey(item))
					{
						mask |= ItemTable._weaponTypes.get(item).mask();
					}

					if (ItemTable._armorTypes.containsKey(item))
					{
						mask |= ItemTable._armorTypes.get(item).mask();
					}

					if (item.equals("crossbow"))
					{
						mask |= L2WeaponType.CROSSBOWK.mask();
					}

					if (old == mask)
					{
						Log.info("[parseUsingCondition=\"kind\"] Unknown item type name: " + item);
					}
				}
				cond = joinAnd(cond, new ConditionUsingItemType(mask));
			}
			else if (a.getKey().equalsIgnoreCase("skill"))
			{
				int id = Integer.parseInt(a.getValue());
				cond = joinAnd(cond, new ConditionUsingSkill(id));
			}
			else if (a.getKey().equalsIgnoreCase("slotitem"))
			{
				StringTokenizer st = new StringTokenizer(a.getValue(), ";");
				int id = Integer.parseInt(st.nextToken().trim());
				int slot = Integer.parseInt(st.nextToken().trim());
				int enchant = 0;
				if (st.hasMoreTokens())
				{
					enchant = Integer.parseInt(st.nextToken().trim());
				}
				cond = joinAnd(cond, new ConditionSlotItemId(slot, id, enchant));
			}
			else if (a.getKey().equalsIgnoreCase("weaponChange"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionChangeWeapon(val));
			}
		}
		if (cond == null)
		{
			Log.severe("Unrecognized <using> condition in " + _name);
		}
		return cond;
	}

	protected Condition parseGameCondition(XmlNode n)
	{
		Condition cond = null;
		for (Entry<String, String> a : n.getAttributes().entrySet())
		{
			if (a.getKey().equalsIgnoreCase("skill"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionWithSkill(val));
			}
			if (a.getKey().equalsIgnoreCase("night"))
			{
				boolean val = Boolean.valueOf(a.getValue());
				cond = joinAnd(cond, new ConditionGameTime(CheckGameTime.NIGHT, val));
			}
			if (a.getKey().equalsIgnoreCase("chance"))
			{
				int val = Integer.decode(getValue(a.getValue()));
				cond = joinAnd(cond, new ConditionGameChance(val));
			}
		}
		if (cond == null)
		{
			Log.severe("Unrecognized <game> condition in " + _name);
		}
		return cond;
	}

	protected Lambda getLambda(XmlNode node, Object template)
	{
		String val = node.getString("val", null);
		if (val != null)
		{
			if (val.charAt(0) == '$')
			{
				if (val.equalsIgnoreCase("$player_level"))
				{
					return new LambdaStats(LambdaStats.StatsType.PLAYER_LEVEL);
				}
				if (val.equalsIgnoreCase("$target_level"))
				{
					return new LambdaStats(LambdaStats.StatsType.TARGET_LEVEL);
				}
				if (val.equalsIgnoreCase("$player_max_hp"))
				{
					return new LambdaStats(LambdaStats.StatsType.PLAYER_MAX_HP);
				}
				if (val.equalsIgnoreCase("$player_max_mp"))
				{
					return new LambdaStats(LambdaStats.StatsType.PLAYER_MAX_MP);
				}
				// try to find value out of item fields
				StatsSet set = getStatsSet();
				String field = set.getString(val.substring(1));
				if (field != null)
				{
					return new LambdaConst(Double.parseDouble(getValue(field)));
				}
				// failed
				throw new IllegalArgumentException("Unknown value " + val);
			}
			else
			{
				return new LambdaConst(Double.parseDouble(getValue(val)));
			}
		}
		LambdaCalc calc = new LambdaCalc();
		node = node.getFirstChild();
		if (node == null || !"val".equals(node.getName()))
		{
			throw new IllegalArgumentException("Value not specified");
		}

		for (XmlNode n : node.getChildren())
		{
			attachLambdaFunc(n, template, calc);
		}

		return calc;
	}

	protected String getValue(String value)
	{
		return value;
	}

	protected Condition joinAnd(Condition cond, Condition c)
	{
		if (cond == null)
		{
			return c;
		}
		if (cond instanceof ConditionLogicAnd)
		{
			((ConditionLogicAnd) cond).add(c);
			return cond;
		}

		ConditionLogicAnd and = new ConditionLogicAnd();
		and.add(cond);
		and.add(c);
		return and;
	}

	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}
}
