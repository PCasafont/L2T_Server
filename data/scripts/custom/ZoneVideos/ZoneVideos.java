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

package custom.ZoneVideos;

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.Race;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.serverpackets.ExShowUsmPacket;

public class ZoneVideos extends Quest
{
	private static final boolean showIntroMovies = false;

	private static final int[] ZONES = {523400, 523402, 523403, 523404};

	private static final int[] VIDEOS = {101, 102, 103, 78, 77};

	@Override
	public final String onEnterWorld(L2PcInstance player)
	{
		QuestState st = player.getQuestState("ZoneVideos");
		if (st == null)
		{
			st = newQuestState(player);
		}
		if (showIntroMovies)
		{
			if (st.getGlobalQuestVar("FirstZoneVid").length() == 0)
			{
				// GoD intro
				player.sendPacket(new ExShowUsmPacket(player.getRace() == Race.Ertheia ? 147 : 148));
				st.saveGlobalQuestVar("FirstZoneVid", "done");
			}
		}
		return null;
	}

	@Override
	public final String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;
			QuestState st = player.getQuestState("ZoneVideos");
			if (st == null)
			{
				st = newQuestState(player);
			}
			if (st.getGlobalQuestVar("ZoneVid" + zone.getId()).length() == 0)
			{
				int videoId = zone.getId() % 100;
				if (!showIntroMovies && videoId > 100)
				{
					return null;
				}

				if (videoId == 0 &&
						player.getQuestState("Q10320_LetsGoToTheCentralSquare") != null) // TODO: first quest name
				{
					videoId = 1;
				}
				player.showQuestMovie(VIDEOS[videoId]);
				st.saveGlobalQuestVar("ZoneVid" + zone.getId(), "done");
			}
		}
		return null;
	}

	public ZoneVideos(int questId, String name, String descr)
	{
		super(questId, name, descr);

		setOnEnterWorld(true);

		for (int zoneId : ZONES)
		{
			addEnterZoneId(zoneId);
		}
	}

	public static void main(String[] args)
	{
		new ZoneVideos(-1, "ZoneVideos", "zone videos");
	}
}
