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

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class RecipeController
{

	private Map<Integer, L2RecipeList> lists;
	private static final Map<Integer, RecipeItemMaker> activeMakers = new HashMap<>();
	private static final String RECIPES_FILE = "recipes.xml";

	public static RecipeController getInstance()
	{
		return SingletonHolder.instance;
	}

	private RecipeController()
	{
		this.lists = new HashMap<>();

		try
		{
			loadFromXML();
			Log.info("RecipeController: Loaded " + this.lists.size() + " recipes.");
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed loading recipe list", e);
		}
	}

	public int getRecipesCount()
	{
		return this.lists.size();
	}

	public L2RecipeList getRecipeList(int listId)
	{
		return this.lists.get(listId);
	}

	public L2RecipeList getRecipeByItemId(int itemId)
	{
		for (L2RecipeList find : this.lists.values())
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
		int[] idList = new int[this.lists.size()];
		int i = 0;
		for (L2RecipeList rec : this.lists.values())
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
			maker = this.activeMakers.get(player.getObjectId());
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
		this.activeMakers.remove(player.getObjectId());
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
				(maker = this.activeMakers.get(manufacturer.getObjectId())) != null) // check if busy
		{
			player.sendMessage("Manufacturer is busy, please try later.");
			return;
		}

		maker = new RecipeItemMaker(manufacturer, recipeList, player);
		if (maker.isValid)
		{
			if (Config.ALT_GAME_CREATION)
			{
				this.activeMakers.put(manufacturer.getObjectId(), maker);
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
		if (Config.ALT_GAME_CREATION && (maker = this.activeMakers.get(player.getObjectId())) != null)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1);
			sm.addItemName(recipeList.getItemId());
			sm.addString("You are busy creating");
			player.sendPacket(sm);
			return;
		}

		maker = new RecipeItemMaker(player, recipeList, player);
		if (maker.isValid)
		{
			if (Config.ALT_GAME_CREATION)
			{
				this.activeMakers.put(player.getObjectId(), maker);
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

							this.lists.put(id, recipeList);
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
		protected boolean isValid;
		protected List<TempItem> items = null;
		protected final L2RecipeList recipeList;
		protected final L2PcInstance player; // "crafter"
		protected final L2PcInstance target; // "customer"
		protected final L2Skill skill;
		protected final int skillId;
		protected final int skillLevel;
		protected int creationPasses = 1;
		protected int itemGrab;
		protected int exp = -1;
		protected int sp = -1;
		protected long price;
		protected int totalItems;
		@SuppressWarnings("unused")
		protected int materialsRefPrice;
		protected int delay;

		public RecipeItemMaker(L2PcInstance pPlayer, L2RecipeList pRecipeList, L2PcInstance pTarget)
		{
			this.player = pPlayer;
			this.target = pTarget;
			this.recipeList = pRecipeList;

			this.isValid = false;
			this.skillId = this.recipeList.isDwarvenRecipe() ? L2Skill.SKILL_CREATE_DWARVEN : L2Skill.SKILL_CREATE_COMMON;
			this.skillLevel = this.player.getSkillLevelHash(this.skillId);
			this.skill = this.player.getKnownSkill(this.skillId);

			this.player.isInCraftMode(true);

			if (this.player.isAlikeDead())
			{
				this.player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}

			if (this.target.isAlikeDead())
			{
				this.target.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}

			if (this.target.isProcessingTransaction())
			{
				this.target.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}

			if (this.player.isProcessingTransaction())
			{
				this.player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}

			// validate recipe list
			if (this.recipeList.getRecipes().length == 0)
			{
				this.player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}

			// validate skill level
			if (this.recipeList.getLevel() > skillLevel)
			{
				this.player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}

			// check that customer can afford to pay for creation services
			if (this.player != this.target)
			{
				for (L2ManufactureItem temp : this.player.getCreateList().getList())
				{
					if (temp.getRecipeId() == this.recipeList.getId()) // find recipe for item we want manufactured
					{
						this.price = temp.getCost();
						if (this.target.getAdena() < this.price) // check price
						{
							this.target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
							abort();
							return;
						}
						break;
					}
				}
			}

			// make temporary items
			if ((this.items = listItems(false)) == null)
			{
				abort();
				return;
			}

			// calculate reference price
			for (TempItem i : this.items)
			{
				this.materialsRefPrice += i.getReferencePrice() * i.getQuantity();
				this.totalItems += i.getQuantity();
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

			this.player.isInCraftMode(false);
			this.isValid = true;
		}

		@Override
		public void run()
		{
			if (!Config.IS_CRAFTING_ENABLED)
			{
				this.target.sendMessage("Item creation is currently disabled.");
				abort();
				return;
			}

			if (this.player == null || this.target == null)
			{
				Log.warning("player or target == null (disconnected?), aborting" + this.target + this.player);
				abort();
				return;
			}

			if (!this.player.isOnline() || !this.target.isOnline())
			{
				Log.warning("player or target is not online, aborting " + this.target + this.player);
				abort();
				return;
			}

			if (Config.ALT_GAME_CREATION && activeMakers.get(this.player.getObjectId()) == null)
			{
				if (this.target != this.player)
				{
					this.target.sendMessage("Manufacture aborted");
					this.player.sendMessage("Manufacture aborted");
				}
				else
				{
					this.player.sendMessage("Item creation aborted");
				}

				abort();
				return;
			}

			if (Config.ALT_GAME_CREATION && !this.items.isEmpty())
			{

				if (!calculateStatUse(true, true))
				{
					return; // check stat use
				}
				updateCurMp(); // update craft window mp bar

				grabSomeItems(); // grab (equip) some more items with a nice msg to player

				// if still not empty, schedule another pass
				if (!this.items.isEmpty())
				{
					// divided by RATE_CONSUMABLES_COST to remove craft time increase on higher consumables rates
					this.delay = (int) (Config.ALT_GAME_CREATION_SPEED * this.player.getMReuseRate(this.skill) *
							TimeController.TICKS_PER_SECOND / Config.RATE_CONSUMABLE_COST) *
							TimeController.MILLIS_IN_TICK;

					// This packet won't show crafting (client-side change in the Create Item skill)
					MagicSkillUse msk = new MagicSkillUse(this.player, this.skillId, this.skillLevel, this.delay, 0);
					this.player.broadcastPacket(msk);

					this.player.sendPacket(new SetupGauge(0, this.delay));
					ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + this.delay);
				}
				else
				{
					// for alt mode, sleep delay msec before finishing
					this.player.sendPacket(new SetupGauge(0, this.delay));

					try
					{
						Thread.sleep(this.delay);
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
			if (this.target != this.player && this.price > 0) // customer must pay for services
			{
				// attempt to pay for item
				L2ItemInstance adenatransfer =
						this.target.transferItem("PayManufacture", this.target.getInventory().getAdenaInstance().getObjectId(),
								this.price, this.player.getInventory(), this.player);

				if (adenatransfer == null)
				{
					this.target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
					abort();
					return;
				}
			}

			//Calculate the chance with the LUC stat
			int chance = (int) (this.recipeList.getSuccessRate() + (this.player.getLUC() - 20) * 0.2);
			if ((this.items = listItems(true)) == null) // this line actually takes materials from inventory
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
				if (this.target != this.player)
				{
					SystemMessage msg =
							SystemMessage.getSystemMessage(SystemMessageId.CREATION_OF_S2_FOR_C1_AT_S3_ADENA_FAILED);
					msg.addString(this.target.getName());
					msg.addItemName(this.recipeList.getItemId());
					msg.addItemNumber(this.price);
					this.player.sendPacket(msg);

					msg = SystemMessage.getSystemMessage(SystemMessageId.C1_FAILED_TO_CREATE_S2_FOR_S3_ADENA);
					msg.addString(this.player.getName());
					msg.addItemName(this.recipeList.getItemId());
					msg.addItemNumber(this.price);
					this.target.sendPacket(msg);
				}
				else
				{
					this.target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_MIXING_FAILED));
				}
				updateMakeInfo(false);
			}
			// update load and mana bar of craft window
			updateCurMp();
			updateCurLoad();
			activeMakers.remove(this.player.getObjectId());
			this.player.isInCraftMode(false);
			this.target.sendPacket(new ItemList(this.target, false));
		}

		private void updateMakeInfo(boolean success)
		{
			if (this.target == this.player)
			{
				this.target.sendPacket(new RecipeItemMakeInfo(this.recipeList.getId(), this.target, success));
			}
			else
			{
				this.target.sendPacket(new RecipeShopItemInfo(this.player, this.recipeList.getId()));
			}
		}

		private void updateCurLoad()
		{
			StatusUpdate su = new StatusUpdate(this.target);
			su.addAttribute(StatusUpdate.CUR_LOAD, this.target.getCurrentLoad());
			this.target.sendPacket(su);
		}

		private void updateCurMp()
		{
			StatusUpdate su = new StatusUpdate(this.target);
			su.addAttribute(StatusUpdate.CUR_MP, (int) this.target.getCurrentMp());
			this.target.sendPacket(su);
		}

		private void grabSomeItems()
		{
			int grabItems = this.itemGrab;
			while (grabItems > 0 && !this.items.isEmpty())
			{
				TempItem item = this.items.get(0);

				int count = item.getQuantity();
				if (count >= grabItems)
				{
					count = grabItems;
				}

				item.setQuantity(item.getQuantity() - count);
				if (item.getQuantity() <= 0)
				{
					this.items.remove(0);
				}
				else
				{
					this.items.set(0, item);
				}

				grabItems -= count;

				if (this.target == this.player)
				{
					SystemMessage sm =
							SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED); // you equipped ...
					sm.addItemNumber(count);
					sm.addItemName(item.getItemId());
					this.player.sendPacket(sm);
				}
				else
				{
					this.target.sendMessage(
							"Manufacturer " + this.player.getName() + " used " + count + " " + item.getItemName());
				}
			}
		}

		// AltStatChange parameters make their effect here
		private void calculateAltStatChange()
		{
			this.itemGrab = this.skillLevel;

			for (L2RecipeStatInstance altStatChange : this.recipeList.getAltStatChange())
			{
				if (altStatChange.getType() == L2RecipeStatInstance.StatType.XP)
				{
					this.exp = altStatChange.getValue();
				}
				else if (altStatChange.getType() == L2RecipeStatInstance.StatType.SP)
				{
					this.sp = altStatChange.getValue();
				}
				else if (altStatChange.getType() == L2RecipeStatInstance.StatType.GIM)
				{
					this.itemGrab *= altStatChange.getValue();
				}
			}
			// determine number of creation passes needed
			this.creationPasses = this.totalItems / this.itemGrab + (this.totalItems % this.itemGrab != 0 ? 1 : 0);
			if (this.creationPasses < 1)
			{
				this.creationPasses = 1;
			}
		}

		// StatUse
		private boolean calculateStatUse(boolean isWait, boolean isReduce)
		{
			boolean ret = true;
			for (L2RecipeStatInstance statUse : this.recipeList.getStatUse())
			{
				double modifiedValue = statUse.getValue() / this.creationPasses;
				if (statUse.getType() == L2RecipeStatInstance.StatType.HP)
				{
					// we do not want to kill the player, so its CurrentHP must be greater than the reduce value
					if (this.player.getCurrentHp() <= modifiedValue)
					{
						// rest (wait for HP)
						if (Config.ALT_GAME_CREATION && isWait)
						{
							this.player.sendPacket(new SetupGauge(0, this.delay));
							ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + this.delay);
						}
						else
						// no rest - report no hp
						{
							this.target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_HP));
							abort();
						}
						ret = false;
					}
					else if (isReduce)
					{
						this.player.reduceCurrentHp(modifiedValue, this.player, this.skill);
					}
				}
				else if (statUse.getType() == L2RecipeStatInstance.StatType.MP)
				{
					if (this.player.getCurrentMp() < modifiedValue)
					{
						// rest (wait for MP)
						if (Config.ALT_GAME_CREATION && isWait)
						{
							this.player.sendPacket(new SetupGauge(0, this.delay));
							ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + this.delay);
						}
						else
						// no rest - report no mana
						{
							this.target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP));
							abort();
						}
						ret = false;
					}
					else if (isReduce)
					{
						this.player.reduceCurrentMp(modifiedValue);
					}
				}
				else
				{
					// there is an unknown StatUse value
					this.target.sendMessage("Recipe error!!!, please tell this to your GM.");
					ret = false;
					abort();
				}
			}
			return ret;
		}

		private List<TempItem> listItems(boolean remove)
		{
			L2RecipeInstance[] recipes = this.recipeList.getRecipes();
			Inventory inv = this.target.getInventory();
			List<TempItem> materials = new ArrayList<>();
			SystemMessage sm;

			for (L2RecipeInstance recipe : recipes)
			{
				int quantity = this.recipeList.isConsumable() ? (int) (recipe.getQuantity() * Config.RATE_CONSUMABLE_COST) :
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
						this.target.sendPacket(sm);

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
					inv.destroyItemByItemId("Manufacture", tmp.getItemId(), tmp.getQuantity(), this.target, this.player);

					if (tmp.getQuantity() > 1)
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
						sm.addItemName(tmp.getItemId());
						sm.addItemNumber(tmp.getQuantity());
						this.target.sendPacket(sm);
					}
					else
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(tmp.getItemId());
						this.target.sendPacket(sm);
					}
				}
			}
			return materials;
		}

		private void abort()
		{
			updateMakeInfo(false);
			this.player.isInCraftMode(false);
			activeMakers.remove(this.player.getObjectId());
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
			private int itemId;
			private int quantity;
			private int referencePrice;
			private String itemName;

			/**
			 * @param item
			 * @param quantity of that item
			 */
			public TempItem(L2ItemInstance item, int quantity)
			{
				super();
				this.itemId = item.getItemId();
				this.quantity = quantity;
				this.itemName = item.getItem().getName();
				this.referencePrice = item.getReferencePrice();
			}

			/**
			 * @return Returns the quantity.
			 */
			public int getQuantity()
			{
				return this.quantity;
			}

			/**
			 * @param quantity The quantity to set.
			 */
			public void setQuantity(int quantity)
			{
				this.quantity = quantity;
			}

			public int getReferencePrice()
			{
				return this.referencePrice;
			}

			/**
			 * @return Returns the itemId.
			 */
			public int getItemId()
			{
				return this.itemId;
			}

			/**
			 * @return Returns the itemName.
			 */
			public String getItemName()
			{
				return this.itemName;
			}
		}

		private void rewardPlayer()
		{
			int rareProdId = this.recipeList.getRareItemId();
			int itemId = this.recipeList.getItemId();
			int itemCount = this.recipeList.getCount();
			L2Item template = ItemTable.getInstance().getTemplate(itemId);

			// check that the current recipe has a rare production or not
			if (rareProdId != -1 && (rareProdId == itemId || Config.CRAFT_MASTERWORK))
			{
				if (Rnd.get(100) < this.recipeList.getRarity())
				{
					itemId = rareProdId;
					itemCount = this.recipeList.getRareCount();
				}
			}

			this.target.getInventory().addItem("Manufacture", itemId, itemCount, this.target, this.player);

			// inform customer of earned item
			SystemMessage sm = null;
			if (this.target != this.player)
			{
				// inform manufacturer of earned profit
				if (itemCount == 1)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_CREATED_FOR_C1_FOR_S3_ADENA);
					sm.addString(this.target.getName());
					sm.addItemName(itemId);
					sm.addItemNumber(this.price);
					this.player.sendPacket(sm);

					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CREATED_S2_FOR_S3_ADENA);
					sm.addString(this.player.getName());
					sm.addItemName(itemId);
					sm.addItemNumber(this.price);
					this.target.sendPacket(sm);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S3_S_CREATED_FOR_C1_FOR_S4_ADENA);
					sm.addString(this.target.getName());
					sm.addNumber(itemCount);
					sm.addItemName(itemId);
					sm.addItemNumber(this.price);
					this.player.sendPacket(sm);

					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CREATED_S2_S3_S_FOR_S4_ADENA);
					sm.addString(this.player.getName());
					sm.addNumber(itemCount);
					sm.addItemName(itemId);
					sm.addItemNumber(this.price);
					this.target.sendPacket(sm);
				}
			}

			if (itemCount > 1)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				sm.addItemName(itemId);
				sm.addItemNumber(itemCount);
				this.target.sendPacket(sm);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				sm.addItemName(itemId);
				this.target.sendPacket(sm);
			}

			if (Config.ALT_GAME_CREATION)
			{
				int recipeLevel = this.recipeList.getLevel();
				if (this.exp < 0)
				{
					this.exp = template.getReferencePrice() * itemCount;
					this.exp /= recipeLevel;
				}
				if (this.sp < 0)
				{
					this.sp = this.exp / 10;
				}
				if (itemId == rareProdId)
				{
					this.exp *= Config.ALT_GAME_CREATION_RARE_XPSP_RATE;
					this.sp *= Config.ALT_GAME_CREATION_RARE_XPSP_RATE;
				}

				// one variation

				// exp -= materialsRefPrice;   // mat. ref. price is not accurate so other method is better

				if (this.exp < 0)
				{
					this.exp = 0;
				}
				if (this.sp < 0)
				{
					this.sp = 0;
				}

				for (int i = this.skillLevel; i > recipeLevel; i--)
				{
					this.exp /= 4;
					this.sp /= 4;
				}

				// Added multiplication of Creation speed with XP/SP gain
				// slower crafting -> more XP,  faster crafting -> less XP
				// you can use ALT_GAME_CREATION_XP_RATE/SP to
				// modify XP/SP gained (default = 1)

				this.player.addExpAndSp((int) this.player.calcStat(Stats.EXP_RATE,
						this.exp * Config.ALT_GAME_CREATION_XP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null),
						(int) this.player.calcStat(Stats.SP_RATE,
								this.sp * Config.ALT_GAME_CREATION_SP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null));
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
		protected static final RecipeController instance = new RecipeController();
	}
}
