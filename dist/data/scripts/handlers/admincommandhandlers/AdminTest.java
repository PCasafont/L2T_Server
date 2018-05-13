/*
 *
 * $Author: luisantonioa $
 * $Date: 25/07/2005 17:15:21 $
 * $Revision: 1 $
 * $Log: AdminTest.java,v $
 * Revision 1  25/07/2005 17:15:21  luisantonioa
 * Added copyright notice
 *
 *
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

package handlers.admincommandhandlers;

import l2server.Config;
import l2server.DatabasePool;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.*;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.instancemanager.CustomAuctionManager;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.GuardInstance;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.RaidBossInstance;
import l2server.gameserver.model.actor.stat.PcStat;
import l2server.gameserver.model.entity.ClanWarManager;
import l2server.gameserver.model.entity.ClanWarManager.ClanWar;
import l2server.gameserver.model.olympiad.Olympiad;
import l2server.gameserver.model.olympiad.OlympiadNobleInfo;
import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.L2GameClient.GameClientState;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.gameserver.templates.item.ItemTemplate;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class AdminTest implements IAdminCommandHandler {
	private static Logger log = LoggerFactory.getLogger(AdminTest.class.getName());

	private static final String[] ADMIN_COMMANDS = {"admin_stats", "admin_skill_test", "admin_known", "admin_test", "admin_do"};

	private List<NpcTemplate> npcTemplates = new ArrayList<NpcTemplate>();
	private List<Location> coords = new ArrayList<Location>();

	/* (non-Javadoc)
	 * @see l2server.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, l2server.gameserver.model.Player)
	 */
	@Override
	public boolean useAdminCommand(String command, Player activeChar) {
		StringTokenizer st = new StringTokenizer(command);

		st.nextToken();

		if (command.equals("admin_stats")) {
			for (String line : ThreadPoolManager.getInstance().getStats()) {
				activeChar.sendMessage(line);
			}
		} else if (command.startsWith("admin_skill_test") || command.startsWith("admin_st")) {
			try {
				int id = Integer.parseInt(st.nextToken());
				if (command.startsWith("admin_skill_test")) {
					adminTestSkill(activeChar, id, true);
				} else {
					adminTestSkill(activeChar, id, false);
				}
			} catch (NumberFormatException e) {
				activeChar.sendMessage("Command format is //skill_test <ID>");
			} catch (NoSuchElementException nsee) {
				activeChar.sendMessage("Command format is //skill_test <ID>");
			}
		} else if (command.equals("admin_known on")) {
			Config.CHECK_KNOWN = true;
		} else if (command.equals("admin_known off")) {
			Config.CHECK_KNOWN = false;
		} else if (command.toLowerCase().startsWith("admin_test")) {
			//TradeController.getInstance().reload();
			//activeChar.sendMessage("Shops have been successfully reloaded!");
		} else if (command.startsWith("admin_do")) {
			if (!st.hasMoreTokens()) {
				activeChar.sendMessage("You forgot to tell me what to execute. Ex: onExecute InventoryToMultisell");
				return false;
			}

			String secondaryCommand = st.nextToken();

			if (secondaryCommand.equals("TeleportAllPlayersToMe")) {
				for (Player player : World.getInstance().getAllPlayersArray()) {
					player.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ());
				}
			} else if (secondaryCommand.equals("RefreshVisualEffects")) {
				activeChar.sendPacket(new ExUserEffects(activeChar));
			} else if (secondaryCommand.equals("shiet")) {
				for (NpcTemplate temp : NpcTable.getInstance().getAllTemplates()) {
					if (!temp.getBaseSet().getBool("overrideSpawns", false)) {
						Util.logToFile("<npc id=\"" + temp.NpcId + "\" overrideSpawns=\"true\" /> <!-- " + temp.Name + " -->",
								"overriddenSpawns",
								"xml",
								true,
								false);
					}
				}

				activeChar.sendMessage("Done...");
			} else if (secondaryCommand.equals("FixAph")) {
				SpawnTable.getInstance().despawnSpecificTable("gainak_siege");

				SpawnTable.getInstance().spawnSpecificTable("gainak_siege");
			} else if (secondaryCommand.equals("ShowRaids")) {
				for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable()) {
					if (!(spawn.getNpc() instanceof RaidBossInstance)) {
						continue;
					}

					activeChar.sendMessage(spawn.getNpc().getName());
				}
			} else if (secondaryCommand.equals("ReloadMobRes")) {
				activeChar.sendMessage("reloaded.");
			} else if (secondaryCommand.equals("OlyFeex")) {
				Olympiad.getInstance().loadNoblesRank();

				activeChar.sendMessage("olyfeexed?.");
			} else if (secondaryCommand.equals("FindOlyAbuses")) {
				final int[] HERO_IDS = {
						// Regular Classes
						88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114,
						115, 116, 117, 118, 131, 132, 133, 134, 186, 187,

						// Awakened Classes
						148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172,
						173, 174, 175, 176, 177, 178, 179, 180, 181, 188, 189};

				String OLYMPIAD_GET_HEROES =
						"SELECT charId FROM olympiad_nobles " + "WHERE class_id = ? AND competitions_done >= 10 AND competitions_won > 0 " +
								"ORDER BY olympiad_points DESC, competitions_done DESC, competitions_won DESC";

				Connection con = null;

				try {
					con = DatabasePool.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement(OLYMPIAD_GET_HEROES);
					for (int classId : HERO_IDS) {
						String characterName = "";
						String accountName = "";
						@SuppressWarnings("unused") String lastIP = "";
						@SuppressWarnings("unused") String lastHWID = "";
						int charId = 0;
						@SuppressWarnings("unused") String className = PlayerClassTable.getInstance().getClassNameById(classId);

						statement.setInt(1, classId);
						ResultSet rset = statement.executeQuery();
						statement.clearParameters();

						if (rset.next()) {
							charId = rset.getInt("charId");
							OlympiadNobleInfo hero = Olympiad.getInstance().getNobleInfo(charId);

							activeChar.sendMessage(
									"Hero for " + PlayerClassTable.getInstance().getClassNameById(classId) + " = " + hero.getName() + ".");

							characterName = hero.getName();

							Connection con2 = null;
							try {
								con2 = DatabasePool.getInstance().getConnection();
								PreparedStatement statement2 = con.prepareStatement("SELECT * FROM characters WHERE char_name = ?");

								statement2.setString(1, characterName);
								ResultSet rset2 = statement2.executeQuery();
								statement2.clearParameters();

								if (rset2.next()) {
									accountName = rset2.getString("account_name");
								}

								rset2.close();
								statement2.close();
							} catch (SQLException e) {
								log.warn("ERR 1");
							} finally {
								DatabasePool.close(con2);
							}

							try {
								con2 = DatabasePool.getInstance().getConnection();
								PreparedStatement statement2 = con2.prepareStatement("SELECT * FROM accounts WHERE login = ?");

								statement2.setString(1, accountName);
								ResultSet rset2 = statement2.executeQuery();
								statement2.clearParameters();

								if (rset2.next()) {
									lastIP = rset2.getString("lastIP");
								}

								rset2.close();
								statement2.close();
							} catch (SQLException e) {
								log.warn("ERR 2");
							} finally {
								DatabasePool.close(con2);
							}
						}

						rset.close();
					}
					statement.close();
				} catch (SQLException e) {
					log.warn("Couldn't load heros from DB");
				} finally {
					DatabasePool.close(con);
				}
			} else if (secondaryCommand.equals("DeleteIstina")) {
				WorldObject target = activeChar.getTarget();

				InstanceManager.getInstance().deleteInstanceTime(target.getObjectId(), 169);
			} else if (secondaryCommand.equals("GoToTarget")) {
				WorldObject target = activeChar.getTarget();

				activeChar.teleToLocation(target.getX(), target.getY(), target.getZ());
			} else if (secondaryCommand.equals("FakeCast")) {
				WorldObject target = activeChar.getTarget();

				if (target instanceof MonsterInstance) {
					final MonsterInstance monster = (MonsterInstance) target;
					//(Creature cha, int skillId, int skillLevel, WorldObject[] targets)
					activeChar.sendPacket(new MagicSkillLaunched(monster, 5082, 1, new WorldObject[]{activeChar, activeChar.getSummons().get(0)}));
				}
			} else if (secondaryCommand.equals("DeleteWar")) {
				L2Clan equinox = ClanTable.getInstance().getClanByName("Equinox");
				L2Clan illuminati = ClanTable.getInstance().getClanByName("SaintsOfBots");

				ClanWarManager.getInstance().getWar(equinox, illuminati).delete();

				activeChar.sendMessage("Done.");
			} else if (secondaryCommand.equals("FixOlyToon")) {
				final Player target = activeChar.getTarget().getActingPlayer();

				Olympiad.getInstance().removeNoble(target.getObjectId());

				activeChar.sendMessage("DONE.");
			} else if (secondaryCommand.equals("CumToMe")) {
				WorldObject target = activeChar.getTarget();

				if (target instanceof Creature) {
					Creature targetedCharacter = (Creature) target;

					targetedCharacter.getAI()
							.setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
									new L2CharPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 0));
				}
			} else if (secondaryCommand.equals("GetMovin")) {
				WorldObject target = activeChar.getTarget();

				if (target instanceof Creature) {
					Creature targetedCharacter = (Creature) target;

					// Giran Weapon Shop
					targetedCharacter.getAI()
							.setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
									new L2CharPosition(79778 + Rnd.get(-100, 100), 146671 + Rnd.get(-100, 100), -3515, 0));

					// Giran Grocery
					//targetedCharacter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(80434, 147896, -3504, 0));
				}
			} else if (secondaryCommand.equals("GetMova")) {
				WorldObject target = activeChar.getTarget();

				if (target instanceof Creature) {
					Creature targetedCharacter = (Creature) target;

					// Giran Grocery
					targetedCharacter.getAI()
							.setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
									new L2CharPosition(79671 + Rnd.get(-250, 250), 147530 + Rnd.get(-250, 250), -3504, 0));
				}
			} else if (secondaryCommand.equals("DropHerb")) {
				Item droppedItem = null;

				int buffHerbs = 50107 + Rnd.get(-7, 10);
				@SuppressWarnings("unused") int buffHerbsChance = 20;

				// Hunter Deathmatch - PP/EE/SE Buff Herbs...
				droppedItem = ItemTable.getInstance().createItem("EventReward", buffHerbs, 1, activeChar, activeChar);
				droppedItem.dropMe(activeChar, activeChar.getX() + Rnd.get(-50, 50), activeChar.getY() + Rnd.get(-50, 50), activeChar.getZ());

				droppedItem = ItemTable.getInstance().createItem("EventReward", 8156, 1, activeChar, activeChar);
				droppedItem.dropMe(activeChar, activeChar.getX() + Rnd.get(-50, 50), activeChar.getY() + Rnd.get(-50, 50), activeChar.getZ());
			} else if (secondaryCommand.equals("SetAgathion")) {
				final int agathionId = Integer.parseInt(st.nextToken());

				activeChar.setAgathionId(agathionId);
				activeChar.broadcastUserInfo();
			} else if (secondaryCommand.equals("StartAuction")) {
				final int auctionId = Integer.parseInt(st.nextToken());

				CustomAuctionManager.getInstance().startAuction(auctionId);

				activeChar.sendMessage("Started the Auction " + auctionId + ".");
			} else if (secondaryCommand.equals("EndAuction")) {
				final int auctionId = Integer.parseInt(st.nextToken());

				activeChar.sendMessage("Ended the Auction " + auctionId + ".");
			} else if (secondaryCommand.equals("FixAuction")) {
				CustomAuctionManager.getInstance().tryToBid(activeChar, 268929159, 1, "Adena");
				activeChar.sendMessage("BLA");
			} else if (secondaryCommand.equals("SetObserver")) {
				if (activeChar.getTarget() instanceof Player) {
					final Player player = (Player) activeChar.getTarget();

					player.enterObserverMode(activeChar.getX(), activeChar.getY(), activeChar.getZ());
				}

				activeChar.sendMessage("Done.");
			} else if (secondaryCommand.equals("LeaveObserver")) {
				if (activeChar.getTarget() instanceof Player) {
					final Player player = (Player) activeChar.getTarget();

					player.leaveObserverMode();
				}

				activeChar.sendMessage("Done.");
			} else if (secondaryCommand.equals("TestTempLevel")) {
				activeChar.setTemporaryLevel((byte) 60);
				activeChar.setTemporaryLevel((byte) 0);
				activeChar.setTemporaryLevel((byte) 35);
			} else if (secondaryCommand.equals("NoFeed")) {
				if (!(activeChar.getTarget() instanceof Player)) {
					return false;
				}

				final Player target = (Player) activeChar.getTarget();

				Item item = activeChar.getInventory().getItemByItemId(6392);

				if (item != null) {
					activeChar.sendMessage("Destroying Medals...");
					target.getInventory().destroyItem("Admin", item, activeChar, activeChar);
				}

				item = activeChar.getInventory().getItemByItemId(6393);

				if (item != null) {
					activeChar.sendMessage("Destroying Glitt Medals...");
					target.getInventory().destroyItem("Admin", item, activeChar, activeChar);
				}

				item = activeChar.getInventory().getItemByItemId(6393);

				if (item != null) {
					activeChar.sendMessage("Destroying Noble Brooch...");
					target.getInventory().destroyItem("Admin", item, activeChar, activeChar);
				}
			} else if (secondaryCommand.equals("VisualEffect")) {
				for (Integer a : activeChar.getAbnormalEffect()) {
					activeChar.stopVisualEffect(a);
				}

				int id = Integer.parseInt(st.nextToken());

				activeChar.startVisualEffect(id);
				activeChar.sendPacket(new ExUserEffects(activeChar));

				if (activeChar.getTarget() instanceof Player) {
					final Player target = (Player) activeChar.getTarget();

					for (Integer a : target.getAbnormalEffect()) {
						target.stopVisualEffect(a);
					}

					target.startVisualEffect(id);
					target.sendPacket(new ExUserEffects(target));
				}
			} else if (secondaryCommand.equals("Social")) {
				for (Player player : World.getInstance().getAllPlayersArray()) {
					player.broadcastPacket(new SocialAction(player.getObjectId(), 33));
				}
			} else if (secondaryCommand.equals("MagicGem")) {
				for (Player player : World.getInstance().getAllPlayers().values()) {
					if (player.getInventory().getItemByItemId(1373) == null) {
						player.addItem("AdminDo", 1373, 1, activeChar, true);
						activeChar.sendMessage("Magic Gem given to " + player.getName());
					}
				}
			} else if (secondaryCommand.equals("CheckClones")) {
				for (WorldObject obj : activeChar.getKnownList().getKnownObjects().values()) {
					//if (Util.calculateDistance(obj, activeChar, false) > 50)
					//	continue;

					if (!(obj instanceof GuardInstance)) {
						continue;
					}

					activeChar.sendMessage("Found Object " + obj);
					activeChar.sendMessage("IsDecayed " + ((GuardInstance) obj).isDecayed());
					//obj.decayMe();
					break;
				}
			} else if (secondaryCommand.equals("TellEmVote")) {
				for (Player player : World.getInstance().getAllPlayers().values()) {
					//if (player.getInventory().getItemByItemId(15393) != null)
					//	continue;

					//if (!player.isGM())
					//	continue;

					//player.sendPacket(new CreatureSay(0x00, Say2.TELL, "Jonah", "Hey! you don't have a Vitality Belt. Come to me in Giran for one! unlimited vitality, better adena drops, auto-loot in boosted hunting grounds... you need a Vitality Belt!"));
					//player.sendPacket(new CreatureSay(0x00, Say2.TELL, "Jonah", "Kff! I noticed you don't have a Vitality Belt... only newbies run around without a Vitality Belt! Come to me in Giran for one!"));
					//player.sendPacket(new CreatureSay(0x00, Say2.TELL, "Lorain", "Hey! I'm now giving out Divine Protection Elixirs to mini-games participants! come get yours! join the mini game!"));
					player.sendPacket(new CreatureSay(0x00, Say2.SHOUT, "DarkCore", "I WILL LICK YOUR NUTS FOR NOBLE ITEMS, PM ME"));

					//activeChar.sendMessage("Said it to " + player.getName() + ".");
				}
			} else if (secondaryCommand.equals("TellHim")) {
				((Creature) activeChar.getTarget()).setInvul(false);
			} else if (secondaryCommand.equals("FixClanWars")) {
				for (L2Clan clan : ClanTable.getInstance().getClans()) {
					for (ClanWar war : clan.getWars()) {
						war.delete();
					}
				}

				activeChar.sendMessage("Fixed clan wars.");
			} else if (secondaryCommand.equals("Testur")) {
				activeChar.getClient().setDetached(true);
			} else if (secondaryCommand.equals("Vitality")) {
				int level = 4;
				int vitality = PcStat.MAX_VITALITY_POINTS / 4 * level;

				for (Player player : World.getInstance().getAllPlayersArray()) {
					player.setVitalityPoints(vitality, false, true);
				}

				activeChar.sendMessage("Done.");
			} else if (secondaryCommand.equals("EndOlympiads")) {
				Olympiad.getInstance().endOlympiads();
			} else if (secondaryCommand.equals("Crest")) {
				if (activeChar.getClan().getAllyCrestId() != 0) {
					activeChar.sendPacket(new AllyCrest(activeChar.getClan().getAllyCrestId()));
					activeChar.sendMessage("Crest sent.. ( " + activeChar.getClan().getAllyCrestId());
				}
			} else if (secondaryCommand.equals("GrabPosition")) {
				coords.add(new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ()));

				activeChar.sendMessage("Recorded current position.");
			} else if (secondaryCommand.equals("ClearPositions")) {
				coords.clear();

				activeChar.sendMessage("Cleared recorded positions.");
			} else if (secondaryCommand.equals("PrintPositions")) {
				for (Location l : coords) {
					System.out.println("<node X=\"" + l.getX() + "\" Y=\"" + l.getY() + "\" />");
				}
			} else if (secondaryCommand.equals("GrabNearbyMonsters")) {
				for (WorldObject obj : activeChar.getKnownList().getKnownObjects().values()) {
					if (obj instanceof MonsterInstance && !(obj instanceof RaidBossInstance)) {
						final MonsterInstance monster = (MonsterInstance) obj;

						if (npcTemplates.contains(monster.getTemplate())) {
							continue;
						}

						npcTemplates.add(monster.getTemplate());
						activeChar.sendMessage("Added " + monster.getName() + ".");
					}
				}
			} else if (secondaryCommand.equals("ClearNpcs")) {
				npcTemplates.clear();

				activeChar.sendMessage("Templates cleared.");
			} else if (secondaryCommand.equals("PrintDropsForNpcs")) {
				for (NpcTemplate npcTemplate : npcTemplates) {
					System.out.println("\t<npc id='" + npcTemplate.NpcId + "'> <!-- " + npcTemplate.getName() + " -->");
					System.out.println("\t\t<droplist>");
					System.out.println(
							"\t\t\t<item category='1' categoryChance='100' id='5572' minCount='2' maxCount='3' dropChance='100'/> <!--  Wind Mantra -->");
					System.out.println(
							"\t\t\t<item category='2' categoryChance='100' id='5570' minCount='1' maxCount='2' dropChance='100'/> <!--  Water Mantra -->");
					System.out.println(
							"\t\t\t<item category='3' categoryChance='100' id='5574' minCount='1' maxCount='1' dropChance='100'/> <!--  Fire Mantra -->");
					System.out.println("\t\t</droplist>");
					System.out.println("\t</npc>");
				}
			} else if (secondaryCommand.equals("InventoryToMultisell")) {
				String log = "<?xml version='1.0' encoding='utf-8'?>\n<list>\n";
				for (Item item : activeChar.getInventory().getItems()) {
					log += "\t<!-- " + item.getName() + " -->\n";
					log += "\t<item>\n";
					log += "\t\t<ingredient id=\"57\" count=\"1\" /> <!-- Adena -->\n";
					log += "\t\t<production id=\"" + item.getItemId() + "\" count=\"1\" />\n";
					log += "\t</item>\n";
				}

				log += "</list>";

				Util.logToFile(log, "InventoryToMultisell", "xml", false, false);
			} else if (secondaryCommand.equals("EpicTest")) {
				SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE d MMMMMMM");
				// new SimpleDateFormat("EEEE d MMMMMMM k:m:s:");

				long baiumRespawnTime = System.currentTimeMillis() + GrandBossManager.getInstance().getUnlockTime(29020);

				activeChar.sendMessage("Baium Respawn: " + dateFormatter.format(baiumRespawnTime));

				long earliestSpawnTime = 0;
				long latestSpawnTime = 0;

				String earliestSpawnTimeDay = "";
				String latestSpawnTimeDay = "";

				switch (Rnd.get(0, 2)) {
					case 0: {
						// Shows -1 +1
						earliestSpawnTime = baiumRespawnTime - 3600000;
						latestSpawnTime = baiumRespawnTime + 3600000;
						break;
					}
					case 1: {
						// Shows -2 0
						earliestSpawnTime = baiumRespawnTime - 2 * 3600000;
						latestSpawnTime = baiumRespawnTime;
						break;
					}
					case 2: {
						// Shows 0 +2
						earliestSpawnTime = baiumRespawnTime;
						latestSpawnTime = baiumRespawnTime + 2 * 3600000;
						break;
					}
				}

				earliestSpawnTimeDay = dateFormatter.format(earliestSpawnTime);
				latestSpawnTimeDay = dateFormatter.format(latestSpawnTime);

				dateFormatter = new SimpleDateFormat("k:m:s:");

				if (!earliestSpawnTimeDay.equals(latestSpawnTimeDay)) {
					activeChar.sendMessage(
							"Baium will be spawning between " + earliestSpawnTimeDay + " at " + dateFormatter.format(earliestSpawnTime) +
									" and the " + latestSpawnTimeDay + " at " + dateFormatter.format(latestSpawnTime) + ".");
				} else {
					activeChar.sendMessage(
							"Baium will be spawning on " + earliestSpawnTimeDay + " between " + dateFormatter.format(earliestSpawnTime) + " and " +
									dateFormatter.format(latestSpawnTime) + ".");
				}
			} else if (secondaryCommand.equals("SpawnMonsters")) {
				int fromId = Integer.parseInt(st.nextToken());
				int toId = Integer.parseInt(st.nextToken());

				//int x = activeChar.getX(), y = activeChar.getY(), z = activeChar.getZ();

				float headingAngle = (float) ((activeChar.getHeading() + Rnd.get(-15000, 15000)) * Math.PI) / Short.MAX_VALUE;

				int range = 50;

				float x = activeChar.getX() + range * (float) Math.cos(headingAngle);
				float y = activeChar.getY() + range * (float) Math.sin(headingAngle);
				float z = activeChar.getZ() + 1;

				@SuppressWarnings("unused") int spawnedMonsters = 0;
				for (int id = fromId; id < toId; id++) {
					L2Spawn spawn = null;

					NpcTemplate template = NpcTable.getInstance().getTemplate(id);
					try {
						spawn = new L2Spawn(template);
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}

					x = activeChar.getX() + range * (float) Math.cos(headingAngle);
					y = activeChar.getY() + range * (float) Math.sin(headingAngle);
					z = activeChar.getZ() + 1;

					spawn.setInstanceId(0);
					spawn.setHeading(Rnd.get(65535));
					spawn.setX((int) x);
					spawn.setY((int) y);
					spawn.setZ((int) z);
					spawn.stopRespawn();
					spawn.doSpawn(false);

					range += 50;
				}
			} else if (secondaryCommand.equals("CleanInventory")) {
				activeChar.getInventory().destroyAllItems("Admin", activeChar, activeChar);
			} else if (secondaryCommand.equals("GenerateBossWeapons")) {
				final int[] weaponsIds = {
						// Antharas
						36417, 36418, 36419, 36420, 36421, 36422, 36423, 36424, 36425, 36426,
						// Valakas
						36427, 36428, 36429, 36430, 36431, 36432, 36433,
						// Lindvior
						36434, 36435, 36436, 36437, 36438, 36439, 36440};

				for (int itemId : weaponsIds) {
					final ItemTemplate itemTemplate = ItemTable.getInstance().getTemplate(itemId);

					String baseItemName = itemTemplate.getName();
					String[] itemNames = new String[3];

					itemNames[0] = baseItemName.replace("Fragment", "Standard");
					itemNames[1] = baseItemName.replace("Fragment", "High-grade");
					itemNames[2] = baseItemName.replace("Fragment", "Top-grade");
					ItemTemplate[] itemTemplates = new ItemTemplate[3];

					activeChar.sendMessage("Base Item Name: " + baseItemName);
					activeChar.sendMessage("Will be looking for...:");
					for (String s : itemNames) {
						activeChar.sendMessage("- '" + s + "'");
					}

					for (ItemTemplate item : ItemTable.getInstance().getAllItems()) {
						if (item == null) {
							continue;
						}

						if (item.getName().equalsIgnoreCase(itemNames[0])) {
							itemTemplates[0] = item;
						}
						if (item.getName().equalsIgnoreCase(itemNames[1])) {
							itemTemplates[1] = item;
						}
						if (item.getName().equalsIgnoreCase(itemNames[2])) {
							itemTemplates[2] = item;
						}
					}

					for (ItemTemplate item : itemTemplates) {
						activeChar.sendMessage("Found Item: " + item.getName());
					}

					String toLog = "";
					/*
                    toLog += "\t<item>\n";
					toLog += "\t\t<ingredient id=\"50009\" count=\"250\" /> <!-- Raid Heart -->\n";
					toLog += "\t\t<production id=\"" + itemId + "\" count=\"1\" /> <!-- " + baseItemName + " -->\n";
					toLog += "\t\t<production id=\"" + itemId + "\" count=\"1\" chance=\"75\" /> <!-- " + baseItemName + " -->\n";
					toLog += "\t\t<production id=\"9143\" count=\"25\" chance=\"50\" /> <!-- Golden Apiga -->\n";
					toLog += "\t</item>\n";
					toLog += "\t<item>\n";
					toLog += "\t\t<ingredient id=\"" + itemId + "\" count=\"1\" /> <!-- " + baseItemName + " -->\n";
					toLog += "\t\t<ingredient id=\"50009\" count=\"100\" /> <!-- Raid Heart -->\n";
					toLog += "\t\t<production id=\"" + itemTemplates[0].getItemId() + "\" count=\"1\" /> <!-- " + itemTemplates[0].getName() + " -->\n";
					toLog += "\t\t<production id=\"" + itemTemplates[0].getItemId() + "\" count=\"1\" chance=\"50\" /> <!-- " + itemTemplates[0].getName() + " -->\n";
					toLog += "\t\t<production id=\"" + itemId + "\" count=\"1\" chance=\"50\" /> <!-- " + baseItemName + " -->\n";
					toLog += "\t</item>\n";
					toLog += "\t<item>\n";
					toLog += "\t\t<ingredient id=\"" + itemTemplates[0].getItemId() + "\" count=\"1\" /> <!-- " + itemTemplates[0].getName() + " -->\n";
					toLog += "\t\t<ingredient id=\"50009\" count=\"100\" /> <!-- Raid Heart -->\n";
					toLog += "\t\t<production id=\"" + itemTemplates[1].getItemId() + "\" count=\"1\" /> <!-- " + itemTemplates[1].getName() + " -->\n";
					toLog += "\t\t<production id=\"" + itemTemplates[1].getItemId() + "\" count=\"1\" chance=\"25\" /> <!-- " + itemTemplates[1].getName() + " -->\n";
					toLog += "\t\t<production id=\"" + itemTemplates[0].getItemId() + "\" count=\"1\" chance=\"75\" /> <!-- " + itemTemplates[0].getName() + " -->\n";
					toLog += "\t</item>\n";
					toLog += "\t<item>\n";
					toLog += "\t\t<ingredient id=\"" + itemTemplates[1].getItemId() + "\" count=\"1\" /> <!-- " + itemTemplates[1].getName() + " -->\n";
					toLog += "\t\t<ingredient id=\"50009\" count=\"100\" /> <!-- Raid Heart -->\n";
					toLog += "\t\t<production id=\"" + itemTemplates[2].getItemId() + "\" count=\"1\" /> <!-- " + itemTemplates[2].getName() + " -->\n";
					toLog += "\t\t<production id=\"" + itemTemplates[2].getItemId() + "\" count=\"1\" chance=\"10\" /> <!-- " + itemTemplates[2].getName() + " -->\n";
					toLog += "\t\t<production id=\"" + itemTemplates[1].getItemId() + "\" count=\"1\" chance=\"90\" /> <!-- " + itemTemplates[1].getName() + " -->\n";
					toLog += "\t</item>\n";
					 */

					/*
					toLog += "\t<item id=\"" + itemId + "\" type=\"Weapon\" name=\"" + baseItemName + "\" canBeUsedAsApp=\"true\" overrideStats=\"true\" overrideSkills=\"true\" />\n";
					toLog += "\t<item id=\"" + itemTemplates[0].getItemId() + "\" type=\"Weapon\" name=\"" + itemTemplates[0].getName() + "\" canBeUsedAsApp=\"true\" overrideStats=\"true\" overrideSkills=\"true\" />\n";
					toLog += "\t<item id=\"" + itemTemplates[1].getItemId() + "\" type=\"Weapon\" name=\"" + itemTemplates[1].getName() + "\" canBeUsedAsApp=\"true\" overrideStats=\"true\" overrideSkills=\"true\" />\n";
					toLog += "\t<item id=\"" + itemTemplates[2].getItemId() + "\" type=\"Weapon\" name=\"" + itemTemplates[2].getName() + "\" canBeUsedAsApp=\"true\" overrideStats=\"true\" overrideSkills=\"true\" />\n";
					 */

					toLog += "case " + itemId + ": // " + baseItemName + "\n";
					toLog += "case " + itemTemplates[0].getItemId() + ": // " + itemTemplates[0].getName() + "\n";
					toLog += "case " + itemTemplates[1].getItemId() + ": // " + itemTemplates[1].getName() + "\n";
					toLog += "case " + itemTemplates[2].getItemId() + ": // " + itemTemplates[2].getName() + "\n";

					Util.logToFile(toLog, "NewShoppos", "xml", true, false);
				}
			} else if (secondaryCommand.startsWith("Rape")) {
				if (!st.hasMoreTokens()) {
					activeChar.sendMessage("Input a monster ID...");
					return false;
				}

				int monsterId = Integer.parseInt(st.nextToken());

				if (!st.hasMoreTokens()) {
					activeChar.sendMessage("Input the amount of time to rape it...");
					return false;
				}

				int killCount = Integer.parseInt(st.nextToken());

				for (int i = 0; i < killCount; i++) {
					L2Spawn spawn = null;

					NpcTemplate template = NpcTable.getInstance().getTemplate(monsterId);
					try {
						spawn = new L2Spawn(template);
					} catch (Exception e) {
						e.printStackTrace();
					}

					spawn.setInstanceId(0);
					spawn.setHeading(Rnd.get(65535));
					spawn.setX(activeChar.getX() + Rnd.get(-50, 50));
					spawn.setY(activeChar.getY() + Rnd.get(-50, 50));
					spawn.setZ(activeChar.getZ());
					spawn.stopRespawn();
					spawn.doSpawn(false);

					Npc npc = spawn.getNpc();

					npc.reduceCurrentHp(npc.getMaxHp() + 1, activeChar, null);
				}
			} else if (secondaryCommand.equals("ShowSpawns")) {
				final int npcId = Integer.parseInt(st.nextToken());

				for (L2Spawn spawn : SpawnTable.getInstance().getAllSpawns(npcId)) {
					String out = "<spawn x=\"" + spawn.getX() + "\" y=\"" + spawn.getY() + "\" z=\"" + spawn.getZ() + "\" heading=\"" +
							spawn.getHeading() + "\" respawn=\"10000\" />";
					System.out.println(out);
				}
			} else if (secondaryCommand.equals("FarmSimulator")) {
				int killedMonsters = 0;
				int playerLevel = activeChar.getLevel();

				NpcTemplate[] monsters = NpcTable.getInstance().getAllMonstersBetweenLevels(playerLevel - 5, playerLevel + 5);

				for (NpcTemplate monster : monsters) {
					if (activeChar.getLevel() + 5 < monster.Level) {
						break;
					}

					boolean canSpawn = false;

					if (monster.Level < 10) {
						canSpawn = true;
					}

					if (canSpawn) {
						L2Spawn spawn = null;

						NpcTemplate template = NpcTable.getInstance().getTemplate(monster.NpcId);
						try {
							spawn = new L2Spawn(template);
						} catch (Exception e) {
							e.printStackTrace();
						}

						spawn.setInstanceId(0);
						spawn.setHeading(Rnd.get(65535));
						spawn.setX(activeChar.getX() + Rnd.get(-50, 50));
						spawn.setY(activeChar.getY() + Rnd.get(-50, 50));
						spawn.setZ(activeChar.getZ());
						spawn.stopRespawn();
						spawn.doSpawn(false);

						Npc npc = spawn.getNpc();

						npc.reduceCurrentHp(npc.getMaxHp() + 1, activeChar, null);

						killedMonsters++;
					}
				}

				activeChar.sendMessage(killedMonsters + " monsters were killed.");
			} else if (secondaryCommand.equals("OlyCamera")) {
				if (activeChar.getTarget() instanceof Player) {
					final Player target = (Player) activeChar.getTarget();

					target.sendPacket(new ExOlympiadMode(3));
				} else {
					activeChar.sendPacket(new ExOlympiadMode(3));
				}
			} else if (secondaryCommand.equals("Login")) {
				if (!st.hasMoreTokens()) {
					activeChar.sendMessage("Specify the name of the character to log into.");
					return false;
				}

				final String logIntoCharacterName = st.nextToken();

				String charNameToSwitch = "";
				if (st.hasMoreTokens()) {
					charNameToSwitch = st.nextToken();
				}

				Player toon = activeChar;
				if (!charNameToSwitch.equals("")) {
					toon = World.getInstance().getPlayer(charNameToSwitch);
					activeChar.sendMessage("Logging " + toon.getName() + " into " + logIntoCharacterName);
				}

				final int charId = CharNameTable.getInstance().getIdByName(logIntoCharacterName);

				if (charId == 0) {
					activeChar.sendMessage("No character with such name. Try again.");
					return false;
				}

				final L2GameClient gameClient = toon.getClient();

				toon.setClient(null);

				gameClient.saveCharToDisk();

				gameClient.setActiveChar(null);

				// return the client to the authed status
				gameClient.setState(GameClientState.AUTHED);

				gameClient.sendPacket(RestartResponse.STATIC_PACKET_TRUE);

				// send char list
				CharSelectionInfo cl = new CharSelectionInfo(gameClient.getAccountName(), gameClient.getSessionId().playOkID1);
				gameClient.sendPacket(cl);
				gameClient.setCharSelection(cl.getCharInfo());

				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
					@Override
					public void run() {
						Player cha = Player.load(charId);

						if (cha == null) {
							gameClient.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}

						cha.setClient(gameClient);
						gameClient.setActiveChar(cha);

						//BotsManager.getInstance().logPlayer(cha, true);

						gameClient.setState(GameClientState.IN_GAME);

						CharSelected cs = new CharSelected(cha, gameClient.getSessionId().playOkID1);
						gameClient.sendPacket(cs);

						cha.setOnlineStatus(true, false);
					}
				}, 1000);
			}
		}
		return true;
	}

	private void adminTestSkill(Player activeChar, int id, boolean msu) {
		Creature caster;
		WorldObject target = activeChar.getTarget();
		if (!(target instanceof Creature)) {
			caster = activeChar;
		} else {
			caster = (Creature) target;
		}

		Skill skill = SkillTable.getInstance().getInfo(id, 1);
		if (skill != null) {
			caster.setTarget(activeChar);
			if (msu) {
				caster.broadcastPacket(new MagicSkillUse(caster,
						activeChar,
						id,
						1,
						skill.getHitTime(),
						skill.getReuseDelay(),
						skill.getReuseHashCode(),
						0,
						0));
			} else {
				caster.doCast(skill);
			}
		}
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
	 */
	@Override
	public String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}
}
