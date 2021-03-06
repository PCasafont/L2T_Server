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

package l2server.gameserver.datatables;

import l2server.Config;
import l2server.gameserver.model.EnchantEffect;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.stats.funcs.*;
import l2server.util.loader.annotations.Load;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Pere
 */
public class EnchantEffectTable {
	private static Logger log = LoggerFactory.getLogger(EnchantEffectTable.class.getName());

	public static EnchantEffectTable getInstance() {
		return SingletonHolder.instance;
	}

	private final Map<Integer, EnchantEffect> effects = new HashMap<>();

	// =========================================================
	// Constructor
	private EnchantEffectTable() {
	}
	
	@Load
	public void load() {
		File dir = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "enchanteffects");
		if (!dir.exists()) {
			log.warn("Dir " + dir.getAbsolutePath() + " does not exist");
			return;
		}

		List<File> validFiles = new ArrayList<>();
		File[] files = dir.listFiles();
		for (File f : files) {
			if (f.getName().endsWith(".xml")) {
				validFiles.add(f);
			}
		}

		for (File f : validFiles) {
			try {
				XmlDocument doc = new XmlDocument(f);
				for (XmlNode e : doc.getChildren()) {
					if (!e.getName().equalsIgnoreCase("effect")) {
						continue;
					}

					int id = e.getInt("id");
					int rarity = e.getInt("rarity");
					int slot = e.getInt("slot");
					EnchantEffect effect = new EnchantEffect(id, rarity, slot);

					for (XmlNode effectNode : e.getChildren()) {
						if (effectNode.getName().equalsIgnoreCase("skill")) {
							int skillId = effectNode.getInt("id");
							int skillLevel = effectNode.getInt("level");
							effect.setSkill(skillId, skillLevel);
							continue;
						}

						String stat = effectNode.getString("stat", "");
						double val = effectNode.getDouble("val", 0.0);
						Func func = null;
						if (effectNode.getName().equalsIgnoreCase("add")) {
							func = new FuncAdd(Stats.fromString(stat), effect, new LambdaConst(val));
						} else if (effectNode.getName().equalsIgnoreCase("baseAdd")) {
							func = new FuncBaseAdd(Stats.fromString(stat), effect, new LambdaConst(val));
						} else if (effectNode.getName().equalsIgnoreCase("addPercent")) {
							func = new FuncAddPercent(Stats.fromString(stat), effect, new LambdaConst(val));
						}

						if (func != null) {
							effect.addFunc(func);
						}
					}

					effects.put(id, effect);
				}
			} catch (Exception e) {
				log.error("Error loading enchant effects", e);
				return;
			}
		}

		log.info("EnchantEffectTable: Loaded " + effects.size() + " enchant effects.");
	}

	public final EnchantEffect getEffect(int id) {
		return effects.get(id);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final EnchantEffectTable instance = new EnchantEffectTable();
	}
}
