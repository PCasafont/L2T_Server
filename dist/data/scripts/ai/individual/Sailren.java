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

package ai.individual;

import ai.group_template.L2AttackableAIScript;
import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.GrandBossInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.model.zone.type.BossZone;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.SpecialCamera;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class Sailren extends L2AttackableAIScript {
	private static final int MANAGER = 32109;
	private static final int CUBE = 32107;

	private static final int SAILREN = 29065;
	private static final int VELO = 22223;
	private static final int PTERO = 22199;
	private static final int TREX = 22217;

	private static final int GUARD1 = 22196; // Velociraptor
	private static final int GUARD2 = 22199; // Pterosaur
	private static final int GUARD3 = 22217; // Tyrannosaurus

	//Locations
	private static final int SAILREN_X = 27333;
	private static final int SAILREN_Y = -6835;
	private static final int SAILREN_Z = -1970;

	private static final int MOBS_X = 27213;
	private static final int MOBS_Y = -6539;
	private static final int MOBS_Z = -1976;

	private static final int SPAWN_X = 27190;
	private static final int SPAWN_Y = -7163;
	private static final int SPAWN_Z = -1968;

	//requirements
	private static final int REQUIRED_ITEM = 8784;
	private static final int MIN_PLAYERS = 2;
	private static final int MAX_PLAYERS = 9;
	private static final int MIN_LEVEL = 70;

	//SAILREN Status Tracking :
	private static final byte DORMANT = 0; //SAILREN is spawned and no one has entered yet. Entry is unlocked
	private static final byte FIGHTING = 1; //SAILREN is engaged in battle, annihilating his foes. Entry is locked
	private static final byte DEAD = 2; //SAILREN has been killed. Entry is locked

	private BossZone Zone = null;

	private List<Player> playersInside = new ArrayList<Player>();
	private ArrayList<Integer> allowedPlayers = new ArrayList<Integer>();

	// Task
	protected ScheduledFuture<?> activityCheckTask = null;
	protected long LastAction = 0;
	private static final int INACTIVITYTIME = 900000;

	private List<Npc> velos;

	private static int[] MOBS = {SAILREN, GUARD1, GUARD2, GUARD3, 22198, 22223, 22218};

	public Sailren(int questId, String name, String descr) {
		super(questId, name, descr);

		addStartNpc(MANAGER);
		addTalkId(MANAGER);
		addTalkId(CUBE);
		for (int mob : MOBS) {
			addKillId(mob);
			addAttackId(mob);
		}
		Zone = GrandBossManager.getInstance().getZone(SPAWN_X, SPAWN_Y, SPAWN_Z);
		StatsSet info = GrandBossManager.getInstance().getStatsSet(SAILREN);
		int status = GrandBossManager.getInstance().getBossStatus(SAILREN);
		if (status == DEAD) {
			long temp = info.getLong("respawn_time") - System.currentTimeMillis();
			if (temp > 0) {
				startQuestTimer("SAILREN_unlock", temp, null, null);
			} else {
				GrandBossManager.getInstance().setBossStatus(SAILREN, DORMANT);
			}
		} else if (status != DORMANT) {
			GrandBossManager.getInstance().setBossStatus(SAILREN, DORMANT);
		}
	}

	private boolean checkConditions(Player player) {
		if (player.getInventory().getItemByItemId(REQUIRED_ITEM) == null) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_REQUIRED_ITEMS);
			player.sendPacket(sm);
			return false;
		}

		L2Party party = player.getParty();

		if (party == null) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			player.sendPacket(sm);
			return false;
		} else if (party.getLeader() != player) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			player.sendPacket(sm);
			return false;
		} else if (party.getMemberCount() < MIN_PLAYERS || party.getMemberCount() > MAX_PLAYERS) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			player.sendPacket(sm);
			return false;
		}

		for (Player partyMember : party.getPartyMembers()) {

			if (partyMember == null) {
				continue;
			} else if (partyMember.getLevel() < MIN_LEVEL) {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			} else if (!Util.checkIfInRange(1000, player, partyMember, true)) {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}
		}

		return true;
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		if (event.equalsIgnoreCase("start")) {
			velos = new ArrayList<Npc>();
			int x, y;
			Npc temp;
			for (int i = 0; i < 3; i++) {
				x = SAILREN_X + Rnd.get(100);
				y = SAILREN_Y + Rnd.get(100);
				temp = this.addSpawn(VELO, x, y, MOBS_Z, 0, false, 0);
				temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				temp.setRunning();
				velos.add(temp);
			}
		} else if (event.equalsIgnoreCase("spawn")) {
			GrandBossInstance Sailren = (GrandBossInstance) addSpawn(SAILREN, SAILREN_X, SAILREN_Y, SAILREN_Z, 27306, false, 0);
			GrandBossManager.getInstance().addBoss(Sailren);

			Zone.broadcastPacket(new SpecialCamera(Sailren.getObjectId(), 300, 275, 0, 1200, 10000));
			Zone.broadcastPacket(new MagicSkillUse(Sailren, Sailren, 5090, 1, 10000, 0, 0));
		} else if (event.equalsIgnoreCase("despawn")) {
			if (npc != null) {
				npc.deleteMe();
			}

			Zone.oustAllPlayers();
		} else if (event.equalsIgnoreCase("Sailren_unlock")) {
			GrandBossManager.getInstance().setBossStatus(SAILREN, DORMANT);
		}

		return null;
	}

	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet) {
		LastAction = System.currentTimeMillis();

		return null;
	}

	private class CheckActivity implements Runnable {
		@Override
		public void run() {
			long temp = System.currentTimeMillis() - LastAction;
			if (temp > INACTIVITYTIME) {
				GrandBossManager.getInstance().setBossStatus(SAILREN, DORMANT);
				activityCheckTask.cancel(false);
				allowedPlayers.clear();
				playersInside.clear();
			}
		}
	}

	@Override
	public String onTalk(Npc npc, Player player) {
		String htmltext = "";
		if (npc.getNpcId() == MANAGER) {
			if (GrandBossManager.getInstance().getBossStatus(SAILREN) == DORMANT) {
				if (!checkConditions(player)) {
					return htmltext;
				} else {
					Item item = player.getInventory().getItemByItemId(REQUIRED_ITEM);
					player.getInventory().destroyItem("Sailren AI", item, 1, null, null);

					GrandBossManager.getInstance().setBossStatus(SAILREN, FIGHTING);

					for (Player member : player.getParty().getPartyMembers()) {
						if (member != null) {
							allowedPlayers.add(member.getObjectId());
						}
					}

					Zone.setAllowedPlayers(allowedPlayers);

					for (Player member : player.getParty().getPartyMembers()) {
						if (member != null) {
							member.teleToLocation(SPAWN_X + Rnd.get(50), SPAWN_Y + Rnd.get(50), SPAWN_Z, true);
							if (member.getPet() != null) {
								member.getPet().teleToLocation(SPAWN_X + Rnd.get(50), SPAWN_Y + Rnd.get(50), SPAWN_Z, true);
							}
							for (SummonInstance summon : member.getSummons()) {
								summon.teleToLocation(SPAWN_X + Rnd.get(50), SPAWN_Y + Rnd.get(50), SPAWN_Z, true);
							}
							playersInside.add(member);
							Zone.allowPlayerEntry(member, 300);
						}
					}
					LastAction = System.currentTimeMillis();
					// Start repeating timer to check for inactivity
					activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);

					startQuestTimer("start", 120000, npc, player);
				}
			} else {
				htmltext = "<html><body>Someone else is already inside the Magic Force Field. Try again later.</body></html>";
			}
		} else if (npc.getNpcId() == CUBE) {
			if (player != null) {
				player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				if (player.getPet() != null) {
					player.getPet().teleToLocation(MapRegionTable.TeleportWhereType.Town);
				}
			}
		}

		return htmltext;
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isPet) {
		if (npc.getNpcId() == SAILREN) {
			activityCheckTask.cancel(false);
			GrandBossManager.getInstance().setBossStatus(SAILREN, DEAD);
			long respawnTime = (long) Config.SAILREN_INTERVAL_SPAWN + Rnd.get(Config.SAILREN_RANDOM_SPAWN);
			startQuestTimer("Sailren_unlock", respawnTime, npc, null);
			// also save the respawn time so that the info is maintained past reboots
			StatsSet info = GrandBossManager.getInstance().getStatsSet(SAILREN);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatsSet(SAILREN, info);
			Npc cube = addSpawn(CUBE, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
			startQuestTimer("despawn", 300000, cube, null);
		} else if (npc.getNpcId() == VELO) {
			if (velos == null) {
				return "";
			}
			velos.remove(npc);
			Player target = (Player) npc.getTarget();
			npc.deleteMe();
			if (velos.isEmpty()) {
				velos.clear();
				velos = null;
				Npc temp = this.addSpawn(PTERO, MOBS_X, MOBS_Y, MOBS_Z, 0, false, 0);
				temp.setTarget(target);
				temp.setRunning();
				temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		} else if (npc.getNpcId() == PTERO && npc.getTarget() instanceof Player) {
			Player target = (Player) npc.getTarget();
			npc.deleteMe();
			Npc temp = this.addSpawn(TREX, MOBS_X, MOBS_Y, MOBS_Z, 0, false, 0);
			temp.setTarget(target);
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			temp.setRunning();
		} else if (npc.getNpcId() == TREX) {
			npc.deleteMe();
			this.startQuestTimer("spawn", 300000, null, null);
		}

		return null;
	}

	public static void main(String[] args) {
		new Sailren(-1, "Sailren", "ai");
	}
}
