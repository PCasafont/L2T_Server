package l2server.gameserver.Nodes;

import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.TimeController;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.MagicSkillLaunched;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.SetupGauge;
import l2server.gameserver.util.Util;

import java.util.Vector;

/**
 * @author Vasper
 * @since 4/13/2017
 */
public class NodesManager {
	private int maxNodes = 4;
	public int currentNodes;
	public Vector<Node> nodes = new Vector<Node>();
	
	public boolean SpawnNewNode(Player player) {
		
		Node newNode = new Node(1999999968, NpcTable.getInstance().getTemplate(95000), 1, 1, 3);

		currentNodes++;
		//nodes.add(newNode);
		newNode.setIsMortal(false);
		newNode.setIsImmobilized(true);
		newNode.ownersId = new Vector<Integer>();
		newNode.spawnMe(player.getX(), player.getY(), player.getZ());

		return true;
	}
	
	public void tryOwnNode(Player activeChar, Npc npc) {
		if (activeChar == null) {
			return;
		}
		
		Node node = (Node) npc;

		if (node.ownersId.contains(activeChar.getObjectId())) {
			activeChar.sendMessage("You're already part of the owners !");
			return;
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
	
	class OwnNodeCastFinalizer implements Runnable {
		private Player player;
		private Npc npc;
		private Node node;
		
		OwnNodeCastFinalizer(Player player, Npc npc, Node node) {
			this.player = player;
			this.npc = npc;
			this.node = node;
		}
		
		@Override
		public void run() {
			if (player.isCastingNow()) {
				player.sendPacket(new MagicSkillLaunched(player, 11030, 1));
				player.setIsCastingNow(false);
				
				if (player.getTarget() == npc && !npc.isDead() && Util.checkIfInRange(1000, player, npc, true)) {
					String name = player.getName();
					if (player.getActingPlayer() != null) {
						name = player.getActingPlayer().getName();
					}
					Announcements.getInstance().announceToAll(name + " new owner!");
					node.AddOwner(player);
				}
			}
		}
	}
	
	public static NodesManager getInstance() {
		return NodesManager.SingletonHolder.instance;
	}
	
	private static class SingletonHolder {
		protected static final NodesManager instance = new NodesManager();
	}
}
