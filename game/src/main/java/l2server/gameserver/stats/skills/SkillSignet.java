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

/*
  @author Forsaiken
 */

package l2server.gameserver.stats.skills;

import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.EffectPointInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.util.Point3D;

public final class SkillSignet extends Skill {
	private int effectNpcId;
	public int effectId;
	public int effectLevel;

	public SkillSignet(StatsSet set) {
		super(set);
		effectNpcId = set.getInteger("effectNpcId", -1);
		effectId = set.getInteger("effectId", -1);
		effectLevel = set.getInteger("effectLevel", 1);
	}

	@Override
	public void useSkill(Creature caster, WorldObject[] targets) {
		if (caster.isAlikeDead()) {
			return;
		}

		NpcTemplate template = NpcTable.getInstance().getTemplate(effectNpcId);
		EffectPointInstance effectPoint = new EffectPointInstance(IdFactory.getInstance().getNextId(), template, caster);
		effectPoint.setCurrentHp(effectPoint.getMaxHp());
		effectPoint.setCurrentMp(effectPoint.getMaxMp());
		//World.getInstance().storeObject(effectPoint);

		int x = caster.getX();
		int y = caster.getY();
		int z = caster.getZ();

		if (caster instanceof Player && getTargetType() == SkillTargetType.TARGET_GROUND) {
			Point3D wordPosition = caster.getSkillCastPosition();

			if (wordPosition != null) {
				x = wordPosition.getX();
				y = wordPosition.getY();
				z = wordPosition.getZ();
			}
		}
		getEffects(caster, effectPoint);

		effectPoint.setInvul(true);
		effectPoint.spawnMe(x, y, z);
	}
}
