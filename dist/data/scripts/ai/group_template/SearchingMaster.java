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

package ai.group_template;

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;

public class SearchingMaster extends L2AttackableAIScript {
	private static final int[] mobs = {20965, 20966, 20967, 20968, 20969, 20970, 20971, 20972, 20973};

	public SearchingMaster(int questId, String name, String descr) {
		super(questId, name, descr);
		for (int id : mobs) {
			addAttackId(id);
		}
	}

	@Override
	public String onAttack(Npc npc, Player player, int damage, boolean isPet) {
		if (player == null) {
			return null;
		}

		npc.setIsRunning(true);
		((Attackable) npc).addDamageHate(player, 0, 999);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);

		return super.onAttack(npc, player, damage, isPet);
	}

	public static void main(String[] args) {
		new SearchingMaster(-1, "SearchingMaster", "ai");
	}
}
