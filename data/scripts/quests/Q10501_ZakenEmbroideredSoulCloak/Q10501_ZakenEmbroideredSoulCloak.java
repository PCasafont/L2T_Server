package quests.Q10501_ZakenEmbroideredSoulCloak;

import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.model.quest.Quest;
import l2tserver.gameserver.model.quest.QuestState;
import l2tserver.gameserver.model.quest.State;
import l2tserver.util.Rnd;

public class Q10501_ZakenEmbroideredSoulCloak extends Quest
{
	// Quest Number (QN)
	private static final String qn = "10501_ZakenEmbroideredSoulCloak";
	
	// NPCs
	private static final int OlfAdams = 32612;
	private static final int Zaken = 29181;
	
	// Items
	private static final int ZakenSoulFragment = 21722;
	private static final int ZakenSoulCloak = 21719;
	
	// Conditions
	private static final int RequiredItems = 20;
	private static final int MinGiven = 1;
	private static final int MaxGiven = 3;
	private static final int MinLevel = 78;
	
	
	public Q10501_ZakenEmbroideredSoulCloak(int questId, String name, String descr) 
	{
		super(questId, name, descr);
		addStartNpc(OlfAdams);
		addTalkId(OlfAdams);
		addKillId(Zaken);
		
		questItemIds = new int[] {ZakenSoulFragment};
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
					htmltext = "32612-Zaken-02.htm"; // Player has not the minimum level
				break;
			}
			case State.STARTED:
			{
				final long count = st.getQuestItemsCount(ZakenSoulFragment); // How many items player has
				if (st.getInt("cond") == 1 && count < RequiredItems)
					htmltext = "32612-Zaken-03.htm"; // Still has not the required amount
				else if (count >= RequiredItems)
				{
					// Player must have all required items
					st.takeItems(ZakenSoulFragment, RequiredItems);
					st.giveItems(ZakenSoulCloak, 1);
					st.unset("cond");
					st.exitQuest(false); // Not repeatable
					st.playSound("ItemSound.quest_finish");
					htmltext = "32612-Zaken-04.htm"; // End Quest
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
		
		if (st != null && st.getState() == State.STARTED)
			st.giveItems(ZakenSoulFragment, Rnd.get(MinGiven, MaxGiven));
	}
	
	public static void main(String[] args)
	{
		new Q10501_ZakenEmbroideredSoulCloak(10501, qn, "ZakenEmbroideredSoulCloak");
	}

}
