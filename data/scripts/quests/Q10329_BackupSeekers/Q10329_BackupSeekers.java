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

package quests.Q10329_BackupSeekers;

import java.util.ArrayList;
import java.util.List;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.L2NpcWalkerAI;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2NpcWalkerNode;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.GlobalQuest;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.NpcSay;

/**
 * @author Pere
 */
public class Q10329_BackupSeekers extends Quest
{
	// Quest
	public static String qn = "Q10329_BackupSeekers";

	// NPC
	private int kakai = 30565;
	private int atran = 33448;
	private int apprentice = 33124;

	private int guideId = 33204;
	private List<L2NpcWalkerNode> guideRoute1 = new ArrayList<L2NpcWalkerNode>();
	private List<L2NpcWalkerNode> guideRoute2 = new ArrayList<L2NpcWalkerNode>();
	private int guideFirstChatId = 1811264;
	private int guideWaitChatId = 1811265;
	private int guideTalkId1 = 1811266;
	private int guideTalkId2 = 1811266;
	private int guideLastChatId1 = 1811268;
	private int guideTalkId3 = 1811269;
	private int guideTalkId4 = 1811270;
	private int guideTalkId5 = 1811271;
	private int guideTalkId6 = 1811272;
	private int guideLastChatId2 = 1811273;

	public Q10329_BackupSeekers(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(kakai);
		addTalkId(kakai);
		addTalkId(atran);

		addEventId(guideId, QuestEventType.ON_ARRIVED);
		addEventId(guideId, QuestEventType.ON_PLAYER_ARRIVED);

		guideRoute1.add(new L2NpcWalkerNode(-117996, 255845, -1320, 0, "", true));
		guideRoute1.add(new L2NpcWalkerNode(-117753, 255781, -1304, 0, "", true));
		guideRoute1.add(new L2NpcWalkerNode(-117602, 255742, -1296, 0, "", true));
		guideRoute1.add(new L2NpcWalkerNode(-116774, 255396, -1416, 0, "", true));
		guideRoute1.add(new L2NpcWalkerNode(-115702, 254779, -1512, 0, "", true));
		guideRoute1.add(new L2NpcWalkerNode(-114683, 254738, -1528, 0, "", true));
		guideRoute1.add(new L2NpcWalkerNode(-114567, 253462, -1528, 0, "", true));
		guideRoute1.add(new L2NpcWalkerNode(-114367, 252764, -1544, 0, "", true));

		guideRoute2.add(new L2NpcWalkerNode(-114074, 252514, -1560, 0, "", true));
		guideRoute2.add(new L2NpcWalkerNode(-114280, 252371, -1560, 0, "", true));
		guideRoute2.add(new L2NpcWalkerNode(-114395, 250846, -1760, 0, "", true));
		guideRoute2.add(new L2NpcWalkerNode(-113156, 250431, -1912, 0, "", true));
		guideRoute2.add(new L2NpcWalkerNode(-112726, 250467, -1984, 0, "", true));
		guideRoute2.add(new L2NpcWalkerNode(-112442, 250381, -2032, 0, "", true));
		guideRoute2.add(new L2NpcWalkerNode(-112063, 249669, -2248, 0, "", true));
		guideRoute2.add(new L2NpcWalkerNode(-110915, 249131, -2512, 0, "", true));
		guideRoute2.add(new L2NpcWalkerNode(-110040, 248279, -2704, 0, "", true));
		guideRoute2.add(new L2NpcWalkerNode(-109876, 248027, -2752, 0, "", true));
		guideRoute2.add(new L2NpcWalkerNode(-109713, 246992, -2960, 0, "", true));
		guideRoute2.add(new L2NpcWalkerNode(-109216, 247002, -3080, 0, "", true));
		guideRoute2.add(new L2NpcWalkerNode(-107937, 248639, -3224, 0, "", true));
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, final L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);

		if (st == null)
		{
			return htmltext;
		}

		if (npc.getNpcId() == kakai && event.equalsIgnoreCase("30565-03.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");

			final L2Npc guide = addSpawn(guideId, -117996, 255845, -1320, 0, false, 600000);
			L2NpcWalkerAI guideAI = new L2NpcWalkerAI(guide.new AIAccessor());
			guide.setAI(guideAI);
			guideAI.initializeRoute(guideRoute1, player);
			guideAI.setWaiting(true);

			NpcSay ns = new NpcSay(guide.getObjectId(), Say2.ALL_NOT_RECORDED, guide.getNpcId(), guideFirstChatId);
			ns.addStringParameter(player.getName());
			guide.broadcastPacket(ns);

			// Say another thing after 7.5s
			ThreadPoolManager.getInstance().scheduleAi(new Runnable()
			{
				@Override
				public void run()
				{
					if (guide.isDecayed())
					{
						return;
					}

					guide.broadcastPacket(
							new NpcSay(guide.getObjectId(), Say2.ALL_NOT_RECORDED, guide.getNpcId(), guideTalkId1));
				}
			}, 7500);

			// And another thing after 15s
			ThreadPoolManager.getInstance().scheduleAi(new Runnable()
			{
				@Override
				public void run()
				{
					if (guide.isDecayed())
					{
						return;
					}

					guide.broadcastPacket(
							new NpcSay(guide.getObjectId(), Say2.ALL_NOT_RECORDED, guide.getNpcId(), guideTalkId2));
				}
			}, 15000);

			// Delete in 1 min
			ThreadPoolManager.getInstance().scheduleAi(new Runnable()
			{
				@Override
				public void run()
				{
					if (guide.isDecayed())
					{
						return;
					}
					if (!player.isInsideRadius(guide, 3000, false, false))
					{
						guide.deleteMe();
					}
					else
					{
						ThreadPoolManager.getInstance().scheduleAi(this, 60000);
					}
				}
			}, 60000);
		}
		else if (npc.getNpcId() == atran && event.equalsIgnoreCase("33448-02.htm") && st.getInt("cond") == 1)
		{
			st.unset("cond");
			st.giveItems(906, 1);
			st.giveItems(875, 2);
			st.giveItems(57, 25000);
			st.addExpAndSp(16900, 5000);
			st.playSound("ItemSound.quest_finish");
			player.sendPacket(new ExShowScreenMessage(11022201, 1, false, 10000));
			st.exitQuest(false);

			// Main quests state
			player.setGlobalQuestFlag(GlobalQuest.STARTING, 10);
		}
		else if (npc.getNpcId() == apprentice && event.equalsIgnoreCase("MountKookaru"))
		{
			player.doSimultaneousCast(SkillTable.getInstance().getInfo(9204, 1));
			st.set("secondRoute", "1");

			final L2Npc guide = addSpawn(guideId, -114074, 252514, -1560, 0, false, 600000);
			L2NpcWalkerAI guideAI = new L2NpcWalkerAI(guide.new AIAccessor());
			guide.setAI(guideAI);
			guideAI.initializeRoute(guideRoute2, player);
			guideAI.setWaiting(true);

			NpcSay ns = new NpcSay(guide.getObjectId(), Say2.ALL_NOT_RECORDED, guide.getNpcId(), guideFirstChatId);
			ns.addStringParameter(player.getName());
			guide.broadcastPacket(ns);

			// Say another thing after 7.5s
			ThreadPoolManager.getInstance().scheduleAi(new Runnable()
			{
				@Override
				public void run()
				{
					if (guide.isDecayed())
					{
						return;
					}

					guide.broadcastPacket(
							new NpcSay(guide.getObjectId(), Say2.ALL_NOT_RECORDED, guide.getNpcId(), guideTalkId3));
				}
			}, 7500);

			// And another thing after 15s
			ThreadPoolManager.getInstance().scheduleAi(new Runnable()
			{
				@Override
				public void run()
				{
					if (guide.isDecayed())
					{
						return;
					}

					guide.broadcastPacket(
							new NpcSay(guide.getObjectId(), Say2.ALL_NOT_RECORDED, guide.getNpcId(), guideTalkId4));
				}
			}, 15000);

			// Say another thing after 23.5s
			ThreadPoolManager.getInstance().scheduleAi(new Runnable()
			{
				@Override
				public void run()
				{
					if (guide.isDecayed())
					{
						return;
					}

					guide.broadcastPacket(
							new NpcSay(guide.getObjectId(), Say2.ALL_NOT_RECORDED, guide.getNpcId(), guideTalkId5));
				}
			}, 23500);

			// And another thing after 30s
			ThreadPoolManager.getInstance().scheduleAi(new Runnable()
			{
				@Override
				public void run()
				{
					if (guide.isDecayed())
					{
						return;
					}

					guide.broadcastPacket(
							new NpcSay(guide.getObjectId(), Say2.ALL_NOT_RECORDED, guide.getNpcId(), guideTalkId6));
				}
			}, 30000);

			// Delete in 1 min
			ThreadPoolManager.getInstance().scheduleAi(new Runnable()
			{
				@Override
				public void run()
				{
					if (guide.isDecayed())
					{
						return;
					}
					if (!player.isInsideRadius(guide, 5000, false, false))
					{
						guide.deleteMe();
					}
					else
					{
						ThreadPoolManager.getInstance().scheduleAi(this, 60000);
					}
				}
			}, 60000);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}

		if (npc.getNpcId() == kakai)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (canStart(player))
					{
						htmltext = "30565-01.htm";
					}
					else
					{
						htmltext = "30565-00.htm"; // TODO
					}
					break;
				case State.STARTED:
					htmltext = "30565-04.htm"; // TODO
					break;
				case State.COMPLETED:
					htmltext = "30565-05.htm"; // TODO
					break;
			}
		}
		else if (npc.getNpcId() == atran && st.getInt("cond") == 1)
		{
			htmltext = "33448-01.htm";
		}
		return htmltext;
	}

	@Override
	public String onArrived(final L2NpcWalkerAI guideAI)
	{
		List<L2NpcWalkerNode> guideRoute = guideRoute1;
		int guideLastChatId = guideLastChatId1;

		if (guideAI.getGuided().getQuestState(qn).getInt("secondRoute") == 1)
		{
			guideRoute = guideRoute2;
			guideLastChatId = guideLastChatId2;
		}

		if (!guideAI.getActor().isInsideRadius(guideAI.getGuided(), guideAI.getWaitRadius() + 50, false, false) ||
				guideAI.getCurrentPos() == guideRoute.size() - 1)
		{
			if (guideAI.getCurrentPos() == 1)
			{
				guideAI.setWaiting(true);
				return null;
			}
			int chatId = guideLastChatId;
			if (guideAI.getCurrentPos() != guideRoute.size() - 1)
			{
				guideAI.walkToGuided(40);
				chatId = guideWaitChatId;
			}
			NpcSay ns =
					new NpcSay(guideAI.getActor().getObjectId(), Say2.ALL_NOT_RECORDED, guideAI.getActor().getNpcId(),
							chatId);
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
	public String onPlayerArrived(final L2NpcWalkerAI guideAI)
	{
		List<L2NpcWalkerNode> guideRoute = guideRoute1;

		if (guideAI.getGuided() == null || guideAI.getGuided().getQuestState(qn) == null)
		{
			return null;
		}

		if (guideAI.getGuided().getQuestState(qn).getInt("secondRoute") == 1)
		{
			guideRoute = guideRoute2;
		}

		if (guideAI.getCurrentPos() == guideRoute.size() - 1)
		{
			// Delete in 5 sec
			ThreadPoolManager.getInstance().scheduleAi(new Runnable()
			{
				@Override
				public void run()
				{
					if (!guideAI.getActor().isDecayed())
					{
						guideAI.getActor().deleteMe();
					}
				}
			}, 2000);
			return null;
		}
		guideAI.walkToLocation();
		guideAI.setWaiting(false);
		return null;
	}

	@Override
	public boolean canStart(L2PcInstance player)
	{
		return player.getGlobalQuestFlag(GlobalQuest.STARTING, 9) && player.getLevel() <= 20;
	}

	public static void main(String[] args)
	{
		new Q10329_BackupSeekers(10329, qn, "Going outside the village. Opportunity to obtain an accessory.");
	}
}
