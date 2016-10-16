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
import lombok.Getter;
import lombok.Setter;

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
	@Getter private final int id;
	protected L2ZoneForm zone;
	protected ConcurrentHashMap<Integer, L2Character> characterList;

	/**
	 * Parameters to affect specific characters
	 */
	private boolean checkAffected = false;

	@Getter @Setter private String name = null;
	private int minLvl;
	private int maxLvl;
	private int[] race;
	private int[] clazz;
	private char classType;
	private Map<Quest.QuestEventType, ArrayList<Quest>> questEvents;
	private InstanceType target = InstanceType.L2Character; // default all chars

	protected L2ZoneType(int id)
	{
		this.id = id;
		characterList = new ConcurrentHashMap<>();

		minLvl = 0;
		maxLvl = 0xFF;

		classType = 0;

		race = null;
		clazz = null;
	}

	/**
	 * Setup new parameters for this zone
	 *
	 * @param value
	 */
	public void setParameter(String name, String value)
	{
		checkAffected = true;

		// Zone name
		switch (name)
		{
			case "name":
				this.name = value;
				break;
			// Minimum level
			case "affectedLvlMin":
				minLvl = Integer.parseInt(value);
				break;
			// Maximum level
			case "affectedLvlMax":
				maxLvl = Integer.parseInt(value);
				break;
			// Affected Races
			case "affectedRace":
				// Create a new array holding the affected race
				if (race == null)
				{
					race = new int[1];
					race[0] = Integer.parseInt(value);
				}
				else
				{
					int[] temp = new int[race.length + 1];

					int i = 0;
					for (; i < race.length; i++)
					{
						temp[i] = race[i];
					}

					temp[i] = Integer.parseInt(value);

					race = temp;
				}
				break;
			// Affected classes
			case "affectedClassId":
				// Create a new array holding the affected classIds
				if (clazz == null)
				{
					clazz = new int[1];
					clazz[0] = Integer.parseInt(value);
				}
				else
				{
					int[] temp = new int[clazz.length + 1];

					int i = 0;
					for (; i < clazz.length; i++)
					{
						temp[i] = clazz[i];
					}

					temp[i] = Integer.parseInt(value);

					clazz = temp;
				}
				break;
			// Affected class type
			case "affectedClassType":
				if (value.equals("Fighter"))
				{
					classType = 1;
				}
				else
				{
					classType = 2;
				}
				break;
			case "targetClass":
				target = Enum.valueOf(InstanceType.class, value);
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
		if (character.getLevel() < minLvl || character.getLevel() > maxLvl)
		{
			return false;
		}

		// check obj class
		if (!character.isInstanceType(target))
		{
			return false;
		}

		if (character instanceof L2PcInstance)
		{
			// Check class type
			if (classType != 0)
			{
				if (((L2PcInstance) character).isMageClass())
				{
					if (classType == 1)
					{
						return false;
					}
				}
				else if (classType == 2)
				{
					return false;
				}
			}

			// Check race
			if (race != null)
			{
				boolean ok = false;

				for (int element : race)
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
			if (clazz != null)
			{
				boolean ok = false;

				for (int clas : clazz)
				{
					if (((L2PcInstance) character).getCurrentClass().getId() == clas)
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
		if (this.zone != null)
		{
			throw new IllegalStateException("Zone already set");
		}
		this.zone = zone;
	}

	/**
	 * Returns this zones zone form
	 *
	 * @return
	 */
	public L2ZoneForm getZone()
	{
		return zone;
	}

	/**
	 * Checks if the given coordinates are within zone's plane
	 *
	 * @param x
	 * @param y
	 */
	public boolean isInsideZone(int x, int y)
	{
		return zone.isInsideZone(x, y, zone.getHighZ());
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
		return zone.isInsideZone(x, y, z);
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
		if (checkAffected)
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
			if (!characterList.containsKey(character.getObjectId()))
			{
				ArrayList<Quest> quests = getQuestByEvent(Quest.QuestEventType.ON_ENTER_ZONE);
				if (quests != null)
				{
					for (Quest quest : quests)
					{
						quest.notifyEnterZone(character, this);
					}
				}

				characterList.put(character.getObjectId(), character);
				onEnter(character);
			}
		}
		else
		{
			// Was the character inside this zone?
			if (characterList.containsKey(character.getObjectId()))
			{
				ArrayList<Quest> quests = getQuestByEvent(Quest.QuestEventType.ON_EXIT_ZONE);
				if (quests != null)
				{
					for (Quest quest : quests)
					{
						quest.notifyExitZone(character, this);
					}
				}
				characterList.remove(character.getObjectId());
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
		if (characterList.containsKey(character.getObjectId()))
		{
			ArrayList<Quest> quests = getQuestByEvent(Quest.QuestEventType.ON_EXIT_ZONE);
			if (quests != null)
			{
				for (Quest quest : quests)
				{
					quest.notifyExitZone(character, this);
				}
			}
			characterList.remove(character.getObjectId());
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
		return characterList.containsKey(character.getObjectId());
	}

	protected abstract void onEnter(L2Character character);

	protected abstract void onExit(L2Character character);

	public void onDieInside(L2Character character, L2Character killer)
	{
		if (characterList.containsKey(character.getObjectId()))
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
		return characterList;
	}

	public void addQuestEvent(Quest.QuestEventType EventType, Quest q)
	{
		if (questEvents == null)
		{
			questEvents = new HashMap<>();
		}
		ArrayList<Quest> questByEvents = questEvents.get(EventType);
		if (questByEvents == null)
		{
			questByEvents = new ArrayList<>();
		}
		if (!questByEvents.contains(q))
		{
			questByEvents.add(q);
		}
		questEvents.put(EventType, questByEvents);
	}

	public ArrayList<Quest> getQuestByEvent(Quest.QuestEventType EventType)
	{
		if (questEvents == null)
		{
			return null;
		}
		return questEvents.get(EventType);
	}

	/**
	 * Broadcasts packet to all players inside the zone
	 */
	public void broadcastPacket(L2GameServerPacket packet)
	{
		if (characterList.isEmpty())
		{
			return;
		}

		for (L2Character character : characterList.values())
		{
			if (character != null && character instanceof L2PcInstance)
			{
				character.sendPacket(packet);
			}
		}
	}

	public InstanceType getTargetType()
	{
		return target;
	}

	public void setTargetType(InstanceType type)
	{
		target = type;
		checkAffected = true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [" + id + "]";
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
		for (L2Character ch : characterList.values())
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
		for (L2Character ch : characterList.values())
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
		for (L2Character ch : characterList.values())
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
		for (L2Character ch : characterList.values())
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
		if (characterList.isEmpty())
		{
			return;
		}

		for (L2Character character : characterList.values())
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
