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

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Npc;

import ai.group_template.L2AttackableAIScript;

import java.util.concurrent.ScheduledFuture;

/**
 * @author LasTravel
 * @author Pere
 *         <p>
 *         Shadow Snare (skill id: 11058) AI
 */

public class ShadowSnare extends L2AttackableAIScript
{
	private static final int[] _whisperOfFearIds = {13323, 13324, 13325};
	private static final L2Skill _shadowSnareZone = SkillTable.getInstance().getInfo(11059, 1);

	public ShadowSnare(int id, String name, String descr)
	{
		super(id, name, descr);

		for (int i : _whisperOfFearIds)
		{
			addSpawnId(i);
		}
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.disableCoreAI(true);

		ShadowSnareAI ai = new ShadowSnareAI(npc);

		ai.setSchedule(ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ai, 1000, 2000));

		return null;
	}

	class ShadowSnareAI implements Runnable
	{
		private L2Npc _whisperOfFear;
		private ScheduledFuture<?> _schedule = null;

		protected ShadowSnareAI(L2Npc npc)
		{
			_whisperOfFear = npc;
		}

		public void setSchedule(ScheduledFuture<?> schedule)
		{
			_schedule = schedule;
		}

		@Override
		public void run()
		{
			if (_whisperOfFear == null || _whisperOfFear.isDead() || _whisperOfFear.isDecayed() ||
					_whisperOfFear.getOwner().isAlikeDead())
			{
				if (_schedule != null)
				{
					_schedule.cancel(true);
					return;
				}
			}

			_whisperOfFear.setTarget(_whisperOfFear);
			_whisperOfFear.doCast(_shadowSnareZone);
		}
	}

	public static void main(String[] args)
	{
		new ShadowSnare(-1, "ShadowSnare", "ai/individual");
	}
}
