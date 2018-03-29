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

package events.GiftOfVitality;

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.QuestManager;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * * @author Gnacik
 * *
 * * Retail Event : 'Gift of Vitality'
 */
public class GiftOfVitality extends Quest
{
	// Reuse between buffs
	private static final int hours = 5;

	private static final int jack = 4306;

	private static final int[][] spawns = {
			{82766, 149438, -3464, 33865},
			{82286, 53291, -1488, 15250},
			{147060, 25943, -2008, 18774},
			{148096, -55466, -2728, 40541},
			{87116, -141332, -1336, 52193},
			{43521, -47542, -792, 31655},
			{17203, 144949, -3024, 18166},
			{111164, 221062, -3544, 2714},
			{-13869, 122063, -2984, 18270},
			{-83161, 150915, -3120, 17311},
			{45402, 48355, -3056, 49153},
			{115616, -177941, -896, 30708},
			{-44928, -113608, -192, 30212},
			{-84037, 243194, -3728, 8992},
			{-119690, 44583, 360, 29289},
			{12084, 16576, -4584, 57345}
	};

	public GiftOfVitality(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(jack);
		addFirstTalkId(jack);
		addTalkId(jack);
		for (int[] spawn : spawns)
		{
			addSpawn(jack, spawn[0], spawn[1], spawn[2], spawn[3], false, 0);
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		htmltext = event;

		if (event.equalsIgnoreCase("vitality"))
		{
			long reuse = 0;
			String streuse = st.get("reuse");
			if (streuse != null)
			{
				reuse = Long.parseLong(streuse);
			}
			if (reuse > System.currentTimeMillis())
			{
				long remainingTime = (reuse - System.currentTimeMillis()) / 1000;
				int hours = (int) (remainingTime / 3600);
				int minutes = (int) (remainingTime % 3600 / 60);
				SystemMessage sm =
						SystemMessage.getSystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES);
				sm.addSkillName(23179);
				sm.addNumber(hours);
				sm.addNumber(minutes);
				player.sendPacket(sm);
				htmltext = "4306-notime.htm";
			}
			else
			{
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getInfo(23179, 1)); // Gift of Vitality
				st.setState(State.STARTED);
				st.set("reuse", String.valueOf(System.currentTimeMillis() + hours * 60 * 60 * 1000));
				htmltext = "4306-okvitality.htm";
			}
		}
		else if (event.equalsIgnoreCase("memories_player"))
		{
			if (player.getLevel() < 76)
			{
				htmltext = "4306-nolevel.htm";
			}
			else
			{
				if (player.isMageClass())
				{
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(5627, 1)); // Wind Walk
					npc.doCast(SkillTable.getInstance().getInfo(5628, 1)); // Shield
					npc.doCast(SkillTable.getInstance().getInfo(5637, 1)); // Magic Barrier
					npc.doCast(SkillTable.getInstance().getInfo(5633, 1)); // Bless the Soul
					npc.doCast(SkillTable.getInstance().getInfo(5634, 1)); // Acumen
					npc.doCast(SkillTable.getInstance().getInfo(5635, 1)); // Concentration
					npc.doCast(SkillTable.getInstance().getInfo(5636, 1)); // Empower
				}
				else
				{
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(5627, 1)); // Wind Walk
					npc.doCast(SkillTable.getInstance().getInfo(5628, 1)); // Shield
					npc.doCast(SkillTable.getInstance().getInfo(5637, 1)); // Magic Barrier
					npc.doCast(SkillTable.getInstance().getInfo(5629, 1)); // Bless the Body
					npc.doCast(SkillTable.getInstance().getInfo(5630, 1)); // Vampiric Rage
					npc.doCast(SkillTable.getInstance().getInfo(5631, 1)); // Regeneration
					npc.doCast(SkillTable.getInstance().getInfo(5632, 1)); // Haste
				}
				htmltext = "4306-okbuff.htm";
			}
		}
		else if (event.equalsIgnoreCase("memories_summon"))
		{
			if (player.getLevel() < 76)
			{
				htmltext = "4306-nolevel.htm";
			}
			else if (player.getSummons().isEmpty())
			{
				htmltext = "4306-nosummon.htm";
			}
			else
			{
				npc.setTarget(player.getPet());
				npc.doCast(SkillTable.getInstance().getInfo(5627, 1)); // Wind Walk
				npc.doCast(SkillTable.getInstance().getInfo(5628, 1)); // Shield
				npc.doCast(SkillTable.getInstance().getInfo(5637, 1)); // Magic Barrier
				npc.doCast(SkillTable.getInstance().getInfo(5629, 1)); // Bless the Body
				npc.doCast(SkillTable.getInstance().getInfo(5633, 1)); // Bless the Soul
				npc.doCast(SkillTable.getInstance().getInfo(5630, 1)); // Vampiric Rage
				npc.doCast(SkillTable.getInstance().getInfo(5634, 1)); // Acumen
				npc.doCast(SkillTable.getInstance().getInfo(5631, 1)); // Regeneration
				npc.doCast(SkillTable.getInstance().getInfo(5635, 1)); // Concentration
				npc.doCast(SkillTable.getInstance().getInfo(5632, 1)); // Haste
				npc.doCast(SkillTable.getInstance().getInfo(5636, 1)); // Empower
				htmltext = "4306-okbuff.htm";
			}
		}

		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		return "4306.htm";
	}

	public static void main(String[] args)
	{
		new GiftOfVitality(-1, "GiftOfVitality", "events");
	}
}
