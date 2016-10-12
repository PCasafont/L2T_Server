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

import l2server.gameserver.model.L2CrystallizeReward;
import l2server.gameserver.model.L2ExtractableProduct;
import l2server.gameserver.stats.conditions.Condition;
import l2server.gameserver.stats.funcs.FuncTemplate;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.item.L2EtcItem;
import l2server.gameserver.templates.item.L2Item;
import l2server.log.Log;
import l2server.util.xml.XmlNode;

import java.lang.reflect.Constructor;
import java.util.Map.Entry;

/**
 * @author mkizub, JIV
 */
public final class ItemParser extends StatsParser
{
	private String _type;
	private StatsSet _set;
	private L2Item _item = null;

	public ItemParser(XmlNode node)
	{
		super(node);
	}

	@Override
	protected StatsSet getStatsSet()
	{
		return _set;
	}

	@Override
	public void parse() throws RuntimeException
	{
		_type = _node.getString("type");
		_set = new StatsSet();
		for (Entry<String, String> e : _node.getAttributes().entrySet())
		{
			_set.set(e.getKey(), e.getValue());
		}

		makeItem();

		parseChildren();

		if (_node.hasAttribute("rCrit") && !_node.hasAttribute("mCritRate"))
		{
			_node.getAttributes().put("mCritRate", _node.getString("rCrit"));
		}
		parseTemplate(_node, _item);
	}

	public void parse(ItemParser original) throws RuntimeException
	{
		_type = _node.getString("type", original._type);

		_set = new StatsSet();
		_set.add(original._set);

		for (Entry<String, String> e : _node.getAttributes().entrySet())
		{
			_set.set(e.getKey(), e.getValue());
		}

		makeItem();

		if (original._item.getConditions() != null && !_set.getBool("overrideCond", false))
		{
			for (Condition cond : original._item.getConditions())
			{
				_item.attach(cond);
			}
		}

		if (original._item.getSkills() != null && !_set.getBool("overrideSkills", false))
		{
			for (SkillHolder sh : original._item.getSkills())
			{
				_item.attach(sh);
			}
		}

		parseChildren();

		if (original._item.getFuncs() != null && !_set.getBool("overrideStats", false))
		{
			for (FuncTemplate func : original._item.getFuncs())
			{
				_item.attach(func);
			}
		}

		parseTemplate(_node, _item);
	}

	private void parseChildren()
	{
		for (XmlNode n : _node.getChildren())
		{
			if (n.getName().equalsIgnoreCase("cond"))
			{
				Condition condition = parseCondition(n.getFirstChild(), _item);
				if (condition != null && n.hasAttribute("msg"))
				{
					condition.setMessage(n.getString("msg"));
				}
				else if (condition != null && n.hasAttribute("msgId"))
				{
					condition.setMessageId(Integer.decode(getValue(n.getString("msgId"))));
					if (n.hasAttribute("addName") && Integer.decode(getValue(n.getString("msgId"))) > 0)
					{
						condition.addName();
					}
				}
				_item.attach(condition);
			}
			else if (n.getName().equalsIgnoreCase("skill"))
			{
				int skillId = n.getInt("id");
				int skillLvl = n.getInt("level");
				_item.attach(new SkillHolder(skillId, skillLvl));
			}
			else if (n.getName().equalsIgnoreCase("crystallizeReward"))
			{
				int itemId = n.getInt("id");
				int count = n.getInt("count");
				double chance = n.getDouble("chance");
				_item.attach(new L2CrystallizeReward(itemId, count, chance));
			}
			else if (n.getName().equalsIgnoreCase("capsuledItem") && _item instanceof L2EtcItem)
			{
				int itemId = n.getInt("id");
				int min = n.getInt("min");
				int max = n.getInt("max");
				double chance = n.getDouble("chance");
				if (max < min)
				{
					Log.info("> Max amount < Min amount in part " + itemId + ", item " + _item);
					continue;
				}
				((L2EtcItem) _item).attach(new L2ExtractableProduct(itemId, min, max, chance));
			}
		}
	}

	private void makeItem() throws RuntimeException
	{
		if (_item != null)
		{
			return; // item is already created
		}
		try
		{
			Constructor<?> c =
					Class.forName("l2server.gameserver.templates.item.L2" + _type).getConstructor(StatsSet.class);
			_item = (L2Item) c.newInstance(_set);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public L2Item getItem()
	{
		return _item;
	}
}
