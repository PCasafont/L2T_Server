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
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.util.Rnd;

import java.util.HashMap;
import java.util.Map;

public class PrisonGuards extends L2AttackableAIScript {
	private static final int GUARD1 = 18367;
	private static final int GUARD2 = 18368;
	private static final int STAMP = 10013;
	private static final String[] GUARDVARS = {"1st", "2nd", "3rd", "4th"};
	private static final String qn = "IOPRace";

	private static final int silence = 4098;
	private static final int pertification = 4578;
	private static final int eventTimer = 5239;

	private boolean firstAttacked = false;

	private Map<Npc, Integer> guards = new HashMap<Npc, Integer>();

	public PrisonGuards(int questId, String name, String descr) {
		super(questId, name, descr);
		int[] mob = {GUARD1, GUARD2};
		registerMobs(mob);

		// place 1
		guards.put(addSpawn(GUARD2, 160704, 184704, -3704, 49152, false, 0), 0);
		guards.put(addSpawn(GUARD2, 160384, 184704, -3704, 49152, false, 0), 0);
		guards.put(addSpawn(GUARD1, 160528, 185216, -3704, 49152, false, 0), 0);
		// place 2
		guards.put(addSpawn(GUARD2, 135120, 171856, -3704, 49152, false, 0), 1);
		guards.put(addSpawn(GUARD2, 134768, 171856, -3704, 49152, false, 0), 1);
		guards.put(addSpawn(GUARD1, 134928, 172432, -3704, 49152, false, 0), 1);
		// place 3
		guards.put(addSpawn(GUARD2, 146880, 151504, -2872, 49152, false, 0), 2);
		guards.put(addSpawn(GUARD2, 146366, 151506, -2872, 49152, false, 0), 2);
		guards.put(addSpawn(GUARD1, 146592, 151888, -2872, 49152, false, 0), 2);
		// place 4
		guards.put(addSpawn(GUARD2, 155840, 160448, -3352, 0, false, 0), 3);
		guards.put(addSpawn(GUARD2, 155840, 159936, -3352, 0, false, 0), 3);
		guards.put(addSpawn(GUARD1, 155578, 160177, -3352, 0, false, 0), 3);

		for (Npc npc : guards.keySet()) {
			npc.setIsImmobilized(true);
			if (npc.getNpcId() == GUARD1) {
				npc.setIsInvul(true);
				npc.disableCoreAI(true);
			}
		}
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		if (event.equalsIgnoreCase("Respawn")) {
			Npc newGuard = addSpawn(npc.getNpcId(),
					npc.getSpawn().getX(),
					npc.getSpawn().getY(),
					npc.getSpawn().getZ(),
					npc.getSpawn().getHeading(),
					false,
					0);
			newGuard.setIsImmobilized(true);
			if (npc.getNpcId() == GUARD1) {
				newGuard.setIsInvul(true);
				newGuard.disableCoreAI(true);
			}

			int place = guards.get(npc);
			guards.remove(npc);
			guards.put(newGuard, place);
		} else if (event.equalsIgnoreCase("attackEnd")) {
			if (npc.getNpcId() == GUARD2) {
				if (npc.getX() != npc.getSpawn().getX() || npc.getY() != npc.getSpawn().getY()) {
					npc.teleToLocation(npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(), npc.getSpawn().getHeading(), false);
					npc.setIsImmobilized(true);
				}
				((Attackable) npc).getAggroList().clear();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}

		return null;
	}

	@Override
	public String onSkillSee(Npc npc, Player player, Skill skill, WorldObject[] targets, boolean isPet) {
		Creature caster = isPet ? player.getPet() : player;
		if (caster == null) {
			caster = player.getSummon(0);
		}

		if (npc.getNpcId() == GUARD2) {
			if (firstAttacked && caster.getFirstEffect(eventTimer) == null) {
				if (caster.getFirstEffect(silence) == null) {
					castDebuff(npc, caster, silence, isPet, false, true);
				}
			}
		}

		return super.onSkillSee(npc, player, skill, targets, isPet);
	}

	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isPet) {
		Creature target = isPet ? player.getPet() : player;
		if (target == null) {
			target = player.getSummon(0);
		}

		if (npc.getNpcId() == GUARD2) {
			if (target.getFirstEffect(eventTimer) != null) {
				cancelQuestTimer("attackEnd", null, null);
				startQuestTimer("attackEnd", 180000, npc, null);

				npc.setIsImmobilized(false);
				npc.setTarget(target);
				npc.setRunning();
				((Attackable) npc).addDamageHate(target, 0, 999);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			} else {
				if (npc.getX() != npc.getSpawn().getX() || npc.getY() != npc.getSpawn().getY()) {
					npc.teleToLocation(npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(), npc.getSpawn().getHeading(), false);
					npc.setIsImmobilized(true);
				}
				((Attackable) npc).getAggroList().remove(target);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				return null;
			}
		}

		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public String onAttack(Npc npc, Player player, int damage, boolean isPet) {
		Creature attacker = isPet ? player.getPet() : player;
		if (attacker == null) {
			attacker = player.getSummon(0);
		}

		firstAttacked = true;

		if (attacker.getFirstEffect(eventTimer) == null) {
			if (attacker.getFirstEffect(pertification) == null) {
				castDebuff(npc, attacker, pertification, isPet, true, false);
			}

			npc.setTarget(null);
			((Attackable) npc).getAggroList().remove(attacker);
			((Attackable) npc).stopHating(attacker);
			((Attackable) npc).abortAttack();
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			return null;
		}

		if (npc.getNpcId() == GUARD2) {
			cancelQuestTimer("attackEnd", null, null);
			startQuestTimer("attackEnd", 180000, npc, null);

			npc.setIsImmobilized(false);
			npc.setTarget(attacker);
			npc.setRunning();
			((Attackable) npc).addDamageHate(attacker, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		} else if (npc.getNpcId() == GUARD1 && Rnd.get(100) < 5) {
			if (player.getQuestState(qn) != null && player.getQuestState(qn).getInt(GUARDVARS[guards.get(npc)]) != 1) {
				player.getQuestState(qn).set(GUARDVARS[guards.get(npc)], "1");
				player.getQuestState(qn).giveItems(STAMP, 1);
			}
		}

		return super.onAttack(npc, player, damage, isPet);
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isPet) {
		if (guards.containsKey(npc)) {
			startQuestTimer("Respawn", 20000, npc, null);
		}

		return super.onKill(npc, player, isPet);
	}

	private void castDebuff(Npc npc, Creature player, int effectId, boolean isSummon, boolean fromAttack, boolean isSpell) {
		if (fromAttack) {
			/*
			 * 1800107 It's not easy to obtain.
			 * 1800108 You're out of your mind coming here...
			 */
			int msg = npc.getNpcId() == GUARD1 ? 1800107 : 1800108;
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), msg));
		}

		Skill skill = SkillTable.getInstance().getInfo(effectId, isSpell ? 9 : 1);
		if (skill != null) {
			//npc.setTarget(isSummon ? player.getPet() : player); //TODO: correct target
			npc.setTarget(player);
			npc.doCast(skill);
		}
	}

	public static void main(String[] args) {
		new PrisonGuards(-1, "PrisonGuards", "ai");
	}
}
