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
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;

import java.util.concurrent.ScheduledFuture;

/**
 * @author LasTravel
 * @author Pere
 * <p>
 * Summon Protection Stone (skill id: 11359) AI
 */

public class ProtectionStone extends L2AttackableAIScript {
	private static final int protectionStoneId = 13423;
	private static final int arcaneProtectionId = 11360;
	private static final int summonProtectionStoneId = 11359;

	public ProtectionStone(int id, String name, String descr) {
		super(id, name, descr);

		addSpawnId(protectionStoneId);
	}

	@Override
	public final String onSpawn(Npc npc) {
		npc.disableCoreAI(true);

		ProtectionStoneAI ai = new ProtectionStoneAI(npc);

		ai.setSchedule(ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ai, 1000, 5000));

		return null;
	}

	class ProtectionStoneAI implements Runnable {
		private Npc protectionStone;
		private Player owner;
		private ScheduledFuture<?> schedule = null;
		@SuppressWarnings("unused")
		private Skill arcaneProtection;

		protected ProtectionStoneAI(Npc npc) {
			protectionStone = npc;
			owner = npc.getOwner();
			arcaneProtection = SkillTable.getInstance().getInfo(arcaneProtectionId, owner.getSkillLevelHash(summonProtectionStoneId));
		}

		public void setSchedule(ScheduledFuture<?> schedule) {
			this.schedule = schedule;
		}

		@Override
		public void run() {
			if (protectionStone == null || protectionStone.isDead() || protectionStone.isDecayed()) {
				if (schedule != null) {
					schedule.cancel(true);
					return;
				}
			}

			L2Party party = owner.getParty();
			for (Player player : protectionStone.getKnownList().getKnownPlayersInRadius(250)) {
				if (player != owner && (player.getParty() == null || player.getParty() != party)) {
					continue;
				}

				Abnormal effect = player.getFirstEffect(11360);

				int buffLevel = effect == null ? 1 : effect.getLevel() + 1;

				if (buffLevel > 3) {
					buffLevel = 3;
				}

				if (effect != null) {
					effect.exit();
				}

				final Skill skill = SkillTable.getInstance().getInfo(11360, buffLevel);

				skill.getEffects(protectionStone, player);
			}
		}
	}

	public static void main(String[] args) {
		new ProtectionStone(-1, "ProtectionStone", "ai/individual");
	}
}
