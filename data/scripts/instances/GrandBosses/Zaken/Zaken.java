package instances.GrandBosses.Zaken;

import ai.group_template.L2AttackableAIScript;
import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExSendUIEvent;
import l2server.gameserver.network.serverpackets.ExSendUIEventRemove;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author LasTravel
 *         <p>
 *         Zaken Boss - Day time/Day Time 83/ Night Time
 *         <p>
 *         Source:
 *         - L2Inc Zaken Instance :(
 *         - https://www.youtube.com/watch?v=mIZcYjwGlnQ (Day Time Easy)
 *         - https://www.youtube.com/watch?v=dAE90X8yh4A (Day Time Hard)
 */

public class Zaken extends L2AttackableAIScript
{
	//Quest
	private static final boolean debug = false;
	private static final String qn = "Zaken";

	//Id's
	private static final int zakenDayTimeEasy = 29176;//60
	private static final int zakenDayTimeHard = 29181;//83
	private static final int zakenNightTime = 29022;//60
	private static final int dayTimeEasy = 133;
	private static final int dayTimeHard = 135;
	private static final int nightTimeEpic = 114;
	private static final int[] templates = {nightTimeEpic, dayTimeEasy, dayTimeHard};
	private static final int pathfinderId = 32713;
	private static final int barrelId = 32705;

	//Others
	private static final List<L2ZoneType> zakenRooms = new ArrayList<L2ZoneType>(15);

	private static final Location[] zakenSpawns = {
			new Location(54237, 218135, -3496),
			new Location(56288, 218087, -3496),
			new Location(55273, 219140, -3496),
			new Location(54232, 220184, -3496),
			new Location(56259, 220168, -3496),
			new Location(54250, 218122, -3224),
			new Location(56308, 218125, -3224),
			new Location(55243, 219064, -3224),
			new Location(54255, 220156, -3224),
			new Location(56255, 220161, -3224),
			new Location(54261, 218095, -2952),
			new Location(56258, 218086, -2952),
			new Location(55258, 219080, -2952),
			new Location(54292, 220096, -2952),
			new Location(56258, 220135, -2952)
	};

	private static final Map<Location, Integer> barrelSpawnsInfo = new HashMap<Location, Integer>();

	{
		barrelSpawnsInfo.put(new Location(53312, 220128, -3484), 120114); //ok
		barrelSpawnsInfo.put(new Location(54241, 221062, -3479), 120114); //ok
		barrelSpawnsInfo.put(new Location(54333, 219104, -3484), 120113); //ok
		barrelSpawnsInfo.put(new Location(53312, 218079, -3484), 120111); //ok
		barrelSpawnsInfo.put(new Location(55260, 218171, -3484), 120113); //ok
		barrelSpawnsInfo.put(new Location(55266, 220042, -3484), 120113); //ok
		barrelSpawnsInfo.put(new Location(56288, 221056, -3484), 120115); //ok
		barrelSpawnsInfo.put(new Location(57200, 220128, -3484), 120115); //ok
		barrelSpawnsInfo.put(new Location(56192, 219104, -3484), 120113); //ok
		barrelSpawnsInfo.put(new Location(57216, 218080, -3484), 120112); //ok
		barrelSpawnsInfo.put(new Location(56286, 217156, -3484), 120112); //ok
		barrelSpawnsInfo.put(new Location(54240, 217168, -3484), 120111); //ok
		barrelSpawnsInfo.put(new Location(53332, 220128, -3207), 120119); //ok
		barrelSpawnsInfo.put(new Location(54240, 221040, -3212), 120119); //ok
		barrelSpawnsInfo.put(new Location(54336, 219104, -3212), 120118); //ok
		barrelSpawnsInfo.put(new Location(53312, 218080, -3212), 120116); //ok
		barrelSpawnsInfo.put(new Location(55270, 218176, -3212), 120118); //ok
		barrelSpawnsInfo.put(new Location(55264, 220032, -3212), 120118); //ok
		barrelSpawnsInfo.put(new Location(56288, 221040, -3212), 120120); //ok
		barrelSpawnsInfo.put(new Location(57200, 220128, -3212), 120120); //ok
		barrelSpawnsInfo.put(new Location(56192, 219104, -3212), 120118); //ok
		barrelSpawnsInfo.put(new Location(57213, 218080, -3209), 120117); //ok
		barrelSpawnsInfo.put(new Location(56293, 217149, -3211), 120117); //ok
		barrelSpawnsInfo.put(new Location(54240, 217152, -3212), 120116); //ok
		barrelSpawnsInfo.put(new Location(53328, 220128, -2940), 120124); //ok
		barrelSpawnsInfo.put(new Location(54240, 221040, -2940), 120124); //ok
		barrelSpawnsInfo.put(new Location(54331, 219104, -2940), 120123); //ok
		barrelSpawnsInfo.put(new Location(53328, 218080, -2936), 120121); //ok
		barrelSpawnsInfo.put(new Location(55264, 218165, -2940), 120123); //ok
		barrelSpawnsInfo.put(new Location(55264, 220016, -2940), 120123); //ok
		barrelSpawnsInfo.put(new Location(56288, 221024, -2940), 120125); //ok
		barrelSpawnsInfo.put(new Location(57200, 220128, -2940), 120125); //ok
		barrelSpawnsInfo.put(new Location(56192, 219104, -2940), 120123); //ok
		barrelSpawnsInfo.put(new Location(57200, 218080, -2940), 120122); //ok
		barrelSpawnsInfo.put(new Location(56288, 217152, -2940), 120122); //ok
		barrelSpawnsInfo.put(new Location(54240, 217152, -2940), 120121); //ok
	}

	;

	//Cords
	private static final Location playerEnter = new Location(52646, 219100, -3233);

	private class ZakenWorld extends InstanceWorld
	{
		private Map<L2Npc, Integer> barrelInfo;
		private int blueCandlesCount;
		private int zakenId;
		private int dollBladerId;
		private int valeMasterId;
		private int zombieCaptainId;
		private int zombieId;
		private L2Npc zakenBoss;
		private Location zakenLocation;

		public ZakenWorld()
		{
			barrelInfo = new HashMap<L2Npc, Integer>();
		}
	}

	public Zaken(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(pathfinderId);
		addTalkId(pathfinderId);
		addFirstTalkId(pathfinderId);
		addFirstTalkId(barrelId);
		addKillId(zakenNightTime);
		addKillId(zakenDayTimeEasy);
		addKillId(zakenDayTimeHard);

		for (int zoneId = 120111; zoneId <= 120125; zoneId++)
		{
			L2ZoneType zakenRoom = ZoneManager.getInstance().getZoneById(zoneId);

			zakenRooms.add(zakenRoom);
		}
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (debug)
		{
			Log.warning(getName() + ": onAdvEvent: " + event);
		}

		InstanceWorld wrld = null;
		if (npc != null)
		{
			wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		}
		else if (player != null)
		{
			wrld = InstanceManager.getInstance().getPlayerWorld(player);
		}
		else
		{
			Log.warning(getName() + ": onAdvEvent: Unable to get world.");
			return null;
		}

		if (wrld != null && wrld instanceof ZakenWorld)
		{
			ZakenWorld world = (ZakenWorld) wrld;
			if (event.equalsIgnoreCase("stage_0_start"))
			{
				InstanceManager.getInstance()
						.sendPacket(world.instanceId, new ExSendUIEvent(0, 1, 0, 0, 1911119)); //Elapsed Time

				world.zakenLocation = zakenSpawns[Rnd.get(zakenSpawns.length)];

				//Zaken is already spawned on the Night Time Instance
				if (world.templateId == nightTimeEpic)
				{
					world.zakenBoss = addSpawn(world.zakenId, world.zakenLocation.getX(), world.zakenLocation.getY(),
							world.zakenLocation.getZ(), 0, false, 0, false, world.instanceId);
				}
				else
				{
					//Barrels
					for (Entry<Location, Integer> barrelInfo : barrelSpawnsInfo.entrySet())
					{
						L2Npc barrel = addSpawn(barrelId, barrelInfo.getKey().getX(), barrelInfo.getKey().getY(),
								barrelInfo.getKey().getZ(), 0, false, 0, false, world.instanceId);
						world.barrelInfo.put(barrel, barrelInfo.getValue());
					}
					InstanceManager.getInstance().sendPacket(world.instanceId,
							new ExShowScreenMessage(1800866, 10000)); //The candles can lead you to Zaken. Destroy him
				}

				if (debug)
				{
					Log.warning(getName() + ": Zaken will be spawned on cords: " + world.zakenLocation.getX() + ", " +
							world.zakenLocation.getY() + ", " + world.zakenLocation.getZ());
				}
			}
			else if (event.equalsIgnoreCase("stage_all_check_barrel"))
			{
				if (world.blueCandlesCount == 4)
				{
					return "";
				}

				if (npc.isInsideRadius(world.zakenLocation.getX(), world.zakenLocation.getY(),
						world.zakenLocation.getZ(), 1500, true, false))
				{
					npc.setDisplayEffect(3); //Blue

					world.blueCandlesCount++;

					if (world.blueCandlesCount == 4)
					{
						InstanceManager.getInstance().sendPacket(world.instanceId,
								new ExShowScreenMessage(1800867, 5000)); //Who dares awaken the mighty Zaken?

						world.zakenBoss =
								addSpawn(world.zakenId, world.zakenLocation.getX(), world.zakenLocation.getY(),
										world.zakenLocation.getZ(), 0, false, 0, false, world.instanceId);
						world.zakenBoss.setTarget(player);
						world.zakenBoss.setIsRunning(true);

						((L2Attackable) world.zakenBoss).addDamageHate(player, 500, 99999);
						((L2Attackable) world.zakenBoss).getAI()
								.setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
					}
				}
				else
				{
					npc.setDisplayEffect(2);

					//Spawn Minions
					for (L2ZoneType zakenRoom : zakenRooms)
					{
						if (world.barrelInfo.get(npc) == zakenRoom.getId())
						{
							int[] randomSpawn = null;

							if (debug)
							{
								Log.warning(getName() + ": Found the nearest zone!");
							}

							for (int i = 1; i <= 6; i++)
							{
								randomSpawn = zakenRoom.getZone().getRandomPoint();
								addSpawn(world.zombieId, randomSpawn[0], randomSpawn[1], randomSpawn[2], Rnd.get(65000),
										false, 0, false, world.instanceId);
							}

							for (int i = 1; i <= 4; i++)
							{
								randomSpawn = zakenRoom.getZone().getRandomPoint();
								addSpawn(world.dollBladerId, randomSpawn[0], randomSpawn[1], randomSpawn[2],
										Rnd.get(65000), false, 0, false, world.instanceId);
							}

							for (int i = 1; i <= 3; i++)
							{
								randomSpawn = zakenRoom.getZone().getRandomPoint();
								addSpawn(world.valeMasterId, randomSpawn[0], randomSpawn[1], randomSpawn[2],
										Rnd.get(65000), false, 0, false, world.instanceId);
							}

							for (int i = 1; i <= 3; i++)
							{
								randomSpawn = zakenRoom.getZone().getRandomPoint();
								addSpawn(world.zombieCaptainId, randomSpawn[0], randomSpawn[1], randomSpawn[2],
										Rnd.get(65000), false, 0, false, world.instanceId);
							}
							break;
						}
					}
				}
			}
		}

		if (npc != null && npc.getNpcId() == pathfinderId)
		{
			if (event.endsWith(".html"))
			{
				return event;
			}
			else if (Util.isDigit(event) && Util.contains(templates, Integer.valueOf(event)))
			{
				try
				{
					enterInstance(player, Integer.valueOf(event));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance attacker, boolean isPet)
	{
		if (debug)
		{
			Log.warning(getName() + ": onKill: " + attacker.getName());
		}

		final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());

		if (tmpWorld instanceof ZakenWorld)
		{
			final ZakenWorld world = (ZakenWorld) tmpWorld;
			if (npc.getNpcId() == world.zakenId)
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new ExSendUIEventRemove());
				InstanceManager.getInstance().setInstanceReuse(world.instanceId, world.templateId,
						world.templateId == dayTimeEasy ? false : world.templateId == dayTimeHard ? false : true);
				InstanceManager.getInstance().finishInstance(world.instanceId, true);
			}
		}

		return super.onKill(npc, attacker, isPet);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (debug)
		{
			Log.warning(getName() + ": onFirstTalk: " + player.getName());
		}

		if (npc.getNpcId() == barrelId)
		{
			if (npc.getDisplayEffect() == 0)
			{
				npc.setDisplayEffect(1); //Sparks

				startQuestTimer("stage_all_check_barrel", 5000, npc, player);
			}
		}
		else if (npc.getNpcId() == pathfinderId)
		{
			return "32713.html";
		}

		return super.onFirstTalk(npc, player);
	}

	private void setupIDs(ZakenWorld world, int template_id)
	{
		if (template_id == nightTimeEpic) //Cavern of the Pirate Captain (Nightmare)
		{
			world.zakenId = zakenNightTime;
		}
		else if (template_id == dayTimeEasy) //Cavern of the Pirate Captain (Daydream)
		{
			world.zakenId = zakenDayTimeEasy;
			world.dollBladerId = 29023;
			world.valeMasterId = 29024;
			world.zombieCaptainId = 29026;
			world.zombieId = 29027;
		}
		else
		//135 Cavern of the Pirate Captain (Distant Daydream)
		{
			world.zakenId = zakenDayTimeHard;
			world.dollBladerId = 29182;
			world.valeMasterId = 29183;
			world.zombieCaptainId = 29184;
			world.zombieId = 29185;
		}
	}

	private final synchronized void enterInstance(L2PcInstance player, int template_id)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof ZakenWorld))
			{
				player.sendPacket(
						SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}

			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null)
			{
				if (inst.getInstanceEndTime() > 300600 && world.allowed.contains(player.getObjectId()))
				{
					player.setInstanceId(world.instanceId);
					player.teleToLocation(playerEnter, true);
				}
			}
			return;
		}
		else
		{
			//DAFUK!
			int minPlayers = template_id == dayTimeEasy ? 7 : template_id == dayTimeHard ? 7 : 14;
			int maxPlayers = template_id == dayTimeEasy ? 21 : template_id == dayTimeHard ? 21 : 350;
			int minLevel = template_id == dayTimeEasy ? 53 : template_id == dayTimeHard ? 76 : 53;
			int maxLevel = template_id == dayTimeEasy ? 68 : template_id == dayTimeHard ? 90 : 68;

			if (Config.isServer(Config.TENKAI_ESTHUS) && template_id == dayTimeHard)
			{
				minPlayers = 7;
				maxLevel = Config.MAX_LEVEL;
			}

			if (!debug && !InstanceManager.getInstance()
					.checkInstanceConditions(player, template_id, minPlayers, maxPlayers, minLevel, maxLevel))
			{
				return;
			}

			String template = template_id == dayTimeEasy ? "Zaken-daytime" :
					template_id == dayTimeHard ? "Zaken-daytime83" : "Zaken-nighttime";
			final int instanceId = InstanceManager.getInstance().createDynamicInstance(template + ".xml");
			world = new ZakenWorld();
			world.instanceId = instanceId;
			world.templateId = template_id;
			world.status = 0;

			InstanceManager.getInstance().addWorld(world);

			setupIDs((ZakenWorld) world, template_id);

			List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();
			if (debug)
			{
				allPlayers.add(player);
			}
			else
			{
				if (player.getParty().getCommandChannel() != null)
				{
					allPlayers.addAll(player.getParty().getCommandChannel().getMembers());
				}
				else
				{
					allPlayers.addAll(player.getParty().getPartyMembers());
				}
			}

			for (L2PcInstance enterPlayer : allPlayers)
			{
				if (enterPlayer == null)
				{
					continue;
				}

				world.allowed.add(enterPlayer.getObjectId());

				enterPlayer.stopAllEffectsExceptThoseThatLastThroughDeath();
				enterPlayer.setInstanceId(instanceId);
				enterPlayer.teleToLocation(playerEnter, true);
			}

			startQuestTimer("stage_0_start", 5000, null, player);

			Log.fine(getName() + ": [" + template_id + "] instance started: " + instanceId + " created by player: " +
					player.getName());
			return;
		}
	}

	public static void main(String[] args)
	{
		new Zaken(-1, qn, "instances/GrandBosses");
	}
}
