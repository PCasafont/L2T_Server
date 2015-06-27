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
package l2server.gameserver.bots;

import gnu.trove.TIntIntHashMap;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.bots.controllers.BotController;
import l2server.gameserver.bots.controllers.FighterController;
import l2server.gameserver.bots.controllers.MageController;
import l2server.gameserver.bots.controllers.awakened.AFKController;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.datatables.CharTemplateTable;
import l2server.gameserver.datatables.EnchantItemTable;
import l2server.gameserver.datatables.HennaTable;
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.datatables.SkillTreeTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2SkillLearn;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.Experience;
import l2server.gameserver.model.base.PlayerClass;
import l2server.gameserver.model.base.Race;
import l2server.gameserver.templates.chars.L2PcTemplate;
import l2server.gameserver.templates.chars.L2PcTemplate.PcTemplateItem;
import l2server.gameserver.templates.item.L2Henna;
import l2server.log.Log;
import l2server.util.Rnd;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

/**
 * @author LittleHakor
 */

public class BotsManager
{

	private static Map<Integer, BotClass> _botClasses					= new HashMap<Integer, BotClass>();
	private static ArrayList<String> _allAvailableBots 					= new ArrayList<String>();		//Character names that are already created ready to be logged
	private static Map<String, List<L2PcInstance>> _preparedBots 		= new HashMap<String, List<L2PcInstance>>();	//List with the bots to use on different features
	private static Map<Integer, ItemSets> _weaponSetItems 				= new HashMap<Integer, ItemSets>();
	private static Map<Integer, ItemSets> _armorSetItems 				= new HashMap<Integer, ItemSets>();
	private static Map<Integer, ItemSets> _hatSetItems 					= new HashMap<Integer, ItemSets>();
	private static Map<Integer, ItemSets> _jewelsSetItems 				= new HashMap<Integer, ItemSets>();
	private static Map<Integer, ItemSets> _randomSetItems 				= new HashMap<Integer, ItemSets>();
	private static Map<Integer, ItemSets> _broochesSetItems				= new HashMap<Integer, ItemSets>();
	private static Map<Integer, ItemSets> _talismansSetItems 			= new HashMap<Integer, ItemSets>();
	private static Map<Integer, ClassItems> _allClassItems 				= new HashMap<Integer, ClassItems>();
	private static Map<Integer, Map<Integer, Integer>> _abilitiesPresets		 = new HashMap<Integer, Map<Integer, Integer>>();
	private static Map<Integer, ArrayList<String>> _allBotNamesByClass 	= new HashMap<Integer, ArrayList<String>>();
	
	private List<String> _availableNames;

	public BotsManager()
	{
		init();
	}
	
	private final void init()
	{
		try
		{
			loadClasses();
			loadEquipments();
			loadBotNames();
			loadBots();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private final void loadClasses()
	{
		File file = new File(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "bots/classes.xml");
		XmlDocument doc = new XmlDocument(file);
		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("class"))
					{
						int classId = d.getInt("id");
						String className = d.getString("className");
						Race race = Race.valueOf(d.getString("classRace"));
						int classRaceId = d.getInt("classRaceId");
						boolean isAvailable = d.getBool("isAvailable");
						if (isAvailable)
							_botClasses.put(classId, new BotClass(classId, className, race, classRaceId));
					}
				}
			}
		}
		Log.info("BotsManager: loaded " + _botClasses.size() + " bot classes!");
	}
	
	private final void loadEquipments()
	{
		File file = new File(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "bots/Equipments.xml");
		XmlDocument doc = new XmlDocument(file);
		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("jewelSets") || d.getName().equalsIgnoreCase("randomSetItems") || d.getName().equalsIgnoreCase("hatSetItems") ||
							d.getName().equalsIgnoreCase("weaponSets") || d.getName().equalsIgnoreCase("armorSets") || d.getName().equalsIgnoreCase("broochesSetItems") ||
							d.getName().equalsIgnoreCase("talismanSetItems"))
					{
						for (XmlNode b : d.getChildren())
						{
							if (b.getName().equalsIgnoreCase("set"))
							{
								int setId = b.getInt("id");
								List<Integer> setParts = new ArrayList<Integer>();
								for (XmlNode c : b.getChildren())
								{
									int partId = c.getInt("id");
									setParts.add(partId);
								}
								if (d.getName().equalsIgnoreCase("randomSetItems"))
									_randomSetItems.put(setId, new ItemSets(setId, setParts));
								if (d.getName().equalsIgnoreCase("hatSetItems"))
									_hatSetItems.put(setId, new ItemSets(setId, setParts));
								if (d.getName().equalsIgnoreCase("jewelSets"))
									_jewelsSetItems.put(setId, new ItemSets(setId, setParts));
								if (d.getName().equalsIgnoreCase("weaponSets"))
									_weaponSetItems.put(setId, new ItemSets(setId, setParts));
								if(d.getName().equalsIgnoreCase("armorSets"))
									_armorSetItems.put(setId, new ItemSets(setId, setParts));
								if (d.getName().equalsIgnoreCase("broochesSetItems"))
									_broochesSetItems.put(setId, new ItemSets(setId, setParts));
								if (d.getName().equalsIgnoreCase("talismanSetItems"))
									_talismansSetItems.put(setId, new ItemSets(setId, setParts));
							}
						}
					}
					else if (d.getName().equalsIgnoreCase("abilitySkills"))
					{
						for (XmlNode b : d.getChildren())
						{
							if (b.getName().equalsIgnoreCase("set"))
							{
								int setId = b.getInt("id");
								Map<Integer, Integer> skillInfo = new HashMap<Integer, Integer>();
								for (XmlNode c : b.getChildren())
								{
									int skillId = c.getInt("id");
									int skillLevel = c.getInt("level");
									skillInfo.put(skillId, skillLevel);
								}
								_abilitiesPresets.put(setId, skillInfo);
							}
						}
					}
					else if (d.getName().equalsIgnoreCase("classItems"))
					{
						for (XmlNode b : d.getChildren())
						{
							if (b.getName().equalsIgnoreCase("class"))
							{
								int classId = b.getInt("id");
								Map<Integer, List<Integer>> weaponItems = new HashMap<Integer, List<Integer>>();
								Map<Integer, List<Integer>> armorItems = new HashMap<Integer, List<Integer>>();
								Map<Integer, Integer> dyes = new HashMap<Integer, Integer>();
								int hatId = 0;
								int broochSetId = 0;
								int abilitiesSchemeId = 0;
								
								for (XmlNode c : b.getChildren())
								{
									if (c.getName().equalsIgnoreCase("dye"))
									{
										int symbolId = c.getInt("id", 0);
										int symbolCount = c.getInt("count", 1);
										if (symbolId != 0)
											dyes.put(symbolId, symbolCount);
									}
									else if (c.getName().equalsIgnoreCase("presetAbilities"))
									{
										abilitiesSchemeId = c.getInt("id");
									}
									else if (c.getName().equalsIgnoreCase("brooches"))
									{
										broochSetId = c.getInt("id");
									}
									else if (c.getName().equalsIgnoreCase("weapon"))
									{
										int itemId = c.getInt("id");
										int itemClassId = c.getInt("classId", -1);
										if (weaponItems.get(itemClassId) == null)
										{	
											List<Integer> items = new ArrayList<Integer>();
											items.add(itemId);
											
											weaponItems.put(itemClassId, items);
										}
										else
											weaponItems.get(itemClassId).add(itemId);
									}
									else if (c.getName().equalsIgnoreCase("hat"))
									{
										hatId = c.getInt("id");
									}
									else if (c.getName().equalsIgnoreCase("armorSet"))
									{
										int itemId = c.getInt("id");
										if (armorItems.get(-1) == null)
										{	
											List<Integer> items = new ArrayList<Integer>();
											items.add(itemId);
											
											armorItems.put(-1, items);
										}
										else
											armorItems.get(-1).add(itemId);
									}
								}
								_allClassItems.put(classId, new ClassItems(classId, armorItems, weaponItems, hatId, dyes, broochSetId, abilitiesSchemeId));
							}
						}
					}
				}
			}
		}
		Log.info("BotsManager: Loaded " + _allClassItems.size() + " class items!");
	}
	
	private class BotClass
	{
		private int _classId;
		private String _className;
		private Race _classRace;
		private int _classRaceId;
		
		private BotClass(int classId, String className, Race race, int classRaceId)
		{
			_classId = classId;
			_className = className;
			_classRace = race;
			_classRaceId = classRaceId;
		}
		
		private int getRaceClassId()
		{
			return _classRaceId;
		}
		
		private int getRaceId()
		{
			return _classRace.ordinal();
		}

		@SuppressWarnings("unused") // TODO
		private String getClassName()
		{
			return _className;
		}
		
		private int getClassId()
		{
			return _classId;
		}
	}
	
	private class ItemSets
	{
		private int _setId;
		private List<Integer> _setItems;
		
		private ItemSets(int setId, List<Integer> setItems)
		{
			_setId = setId;
			_setItems = setItems;
		}

		@SuppressWarnings("unused") // TODO
		private int getSetId()
		{
			return _setId;
		}
		
		private List<Integer> getSetItems()
		{
			return _setItems;
		}
	}
	
	private class ClassItems
	{
		private int _classId; //Base awaken class id
		private Map<Integer, Integer> _dyes;
		private Map<Integer, List<Integer>> _armorItems;
		private Map<Integer, List<Integer>> _weaponItems;
		private int _hatSetId;
		private int _broochesItems;
		private int _abilitiesSchemeId;
		
		private ClassItems(int classId, Map<Integer, List<Integer>> armorItems, Map<Integer, List<Integer>> weaponItems, int hatSetId, Map<Integer, Integer> dyes, int broochesItems, int abilitiesSchemeId)
		{
			_classId = classId;
			_armorItems = armorItems;
			_weaponItems = weaponItems;
			_hatSetId = hatSetId;
			_dyes = dyes;
			_broochesItems = broochesItems;
			_abilitiesSchemeId = abilitiesSchemeId;
		}
		
		@SuppressWarnings("unused") // TODO
		private int getClassId()
		{
			return _classId;
		}
		
		private Map<Integer, Integer> getDyes()
		{
			return _dyes;
		}
		
		private List<Integer> getRandomArmorItems()
		{
			int randomSet = _armorItems.get(-1).get(Rnd.get(_armorItems.get(-1).size()));	//Select one random set
			return _armorSetItems.get(randomSet).getSetItems();
		}

		private int getRandomWeaponItems(PlayerClass playerClass)
		{
			int classId = playerClass.getId();
			if (_weaponItems.get(classId) == null)
				classId = playerClass.getAwakeningClassId();
			
			int randomSet = _weaponItems.get(classId).get(Rnd.get(_weaponItems.get(classId).size()));	//Select one random weapon
			return _weaponSetItems.get(randomSet).getSetItems().get(Rnd.get(_weaponSetItems.get(randomSet).getSetItems().size()));
		}

		@SuppressWarnings("unused")
		private int getRandomHatItems()
		{
			return _hatSetItems.get(_hatSetId).getSetItems().get(Rnd.get(_hatSetItems.get(_hatSetId).getSetItems().size()));
		}

		private List<Integer> getRandomBroochesLevels()
		{
			//From our brooches set return random levels
			List<Integer> boorhces = new ArrayList<Integer>();
			for (int broochId : _broochesSetItems.get(_broochesItems).getSetItems())
			{
				boorhces.add((broochId + Rnd.get(5)));
			}
			return boorhces;
		}
		
		private int getAbilitiesSchemeId()
		{
			return _abilitiesSchemeId;
		}
	}
	
	private final void loadBotNames()
	{
		// We start by loading the list of names...
		String filePath = Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "bots/Names.txt";
		try
		{
			_availableNames = Files.readAllLines(Paths.get(filePath), StandardCharsets.ISO_8859_1);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private final void loadBots()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			
			ResultSet rs = null;
			statement = con.prepareStatement("SELECT char_name FROM characters WHERE account_name = ? ORDER BY RAND()");
			statement.setString(1, "!");
			
			rs = statement.executeQuery();
			while (rs.next())
			{
				String botName = rs.getString("char_name");
				if (_allAvailableBots.contains(botName))
					continue;
				
				_allAvailableBots.add(botName);
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, "", e);
			}
		}
		Log.info("BotsManager: We've " + _allAvailableBots.size() + " bots ready to be used!");
	}
	
	public final L2PcInstance loadPlayer(final String name, final BotMode mode)
	{
		final int playerId = CharNameTable.getInstance().getIdByName(name);
		if (playerId == 0)
		{
			Log.info("FakePlayerController: " + name + " couldn't be found.");
			return null;
		}
		
		final L2PcInstance player = L2PcInstance.load(playerId);
		if (player == null)
		{
			Log.info("FakePlayerController: " + name + " couldn't be loaded.");
			return null;
		}
		
		logPlayer(player, false, mode);
		
		return player;
	}
	
	public final String getBotName()
	{
		// Randomize elements order.
		Collections.shuffle(_allAvailableBots);
		//Log.info("There are " + _allBotNames.size() + " bot names available.");
		for (int i = _allAvailableBots.size(); i-- > 0;)
		{
			final String botName = _allAvailableBots.get(i);
			
			if (L2World.getInstance().getPlayer(botName) != null)
				continue;
			
			return botName;
		}
		return null;
	}
	
	public final String getBotNameByClass(final int classId)
	{
		if (!_allBotNamesByClass.containsKey(classId))
			return null;
		
		ArrayList<String> availableBots = _allBotNamesByClass.get(classId);
		
		// Randomize elements order.
		Collections.shuffle(availableBots);
		
		Log.info("There are " + availableBots.size() + " bots available for the class " + classId + ".");
		
		for (int i = availableBots.size(); i-- > 0;)
		{
			final String botName = availableBots.get(i);
			
			if (L2World.getInstance().getPlayer(botName) != null)
				continue;
			
			return botName;
		}
		
		return null;
	}
	
	public final String[] getBotNamesByClass(final int classId, final int botAmount)
	{
		if (!_allBotNamesByClass.containsKey(classId))
			return null;
		
		ArrayList<String> availableBots = _allBotNamesByClass.get(classId);
		
		// Randomize elements order.
		Collections.shuffle(availableBots);
		
		Log.info("There are " + availableBots.size() + " bots available for the class " + classId + ".");
		
		ArrayList<String> result = new ArrayList<String>();
		for (int i = availableBots.size(); i-- > 0;)
		{
			final String botName = availableBots.get(i);
			
			if (L2World.getInstance().getPlayer(botName) != null)
				continue;
			
			result.add(botName);
			
			if (result.size() == botAmount)
				break;
		}
		
		return result.toArray(new String[result.size()]);
	}
	
	public final String getRandomName()
	{
		return _availableNames.get(Rnd.get(_availableNames.size()));
	}
	
	public BotClass getBotClassById(int classId)
	{
		return _botClasses.get(classId);
	}
	
	public final BotController initControllerFor(final L2PcInstance player, BotMode botMode)
	{
		Class<?> botControllerClass = null;
		
		BotClass botClass = _botClasses.get(player.getClassId());
		if (botClass == null)
		{
			Log.info("BotsManager: Cant locate the botclass for the class id " + player.getClassId());
			return null;
		}
		
		String classPackage = "l2server.gameserver.bots.controllers.awakened.";
		int baseAwakeClass = player.getCurrentClass().getParent().getAwakeningClassId();
		String baseClassName = PlayerClassTable.getInstance().getClassNameById(baseAwakeClass);
		final String className = baseClassName.replace(" ", "") + "Controller";
		
		try
		{
			if (botMode == BotMode.AFKER)
				botControllerClass = AFKController.class;
			else
				botControllerClass = Class.forName(classPackage + className);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		Object botController = null;
		try
		{
			Constructor<?> constructor = botControllerClass.getConstructor(L2PcInstance.class);
			botController = constructor.newInstance(player);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			if (player.isMage())
				botController = new MageController(player);
			else
				botController = new FighterController(player);
		}
		
		return (BotController) botController;
	}
	
	public final void logPlayer(final L2PcInstance player, boolean isHumanBehind, final BotMode mode)
	{
		BotController botController = initControllerFor(player, mode);
		if (botController == null)
			return;
		
		player.setBotController(botController);
		botController.setMode(mode);
		botController.setCurrentWeapon(player.getActiveWeaponInstance());
		player.getBotController().onEnterWorld(false);
		player.setOnlineStatus(true, true);
	}
	
	public final void logOutPlayer(L2PcInstance player)
	{
		for (Entry<String, List<L2PcInstance>> i : _preparedBots.entrySet())
		{
			if (i.getValue().contains(player))
			{
				i.getValue().remove(player);
				Log.info("BotsManager: Deleted " + player.getName() + " from " + i.getKey());
				break;
			}
		}
		player.setOnlineStatus(false, true);
	}
	
	public final L2PcInstance createPlayer(final BotType botType, final int classId, BotMode botMode)
	{
		L2PcInstance newChar = null;
		L2PcTemplate template = null;
		
		String name = "";
		
		int raceId = 0;
		int baseClassId = 0;
		int finalClassId = classId;
		if (classId == -1)
		{
			L2PcTemplate temp = CharTemplateTable.getInstance().getTemplate(Rnd.get(9));
			raceId = temp.race.ordinal();
			baseClassId = temp.startingClassId;
			finalClassId = baseClassId;
		}
		else
		{
			BotClass botClass = _botClasses.get(classId);
			raceId = botClass.getRaceId();
			baseClassId = botClass.getRaceClassId();
		}
		
		//final String className = (String) classData[1];
		
		int selectedSex = Rnd.get(0, 1);
		switch (finalClassId)
		{
			case 157: // Tyrr Doombringers, Males only.
				selectedSex = 0;
				break;
			case 165: // Yul Trickster, Females only.
				selectedSex = 1;
				break;
		}
		
		int baseTemplateId = 0;
		for (int i = 0; i < 11; i++)
		{
			int startingClassId = CharTemplateTable.getInstance().getTemplate(i).startingClassId;
			if (raceId == 5 && selectedSex == 1)
				startingClassId++;
			if (startingClassId == baseClassId)
			{
				baseTemplateId = i;
				template = CharTemplateTable.getInstance().getTemplate(i);
				break;
			}
		}
		
		if (template == null)
		{
			Log.severe("BotsManager: Character creation failed for ClassId[" + finalClassId + "]... couldn't find template.");
			return null;
		}
		
		int maxHairStyle = selectedSex == 0 ? 4 : 6;
		int maxHairColor = template.race == Race.Kamael ? 2 : 3;
		int maxFace = 2;
		
		byte selectedHairStyle = (byte) Rnd.get(0, maxHairStyle);
		byte selectedHairColor = (byte) Rnd.get(0, maxHairColor);
		byte selectedFace = (byte) Rnd.get(0, maxFace);
		
		int lastNumber = 0;
		while (true)
		{
			name = botType == BotType.TESTER ? "" + lastNumber++ : getRandomName();
			synchronized (CharNameTable.getInstance())
			{
				if (CharNameTable.getInstance().doesCharNameExist(name))
				{
					Log.fine("Character Creation Failure: Message generated: You cannot create another character. Please delete the existing character and try again.");
					continue;
				}
				
				template = CharTemplateTable.getInstance().getTemplate(baseTemplateId);
				if (template == null)
				{
					Log.fine("Character Creation Failure: " + name + " classId: " + baseClassId + " Template: " + template + " Message generated: Your character creation has failed.");
					continue;
				}
				
				int objectId = IdFactory.getInstance().getNextId();
				newChar = L2PcInstance.create(objectId, template, "!", name, selectedHairStyle, selectedHairColor, selectedFace, selectedSex != 0, finalClassId);
			}
			
			// Config
			int lvl = Rnd.get(95, Config.MAX_LEVEL);
			int equipChance = 100;
			int minEnchant = 7;
			int maxEnchant = 11;
			int abilitiesChance = 80;
			
			long allExpToAdd = Experience.getAbsoluteExp(lvl) - newChar.getExp();
			newChar.getStat().addSp(Config.STARTING_SP);
			newChar.addExpAndSp(allExpToAdd, 999999999);
			newChar.giveAvailableSkills(true);
			newChar.setCurrentMp(newChar.getMaxMp());
			newChar.setCurrentHp(newChar.getMaxHp());
			newChar.setCurrentCp(newChar.getMaxCp());

			
			L2World.getInstance().storeObject(newChar);
			
			template = newChar.getTemplate();
			
			newChar.addAdena("Init", Config.STARTING_ADENA, null, false);
			newChar.setXYZInvisible(-113497, -244498, -15540);
			newChar.setTitle("");
			
			//Items
			int preWaken = newChar.getCurrentClass().getParent().getAwakeningClassId();
			ClassItems classInfo = _allClassItems.get(preWaken);
			if (classInfo == null)
			{
				Log.info("BotsManager: Cant locate the class items for the class id " + preWaken);
				continue;
			}

			List<Integer> allItems = new ArrayList<Integer>();
			if (Rnd.get(100) < equipChance)
			{
				allItems.addAll(classInfo.getRandomArmorItems());	//Armor
				allItems.add(classInfo.getRandomWeaponItems(newChar.getCurrentClass())); //Weapon
				allItems.addAll(_jewelsSetItems.get(Rnd.get(_jewelsSetItems.size()) + 1).getSetItems());	//Jewels
				allItems.addAll(_randomSetItems.get(Rnd.get(_randomSetItems.size()) + 1).getSetItems()); //Random items; shirt belt..
				//allItems.add(classInfo.getRandomHatItems()); //Random hat
				allItems.addAll(classInfo.getRandomBroochesLevels());
				allItems.addAll(_talismansSetItems.get(1).getSetItems());
				
				//Cloak
				int cloakId = 30171 + preWaken;
				if (preWaken == 144)
					cloakId++;
				else if (preWaken == 145)
					cloakId--;
				allItems.add(cloakId);
			}
			else
			{
				for (PcTemplateItem ia : template.getItems())
					allItems.add(ia.getItemId());
			}
			
			//Equip
			for (int itemId : allItems)
			{
				L2ItemInstance item = newChar.getInventory().addItem("Init", itemId, 1, newChar, null);
				if (item == null)
					continue;
				
				if (EnchantItemTable.isEnchantable(item))
					item.setEnchantLevel(Rnd.get(minEnchant, maxEnchant));
				
				if (item.isEquipable())
					newChar.getInventory().equipItem(item);
			}
			
			//Dyes
			Map<Integer, Integer> dyesToAdd = classInfo.getDyes();
			if (dyesToAdd != null)
			{
				for (Entry<Integer, Integer> symbol : dyesToAdd.entrySet())
				{
					L2Henna henna = HennaTable.getInstance().getTemplate(symbol.getKey());
					if (henna == null)
					{
						Log.info("BotsManager: Cant locate the symbol id " + symbol.getKey());
						continue;
					}
					for (int count = 0; count < symbol.getValue(); count ++)
						newChar.addHenna(henna);
				}
			}
			
			if (Rnd.get(100) < abilitiesChance)
			{
				//Abilities
				TIntIntHashMap abList = new TIntIntHashMap();
				for (Entry<Integer, Integer> ab : _abilitiesPresets.get(classInfo.getAbilitiesSchemeId()).entrySet())
				{
					abList.put(ab.getKey(), ab.getValue());
				}
				newChar.setAbilities(abList);
			}
			
			for (L2SkillLearn skill : SkillTreeTable.getInstance().getAvailableClassSkills(newChar))
			{
				if (skill.getMinLevel() > 1)
					continue;
				
				newChar.addSkill(SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel()), true);
				if (Config.DEBUG)
					Log.fine("Adding starter skill:" + skill.getId() + " / " + skill.getLevel());
			}
			
			newChar.setOnlineStatus(true, false);
			newChar.deleteMe();
			break;
		}
		
		if (classId == -1)
			return null;
		
		// Character is created. Let's log him in, increase his level to 99 and learn all skills.
		final L2PcInstance player = loadPlayer(name, botMode);
		
		return player;
	}
	
	private static BotsManager _instance;
	
	public static BotsManager getInstance()
	{
		if (_instance == null)
			_instance = new BotsManager();
		
		return _instance;
	}

	public List<L2PcInstance> getBots(String usage)
	{
		return _preparedBots.get(usage);
	}
	
	public void prepareBots(int amountOfBots, int level, String usage, BotMode botMode)
	{
		int currentAddedBots = 0;
		List<L2PcInstance> bots = new ArrayList<L2PcInstance>();
		if (!_allAvailableBots.isEmpty() && botMode != BotMode.AFKER)	//Always new bots for afkers
		{
			//TODO check if is not used at other list?
			for (String name : _allAvailableBots)
			{
				if (currentAddedBots == amountOfBots)
					break;
				
				if (L2World.getInstance().getPlayer(name) != null)
					continue;
					
				L2PcInstance bot = loadPlayer(name, botMode);
				if (bot != null)
				{	
					bot.getBotController().stopController();
					bots.add(bot);
					currentAddedBots++;
				}
			}
		}
		
		List<Integer> randomClass = new ArrayList<Integer>();
		randomClass.addAll(_botClasses.keySet());
		
		int currentSize = bots.size();
		if (currentSize < amountOfBots)
		{
			//Log.info("BotsManager: We need create " + (amountOfBots - currentSize) + " bots more for " + usage);
			while (currentSize < amountOfBots)
			{
				BotClass botClass = _botClasses.get(randomClass.get(Rnd.get(randomClass.size())));
				if (botClass == null)
					continue;
				
				if (amountOfBots == currentSize)
					break;
				
				L2PcInstance bot = createPlayer(BotType.REGULAR, botClass.getClassId(), botMode);
				if (bot != null)
				{	
					bot.getBotController().stopController();
					bots.add(bot);
					currentSize++;
				}
			}
		}
		
		//Be sure the bots are waiting at secure place
		for (L2PcInstance bot : bots)
		{
			if (bot != null)
				bot.teleToLocation(-114398, -244920, -15541);
		}
		
		//Add the current under-use bots to the list too...
		if(_preparedBots.containsKey(usage))
			bots.addAll(_preparedBots.get(usage));
		
		_preparedBots.put(usage, bots);
		
		//Log.info("BotsManager: " + _preparedBots.get(usage).size() + " bots ready to be used on "+usage+"!");
	}
}