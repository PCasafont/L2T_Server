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
package l2tserver.gameserver.stats.effects;

import java.util.ArrayList;

import l2tserver.gameserver.model.L2Abnormal;
import l2tserver.gameserver.model.L2Effect;
import l2tserver.gameserver.model.L2ItemInstance;
import l2tserver.gameserver.model.L2Skill;
import l2tserver.gameserver.model.actor.L2Character;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.L2Summon;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.stats.Env;
import l2tserver.gameserver.templates.skills.L2AbnormalType;
import l2tserver.gameserver.templates.skills.L2EffectTemplate;
import l2tserver.gameserver.templates.skills.L2EffectType;
import l2tserver.util.Rnd;

/**
 * 
 * @author Kilian
 *
 */
public class EffectCancel extends L2Effect
{
	
	// Resistance given by each buff enchant level
	private final double ENCHANT_BENEFIT = 0.5;
	
	// Minimum cancellation chance
	private final int MIN_CANCEL_CHANCE = 0;
	
	// Whether the skill should depend on level difference
	private boolean LVL_DEPENDENT_PVP = true;
	private double 	PER_LVL_PENALTY = 5;
	
	// Skills which affect cancel land rate
	private final int ULTIMATE_DEFENSE_ID	= 110;
	private final int ULTIMATE_EVASION_ID	= 111;
	private final int LIONHEART_ID			= 287;
	private final int TOUCH_OF_LIFE_ID 		= 341;
	private final int VENGEANCE_ID			= 368;
	private final int ANGELIC_ICON_ID		= 406;
	private final int ZEALOT_ID				= 420;
	private final int DKULTIMATE_DEFENSE_ID	= 684;
	private final int EXCITING_ADV_ID		= 768;
	private final int WIND_RIDING_ID		= 769;
	private final int GHOST_WALKING_ID		= 770;
	private final int FLAME_ICON_ID			= 785;
	private final int TOUCH_OF_EVA_ID		= 787;
	private final int SERVITOR_EMP_ID		= 1299;
	private final int HARMONY_OF_NOBL_ID	= 1326;
	private final int SYMPHONY_OF_NOBL_ID	= 1327;
	private final int ARCANE_CHAOS_ID		= 1338;
	private final int ARCANE_PROTECTION_ID 	= 1354;
	private final int CHANT_OF_SPIRIT_ID 	= 1362;
	private final int HEROIC_VALOR_ID		= 1374;
	private final int MAGNUS_CHANT_ID		= 1413;
	private final int PAAGRIOS_EMBLEM_ID	= 1415;
	
	public EffectCancel(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.CANCEL;
	}
	
	/**
	 * 
	 * @see l2tserver.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		return cancel(getEffector(), getEffected(), getSkill());
	}
	
	private boolean cancel(L2Character caster, L2Character target, L2Skill skill)
	{
		// Remove charged shots from weapon if there is/are any
		dischargeShots(caster, skill);

		final int maxNegate = skill.getMaxNegatedEffects();	// Skill cancels up to this amount of buffs
		int rate = 25;
		
		L2PcInstance ctarget;

		// Only apply cancellation effect to characters
		if (!(target instanceof L2PcInstance))
			return false;
		
		ctarget = (L2PcInstance)target;
		
		// No effect on dead targets
		if (ctarget.isDead())
			return false;

		// Reference to the collection of target's buffs and debuffs
		final L2Abnormal[] effects = ctarget.getAllEffects();
		
		// Check target for cancel resistance buffs
		double vulnerability = determineVulnerability(effects);
		
		// Consider caster skill and target level
		if (LVL_DEPENDENT_PVP && caster instanceof L2PcInstance)
		{
			int magicLvl = skill.getMagicLevel();
			if (magicLvl <= 0)
				magicLvl = caster.getActingPlayer().getLevel();
			rate -= (ctarget.getActingPlayer().getLevel() - magicLvl) * PER_LVL_PENALTY;
		}
		
		if (rate < 0)
			rate = 0;

		// Feedback for active .landrates command
		if (caster instanceof L2PcInstance && caster.getActingPlayer().isLandRates())
		{
			caster.sendMessage("Your cancel effect has a base land rate of "+rate
									+". However, enchanted buffs reduce the individual chance.");
		}
		if (ctarget.getActingPlayer().isLandRates())
		{
			ctarget.sendMessage("The enemy's cancel effect has a base land rate of "+rate
								+". However, enchanted buffs reduce the individual chance.");
		}

		// Reduce the land rate in case resistance buffs were found
		rate /= vulnerability;
		
		// Call cancellation process depending on skill type
		if (rate > 0)
			generalBuffCancellation(caster ,effects, maxNegate, rate);

		return true;
	}
	
	private void dischargeShots(L2Character _activeChar, L2Skill _skill)
	{
		final L2ItemInstance weaponInst = _activeChar.getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (_skill.isMagic())
			{
				if (weaponInst.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				else if (weaponInst.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
					weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		else if (_activeChar instanceof L2Summon)
		{
			final L2Summon activeSummon = (L2Summon) _activeChar;
			
			if (_skill.isMagic())
			{
				if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		else if (_activeChar instanceof L2Npc)
		{
			((L2Npc)_activeChar)._soulshotcharged = false;
			((L2Npc)_activeChar)._spiritshotcharged = false;
		}
	}
	
	private void generalBuffCancellation(L2Character _activeChar, L2Abnormal[] _effects, int _maxNegate, int _rate)
	{
		ArrayList<L2Abnormal> buffs = new ArrayList<L2Abnormal>();
		
		// Filter buff-type effects out of the effect collection
		for (L2Abnormal effect : _effects)
		{
			if (effect.canBeStolen() || effect.getEffectMask() == L2EffectType.INVINCIBLE.getMask())
				buffs.add(effect);
		}
		
		// In case there are less than _maxNegate buffs available, it would cause multiple tries on same buff
		if (buffs.size() < _maxNegate)
			_maxNegate = buffs.size();
		
		int candidate = 0;
		for (int i = 0; i < _maxNegate; i++)
		{
			// Get a random buff index for cancellation try
			candidate = Rnd.get(buffs.size());
			
			// Save original rate temporarily
			int tempRate = _rate;

			// Reduce land rate depending on effect's enchant level
			if (buffs.get(candidate).getEnchantRouteId() > 0)
				_rate -= buffs.get(candidate).getEnchantLevel() * ENCHANT_BENEFIT;
			if (_rate < MIN_CANCEL_CHANCE)
				_rate = MIN_CANCEL_CHANCE;

			// More detailed .landrates feedback considering enchanted buffs
			if (_activeChar instanceof L2PcInstance && _activeChar.getActingPlayer().isLandRates())
			{
				_activeChar.sendMessage("Attempted to remove "+buffs.get(candidate).getSkill().getName()
										+" with "+_rate+"% chance.");
			}
			
			// Give it a try with _rate% chance
			if (Rnd.get(100) < _rate)
			{
				L2Abnormal buff = buffs.get(candidate);
				if (buff == null)
					return;
				buff.getEffected().onExitChanceEffect(buff.getSkill(), buff.getSkill().getElement());
				buff.exit();
				if (_activeChar instanceof L2PcInstance && _activeChar.getActingPlayer().isLandRates())
					_activeChar.sendMessage("Attempt to remove "+buff.getSkill().getName()+" succeeded.");
			}
			
			// Restore original rate
			_rate = tempRate;
			
			// Remove the reference to the canceled buffs from the collection to not try same again
			buffs.remove(candidate);
		}
	}
	
	private double determineVulnerability(L2Abnormal[] _effects)
	{
		double vulnerability = 1;
		
		// Run through target's buffs and check for cancel resistance buffs
		for (L2Abnormal buff : _effects)
		{
			// Ignore debuffs, only check buffs (one exception is Arcane Chaos)
			if (!buff.canBeStolen() && buff.getSkill().getId() != ARCANE_CHAOS_ID)
				continue;
			
			switch (buff.getSkill().getId())
			{
				case ARCANE_CHAOS_ID:
					vulnerability *= 0.7;
					break;
				case CHANT_OF_SPIRIT_ID:
				case ARCANE_PROTECTION_ID:
				case PAAGRIOS_EMBLEM_ID:
					vulnerability *= 1.3;
					break;
				case ANGELIC_ICON_ID:
				case ZEALOT_ID:
				case LIONHEART_ID:
				case FLAME_ICON_ID:
				case HEROIC_VALOR_ID:
				case MAGNUS_CHANT_ID:
					vulnerability *= 1.4;
					break;
				case TOUCH_OF_EVA_ID:
					vulnerability *= 1.6;
					break;
				case ULTIMATE_EVASION_ID:
				case ULTIMATE_DEFENSE_ID:
				case VENGEANCE_ID:
				case DKULTIMATE_DEFENSE_ID:
				case SERVITOR_EMP_ID:
				case HARMONY_OF_NOBL_ID:
				case SYMPHONY_OF_NOBL_ID:
					vulnerability *= 1.8;
					break;
				case EXCITING_ADV_ID:
				case WIND_RIDING_ID:
				case GHOST_WALKING_ID:
					vulnerability *= 1.9;
					break;
				case TOUCH_OF_LIFE_ID:
					vulnerability *= 2;
					break;
				default:
					break;
			}
		}
		
		return vulnerability;
	}
	
	/**
	 * 
	 * @see l2tserver.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
}
