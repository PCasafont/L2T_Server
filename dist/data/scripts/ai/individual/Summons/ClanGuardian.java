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
import l2server.gameserver.GeoData;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

/**
 * @author LasTravel
 * @author Pere
 * <p>
 * Summon Clan Guardian (skill id: 19008) AI
 */

public class ClanGuardian extends L2AttackableAIScript {
	private static final int clanGuardian = 15053;
	private static final L2Skill clanGuardianRecovery = SkillTable.getInstance().getInfo(19018, 1);

	public ClanGuardian(int id, String name, String descr) {
		super(id, name, descr);

		addSpawnId(clanGuardian);
	}

	@Override
	public final String onSpawn(L2Summon npc) {
		ClanGuardianAI ai = new ClanGuardianAI(npc);

		ai.setSchedule(ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ai, 5000, 10000));

		return null;
	}

	class ClanGuardianAI implements Runnable {
		private L2Summon clanGuardian;
		private L2PcInstance owner;
		private ScheduledFuture<?> schedule = null;

		protected ClanGuardianAI(L2Summon npc) {
			clanGuardian = npc;
			owner = npc.getOwner();
		}

		public void setSchedule(ScheduledFuture<?> schedule) {
			this.schedule = schedule;
		}

		@Override
		public void run() {
			if (clanGuardian == null || clanGuardian.isDead() || !owner.getSummons().contains(clanGuardian)) {
				if (schedule != null) {
					schedule.cancel(true);
					return;
				}
			}

			Collection<L2PcInstance> players = clanGuardian.getKnownList().getKnownPlayersInRadius(500);

			for (L2PcInstance player : players) {
				if (isValidTarget(player, clanGuardian)) {
					clanGuardian.setTarget(player);
					clanGuardian.doCast(clanGuardianRecovery);
				}
			}
		}
	}

	private boolean isValidTarget(L2PcInstance target, L2Summon summon) {
		if (target == null || summon == null) {
			return false;
		}

		if (summon.isDead() || target.isDead()) {
			return false;
		}

		if (target.isInvul(summon.getOwner())) {
			return false;
		}

		if (target.getClan() != summon.getOwner().getClan()) {
			return false;
		}

		if (!GeoData.getInstance().canSeeTarget(summon, target)) {
			return false;
		}

		if (!summon.isInsideRadius(target, 500, true, false)) {
			return false;
		}

		if (target.getCurrentHp() == target.getMaxHp() && target.getCurrentCp() == target.getMaxCp() && target.getCurrentMp() == target.getMaxMp()) {
			return false;
		}

		return true;
	}

	public static void main(String[] args) {
		new ClanGuardian(-1, "ClanGuardian", "ai/individual");
	}
}
