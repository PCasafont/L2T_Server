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
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class ZombieGatekeepers extends L2AttackableAIScript {
	public ZombieGatekeepers(int questId, String name, String descr) {
		super(questId, name, descr);
		super.addAttackId(22136);
		super.addAggroRangeEnterId(22136);
	}

	private HashMap<Integer, ArrayList<Creature>> attackersList = new HashMap<Integer, ArrayList<Creature>>();

	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet, Skill skill) {
		int npcObjId = npc.getObjectId();

		Creature target = isPet ? attacker.getPet() : attacker;

		if (attackersList.get(npcObjId) == null) {
			ArrayList<Creature> player = new ArrayList<Creature>();
			player.add(target);
			attackersList.put(npcObjId, player);
		} else if (!attackersList.get(npcObjId).contains(target)) {
			attackersList.get(npcObjId).add(target);
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isPet) {
		int npcObjId = npc.getObjectId();

		Creature target = isPet ? player.getPet() : player;

		Item VisitorsMark = player.getInventory().getItemByItemId(8064);
		Item FadedVisitorsMark = player.getInventory().getItemByItemId(8065);
		Item PagansMark = player.getInventory().getItemByItemId(8067);

		long mark1 = VisitorsMark == null ? 0 : VisitorsMark.getCount();
		long mark2 = FadedVisitorsMark == null ? 0 : FadedVisitorsMark.getCount();
		long mark3 = PagansMark == null ? 0 : PagansMark.getCount();

		if (mark1 == 0 && mark2 == 0 && mark3 == 0) {
			((Attackable) npc).addDamageHate(target, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		} else {
			if (attackersList.get(npcObjId) == null || !attackersList.get(npcObjId).contains(target)) {
				((Attackable) npc).getAggroList().remove(target);
			} else {
				((Attackable) npc).addDamageHate(target, 0, 999);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}

		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isPet) {
		int npcObjId = npc.getObjectId();
		if (attackersList.get(npcObjId) != null) {
			attackersList.get(npcObjId).clear();
		}

		return super.onKill(npc, killer, isPet);
	}

	public static void main(String[] args) {
		new ZombieGatekeepers(-1, "ZombieGatekeepers", "ai");
	}
}
