package ai.individual;

import ai.group_template.L2AttackableAIScript;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.util.Rnd;

/**
 * @author LasTravel
 */

public class NecromancerOfTheValley extends L2AttackableAIScript {
	private static final int necromancerOfTheValley = 22858;

	public NecromancerOfTheValley(int questId, String name, String descr) {
		super(questId, name, descr);

		addKillId(necromancerOfTheValley);
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isPet) {
		if (Rnd.get(100) < 30) {
			Creature attacker = isPet ? killer.getPet() : killer;

			if (attacker == null) {
				return super.onKill(npc, killer, isPet);
			}

			for (int a = 22818; a < 22820; a++) {
				Attackable minion = (Attackable) addSpawn(a, npc.getX(), npc.getY(), npc.getZ() + 10, npc.getHeading(), false, 0, true);

				minion.setIsRunning(true);

				minion.addDamageHate(attacker, 0, 500);

				minion.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
			}
		}

		return super.onKill(npc, killer, isPet);
	}

	public static void main(String[] args) {
		new NecromancerOfTheValley(-1, "NecromancerOfTheValley", "ai");
	}
}
