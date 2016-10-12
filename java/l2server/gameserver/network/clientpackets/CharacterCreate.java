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
import l2server.gameserver.GeoData;
import l2server.gameserver.Shutdown;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.datatables.CharTemplateTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.datatables.SkillTreeTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.instancemanager.MainTownManager;
import l2server.gameserver.instancemanager.MainTownManager.MainTownInfo;
import l2server.gameserver.instancemanager.QuestManager;
import l2server.gameserver.model.*;
import l2server.gameserver.model.L2Macro.L2MacroCmd;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.serverpackets.CharCreateFail;
import l2server.gameserver.network.serverpackets.CharCreateOk;
import l2server.gameserver.network.serverpackets.CharSelectionInfo;
import l2server.gameserver.templates.chars.L2PcTemplate;
import l2server.gameserver.templates.chars.L2PcTemplate.PcTemplateItem;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@SuppressWarnings("unused")
public final class CharacterCreate extends L2GameClientPacket
{

	protected static final Logger _logAccounting = Logger.getLogger("accounting");

	// cSdddddddddddd
	private String _name;
	private int _race;
	private byte _sex;
	private int _classId;
	private int _baseTemplateId;
	private int _int;
	private int _str;
	private int _con;
	private int _men;
	private int _dex;
	private int _wit;
	private byte _hairStyle;
	private byte _hairColor;
	private byte _face;

	@Override
	protected void readImpl()
	{
		_name = readS();
		_race = readD();
		_sex = (byte) readD();
		_classId = readD();
		if (Config.IS_CLASSIC && _classId > 53)
		{
			_classId = 53;
			_name = ""; // Force invalid message when they try to create > dwarf on classic
		}
		L2PcTemplate t = null;
		for (int i = 0; i < 14; i++)
		{
			L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(i);
			if (template == null)
			{
				continue;
			}

			int startingClassId = template.startingClassId;
			if (_race == 5 && _sex == 1)
			{
				startingClassId++;
			}
			else if (_race == 6 && _sex == 0)
			{
				_sex = 1;
			}
			if (startingClassId == _classId)
			{
				_baseTemplateId = i;
				t = CharTemplateTable.getInstance().getTemplate(i);
				break;
			}
		}
		_int = readD();
		_str = readD();
		_con = readD();
		_men = readD();
		_dex = readD();
		_wit = readD();
		_hairStyle = (byte) readD();
		_hairColor = (byte) readD();
		_face = (byte) readD();
	}

	@Override
	protected void runImpl()
	{
		if (Shutdown.getInstance().isShuttingDown())
		{
			sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
			return;
		}

		// Last Verified: May 30, 2009 - Gracia Final - Players are able to create characters with names consisting of as little as 1,2,3 letter/number combinations.
		if (_name.length() < 1 || _name.length() > 16)
		{
			if (Config.DEBUG)
			{
				Log.fine("Character Creation Failure: Character name " + _name +
						" is invalid. Message generated: Your title cannot exceed 16 characters in length. Please try again.");
			}

			sendPacket(new CharCreateFail(CharCreateFail.REASON_16_ENG_CHARS));
			return;
		}

		if (Config.FORBIDDEN_NAMES.length > 1)
		{
			for (String st : Config.FORBIDDEN_NAMES)
			{
				if (_name.toLowerCase().contains(st.toLowerCase()))
				{
					sendPacket(new CharCreateFail(CharCreateFail.REASON_INCORRECT_NAME));
					return;
				}
			}
		}

		// Last Verified: May 30, 2009 - Gracia Final
		if (!Util.isAlphaNumeric(_name) || !isValidName(_name))
		{
			if (Config.DEBUG)
			{
				Log.fine("Character Creation Failure: Character name " + _name +
						" is invalid. Message generated: Incorrect name. Please try again.");
			}

			sendPacket(new CharCreateFail(CharCreateFail.REASON_INCORRECT_NAME));
			return;
		}

		if (_face > 2 || _face < 0)
		{
			Log.warning("Character Creation Failure: Character face " + _face + " is invalid. Possible client hack. " +
					getClient());

			sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
			return;
		}

		if (_hairStyle < 0 || _sex == 0 && _hairStyle > 4 || _sex != 0 && _hairStyle > 6)
		{
			Log.warning("Character Creation Failure: Character hair style " + _hairStyle +
					" is invalid. Possible client hack. " + getClient());

			sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
			return;
		}

		if (_hairColor > 3 || _hairColor < 0)
		{
			Log.warning("Character Creation Failure: Character hair color " + _hairColor +
					" is invalid. Possible client hack. " + getClient());

			sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
			return;
		}

		L2PcInstance newChar = null;
		L2PcTemplate template = null;

		/*
		 * DrHouse: Since checks for duplicate names are done using SQL, lock must be held until data is written to DB as well.
		 */
		synchronized (CharNameTable.getInstance())
		{
			if (CharNameTable.getInstance().accountCharNumber(getClient().getAccountName()) >=
					Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT && Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0)
			{
				if (Config.DEBUG)
				{
					Log.fine("Max number of characters reached. Creation failed.");
				}

				sendPacket(new CharCreateFail(CharCreateFail.REASON_TOO_MANY_CHARACTERS));
				return;
			}
			else if (CharNameTable.getInstance().doesCharNameExist(_name))
			{
				if (Config.DEBUG)
				{
					Log.fine(
							"Character Creation Failure: Message generated: You cannot create another character. Please delete the existing character and try again.");
				}

				sendPacket(new CharCreateFail(CharCreateFail.REASON_NAME_ALREADY_EXISTS));
				return;
			}

			template = CharTemplateTable.getInstance().getTemplate(_baseTemplateId);

			if (template == null)
			{
				if (Config.DEBUG)
				{
					Log.fine("Character Creation Failure: " + _name + " classId: " + _classId + " Template: " +
							template + " Message generated: Your character creation has failed.");
				}

				sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
				return;
			}

			int objectId = IdFactory.getInstance().getNextId();
			newChar = L2PcInstance
					.create(objectId, template, getClient().getAccountName(), _name, _hairStyle, _hairColor, _face,
							_sex != 0, _classId);
		}

		newChar.setCurrentHp(newChar.getMaxHp());
		newChar.setCurrentCp(newChar.getMaxCp());
		newChar.setCurrentMp(newChar.getMaxMp());
		// newChar.setMaxLoad(template.baseLoad);

		CharCreateOk cco = new CharCreateOk();
		sendPacket(cco);

		initNewChar(getClient(), newChar);

		LogRecord record = new LogRecord(Level.INFO, "Created new character");
		record.setParameters(new Object[]{newChar, getClient()});
		_logAccounting.log(record);
	}

	public static boolean isValidName(String text)
	{
		boolean result = true;
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.CNAME_TEMPLATE);
		}
		catch (PatternSyntaxException e) // case of illegal pattern
		{
			Log.warning("ERROR : Character name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		Matcher regexp = pattern.matcher(text);
		if (!regexp.matches())
		{
			result = false;
		}
		return result;
	}

	private void initNewChar(L2GameClient client, L2PcInstance newChar)
	{
		if (Config.DEBUG)
		{
			Log.fine("Character init start");
		}

		L2World.getInstance().storeObject(newChar);

		L2PcTemplate template = newChar.getTemplate();

		newChar.addAdena("Init", Config.STARTING_ADENA, null, false);

		MainTownInfo mainTown = MainTownManager.getInstance().getCurrentMainTown();
		if (mainTown != null)
		{
			int startX = mainTown.getStartX() + Rnd.get(-mainTown.getStartRandom(), mainTown.getStartRandom());
			int startY = mainTown.getStartY() + Rnd.get(-mainTown.getStartRandom(), mainTown.getStartRandom());
			int startZ = GeoData.getInstance().getHeight(startX, startY, mainTown.getStartZ());
			newChar.setXYZInvisible(startX, startY, startZ);
		}
		else
		{
			newChar.setXYZInvisible(template.startX + Rnd.get(-template.startRandom, template.startRandom),
					template.startY + Rnd.get(-template.startRandom, template.startRandom), template.startZ);
		}

		newChar.setTitle("");

		if (Config.STARTING_LEVEL > 1)
		{
			newChar.getStat().addLevel((byte) (Config.STARTING_LEVEL - 1));
		}
		if (Config.STARTING_SP > 0)
		{
			newChar.getStat().addSp(Config.STARTING_SP);
		}

		L2ShortCut shortcut;
		// add attack shortcut
		shortcut = new L2ShortCut(0, 0, 3, 2, 0, 1);
		newChar.registerShortCut(shortcut);
		// add take shortcut
		shortcut = new L2ShortCut(3, 0, 3, 5, 0, 1);
		newChar.registerShortCut(shortcut);
		// add sit shortcut
		shortcut = new L2ShortCut(10, 0, 3, 0, 0, 1);
		newChar.registerShortCut(shortcut);

		for (PcTemplateItem ia : template.getItems())
		{
			L2ItemInstance item = newChar.getInventory().addItem("Init", ia.getItemId(), ia.getAmount(), newChar, null);

			if (item == null)
			{
				Log.warning("Could not create item during char creation: itemId " + ia.getItemId() + ", amount " +
						ia.getAmount() + ".");
				continue;
			}

			// add tutbook shortcut
			if (item.getItemId() == 5588)
			{
				shortcut = new L2ShortCut(11, 0, 1, item.getObjectId(), 0, 1);
				newChar.registerShortCut(shortcut);
			}

			if (item.isEquipable() && ia.isEquipped())
			{
				newChar.getInventory().equipItem(item);
			}
		}

		for (L2SkillLearn skill : SkillTreeTable.getInstance().getAvailableClassSkills(newChar))
		{
			if (skill.getMinLevel() > 1)
			{
				continue;
			}

			newChar.addSkill(SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel()), true);
			if (skill.getId() == 1001 || skill.getId() == 1177 || skill.getId() == 30001)
			{
				shortcut = new L2ShortCut(1, 0, 2, skill.getId(), skill.getLevel(), 1);
				newChar.registerShortCut(shortcut);
			}
			if (skill.getId() == 1216)
			{
				shortcut = new L2ShortCut(10, 0, 2, skill.getId(), skill.getLevel(), 1);
				newChar.registerShortCut(shortcut);
			}
			if (Config.DEBUG)
			{
				Log.fine("Adding starter skill:" + skill.getId() + " / " + skill.getLevel());
			}
		}

		newChar.addRaceSkills();

		addCustomMacros(newChar);

		if (!Config.DISABLE_TUTORIAL)
		{
			startTutorialQuest(newChar);
		}

		newChar.setOnlineStatus(true, false);
		newChar.deleteMe();

		CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		client.getConnection().sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());

		if (Config.DEBUG)
		{
			Log.fine("Character init end");
		}
	}

	public void startTutorialQuest(L2PcInstance player)
	{
		QuestState qs = player.getQuestState("Q255_Tutorial");
		Quest q = null;
		if (qs == null)
		{
			q = QuestManager.getInstance().getQuest("Q255_Tutorial");
		}
		if (q != null)
		{
			q.newQuestState(player).setState(State.STARTED);
		}
	}

	private void addCustomMacros(L2PcInstance player)
	{
		L2ItemInstance item;
		L2Macro macro;
		L2ShortCut shortcut;

		if (Config.isServer(Config.TENKAI))
		{
			// Magic Gem
			item = player.getInventory().addItem("Init", 1373, 1, player, null);
			shortcut = new L2ShortCut(4, 0, 1, item.getObjectId(), 0, 1);
			player.registerShortCut(shortcut);

			// Deck
			item = player.getInventory().addItem("Init", 938, 1, player, null);
			shortcut = new L2ShortCut(5, 0, 1, item.getObjectId(), 0, 1);
			player.registerShortCut(shortcut);

			// .event macro
			macro = new L2Macro(1000, 5, "Event Info", "Event Information", "EVNT",
					new L2MacroCmd[]{new L2MacroCmd(0, 3, 0, 0, ".event")});
			player.registerMacro(macro);
			shortcut = new L2ShortCut(6, 0, 4, 1000, 0, 0);
			player.registerShortCut(shortcut);
			// .hidden stats macro
			macro = new L2Macro(1001, 5, "My Hidden Stats", "Hidden Stats Panel", "MHST",
					new L2MacroCmd[]{new L2MacroCmd(0, 3, 0, 0, ".myhiddenstats")});
			player.registerMacro(macro);
			shortcut = new L2ShortCut(7, 0, 4, 1001, 0, 0);
			player.registerShortCut(shortcut);
			// .treasure macro
			macro = new L2Macro(1002, 5, "Treasure", "Treasure Seeking Hints", "TRSR",
					new L2MacroCmd[]{new L2MacroCmd(0, 3, 0, 0, ".treasure")});
			player.registerMacro(macro);
			shortcut = new L2ShortCut(8, 0, 4, 1002, 0, 0);
			player.registerShortCut(shortcut);
			// .noexp macro
			macro = new L2Macro(1003, 5, "No Exp", "To not earn experience", "NOEX",
					new L2MacroCmd[]{new L2MacroCmd(0, 3, 0, 0, ".noexp")});
			player.registerMacro(macro);
			shortcut = new L2ShortCut(9, 0, 4, 1003, 0, 0);
			player.registerShortCut(shortcut);
			// .blockrequests macro
			macro = new L2Macro(1004, 5, "Block Requests", "To block all the requests", "BKTR",
					new L2MacroCmd[]{new L2MacroCmd(0, 3, 0, 0, ".blockrequests")});
			player.registerMacro(macro);
			shortcut = new L2ShortCut(10, 0, 4, 1004, 0, 0);
			player.registerShortCut(shortcut);
			// .refusebuff macro
			macro = new L2Macro(1005, 5, "Refuse Buff", "To refuse other players' buffs", "RFBF",
					new L2MacroCmd[]{new L2MacroCmd(0, 3, 0, 0, ".refusebuff")});
			player.registerMacro(macro);
			shortcut = new L2ShortCut(11, 0, 4, 1005, 0, 0);
			player.registerShortCut(shortcut);
			// .landrates macro
            /*macro = new L2Macro(1006, 5, "Land Rates", "To see the skill land rates", "LDRT", new L2MacroCmd[]{new L2MacroCmd(0, 3, 0, 0, ".landrates")});
			player.registerMacro(macro);
			shortcut = new L2ShortCut(11, 0, 4, 1006, 0, 0);
			player.registerShortCut(shortcut);*/
		}

		if (Config.isServer(Config.TENKAI_ESTHUS))
		{
			// Temp epic jewels
			player.getInventory().addItem("Init", 37732, 1, player, null);
			player.getInventory().addItem("Init", 37733, 1, player, null);
			player.getInventory().addItem("Init", 26469, 1, player, null);
			player.getInventory().addItem("Init", 37734, 1, player, null);
			player.getInventory().addItem("Init", 37735, 1, player, null);
		}
	}
}
