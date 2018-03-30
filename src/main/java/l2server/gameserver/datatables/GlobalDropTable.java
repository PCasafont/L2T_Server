package l2server.gameserver.datatables;

import gnu.trove.TIntIntHashMap;
import l2server.Config;
import l2server.gameserver.Reloadable;
import l2server.gameserver.ReloadableManager;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.util.Rnd;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Pere
 */
public class GlobalDropTable implements Reloadable {
	public class GlobalDropCategory {
		private List<Integer> itemIds = new ArrayList<>();
		private String description;
		private int chance;
		private int minAmount;
		private int maxAmount;
		private int mobId;
		private int minLevel;
		private int maxLevel;
		private boolean raidOnly;
		private int maxDailyCount;

		private TIntIntHashMap countsPerPlayer = new TIntIntHashMap();

		public GlobalDropCategory(String description,
		                          int chance,
		                          int minAmount,
		                          int maxAmount,
		                          int mobId,
		                          int minLevel,
		                          int maxLevel,
		                          boolean raidOnly,
		                          int maxDailyCount) {
			this.description = description;
			this.chance = chance;
			this.minAmount = minAmount;
			this.maxAmount = maxAmount;
			this.mobId = mobId;
			this.minLevel = minLevel;
			this.maxLevel = maxLevel;
			this.raidOnly = raidOnly;
			this.maxDailyCount = maxDailyCount;
		}

		public void addItem(int itemId) {
			itemIds.add(itemId);
		}

		public String getDescription() {
			return description;
		}

		public int getChance() {
			return chance;
		}

		public int getMinAmount() {
			return minAmount;
		}

		public int getMaxAmount() {
			return maxAmount;
		}

		public int getMobId() {
			return mobId;
		}

		public int getMinLevel() {
			return minLevel;
		}

		public int getMaxLevel() {
			return maxLevel;
		}

		public List<Integer> getRewards() {
			return itemIds;
		}

		public int getRandomReward() {
			return itemIds.get(Rnd.get(itemIds.size()));
		}

		public boolean isRaidOnly() {
			return raidOnly;
		}

		public int getMaxDailyCount() {
			return maxDailyCount;
		}

		public int getCountForPlayer(L2PcInstance player) {
			int count = 0;
			synchronized (countsPerPlayer) {
				if (countsPerPlayer.containsKey(player.getObjectId())) {
					count = countsPerPlayer.get(player.getObjectId());
				}
			}

			return count;
		}

		public void increaseCountForPlayer(L2PcInstance player) {
			synchronized (countsPerPlayer) {
				int count = 0;
				if (countsPerPlayer.containsKey(player.getObjectId())) {
					count = countsPerPlayer.get(player.getObjectId());
				}

				countsPerPlayer.put(player.getObjectId(), count + 1);
			}
		}

		public void resetCountsPerPlayer() {
			synchronized (countsPerPlayer) {
				countsPerPlayer.clear();
			}
		}

		public boolean canLootNow(L2PcInstance player) {
			if (maxDailyCount <= 0) {
				return true;
			}

			return getCountForPlayer(player) < maxDailyCount;
		}
	}

	private static GlobalDropTable instance;

	private static List<GlobalDropCategory> globalDropCategories = new ArrayList<>();

	ScheduledFuture<?> resetSchedule = null;

	public static GlobalDropTable getInstance() {
		if (instance == null) {
			instance = new GlobalDropTable();
		}

		return instance;
	}

	private GlobalDropTable() {
		reload();

		ReloadableManager.getInstance().register("globaldrops", this);
	}

	@Override
	public boolean reload() {
		globalDropCategories.clear();
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "globalDrops.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode d : doc.getChildren()) {
			if (d.getName().equalsIgnoreCase("globalDrop")) {
				String description = d.getString("description");
				int chance = (int) (d.getFloat("chance") * 1000.0f);
				int minAmount = d.getInt("minAmount", 1);
				int maxAmount = d.getInt("maxAmount", 1);
				int mobId = d.getInt("mobId", 0);
				int minLevel = d.getInt("minLevel", 1);
				int maxLevel = d.getInt("maxLevel", 100);
				boolean raidOnly = d.getBool("raidOnly", false);
				int maxDailyCount = d.getInt("maxDailyCount", 0);

				GlobalDropCategory drop =
						new GlobalDropCategory(description, chance, minAmount, maxAmount, mobId, minLevel, maxLevel, raidOnly, maxDailyCount);
				for (XmlNode propertyNode : d.getChildren()) {
					if (propertyNode.getName().equalsIgnoreCase("item")) {
						int itemId = propertyNode.getInt("id");
						drop.addItem(itemId);
					}
				}
				globalDropCategories.add(drop);
			}
		}

		if (resetSchedule != null) {
			resetSchedule.cancel(false);
		}

		Calendar firstRun = Calendar.getInstance();
		firstRun.set(Calendar.HOUR_OF_DAY, 6);
		firstRun.set(Calendar.MINUTE, 0);
		firstRun.set(Calendar.SECOND, 0);
		if (firstRun.getTimeInMillis() < System.currentTimeMillis()) {
			firstRun.add(Calendar.DAY_OF_MONTH, 1);
		}

		long initial = firstRun.getTimeInMillis() - System.currentTimeMillis();
		long delay = 24 * 3600 * 1000L;

		resetSchedule = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> {
			for (GlobalDropCategory cat : globalDropCategories) {
				cat.resetCountsPerPlayer();
			}
		}, initial, delay);

		return true;
	}

	@Override
	public String getReloadMessage(boolean success) {
		return "Global Drops reloaded";
	}

	public List<GlobalDropCategory> getGlobalDropCategories() {
		return globalDropCategories;
	}
}
