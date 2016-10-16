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

package instances.FinalEmperialTomb;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import gnu.trove.TIntObjectHashMap;
import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.L2Object.InstanceType;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2Territory;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2GrandBossInstance;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.network.NpcStringId;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.Earthquake;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.MagicSkillCancelled;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.NpcInfo;
import l2server.gameserver.network.serverpackets.SocialAction;
import l2server.gameserver.network.serverpackets.SpecialCamera;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.SkillHolder;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

/*
TODO:
- test when Frintezza song use 5008 effect skill
- maybe test more deeply halishas AI
- use correct Song names
- use proper zone spawn system
Contributing authors: Gigiikun
 */
public class FinalEmperialTomb extends Quest
{
	private class FETWorld extends InstanceWorld
	{
		public Lock lock = new ReentrantLock();
		public List<L2Npc> npcList = new ArrayList<L2Npc>();
		public int darkChoirPlayerCount = 0;
		public FrintezzaSong OnSong = null;
		public ScheduledFuture<?> songTask = null;
		public ScheduledFuture<?> songEffectTask = null;
		public boolean isVideo = false;
		public L2Npc frintezzaDummy = null;
		public L2Npc overheadDummy = null;
		public L2Npc portraitDummy1 = null;
		public L2Npc portraitDummy3 = null;
		public L2Npc scarletDummy = null;
		public L2GrandBossInstance frintezza = null;
		public L2GrandBossInstance activeScarlet = null;
		public List<L2MonsterInstance> demons = new ArrayList<L2MonsterInstance>();
		public Map<L2MonsterInstance, Integer> portraits = new HashMap<L2MonsterInstance, Integer>();
		public int scarlet_x = 0;
		public int scarlet_y = 0;
		public int scarlet_z = 0;
		public int scarlet_h = 0;
		public int scarlet_a = 0;

		public FETWorld()
		{
		}
	}

	private static class FETSpawn
	{
		public boolean isZone = false;
		public boolean isNeededNextFlag = false;
		public int npcId;
		public int x = 0;
		public int y = 0;
		public int z = 0;
		public int h = 0;
		public int zone = 0;
		public int count = 0;
	}

	private static class FrintezzaSong
	{
		public SkillHolder skill;
		public SkillHolder effectSkill;
		public NpcStringId songName;
		public int chance;

		public FrintezzaSong(SkillHolder sk, SkillHolder esk, NpcStringId sn, int ch)
		{
			skill = sk;
			effectSkill = esk;
			songName = sn;
			chance = ch;
		}
	}

	private static final String qn = "FinalEmperialTomb";
	private static final int INSTANCEID = 136; // this is the client number
	private static final boolean debug = false;

	private final TIntObjectHashMap<L2Territory> spawnZoneList = new TIntObjectHashMap<L2Territory>();
	private final TIntObjectHashMap<List<FETSpawn>> spawnList = new TIntObjectHashMap<List<FETSpawn>>();
	private final List<Integer> mustKillMobsId = new ArrayList<Integer>();

	// Teleports
	private static final int[] ENTER_TELEPORT = {-88015, -141153, -9168};

	//NPCs
	private static final int GUIDE = 32011;
	private static final int CUBE = 29061;

	//mobs
	private static final int SCARLET1 = 29046;
	private static final int SCARLET2 = 29047;
	private static final int FRINTEZZA = 29045;
	private static final int[] PORTRAITS = {29048, 29049};
	private static final int[] DEMONS = {29050, 29051};
	private static final int HALL_ALARM = 18328;
	private static final int DARK_CHOIR_PLAYER = 18339;
	private static final int[] AI_DISABLED_MOBS = {18328};

	private static final int FIRST_SCARLET_WEAPON = 8204;
	private static final int SECOND_SCARLET_WEAPON = 7903;
	private static final SkillHolder INTRO_SKILL = new SkillHolder(5004, 1);
	private static final SkillHolder FIRST_MORPH_SKILL = new SkillHolder(5017, 1);

	private static final FrintezzaSong[] FRINTEZZASONGLIST = {
			new FrintezzaSong(new SkillHolder(5007, 1), new SkillHolder(5008, 1), NpcStringId.REQUIEM_OF_HATRED, 5),
			new FrintezzaSong(new SkillHolder(5007, 2), new SkillHolder(5008, 2), NpcStringId.RONDO_OF_SOLITUDE, 50),
			new FrintezzaSong(new SkillHolder(5007, 3), new SkillHolder(5008, 3), NpcStringId.FRENETIC_TOCCATA, 70),
			new FrintezzaSong(new SkillHolder(5007, 4), new SkillHolder(5008, 4), NpcStringId.FUGUE_OF_JUBILATION, 90),
			new FrintezzaSong(new SkillHolder(5007, 5), new SkillHolder(5008, 5), NpcStringId.HYPNOTIC_MAZURKA, 100),
			};

	// Doors/Walls/Zones
	private static final int[] FIRST_ROOM_DOORS =
			{17130051, 17130052, 17130053, 17130054, 17130055, 17130056, 17130057, 17130058};
	private static final int[] SECOND_ROOM_DOORS =
			{17130061, 17130062, 17130063, 17130064, 17130065, 17130066, 17130067, 17130068, 17130069, 17130070};

	private static final int[] FIRST_ROUTE_DOORS = {17130042, 17130043};
	private static final int[] SECOND_ROUTE_DOORS = {17130045, 17130046};
	private static final L2CharPosition MOVE_TO_CENTER = new L2CharPosition(-87904, -141296, -9168, 0);

	// spawns
	private static final int TIME_BETWEEN_DEMON_SPAWNS = 20000;
	private static final int MAX_DEMONS = 24;
	private static final int[][] PORTRAIT_SPAWNS = {
			{29048, -89381, -153981, -9168, 3368, -89378, -153968, -9168, 3368},
			{29048, -86234, -152467, -9168, 37656, -86261, -152492, -9168, 37656},
			{29049, -89342, -152479, -9168, -5152, -89311, -152491, -9168, -5152},
			{29049, -86189, -153968, -9168, 29456, -86217, -153956, -9168, 29456}
	};

	// Initialization at 6:30 am on Wednesday and Saturday
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;
	private static final int RESET_DAY_1 = 4;
	private static final int RESET_DAY_2 = 7;

	private void load()
	{
		//int spawnCount = 0;
		try
		{
			File file = new File(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER +
					"scripts/instances/FinalEmperialTomb/final_emperial_tomb.xml");
			if (!file.exists())
			{
				Log.severe("[Final Emperial Tomb] Missing final_emperial_tomb.xml. The quest wont work without it!");
				return;
			}

			XmlDocument doc = new XmlDocument(file);
			XmlNode first = doc.getFirstChild();
			if (first != null && "list".equalsIgnoreCase(first.getName()))
			{
				for (XmlNode n : first.getChildren())
				{
					if (n.getName().equalsIgnoreCase("npc"))
					{
						for (XmlNode d : n.getChildren())
						{
							if (d.getName().equalsIgnoreCase("spawn"))
							{
								if (!d.hasAttribute("npcId"))
								{
									Log.severe("[Final Emperial Tomb] Missing npcId in npc List, skipping");
									continue;
								}
								int npcId = d.getInt("npcId");
								if (!d.hasAttribute("flag"))
								{
									Log.severe("[Final Emperial Tomb] Missing flag in npc List npcId: " + npcId +
											", skipping");
									continue;
								}
								int flag = d.getInt("flag");
								if (!spawnList.contains(flag))
								{
									spawnList.put(flag, new ArrayList<FETSpawn>());
								}

								for (XmlNode cd : d.getChildren())
								{
									if (cd.getName().equalsIgnoreCase("loc"))
									{
										FETSpawn spw = new FETSpawn();
										spw.npcId = npcId;

										if (cd.hasAttribute("x"))
										{
											spw.x = cd.getInt("x");
										}
										else
										{
											continue;
										}

										if (cd.hasAttribute("y"))
										{
											spw.y = cd.getInt("y");
										}
										else
										{
											continue;
										}

										if (cd.hasAttribute("z"))
										{
											spw.z = cd.getInt("z");
										}
										else
										{
											continue;
										}

										if (cd.hasAttribute("heading"))
										{
											spw.h = cd.getInt("heading");
										}
										else
										{
											continue;
										}

										spw.isNeededNextFlag = cd.getBool("mustKill", false);
										if (spw.isNeededNextFlag)
										{
											mustKillMobsId.add(npcId);
										}
										spawnList.get(flag).add(spw);
										//spawnCount++;
									}
									else if (cd.getName().equalsIgnoreCase("zone"))
									{
										FETSpawn spw = new FETSpawn();
										spw.npcId = npcId;
										spw.isZone = true;

										if (cd.hasAttribute("id"))
										{
											spw.zone = cd.getInt("id");
										}
										else
										{
											continue;
										}

										if (cd.hasAttribute("count"))
										{
											spw.count = cd.getInt("count");
										}
										else
										{
											continue;
										}

										spw.isNeededNextFlag = cd.getBool("mustKill", false);
										if (spw.isNeededNextFlag)
										{
											mustKillMobsId.add(npcId);
										}
										spawnList.get(flag).add(spw);
										//spawnCount++;
									}
								}
							}
						}
					}
					else if (n.getName().equalsIgnoreCase("spawnZones"))
					{
						for (XmlNode d : n.getChildren())
						{
							if (d.getName().equalsIgnoreCase("zone"))
							{
								if (!d.hasAttribute("id"))
								{
									Log.severe("[Final Emperial Tomb] Missing id in spawnZones List, skipping");
									continue;
								}
								int id = d.getInt("id");

								if (!d.hasAttribute("minZ"))
								{
									Log.severe("[Final Emperial Tomb] Missing minZ in spawnZones List id: " + id +
											", skipping");
									continue;
								}
								int minz = d.getInt("minZ");

								if (!d.hasAttribute("maxZ"))
								{
									Log.severe("[Final Emperial Tomb] Missing maxZ in spawnZones List id: " + id +
											", skipping");
									continue;
								}
								int maxz = d.getInt("maxZ");
								L2Territory ter = new L2Territory(id);

								for (XmlNode cd : d.getChildren())
								{
									if (cd.getName().equalsIgnoreCase("point"))
									{
										int x, y;
										if (cd.hasAttribute("x"))
										{
											x = cd.getInt("x");
										}
										else
										{
											continue;
										}

										if (cd.hasAttribute("y"))
										{
											y = cd.getInt("y");
										}
										else
										{
											continue;
										}

										ter.add(x, y, minz, maxz, 0);
									}
								}

								spawnZoneList.put(id, ter);
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING,
					"[Final Emperial Tomb] Could not parse final_emperial_tomb.xml file: " + e.getMessage(), e);
		}

		if (debug)
		{
			Log.info("[Final Emperial Tomb] Loaded " + spawnZoneList.size() + " spawn zones data.");
			//Log.info("[Final Emperial Tomb] Loaded " + spawnCount + " spawns data.");
		}
	}

	protected void openDoor(int doorId, int instanceId)
	{
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
		{
			if (door.getDoorId() == doorId)
			{
				door.openMe();
			}
		}
	}

	protected void closeDoor(int doorId, int instanceId)
	{
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
		{
			if (door.getDoorId() == doorId)
			{
				if (door.getOpen())
				{
					door.closeMe();
				}
			}
		}
	}

	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}

	protected int enterInstance(L2PcInstance player, String template, int[] coords)
	{
		int instanceId = 0;
		//check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		//existing instance
		if (world != null)
		{
			if (!(world instanceof FETWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return 0;
			}
			teleportPlayer(player, coords, world.instanceId);
			return world.instanceId;
		}

		//New instance
		int maxLvl = 92;
		if (Config.isServer(Config.TENKAI_ESTHUS))
		{
			maxLvl = Config.MAX_LEVEL;
		}
		if (!InstanceManager.getInstance()
				.checkInstanceConditions(player, INSTANCEID, Config.FRINTEZZA_MIN_PLAYERS, 45, 80, maxLvl))
		{
			return 0;
		}
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		//Instance ins = InstanceManager.getInstance().getInstance(instanceId);
		//ins.setSpawnLoc(new int[]{player.getX(),player.getY(),player.getZ()});
		world = new FETWorld();
		world.instanceId = instanceId;
		world.status = 0;
		InstanceManager.getInstance().addWorld(world);
		controlStatus((FETWorld) world);
		Log.info("Final Emperial Tomb started " + template + " Instance: " + instanceId + " created by player: " +
				player.getName());
		// teleport players
		for (L2PcInstance channelMember : Config.FRINTEZZA_MIN_PLAYERS > Config.MAX_MEMBERS_IN_PARTY ?
				player.getParty().getCommandChannel().getMembers() :
				player.getParty().getCommandChannel() != null ? player.getParty().getCommandChannel().getMembers() :
						player.getParty().getPartyMembers())
		{
			world.allowed.add(channelMember.getObjectId());
			teleportPlayer(channelMember, coords, instanceId);
		}
		return instanceId;
	}

	protected synchronized boolean checkKillProgress(L2Npc mob, FETWorld world)
	{
		if (world.npcList.contains(mob))
		{
			world.npcList.remove(mob);
		}
		return world.npcList.size() == 0;
	}

	private void spawnFlaggedNPCs(FETWorld world, int flag)
	{
		if (world.lock.tryLock())
		{
			try
			{
				for (FETSpawn spw : spawnList.get(flag))
				{
					if (spw.isZone)
					{
						for (int i = 0; i < spw.count; i++)
						{
							if (spawnZoneList.contains(spw.zone))
							{
								int[] point = spawnZoneList.get(spw.zone).getRandomPoint();
								spawn(world, spw.npcId, point[0], point[1], GeoData.getInstance()
												.getSpawnHeight(point[0], point[1], point[2], point[3], null), Rnd.get(65535),
										spw.isNeededNextFlag);
							}
							else
							{
								Log.info("[Final Emperial Tomb] Missing zone: " + spw.zone);
							}
						}
					}
					else
					{
						spawn(world, spw.npcId, spw.x, spw.y, spw.z, spw.h, spw.isNeededNextFlag);
					}
				}
			}
			finally
			{
				world.lock.unlock();
			}
		}
	}

	protected boolean controlStatus(FETWorld world)
	{
		if (world.lock.tryLock())
		{
			try
			{
				if (debug)
				{
					Log.info("[Final Emperial Tomb] Starting " + world.status + ". status.");
				}
				world.npcList.clear();
				switch (world.status)
				{
					case 0:
						spawnFlaggedNPCs(world, 0);
						break;
					case 1:
						for (int doorId : FIRST_ROUTE_DOORS)
						{
							openDoor(doorId, world.instanceId);
						}
						spawnFlaggedNPCs(world, world.status);
						break;
					case 2:
						for (int doorId : SECOND_ROUTE_DOORS)
						{
							openDoor(doorId, world.instanceId);
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 0), 600000);
						break;
					case 3: // first morph
						if (world.songEffectTask != null)
						{
							world.songEffectTask.cancel(false);
						}
						world.songEffectTask = null;
						world.activeScarlet.setIsInvul(true);
						if (world.activeScarlet.isCastingNow())
						{
							world.activeScarlet.abortCast();
						}
						setInstanceTimeRestrictions(world);
						world.activeScarlet.doCast(FIRST_MORPH_SKILL.getSkill());
						ThreadPoolManager.getInstance().scheduleGeneral(new SongTask(world, 2), 1500);
						break;
					case 4: // second morph
						world.isVideo = true;
						broadCastPacket(world, new MagicSkillCancelled(world.frintezza.getObjectId()));
						if (world.songEffectTask != null)
						{
							world.songEffectTask.cancel(false);
						}
						world.songEffectTask = null;
						ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 23), 2000);
						ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 24), 2100);
						break;
					case 5: // raid success
						world.isVideo = true;
						broadCastPacket(world, new MagicSkillCancelled(world.frintezza.getObjectId()));
						if (world.songTask != null)
						{
							world.songTask.cancel(true);
						}
						if (world.songEffectTask != null)
						{
							world.songEffectTask.cancel(false);
						}
						world.songTask = null;
						world.songEffectTask = null;
						ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 33), 500);
						break;
					case 6: // open doors
						InstanceManager.getInstance().getInstance(world.instanceId).setDuration(300000);
						for (int doorId : FIRST_ROOM_DOORS)
						{
							openDoor(doorId, world.instanceId);
						}
						for (int doorId : FIRST_ROUTE_DOORS)
						{
							openDoor(doorId, world.instanceId);
						}
						for (int doorId : SECOND_ROUTE_DOORS)
						{
							openDoor(doorId, world.instanceId);
						}
						for (int doorId : SECOND_ROOM_DOORS)
						{
							closeDoor(doorId, world.instanceId);
						}
						break;
				}
				world.status++;
				return true;
			}
			finally
			{
				world.lock.unlock();
			}
		}
		return false;
	}

	protected void spawn(FETWorld world, int npcId, int x, int y, int z, int h, boolean addToKillTable)
	{
		L2Npc npc = addSpawn(npcId, x, y, z, h, false, 0, false, world.instanceId);
		if (addToKillTable)
		{
			world.npcList.add(npc);
		}
		npc.setIsNoRndWalk(true);
		if (npc.isInstanceType(InstanceType.L2Attackable))
		{
			((L2Attackable) npc).setCanSeeThroughSilentMove(true);
		}
		if (Util.contains(AI_DISABLED_MOBS, npcId))
		{
			npc.disableCoreAI(true);
		}
		if (npcId == DARK_CHOIR_PLAYER)
		{
			world.darkChoirPlayerCount++;
		}
	}

	private class DemonSpawnTask implements Runnable
	{
		private final FETWorld world;

		DemonSpawnTask(FETWorld world)
		{
			this.world = world;
		}

		@Override
		public void run()
		{
			if (InstanceManager.getInstance().getWorld(world.instanceId) != world || world.portraits.isEmpty())
			{
				if (debug)
				{
					Log.info("[Final Emperial Tomb] Instance is deleted or all Portraits is killed.");
				}
				return;
			}
			for (int i : world.portraits.values())
			{
				if (world.demons.size() > MAX_DEMONS)
				{
					break;
				}
				L2MonsterInstance demon = (L2MonsterInstance) addSpawn(PORTRAIT_SPAWNS[i][0] + 2, PORTRAIT_SPAWNS[i][5],
						PORTRAIT_SPAWNS[i][6], PORTRAIT_SPAWNS[i][7], PORTRAIT_SPAWNS[i][8], false, 0, false, world.instanceId);
				updateKnownList(world, demon);
				world.demons.add(demon);
			}
			ThreadPoolManager.getInstance().scheduleGeneral(new DemonSpawnTask(world), TIME_BETWEEN_DEMON_SPAWNS);
		}
	}

	private class SongTask implements Runnable
	{
		private final FETWorld world;
		private final int status;

		SongTask(FETWorld world, int status)
		{
			this.world = world;
			this.status = status;
		}

		@Override
		public void run()
		{
			if (InstanceManager.getInstance().getWorld(world.instanceId) != world)
			{
				return;
			}
			switch (status)
			{
				case 0: // new song play
					if (world.isVideo)
					{
						world.songTask =
								ThreadPoolManager.getInstance().scheduleGeneral(new SongTask(world, 0), 1000);
					}
					else if (world.frintezza != null && !world.frintezza.isDead())
					{
						int rnd = Rnd.get(100);
						for (FrintezzaSong element : FRINTEZZASONGLIST)
						{
							if (rnd < element.chance)
							{
								world.OnSong = element;
								broadCastPacket(world, new ExShowScreenMessage(element.songName.getId(), 3000));
								broadCastPacket(world, new MagicSkillUse(world.frintezza, world.frintezza,
										element.skill.getSkillId(), element.skill.getSkillLvl(),
										element.skill.getSkill().getHitTime(), 0, 0));
								world.songEffectTask = ThreadPoolManager.getInstance()
										.scheduleGeneral(new SongTask(world, 1),
												element.skill.getSkill().getHitTime() - 10000);
								world.songTask = ThreadPoolManager.getInstance()
										.scheduleGeneral(new SongTask(world, 0),
												element.skill.getSkill().getHitTime());
								break;
							}
						}
					}
					break;
				case 1: // Frintezza song effect
					world.songEffectTask = null;
					L2Skill skill = world.OnSong.effectSkill.getSkill();
					if (skill == null)
					{
						return;
					}

					if (world.frintezza != null && !world.frintezza.isDead() && world.activeScarlet != null &&
							!world.activeScarlet.isDead())
					{
						List<L2Character> targetList = new ArrayList<L2Character>();
						if (skill.getSkillType() == L2SkillType.DEBUFF)
						{
							for (int objId : world.allowed)
							{
								L2PcInstance player = L2World.getInstance().getPlayer(objId);
								if (player != null && player.isOnline() && player.getInstanceId() == world.instanceId)
								{
									if (!player.isDead())
									{
										targetList.add(player);
									}
									if (player.getPet() != null && !player.getPet().isDead())
									{
										targetList.add(player.getPet());
									}
								}
							}
						}
						else
						{
							targetList.add(world.activeScarlet);
						}
						if (targetList.size() > 0)
						{
							world.frintezza.doCast(skill, targetList.get(0),
									targetList.toArray(new L2Character[targetList.size()]));
						}
					}
					break;
				case 2: // finish morph
					world.activeScarlet.setRHandId(SECOND_SCARLET_WEAPON);
					world.activeScarlet.setIsInvul(false);
					break;
			}
		}
	}

	private class IntroTask implements Runnable
	{
		private final FETWorld world;
		private final int status;

		IntroTask(FETWorld world, int status)
		{
			this.world = world;
			this.status = status;
		}

		@Override
		public void run()
		{
			switch (status)
			{
				case 0:
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 1), 27000);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 2), 30000);
					broadCastPacket(world, new Earthquake(-87784, -155083, -9087, 45, 27));
					break;
				case 1:
					for (int doorId : FIRST_ROOM_DOORS)
					{
						closeDoor(doorId, world.instanceId);
					}
					for (int doorId : FIRST_ROUTE_DOORS)
					{
						closeDoor(doorId, world.instanceId);
					}
					for (int doorId : SECOND_ROOM_DOORS)
					{
						closeDoor(doorId, world.instanceId);
					}
					for (int doorId : SECOND_ROUTE_DOORS)
					{
						closeDoor(doorId, world.instanceId);
					}
					addSpawn(29061, -87904, -141296, -9168, 0, false, 0, false, world.instanceId);
					break;
				case 2:
					world.frintezzaDummy =
							addSpawn(29052, -87784, -155083, -9087, 16048, false, 0, false, world.instanceId);
					world.frintezzaDummy.setIsInvul(true);
					world.frintezzaDummy.setIsImmobilized(true);

					world.overheadDummy =
							addSpawn(29052, -87784, -153298, -9175, 16384, false, 0, false, world.instanceId);
					world.overheadDummy.setIsInvul(true);
					world.overheadDummy.setIsImmobilized(true);
					world.overheadDummy.setCollisionHeight(600);
					broadCastPacket(world, new NpcInfo(world.overheadDummy, null));

					world.portraitDummy1 =
							addSpawn(29052, -89566, -153168, -9165, 16048, false, 0, false, world.instanceId);
					world.portraitDummy1.setIsImmobilized(true);
					world.portraitDummy1.setIsInvul(true);

					world.portraitDummy3 =
							addSpawn(29052, -86004, -153168, -9165, 16048, false, 0, false, world.instanceId);
					world.portraitDummy3.setIsImmobilized(true);
					world.portraitDummy3.setIsInvul(true);

					world.scarletDummy =
							addSpawn(29053, -87784, -153298, -9175, 16384, false, 0, false, world.instanceId);
					world.scarletDummy.setIsInvul(true);
					world.scarletDummy.setIsImmobilized(true);

					stopPc();
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 3), 1000);
					break;
				case 3:
					broadCastPacket(world,
							new SpecialCamera(world.overheadDummy.getObjectId(), 0, 75, -89, 0, 100, 0, 0, 1, 0));
					broadCastPacket(world,
							new SpecialCamera(world.overheadDummy.getObjectId(), 0, 75, -89, 0, 100, 0, 0, 1, 0));
					broadCastPacket(world,
							new SpecialCamera(world.overheadDummy.getObjectId(), 300, 90, -10, 6500, 7000, 0, 0, 1,
									0));

					world.frintezza =
							(L2GrandBossInstance) addSpawn(FRINTEZZA, -87784, -155083, -9087, 16048, false, 0, false,
									world.instanceId);
					world.frintezza.setIsImmobilized(true);
					world.frintezza.setIsInvul(true);
					world.frintezza.disableAllSkills();
					updateKnownList(world, world.frintezza);

					for (int[] element : PORTRAIT_SPAWNS)
					{
						L2MonsterInstance demon =
								(L2MonsterInstance) addSpawn(element[0] + 2, element[5], element[6], element[7],
										element[8], false, 0, false, world.instanceId);
						demon.setIsImmobilized(true);
						demon.disableAllSkills();
						updateKnownList(world, demon);
						world.demons.add(demon);
					}
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 4), 6500);
					break;
				case 4:
					broadCastPacket(world,
							new SpecialCamera(world.frintezzaDummy.getObjectId(), 1800, 90, 8, 6500, 7000, 0, 0, 1,
									0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 5), 900);
					break;
				case 5:
					broadCastPacket(world,
							new SpecialCamera(world.frintezzaDummy.getObjectId(), 140, 90, 10, 2500, 4500, 0, 0, 1,
									0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 6), 4000);
					break;
				case 6:
					broadCastPacket(world,
							new SpecialCamera(world.frintezza.getObjectId(), 40, 75, -10, 0, 1000, 0, 0, 1, 0));
					broadCastPacket(world,
							new SpecialCamera(world.frintezza.getObjectId(), 40, 75, -10, 0, 12000, 0, 0, 1, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 7), 1350);
					break;
				case 7:
					broadCastPacket(world, new SocialAction(world.frintezza.getObjectId(), 2));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 8), 7000);
					break;
				case 8:
					world.frintezzaDummy.deleteMe();
					world.frintezzaDummy = null;
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 9), 1000);
					break;
				case 9:
					broadCastPacket(world, new SocialAction(world.demons.get(1).getObjectId(), 1));
					broadCastPacket(world, new SocialAction(world.demons.get(2).getObjectId(), 1));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 10), 400);
					break;
				case 10:
					broadCastPacket(world, new SocialAction(world.demons.get(0).getObjectId(), 1));
					broadCastPacket(world, new SocialAction(world.demons.get(3).getObjectId(), 1));
					sendPacketX(
							new SpecialCamera(world.portraitDummy1.getObjectId(), 1000, 118, 0, 0, 1000, 0, 0, 1, 0),
							new SpecialCamera(world.portraitDummy3.getObjectId(), 1000, 62, 0, 0, 1000, 0, 0, 1, 0),
							-87784);
					sendPacketX(
							new SpecialCamera(world.portraitDummy1.getObjectId(), 1000, 118, 0, 0, 10000, 0, 0, 1, 0),
							new SpecialCamera(world.portraitDummy3.getObjectId(), 1000, 62, 0, 0, 10000, 0, 0, 1, 0),
							-87784);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 11), 2000);
					break;
				case 11:
					broadCastPacket(world,
							new SpecialCamera(world.frintezza.getObjectId(), 240, 90, 0, 0, 1000, 0, 0, 1, 0));
					broadCastPacket(world,
							new SpecialCamera(world.frintezza.getObjectId(), 240, 90, 25, 5500, 10000, 0, 0, 1, 0));
					broadCastPacket(world, new SocialAction(world.frintezza.getObjectId(), 3));
					world.portraitDummy1.deleteMe();
					world.portraitDummy3.deleteMe();
					world.portraitDummy1 = null;
					world.portraitDummy3 = null;
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 12), 4500);
					break;
				case 12:
					broadCastPacket(world,
							new SpecialCamera(world.frintezza.getObjectId(), 100, 195, 35, 0, 10000, 0, 0, 1, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 13), 700);
					break;
				case 13:
					broadCastPacket(world,
							new SpecialCamera(world.frintezza.getObjectId(), 100, 195, 35, 0, 10000, 0, 0, 1, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 14), 1300);
					break;
				case 14:
					broadCastPacket(world,
							new ExShowScreenMessage(NpcStringId.MOURNFUL_CHORALE_PRELUDE.getId(), 5000));
					broadCastPacket(world,
							new SpecialCamera(world.frintezza.getObjectId(), 120, 180, 45, 1500, 10000, 0, 0, 1, 0));
					broadCastPacket(world,
							new MagicSkillUse(world.frintezza, world.frintezza, 5006, 1, 34000, 0, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 15), 1500);
					break;
				case 15:
					broadCastPacket(world,
							new SpecialCamera(world.frintezza.getObjectId(), 520, 135, 45, 8000, 10000, 0, 0, 1, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 16), 7500);
					break;
				case 16:
					broadCastPacket(world,
							new SpecialCamera(world.frintezza.getObjectId(), 1500, 110, 25, 10000, 13000, 0, 0, 1, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 17), 9500);
					break;
				case 17:
					broadCastPacket(world,
							new SpecialCamera(world.overheadDummy.getObjectId(), 930, 160, -20, 0, 1000, 0, 0, 1, 0));
					broadCastPacket(world,
							new SpecialCamera(world.overheadDummy.getObjectId(), 600, 180, -25, 0, 10000, 0, 0, 1, 0));
					broadCastPacket(world,
							new MagicSkillUse(world.scarletDummy, world.overheadDummy, 5004, 1, 5800, 0, 0));

					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 18), 5000);
					break;
				case 18:
					world.activeScarlet =
							(L2GrandBossInstance) addSpawn(29046, -87784, -153298, -9165, 16384, false, 0, false, world.instanceId);
					world.activeScarlet.setRHandId(FIRST_SCARLET_WEAPON);
					world.activeScarlet.setIsInvul(true);
					world.activeScarlet.setIsImmobilized(true);
					world.activeScarlet.disableAllSkills();
					updateKnownList(world, world.activeScarlet);
					broadCastPacket(world, new SocialAction(world.activeScarlet.getObjectId(), 3));
					broadCastPacket(world,
							new SpecialCamera(world.scarletDummy.getObjectId(), 800, 180, 10, 1000, 10000, 0, 0, 1,
									0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 19), 2100);
					break;
				case 19:
					broadCastPacket(world,
							new SpecialCamera(world.activeScarlet.getObjectId(), 300, 60, 8, 0, 10000, 0, 0, 1, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 20), 2000);
					break;
				case 20:
					broadCastPacket(world,
							new SpecialCamera(world.activeScarlet.getObjectId(), 500, 90, 10, 3000, 5000, 0, 0, 1, 0));
					world.songTask = ThreadPoolManager.getInstance().scheduleGeneral(new SongTask(world, 0), 100);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 21), 3000);
					break;
				case 21:
					for (int i = 0; i < PORTRAIT_SPAWNS.length; i++)
					{
						L2MonsterInstance portrait =
								(L2MonsterInstance) addSpawn(PORTRAIT_SPAWNS[i][0], PORTRAIT_SPAWNS[i][1],
										PORTRAIT_SPAWNS[i][2], PORTRAIT_SPAWNS[i][3], PORTRAIT_SPAWNS[i][4], false, 0,
										false, world.instanceId);
						updateKnownList(world, portrait);
						world.portraits.put(portrait, i);
					}

					world.overheadDummy.deleteMe();
					world.scarletDummy.deleteMe();
					world.overheadDummy = null;
					world.scarletDummy = null;

					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 22), 2000);
					break;
				case 22:
					for (L2MonsterInstance demon : world.demons)
					{
						demon.setIsImmobilized(false);
						demon.enableAllSkills();
					}
					world.activeScarlet.setIsInvul(false);
					world.activeScarlet.setIsImmobilized(false);
					world.activeScarlet.enableAllSkills();
					world.activeScarlet.setRunning();
					world.activeScarlet.doCast(INTRO_SKILL.getSkill());
					world.frintezza.enableAllSkills();
					world.frintezza.disableCoreAI(true);
					world.frintezza.setIsMortal(false);
					startPc();

					ThreadPoolManager.getInstance()
							.scheduleGeneral(new DemonSpawnTask(world), TIME_BETWEEN_DEMON_SPAWNS);
					break;
				case 23:
					broadCastPacket(world, new SocialAction(world.frintezza.getObjectId(), 4));
					break;
				case 24:
					stopPc();
					broadCastPacket(world,
							new SpecialCamera(world.frintezza.getObjectId(), 250, 120, 15, 0, 1000, 0, 0, 1, 0));
					broadCastPacket(world,
							new SpecialCamera(world.frintezza.getObjectId(), 250, 120, 15, 0, 10000, 0, 0, 1, 0));
					world.activeScarlet.abortAttack();
					world.activeScarlet.abortCast();
					world.activeScarlet.setIsInvul(true);
					world.activeScarlet.setIsImmobilized(true);
					world.activeScarlet.disableAllSkills();
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 25), 7000);
					break;
				case 25:
					broadCastPacket(world,
							new MagicSkillUse(world.frintezza, world.frintezza, 5006, 1, 34000, 0, 0));
					broadCastPacket(world,
							new SpecialCamera(world.frintezza.getObjectId(), 500, 70, 15, 3000, 10000, 0, 0, 1, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 26), 3000);
					break;
				case 26:
					broadCastPacket(world,
							new SpecialCamera(world.frintezza.getObjectId(), 2500, 90, 12, 6000, 10000, 0, 0, 1, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 27), 3000);
					break;
				case 27:
					world.scarlet_x = world.activeScarlet.getX();
					world.scarlet_y = world.activeScarlet.getY();
					world.scarlet_z = world.activeScarlet.getZ();
					world.scarlet_h = world.activeScarlet.getHeading();
					if (world.scarlet_h < 32768)
					{
						world.scarlet_a = Math.abs(180 - (int) (world.scarlet_h / 182.044444444));
					}
					else
					{
						world.scarlet_a = Math.abs(540 - (int) (world.scarlet_h / 182.044444444));
					}
					broadCastPacket(world,
							new SpecialCamera(world.activeScarlet.getObjectId(), 250, world.scarlet_a, 12, 0, 1000, 0,
									0, 1, 0));
					broadCastPacket(world,
							new SpecialCamera(world.activeScarlet.getObjectId(), 250, world.scarlet_a, 12, 0, 10000,
									0, 0, 1, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 28), 500);
					break;
				case 28:
					world.activeScarlet.doDie(world.activeScarlet);
					broadCastPacket(world,
							new SpecialCamera(world.activeScarlet.getObjectId(), 450, world.scarlet_a, 14, 8000, 8000,
									0, 0, 1, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 29), 6250);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 30), 7200);
					break;
				case 29:
					world.activeScarlet.deleteMe();
					world.activeScarlet = null;
					break;
				case 30:
					world.activeScarlet = (L2GrandBossInstance) addSpawn(SCARLET2, world.scarlet_x, world.scarlet_y,
							world.scarlet_z, world.scarlet_h, false, 0, false, world.instanceId);
					world.activeScarlet.setIsInvul(true);
					world.activeScarlet.setIsImmobilized(true);
					world.activeScarlet.disableAllSkills();
					updateKnownList(world, world.activeScarlet);

					broadCastPacket(world,
							new SpecialCamera(world.activeScarlet.getObjectId(), 450, world.scarlet_a, 12, 500, 14000,
									0, 0, 1, 0));

					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 31), 8100);
					break;
				case 31:
					broadCastPacket(world, new SocialAction(world.activeScarlet.getObjectId(), 2));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 32), 9000);
					break;
				case 32:
					startPc();
					world.activeScarlet.setIsInvul(false);
					world.activeScarlet.setIsImmobilized(false);
					world.activeScarlet.enableAllSkills();
					world.isVideo = false;
					break;
				case 33:
					broadCastPacket(world,
							new SpecialCamera(world.activeScarlet.getObjectId(), 300, world.scarlet_a - 180, 5, 0,
									7000, 0, 0, 1, 0));
					broadCastPacket(world,
							new SpecialCamera(world.activeScarlet.getObjectId(), 200, world.scarlet_a, 85, 4000,
									10000, 0, 0, 1, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 34), 7400);
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 35), 7500);
					break;
				case 34:
					world.frintezza.doDie(world.frintezza);
					break;
				case 35:
					broadCastPacket(world,
							new SpecialCamera(world.frintezza.getObjectId(), 100, 120, 5, 0, 7000, 0, 0, 1, 0));
					broadCastPacket(world,
							new SpecialCamera(world.frintezza.getObjectId(), 100, 90, 5, 5000, 15000, 0, 0, 1, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 36), 7000);
					break;
				case 36:
					broadCastPacket(world,
							new SpecialCamera(world.frintezza.getObjectId(), 900, 90, 25, 7000, 10000, 0, 0, 1, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new IntroTask(world, 37), 9000);
					break;
				case 37:
					controlStatus(world);
					world.isVideo = false;
					startPc();
					break;
			}
		}

		private void stopPc()
		{
			for (int objId : world.allowed)
			{
				L2PcInstance player = L2World.getInstance().getPlayer(objId);
				if (player != null && player.isOnline() && player.getInstanceId() == world.instanceId)
				{
					player.abortAttack();
					player.abortCast();
					player.disableAllSkills();
					player.setTarget(null);
					player.stopMove(null);
					player.setIsImmobilized(true);
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}
			}
		}

		private void startPc()
		{
			for (int objId : world.allowed)
			{
				L2PcInstance player = L2World.getInstance().getPlayer(objId);
				if (player != null && player.isOnline() && player.getInstanceId() == world.instanceId)
				{
					player.enableAllSkills();
					player.setIsImmobilized(false);
				}
			}
		}

		private void sendPacketX(L2GameServerPacket packet1, L2GameServerPacket packet2, int x)
		{
			for (int objId : world.allowed)
			{
				L2PcInstance player = L2World.getInstance().getPlayer(objId);
				if (player != null && player.isOnline() && player.getInstanceId() == world.instanceId)
				{
					if (player.getX() < x)
					{
						player.sendPacket(packet1);
					}
					else
					{
						player.sendPacket(packet2);
					}
				}
			}
		}
	}

	private class StatusTask implements Runnable
	{
		private final FETWorld world;
		private final int status;

		StatusTask(FETWorld world, int status)
		{
			this.world = world;
			this.status = status;
		}

		@Override
		public void run()
		{
			if (InstanceManager.getInstance().getWorld(world.instanceId) != world)
			{
				return;
			}
			switch (status)
			{
				case 0:
					ThreadPoolManager.getInstance().scheduleGeneral(new StatusTask(world, 1), 2000);
					for (int doorId : FIRST_ROOM_DOORS)
					{
						openDoor(doorId, world.instanceId);
					}
					break;
				case 1:
					addAggroToMobs();
					break;
				case 2:
					ThreadPoolManager.getInstance().scheduleGeneral(new StatusTask(world, 3), 100);
					for (int doorId : SECOND_ROOM_DOORS)
					{
						openDoor(doorId, world.instanceId);
					}
					break;
				case 3:
					addAggroToMobs();
					break;
				case 4:
					controlStatus(world);
					break;
			}
		}

		private void addAggroToMobs()
		{
			L2PcInstance target = L2World.getInstance().getPlayer(world.allowed.get(Rnd.get(world.allowed.size())));
			if (target == null || target.getInstanceId() != world.instanceId || target.isDead() ||
					target.isFakeDeath())
			{
				for (int objId : world.allowed)
				{
					target = L2World.getInstance().getPlayer(objId);
					if (target != null && target.getInstanceId() == world.instanceId && !target.isDead() &&
							!target.isFakeDeath())
					{
						break;
					}
					target = null;
				}
			}
			for (L2Npc mob : world.npcList)
			{
				mob.setRunning();
				if (target != null)
				{
					((L2MonsterInstance) mob).addDamageHate(target, 0, 500);
					mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
				else
				{
					mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_CENTER);
				}
			}
		}
	}

	protected void setInstanceTimeRestrictions(FETWorld world)
	{
		Calendar reenter = Calendar.getInstance();
		reenter.set(Calendar.MINUTE, RESET_MIN);
		reenter.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
		// if time is >= RESET_HOUR - roll to the next day
		if (reenter.getTimeInMillis() <= System.currentTimeMillis())
		{
			reenter.add(Calendar.DAY_OF_MONTH, 1);
		}
		if (reenter.get(Calendar.DAY_OF_WEEK) <= RESET_DAY_1)
		{
			while (reenter.get(Calendar.DAY_OF_WEEK) != RESET_DAY_1)
			{
				reenter.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		else
		{
			while (reenter.get(Calendar.DAY_OF_WEEK) != RESET_DAY_2)
			{
				reenter.add(Calendar.DAY_OF_MONTH, 1);
			}
		}

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
		sm.addInstanceName(INSTANCEID);

		// set instance reenter time for all allowed players
		for (int objectId : world.allowed)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objectId);
			InstanceManager.getInstance().setInstanceTime(objectId, INSTANCEID, reenter.getTimeInMillis());
			if (player != null && player.isOnline())
			{
				player.sendPacket(sm);
			}
		}
	}

	private void broadCastPacket(FETWorld world, L2GameServerPacket packet)
	{
		for (int objId : world.allowed)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			if (player != null && player.isOnline() && player.getInstanceId() == world.instanceId)
			{
				player.sendPacket(packet);
			}
		}
	}

	private void updateKnownList(FETWorld world, L2Npc npc)
	{
		Map<Integer, L2PcInstance> npcKnownPlayers = npc.getKnownList().getKnownPlayers();
		for (int objId : world.allowed)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);
			if (player != null && player.isOnline() && player.getInstanceId() == world.instanceId)
			{
				npcKnownPlayers.put(player.getObjectId(), player);
			}
		}
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof FETWorld)
		{
			FETWorld world = (FETWorld) tmpworld;
			if (npc.getNpcId() == SCARLET1 && world.status == 3 && npc.getCurrentHp() < npc.getMaxHp() * 0.80)
			{
				controlStatus(world);
			}
			else if (npc.getNpcId() == SCARLET1 && world.status == 4 && npc.getCurrentHp() < npc.getMaxHp() * 0.20)
			{
				controlStatus(world);
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof FETWorld)
		{
			FETWorld world = (FETWorld) tmpworld;
			if (npc.getNpcId() == HALL_ALARM)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new StatusTask(world, 0), 2000);
				if (debug)
				{
					Log.info("[Final Emperial Tomb] Hall alarm is disabled, doors will open!");
				}
			}
			else if (npc.getNpcId() == DARK_CHOIR_PLAYER)
			{
				world.darkChoirPlayerCount--;
				if (world.darkChoirPlayerCount < 1)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new StatusTask(world, 2), 2000);
					if (debug)
					{
						Log.info("[Final Emperial Tomb] All Dark Choir Players are killed, doors will open!");
					}
				}
			}
			else if (npc.getNpcId() == SCARLET2)
			{
				controlStatus(world);
			}
			else if (world.status <= 2)
			{
				if (checkKillProgress(npc, world))
				{
					controlStatus(world);
				}
			}
			else if (world.demons.contains(npc))
			{
				world.demons.remove(npc);
			}
			else if (world.portraits.containsKey(npc))
			{
				world.portraits.remove(npc);
			}
		}
		return "";
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			st = newQuestState(player);
		}
		if (npcId == GUIDE)
		{
			enterInstance(player, "FinalEmperialTomb.xml", ENTER_TELEPORT);
		}
		else if (npc.getNpcId() == CUBE)
		{
			int x = -87534 + Rnd.get(500);
			int y = -153048 + Rnd.get(500);
			player.teleToLocation(x, y, -9165);
			return null;
		}
		return "";
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (skill.isSuicideAttack())
		{
			return onKill(npc, null, false);
		}
		return super.onSpellFinished(npc, player, skill);
	}

	public FinalEmperialTomb(int questId, String name, String descr)
	{
		super(questId, name, descr);

		load();
		addStartNpc(GUIDE);
		addTalkId(GUIDE);
		addStartNpc(CUBE);
		addTalkId(CUBE);
		addKillId(HALL_ALARM);
		addKillId(DARK_CHOIR_PLAYER);
		addAttackId(SCARLET1);
		addKillId(SCARLET2);
		addSpellFinishedId(18333);
		for (int mobId : PORTRAITS)
		{
			addKillId(mobId);
		}
		for (int mobId : DEMONS)
		{
			addKillId(mobId);
		}
		for (int mobId : mustKillMobsId)
		{
			addKillId(mobId);
		}
	}

	public static void main(String[] args)
	{
		// now call the constructor (starts up the)
		new FinalEmperialTomb(-1, qn, "instances");
	}
}
