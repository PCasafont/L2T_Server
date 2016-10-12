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

package l2server.gameserver;

import l2server.Config;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class RecipeController
{

	private Map<Integer, L2RecipeList> _lists;
	private static final Map<Integer, RecipeItemMaker> _activeMakers = new HashMap<>();
	private static final String RECIPES_FILE = "recipes.xml";

	public static RecipeController getInstance()
	{
		return SingletonHolder._instance;
	}

	private RecipeController()
	{
		_lists = new HashMap<>();

		try
		{
			loadFromXML();
			Log.info("RecipeController: Loaded " + _lists.size() + " recipes.");
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed loading recipe list", e);
		}
	}

	public int getRecipesCount()
	{
		return _lists.size();
	}

	public L2RecipeList getRecipeList(int listId)
	{
		return _lists.get(listId);
	}

	public L2RecipeList getRecipeByItemId(int itemId)
	{
		for (L2RecipeList find : _lists.values())
		{
			if (find.getRecipeId() == itemId)
			{
				return find;
			}
		}
		return null;
	}

	public int[] getAllItemIds()
	{
		int[] idList = new int[_lists.size()];
		int i = 0;
		for (L2RecipeList rec : _lists.values())
		{
			idList[i++] = rec.getRecipeId();
		}
		return idList;
	}

	public synchronized void requestBookOpen(L2PcInstance player, boolean isDwarvenCraft)
	{
		RecipeItemMaker maker = null;
		if (Config.ALT_GAME_CREATION)
		{
			maker = _activeMakers.get(player.getObjectId());
		}

		if (maker == null)
		{
			RecipeBookItemList response = new RecipeBookItemList(isDwarvenCraft, player.getMaxMp());
			response.addRecipes(isDwarvenCraft ? player.getDwarvenRecipeBook() : player.getCommonRecipeBook());
			player.sendPacket(response);
			return;
		}

		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING));
	}

	public synchronized void requestMakeItemAbort(L2PcInstance player)
	{
		_activeMakers.remove(player.getObjectId());
	}

	public synchronized void requestManufactureItem(L2PcInstance manufacturer, int recipeListId, L2PcInstance player)
	{
		L2RecipeList recipeList = getValidRecipeList(player, recipeListId);

		if (recipeList == null)
		{
			return;
		}

		List<L2RecipeList> dwarfRecipes = Arrays.asList(manufacturer.getDwarvenRecipeBook());
		List<L2RecipeList> commonRecipes = Arrays.asList(manufacturer.getCommonRecipeBook());

		if (!dwarfRecipes.contains(recipeList) && !commonRecipes.contains(recipeList))
		{
			Util.handleIllegalPlayerAction(player,
					"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
							" sent a false recipe id.", Config.DEFAULT_PUNISH);
			return;
		}

		RecipeItemMaker maker;

		if (Config.ALT_GAME_CREATION &&
				(maker = _activeMakers.get(manufacturer.getObjectId())) != null) // check if busy
		{
			player.sendMessage("Manufacturer is busy, please try later.");
			return;
		}

		maker = new RecipeItemMaker(manufacturer, recipeList, player);
		if (maker._isValid)
		{
			if (Config.ALT_GAME_CREATION)
			{
				_activeMakers.put(manufacturer.getObjectId(), maker);
				ThreadPoolManager.getInstance().scheduleGeneral(maker, 100);
			}
			else
			{
				maker.run();
			}
		}
	}

	public synchronized void requestMakeItem(L2PcInstance player, int recipeListId)
	{
		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player) || player.isInDuel())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT));
			return;
		}

		L2RecipeList recipeList = getValidRecipeList(player, recipeListId);

		if (recipeList == null)
		{
			return;
		}

		List<L2RecipeList> dwarfRecipes = Arrays.asList(player.getDwarvenRecipeBook());
		List<L2RecipeList> commonRecipes = Arrays.asList(player.getCommonRecipeBook());

		if (!dwarfRecipes.contains(recipeList) && !commonRecipes.contains(recipeList))
		{
			Util.handleIllegalPlayerAction(player,
					"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
							" sent a false recipe id.", Config.DEFAULT_PUNISH);
			return;
		}

		RecipeItemMaker maker;

		// check if already busy (possible in alt mode only)
		if (Config.ALT_GAME_CREATION && (maker = _activeMakers.get(player.getObjectId())) != null)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1);
			sm.addItemName(recipeList.getItemId());
			sm.addString("You are busy creating");
			player.sendPacket(sm);
			return;
		}

		maker = new RecipeItemMaker(player, recipeList, player);
		if (maker._isValid)
		{
			if (Config.ALT_GAME_CREATION)
			{
				_activeMakers.put(player.getObjectId(), maker);
				ThreadPoolManager.getInstance().scheduleGeneral(maker, 100);
			}
			else
			{
				maker.run();
			}
		}
	}

	private void loadFromXML()
	{
		File file = new File(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "" + RECIPES_FILE);
		File customFile = new File(Config.DATAPACK_ROOT + "/data_" + Config.SERVER_NAME + "/recipes.xml");
		if (customFile.exists())
		{
			file = customFile;
		}

		if (file.exists())
		{
			XmlDocument doc = new XmlDocument(file);
			List<L2RecipeInstance> recipePartList = new ArrayList<>();
			List<L2RecipeStatInstance> recipeStatUseList = new ArrayList<>();
			List<L2RecipeStatInstance> recipeAltStatChangeList = new ArrayList<>();

			for (XmlNode n : doc.getChildren())
			{
				if (n.getName().equalsIgnoreCase("list"))
				{
					recipesFile:
					for (XmlNode d : n.getChildren())
					{
						if (d.getName().equalsIgnoreCase("item"))
						{
							recipePartList.clear();
							recipeStatUseList.clear();
							recipeAltStatChangeList.clear();
							int id = -1;
							boolean haveRare = false;
							StatsSet set = new StatsSet();

							id = d.getInt("id");
							set.set("id", id);
							set.set("recipeId", d.getInt("recipeId"));
							set.set("recipeName", d.getString("name"));
							set.set("craftLevel", d.getInt("craftLevel"));
							set.set("isDwarvenRecipe", d.getString("type").equalsIgnoreCase("dwarven"));
							set.set("successRate", d.getInt("successRate"));

							for (XmlNode c : d.getChildren())
							{
								if (c.getName().equalsIgnoreCase("statUse"))
								{
									String statName = c.getString("name");
									int value = c.getInt("value");
									try
									{
										recipeStatUseList.add(new L2RecipeStatInstance(statName, value));
									}
									catch (Exception e)
									{
										Log.severe(
												"Error in StatUse parameter for recipe item id: " + id + ", skipping");
										continue recipesFile;
									}
								}
								else if (c.getName().equalsIgnoreCase("altStatChange"))
								{
									String statName = c.getString("name");
									int value = c.getInt("value");
									try
									{
										recipeAltStatChangeList.add(new L2RecipeStatInstance(statName, value));
									}
									catch (Exception e)
									{
										Log.severe("Error in AltStatChange parameter for recipe item id: " + id +
												", skipping");
										continue recipesFile;
									}
								}
								else if (c.getName().equalsIgnoreCase("ingredient"))
								{
									int ingId = c.getInt("id");
									int ingCount = c.getInt("count");
									recipePartList.add(new L2RecipeInstance(ingId, ingCount));
								}
								else if (c.getName().equalsIgnoreCase("production"))
								{
									set.set("itemId", c.getInt("id"));
									set.set("count", c.getInt("count"));
								}
								else if (c.getName().equalsIgnoreCase("productionRare"))
								{
									set.set("rareItemId", c.getInt("id"));
									set.set("rareCount", c.getInt("count"));
									set.set("rarity", c.getInt("rarity"));
									haveRare = true;
								}
							}

							L2RecipeList recipeList = new L2RecipeList(set, haveRare);
							for (L2RecipeInstance recipePart : recipePartList)
							{
								recipeList.addRecipe(recipePart);
							}
							for (L2RecipeStatInstance recipeStatUse : recipeStatUseList)
							{
								recipeList.addStatUse(recipeStatUse);
							}
							for (L2RecipeStatInstance recipeAltStatChange : recipeAltStatChangeList)
							{
								recipeList.addAltStatChange(recipeAltStatChange);
							}

							_lists.put(id, recipeList);
						}
					}
				}
			}
		}
		else
		{
			Log.severe("Recipes file (" + file.getAbsolutePath() + ") doesnt exists.");
		}
	}

	private static class RecipeItemMaker implements Runnable
	{
		protected boolean _isValid;
		protected List<TempItem> _items = null;
		protected final L2RecipeList _recipeList;
		protected final L2PcInstance _player; // "crafter"
		protected final L2PcInstance _target; // "customer"
		protected final L2Skill _skill;
		protected final int _skillId;
		protected final int _skillLevel;
		protected int _creationPasses = 1;
		protected int _itemGrab;
		protected int _exp = -1;
		protected int _sp = -1;
		protected long _price;
		protected int _totalItems;
		@SuppressWarnings("unused")
		protected int _materialsRefPrice;
		protected int _delay;

		public RecipeItemMaker(L2PcInstance pPlayer, L2RecipeList pRecipeList, L2PcInstance pTarget)
		{
			_player = pPlayer;
			_target = pTarget;
			_recipeList = pRecipeList;

			_isValid = false;
			_skillId = _recipeList.isDwarvenRecipe() ? L2Skill.SKILL_CREATE_DWARVEN : L2Skill.SKILL_CREATE_COMMON;
			_skillLevel = _player.getSkillLevelHash(_skillId);
			_skill = _player.getKnownSkill(_skillId);

			_player.isInCraftMode(true);

			if (_player.isAlikeDead())
			{
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}

			if (_target.isAlikeDead())
			{
				_target.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}

			if (_target.isProcessingTransaction())
			{
				_target.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}

			if (_player.isProcessingTransaction())
			{
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}

			// validate recipe list
			if (_recipeList.getRecipes().length == 0)
			{
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}

			// validate skill level
			if (_recipeList.getLevel() > _skillLevel)
			{
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}

			// check that customer can afford to pay for creation services
			if (_player != _target)
			{
				for (L2ManufactureItem temp : _player.getCreateList().getList())
				{
					if (temp.getRecipeId() == _recipeList.getId()) // find recipe for item we want manufactured
					{
						_price = temp.getCost();
						if (_target.getAdena() < _price) // check price
						{
							_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
							abort();
							return;
						}
						break;
					}
				}
			}

			// make temporary items
			if ((_items = listItems(false)) == null)
			{
				abort();
				return;
			}

			// calculate reference price
			for (TempItem i : _items)
			{
				_materialsRefPrice += i.getReferencePrice() * i.getQuantity();
				_totalItems += i.getQuantity();
			}

			// initial statUse checks
			if (!calculateStatUse(false, false))
			{
				abort();
				return;
			}

			// initial AltStatChange checks
			if (Config.ALT_GAME_CREATION)
			{
				calculateAltStatChange();
			}

			updateMakeInfo(true);
			updateCurMp();
			updateCurLoad();

			_player.isInCraftMode(false);
			_isValid = true;
		}

		@Override
		public void run()
		{
			if (!Config.IS_CRAFTING_ENABLED)
			{
				_target.sendMessage("Item creation is currently disabled.");
				abort();
				return;
			}

			if (_player == null || _target == null)
			{
				Log.warning("player or target == null (disconnected?), aborting" + _target + _player);
				abort();
				return;
			}

			if (!_player.isOnline() || !_target.isOnline())
			{
				Log.warning("player or target is not online, aborting " + _target + _player);
				abort();
				return;
			}

			if (Config.ALT_GAME_CREATION && _activeMakers.get(_player.getObjectId()) == null)
			{
				if (_target != _player)
				{
					_target.sendMessage("Manufacture aborted");
					_player.sendMessage("Manufacture aborted");
				}
				else
				{
					_player.sendMessage("Item creation aborted");
				}

				abort();
				return;
			}

			if (Config.ALT_GAME_CREATION && !_items.isEmpty())
			{

				if (!calculateStatUse(true, true))
				{
					return; // check stat use
				}
				updateCurMp(); // update craft window mp bar

				grabSomeItems(); // grab (equip) some more items with a nice msg to player

				// if still not empty, schedule another pass
				if (!_items.isEmpty())
				{
					// divided by RATE_CONSUMABLES_COST to remove craft time increase on higher consumables rates
					_delay = (int) (Config.ALT_GAME_CREATION_SPEED * _player.getMReuseRate(_skill) *
							TimeController.TICKS_PER_SECOND / Config.RATE_CONSUMABLE_COST) *
							TimeController.MILLIS_IN_TICK;

					// This packet won't show crafting (client-side change in the Create Item skill)
					MagicSkillUse msk = new MagicSkillUse(_player, _skillId, _skillLevel, _delay, 0);
					_player.broadcastPacket(msk);

					_player.sendPacket(new SetupGauge(0, _delay));
					ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + _delay);
				}
				else
				{
					// for alt mode, sleep delay msec before finishing
					_player.sendPacket(new SetupGauge(0, _delay));

					try
					{
						Thread.sleep(_delay);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					finally
					{
						finishCrafting();
					}
				}
			} // for old craft mode just finish
			else
			{
				finishCrafting();
			}
		}

		private void finishCrafting()
		{
			if (!Config.ALT_GAME_CREATION)
			{
				calculateStatUse(false, true);
			}

			// first take adena for manufacture
			if (_target != _player && _price > 0) // customer must pay for services
			{
				// attempt to pay for item
				L2ItemInstance adenatransfer =
						_target.transferItem("PayManufacture", _target.getInventory().getAdenaInstance().getObjectId(),
								_price, _player.getInventory(), _player);

				if (adenatransfer == null)
				{
					_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
					abort();
					return;
				}
			}

			//Calculate the chance with the LUC stat
			int chance = (int) (_recipeList.getSuccessRate() + (_player.getLUC() - 20) * 0.2);
			if ((_items = listItems(true)) == null) // this line actually takes materials from inventory
			{ // handle possible cheaters here
				// (they click craft then try to get rid of items in order to get free craft)
			}
			else if (Rnd.get(100) < chance)
			{
				rewardPlayer(); // and immediately puts created item in its place
				updateMakeInfo(true);
			}
			else
			{
				if (_target != _player)
				{
					SystemMessage msg =
							SystemMessage.getSystemMessage(SystemMessageId.CREATION_OF_S2_FOR_C1_AT_S3_ADENA_FAILED);
					msg.addString(_target.getName());
					msg.addItemName(_recipeList.getItemId());
					msg.addItemNumber(_price);
					_player.sendPacket(msg);

					msg = SystemMessage.getSystemMessage(SystemMessageId.C1_FAILED_TO_CREATE_S2_FOR_S3_ADENA);
					msg.addString(_player.getName());
					msg.addItemName(_recipeList.getItemId());
					msg.addItemNumber(_price);
					_target.sendPacket(msg);
				}
				else
				{
					_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_MIXING_FAILED));
				}
				updateMakeInfo(false);
			}
			// update load and mana bar of craft window
			updateCurMp();
			updateCurLoad();
			_activeMakers.remove(_player.getObjectId());
			_player.isInCraftMode(false);
			_target.sendPacket(new ItemList(_target, false));
		}

		private void updateMakeInfo(boolean success)
		{
			if (_target == _player)
			{
				_target.sendPacket(new RecipeItemMakeInfo(_recipeList.getId(), _target, success));
			}
			else
			{
				_target.sendPacket(new RecipeShopItemInfo(_player, _recipeList.getId()));
			}
		}

		private void updateCurLoad()
		{
			StatusUpdate su = new StatusUpdate(_target);
			su.addAttribute(StatusUpdate.CUR_LOAD, _target.getCurrentLoad());
			_target.sendPacket(su);
		}

		private void updateCurMp()
		{
			StatusUpdate su = new StatusUpdate(_target);
			su.addAttribute(StatusUpdate.CUR_MP, (int) _target.getCurrentMp());
			_target.sendPacket(su);
		}

		private void grabSomeItems()
		{
			int grabItems = _itemGrab;
			while (grabItems > 0 && !_items.isEmpty())
			{
				TempItem item = _items.get(0);

				int count = item.getQuantity();
				if (count >= grabItems)
				{
					count = grabItems;
				}

				item.setQuantity(item.getQuantity() - count);
				if (item.getQuantity() <= 0)
				{
					_items.remove(0);
				}
				else
				{
					_items.set(0, item);
				}

				grabItems -= count;

				if (_target == _player)
				{
					SystemMessage sm =
							SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED); // you equipped ...
					sm.addItemNumber(count);
					sm.addItemName(item.getItemId());
					_player.sendPacket(sm);
				}
				else
				{
					_target.sendMessage(
							"Manufacturer " + _player.getName() + " used " + count + " " + item.getItemName());
				}
			}
		}

		// AltStatChange parameters make their effect here
		private void calculateAltStatChange()
		{
			_itemGrab = _skillLevel;

			for (L2RecipeStatInstance altStatChange : _recipeList.getAltStatChange())
			{
				if (altStatChange.getType() == L2RecipeStatInstance.StatType.XP)
				{
					_exp = altStatChange.getValue();
				}
				else if (altStatChange.getType() == L2RecipeStatInstance.StatType.SP)
				{
					_sp = altStatChange.getValue();
				}
				else if (altStatChange.getType() == L2RecipeStatInstance.StatType.GIM)
				{
					_itemGrab *= altStatChange.getValue();
				}
			}
			// determine number of creation passes needed
			_creationPasses = _totalItems / _itemGrab + (_totalItems % _itemGrab != 0 ? 1 : 0);
			if (_creationPasses < 1)
			{
				_creationPasses = 1;
			}
		}

		// StatUse
		private boolean calculateStatUse(boolean isWait, boolean isReduce)
		{
			boolean ret = true;
			for (L2RecipeStatInstance statUse : _recipeList.getStatUse())
			{
				double modifiedValue = statUse.getValue() / _creationPasses;
				if (statUse.getType() == L2RecipeStatInstance.StatType.HP)
				{
					// we do not want to kill the player, so its CurrentHP must be greater than the reduce value
					if (_player.getCurrentHp() <= modifiedValue)
					{
						// rest (wait for HP)
						if (Config.ALT_GAME_CREATION && isWait)
						{
							_player.sendPacket(new SetupGauge(0, _delay));
							ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + _delay);
						}
						else
						// no rest - report no hp
						{
							_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_HP));
							abort();
						}
						ret = false;
					}
					else if (isReduce)
					{
						_player.reduceCurrentHp(modifiedValue, _player, _skill);
					}
				}
				else if (statUse.getType() == L2RecipeStatInstance.StatType.MP)
				{
					if (_player.getCurrentMp() < modifiedValue)
					{
						// rest (wait for MP)
						if (Config.ALT_GAME_CREATION && isWait)
						{
							_player.sendPacket(new SetupGauge(0, _delay));
							ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + _delay);
						}
						else
						// no rest - report no mana
						{
							_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP));
							abort();
						}
						ret = false;
					}
					else if (isReduce)
					{
						_player.reduceCurrentMp(modifiedValue);
					}
				}
				else
				{
					// there is an unknown StatUse value
					_target.sendMessage("Recipe error!!!, please tell this to your GM.");
					ret = false;
					abort();
				}
			}
			return ret;
		}

		private List<TempItem> listItems(boolean remove)
		{
			L2RecipeInstance[] recipes = _recipeList.getRecipes();
			Inventory inv = _target.getInventory();
			List<TempItem> materials = new ArrayList<>();
			SystemMessage sm;

			for (L2RecipeInstance recipe : recipes)
			{
				int quantity = _recipeList.isConsumable() ? (int) (recipe.getQuantity() * Config.RATE_CONSUMABLE_COST) :
						recipe.getQuantity();

				if (quantity > 0)
				{
					L2ItemInstance item = inv.getItemByItemId(recipe.getItemId());
					long itemQuantityAmount = item == null ? 0 : item.getCount();

					// check materials
					if (itemQuantityAmount < quantity)
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.MISSING_S2_S1_TO_CREATE);
						sm.addItemName(recipe.getItemId());
						sm.addItemNumber(quantity - itemQuantityAmount);
						_target.sendPacket(sm);

						abort();
						return null;
					}

					// make new temporary object, just for counting purposes

					TempItem temp = new TempItem(item, quantity);
					materials.add(temp);
				}
			}

			if (remove)
			{
				for (TempItem tmp : materials)
				{
					inv.destroyItemByItemId("Manufacture", tmp.getItemId(), tmp.getQuantity(), _target, _player);

					if (tmp.getQuantity() > 1)
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
						sm.addItemName(tmp.getItemId());
						sm.addItemNumber(tmp.getQuantity());
						_target.sendPacket(sm);
					}
					else
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(tmp.getItemId());
						_target.sendPacket(sm);
					}
				}
			}
			return materials;
		}

		private void abort()
		{
			updateMakeInfo(false);
			_player.isInCraftMode(false);
			_activeMakers.remove(_player.getObjectId());
		}

		/**
		 * FIXME: This class should be in some other file, but I don't know where
		 * <p>
		 * Class explanation:
		 * For item counting or checking purposes. When you don't want to modify inventory
		 * class contains itemId, quantity, ownerId, referencePrice, but not objectId
		 */
		private class TempItem
		{ // no object id stored, this will be only "list" of items with it's owner
			private int _itemId;
			private int _quantity;
			private int _referencePrice;
			private String _itemName;

			/**
			 * @param item
			 * @param quantity of that item
			 */
			public TempItem(L2ItemInstance item, int quantity)
			{
				super();
				_itemId = item.getItemId();
				_quantity = quantity;
				_itemName = item.getItem().getName();
				_referencePrice = item.getReferencePrice();
			}

			/**
			 * @return Returns the quantity.
			 */
			public int getQuantity()
			{
				return _quantity;
			}

			/**
			 * @param quantity The quantity to set.
			 */
			public void setQuantity(int quantity)
			{
				_quantity = quantity;
			}

			public int getReferencePrice()
			{
				return _referencePrice;
			}

			/**
			 * @return Returns the itemId.
			 */
			public int getItemId()
			{
				return _itemId;
			}

			/**
			 * @return Returns the itemName.
			 */
			public String getItemName()
			{
				return _itemName;
			}
		}

		private void rewardPlayer()
		{
			int rareProdId = _recipeList.getRareItemId();
			int itemId = _recipeList.getItemId();
			int itemCount = _recipeList.getCount();
			L2Item template = ItemTable.getInstance().getTemplate(itemId);

			// check that the current recipe has a rare production or not
			if (rareProdId != -1 && (rareProdId == itemId || Config.CRAFT_MASTERWORK))
			{
				if (Rnd.get(100) < _recipeList.getRarity())
				{
					itemId = rareProdId;
					itemCount = _recipeList.getRareCount();
				}
			}

			_target.getInventory().addItem("Manufacture", itemId, itemCount, _target, _player);

			// inform customer of earned item
			SystemMessage sm = null;
			if (_target != _player)
			{
				// inform manufacturer of earned profit
				if (itemCount == 1)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_CREATED_FOR_C1_FOR_S3_ADENA);
					sm.addString(_target.getName());
					sm.addItemName(itemId);
					sm.addItemNumber(_price);
					_player.sendPacket(sm);

					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CREATED_S2_FOR_S3_ADENA);
					sm.addString(_player.getName());
					sm.addItemName(itemId);
					sm.addItemNumber(_price);
					_target.sendPacket(sm);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S3_S_CREATED_FOR_C1_FOR_S4_ADENA);
					sm.addString(_target.getName());
					sm.addNumber(itemCount);
					sm.addItemName(itemId);
					sm.addItemNumber(_price);
					_player.sendPacket(sm);

					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CREATED_S2_S3_S_FOR_S4_ADENA);
					sm.addString(_player.getName());
					sm.addNumber(itemCount);
					sm.addItemName(itemId);
					sm.addItemNumber(_price);
					_target.sendPacket(sm);
				}
			}

			if (itemCount > 1)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				sm.addItemName(itemId);
				sm.addItemNumber(itemCount);
				_target.sendPacket(sm);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				sm.addItemName(itemId);
				_target.sendPacket(sm);
			}

			if (Config.ALT_GAME_CREATION)
			{
				int recipeLevel = _recipeList.getLevel();
				if (_exp < 0)
				{
					_exp = template.getReferencePrice() * itemCount;
					_exp /= recipeLevel;
				}
				if (_sp < 0)
				{
					_sp = _exp / 10;
				}
				if (itemId == rareProdId)
				{
					_exp *= Config.ALT_GAME_CREATION_RARE_XPSP_RATE;
					_sp *= Config.ALT_GAME_CREATION_RARE_XPSP_RATE;
				}

				// one variation

				// exp -= materialsRefPrice;   // mat. ref. price is not accurate so other method is better

				if (_exp < 0)
				{
					_exp = 0;
				}
				if (_sp < 0)
				{
					_sp = 0;
				}

				for (int i = _skillLevel; i > recipeLevel; i--)
				{
					_exp /= 4;
					_sp /= 4;
				}

				// Added multiplication of Creation speed with XP/SP gain
				// slower crafting -> more XP,  faster crafting -> less XP
				// you can use ALT_GAME_CREATION_XP_RATE/SP to
				// modify XP/SP gained (default = 1)

				_player.addExpAndSp((int) _player.calcStat(Stats.EXP_RATE,
						_exp * Config.ALT_GAME_CREATION_XP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null),
						(int) _player.calcStat(Stats.SP_RATE,
								_sp * Config.ALT_GAME_CREATION_SP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null));
			}
			updateMakeInfo(true); // success
		}
	}

	private L2RecipeList getValidRecipeList(L2PcInstance player, int id)
	{
		L2RecipeList recipeList = getRecipeList(id);

		if (recipeList == null || recipeList.getRecipes().length == 0)
		{
			player.sendMessage("No recipe for: " + id);
			player.isInCraftMode(false);
			return null;
		}
		return recipeList;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final RecipeController _instance = new RecipeController();
	}
}
