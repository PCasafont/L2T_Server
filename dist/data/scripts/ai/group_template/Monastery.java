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
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.*;
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.templates.skills.SkillType;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.Collection;

public class Monastery extends L2AttackableAIScript {
	static final int[] mobs1 = {22124, 22125, 22126, 22127, 22129};
	static final int[] mobs2 = {22134, 22135};

	static final int[] messages = {1121006, // You cannot carry a weapon without authorization!
			10077, // $s1, why would you choose the path of darkness?!
			10078 // $s1! How dare you defy the will of Einhasad!
	};

	public Monastery(int questId, String name, String descr) {
		super(questId, name, descr);
		registerMobs(mobs1, QuestEventType.ON_AGGRO_RANGE_ENTER, QuestEventType.ON_SPAWN, QuestEventType.ON_SPELL_FINISHED);
		registerMobs(mobs2, QuestEventType.ON_SKILL_SEE);
	}

	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isPet) {
		if (Util.contains(mobs1, npc.getNpcId()) && !npc.isInCombat() && npc.getTarget() == null) {
			if (player.getActiveWeaponInstance() != null) {
				npc.setTarget(player);
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), messages[0]));
				switch (npc.getNpcId()) {
					case 22124:
					case 22126: {
						Skill skill = SkillTable.getInstance().getInfo(4589, 8);
						npc.doCast(skill);
						break;
					}
					default: {
						npc.setRunning(true);
						((Attackable) npc).addDamageHate(player, 0, 999);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
						break;
					}
				}
			} else if (((Attackable) npc).getMostHated() == null) {
				return null;
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	@Override
	public String onSkillSee(Npc npc, Player caster, Skill skill, WorldObject[] targets, boolean isPet) {
		if (Util.contains(mobs2, npc.getNpcId())) {
			if (skill.getSkillType() == SkillType.AGGDAMAGE && targets.length != 0) {
				for (WorldObject obj : targets) {
					if (obj.equals(npc)) {
						NpcSay packet = new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), messages[Rnd.get(2) + 1]);
						packet.addStringParameter(caster.getName());
						npc.broadcastPacket(packet);
						((Attackable) npc).addDamageHate(caster, 0, 999);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, caster);
						break;
					}
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onSpawn(Npc npc) {
		if (Util.contains(mobs1, npc.getNpcId())) {
			ArrayList<Playable> result = new ArrayList<Playable>();
			Collection<WorldObject> objs = npc.getKnownList().getKnownObjects().values();
			for (WorldObject obj : objs) {
				if (obj instanceof Player || obj instanceof PetInstance) {
					if (Util.checkIfInRange(npc.getAggroRange(), npc, obj, true) && !((Creature) obj).isDead()) {
						result.add((Playable) obj);
					}
				}
			}
			if (!result.isEmpty() && result.size() != 0) {
				Object[] characters = result.toArray();
				for (Object obj : characters) {
					Playable target = (Playable) (obj instanceof Player ? obj : ((Summon) obj).getOwner());
					if (target.getActiveWeaponInstance() != null && !npc.isInCombat() && npc.getTarget() == null) {
						npc.setTarget(target);
						npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), messages[0]));
						switch (npc.getNpcId()) {
							case 22124:
							case 22126:
							case 22127: {
								Skill skill = SkillTable.getInstance().getInfo(4589, 8);
								npc.doCast(skill);
								break;
							}
							default: {
								npc.setRunning(true);
								((Attackable) npc).addDamageHate(target, 0, 999);
								npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
								break;
							}
						}
					}
				}
			}
		}
		return super.onSpawn(npc);
	}

	@Override
	public String onSpellFinished(Npc npc, Player player, Skill skill) {
		if (Util.contains(mobs1, npc.getNpcId()) && skill.getId() == 4589) {
			npc.setRunning(true);
			((Attackable) npc).addDamageHate(player, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		return super.onSpellFinished(npc, player, skill);
	}

	public static void main(String[] args) {
		new Monastery(-1, "Monastery", "ai");
	}
}
