package l2server.gameserver.Nodes;

import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.TimeController;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.MagicSkillLaunched;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.SetupGauge;
import l2server.gameserver.util.Util;

import java.util.Vector;

/**
 * @author Vasper
 * @since 4/13/2017
 */
public class NodesManager
{
	private int maxNodes = 4;
	public int currentNodes;
	public Vector<L2Node> nodes = new Vector<L2Node>();
	
	public boolean SpawnNewNode(L2PcInstance player)
	{
		
		L2Node newNode = new L2Node(1999999968, NpcTable.getInstance().getTemplate(95000),1,1, 3);

		currentNodes++;
		//nodes.add(newNode);
		newNode.setIsMortal(false);
		newNode.setIsImmobilized(true);
		newNode.ownersId = new Vector<Integer>();
		newNode.spawnMe(player.getX(), player.getY(), player.getZ());

		return true;
	}
	
	public void tryOwnNode(L2PcInstance activeChar, L2Npc npc)
	{
		if (activeChar == null)
		{
			return;
		}
		
		
		L2Node node = (L2Node) npc;

		if (node.ownersId.contains(activeChar.getObjectId()))
		{
			activeChar.sendMessage("You're already part of the owners !");
			return ;
		}
		
		activeChar.stopMove(null, false);
		int castingMillis = 5000; //(int)node.GetOwnTime();
		activeChar.broadcastPacket(new MagicSkillUse(activeChar, 11030, 1, castingMillis, 0));
		activeChar.sendPacket(new SetupGauge(0, castingMillis));
		activeChar.sendMessage("Remaining time : " + castingMillis / 1000 + " secs.");
		activeChar.setLastSkillCast(SkillTable.getInstance().getInfo(11030, 1));
		OwnNodeCastFinalizer fcf = new OwnNodeCastFinalizer(activeChar, npc, node);
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleEffect(fcf, castingMillis));
		activeChar.forceIsCasting(TimeController.getGameTicks() + castingMillis / TimeController.MILLIS_IN_TICK);
	}
	
	class OwnNodeCastFinalizer implements Runnable
	{
		private L2PcInstance _player;
		private L2Npc _npc;
		private L2Node _node;
		
		OwnNodeCastFinalizer(L2PcInstance player, L2Npc npc, L2Node node)
		{
			_player = player;
			_npc = npc;
			_node = node;
		}
		
		@Override
		public void run()
		{
			if (_player.isCastingNow())
			{
				_player.sendPacket(new MagicSkillLaunched(_player, 11030, 1));
				_player.setIsCastingNow(false);
				
				if (_player.getTarget() == _npc && !_npc.isDead() &&
						Util.checkIfInRange(1000, _player, _npc, true))
				{
					String name = _player.getName();
					if (_player.getActingPlayer() != null)
					{
						name = _player.getActingPlayer().getName();
					}
					Announcements.getInstance().announceToAll(name + " new owner!");
					_node.AddOwner(_player);
				}
			}
		}
	}
	
	
	public static NodesManager getInstance()
	{
		return NodesManager.SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final NodesManager _instance = new NodesManager();
	}
}
