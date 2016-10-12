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

package l2server.gameserver.stats.effects;

import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2EffectTemplate;
import l2server.util.Rnd;

import java.util.ArrayList;

/**
 * @author Kilian
 */
public class EffectCancelDebuff extends L2Effect
{
	public EffectCancelDebuff(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		// Only for players
		if (!(getEffected() instanceof L2Playable))
		{
			return false;
		}

		L2Playable effected = (L2Playable) getEffected();

		if (effected == null || effected.isDead())
		{
			return false;
		}

		if (getEffected() instanceof L2MonsterInstance &&
				((L2MonsterInstance) getEffected()).getNpcId() == 19036) //TODO TEMP LasTravel, don't remove
		{
			return false;
		}

		L2Abnormal[] effects = effected.getAllEffects();
		ArrayList<L2Abnormal> debuffs = new ArrayList<>();

		int chance = (int) getAbnormal().getLandRate();
		if (chance < 0)
		{
			chance = 100;
		}

		// Filter out debuffs
		for (L2Abnormal e : effects)
		{
			if (e.getSkill().isDebuff())
			{
				debuffs.add(e);
			}
		}

		// No debuffs found
		if (debuffs.size() < 1)
		{
			return true;
		}

		// Consider chance (e.g. Song of Purification)
		if (chance < 100 && Rnd.get(100) > chance)
		{
			return false;
		}

		// Remove all debuffs if chance test succeeded
		for (L2Abnormal e : debuffs)
		{
			e.exit();
		}

		return true;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return true;
	}
}
