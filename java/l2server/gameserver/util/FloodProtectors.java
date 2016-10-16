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
		this.pickUp = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_PICKUP_ITEM);
		this.useItem = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_USE_ITEM);
		this.rollDice = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_ROLL_DICE);
		this.firework = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_FIREWORK);
		this.itemPetSummon = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_ITEM_PET_SUMMON);
		this.heroVoice = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_HERO_VOICE);
		this.shoutChat = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_SHOUT_CHAT);
		this.tradeChat = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_TRADE_CHAT);
		this.globalChat = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_GLOBAL_CHAT);
		this.subclass = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_SUBCLASS);
		this.dropItem = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_DROP_ITEM);
		this.serverBypass = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_SERVER_BYPASS);
		this.multiSell = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MULTISELL);
		this.transaction = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_TRANSACTION);
		this.manufacture = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MANUFACTURE);
		this.manor = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MANOR);
		this.sendMail = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_SENDMAIL);
		this.characterSelect = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_CHARACTER_SELECT);
		this.itemAuction = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_ITEM_AUCTION);
		this.magicGem = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_MAGICGEM);
		this.eventBypass = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_EVENTBYPASS);
		this.reportBot = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_REPORT_BOT);
	}

	/**
	 * Returns {@link #_useItem}.
	 *
	 * @return {@link #_useItem}
	 */
	public FloodProtectorAction getUseItem()
	{
		return this.useItem;
	}

	/**
	 * Returns {@link #_rollDice}.
	 *
	 * @return {@link #_rollDice}
	 */
	public FloodProtectorAction getRollDice()
	{
		return this.rollDice;
	}

	/**
	 * Returns {@link #_firework}.
	 *
	 * @return {@link #_firework}
	 */
	public FloodProtectorAction getFirework()
	{
		return this.firework;
	}

	/**
	 * Returns {@link #_itemPetSummon}.
	 *
	 * @return {@link #_itemPetSummon}
	 */
	public FloodProtectorAction getItemPetSummon()
	{
		return this.itemPetSummon;
	}

	/**
	 * Returns {@link #_heroVoice}.
	 *
	 * @return {@link #_heroVoice}
	 */
	public FloodProtectorAction getHeroVoice()
	{
		return this.heroVoice;
	}

	/**
	 * Returns {@link #_shoutChat}.
	 *
	 * @return {@link #_shoutChat}
	 */
	public FloodProtectorAction getShoutChat()
	{
		return this.shoutChat;
	}

	/**
	 * Returns {@link #_tradeChat}.
	 *
	 * @return {@link #_tradeChat}
	 */
	public FloodProtectorAction getTradeChat()
	{
		return this.tradeChat;
	}

	/**
	 * Returns {@link #_globalChat}.
	 *
	 * @return {@link #_globalChat}
	 */
	public FloodProtectorAction getGlobalChat()
	{
		return this.globalChat;
	}

	/**
	 * Returns {@link #_subclass}.
	 *
	 * @return {@link #_subclass}
	 */
	public FloodProtectorAction getSubclass()
	{
		return this.subclass;
	}

	/**
	 * Returns {@link #_dropItem}.
	 *
	 * @return {@link #_dropItem}
	 */
	public FloodProtectorAction getDropItem()
	{
		return this.dropItem;
	}

	/**
	 * Returns {@link #_serverBypass}.
	 *
	 * @return {@link #_serverBypass}
	 */
	public FloodProtectorAction getServerBypass()
	{
		return this.serverBypass;
	}

	/**
	 * Returns .
	 *
	 * @return
	 */
	public FloodProtectorAction getMultiSell()
	{
		return this.multiSell;
	}

	/**
	 * Returns {@link #_transaction}.
	 *
	 * @return {@link #_transaction}
	 */
	public FloodProtectorAction getTransaction()
	{
		return this.transaction;
	}

	/**
	 * Returns {@link #_manufacture}.
	 *
	 * @return {@link #_manufacture}
	 */
	public FloodProtectorAction getManufacture()
	{
		return this.manufacture;
	}

	/**
	 * Returns {@link #_manor}.
	 *
	 * @return {@link #_manor}
	 */
	public FloodProtectorAction getManor()
	{
		return this.manor;
	}

	/**
	 * Returns {@link #_sendMail}.
	 *
	 * @return {@link #_sendMail}
	 */
	public FloodProtectorAction getSendMail()
	{
		return this.sendMail;
	}

	/**
	 * Returns {@link #_characterSelect}.
	 *
	 * @return {@link #_characterSelect}
	 */
	public FloodProtectorAction getCharacterSelect()
	{
		return this.characterSelect;
	}

	/**
	 * Returns {@link #_itemAuction}.
	 *
	 * @return {@link #_itemAuction}
	 */
	public FloodProtectorAction getItemAuction()
	{
		return this.itemAuction;
	}

	/**
	 * Returns {@link #_magicGem}.
	 *
	 * @return {@link #_magicGem}
	 */
	public FloodProtectorAction getMagicGem()
	{
		return this.magicGem;
	}

	/**
	 * Returns {@link #_eventBypass}.
	 *
	 * @return {@link #_eventBypass}
	 */
	public FloodProtectorAction getEventBypass()
	{
		return this.eventBypass;
	}

	/**
	 * Returns {@link #_pickUp}.
	 *
	 * @return {@link #_pickUp}
	 */
	public FloodProtectorAction getPickUpItem()
	{
		return this.pickUp;
	}

	public FloodProtectorAction getReportBot()
	{
		return this.reportBot;
	}
}
