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
package l2tserver.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import l2tserver.Config;
import l2tserver.gameserver.Reloadable;
import l2tserver.gameserver.ReloadableManager;
import l2tserver.gameserver.datatables.CharNameTable;
import l2tserver.gameserver.datatables.CharTemplateTable;
import l2tserver.gameserver.idfactory.IdFactory;
import l2tserver.gameserver.model.L2ItemInstance;
import l2tserver.gameserver.model.L2Party;
import l2tserver.gameserver.model.actor.instance.L2ApInstance;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.model.base.Race;
import l2tserver.gameserver.templates.chars.L2PcTemplate;
import l2tserver.gameserver.templates.chars.L2PcTemplate.PcTemplateItem;
import l2tserver.log.Log;
import l2tserver.util.Rnd;

/**
 * @author Pere
 */
public class ArtificialPlayersManager implements Reloadable
{
	List<L2ApInstance> _players = new ArrayList<L2ApInstance>();
	
	ScheduledFuture<?> _pvpCheck = null;
	List<L2Party> _partiesSent = new ArrayList<L2Party>();
	
	private ArtificialPlayersManager()
	{
		reload();
		ReloadableManager.getInstance().register("aplayers", this);
	}
	
	public static final ArtificialPlayersManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public boolean reload()
	{
		_players.clear();
		
		/*Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con.prepareStatement("SELECT charId FROM characters WHERE account_name LIKE '!'");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				int charId = rset.getInt("charId");
				
				L2ApInstance player = (L2ApInstance)L2World.getInstance().getPlayer(charId);
				
				if (player == null || !player.isOnline())
				{
					player = (L2ApInstance)L2PcInstance.load(charId);
					//player.setOnlineStatus(true, false);
					//player.spawnMe();
				}
				
				//player.setAI(null);
				//player.getAI();
				
				_players.add(player);
			}
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
				e.printStackTrace();
			}
		}
		
		//createRandomParty();
		
		Log.info("Loaded " + _players.size() + " artificial players.");
		
		if (_pvpCheck != null)
			_pvpCheck.cancel(false);
		
		_pvpCheck = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable()
		{
			public void run()
			{
				for (L2Party party : _partiesSent)
				{
					for (L2PcInstance member : party.getPartyMembers())
						member.deleteMe();
				}
				
				_partiesSent.clear();
				
				if (_partiesSent.isEmpty())
					return;
				
				int pvpers = 0;
				Map<Integer, Integer> allies = new HashMap<Integer, Integer>();
				for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
				{
					if (player.getPvpFlag() == 0 || player.isInsideZone(L2Character.ZONE_PEACE)
							|| player.isInsideZone(L2Character.ZONE_SIEGE)
							|| player.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND)
							|| player.getInstanceId() != 0)
						continue;
					
					pvpers++;
					if (player instanceof L2ApInstance)
						continue;
					
					if (!allies.containsKey(player.getAllyId()))
						allies.put(player.getAllyId(), 1);
					else
						allies.put(player.getAllyId(), allies.get(player.getAllyId()) + 1);
				}

				int biggestAlly = 0;
				int smallestAlly = 100;
				for (int members : allies.values())
				{
					if (members > biggestAlly)
						biggestAlly = members;
					if (members < smallestAlly)
						smallestAlly = members;
				}
				
				if (biggestAlly - smallestAlly > 5)
				{
					while (_partiesSent.size() * 7 < biggestAlly)
					{
						L2Party party = createRandomParty();
						for (L2PcInstance member : party.getPartyMembers())
						{
							member.teleToLocation(-20893, 186060, -4103, true);
							member.setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);
							member.startPvPFlag();
						}
						_partiesSent.add(party);
					}
				}
				else if (pvpers < 5)
				{
					while (_partiesSent.size() < 2)
					{
						L2Party party = createRandomParty();
						for (L2PcInstance member : party.getPartyMembers())
						{
							member.teleToLocation(-24501, 187976, -3975, true);
							member.startPvPFlag();
						}
						_partiesSent.add(party);
					}
				}
				else if (_partiesSent.size() > 0 && pvpers > 20)
				{
					L2Party party = _partiesSent.remove(0);
					for (L2PcInstance member : party.getPartyMembers())
						member.deleteMe();
				}
			}
		}, 100000L, 100000L);*/
		
		return true;
	}
	
	/**
	 * Creates a new L2ApInstance based on the given Class ID
	 * @param classId the classId of the AP to create
	 * @return a new (and spawned) L2ApInstance
	 */
	public L2ApInstance createChar(int classId)
	{
		L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(Rnd.get(6) * 2 + Rnd.get(2));

		switch (classId)
		{
			case 139:
			case 141:
			case 145:
			case 146:
				while (template != null && template.race == Race.Kamael)
					template = CharTemplateTable.getInstance().getTemplate(Rnd.get(6) * 2 + Rnd.get(2));
				break;
		}
		
		if (template == null)
			return null;
		
		String name = "";
		switch (classId)
		{
			case 139:
				name = "Sigel";
				break;
			case 140:
				name = "Tyrr";
				break;
			case 141:
				name = "Othell";
				break;
			case 142:
				name = "Yul";
				break;
			case 143:
				name = "Feoh";
				break;
			case 144:
				name = "Iss";
				break;
			case 145:
				name = "Wynn";
				break;
			case 146:
				name = "Aeore";
				break;
		}
		
		int objectId = IdFactory.getInstance().getNextId();
		L2PcInstance newChar = L2PcInstance.create(objectId, template, "!", name, (byte)Rnd.get(5), (byte)Rnd.get(4), (byte)Rnd.get(3), Rnd.get(2) == 0, classId);
		
		newChar.setCurrentHp(newChar.getMaxHp());
		newChar.setCurrentCp(newChar.getMaxCp());
		newChar.setCurrentMp(newChar.getMaxMp());
		
		newChar.addAdena("Init", Config.STARTING_ADENA, null, false);
		
		// TODO: Hardcode template spawns or put them in a config, since now they are all the same for all the templates
		newChar.setXYZInvisible(-114535 + Rnd.get(219), 259818 + Rnd.get(227), -1203);
		
		newChar.setTitle("");
				
		newChar.getStat().addLevel((byte)98);
		
		for (PcTemplateItem ia : template.getItems())
		{
			L2ItemInstance item = newChar.getInventory().addItem("Init", ia.getItemId(), ia.getAmount(), newChar, null);
			
			if (item == null)
			{
				Log.warning("Could not create item during char creation: itemId " + ia.getItemId() + ", amount " + ia.getAmount() + ".");
				continue;
			}
			
			if (item.isEquipable() && ia.isEquipped())
				newChar.getInventory().equipItem(item);
		}
		
		for (int itemId : new int[]{1})
		{
			L2ItemInstance item = newChar.getInventory().addItem("Init", itemId, 1, newChar, null);
			
			if (item == null)
				continue;
			
			if (item.isEquipable())
				newChar.getInventory().equipItem(item);
		}
		
		newChar.giveAvailableSkills(true);
		newChar.store();
		
		L2ApInstance player = (L2ApInstance)L2PcInstance.load(objectId);
		
		if (player == null)
			return null;
		
		//player.setOnlineStatus(true, false);
		//player.spawnMe();
		_players.add(player);
		
		return player;
	}
	
	/**
	 * Creates a party of APlayerInstances
	 * @param classCombination List of ClassIDs
	 */
	public L2Party createParty(List<Integer> classCombination)
	{
		List<L2ApInstance> available = new ArrayList<L2ApInstance>();
		
		for (L2ApInstance player : _players)
		{
			if (player.getParty() == null)
				available.add(player);
		}
		
		List<L2ApInstance> members = new ArrayList<L2ApInstance>();
		
		for (L2ApInstance player : available)
		{
			if (classCombination.contains(player.getClassId()))
			{
				members.add(player);
				classCombination.remove((Integer)player.getClassId());
			}
		}
		
		for (Integer classId : classCombination)
		{
			L2ApInstance newChar = createChar(classId);
			if (newChar != null)
				members.add(newChar);
		}
		
		if (members.size() < 2)
			return null;
		
		L2Party party = new L2Party(members.get(0), L2Party.ITEM_RANDOM);
		
		for (L2ApInstance member : members)
		{
			CharNameTable.getInstance().addName(member);
			member.setOnlineStatus(true, false);
			member.spawnMe();
			member.setAI(null);
			member.getAI();
			party.addPartyMember(member);
			member.setParty(party);
		}
		
		return party;
	}
	
	/**
	 * Create a L2APlayerInstance party with random members
	 * <b>NOTE:</b> In fact, creates a static party ATM D:
	 */
	public L2Party createRandomParty()
	{
		List<Integer> classCombination = new ArrayList<Integer>();
		
		// The healer is the leader
		classCombination.add(146);
		
		// There must be an enchanter too
		classCombination.add(144);
		
		// The rest, purely random for now
		//for (int i = 0; i < 5; i++)
		//	classCombination.add(139 + Rnd.get(5));
		
		// ..or not
		classCombination.add(139);
		classCombination.add(140);
		classCombination.add(141);
		classCombination.add(142);
		classCombination.add(143);
		
		return createParty(classCombination);
	}
	
	public List<L2ApInstance> getAllAPlayers()
	{
		return _players;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ArtificialPlayersManager _instance = new ArtificialPlayersManager();
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return success ? "Artificial players reloaded!" : "Couldn't reload Artificial Players.";
	}
}
