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
public class GlobalDropTable implements Reloadable
{
	public class GlobalDropCategory
	{
		private List<Integer> _itemIds = new ArrayList<>();
		private String _description;
		private int _chance;
		private int _minAmount;
		private int _maxAmount;
		private int _mobId;
		private int _minLevel;
		private int _maxLevel;
		private boolean _raidOnly;
		private int _maxDailyCount;

		private TIntIntHashMap _countsPerPlayer = new TIntIntHashMap();

		public GlobalDropCategory(String description, int chance, int minAmount, int maxAmount, int mobId, int minLevel, int maxLevel, boolean raidOnly, int maxDailyCount)
		{
			_description = description;
			_chance = chance;
			_minAmount = minAmount;
			_maxAmount = maxAmount;
			_mobId = mobId;
			_minLevel = minLevel;
			_maxLevel = maxLevel;
			_raidOnly = raidOnly;
			_maxDailyCount = maxDailyCount;
		}

		public void addItem(int itemId)
		{
			_itemIds.add(itemId);
		}

		public String getDescription()
		{
			return _description;
		}

		public int getChance()
		{
			return _chance;
		}

		public int getMinAmount()
		{
			return _minAmount;
		}

		public int getMaxAmount()
		{
			return _maxAmount;
		}

		public int getMobId()
		{
			return _mobId;
		}

		public int getMinLevel()
		{
			return _minLevel;
		}

		public int getMaxLevel()
		{
			return _maxLevel;
		}

		public List<Integer> getRewards()
		{
			return _itemIds;
		}

		public int getRandomReward()
		{
			return _itemIds.get(Rnd.get(_itemIds.size()));
		}

		public boolean isRaidOnly()
		{
			return _raidOnly;
		}

		public int getMaxDailyCount()
		{
			return _maxDailyCount;
		}

		public int getCountForPlayer(L2PcInstance player)
		{
			int count = 0;
			synchronized (_countsPerPlayer)
			{
				if (_countsPerPlayer.containsKey(player.getObjectId()))
				{
					count = _countsPerPlayer.get(player.getObjectId());
				}
			}

			return count;
		}

		public void increaseCountForPlayer(L2PcInstance player)
		{
			synchronized (_countsPerPlayer)
			{
				int count = 0;
				if (_countsPerPlayer.containsKey(player.getObjectId()))
				{
					count = _countsPerPlayer.get(player.getObjectId());
				}

				_countsPerPlayer.put(player.getObjectId(), count + 1);
			}
		}

		public void resetCountsPerPlayer()
		{
			synchronized (_countsPerPlayer)
			{
				_countsPerPlayer.clear();
			}
		}

		public boolean canLootNow(L2PcInstance player)
		{
			if (_maxDailyCount <= 0)
			{
				return true;
			}

			return getCountForPlayer(player) < _maxDailyCount;
		}
	}

	private static GlobalDropTable _instance;

	private static List<GlobalDropCategory> _globalDropCategories = new ArrayList<>();

	ScheduledFuture<?> _resetSchedule = null;

	public static GlobalDropTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new GlobalDropTable();
		}

		return _instance;
	}

	private GlobalDropTable()
	{
		reload();

		ReloadableManager.getInstance().register("globaldrops", this);
	}

	@Override
	public boolean reload()
	{
		_globalDropCategories.clear();
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "globalDrops.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("globalDrop"))
					{
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
								new GlobalDropCategory(description, chance, minAmount, maxAmount, mobId, minLevel,
										maxLevel, raidOnly, maxDailyCount);
						for (XmlNode propertyNode : d.getChildren())
						{
							if (propertyNode.getName().equalsIgnoreCase("item"))
							{
								int itemId = propertyNode.getInt("id");
								drop.addItem(itemId);
							}
						}
						_globalDropCategories.add(drop);
					}
				}
			}
		}

		if (_resetSchedule != null)
		{
			_resetSchedule.cancel(false);
		}

		Calendar firstRun = Calendar.getInstance();
		firstRun.set(Calendar.HOUR_OF_DAY, 6);
		firstRun.set(Calendar.MINUTE, 0);
		firstRun.set(Calendar.SECOND, 0);
		if (firstRun.getTimeInMillis() < System.currentTimeMillis())
		{
			firstRun.add(Calendar.DAY_OF_MONTH, 1);
		}

		long initial = firstRun.getTimeInMillis() - System.currentTimeMillis();
		long delay = 24 * 3600 * 1000L;

		_resetSchedule = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			for (GlobalDropCategory cat : _globalDropCategories)
			{
				cat.resetCountsPerPlayer();
			}
		}, initial, delay);

		return true;
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return "Global Drops reloaded";
	}

	public List<GlobalDropCategory> getGlobalDropCategories()
	{
		return _globalDropCategories;
	}
}
