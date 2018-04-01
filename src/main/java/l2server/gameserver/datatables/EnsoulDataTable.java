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
import l2server.gameserver.model.EnsoulEffect;
import l2server.gameserver.model.SoulCrystal;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.stats.conditions.Condition;
import l2server.gameserver.stats.funcs.FuncTemplate;
import l2server.gameserver.stats.funcs.Lambda;
import l2server.gameserver.stats.funcs.LambdaConst;
import l2server.log.Log;
import l2server.util.loader.annotations.Load;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pere
 */
public class EnsoulDataTable {
	public static EnsoulDataTable getInstance() {
		return SingletonHolder.instance;
	}

	private final Map<Integer, EnsoulEffect> effects = new HashMap<>();
	private final Map<Integer, SoulCrystal> crystals = new HashMap<>();

	// =========================================================
	// Constructor
	private EnsoulDataTable() {
	}
	
	@Load
	private void load() {
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "ensoul/effects.xml");
		if (!file.exists()) {
			Log.warning("File " + file.getAbsolutePath() + " does not exist");
			return;
		}

		XmlDocument doc = new XmlDocument(file);
		for (XmlNode effectNode : doc.getChildren()) {
			if (!effectNode.getName().equalsIgnoreCase("effect")) {
				continue;
			}

			int id = effectNode.getInt("id");
			String name = effectNode.getString("name");
			int group = effectNode.getInt("group");
			int stage = effectNode.getInt("stage");
			EnsoulEffect effect = new EnsoulEffect(id, name, group, stage);

			for (XmlNode funcNode : effectNode.getChildren()) {
				FuncTemplate ft = parseFunc(funcNode);
				if (ft != null) {
					effect.addFunc(ft.getFunc(effect));
				}
			}

			effects.put(id, effect);
		}

		Log.info("EnsoulDataTable: Loaded " + effects.size() + " ensoul effects.");

		file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "ensoul/crystals.xml");
		if (!file.exists()) {
			Log.warning("File " + file.getAbsolutePath() + " does not exist");
			return;
		}

		doc = new XmlDocument(file);
		for (XmlNode crystalNode : doc.getChildren()) {
			if (!crystalNode.getName().equalsIgnoreCase("crystal")) {
				continue;
			}

			int id = crystalNode.getInt("id");
			boolean special = crystalNode.getBool("special", false);
			SoulCrystal sc = new SoulCrystal(id, special);

			for (XmlNode effectNode : crystalNode.getChildren()) {
				int effectId = effectNode.getInt("id");
				sc.addEffect(effects.get(effectId));
			}

			crystals.put(id, sc);
		}

		Log.info("EnsoulDataTable: Loaded " + crystals.size() + " soul crystals.");
	}

	public final EnsoulEffect getEffect(int id) {
		return effects.get(id);
	}

	public final SoulCrystal getCrystal(int id) {
		return crystals.get(id);
	}

	private FuncTemplate parseFunc(XmlNode n) {
		String funcName = n.getName();
		funcName = funcName.substring(0, 1).toUpperCase() + funcName.substring(1);
		String statString = n.getString("stat");
		double val = n.getDouble("val", 0.0);
		Stats stat = Stats.fromString(statString);
		Lambda lambda = new LambdaConst(val);

		Condition applyCond = null; //TODO
		return new FuncTemplate(applyCond, funcName, stat, lambda);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final EnsoulDataTable instance = new EnsoulDataTable();
	}
}
