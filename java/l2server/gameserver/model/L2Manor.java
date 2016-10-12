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

package l2server.gameserver.model;

import l2server.Config;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.templates.item.L2Item;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class for manor
 *
 * @author l3x
 */

public class L2Manor
{
	private static ConcurrentHashMap<Integer, SeedData> _seeds;

	private L2Manor()
	{
		_seeds = new ConcurrentHashMap<>();
		parseData();
	}

	public static L2Manor getInstance()
	{
		return SingletonHolder._instance;
	}

	public ArrayList<Integer> getAllCrops()
	{
		ArrayList<Integer> crops = new ArrayList<>();

		for (SeedData seed : _seeds.values())
		{
			if (!crops.contains(seed.getCrop()) && seed.getCrop() != 0 && !crops.contains(seed.getCrop()))
			{
				crops.add(seed.getCrop());
			}
		}

		return crops;
	}

	public int getSeedBasicPrice(int seedId)
	{
		L2Item seedItem = ItemTable.getInstance().getTemplate(seedId);

		if (seedItem != null)
		{
			return seedItem.getReferencePrice();
		}
		else
		{
			return 0;
		}
	}

	public int getSeedBasicPriceByCrop(int cropId)
	{
		for (SeedData seed : _seeds.values())
		{
			if (seed.getCrop() == cropId)
			{
				return getSeedBasicPrice(seed.getId());
			}
		}
		return 0;
	}

	public int getCropBasicPrice(int cropId)
	{
		L2Item cropItem = ItemTable.getInstance().getTemplate(cropId);

		if (cropItem != null)
		{
			return cropItem.getReferencePrice();
		}
		else
		{
			return 0;
		}
	}

	public int getMatureCrop(int cropId)
	{
		for (SeedData seed : _seeds.values())
		{
			if (seed.getCrop() == cropId)
			{
				return seed.getMature();
			}
		}
		return 0;
	}

	/**
	 * Returns price which lord pays to buy one seed
	 *
	 * @param seedId
	 * @return seed price
	 */
	public long getSeedBuyPrice(int seedId)
	{
		long buyPrice = getSeedBasicPrice(seedId);
		return buyPrice > 0 ? buyPrice : 1;
	}

	public int getSeedMinLevel(int seedId)
	{
		SeedData seed = _seeds.get(seedId);

		if (seed != null)
		{
			return seed.getLevel() - 5;
		}
		return -1;
	}

	public int getSeedMaxLevel(int seedId)
	{
		SeedData seed = _seeds.get(seedId);

		if (seed != null)
		{
			return seed.getLevel() + 5;
		}
		return -1;
	}

	public int getSeedLevelByCrop(int cropId)
	{
		for (SeedData seed : _seeds.values())
		{
			if (seed.getCrop() == cropId)
			{
				return seed.getLevel();
			}
		}
		return 0;
	}

	public int getSeedLevel(int seedId)
	{
		SeedData seed = _seeds.get(seedId);

		if (seed != null)
		{
			return seed.getLevel();
		}
		return -1;
	}

	public boolean isAlternative(int seedId)
	{
		for (SeedData seed : _seeds.values())
		{
			if (seed.getId() == seedId)
			{
				return seed.isAlternative();
			}
		}
		return false;
	}

	public int getCropType(int seedId)
	{
		SeedData seed = _seeds.get(seedId);

		if (seed != null)
		{
			return seed.getCrop();
		}
		return -1;
	}

	public int getRewardItem(int cropId, int type)
	{
		for (SeedData seed : _seeds.values())
		{
			if (seed.getCrop() == cropId)
			{
				return seed.getReward(type); // there can be several
				// seeds with same crop, but
				// reward should be the same for
				// all
			}
		}
		return -1;
	}

	public int getRewardItemBySeed(int seedId, int type)
	{
		SeedData seed = _seeds.get(seedId);

		if (seed != null)
		{
			return seed.getReward(type);
		}
		return 0;
	}

	/**
	 * Return all crops which can be purchased by given castle
	 *
	 * @param castleId
	 * @return
	 */
	public ArrayList<Integer> getCropsForCastle(int castleId)
	{
		ArrayList<Integer> crops = new ArrayList<>();

		for (SeedData seed : _seeds.values())
		{
			if (seed.getManorId() == castleId && !crops.contains(seed.getCrop()))
			{
				crops.add(seed.getCrop());
			}
		}

		return crops;
	}

	/**
	 * Return list of seed ids, which belongs to castle with given id
	 *
	 * @param castleId - id of the castle
	 * @return seedIds - list of seed ids
	 */
	public ArrayList<Integer> getSeedsForCastle(int castleId)
	{
		ArrayList<Integer> seedsID = new ArrayList<>();

		for (SeedData seed : _seeds.values())
		{
			if (seed.getManorId() == castleId && !seedsID.contains(seed.getId()))
			{
				seedsID.add(seed.getId());
			}
		}

		return seedsID;
	}

	/**
	 * Returns castle id where seed can be sowned<br>
	 *
	 * @param seedId
	 * @return castleId
	 */
	public int getCastleIdForSeed(int seedId)
	{
		SeedData seed = _seeds.get(seedId);

		if (seed != null)
		{
			return seed.getManorId();
		}
		return 0;
	}

	public int getSeedSaleLimit(int seedId)
	{
		SeedData seed = _seeds.get(seedId);

		if (seed != null)
		{
			return seed.getSeedLimit();
		}
		return 0;
	}

	public int getCropPuchaseLimit(int cropId)
	{
		for (SeedData seed : _seeds.values())
		{
			if (seed.getCrop() == cropId)
			{
				return seed.getCropLimit();
			}
		}
		return 0;
	}

	private static class SeedData
	{
		private int _id;
		private int _level; // seed level
		private int _crop; // crop type
		private int _mature; // mature crop type
		private int _type1;
		private int _type2;
		private int _manorId; // id of manor (castle id) where seed can be farmed
		private boolean _isAlternative;
		private int _limitSeeds;
		private int _limitCrops;

		public SeedData(int level, int crop, int mature)
		{
			_level = level;
			_crop = crop;
			_mature = mature;
		}

		public void setData(int id, int t1, int t2, int manorId, boolean isAlt, int lim1, int lim2)
		{
			_id = id;
			_type1 = t1;
			_type2 = t2;
			_manorId = manorId;
			_isAlternative = isAlt;
			_limitSeeds = lim1;
			_limitCrops = lim2;
		}

		public int getManorId()
		{
			return _manorId;
		}

		public int getId()
		{
			return _id;
		}

		public int getCrop()
		{
			return _crop;
		}

		public int getMature()
		{
			return _mature;
		}

		public int getReward(int type)
		{
			return type == 1 ? _type1 : _type2;
		}

		public int getLevel()
		{
			return _level;
		}

		public boolean isAlternative()
		{
			return _isAlternative;
		}

		public int getSeedLimit()
		{
			return _limitSeeds * Config.RATE_DROP_MANOR;
		}

		public int getCropLimit()
		{
			return _limitCrops * Config.RATE_DROP_MANOR;
		}

		@Override
		public String toString()
		{
			return "SeedData [_id=" + _id + ", _level=" + _level + ", _crop=" + _crop + ", _mature=" + _mature +
					", _type1=" + _type1 + ", _type2=" + _type2 + ", _manorId=" + _manorId + ", _isAlternative=" +
					_isAlternative + ", _limitSeeds=" + _limitSeeds + ", _limitCrops=" + _limitCrops + "]";
		}
	}

	private void parseData()
	{
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "seeds.xml");
		XmlDocument doc = new XmlDocument(file);

		//list
		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				//castle
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("castle"))
					{
						int castleId = d.getInt("id");
						//crop
						for (XmlNode c : d.getChildren())
						{
							if (c.getName().equalsIgnoreCase("crop"))
							{
								int cropId = c.getInt("id");
								int seedId = 0;
								int matureId = 0;
								int type1R = 0;
								int type2R = 0;
								boolean isAlt = false;
								int level = 0;
								int limitSeeds = 0;
								int limitCrops = 0;

								//attrib
								for (XmlNode a : c.getChildren())
								{
									if (a.getName().equalsIgnoreCase("seed_id"))
									{
										seedId = a.getInt("val");
									}
									else if (a.getName().equalsIgnoreCase("mature_id"))
									{
										matureId = a.getInt("val");
									}
									else if (a.getName().equalsIgnoreCase("reward1"))
									{
										type1R = a.getInt("val");
									}
									else if (a.getName().equalsIgnoreCase("reward2"))
									{
										type2R = a.getInt("val");
									}
									else if (a.getName().equalsIgnoreCase("alternative"))
									{
										isAlt = a.getInt("val") == 1;
									}
									else if (a.getName().equalsIgnoreCase("level"))
									{
										level = a.getInt("val");
									}
									else if (a.getName().equalsIgnoreCase("limit_seed"))
									{
										limitSeeds = a.getInt("val");
									}
									else if (a.getName().equalsIgnoreCase("limit_crops"))
									{
										limitCrops = a.getInt("val");
									}
								}

								SeedData seed = new SeedData(level, cropId, matureId);
								seed.setData(seedId, type1R, type2R, castleId, isAlt, limitSeeds, limitCrops);
								_seeds.put(seed.getId(), seed);
							}
						}
					}
				}
			}
			Log.info(getClass().getSimpleName() + ": Loaded " + _seeds.size() + " Seeds.");
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final L2Manor _instance = new L2Manor();
	}

	public static void main(String[] arg)
	{
		L2Manor.getInstance();
	}
}
