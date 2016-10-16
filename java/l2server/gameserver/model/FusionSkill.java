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
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.stats.effects.EffectFusion;
import l2server.gameserver.util.Util;
import l2server.log.Log;

import java.util.concurrent.Future;

/**
 * @author kombat, Forsaiken
 */
public final class FusionSkill
{

	protected int skillCastRange;
	protected int fusionId;
	protected int fusionLevel;
	protected L2Character caster;
	protected L2Character target;
	protected Future<?> geoCheckTask;

	public L2Character getCaster()
	{
		return this.caster;
	}

	public L2Character getTarget()
	{
		return this.target;
	}

	public FusionSkill(L2Character caster, L2Character target, L2Skill skill)
	{
		this.skillCastRange = skill.getCastRange();
		this.caster = caster;
		this.target = target;
		this.fusionId = skill.getTriggeredId();
		this.fusionLevel = skill.getTriggeredLevel();

		L2Abnormal effect = this.target.getFirstEffect(this.fusionId);
		if (effect != null)
		{
			for (L2Effect eff : effect.getEffects())
			{
				if (eff instanceof EffectFusion)
				{
					((EffectFusion) eff).increaseEffect();
				}
			}
		}
		else
		{
			L2Skill force = SkillTable.getInstance().getInfo(this.fusionId, this.fusionLevel);
			if (force != null)
			{
				force.getEffects(this.caster, this.target, null);
			}
			else
			{
				Log.warning("Triggered skill [" + this.fusionId + ";" + this.fusionLevel + "] not found!");
			}
		}
		this.geoCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new GeoCheckTask(), 1000, 1000);
	}

	public void onCastAbort()
	{
		this.caster.setFusionSkill(null);
		this.caster.setContinuousDebuffTargets(null);
		L2Abnormal effect = this.target.getFirstEffect(this.fusionId);
		if (effect != null)
		{
			for (L2Effect eff : effect.getEffects())
			{
				if (eff instanceof EffectFusion)
				{
					((EffectFusion) eff).decreaseForce();
				}
			}
		}

		this.geoCheckTask.cancel(true);
	}

	public class GeoCheckTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (!Util.checkIfInRange(skillCastRange, caster, target, true))
				{
					caster.abortCast();
				}

				if (!GeoData.getInstance().canSeeTarget(caster, target))
				{
					caster.abortCast();
				}
			}
			catch (Exception e)
			{
				// ignore
			}
		}
	}
}
