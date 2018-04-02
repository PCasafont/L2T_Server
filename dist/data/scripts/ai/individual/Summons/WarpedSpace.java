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
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.templates.skills.AbnormalType;

import java.util.concurrent.ScheduledFuture;

/**
 * @author LasTravel
 * @author Pere
 * <p>
 * Warped Space (skill id: 30519) AI
 */

public class WarpedSpace extends L2AttackableAIScript {
	private static final int gravityCoreId = 13432;
	private static final Skill spatialTrap = SkillTable.getInstance().getInfo(30528, 1);

	public WarpedSpace(int id, String name, String descr) {
		super(id, name, descr);

		addSpawnId(gravityCoreId);
	}

	@Override
	public final String onSpawn(Npc npc) {
		npc.disableCoreAI(true);

		WarpedSpaceAI ai = new WarpedSpaceAI(npc);

		ai.setSchedule(ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ai, 100, 10));

		return null;
	}

	class WarpedSpaceAI implements Runnable {
		private Npc gravityCore;
		private ScheduledFuture<?> schedule = null;

		protected WarpedSpaceAI(Npc npc) {
			gravityCore = npc;
		}

		public void setSchedule(ScheduledFuture<?> schedule) {
			this.schedule = schedule;
		}

		@Override
		public void run() {
			if (gravityCore == null || gravityCore.isDead() || gravityCore.isDecayed() || gravityCore.getOwner().isAlikeDead()) {
				for (Creature ch : gravityCore.getKnownList().getKnownCharactersInRadius(175)) {
					if (ch.getFirstEffect(AbnormalType.SPATIAL_TRAP) != null) {
						ch.getFirstEffect(AbnormalType.SPATIAL_TRAP).exit();
					}
				}

				if (schedule != null) {
					schedule.cancel(true);
					return;
				}
			}

			for (Creature ch : gravityCore.getKnownList().getKnownCharactersInRadius(175)) {
				if (ch.getFirstEffect(spatialTrap.getId()) != null) {
					continue;
				} else if (gravityCore.getOwner() == ch) {
					SkillTable.getInstance().getInfo(30527, 1).getEffects(gravityCore, ch);
					continue;
				}

				spatialTrap.getEffects(gravityCore, ch);
			}
		}
	}

	public static void main(String[] args) {
		new WarpedSpace(-1, "WarpedSpace", "ai/individual");
	}
}
