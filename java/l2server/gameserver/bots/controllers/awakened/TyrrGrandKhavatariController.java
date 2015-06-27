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
public class TyrrGrandKhavatariController extends TyrrController
{
	//Attack Skills
	private static final int RAGING_FORCE = 346;
	private static final int FORCE_STORM = 35;
	private static final int MOMENTUM_FLASH = 10327;

	//Toggle
	private static final int FURY_FISTS = 222;
	
	private static final int[] ATTACK_SKILL_IDS =
	{
		RAGING_FORCE,
		FORCE_STORM,
		MOMENTUM_FLASH
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		FURY_FISTS
	};
	
	public TyrrGrandKhavatariController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		return super.pickSpecialSkill(target);
	}
	
	@Override
	public final int[] getEssentialBuffSkillIds()
	{
		return ESSENTIAL_BUFF_SKILL_IDS;
	}
	
	@Override
	public int[] getAttackSkillIds()
	{
		return ATTACK_SKILL_IDS;
	}
}
