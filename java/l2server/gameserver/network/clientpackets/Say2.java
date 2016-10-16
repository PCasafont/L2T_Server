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
import l2server.L2DatabaseFactory;
import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.handler.ChatHandler;
import l2server.gameserver.handler.IChatHandler;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * This class ...
 *
 * @version $Revision: 1.16.2.12.2.7 $ $Date: 2005/04/11 10:06:11 $
 */
public final class Say2 extends L2GameClientPacket
{

	//private static Logger _logChat = Logger.getLogger("chat");

	public static final int ALL = 0;
	public static final int SHOUT = 1; //!
	public static final int TELL = 2;
	public static final int PARTY = 3; //#
	public static final int CLAN = 4; //@
	public static final int GM = 5;
	public static final int PETITION_PLAYER = 6; // used for petition
	public static final int PETITION_GM = 7; //* used for petition
	public static final int TRADE = 8; //+
	public static final int ALLIANCE = 9; //$
	public static final int ANNOUNCEMENT = 10;
	public static final int BOAT = 11;
	public static final int L2FRIEND = 12;
	public static final int MSNCHAT = 13;
	public static final int PARTYMATCH_ROOM = 14;
	public static final int PARTYROOM_COMMANDER = 15; //(Yellow)
	public static final int PARTYROOM_ALL = 16; //(Red)
	public static final int HERO_VOICE = 17;
	public static final int CRITICAL_ANNOUNCE = 18;
	public static final int SCREEN_ANNOUNCE = 19;
	public static final int BATTLEFIELD = 20;
	public static final int MPCC_ROOM = 21;
	public static final int ALL_NOT_RECORDED = 22;
	public static final int UNK_1 = 23;
	public static final int UNK_2 = 24;
	public static final int GLOBAL = 25;

	private static final String[] CHAT_NAMES = {
			"ALL",
			"SHOUT",
			"TELL",
			"PARTY",
			"CLAN",
			"GM",
			"PETITION_PLAYER",
			"PETITION_GM",
			"TRADE",
			"ALLIANCE",
			"ANNOUNCEMENT",
			//10
			"BOAT",
			"L2FRIEND",
			"MSNCHAT",
			"PARTYMATCH_ROOM",
			"PARTYROOM_COMMANDER",
			"PARTYROOM_ALL",
			"HERO_VOICE",
			"CRITICAL_ANNOUNCE",
			"SCREEN_ANNOUNCE",
			"BATTLEFIELD",
			"MPCC_ROOM",
			"ALL_NOT_RECORDED",
			"UNK_1",
			"UNK_2",
			"GLOBAL"
	};

	private String _text;
	private int _type;
	private String _target;

	@Override
	protected void readImpl()
	{
		_text = readS();
		_type = readD();
		_target = _type == TELL ? readS() : null;
	}

	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
		{
			Log.info("Say2: Msg Type = '" + _type + "' Text = '" + _text + "'.");
		}

		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		if (_type < 0 || _type >= CHAT_NAMES.length)
		{
			Log.warning("Say2: Invalid type: " + _type + " Player : " + activeChar.getName() + " text: " +
					String.valueOf(_text));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.logout();
			return;
		}

		if (_text.isEmpty())
		{
			Log.warning(activeChar.getName() + ": sending empty text. Possible packet hack!");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.logout();
			return;
		}

		if (!_text.contains("Type="))
		{
			_text = _text.replaceAll("\\s+", " ");
		}

		// Even though the client can handle more characters than it's current limit allows, an overflow (critical error) happens if you pass a huge (1000+) message.
		// April 27, 2009 - Verified on Gracia P2 & Final official client as 105
		// Allow higher limit if player shift some item (text is longer then)
		if (!activeChar.isGM() &&
				(_text.indexOf(8) >= 0 && _text.length() > 500 || _text.indexOf(8) < 0 && _text.length() > 105))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DONT_SPAM));
			return;
		}

		if (activeChar.getName().equals("Elrondd") || activeChar.getName().equals("Quicer") ||
				activeChar.getHWID().equals("BFEBFBFF0001067A527AC38E") ||
				activeChar.getHWID().equals("BFEBFBFF000306A9D6038B4D"))
		{
			activeChar.sendMessage("Your right to use the chat has been revoked.");
			return;
		}
		/*
        if (activeChar.isPlayingMiniGame())
		{
			activeChar.sendMessage("You may not chat at this time.");
			return;
		}*/

		if (!_text.equalsIgnoreCase(".event") && activeChar.isPlayingEvent() &&
				(activeChar.getEvent().isType(EventType.DeathMatch) ||
						activeChar.getEvent().isType(EventType.Survival) ||
						activeChar.getEvent().isType(EventType.KingOfTheHill)))
		{
			activeChar.sendMessage("You cannot talk during an All vs All PvP Event");
			return;
		}

		if (activeChar.isCursedWeaponEquipped() && (_type == TRADE || _type == SHOUT || _type == GLOBAL))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(
					SystemMessageId.SHOUT_AND_TRADE_CHAT_CANNOT_BE_USED_WHILE_POSSESSING_CURSED_WEAPON));
			return;
		}

		if (activeChar.isChatBanned())
		{
			if (_type == ALL || _type == SHOUT || _type == TRADE || _type == HERO_VOICE || _type == GLOBAL)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED));
				return;
			}
		}

		if (activeChar.isInJail() && Config.JAIL_DISABLE_CHAT)
		{
			if (_type != ALL)
			{
				activeChar.sendMessage("You can not chat with players outside of the jail.");
				return;
			}
		}

		if (_type == PETITION_PLAYER && activeChar.isGM())
		{
			_type = PETITION_GM;
		}

		if (Config.LOG_CHAT)
		{
			if (_type == CLAN && activeChar.getClan() != null)
			{
				_target = activeChar.getClan().getName();
			}
			else if (_type == ALLIANCE && activeChar.getClan() != null)
			{
				_target = activeChar.getClan().getAllyName();
			}

			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();

				PreparedStatement statement = con.prepareStatement(
						"REPLACE INTO log_chat(time, type, talker, listener, text) VALUES (?,?,?,?,?);");

				statement.setLong(1, System.currentTimeMillis());
				statement.setString(2, CHAT_NAMES[_type]);
				statement.setString(3, activeChar.getName());
				statement.setString(4, _target);
				statement.setString(5, _text);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			/*LogRecord record = new LogRecord(Level.INFO, _text);
			record.setLoggerName("chat");

			if (_type == TELL)
				record.setParameters(new Object[]{CHAT_NAMES[_type], "[" + activeChar.getName() + " to "+_target+"]"});
			else
				record.setParameters(new Object[]{CHAT_NAMES[_type], "[" + activeChar.getName() + "]"});

			_logChat.log(record);*/
		}

		if (_text.indexOf(8) >= 0 && !parseAndPublishItem(activeChar))
		{
			return;
		}

		// Say Filter implementation
		if (Config.USE_SAY_FILTER)
		{
			checkText();
		}

		IChatHandler handler = ChatHandler.getInstance().getChatHandler(_type);
		if (handler != null)
		{
			// Elcardia -> Elcopia OP replacement
			//_text = _text.replaceAll("([lLiI1][ -._]*[cCkK][ -._]*)[aA4]"
			//		+ "([ -._]*)[rR]([ -._]*)[dD]", "$1o$2$3p");

			handler.handleChat(_type, activeChar, _target, _text);
		}
		else
		{
			Log.info("No handler registered for ChatType: " + _type + " Player: " + getClient());
		}
	}

	private void checkText()
	{
		String filteredText = _text;
		for (String pattern : Config.FILTER_LIST)
		{
			filteredText = filteredText.replaceAll("(?i)" + pattern, Config.CHAT_FILTER_CHARS);
		}
		_text = filteredText;
	}

	private boolean parseAndPublishItem(L2PcInstance owner)
	{
		int pos1 = -1;
		while ((pos1 = _text.indexOf(8, pos1)) > -1)
		{
			int pos = _text.indexOf("ID=", pos1);
			if (pos == -1)
			{
				return false;
			}
			StringBuilder result = new StringBuilder(9);
			pos += 3;
			while (Character.isDigit(_text.charAt(pos)))
			{
				result.append(_text.charAt(pos++));
			}
			int id = Integer.parseInt(result.toString());
			L2Object item = L2World.getInstance().findObject(id);
			if (item instanceof L2ItemInstance)
			{
				if (owner.getInventory().getItemByObjectId(id) == null)
				{
					Log.info(getClient() + " trying publish item which doesnt own! ID:" + id);
					return false;
				}
				((L2ItemInstance) item).publish();
			}
			else
			{
				Log.info(getClient() + " trying publish object which is not item! Object:" + item);
				return false;
			}
			pos1 = _text.indexOf(8, pos) + 1;
			if (pos1 == 0) // missing ending tag
			{
				Log.info(getClient() + " sent invalid publish item msg! ID:" + id);
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
