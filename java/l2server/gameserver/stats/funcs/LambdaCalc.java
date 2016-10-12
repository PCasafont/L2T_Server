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

/**
 * @author mkizub
 */
public final class LambdaCalc extends Lambda
{

	public Func[] funcs;

	public LambdaCalc()
	{
		funcs = new Func[0];
	}

	@Override
	public double calc(Env env)
	{
		double saveValue = env.value;
		try
		{
			env.value = 0;
			for (Func f : funcs)
			{
				f.calc(env);
			}
			return env.value;
		}
		finally
		{
			env.value = saveValue;
		}
	}

	public void addFunc(Func f)
	{
		int len = funcs.length;
		Func[] tmp = new Func[len + 1];
		System.arraycopy(funcs, 0, tmp, 0, len);
		tmp[len] = f;
		funcs = tmp;
	}
}
