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
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LasTravel
 * <p>
 * Spicula Clone Generator AI
 */

public class SpiculaCloneGenerator extends L2AttackableAIScript {
	private static final int yin = 19320;
	private static final int yinFragment = 19308;
	private static final int spiculaElite = 23303;
	private static Map<Integer, Long> yinControl = new HashMap<Integer, Long>();

	public SpiculaCloneGenerator(int id, String name, String descr) {
		super(id, name, descr);

		addKillId(yinFragment);
		addAttackId(yin);
		addSpawnId(yin);
		addSpawnId(yinFragment);

		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable()) {
			if (spawn == null) {
				continue;
			}

			if (spawn.getNpcId() == yin || spawn.getNpcId() == yinFragment) {
				notifySpawn(spawn.getNpc());
			}
		}
	}

	@Override
	public final String onSpawn(Npc npc) {
		if (npc.getNpcId() == yin) {
			npc.setInvul(true);
		}

		npc.setImmobilized(true);

		return super.onSpawn(npc);
	}

	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet, Skill skill) {
		if (yinControl.containsKey(npc.getObjectId())) {
			if (System.currentTimeMillis() >= yinControl.get(npc.getObjectId()) + 180000) {
				yinControl.put(npc.getObjectId(), System.currentTimeMillis());

				spawnSpiculas(npc, attacker);
			}
		} else {
			yinControl.put(npc.getObjectId(), System.currentTimeMillis());

			spawnSpiculas(npc, attacker);
		}

		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isPet) {
		spawnSpiculas(npc, killer);

		return super.onKill(npc, killer, isPet);
	}

	private void spawnSpiculas(Npc npc, Player killer) {
		npc.broadcastPacket(new ExShowScreenMessage("$s1 has summoned Elite Soldiers through the Clone Generator.".replace("$s1", killer.getName()),
				3000)); //id: 1802277

		for (int a = 0; a <= (npc.getNpcId() == yinFragment ? 2 : 4); a++) {
			Npc minion = addSpawn(spiculaElite, killer.getX(), killer.getY(), killer.getZ(), 0, true, 180000, true);

			minion.setRunning(true);

			minion.setTarget(killer);

			((MonsterInstance) minion).addDamageHate(killer, 500, 99999);

			minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
		}
	}

	@Override
	public int getOnKillDelay(int npcId) {
		return 0;
	}

	public static void main(String[] args) {
		new SpiculaCloneGenerator(-1, "SpiculaCloneGenerator", "ai");
	}
}
