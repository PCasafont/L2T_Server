package quests.Q255_Tutorial;

import l2server.Config;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.base.PlayerClass;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;

public class Q255_Tutorial_Old extends Quest {
	private static final String qn = "Q255_Tutorial";

	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		if (Config.DISABLE_TUTORIAL) {
			return "";
		}
		QuestState st = player.getQuestState(qn);
		PlayerClass cl = st.getPlayer().getCurrentClass();
		String htmltext = "";

		if (event.startsWith("UC")) // User Connected
		{
			int playerLevel = player.getLevel();
			if (playerLevel < 6 && st.getInt("onlyone") == 0) {
				int uc = st.getInt("ucMemo");
				if (uc == 0) {
					st.set("ucMemo", "0");
					st.startQuestTimer("QT", 10000);
					st.set("Ex", "-2");
				} else if (uc == 1) {
					st.showQuestionMark(1);
					st.playTutorialVoice("tutorial_voice_006");
					st.playSound("ItemSound.quest_tutorial");
				} else if (uc == 2) {
					if (st.getInt("Ex") == 2) {
						st.showQuestionMark(3);
						st.playSound("ItemSound.quest_tutorial");
					}
					if (st.getQuestItemsCount(6353) == 1) {
						st.showQuestionMark(5);
						st.playSound("ItemSound.quest_tutorial");
					}
				} else if (uc == 3) {
					st.showQuestionMark(12);
					st.playSound("ItemSound.quest_tutorial");
					st.onTutorialClientEvent(0);
				} else {
					return "";
				}
			} else if (playerLevel == 18 && player.getQuestState("10276_MutatedKaneusGludio") != null) {
				st.showQuestionMark(33);
				st.playSound("ItemSound.quest_tutorial");
			} else if (playerLevel == 28 && player.getQuestState("10277_MutatedKaneusDion") != null) {
				st.showQuestionMark(33);
				st.playSound("ItemSound.quest_tutorial");
			} else if (playerLevel == 28 && player.getQuestState("10278_MutatedKaneusHeine") != null) {
				st.showQuestionMark(33);
				st.playSound("ItemSound.quest_tutorial");
			} else if (playerLevel == 28 && player.getQuestState("10279_MutatedKaneusOren") != null) {
				st.showQuestionMark(33);
				st.playSound("ItemSound.quest_tutorial");
			} else if (playerLevel == 28 && player.getQuestState("10280_MutatedKaneusSchuttgart") != null) {
				st.showQuestionMark(33);
				st.playSound("ItemSound.quest_tutorial");
			} else if (playerLevel == 28 && player.getQuestState("10281_MutatedKaneusRune") != null) {
				st.showQuestionMark(33);
				st.playSound("ItemSound.quest_tutorial");
			} else if (playerLevel == 79 && player.getQuestState("192_SevenSignSeriesOfDoubt") != null) {
				st.showQuestionMark(33);
				st.playSound("ItemSound.quest_tutorial");
			}
		} else if (event.startsWith("QT")) // Quest timer
		{
			int Ex = st.getInt("Ex");
			if (Ex == -2) {
				if (cl.getLevel() == 1) {
					htmltext = "tutorial_human_fighter001.htm";
					st.playTutorialVoice("tutorial_voice_001a");
					if (st.getQuestItemsCount(5588) == 0) {
						;
					}
					st.giveItems(5588, 1);
					st.startQuestTimer("QT", 30000);
					st.set("Ex", "-3");
				}
			} else if (Ex == -3) {
				st.playTutorialVoice("tutorial_voice_002");
				st.set("Ex", "0");
			} else if (Ex == -4) {
				st.playTutorialVoice("tutorial_voice_008");
				st.set("Ex", "-5");
			}
		} else if (event.startsWith("TE")) // Tutorial Event
		{
			int event_id = Integer.valueOf(event.substring(2));
			switch (event_id) {
				case 0:
					st.closeTutorialHtml();
					break;
				case 1:
					st.closeTutorialHtml();
					st.playTutorialVoice("tutorial_voice_006");
					st.showQuestionMark(1);
					st.playSound("ItemSound.quest_tutorial");
					st.startQuestTimer("QT", 30000);
					st.set("Ex", "-4");
				case 2:
					st.playTutorialVoice("tutorial_voice_003");
					htmltext = "tutorial_02.htm";
					st.onTutorialClientEvent(1);
					st.set("Ex", "-5");
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
					break;
				case 23:
					/*# table for Tutorial Close Link (23) 2nd class transfer [html]
                    TCLb = {
						4 ) "tutorial_22aa.htm";,
						7 ) "tutorial_22ba.htm";,
						11) "tutorial_22ca.htm";,
						15) "tutorial_22da.htm";,
						19) "tutorial_22ea.htm";,
						22) "tutorial_22fa.htm";,
						26) "tutorial_22ga.htm";,
						32) "tutorial_22na.htm";,
						35) "tutorial_22oa.htm";,
						39) "tutorial_22pa.htm";,
						50) "tutorial_22ka.htm";
						}*/
					// TODO
                    /*if (classId in TCLb.keys():
						htmltext = TCLb[classId];*/
					break;
				case 24:
					/*# table for Tutorial Close Link (24) 2nd class transfer [html]
					TCLc = {
						4 ) "tutorial_22ab.htm";,
						7 ) "tutorial_22bb.htm";,
						11) "tutorial_22cb.htm";,
						15) "tutorial_22db.htm";,
						19) "tutorial_22eb.htm";,
						22) "tutorial_22fb.htm";,
						26) "tutorial_22gb.htm";,
						32) "tutorial_22nb.htm";,
						35) "tutorial_22ob.htm";,
						39) "tutorial_22pb.htm";,
						50) "tutorial_22kb.htm";
						}*/
					// TODO
					/* if (classId in TCLc.keys():
						htmltext = TCLc[classId];*/
					break;
				case 25:
					htmltext = "tutorial_22cc.htm";
					break;
				case 26:
					/*# table for Tutorial Close Link (26) 2nd class transfer [html]
					TCLa = {
						1 ) "tutorial_22w.htm";,
						4 ) "tutorial_22.htm";,
						7 ) "tutorial_22b.htm";,
						11) "tutorial_22c.htm";,
						15) "tutorial_22d.htm";,
						19) "tutorial_22e.htm";,
						22) "tutorial_22f.htm";,
						26) "tutorial_22g.htm";,
						29) "tutorial_22h.htm";,
						32) "tutorial_22n.htm";,
						35) "tutorial_22o.htm";,
						39) "tutorial_22p.htm";,
						42) "tutorial_22q.htm";,
						45) "tutorial_22i.htm";,
						47) "tutorial_22j.htm";,
						50) "tutorial_22k.htm";,
						54) "tutorial_22l.htm";,
						56) "tutorial_22m.htm";
						}*/
					// TODO
					/*if (classId in TCLa.keys():
						htmltext = TCLa[classId];*/
					break;
				case 27:
					htmltext = "tutorial_29.htm";
					break;
				case 28:
					htmltext = "tutorial_28.htm";
			}
		} else if (event.startsWith("CE")) // Client Event
		{
			int event_id = Integer.valueOf(event.substring(2));
			int playerLevel = player.getLevel();
			switch (event_id) {
				case 1:
					if (playerLevel < 6) {
						st.playTutorialVoice("tutorial_voice_004");
						htmltext = "tutorial_03.htm";
						st.playSound("ItemSound.quest_tutorial");
						st.onTutorialClientEvent(2);
					}
					break;
				case 2:
					if (playerLevel < 6) {
						st.playTutorialVoice("tutorial_voice_005");
						htmltext = "tutorial_05.htm";
						st.playSound("ItemSound.quest_tutorial");
						st.onTutorialClientEvent(8);
					}
					break;
				case 8:
					/*# table for Client Event Enable (8) [html, x, y, z]
					CEEa = {
						0 ) ["tutorial_human_fighter007.htm";,-71424,258336,-3109],
						10) ["tutorial_human_mage007.htm";,-91036,248044,-3568],
						18) ["tutorial_elf007.htm";,46112,41200,-3504],
						25) ["tutorial_elf007.htm";,46112,41200,-3504],
						31) ["tutorial_delf007.htm";,28384,11056,-4233],
						38) ["tutorial_delf007.htm";,28384,11056,-4233],
						44) ["tutorial_orc007.htm";,-56736,-113680,-672],
						49) ["tutorial_orc007.htm";,-56736,-113680,-672],
						53) ["tutorial_dwarven_fighter007.htm";,108567,-173994,-406],
						123: ["tutorial_kamael007.htm";,-125872,38016,1251],
						124: ["tutorial_kamael007.htm";,-125872,38016,1251]
						}*/
					// TODO
					/*if (playerLevel < 6 && classId in CEEa.keys())
					{
						htmltext, x, y, z = CEEa[classId];
						st.playSound("ItemSound.quest_tutorial");
						st.addRadar(x,y,z);
						st.playTutorialVoice("tutorial_voice_007");
						st.set("ucMemo","1");
						st.set("Ex","-5");
					}*/
					break;
				case 30:
					if (playerLevel < 10 && st.getInt("Die") == 0) {
						st.playTutorialVoice("tutorial_voice_016");
						st.playSound("ItemSound.quest_tutorial");
						st.set("Die", "1");
						st.showQuestionMark(8);
						st.onTutorialClientEvent(0);
					}
					break;
				case 800000:
					if (playerLevel < 6 && st.getInt("sit") == 0) {
						st.playTutorialVoice("tutorial_voice_018");
						st.playSound("ItemSound.quest_tutorial");
						st.set("sit", "1");
						st.onTutorialClientEvent(0);
						htmltext = "tutorial_21z.htm";
					}
					break;
				case 40:
					switch (playerLevel) {
						case 5:
							if (player.getCurrentClass().getLevel() == 0 && st.getInt("lvl") < 5 &&
									(!player.getCurrentClass().isMage() || cl.getId() == 49)) {
								st.playTutorialVoice("tutorial_voice_014");
								st.showQuestionMark(9);
								st.playSound("ItemSound.quest_tutorial");
								st.set("lvl", "5");
							}
							break;
						case 6:
							if (st.getInt("lvl") < 6 && player.getCurrentClass().getLevel() == 1) {
								st.playTutorialVoice("tutorial_voice_020");
								st.playSound("ItemSound.quest_tutorial");
								st.showQuestionMark(24);
								st.set("lvl", "6");
							}
							break;
						case 7:
							if (player.getCurrentClass().isMage() && cl.getId() != 49 && st.getInt("lvl") < 7 &&
									player.getCurrentClass().getLevel() == 1) {
								st.playTutorialVoice("tutorial_voice_019");
								st.playSound("ItemSound.quest_tutorial");
								st.set("lvl", "7");
								st.showQuestionMark(11);
							}
							break;
						case 10:
							if (st.getInt("lvl") < 10) {
								st.playTutorialVoice("tutorial_voice_030");
								st.playSound("ItemSound.quest_tutorial");
								st.set("lvl", "10");
								st.showQuestionMark(27);
							}
							break;
						case 15:
							if (st.getInt("lvl") < 15) {
								st.playSound("ItemSound.quest_tutorial");
								st.set("lvl", "15");
								st.showQuestionMark(17);
							}
							break;
						case 18:
							if (st.getInt("lvl") < 18) {
								st.playSound("ItemSound.quest_tutorial");
								st.set("lvl", "18");
								st.showQuestionMark(33);
							}
							break;
						case 19:
							if (st.getInt("lvl") < 19) {
								int race = st.getPlayer().getRace().ordinal();
								if (race != 5 && player.getCurrentClass().getLevel() == 1 && cl.getLevel() == 1) {
									//st.playTutorialVoice("tutorial_voice_???");
									st.playSound("ItemSound.quest_tutorial");
									st.set("lvl", "19");
									st.showQuestionMark(35);
								}
							}
							break;
						case 28:
							if (st.getInt("lvl") < 28) {
								st.playSound("ItemSound.quest_tutorial");
								st.set("lvl", "28");
								st.showQuestionMark(33);
							}
							break;
						case 35:
							if (st.getInt("lvl") < 35) {
								int race = st.getPlayer().getRace().ordinal();
								if (race != 5 && player.getCurrentClass().getLevel() == 20 && cl.getLevel() == 20) {
									//st.playTutorialVoice("tutorial_voice_???");
									st.playSound("ItemSound.quest_tutorial");
									st.set("lvl", "35");
									st.showQuestionMark(34);
								}
							}
							break;
						case 38:
							if (st.getInt("lvl") < 38) {
								st.playSound("ItemSound.quest_tutorial");
								st.set("lvl", "38");
								st.showQuestionMark(33);
							}
							break;
						case 48:
							if (st.getInt("lvl") < 48) {
								st.playSound("ItemSound.quest_tutorial");
								st.set("lvl", "48");
								st.showQuestionMark(33);
							}
							break;
						case 58:
							if (st.getInt("lvl") < 58) {
								st.playSound("ItemSound.quest_tutorial");
								st.set("lvl", "58");
								st.showQuestionMark(33);
							}
							break;
						case 68:
							if (st.getInt("lvl") < 68) {
								st.playSound("ItemSound.quest_tutorial");
								st.set("lvl", "68");
								st.showQuestionMark(33);
							}
							break;
						case 79:
							if (st.getInt("lvl") < 79) {
								st.playSound("ItemSound.quest_tutorial");
								st.set("lvl", "79");
								st.showQuestionMark(33);
							}
							break;
					}
				case 45:
					if (playerLevel < 10 && st.getInt("HP") == 0) {
						st.playTutorialVoice("tutorial_voice_017");
						st.playSound("ItemSound.quest_tutorial");
						st.set("HP", "1");
						st.showQuestionMark(10);
						st.onTutorialClientEvent(800000);
					}
					break;
				case 57:
					if (playerLevel < 6 && st.getInt("Adena") == 0) {
						st.playTutorialVoice("tutorial_voice_012");
						st.playSound("ItemSound.quest_tutorial");
						st.set("Adena", "1");
						st.showQuestionMark(23);
					}
					break;
				case 6353:
					if (playerLevel < 6 && st.getInt("Gemstone") == 0) {
						st.playTutorialVoice("tutorial_voice_013");
						st.playSound("ItemSound.quest_tutorial");
						st.set("Gemstone", "1");
						st.showQuestionMark(5);
					}
					break;
			}
		} else if (event.startsWith("QM")) // Question Mark
		{
			int markId = Integer.valueOf(event.substring(2));
			switch (markId) {
				case 1:
					/*# table for Client Event Enable (8) [html, x, y, z]
					CEEa = {
						0 ) ["tutorial_human_fighter007.htm";,-71424,258336,-3109],
						10) ["tutorial_human_mage007.htm";,-91036,248044,-3568],
						18) ["tutorial_elf007.htm";,46112,41200,-3504],
						25) ["tutorial_elf007.htm";,46112,41200,-3504],
						31) ["tutorial_delf007.htm";,28384,11056,-4233],
						38) ["tutorial_delf007.htm";,28384,11056,-4233],
						44) ["tutorial_orc007.htm";,-56736,-113680,-672],
						49) ["tutorial_orc007.htm";,-56736,-113680,-672],
						53) ["tutorial_dwarven_fighter007.htm";,108567,-173994,-406],
						123: ["tutorial_kamael007.htm";,-125872,38016,1251],
						124: ["tutorial_kamael007.htm";,-125872,38016,1251]
						}*/
					st.playTutorialVoice("tutorial_voice_007");
					st.set("Ex", "-5");
					// TODO
					/*if (classId in CEEa.keys())
					{
						htmltext, x, y, z = CEEa[classId];
						st.addRadar(x,y,z);
					}*/
					break;
				case 3:
					htmltext = "tutorial_09.htm";
				case 5:
					/*# table for Client Event Enable (8) [html, x, y, z]
					CEEa = {
						0 ) ["tutorial_human_fighter007.htm";,-71424,258336,-3109],
						10) ["tutorial_human_mage007.htm";,-91036,248044,-3568],
						18) ["tutorial_elf007.htm";,46112,41200,-3504],
						25) ["tutorial_elf007.htm";,46112,41200,-3504],
						31) ["tutorial_delf007.htm";,28384,11056,-4233],
						38) ["tutorial_delf007.htm";,28384,11056,-4233],
						44) ["tutorial_orc007.htm";,-56736,-113680,-672],
						49) ["tutorial_orc007.htm";,-56736,-113680,-672],
						53) ["tutorial_dwarven_fighter007.htm";,108567,-173994,-406],
						123: ["tutorial_kamael007.htm";,-125872,38016,1251],
						124: ["tutorial_kamael007.htm";,-125872,38016,1251]
						}*/
					// TODO
					/*if (classId in CEEa.keys())
					{
						htmltext, x, y, z = CEEa[classId];
						st.addRadar(x,y,z);
					}*/
					htmltext = "tutorial_11.htm";
					break;
				case 7:
					htmltext = "tutorial_15.htm";
					st.set("ucMemo", "3");
					break;
				case 8:
					htmltext = "tutorial_18.htm";
					break;
				case 9:
					/*# table for Question Mark Clicked (9 & 11) learning skills [html, x, y, z]
					QMCa = {
						0 ) ["tutorial_fighter017.htm";,-83165,242711,-3720],
						10) ["tutorial_mage017.htm";,-85247,244718,-3720],
						18) ["tutorial_fighter017.htm";,45610,52206,-2792],
						25) ["tutorial_mage017.htm";,45610,52206,-2792],
						31) ["tutorial_fighter017.htm";,10344,14445,-4242],
						38) ["tutorial_mage017.htm";,10344,14445,-4242],
						44) ["tutorial_fighter017.htm";,-46324,-114384,-200],
						49) ["tutorial_fighter017.htm";,-46305,-112763,-200],
						53) ["tutorial_fighter017.htm";,115447,-182672,-1440],
						123: ["tutorial_fighter017.htm";,-118132,42788,723],
						124: ["tutorial_fighter017.htm";,-118132,42788,723]
						}*/
					// TODO
					/*if (classId in QMCa.keys())
					{
						htmltext, x, y, z = QMCa[classId];
						st.addRadar(x,y,z);
					}*/
					break;
				case 10:
					htmltext = "tutorial_19.htm";
					break;
				case 11:
					/*# table for Question Mark Clicked (9 & 11) learning skills [html, x, y, z]
					QMCa = {
						0 ) ["tutorial_fighter017.htm";,-83165,242711,-3720],
						10) ["tutorial_mage017.htm";,-85247,244718,-3720],
						18) ["tutorial_fighter017.htm";,45610,52206,-2792],
						25) ["tutorial_mage017.htm";,45610,52206,-2792],
						31) ["tutorial_fighter017.htm";,10344,14445,-4242],
						38) ["tutorial_mage017.htm";,10344,14445,-4242],
						44) ["tutorial_fighter017.htm";,-46324,-114384,-200],
						49) ["tutorial_fighter017.htm";,-46305,-112763,-200],
						53) ["tutorial_fighter017.htm";,115447,-182672,-1440],
						123: ["tutorial_fighter017.htm";,-118132,42788,723],
						124: ["tutorial_fighter017.htm";,-118132,42788,723]
						}*/
					// TODO
					/*if (classId in QMCa.keys())
					{
						htmltext, x, y, z = QMCa[classId];
						st.addRadar(x,y,z);
					}*/
					break;
				case 12:
					htmltext = "tutorial_15.htm";
					st.set("ucMemo", "4");
					break;
				case 13:
					htmltext = "tutorial_30.htm";
					break;
				case 17:
					htmltext = "tutorial_27.htm";
					break;
				case 23:
					htmltext = "tutorial_24.htm";
					break;
				case 24:
					/*# table for Question Mark Clicked (24) newbie lvl [html]
					QMCb = {
						0 ) "tutorial_human009.htm";,
						10) "tutorial_human009.htm";,
						18) "tutorial_elf009.htm";,
						25) "tutorial_elf009.htm";,
						31) "tutorial_delf009.htm";,
						38) "tutorial_delf009.htm";,
						44) "tutorial_orc009.htm";,
						49) "tutorial_orc009.htm";,
						53) "tutorial_dwarven009.htm";,
						123: "tutorial_kamael009.htm";,
						124: "tutorial_kamael009.htm";
						}*/
					// TODO
					/*if (classId in QMCb.keys())
						htmltext = QMCb[classId];*/
					break;
				case 26:
					if (st.getPlayer().getCurrentClass().isMage() && cl.getId() != 49) {
						htmltext = "tutorial_newbie004b.htm";
					} else {
						htmltext = "tutorial_newbie004a.htm";
					}
					break;
				case 27:
					htmltext = "tutorial_20.htm";
					break;
				case 33:
					switch (player.getLevel()) {
						case 18:
							htmltext = "tutorial_kama_18.htm";
							break;
						case 28:
							htmltext = "tutorial_kama_28.htm";
							break;
						case 38:
							htmltext = "tutorial_kama_38.htm";
							break;
						case 48:
							htmltext = "tutorial_kama_48.htm";
							break;
						case 58:
							htmltext = "tutorial_kama_58.htm";
							break;
						case 68:
							htmltext = "tutorial_kama_68.htm";
							break;
						case 79:
							htmltext = "tutorial_epic_quest.htm";
					}
					break;
				case 34:
					htmltext = "tutorial_28.htm";
					break;
				case 35:
					/*# table for Question Mark Clicked (35) 1st class transfer [html]
					QMCc = {
						0 ) "tutorial_21.htm";,
						10) "tutorial_21a.htm";,
						18) "tutorial_21b.htm";,
						25) "tutorial_21c.htm";,
						31) "tutorial_21g.htm";,
						38) "tutorial_21h.htm";,
						44) "tutorial_21d.htm";,
						49) "tutorial_21e.htm";,
						53) "tutorial_21f.htm";
						}*/
					// TODO
					/*if (classId in QMCc.keys())
						htmltext = QMCc[classId];*/
					break;
			}
		}
		if (htmltext == "") {
			return "";
		}
		st.showTutorialHTML(htmltext);
		return "";
	}

	public Q255_Tutorial_Old(int questId, String name, String descr) {
		super(questId, name, descr);
	}

	public static void main(String[] args) {
		new Q255_Tutorial_Old(255, qn, "Tutorial");
	}
}
