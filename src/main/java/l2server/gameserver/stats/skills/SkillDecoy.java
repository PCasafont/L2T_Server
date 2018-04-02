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

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.DecoyInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.util.Rnd;

public class SkillDecoy extends Skill {
	private final int npcId;
	private final int summonTotalLifeTime;

	public SkillDecoy(StatsSet set) {
		super(set);
		npcId = set.getInteger("npcId", 0);
		summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 20000);
	}

	@Override
	public void useSkill(Creature caster, WorldObject[] targets) {
		if (caster.isAlikeDead() || !(caster instanceof Player)) {
			return;
		}

		if (npcId == 0) {
			return;
		}

		final Player activeChar = (Player) caster;

		if (activeChar.inObserverMode()) {
			return;
		}

		if (activeChar.getPet() != null || activeChar.isMounted() || !activeChar.getSummons().isEmpty()) {
			return;
		}

		NpcTemplate decoyTemplate = NpcTable.getInstance().getTemplate(npcId);

		//TODO LasTravel, let's fix it by skill name, because for example Confusion Decoy have same npcIds as clone attak...
		if (getName().equalsIgnoreCase("Clone Attack")) {
			float angle = Rnd.get(1000);
			for (int i = 0; i < 3; i++) {
				final DecoyInstance decoy = new DecoyInstance(IdFactory.getInstance().getNextId(), decoyTemplate, activeChar, this);
				decoy.setCurrentHp(decoy.getMaxHp());
				decoy.setCurrentMp(decoy.getMaxMp());
				decoy.setHeading(activeChar.getHeading());
				activeChar.setDecoy(decoy);
				//World.getInstance().storeObject(Decoy);
				int x = Math.round(targets[0].getX() + (float) Math.cos(angle / 1000 * 2 * Math.PI) * 30);
				int y = Math.round(targets[0].getY() + (float) Math.sin(angle / 1000 * 2 * Math.PI) * 30);
				int z = targets[0].getZ() + 50;
				decoy.spawnMe(x, y, z);

				decoy.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, targets[0]);

				angle += 1000 / 3;
			}
		} else {
			final DecoyInstance decoy = new DecoyInstance(IdFactory.getInstance().getNextId(), decoyTemplate, activeChar, this);
			decoy.setCurrentHp(decoy.getMaxHp());
			decoy.setCurrentMp(decoy.getMaxMp());
			decoy.setHeading(activeChar.getHeading());
			activeChar.setDecoy(decoy);
			//World.getInstance().storeObject(Decoy);
			decoy.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		}

		// self Effect
		if (hasSelfEffects()) {
			final Abnormal effect = activeChar.getFirstEffect(getId());
			if (effect != null && effect.isSelfEffect()) {
				//Replace old effect with new one.
				effect.exit();
			}
			getEffectsSelf(activeChar);
		}
	}

	public final int getTotalLifeTime() {
		return summonTotalLifeTime;
	}
}
