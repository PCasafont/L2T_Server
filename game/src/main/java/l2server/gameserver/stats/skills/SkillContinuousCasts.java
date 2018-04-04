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

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.handler.SkillHandler;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.MagicSkillLaunched;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.templates.StatsSet;
import l2server.util.Point3D;

public class SkillContinuousCasts extends Skill {
	private final int skillId;
	private final int skillLvl;
	private final int skillEnchantRoute;
	private final int skillEnchantLvl;
	private final int castAmount;

	public SkillContinuousCasts(StatsSet set) {
		super(set);

		skillId = set.getInteger("castId", 0);
		skillLvl = set.getInteger("castLvl", 0);
		skillEnchantRoute = set.getInteger("castEnchantRoute", 0);
		skillEnchantLvl = set.getInteger("castEnchantLvl", 0);
		castAmount = set.getInteger("castAmount", 0);
	}

	@Override
	public void useSkill(Creature activeChar, WorldObject[] targets) {
		Point3D position = activeChar.getSkillCastPosition();
		ThreadPoolManager.getInstance().scheduleEffect(new CastTask(activeChar, position), 1000);
	}

	private class CastTask implements Runnable {
		private int count = castAmount;
		Creature activeChar;
		Point3D position;

		public CastTask(Creature activeChar, Point3D position) {
			this.activeChar = activeChar;
			this.position = position;
		}

		@Override
		public void run() {
			if (!cast(activeChar, position)) {
				return;
			}

			if (count > 0) {
				ThreadPoolManager.getInstance().scheduleEffect(this, 1000);
			}

			count--;
		}
	}

	public boolean cast(Creature activeChar, Point3D position) {
		if (activeChar.isAlikeDead() || !activeChar.isCastingNow() || activeChar.getLastSkillCast() != this) {
			return false;
		}

		WorldObject[] targets = getTargetList(activeChar, false, activeChar);
		activeChar.broadcastPacket(new MagicSkillLaunched(activeChar, getDisplayId(), getLevelHash(), targets));
		for (WorldObject targetObj : targets) {
			if (!(targetObj instanceof Creature)) {
				continue;
			}

			Creature target = (Creature) targetObj;
			if (target.isDead()) {
				continue;
			}

			int level = skillLvl;
			if (level == 0) {
				level = 1;
				for (Abnormal e : target.getAllEffects()) {
					if (e == null) {
						continue;
					}

					if (e.getSkill().getId() == skillId) {
						int maxLevel = SkillTable.getInstance().getMaxLevel(e.getSkill().getId());
						if (e.getSkill().getLevel() < maxLevel) {
							level = e.getSkill().getLevel() + 1;
						} else {
							level = maxLevel;
						}
					}
				}
			}

			Skill skillToCast = SkillTable.getInstance().getInfo(skillId, level, skillEnchantRoute, skillEnchantLvl);
			//System.out.println(targetObj + " " + level);
			if (!skillToCast.checkCondition(activeChar, target, false)) {
				activeChar.sendMessage("Can't reach " + target);
				continue;
			}

			if (activeChar instanceof Player && !((Player) activeChar).checkPvpSkill(target, skillToCast)) {
				continue;
			}

			WorldObject[] subTargets = skillToCast.getTargetList(activeChar, false, target);
			if (subTargets.length == 0) {
				activeChar.sendMessage("No target found");
				continue;
			}

			Creature firstTarget = (Creature) subTargets[0];
			ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skillToCast.getSkillType());
			activeChar.broadcastPacket(new MagicSkillUse(activeChar, firstTarget, skillToCast.getDisplayId(), skillToCast.getLevelHash(), 0, 0, 0));

			// Launch the magic skill and calculate its effects
			// TODO: once core will support all possible effects, use effects (not handler)
			if (handler != null) {
				handler.useSkill(activeChar, skillToCast, subTargets);
			} else {
				skillToCast.useSkill(activeChar, subTargets);
			}
		}

		activeChar.broadcastPacket(new MagicSkillLaunched(activeChar, getDisplayId(), getLevelHash(), new Creature[]{activeChar}));

		if (activeChar instanceof Player) {
			((Player) activeChar).rechargeAutoSoulShot(false, true, false);
		}

		return true;
	}
}
