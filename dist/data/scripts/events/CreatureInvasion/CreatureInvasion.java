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

package events.CreatureInvasion;

import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.templates.item.WeaponTemplate;
import l2server.gameserver.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import l2server.util.Rnd;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LasTravel
 */
public class CreatureInvasion extends Quest {
	private static Logger log = LoggerFactory.getLogger(CreatureInvasion.class.getName());


	private static final boolean debug = false;
	private static final String qn = "CreatureInvasion";

	private static final int invasionDuration = 7; //Minutes
	private static final int[] weakCreatures = {13031, 13120};
	private static final int[] hardCreatures = {13123, 13034};
	private static final int[] strangeCreatures = {13035, 13124};
	private static final int[] bowSkillIds = {3260, 3262};
	private static final int bowId = 9141;
	private static final int bossId = 26123;
	private static final int[] allCreatureIds =
			{weakCreatures[0], weakCreatures[1], hardCreatures[0], hardCreatures[1], strangeCreatures[0], strangeCreatures[1]};
	private static Map<Integer, AttackInfo> attackInfo = new HashMap<Integer, AttackInfo>();
	private static Map<String, String> rewardedIps = new HashMap<String, String>();
	private static Map<String, List<DropChances>> dropInfo = new HashMap<String, List<DropChances>>();
	private static ArrayList<Npc> allCreatures = new ArrayList<Npc>();
	private static boolean isEventStarted;
	private static BossAttackInfo bossAttackInfo;

	public CreatureInvasion(int id, String name, String descr) {
		super(id, name, descr);

		addAttackId(bossId);
		addKillId(bossId);

		for (int i : allCreatureIds) {
			addAttackId(i);
			addKillId(i);
		}

		loadCreatureDrops();
	}

	private void loadCreatureDrops() {
		dropInfo.clear();
		File file = new File(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "scripts/events/CreatureInvasion/creatureDrops.xml");
		if (!file.exists()) {
			return;
		}

		XmlDocument doc = new XmlDocument(file);
		for (XmlNode d : doc.getChildren()) {
			if (d.getName().equalsIgnoreCase("drop")) {
				String category = d.getString("category");
				List<DropChances> dropChances = new ArrayList<DropChances>();

				for (XmlNode b : d.getChildren()) {
					int itemId = b.getInt("itemId");
					long min = b.getLong("min");
					long max = b.getLong("max");
					int chance = b.getInt("chance");
					dropChances.add(new DropChances(itemId, min, max, chance));
				}
				dropInfo.put(category, dropChances);
			}
		}
		log.info(getName() + ": Loaded " + dropInfo.size() + " drop categories!");
	}

	private class DropChances {
		private int itemId;
		private long minAmount;
		private long maxAmount;
		private int chance;

		private DropChances(int itemId, long minAmount, long maxAmount, int chance) {
			this.itemId = itemId;
			this.minAmount = minAmount;
			this.maxAmount = maxAmount;
			this.chance = chance;
		}

		private int getItemId() {
			return itemId;
		}

		private long getMinAmount() {
			return minAmount;
		}

		private long getMaxAmount() {
			return maxAmount;
		}

		private int getChance() {
			return chance;
		}
	}

	private class BossAttackInfo {
		private Npc boss;
		private Map<Integer, Long> registredDamages;

		private BossAttackInfo(Npc boss) {
			this.boss = boss;
			registredDamages = new HashMap<Integer, Long>();
		}

		private void deleteBoss() {
			if (boss != null) {
				boss.deleteMe();
			}
		}

		private void addDamage(int playerId, long damage) {
			synchronized (registredDamages) {
				if (!registredDamages.containsKey(playerId)) {
					registredDamages.put(playerId, damage);
				} else {
					registredDamages.put(playerId, registredDamages.get(playerId) + damage);
				}
			}
		}

		private void giveRewards() {
			synchronized (registredDamages) {
				for (Player player : World.getInstance().getAllPlayersArray()) {
					if (player == null || player.getInstanceId() != 0 || player.isInStoreMode() || !player.isInsideRadius(boss, 3000, false, false) ||
							registredDamages.get(player.getObjectId()) == null || registredDamages.get(player.getObjectId()) < 1000) {
						continue;
					}

					if (rewardedIps.containsKey(player.getExternalIP()) &&
							rewardedIps.get(player.getExternalIP()).equalsIgnoreCase(player.getInternalIP())) {
						continue;
					}

					rewardedIps.put(player.getExternalIP(), player.getInternalIP());

					for (DropChances i : dropInfo.get("boss")) {
						if (Rnd.get(100) < i.getChance()) {
							long amount = Rnd.get(i.getMinAmount(), i.getMaxAmount());
							player.addItem(getName(), i.getItemId(), amount, player, true);
							boss.broadcastChat(
									player.getName() + " received " + amount + " " + ItemTable.getInstance().getTemplate(i.getItemId()).getName() +
											"!", 0);
						}
					}
				}
				log.info(getName() + ": Rewarded: " + rewardedIps.size() + " players!");
			}
		}
	}

	private class AttackInfo {
		private Long attackedTime;
		private int playerId;
		private String externalIP;
		private String internalIP;

		private AttackInfo(int playerId, String externalIP, String internalIP) {
			this.playerId = playerId;
			this.externalIP = externalIP;
			this.internalIP = internalIP;
			setAttackedTime();
		}

		private long getAttackedTime() {
			return attackedTime;
		}

		private void setAttackedTime() {
			attackedTime = System.currentTimeMillis();
		}

		private int getPlayerId() {
			return playerId;
		}

		private String getExternalIP() {
			return externalIP;
		}

		private String getInternalIP() {
			return internalIP;
		}

		private void updateInfo(int playerId, String externalIP, String internalIP) {
			this.playerId = playerId;
			this.externalIP = externalIP;
			this.internalIP = internalIP;
			setAttackedTime();
		}
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		if (debug) {
			log.warn(getName() + ": onAdvEvent: " + event);
		}

		if (event.equalsIgnoreCase("start_invasion")) {
			if (!isEventStarted) {
				isEventStarted = true;

				Announcements.getInstance().announceToAll("The Creature Invasion has started!");
				for (Player pl : World.getInstance().getAllPlayersArray()) {
					if (pl == null || pl.getInstanceId() != 0 || pl.getEvent() != null || pl.getIsInsideGMEvent() || pl.inObserverMode() ||
							pl.isInOlympiadMode() || pl.isInStoreMode() || GrandBossManager.getInstance().getZone(pl) != null) {
						continue;
					}

					for (int i = 0; i <= Rnd.get(1, 2); i++) {
						spawnCreature(pl.getX(), pl.getY(), pl.getZ());
					}
				}
				startQuestTimer("spawn_boss", invasionDuration * 60000, null, null);
			} else {
				notifyEvent("end_event", null, null);
			}
		} else if (event.equalsIgnoreCase("spawn_boss")) {
			Npc boss = addSpawn(bossId, -114373, 252703, -1552, 65137, false, 0);
			bossAttackInfo = new BossAttackInfo(boss);

			Announcements.getInstance().announceToAll("The Golden Pig has appeared on Talking Island Village !");

			startQuestTimer("end_event", invasionDuration * 60000, null, null);
		} else if (event.equalsIgnoreCase("end_event")) {
			if (isEventStarted) {
				QuestTimer timer = getQuestTimer("spawn_boss", null, null);
				if (timer != null) {
					timer.cancel();
				}

				if (bossAttackInfo != null) {
					bossAttackInfo.deleteBoss();
				}

				synchronized (allCreatures) {
					for (Npc creature : allCreatures) {
						if (creature == null) {
							continue;
						}
						creature.deleteMe();
					}
				}

				isEventStarted = false;
				bossAttackInfo = null;
				attackInfo.clear();
				allCreatures.clear();
				rewardedIps.clear();

				Announcements.getInstance().announceToAll("The Creature Invasion has been ended!");
			}
		}
		return "";
	}

	@Override
	public String onAttack(Npc npc, Player player, int damage, boolean isPet, Skill skill) {
		if (debug) {
			log.warn(getName() + ": onAttack: " + npc.getName());
		}

		if (player.getSummons() != null) {
			for (Summon s : player.getSummons()) {
				s.unSummon(player);
			}
		}

		if (player.getPet() != null) {
			player.getPet().unSummon(player);
		}

		if (!isEventStarted) {
			npc.deleteMe();
		}

		if (!isValidAttack(player, skill, npc)) {
			if (!player.isGM() && player.isInsideZone(Creature.ZONE_PEACE)) {
				player.doDie(null);
			}

			return "";
		}

		if (Util.contains(allCreatureIds, npc.getNpcId())) {
			synchronized (attackInfo) {
				AttackInfo attackInfo = this.attackInfo.get(npc.getObjectId());

				int sameIPs = 0;
				int underAttack = 0;

				for (Map.Entry<Integer, AttackInfo> info : this.attackInfo.entrySet()) {
					if (info == null) {
						continue;
					}

					AttackInfo i = info.getValue();
					if (i == null) {
						continue;
					}

					if (System.currentTimeMillis() < i.getAttackedTime() + 7000) {
						if (i.getPlayerId() == player.getObjectId()) {
							underAttack++;
						}

						if (i.getExternalIP().equalsIgnoreCase(player.getExternalIP()) &&
								i.getInternalIP().equalsIgnoreCase(player.getInternalIP())) {
							sameIPs++;
						}

						if (underAttack > 1 || sameIPs > 1) {
							player.doDie(npc);

							if (underAttack > 1) {
								player.sendPacket(new NpcSay(npc.getObjectId(),
										2,
										npc.getTemplate().TemplateId,
										player.getName() + " you cant attack more than one mob at same time!"));
							}

							if (sameIPs > 1) {
								player.sendPacket(new NpcSay(npc.getObjectId(),
										2,
										npc.getTemplate().TemplateId,
										player.getName() + " dualbox is not allowed here!"));
							}
							return "";
						}
					}
				}

				if (attackInfo == null) {
					attackInfo = new AttackInfo(player.getObjectId(), player.getExternalIP(), player.getInternalIP());
					this.attackInfo.put(npc.getObjectId(), attackInfo);
				} else {
					//Already exists information for this NPC
					//Check if the attacker is the same as the stored
					if (attackInfo.getPlayerId() != player.getObjectId()) {
						//The attacker is not same
						//If the last attacked stored info +10 seconds is bigger than the current time, this mob is currently attacked by someone
						if (attackInfo.getAttackedTime() + 7000 > System.currentTimeMillis()) {
							player.doDie(null);
							player.sendPacket(new NpcSay(npc.getObjectId(),
									2,
									npc.getTemplate().TemplateId,
									player.getName() + " don't attack mobs from other players!"));
							return "";
						} else {
							//Add new information, none is currently attacking this NPC
							attackInfo.updateInfo(player.getObjectId(), player.getExternalIP(), player.getInternalIP());
						}
					} else {
						//player id is the same, update the attack time
						attackInfo.setAttackedTime();
					}
				}
			}
		} else if (npc.getNpcId() == bossId) {
			bossAttackInfo.addDamage(player.getObjectId(), damage);
		}
		return super.onAttack(npc, player, damage, isPet);
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isPet) {
		if (debug) {
			log.warn(getName() + ": onKill: " + npc.getName());
		}

		if (!isEventStarted) {
			log.warn(killer.getName() + ": is killing creatures out of the event...!");
			return "";
		}

		if (Util.contains(allCreatureIds, npc.getNpcId())) {
			synchronized (attackInfo) {
				AttackInfo info = attackInfo.get(npc.getObjectId()); //Get the attack info
				if (info != null) {
					attackInfo.remove(npc.getObjectId()); //Delete the stored info for this npc
				}

				spawnCreature(killer.getX(), killer.getY(), killer.getZ());

				//TODO Reward the player based on his playerLevel
				if (isValidAttack(killer, killer.getLastSkillCast(), npc)) {
					String dropType = "newPlayer";
					if (killer.getOnlineTime() > 10 * 3600) {
						dropType = "oldPlayer";
					}

					int a = 0;
					for (DropChances i : dropInfo.get(dropType)) {
						int dropChance = i.getChance();
						long maxAmount = i.getMaxAmount();

						if (Util.contains(strangeCreatures, npc.getNpcId())) {
							dropChance *= 1.2;
							maxAmount *= 1.2;
						}

						if (Rnd.get(100) < dropChance) {
							if (a == 3) {
								break;
							}
							killer.addItem(getName(), i.getItemId(), Rnd.get(i.getMinAmount(), maxAmount), killer, true);
						}
					}
				}
			}
		} else if (npc.getNpcId() == bossId) {
			bossAttackInfo.giveRewards();

			QuestTimer q = getQuestTimer("end_event", null, null);
			if (q != null) {
				q.cancel();
			}

			notifyEvent("end_event", null, null);
		}
		return super.onKill(npc, killer, isPet);
	}

	private void spawnCreature(int x, int y, int z) {
		if (bossAttackInfo != null) {
			return;
		}

		Npc creature = addSpawn(getCreatureId(), x, y, z + 5, 0, true, 0);
		synchronized (allCreatures) {
			allCreatures.add(creature);
		}
	}

	private boolean isValidAttack(Player player, Skill skill, Npc npc) {
		if (player == null) {
			return false;
		}

		WeaponTemplate playerWeapon = player.getActiveWeaponItem();
		if (playerWeapon == null || playerWeapon.getItemId() != bowId) {
			player.sendPacket(new NpcSay(npc.getObjectId(),
					2,
					npc.getTemplate().TemplateId,
					player.getName() + " You should use the Redemption Bow, buy it from Charlotte!"));
			return false;
		}

		if (skill == null || !Util.contains(bowSkillIds, skill.getId())) {
			if (skill != null && skill.hasEffects()) {
				Abnormal abn = npc.getFirstEffect(skill.getId());
				if (abn != null) {
					abn.exit();
				}
			}

			player.sendPacket(new NpcSay(npc.getObjectId(),
					2,
					npc.getTemplate().TemplateId,
					player.getName() + " You should use the Redemption Bow Skills!"));
			return false;
		}
		return true;
	}

	private int getCreatureId() {
		int rnd = Rnd.get(1, 100);
		if (rnd > 10) {
			return weakCreatures[Rnd.get(weakCreatures.length)];
		} else if (rnd > 3) {
			return hardCreatures[Rnd.get(hardCreatures.length)];
		} else {
			return strangeCreatures[Rnd.get(strangeCreatures.length)];
		}
	}

	@Override
	public int getOnKillDelay(int npcId) {
		return 0;
	}

	public static void main(String[] args) {
		new CreatureInvasion(-1, qn, "events");
	}
}
