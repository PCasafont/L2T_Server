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

package l2server.gameserver.templates.skills;

import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.VisualEffect;
import l2server.gameserver.stats.conditions.Condition;
import l2server.gameserver.stats.funcs.FuncTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author mkizub
 */
public class L2AbnormalTemplate
{
	static Logger _log = Logger.getLogger(L2AbnormalTemplate.class.getName());

	public final Condition applayCond;
	public final int counter;
	public final int duration; // in seconds
	public final VisualEffect[] visualEffect;
	public FuncTemplate[] funcTemplates;
	public final String[] stackType;
	public final byte stackLvl;
	public final boolean icon;
	public final double landRate; // to handle chance
	public final L2AbnormalType effectType; // to handle resistences etc...
	public final int comboId;

	public L2EffectTemplate[] effects = {};

	public L2AbnormalTemplate(Condition pApplayCond, int pCounter, int pDuration, VisualEffect[] pVisualEffect, String[] pStackType, byte pStackLvl, boolean showicon, double eLandRate, L2AbnormalType eType, int eComboId)
	{
		applayCond = pApplayCond;
		counter = pCounter;
		duration = pDuration;
		visualEffect = pVisualEffect;
		stackType = pStackType;
		stackLvl = pStackLvl;
		icon = showicon;
		landRate = eLandRate;
		effectType = eType;
		comboId = eComboId;
	}

	public final void attach(L2EffectTemplate effect)
	{
		if (effects == null)
		{
			effects = new L2EffectTemplate[]{effect};
		}
		else
		{
			int len = effects.length;
			L2EffectTemplate[] tmp = new L2EffectTemplate[len + 1];
			System.arraycopy(effects, 0, tmp, 0, len);
			tmp[len] = effect;
			effects = tmp;
		}
	}

	public L2Abnormal getEffect(Env env)
	{
		List<L2Effect> list = new ArrayList<>();
		for (L2EffectTemplate temp : effects)
		{
			list.add(temp.getEffect(env));
		}

		L2Effect[] effs = new L2Effect[list.size()];
		list.toArray(effs);
		L2Abnormal abnormal = new L2Abnormal(env, this, effs);
		for (L2Effect temp : effs)
		{
			temp.setAbnormal(abnormal);
		}

		return abnormal;
	}

	/**
	 * Creates an L2Effect instance from an existing one and an Env object.
	 *
	 * @param env
	 * @param stolen
	 * @return
	 */
	public L2Abnormal getStolenEffect(Env env, L2Abnormal stolen)
	{
		List<L2Effect> list = new ArrayList<>();
		for (L2Effect temp : stolen.getEffects())
		{
			list.add(L2EffectTemplate.getStolenEffect(env, temp));
		}

		L2Effect[] effs = new L2Effect[list.size()];
		list.toArray(effs);
		L2Abnormal abnormal = new L2Abnormal(env, this, effs);
		for (L2Effect temp : effs)
		{
			temp.setAbnormal(abnormal);
		}

		return abnormal;
	}

	public void attach(FuncTemplate f)
	{
		if (funcTemplates == null)
		{
			funcTemplates = new FuncTemplate[]{f};
		}
		else
		{
			int len = funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			funcTemplates = tmp;
		}
	}
}
