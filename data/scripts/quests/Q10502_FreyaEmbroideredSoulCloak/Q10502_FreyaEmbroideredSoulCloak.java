
package quests.Q10502_FreyaEmbroideredSoulCloak;

import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.util.Rnd;

public class Q10502_FreyaEmbroideredSoulCloak extends Quest
{
	// Quest Number
	private static final String qn = "10502_FreyaEmbroideredSoulCloak";
	
	// NPCs
	private static final int OlfAdams = 32612;
	private static final int Freya = 29179;
	private static final int FreyaXtreme = 29180;
	
	// Items
	private static final int FreyaSoulFragment = 21723;
	private static final int FreyaSoulCloak = 21720;
	
	// Conditions
	private static final int RequiredItems = 20;
	private static final int MinGiven = 1;
	private static final int MaxGiven = 3;
	private static final int MinLevel = 82;
	
	public Q10502_FreyaEmbroideredSoulCloak(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(OlfAdams);
		addTalkId(OlfAdams);
		addKillId(Freya);
		addKillId(FreyaXtreme);
		
		questItemIds = new int[] { FreyaSoulFragment };
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32612-02.htm")) // Start quest
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				if (player.getLevel() >= MinLevel)
					htmltext = "32612-01.htm"; // Quest introduction
				else
					htmltext = "32612-Freya-02.htm"; // Player has not the minimum level
				break;
			}
			case State.STARTED:
			{
				final long count = st.getQuestItemsCount(FreyaSoulFragment); // How many items player has
				if ((st.getInt("cond") == 1) && (st.getQuestItemsCount(FreyaSoulFragment) < RequiredItems))
					htmltext = "32612-Freya-03.htm"; // Still has not the required amount
				else if (count >= RequiredItems)
				{
					// Player must have all required items
					st.takeItems(FreyaSoulFragment, RequiredItems);
					st.giveItems(FreyaSoulCloak, 1);
					st.unset("cond");
					st.exitQuest(false); // Not repeatable
					st.playSound("ItemSound.quest_finish");
					htmltext = "32612-Freya-04.htm"; // End Quest
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player); // Already completed
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (player.getParty() != null)
		{
			for (L2PcInstance partyMember : player.getParty().getPartyMembers())
				giveFragments(partyMember);
		}
		else
			giveFragments(player);
		
		return null;
	}
	
	private void giveFragments(L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);
		
		if ((st != null) && (st.getState() == State.STARTED))
			st.giveItems(FreyaSoulFragment, Rnd.get(MinGiven, MaxGiven));
	}
	
	public static void main(String[] args)
	{
		new Q10502_FreyaEmbroideredSoulCloak(10502, qn, "FreyaEmbroideredSoulCloak");
	}
	
}
