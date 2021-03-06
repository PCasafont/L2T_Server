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

import ai.group_template.L2AttackableAIScript;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Npc;

import java.util.concurrent.ScheduledFuture;

/**
 * @author LasTravel
 * @author Pere
 * <p>
 * Shadow Snare (skill id: 11058) AI
 */

public class ShadowSnare extends L2AttackableAIScript {
	private static final int[] whisperOfFearIds = {13323, 13324, 13325};
	private static final Skill shadowSnareZone = SkillTable.getInstance().getInfo(11059, 1);

	public ShadowSnare(int id, String name, String descr) {
		super(id, name, descr);

		for (int i : whisperOfFearIds) {
			addSpawnId(i);
		}
	}

	@Override
	public final String onSpawn(Npc npc) {
		npc.disableCoreAI(true);

		ShadowSnareAI ai = new ShadowSnareAI(npc);

		ai.setSchedule(ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ai, 1000, 2000));

		return null;
	}

	class ShadowSnareAI implements Runnable {
		private Npc whisperOfFear;
		private ScheduledFuture<?> schedule = null;

		protected ShadowSnareAI(Npc npc) {
			whisperOfFear = npc;
		}

		public void setSchedule(ScheduledFuture<?> schedule) {
			this.schedule = schedule;
		}

		@Override
		public void run() {
			if (whisperOfFear == null || whisperOfFear.isDead() || whisperOfFear.isDecayed() || whisperOfFear.getOwner().isAlikeDead()) {
				if (schedule != null) {
					schedule.cancel(true);
					return;
				}
			}

			whisperOfFear.setTarget(whisperOfFear);
			whisperOfFear.doCast(shadowSnareZone);
		}
	}

	public static void main(String[] args) {
		new ShadowSnare(-1, "ShadowSnare", "ai/individual");
	}
}
