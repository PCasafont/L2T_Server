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

package l2server.gameserver.model.zone;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.MapRegionTable.TeleportWhereType;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Object.InstanceType;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2BoatInstance;
import l2server.gameserver.model.actor.instance.L2NpcInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.network.serverpackets.ExServerPrimitive;
import l2server.gameserver.network.serverpackets.ExStartScenePlayer;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract base class for any zone type
 * Handles basic operations
 *
 * @author durgus
 */
public abstract class L2ZoneType
{
	private final int _id;
	protected L2ZoneForm _zone;
	protected ConcurrentHashMap<Integer, L2Character> _characterList;

	/**
	 * Parameters to affect specific characters
	 */
	private boolean _checkAffected = false;

	private String _name = null;
	private int _minLvl;
	private int _maxLvl;
	private int[] _race;
	private int[] _class;
	private char _classType;
	private Map<Quest.QuestEventType, ArrayList<Quest>> _questEvents;
	private InstanceType _target = InstanceType.L2Character; // default all chars

	protected L2ZoneType(int id)
	{
		_id = id;
		_characterList = new ConcurrentHashMap<>();

		_minLvl = 0;
		_maxLvl = 0xFF;

		_classType = 0;

		_race = null;
		_class = null;
	}

	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * Setup new parameters for this zone
	 *
	 * @param value
	 */
	public void setParameter(String name, String value)
	{
		_checkAffected = true;

		// Zone name
		switch (name)
		{
			case "name":
				_name = value;
				break;
			// Minimum level
			case "affectedLvlMin":
				_minLvl = Integer.parseInt(value);
				break;
			// Maximum level
			case "affectedLvlMax":
				_maxLvl = Integer.parseInt(value);
				break;
			// Affected Races
			case "affectedRace":
				// Create a new array holding the affected race
				if (_race == null)
				{
					_race = new int[1];
					_race[0] = Integer.parseInt(value);
				}
				else
				{
					int[] temp = new int[_race.length + 1];

					int i = 0;
					for (; i < _race.length; i++)
					{
						temp[i] = _race[i];
					}

					temp[i] = Integer.parseInt(value);

					_race = temp;
				}
				break;
			// Affected classes
			case "affectedClassId":
				// Create a new array holding the affected classIds
				if (_class == null)
				{
					_class = new int[1];
					_class[0] = Integer.parseInt(value);
				}
				else
				{
					int[] temp = new int[_class.length + 1];

					int i = 0;
					for (; i < _class.length; i++)
					{
						temp[i] = _class[i];
					}

					temp[i] = Integer.parseInt(value);

					_class = temp;
				}
				break;
			// Affected class type
			case "affectedClassType":
				if (value.equals("Fighter"))
				{
					_classType = 1;
				}
				else
				{
					_classType = 2;
				}
				break;
			case "targetClass":
				_target = Enum.valueOf(InstanceType.class, value);
				break;
			default:
				Log.info(getClass().getSimpleName() + ": Unknown parameter - " + name + " in zone: " + getId());
				break;
		}
	}

	/**
	 * Checks if the given character is affected by this zone
	 *
	 * @param character
	 * @return
	 */
	private boolean isAffected(L2Character character)
	{
		// Check lvl
		if (character.getLevel() < _minLvl || character.getLevel() > _maxLvl)
		{
			return false;
		}

		// check obj class
		if (!character.isInstanceType(_target))
		{
			return false;
		}

		if (character instanceof L2PcInstance)
		{
			// Check class type
			if (_classType != 0)
			{
				if (((L2PcInstance) character).isMageClass())
				{
					if (_classType == 1)
					{
						return false;
					}
				}
				else if (_classType == 2)
				{
					return false;
				}
			}

			// Check race
			if (_race != null)
			{
				boolean ok = false;

				for (int element : _race)
				{
					if (((L2PcInstance) character).getRace().ordinal() == element)
					{
						ok = true;
						break;
					}
				}

				if (!ok)
				{
					return false;
				}
			}

			// Check class
			if (_class != null)
			{
				boolean ok = false;

				for (int _clas : _class)
				{
					if (((L2PcInstance) character).getCurrentClass().getId() == _clas)
					{
						ok = true;
						break;
					}
				}

				if (!ok)
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Set the zone for this L2ZoneType Instance
	 *
	 * @param zone
	 */
	public void setZone(L2ZoneForm zone)
	{
		if (_zone != null)
		{
			throw new IllegalStateException("Zone already set");
		}
		_zone = zone;
	}

	/**
	 * Returns this zones zone form
	 *
	 * @return
	 */
	public L2ZoneForm getZone()
	{
		return _zone;
	}

	/**
	 * Set the zone name.
	 *
	 * @param name
	 */
	public void setName(String name)
	{
		_name = name;
	}

	/**
	 * Returns zone name
	 *
	 * @return
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * Checks if the given coordinates are within zone's plane
	 *
	 * @param x
	 * @param y
	 */
	public boolean isInsideZone(int x, int y)
	{
		return _zone.isInsideZone(x, y, _zone.getHighZ());
	}

	/**
	 * Checks if the given coordinates are within the zone
	 *
	 * @param x
	 * @param y
	 * @param z
	 */
	public boolean isInsideZone(int x, int y, int z)
	{
		return _zone.isInsideZone(x, y, z);
	}

	/**
	 * Checks if the given object is inside the zone.
	 *
	 * @param object
	 */
	public boolean isInsideZone(L2Object object)
	{
		return isInsideZone(object.getX(), object.getY(), object.getZ());
	}

	public double getDistanceToZone(int x, int y)
	{
		return getZone().getDistanceToZone(x, y);
	}

	public double getDistanceToZone(L2Object object)
	{
		return getZone().getDistanceToZone(object.getX(), object.getY());
	}

	public void revalidateInZone(L2Character character)
	{
		// If the character can't be affected by this zone return
		if (_checkAffected)
		{
			if (!isAffected(character))
			{
				return;
			}
		}

		// If the object is inside the zone...
		if (isInsideZone(character.getX(), character.getY(), character.getZ()))
		{
			// Was the character not yet inside this zone?
			if (!_characterList.containsKey(character.getObjectId()))
			{
				ArrayList<Quest> quests = getQuestByEvent(Quest.QuestEventType.ON_ENTER_ZONE);
				if (quests != null)
				{
					for (Quest quest : quests)
					{
						quest.notifyEnterZone(character, this);
					}
				}

				_characterList.put(character.getObjectId(), character);
				onEnter(character);
			}
		}
		else
		{
			// Was the character inside this zone?
			if (_characterList.containsKey(character.getObjectId()))
			{
				ArrayList<Quest> quests = getQuestByEvent(Quest.QuestEventType.ON_EXIT_ZONE);
				if (quests != null)
				{
					for (Quest quest : quests)
					{
						quest.notifyExitZone(character, this);
					}
				}
				_characterList.remove(character.getObjectId());
				onExit(character);
			}
		}
	}

	/**
	 * Force fully removes a character from the zone
	 * Should use during teleport / logoff
	 *
	 * @param character
	 */
	public void removeCharacter(L2Character character)
	{
		if (_characterList.containsKey(character.getObjectId()))
		{
			ArrayList<Quest> quests = getQuestByEvent(Quest.QuestEventType.ON_EXIT_ZONE);
			if (quests != null)
			{
				for (Quest quest : quests)
				{
					quest.notifyExitZone(character, this);
				}
			}
			_characterList.remove(character.getObjectId());
			onExit(character);
		}
	}

	/**
	 * Will scan the zones char list for the character
	 *
	 * @param character
	 * @return
	 */
	public boolean isCharacterInZone(L2Character character)
	{
		return _characterList.containsKey(character.getObjectId());
	}

	protected abstract void onEnter(L2Character character);

	protected abstract void onExit(L2Character character);

	public void onDieInside(L2Character character, L2Character killer)
	{
		if (_characterList.containsKey(character.getObjectId()))
		{
			ArrayList<Quest> quests = getQuestByEvent(Quest.QuestEventType.ON_DIE_ZONE);
			if (quests != null)
			{
				for (Quest quest : quests)
				{
					quest.notifyDieZone(character, killer, this);
				}
			}
		}
	}

	public abstract void onReviveInside(L2Character character);

	public ConcurrentHashMap<Integer, L2Character> getCharactersInside()
	{
		return _characterList;
	}

	public void addQuestEvent(Quest.QuestEventType EventType, Quest q)
	{
		if (_questEvents == null)
		{
			_questEvents = new HashMap<>();
		}
		ArrayList<Quest> questByEvents = _questEvents.get(EventType);
		if (questByEvents == null)
		{
			questByEvents = new ArrayList<>();
		}
		if (!questByEvents.contains(q))
		{
			questByEvents.add(q);
		}
		_questEvents.put(EventType, questByEvents);
	}

	public ArrayList<Quest> getQuestByEvent(Quest.QuestEventType EventType)
	{
		if (_questEvents == null)
		{
			return null;
		}
		return _questEvents.get(EventType);
	}

	/**
	 * Broadcasts packet to all players inside the zone
	 */
	public void broadcastPacket(L2GameServerPacket packet)
	{
		if (_characterList.isEmpty())
		{
			return;
		}

		for (L2Character character : _characterList.values())
		{
			if (character != null && character instanceof L2PcInstance)
			{
				character.sendPacket(packet);
			}
		}
	}

	public InstanceType getTargetType()
	{
		return _target;
	}

	public void setTargetType(InstanceType type)
	{
		_target = type;
		_checkAffected = true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [" + _id + "]";
	}

	public void visualizeZone(L2PcInstance viewer)
	{
		ExServerPrimitive packet = new ExServerPrimitive(toString());
		getZone().visualizeZone(packet, toString(), viewer.getZ() + 20);
		viewer.sendPacket(packet);
	}

	public void unVisualizeZone(L2PcInstance viewer)
	{
		viewer.sendPacket(new ExServerPrimitive(toString()));
	}

	public List<L2PcInstance> getPlayersInside()
	{
		List<L2PcInstance> players = new ArrayList<>();
		for (L2Character ch : _characterList.values())
		{
			if (ch != null && ch instanceof L2PcInstance)
			{
				players.add(ch.getActingPlayer());
			}
		}

		return players;
	}

	public List<L2Npc> getNpcsInside()
	{
		List<L2Npc> npcs = new ArrayList<>();
		for (L2Character ch : _characterList.values())
		{
			if (ch == null || ch instanceof L2Playable || ch instanceof L2BoatInstance ||
					!(ch instanceof L2Attackable) && !(ch instanceof L2NpcInstance))
			{
				continue;
			}

			npcs.add((L2Npc) ch);
		}

		return npcs;
	}

	public void showVidToZone(int vidId)
	{
		stopWholeZone();

		broadcastMovie(vidId);

		ThreadPoolManager.getInstance().scheduleGeneral(this::startWholeZone,
				ScenePlayerDataTable.getInstance().getVideoDuration(vidId) + 1000);
	}

	public void stopWholeZone()
	{
		for (L2Character ch : _characterList.values())
		{
			if (ch == null)
			{
				continue;
			}

			ch.setTarget(null);
			ch.abortAttack();
			ch.abortCast();
			ch.disableAllSkills();
			ch.stopMove(null);
			ch.setIsInvul(true);
			ch.setIsImmobilized(true);
			ch.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}

	public void broadcastMovie(int vidId)
	{
		for (L2PcInstance pl : getPlayersInside())
		{
			if (pl == null)
			{
				continue;
			}

			pl.setMovieId(vidId);
			pl.sendPacket(new ExStartScenePlayer(vidId));
		}
	}

	public void startWholeZone()
	{
		for (L2Character ch : _characterList.values())
		{
			if (ch == null)
			{
				continue;
			}

			ch.enableAllSkills();
			ch.setIsInvul(false);
			ch.setIsImmobilized(false);
		}
	}

	public void sendDelayedPacketToZone(final int delayMsSec, final L2GameServerPacket packet)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(() -> broadcastPacket(packet), delayMsSec);
	}

	public void oustAllPlayers()
	{
		if (_characterList.isEmpty())
		{
			return;
		}

		for (L2Character character : _characterList.values())
		{
			if (character == null)
			{
				continue;
			}

			if (character instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) character;
				if (player.isOnline())
				{
					player.teleToLocation(TeleportWhereType.Town);
				}
			}
		}
	}
}
