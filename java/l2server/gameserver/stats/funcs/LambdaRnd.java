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
import l2server.util.Rnd;

/**
 * @author mkizub
 */
public final class LambdaRnd extends Lambda
{
	private final Lambda max;
	private final boolean linear;

	public LambdaRnd(Lambda max, boolean linear)
	{
		this.max = max;
		this.linear = linear;
	}

	@Override
	public double calc(Env env)
	{
		if (linear)
		{
			return max.calc(env) * Rnd.nextDouble();
		}
		return max.calc(env) * Rnd.nextGaussian();
	}
}
