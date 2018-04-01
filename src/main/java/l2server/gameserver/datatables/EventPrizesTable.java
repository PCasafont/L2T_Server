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
import l2server.gameserver.events.instanced.EventPrize;
import l2server.gameserver.events.instanced.EventPrize.EventPrizeCategory;
import l2server.gameserver.events.instanced.EventPrize.EventPrizeItem;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.log.Log;
import l2server.util.Rnd;
import l2server.util.loader.annotations.Load;
import l2server.util.loader.annotations.Reload;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Pere
 */
public class EventPrizesTable {
	private final Map<String, List<EventPrize>> prizes = new HashMap<>();

	private EventPrizesTable() {
	}

	@Reload("eventPrizes")
	@Load
	public void load() {
		prizes.clear();

		XmlDocument doc = new XmlDocument(new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "eventPrizes.xml"));
		int przCount = 0;
		for (XmlNode n : doc.getChildren()) {
			if (!n.getName().equals("list")) {
				continue;
			}

			for (XmlNode prizeNode : n.getChildren()) {
				if (!prizeNode.getName().equals("prizeList")) {
					continue;
				}

				String name = prizeNode.getString("name");
				for (XmlNode node : prizeNode.getChildren()) {
					List<EventPrize> list = prizes.get(name);
					if (list == null) {
						list = new ArrayList<>();
						prizes.put(name, list);
					}

					if (node.getName().equalsIgnoreCase("prizeItem")) {
						list.add(new EventPrizeItem(node));
						przCount++;
					} else if (node.getName().equalsIgnoreCase("prizeCategory")) {
						list.add(new EventPrizeCategory(node));
						przCount++;
					}
				}
			}
		}

		Log.info("Event Prizes Table: loaded " + przCount + " prizes in " + prizes.size() + " categories.");
	}

	public void rewardPlayer(String prizeName, L2PcInstance player, float teamMultiplier, float performanceMultiplier) {
		List<EventPrize> list = prizes.get(prizeName);
		if (list == null) {
			return;
		}

		for (EventPrize prize : list) {
			float multiplier = teamMultiplier;
			if (prize.dependsOnPerformance()) {
				multiplier *= performanceMultiplier;
			}

			float chance = prize.getChance() * multiplier;
			if (chance < 100.0f) {
				float rnd = Rnd.get(100000) / 1000.0f;
				if (chance < rnd) {
					continue;
				}

				multiplier = 1.0f;
			} else {
				multiplier = chance / 100.0f;
			}

			while (multiplier > 0) {
				EventPrizeItem prizeItem = prize.getItem();
				float mul = 1.0f;
				if (multiplier < 1.0f) {
					mul = multiplier;
				}
				int prizeCount = Math.round(Rnd.get(prizeItem.getMin(), prizeItem.getMax()) * mul);
				if (prizeCount > 0) {
					player.addItem("Event", prizeItem.getId(), prizeCount, player, true);
				}

				multiplier -= 1.0f;
			}
		}
	}

	private static EventPrizesTable instance;

	public static EventPrizesTable getInstance() {
		if (instance == null) {
			instance = new EventPrizesTable();
		}

		return instance;
	}
}
