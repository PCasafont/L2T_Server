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

package quests.Q10326_RespectYourElders;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.L2NpcWalkerAI;
import l2server.gameserver.model.L2NpcWalkerNode;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.GlobalQuest;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.NpcSay;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class Q10326_RespectYourElders extends Quest {
	// Quest
	public static String qn = "Q10326_RespectYourElders";
	
	// NPC
	private int gallint = 32980;
	private int pantheon = 32972;
	
	private int guideId = 32971;
	private List<L2NpcWalkerNode> guideRoute = new ArrayList<L2NpcWalkerNode>();
	private int guideFirstChatId = 1032307;
	private int guideWaitChatId = 1032308;
	private int guideLastChatId = 1032309;
	
	public Q10326_RespectYourElders(int questId, String name, String descr) {
		super(questId, name, descr);
		addStartNpc(gallint);
		addTalkId(gallint);
		addTalkId(pantheon);
		
		addEventId(guideId, QuestEventType.ON_ARRIVED);
		addEventId(guideId, QuestEventType.ON_PLAYER_ARRIVED);
		
		guideRoute.add(new L2NpcWalkerNode(-116572, 255510, -1424, 0, "", true));
		guideRoute.add(new L2NpcWalkerNode(-116567, 255628, -1432, 0, "", true));
		guideRoute.add(new L2NpcWalkerNode(-116545, 256251, -1456, 0, "", true));
		guideRoute.add(new L2NpcWalkerNode(-116478, 257207, -1512, 0, "", true));
		guideRoute.add(new L2NpcWalkerNode(-116424, 257578, -1512, 0, "", true));
		guideRoute.add(new L2NpcWalkerNode(-116380, 257815, -1512, 0, "", true));
		guideRoute.add(new L2NpcWalkerNode(-114955, 257765, -1136, 0, "", true));
		guideRoute.add(new L2NpcWalkerNode(-114420, 257222, -1136, 0, "", true));
		guideRoute.add(new L2NpcWalkerNode(-114330, 257299, -1136, 0, "", true));
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, final L2PcInstance player) {
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null) {
			return htmltext;
		}
		
		if (npc.getNpcId() == gallint && event.equalsIgnoreCase("32980-03.htm")) {
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
			
			final L2Npc guide = addSpawn(guideId, -116572, 255510, -1424, 0, false, 600000);
			L2NpcWalkerAI guideAI = new L2NpcWalkerAI(guide);
			guide.setAI(guideAI);
			guideAI.initializeRoute(guideRoute, player);
			guideAI.setWaiting(true);
			
			NpcSay ns = new NpcSay(guide.getObjectId(), Say2.ALL_NOT_RECORDED, guide.getNpcId(), guideFirstChatId);
			ns.addStringParameter(player.getName());
			guide.broadcastPacket(ns);
			
			// Delete in 1 min
			ThreadPoolManager.getInstance().scheduleAi(new Runnable() {
				@Override
				public void run() {
					if (guide.isDecayed()) {
						return;
					}
					if (!player.isInsideRadius(guide, 3000, false, false)) {
						guide.deleteMe();
					} else {
						ThreadPoolManager.getInstance().scheduleAi(this, 60000);
					}
				}
			}, 60000);
		} else if (npc.getNpcId() == pantheon && event.equalsIgnoreCase("32972-02.htm") && st.getInt("cond") == 1) {
			st.unset("cond");
			st.giveItems(57, 14000);
			st.addExpAndSp(5300, 2800);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
			
			// Main quests state
			player.setGlobalQuestFlag(GlobalQuest.STARTING, 7);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player) {
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null) {
			return htmltext;
		}
		
		if (npc.getNpcId() == gallint) {
			switch (st.getState()) {
				case State.CREATED:
					if (canStart(player)) {
						htmltext = "32980-01.htm";
					} else {
						htmltext = "32980-00.htm"; // TODO
					}
					break;
				case State.STARTED:
					htmltext = "32980-04.htm"; // TODO
					break;
				case State.COMPLETED:
					htmltext = "32980-05.htm"; // TODO
					break;
			}
		} else if (npc.getNpcId() == pantheon && st.getInt("cond") == 1) {
			htmltext = "32972-01.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onArrived(final L2NpcWalkerAI guideAI) {
		if (!guideAI.getActor().isInsideRadius(guideAI.getGuided(), guideAI.getWaitRadius() + 50, false, false) ||
				guideAI.getCurrentPos() == guideRoute.size() - 1) {
			if (guideAI.getCurrentPos() == 1) {
				guideAI.setWaiting(true);
				return null;
			}
			int chatId = guideLastChatId;
			if (guideAI.getCurrentPos() != guideRoute.size() - 1) {
				guideAI.walkToGuided(40);
				chatId = guideWaitChatId;
			}
			NpcSay ns = new NpcSay(guideAI.getActor().getObjectId(), Say2.ALL_NOT_RECORDED, guideAI.getActor().getNpcId(), chatId);
			ns.addStringParameter(guideAI.getGuided().getName());
			guideAI.getActor().broadcastPacket(ns);
			guideAI.setWaiting(true);
			return null;
		}
		
		guideAI.walkToLocation();
		guideAI.setWaiting(false);
		return null;
	}
	
	@Override
	public String onPlayerArrived(final L2NpcWalkerAI guideAI) {
		if (guideAI.getCurrentPos() == guideRoute.size() - 1) {
			// Delete in 5 sec
			ThreadPoolManager.getInstance().scheduleAi(new Runnable() {
				@Override
				public void run() {
					if (!guideAI.getActor().isDecayed()) {
						guideAI.getActor().deleteMe();
					}
				}
			}, 5000);
			return null;
		}
		guideAI.walkToLocation();
		guideAI.setWaiting(false);
		return null;
	}
	
	@Override
	public boolean canStart(L2PcInstance player) {
		return player.getGlobalQuestFlag(GlobalQuest.STARTING, 6) && player.getLevel() <= 20;
	}
	
	public static void main(String[] args) {
		new Q10326_RespectYourElders(10326, qn, "Moving from Admin Office to the Museum.");
	}
}
