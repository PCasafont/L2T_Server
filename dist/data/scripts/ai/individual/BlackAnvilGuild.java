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
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.util.Util;

/**
 * @author LasTravel
 */

public class BlackAnvilGuild extends L2AttackableAIScript {
	private static final int[] guildGolems = {19309, 19311, 19313};

	public BlackAnvilGuild(int id, String name, String descr) {
		super(id, name, descr);

		for (int a : guildGolems) {
			addSpawnId(a);
		}

		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable()) {
			if (spawn == null) {
				continue;
			}

			if (Util.contains(guildGolems, spawn.getNpcId())) {
				notifySpawn(spawn.getNpc());
			}
		}
	}

	@Override
	public final String onSpawn(Npc npc) {
		npc.setIsInvul(true);

		return super.onSpawn(npc);
	}

	public static void main(String[] args) {
		new BlackAnvilGuild(-1, "BlackAnvilGuild", "ai");
	}
}
