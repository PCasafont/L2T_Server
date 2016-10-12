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

import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Stats;

public class FuncSub extends Func
{
	private final Lambda _lambda;

	public FuncSub(Stats pStat, Object owner, Lambda lambda)
	{
		super(pStat, owner);
		_lambda = lambda;
	}

	@Override
	public int getOrder()
	{
		return 0x40;
	}

	@Override
	public void calc(Env env)
	{
		if (cond == null || cond.test(env))
		{
			env.value -= _lambda.calc(env);
		}
	}
}
