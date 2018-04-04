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

package l2server.gameserver.model;

import l2server.gameserver.GeoData;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.stats.effects.EffectFusion;
import l2server.gameserver.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

/**
 * @author kombat, Forsaiken
 */
public final class FusionSkill {
	private static Logger log = LoggerFactory.getLogger(FusionSkill.class.getName());

	protected int skillCastRange;
	protected int fusionId;
	protected int fusionLevel;
	protected Creature caster;
	protected Creature target;
	protected Future<?> geoCheckTask;

	public Creature getCaster() {
		return caster;
	}

	public Creature getTarget() {
		return target;
	}

	public FusionSkill(Creature caster, Creature target, Skill skill) {
		skillCastRange = skill.getCastRange();
		this.caster = caster;
		this.target = target;
		fusionId = skill.getTriggeredId();
		fusionLevel = skill.getTriggeredLevel();

		Abnormal effect = target.getFirstEffect(fusionId);
		if (effect != null) {
			for (L2Effect eff : effect.getEffects()) {
				if (eff instanceof EffectFusion) {
					((EffectFusion) eff).increaseEffect();
				}
			}
		} else {
			Skill force = SkillTable.getInstance().getInfo(fusionId, fusionLevel);
			if (force != null) {
				force.getEffects(caster, target, null);
			} else {
				log.warn("Triggered skill [" + fusionId + ";" + fusionLevel + "] not found!");
			}
		}
		geoCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new GeoCheckTask(), 1000, 1000);
	}

	public void onCastAbort() {
		caster.setFusionSkill(null);
		caster.setContinuousDebuffTargets(null);
		Abnormal effect = target.getFirstEffect(fusionId);
		if (effect != null) {
			for (L2Effect eff : effect.getEffects()) {
				if (eff instanceof EffectFusion) {
					((EffectFusion) eff).decreaseForce();
				}
			}
		}

		geoCheckTask.cancel(true);
	}

	public class GeoCheckTask implements Runnable {
		@Override
		public void run() {
			try {
				if (!Util.checkIfInRange(skillCastRange, caster, target, true)) {
					caster.abortCast();
				}

				if (!GeoData.getInstance().canSeeTarget(caster, target)) {
					caster.abortCast();
				}
			} catch (Exception e) {
				// ignore
			}
		}
	}
}
