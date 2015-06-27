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
public class OthellWindRiderController extends OthellController
{
	// For 10 sec., has a 90% of evading melee and ranged P. Skill attacks. Requires a dagger or dual dagger.
	// Cooldown: 120s
	private static final int SHADOW_DODGE_ID = 10606;
		
	// Increases your Vital Spot Attack Rate by 30% for 2 minutes. Requires a dagger or dual dagger.
	// Cooldown: 150s
	private static final int MORTAL_STRIKE_ID = 410;
	
	// For 5 min., Critical Damage + 304 and vital spot attack success rate + 15%.
	// Front Critical Damage - 30%, Side Critical Rate + 30% and Critical Damage + 30%.
	// Rear Critical Rate + 50% and Critical Damage + 50%. Requires a dagger or dual dagger.
	// Cooldown: 30s
	private static final int CRITICAL_CHANCES_ID = 10609;
		
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		MORTAL_STRIKE_ID,
		CRITICAL_CHANCES_ID,
	};

	public OthellWindRiderController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		switch (_ultimatesUsageStyle)
		{
			// Style 1:
			// Use Shadow Dodge whenever possible.
			case ULTIMATES_USAGE_STYLE_ONE:
			{
				if (isSkillAvailable(SHADOW_DODGE_ID))
					return SHADOW_DODGE_ID;
				
				break;
			}
		}
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	public final int[] getEssentialBuffSkillIds()
	{
		return ESSENTIAL_BUFF_SKILL_IDS;
	}
}