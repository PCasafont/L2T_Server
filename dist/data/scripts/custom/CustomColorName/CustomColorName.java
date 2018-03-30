package custom.CustomColorName;

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;

/**
 * @author LasTravel
 */

public class CustomColorName extends Quest {
	public CustomColorName(int questId, String name, String descr) {
		super(questId, name, descr);
		setOnEnterWorld(true);
	}
	
	@Override
	public final String onEnterWorld(L2PcInstance player) {
		QuestState st = player.getQuestState("CustomColorName");
		if (st == null) {
			st = newQuestState(player);
		}
		
		if (st.getGlobalQuestVar("CustomColorName").length() > 0) {
			player.getAppearance().setNameColor(Integer.decode("0x" + st.getGlobalQuestVar("CustomColorName")));
		}
		return null;
	}
	
	public static void main(String[] args) {
		new CustomColorName(-1, "CustomColorName", "custom");
	}
}
