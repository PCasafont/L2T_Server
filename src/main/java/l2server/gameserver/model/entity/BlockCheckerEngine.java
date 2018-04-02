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

package l2server.gameserver.model.entity;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.instancemanager.HandysBlockCheckerManager;
import l2server.gameserver.instancemanager.HandysBlockCheckerManager.ArenaParticipantsHolder;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.BlockInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

/**
 * @author BiggBoss
 */
public final class BlockCheckerEngine {
	private static Logger log = LoggerFactory.getLogger(BlockCheckerEngine.class.getName());

	// The object which holds all basic members info
	private HandysBlockCheckerManager.ArenaParticipantsHolder holder;
	// Maps to hold player of each team and his points
	private HashMap<Player, Integer> redTeamPoints = new HashMap<>();
	private HashMap<Player, Integer> blueTeamPoints = new HashMap<>();
	// The initial points of the event
	private int redPoints = 15;
	private int bluePoints = 15;
	// Current used arena
	private int arena = -1;
	// All blocks
	private ArrayList<L2Spawn> spawns = new ArrayList<>();
	// Sets if the red team won the event at the end of this (used for packets)
	private boolean isRedWinner;
	// Time when the event starts. Used on packet sending
	private long startedTime;
	// The needed arena coordinates
	// Arena X: team1X, team1Y, team2X, team2Y, ArenaCenterX, ArenaCenterY
	private static final int[][] arenaCoordinates = {
			// Arena 0 - Team 1 XY, Team 2 XY - CENTER XY
			{-58368, -62745, -57751, -62131, -58053, -62417},
			// Arena 1 - Team 1 XY, Team 2 XY - CENTER XY
			{-58350, -63853, -57756, -63266, -58053, -63551},
			// Arena 2 - Team 1 XY, Team 2 XY - CENTER XY
			{-57194, -63861, -56580, -63249, -56886, -63551},
			// Arena 3 - Team 1 XY, Team 2 XY - CENTER XY
			{-57200, -62727, -56584, -62115, -56850, -62391}};
	// Common z coordinate
	private static final int zCoord = -2405;
	// List of dropped items in event (for later deletion)
	private ArrayList<Item> drops = new ArrayList<>();
	// Default arena
	private static final byte DEFAULT_ARENA = -1;
	// Event is started
	private boolean isStarted = false;
	// Event end
	private ScheduledFuture<?> task;
	// Preserve from exploit reward by logging out
	private boolean abnormalEnd = false;

	public BlockCheckerEngine(HandysBlockCheckerManager.ArenaParticipantsHolder holder, int arena) {
		this.holder = holder;
		if (arena > -1 && arena < 4) {
			this.arena = arena;
		}

		for (Player player : holder.getRedPlayers()) {
			redTeamPoints.put(player, 0);
		}
		for (Player player : holder.getBluePlayers()) {
			blueTeamPoints.put(player, 0);
		}
	}

	/**
	 * Updates the player holder before the event starts
	 * to synchronize all info
	 *
	 * @param holder
	 */
	public void updatePlayersOnStart(ArenaParticipantsHolder holder) {
		this.holder = holder;
	}

	/**
	 * Returns the current holder object of this
	 * object engine
	 *
	 * @return HandysBlockCheckerManager.ArenaParticipantsHolder
	 */
	public ArenaParticipantsHolder getHolder() {
		return holder;
	}

	/**
	 * Will return the id of the arena used
	 * by this event
	 *
	 * @return false;
	 */
	public int getArena() {
		return arena;
	}

	/**
	 * Returns the time when the event
	 * started
	 *
	 * @return long
	 */
	public long getStarterTime() {
		return startedTime;
	}

	/**
	 * Returns the current red team points
	 *
	 * @return int
	 */
	public int getRedPoints() {
		synchronized (this) {
			return redPoints;
		}
	}

	/**
	 * Returns the current blue team points
	 *
	 * @return int
	 */
	public int getBluePoints() {
		synchronized (this) {
			return bluePoints;
		}
	}

	/**
	 * Returns the player points
	 *
	 * @param player
	 * @param isRed
	 * @return int
	 */
	public int getPlayerPoints(Player player, boolean isRed) {
		if (!redTeamPoints.containsKey(player) && !blueTeamPoints.containsKey(player)) {
			return 0;
		}

		if (isRed) {
			return redTeamPoints.get(player);
		} else {
			return blueTeamPoints.get(player);
		}
	}

	/**
	 * Increases player points for his teams
	 *
	 * @param player
	 * @param team
	 */
	public synchronized void increasePlayerPoints(Player player, int team) {
		if (player == null) {
			return;
		}

		if (team == 0) {
			int points = redTeamPoints.get(player) + 1;
			redTeamPoints.put(player, points);
			redPoints++;
			bluePoints--;
		} else {
			int points = blueTeamPoints.get(player) + 1;
			blueTeamPoints.put(player, points);
			bluePoints++;
			redPoints--;
		}
	}

	/**
	 * Will add a new drop into the list of
	 * dropped items
	 *
	 * @param item
	 */
	public void addNewDrop(Item item) {
		if (item != null) {
			drops.add(item);
		}
	}

	/**
	 * Will return true if the event is alredy
	 * started
	 *
	 * @return boolean
	 */
	public boolean isStarted() {
		return isStarted;
	}

	/**
	 * Will send all packets for the event members with
	 * the relation info
	 */
	private void broadcastRelationChanged(Player plr) {
		for (Player p : holder.getAllPlayers()) {
			p.sendPacket(new RelationChanged(plr, plr.getRelation(p), plr.isAutoAttackable(p)));
		}
	}

	/**
	 * Called when a there is an empty team. The event
	 * will end.
	 */
	public void endEventAbnormally() {
		try {
			synchronized (this) {
				isStarted = false;

				if (task != null) {
					task.cancel(true);
				}

				abnormalEnd = true;

				ThreadPoolManager.getInstance().executeTask(new EndEvent());

				if (Config.DEBUG) {
					log.debug("Handys Block Checker Event at arena " + arena + " ended due lack of players!");
				}
			}
		} catch (Exception e) {
			log.error("Couldnt end Block Checker event at " + arena, e);
		}
	}

	/**
	 * This inner class set ups all player
	 * and arena parameters to start the event
	 */
	public class StartEvent implements Runnable {
		// In event used skills
		private Skill freeze, transformationRed, transformationBlue;
		// Common and unparametizer packet
		private final ExCubeGameCloseUI closeUserInterface = new ExCubeGameCloseUI();

		public StartEvent() {
			// Initialize all used skills
			freeze = SkillTable.getInstance().getInfo(6034, 1);
			transformationRed = SkillTable.getInstance().getInfo(6035, 1);
			transformationBlue = SkillTable.getInstance().getInfo(6036, 1);
		}

		/**
		 * Will set up all player parameters and
		 * port them to their respective location
		 * based on their teams
		 */
		private void setUpPlayers() {
			// Set current arena as being used
			HandysBlockCheckerManager.getInstance().setArenaBeingUsed(arena);

			// Initialize packets avoiding create a new one per player
			redPoints = spawns.size() / 2;
			bluePoints = spawns.size() / 2;
			final ExCubeGameChangePoints initialPoints = new ExCubeGameChangePoints(300, bluePoints, redPoints);
			ExCubeGameExtendedChangePoints clientSetUp;

			for (Player player : holder.getAllPlayers()) {
				if (player == null) {
					continue;
				}

				// Send the secret client packet set up
				boolean isRed = holder.getRedPlayers().contains(player);

				clientSetUp = new ExCubeGameExtendedChangePoints(300, bluePoints, redPoints, isRed, player, 0);
				player.sendPacket(clientSetUp);

				player.sendPacket(ActionFailed.STATIC_PACKET);

				// Teleport Player - Array access
				// Team 0 * 2 = 0; 0 = 0, 0 + 1 = 1.
				// Team 1 * 2 = 2; 2 = 2, 2 + 1 = 3
				int tc = holder.getPlayerTeam(player) * 2;
				// Get x and y coordinates
				int x = arenaCoordinates[arena][tc];
				int y = arenaCoordinates[arena][tc + 1];
				player.teleToLocation(x, y, zCoord);
				// Set the player team
				if (isRed) {
					redTeamPoints.put(player, 0);
					player.setTeam(2);
				} else {
					blueTeamPoints.put(player, 0);
					player.setTeam(1);
				}
				player.stopAllEffects();

				if (player.getPet() != null) {
					player.getPet().unSummon(player);
				}
				for (SummonInstance summon : player.getSummons()) {
					summon.unSummon(player);
				}

				// Give the player start up effects
				// Freeze
				freeze.getEffects(player, player);
				// Tranformation
				if (holder.getPlayerTeam(player) == 0) {
					transformationRed.getEffects(player, player);
				} else {
					transformationBlue.getEffects(player, player);
				}
				// Set the current player arena
				player.setBlockCheckerArena((byte) arena);
				player.setInsideZone(Creature.ZONE_PVP, true);
				// Send needed packets
				player.sendPacket(initialPoints);
				player.sendPacket(closeUserInterface);
				// ExBasicActionList
				final ExBasicActionList actionList = ExBasicActionList.getStaticPacket(player);
				player.sendPacket(actionList);
				broadcastRelationChanged(player);
			}
		}

		@Override
		public void run() {
			// Wrong arena passed, stop event
			if (arena == -1) {
				log.error("Couldnt set up the arena Id for the Block Checker event, cancelling event...");
				return;
			}
			isStarted = true;
			// Spawn the blocks
			ThreadPoolManager.getInstance().executeTask(new SpawnRound(16, 1));
			// Start up player parameters
			setUpPlayers();
			// Set the started time
			startedTime = System.currentTimeMillis() + 300000;
		}
	}

	/**
	 * This class spawns the second round of boxes
	 * and schedules the event end
	 */
	private class SpawnRound implements Runnable {
		int numOfBoxes;
		int round;

		SpawnRound(int numberOfBoxes, int round) {
			numOfBoxes = numberOfBoxes;
			this.round = round;
		}

		@Override
		public void run() {
			if (!isStarted) {
				return;
			}

			switch (round) {
				case 1:
					// Schedule second spawn round
					task = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRound(20, 2), 60000);
					break;
				case 2:
					// Schedule third spawn round
					task = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnRound(14, 3), 60000);
					break;
				case 3:
					// Schedule Event End Count Down
					task = ThreadPoolManager.getInstance().scheduleGeneral(new EndEvent(), 180000);
					break;
			}
			// random % 2, if == 0 will spawn a red block
			// if != 0, will spawn a blue block
			byte random = 2;
			// common template
			final NpcTemplate template = NpcTable.getInstance().getTemplate(18672);
			// Spawn blocks
			try {
				// Creates 50 new blocks
				for (int i = 0; i < numOfBoxes; i++) {
					L2Spawn spawn = new L2Spawn(template);
					spawn.setX(arenaCoordinates[arena][4] + Rnd.get(-400, 400));
					spawn.setY(arenaCoordinates[arena][5] + Rnd.get(-400, 400));
					spawn.setZ(zCoord);
					spawn.setHeading(1);
					spawn.setRespawnDelay(1);
					SpawnTable.getInstance().addNewSpawn(spawn, false);
					spawn.startRespawn();
					spawn.doSpawn();
					BlockInstance block = (BlockInstance) spawn.getNpc();
					// switch color
					if (random % 2 == 0) {
						block.setRed(true);
					} else {
						block.setRed(false);
					}

					block.disableCoreAI(true);
					spawns.add(spawn);
					random++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Spawn the block carrying girl
			if (round == 1 || round == 2) {
				NpcTemplate girl = NpcTable.getInstance().getTemplate(18676);
				try {
					final L2Spawn girlSpawn = new L2Spawn(girl);
					girlSpawn.setX(arenaCoordinates[arena][4] + Rnd.get(-400, 400));
					girlSpawn.setY(arenaCoordinates[arena][5] + Rnd.get(-400, 400));
					girlSpawn.setZ(zCoord);
					girlSpawn.setHeading(1);
					girlSpawn.setRespawnDelay(1);
					SpawnTable.getInstance().addNewSpawn(girlSpawn, false);
					girlSpawn.startRespawn();
					girlSpawn.doSpawn();
					// Schedule his deletion after 9 secs of spawn
					ThreadPoolManager.getInstance().scheduleGeneral(new CarryingGirlUnspawn(girlSpawn), 9000);
				} catch (Exception e) {
					log.warn("Couldnt Spawn Block Checker NPCs! Wrong instance type at npc table?");
					if (Config.DEBUG) {
						e.printStackTrace();
					}
				}
			}

			redPoints += numOfBoxes / 2;
			bluePoints += numOfBoxes / 2;

			int timeLeft = (int) ((getStarterTime() - System.currentTimeMillis()) / 1000);
			ExCubeGameChangePoints changePoints = new ExCubeGameChangePoints(timeLeft, getBluePoints(), getRedPoints());
			getHolder().broadCastPacketToTeam(changePoints);
		}
	}

	private class CarryingGirlUnspawn implements Runnable {
		private L2Spawn spawn;

		private CarryingGirlUnspawn(L2Spawn spawn) {
			this.spawn = spawn;
		}

		@Override
		public void run() {
			if (spawn == null) {
				log.warn("HBCE: Block Carrying Girl is null");
				return;
			}
			SpawnTable.getInstance().deleteSpawn(spawn, false);
			spawn.stopRespawn();
			spawn.getNpc().deleteMe();
		}
	}

	/*
	private class CountDown implements Runnable
	{
		@Override
		public void run()
		{
			holder.broadCastPacketToTeam(SystemMessage.getSystemMessage(SystemMessageId.BLOCK_CHECKER_ENDS_5));
			ThreadPoolManager.getInstance().scheduleGeneral(new EndEvent(), 5000);
		}
	}
	 */

	/**
	 * This class erase all event parameters on player
	 * and port them back near Handy. Also, unspawn
	 * blocks, runs a garbage collector and set as free
	 * the used arena
	 */
	private class EndEvent implements Runnable {
		// Garbage collector and arena free setter
		private void clearMe() {
			HandysBlockCheckerManager.getInstance().clearPaticipantQueueByArenaId(arena);
			holder.clearPlayers();
			blueTeamPoints.clear();
			redTeamPoints.clear();
			HandysBlockCheckerManager.getInstance().setArenaFree(arena);

			for (L2Spawn spawn : spawns) {
				spawn.stopRespawn();
				spawn.getNpc().deleteMe();
				SpawnTable.getInstance().deleteSpawn(spawn, false);
				spawn = null;
			}
			spawns.clear();

			for (Item item : drops) {
				// npe
				if (item == null) {
					continue;
				}

				// a player has it, it will be deleted later
				if (!item.isVisible() || item.getOwnerId() != 0) {
					continue;
				}

				item.decayMe();
				World.getInstance().removeObject(item);
			}
			drops.clear();
		}

		/**
		 * Reward players after event.
		 * Tie - No Reward
		 */
		private void rewardPlayers() {
			if (redPoints == bluePoints) {
				return;
			}

			isRedWinner = redPoints > bluePoints;

			if (isRedWinner) {
				rewardAsWinner(true);
				rewardAsLooser(false);
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.TEAM_C1_WON);
				msg.addString("Red Team");
				holder.broadCastPacketToTeam(msg);
			} else if (bluePoints > redPoints) {
				rewardAsWinner(false);
				rewardAsLooser(true);
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.TEAM_C1_WON);
				msg.addString("Blue Team");
				holder.broadCastPacketToTeam(msg);
			} else {
				rewardAsLooser(true);
				rewardAsLooser(false);
			}
		}

		/**
		 * Reward the speicifed team as a winner team
		 * 1) Higher score - 8 extra
		 * 2) Higher score - 5 extra
		 *
		 * @param isRed
		 */
		private void rewardAsWinner(boolean isRed) {
			HashMap<Player, Integer> tempPoints = isRed ? redTeamPoints : blueTeamPoints;

			// Main give
			for (Player pc : tempPoints.keySet()) {
				if (pc == null) {
					continue;
				}

				if (tempPoints.get(pc) >= 10) {
					pc.addItem("Block Checker", 13067, 2, pc, true);
				} else {
					tempPoints.remove(pc);
				}
			}

			int first = 0, second = 0;
			Player winner1 = null, winner2 = null;
			for (Entry<Player, Integer> entry : tempPoints.entrySet()) {
				Player pc = entry.getKey();
				int pcPoints = entry.getValue();
				if (pcPoints > first) {
					// Move old data
					second = first;
					winner2 = winner1;
					// Set new data
					first = pcPoints;
					winner1 = pc;
				} else if (pcPoints > second) {
					second = pcPoints;
					winner2 = pc;
				}
			}
			if (winner1 != null) {
				winner1.addItem("Block Checker", 13067, 8, winner1, true);
			}
			if (winner2 != null) {
				winner2.addItem("Block Checker", 13067, 5, winner2, true);
			}
		}

		/**
		 * Will reward the looser team with the
		 * predefined rewards
		 * Player got >= 10 points: 2 coins
		 * Player got < 10 points: 0 coins
		 *
		 * @param isRed
		 */
		private void rewardAsLooser(boolean isRed) {
			HashMap<Player, Integer> tempPoints = isRed ? redTeamPoints : blueTeamPoints;

			for (Entry<Player, Integer> entry : tempPoints.entrySet()) {
				Player player = entry.getKey();
				if (player != null && entry.getValue() >= 10) {
					player.addItem("Block Checker", 13067, 2, player, true);
				}
			}
		}

		/**
		 * Telport players back, give status back and
		 * send final packet
		 */
		private void setPlayersBack() {
			final ExCubeGameEnd end = new ExCubeGameEnd(isRedWinner);

			for (Player player : holder.getAllPlayers()) {
				if (player == null) {
					continue;
				}

				player.stopAllEffects();
				// Remove team aura
				player.setTeam(0);
				// Set default arena
				player.setBlockCheckerArena(DEFAULT_ARENA);
				// Remove the event items
				PcInventory inv = player.getInventory();
				if (inv.getItemByItemId(13787) != null) {
					long count = inv.getInventoryItemCount(13787, 0);
					inv.destroyItemByItemId("Handys Block Checker", 13787, count, player, player);
				}
				if (inv.getItemByItemId(13788) != null) {
					long count = inv.getInventoryItemCount(13788, 0);
					inv.destroyItemByItemId("Handys Block Checker", 13788, count, player, player);
				}
				broadcastRelationChanged(player);
				// Teleport Back
				player.teleToLocation(-57478, -60367, -2370);
				player.setInsideZone(Creature.ZONE_PVP, false);
				// Send end packet
				player.sendPacket(end);
				player.broadcastUserInfo();
			}
		}

		@Override
		public void run() {
			if (!abnormalEnd) {
				rewardPlayers();
			}
			setPlayersBack();
			clearMe();
			isStarted = false;
			abnormalEnd = false;
		}
	}
}
