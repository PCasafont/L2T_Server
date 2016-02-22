
package instances.GrandBosses.Beleth;

import java.util.ArrayList;
import java.util.List;

import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.PlaySound;
import l2server.gameserver.network.serverpackets.SocialAction;
import l2server.gameserver.network.serverpackets.SpecialCamera;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Broadcast;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel (Based on Abyss script)
 *
 *	Source:
 *			- http://www.youtube.com/watch?v=XC57OBGMqgo
 *			- http://www.youtube.com/watch?v=QYrtMywPuHI&feature=related
 */

public class Beleth extends L2AttackableAIScript
{
	//Quest
	private static final boolean _debug = false;
	private static final String _qn = "Beleth";
	
	//Id's
	private static final int _npcEnter = !Config.isServer(Config.DREAMS) ? 33881 : 80001;
	private static final int _stoneId = 32470;
	private static final int _priestId = 29128;
	private static final int _realBelethId = 29118;
	private static final int _fakeBelethId = 29119;
	private static final int _door_1 = 20240001;
	private static final int _door_2 = 20240002;
	private static final int _door_3 = 20240003;
	private static final int _instanceTemplateId = 500;
	private static final int _camera_1_id = 29120;
	private static final int _camera_2_id = 29121;
	private static final int _camera_3_id = 29122;
	private static final int _camera_4_id = 29123;
	private static final int _camera_6_id = 29125;
	
	//Vars
	private static int[] _cloneX = new int[32];
	private static int[] _cloneY = new int[32];
	private static int[] _cloneH = new int[32];
	
	//Cords
	private static final Location[] _enterCords = { new Location(16311, 209100, -9360), new Location(16002, 209388, -9360), new Location(16572, 209655, -9360), new Location(16514, 209942, -9360), new Location(16364, 209348, -9360) };
	
	public Beleth(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addTalkId(_npcEnter);
		
		addStartNpc(_npcEnter);
		
		addFirstTalkId(_stoneId);
		
		addFirstTalkId(_priestId);
		
		addKillId(_realBelethId);
		
		addKillId(_fakeBelethId);
		
		addAggroRangeEnterId(_realBelethId);
		
		addAggroRangeEnterId(_fakeBelethId);
		
		//Calculate shits
		double angle = 22.5;
		
		int innerRad;
		
		int outerRad;
		
		for (int i = 0; i < 16; i++)
		{
			if ((i % 2) == 0)
			{
				innerRad = 650;
				
				outerRad = 1200;
			}
			else
			{
				innerRad = 700;
				
				outerRad = 1250;
			}
			
			_cloneX[i] = 16327;
			
			_cloneX[i] += (int) (innerRad * Math.sin(i * Math.toRadians(angle)));
			
			_cloneY[i] = 213135;
			
			_cloneY[i] += (int) (innerRad * Math.cos(i * Math.toRadians(angle)));
			
			_cloneH[i] = Util.convertDegreeToClientHeading(270 - (i * angle));
			
			_cloneX[i + 16] = 16327;
			
			_cloneX[i + 16] += (int) (outerRad * Math.sin(i * Math.toRadians(angle)));
			
			_cloneY[i + 16] = 213135;
			
			_cloneY[i + 16] += (int) (outerRad * Math.cos(i * Math.toRadians(angle)));
			
			_cloneH[i + 16] = Util.convertDegreeToClientHeading(90 - (i * angle));
		}
	}
	
	private class belethWorld extends InstanceWorld
	{
		private List<L2Npc> Minions = null;
		
		private L2Npc beleth;
		
		private L2Npc priest;
		
		private L2Npc camera_1;
		
		private L2Npc camera_2;
		
		private L2Npc camera_3;
		
		private L2Npc camera_4;
		
		public belethWorld()
		{
			Minions = new ArrayList<L2Npc>();
		}
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (_debug)
			Log.warning(getName() + ": onAggroRangeEnter: " + player);
		
		if ((npc.getNpcId() == _realBelethId) || (npc.getNpcId() == _fakeBelethId))
		{
			//Trick
			if (isPet)
			{
				if (player.getPet() != null)
				{
					((L2Attackable) npc).addDamageHate(player.getPet(), 0, 999);
					
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player.getPet());
				}
				else if (player.getSummons() != null)
				{
					for (L2SummonInstance summon : player.getSummons())
					{
						if (summon == null)
						{
							continue;
						}
						
						((L2Attackable) npc).addDamageHate(summon, 0, 999);
						
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, summon);
					}
				}
			}
		}
		
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (_debug)
			Log.warning(getName() + ": onFirstTalk: " + player);
		
		int npcid = npc.getNpcId();
		
		if (npcid == _stoneId)
		{
			return null;
		}
		
		return super.onFirstTalk(npc, player);
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (_debug)
			Log.warning(getName() + ": onAdvEvent: " + event);
		
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
		
		if ((wrld != null) && (wrld instanceof belethWorld))
		{
			belethWorld world = (belethWorld) wrld;
			
			if (event.equalsIgnoreCase("stage_1_open_door"))
			{
				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_door_1).openMe();
				
				startQuestTimer("stage_1_intro_1", _debug ? 20000 : 5 * 60000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1"))
			{
				InstanceManager.getInstance().stopWholeInstance(world.instanceId);
				
				world.camera_1 = addSpawn(_camera_1_id, 16323, 213142, -9357, 0, false, 0, false, world.instanceId);
				
				world.camera_2 = addSpawn(_camera_2_id, 16323, 210741, -9357, 0, false, 0, false, world.instanceId);
				
				world.camera_3 = addSpawn(_camera_3_id, 16323, 213170, -9357, 0, false, 0, false, world.instanceId);
				
				world.camera_4 = addSpawn(_camera_4_id, 16323, 214917, -9356, 0, false, 0, false, world.instanceId);
				
				InstanceManager.getInstance().sendPacket(world.instanceId, new PlaySound(1, "BS07_A", 1, world.camera_1.getObjectId(), world.camera_1.getX(), world.camera_1.getY(), world.camera_1.getZ()));
				
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_1.getObjectId(), 400, 75, -25, 0, 2500, 0, 0, 1, 0));
				
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_1.getObjectId(), 400, 75, -25, 0, 2500, 0, 0, 1, 0));
				
				startQuestTimer("stage_1_intro_1_2", 300, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_2"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_1.getObjectId(), 1800, -45, -45, 5000, 5000, 0, 0, 1, 0));
				
				startQuestTimer("stage_1_intro_1_3", 4900, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_3"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_1.getObjectId(), 2500, -120, -45, 5000, 5000, 0, 0, 1, 0));
				
				startQuestTimer("stage_1_intro_1_4", 4900, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_4"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_2.getObjectId(), 2200, 130, 0, 0, 1500, -20, 15, 1, 0));
				
				startQuestTimer("stage_1_intro_1_5", 1400, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_5"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_2.getObjectId(), 2300, 100, 0, 2000, 4500, 0, 10, 1, 0));
				
				startQuestTimer("stage_1_intro_1_6", 2500, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_6"))
			{
				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_door_1).closeMe();
				
				startQuestTimer("stage_1_intro_1_7", 1700, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_7"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_4.getObjectId(), 1500, 210, 0, 0, 1500, 0, 0, 1, 0));
				
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_4.getObjectId(), 900, 255, 0, 5000, 6500, 0, 10, 1, 0));
				
				startQuestTimer("stage_1_intro_1_8", 6000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_8"))
			{
				addSpawn(_camera_6_id, 16323, 214917, -9356, 0, false, 0, false, world.instanceId); //Camera 6
				
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_4.getObjectId(), 900, 255, 0, 0, 1500, 0, 10, 1, 0));
				
				startQuestTimer("stage_1_intro_1_9", 1000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_9"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_4.getObjectId(), 1000, 255, 0, 7000, 17000, 0, 25, 1, 0));
				
				startQuestTimer("stage_1_intro_1_10", 3000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_10"))
			{
				world.beleth = addSpawn(_realBelethId, 16321, 214211, -9352, 49369, false, 0, false, world.instanceId);
				
				startQuestTimer("stage_1_intro_1_11", 200, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_11"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SocialAction(world.beleth.getObjectId(), 1));
				
				for (int i = 0; i < 6; i++)
				{
					int x = (int) ((150 * Math.cos(i * 1.046666667)) + 16323);
					
					int y = (int) ((150 * Math.sin(i * 1.046666667)) + 213059);
					
					L2Npc minion = addSpawn(_fakeBelethId, x, y, -9357, 49152, false, 0, false, world.instanceId);
					
					minion.setShowSummonAnimation(true);
					
					minion.decayMe();
					
					world.Minions.add(minion);
				}
				
				startQuestTimer("stage_1_intro_1_12", 6800, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_12"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.beleth.getObjectId(), 0, 270, -5, 0, 4000, 0, 0, 1, 0));
				
				startQuestTimer("stage_1_intro_1_13", 3500, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_13"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SocialAction(world.beleth.getObjectId(), 4));
				
				InstanceManager.getInstance().sendPacket(world.instanceId, new MagicSkillUse(world.beleth, world.beleth, 5531, 1, 2000, 0, 0));
				
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.beleth.getObjectId(), 800, 270, 10, 3000, 6000, 0, 0, 1, 0));
				
				startQuestTimer("stage_1_intro_1_14", 5000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_14"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_3.getObjectId(), 100, 270, 15, 0, 5000, 0, 0, 1, 0));
				
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_3.getObjectId(), 100, 270, 15, 0, 5000, 0, 0, 1, 0));
				
				startQuestTimer("stage_1_intro_1_15", 100, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_15"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_3.getObjectId(), 100, 270, 15, 3000, 6000, 0, 5, 1, 0));
				
				startQuestTimer("stage_1_intro_1_16", 1400, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_16"))
			{
				world.beleth.teleToLocation(16323, 213059, -9357, 49152, false);
				
				world.beleth.setIsImmobilized(true);
				
				if (world.beleth.isCastingNow())
				{
					world.beleth.abortCast();
				}
				
				startQuestTimer("stage_1_intro_1_17", 200, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_17"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new MagicSkillUse(world.beleth, world.beleth, 5532, 1, 2000, 0, 0));
				
				startQuestTimer("stage_1_intro_1_18", 2000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_18"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_3.getObjectId(), 700, 270, 20, 1500, 4000, 0, 0, 1, 0));
				
				startQuestTimer("stage_1_intro_1_19", 4000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_19"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_3.getObjectId(), 40, 260, 0, 0, 4000, 0, 0, 1, 0));
				
				for (L2Npc blth : world.Minions)
				{
					blth.spawnMe();
					
					blth.setIsImmobilized(true);//3000
				}
				
				startQuestTimer("stage_1_intro_1_20", 3000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_20"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_3.getObjectId(), 40, 280, 0, 0, 4000, 5, 0, 1, 0));
				
				L2Npc minion = addSpawn(_fakeBelethId, 16253, 213144, -9357, 49152, false, 0, false, world.instanceId);
				
				minion.setShowSummonAnimation(true);
				
				minion.decayMe();
				
				world.Minions.add(minion);
				
				minion.spawnMe();
				
				minion.setIsImmobilized(true);
				
				startQuestTimer("stage_1_intro_1_21", 3000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_21"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_3.getObjectId(), 5, 250, 5, 0, 13000, 20, 15, 1, 0));
				
				startQuestTimer("stage_1_intro_1_22", 1000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_22"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SocialAction(world.beleth.getObjectId(), 3));
				
				startQuestTimer("stage_1_intro_1_23", 4000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_23"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new MagicSkillUse(world.beleth, world.beleth, 5533, 1, 2000, 0, 0));
				
				startQuestTimer("stage_1_intro_1_24", 2000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_24"))
			{
				world.beleth.deleteMe();
				
				startQuestTimer("stage_1_intro_1_25", 1000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_25"))
			{
				for (L2Npc bel : world.Minions)
					bel.deleteMe();
				world.Minions.clear();
				
				startQuestTimer("stage_1_intro_1_26", 3000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_26"))
			{
				world.camera_1.deleteMe();
				
				world.camera_2.deleteMe();
				
				world.camera_3.deleteMe();
				
				world.camera_4.deleteMe();
				
				startQuestTimer("stage_1_intro_1_27", 1, null, player);
			}
			else if (event.equalsIgnoreCase("stage_1_intro_1_27"))
			{
				spawnBeleths(world);
				
				InstanceManager.getInstance().startWholeInstance(world.instanceId);
			}
			else if (event.equalsIgnoreCase("stage_final_1"))
			{
				world.beleth.doDie(null);
				
				world.camera_3 = addSpawn(_camera_3_id, 16323, 213170, -9357, 0, false, 0, false, world.instanceId);
				
				InstanceManager.getInstance().sendPacket(world.instanceId, new PlaySound(1, "BS07_D", 1, world.camera_3.getObjectId(), world.camera_3.getX(), world.camera_3.getY(), world.camera_3.getZ()));
				
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_3.getObjectId(), 400, 290, 25, 0, 10000, 0, 0, 1, 0));
				
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_3.getObjectId(), 400, 290, 25, 0, 10000, 0, 0, 1, 0));
				
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_3.getObjectId(), 400, 110, 25, 4000, 10000, 0, 0, 1, 0));
				
				InstanceManager.getInstance().sendPacket(world.instanceId, new SocialAction(world.beleth.getObjectId(), 5));
				
				startQuestTimer("stage_final_2", 4000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_final_2"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_1.getObjectId(), 400, 295, 25, 4000, 5000, 0, 0, 1, 0));
				
				startQuestTimer("stage_final_3", 4500, null, player);
			}
			else if (event.equalsIgnoreCase("stage_final_3"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_1.getObjectId(), 400, 295, 10, 4000, 11000, 0, 25, 1, 0));
				
				startQuestTimer("stage_final_4", 9000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_final_4"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_1.getObjectId(), 250, 90, 25, 0, 1000, 0, 0, 1, 0));
				
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_1.getObjectId(), 250, 90, 25, 0, 10000, 0, 0, 1, 0));
				
				startQuestTimer("stage_final_5", 2000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_final_5"))
			{
				world.priest.spawnMe();
				
				world.beleth.deleteMe();
				
				world.camera_2 = addSpawn(_camera_2_id, 14056, 213170, -9357, 0, false, 0, false, world.instanceId);
				
				startQuestTimer("stage_final_6", 3500, null, player);
			}
			else if (event.equalsIgnoreCase("stage_final_6"))
			{
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_2.getObjectId(), 800, 180, 0, 0, 4000, 0, 10, 1, 0));
				
				InstanceManager.getInstance().sendPacket(world.instanceId, new SpecialCamera(world.camera_2.getObjectId(), 800, 180, 0, 0, 4000, 0, 10, 1, 0));
				
				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_door_2).openMe();
				
				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(_door_3).openMe();
				
				startQuestTimer("stage_final_7", 4000, null, player);
			}
			else if (event.equalsIgnoreCase("stage_final_7"))
			{
				world.camera_1.deleteMe();
				
				world.camera_2.deleteMe();
				
				Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
				
				if (inst != null)
				{
					inst.setDuration(300000);
				}
			}
		}
		
		if ((npc != null) && (npc.getNpcId() == _npcEnter) && Util.isDigit(event) && (Integer.valueOf(event) == _instanceTemplateId))
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
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (_debug)
			Log.warning(getName() + ": onKill: " + npc.getName());
		
		InstanceWorld wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		
		if ((wrld != null) && (wrld instanceof belethWorld))
		{
			belethWorld world = (belethWorld) wrld;
			
			if (npc.getNpcId() == _realBelethId)
			{
				despawnAll(world);
				
				world.beleth.deleteMe();
				
				world.beleth = addSpawn(_realBelethId, 16323, 213170, -9357, 49152, false, 0, false, world.instanceId);
				
				world.beleth.setIsInvul(true);
				
				world.beleth.setIsImmobilized(true);
				
				world.beleth.disableAllSkills();
				
				world.priest = addSpawn(_priestId, 16323, 213170, -9357, 49152, false, 0, false, world.instanceId);
				
				world.priest.setShowSummonAnimation(true);
				
				world.priest.decayMe();
				
				addSpawn(_stoneId, 12470, 215607, -9381, 49152, false, 0, false, world.instanceId); //Stone
				
				startQuestTimer("stage_final_1", 1000, null, player);
			}
			else if (npc.getNpcId() == _fakeBelethId)
			{
				npc.abortCast();
				
				npc.setTarget(null);
				
				npc.deleteMe();
			}
		}
		
		return super.onKill(npc, player, isPet);
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (_debug)
			Log.warning(getName() + ": onTalk: " + player.getName());
		
		if (npc.getNpcId() == _npcEnter)
		{
			return "tryEnter.html";
		}
		
		return super.onTalk(npc, player);
	}
	
	private void spawnBeleths(belethWorld world)
	{
		int realbeleth = Rnd.get(32);
		
		L2Npc npc;
		
		for (int i = 0; i < 32; i++)
		{
			if (i == realbeleth)
			{
				npc = addSpawn(_realBelethId, _cloneX[i], _cloneY[i], -9353, _cloneH[i], false, 0, false, world.instanceId);
			}
			else
			{
				npc = addSpawn(_fakeBelethId, _cloneX[i], _cloneY[i], -9353, _cloneH[i], false, 0, false, world.instanceId);
			}
			
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
			
			world.Minions.add(npc);
		}
	}
	
	private void despawnAll(belethWorld world)
	{
		for (L2Npc npc : world.Minions)
		{
			npc.getSpawn().stopRespawn();
			
			npc.deleteMe();
		}
		
		world.Minions.clear();
	}
	
	private final synchronized void enterInstance(L2PcInstance player, int template_id)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		
		if (world != null)
		{
			if (!(world instanceof belethWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				
				return;
			}
			
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			
			if (inst != null)
			{
				Broadcast.toGameMasters(player.getName() + " trying to re enter beleth");
				if (((System.currentTimeMillis() + 300000) > inst.getInstanceEndTime()) && world.allowed.contains(player.getObjectId()))
				{
					player.setInstanceId(world.instanceId);
					
					player.teleToLocation(16323, 211588, -9359);
				}
			}
			
			return;
		}
		else
		{
			if (!_debug && !InstanceManager.getInstance().checkInstanceConditions(player, template_id, 5, 35, 75, 91))
			{
				return;
			}
			
			final int instanceId = InstanceManager.getInstance().createDynamicInstance(_qn + ".xml");
			
			world = new belethWorld();
			
			world.instanceId = instanceId;
			
			world.templateId = template_id;
			
			world.status = 0;
			
			InstanceManager.getInstance().addWorld(world);
			
			List<L2PcInstance> allPlayers = new ArrayList<L2PcInstance>();
			
			if (_debug)
			{
				allPlayers.add(player);
			}
			else
			{
				allPlayers.addAll(player.getParty().getCommandChannel().getMembers());
			}
			
			for (L2PcInstance enterPlayer : allPlayers)
			{
				if (enterPlayer == null)
				{
					continue;
				}
				
				world.allowed.add(enterPlayer.getObjectId());
				
				//InstanceManager.getInstance().setupPlayer(enterPlayer, template_id, true);
				
				enterPlayer.setInstanceId(instanceId);
				
				enterPlayer.teleToLocation(_enterCords[Rnd.get(0, _enterCords.length - 1)], true);
			}
			
			startQuestTimer("stage_1_open_door", 3000, null, player);
			
			Log.info(getName() + ": [" + template_id + "] instance started: " + instanceId + " created by player: " + player.getName());
			
			return;
		}
	}
	
	public static void main(String[] args)
	{
		new Beleth(-1, _qn, "instances/GrandBosses");
	}
}
