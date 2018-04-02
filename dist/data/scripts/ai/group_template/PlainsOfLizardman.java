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
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.handler.SkillHandler;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.MagicSkillUse;

/**
 * * @author Gnacik
 * *
 */
public class PlainsOfLizardman extends L2AttackableAIScript {
	private static final int[] MOBS = {18864, 18865, 18866, 18868};

	private static final int FANTASY_MUSHROOM = 18864;
	private static final int FANTASY_MUSHROOM_SKILL = 6427;

	private static final int RAINBOW_FROG = 18866;
	private static final int RAINBOW_FROG_SKILL = 6429;

	private static final int STICKY_MUSHROOM = 18865;
	private static final int STICKY_MUSHROOM_SKILL = 6428;

	private static final int ENERGY_PLANT = 18868;
	private static final int ENERGY_PLANT_SKILL = 6430;

	public PlainsOfLizardman(int questId, String name, String descr) {
		super(questId, name, descr);

		registerMobs(MOBS, QuestEventType.ON_ATTACK);
	}

	public static void main(String[] args) {
		new PlainsOfLizardman(-1, "PlainsOfLizardman", "ai");
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		if (player != null && !player.isAlikeDead()) {
			// TODO for summons
			boolean isPet = false;
			if (event.endsWith("_pet") && player.getPet() != null && !player.getPet().isDead()) {
				isPet = true;
			}

			if (event.startsWith("rainbow_frog")) {
				triggerSkill(npc, isPet ? player.getPet() : player, RAINBOW_FROG_SKILL, 1);
			} else if (event.startsWith("energy_plant")) {
				triggerSkill(npc, isPet ? player.getPet() : player, ENERGY_PLANT_SKILL, 1);
			} else if (event.startsWith("sticky_mushroom")) {
				triggerSkill(npc, isPet ? player.getPet() : player, STICKY_MUSHROOM_SKILL, 1);
			} else if (event.startsWith("fantasy_mushroom")) {
				Skill skill = SkillTable.getInstance().getInfo(FANTASY_MUSHROOM_SKILL, 1);
				npc.doCast(skill);
				for (Creature target : npc.getKnownList().getKnownCharactersInRadius(200)) {
					if (target != null && target instanceof Attackable && target.getAI() != null) {
						skill.getEffects(npc, target);
						attackPlayer((Attackable) target, isPet ? player.getPet() : player);
					}
				}
				npc.doDie(player);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet) {
		if (npc.isDead()) {
			return null;
		}

		if (npc.getNpcId() == RAINBOW_FROG) {
			if (isPet) {
				startQuestTimer("rainbow_frog_pet", 2000, npc, attacker);
			} else {
				startQuestTimer("rainbow_frog", 2000, npc, attacker);
			}
			npc.doDie(attacker);
		} else if (npc.getNpcId() == STICKY_MUSHROOM) {
			if (isPet) {
				startQuestTimer("sticky_mushroom_pet", 2000, npc, attacker);
			} else {
				startQuestTimer("sticky_mushroom", 2000, npc, attacker);
			}
			npc.doDie(attacker);
		} else if (npc.getNpcId() == ENERGY_PLANT) {
			if (isPet) {
				startQuestTimer("energy_plant_pet", 2000, npc, attacker);
			} else {
				startQuestTimer("energy_plant", 2000, npc, attacker);
			}
			npc.doDie(attacker);
		} else if (npc.getNpcId() == FANTASY_MUSHROOM) {
			for (Creature target : npc.getKnownList().getKnownCharactersInRadius(1000)) {
				if (target != null && target instanceof Attackable && target.getAI() != null) {
					target.setIsRunning(true);
					target.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(npc.getX(), npc.getY(), npc.getZ(), 0));
				}
			}
			if (isPet) {
				startQuestTimer("fantasy_mushroom_pet", 3000, npc, attacker);
			} else {
				startQuestTimer("fantasy_mushroom", 3000, npc, attacker);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	private void triggerSkill(Creature caster, Playable playable, int skill_id, int skill_level) {
		Creature[] targets = new Creature[1];
		targets[0] = playable;

		Skill trigger = SkillTable.getInstance().getInfo(skill_id, skill_level);

		if (trigger != null && playable.isInsideRadius(caster, trigger.getCastRange(), true, false) &&
				playable.getInstanceId() == caster.getInstanceId()) {
			playable.broadcastPacket(new MagicSkillUse(playable, playable, skill_id, skill_level, 0, 0, 0));

			ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(trigger.getSkillType());
			if (handler != null) {
				handler.useSkill(playable, trigger, targets);
			} else {
				trigger.useSkill(playable, targets);
			}
		}
	}

	private void attackPlayer(Attackable npc, Playable playable) {
		npc.setIsRunning(true);
		npc.addDamageHate(playable, 0, 999);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, playable);
	}
}
