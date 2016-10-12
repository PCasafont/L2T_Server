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

package l2server.gameserver.model.olympiad;

import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.QuestState;

/**
 * @author DS
 */
public final class OlympiadParticipant
{
	public final int objectId;
	public L2PcInstance player;
	public final String name;
	public final int side;
	public final int baseClass;
	public boolean disconnected = false;
	public boolean defaulted = false;
	public final OlympiadNobleInfo nobleInfo;

	public OlympiadParticipant(L2PcInstance plr, int olympiadSide)
	{
		objectId = plr.getObjectId();
		player = plr;
		name = plr.getName();
		side = olympiadSide;
		baseClass = plr.getBaseClass();
		nobleInfo = Olympiad.getInstance().getNobleInfo(objectId);
	}

	public final void updatePlayer()
	{
		if (player == null || !player.isOnline())
		{
			player = L2World.getInstance().getPlayer(objectId);
		}
	}

	public final void competitionDone(CompetitionType type, boolean hasWon)
	{
		QuestState st = player.getQuestState("Q551_OlympiadStarter");
		if (st != null)
		{
			st.getQuest().notifyOlympiadCombat(player, type, hasWon);
		}

		st = player.getQuestState("Q552_OlympiadVeteran");
		if (st != null)
		{
			st.getQuest().notifyOlympiadCombat(player, type, hasWon);
		}

		st = player.getQuestState("Q553_OlympiadUndefeated");
		if (st != null)
		{
			st.getQuest().notifyOlympiadCombat(player, type, hasWon);
		}
	}
}
