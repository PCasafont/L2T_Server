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
import lombok.Getter;

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
	@Getter private final FloodProtectorAction pickUpItem;

	/**
	 * Use-item flood protector.
	 */
	@Getter private final FloodProtectorAction useItem;
	/**
	 * Roll-dice flood protector.
	 */
	@Getter private final FloodProtectorAction rollDice;
	/**
	 * Firework flood protector.
	 */
	@Getter private final FloodProtectorAction firework;
	/**
	 * Item-pet-summon flood protector.
	 */
	@Getter private final FloodProtectorAction itemPetSummon;
	/**
	 * Hero-voice flood protector.
	 */
	@Getter private final FloodProtectorAction heroVoice;
	/**
	 * Shout-chat flood protector.
	 */
	@Getter private final FloodProtectorAction shoutChat;
	/**
	 * Trade-chat flood protector.
	 */
	@Getter private final FloodProtectorAction tradeChat;
	/**
	 * Global-chat flood protector.
	 */
	@Getter private final FloodProtectorAction globalChat;
	/**
	 * Subclass flood protector.
	 */
	@Getter private final FloodProtectorAction subclass;
	/**
	 * Drop-item flood protector.
	 */
	@Getter private final FloodProtectorAction dropItem;
	/**
	 * Server-bypass flood protector.
	 */
	@Getter private final FloodProtectorAction serverBypass;
	/**
	 * Multisell flood protector.
	 */
	@Getter private final FloodProtectorAction multiSell;
	/**
	 * Transaction flood protector.
	 */
	@Getter private final FloodProtectorAction transaction;
	/**
	 * Manufacture flood protector.
	 */
	@Getter private final FloodProtectorAction manufacture;
	/**
	 * Manor flood protector.
	 */
	@Getter private final FloodProtectorAction manor;
	/**
	 * Send mail flood protector.
	 */
	@Getter private final FloodProtectorAction sendMail;
	/**
	 * Character Select protector
	 */
	@Getter private final FloodProtectorAction characterSelect;
	/**
	 * Item Auction
	 */
	@Getter private final FloodProtectorAction itemAuction;
	/**
	 * Magic Gem flood protector.
	 */
	@Getter private final FloodProtectorAction magicGem;
	/**
	 * Event Bypass flood protector.
	 */
	@Getter private final FloodProtectorAction eventBypass;

	@Getter private final FloodProtectorAction reportBot;

	/**
	 * Creates new instance of FloodProtectors.
	 */
	public FloodProtectors(final L2GameClient client)
	{
		super();
		pickUpItem = new FloodProtectorAction(client, Config.FLOOD_PROTECTOR_PICKUP_ITEM);
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
}
