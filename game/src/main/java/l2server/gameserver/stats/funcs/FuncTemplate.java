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

package l2server.gameserver.stats.funcs;

import l2server.gameserver.stats.Stats;
import l2server.gameserver.stats.conditions.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author mkizub
 */
public final class FuncTemplate {
	private static Logger log = LoggerFactory.getLogger(FuncTemplate.class.getName());
	
	public Condition applayCond;
	public final Class<?> func;
	public final Constructor<?> constructor;
	public final Stats stat;
	public final Lambda lambda;
	
	public FuncTemplate(Condition pApplayCond, String pFunc, Stats pStat, Lambda pLambda) {
		applayCond = pApplayCond;
		stat = pStat;
		lambda = pLambda;
		try {
			func = Class.forName("l2server.gameserver.stats.funcs.Func" + pFunc);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		try {
			constructor = func.getConstructor(Stats.class, // stats to update
					Object.class, // owner
					Lambda.class // value for function
			);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Func getFunc(Object owner) {
		try {
			Func f = (Func) constructor.newInstance(stat, owner, lambda);
			if (applayCond != null) {
				f.setCondition(applayCond);
			}
			return f;
		} catch (IllegalAccessException e) {
			log.warn("", e);
			return null;
		} catch (InstantiationException e) {
			log.warn("", e);
			return null;
		} catch (InvocationTargetException e) {
			log.warn("", e);
			return null;
		}
	}
}
