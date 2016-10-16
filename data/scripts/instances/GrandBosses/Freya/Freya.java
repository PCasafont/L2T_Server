package instances.GrandBosses.Freya;

import ai.group_template.L2AttackableAIScript;
import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2GuardInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LasTravel
 *         <p>
 *         Freya Boss - Normal/Extreme mode
 *         <p>
 *         Source:
 *         - http://www.youtube.com/watch?v=cEDD0aLDx8E
 *         - http://www.youtube.com/watch?v=I-NelQQANA8&feature=related
 *         - http://www.youtube.com/watch?v=pA-IY-XJe4M&feature=related
 *         - http://www.youtube.com/watch?v=dhvbOOLSddc&feature=related
 *         - http://www.youtube.com/watch?v=NsDDytCXIEs&feature=related
 *         - http://www.youtube.com/watch?v=V3htZkt7uuM
 */

public class Freya extends L2AttackableAIScript
{
	//Quest
	private static final boolean debug = false;
	private static final String qn = "Freya";

	//Id's
	@SuppressWarnings("unused") private static final int glacier = 18853;
	private static final int frozenCore = 15470;
	private static final int dummy = 18932;
	private static final int jiniaId = 32781;
	private static final int jiniaInnerId = 18850;
	private static final int kegorInnerId = 18851;
	private static final int sirraId = 32762;
	private static final int[] templates = {139, 144};
	private static final int[] _all_mobs = {29177, 29178, 29179, 29180, 18854, 18855, 18856, 25699, 25700};
	private static final int[] roomEffects = {23140202, 23140204, 23140206, 23140208, 23140212, 23140214, 23140216};

	//Cords
	private static final Location[] playerEnter = {
			new Location(114185, -112435, -11210),
			new Location(114183, -112280, -11210),
			new Location(114024, -112435, -11210),
			new Location(114024, -112278, -11210),
			new Location(113865, -112435, -11210),
			new Location(113865, -112276, -11210)
	};

	//Spawns
	private static final int[][] iceKnightAroundRoom = {
			{113845, -116091, -11168, 8264},
			{113381, -115622, -11168, 8264},
			{113380, -113978, -11168, -8224},
			{113845, -113518, -11168, -8224},
			{115591, -113516, -11168, -24504},
			{116053, -113981, -11168, -24504},
			{116061, -115611, -11168, 24804},
			{115597, -116080, -11168, 24804},
			{112942, -115480, -10960, 52},
			{112940, -115146, -10960, 52},
			{112945, -114453, -10960, 52},
			{112945, -114123, -10960, 52},
			{116497, -114117, -10960, 32724},
			{116499, -114454, -10960, 32724},
			{116501, -115145, -10960, 32724},
			{116502, -115473, -10960, 32724}
	};

	private static final int[][] iceBreathingCenterRoom = {
			{114713, -115109, -11202, 16456},
			{114008, -115080, -11202, 3568},
			{114422, -115508, -11202, 12400},
			{115023, -115508, -11202, 20016},
			{115459, -115079, -11202, 27936},
			{114176, -114576, -11202, 16456},
			{115264, -114576, -11202, 3568}
	};

	private class FreyaWorld extends InstanceWorld
	{
		private int FirstFreyaId;
		private int LastFreyaId;
		private int GlakiasId;
		private int IceKnightId;
		private int ArchBreathId;
		private ArrayList<L2Npc> AllMobs;
		private ArrayList<L2Npc> Monuments;
		private L2Npc Freya;
		private L2Npc dummy;
		private L2GuardInstance jiniaInner;
		private L2GuardInstance kegorInner;

		public FreyaWorld()
		{
			AllMobs = new ArrayList<L2Npc>();
			Monuments = new ArrayList<L2Npc>();
		}
	}

	public Freya(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addTalkId(jiniaId);
		addStartNpc(jiniaId);
		addTalkId(sirraId);
		addStartNpc(sirraId);
		addFirstTalkId(sirraId);
		addFirstTalkId(jiniaInnerId);
		addFirstTalkId(kegorInnerId);

		for (int mob : _all_mobs)
		{
			addAttackId(mob);
			addKillId(mob);
			addSpellFinishedId(mob);
		}
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (debug)
		{
			Log.warning(getName() + ": onSpellFinished: " + skill.getName());
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
			Log.warning(getName() + ": onSpellFinished: Unable to get world.");
			return null;
		}

		if (wrld != null && wrld instanceof FreyaWorld)
		{
			FreyaWorld world = (FreyaWorld) wrld;
			if (npc.getNpcId() == world.FirstFreyaId || npc.getNpcId() == world.LastFreyaId)
			{
				switch (skill.getId())
				{
					case 6277: //Summon Spirits
						int radius = 100;

						for (int i = 0; i < 10; i++)
						{
							int x = (int) (radius * Math.cos(i * 0.618));
							int y = (int) (radius * Math.sin(i * 0.618));

							L2Npc minion =
									addSpawn(world.ArchBreathId, npc.getX() + x, npc.getY() + y, npc.getZ() + 20, -1,
											false, 0, true, world.instanceId);
							minion.setIsRunning(true);
							minion.getSpawn().stopRespawn();
							world.AllMobs.add(minion);
						}
						break;

					case 6697: //Eternal Blizzard
					case 6274:
					case 6275:
					case 6276:
						InstanceManager.getInstance()
								.sendPacket(world.instanceId, new ExShowScreenMessage(1801111, 2, true, 5000));
						break;
				}

				if (!npc.isInvul())
				{
					if (Rnd.get(100) < 10)
					{
						int sound = Rnd.get(1, 14);
						InstanceManager.getInstance().sendPacket(world.instanceId, new PlaySound(
								sound >= 10 ? "SystemMsg_e.freya_voice_" : "SystemMsg_e.freya_voice_0" + sound));
					}
				}
			}
		}
		return super.onSpellFinished(npc, player, skill);
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

		if (wrld != null && wrld instanceof FreyaWorld)
		{
			FreyaWorld world = (FreyaWorld) wrld;
			if (event.equalsIgnoreCase("stage_1_opendoor"))
			{
				if (world.status != 0)
				{
					return "";
				}

				world.status = 1;

				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(23140101).openMe();

				startQuestTimer("stage_1_intro", debug ? 10000 : 5 * 60000, npc, null);
			}
			else if (event.equalsIgnoreCase("stage_1_intro"))
			{
				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(23140101).closeMe();

				//Kick retards
				ArrayList<Integer> allowedPlayers = new ArrayList<Integer>(world.allowed);
				for (int objId : allowedPlayers)
				{
					L2PcInstance pl = L2World.getInstance().getPlayer(objId);
					if (pl != null && pl.isOnline() && pl.getInstanceId() == world.instanceId)
					{
						if (pl.getY() > -113290 && pl.getZ() < -10990)
						{
							world.allowed.remove((Integer) pl.getObjectId());
							pl.logout(true);
						}
					}
				}

				InstanceManager.getInstance().showVidToInstance(15, world.instanceId);

				startQuestTimer("stage_1_begin", ScenePlayerDataTable.getInstance().getVideoDuration(15) + 1000, npc,
						null);
			}
			else if (event.equalsIgnoreCase("stage_1_begin"))
			{
				world.status = 2;

				spawnMonuments(world);

				world.Freya =
						addSpawn(world.FirstFreyaId, 114720, -117085, -11088, 15956, false, 0, false, world.instanceId);

				world.dummy = addSpawn(dummy, 114707, -114797, -11199, 0, false, 0, false, world.instanceId);

				world.AllMobs.add(world.dummy);

				InstanceManager.getInstance()
						.sendPacket(world.instanceId, new ExShowScreenMessage(1300150, 2, true, 7000));

				startQuestTimer("stage_1_freyamove", 2000, world.Freya, null, false);
			}
			else if (event.equalsIgnoreCase("stage_1_freyamove"))
			{
				InstanceManager.getInstance()
						.sendPacket(world.instanceId, new ExShowScreenMessage(1801097, 2, true, 5000));

				world.dummy.setDisplayEffect(1);

				world.Freya.setIsRunning(true);
				world.Freya.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
						new L2CharPosition(114717, -114973, -11200, 0));
				world.Freya.getSpawn().setX(114717);
				world.Freya.getSpawn().setY(-114973);
				world.Freya.getSpawn().setZ(-11200);

				spawnMobs(world, world.IceKnightId);
			}
			else if (event.equalsIgnoreCase("stage_1_finalmovie"))
			{
				world.Freya.deleteMe();

				despawnAll(world);

				InstanceManager.getInstance().showVidToInstance(16, world.instanceId);

				startQuestTimer("stage_1_pause", ScenePlayerDataTable.getInstance().getVideoDuration(16) + 1000, npc,
						null);
			}
			else if (event.equalsIgnoreCase("stage_1_pause"))
			{
				world.status = 4;

				world.Freya =
						addSpawn(world.FirstFreyaId, 114723, -117502, -10672, 15956, false, 0, false, world.instanceId);
				world.Freya.setIsImmobilized(true);
				world.Freya.setIsInvul(true);
				world.Freya.disableCoreAI(true);
				world.Freya.disableAllSkills();

				despawnKnights(world);

				InstanceManager.getInstance().sendPacket(world.instanceId, new ExSendUIEvent(0, 0, 60, 0, 1801090));

				startQuestTimer("stage_2_begin", 60000, npc, null);
			}
			else if (event.equalsIgnoreCase("stage_2_begin"))
			{
				world.status = 5;

				InstanceManager.getInstance().sendPacket(world.instanceId, new ExSendUIEventRemove());
				InstanceManager.getInstance()
						.sendPacket(world.instanceId, new ExShowScreenMessage(1300151, 2, true, 7000));

				world.dummy.setDisplayEffect(3);

				spawnMonuments(world);

				spawnMobs(world, world.IceKnightId);

				startQuestTimer("stage_2_glakiasmovie", 60000, npc, null);
				startQuestTimer("stage_2_spawnbreaths", 30000, npc, null);
			}
			else if (event.equalsIgnoreCase("stage_2_spawnbreaths"))
			{
				for (L2Npc knight : world.AllMobs)
				{
					knight.getSpawn().stopRespawn();
				}
				spawnMobs(world, world.ArchBreathId);
			}
			else if (event.equalsIgnoreCase("stage_2_glakiasmovie"))
			{
				InstanceManager.getInstance().showVidToInstance(23, world.instanceId);

				startQuestTimer("stage_2_glakiasspawn", 7000 + 1000, npc, null);
			}
			else if (event.equalsIgnoreCase("stage_2_glakiasspawn"))
			{
				addSpawn(world.GlakiasId, 114707, -114799, -11199, 15956, false, 0, false, world.instanceId);
			}
			else if (event.equalsIgnoreCase("stage_2_pause"))
			{
				despawnAll(world);

				despawnKnights(world);

				InstanceManager.getInstance().sendPacket(world.instanceId, new ExSendUIEvent(0, 0, 60, 0, 1801090));

				startQuestTimer("stage_3_movie", 60000, npc, null);
			}
			else if (event.equalsIgnoreCase("stage_3_movie"))
			{
				world.Freya.deleteMe();

				InstanceManager.getInstance().showVidToInstance(17, world.instanceId);

				startQuestTimer("stage_3_begin", ScenePlayerDataTable.getInstance().getVideoDuration(17) + 1000, npc,
						null);
			}
			else if (event.equalsIgnoreCase("stage_3_begin"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new ExSendUIEventRemove());

				spawnMonuments(world);

				world.Freya =
						addSpawn(world.LastFreyaId, 114720, -117085, -11088, 15956, false, 0, false, world.instanceId);

				InstanceManager.getInstance()
						.sendPacket(world.instanceId, new ExShowScreenMessage(1300152, 2, true, 7000));

				world.dummy.setDisplayEffect(4);

				spawnMobs(world, world.ArchBreathId);

				startQuestTimer("stage_3_spawnmonuments", 60000, npc, null);

				//Start room tempest effects
				for (int id : roomEffects)
				{
					InstanceManager.getInstance().sendPacket(world.instanceId, new EventTrigger(id, true));
				}
			}
			else if (event.equalsIgnoreCase("stage_3_spawnmonuments"))
			{
				world.status = 6;
				for (L2Npc monument : world.Monuments)
				{
					world.AllMobs.add(monument);

					monument.getSpawn().stopRespawn();
					monument.setIsImmobilized(false);
					monument.setIsInvul(false);
					monument.enableAllSkills();
					monument.disableCoreAI(false);

					startQuestTimer("random_first_attack", Rnd.get(1000, 10000), monument, null);
				}
			}
			else if (event.equalsIgnoreCase("random_first_attack"))
			{
				if (!world.Monuments.isEmpty())
				{
					if (world.status < 6 && world.Monuments.contains(npc))
					{
						return null;
					}

					if (world.status == 6 && world.Monuments.contains(npc))
					{
						npc.setDisplayEffect(2);
					}

					int playerId = world.allowed.get(world.allowed.size() - 1);
					L2PcInstance victim = L2World.getInstance().getPlayer(playerId);
					if (victim != null && victim.isOnline() && victim.getInstanceId() == world.instanceId &&
							!npc.isImmobilized())
					{
						((L2Attackable) npc).addDamageHate(victim, 0, 99999);

						npc.setIsRunning(true);
						npc.setTarget(victim);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, victim, null);
					}
					else
					{
						startQuestTimer("random_first_attack", Rnd.get(1000, 2000), npc, null);
					}
				}
				else
				{
					if (npc.getNpcId() == world.GlakiasId || npc.getNpcId() == world.ArchBreathId)
					{
						npc.deleteMe();
					}
				}
			}
			else if (event.equalsIgnoreCase("stage_4_spawns"))
			{
				world.jiniaInner = (L2GuardInstance) addSpawn(18850, 114727, -114700, -11200, -16260, false, 0, false,
						world.instanceId);
				world.jiniaInner.setCanReturnToSpawnPoint(false);

				world.kegorInner = (L2GuardInstance) addSpawn(18851, 114690, -114700, -11200, -16260, false, 0, false,
						world.instanceId);
				world.kegorInner.setCanReturnToSpawnPoint(false);

				startQuestTimer("stage_4_buffs", 5000, npc, null);
			}
			else if (event.equalsIgnoreCase("stage_4_buffs"))
			{
				InstanceManager.getInstance()
						.sendPacket(world.instanceId, new ExShowScreenMessage(1801089, 2, true, 7000));

				//Invul again
				world.jiniaInner.setIsInvul(true);
				world.kegorInner.setIsInvul(true);

				world.jiniaInner.doCast(SkillTable.getInstance().getInfo(6288, 1));
				world.kegorInner.doCast(SkillTable.getInstance().getInfo(6289, 1));

				startQuestTimer("stage_4_attack", 6000, npc, null);
			}
			else if (event.equalsIgnoreCase("stage_4_attack"))
			{
				world.kegorInner.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				world.jiniaInner.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

				//Kegor
				world.kegorInner.setTarget(world.Freya);
				((L2Attackable) world.kegorInner).addDamageHate(world.Freya, 500, 99999);
				world.kegorInner.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world.Freya, null);

				//Jinia
				world.jiniaInner.setTarget(world.Freya);
				((L2Attackable) world.jiniaInner).addDamageHate(world.Freya, 500, 99999);
				world.jiniaInner.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world.Freya, null);

				world.jiniaInner.setIsRunning(true);
				world.kegorInner.setIsRunning(true);
			}
			else if (event.equalsIgnoreCase("stage_4_finalscene"))
			{
				InstanceManager.getInstance().showVidToInstance(20, world.instanceId);
			}
		}

		if (npc.getNpcId() == jiniaId && Util.isDigit(event) && Util.contains(templates, Integer.valueOf(event)))
		{
			try
			{
				enterInstance(player, Integer.valueOf(event));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return null;
		}
		return null;
	}

	@Override
	public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (debug)
		{
			Log.warning(getName() + ": onAttack: " + npc.getName());
		}

		final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpWorld instanceof FreyaWorld)
		{
			final FreyaWorld world = (FreyaWorld) tmpWorld;

			if (world.status == 2 && npc.getNpcId() == world.FirstFreyaId)
			{
				if (npc.getCurrentHp() < npc.getMaxHp() * 0.3)
				{
					world.status = 3;

					startQuestTimer("stage_1_finalmovie", 1000, npc, attacker);
				}
			}
			else if (world.status == 6 && npc.getNpcId() == world.LastFreyaId)
			{
				if (npc.getCurrentHp() < npc.getMaxHp() * 0.3)
				{
					world.status = 7;

					InstanceManager.getInstance().showVidToInstance(18, world.instanceId);

					startQuestTimer("stage_4_spawns", ScenePlayerDataTable.getInstance().getVideoDuration(18), npc,
							null);
				}
			}
			else if (npc.getNpcId() == world.IceKnightId && npc.getDisplayEffect() == 1)
			{
				if (npc.getCurrentHp() < npc.getMaxHp() * 0.3)
				{
					npc.setDisplayEffect(2);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (debug)
		{
			Log.warning(getName() + ": onKill: " + npc.getName());
		}

		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof FreyaWorld)
		{
			FreyaWorld world = (FreyaWorld) tmpworld;

			npc.deleteMe();

			if (npc.getNpcId() == world.GlakiasId)
			{
				startQuestTimer("stage_2_pause", 500, npc, null);
			}
			else if (npc.getNpcId() == world.LastFreyaId)
			{
				InstanceManager.getInstance().setInstanceReuse(world.instanceId, world.instanceId, true);
				InstanceManager.getInstance().finishInstance(world.instanceId, true);
				InstanceManager.getInstance().showVidToInstance(19, world.instanceId);

				startQuestTimer("stage_4_finalscene", ScenePlayerDataTable.getInstance().getVideoDuration(19) + 5000,
						npc, null);

				//Stop room tempest effects
				for (int id : roomEffects)
				{
					InstanceManager.getInstance().sendPacket(world.instanceId, new EventTrigger(id, false));
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (debug)
		{
			Log.warning(getName() + ": onFirstTalk: " + player);
		}

		int npcid = npc.getNpcId();
		if (npcid == jiniaInnerId || npcid == kegorInnerId)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else if (npcid == sirraId)
		{
			return "Sirra.html";
		}
		return super.onFirstTalk(npc, player);
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (debug)
		{
			Log.warning(getName() + ": onTalk: " + player.getName());
		}

		int npcId = npc.getNpcId();
		if (npcId == jiniaId)
		{
			return "Jinia.html";
		}

		return "";
	}

	private void setupIDs(FreyaWorld world, int template_id)
	{
		if (template_id == 144) //EPIC
		{
			world.FirstFreyaId = 29177;
			world.LastFreyaId = 29180;
			world.GlakiasId = 25700;
			world.IceKnightId = 18856;
			world.ArchBreathId = 18854;
		}
		else
		{
			world.FirstFreyaId = 29177;
			world.LastFreyaId = 29179;
			world.GlakiasId = 25699;
			world.IceKnightId = 18855;
			world.ArchBreathId = 18854;
		}
	}

	private final synchronized void enterInstance(L2PcInstance player, int template_id)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof FreyaWorld))
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
					player.teleToLocation(114711, -113701, -11199, true);
				}
			}
			return;
		}
		else
		{
			//int minPlayers = template_id == 144 ? Config.FREYA_MIN_PLAYERS * 2 : Config.FREYA_MIN_PLAYERS;
			int minPlayers = Config.FREYA_MIN_PLAYERS;
			int maxLvl = 93;
			if (Config.isServer(Config.TENKAI_ESTHUS))
			{
				maxLvl = Config.MAX_LEVEL;
			}
			if (!debug && !InstanceManager.getInstance()
					.checkInstanceConditions(player, template_id, minPlayers, 35, 77, maxLvl))
			{
				return;
			}

			final int instanceId = InstanceManager.getInstance().createDynamicInstance(qn + ".xml");
			world = new FreyaWorld();
			world.instanceId = instanceId;
			world.templateId = template_id;
			world.status = 0;

			InstanceManager.getInstance().addWorld(world);

			setupIDs((FreyaWorld) world, template_id);

			List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();
			if (debug)
			{
				allPlayers.add(player);
			}
			else
			{
				allPlayers.addAll(minPlayers > Config.MAX_MEMBERS_IN_PARTY ?
						player.getParty().getCommandChannel().getMembers() :
						player.getParty().getCommandChannel() != null ?
								player.getParty().getCommandChannel().getMembers() :
								player.getParty().getPartyMembers());
			}

			for (L2PcInstance enterPlayer : allPlayers)
			{
				if (enterPlayer == null)
				{
					continue;
				}

				//Delete Frozen core
				enterPlayer.deleteAllItemsById(frozenCore);
				//Give new Frozen Core
				enterPlayer.getInventory().addItem(qn, frozenCore, 3, enterPlayer, enterPlayer);

				world.allowed.add(enterPlayer.getObjectId());

				enterPlayer.stopAllEffectsExceptThoseThatLastThroughDeath();
				enterPlayer.setInstanceId(instanceId);
				enterPlayer.teleToLocation(playerEnter[Rnd.get(0, playerEnter.length - 1)], true);
			}

			Log.fine(getName() + ": [" + template_id + "] instance started: " + instanceId + " created by player: " +
					player.getName());
			return;
		}
	}

	private void despawnAll(FreyaWorld world)
	{
		for (L2Npc npc : world.AllMobs)
		{
			npc.getSpawn().stopRespawn();
			npc.deleteMe();
		}
		world.AllMobs.clear();
	}

	private void despawnKnights(FreyaWorld world)
	{
		for (L2Npc npc : world.Monuments)
		{
			npc.setDisplayEffect(2);
			npc.getSpawn().stopRespawn();
			npc.doDie(npc);
		}
		world.Monuments.clear();
	}

	private void spawnMobs(FreyaWorld world, int mobId)
	{
		for (int sp[] : iceBreathingCenterRoom)
		{
			L2Npc npc = addSpawn(mobId, sp[0], sp[1], sp[2], sp[3], false, 0, false, world.instanceId);
			world.AllMobs.add(npc);

			if (mobId == world.IceKnightId)
			{
				npc.setDisplayEffect(2);
			}
			else
			{
				npc.setDisplayEffect(1);
			}

			npc.setIsRunning(true);

			L2Spawn spawn = npc.getSpawn();
			spawn.setRespawnDelay(20);
			spawn.startRespawn();
		}
	}

	private void spawnMonuments(FreyaWorld world)
	{
		for (int sp[] : iceKnightAroundRoom)
		{
			L2Npc npc = addSpawn(world.IceKnightId, sp[0], sp[1], sp[2], sp[3], false, 0, false, world.instanceId);
			world.Monuments.add(npc);
			npc.disableCoreAI(true);
			npc.setIsImmobilized(true);
			npc.setIsInvul(true);
			npc.disableAllSkills();
			npc.setDisplayEffect(1);
			npc.setEnchant(1);

			L2Spawn spawn = npc.getSpawn();
			spawn.getNpc().setSpawn(spawn);
			spawn.setRespawnDelay(20);
			spawn.startRespawn();
		}
	}

	public static void main(String[] args)
	{
		new Freya(-1, qn, "instances/GrandBosses");
	}
}
