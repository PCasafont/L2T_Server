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

package ai.individual.Summons;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

import ai.group_template.L2AttackableAIScript;
import l2server.gameserver.GeoData;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author LasTravel
 * @author Pere
 *         <p>
 *         Clan Flag AI
 *         <p>
 *         Source:
 *         - http://www.lineage2.com/en/game/patch-notes/tauti/clans/
 */

public class ClanFlag extends L2AttackableAIScript
{
	private static final int clanFlagId = 19269;
	private static final L2Skill clanRising = SkillTable.getInstance().getInfo(15095, 1);
	private static final L2Skill clanCurse = SkillTable.getInstance().getInfo(15096, 1);

	public ClanFlag(int id, String name, String descr)
	{
		super(id, name, descr);

		addSpawnId(this.clanFlagId);
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.disableCoreAI(true);

		ClanFlagAI ai = new ClanFlagAI(npc);

		ai.setSchedule(ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ai, 5000, 10000));

		return null;
	}

	class ClanFlagAI implements Runnable
	{
		private L2Npc clanFlag;
		private ScheduledFuture<?> schedule = null;

		protected ClanFlagAI(L2Npc npc)
		{
			this.clanFlag = npc;
		}

		public void setSchedule(ScheduledFuture<?> schedule)
		{
			this.schedule = schedule;
		}

		@Override
		public void run()
		{
			if (this.clanFlag == null || this.clanFlag.isDead() || this.clanFlag.isDecayed())
			{
				if (this.schedule != null)
				{
					this.schedule.cancel(true);
					return;
				}
			}

			this.clanFlag.setTitle(this.clanFlag.getOwner().getClan().getName());

			Collection<L2PcInstance> players = this.clanFlag.getKnownList().getKnownPlayersInRadius(2000);
			for (L2PcInstance player : players)
			{
				doAction(player, this.clanFlag);
			}
		}
	}

	private void doAction(L2PcInstance target, L2Npc npc)
	{
		if (target == null || npc == null || npc.getOwner() == null)
		{
			return;
		}

		if (npc.isDead() || target.isDead())
		{
			return;
		}

		if (!GeoData.getInstance().canSeeTarget(npc, target))
		{
			return;
		}

		if (!npc.isInsideRadius(target, 2000, true, false))
		{
			return;
		}

		if (target.getClan() == npc.getOwner().getClan())
		{
			this.clanRising.getEffects(npc, target);
		}
		else
		{
			this.clanCurse.getEffects(npc, target);
		}
	}

	public static void main(String[] args)
	{
		new ClanFlag(-1, "ClanFlag", "ai/individual");
	}
}
