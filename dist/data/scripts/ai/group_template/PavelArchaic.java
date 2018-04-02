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
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

/**
 * * @author Gnacik
 */
public class PavelArchaic extends L2AttackableAIScript {
	private static final int[] mobs1 = {22801, 22804};
	private static final int[] mobs2 = {18917};

	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet) {
		if (!npc.isDead() && Util.contains(mobs2, npc.getNpcId())) {
			npc.doDie(attacker);

			if (Rnd.get(100) < 40) {
				Attackable golem1 = (Attackable) addSpawn(22801, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
				attackPlayer(golem1, attacker);

				Attackable golem2 = (Attackable) addSpawn(22804, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
				attackPlayer(golem2, attacker);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isPet) {
		if (Util.contains(mobs1, npc.getNpcId())) {
			Attackable
					golem = (Attackable) addSpawn(npc.getNpcId() + 1, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0);
			attackPlayer(golem, killer);
		}
		return super.onKill(npc, killer, isPet);
	}

	private void attackPlayer(Attackable npc, Player player) {
		npc.setIsRunning(true);
		npc.addDamageHate(player, 0, 999);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
	}

	public PavelArchaic(int questId, String name, String descr) {
		super(questId, name, descr);
		registerMobs(mobs1, QuestEventType.ON_KILL);
		registerMobs(mobs2, QuestEventType.ON_ATTACK);
	}

	public static void main(String[] args) {
		new PavelArchaic(-1, "PavelArchaic", "ai");
	}
}
