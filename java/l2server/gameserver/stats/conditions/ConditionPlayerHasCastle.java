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

package l2server.gameserver.stats.conditions;

import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.Env;

/**
 * The Class ConditionPlayerHasCastle.
 *
 * @author MrPoke
 */
public final class ConditionPlayerHasCastle extends Condition
{

	private final int _castle;

	/**
	 * Instantiates a new condition player has castle.
	 *
	 * @param castle the castle
	 */
	public ConditionPlayerHasCastle(int castle)
	{
		_castle = castle;
	}

	/**
	 * Test impl.
	 *
	 * @param env the env
	 * @return true, if successful
	 * @see l2server.gameserver.stats.conditions.Condition#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.player instanceof L2PcInstance))
		{
			return false;
		}

		if (env.player.isGM())
		{
			return true;
		}

		L2Clan clan = ((L2PcInstance) env.player).getClan();
		if (clan == null)
		{
			return _castle == 0;
		}

		// Any castle
		if (_castle == -1)
		{
			return clan.getHasCastle() > 0;
		}

		if (_castle > 10 && clan.getHasCastle() > 0)
		{
			return CastleManager.getInstance().getCastleById(clan.getHasCastle()).getTendency() == _castle % 10;
		}

		return clan.getHasCastle() == _castle;
	}
}
