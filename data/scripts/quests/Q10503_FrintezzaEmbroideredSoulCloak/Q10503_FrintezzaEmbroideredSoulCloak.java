package quests.Q10503_FrintezzaEmbroideredSoulCloak;

import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.util.Rnd;

public class Q10503_FrintezzaEmbroideredSoulCloak extends Quest
{
	// Quest Number
	private static final String qn = "10503_FrintezzaEmbroideredSoulCloak";

	// NPCs
	private static final int OlfAdams = 32612;
	private static final int ScarletVanHalisha = 29047; // In fact, he's Frintezza's Raid Boss

	// Items
	private static final int FrintezzaSoulFragment = 21724;
	private static final int FrintezzaSoulCloak = 21721;

	// Conditions
	private static final int RequiredItems = 20;
	private static final int MinGiven = 1;
	private static final int MaxGiven = 3;
	private static final int MinLevel = 80;

	public Q10503_FrintezzaEmbroideredSoulCloak(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(OlfAdams);
		addTalkId(OlfAdams);
		addKillId(ScarletVanHalisha);

		questItemIds = new int[]{FrintezzaSoulFragment};
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);

		if (st == null)
		{
			return htmltext;
		}

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
		{
			return htmltext;
		}

		switch (st.getState())
		{
			case State.CREATED:
			{
				if (player.getLevel() >= MinLevel)
				{
					htmltext = "32612-01.htm"; // Quest introduction
				}
				else
				{
					htmltext = "32612-Frintezza-02.htm"; // Player has not the minimum level
				}
				break;
			}
			case State.STARTED:
			{
				final long count = st.getQuestItemsCount(FrintezzaSoulFragment); // How many items player has
				if (st.getInt("cond") == 1 && st.getQuestItemsCount(FrintezzaSoulFragment) < RequiredItems)
				{
					htmltext = "32612-Frintezza-03.htm"; // Still has not the required amount
				}
				else if (count >= RequiredItems)
				{
					// Player must have all required items
					st.takeItems(FrintezzaSoulFragment, RequiredItems);
					st.giveItems(FrintezzaSoulCloak, 1);
					st.unset("cond");
					st.exitQuest(false); // Not repeatable
					st.playSound("ItemSound.quest_finish");
					htmltext = "32612-Frintezza-04.htm"; // End Quest
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
			{
				giveFragments(partyMember);
			}
		}
		else
		{
			giveFragments(player);
		}

		return null;
	}

	private void giveFragments(L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);

		if (st != null && st.getState() == State.STARTED)
		{
			st.giveItems(FrintezzaSoulCloak, Rnd.get(MinGiven, MaxGiven));
		}
	}

	public static void main(String[] args)
	{
		new Q10503_FrintezzaEmbroideredSoulCloak(10503, qn, "FrintezzaEmbroideredSoulCloak");
	}
}
