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

package l2server.gameserver.network.clientpackets;

import l2server.Config;
import l2server.gameserver.datatables.EnchantItemTable;
import l2server.gameserver.datatables.EnchantMultiSellTable;
import l2server.gameserver.datatables.EnchantMultiSellTable.EnchantMultiSellCategory;
import l2server.gameserver.datatables.EnchantMultiSellTable.EnchantMultiSellEntry;
import l2server.gameserver.datatables.MerchantPriceConfigTable;
import l2server.gameserver.datatables.MerchantPriceConfigTable.MerchantPriceConfig;
import l2server.gameserver.datatables.MultiSell;
import l2server.gameserver.handler.IVoicedCommandHandler;
import l2server.gameserver.handler.VoicedCommandHandler;
import l2server.gameserver.model.*;
import l2server.gameserver.model.TradeList.TradeItem;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.model.multisell.Ingredient;
import l2server.gameserver.model.multisell.MultiSellEntry;
import l2server.gameserver.model.multisell.PreparedListContainer;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.DirectEnchantMultiSellList.DirectEnchantMultiSellConfig;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.item.L2Item;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.Map.Entry;

/**
 * The Class MultiSellChoose.
 */
public class MultiSellChoose extends L2GameClientPacket
{

	private int _listId;
	private int _entryId;
	private long _amount;

	/* (non-Javadoc)
	 * @see l2server.gameserver.network.clientpackets.L2GameClientPacket#readImpl()
	 */
	@Override
	protected void readImpl()
	{
		_listId = readD();
		_entryId = readD();
		_amount = readQ();
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.network.clientpackets.L2GameClientPacket#runImpl()
	 */
	@Override
	public void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		if (!getClient().getFloodProtectors().getMultiSell().tryPerformAction("multisell choose"))
		{
			player.setMultiSell(null);
			return;
		}

		if (_amount < 1 || _amount > 5000)
		{
			player.setMultiSell(null);
			return;
		}

		L2PcInstance storePlayer = L2World.getInstance().getPlayer(_listId);
		if (storePlayer != null)
		{
			chooseFromPlayer(player, storePlayer);
			return;
		}

		if (_listId == EnchantMultiSellList.ShopId)
		{
			enchantItem(player);
			return;
		}

		DirectEnchantMultiSellConfig config = DirectEnchantMultiSellConfig.getConfig(_listId);
		if (config != null)
		{
			transformItem(player, config);
			return;
		}

		PreparedListContainer list = player.getMultiSell();
		if (list == null || list.getListId() != _listId)
		{
			player.setMultiSell(null);
			return;
		}

		L2Npc target = player.getLastFolkNPC();
		if (!player.isGM() &&
				(target == null || !list.checkNpcObjectId(target.getObjectId()) || !target.canInteract(player)))
		{
			player.setMultiSell(null);
			return;
		}

		for (MultiSellEntry entry : list.getEntries())
		{
			if (entry.getEntryId() == _entryId)
			{
				if (!entry.isStackable() && _amount > 1)
				{
					Log.severe("Character: " + player.getName() +
							" is trying to set amount > 1 on non-stackable multisell, id:" + _listId + ":" + _entryId);
					player.setMultiSell(null);
					return;
				}

				final PcInventory inv = player.getInventory();

				int slots = 0;
				int weight = 0;
				for (Ingredient e : entry.getProducts())
				{
					if (e.getItemId() < 0) // special
					{
						continue;
					}

					if (!e.isStackable())
					{
						slots += e.getItemCount() * _amount;
					}
					else if (player.getInventory().getItemByItemId(e.getItemId()) == null)
					{
						slots++;
					}
					weight += e.getItemCount() * _amount * e.getWeight();
				}

				if (!inv.validateWeight(weight))
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
					return;
				}

				if (!inv.validateCapacity(slots))
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
					return;
				}

				ArrayList<Ingredient> ingredientsList = new ArrayList<>(entry.getIngredients().size());
				// Generate a list of distinct ingredients and counts in order to check if the correct item-counts
				// are possessed by the player
				boolean newIng;
				for (Ingredient e : entry.getIngredients())
				{
					if (player.isGM())
					{
						player.sendMessage("Item ID = " + e.getItemId());
					}
					/*
                    if (e.getItemId() == 13722)
					{
						player.sendMessage("You can't use your Olympiad Tokens yet. Try again later.");
						return;
					}*/

					newIng = true;
					// at this point, the template has already been modified so that enchantments are properly included
					// whenever they need to be applied.  Uniqueness of items is thus judged by item id AND enchantment level
					for (int i = ingredientsList.size(); --i >= 0; )
					{
						Ingredient ex = ingredientsList.get(i);
						// if the item was already added in the list, merely increment the count
						// this happens if 1 list entry has the same ingredient twice (example 2 swords = 1 dual)
						if (ex.getItemId() == e.getItemId() && ex.getEnchantLevel() == e.getEnchantLevel())
						{
							if (ex.getItemCount() + e.getItemCount() > Long.MAX_VALUE)
							{
								player.sendPacket(SystemMessage.getSystemMessage(
										SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
								return;
							}
							// two same ingredients, merge into one and replace old
							final Ingredient ing = ex.clone();
							ing.setItemCount(ex.getItemCount() + e.getItemCount());
							ingredientsList.set(i, ing);
							newIng = false;
							break;
						}
					}
					if (newIng)
					{
						// if it's a new ingredient, just store its info directly (item id, count, enchantment)
						ingredientsList.add(e);
					}
				}

				// now check if the player has sufficient items in the inventory to cover the ingredients' expences
				for (Ingredient e : ingredientsList)
				{
					if (e.getItemCount() * _amount > Long.MAX_VALUE)
					{
						player.sendPacket(SystemMessage
								.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
						return;
					}
					if (e.getItemId() < 0)
					{
						if (!MultiSell.checkSpecialIngredient(e.getItemId(), e.getItemCount() * _amount, player))
						{
							return;
						}
					}
					else
					{
						// if this is not a list that maintains enchantment, check the count of all items that have the given id.
						// otherwise, check only the count of items with exactly the needed enchantment level
						final long required = Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMaintainIngredient() ?
								e.getItemCount() * _amount : e.getItemCount();
						if (inv.getInventoryItemCount(e.getItemId(),
								list.getMaintainEnchantment() ? e.getEnchantLevel() : -1, false) < required)
						{
							SystemMessage sm =
									SystemMessage.getSystemMessage(SystemMessageId.S2_UNIT_OF_THE_ITEM_S1_REQUIRED);
							sm.addItemName(e.getTemplate());
							sm.addNumber((int) required);
							player.sendPacket(sm);
							return;
						}
					}
				}

				EnsoulEffect[] ensoulEffects = null;
				ArrayList<L2Augmentation> augmentation = new ArrayList<>();
				Elementals[] elemental = null;
				int appearance = 0;
                /* All ok, remove items and add final product */
				for (Ingredient e : entry.getIngredients())
				{
					if (e.getItemId() < 0)
					{
						if (!MultiSell.getSpecialIngredient(e.getItemId(), e.getItemCount() * _amount, player))
						{
							return;
						}
					}
					else
					{
						L2ItemInstance itemToTake = inv.getItemByItemId(
								e.getItemId()); // initialize and initial guess for the item to take.
						if (itemToTake == null)
						{ //this is a cheat, transaction will be aborted and if any items already taken will not be returned back to inventory!
							Log.severe("Character: " + player.getName() + " is trying to cheat in multisell, id:" +
									_listId + ":" + _entryId);
							player.setMultiSell(null);
							return;
						}

						/*if (itemToTake.isEquipped())
						{ //this is a cheat, transaction will be aborted and if any items already taken will not be returned back to inventory!
							Logozo.severe("Character: " + player.getName() + " is trying to cheat in multisell, exchanging equipped item, merchatnt id:" + merchant.getNpcId());
							player.setMultiSell(null);
							return;
						}*/

						if (Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMaintainIngredient())
						{
							// if it's a stackable item, just reduce the amount from the first (only) instance that is found in the inventory
							if (itemToTake.isStackable())
							{
								if (!player
										.destroyItem("Multisell", itemToTake.getObjectId(), e.getItemCount() * _amount,
												player.getTarget(), true))
								{
									player.setMultiSell(null);
									return;
								}
							}
							else
							{
								// for non-stackable items, one of two scenaria are possible:
								// a) list maintains enchantment: get the instances that exactly match the requested enchantment level
								// b) list does not maintain enchantment: get the instances with the LOWEST enchantment level

								// a) if enchantment is maintained, then get a list of items that exactly match this enchantment
								if (list.getMaintainEnchantment())
								{
									// loop through this list and remove (one by one) each item until the required amount is taken.
									L2ItemInstance[] inventoryContents =
											inv.getAllItemsByItemId(e.getItemId(), e.getEnchantLevel(), false);
									for (int i = 0; i < e.getItemCount() * _amount; i++)
									{
										if (inventoryContents[i].isSoulEnhanced())
										{
											ensoulEffects = inventoryContents[i].getEnsoulEffects();
										}
										if (inventoryContents[i].isAugmented())
										{
											augmentation.add(inventoryContents[i].getAugmentation());
										}
										if (inventoryContents[i].getElementals() != null)
										{
											elemental = inventoryContents[i].getElementals();
										}
										if (inventoryContents[i].getAppearance() > 0)
										{
											appearance = inventoryContents[i].getAppearance();
										}
										if (!player.destroyItem("Multisell", inventoryContents[i].getObjectId(), 1,
												player.getTarget(), true))
										{
											player.setMultiSell(null);
											return;
										}
									}
								}
								else
								// b) enchantment is not maintained.  Get the instances with the LOWEST enchantment level
								{
									/* NOTE: There are 2 ways to achieve the above goal.
									 * 1) Get all items that have the correct itemId, loop through them until the lowest enchantment
									 * 		level is found.  Repeat all this for the next item until proper count of items is reached.
									 * 2) Get all items that have the correct itemId, sort them once based on enchantment level,
									 * 		and get the range of items that is necessary.
									 * Method 1 is faster for a small number of items to be exchanged.
									 * Method 2 is faster for large amounts.
									 *
									 * EXPLANATION:
									 *   Worst case scenario for algorithm 1 will make it run in a number of cycles given by:
									 * m*(2n-m+1)/2 where m is the number of items to be exchanged and n is the total
									 * number of inventory items that have a matching id.
									 *   With algorithm 2 (sort), sorting takes n*log(n) time and the choice is done in a single cycle
									 * for case b (just grab the m first items) or in linear time for case a (find the beginning of items
									 * with correct enchantment, index x, and take all items from x to x+m).
									 * Basically, whenever m > log(n) we have: m*(2n-m+1)/2 = (2nm-m*m+m)/2 >
									 * (2nlogn-logn*logn+logn)/2 = nlog(n) - log(n*n) + log(n) = nlog(n) + log(n/n*n) =
									 * nlog(n) + log(1/n) = nlog(n) - log(n) = (n-1)log(n)
									 * So for m < log(n) then m*(2n-m+1)/2 > (n-1)log(n) and m*(2n-m+1)/2 > nlog(n)
									 *
									 * IDEALLY:
									 * In order to best optimize the performance, choose which algorithm to run, based on whether 2^m > n
									 * if ( (2<<(e.getItemCount() * _amount)) < inventoryContents.length )
									 *   // do Algorithm 1, no sorting
									 * else
									 *   // do Algorithm 2, sorting
									 *
									 * CURRENT IMPLEMENTATION:
									 * In general, it is going to be very rare for a person to do a massive exchange of non-stackable items
									 * For this reason, we assume that algorithm 1 will always suffice and we keep things simple.
									 * If, in the future, it becomes necessary that we optimize, the above discussion should make it clear
									 * what optimization exactly is necessary (based on the comments under "IDEALLY").
									 */

									// choice 1.  Small number of items exchanged.  No sorting.
									for (int i = 1; i <= e.getItemCount() * _amount; i++)
									{
										L2ItemInstance[] inventoryContents =
												inv.getAllItemsByItemId(e.getItemId(), false);

										itemToTake = inventoryContents[0];
										// get item with the LOWEST enchantment level  from the inventory...
										// +0 is lowest by default...
										if (itemToTake.getEnchantLevel() > 0)
										{
											for (L2ItemInstance item : inventoryContents)
											{
												if (item.getEnchantLevel() < itemToTake.getEnchantLevel())
												{
													itemToTake = item;
													// nothing will have enchantment less than 0. If a zero-enchanted
													// item is found, just take it
													if (itemToTake.getEnchantLevel() == 0)
													{
														break;
													}
												}
											}
										}
										if (!player.destroyItem("Multisell", itemToTake.getObjectId(), 1,
												player.getTarget(), true))
										{
											player.setMultiSell(null);
											return;
										}
									}
								}
							}
						}
					}
				}

				// Generate the appropriate items
				float cumulatedChance = 0.0f;
				float random = Rnd.get(10000) / 100.0f;
				for (Ingredient e : entry.getProducts())
				{
					if (list.isChance())
					{
						cumulatedChance += e.getChance();
						if (e.getChance() == 0 || random < cumulatedChance - e.getChance() || random >= cumulatedChance)
						{
							continue;
						}
					}

					if (e.getItemId() < 0)
					{
						MultiSell.addSpecialProduct(e.getItemId(), e.getItemCount() * _amount, player);
					}
					else
					{
						if (e.isStackable())
						{
							inv.addItem("Multisell", e.getItemId(), e.getItemCount() * _amount, player,
									player.getTarget());
						}
						else
						{
							L2ItemInstance product = null;
							for (int i = 0; i < e.getItemCount() * _amount; i++)
							{
								product = inv.addItem("Multisell", e.getItemId(), 1, player, player.getTarget());
								if (list.getMaintainEnchantment())
								{
									if (ensoulEffects != null)
									{
										for (int j = 0; j < ensoulEffects.length; j++)
										{
											product.setEnsoulEffect(j, ensoulEffects[j]);
										}
									}
									if (i < augmentation.size())
									{
										product.setAugmentation(new L2Augmentation(augmentation.get(i).getAugment1(),
												augmentation.get(i).getAugment2()));
									}
									if (elemental != null)
									{
										for (Elementals elm : elemental)
										{
											product.setElementAttr(elm.getElement(), elm.getValue());
										}
									}
									product.setEnchantLevel(e.getEnchantLevel());
									product.setAppearance(appearance);
									product.updateDatabase();
								}
							}
						}

						// msg part
						SystemMessage sm;

						if (e.getItemCount() * _amount > 1)
						{
							sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
							sm.addItemName(e.getItemId());
							sm.addItemNumber(e.getItemCount() * _amount);
							player.sendPacket(sm);
							sm = null;
						}
						else
						{
							if (list.getMaintainEnchantment() && e.getEnchantLevel() > 0)
							{
								sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2);
								sm.addItemNumber(e.getEnchantLevel());
								sm.addItemName(e.getItemId());
							}
							else
							{
								sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
								sm.addItemName(e.getItemId());
							}
							player.sendPacket(sm);
							sm = null;
						}
					}
				}
				player.sendPacket(new ItemList(player, false));

				StatusUpdate su = new StatusUpdate(player);
				su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
				player.sendPacket(su);
				su = null;

				// finally, give the tax to the castle...
				if (entry.getTaxAmount() > 0)
				{
					target.getCastle().addToTreasury(entry.getTaxAmount() * _amount);
				}

				break;
			}
		}
	}

	private void chooseFromPlayer(L2PcInstance player, L2PcInstance storePlayer)
	{
		TradeList list = storePlayer.getCustomSellList();
		if (list == null || player.getObjectId() == storePlayer.getObjectId() || _amount < 1 ||
				storePlayer.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_CUSTOM_SELL)
		{
			return;
		}

		storePlayer.hasBeenStoreActive();

		TradeItem item = null;
		int index = 1;
		for (TradeItem it : list.getItems())
		{
			if (index == _entryId)
			{
				item = it;
				break;
			}
			index++;
		}

		if (item == null)
		{
			return;
		}

		final PcInventory inv = player.getInventory();
		final PcInventory storeInv = storePlayer.getInventory();

		int slots = 0;
		int weight = 0;
		if (item.getItem().getItemId() < 0)
		{
			return;
		}

		if (!item.getItem().isStackable())
		{
			slots += item.getCount() * _amount;
		}
		else if (player.getInventory().getItemByItemId(item.getItem().getItemId()) == null)
		{
			slots++;
		}
		weight += item.getCount() * _amount * item.getItem().getWeight();

		if (!inv.validateWeight(weight))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			return;
		}

		if (!inv.validateCapacity(slots))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
			return;
		}

		// check if the store player has sufficient items in the inventory to cover the products' expenses
		if (_amount > Long.MAX_VALUE)
		{
			player.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}
		// if this is not a list that maintains enchantment, check the count of all items that have the given id.
		// otherwise, check only the count of items with exactly the needed enchantment level
		long storeItemCount =
				Math.min(item.getCount(), storeInv.getInventoryItemCount(item.getItem().getItemId(), -1, false));
		if (storeItemCount < _amount)
		{
			player.sendMessage("The store has only " + storeItemCount + " units of that item!");
			return;
		}

		// now check if the player has sufficient items in the inventory to cover the ingredients' expenses
		for (L2Item priceItem : item.getPriceItems().keySet())
		{
			long count = item.getPriceItems().get(priceItem);
			if (count * _amount > Long.MAX_VALUE)
			{
				player.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}
			if (priceItem.getItemId() < 0)
			{
				if (!MultiSell.checkSpecialIngredient(priceItem.getItemId(), count * _amount, player))
				{
					return;
				}
			}
			else
			{
				// if this is not a list that maintains enchantment, check the count of all items that have the given id.
				// otherwise, check only the count of items with exactly the needed enchantment level
				final long required = count * _amount;
				if (inv.getInventoryItemCount(priceItem.getItemId(), -1, false) < required)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_UNIT_OF_THE_ITEM_S1_REQUIRED);
					sm.addItemName(priceItem);
					sm.addNumber((int) required);
					player.sendPacket(sm);
					return;
				}
			}
		}

		for (L2Item priceItem : item.getPriceItems().keySet())
		{
			long count = item.getPriceItems().get(priceItem);
			L2ItemInstance itemToTake =
					inv.getItemByItemId(priceItem.getItemId()); // initialize and initial guess for the item to take.
			itemToTake =
					player.checkItemManipulation(itemToTake.getObjectId(), count * _amount, "Custom Private Store");
			if (itemToTake == null)
			{
				// this is a cheat, transaction will be aborted and if any items already taken will not be returned back to inventory!
				Log.severe("Character: " + player.getName() + " is trying to cheat in multisell, id:" + _listId + ":" +
						_entryId);
				return;
			}

			// if it's a stackable item, just reduce the amount from the first (only) instance that is found in the inventory
			if (itemToTake.isStackable())
			{
				if (inv.transferItem("CustomPrivateStore1 (" + player.getName() + "->" + storePlayer.getName() + ")",
						itemToTake.getObjectId(), count * _amount, storeInv, player, storePlayer) == null)
				{
					return;
				}
			}
			else
			{
				for (int i = 1; i <= count * _amount; i++)
				{
					L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(priceItem.getItemId(), false);

					itemToTake = inventoryContents[0];
					// get item with the LOWEST enchantment level  from the inventory...
					// +0 is lowest by default...
					if (itemToTake.getEnchantLevel() > 0)
					{
						for (L2ItemInstance ii : inventoryContents)
						{
							if (ii.getEnchantLevel() < itemToTake.getEnchantLevel())
							{
								itemToTake = ii;
								// nothing will have enchantment less than 0. If a zero-enchanted
								// item is found, just take it
								if (itemToTake.getEnchantLevel() == 0)
								{
									break;
								}
							}
						}
					}
					if (inv.transferItem(
							"CustomPrivateStore2 (" + player.getName() + "->" + storePlayer.getName() + ")",
							itemToTake.getObjectId(), 1, storeInv, player, storePlayer) == null)
					{
						return;
					}
				}
			}
		}

		if (item.getItem().isStackable())
		{
			storeInv.transferItem("CustomPrivateStore3 (" + player.getName() + "->" + storePlayer.getName() + ")",
					item.getObjectId(), _amount, inv, storePlayer, player);
		}
		else
		{
			for (int i = 0; i < _amount; i++)
			{
				storeInv.transferItem("CustomPrivateStore4 (" + player.getName() + "->" + storePlayer.getName() + ")",
						item.getObjectId(), 1, inv, storePlayer, player);
			}
		}

		item.setCount(item.getCount() - _amount);

		list.updateItems();
		String param = "";
		if (list.getItemCount() == 0)
		{
			param = "stop";
		}
		IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("sell");
		if (vch != null)
		{
			vch.useVoicedCommand("sell", storePlayer, param);
		}

		player.sendPacket(new PlayerMultiSellList(storePlayer));

		// msg part
		SystemMessage sm;
		if (_amount > 1)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(item.getItem().getItemId());
			sm.addItemNumber(_amount);
			player.sendPacket(sm);
			sm = null;
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
			sm.addItemName(item.getItem().getItemId());
			player.sendPacket(sm);
			sm = null;
		}

		player.sendPacket(new ItemList(player, false));

		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);

		for (L2Item priceItem : item.getPriceItems().keySet())
		{
			long count = item.getPriceItems().get(priceItem);
			if (count * _amount > 1)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				sm.addItemName(priceItem.getItemId());
				sm.addItemNumber(count * _amount);
				storePlayer.sendPacket(sm);
				sm = null;
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				sm.addItemName(priceItem.getItemId());
				storePlayer.sendPacket(sm);
				sm = null;
			}
		}

		storePlayer.sendPacket(new ItemList(storePlayer, false));

		su = new StatusUpdate(storePlayer);
		su.addAttribute(StatusUpdate.CUR_LOAD, storePlayer.getCurrentLoad());
		storePlayer.sendPacket(su);
		su = null;
	}

	private void transformItem(L2PcInstance player, DirectEnchantMultiSellConfig config)
	{
		L2ItemInstance item = player.getInventory().getItemByObjectId(_entryId);
		if (item == null)
		{
			return;
		}

		if (item.isEquipped() || !EnchantItemTable.isEnchantable(item) || item.getEnchantLevel() >= config.enchantLevel)
		{
			return;
		}

		int currencyId = item.isWeapon() ? config.weaponMaterialId :
				item.getItem().getBodyPart() >= L2Item.SLOT_R_EAR &&
						item.getItem().getBodyPart() <= L2Item.SLOT_LR_FINGER ? config.jewelMaterialId :
						config.armorMaterialId;

		int costCount = item.isWeapon() ? config.costCount : (int) (config.costCount / config.priceDividerForArmor);
		if (player.getInventory().getInventoryItemCount(currencyId, -1, false) < costCount)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_UNIT_OF_THE_ITEM_S1_REQUIRED);
			sm.addItemName(currencyId);
			sm.addNumber(config.costCount);
			player.sendPacket(sm);
			return;
		}

		if (!player.destroyItemByItemId("Transform Multisell", currencyId, costCount, player, true))
		{
			return;
		}

		item.setEnchantLevel(config.enchantLevel);
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2);
		sm.addItemNumber(item.getEnchantLevel());
		sm.addItemName(item.getItemId());
		player.sendPacket(sm);
	}

	private void enchantItem(L2PcInstance player)
	{
		int categoryId = _entryId / EnchantMultiSellList.ItemIdMod;
		int itemModdedId = _entryId % EnchantMultiSellList.ItemIdMod;
		EnchantMultiSellCategory category = EnchantMultiSellTable.getInstance().getCategory(categoryId);
		if (category == null)
		{
			return;
		}

		L2ItemInstance item = null;
		for (L2ItemInstance candidate : player.getInventory().getItems())
		{
			if (candidate.getObjectId() % EnchantMultiSellList.ItemIdMod == itemModdedId)
			{
				item = candidate;
				break;
			}
		}

		if (item == null)
		{
			return;
		}

		if (item.isEquipped() || !EnchantItemTable.isEnchantable(item) ||
				!category.Entries.containsKey(item.getEnchantLevel() + 1))
		{
			return;
		}

		EnchantMultiSellEntry entry = category.Entries.get(item.getEnchantLevel() + 1);
		MerchantPriceConfig mpc = MerchantPriceConfigTable.getInstance().getMerchantPriceConfig(player);

		for (Entry<Integer, Long> extraIngredient : entry.Ingredients.entrySet())
		{
			int currency = extraIngredient.getKey();
			long amount = extraIngredient.getValue();
			if (!item.isWeapon())
			{
				amount /= 5;
			}
			if (amount == 0)
			{
				amount = 1;
			}

			if (currency == 57)
			{
				long tax = (long) (amount * mpc.getCastleTaxRate());
				amount += tax;

				if (tax > 0)
				{
					mpc.getCastle().addToTreasury(tax);
				}
			}

			if (player.getInventory().getInventoryItemCount(currency, -1, false) < amount)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_UNIT_OF_THE_ITEM_S1_REQUIRED);
				sm.addItemName(currency);
				sm.addNumber(amount);
				player.sendPacket(sm);
				return;
			}

			if (!player.destroyItemByItemId("Enchant Multisell", currency, amount, player, true))
			{
				return;
			}
		}

		int random = Rnd.get(100);
		int cumulative = 0;
		for (Entry<Integer, Integer> possibleProduct : entry.Products.entrySet())
		{
			cumulative += possibleProduct.getValue();
			if (random < cumulative)
			{
				item.setEnchantLevel(possibleProduct.getKey());
				break;
			}
		}

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2);
		sm.addItemNumber(item.getEnchantLevel());
		sm.addItemName(item.getItemId());
		player.sendPacket(sm);

		player.sendPacket(new EnchantMultiSellList(player));
	}
}
