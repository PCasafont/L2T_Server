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

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * @author LittleHakor
 */
public class TyrrTitanController extends TyrrController
{
	private static final int FRENZY_ID = 176;
	private static final int GUTS_ID = 139;
	
	public TyrrTitanController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		int hpPercent = getPlayerHealthPercent();

		if (hpPercent < 30)
		{
			if (isSkillAvailable(FRENZY_ID) && _player.getFirstEffect(FRENZY_ID) == null)
				return FRENZY_ID;
		}
		
		if (isAllowedToUseEmergencySkills())
		{
			if (isSkillAvailable(POWER_REVIVAL_ID))
				return POWER_REVIVAL_ID;
		}

		return super.pickSpecialSkill(target);
	}
	
	@Override
	protected final boolean isAllowedToUseEmergencySkills()
	{
		final boolean isFrenzyAvailable = isSkillAvailable(FRENZY_ID);
		final boolean isGutsAvailable = isSkillAvailable(GUTS_ID);
		
		final boolean isFrenzyActive = _player.getFirstEffect(FRENZY_ID) != null;
		final boolean isGutsActive = _player.getFirstEffect(GUTS_ID) != null;
		
		// Both of them are available... they can't be active... not allowed to use Battle Roar.
		if (isFrenzyAvailable && isGutsAvailable)
			return false;
		
		if (!isFrenzyAvailable)
		{
			if (isFrenzyActive)
				return true;
		}
		
		if (!isGutsAvailable)
		{
			if (isGutsActive)
				return true;
		}
		
		// None of them was available or active... let him go for it.
		if (!isFrenzyAvailable && !isGutsAvailable)
			return true;
		
		return false;
	}
}
