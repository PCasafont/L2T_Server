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

import l2server.gameserver.model.ChanceCondition;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.conditions.Condition;
import l2server.gameserver.stats.funcs.Lambda;
import l2server.log.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mkizub
 */
public class L2EffectTemplate
{
	static Logger _log = Logger.getLogger(L2EffectTemplate.class.getName());

	private final Class<?> _func;
	private final Constructor<?> _constructor;

	public final L2AbnormalTemplate abnormal;

	public final Condition applayCond;
	public final Lambda lambda;
	public final String funcName;

	public final int triggeredId;
	public final int triggeredLevel;
	public final int triggeredEnchantRoute;
	public final int triggeredEnchantLevel;
	public final ChanceCondition chanceCondition;

	public L2EffectTemplate(L2AbnormalTemplate pAbnormal, Condition pApplayCond, Lambda pLambda, String func, int trigId, int trigLvl, int trigEnchRt, int trigEnchLvl, ChanceCondition chanceCond)
	{
		abnormal = pAbnormal;

		applayCond = pApplayCond;
		lambda = pLambda;
		funcName = func;

		triggeredId = trigId;
		triggeredLevel = trigLvl;
		triggeredEnchantRoute = trigEnchRt;
		triggeredEnchantLevel = trigEnchLvl;
		chanceCondition = chanceCond;

		try
		{
			_func = Class.forName("l2server.gameserver.stats.effects.Effect" + func);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			_constructor = _func.getConstructor(Env.class, L2EffectTemplate.class);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}

	public L2Effect getEffect(Env env)
	{
		try
		{
			return (L2Effect) _constructor.newInstance(env, this);
		}
		catch (IllegalAccessException e)
		{
			Log.log(Level.WARNING, "", e);
			return null;
		}
		catch (InstantiationException e)
		{
			Log.log(Level.WARNING, "", e);
			return null;
		}
		catch (InvocationTargetException e)
		{
			Log.log(Level.WARNING, "Error creating new instance of Class " + _func + " Exception was: " +
					e.getTargetException().getMessage(), e.getTargetException());
			return null;
		}
	}

	/**
	 * Creates an L2Effect instance from an existing one and an Env object.
	 *
	 * @param env
	 * @param stolen
	 * @return
	 */
	public static L2Effect getStolenEffect(Env env, L2Effect stolen)
	{
		Class<?> func;
		Constructor<?> stolenCons;
		try
		{
			func = Class.forName("l2server.gameserver.stats.effects.Effect" + stolen.getTemplate().funcName);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			stolenCons = func.getConstructor(Env.class, L2Effect.class);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			// if (_applayCond != null)
			// effect.setCondition(_applayCond);
			return (L2Effect) stolenCons.newInstance(env, stolen);
		}
		catch (IllegalAccessException e)
		{
			Log.log(Level.WARNING, "", e);
			return null;
		}
		catch (InstantiationException e)
		{
			Log.log(Level.WARNING, "", e);
			return null;
		}
		catch (InvocationTargetException e)
		{
			Log.log(Level.WARNING, "Error creating new instance of Class " + func + " Exception was: " +
					e.getTargetException().getMessage(), e.getTargetException());
			return null;
		}
	}
}
