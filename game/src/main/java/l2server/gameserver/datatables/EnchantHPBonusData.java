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
import l2server.gameserver.model.Item;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.stats.funcs.FuncTemplate;
import l2server.gameserver.stats.funcs.LambdaConst;
import l2server.gameserver.templates.item.ItemTemplate;
import l2server.util.loader.annotations.Load;
import l2server.util.loader.annotations.Reload;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author MrPoke
 */
public class EnchantHPBonusData {
	private static Logger log = LoggerFactory.getLogger(EnchantHPBonusData.class.getName());



	private final Map<Integer, Integer[]> armorHPBonus = new HashMap<>();
	private static final float fullArmorModifier = 1.5f;

	public static EnchantHPBonusData getInstance() {
		return SingletonHolder.instance;
	}

	private EnchantHPBonusData() {
	}
	
	@Reload("enchantHpBonus")
	@Load(dependencies = ItemTable.class)
	private void load() {
		armorHPBonus.clear();
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "enchantHPBonus.xml");

		if (file.exists()) {
			XmlDocument doc = new XmlDocument(file);
			for (XmlNode d : doc.getChildren()) {
				if (d.getName().equalsIgnoreCase("enchantHP")) {
					if (!d.hasAttribute("grade")) {
						log.error("[EnchantHPBonusData] Missing grade, skipping");
						continue;
					}
					int grade = d.getInt("grade");

					if (!d.hasAttribute("values")) {
						log.error("[EnchantHPBonusData] Missing bonus id: " + grade + ", skipping");
						continue;
					}
					StringTokenizer st = new StringTokenizer(d.getString("values"), ",");
					int tokenCount = st.countTokens();
					Integer[] bonus = new Integer[tokenCount];
					for (int i = 0; i < tokenCount; i++) {
						Integer value = Integer.decode(st.nextToken().trim());
						if (value == null) {
							log.error("[EnchantHPBonusData] Bad Hp value!! grade: " + grade + " token: " + i);
							value = 0;
						}
						bonus[i] = value;
					}
					armorHPBonus.put(grade, bonus);
				}
			}
			if (armorHPBonus.isEmpty()) {
				return;
			}

			Collection<Integer> itemIds = ItemTable.getInstance().getAllArmorsId();
			int count = 0;

			for (Integer itemId : itemIds) {
				ItemTemplate item = ItemTable.getInstance().getTemplate(itemId);
				if (item != null && item.getCrystalType() != ItemTemplate.CRYSTAL_NONE) {
					switch (item.getBodyPart()) {
						case ItemTemplate.SLOT_CHEST:
						case ItemTemplate.SLOT_FEET:
						case ItemTemplate.SLOT_GLOVES:
						case ItemTemplate.SLOT_HEAD:
						case ItemTemplate.SLOT_LEGS:
						case ItemTemplate.SLOT_BACK:
						case ItemTemplate.SLOT_FULL_ARMOR:
						case ItemTemplate.SLOT_UNDERWEAR:
						case ItemTemplate.SLOT_L_HAND:
							count++;
							FuncTemplate ft = new FuncTemplate(null, "EnchantHp", Stats.MAX_HP, new LambdaConst(0));
							item.attach(ft);
							break;
					}
				}
			}

			// shields in the weapons table
			itemIds = ItemTable.getInstance().getAllWeaponsId();
			for (Integer itemId : itemIds) {
				ItemTemplate item = ItemTable.getInstance().getTemplate(itemId);
				if (item != null && item.getCrystalType() != ItemTemplate.CRYSTAL_NONE) {
					switch (item.getBodyPart()) {
						case ItemTemplate.SLOT_L_HAND:
							count++;
							FuncTemplate ft = new FuncTemplate(null, "EnchantHp", Stats.MAX_HP, new LambdaConst(0));
							item.attach(ft);
							break;
					}
				}
			}
			log.info("Enchant HP Bonus registered for " + count + " items.");
		}
	}

	public final int getHPBonus(Item item) {
		final Integer[] values = armorHPBonus.get(item.getItem().getItemGradePlain());

		if (values == null || values.length == 0) {
			return 0;
		}

		if (item.getItem().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR) {
			return (int) (values[Math.min(item.getEnchantLevel(), values.length) - 1] * fullArmorModifier);
		} else {
			return values[Math.min(item.getEnchantLevel(), values.length) - 1];
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final EnchantHPBonusData instance = new EnchantHPBonusData();
	}
}
