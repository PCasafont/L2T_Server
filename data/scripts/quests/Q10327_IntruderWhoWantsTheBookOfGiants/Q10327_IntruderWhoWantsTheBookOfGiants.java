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

package quests.Q10327_IntruderWhoWantsTheBookOfGiants;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2GuardInstance;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.GlobalQuest;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.util.Rnd;

/**
 * @author Pere
 */
public class Q10327_IntruderWhoWantsTheBookOfGiants extends Quest
{
	// Quest
	public static String qn = "Q10327_IntruderWhoWantsTheBookOfGiants";

	// NPC
	private int pantheon = 32972;
	private int guard = 33004;
	private int book = 33126;
	private int intruder = 23121;

	private int bookItem = 17575;

	public Q10327_IntruderWhoWantsTheBookOfGiants(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(pantheon);
		addTalkId(pantheon);
		addFirstTalkId(guard);
		addFirstTalkId(book);
		addSkillSeeId(guard);
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

		if (npc.getNpcId() == pantheon)
		{
			if (event.equalsIgnoreCase("32972-03.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.equalsIgnoreCase("teleport"))
			{
				InstanceManager.getInstance().createInstance(player.getObjectId());

				int[] bookIds = new int[4];
				st.set("guardId", String.valueOf(
						addSpawn(guard, -114710, 245457, -7968, 49152, false, 0, false, player.getObjectId())
								.getObjectId()));
				bookIds[0] =
						addSpawn(book, -113757, 244686, -7952, 0, false, 0, false, player.getObjectId()).getObjectId();
				bookIds[1] =
						addSpawn(book, -115672, 244683, -7952, 0, false, 0, false, player.getObjectId()).getObjectId();
				bookIds[2] =
						addSpawn(book, -114714, 245750, -7952, 0, false, 0, false, player.getObjectId()).getObjectId();
				bookIds[3] =
						addSpawn(book, -114706, 243605, -7952, 0, false, 0, false, player.getObjectId()).getObjectId();

				st.set("giantsBookId", String.valueOf(bookIds[Rnd.get(4)]));

				player.setInstanceId(player.getObjectId());
				player.teleToLocation(-114696, 243926, -7868, false);
				return null;
			}
			else if (event.equalsIgnoreCase("32972-05.htm") && st.getInt("cond") == 3)
			{
				player.sendPacket(new ExShowScreenMessage(11022201, 0, true, 7000));
				st.unset("cond");
				st.takeItems(bookItem, -1);
				st.giveItems(112, 2);
				st.giveItems(57, 16000);
				st.addExpAndSp(7800, 3500);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);

				// Main quests state
				player.setGlobalQuestFlag(GlobalQuest.STARTING, 8);
			}
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, final L2PcInstance player)
	{
		String htmltext = null;
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			npc.showChatWindow(player);
			return null;
		}

		if (npc.getNpcId() == guard)
		{
			if (st.getInt("cond") < 3 && player.getInstanceId() == player.getObjectId())
			{
				htmltext = "33004-01.htm";
			}
		}
		else if (npc.getNpcId() == book)
		{
			if (st.getInt("cond") == 1 && st.getInt("giantsBookId") == npc.getObjectId())
			{
				htmltext = "33126-01.htm";
				st.set("cond", "2");
				st.giveItems(bookItem, 1);
				st.playSound("ItemSound.quest_middle");
				player.sendPacket(new ExShowScreenMessage(1032322, 0, true, 7000));
				final L2Npc intruder1 =
						addSpawn(intruder, -114835, 244966, -7976, 16072, false, 0, false, player.getObjectId());
				final L2Npc intruder2 =
						addSpawn(intruder, -114564, 244954, -7976, 16072, false, 0, false, player.getObjectId());
				final L2GuardInstance guard = (L2GuardInstance) L2World.getInstance().findObject(st.getInt("guardId"));
				guard.setRunning();

				ThreadPoolManager.getInstance().executeAi(new Runnable()
				{
					@Override
					public void run()
					{
						if (InstanceManager.getInstance().getInstance(player.getObjectId()) == null)
						{
							return;
						}

						if (guard.getHateList() == null)
						{
							L2Npc moveTo = intruder1;
							if (moveTo.isDead())
							{
								moveTo = intruder2;
							}
							int x = moveTo.getX() - 100 + Rnd.get(200);
							int y = moveTo.getY() - 100 + Rnd.get(200);
							int z = moveTo.getZ();
							guard.getAI()
									.setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(x, y, z, 0));
						}

						boolean allDead = true;
						for (L2Npc iNpc : InstanceManager.getInstance().getInstance(player.getObjectId()).getNpcs())
						{
							if (iNpc instanceof L2MonsterInstance && !iNpc.isDead())
							{
								allDead = false;
							}
						}

						if (allDead)
						{
							player.sendPacket(new ExShowScreenMessage(1032327, 0, true, 10000));
							st.set("cond", "3");
							st.playSound("ItemSound.quest_middle");
							guard.setWalking();
							guard.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
									new L2CharPosition(guard.getSpawn().getX(), guard.getSpawn().getY(),
											guard.getSpawn().getZ(), 0));
						}
						else
						{
							ThreadPoolManager.getInstance().scheduleAi(this, 2000);
						}
					}
				});
			}
			else
			{
				htmltext = "33126-02.htm";
			}
		}

		if (htmltext == null)
		{
			npc.showChatWindow(player);
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

		if (npc.getNpcId() == pantheon)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (canStart(player))
					{
						htmltext = "32972-01.htm";
					}
					else
					{
						htmltext = "32972-00.htm";
					}
					break;
				case State.STARTED:
					htmltext = "32972-03.htm";
					if (st.getInt("cond") == 3)
					{
						htmltext = "32972-04.htm";
					}
					break;
				case State.COMPLETED:
					htmltext = "32972-06.htm"; // TODO
					break;
			}
		}
		return htmltext;
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (!(npc instanceof L2GuardInstance))
		{
			return null;
		}

		L2GuardInstance guard = (L2GuardInstance) npc;

		for (L2Object o : targets)
		{
			if (!(o instanceof L2MonsterInstance))
			{
				continue;
			}

			L2MonsterInstance intruder = (L2MonsterInstance) o;

			guard.addDamageHate(intruder, 0, 100000);
		}

		return null;
	}

	@Override
	public boolean canStart(L2PcInstance player)
	{
		return player.getGlobalQuestFlag(GlobalQuest.STARTING, 7) && player.getLevel() <= 20;
	}

	public static void main(String[] args)
	{
		new Q10327_IntruderWhoWantsTheBookOfGiants(10327, qn,
				"Using skills to fight monsters. Oppotunity to obtain a no-Grade accessory.");
	}
}
