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
import l2server.DatabasePool;
import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.instancemanager.CastleSiegeManager;
import l2server.gameserver.instancemanager.MercTicketManager;
import l2server.gameserver.instancemanager.SiegeGuardManager;
import l2server.gameserver.model.*;
import l2server.gameserver.model.L2SiegeClan.SiegeClanType;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.ControlTowerInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.model.olympiad.HeroesManager;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.RelationChanged;
import l2server.gameserver.network.serverpackets.SiegeInfo;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.network.serverpackets.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class Siege implements Siegable {
	private static Logger log = LoggerFactory.getLogger(Siege.class.getName());
	
	// typeId's
	public static final byte OWNER = -1;
	public static final byte DEFENDER = 0;
	public static final byte ATTACKER = 1;
	public static final byte DEFENDER_NOT_APPROWED = 2;
	private int controlTowerCount;
	private int flameTowerCount;
	
	public enum TeleportWhoType {
		All,
		Attacker,
		DefenderNotOwner,
		Owner,
		Spectator
	}
	
	public class ScheduleEndSiegeTask implements Runnable {
		private Castle castleInst;
		
		public ScheduleEndSiegeTask(Castle pCastle) {
			castleInst = pCastle;
		}
		
		@Override
		public void run() {
			if (!getIsInProgress()) {
				return;
			}
			
			try {
				long timeRemaining = siegeEndDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				if (timeRemaining > 3600000) {
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HOURS_UNTIL_SIEGE_CONCLUSION);
					sm.addNumber(2);
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance()
							.scheduleGeneral(new ScheduleEndSiegeTask(castleInst), timeRemaining - 3600000); // Prepare task for 1 hr left.
				} else if (timeRemaining <= 3600000 && timeRemaining > 600000) {
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION);
					sm.addNumber(Math.round(timeRemaining / 60000));
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance()
							.scheduleGeneral(new ScheduleEndSiegeTask(castleInst), timeRemaining - 600000); // Prepare task for 10 minute left.
				} else if (timeRemaining <= 600000 && timeRemaining > 300000) {
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION);
					sm.addNumber(Math.round(timeRemaining / 60000));
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance()
							.scheduleGeneral(new ScheduleEndSiegeTask(castleInst), timeRemaining - 300000); // Prepare task for 5 minute left.
				} else if (timeRemaining <= 300000 && timeRemaining > 10000) {
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION);
					sm.addNumber(Math.round(timeRemaining / 60000));
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance()
							.scheduleGeneral(new ScheduleEndSiegeTask(castleInst), timeRemaining - 10000); // Prepare task for 10 seconds count down
				} else if (timeRemaining <= 10000 && timeRemaining > 0) {
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CASTLE_SIEGE_S1_SECONDS_LEFT);
					sm.addNumber(Math.round(timeRemaining / 1000));
					announceToPlayer(sm, true);
					ThreadPoolManager.getInstance()
							.scheduleGeneral(new ScheduleEndSiegeTask(castleInst), timeRemaining); // Prepare task for second count down
				} else {
					castleInst.getSiege().endSiege();
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
	
	public class ScheduleAddReputationPointsToMembers implements Runnable {
		private Castle castleInst;
		
		public ScheduleAddReputationPointsToMembers(Castle pCastle) {
			castleInst = pCastle;
		}
		
		@Override
		public void run() {
			if (!getIsInProgress()) {
				return;
			}
			
			try {
				if (castleInst.getSiege().getIsInProgress()) {
					for (Player pc : castleInst.getSiege().getPlayersInZone()) {
						for (L2SiegeClan sc : castleInst.getSiege().getAttackerClans()) {
							if (sc.getClanId() == pc.getClanId()) {
								pc.getClan().addReputationScore(1, true);
							}
						}
					}
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleAddReputationPointsToMembers(castleInst), 5 * 60 * 1000);
				} else {
					castleInst.getSiege().endSiege();
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
	
	public class ScheduleStartSiegeTask implements Runnable {
		private Castle castleInst;
		
		public ScheduleStartSiegeTask(Castle pCastle) {
			castleInst = pCastle;
		}
		
		@Override
		public void run() {
			scheduledStartSiegeTask.cancel(false);
			if (getIsInProgress()) {
				return;
			}
			
			try {
				if (!getIsTimeRegistrationOver()) {
					long regTimeRemaining = getTimeRegistrationOverDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
					if (regTimeRemaining > 0) {
						scheduledStartSiegeTask =
								ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(castleInst), regTimeRemaining);
						return;
					} else {
						endTimeRegistration(true);
					}
				}
				
				long timeRemaining = getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				if (timeRemaining > 86400000) {
					scheduledStartSiegeTask = ThreadPoolManager.getInstance()
							.scheduleGeneral(new ScheduleStartSiegeTask(castleInst),
									timeRemaining - 86400000); // Prepare task for 24 before siege start to end registration
				} else if (timeRemaining <= 86400000 && timeRemaining > 13600000) {
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED);
					sm.addString(getCastle().getName());
					Announcements.getInstance().announceToAll(sm);
					isRegistrationOver = true;
					clearSiegeWaitingClan();
					scheduledStartSiegeTask = ThreadPoolManager.getInstance()
							.scheduleGeneral(new ScheduleStartSiegeTask(castleInst),
									timeRemaining - 13600000); // Prepare task for 1 hr left before siege start.
				} else if (timeRemaining <= 13600000 && timeRemaining > 600000) {
					scheduledStartSiegeTask = ThreadPoolManager.getInstance()
							.scheduleGeneral(new ScheduleStartSiegeTask(castleInst), timeRemaining - 600000); // Prepare task for 10 minute left.
				} else if (timeRemaining <= 600000 && timeRemaining > 300000) {
					scheduledStartSiegeTask = ThreadPoolManager.getInstance()
							.scheduleGeneral(new ScheduleStartSiegeTask(castleInst), timeRemaining - 300000); // Prepare task for 5 minute left.
				} else if (timeRemaining <= 300000 && timeRemaining > 10000) {
					scheduledStartSiegeTask = ThreadPoolManager.getInstance()
							.scheduleGeneral(new ScheduleStartSiegeTask(castleInst), timeRemaining - 10000); // Prepare task for 10 seconds count down
				} else if (timeRemaining <= 10000 && timeRemaining > 0) {
					scheduledStartSiegeTask = ThreadPoolManager.getInstance()
							.scheduleGeneral(new ScheduleStartSiegeTask(castleInst), timeRemaining); // Prepare task for second count down
				} else {
					castleInst.getSiege().startSiege();
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
	
	private List<L2SiegeClan> attackerClans = new ArrayList<>();
	private List<L2SiegeClan> defenderClans = new ArrayList<>();
	private List<L2SiegeClan> defenderWaitingClans = new ArrayList<>();
	
	// Castle setting
	private Castle[] castle;
	private boolean isInProgress = false;
	private boolean isNormalSide = true; // true = Atk is Atk, false = Atk is Def
	protected boolean isRegistrationOver = false;
	protected Calendar siegeEndDate;
	private SiegeGuardManager siegeGuardManager;
	protected ScheduledFuture<?> scheduledStartSiegeTask = null;
	protected int firstOwnerClanId = -1;
	
	public Siege(Castle[] castle) {
		this.castle = castle;
		siegeGuardManager = new SiegeGuardManager(getCastle());
		
		startAutoTask();
	}
	
	@Override
	public void endSiege() {
		if (getIsInProgress()) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_ENDED);
			sm.addString(getCastle().getName());
			Announcements.getInstance().announceToAll(sm);
			
			if (getCastle().getOwnerId() > 0) {
				L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE);
				sm.addString(clan.getName());
				sm.addString(getCastle().getName());
				Announcements.getInstance().announceToAll(sm);
				
				if (clan.getClanId() == firstOwnerClanId) {
					// Owner is unchanged
					final int num = CastleSiegeManager.getInstance().getBloodAllianceReward();
					int count = getCastle().getBloodAlliance();
					if (num > 0) {
						getCastle().setBloodAlliance(count + num);
					}
				} else {
					getCastle().setBloodAlliance(0);
					for (L2ClanMember member : clan.getMembers()) {
						if (member != null) {
							Player player = member.getPlayerInstance();
							if (player != null && player.isNoble()) {
								HeroesManager.getInstance().setCastleTaken(player.getObjectId(), getCastle().getCastleId());
							}
						}
					}
				}
			} else {
				sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_S1_DRAW);
				sm.addString(getCastle().getName());
				Announcements.getInstance().announceToAll(sm);
			}
			
			getCastle().updateClansReputation();
			removeFlags(); // Removes all flags. Note: Remove flag before teleporting players
			teleportPlayer(TeleportWhoType.Attacker, MapRegionTable.TeleportWhereType.Town); // Teleport to the second closest town
			teleportPlayer(TeleportWhoType.DefenderNotOwner, MapRegionTable.TeleportWhereType.Town); // Teleport to the second closest town
			teleportPlayer(TeleportWhoType.Spectator, MapRegionTable.TeleportWhereType.Town); // Teleport to the second closest town
			isInProgress = false; // Flag so that siege instance can be started
			updatePlayerSiegeStateFlags(true);
			saveCastleSiege(); // Save castle specific data
			clearSiegeClan(); // Clear siege clan from db
			SpawnTable.getInstance().despawnSpecificTable(getCastle().getName() + "_control_tower"); // Remove all control tower from this castle
			SpawnTable.getInstance().despawnSpecificTable(getCastle().getName() + "_flame_tower");
			siegeGuardManager.unspawnSiegeGuard(); // Remove all spawned siege guard from this castle
			if (getCastle().getOwnerId() > 0) {
				siegeGuardManager.removeMercs();
			}
			getCastle().spawnDoor(); // Respawn door to castle
			getCastle().getZone().setIsActive(false);
			getCastle().getZone().updateZoneStatusForCharactersInside();
			getCastle().getZone().setSiegeInstance(null);
		}
	}
	
	private void removeDefender(L2SiegeClan sc) {
		if (sc != null) {
			getDefenderClans().remove(sc);
		}
	}
	
	private void removeAttacker(L2SiegeClan sc) {
		if (sc != null) {
			getAttackerClans().remove(sc);
		}
	}
	
	private void addDefender(L2SiegeClan sc, SiegeClanType type) {
		if (sc == null) {
			return;
		}
		sc.setType(type);
		getDefenderClans().add(sc);
	}
	
	private void addAttacker(L2SiegeClan sc) {
		if (sc == null) {
			return;
		}
		sc.setType(SiegeClanType.ATTACKER);
		getAttackerClans().add(sc);
	}
	
	/**
	 * When control of castle changed during siege<BR><BR>
	 */
	public void midVictory() {
		if (getIsInProgress()) // Siege still in progress
		{
			if (getCastle().getOwnerId() > 0) {
				siegeGuardManager.removeMercs(); // Remove all merc entry from db
			}
			
			if (getDefenderClans().isEmpty() && // If defender doesn't exist (Pc vs Npc)
					getAttackerClans().size() == 1) {
				L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
				removeAttacker(sc_newowner);
				addDefender(sc_newowner, SiegeClanType.OWNER);
				endSiege();
				return;
			}
			if (getCastle().getOwnerId() > 0) {
				int allyId = ClanTable.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
				if (getDefenderClans().isEmpty()) // If defender doesn't exist (Pc vs Npc)
				// and only an alliance attacks
				{
					// The player's clan is in an alliance
					if (allyId != 0) {
						boolean allinsamealliance = true;
						for (L2SiegeClan sc : getAttackerClans()) {
							if (sc != null) {
								if (ClanTable.getInstance().getClan(sc.getClanId()).getAllyId() != allyId) {
									allinsamealliance = false;
								}
							}
						}
						if (allinsamealliance) {
							L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
							removeAttacker(sc_newowner);
							addDefender(sc_newowner, SiegeClanType.OWNER);
							endSiege();
							return;
						}
					}
				}
				
				List<L2SiegeClan> toIterate = new ArrayList<>(getDefenderClans());
				for (L2SiegeClan sc : toIterate) {
					if (sc != null) {
						removeDefender(sc);
						addAttacker(sc);
					}
				}
				
				L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
				removeAttacker(sc_newowner);
				addDefender(sc_newowner, SiegeClanType.OWNER);
				
				// The player's clan is in an alliance
				if (allyId != 0) {
					L2Clan[] clanList = ClanTable.getInstance().getClans();
					
					for (L2Clan clan : clanList) {
						if (clan.getAllyId() == allyId) {
							L2SiegeClan sc = getAttackerClan(clan.getClanId());
							if (sc != null) {
								removeAttacker(sc);
								addDefender(sc, SiegeClanType.DEFENDER);
							}
						}
					}
				}
				teleportPlayer(TeleportWhoType.Attacker, MapRegionTable.TeleportWhereType.SiegeFlag); // Teleport to the second closest town
				teleportPlayer(TeleportWhoType.Spectator, MapRegionTable.TeleportWhereType.Town); // Teleport to the second closest town
				
				removeDefenderFlags(); // Removes defenders' flags
				getCastle().removeUpgrade(); // Remove all castle upgrade
				getCastle().spawnDoor(true); // Respawn door to castle but make them weaker (50% hp)
				SpawnTable.getInstance().despawnSpecificTable(getCastle().getName() + "_control_tower"); // Remove all control tower from this castle
				SpawnTable.getInstance().despawnSpecificTable(getCastle().getName() + "_flame_tower");
				controlTowerCount = 0;//Each new siege midvictory CT are completely respawned.
				flameTowerCount = 0;
				updatePlayerSiegeStateFlags(false);
			}
		}
	}
	
	/**
	 * When siege starts<BR><BR>
	 */
	@Override
	public void startSiege() {
		if (!getIsInProgress()) {
			firstOwnerClanId = getCastle().getOwnerId();
			
			if (getAttackerClans().isEmpty()) {
				SystemMessage sm;
				if (getCastle().getOwnerId() <= 0) {
					sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
				} else {
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED);
				}
				sm.addString(getCastle().getName());
				Announcements.getInstance().announceToAll(sm);
				saveCastleSiege();
				return;
			}
			
			isNormalSide = true; // Atk is now atk
			isInProgress = true; // Flag so that same siege instance cannot be started again
			
			loadSiegeClan(); // Load siege clan from db
			updatePlayerSiegeStateFlags(false);
			teleportPlayer(TeleportWhoType.Attacker, MapRegionTable.TeleportWhereType.Town); // Teleport to the closest town
			//teleportPlayer(Siege.TeleportWhoType.Spectator, MapRegionTable.TeleportWhereType.Town);	  // Teleport to the second closest town
			SpawnTable.getInstance().spawnSpecificTable(getCastle().getName() + "_control_tower");
			SpawnTable.getInstance().spawnSpecificTable(getCastle().getName() + "_flame_tower"); // Spawn control tower
			controlTowerCount = SpawnTable.getInstance().getSpecificSpawns(getCastle().getName() + "_control_tower").size();
			flameTowerCount = SpawnTable.getInstance().getSpecificSpawns(getCastle().getName() + "_flame_tower").size();
			getCastle().spawnDoor(); // Spawn door
			spawnSiegeGuard(); // Spawn siege guard
			MercTicketManager.getInstance().deleteTickets(getCastle().getCastleId()); // remove the tickets from the ground
			getCastle().getZone().setSiegeInstance(this);
			getCastle().getZone().setIsActive(true);
			getCastle().getZone().updateZoneStatusForCharactersInside();
			
			// Schedule a task to prepare auto siege end
			siegeEndDate = Calendar.getInstance();
			siegeEndDate.add(Calendar.MINUTE, CastleSiegeManager.getInstance().getSiegeLength());
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(getCastle()), 1000); // Prepare auto end task
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleAddReputationPointsToMembers(getCastle()), 0);
			
			for (L2SiegeClan sc : getAttackerClans()) {
				L2Clan clan = ClanTable.getInstance().getClan(sc.getClanId());
				if (clan.getLevel() >= 5) {
					clan.addReputationScore(clan.getMembersCount(), true);
					// TODO : Clan Reputation is gained along with Individual Fame.
				}
			}
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_STARTED);
			sm.addString(getCastle().getName());
			Announcements.getInstance().announceToAll(sm);
		}
	}
	
	/**
	 * Announce to player.<BR><BR>
	 *
	 * @param message   The SystemMessage to send to player
	 * @param bothSides True - broadcast to both attackers and defenders. False - only to defenders.
	 */
	public void announceToPlayer(SystemMessage message, boolean bothSides) {
		for (L2SiegeClan siegeClans : getDefenderClans()) {
			L2Clan clan = ClanTable.getInstance().getClan(siegeClans.getClanId());
			for (Player member : clan.getOnlineMembers(0)) {
				if (member != null) {
					member.sendPacket(message);
				}
			}
		}
		
		if (bothSides) {
			for (L2SiegeClan siegeClans : getAttackerClans()) {
				L2Clan clan = ClanTable.getInstance().getClan(siegeClans.getClanId());
				for (Player member : clan.getOnlineMembers(0)) {
					if (member != null) {
						member.sendPacket(message);
					}
				}
			}
		}
	}
	
	public void updatePlayerSiegeStateFlags(boolean clear) {
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans()) {
			if (siegeclan == null) {
				continue;
			}
			
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			
			if (clan == null) {
				continue;
			}
			
			for (Player member : clan.getOnlineMembers(0)) {
				if (member == null) {
					continue;
				}
				
				if (clear) {
					member.setSiegeState((byte) 0);
					member.setSiegeSide(0);
					member.setIsInSiege(false);
					member.stopFameTask();
				} else {
					member.setSiegeState((byte) 1);
					member.setSiegeSide(getCastle().getCastleId());
					if (checkIfInZone(member)) {
						member.setIsInSiege(true);
						member.startFameTask(Config.CASTLE_ZONE_FAME_TASK_FREQUENCY * 1000, Config.CASTLE_ZONE_FAME_AQUIRE_POINTS);
					}
				}
				member.sendPacket(new UserInfo(member));
				//synchronized (member.getKnownList().getKnownPlayers())
				{
					for (Player player : member.getKnownList().getKnownPlayers().values()) {
						if (player == null) {
							continue;
						}
						
						player.sendPacket(new RelationChanged(member, member.getRelation(player), member.isAutoAttackable(player)));
						if (member.getPet() != null) {
							player.sendPacket(new RelationChanged(member.getPet(), member.getRelation(player), member.isAutoAttackable(player)));
						}
						for (SummonInstance summon : member.getSummons()) {
							player.sendPacket(new RelationChanged(summon, member.getRelation(player), member.isAutoAttackable(player)));
						}
					}
				}
			}
		}
		for (L2SiegeClan siegeclan : getDefenderClans()) {
			if (siegeclan == null) {
				continue;
			}
			
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (Player member : clan.getOnlineMembers(0)) {
				if (member == null) {
					continue;
				}
				
				if (clear) {
					member.setSiegeState((byte) 0);
					member.setSiegeSide(0);
					member.setIsInSiege(false);
					member.stopFameTask();
				} else {
					member.setSiegeState((byte) 2);
					member.setSiegeSide(getCastle().getCastleId());
					if (checkIfInZone(member)) {
						member.setIsInSiege(true);
						member.startFameTask(Config.CASTLE_ZONE_FAME_TASK_FREQUENCY * 1000, Config.CASTLE_ZONE_FAME_AQUIRE_POINTS);
					}
				}
				member.sendPacket(new UserInfo(member));
				//synchronized (member.getKnownList().getKnownPlayers())
				{
					for (Player player : member.getKnownList().getKnownPlayers().values()) {
						if (player == null) {
							continue;
						}
						player.sendPacket(new RelationChanged(member, member.getRelation(player), member.isAutoAttackable(player)));
						if (member.getPet() != null) {
							player.sendPacket(new RelationChanged(member.getPet(), member.getRelation(player), member.isAutoAttackable(player)));
						}
					}
				}
			}
		}
	}
	
	/**
	 * Approve clan as defender for siege<BR><BR>
	 *
	 * @param clanId The int of player's clan id
	 */
	public void approveSiegeDefenderClan(int clanId) {
		if (clanId <= 0) {
			return;
		}
		saveSiegeClan(ClanTable.getInstance().getClan(clanId), DEFENDER, true);
		loadSiegeClan();
	}
	
	/**
	 * Return true if object is inside the zone
	 */
	public boolean checkIfInZone(WorldObject object) {
		return checkIfInZone(object.getX(), object.getY(), object.getZ());
	}
	
	/**
	 * Return true if object is inside the zone
	 */
	public boolean checkIfInZone(int x, int y, int z) {
		return getIsInProgress() && getCastle().checkIfInZone(x, y, z); // Castle zone during siege
	}
	
	/**
	 * Return true if clan is attacker<BR><BR>
	 *
	 * @param clan The L2Clan of the player
	 */
	@Override
	public boolean checkIsAttacker(L2Clan clan) {
		return getAttackerClan(clan) != null;
	}
	
	/**
	 * Return true if clan is defender<BR><BR>
	 *
	 * @param clan The L2Clan of the player
	 */
	@Override
	public boolean checkIsDefender(L2Clan clan) {
		return getDefenderClan(clan) != null;
	}
	
	/**
	 * Return true if clan is defender waiting approval<BR><BR>
	 *
	 * @param clan The L2Clan of the player
	 */
	public boolean checkIsDefenderWaiting(L2Clan clan) {
		return getDefenderWaitingClan(clan) != null;
	}
	
	/**
	 * Clear all registered siege clans from database for castle
	 */
	public void clearSiegeClan() {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=?");
			statement.setInt(1, getCastle().getCastleId());
			statement.execute();
			statement.close();
			
			if (getCastle().getOwnerId() > 0) {
				statement = con.prepareStatement("DELETE FROM siege_clans WHERE clan_id=?");
				statement.setInt(1, getCastle().getOwnerId());
				statement.execute();
			}
			
			for (L2SiegeClan sc : getAttackerClans()) {
				L2Clan clan = ClanTable.getInstance().getClan(sc.getClanId());
				if (clan != null) {
					clan.checkTendency();
				}
			}
			
			getAttackerClans().clear();
			
			for (L2SiegeClan sc : getDefenderClans()) {
				L2Clan clan = ClanTable.getInstance().getClan(sc.getClanId());
				if (clan != null) {
					clan.checkTendency();
				}
			}
			
			getDefenderClans().clear();
			getDefenderWaitingClans().clear();
		} catch (Exception e) {
			log.warn("Exception: clearSiegeClan(): " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}
	
	/**
	 * Clear all siege clans waiting for approval from database for castle
	 */
	public void clearSiegeWaitingClan() {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? AND type = 2");
			statement.setInt(1, getCastle().getCastleId());
			statement.execute();
			
			getDefenderWaitingClans().clear();
		} catch (Exception e) {
			log.warn("Exception: clearSiegeWaitingClan(): " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}
	
	/**
	 * Return list of Player registered as attacker in the zone.
	 */
	@Override
	public List<Player> getAttackersInZone() {
		List<Player> players = new ArrayList<>();
		L2Clan clan;
		for (L2SiegeClan siegeclan : getAttackerClans()) {
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			for (Player player : clan.getOnlineMembers(0)) {
				if (player == null) {
					continue;
				}
				
				if (player.isInSiege()) {
					players.add(player);
				}
			}
		}
		return players;
	}
	
	/**
	 * Return list of Player registered as defender but not owner in the zone.
	 */
	public List<Player> getDefendersButNotOwnersInZone() {
		List<Player> players = new ArrayList<>();
		L2Clan clan;
		for (L2SiegeClan siegeclan : getDefenderClans()) {
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			if (clan.getClanId() == getCastle().getOwnerId()) {
				continue;
			}
			for (Player player : clan.getOnlineMembers(0)) {
				if (player == null) {
					continue;
				}
				
				if (player.isInSiege()) {
					players.add(player);
				}
			}
		}
		return players;
	}
	
	/**
	 * Return list of Player in the zone.
	 */
	public List<Player> getPlayersInZone() {
		return getCastle().getZone().getAllPlayers();
	}
	
	/**
	 * Return list of Player owning the castle in the zone.
	 */
	public List<Player> getOwnersInZone() {
		List<Player> players = new ArrayList<>();
		L2Clan clan;
		for (L2SiegeClan siegeclan : getDefenderClans()) {
			clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
			if (clan.getClanId() != getCastle().getOwnerId()) {
				continue;
			}
			for (Player player : clan.getOnlineMembers(0)) {
				if (player == null) {
					continue;
				}
				
				if (player.isInSiege()) {
					players.add(player);
				}
			}
		}
		return players;
	}
	
	/**
	 * Return list of Player not registered as attacker or defender in the zone.
	 */
	public List<Player> getSpectatorsInZone() {
		List<Player> players = new ArrayList<>();
		
		//synchronized (World.getInstance().getAllPlayers())
		for (Player player : getCastle().getZone().getAllPlayers()) {
			if (player == null) {
				continue;
			}
			
			if (!player.isInSiege()) {
				players.add(player);
			}
		}
		return players;
	}
	
	/**
	 * Control Tower was killed
	 */
	public void killedCT(Npc ct) {
		controlTowerCount--;
		if (controlTowerCount < 0) {
			controlTowerCount = 0;
		}
	}
	
	/**
	 * Remove the flag that was killed
	 */
	public void killedFlag(Npc flag) {
		if (flag == null) {
			return;
		}
		for (L2SiegeClan clan : getAttackerClans()) {
			if (clan.removeFlag(flag)) {
				return;
			}
		}
	}
	
	/**
	 * Display list of registered clans
	 */
	public void listRegisterClan(Player player) {
		player.sendPacket(new SiegeInfo(getCastle()));
	}
	
	/**
	 * Register clan as attacker<BR><BR>
	 *
	 * @param player The Player of the player trying to register
	 */
	public void registerAttacker(Player player) {
		registerAttacker(player, false);
	}
	
	public void registerAttacker(Player player, boolean force) {
		if (player.getClan() == null) {
			return;
		}
		int allyId = 0;
		if (getCastle().getOwnerId() != 0) {
			allyId = ClanTable.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
		}
		if (allyId != 0) {
			if (player.getClan().getAllyId() == allyId && !force) {
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_ATTACK_ALLIANCE_CASTLE));
				return;
			}
		}
		if (force || checkIfCanRegister(player, ATTACKER)) {
			saveSiegeClan(player.getClan(), ATTACKER, false); // Save to database
		}
	}
	
	/**
	 * Register clan as defender<BR><BR>
	 *
	 * @param player The Player of the player trying to register
	 */
	public void registerDefender(Player player) {
		registerDefender(player, false);
	}
	
	public void registerDefender(Player player, boolean force) {
		if (getCastle().getOwnerId() <= 0) {
			player.sendMessage("You cannot register as a defender because " + getCastle().getName() + " is owned by NPC.");
		} else if (force || checkIfCanRegister(player, DEFENDER_NOT_APPROWED)) {
			saveSiegeClan(player.getClan(), DEFENDER_NOT_APPROWED, false); // Save to database
		}
	}
	
	/**
	 * Remove clan from siege<BR><BR>
	 *
	 * @param clanId The int of player's clan id
	 */
	public void removeSiegeClan(int clanId) {
		if (clanId <= 0) {
			return;
		}
		
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? AND clan_id=?");
			statement.setInt(1, getCastle().getCastleId());
			statement.setInt(2, clanId);
			statement.execute();
			
			loadSiegeClan();
		} catch (Exception e) {
			log.warn("Exception: removeSiegeClan(): " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}
	
	/**
	 * Remove clan from siege<BR><BR>
	 */
	public void removeSiegeClan(L2Clan clan) {
		if (clan == null || clan.getHasCastle() == getCastle().getCastleId() ||
				!CastleSiegeManager.getInstance().checkIsRegistered(clan, getCastle().getCastleId())) {
			return;
		}
		removeSiegeClan(clan.getClanId());
	}
	
	/**
	 * Remove clan from siege<BR><BR>
	 *
	 * @param player The Player of player/clan being removed
	 */
	public void removeSiegeClan(Player player) {
		removeSiegeClan(player.getClan());
	}
	
	/**
	 * Start the auto tasks<BR><BR>
	 */
	public void startAutoTask() {
		correctSiegeDateTime();
		
		log.debug("Siege of " + getCastle().getName() + ": " + getCastle().getSiegeDate().getTime());
		
		loadSiegeClan();
		
		// Schedule siege auto start
		if (scheduledStartSiegeTask != null) {
			scheduledStartSiegeTask.cancel(false);
		}
		scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(getCastle()), 1000);
	}
	
	/**
	 * Teleport players
	 */
	public void teleportPlayer(TeleportWhoType teleportWho, MapRegionTable.TeleportWhereType teleportWhere) {
		List<Player> players;
		switch (teleportWho) {
			case Owner:
				players = getOwnersInZone();
				break;
			case Attacker:
				players = getAttackersInZone();
				break;
			case DefenderNotOwner:
				players = getDefendersButNotOwnersInZone();
				break;
			case Spectator:
				players = getSpectatorsInZone();
				break;
			default:
				players = getPlayersInZone();
		}
		
		for (Player player : players) {
			if (player.isGM() || player.isInJail()) {
				continue;
			}
			player.teleToLocation(teleportWhere);
		}
	}
	
	/**
	 * Add clan as attacker<BR><BR>
	 *
	 * @param clanId The int of clan's id
	 */
	private void addAttacker(int clanId) {
		getAttackerClans().add(new L2SiegeClan(clanId, SiegeClanType.ATTACKER)); // Add registered attacker to attacker list
	}
	
	/**
	 * Add clan as defender<BR><BR>
	 *
	 * @param clanId The int of clan's id
	 */
	private void addDefender(int clanId) {
		getDefenderClans().add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER)); // Add registered defender to defender list
	}
	
	/**
	 * <p>Add clan as defender with the specified type</p>
	 *
	 * @param clanId The int of clan's id
	 * @param type   the type of the clan
	 */
	private void addDefender(int clanId, SiegeClanType type) {
		getDefenderClans().add(new L2SiegeClan(clanId, type));
	}
	
	/**
	 * Add clan as defender waiting approval<BR><BR>
	 *
	 * @param clanId The int of clan's id
	 */
	private void addDefenderWaiting(int clanId) {
		getDefenderWaitingClans().add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER_PENDING)); // Add registered defender to defender list
	}
	
	/**
	 * Return true if the player can register.<BR><BR>
	 *
	 * @param player The Player of the player trying to register
	 * @param typeId -1 = owner 0 = defender, 1 = attacker, 2 = defender waiting
	 */
	private boolean checkIfCanRegister(Player player, byte typeId) {
		if (getIsRegistrationOver()) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DEADLINE_FOR_SIEGE_S1_PASSED);
			sm.addString(getCastle().getName());
			player.sendPacket(sm);
		} else if (getIsInProgress()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2));
		} else if (player.getClan() == null || player.getClan().getLevel() < CastleSiegeManager.getInstance().getSiegeClanMinLevel()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_CLAN_LEVEL_5_ABOVE_MAY_SIEGE));
		} else if (player.getClan().getHasCastle() > 0) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE));
		} else if (player.getClan().getClanId() == getCastle().getOwnerId()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING));
		} else if (CastleSiegeManager.getInstance().checkIsRegistered(player.getClan(), getCastle().getCastleId())) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE));
		} else if (checkIfAlreadyRegisteredForSameDay(player.getClan())) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE));
		} else if (typeId == ATTACKER && getAttackerClans().size() >= CastleSiegeManager.getInstance().getAttackerMaxClans()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACKER_SIDE_FULL));
		} else if ((typeId == DEFENDER || typeId == DEFENDER_NOT_APPROWED || typeId == OWNER) &&
				getDefenderClans().size() + getDefenderWaitingClans().size() >= CastleSiegeManager.getInstance().getDefenderMaxClans()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEFENDER_SIDE_FULL));
		} else {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Return true if the clan has already registered to a siege for the same day.<BR><BR>
	 *
	 * @param clan The L2Clan of the player trying to register
	 */
	public boolean checkIfAlreadyRegisteredForSameDay(L2Clan clan) {
		for (Siege siege : CastleSiegeManager.getInstance().getSieges()) {
			if (siege == this) {
				continue;
			}
			if (siege.getSiegeDate().get(Calendar.DAY_OF_WEEK) == getSiegeDate().get(Calendar.DAY_OF_WEEK)) {
				if (siege.checkIsAttacker(clan)) {
					return true;
				}
				if (siege.checkIsDefender(clan)) {
					return true;
				}
				if (siege.checkIsDefenderWaiting(clan)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Return the correct siege date as Calendar.<BR><BR>
	 */
	public void correctSiegeDateTime() {
		boolean corrected = false;
		
		if (getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
			// Since siege has past reschedule it to the next one
			// This is usually caused by server being down
			corrected = true;
			setNextSiegeDate();
		}

		/*if (!SevenSigns.getInstance().isDateInSealValidPeriod(getCastle().getSiegeDate()))
		{
			// no sieges in Quest period! reschedule it to the next SealValidationPeriod
			// This is usually caused by server being down
			corrected = true;
			setNextSiegeDate();
		}*/
		
		if (corrected) {
			saveSiegeDate();
		}
	}
	
	/**
	 * Load siege clans.
	 */
	private void loadSiegeClan() {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			getAttackerClans().clear();
			getDefenderClans().clear();
			getDefenderWaitingClans().clear();
			
			// Add castle owner as defender (add owner first so that they are on the top of the defender list)
			if (getCastle().getOwnerId() > 0) {
				addDefender(getCastle().getOwnerId(), SiegeClanType.OWNER);
			}
			
			ResultSet rs = null;
			
			con = DatabasePool.getInstance().getConnection();
			
			statement = con.prepareStatement("SELECT clan_id,type FROM siege_clans WHERE castle_id=?");
			statement.setInt(1, getCastle().getCastleId());
			rs = statement.executeQuery();
			
			int typeId;
			while (rs.next()) {
				typeId = rs.getInt("type");
				if (typeId == DEFENDER) {
					addDefender(rs.getInt("clan_id"));
				} else if (typeId == ATTACKER) {
					addAttacker(rs.getInt("clan_id"));
				} else if (typeId == DEFENDER_NOT_APPROWED) {
					addDefenderWaiting(rs.getInt("clan_id"));
				}
			}
		} catch (Exception e) {
			log.warn("Exception: loadSiegeClan(): " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}
	
	/**
	 * Remove all flags.
	 */
	private void removeFlags() {
		for (L2SiegeClan sc : getAttackerClans()) {
			if (sc != null) {
				sc.removeFlags();
			}
		}
		for (L2SiegeClan sc : getDefenderClans()) {
			if (sc != null) {
				sc.removeFlags();
			}
		}
	}
	
	/**
	 * Remove flags from defenders.
	 */
	private void removeDefenderFlags() {
		for (L2SiegeClan sc : getDefenderClans()) {
			if (sc != null) {
				sc.removeFlags();
			}
		}
	}
	
	/**
	 * Save castle siege related to database.
	 */
	private void saveCastleSiege() {
		setNextSiegeDate(); // Set the next set date for 2 weeks from now
		// Schedule Time registration end
		getTimeRegistrationOverDate().setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		getTimeRegistrationOverDate().add(Calendar.DAY_OF_MONTH, 1);
		getCastle().setIsTimeRegistrationOver(false);
		
		saveSiegeDate(); // Save the new date
		startAutoTask(); // Prepare auto start siege and end registration
	}
	
	/**
	 * Save siege date to database.
	 */
	public void saveSiegeDate() {
		if (scheduledStartSiegeTask != null) {
			scheduledStartSiegeTask.cancel(true);
			scheduledStartSiegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(getCastle()), 1000);
		}
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE castle SET siegeDate = ?, regTimeEnd = ?, regTimeOver = ?  WHERE id = ?");
			statement.setLong(1, getSiegeDate().getTimeInMillis());
			statement.setLong(2, getTimeRegistrationOverDate().getTimeInMillis());
			statement.setString(3, String.valueOf(getIsTimeRegistrationOver()));
			statement.setInt(4, getCastle().getCastleId());
			statement.execute();
		} catch (Exception e) {
			log.warn("Exception: saveSiegeDate(): " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}
	
	/**
	 * Save registration to database.<BR><BR>
	 *
	 * @param clan   The L2Clan of player
	 * @param typeId -1 = owner 0 = defender, 1 = attacker, 2 = defender waiting
	 */
	private void saveSiegeClan(L2Clan clan, byte typeId, boolean isUpdateRegistration) {
		if (clan.getHasCastle() > 0) {
			return;
		}
		
		Connection con = null;
		PreparedStatement statement = null;
		try {
			if (typeId == DEFENDER || typeId == DEFENDER_NOT_APPROWED || typeId == OWNER) {
				if (getDefenderClans().size() + getDefenderWaitingClans().size() >= CastleSiegeManager.getInstance().getDefenderMaxClans()) {
					return;
				}
			} else {
				if (getAttackerClans().size() >= CastleSiegeManager.getInstance().getAttackerMaxClans()) {
					return;
				}
			}
			
			con = DatabasePool.getInstance().getConnection();
			if (!isUpdateRegistration) {
				statement = con.prepareStatement("INSERT INTO siege_clans (clan_id,castle_id,type,castle_owner) VALUES (?,?,?,0)");
				statement.setInt(1, clan.getClanId());
				statement.setInt(2, getCastle().getCastleId());
				statement.setInt(3, typeId);
				statement.execute();
			} else {
				statement = con.prepareStatement("UPDATE siege_clans SET type = ? WHERE castle_id = ? AND clan_id = ?");
				statement.setInt(1, typeId);
				statement.setInt(2, getCastle().getCastleId());
				statement.setInt(3, clan.getClanId());
				statement.execute();
			}
			
			if (typeId == DEFENDER || typeId == OWNER) {
				addDefender(clan.getClanId());
			} else if (typeId == ATTACKER) {
				addAttacker(clan.getClanId());
			} else if (typeId == DEFENDER_NOT_APPROWED) {
				addDefenderWaiting(clan.getClanId());
			}
		} catch (Exception e) {
			log.warn("Exception: saveSiegeClan(L2Clan clan, int typeId, boolean isUpdateRegistration): " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}
	
	/**
	 * Set the date for the next siege.
	 */
	private void setNextSiegeDate() {
		while (getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
			if (getCastle().getSiegeDate().get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY &&
					getCastle().getSiegeDate().get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				getCastle().getSiegeDate().set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
			}
			// from CT2.3 Castle sieges are on Sunday, but if server admins allow to set day of the siege
			// than sieges can occur on Saturdays as well
			if (getCastle().getSiegeDate().get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && !Config.CL_SET_SIEGE_TIME_LIST.contains("day")) {
				getCastle().getSiegeDate().set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			}
			// set the next siege day to the next weekend
			getCastle().getSiegeDate().add(Calendar.DAY_OF_MONTH, 7);
		}
		
		//if (!SevenSigns.getInstance().isDateInSealValidPeriod(getCastle().getSiegeDate()) && !Config.isServer(Config.PVP))
		//	getCastle().getSiegeDate().add(Calendar.DAY_OF_MONTH, 7);
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ANNOUNCED_SIEGE_TIME);
		sm.addString(getCastle().getName());
		Announcements.getInstance().announceToAll(sm);
		
		isRegistrationOver = false; // Allow registration for next siege
	}
	
	/**
	 * Spawn siege guard.<BR><BR>
	 */
	private void spawnSiegeGuard() {
		getSiegeGuardManager().spawnSiegeGuard();
		
		// Register guard to the closest Control Tower
		// When CT dies, so do all the guards that it controls
		if (!getSiegeGuardManager().getSiegeGuardSpawn().isEmpty()) {
			ControlTowerInstance closestCt;
			int x, y, z;
			double distance;
			double distanceClosest = 0;
			for (L2Spawn spawn : getSiegeGuardManager().getSiegeGuardSpawn()) {
				if (spawn == null) {
					continue;
				}
				
				closestCt = null;
				distanceClosest = Integer.MAX_VALUE;
				
				x = spawn.getX();
				y = spawn.getY();
				z = spawn.getZ();
				
				for (L2Spawn ct : SpawnTable.getInstance().getSpecificSpawns(getCastle().getName() + "_control_tower")) {
					if (ct == null) {
						continue;
					}
					
					distance = ct.getNpc().getDistanceSq(x, y, z);
					
					if (distance < distanceClosest) {
						closestCt = (ControlTowerInstance) ct.getNpc();
						distanceClosest = distance;
					}
				}
				if (closestCt != null) {
					closestCt.registerGuard(spawn);
				}
			}
		}
	}
	
	@Override
	public final L2SiegeClan getAttackerClan(L2Clan clan) {
		if (clan == null) {
			return null;
		}
		return getAttackerClan(clan.getClanId());
	}
	
	@Override
	public final L2SiegeClan getAttackerClan(int clanId) {
		for (L2SiegeClan sc : getAttackerClans()) {
			if (sc != null && sc.getClanId() == clanId) {
				return sc;
			}
		}
		return null;
	}
	
	@Override
	public final List<L2SiegeClan> getAttackerClans() {
		if (isNormalSide) {
			return attackerClans;
		}
		return defenderClans;
	}
	
	public final int getAttackerRespawnDelay() {
		return CastleSiegeManager.getInstance().getAttackerRespawnDelay();
	}
	
	public final Castle getCastle() {
		if (castle == null || castle.length <= 0) {
			return null;
		}
		return castle[0];
	}
	
	@Override
	public final L2SiegeClan getDefenderClan(L2Clan clan) {
		if (clan == null) {
			return null;
		}
		return getDefenderClan(clan.getClanId());
	}
	
	@Override
	public final L2SiegeClan getDefenderClan(int clanId) {
		for (L2SiegeClan sc : getDefenderClans()) {
			if (sc != null && sc.getClanId() == clanId) {
				return sc;
			}
		}
		return null;
	}
	
	@Override
	public final List<L2SiegeClan> getDefenderClans() {
		if (isNormalSide) {
			return defenderClans;
		}
		return attackerClans;
	}
	
	public final L2SiegeClan getDefenderWaitingClan(L2Clan clan) {
		if (clan == null) {
			return null;
		}
		return getDefenderWaitingClan(clan.getClanId());
	}
	
	public final L2SiegeClan getDefenderWaitingClan(int clanId) {
		for (L2SiegeClan sc : getDefenderWaitingClans()) {
			if (sc != null && sc.getClanId() == clanId) {
				return sc;
			}
		}
		return null;
	}
	
	public final List<L2SiegeClan> getDefenderWaitingClans() {
		return defenderWaitingClans;
	}
	
	public final boolean getIsInProgress() {
		return isInProgress;
	}
	
	public final boolean getIsRegistrationOver() {
		return isRegistrationOver;
	}
	
	public final boolean getIsTimeRegistrationOver() {
		return getCastle().getIsTimeRegistrationOver();
	}
	
	@Override
	public final Calendar getSiegeDate() {
		return getCastle().getSiegeDate();
	}
	
	public final Calendar getTimeRegistrationOverDate() {
		return getCastle().getTimeRegistrationOverDate();
	}
	
	public void endTimeRegistration(boolean automatic) {
		getCastle().setIsTimeRegistrationOver(true);
		if (!automatic) {
			saveSiegeDate();
		}
	}
	
	@Override
	public List<Npc> getFlag(L2Clan clan) {
		if (clan != null) {
			L2SiegeClan sc = getAttackerClan(clan);
			if (sc != null) {
				return sc.getFlag();
			}
		}
		return null;
	}
	
	public final SiegeGuardManager getSiegeGuardManager() {
		if (siegeGuardManager == null) {
			siegeGuardManager = new SiegeGuardManager(getCastle());
		}
		return siegeGuardManager;
	}
	
	public int getControlTowerCount() {
		return controlTowerCount;
	}
	
	public void disableTraps() {
		flameTowerCount--;
	}
	
	/**
	 * @return boolean - traps are active
	 */
	public boolean isTrapsActive() {
		//return true;
		return flameTowerCount > 0;
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.model.entity.Siegable#giveFame()
	 */
	@Override
	public boolean giveFame() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.model.entity.Siegable#getFameFrequency()
	 */
	@Override
	public int getFameFrequency() {
		return Config.CASTLE_ZONE_FAME_TASK_FREQUENCY;
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.model.entity.Siegable#getFameAmount()
	 */
	@Override
	public int getFameAmount() {
		return Config.CASTLE_ZONE_FAME_AQUIRE_POINTS;
	}
}
