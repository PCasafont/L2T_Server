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

package l2server.gameserver.stats.skills;

import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.util.Point3D;
import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkillSpawn extends Skill {
	private static Logger log = LoggerFactory.getLogger(SkillSpawn.class.getName());

	private final int npcId;
	private final int despawnDelay;
	private final boolean summonSpawn;
	private final boolean randomOffset;
	private int count;

	public SkillSpawn(StatsSet set) {
		super(set);
		npcId = set.getInteger("npcId", 0);
		despawnDelay = set.getInteger("despawnDelay", 0);
		summonSpawn = set.getBool("isSummonSpawn", false);
		randomOffset = set.getBool("randomOffset", true);
		count = set.getInteger("count", 1);
	}

	@Override
	public void useSkill(Creature caster, WorldObject[] targets) {
		if (caster.isAlikeDead()) {
			return;
		}

		if (npcId == 0) {
			log.warn("NPC ID not defined for skill ID:" + getId());
			return;
		}

		final NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
		if (template == null) {
			log.warn("Spawn of the nonexisting NPC ID:" + npcId + ", skill ID:" + getId());
			return;
		}

		try {
			int x = 0;
			int y = 0;
			int z = 0;

			boolean skillMastery = Formulas.calcSkillMastery(caster, this);
			int first = 0;
			if (skillMastery && getId() == 10532) {
				first = -count;
			}

			for (int i = first; i < count; i++) {
				final L2Spawn spawn = new L2Spawn(template);

				spawn.setInstanceId(caster.getInstanceId());
				spawn.setHeading(-1);

				if (caster instanceof Player && getTargetType() == SkillTargetType.TARGET_GROUND) {
					Point3D wordPosition = caster.getSkillCastPosition();

					if (wordPosition != null) {
						x = wordPosition.getX();
						y = wordPosition.getY();
						z = wordPosition.getZ();
					}
				} else {
					if (randomOffset) {
						x = caster.getX() + (Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20));
						y = caster.getY() + (Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20));
					} else {
						x = caster.getX();
						y = caster.getY();
					}
					z = caster.getZ();
				}

				spawn.setX(x);
				spawn.setY(y);
				spawn.setZ(z + 20);

				spawn.stopRespawn();

				Npc npc = spawn.getNpc();

				if (caster instanceof Player) {
					npc.setOwner((Player) caster);
					npc.setInstanceId(caster.getInstanceId());
				}

				npc.setRunning(true);
				spawn.doSpawn(summonSpawn);

				if (despawnDelay > 0) {
					npc.scheduleDespawn(despawnDelay);
				}
			}
		} catch (Exception e) {
			log.warn("Exception while spawning NPC ID: " + npcId + ", skill ID: " + getId() + ", exception: " + e.getMessage(), e);
		}

		// self Effect
		if (hasSelfEffects()) {
			final Abnormal effect = caster.getFirstEffect(getId());
			if (effect != null && effect.isSelfEffect()) {
				//Replace old effect with new one.
				effect.exit();
			}
			getEffectsSelf(caster);
		}
	}
}
