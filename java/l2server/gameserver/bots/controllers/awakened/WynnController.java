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

import l2server.gameserver.bots.controllers.MageController;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * @author LittleHakor
 */
public class WynnController extends MageController
{
	@SuppressWarnings("unused") // TODO
	private static final int SUMMON_WHITE_WING = 8605;
	private static final int SUMMON_CUTE_BEAR = 11256;
	private static final int SUMMON_SABER_TOOTH_COUGAR = 11257;
	private static final int SUMMON_GRIM_REAPER = 11258;
	private static final int MARK_OF_WEAKNESS = 11259;
	private static final int MARK_OF_VOID = 11260;
	private static final int MARK_OF_PLAGUE = 11261;
	private static final int MARK_OF_TRICK = 11262;
	private static final int INVOKE = 11263;
	@SuppressWarnings("unused") // TODO
	private static final int STRONG_WILL = 11264;
	@SuppressWarnings("unused") // TODO
	private static final int SUMMON_TEAM_MATE = 11265;
	private static final int SUMMON_DEATH_GATE = 11266;
	@SuppressWarnings("unused") // TODO
	private static final int SPIRIT_OF_NAVIAROPE = 11267;
	@SuppressWarnings("unused") // TODO
	private static final int SUMMON_AVENGING_CUBIC = 11268;
	@SuppressWarnings("unused") // TODO
	private static final int MASS_SERVITOR_HEAL = 11269;
	private static final int MARK_RETRIEVER = 11271;
	@SuppressWarnings("unused") // TODO
	private static final int REPLACE = 11272;
	private static final int EXILE = 11273;
	@SuppressWarnings("unused") // TODO
	private static final int SUMMON_BARRIER = 11274;
	private static final int DIMENSIONAL_BINDING = 11276;
	@SuppressWarnings("unused") // TODO
	private static final int ULTIMATE_SERVITOR_SHARE = 11288;
	private static final int MASS_EXILE = 11296;
	@SuppressWarnings("unused") // TODO
	private static final int BLESSING_OF_THE_GIANTS = 11297;
	private static final int MARK_OF_FIRE = 11298;
	@SuppressWarnings("unused") // TODO
	private static final int SERVITOR_BALANCE_LIFE = 11299;
	@SuppressWarnings("unused") // TODO
	private static final int SERVITOR_MAJOR_HEAL = 11302;
	@SuppressWarnings("unused") // TODO
	private static final int MASS_SERVITOR_MAGIC_BARRIER = 11303;
	@SuppressWarnings("unused") // TODO
	private static final int MASS_SERVITOR_HASTE = 11304;
	@SuppressWarnings("unused") // TODO
	private static final int MASS_SERVITOR_SHIELD = 11305;
	@SuppressWarnings("unused") // TODO
	private static final int MASS_SERVITOR_EMPOWER = 11306;
	@SuppressWarnings("unused") // TODO
	private static final int MASS_SERVITOR_MIGHT = 11307;
	@SuppressWarnings("unused") // TODO
	private static final int MASS_SERVITOR_WIND_WALK = 11308;
	@SuppressWarnings("unused") // TODO
	private static final int MASS_SERVITOR_BLESSING = 11309;
	@SuppressWarnings("unused") // TODO
	private static final int MASS_SERVITOR_ULTIMATE_DEFENSE = 11310;
	@SuppressWarnings("unused") // TODO
	private static final int GREATER_SERVITOR_HASTE = 11347;
	@SuppressWarnings("unused") // TODO
	private static final int ARCANE_RAGE = 11350;
	
	//Toggles
	private static final int ULTIMATE_TRANSFER_PAIN_ID = 11270;
	private static final int DUAL_MAXIMUM_HP = 1986;
	private static final int WYN_FORCE_ID = 1937;
	
	private final static int[] COMBAT_TOGGLE_IDS =
	{
		WYN_FORCE_ID,
		DUAL_MAXIMUM_HP,
		ULTIMATE_TRANSFER_PAIN_ID
	};
	
	private final int[] ATTACK_SKILL_IDS =
	{
		INVOKE,
		MARK_OF_FIRE,
		MARK_RETRIEVER,
		EXILE,
		MASS_EXILE
	};
	
	private final int[] DEBUFF_SKILL_IDS =
	{
		MARK_OF_WEAKNESS,
		MARK_OF_VOID,
		MARK_OF_PLAGUE,
		MARK_OF_TRICK,
		SUMMON_DEATH_GATE,
		DIMENSIONAL_BINDING
	};
	
	private final static int[] SUMMON_SKILL_IDS =
	{
		SUMMON_CUTE_BEAR,
		SUMMON_SABER_TOOTH_COUGAR,
		SUMMON_GRIM_REAPER
	};
	
	public WynnController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected boolean isSummonAllowedToAssist()
	{
		return true;
	}
	
	@Override
	protected boolean isAllowedToSummonInCombat()
	{
		return true;
	}
	
	@Override
	protected int pickSpecialSkill(final L2Character target)
	{
		return super.pickSpecialSkill(target);
	}
	
	@Override
	public int[] getSummonSkillIds()
	{
		return SUMMON_SKILL_IDS;
	}
	
	@Override
	public int[] getDebuffSkillIds()
	{
		return DEBUFF_SKILL_IDS;
	}
	
	@Override
	public int[] getAttackSkillIds()
	{
		return ATTACK_SKILL_IDS;
	}
	
	@Override
	public final int[] getCombatToggles()
	{
		return COMBAT_TOGGLE_IDS;
	}
}