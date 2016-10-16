/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.util;

import l2server.Config;
import l2server.gameserver.network.L2GameClient;

/**
 * Collection of flood protectors for single player.
 *
 * @author fordfrog
 */
public final class FloodProtectors
{
	/**
	 * Pick up item flood protector.
	 */
	private final FloodProtectorAction pickUp;

	/**
	 * Use-item flood protector.
	 */
	private final FloodProtectorAction useItem;
	/**
	 * Roll-dice flood protector.
	 */
	private final FloodProtectorAction rollDice;
	/**
	 * Firework flood protector.
	 */
	private final FloodProtectorAction firework;
	/**
	 * Item-pet-summon flood protector.
	 */
	private final FloodProtectorAction itemPetSummon;
	/**
	 * Hero-voice flood protector.
	 */
	private final FloodProtectorAction heroVoice;
	/**
	 * Shout-chat flood protector.
	 */
	private final FloodProtectorAction shoutChat;
	/**
	 * Trade-chat flood protector.
	 */
	private final FloodProtectorAction tradeChat;
	/**
	 * Global-chat flood protector.
	 */
	private final FloodProtectorAction globalChat;
	/**
	 * Subclass flood protector.
	 */
	private final FloodProtectorAction subclass;
	/**
	 * Drop-item flood protector.
	 */
	private final FloodProtectorAction dropItem;
	/**
	 * Server-bypass flood protector.
	 */
	private final FloodProtectorAction serverBypass;
	/**
	 * Multisell flood protector.
	 */
	private final FloodProtectorAction multiSell;
	/**
	 * Transaction flood protector.
	 */
	private final FloodProtectorAction transaction;
	/**
	 * Manufacture flood protector.
	 */
	private final FloodProtectorAction manufacture;
	/**
	 * Manor flood protector.
	 */
	private final FloodProtectorAction manor;
	/**
	 * Send mail flood protector.
	 */
	private final FloodProtectorAction sendMail;
	/**
	 * Character Select protector
	 */
	private final FloodProtectorAction characterSelect;
	/**
	 * Item Auction
	 */
	private final FloodProtectorAction itemAuction;
	/**
	 * Magic Gem flood protector.
	 */
	private final FloodProtectorAction magicGem;
	/**
	 * Event Bypass flood protector.
	 */
	private final FloodProtectorAction eventBypass;

	private final FloodProtectorAction reportBot;

	/**
	 * Creates new instance of FloodProtectors.
	 */
	public FloodProtectors(final L2GameClient client)
	{
		super();
		pickUp = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_PICKUP_ITEM);
		useItem = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_USE_ITEM);
		rollDice = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_ROLL_DICE);
		firework = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_FIREWORK);
		itemPetSummon = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_ITEM_PET_SUMMON);
		heroVoice = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_HERO_VOICE);
		shoutChat = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_SHOUT_CHAT);
		tradeChat = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_TRADE_CHAT);
		globalChat = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_GLOBAL_CHAT);
		subclass = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_SUBCLASS);
		dropItem = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_DROP_ITEM);
		serverBypass = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_SERVER_BYPASS);
		multiSell = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MULTISELL);
		transaction = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_TRANSACTION);
		manufacture = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MANUFACTURE);
		manor = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MANOR);
		sendMail = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_SENDMAIL);
		characterSelect = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_CHARACTER_SELECT);
		itemAuction = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_ITEM_AUCTION);
		magicGem = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MAGICGEM);
		eventBypass = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_EVENTBYPASS);
		reportBot = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_REPORT_BOT);
	}

	/**
	 * Returns {@link #_useItem}.
	 *
	 * @return {@link #_useItem}
	 */
	public FloodProtectorAction getUseItem()
	{
		return useItem;
	}

	/**
	 * Returns {@link #_rollDice}.
	 *
	 * @return {@link #_rollDice}
	 */
	public FloodProtectorAction getRollDice()
	{
		return rollDice;
	}

	/**
	 * Returns {@link #_firework}.
	 *
	 * @return {@link #_firework}
	 */
	public FloodProtectorAction getFirework()
	{
		return firework;
	}

	/**
	 * Returns {@link #_itemPetSummon}.
	 *
	 * @return {@link #_itemPetSummon}
	 */
	public FloodProtectorAction getItemPetSummon()
	{
		return itemPetSummon;
	}

	/**
	 * Returns {@link #_heroVoice}.
	 *
	 * @return {@link #_heroVoice}
	 */
	public FloodProtectorAction getHeroVoice()
	{
		return heroVoice;
	}

	/**
	 * Returns {@link #_shoutChat}.
	 *
	 * @return {@link #_shoutChat}
	 */
	public FloodProtectorAction getShoutChat()
	{
		return shoutChat;
	}

	/**
	 * Returns {@link #_tradeChat}.
	 *
	 * @return {@link #_tradeChat}
	 */
	public FloodProtectorAction getTradeChat()
	{
		return tradeChat;
	}

	/**
	 * Returns {@link #_globalChat}.
	 *
	 * @return {@link #_globalChat}
	 */
	public FloodProtectorAction getGlobalChat()
	{
		return globalChat;
	}

	/**
	 * Returns {@link #_subclass}.
	 *
	 * @return {@link #_subclass}
	 */
	public FloodProtectorAction getSubclass()
	{
		return subclass;
	}

	/**
	 * Returns {@link #_dropItem}.
	 *
	 * @return {@link #_dropItem}
	 */
	public FloodProtectorAction getDropItem()
	{
		return dropItem;
	}

	/**
	 * Returns {@link #_serverBypass}.
	 *
	 * @return {@link #_serverBypass}
	 */
	public FloodProtectorAction getServerBypass()
	{
		return serverBypass;
	}

	/**
	 * Returns .
	 *
	 * @return
	 */
	public FloodProtectorAction getMultiSell()
	{
		return multiSell;
	}

	/**
	 * Returns {@link #_transaction}.
	 *
	 * @return {@link #_transaction}
	 */
	public FloodProtectorAction getTransaction()
	{
		return transaction;
	}

	/**
	 * Returns {@link #_manufacture}.
	 *
	 * @return {@link #_manufacture}
	 */
	public FloodProtectorAction getManufacture()
	{
		return manufacture;
	}

	/**
	 * Returns {@link #_manor}.
	 *
	 * @return {@link #_manor}
	 */
	public FloodProtectorAction getManor()
	{
		return manor;
	}

	/**
	 * Returns {@link #_sendMail}.
	 *
	 * @return {@link #_sendMail}
	 */
	public FloodProtectorAction getSendMail()
	{
		return sendMail;
	}

	/**
	 * Returns {@link #_characterSelect}.
	 *
	 * @return {@link #_characterSelect}
	 */
	public FloodProtectorAction getCharacterSelect()
	{
		return characterSelect;
	}

	/**
	 * Returns {@link #_itemAuction}.
	 *
	 * @return {@link #_itemAuction}
	 */
	public FloodProtectorAction getItemAuction()
	{
		return itemAuction;
	}

	/**
	 * Returns {@link #_magicGem}.
	 *
	 * @return {@link #_magicGem}
	 */
	public FloodProtectorAction getMagicGem()
	{
		return magicGem;
	}

	/**
	 * Returns {@link #_eventBypass}.
	 *
	 * @return {@link #_eventBypass}
	 */
	public FloodProtectorAction getEventBypass()
	{
		return eventBypass;
	}

	/**
	 * Returns {@link #_pickUp}.
	 *
	 * @return {@link #_pickUp}
	 */
	public FloodProtectorAction getPickUpItem()
	{
		return pickUp;
	}

	public FloodProtectorAction getReportBot()
	{
		return reportBot;
	}
}
