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

package ai.individual;

import ai.group_template.L2AttackableAIScript;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

/**
 * @author LasTravel
 * <p>
 * Imperial Tomb Succubus AI
 * <p>
 * Source:
 * - http://l2wiki.com/Imperial_Tomb
 */

public class ImperialTombSuccubus extends L2AttackableAIScript {
	private static final int[] succubusIds = {23191, 23192, 23197, 23198};
	private static final int[] buffIds = {14975, 14976, 14977};

	public ImperialTombSuccubus(int id, String name, String descr) {
		super(id, name, descr);

		for (int a : succubusIds) {
			addKillId(a);
		}

		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable()) {
			if (spawn == null) {
				continue;
			}

			if (Util.contains(succubusIds, spawn.getNpcId())) {
				spawn.getNpc().setShowSummonAnimation(true);
			}
		}
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isPet) {
		if (Rnd.get(100) > 50) {
			SkillTable.getInstance().getInfo(buffIds[Rnd.get(buffIds.length)], 1).getEffects(killer, killer);
		}

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public int getOnKillDelay(int npcId) {
		return 0;
	}

	public static void main(String[] args) {
		new ImperialTombSuccubus(-1, "ImperialTombSuccubus", "ai");
	}
}
