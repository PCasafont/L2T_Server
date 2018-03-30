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

package quests.Q10322_SearchingForTheMysteriousPower;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.L2NpcWalkerAI;
import l2server.gameserver.datatables.HelperBuffTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2NpcWalkerNode;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.GlobalQuest;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.network.serverpackets.TutorialShowHtml;
import l2server.gameserver.templates.L2HelperBuff;
import l2server.gameserver.templates.skills.L2SkillType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class Q10322_SearchingForTheMysteriousPower extends Quest {
	// Quest
	public static String qn = "Q10322_SearchingForTheMysteriousPower";

	// NPC
	private int shannon = 32974;
	private int yibein = 33464;
	private int newbieHelper = 32981;
	private int scarecrow = 27457;

	private int guideId = 33016;
	private List<L2NpcWalkerNode> guideRoute = new ArrayList<L2NpcWalkerNode>();
	private int guideFirstChatId = 1032301;
	private int guideWaitChatId = 1032302;
	private int guideLastChatId = 1032303;

	public Q10322_SearchingForTheMysteriousPower(int questId, String name, String descr) {
		super(questId, name, descr);
		addStartNpc(shannon);
		addTalkId(shannon);
		addTalkId(yibein);
		addTalkId(newbieHelper);
		addKillId(scarecrow);

		addEventId(guideId, QuestEventType.ON_ARRIVED);
		addEventId(guideId, QuestEventType.ON_PLAYER_ARRIVED);

		guideRoute.add(new L2NpcWalkerNode(-111487, 255894, -1440, 0, "", true));
		guideRoute.add(new L2NpcWalkerNode(-111610, 255776, -1440, 0, "", true));
		guideRoute.add(new L2NpcWalkerNode(-111972, 255433, -1440, 0, "", true));
		guideRoute.add(new L2NpcWalkerNode(-112565, 254658, -1528, 0, "", true));
		guideRoute.add(new L2NpcWalkerNode(-112304, 254244, -1536, 0, "", true));
		guideRoute.add(new L2NpcWalkerNode(-111704, 254022, -1672, 0, "", true));
		guideRoute.add(new L2NpcWalkerNode(-110981, 253740, -1760, 0, "", true));
		guideRoute.add(new L2NpcWalkerNode(-111319, 253871, -1736, 0, "", true));
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, final L2PcInstance player) {
		String htmltext = event;
		QuestState st = player.getQuestState(qn);

		if (st == null) {
			return htmltext;
		}

		if (npc.getNpcId() == shannon && event.equalsIgnoreCase("32974-03.htm")) {
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");

			final L2Npc guide = addSpawn(guideId, -111487, 255894, -1440, 1000, false, 600000);
			final L2NpcWalkerAI guideAI = new L2NpcWalkerAI(guide);
			guide.setAI(guideAI);

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
					if (!player.isInsideRadius(guide, 2000, false, false)) {
						guide.deleteMe();
					} else {
						ThreadPoolManager.getInstance().scheduleAi(this, 60000);
					}
				}
			}, 60000);

			// Walk in 2 sec
			ThreadPoolManager.getInstance().scheduleAi(new Runnable() {
				@Override
				public void run() {
					guideAI.initializeRoute(guideRoute, player);
					guideAI.walkToLocation();
				}
			}, 2000);
		} else if (npc.getNpcId() == newbieHelper) {
			if (event.equalsIgnoreCase("32981-02.htm")) {
				npc.setTarget(player);
				for (L2HelperBuff helperBuff : HelperBuffTable.getInstance().getHelperBuffTable()) {
					if (helperBuff.isMagicClassBuff() == player.isMageClass() && helperBuff.getLowerLevel() < 10) {
						L2Skill skill = SkillTable.getInstance().getInfo(helperBuff.getSkillID(), helperBuff.getSkillLevel());
						if (skill.getSkillType() == L2SkillType.SUMMON) {
							player.doSimultaneousCast(skill);
						} else {
							npc.doCast(skill);
						}
					}
				}
				player.sendPacket(new TutorialShowHtml(2, "..\\L2text\\QT_002_Guide_01.htm"));
				st.set("cond", "5");
				st.playSound("ItemSound.quest_middle");
			}
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

		if (npc.getNpcId() == shannon) {
			switch (st.getState()) {
				case State.CREATED:
					if (canStart(player)) {
						htmltext = "32974-01.htm";
					} else {
						htmltext = "32974-00.htm"; // TODO
					}
					break;
				case State.STARTED:
					htmltext = "32974-04.htm"; // TODO
					break;
				case State.COMPLETED:
					htmltext = "32974-05.htm"; // TODO
					break;
			}
		} else if (npc.getNpcId() == yibein) {
			if (st.getInt("cond") == 1) {
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
				htmltext = "33464-01.htm";
			} else if (st.getInt("cond") == 2) {
				htmltext = "33464-02.htm";
			} else if (st.getInt("cond") == 3) {
				st.set("cond", "4");
				st.playSound("ItemSound.quest_middle");
				htmltext = "33464-03.htm";
			} else if (st.getInt("cond") == 6) {
				st.unset("cond");
				st.giveItems(57, 7000);
				st.addExpAndSp(300, 800);
				st.giveItems(7816, 1);
				st.giveItems(7817, 1);
				st.giveItems(7818, 1);
				st.giveItems(7819, 1);
				st.giveItems(7820, 1);
				st.giveItems(7821, 1);
				st.giveItems(17, 500);
				st.giveItems(1060, 50);
				st.playSound("ItemSound.quest_finish");
				//TODO: yibein saying the text id 32203: broadcast or just to player?
				player.sendPacket(new ExShowScreenMessage(1032201, 1, false, 10000));
				st.exitQuest(false);

				// Main quests state
				player.setGlobalQuestFlag(GlobalQuest.STARTING, 3);

				htmltext = "33464-04.htm";
			}
		} else if (npc.getNpcId() == newbieHelper) {
			if (st.getInt("cond") == 4) {
				htmltext = "32981-01.htm";
			} else if (st.getInt("cond") == 5) {
				htmltext = "32981-03.htm";
			}
		}
		return htmltext;
	}

	@Override
	public boolean canStart(L2PcInstance player) {
		return player.getGlobalQuestFlag(GlobalQuest.STARTING, 2) && player.getLevel() <= 20;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet) {
		QuestState st = player.getQuestState(qn);
		if (st == null) {
			return null;
		}

		if (st.getState() == State.STARTED && npc.getNpcId() == scarecrow) {
			if (st.getInt("cond") == 2) {
				st.set("cond", "3");
				st.playSound("ItemSound.quest_middle");
			} else if (st.getInt("cond") == 5) {
				st.set("cond", "6");
				st.playSound("ItemSound.quest_middle");
			}
		}
		return null;
	}

	@Override
	public String onArrived(final L2NpcWalkerAI guideAI) {
		if (!guideAI.getActor().isInsideRadius(guideAI.getGuided(), guideAI.getWaitRadius() + 50, false, false) ||
				guideAI.getCurrentPos() == guideRoute.size() - 2) {
			if (guideAI.getCurrentPos() == 1) {
				guideAI.setWaiting(true);
				return null;
			}
			int chatId = guideLastChatId;
			if (guideAI.getCurrentPos() != guideRoute.size() - 2) {
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
		if (guideAI.getCurrentPos() == guideRoute.size() - 2) {
			// Walk and delete in 1.5 sec
			ThreadPoolManager.getInstance().scheduleAi(new Runnable() {
				private boolean delete = false;

				@Override
				public void run() {
					if (!delete) {
						guideAI.walkToLocation();
						delete = true;
						ThreadPoolManager.getInstance().scheduleAi(this, 500);
						return;
					}
					if (!guideAI.getActor().isDecayed()) {
						guideAI.getActor().deleteMe();
					}
				}
			}, 1500);
			return null;
		}
		guideAI.walkToLocation();
		guideAI.setWaiting(false);
		return null;
	}

	@Override
	public int getOnKillDelay(int npcId) {
		if (npcId == scarecrow) {
			return 0;
		}
		return super.getOnKillDelay(npcId);
	}

	public static void main(String[] args) {
		new Q10322_SearchingForTheMysteriousPower(10322, qn, "Training after receiving newbie buffs and obtaining a newbie weapon.");
	}
}
