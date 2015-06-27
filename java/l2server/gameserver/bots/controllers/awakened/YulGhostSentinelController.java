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
public class YulGhostSentinelController extends YulController
{
	// For 30 minutes when a bow or crossbow is equipped, P. Atk. + 10%, Skill Power + 10%, and received M. Critical Damage - 15%.
	// Cooldown: 30s
	private static final int HAWK_EYE_ID = 10903;
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		HAWK_EYE_ID
	};
	
	public YulGhostSentinelController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	public final int[] getEssentialBuffSkillIds()
	{
		return ESSENTIAL_BUFF_SKILL_IDS;
	}
}