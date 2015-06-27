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
public class WynnSpectralMasterController extends WynnController
{
	private static final int SUMMON_NIGHTSHADE = 11338;
	private static final int SUMMON_SPECTRAL_LORD = 11339;
	private static final int SUMMON_SOULLESS = 11340;
	
	private static final int SPECTRAL_RAGE = 11356;
	private static final int SUMMON_DEMONIC_CRUCIFIX = 11365;
	@SuppressWarnings("unused") // TODO
	private static final int GREATER_SERVITOR_MIGHT = 11349;
	
	private final static int[] DEBUFF_SKILL_IDS =
	{
		SUMMON_DEMONIC_CRUCIFIX
	};
	
	private final static int[] ATTACK_SKILL_IDS =
	{
		SPECTRAL_RAGE
	};
	
	private final static int[] SUMMON_SKILL_IDS =
	{
		SUMMON_NIGHTSHADE,
		SUMMON_SPECTRAL_LORD,
		SUMMON_SOULLESS
	};

	
	public WynnSpectralMasterController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		return super.pickSpecialSkill(target);
	}
	
	@Override
	public final int[] getDebuffSkillIds()
	{
		return DEBUFF_SKILL_IDS;
	}
	
	@Override
	public final int[] getAttackSkillIds()
	{
		return ATTACK_SKILL_IDS;
	}
	
	@Override
	public final int[] getSummonSkillIds()
	{
		return SUMMON_SKILL_IDS;
	}
}