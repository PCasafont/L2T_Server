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
import l2server.gameserver.templates.item.EtcItemTemplate;
import l2server.gameserver.templates.item.ItemTemplate;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Map.Entry;

/**
 * @author mkizub, JIV
 */
public final class ItemParser extends StatsParser {
	
	private static Logger log = LoggerFactory.getLogger(ItemParser.class.getName());
	
	private String type;
	private StatsSet set;
	private ItemTemplate item = null;

	public ItemParser(XmlNode node) {
		super(node);
	}

	@Override
	protected StatsSet getStatsSet() {
		return set;
	}

	@Override
	public void parse() throws RuntimeException {
		type = node.getString("type");
		set = new StatsSet();
		for (Entry<String, String> e : node.getAttributes().entrySet()) {
			set.set(e.getKey(), e.getValue());
		}

		makeItem();

		parseChildren();

		if (node.hasAttribute("rCrit") && !node.hasAttribute("mCritRate")) {
			node.getAttributes().put("mCritRate", node.getString("rCrit"));
		}
		parseTemplate(node, item);
	}

	public void parse(ItemParser original) throws RuntimeException {
		type = node.getString("type", original.type);

		set = new StatsSet();
		set.add(original.set);

		for (Entry<String, String> e : node.getAttributes().entrySet()) {
			set.set(e.getKey(), e.getValue());
		}

		makeItem();

		if (original.item.getConditions() != null && !set.getBool("overrideCond", false)) {
			for (Condition cond : original.item.getConditions()) {
				item.attach(cond);
			}
		}

		if (original.item.getSkills() != null && !set.getBool("overrideSkills", false)) {
			for (SkillHolder sh : original.item.getSkills()) {
				item.attach(sh);
			}
		}

		parseChildren();

		if (original.item.getFuncs() != null && !set.getBool("overrideStats", false)) {
			for (FuncTemplate func : original.item.getFuncs()) {
				item.attach(func);
			}
		}

		parseTemplate(node, item);
	}

	private void parseChildren() {
		for (XmlNode n : node.getChildren()) {
			if (n.getName().equalsIgnoreCase("cond")) {
				Condition condition = parseCondition(n.getFirstChild(), item);
				if (condition != null && n.hasAttribute("msg")) {
					condition.setMessage(n.getString("msg"));
				} else if (condition != null && n.hasAttribute("msgId")) {
					condition.setMessageId(Integer.decode(getValue(n.getString("msgId"))));
					if (n.hasAttribute("addName") && Integer.decode(getValue(n.getString("msgId"))) > 0) {
						condition.addName();
					}
				}
				item.attach(condition);
			} else if (n.getName().equalsIgnoreCase("skill")) {
				int skillId = n.getInt("id");
				int skillLvl = n.getInt("level");
				item.attach(new SkillHolder(skillId, skillLvl));
			} else if (n.getName().equalsIgnoreCase("crystallizeReward")) {
				int itemId = n.getInt("id");
				int count = n.getInt("count");
				double chance = n.getDouble("chance");
				item.attach(new L2CrystallizeReward(itemId, count, chance));
			} else if (n.getName().equalsIgnoreCase("capsuledItem") && item instanceof EtcItemTemplate) {
				int itemId = n.getInt("id");
				int min = n.getInt("min");
				int max = n.getInt("max");
				double chance = n.getDouble("chance");
				if (max < min) {
					log.info("> Max amount < Min amount in part " + itemId + ", item " + item);
					continue;
				}
				((EtcItemTemplate) item).attach(new L2ExtractableProduct(itemId, min, max, chance));
			}
		}
	}

	private void makeItem() throws RuntimeException {
		if (item != null) {
			return; // item is already created
		}
		try {
			Constructor<?> c = Class.forName("l2server.gameserver.templates.item." + type + "Template").getConstructor(StatsSet.class);
			item = (ItemTemplate) c.newInstance(set);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ItemTemplate getItem() {
		return item;
	}
}
