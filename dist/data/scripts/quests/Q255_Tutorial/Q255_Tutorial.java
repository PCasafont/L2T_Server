package quests.Q255_Tutorial;

import l2server.Config;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;

public class Q255_Tutorial extends Quest
{
	private static final String qn = "Q255_Tutorial";

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return "";
		}
		QuestState st = player.getQuestState(qn);
		String htmltext = "";

		if (event.startsWith("UC")) // User Connected
		{
			int playerLevel = player.getLevel();
			if (playerLevel < 6 && st.getInt("done") == 0)
			{
				st.onTutorialClientEvent(0);
				st.showQuestionMark(100);
				st.playSound("ItemSound.quest_tutorial");
			}
		}
		else if (event.startsWith("QT")) // Quest timer
		{
			int Ex = st.getInt("Ex");
			if (Ex == -2)
			{
			}
		}
		else if (event.startsWith("TE")) // Tutorial Event
		{
			int event_id = Integer.valueOf(event.substring(2));
			switch (event_id)
			{
				case 0:
				case 1:
					st.closeTutorialHtml();
					st.set("done", "1");
					break;
				case 2:
					st.playTutorialVoice("tutorial_voice_003");
					htmltext = "tutorial_02.htm";
					st.onTutorialClientEvent(1);
					break;
				case 3:
					htmltext = "tutorial_03.htm";
					st.onTutorialClientEvent(2);
					break;
				case 5:
					htmltext = "tutorial_05.htm";
					st.onTutorialClientEvent(8);
					break;
				case 7:
					htmltext = "tutorial_100.htm";
					st.onTutorialClientEvent(0);
					break;
				case 8:
					htmltext = "tutorial_101.htm";
					st.onTutorialClientEvent(0);
					break;
				case 10:
					htmltext = "tutorial_103.htm";
					st.onTutorialClientEvent(0);
					break;
				case 12:
					st.closeTutorialHtml();
					st.set("done", "1");
					break;
				case 27:
					htmltext = "tutorial_29.htm";
					break;
				case 28:
					htmltext = "tutorial_28.htm";
			}
		}
		else if (event.startsWith("CE")) // Client Event
		{
			int event_id = Integer.valueOf(event.substring(2));
			int playerLevel = player.getLevel();
			switch (event_id)
			{
				case 1:
					if (playerLevel < 6)
					{
						st.playTutorialVoice("tutorial_voice_004");
						htmltext = "tutorial_03.htm";
						st.playSound("ItemSound.quest_tutorial");
						st.onTutorialClientEvent(2);
					}
					break;
				case 2:
					if (playerLevel < 6)
					{
						st.playTutorialVoice("tutorial_voice_005");
						htmltext = "tutorial_05.htm";
						st.playSound("ItemSound.quest_tutorial");
						st.onTutorialClientEvent(8);
					}
					break;
				case 8:
					if (playerLevel < 6)
					{
						st.playTutorialVoice("tutorial_voice_007");
						htmltext = "tutorial_08.htm";
						st.playSound("ItemSound.quest_tutorial");
					}
					break;
				case 30:
					if (playerLevel < 10 && st.getInt("Die") == 0)
					{
						st.playTutorialVoice("tutorial_voice_016");
						st.playSound("ItemSound.quest_tutorial");
						st.set("Die", "1");
						st.showQuestionMark(8);
						st.onTutorialClientEvent(0);
					}
					break;
				case 800000:
					if (playerLevel < 6 && st.getInt("sit") == 0)
					{
						st.playTutorialVoice("tutorial_voice_018");
						st.playSound("ItemSound.quest_tutorial");
						st.set("sit", "1");
						st.onTutorialClientEvent(0);
						htmltext = "tutorial_21z.htm";
					}
					break;
			}
		}
		else if (event.startsWith("QM")) // Question Mark clicked
		{
			int markId = Integer.valueOf(event.substring(2));
			switch (markId)
			{
				case 100:
					st.playTutorialVoice("tutorial_voice_002");
					st.set("Ex", "-5");
					htmltext = "tutorial_01.htm";
					break;
			}
		}
		if (htmltext == "")
		{
			return "";
		}
		st.showTutorialHTML(htmltext);
		return "";
	}

	public Q255_Tutorial(int questId, String name, String descr)
	{
		super(questId, name, descr);
	}

	public static void main(String[] args)
	{
		new Q255_Tutorial(255, qn, "Tutorial");
	}
}
