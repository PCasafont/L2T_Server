package instances.DimensionalDoor;

import l2server.Config;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

import java.util.HashMap;
import java.util.Map;

public class DimensionalDoor extends Quest {
	private static final String qn = "DimensionalDoor";
	private static final boolean debug = false;

	//Ids
	private static final int npcManagerId = 80200;
	private static final int shinyCoin = 37559;
	private static final Map<Integer, Integer> availableSkills = new HashMap<Integer, Integer>();
	private static final int[][] availableSkillsIds = {
			//Skill id, skill price amount
			{1372, 5}, //Expand Inventory (Fishing skill)
			{1371, 3}, //Expand Warehouse (Fishing skill)
			{19222, 10}, //Dignity of the Exalted
			{19229, 8}, //Fate of the Exalted
			{19226, 10}, //Favor of the Exalted
			{19224, 5} //Blessing of the Exalted
			//{19225, 5}	//Summon Battle Potion
	};

	public DimensionalDoor(int questId, String name, String descr) {
		super(questId, name, descr);

		addStartNpc(npcManagerId);
		addTalkId(npcManagerId);
		addFirstTalkId(npcManagerId);

		for (int[] i : availableSkillsIds) {
			availableSkills.put(i[0], i[1]);
		}
	}

	public String onTalkNpc(L2Npc npc, L2PcInstance player) {
		return null;
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player) {
		if (debug) {
			Log.warning(getName() + ": onAdvEvent: " + event);
		}

		if (event.equalsIgnoreCase("main")) {
			return qn + (Config.SERVER_NAME.contains("vasper") ? "_old" : "") + ".html";
		} else if (event.equalsIgnoreCase("learnSkills")) {
			return "learnSkills.html";
		} else if (event.startsWith("claim_")) {
			int rewardId = Integer.valueOf(event.replace("claim_", ""));
			if (availableSkills.containsKey(rewardId)) {
				int maxLevel = SkillTable.getInstance().getMaxLevel(rewardId);
				int skillLevelToLearn = getProperSkillLevel(player.getSkillLevelHash(rewardId), maxLevel);
				if (skillLevelToLearn != -1) {
					if (!player.destroyItemByItemId(qn, shinyCoin, availableSkills.get(rewardId), npc, true)) {
						return "";
					}

					L2Skill rewardSkill = SkillTable.getInstance().getInfo(rewardId, skillLevelToLearn);
					player.addSkill(rewardSkill, true);
					player.sendSkillList();

					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.LEARNED_SKILL_S1);
					sm.addSkillName(rewardSkill);
					player.sendPacket(sm);
				} else {
					player.sendMessage("You already have the skill at the max level!");
				}
			}
			return "learnSkills.html";
		}

		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player) {
		if (debug) {
			Log.warning(getName() + ": onFirstTalk: " + player.getName());
		}

		return qn + (Config.SERVER_NAME.contains("vasper") ? "_old" : "") + ".html";
	}

	public static int getNpcManagerId() {
		return npcManagerId;
	}

	public static int getDimensionalDoorRewardId() {
		return shinyCoin;
	}

	public static int getDimensionalDoorRewardRate() {
		return 1;
	}

	private int getProperSkillLevel(int currentPlayerSkillLevel, int maxSkillLevel) {
		int skillLevelToLearn = -1;
		int currentPlayerLevel = currentPlayerSkillLevel;
		if (currentPlayerLevel == -1) {
			skillLevelToLearn = 1;
		} else {
			if (currentPlayerLevel < maxSkillLevel) {
				skillLevelToLearn = currentPlayerLevel + 1;
			}
		}
		return skillLevelToLearn;
	}

	public static void main(String[] args) {
		new DimensionalDoor(-1, qn, "instances");
	}
}
