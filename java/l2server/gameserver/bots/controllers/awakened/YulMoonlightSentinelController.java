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

import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * @author LittleHakor
 */
public class YulMoonlightSentinelController extends YulController
{
	// For 30 minutes when a bow or crossbow is equipped, P. Atk. + 10%, chance of receiving an M. Critical Hit - 15%, and P. Skill Cooldown - 10%.
	// Cooldown: 30s
	private static final int RAPID_FIRE_POSITION_ID = 10856;
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		RAPID_FIRE_POSITION_ID
	};
	
	public YulMoonlightSentinelController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	public final int[] getEssentialBuffSkillIds()
	{
		return ESSENTIAL_BUFF_SKILL_IDS;
	}
}