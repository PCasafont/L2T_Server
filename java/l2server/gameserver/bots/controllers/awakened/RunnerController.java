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
package l2server.gameserver.bots.controllers.awakened;

import l2server.gameserver.bots.controllers.BotController;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.util.Rnd;

/**
 * Used by characters that tend to love running around, like mages, archers...
 * @author LittleHakor
 */
public class RunnerController extends BotController
{
	private int _kiteRate;
	
	public RunnerController(final L2PcInstance player)
	{
		super(player);
		
		// 1 / 3 runners wont run.
		if (Rnd.get(0, 2) == 0)
			_kiteRate = 0;
		else
			_kiteRate = Rnd.get(10, 50);
	}
	
	@Override
	protected int getMinimumRangeToKite(final L2Character targetedCharacter)
	{
		return targetedCharacter.isStunned() ? Rnd.get(400, 600) : super.getMinimumRangeToKite(targetedCharacter);
	}
	
	@Override
	protected int getKiteRate(final L2Character targetedCharacter)
	{
		// TODO
		// Additionally increase the chance if a gore buff is detected on a target hitting us.
		
		return _kiteRate;
	}
	
	@Override
	protected boolean maybeMoveToBestPosition(final L2Character targetedCharacter)
	{
		return maybeKite(targetedCharacter);
	}
	
	@Override
	protected void moveToBestPosition(final L2Character targetedCharacter)
	{
		kiteToBestPosition(targetedCharacter, true, 200, Rnd.get(300, 600));
	}
}