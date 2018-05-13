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
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.InstanceType;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.BoatInstance;
import l2server.gameserver.model.actor.instance.NpcInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.network.serverpackets.ExServerPrimitive;
import l2server.gameserver.network.serverpackets.ExStartScenePlayer;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public abstract class ZoneType {
	protected static Logger log = LoggerFactory.getLogger(ZoneType.class.getName());
	
	private final int id;
	protected L2ZoneForm zone;
	protected ConcurrentHashMap<Integer, Creature> characterList;
	
	/**
	 * Parameters to affect specific characters
	 */
	private boolean checkAffected = false;
	
	private String name = null;
	private int minLvl;
	private int maxLvl;
	private int[] race;
	private int[] clazz;
	private char classType;
	private Map<Quest.QuestEventType, ArrayList<Quest>> questEvents;
	private InstanceType target = InstanceType.L2Character; // default all chars
	
	protected ZoneType(int id) {
		this.id = id;
		characterList = new ConcurrentHashMap<>();
		
		minLvl = 0;
		maxLvl = 0xFF;
		
		classType = 0;
		
		race = null;
		clazz = null;
	}
	
	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Setup new parameters for this zone
	 *
	 */
	public void setParameter(String name, String value) {
		checkAffected = true;
		
		// Zone name
		switch (name) {
			case "name":
				name = value;
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
				if (race == null) {
					race = new int[1];
					race[0] = Integer.parseInt(value);
				} else {
					int[] temp = new int[race.length + 1];
					
					int i = 0;
					for (; i < race.length; i++) {
						temp[i] = race[i];
					}
					
					temp[i] = Integer.parseInt(value);
					
					race = temp;
				}
				break;
			// Affected classes
			case "affectedClassId":
				// Create a new array holding the affected classIds
				if (clazz == null) {
					clazz = new int[1];
					clazz[0] = Integer.parseInt(value);
				} else {
					int[] temp = new int[clazz.length + 1];
					
					int i = 0;
					for (; i < clazz.length; i++) {
						temp[i] = clazz[i];
					}
					
					temp[i] = Integer.parseInt(value);
					
					clazz = temp;
				}
				break;
			// Affected class type
			case "affectedClassType":
				if (value.equals("Fighter")) {
					classType = 1;
				} else {
					classType = 2;
				}
				break;
			case "targetClass":
				target = Enum.valueOf(InstanceType.class, value);
				break;
			default:
				log.warn("Unknown parameter - " + name + " in zone: " + getId());
				break;
		}
	}
	
	/**
	 * Checks if the given character is affected by this zone
	 *
	 */
	private boolean isAffected(Creature character) {
		// Check lvl
		if (character.getLevel() < minLvl || character.getLevel() > maxLvl) {
			return false;
		}
		
		// check obj class
		if (!character.isInstanceType(target)) {
			return false;
		}
		
		if (character instanceof Player) {
			// Check class type
			if (classType != 0) {
				if (((Player) character).isMageClass()) {
					if (classType == 1) {
						return false;
					}
				} else if (classType == 2) {
					return false;
				}
			}
			
			// Check race
			if (race != null) {
				boolean ok = false;
				
				for (int element : race) {
					if (((Player) character).getRace().ordinal() == element) {
						ok = true;
						break;
					}
				}
				
				if (!ok) {
					return false;
				}
			}
			
			// Check class
			if (clazz != null) {
				boolean ok = false;
				
				for (int clas : clazz) {
					if (((Player) character).getCurrentClass().getId() == clas) {
						ok = true;
						break;
					}
				}
				
				if (!ok) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Set the zone for this ZoneType Instance
	 *
	 */
	public void setZone(L2ZoneForm zone) {
		if (this.zone != null) {
			throw new IllegalStateException("Zone already set");
		}
		this.zone = zone;
	}
	
	/**
	 * Returns this zones zone form
	 *
	 */
	public L2ZoneForm getZone() {
		return zone;
	}
	
	/**
	 * Set the zone name.
	 *
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns zone name
	 *
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Checks if the given coordinates are within zone's plane
	 *
	 */
	public boolean isInsideZone(int x, int y) {
		return zone.isInsideZone(x, y, zone.getHighZ());
	}
	
	/**
	 * Checks if the given coordinates are within the zone
	 */
	public boolean isInsideZone(int x, int y, int z) {
		return zone.isInsideZone(x, y, z);
	}
	
	/**
	 * Checks if the given object is inside the zone.
	 */
	public boolean isInsideZone(WorldObject object) {
		return isInsideZone(object.getX(), object.getY(), object.getZ());
	}
	
	public double getDistanceToZone(int x, int y) {
		return getZone().getDistanceToZone(x, y);
	}
	
	public double getDistanceToZone(WorldObject object) {
		return getZone().getDistanceToZone(object.getX(), object.getY());
	}
	
	public void revalidateInZone(Creature character) {
		// If the character can't be affected by this zone return
		if (checkAffected) {
			if (!isAffected(character)) {
				return;
			}
		}
		
		// If the object is inside the zone...
		if (isInsideZone(character.getX(), character.getY(), character.getZ())) {
			// Was the character not yet inside this zone?
			if (!characterList.containsKey(character.getObjectId())) {
				ArrayList<Quest> quests = getQuestByEvent(Quest.QuestEventType.ON_ENTER_ZONE);
				if (quests != null) {
					for (Quest quest : quests) {
						quest.notifyEnterZone(character, this);
					}
				}
				
				characterList.put(character.getObjectId(), character);
				onEnter(character);
			}
		} else {
			// Was the character inside this zone?
			if (characterList.containsKey(character.getObjectId())) {
				ArrayList<Quest> quests = getQuestByEvent(Quest.QuestEventType.ON_EXIT_ZONE);
				if (quests != null) {
					for (Quest quest : quests) {
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
	 */
	public void removeCharacter(Creature character) {
		if (characterList.containsKey(character.getObjectId())) {
			ArrayList<Quest> quests = getQuestByEvent(Quest.QuestEventType.ON_EXIT_ZONE);
			if (quests != null) {
				for (Quest quest : quests) {
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
	 */
	public boolean isCharacterInZone(Creature character) {
		return characterList.containsKey(character.getObjectId());
	}
	
	protected abstract void onEnter(Creature character);
	
	protected abstract void onExit(Creature character);
	
	public void onDieInside(Creature character, Creature killer) {
		if (characterList.containsKey(character.getObjectId())) {
			ArrayList<Quest> quests = getQuestByEvent(Quest.QuestEventType.ON_DIE_ZONE);
			if (quests != null) {
				for (Quest quest : quests) {
					quest.notifyDieZone(character, killer, this);
				}
			}
		}
	}
	
	public abstract void onReviveInside(Creature character);
	
	public ConcurrentHashMap<Integer, Creature> getCharactersInside() {
		return characterList;
	}
	
	public void addQuestEvent(Quest.QuestEventType EventType, Quest q) {
		if (questEvents == null) {
			questEvents = new HashMap<>();
		}
		ArrayList<Quest> questByEvents = questEvents.get(EventType);
		if (questByEvents == null) {
			questByEvents = new ArrayList<>();
		}
		if (!questByEvents.contains(q)) {
			questByEvents.add(q);
		}
		questEvents.put(EventType, questByEvents);
	}
	
	public ArrayList<Quest> getQuestByEvent(Quest.QuestEventType EventType) {
		if (questEvents == null) {
			return null;
		}
		return questEvents.get(EventType);
	}
	
	/**
	 * Broadcasts packet to all players inside the zone
	 */
	public void broadcastPacket(L2GameServerPacket packet) {
		if (characterList.isEmpty()) {
			return;
		}
		
		for (Creature character : characterList.values()) {
			if (character != null && character instanceof Player) {
				character.sendPacket(packet);
			}
		}
	}
	
	public InstanceType getTargetType() {
		return target;
	}
	
	public void setTargetType(InstanceType type) {
		target = type;
		checkAffected = true;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [" + id + "]";
	}
	
	public void visualizeZone(Player viewer) {
		ExServerPrimitive packet = new ExServerPrimitive(toString());
		getZone().visualizeZone(packet, toString(), viewer.getZ() + 20);
		viewer.sendPacket(packet);
	}
	
	public void unVisualizeZone(Player viewer) {
		viewer.sendPacket(new ExServerPrimitive(toString()));
	}
	
	public List<Player> getPlayersInside() {
		List<Player> players = new ArrayList<>();
		for (Creature ch : characterList.values()) {
			if (ch != null && ch instanceof Player) {
				players.add(ch.getActingPlayer());
			}
		}
		
		return players;
	}
	
	public List<Npc> getNpcsInside() {
		List<Npc> npcs = new ArrayList<>();
		for (Creature ch : characterList.values()) {
			if (ch == null || ch instanceof Playable || ch instanceof BoatInstance ||
					!(ch instanceof Attackable) && !(ch instanceof NpcInstance)) {
				continue;
			}
			
			npcs.add((Npc) ch);
		}
		
		return npcs;
	}
	
	public void showVidToZone(int vidId) {
		stopWholeZone();
		
		broadcastMovie(vidId);
		
		ThreadPoolManager.getInstance().scheduleGeneral(this::startWholeZone, ScenePlayerDataTable.getInstance().getVideoDuration(vidId) + 1000);
	}
	
	public void stopWholeZone() {
		for (Creature ch : characterList.values()) {
			if (ch == null) {
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
	
	public void broadcastMovie(int vidId) {
		for (Player pl : getPlayersInside()) {
			if (pl == null) {
				continue;
			}
			
			pl.setMovieId(vidId);
			pl.sendPacket(new ExStartScenePlayer(vidId));
		}
	}
	
	public void startWholeZone() {
		for (Creature ch : characterList.values()) {
			if (ch == null) {
				continue;
			}
			
			ch.enableAllSkills();
			ch.setIsInvul(false);
			ch.setIsImmobilized(false);
		}
	}
	
	public void sendDelayedPacketToZone(final int delayMsSec, final L2GameServerPacket packet) {
		ThreadPoolManager.getInstance().scheduleGeneral(() -> broadcastPacket(packet), delayMsSec);
	}
	
	public void oustAllPlayers() {
		if (characterList.isEmpty()) {
			return;
		}
		
		for (Creature character : characterList.values()) {
			if (character == null) {
				continue;
			}
			
			if (character instanceof Player) {
				Player player = (Player) character;
				if (player.isOnline()) {
					player.teleToLocation(TeleportWhereType.Town);
				}
			}
		}
	}
}
