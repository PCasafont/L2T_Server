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
package l2server.gameserver.bots.controllers;

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.util.Rnd;

/**
 * Used exclusively by Storm Screamers, Archmages and Mystic Muses
 * @author LittleHakor
 */
public class NukerController extends MageController
{
	private static final int BATTLE_HEAL_ID = 1015;
	
	private byte _healingStyle;
	
	// Style 1: Heal very rarely when you are under 100% HP.
	private static final byte HEALING_STYLE_ONE = 0;
	
	// Style 2: Heal often when you are under 100% HP.
	private static final byte HEALING_STYLE_TWO = 1;
	
	// Style 3: Heal rarely when you are under 50% HP, very often when under 25.
	private static final byte HEALING_STYLE_THREE = 2;
	
	// Style 4: Heal continuously when you hit less than 15% HP.
	private static final byte HEALING_STYLE_FOUR = 3;
	
	/**
	 * [/This is used exclusively by Storm Screamers, Archmages and Mystic Muses...]
	 */
	
	public NukerController(final L2PcInstance player)
	{
		super(player);
		
		_healingStyle = (byte) Rnd.get(HEALING_STYLE_ONE, HEALING_STYLE_FOUR);
	}
	
	@Override
	protected int pickSpecialSkill(final L2Character target)
	{
		final int healthPercent = getPlayerHealthPercent();
		
		if (isSkillAvailable(BATTLE_HEAL_ID))
		{
			// Just heal up before attacking anyone if nobody is attacking you.
			// Otherwise we only heal up from time to time.
			if ((healthPercent < 100 && _knownAttackers.size() == 0))
				return BATTLE_HEAL_ID;
			
			switch (_healingStyle)
			{
				// Style 1: Heal very rarely when you are under 100% HP.
				case HEALING_STYLE_ONE:
				{
					if (healthPercent < 100 && Rnd.get(0, 10) == 0)
						return BATTLE_HEAL_ID;
					
					break;
				}
				// Style 2: Heal often when you are under 100% HP.
				case HEALING_STYLE_TWO:
				{
					if (healthPercent < 100 && Rnd.get(0, 5) == 0)
						return BATTLE_HEAL_ID;
					
					break;
				}
				// Style 3: Heal rarely when you are under 50% HP, very often when under 25.
				case HEALING_STYLE_THREE:
				{
					if (healthPercent < 50 && Rnd.get(0, 5) == 0)
						return BATTLE_HEAL_ID;
					else if (healthPercent < 25 && Rnd.nextBoolean())
						return BATTLE_HEAL_ID;
					
					break;
				}
				// Style 4: Heal continuously when you hit less than 15% HP.
				case HEALING_STYLE_FOUR:
				{
					if (healthPercent < 15)
						return BATTLE_HEAL_ID;
					
					break;
				}
			}
		}
		
		return super.pickSpecialSkill(target);
	}
}
