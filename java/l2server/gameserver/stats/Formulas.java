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

package l2server.gameserver.stats;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.instancemanager.*;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.*;
import l2server.gameserver.model.actor.instance.*;
import l2server.gameserver.model.base.PlayerClass;
import l2server.gameserver.model.base.PlayerState;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.entity.ClanHall;
import l2server.gameserver.model.entity.Fort;
import l2server.gameserver.model.entity.Siege;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.model.zone.type.L2CastleZone;
import l2server.gameserver.model.zone.type.L2ClanHallZone;
import l2server.gameserver.model.zone.type.L2FortZone;
import l2server.gameserver.model.zone.type.L2MotherTreeZone;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.conditions.ConditionLogicOr;
import l2server.gameserver.stats.conditions.ConditionPlayerState;
import l2server.gameserver.stats.conditions.ConditionUsingItemType;
import l2server.gameserver.stats.effects.EffectInvincible;
import l2server.gameserver.stats.funcs.Func;
import l2server.gameserver.templates.item.*;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectType;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;
import l2server.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

/**
 * Global calculations, can be modified by server admins
 */
public final class Formulas
{
	/**
	 * Regen Task period
	 */
	private static final int HP_REGENERATE_PERIOD = 3000; // 3 secs

	public static final byte SHIELD_DEFENSE_FAILED = 0; // no shield defense
	public static final byte SHIELD_DEFENSE_SUCCEED = 1; // normal shield defense
	public static final byte SHIELD_DEFENSE_PERFECT_BLOCK = 2; // perfect block

	public static final byte SKILL_REFLECT_FAILED = 0; // no reflect
	public static final byte SKILL_REFLECT_EFFECTS = 1; // normal reflect, some damage reflected some other not
	public static final byte SKILL_REFLECT_VENGEANCE = 2; // 100% of the damage affect both

	private static final byte MELEE_ATTACK_RANGE = 40;

	static class FuncAddLevel3 extends Func
	{
		static final FuncAddLevel3[] _instancies = new FuncAddLevel3[Stats.NUM_STATS];

		static Func getInstance(Stats stat)
		{
			int pos = stat.ordinal();
			if (_instancies[pos] == null)
			{
				_instancies[pos] = new FuncAddLevel3(stat);
			}
			return _instancies[pos];
		}

		private FuncAddLevel3(Stats pStat)
		{
			super(pStat, null);
		}

		@Override
		public int getOrder()
		{
			return 0x10;
		}

		@Override
		public void calc(Env env)
		{
			env.value += env.player.getLevel() / 3.0;
		}
	}

	static class FuncMultLevelMod extends Func
	{
		static final FuncMultLevelMod[] _instancies = new FuncMultLevelMod[Stats.NUM_STATS];

		static Func getInstance(Stats stat)
		{
			int pos = stat.ordinal();
			if (_instancies[pos] == null)
			{
				_instancies[pos] = new FuncMultLevelMod(stat);
			}
			return _instancies[pos];
		}

		private FuncMultLevelMod(Stats pStat)
		{
			super(pStat, null);
		}

		@Override
		public int getOrder()
		{
			return 0x20;
		}

		@Override
		public void calc(Env env)
		{
			env.value *= env.player.getLevelMod();
		}
	}

	static class FuncMultRegenResting extends Func
	{
		static final FuncMultRegenResting[] _instancies = new FuncMultRegenResting[Stats.NUM_STATS];

		/**
		 * Return the Func object corresponding to the state concerned.<BR><BR>
		 */
		static Func getInstance(Stats stat)
		{
			int pos = stat.ordinal();

			if (_instancies[pos] == null)
			{
				_instancies[pos] = new FuncMultRegenResting(stat);
			}

			return _instancies[pos];
		}

		/**
		 * Constructor of the FuncMultRegenResting.<BR><BR>
		 */
		private FuncMultRegenResting(Stats pStat)
		{
			super(pStat, null);
			setCondition(new ConditionPlayerState(PlayerState.RESTING, true));
		}

		@Override
		public int getOrder()
		{
			return 0x20;
		}

		/**
		 * Calculate the modifier of the state concerned.<BR><BR>
		 */
		@Override
		public void calc(Env env)
		{
			if (!cond.test(env))
			{
				return;
			}

			env.value *= 1.45;
		}
	}

	static class FuncPAtkMod extends Func
	{
		static final FuncPAtkMod _fpa_instance = new FuncPAtkMod();

		static Func getInstance()
		{
			return _fpa_instance;
		}

		private FuncPAtkMod()
		{
			super(Stats.PHYS_ATTACK, null);
		}

		@Override
		public int getOrder()
		{
			return 0x30;
		}

		@Override
		public void calc(Env env)
		{
			double strBonus = BaseStats.STR.calcBonus(env.player);
			double levelMod = env.player.getLevelMod();
			if (env.player instanceof L2PcInstance)
			{
				double chaBonus = BaseStats.CHA.calcBonus(env.player);
				env.value *= strBonus * levelMod * chaBonus;
			}
			else
			{
				boolean canReceiveBaseStatIncrease = true;
				if (env.player instanceof L2Npc)
				{
					canReceiveBaseStatIncrease = ((L2Npc) env.player).getTemplate().BonusFromBaseStats;
				}

				if (canReceiveBaseStatIncrease)
				{
					env.value *= strBonus * levelMod;
				}
			}
		}
	}

	static class FuncMAtkMod extends Func
	{
		static final FuncMAtkMod _fma_instance = new FuncMAtkMod();

		static Func getInstance()
		{
			return _fma_instance;
		}

		private FuncMAtkMod()
		{
			super(Stats.MAGIC_ATTACK, null);
		}

		@Override
		public int getOrder()
		{
			return 0x20;
		}

		@Override
		public void calc(Env env)
		{
			if (env.player instanceof L2PcInstance)
			{
				double intb = BaseStats.INT.calcBonus(env.player);
				double lvlb = env.player.getLevelMod();
				env.value *= lvlb * lvlb * (intb * intb) * BaseStats.CHA.calcBonus(env.player);
			}
			else
			{
				boolean canReceiveBaseStatIncrease = true;
				if (env.player instanceof L2Npc)
				{
					canReceiveBaseStatIncrease = ((L2Npc) env.player).getTemplate().BonusFromBaseStats;
				}

				if (canReceiveBaseStatIncrease)
				{
					float level = env.player.getLevel();
					double intb = BaseStats.INT.calcBonus(env.player);
					float lvlb = (level + 89) / 100;
					env.value *= lvlb * lvlb * (intb * intb);
				}
			}
		}
	}

	static class FuncMDefMod extends Func
	{
		static final FuncMDefMod _fmm_instance = new FuncMDefMod();

		static Func getInstance()
		{
			return _fmm_instance;
		}

		private FuncMDefMod()
		{
			super(Stats.MAGIC_DEFENSE, null);
		}

		@Override
		public int getOrder()
		{
			return 0x20;
		}

		@Override
		public void calc(Env env)
		{
			float level = env.player.getLevel();
			if (env.player instanceof L2PcInstance)
			{
				/*L2PcInstance p = (L2PcInstance) env.player;
                if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) != null)
					env.value -= 5;
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) != null)
					env.value -= 5;
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) != null)
					env.value -= 9;
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) != null)
					env.value -= 9;
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) != null)
					env.value -= 13;*/

				env.value *= BaseStats.MEN.calcBonus(env.player) * BaseStats.CHA.calcBonus(env.player) *
						env.player.getLevelMod();
			}
			else if (env.player instanceof L2PetInstance)
			{
				if (env.player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK) != 0)
				{
					env.value *= BaseStats.MEN.calcBonus(env.player);
				}
			}
			else
			{
				boolean canReceiveBaseStatIncrease = true;
				if (env.player instanceof L2Npc)
				{
					canReceiveBaseStatIncrease = ((L2Npc) env.player).getTemplate().BonusFromBaseStats;
				}

				if (canReceiveBaseStatIncrease)
				{
					env.value *= BaseStats.MEN.calcBonus(env.player) * ((level + 89) / 100);
				}
			}
		}
	}

	static class FuncPDefMod extends Func
	{
		static final FuncPDefMod _fmm_instance = new FuncPDefMod();

		static Func getInstance()
		{
			return _fmm_instance;
		}

		private FuncPDefMod()
		{
			super(Stats.PHYS_DEFENSE, null);
		}

		@Override
		public int getOrder()
		{
			return 0x20;
		}

		@Override
		public void calc(Env env)
		{
			if (env.player instanceof L2PcInstance)
			{
                /*L2PcInstance p = (L2PcInstance) env.player;
				boolean hasMagePDef = false;
				if (p.getCurrentClass() != null
						&& (p.getCurrentClass().isMage()
						|| p.getCurrentClass().getId() == 0x31)) // orc mystics are a special case
					hasMagePDef = true;
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) != null)
					env.value -= 12;
				L2ItemInstance chest = p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
				if (chest != null)
					env.value -= hasMagePDef ? 15 : 31;
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) != null || (chest != null && chest.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR))
					env.value -= hasMagePDef ? 8 : 18;
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) != null)
					env.value -= 8;
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) != null)
					env.value -= 7;*/

				env.value *= BaseStats.CHA.calcBonus(env.player) * env.player.getLevelMod();
			}
			else
			{
				boolean canReceiveBaseStatIncrease = true;
				if (env.player instanceof L2Npc)
				{
					canReceiveBaseStatIncrease = ((L2Npc) env.player).getTemplate().BonusFromBaseStats;
				}

				if (canReceiveBaseStatIncrease)
				{
					float level = env.player.getLevel();
					env.value *= (level + 89) / 100;
				}
			}
		}
	}

	static class FuncBowAtkRange extends Func
	{
		private static final FuncBowAtkRange _fbar_instance = new FuncBowAtkRange();

		static Func getInstance()
		{
			return _fbar_instance;
		}

		private FuncBowAtkRange()
		{
			super(Stats.POWER_ATTACK_RANGE, null);
			setCondition(new ConditionUsingItemType(L2WeaponType.BOW.mask()));
		}

		@Override
		public int getOrder()
		{
			return 0x10;
		}

		@Override
		public void calc(Env env)
		{
			if (!cond.test(env))
			{
				return;
			}
			// default is 40 and with bow should be 500
			env.value += 460;
		}
	}

	static class FuncCrossBowAtkRange extends Func
	{
		private static final FuncCrossBowAtkRange _fcb_instance = new FuncCrossBowAtkRange();

		static Func getInstance()
		{
			return _fcb_instance;
		}

		private FuncCrossBowAtkRange()
		{
			super(Stats.POWER_ATTACK_RANGE, null);
			ConditionLogicOr or = new ConditionLogicOr();
			or.add(new ConditionUsingItemType(L2WeaponType.CROSSBOW.mask()));
			or.add(new ConditionUsingItemType(L2WeaponType.CROSSBOWK.mask()));
			setCondition(or);
		}

		@Override
		public int getOrder()
		{
			return 0x10;
		}

		@Override
		public void calc(Env env)
		{
			if (!cond.test(env))
			{
				return;
			}
			// default is 40 and with crossbow should be 400
			env.value += 460;
		}
	}

	static class FuncAtkAccuracy extends Func
	{
		static final FuncAtkAccuracy _faa_instance = new FuncAtkAccuracy();

		static Func getInstance()
		{
			return _faa_instance;
		}

		private FuncAtkAccuracy()
		{
			super(Stats.ACCURACY_COMBAT, null);
		}

		@Override
		public int getOrder()
		{
			return 0x10;
		}

		@Override
		public void calc(Env env)
		{
			final int level = env.player.getLevel();
			env.value += Math.sqrt(env.player.getDEX()) * 6;
			env.value += level;
			if (level > 69)
			{
				env.value += level - 69;
			}
			if (env.player instanceof L2Summon)
			{
				env.value += level < 60 ? 4 : 5;
			}

			if (Config.isServer(Config.TENKAI) && level > 95 &&
					(env.player instanceof L2Attackable || env.player instanceof L2Summon))
			{
				env.value += level - 85;
			}
		}
	}

	static class FuncMAtkAccuracy extends Func
	{
		static final FuncMAtkAccuracy _fmaa_instance = new FuncMAtkAccuracy();

		static Func getInstance()
		{
			return _fmaa_instance;
		}

		private FuncMAtkAccuracy()
		{
			super(Stats.ACCURACY_MAGIC, null);
		}

		@Override
		public int getOrder()
		{
			return 0x10;
		}

		@Override
		public void calc(Env env)
		{
			final int level = env.player.getLevel();
			env.value += Math.sqrt(env.player.getWIT()) * 6;
			env.value += level;
			if (level > 53)
			{
				env.value += level - 53;
			}
			if (level > 69)
			{
				env.value += level - 69;
			}
			if (level > 85)
			{
				env.value += level - 85;
			}
			if (env.player instanceof L2Summon)
			{
				env.value += level < 60 ? 4 : 5;
			}

			if (Config.isServer(Config.TENKAI) && level > 95 &&
					(env.player instanceof L2Attackable || env.player instanceof L2Summon))
			{
				env.value += level - 85;
			}
		}
	}

	static class FuncAtkEvasion extends Func
	{
		static final FuncAtkEvasion _fae_instance = new FuncAtkEvasion();

		static Func getInstance()
		{
			return _fae_instance;
		}

		private FuncAtkEvasion()
		{
			super(Stats.P_EVASION_RATE, null);
		}

		@Override
		public int getOrder()
		{
			return 0x10;
		}

		@Override
		public void calc(Env env)
		{
			final int level = env.player.getLevel();
			if (env.player instanceof L2PcInstance)
			{
				//[Square(DEX)]*6 + lvl;
				env.value += Math.sqrt(env.player.getDEX()) * 6;
				env.value += level;
				if (level > 69)
				{
					env.value += level - 69;
				}
			}
			else
			{
				//[Square(DEX)]*6 + lvl;
				env.value += Math.sqrt(env.player.getDEX()) * 6;
				env.value += level;
				if (level > 69)
				{
					env.value += level - 69 + 2;
				}
			}
		}
	}

	static class FuncMAtkEvasion extends Func
	{
		static final FuncMAtkEvasion _fmae_instance = new FuncMAtkEvasion();

		static Func getInstance()
		{
			return _fmae_instance;
		}

		private FuncMAtkEvasion()
		{
			super(Stats.M_EVASION_RATE, null);
		}

		@Override
		public int getOrder()
		{
			return 0x10;
		}

		@Override
		public void calc(Env env)
		{
			final int level = env.player.getLevel();
			if (env.player instanceof L2PcInstance)
			{
				env.value += Math.sqrt(env.player.getWIT()) * 6;
				env.value += level;
				if (level > 53)
				{
					env.value += level - 53;
				}
				if (level > 69)
				{
					env.value += level - 69;
				}
				if (level > 85)
				{
					env.value += level - 85;
				}
			}
			else
			{
				env.value += Math.sqrt(env.player.getWIT()) * 6;
				env.value += level;
				if (level > 53)
				{
					env.value += level - 53 + 1;
				}
				if (level > 69)
				{
					env.value += level - 69 + 1;
				}
				if (level > 85)
				{
					env.value += level - 85 + 1;
				}
			}
		}
	}

	static class FuncAtkCritical extends Func
	{
		static final FuncAtkCritical _fac_instance = new FuncAtkCritical();

		static Func getInstance()
		{
			return _fac_instance;
		}

		private FuncAtkCritical()
		{
			super(Stats.CRITICAL_RATE, null);
		}

		@Override
		public int getOrder()
		{
			return 0x09;
		}

		@Override
		public void calc(Env env)
		{
			boolean canReceiveBaseStatIncrease = true;
			if (env.player instanceof L2Npc)
			{
				canReceiveBaseStatIncrease = ((L2Npc) env.player).getTemplate().BonusFromBaseStats;
			}

			if (canReceiveBaseStatIncrease)
			{
				env.value *= BaseStats.DEX.calcBonus(env.player);
				env.value *= 10;
				env.baseValue = env.value;
			}
		}
	}

	static class FuncMAtkCritical extends Func
	{
		static final FuncMAtkCritical _fac_instance = new FuncMAtkCritical();

		static Func getInstance()
		{
			return _fac_instance;
		}

		private FuncMAtkCritical()
		{
			super(Stats.MCRITICAL_RATE, null);
		}

		@Override
		public int getOrder()
		{
			return 0x09;
		}

		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.WIT.calcBonus(env.player);
			env.baseValue = env.value;
		}
	}

	static class FuncPAtkSpeed extends Func
	{
		static final FuncPAtkSpeed _fas_instance = new FuncPAtkSpeed();

		static Func getInstance()
		{
			return _fas_instance;
		}

		private FuncPAtkSpeed()
		{
			super(Stats.POWER_ATTACK_SPEED, null);
		}

		@Override
		public int getOrder()
		{
			return 0x20;
		}

		@Override
		public void calc(Env env)
		{
			boolean canReceiveBaseStatIncrease = true;
			if (env.player instanceof L2Npc)
			{
				canReceiveBaseStatIncrease = ((L2Npc) env.player).getTemplate().BonusFromBaseStats;
			}

			if (canReceiveBaseStatIncrease)
			{
				env.value *= BaseStats.DEX.calcBonus(env.player) * BaseStats.CHA.calcBonus(env.player);
			}
		}
	}

	static class FuncMAtkSpeed extends Func
	{
		static final FuncMAtkSpeed _fas_instance = new FuncMAtkSpeed();

		static Func getInstance()
		{
			return _fas_instance;
		}

		private FuncMAtkSpeed()
		{
			super(Stats.MAGIC_ATTACK_SPEED, null);
		}

		@Override
		public int getOrder()
		{
			return 0x20;
		}

		@Override
		public void calc(Env env)
		{
			boolean canReceiveBaseStatIncrease = true;
			if (env.player instanceof L2Npc)
			{
				canReceiveBaseStatIncrease = ((L2Npc) env.player).getTemplate().BonusFromBaseStats;
			}

			if (canReceiveBaseStatIncrease)
			{
				env.value *= BaseStats.WIT.calcBonus(env.player) * BaseStats.CHA.calcBonus(env.player);
			}
		}
	}

	static class FuncHennaSTR extends Func
	{
		static final FuncHennaSTR _fh_instance = new FuncHennaSTR();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaSTR()
		{
			super(Stats.STAT_STR, null);
		}

		@Override
		public int getOrder()
		{
			return 0x10;
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatSTR();
			}
		}
	}

	static class FuncHennaDEX extends Func
	{
		static final FuncHennaDEX _fh_instance = new FuncHennaDEX();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaDEX()
		{
			super(Stats.STAT_DEX, null);
		}

		@Override
		public int getOrder()
		{
			return 0x10;
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatDEX();
			}
		}
	}

	static class FuncHennaINT extends Func
	{
		static final FuncHennaINT _fh_instance = new FuncHennaINT();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaINT()
		{
			super(Stats.STAT_INT, null);
		}

		@Override
		public int getOrder()
		{
			return 0x10;
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatINT();
			}
		}
	}

	static class FuncHennaMEN extends Func
	{
		static final FuncHennaMEN _fh_instance = new FuncHennaMEN();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaMEN()
		{
			super(Stats.STAT_MEN, null);
		}

		@Override
		public int getOrder()
		{
			return 0x10;
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatMEN();
			}
		}
	}

	static class FuncHennaCON extends Func
	{
		static final FuncHennaCON _fh_instance = new FuncHennaCON();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaCON()
		{
			super(Stats.STAT_CON, null);
		}

		@Override
		public int getOrder()
		{
			return 0x10;
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatCON();
			}
		}
	}

	static class FuncHennaWIT extends Func
	{
		static final FuncHennaWIT _fh_instance = new FuncHennaWIT();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaWIT()
		{
			super(Stats.STAT_WIT, null);
		}

		@Override
		public int getOrder()
		{
			return 0x10;
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatWIT();
			}
		}
	}

	static class FuncHennaLUC extends Func
	{
		static final FuncHennaLUC _fh_instance = new FuncHennaLUC();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaLUC()
		{
			super(Stats.STAT_LUC, null);
		}

		@Override
		public int getOrder()
		{
			return 0x10;
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatLUC();
			}
		}
	}

	static class FuncHennaCHA extends Func
	{
		static final FuncHennaCHA _fh_instance = new FuncHennaCHA();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaCHA()
		{
			super(Stats.STAT_CHA, null);
		}

		@Override
		public int getOrder()
		{
			return 0x10;
		}

		@Override
		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatCHA();
			}
		}
	}

	static class FuncMaxHpMul extends Func
	{
		static final FuncMaxHpMul _fmhm_instance = new FuncMaxHpMul();

		static Func getInstance()
		{
			return _fmhm_instance;
		}

		private FuncMaxHpMul()
		{
			super(Stats.MAX_HP, null);
		}

		@Override
		public int getOrder()
		{
			return 0x20;
		}

		@Override
		public void calc(Env env)
		{
			boolean canReceiveBaseStatIncrease = true;
			if (env.player instanceof L2Npc)
			{
				canReceiveBaseStatIncrease = ((L2Npc) env.player).getTemplate().BonusFromBaseStats;
			}

			if (canReceiveBaseStatIncrease)
			{
				env.value *= BaseStats.CON.calcBonus(env.player) * BaseStats.CHA.calcBonus(env.player);
			}
		}
	}

	static class FuncMaxCpMul extends Func
	{
		static final FuncMaxCpMul _fmcm_instance = new FuncMaxCpMul();

		static Func getInstance()
		{
			return _fmcm_instance;
		}

		private FuncMaxCpMul()
		{
			super(Stats.MAX_CP, null);
		}

		@Override
		public int getOrder()
		{
			return 0x20;
		}

		@Override
		public void calc(Env env)
		{
			boolean canReceiveBaseStatIncrease = true;
			if (env.player instanceof L2Npc)
			{
				canReceiveBaseStatIncrease = ((L2Npc) env.player).getTemplate().BonusFromBaseStats;
			}

			if (canReceiveBaseStatIncrease)
			{
				env.value *= BaseStats.CON.calcBonus(env.player) * BaseStats.CHA.calcBonus(env.player);
			}
		}
	}

	static class FuncMaxMpMul extends Func
	{
		static final FuncMaxMpMul _fmmm_instance = new FuncMaxMpMul();

		static Func getInstance()
		{
			return _fmmm_instance;
		}

		private FuncMaxMpMul()
		{
			super(Stats.MAX_MP, null);
		}

		@Override
		public int getOrder()
		{
			return 0x20;
		}

		@Override
		public void calc(Env env)
		{
			boolean canReceiveBaseStatIncrease = true;
			if (env.player instanceof L2Npc)
			{
				canReceiveBaseStatIncrease = ((L2Npc) env.player).getTemplate().BonusFromBaseStats;
			}

			if (canReceiveBaseStatIncrease)
			{
				env.value *= BaseStats.CHA.calcBonus(env.player);
			}
		}
	}

	/**
	 * Return the period between 2 regenerations task (3s for L2Character, 5 min for L2DoorInstance).<BR><BR>
	 */
	public static int getRegeneratePeriod(L2Character cha)
	{
		if (cha instanceof L2DoorInstance)
		{
			return HP_REGENERATE_PERIOD * 100; // 5 mins
		}

		return HP_REGENERATE_PERIOD; // 3s
	}

	/**
	 * Return the standard NPC Calculator set containing ACCURACY_COMBAT and P_EVASION_RATE.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...).
	 * In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <BR><BR>
	 * <p>
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR><BR>
	 * <p>
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 */
	public static Calculator[] getStdNPCCalculators()
	{
		Calculator[] std = new Calculator[Stats.NUM_STATS];

		std[Stats.MAX_HP.ordinal()] = new Calculator();
		std[Stats.MAX_HP.ordinal()].addFunc(FuncMaxHpMul.getInstance());

		std[Stats.MAX_MP.ordinal()] = new Calculator();
		std[Stats.MAX_MP.ordinal()].addFunc(FuncMaxMpMul.getInstance());

		std[Stats.PHYS_ATTACK.ordinal()] = new Calculator();
		std[Stats.PHYS_ATTACK.ordinal()].addFunc(FuncPAtkMod.getInstance());

		std[Stats.MAGIC_ATTACK.ordinal()] = new Calculator();
		std[Stats.MAGIC_ATTACK.ordinal()].addFunc(FuncMAtkMod.getInstance());

		std[Stats.PHYS_DEFENSE.ordinal()] = new Calculator();
		std[Stats.PHYS_DEFENSE.ordinal()].addFunc(FuncPDefMod.getInstance());

		std[Stats.MAGIC_DEFENSE.ordinal()] = new Calculator();
		std[Stats.MAGIC_DEFENSE.ordinal()].addFunc(FuncMDefMod.getInstance());

		std[Stats.CRITICAL_RATE.ordinal()] = new Calculator();
		std[Stats.CRITICAL_RATE.ordinal()].addFunc(FuncAtkCritical.getInstance());

		std[Stats.MCRITICAL_RATE.ordinal()] = new Calculator();
		std[Stats.MCRITICAL_RATE.ordinal()].addFunc(FuncMAtkCritical.getInstance());

		std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
		std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());

		std[Stats.ACCURACY_MAGIC.ordinal()] = new Calculator();
		std[Stats.ACCURACY_MAGIC.ordinal()].addFunc(FuncMAtkAccuracy.getInstance());

		std[Stats.P_EVASION_RATE.ordinal()] = new Calculator();
		std[Stats.P_EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());

		std[Stats.M_EVASION_RATE.ordinal()] = new Calculator();
		std[Stats.M_EVASION_RATE.ordinal()].addFunc(FuncMAtkEvasion.getInstance());

		std[Stats.POWER_ATTACK_SPEED.ordinal()] = new Calculator();
		std[Stats.POWER_ATTACK_SPEED.ordinal()].addFunc(FuncPAtkSpeed.getInstance());

		std[Stats.MAGIC_ATTACK_SPEED.ordinal()] = new Calculator();
		std[Stats.MAGIC_ATTACK_SPEED.ordinal()].addFunc(FuncMAtkSpeed.getInstance());

		std[Stats.P_EVASION_RATE.ordinal()] = new Calculator();
		std[Stats.P_EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());

		std[Stats.M_EVASION_RATE.ordinal()] = new Calculator();
		std[Stats.M_EVASION_RATE.ordinal()].addFunc(FuncMAtkEvasion.getInstance());

		return std;
	}

	/**
	 * Add basics Func objects to L2PcInstance and L2Summon.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...).
	 * In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <BR><BR>
	 * <p>
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR><BR>
	 *
	 * @param cha L2PcInstance or L2Summon that must obtain basic Func objects
	 */
	public static void addFuncsToNewCharacter(L2Character cha)
	{
		if (cha instanceof L2PcInstance)
		{
			cha.addStatFunc(FuncMaxHpMul.getInstance());
			cha.addStatFunc(FuncMaxCpMul.getInstance());
			cha.addStatFunc(FuncMaxMpMul.getInstance());
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_CP_RATE));
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncBowAtkRange.getInstance());
			cha.addStatFunc(FuncCrossBowAtkRange.getInstance());
			//cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.PHYS_ATTACK));
			//cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.PHYS_DEFENSE));
			//cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.MAGIC_DEFENSE));
			cha.addStatFunc(FuncPAtkMod.getInstance());
			cha.addStatFunc(FuncMAtkMod.getInstance());
			cha.addStatFunc(FuncPDefMod.getInstance());
			cha.addStatFunc(FuncMDefMod.getInstance());
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncMAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncMAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
			cha.addStatFunc(FuncMAtkEvasion.getInstance());
			cha.addStatFunc(FuncPAtkSpeed.getInstance());
			cha.addStatFunc(FuncMAtkSpeed.getInstance());

			cha.addStatFunc(FuncHennaSTR.getInstance());
			cha.addStatFunc(FuncHennaDEX.getInstance());
			cha.addStatFunc(FuncHennaINT.getInstance());
			cha.addStatFunc(FuncHennaMEN.getInstance());
			cha.addStatFunc(FuncHennaCON.getInstance());
			cha.addStatFunc(FuncHennaWIT.getInstance());

			cha.addStatFunc(FuncHennaLUC.getInstance());
			cha.addStatFunc(FuncHennaCHA.getInstance());
		}
		else if (cha instanceof L2Summon)
		{
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncMAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
			cha.addStatFunc(FuncMAtkEvasion.getInstance());
		}
	}

	/**
	 * Calculate the HP regen rate (base + modifiers).<BR>
	 * <BR>
	 */
	public static double calcHpRegen(L2Character cha)
	{
		double init = cha.getTemplate().getBaseHpReg(cha.getLevel());
		double hpRegenMultiplier = cha.isRaid() ? Config.RAID_HP_REGEN_MULTIPLIER : Config.HP_REGEN_MULTIPLIER;
		double hpRegenBonus = 0;

		if (Config.L2JMOD_CHAMPION_ENABLE && cha.isChampion())
		{
			hpRegenMultiplier *= Config.L2JMOD_CHAMPION_HP_REGEN;
		}

		if (cha instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) cha;

			// Calculate correct baseHpReg value for certain level of PC
			init += player.getLevel() > 10 ? (player.getLevel() - 1) / 10.0 : 0.5;

			double siegeModifier = calcSiegeRegenModifer(player);
			if (siegeModifier > 0)
			{
				hpRegenMultiplier *= siegeModifier;
			}

			if (player.isInsideZone(L2Character.ZONE_CLANHALL) && player.getClan() != null &&
					player.getClan().getHasHideout() > 0)
			{
				L2ClanHallZone zone = ZoneManager.getInstance().getZone(player, L2ClanHallZone.class);
				int posChIndex = zone == null ? -1 : zone.getClanHallId();
				int clanHallIndex = player.getClan().getHasHideout();
				if (clanHallIndex > 0 && clanHallIndex == posChIndex)
				{
					ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
					if (clansHall != null)
					{
						if (clansHall.getFunction(ClanHall.FUNC_RESTORE_HP) != null)
						{
							hpRegenMultiplier *=
									1 + (double) clansHall.getFunction(ClanHall.FUNC_RESTORE_HP).getLvl() / 100;
						}
					}
				}
			}

			if (player.isInsideZone(L2Character.ZONE_CASTLE) && player.getClan() != null &&
					player.getClan().getHasCastle() > 0)
			{
				L2CastleZone zone = ZoneManager.getInstance().getZone(player, L2CastleZone.class);
				int posCastleIndex = zone == null ? -1 : zone.getCastleId();
				int castleIndex = player.getClan().getHasCastle();
				if (castleIndex > 0 && castleIndex == posCastleIndex)
				{
					Castle castle = CastleManager.getInstance().getCastleById(castleIndex);
					if (castle != null)
					{
						if (castle.getFunction(Castle.FUNC_RESTORE_HP) != null)
						{
							hpRegenMultiplier *= 1 + (double) castle.getFunction(Castle.FUNC_RESTORE_HP).getLvl() / 100;
						}
					}
				}
			}

			if (player.isInsideZone(L2Character.ZONE_FORT) && player.getClan() != null &&
					player.getClan().getHasFort() > 0)
			{
				L2FortZone zone = ZoneManager.getInstance().getZone(player, L2FortZone.class);
				int posFortIndex = zone == null ? -1 : zone.getFortId();
				int fortIndex = player.getClan().getHasFort();
				if (fortIndex > 0 && fortIndex == posFortIndex)
				{
					Fort fort = FortManager.getInstance().getFortById(fortIndex);
					if (fort != null)
					{
						if (fort.getFunction(Fort.FUNC_RESTORE_HP) != null)
						{
							hpRegenMultiplier *= 1 + (double) fort.getFunction(Fort.FUNC_RESTORE_HP).getLvl() / 100;
						}
					}
				}
			}

			// Mother Tree effect is calculated at last
			if (player.isInsideZone(L2Character.ZONE_MOTHERTREE))
			{
				L2MotherTreeZone zone = ZoneManager.getInstance().getZone(player, L2MotherTreeZone.class);
				int hpBonus = zone == null ? 0 : zone.getHpRegenBonus();
				hpRegenBonus += hpBonus;
			}

			// Calculate Movement bonus
			if (player.isSitting())
			{
				hpRegenMultiplier *= 1.5; // Sitting
			}
			else if (!player.isMoving())
			{
				hpRegenMultiplier *= 1.1; // Staying
			}
			else if (player.isRunning())
			{
				hpRegenMultiplier *= 0.7; // Running
			}

			// Add CON bonus
			init *= cha.getLevelMod() * BaseStats.CON.calcBonus(cha);
		}
		else if (cha instanceof L2PetInstance)
		{
			init = ((L2PetInstance) cha).getPetLevelData().getPetRegenHP() * Config.PET_HP_REGEN_MULTIPLIER;
		}

		if (init < 1)
		{
			init = 1;
		}

		return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null) * hpRegenMultiplier + hpRegenBonus;
	}

	/**
	 * Calculate the MP regen rate (base + modifiers).<BR><BR>
	 */
	public static double calcMpRegen(L2Character cha)
	{
		double init = cha.getTemplate().getBaseMpReg(cha.getLevel());
		double mpRegenMultiplier = cha.isRaid() ? Config.RAID_MP_REGEN_MULTIPLIER : Config.MP_REGEN_MULTIPLIER;
		double mpRegenBonus = 0;

		if (cha instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) cha;

			// Calculate correct baseMpReg value for certain level of PC
			init += 0.3 * ((player.getLevel() - 1) / 10.0);

			// Mother Tree effect is calculated at last'
			if (player.isInsideZone(L2Character.ZONE_MOTHERTREE))
			{
				L2MotherTreeZone zone = ZoneManager.getInstance().getZone(player, L2MotherTreeZone.class);
				int mpBonus = zone == null ? 0 : zone.getMpRegenBonus();
				mpRegenBonus += mpBonus;
			}

			if (player.isInsideZone(L2Character.ZONE_CLANHALL) && player.getClan() != null &&
					player.getClan().getHasHideout() > 0)
			{
				L2ClanHallZone zone = ZoneManager.getInstance().getZone(player, L2ClanHallZone.class);
				int posChIndex = zone == null ? -1 : zone.getClanHallId();
				int clanHallIndex = player.getClan().getHasHideout();
				if (clanHallIndex > 0 && clanHallIndex == posChIndex)
				{
					ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
					if (clansHall != null)
					{
						if (clansHall.getFunction(ClanHall.FUNC_RESTORE_MP) != null)
						{
							mpRegenMultiplier *=
									1 + (double) clansHall.getFunction(ClanHall.FUNC_RESTORE_MP).getLvl() / 100;
						}
					}
				}
			}

			if (player.isInsideZone(L2Character.ZONE_CASTLE) && player.getClan() != null &&
					player.getClan().getHasCastle() > 0)
			{
				L2CastleZone zone = ZoneManager.getInstance().getZone(player, L2CastleZone.class);
				int posCastleIndex = zone == null ? -1 : zone.getCastleId();
				int castleIndex = player.getClan().getHasCastle();
				if (castleIndex > 0 && castleIndex == posCastleIndex)
				{
					Castle castle = CastleManager.getInstance().getCastleById(castleIndex);
					if (castle != null)
					{
						if (castle.getFunction(Castle.FUNC_RESTORE_MP) != null)
						{
							mpRegenMultiplier *= 1 + (double) castle.getFunction(Castle.FUNC_RESTORE_MP).getLvl() / 100;
						}
					}
				}
			}

			if (player.isInsideZone(L2Character.ZONE_FORT) && player.getClan() != null &&
					player.getClan().getHasFort() > 0)
			{
				L2FortZone zone = ZoneManager.getInstance().getZone(player, L2FortZone.class);
				int posFortIndex = zone == null ? -1 : zone.getFortId();
				int fortIndex = player.getClan().getHasFort();
				if (fortIndex > 0 && fortIndex == posFortIndex)
				{
					Fort fort = FortManager.getInstance().getFortById(fortIndex);
					if (fort != null)
					{
						if (fort.getFunction(Fort.FUNC_RESTORE_MP) != null)
						{
							mpRegenMultiplier *= 1 + (double) fort.getFunction(Fort.FUNC_RESTORE_MP).getLvl() / 100;
						}
					}
				}
			}

			// Calculate Movement bonus
			if (player.isSitting())
			{
				mpRegenMultiplier *= 1.5; // Sitting
			}
			else if (!player.isMoving())
			{
				mpRegenMultiplier *= 1.1; // Staying
			}
			else if (player.isRunning())
			{
				mpRegenMultiplier *= 0.7; // Running
			}

			if (Config.isServer(Config.TENKAI) && player.getPvpFlag() == 0 && !player.isInCombat() &&
					!player.isMoving() && !player.isPlayingEvent() && !player.isInOlympiadMode())
			{
				init *= 100;
			}
		}
		else if (cha instanceof L2PetInstance)
		{
			init = ((L2PetInstance) cha).getPetLevelData().getPetRegenMP() * Config.PET_MP_REGEN_MULTIPLIER;
		}

		if (init < 1)
		{
			init = 1;
		}

		return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null) * mpRegenMultiplier + mpRegenBonus;
	}

	/**
	 * Calculate the CP regen rate (base + modifiers).<BR><BR>
	 */
	public static double calcCpRegen(L2Character cha)
	{
		double init = cha.getTemplate().getBaseCpReg(cha.getLevel());
		double cpRegenMultiplier = Config.CP_REGEN_MULTIPLIER;
		double cpRegenBonus = 0;

		if (cha instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) cha;

			// Calculate correct baseHpReg value for certain level of PC
			init += player.getLevel() > 10 ? (player.getLevel() - 1) / 10.0 : 0.5;

			// Calculate Movement bonus
			if (player.isSitting())
			{
				cpRegenMultiplier *= 1.5; // Sitting
			}
			else if (!player.isMoving())
			{
				cpRegenMultiplier *= 1.1; // Staying
			}
			else if (player.isRunning())
			{
				cpRegenMultiplier *= 0.7; // Running
			}
		}
		else
		{
			// Calculate Movement bonus
			if (!cha.isMoving())
			{
				cpRegenMultiplier *= 1.1; // Staying
			}
			else if (cha.isRunning())
			{
				cpRegenMultiplier *= 0.7; // Running
			}
		}

		// Apply CON bonus
		init *= cha.getLevelMod() * BaseStats.CON.calcBonus(cha);
		if (init < 1)
		{
			init = 1;
		}

		return cha.calcStat(Stats.REGENERATE_CP_RATE, init, null, null) * cpRegenMultiplier + cpRegenBonus;
	}

	public static double calcSiegeRegenModifer(L2PcInstance activeChar)
	{
		if (activeChar == null || activeChar.getClan() == null)
		{
			return 0;
		}

		Siege siege = SiegeManager.getInstance()
				.getSiege(activeChar.getPosition().getX(), activeChar.getPosition().getY(),
						activeChar.getPosition().getZ());
		if (siege == null || !siege.getIsInProgress())
		{
			return 0;
		}

		L2SiegeClan siegeClan = siege.getAttackerClan(activeChar.getClan().getClanId());
		if (siegeClan == null || siegeClan.getFlag().isEmpty() ||
				!Util.checkIfInRange(200, activeChar, siegeClan.getFlag().get(0), true))
		{
			return 0;
		}

		return 1.5; // If all is true, then modifer will be 50% more
	}

	/**
	 * Calculated damage caused by ATTACK of attacker on target,
	 * called separatly for each weapon, if dual-weapon is used.
	 *
	 * @param attacker player or NPC that makes ATTACK
	 * @param target   player or NPC, target of ATTACK
	 * @param shld     one of ATTACK_XXX constants
	 * @param crit     if the ATTACK have critical success
	 * @param dual     if dual weapon is used
	 * @param ssBonus  if weapon item was charged by soulshot
	 * @return damage points
	 */
	public static double calcPhysDam(L2Character attacker, L2Character target, byte shld, boolean crit, boolean dual, double ssBonus)
	{
		final boolean isPvP = attacker instanceof L2Playable && target instanceof L2Playable;
		final boolean isPvE = attacker instanceof L2Playable && target instanceof L2Attackable ||
				attacker instanceof L2Attackable && target instanceof L2Playable;

		double pAtk = attacker.getPAtk(target);
		double pDef = target.getPDef(attacker);

		pAtk += calcValakasAttribute(attacker, target, null);

		if (attacker instanceof L2Npc)
		{
			if (((L2Npc) attacker)._soulshotcharged)
			{
				ssBonus = L2ItemInstance.CHARGED_SOULSHOT;
			}
			else
			{
				ssBonus = L2ItemInstance.CHARGED_NONE;
			}

			((L2Npc) attacker)._soulshotcharged = false;
		}

		switch (shld)
		{
			case SHIELD_DEFENSE_SUCCEED:
				pDef += target.getShldDef();
				break;
			case SHIELD_DEFENSE_PERFECT_BLOCK: // perfect block
				return 1.;
		}

		// defence modifier depending of the attacker weapon
		L2Weapon weapon = attacker.getActiveWeaponItem();
		Stats weaponStat = null;
		boolean isBow = false;
		boolean isDual = false;
		if (weapon != null && !attacker.isTransformed())
		{
			switch (weapon.getItemType())
			{
				case BOW:
					isBow = true;
					weaponStat = Stats.BOW_WPN_VULN;
					break;
				case CROSSBOW:
				case CROSSBOWK:
					isBow = true;
					weaponStat = Stats.CROSSBOW_WPN_VULN;
					break;
				case BLUNT:
					weaponStat = Stats.BLUNT_WPN_VULN;
					break;
				case DUALBLUNT:
					isDual = true;
					weaponStat = Stats.BLUNT_WPN_VULN;
					break;
				case DAGGER:
					weaponStat = Stats.DAGGER_WPN_VULN;
					break;
				case DUAL:
					isDual = true;
					weaponStat = Stats.DUAL_WPN_VULN;
					break;
				case DUALFIST:
					isDual = true;
					weaponStat = Stats.DUALFIST_WPN_VULN;
					break;
				case ETC:
					weaponStat = Stats.ETC_WPN_VULN;
					break;
				case FIST:
					weaponStat = Stats.FIST_WPN_VULN;
					break;
				case POLE:
					weaponStat = Stats.POLE_WPN_VULN;
					break;
				case SWORD:
					weaponStat = Stats.SWORD_WPN_VULN;
					break;
				case BIGSWORD:
					weaponStat = Stats.BIGSWORD_WPN_VULN;
					break;
				case BIGBLUNT:
					weaponStat = Stats.BIGBLUNT_WPN_VULN;
					break;
				case DUALDAGGER:
					isDual = true;
					weaponStat = Stats.DUALDAGGER_WPN_VULN;
					break;
				case RAPIER:
					weaponStat = Stats.RAPIER_WPN_VULN;
					break;
				case ANCIENTSWORD:
					weaponStat = Stats.ANCIENT_WPN_VULN;
					break;
			}
		}

		if (isDual && ssBonus >= 2.0)
		{
			ssBonus *= 1.02;
		}

		// for summon use pet weapon vuln, since they can't hold weapon
		if (attacker instanceof L2SummonInstance)
		{
			weaponStat = Stats.PET_WPN_VULN;
		}

		// Calculate the weakness to the weapon
		if (weaponStat != null)
		{
			pAtk = target.calcStat(weaponStat, pAtk, target, null);
		}

		if (target instanceof L2Npc)
		{
			switch (((L2Npc) target).getTemplate().getRace())
			{
				case BEAST:
					pAtk *= attacker.getPAtkMonsters(target);
					break;
				case ANIMAL:
					pAtk *= attacker.getPAtkAnimals(target);
					break;
				case PLANT:
					pAtk *= attacker.getPAtkPlants(target);
					break;
				case DRAGON:
					pAtk *= attacker.getPAtkDragons(target);
					break;
				case BUG:
					pAtk *= attacker.getPAtkInsects(target);
					break;
				case GIANT:
					pAtk *= attacker.getPAtkGiants(target);
					break;
				case MAGICCREATURE:
					pAtk *= attacker.getPAtkMagicCreatures(target);
					break;
				default:
					// nothing
					break;
			}
		}

		double critBonus = 1.0;
		double critStaticBonus = 0.0;
		if (crit)
		{
			critBonus = 2.0;
			pAtk = attacker.calcStat(Stats.CRITICAL_ATTACK, pAtk, target, null);

			if (isInFrontOf(target, attacker))
			{
				critBonus = attacker.calcStat(Stats.CRITICAL_DMG_FRONT, critBonus, target, null);
			}
			else if (isBehind(target, attacker))
			{
				critBonus = attacker.calcStat(Stats.CRITICAL_DMG_BEHIND, critBonus, target, null);
			}
			else
			{
				critBonus = attacker.calcStat(Stats.CRITICAL_DMG_SIDE, critBonus, target, null);
			}

			critBonus = attacker.getPCriticalDamage(target, critBonus, null);
			critStaticBonus = attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, critStaticBonus, target, null);

			critBonus = target.calcStat(Stats.CRIT_VULN, critBonus, attacker, null);
			critStaticBonus = target.calcStat(Stats.CRIT_ADD_VULN, critStaticBonus, attacker, null);
		}

		double positionBonus = 0.0;
		if (attacker.isBehind(target))
		{
			positionBonus = 0.20; // Behind bonus
		}
		else if (!attacker.isInFrontOf(target))
		{
			positionBonus = 0.05; // Side bonus
		}

		// Weapon random damage
		double weaponRandom = attacker.getRandomDamageMultiplier();

		double finalBonus = 1.0;
		// Dmg/def bonusses in PvP fight
		if (isPvP)
		{
			finalBonus *= attacker.getPvPPhysicalDamage(target) / target.getPvPPhysicalDefense(attacker);
		}

		if (isPvE)
		{
			if (isBow)
			{
				finalBonus *= attacker.calcStat(Stats.PVE_BOW_DMG, 1, target, null);
			}

			finalBonus *= attacker.getPvEPhysicalDamage(target) / target.getPvEPhysicalDefense(attacker);
		}

		//Eviscerator damage multiplier
		if (attacker.calcStat(Stats.NORMAL_ATK_DMG, 1, target, null) > 1)
		{
			finalBonus = attacker.calcStat(Stats.NORMAL_ATK_DMG, finalBonus, target, null);

			//Damage Up is a 1sec buff that should affects only one hit but we can do more hits in one sec so..
			L2Abnormal ab = attacker.getFirstEffect(30521);
			if (ab != null)
			{
				ab.exit();
			}
		}

		finalBonus *= calcElemental(attacker, target, null);

		if (target instanceof L2Attackable)
		{
			if (!target.isRaid() && !target.isRaidMinion() && target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY &&
					attacker.getActingPlayer() != null &&
					target.getLevel() - attacker.getActingPlayer().getLevel() >= 2)
			{
				int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
				if (crit)
				{
					if (lvlDiff > Config.NPC_CRIT_DMG_PENALTY.size())
					{
						finalBonus *= Config.NPC_CRIT_DMG_PENALTY.get(Config.NPC_CRIT_DMG_PENALTY.size());
					}
					else
					{
						finalBonus *= Config.NPC_CRIT_DMG_PENALTY.get(lvlDiff);
					}
				}
				else
				{
					if (lvlDiff > Config.NPC_DMG_PENALTY.size())
					{
						finalBonus *= Config.NPC_DMG_PENALTY.get(Config.NPC_DMG_PENALTY.size());
					}
					else
					{
						finalBonus *= Config.NPC_DMG_PENALTY.get(lvlDiff);
					}
				}
			}
		}

		if (Config.isServer(Config.TENKAI) && attacker.getActingPlayer() != null)
		{
			if (pAtk > 100000)
			{
				pAtk = 100000 + Math.pow(pAtk - 100000, 0.8);
			}
			if (critBonus > 3)
			{
				critBonus = 2 + Math.pow(critBonus - 2, 0.55);
			}
		}

		double damage = 77.0 * (weaponRandom * (pAtk * critBonus + pAtk * positionBonus) + critStaticBonus) / pDef *
				finalBonus * ssBonus;
		//if (isBow)
		//	damage = (70.0 * ((pAtk + pAtk * critBonus) * weaponRandom + critStaticBonus + positionBonus * (pAtk + pAtk)) / pDef) * finalBonus;

		//System.out.println(pAtk + " | " + finalBonus
		//		+ " | " + ssBonus + " | " + critBonus + " | " + damage);

		damage = calcCustomModifier(attacker, target, damage);

		if (Config.isServer(Config.TENKAI) && isPvP && damage > 10000)
		{
			damage = 10000 + Math.pow(damage - 10000, 0.9);
		}

		int dmgCap = (int) target.getStat().calcStat(Stats.DAMAGE_CAP, 0, null, null);
		if (dmgCap > 0 && damage > dmgCap)
		{
			damage = dmgCap;
		}

		if (damage > 0 && damage < 1)
		{
			damage = 1;
		}
		else if (damage < 0)
		{
			damage = 0;
		}

		if (Config.isServer(Config.TENKAI) && isPvP && damage > 10000 && pDef > 5000)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(
						"INSERT INTO log_damage" + "(attacker, target, attackerClass, targetClass, damageType, " +
								"attack, defense, levelMod, power, powerBonus, critBonus, " +
								"critStaticBonus, positionBonus, ssBonus, finalBonus, damage)" + "VALUES" +
								"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				statement.setString(1, attacker.getName());
				statement.setString(2, target.getName());
				statement.setInt(3, attacker.getActingPlayer().getClassId());
				statement.setInt(4, target.getActingPlayer().getClassId());
				statement.setString(5, "PHYSICAL");
				statement.setDouble(6, pAtk);
				statement.setDouble(7, pDef);
				statement.setDouble(8, 1);
				statement.setDouble(9, 0);
				statement.setDouble(10, 1);
				statement.setDouble(11, critBonus);
				statement.setDouble(12, critStaticBonus);
				statement.setDouble(13, positionBonus);
				statement.setDouble(14, ssBonus);
				statement.setDouble(15, finalBonus);
				statement.setDouble(16, damage);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}

		return damage;
	}

	/**
	 * Calculated damage caused by SKILL ATTACK of attacker on target,
	 * called separatly for each weapon, if dual-weapon is used.
	 *
	 * @param attacker player or NPC that makes ATTACK
	 * @param target   player or NPC, target of ATTACK
	 * @param skill    The skill used to deal damage
	 * @param crit     if the ATTACK have critical success
	 * @param dual     if dual weapon is used
	 * @return damage points
	 */
	public static double calcPhysSkillDam(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean crit, boolean dual, double ssBonus)
	{
		final boolean isPvP = attacker instanceof L2Playable && target instanceof L2Playable;
		final boolean isPvE = attacker instanceof L2Playable && target instanceof L2Attackable ||
				attacker instanceof L2Attackable && target instanceof L2Playable;

		double pAtk = attacker.getPAtk(target);
		double levelMod = attacker.getLevelMod();
		double pDef = target.getPDef(attacker);

		pAtk += calcValakasAttribute(attacker, target, skill);

		if (attacker instanceof L2Npc)
		{
			if (((L2Npc) attacker)._soulshotcharged)
			{
				ssBonus = L2ItemInstance.CHARGED_SOULSHOT;
			}
			else
			{
				ssBonus = L2ItemInstance.CHARGED_NONE;
			}

			((L2Npc) attacker)._soulshotcharged = false;
		}

		switch (shld)
		{
			case SHIELD_DEFENSE_SUCCEED:
				pDef += target.getShldDef();
				break;
			case SHIELD_DEFENSE_PERFECT_BLOCK: // perfect block
				return 1.;
		}

		if (skill.getIgnoredDefPercent() > 0)
		{
			pDef -= pDef * skill.getIgnoredDefPercent() / 100;
		}

		double power = skill.getPower(attacker, target, isPvP, isPvE);
		double powerBonus = 1.0;
		if (skill.getMaxChargeConsume() > 0)
		{
			if (attacker instanceof L2PcInstance)
			{
				int consume = Math.min(((L2PcInstance) attacker).getCharges(), skill.getMaxChargeConsume());
				for (int i = 0; i < consume; i++)
				{
					powerBonus += 0.1;
				}

				((L2PcInstance) attacker).decreaseCharges(consume);
			}
			powerBonus = attacker.calcStat(Stats.MOMENTUM_POWER, powerBonus, attacker, skill);
		}

		if (skill.getId() == 10265 || skill.getId() == 10288) //Eruption, Hurricane Storm
		{
			L2Weapon attackerWeapon = attacker.getActiveWeaponItem();
			if (attackerWeapon != null)
			{
				//Power - 10% when using a sword, dualsword, blunt, or fist weapon. Power + 50% when using a spear.
				if (attackerWeapon.getItemType() == L2WeaponType.SWORD ||
						attackerWeapon.getItemType() == L2WeaponType.BIGSWORD ||
						attackerWeapon.getItemType() == L2WeaponType.DUAL ||
						attackerWeapon.getItemType() == L2WeaponType.FIST ||
						attackerWeapon.getItemType() == L2WeaponType.BLUNT ||
						attackerWeapon.getItemType() == L2WeaponType.DUALBLUNT ||
						attackerWeapon.getItemType() == L2WeaponType.BIGBLUNT)
				{
					powerBonus *= 0.90;
				}
				else if (attackerWeapon.getItemType() == L2WeaponType.POLE)
				{
					powerBonus *= 1.50;
				}
			}
		}

		// defence modifier depending of the attacker weapon
		L2Weapon weapon = attacker.getActiveWeaponItem();
		Stats weaponStat = null;
		boolean isBow = false;
		boolean isDual = false;
		if (weapon != null && !attacker.isTransformed())
		{
			switch (weapon.getItemType())
			{
				case BOW:
					isBow = true;
					weaponStat = Stats.BOW_WPN_VULN;
					break;
				case CROSSBOW:
				case CROSSBOWK:
					isBow = true;
					weaponStat = Stats.CROSSBOW_WPN_VULN;
					break;
				case BLUNT:
					weaponStat = Stats.BLUNT_WPN_VULN;
					break;
				case DUALBLUNT:
					isDual = true;
					weaponStat = Stats.BLUNT_WPN_VULN;
					break;
				case DAGGER:
					weaponStat = Stats.DAGGER_WPN_VULN;
					break;
				case DUAL:
					isDual = true;
					weaponStat = Stats.DUAL_WPN_VULN;
					break;
				case DUALFIST:
					isDual = true;
					weaponStat = Stats.DUALFIST_WPN_VULN;
					break;
				case ETC:
					weaponStat = Stats.ETC_WPN_VULN;
					break;
				case FIST:
					weaponStat = Stats.FIST_WPN_VULN;
					break;
				case POLE:
					weaponStat = Stats.POLE_WPN_VULN;
					break;
				case SWORD:
					weaponStat = Stats.SWORD_WPN_VULN;
					break;
				case BIGSWORD:
					weaponStat = Stats.BIGSWORD_WPN_VULN;
					break;
				case BIGBLUNT:
					weaponStat = Stats.BIGBLUNT_WPN_VULN;
					break;
				case DUALDAGGER:
					isDual = true;
					weaponStat = Stats.DUALDAGGER_WPN_VULN;
					break;
				case RAPIER:
					weaponStat = Stats.RAPIER_WPN_VULN;
					break;
				case ANCIENTSWORD:
					weaponStat = Stats.ANCIENT_WPN_VULN;
					break;
			}
		}

		if (isDual && ssBonus >= 2.0)
		{
			ssBonus *= 1.02;
		}

		// for summon use pet weapon vuln, since they can't hold weapon
		if (attacker instanceof L2SummonInstance)
		{
			weaponStat = Stats.PET_WPN_VULN;
		}

		if (weaponStat != null)
		{
			// get the vulnerability due to skills (buffs, passives, toggles, etc)
			pAtk = target.calcStat(weaponStat, pAtk, target, skill);
		}

		if (target instanceof L2Npc)
		{
			switch (((L2Npc) target).getTemplate().getRace())
			{
				case BEAST:
					pAtk *= attacker.getPAtkMonsters(target);
					break;
				case ANIMAL:
					pAtk *= attacker.getPAtkAnimals(target);
					break;
				case PLANT:
					pAtk *= attacker.getPAtkPlants(target);
					break;
				case DRAGON:
					pAtk *= attacker.getPAtkDragons(target);
					break;
				case BUG:
					pAtk *= attacker.getPAtkInsects(target);
					break;
				case GIANT:
					pAtk *= attacker.getPAtkGiants(target);
					break;
				case MAGICCREATURE:
					pAtk *= attacker.getPAtkMagicCreatures(target);
					break;
				default:
					// nothing
					break;
			}
		}

		double critBonus = 1.0;
		if (crit)
		{
			critBonus = 2.0 * attacker.calcStat(Stats.PSKILL_CRIT_DMG, 1, target, skill);
		}

		double positionBonus = 0.0;
		if (attacker.isBehind(target))
		{
			positionBonus = 0.20; // Behind bonus
		}
		else if (!attacker.isInFrontOf(target))
		{
			positionBonus = 0.05; // Side bonus
		}

		// Weapon random damage
		double weaponRandom = attacker.getRandomDamageMultiplier();

		double skillDmgBonus = 1.0;
		// Dmg/def bonusses in PvP fight
		if (isPvP)
		{
			skillDmgBonus *= attacker.getPvPPhysicalSkillDamage(target) / target.getPvPPhysicalSkillDefense(attacker);
		}

		if (isPvE)
		{
			if (isBow)
			{
				skillDmgBonus *= attacker.calcStat(Stats.PVE_BOW_SKILL_DMG, 1, target, skill);
			}

			skillDmgBonus *= attacker.getPvEPhysicalSkillDamage(target) / target.getPvEPhysicalSkillDefense(attacker);
		}

		// Physical skill dmg boost
		skillDmgBonus *= attacker.calcStat(Stats.PHYSICAL_SKILL_POWER, 1, target, skill);
		if (Config.isServer(Config.TENKAI) && attacker.getActingPlayer() != null && skillDmgBonus > 2)
		{
			skillDmgBonus = 1 + Math.pow(skillDmgBonus - 1, 0.6);
		}

		double finalBonus = skillDmgBonus;
		finalBonus *= calcElemental(attacker, target, skill);

		if (target instanceof L2Attackable)
		{
			if (!target.isRaid() && !target.isRaidMinion() && target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY &&
					attacker.getActingPlayer() != null &&
					target.getLevel() - attacker.getActingPlayer().getLevel() >= 2)
			{
				int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
				if (lvlDiff > Config.NPC_SKILL_DMG_PENALTY.size())
				{
					finalBonus *= Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size());
				}
				else
				{
					finalBonus *= Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
				}
			}
		}

		if (skill.getId() == 30500 && target.isStunned()) // Lateral hit
		{
			finalBonus *= 1.20;
		}

		if (Config.isServer(Config.TENKAI) && attacker.getActingPlayer() != null)
		{
			if (pAtk > 100000)
			{
				pAtk = 100000 + Math.pow(pAtk - 100000, 0.8);
			}
			if (critBonus > 2)
			{
				critBonus = 1 + Math.pow(critBonus - 1, 0.55);
			}
			if (finalBonus > 2)
			{
				finalBonus = 1 + Math.pow(finalBonus - 1, 0.6);
			}
		}

		double damage = 77.0 * ((pAtk * levelMod + power) * powerBonus * weaponRandom + positionBonus * pAtk) / pDef *
				finalBonus * ssBonus * critBonus;
		//if (isBow)
		{
			//if (ssBonus >= 2.0)
			//	ssBonus *= 0.75;
			//damage = (70.0 * (pAtk * levelMod + pAtk + critBonus * power) * powerBonus * weaponRandom * (1 + positionBonus) / pDef) * finalBonus * ssBonus;
		}

		damage = calcCustomModifier(attacker, target, damage);

		if (Config.isServer(Config.TENKAI) && isPvP && damage > 10000)
		{
			damage = 10000 + Math.pow(damage - 10000, 0.9);
		}

		//if (isPvP)
		//System.out.println(skill.getName() + ": " + pAtk + " | " + levelMod
		//		+ " | " + skill.getPower() + " | " + powerBonus + " | " + finalBonus
		//		+ " | " + ssBonus + " | " + critBonus + " -> " + damage);

		int dmgCap = (int) target.getStat().calcStat(Stats.DAMAGE_CAP, 0, null, null);
		if (dmgCap > 0 && damage > dmgCap)
		{
			damage = dmgCap;
		}

		if (damage > 0 && damage < 1)
		{
			damage = 1;
		}
		else if (damage < 0)
		{
			damage = 0;
		}

		if (Config.isServer(Config.TENKAI) && target instanceof L2PcInstance && attacker instanceof L2PcInstance &&
				damage > 10000 && pDef > 5000)
		{
			Connection con = null;
			try
			{
				//if (damage > 30000)
				//	System.out.println(attacker + " " + damage);
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(
						"INSERT INTO log_damage" + "(attacker, target, attackerClass, targetClass, damageType, " +
								"attack, defense, levelMod, power, powerBonus, critBonus, " +
								"critStaticBonus, positionBonus, ssBonus, finalBonus, damage)" + "VALUES" +
								"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				statement.setString(1, attacker.getName());
				statement.setString(2, target.getName());
				statement.setInt(3, attacker.getActingPlayer().getClassId());
				statement.setInt(4, target.getActingPlayer().getClassId());
				statement.setString(5, "PSKILL");
				statement.setDouble(6, pAtk);
				statement.setDouble(7, pDef);
				statement.setDouble(8, levelMod);
				statement.setDouble(9, power);
				statement.setDouble(10, powerBonus);
				statement.setDouble(11, critBonus);
				statement.setDouble(12, 0);
				statement.setDouble(13, positionBonus);
				statement.setDouble(14, ssBonus);
				statement.setDouble(15, finalBonus);
				statement.setDouble(16, damage);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}

		return damage;
	}

	public static double calcBlowDamage(L2Character attacker, L2Character target, L2Skill skill, byte shld, double ssBonus)
	{
		/*
		if (attacker instanceof L2NpcInstance && ((L2NpcInstance)attacker).getOwner() != null)
			attacker = ((L2NpcInstance)attacker).getOwner();
		else if (attacker instanceof L2GuardInstance && ((L2GuardInstance)attacker).getOwner() != null)
			attacker = ((L2GuardInstance)attacker).getOwner();
		 */

		double pAtk = attacker.calcStat(Stats.CRITICAL_ATTACK, attacker.getPAtk(target), target, skill);
		double levelMod = attacker.getLevelMod();
		double power = skill.getPower(target instanceof L2Playable, target instanceof L2Attackable);
		double pDef = target.getPDef(attacker);

		pAtk += calcValakasAttribute(attacker, target, skill);

		switch (shld)
		{
			case SHIELD_DEFENSE_SUCCEED:
				pDef += target.getShldDef();
				break;
			case SHIELD_DEFENSE_PERFECT_BLOCK: // perfect block
				return 1;
		}

		if (skill.getId() == 10510 && target.getFirstEffect("bleeding") != null) // Chain Blow
		{
			power *= 1.20;
		}

		double critBonus = attacker.calcStat(Stats.PSKILL_CRIT_DMG, 1, target, skill);
		critBonus = attacker.calcStat(Stats.CRITICAL_DAMAGE, critBonus, target, skill);
		double critStaticBonus = attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 1, target, null);
		critBonus = target.calcStat(Stats.CRIT_VULN, critBonus, target, skill);
		critStaticBonus = target.calcStat(Stats.CRIT_ADD_VULN, critStaticBonus, target, skill);

		if (isInFrontOf(target, attacker))
		{
			critBonus = attacker.calcStat(Stats.CRITICAL_DMG_FRONT, critBonus, target, null);
		}
		else if (isBehind(target, attacker))
		{
			critBonus = attacker.calcStat(Stats.CRITICAL_DMG_BEHIND, critBonus, target, null);
		}
		else
		{
			critBonus = attacker.calcStat(Stats.CRITICAL_DMG_SIDE, critBonus, target, null);
		}

		// get the vulnerability for the instance due to skills (buffs, passives, toggles, etc)
		pAtk = target.calcStat(Stats.DAGGER_WPN_VULN, pAtk, target, null);

		double positionBonus = 0.0;
		if (attacker.isBehind(target))
		{
			positionBonus = 0.20; // Behind bonus
		}
		else if (!attacker.isInFrontOf(target))
		{
			positionBonus = 0.05; // Side bonus
		}

		// Random weapon damage
		double weaponRandom = attacker.getRandomDamageMultiplier();

		double finalBonus = 1;
		if (attacker instanceof L2Playable && target instanceof L2Playable)
		{
			// Dmg bonuses in PvP fight
			finalBonus *= attacker.getPvPPhysicalSkillDamage(target);
			// Def bonuses in PvP fight
			finalBonus /= target.getPvPPhysicalSkillDefense(attacker);
		}

		finalBonus *= calcElemental(attacker, target, skill);

		if (target instanceof L2Attackable && !target.isRaid() && !target.isRaidMinion() &&
				target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY && attacker.getActingPlayer() != null &&
				target.getLevel() - attacker.getActingPlayer().getLevel() >= 2)
		{
			int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
			if (lvlDiff > Config.NPC_SKILL_DMG_PENALTY.size())
			{
				finalBonus *= Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size());
			}
			else
			{
				finalBonus *= Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
			}
		}

		if (Config.isServer(Config.TENKAI) && attacker.getActingPlayer() != null)
		{
			if (pAtk > 100000)
			{
				pAtk = 100000 + Math.pow(pAtk - 100000, 0.8);
			}
			if (critBonus > 5)
			{
				critBonus = 4 + Math.pow(critBonus - 4, 0.65);
			}
		}

		double damage = 77.0 *
				((pAtk * levelMod + power) * weaponRandom * 0.666 * critBonus * ssBonus + critStaticBonus * 6 +
						positionBonus * (power + pAtk * ssBonus)) / pDef * finalBonus;

		//System.out.println(attacker.getName());
		//System.out.println(skill.getName() + ": " + skill.getPower() + " | " + positionBonus + " | " + finalBonus
		//		+ " | " + ssBonus + " | " + critBonus + " | " + damage);

		damage = calcCustomModifier(attacker, target, damage);

		if (Config.isServer(Config.TENKAI) && damage > 20000 && attacker.getActingPlayer() != null &&
				target.getActingPlayer() != null)
		{
			damage = 20000 + Math.pow(damage - 20000, 0.93);
		}

		if (attacker instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) attacker;
			player.sendSysMessage(
					"Formula = (77 * ((pAtk * levelMod + power) * weaponRandom * 0.666 * critBonus * ssBonus + critStaticBonus * 6" +
							" + positionBonus * (power + pAtk * ssBonus)) / pDef) * finalBonus");
			player.sendSysMessage("P.Atk = " + pAtk + ".");
			player.sendSysMessage("Level Mod = " + attacker.getLevelMod() + ".");
			player.sendSysMessage("Power = " + power + ".");
			player.sendSysMessage("Soulshots Multiplier = " + ssBonus + ".");
			player.sendSysMessage("Final Multiplier = " + finalBonus + ".");
			player.sendSysMessage("CritDamageModifier = " + critBonus + ".");
			player.sendSysMessage("PositionBonus = " + positionBonus + ".");
			//player.sendSysMessage("WeaponVuln = " + weaponVuln + ".");
			player.sendSysMessage("CritDamageAdd = " + critStaticBonus + ".");
			player.sendSysMessage("Defense = " + pDef + ".");
			player.sendSysMessage(".......");
		}

		if (Config.isServer(Config.TENKAI) && target instanceof L2PcInstance && attacker instanceof L2PcInstance &&
				damage > 10000 && pDef > 5000)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(
						"INSERT INTO log_damage" + "(attacker, target, attackerClass, targetClass, damageType, " +
								"attack, defense, levelMod, power, powerBonus, critBonus, " +
								"critStaticBonus, positionBonus, ssBonus, finalBonus, damage)" + "VALUES" +
								"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				statement.setString(1, attacker.getName());
				statement.setString(2, target.getName());
				statement.setInt(3, attacker.getActingPlayer().getClassId());
				statement.setInt(4, target.getActingPlayer().getClassId());
				statement.setString(5, "BLOW");
				statement.setDouble(6, pAtk);
				statement.setDouble(7, pDef);
				statement.setDouble(8, levelMod);
				statement.setDouble(9, power);
				statement.setDouble(10, 1);
				statement.setDouble(11, critBonus);
				statement.setDouble(12, critStaticBonus);
				statement.setDouble(13, positionBonus);
				statement.setDouble(14, ssBonus);
				statement.setDouble(15, finalBonus);
				statement.setDouble(16, damage);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}

		return damage;
	}

	/*
	public static final double calcCustomPAtkModifier(final L2Character attacker, final L2Character target, double damage)
	{
		// Support for monsters dealing more damages when the attacker have multiple players on his back.
		if (attacker instanceof L2MonsterInstance && target instanceof L2Playable)
		{
			final L2MonsterInstance monster = (L2MonsterInstance) attacker;
			final L2PcInstance player = target.getActingPlayer();

			// The following monsters hit for 90% of the target HP, unless Sigel.
			switch (monster.getNpcId())
			{
				case 50000:
				case 50001:
				{
					if (player.getClassId() >= 148 && player.getClassId() <= 151)
					{
						// Use 10% of the P.Atk against Sigels
						damage *= 0.10;
					}
					if (player.getClassId() >= 152 && player.getClassId() <= 157)
					{
						// Use 200% of the P.Atk against Tyrrs
						if (damage > player.getMaxHp() * 0.02)
							damage *= 0.10;
					}


					break;
				}
				case 22957: // Garden Warrior
				case 22956: // Garden Commander
				case 23160: // Garden Chief Priest
				case 22948: // Garden Scout
				case 22947: // Garden Sentry
				{
					if (player.getClassId() >= 148 && player.getClassId() <= 151)
					{
						// Sigels gets hit for 10% of their HP maximum.
						if (damage > player.getMaxHp() * 0.10)
							damage = player.getMaxHp() * 0.10;
					}
					else
					{
						// Other classes gets hit for 70% of their HP minimum.
						if (damage < player.getMaxHp() * 0.70)
							damage = player.getMaxHp() * 0.70;

						// And 90% of their HP maximum.
						if (damage > player.getMaxHp() * 0.90)
							damage = player.getMaxHp() * 0.90;
					}
					break;
				}
				default:
					break;
			}

			int hatersAmount = ((L2Playable) target).getActingPlayer().getHatersAmount();

			if (monster.getTemplate().HatersDamageMultiplier != 0 && hatersAmount != 0)
			{
				float multiplier = monster.getTemplate().HatersDamageMultiplier * hatersAmount;

				if (multiplier > 1)
					damage *= monster.getTemplate().HatersDamageMultiplier * hatersAmount;
				target.sendMessage("Damages multiplied by " + monster.getTemplate().HatersDamageMultiplier * hatersAmount + ". " + monster.getTemplate().getName() + " Mul = " + monster.getTemplate().HatersDamageMultiplier + ", Haters Amount = " + (hatersAmount + 1));
			}
			else
				target.sendMessage("Damages were not increased. " + monster.getTemplate().getName() + " Mul = " + monster.getTemplate().HatersDamageMultiplier + ", Haters Amount = " + (hatersAmount + 1));
		}

		return damage;
	}*/

	public static double calcCustomModifier(final L2Character attacker, final L2Character target, double damage)
	{
		damage *= damageMultiplier(attacker, target);
		return damage;
	}

	public static double calcMagicDam(L2Character attacker, L2Character target, L2Skill skill, byte shld, double ssMul, boolean mcrit)
	{
		final boolean isPvP = attacker instanceof L2Playable && target instanceof L2Playable;
		final boolean isPvE = attacker instanceof L2Playable && target instanceof L2Attackable ||
				attacker instanceof L2Attackable && target instanceof L2Playable;

		double mAtk = attacker.getMAtk(target, skill);
		double mDef = target.getMDef(attacker, skill);

		// AI SpiritShot
		if (attacker instanceof L2Npc)
		{
			if (((L2Npc) attacker)._spiritshotcharged)
			{
				ssMul = L2ItemInstance.CHARGED_SPIRITSHOT;
			}
			else
			{
				ssMul = L2ItemInstance.CHARGED_NONE;
			}

			((L2Npc) attacker)._spiritshotcharged = false;
		}

		switch (shld)
		{
			case SHIELD_DEFENSE_SUCCEED:
				mDef += target.getShldDef(); // kamael
				break;
			case SHIELD_DEFENSE_PERFECT_BLOCK: // perfect block
				return 1;
		}

		mAtk *= ssMul;

		if (mcrit)
		{
			mAtk = attacker.calcStat(Stats.MAGIC_CRIT_ATTACK, mAtk, target, skill);
		}

		boolean missed = false;
		boolean halfMiss = false;
		// Failure calculation
		if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill))
		{
			final double failureModifier = attacker.calcStat(Stats.MAGIC_FAILURE_RATE, 1, target, skill);
			if (target.getLevel() - attacker.getLevel() > 9 || failureModifier > 100)
			{
				if (attacker instanceof L2PcInstance)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
					sm.addCharName(target);
					sm.addSkillName(skill);
					attacker.sendPacket(sm);
				}

				missed = true;
			}
			else
			{
				if (attacker instanceof L2PcInstance)
				{
					if (skill.getSkillType() == L2SkillType.DRAIN)
					{
						attacker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DRAIN_HALF_SUCCESFUL));
					}
					else
					{
						attacker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
					}
				}

				missed = true;
				halfMiss = true;
			}
		}

		if (missed)
		{
			if (target instanceof L2PcInstance)
			{
				if (skill.getSkillType() == L2SkillType.DRAIN)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_DRAIN);
					sm.addCharName(attacker);
					target.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_MAGIC);
					sm.addCharName(attacker);
					target.sendPacket(sm);
				}
			}
		}

		double critBonus = 1.0;
		double staticCritBonus = 0.0;
		if (!missed && mcrit)
		{
			critBonus *= 2;
			critBonus = attacker.calcStat(Stats.MAGIC_CRIT_DMG, critBonus, target, skill);
			staticCritBonus = attacker.calcStat(Stats.MAGIC_CRIT_DMG_ADD, staticCritBonus, target, skill);
			critBonus = target.calcStat(Stats.MAGIC_CRIT_VULN, critBonus, attacker, skill);
		}

		// Weapon random damage
		double weaponRandom = attacker.getRandomDamageMultiplier();

		double finalBonus = 1.0;
		// Pvp bonuses for dmg/def
		if (isPvP)
		{
			finalBonus *= attacker.getPvPMagicDamage(target) / target.getPvPMagicDefense(attacker);
		}

		if (isPvE)
		{
			finalBonus *= attacker.getPvEMagicDamage(target) / target.getPvEMagicDefense(attacker);
		}

		finalBonus *= attacker.calcStat(Stats.MAGIC_SKILL_POWER, 1, target, skill);
		// CT2.3 general magic vuln
		finalBonus *= target.calcStat(Stats.MAGIC_DAMAGE_VULN, 1, attacker, skill);

		finalBonus *= calcElemental(attacker, target, skill);

		if (target instanceof L2Attackable)
		{
			if (!target.isRaid() && !target.isRaidMinion() && target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY &&
					attacker.getActingPlayer() != null &&
					target.getLevel() - attacker.getActingPlayer().getLevel() >= 2)
			{
				int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
				if (lvlDiff > Config.NPC_SKILL_DMG_PENALTY.size())
				{
					finalBonus *= Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size());
				}
				else
				{
					finalBonus *= Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
				}
			}
		}

		if (skill.getDependOnTargetEffectId().length > 0 &&
				skill.getDamageDepend().length > skill.getDependOnTargetEffectId().length)
		{
			int debuffsFound = 0;
			for (int effectId : skill.getDependOnTargetEffectId())
			{
				if (target.getFirstEffect(effectId) != null)
				{
					debuffsFound++;
				}
			}

			if (debuffsFound < skill.getDamageDepend().length)
			{
				finalBonus *= skill.getDamageDepend()[debuffsFound];
			}
		}

		if (Config.isServer(Config.TENKAI) && attacker.getActingPlayer() != null)
		{
			//if (mAtk > 1000000)
			//	mAtk = 1000000 + Math.pow(mAtk - 1000000, 0.9);
			if (critBonus > 2)
			{
				critBonus = 1 + Math.pow(critBonus - 1, 0.55);
			}
			if (finalBonus > 2)
			{
				finalBonus = 1 + Math.pow(finalBonus - 1, 0.45);
			}
		}

		double damage = 91.0 * Math.sqrt(mAtk) * skill.getPower(attacker, target, isPvP, isPvE) / mDef * finalBonus *
				critBonus * weaponRandom + staticCritBonus;

		damage = calcCustomModifier(attacker, target, damage);

		if (Config.isServer(Config.TENKAI) && isPvP && damage > 10000)
		{
			damage = 10000 + Math.pow(damage - 10000, 0.9);
		}

		if (missed)
		{
			if (halfMiss)
			{
				damage /= 2;
			}
			else
			{
				damage = 0;
			}
		}

		if (damage < 1)
		{
			damage = 1;
		}

		if (Config.isServer(Config.TENKAI) && isPvP && damage > 10000 && mDef > 5000)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(
						"INSERT INTO log_damage" + "(attacker, target, attackerClass, targetClass, damageType, " +
								"attack, defense, levelMod, power, powerBonus, critBonus, " +
								"critStaticBonus, positionBonus, ssBonus, finalBonus, damage)" + "VALUES" +
								"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				statement.setString(1, attacker.getName());
				statement.setString(2, target.getName());
				statement.setInt(3, attacker.getActingPlayer().getClassId());
				statement.setInt(4, target.getActingPlayer().getClassId());
				statement.setString(5, "MAGIC");
				statement.setDouble(6, mAtk / ssMul);
				statement.setDouble(7, mDef);
				statement.setDouble(8, 0);
				statement.setDouble(9, skill.getPower(attacker, target, isPvP, isPvE));
				statement.setDouble(10, 1);
				statement.setDouble(11, critBonus);
				statement.setDouble(12, 0);
				statement.setDouble(13, 0);
				statement.setDouble(14, ssMul);
				statement.setDouble(15, finalBonus);
				statement.setDouble(16, damage);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}

		return damage;
	}

	public static double calcMagicDam(L2CubicInstance attacker, L2Character target, L2Skill skill, boolean mcrit, byte shld)
	{
		// Current info include mAtk in the skill power.
		double mAtk = attacker.getMAtk();
		final boolean isPvP = target instanceof L2Playable;
		final boolean isPvE = target instanceof L2Attackable;
		double mDef = target.getMDef(attacker.getOwner(), skill);

		switch (shld)
		{
			case SHIELD_DEFENSE_SUCCEED:
				mDef += target.getShldDef(); // kamael
				break;
			case SHIELD_DEFENSE_PERFECT_BLOCK: // perfect block
				return 1;
		}

		double damage = 91 * Math.sqrt(mAtk) * skill.getPower(isPvP, isPvE) / mDef;
		L2PcInstance owner = attacker.getOwner();
		// Failure calculation
		if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(owner, target, skill))
		{
			if (target.getLevel() - skill.getMagicLevel() > 9)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
				sm.addCharName(target);
				sm.addSkillName(skill);
				owner.sendPacket(sm);

				damage = 1;
			}
			else
			{
				if (skill.getSkillType() == L2SkillType.DRAIN)
				{
					owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DRAIN_HALF_SUCCESFUL));
				}
				else
				{
					owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
				}

				damage /= 2;
			}

			if (target instanceof L2PcInstance)
			{
				if (skill.getSkillType() == L2SkillType.DRAIN)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_DRAIN);
					sm.addCharName(owner);
					target.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_MAGIC);
					sm.addCharName(owner);
					target.sendPacket(sm);
				}
			}
		}
		else if (mcrit)
		{
			damage *= 3;
		}

		// CT2.3 general magic vuln
		damage *= target.calcStat(Stats.MAGIC_DAMAGE_VULN, 1, null, null);

		damage *= calcElemental(owner, target, skill);

		if (target instanceof L2Attackable)
		{
			if (!target.isRaid() && !target.isRaidMinion() && target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY &&
					attacker.getOwner() != null && target.getLevel() - attacker.getOwner().getLevel() >= 2)
			{
				int lvlDiff = target.getLevel() - attacker.getOwner().getLevel() - 1;
				if (lvlDiff > Config.NPC_SKILL_DMG_PENALTY.size())
				{
					damage *= Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size());
				}
				else
				{
					damage *= Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
				}
			}
		}

		return damage;
	}

	/**
	 * Returns true in case of critical hit
	 */
	public static boolean calcCrit(double rate, L2Character target)
	{
		if (target != null)
		{
			rate /= target.calcStat(Stats.CRIT_DAMAGE_EVASION, 1, null, null);
		}

		return rate > Rnd.get(1000);
	}

	/**
	 * Calculate value of lethal chance
	 */
	public static double calcLethal(L2Character activeChar, L2Character target, int baseLethal, int magiclvl)
	{
		double chance = 0;
		if (magiclvl > 0)
		{
			int delta = (magiclvl + activeChar.getLevel()) / 2 - 1 - target.getLevel();

			// delta [-3,infinite)
			if (delta >= -3)
			{
				chance = baseLethal * ((double) activeChar.getLevel() / target.getLevel());
			}
			// delta [-9, -3[
			else if (delta < -3 && delta >= -9)
			{
				//			   baseLethal
				// chance = -1 * -----------
				//			   (delta / 3)
				chance = -3 * (baseLethal / delta);
			}
			//delta [-infinite,-9[
			else
			{
				chance = baseLethal / 15;
			}
		}
		else
		{
			chance = baseLethal * ((double) activeChar.getLevel() / target.getLevel());
		}

		double rate = 10 * activeChar.calcStat(Stats.LETHAL_RATE, chance, target, null);
		if (Config.isServer(Config.TENKAI) && rate > 2 && target instanceof L2Attackable && target.getLevel() >= 100)
		{
			rate /= 2;
		}
		return rate;
	}

	public static boolean calcLethalHit(L2Character activeChar, L2Character target, L2Skill skill)
	{
		if (target.isRaid() || target instanceof L2RaidBossInstance || target instanceof L2GrandBossInstance ||
				target instanceof L2DoorInstance || target instanceof L2Npc && ((L2Npc) target).getNpcId() == 35062)
		{
			return false;
		}

        /*
          LasTravel
          - Filter the mobs what are defined as a lethal inmune (few farm mobs with hight drop rate)
         */
		if (target instanceof L2MonsterInstance)
		{
			if (((L2MonsterInstance) target).getTemplate().isLethalImmune ||
					((L2MonsterInstance) target).getIsLethalInmune())
			{
				return false;
			}
		}

		// 2nd lethal effect activate (cp,hp to 1 or if target is npc then hp to 1)
		if (skill.getLethalChance2() > 0 &&
				Rnd.get(1000) < calcLethal(activeChar, target, skill.getLethalChance2(), skill.getMagicLevel()))
		{
			if (target instanceof L2Npc)
			{
				target.reduceCurrentHp(target.getCurrentHp() - 1, activeChar, skill);
			}
			else if (target instanceof L2PcInstance) // If is a active player set his HP and CP to 1
			{
				L2PcInstance player = (L2PcInstance) target;
				if (!player.isInvul(activeChar))
				{
					if (!(activeChar instanceof L2PcInstance && activeChar.isGM() &&
							!((L2PcInstance) activeChar).getAccessLevel().canGiveDamage()))
					{
						player.setCurrentHp(1);
						player.setCurrentCp(1);
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LETHAL_STRIKE_SUCCESSFUL));
					}
				}
			}
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LETHAL_STRIKE));
		}
		else if (skill.getLethalChance1() > 0 &&
				Rnd.get(1000) < calcLethal(activeChar, target, skill.getLethalChance1(), skill.getMagicLevel()))
		{
			if (target instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) target;
				if (!player.isInvul())
				{
					if (!(activeChar instanceof L2PcInstance && activeChar.isGM() &&
							!((L2PcInstance) activeChar).getAccessLevel().canGiveDamage()))
					{
						if (Rnd.get(150) < player.getLUC())
						{
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EVADED_KILLING_BLOW));
							L2Skill luckSkill = SkillTable.FrequentSkill.LUCKY_CLOVER.getSkill();
							if (luckSkill != null)
							{
								player.broadcastPacket(
										new MagicSkillUse(player, player, luckSkill.getId(), luckSkill.getLevel(),
												luckSkill.getHitTime(), luckSkill.getReuseDelay(),
												luckSkill.getReuseHashCode(), 0, 0));
							}
						}
						else
						{
							player.setCurrentCp(1); // Set CP to 1
							player.sendPacket(SystemMessage
									.getSystemMessage(SystemMessageId.CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL));
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CP_SIPHON));
						}
					}
				}
			}
			//TODO: remove half kill since SYSMsg got changed.
			else if (target instanceof L2Npc) // If is a monster remove first damage and after 50% of current hp
			{
				target.reduceCurrentHp(target.getCurrentHp() / 2, activeChar, skill);
				activeChar.sendMessage("Half Kill!");
			}
		}
		else
		{
			return false;
		}
		return true;
	}

	public static boolean calcMCrit(double mRate)
	{
		if (Config.isServer(Config.TENKAI))
		{
			// 500 MCrit gives 32% chance
			return Rnd.get(1550) < mRate;
		}

		return Rnd.get(1000) < mRate;
	}

	/**
	 * Returns true in case when ATTACK is canceled due to hit
	 */
	public static boolean calcAtkBreak(L2Character target, double dmg)
	{
		if (target.getFusionSkill() != null)
		{
			return true;
		}

		double init = 0;

		if (Config.ALT_GAME_CANCEL_CAST && target.isCastingNow())
		{
			init = 15;
		}
		if (Config.ALT_GAME_CANCEL_BOW && target.isAttackingNow())
		{
			L2Weapon wpn = target.getActiveWeaponItem();
			if (wpn != null && wpn.getItemType() == L2WeaponType.BOW)
			{
				init = 15;
			}
		}

		if (target.isRaid() || target.isInvul() || init <= 0)
		{
			return false; // No attack break
		}

		// Chance of break is higher with higher dmg
		//init += Math.sqrt(13 * dmg);
		init = Math.sqrt(dmg / target.getMaxVisibleHp()) * 100; // Tenkai custom

		// Chance is affected by target MEN
		init -= BaseStats.MEN.calcBonus(target) * 100 - 100;

		// Calculate all modifiers for ATTACK_CANCEL
		double rate = target.calcStat(Stats.ATTACK_CANCEL, init, null, null);
		//if (rate > 0)
		//	System.out.println(target.getName() + " " + rate);

		// Adjust the rate to be between 1 and 99
		if (rate > 99)
		{
			rate = 99;
		}
		else if (rate < 1)
		{
			rate = 1;
		}

		return Rnd.get(100) < rate;
	}

	/**
	 * Calculate delay (in milliseconds) before next ATTACK
	 */
	public static int calcPAtkSpd(L2Character attacker, L2Character target, double rate)
	{
		// measured Oct 2006 by Tank6585, formula by Sami
		// attack speed 312 equals 1500 ms delay... (or 300 + 40 ms delay?)
		if (rate < 2)
		{
			return 2700;
		}
		else
		{
			return (int) (470000 / rate);
		}
	}

	/**
	 * Calculate delay (in milliseconds) for skills cast
	 */
	public static int calcAtkSpd(L2Character attacker, L2Skill skill, double skillTime)
	{
		double atkSpd;
		if (skill.isMagic())
		{
			atkSpd = attacker.getMAtkSpd();
		}
		else
		{
			atkSpd = attacker.getPAtkSpd();
		}

		return (int) (skillTime * 333 / atkSpd);
	}

	/**
	 * Returns true if hit missed (target evaded)
	 * Formula based on http://l2p.l2wh.com/nonskillattacks.html
	 **/
	public static boolean calcHitMiss(L2Character attacker, L2Character target)
	{
		double chance = 800 + 20 * (attacker.getAccuracy() - target.getEvasionRate(attacker));

		//if (attacker instanceof L2PcInstance && target instanceof L2PcInstance)
		//	Log.info("P: " + attacker.getAccuracy() + " " + target.getEvasionRate(attacker) + ": " + chance);

		// Get additional bonus from the conditions when you are attacking
		chance *= hitConditionBonus.getConditionBonus(attacker, target);

		chance = Math.max(chance, 200);
		chance = Math.min(chance, 980);

		if (attacker instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) attacker;
			if (player.getExpertiseWeaponPenalty() != 0)
			{
				return true;
			}
		}

		return chance < Rnd.get(1000);
	}

	public static boolean calcMagicMiss(L2Character attacker, L2Character target)
	{
		if (Config.isServer(Config.TENKAI))
		{
			double chance = 900 + 15 * (attacker.getMAccuracy() - target.getMEvasionRate(attacker));

			// Get additional bonus from the conditions when you are attacking
			//chance *= hitConditionBonus.getConditionBonus(attacker, target);

			//Log.info(attacker.getName() + "->" + target.getName());
			//if (attacker instanceof L2PcInstance && target instanceof L2PcInstance)
			//	Log.info("M: " + attacker.getMAccuracy() + " " + target.getMEvasionRate(attacker) + ": " + chance);

			//chance = Math.max(chance, 200);
			//chance = Math.min(chance, 980);

			// Guess for Magical Evasions
			chance = Math.max(chance, 300);
			//if (target instanceof L2PcInstance && chance < 1000)
			//	System.out.println(attacker.getName() + " " + target.getName() + " " + attacker.getMAccuracy() + " " + target.getMEvasionRate(attacker) + " " + chance);

			return chance < Rnd.get(1000);
		}

		double chance = (80 + 2 * (attacker.getMAccuracy() - target.getMEvasionRate(attacker))) * 10;

		//Log.info(attacker.getName() + "->" + target.getName());
		//Log.info("M: " + attacker.getMAccuracy() + " " + target.getMEvasionRate(attacker) + ": " + chance);

		// Get additional bonus from the conditions when you are attacking
		chance *= hitConditionBonus.getConditionBonus(attacker, target);

		chance = Math.max(chance, 200);
		chance = Math.min(chance, 980);

		if (attacker instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) attacker;

			if (player.getExpertiseWeaponPenalty() != 0)
			{
				return true;
			}
		}

		return chance < Rnd.get(1000);
	}

	/**
	 * Returns:<br>
	 * 0 = shield defense doesn't succeed<br>
	 * 1 = shield defense succeed<br>
	 * 2 = perfect block<br>
	 *
	 * @param attacker
	 * @param target
	 * @param sendSysMsg
	 * @return
	 */
	public static byte calcShldUse(L2Character attacker, L2Character target, L2Skill skill, boolean sendSysMsg)
	{
		if (skill != null && (skill.ignoreShield() || skill.isMagic()))
		{
			return 0;
		}

		L2Item item = target.getSecondaryWeaponItem();
		if (item == null || !(item instanceof L2Armor) || ((L2Armor) item).getItemType() == L2ArmorType.SIGIL)
		{
			return 0;
		}

		double shldRate = target.calcStat(Stats.SHIELD_RATE, 0, attacker, null) * BaseStats.DEX.calcBonus(target);
		if (shldRate == 0.0)
		{
			return 0;
		}

		int degreeside = (int) target.calcStat(Stats.SHIELD_DEFENCE_ANGLE, 120, null, null);
		if (degreeside < 360 && !target.isFacing(attacker, degreeside))
		{
			return 0;
		}

		// Tenkai custom: Make shields way less effective on non-sigel awakened classes
		/*if (target instanceof L2PcInstance
				&& ((L2PcInstance)target).getClassId() > 139)
			shldRate *= 0.1;*/

		byte result = SHIELD_DEFENSE_FAILED;
		// if attacker use bow and target wear shield, shield block rate is multiplied by 1.3 (30%)
		L2Weapon at_weapon = attacker.getActiveWeaponItem();
		if (at_weapon != null && at_weapon.getItemType() == L2WeaponType.BOW)
		{
			shldRate *= 1.3;
		}

		int rnd = Rnd.get(100);
		if (shldRate * (Config.ALT_PERFECT_SHLD_BLOCK / 100.0) > rnd)
		{
			result = SHIELD_DEFENSE_PERFECT_BLOCK;
		}
		else if (shldRate > rnd)
		{
			result = SHIELD_DEFENSE_SUCCEED;
		}

		if (sendSysMsg && target instanceof L2PcInstance)
		{
			L2PcInstance enemy = (L2PcInstance) target;
			switch (result)
			{
				case SHIELD_DEFENSE_SUCCEED:
					enemy.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SHIELD_DEFENCE_SUCCESSFULL));
					break;
				case SHIELD_DEFENSE_PERFECT_BLOCK:
					enemy.sendPacket(SystemMessage
							.getSystemMessage(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS));
					break;
			}
		}

		if (result >= 1)
		{
			// ON_SHIELD_BLOCK
			if (target.getChanceSkills() != null)
			{
				target.getChanceSkills().onBlock(target);
			}
		}

		return result;
	}

	public static byte calcShldUse(L2Character attacker, L2Character target, L2Skill skill)
	{
		return calcShldUse(attacker, target, skill, true);
	}

	public static byte calcShldUse(L2Character attacker, L2Character target)
	{
		return calcShldUse(attacker, target, null, true);
	}

	public static boolean calcMagicAffected(L2Character actor, L2Character target, L2Skill skill)
	{
		// TODO: CHECK/FIX THIS FORMULA UP!!
		L2SkillType type = skill.getSkillType();
		double defence = 0;
		if (skill.isActive() && skill.isOffensive() && !skill.isNeutral())
		{
			defence = target.getMDef(actor, skill);
		}

		double attack = 2 * actor.getMAtk(target, skill) * (1.0 - calcSkillResistance(actor, target, skill) / 100);
		double d = (attack - defence) / (attack + defence);
		if (target.isRaid())
		{
			switch (type)
			{
				case DEBUFF:
				case AGGDEBUFF:
				case CONTINUOUS_DEBUFF:
					return d > 0 && Rnd.get(1000) == 1;
			}
		}

		d += 0.5 * Rnd.nextGaussian();
		return d > 0;
	}

	public static double calcSkillResistance(L2Character attacker, L2Character target, L2Skill skill)
	{
		double multiplier = 100; // initialize...

		// Get the skill type to calculate its effect in function of base stats
		// of the L2Character target
		if (skill != null)
		{
			// Finally, calculate skilltype vulnerabilities
			L2SkillType type = skill.getSkillType();

			// For additional effects (like STUN, SHOCK, PARALYZE...) on damage skills
			switch (type)
			{
				case PDAM:
				case MDAM:
				case BLOW:
				case DRAIN:
				case CHARGEDAM:
				case FATAL:
				case DEATHLINK:
				case CPDAM:
				case MANADAM:
				case CPDAMPERCENT:
				case MAXHPDAMPERCENT:
					return 100;
			}

			multiplier = calcSkillTypeResistance(target, type);
		}
		return multiplier;
	}

	public static double calcSkillTypeResistance(L2Character target, L2SkillType type)
	{
		double multiplier = 100.0;
		if (type != null)
		{
			switch (type)
			{
				case BETRAY:
				case AGGDEBUFF:
				case ERASE:
					multiplier = target.calcStat(Stats.DERANGEMENT_RES, multiplier, target, null);
					break;
				case CANCEL:
					multiplier = target.calcStat(Stats.CANCEL_RES, multiplier, target, null);
					break;
				default:
			}
		}

		return multiplier;
	}

	public static double calcSkillProficiency(L2Skill skill, L2Character attacker, L2Character target)
	{
		double multiplier = 100; // initialize...

		if (skill != null)
		{
			// Calculate skilltype vulnerabilities
			L2SkillType type = skill.getSkillType();

			// For additional effects (like STUN, SHOCK, PARALYZE...) on damage skills
			switch (type)
			{
				case PDAM:
				case MDAM:
				case BLOW:
				case DRAIN:
				case CHARGEDAM:
				case FATAL:
				case DEATHLINK:
				case CPDAM:
				case MANADAM:
				case CPDAMPERCENT:
				case MAXHPDAMPERCENT:
					return 100;
			}

			multiplier = calcSkillTypeProficiency(multiplier, attacker, target, type);
		}

		return multiplier;
	}

	public static double calcSkillTypeProficiency(double multiplier, L2Character attacker, L2Character target, L2SkillType type)
	{
		if (type != null)
		{
			switch (type)
			{
				case BETRAY:
				case AGGDEBUFF:
				case ERASE:
					multiplier = attacker.calcStat(Stats.DERANGEMENT_PROF, multiplier, target, null);
					break;
				case CANCEL:
					multiplier = attacker.calcStat(Stats.CANCEL_PROF, multiplier, target, null);
					break;
				default:
			}
		}

		return multiplier;
	}

	public static double calcEffectTypeResistance(L2Character target, L2AbnormalType type)
	{
		double multiplier = 1.0;
		if (type != null)
		{
			switch (type)
			{
				case BLEED:
					multiplier = target.calcStat(Stats.BLEED_RES, multiplier, target, null);
					break;
				case POISON:
					multiplier = target.calcStat(Stats.POISON_RES, multiplier, target, null);
					break;
				case STUN:
					multiplier = target.calcStat(Stats.STUN_RES, multiplier, target, null);
					break;
				case PARALYZE:
					multiplier = target.calcStat(Stats.PARALYSIS_RES, multiplier, target, null);
					break;
				case PETRIFY:
					multiplier = target.calcStat(Stats.PETRIFY_RES, multiplier, target, null);
					break;
				case HOLD:
					multiplier = target.calcStat(Stats.HOLD_RES, multiplier, target, null);
					break;
				case SLEEP:
					multiplier = target.calcStat(Stats.SLEEP_RES, multiplier, target, null);
					break;
				case SILENCE:
				case FEAR:
				case LOVE:
				case BETRAY:
				case CONFUSION:
					multiplier = target.calcStat(Stats.DERANGEMENT_RES, multiplier, target, null);
					break;
				case KNOCK_DOWN:
					multiplier = target.calcStat(Stats.KNOCK_DOWN_RES, multiplier, target, null);
					break;
				case KNOCK_BACK:
					multiplier = target.calcStat(Stats.KNOCK_BACK_RES, multiplier, target, null);
					break;
				case PULL:
					multiplier = target.calcStat(Stats.PULL_RES, multiplier, target, null);
					break;
				case AERIAL_YOKE:
					multiplier = target.calcStat(Stats.AERIAL_YOKE_RES, multiplier, target, null);
					break;
				case CANCEL:
					multiplier = target.calcStat(Stats.CANCEL_RES, multiplier, target, null);
					break;
			}

			switch (type)
			{
				case STUN:
				case PARALYZE:
				case KNOCK_BACK:
				case KNOCK_DOWN:
				case HOLD:
				case DISARM:
				case PETRIFY:
					multiplier = target.calcStat(Stats.PHYS_DEBUFF_RES, multiplier, target, null);
					break;
				case SLEEP:
				case MUTATE:
				case FEAR:
				case LOVE:
				case AERIAL_YOKE:
				case SILENCE:
					multiplier = target.calcStat(Stats.MENTAL_DEBUFF_RES, multiplier, target, null);
					break;
			}
		}

		multiplier = target.calcStat(Stats.DEBUFF_RES, multiplier, target, null);
		return multiplier;
	}

	public static double calcEffectTypeProficiency(L2Character attacker, L2Character target, L2AbnormalType type)
	{
		double multiplier = 1.0;
		if (type != null)
		{
			switch (type)
			{
				case BLEED:
					multiplier = attacker.calcStat(Stats.BLEED_PROF, multiplier, target, null);
					break;
				case POISON:
					multiplier = attacker.calcStat(Stats.POISON_PROF, multiplier, target, null);
					break;
				case STUN:
					multiplier = attacker.calcStat(Stats.STUN_PROF, multiplier, target, null);
					break;
				case PARALYZE:
					multiplier = attacker.calcStat(Stats.PARALYSIS_PROF, multiplier, target, null);
					break;
				case PETRIFY:
					multiplier = attacker.calcStat(Stats.PETRIFY_PROF, multiplier, target, null);
					break;
				case HOLD:
					multiplier = attacker.calcStat(Stats.HOLD_PROF, multiplier, target, null);
					break;
				case SLEEP:
					multiplier = attacker.calcStat(Stats.SLEEP_PROF, multiplier, target, null);
					break;
				case SILENCE:
				case FEAR:
				case LOVE:
				case BETRAY:
				case CONFUSION:
					multiplier = attacker.calcStat(Stats.DERANGEMENT_PROF, multiplier, target, null);
					break;
				case KNOCK_DOWN:
					multiplier = attacker.calcStat(Stats.KNOCK_DOWN_PROF, multiplier, target, null);
					break;
				case KNOCK_BACK:
					multiplier = attacker.calcStat(Stats.KNOCK_BACK_PROF, multiplier, target, null);
					break;
				case PULL:
					multiplier = attacker.calcStat(Stats.PULL_PROF, multiplier, target, null);
					break;
				case AERIAL_YOKE:
					multiplier = attacker.calcStat(Stats.AERIAL_YOKE_PROF, multiplier, target, null);
					break;
				case CANCEL:
					multiplier = attacker.calcStat(Stats.CANCEL_PROF, multiplier, target, null);
					break;
				case DEBUFF:
					multiplier = attacker.calcStat(Stats.DEBUFF_PROF, multiplier, target, null);
					break;
			}

			switch (type)
			{
				case STUN:
				case PARALYZE:
				case KNOCK_BACK:
				case KNOCK_DOWN:
				case HOLD:
				case DISARM:
				case PETRIFY:
					multiplier = attacker.calcStat(Stats.PHYS_DEBUFF_PROF, multiplier, target, null);
					break;
				case SLEEP:
				case MUTATE:
				case FEAR:
				case LOVE:
				case AERIAL_YOKE:
				case SILENCE:
					multiplier = attacker.calcStat(Stats.MENTAL_DEBUFF_PROF, multiplier, target, null);
					break;
			}
		}

		multiplier = attacker.calcStat(Stats.DEBUFF_PROF, multiplier, target, null);
		return multiplier;
	}

	public static double calcSkillStatModifier(L2Skill skill, L2Character attacker, L2Character target)
	{
		if (Config.isServer(Config.TENKAI))
		{
			return 0.0;
		}

		if (skill.isMagic())
		{
			return BaseStats.MEN.calcBonus(attacker) * 10;
		}
		else
		{
			return BaseStats.CON.calcBonus(attacker) * 10;
		}
	}

	public static int calcLvlDependModifier(L2Character attacker, L2Character target, L2Skill skill)
	{
		if (skill.getLevelDepend() == 0)
		{
			return 0;
		}

		final int attackerMod;
		if (skill.getMagicLevel() > 0)
		{
			attackerMod = skill.getMagicLevel();
		}
		else
		{
			attackerMod = attacker.getLevel();
		}

		final int delta = attackerMod - target.getLevel();

		// Have it in 5 by 5 jumps
		int deltamod = delta / 5 * 5;
		/*if (deltamod != delta)
		{
			if (delta < 0)
				deltamod -= 5;
			else
				deltamod += 5;
		}

		deltamod *= skill.getLevelDepend();*/

		// 5% per level difference
		deltamod *= 5;

		return deltamod;
	}

	public static int calcElementModifier(L2Character attacker, L2Character target, L2Skill skill)
	{
		final byte element = skill.getElement();

		if (element == Elementals.NONE)
		{
			return 0;
		}

		int result = skill.getElementPower();
		if (attacker.getAttackElement() == element)
		{
			result += attacker.getAttackElementValue(element);
		}

		result -= target.getDefenseElementValue(element);

		if (result < 0)
		{
			return 0;
		}

		return Math.round((float) result / 10);
	}

	public static boolean calcEffectSuccess(L2Character attacker, L2Character target, L2Abnormal effect, L2Skill skill, byte shld, double ssMul)
	{
		if (!skill.isOffensive())
		{
			if (target.calcStat(Stats.BUFF_IMMUNITY, 0.0, attacker, null) > 0.0)
			{
				return false;
			}

			if (target.isAffected(L2EffectType.BLOCK_INVUL.getMask()))
			{
				for (L2Effect eff : effect.getEffects())
				{
					if (eff instanceof EffectInvincible)
					{
						return false;
					}
				}
			}

			if (target.isAffected(L2EffectType.BLOCK_HIDE.getMask()) && effect.getType() == L2AbnormalType.HIDE)
			{
				return false;
			}

			return !(target.isAffected(L2EffectType.BLOCK_TALISMANS.getMask()) && skill.getName().contains("Talisman"));

		}

		if (target.calcStat(Stats.DEBUFF_IMMUNITY, 0.0, attacker, null) > 0.0)
		{
			target.stopEffectsOnDebuffBlock();
			return false;
		}

		if (target.getFirstEffect(L2AbnormalType.SPALLATION) != null &&
				!Util.checkIfInRange(130, attacker, target, false))
		{
			attacker.sendMessage("Your attack has been blocked.");
			target.sendMessage("You blocked an attack.");
			return false;
		}

		if (target instanceof L2MonsterInstance)
		{
			if (((L2MonsterInstance) target).getTemplate().isDebuffImmune)
			{
				return false;
			}
		}

		if (skill.isMagic())
		{
			final double failureModifier = attacker.calcStat(Stats.MAGIC_FAILURE_RATE, 1, target, skill);
			if (failureModifier > 100)
			{
				return false;
			}
		}

		if (target instanceof L2RaidBossInstance || target instanceof L2GrandBossInstance)
		{
			switch (skill.getSkillType())
			{
				case DEBUFF:
				case AGGDAMAGE:
				case PDAM:
				case MDAM:
				case MARK:
				case DRAIN:
				{
					break;
				}
				default:
					return false;
			}
		}

		if (target.isRaid() || target instanceof L2Npc && !(target instanceof L2Attackable))
		{
			switch (effect.getType())
			{
				case CONFUSION:
				case HOLD:
				case STUN:
				case SILENCE:
				case FEAR:
				case PARALYZE:
				case PETRIFY:
				case SLEEP:
				case AERIAL_YOKE:
				case KNOCK_BACK:
				case KNOCK_DOWN:
					return false;
			}
		}

		int rate = 0;

		if (attacker instanceof L2NpcInstance && ((L2NpcInstance) attacker).getOwner() != null)
		{
			attacker = ((L2NpcInstance) attacker).getOwner();
		}
		else if (attacker instanceof L2GuardInstance && ((L2GuardInstance) attacker).getOwner() != null)
		{
			attacker = ((L2GuardInstance) attacker).getOwner();
		}
		else if (attacker instanceof L2TrapInstance)
		{
			attacker = ((L2TrapInstance) attacker).getOwner();
		}

		// Filter the debuffs on the mobs defined as inmune
		if (target instanceof L2Attackable && ((L2Attackable) target).getTemplate().isDebuffImmune)
		{
			return false;
		}

		// Get power of the skill
		int landRate = (int) effect.getLandRate();
		if (landRate < 0)
		{
			return true;
		}

		// Special case for resist ignoring skills
		if (skill.ignoreResists())
		{
			return Rnd.get(100) < landRate;
		}

		// Perfect shield block can protect from debuff
		if (shld == SHIELD_DEFENSE_PERFECT_BLOCK) // perfect block
		{
			return false;
		}

		// Consider stats' influence
		double statMod = calcSkillStatModifier(skill, attacker, target);

		// Resists and proficiency boosts (epics etc.)
		double resModifier = calcEffectTypeResistance(target, effect.getType());
		double profModifier = calcEffectTypeProficiency(attacker, target, effect.getType());
		if (Config.isServer(Config.TENKAI))
		{
			//resModifier = Math.pow(resModifier, 0.75);
			//profModifier = Math.pow(profModifier, 0.75);
		}

		double resMod = profModifier / resModifier;
		if (resMod > 1.9)
		{
			resMod = 1.9;
		}
		else if (resMod < 0.1)
		{
			resMod = 0.1;
		}

		// Bonus for skills with element
		int eleMod = calcElementModifier(attacker, target, skill);

		// Bonus for level difference between target and caster/skill level
		int lvlMod = calcLvlDependModifier(attacker, target, skill);

		//Log.info(vulnModifier + " " + profModifier + " " + resMod + " " + statMod + " " + eleMod + " " + lvlMod);

		// Finally, the calculation of the land rate in ONE customizable formula, to take some confusion out of this mess
		// Factors to consider: power, statMod, resMod, eleMod, lvlMod
		rate = (int) ((landRate + statMod + eleMod + lvlMod) * resMod);

		if (rate > skill.getMaxChance())
		{
			rate = skill.getMaxChance();
		}
		else if (rate < skill.getMinChance())
		{
			rate = skill.getMinChance();
		}

		//if (skill.isMagic() && attacker instanceof L2PcInstance && target instanceof L2PcInstance)
		//	System.out.println(skill.getName() + " " + effect.getType()	+ " " + effect.getLandRate() + "% " + rate + "%"
		//			+ " " + Math.floor(statMod*1000)/1000 + " " + Math.floor(profModifier*1000)/1000 + " " + Math.floor(resModifier*1000)/1000
		//			+ " " + Math.floor(resMod*1000)/1000);

		if (attacker instanceof L2TrapInstance && ((L2TrapInstance) attacker).getOwner() != null &&
				((L2TrapInstance) attacker).getOwner().isLandRates())
		{
			((L2TrapInstance) attacker).getOwner().sendMessage(
					"Your " + skill.getName() + "'s effect had a " + rate + "% chance to land, with a " +
							effect.getLandRate() + " base land rate.");
		}

		// Feedback for .landrates
		if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isLandRates())
		{
			attacker.sendMessage("Your " + skill.getName() + "'s effect had a " + rate + "% chance to land, with a " +
					effect.getLandRate() + " base land rate.");
		}
		if (target instanceof L2PcInstance && ((L2PcInstance) target).isLandRates())
		{
			target.sendMessage(
					"This " + skill.getName() + "'s effect had a " + rate + "% chance to land over you, with a " +
							effect.getLandRate() + " base land rate.");
		}
		if (attacker instanceof L2PcInstance && attacker.isGM())
		{
			((L2PcInstance) attacker).sendSysMessage(
					"Your " + skill.getName() + "'s effect had a " + rate + "% chance to land, with a " +
							effect.getLandRate() + " base land rate.");
		}
		if (target instanceof L2PcInstance && target.isGM())
		{
			((L2PcInstance) target).sendSysMessage(
					"This " + skill.getName() + "'s effect had a " + rate + "% chance to land over you, with a " +
							effect.getLandRate() + " base land rate.");
		}

		// For land rate observation by GM
		if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isLandrateObservationActive())
		{
			for (L2PcInstance obs : ((L2PcInstance) attacker).getLandrateObservers())
			{
				obs.sendMessage(
						"Player:" + attacker.getName() + " Target: " + target.getName() + " Skill:" + skill.getName() +
								" Effect:" + effect.getType() + " Rate:" + rate + "%" + " statMod:" +
								Math.floor(statMod * 1000) / 1000 + " eleMod:" + eleMod + " lvlMod:" + lvlMod +
								" resMod:" + Math.floor(resMod * 1000) /
								1000); // + (skill.isMagic() ? " mAtkMod: " + Math.floor(mAtkMod*1000)/1000 : ""));
			}
		}

		int mezType = target.getMezType(effect.getType());
		float mezMod = target.getMezMod(mezType);
		if (Rnd.get(100) < rate * mezMod)
		{
			target.increaseMezResist(mezType);
			L2Abnormal currentEffect = target.getFirstEffect(skill.getId());
			if (currentEffect != null)
			{
				currentEffect.exit();
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	public static boolean calcSkillSuccess(L2Character attacker, L2Character target, L2Skill skill, byte shld, double ssMul)
	{
		int rate = 0;

		// Determine whether it's a skill used in PvP
		final boolean isPvP = attacker instanceof L2Playable && target instanceof L2Playable;
		final boolean isPvE = attacker instanceof L2Playable && target instanceof L2Attackable;

        /*
          LasTravel
          - Filter the debuffs on the mobs defined as inmune
         */
		if (target instanceof L2Attackable)
		{
			if (((L2Attackable) target).getTemplate().isDebuffImmune)
			{
				return false;
			}
		}

		// Special case for resist ignoring skills
		if (skill.ignoreResists())
		{
			if (attacker.isDebug())
			{
				attacker.sendDebugMessage(skill.getName() + " ignoring resists");
			}

			return Rnd.get(100) < skill.getPower(isPvP, isPvE);
		}

		// Perfect shield block can protect from debuff
		if (shld == SHIELD_DEFENSE_PERFECT_BLOCK) // perfect block
		{
			if (attacker.isDebug())
			{
				attacker.sendDebugMessage(skill.getName() + " blocked by shield");
			}

			return false;
		}

		// Get power of the skill
		int power = (int) skill.getPower(isPvP, isPvE);

		// Decrease base rate for olympiad games to make matches not too debuff dependent
		/*if ((attacker instanceof L2PcInstance && ((L2PcInstance)attacker).isInOlympiadMode())
			|| (attacker instanceof L2SummonInstance && ((L2SummonInstance)attacker).isInOlympiadMode()))
		{
			power /= 2;
		}*/

		// Consider stats' influence
		double statMod = calcSkillStatModifier(skill, attacker, target);

		// Calculate reduction/boost depending on m.atk-m.def-relation for magical skills
		/*double mAtkMod = 1.;
		if (skill.isMagic())
		{
			int ssModifier = 0;

			mAtkMod = target.getMDef(target, skill);
			if (shld == SHIELD_DEFENSE_SUCCEED)
				mAtkMod += target.getShldDef();

			// Add Bonus for Sps/SS
			if (bss)
				ssModifier = 4;
			else if (sps)
				ssModifier = 2;
			else
				ssModifier = 1;

			mAtkMod = 20 * Math.sqrt(ssModifier * attacker.getMAtk(target, skill)) / mAtkMod;
		}*/

		// Resists and proficiency boosts (epics etc.)
		double resModifier = calcSkillResistance(attacker, target, skill);
		double profModifier = calcSkillProficiency(skill, attacker, target);
		double resMod = profModifier / resModifier;

		// Bonus for skills with element
		int eleMod = calcElementModifier(attacker, target, skill);

		// Bonus for level difference between target and caster/skill level
		int lvlMod = calcLvlDependModifier(attacker, target, skill);

		// Finally, the calculation of the land rate in ONE customizable formula, to take some confusion out of this mess
		// Factors to consider: power, statMod, resMod, eleMod, lvlMod
		rate = (int) ((power + statMod + eleMod + lvlMod) * resMod);

		/*if (skill.isMagic())	// And eventually the influence of m.atk/m.def relation between attacker and target
		{
			if (mAtkMod < 1)
				rate *= Math.max(mAtkMod, 0.8) * 0.6;
			else
				rate *= Math.min(mAtkMod, 1.5) * 0.6;	// This implicitly reduces the land rate of all magical debuffs
		}*/

		/*if (rate > skill.getMaxChance())
			rate = skill.getMaxChance();
		else*/
		if (rate < 1)
		{
			rate = 1;
		}

		// Feedback for .landrates
		if (rate < 100)
		{
			if (attacker instanceof L2TrapInstance && ((L2TrapInstance) attacker).getOwner() != null &&
					((L2TrapInstance) attacker).getOwner().isLandRates())
			{
				((L2TrapInstance) attacker).getOwner().sendMessage(
						"Your " + skill.getName() + " had a " + rate + "% chance to land, with a " + skill.getPower() +
								" base land rate.");
			}

			if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isLandRates())
			{
				attacker.sendMessage(
						"Your " + skill.getName() + " had a " + rate + "% chance to land, with a " + skill.getPower() +
								" base land rate.");
			}
			if (target instanceof L2PcInstance && ((L2PcInstance) target).isLandRates())
			{
				target.sendMessage(
						"This " + skill.getName() + " had a " + rate + "% chance to land over you, with a " +
								skill.getPower() + " base land rate.");
			}
		}

		// For land rate observation by GM
		if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isLandrateObservationActive())
		{
			for (L2PcInstance obs : ((L2PcInstance) attacker).getLandrateObservers())
			{
				obs.sendMessage(
						"Player:" + attacker.getName() + " Target: " + target.getName() + " Skill:" + skill.getName() +
								" Rate:" + rate + "%" + " statMod:" + Math.floor(statMod * 1000) / 1000 + " eleMod:" +
								eleMod + " lvlMod:" + lvlMod + " resMod:" + Math.floor(resMod * 1000) /
								1000); // + (skill.isMagic() ? " mAtkMod: " + Math.floor(mAtkMod*1000)/1000 : ""));
			}
		}

		int mezType = target.getMezType(skill.getSkillType());
		float mezMod = target.getMezMod(mezType);
		if (Rnd.get(100) < rate * mezMod)
		{
			target.increaseMezResist(mezType);
			return true;
		}
		else
		{
			return false;
		}
	}

	public static boolean calcCubicSkillSuccess(L2CubicInstance attacker, L2Character target, L2Skill skill, byte shld)
	{
		if (!skill.isOffensive())
		{
			return true;
		}

		if (shld == SHIELD_DEFENSE_PERFECT_BLOCK) // perfect block
		{
			return false;
		}

		final boolean isPvP = target instanceof L2Playable;
		final boolean isPvE = target instanceof L2Attackable;

		L2SkillType type = skill.getSkillType();

		// these skills should not work on RaidBoss
		if (target.isRaid())
		{
			switch (type)
			{
				case DEBUFF:
				case AGGDEBUFF:
				case CONTINUOUS_DEBUFF:
					return false;
			}
		}

		// if target reflect this skill then the effect will fail
		if (calcSkillReflect(target, skill) != SKILL_REFLECT_FAILED)
		{
			return false;
		}

		int value = (int) skill.getPower(isPvP, isPvE);
		double statModifier = calcSkillStatModifier(skill, attacker.getOwner(), target);
		int rate = (int) (value + statModifier);

		// Resists
		double resModifier = calcSkillResistance(attacker.getOwner(), target, skill);
		double profModifier = calcSkillProficiency(skill, attacker.getOwner(), target);
		double resMod = profModifier / resModifier;

		int elementModifier = calcElementModifier(attacker.getOwner(), target, skill);
		rate += elementModifier;

		//lvl modifier.
		int deltamod = calcLvlDependModifier(attacker.getOwner(), target, skill);
		rate += deltamod;

		if (rate > skill.getMaxChance())
		{
			rate = skill.getMaxChance();
		}
		else if (rate < skill.getMinChance())
		{
			rate = skill.getMinChance();
		}

		if (attacker.getOwner().isDebug() || Config.DEVELOPER)
		{
			final StringBuilder stat = new StringBuilder(100);
			StringUtil.append(stat, skill.getName(), " type:", skill.getSkillType().toString(), " power:",
					String.valueOf(value), " stat:", String.format("%1.2f", statModifier), " res:",
					String.format("%1.2f", resMod), "(", String.format("%1.2f", profModifier), "/",
					String.format("%1.2f", resModifier), ") elem:", String.valueOf(elementModifier), " lvl:",
					String.valueOf(deltamod), " total:", String.valueOf(rate));
			final String result = stat.toString();
			if (attacker.getOwner().isDebug())
			{
				attacker.getOwner().sendDebugMessage(result);
			}
			if (Config.DEVELOPER)
			{
				Log.info(result);
			}
		}

		return Rnd.get(100) < rate;
	}

	public static boolean calcMagicSuccess(L2Character attacker, L2Character target, L2Skill skill)
	{
		if (attacker instanceof L2NpcInstance && ((L2NpcInstance) attacker).getOwner() != null)
		{
			attacker = ((L2NpcInstance) attacker).getOwner();
		}
		else if (attacker instanceof L2GuardInstance && ((L2GuardInstance) attacker).getOwner() != null)
		{
			attacker = ((L2GuardInstance) attacker).getOwner();
		}

		int lvlDifference =
				target.getLevel() - (skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel());
		double lvlModifier = Math.pow(1.3, lvlDifference);
		float targetModifier = 1;
		if (target instanceof L2Attackable && !target.isRaid() && !target.isRaidMinion() &&
				target.getLevel() >= Config.MIN_NPC_LVL_MAGIC_PENALTY && attacker.getActingPlayer() != null &&
				target.getLevel() - attacker.getActingPlayer().getLevel() >= 3)
		{
			int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 2;
			if (lvlDiff > Config.NPC_SKILL_CHANCE_PENALTY.size())
			{
				targetModifier = Config.NPC_SKILL_CHANCE_PENALTY.get(Config.NPC_SKILL_CHANCE_PENALTY.size());
			}
			else
			{
				targetModifier = Config.NPC_SKILL_CHANCE_PENALTY.get(lvlDiff);
			}
		}
		// general magic resist
		final double resModifier = target.calcStat(Stats.MAGIC_SUCCESS_RES, 1, null, skill);
		final double failureModifier = attacker.calcStat(Stats.MAGIC_FAILURE_RATE, 1, target, skill);

		int rate = 100 - Math.round((float) (lvlModifier * targetModifier * resModifier * failureModifier));
		if (rate > skill.getMaxChance())
		{
			rate = skill.getMaxChance();
		}
		else if (rate < skill.getMinChance())
		{
			rate = skill.getMinChance();
		}

		if (attacker.isDebug() || Config.DEVELOPER)
		{
			final StringBuilder stat = new StringBuilder(100);
			StringUtil.append(stat, skill.getName(), " lvlDiff:", String.valueOf(lvlDifference), " lvlMod:",
					String.format("%1.2f", lvlModifier), " res:", String.format("%1.2f", resModifier), " fail:",
					String.format("%1.2f", failureModifier), " tgt:", String.valueOf(targetModifier), " total:",
					String.valueOf(rate));
			final String result = stat.toString();
			if (attacker.isDebug())
			{
				attacker.sendDebugMessage(result);
			}
			if (Config.DEVELOPER)
			{
				Log.info(result);
			}
		}
		return Rnd.get(100) < rate;
	}

	public static double calcManaDam(L2Character attacker, L2Character target, L2Skill skill, double ssMul)
	{
		// AI SpiritShot
		if (attacker instanceof L2Npc)
		{
			if (((L2Npc) attacker)._spiritshotcharged)
			{
				ssMul = L2ItemInstance.CHARGED_SPIRITSHOT;
			}
			else
			{
				ssMul = L2ItemInstance.CHARGED_NONE;
			}
			((L2Npc) attacker)._spiritshotcharged = false;
		}
		//Mana Burnt = (SQR(M.Atk)*Power*(Target Max MP/97))/M.Def
		double mAtk = attacker.getMAtk(target, skill);
		double mDef = target.getMDef(attacker, skill);
		final boolean isPvP = attacker instanceof L2Playable && target instanceof L2Playable;
		final boolean isPvE = attacker instanceof L2Playable && target instanceof L2Attackable;
		double mp = target.getMaxMp();
		mAtk *= ssMul;

		double damage = Math.sqrt(mAtk) * skill.getPower(attacker, target, isPvP, isPvE) * (mp / 97) / mDef;
		damage *= 1.0 - calcSkillResistance(attacker, target, skill) / 100;
		if (target instanceof L2Attackable)
		{
			if (!target.isRaid() && !target.isRaidMinion() && target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY &&
					attacker.getActingPlayer() != null &&
					target.getLevel() - attacker.getActingPlayer().getLevel() >= 2)
			{
				int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
				if (lvlDiff > Config.NPC_SKILL_DMG_PENALTY.size())
				{
					damage *= Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size());
				}
				else
				{
					damage *= Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
				}
			}
		}

		return damage;
	}

	public static double calculateSkillResurrectRestorePercent(double baseRestorePercent, L2Character caster)
	{
		if (baseRestorePercent == 0 || baseRestorePercent == 100)
		{
			return baseRestorePercent;
		}

		double restorePercent = baseRestorePercent * BaseStats.WIT.calcBonus(caster);
		if (restorePercent - baseRestorePercent > 20.0)
		{
			restorePercent += 20.0;
		}

		restorePercent = Math.max(restorePercent, baseRestorePercent);
		restorePercent = Math.min(restorePercent, 90.0);

		return restorePercent;
	}

	public static boolean calcPhysicalSkillEvasion(L2Character attacker, L2Character target, L2Skill skill)
	{
		if (skill.isMagic() && skill.getSkillType() != L2SkillType.BLOW)
		{
			return false;
		}

		return Rnd.get(100) < target.calcStat(Stats.P_SKILL_EVASION, 0, null, skill);
	}

	public static boolean calcMagicalSkillEvasion(L2Character attacker, L2Character target, L2Skill skill)
	{
		/*if (skill.isDebuff())
			return false;*/

		if (calcMagicMiss(attacker, target))
		{
			return true;
		}

		return Rnd.get(100) < target.calcStat(Stats.M_SKILL_EVASION, 0, null, skill);
	}

	public static boolean calcSkillMastery(L2Character actor, L2Skill sk)
	{
		if (sk.getSkillType() == L2SkillType.FISHING || sk.getId() == 10786) // Time Bomb
		{
			return false;
		}

		return Rnd.get(100) < actor.getSkillMastery();
	}

	public static double calcValakasAttribute(L2Character attacker, L2Character target, L2Skill skill)
	{
		double calcPower = 0;
		double calcDefen = 0;

		if (skill != null && skill.getAttributeName().contains("valakas"))
		{
			calcPower = attacker.calcStat(Stats.VALAKAS, calcPower, target, skill);
			calcDefen = target.calcStat(Stats.VALAKAS_RES, calcDefen, target, skill);
		}
		else
		{
			calcPower = attacker.calcStat(Stats.VALAKAS, calcPower, target, skill);
			if (calcPower > 0)
			{
				calcPower = attacker.calcStat(Stats.VALAKAS, calcPower, target, skill);
				calcDefen = target.calcStat(Stats.VALAKAS_RES, calcDefen, target, skill);
			}
		}
		return calcPower - calcDefen;
	}

	public static double calcElemental(L2Character attacker, L2Character target, L2Skill skill)
	{
		byte element = attacker.getAttackElement();
		int calcPower = attacker.getAttackElementValue(element);
		if (skill != null && skill.getElement() >= 0)
		{
			if (skill.getElement() != element)
			{
				element = skill.getElement();
				calcPower = skill.getElementPower();
			}
			else
			{
				calcPower += skill.getElementPower();
			}
		}

		if (element < 0)
		{
			element = target.getDefenseElement();
		}

		int calcDefen = target.getDefenseElementValue(element);

		int elemDiff = calcPower - calcDefen;
		double multiplier = 1.0;
		if (elemDiff > 0)
		{
			multiplier = 1.025 + Math.sqrt(Math.pow(elemDiff, 3) / 2) * 0.0001;
			if (multiplier > 1.25)
			{
				multiplier = 1.25;
			}
		}
		else if (elemDiff < 0)
		{
			multiplier = 0.975 - Math.sqrt(Math.pow(-elemDiff, 3) / 2) * 0.0001;
			if (multiplier < 0.75)
			{
				multiplier = 0.75;
			}
		}

		return multiplier;
	}

	/**
	 * Calculate skill reflection according these three possibilities:
	 * <li>Reflect failed</li>
	 * <li>Mormal reflect (just effects). <U>Only possible for skilltypes: BUFF, REFLECT, HEAL_PERCENT,
	 * MANAHEAL_PERCENT, HOT, CPHOT, MPHOT</U></li>
	 * <li>vengEance reflect (100% damage reflected but damage is also dealt to actor). <U>This is only possible
	 * for skills with skilltype PDAM, BLOW, CHARGEDAM, MDAM or DEATHLINK</U></li>
	 * <br><br>
	 *
	 * @param target
	 * @param skill
	 * @return SKILL_REFLECTED_FAILED, SKILL_REFLECT_SUCCEED or SKILL_REFLECT_VENGEANCE
	 */
	public static byte calcSkillReflect(L2Character target, L2Skill skill)
	{
		/*
		 *  Neither some special skills (like hero debuffs...) or those skills
		 *  ignoring resistances can be reflected
		 */
		if (skill.ignoreResists() || !skill.canBeReflected())
		{
			return SKILL_REFLECT_FAILED;
		}

		// only magic and melee skills can be reflected
		if (!skill.isMagic() && (skill.getCastRange() == -1 || skill.getCastRange() > MELEE_ATTACK_RANGE))
		{
			return SKILL_REFLECT_FAILED;
		}

		byte reflect = SKILL_REFLECT_FAILED;
		// check for non-reflected skilltypes, need additional retail check
		switch (skill.getSkillType())
		{
			case BUFF:
			case HEAL_PERCENT:
			case MANAHEAL_PERCENT:
			case UNDEAD_DEFENSE:
			case AGGDEBUFF:
			case CONT:
				return SKILL_REFLECT_FAILED;
			// these skill types can deal damage
			case PDAM:
			case MDAM:
			case BLOW:
			case DRAIN:
			case CHARGEDAM:
			case FATAL:
			case DEATHLINK:
			case CPDAM:
			case MANADAM:
			case CPDAMPERCENT:
			case MAXHPDAMPERCENT:
				final Stats stat =
						skill.isMagic() ? Stats.VENGEANCE_SKILL_MAGIC_DAMAGE : Stats.VENGEANCE_SKILL_PHYSICAL_DAMAGE;
				final double venganceChance = target.getStat().calcStat(stat, 0, target, skill);
				if (venganceChance > Rnd.get(100))
				{
					reflect |= SKILL_REFLECT_VENGEANCE;
				}
				break;
		}

		final double reflectChance = target.calcStat(Stats.REFLECT_DEBUFFS, 0, null, skill);
		if (Rnd.get(100) < reflectChance)
		{
			reflect |= SKILL_REFLECT_EFFECTS;
		}

		return reflect;
	}

	/**
	 * Calculate damage caused by falling
	 *
	 * @param cha
	 * @param fallHeight
	 * @return damage
	 */
	public static double calcFallDam(L2Character cha, int fallHeight)
	{
		if (!Config.ENABLE_FALLING_DAMAGE || fallHeight < 0)
		{
			return 0;
		}
		return cha.calcStat(Stats.FALL, fallHeight * cha.getMaxHp() / 1000, null, null);
	}

	private static double FRONT_MAX_ANGLE = 100;
	private static double BACK_MAX_ANGLE = 40;

	/**
	 * Calculates blow success depending on base chance and relative position of attacker and target
	 *
	 * @param activeChar Target that is performing skill
	 * @param target     Target of this skill
	 * @param skill      Skill which will be used to get base value of blowChance and crit condition
	 * @return Success of blow
	 */
	public static boolean calcBlowSuccess(L2Character activeChar, L2Character target, L2Skill skill)
	{
		int blowChance = skill.getBlowChance();

		// Skill is blow and it has 0% to make dmg... thats just wrong
		if (blowChance == 0)
		{
			Log.log(Level.WARNING, "Skill " + skill.getId() + " - " + skill.getName() +
					" has 0 blow land chance, yet its a blow skill!");
			return false;
		}

		if (!isBehind(target, activeChar))
		{
			if (skill.getId() == 10508 || skill.getId() == 30)
			{
				return false;
			}
		}

		if (isBehind(target, activeChar))
		{
			if ((skill.getCondition() & L2Skill.COND_FRONT) != 0)
			{
				return false;
			}

			blowChance *= 2; //double chance from behind
		}
		else if (isInFrontOf(target, activeChar))
		{
			if ((skill.getCondition() & L2Skill.COND_BEHIND) != 0)
			{
				return false;
			}

			//base chance from front
		}
		else
		{
			if ((skill.getCondition() & L2Skill.COND_BEHIND) != 0 || (skill.getCondition() & L2Skill.COND_FRONT) != 0)
			{
				return false;
			}

			blowChance *= 1.5; //50% better chance from side
		}

		double blowRate =
				activeChar.calcStat(Stats.BLOW_RATE, blowChance * (1.0 + activeChar.getDEX() / 100.0), target, null);
		return blowRate > Rnd.get(100);
	}

	/**
	 * Those are altered formulas for blow lands
	 * Return True if the target is IN FRONT of the L2Character.<BR><BR>
	 */
	public static boolean isInFrontOf(L2Character target, L2Character attacker)
	{
		if (target == null)
		{
			return false;
		}

		if (attacker.calcStat(Stats.IS_BEHIND, 0, attacker, null) > 0)
		{
			return false;
		}

		double angleChar, angleTarget, angleDiff;
		angleTarget = Util.calculateAngleFrom(target, attacker);
		angleChar = Util.convertHeadingToDegree(target.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= -360 + FRONT_MAX_ANGLE)
		{
			angleDiff += 360;
		}
		if (angleDiff >= 360 - FRONT_MAX_ANGLE)
		{
			angleDiff -= 360;
		}
		return Math.abs(angleDiff) <= FRONT_MAX_ANGLE;
	}

	/**
	 * Those are altered formulas for blow lands
	 * Return True if the L2Character is behind the target and can't be seen.<BR><BR>
	 */
	public static boolean isBehind(L2Character target, L2Character attacker)
	{
		if (target == null)
		{
			return false;
		}

		if (attacker.calcStat(Stats.IS_BEHIND, 0, attacker, null) > 0)
		{
			return true;
		}

		double angleChar, angleTarget, angleDiff;
		angleChar = Util.calculateAngleFrom(attacker, target);
		angleTarget = Util.convertHeadingToDegree(target.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= -360 + BACK_MAX_ANGLE)
		{
			angleDiff += 360;
		}
		if (angleDiff >= 360 - BACK_MAX_ANGLE)
		{
			angleDiff -= 360;
		}
		return Math.abs(angleDiff) <= BACK_MAX_ANGLE;
	}

	public static double damageMultiplier(L2Character attacker, L2Character target)
	{
		if (!Config.isServer(Config.TENKAI))
		{
			return 1.0;
		}

		double multiplier = 1.0;
		if (attacker.getActingPlayer() != null)
		{
			PlayerClass attackerClass = attacker.getActingPlayer().getCurrentClass();
			if (target.getActingPlayer() != null) // PvP Damage
			{
				multiplier *= 0.99;
				int attackerClanId = attacker.getActingPlayer().getClanId();
				L2Party attackerParty = attacker.getActingPlayer().getParty();
				int targetClanId = target.getActingPlayer().getClanId();
				L2Party targetParty = attacker.getActingPlayer().getParty();
				if (attackerClanId > 0 && targetClanId > 0 && attackerClanId != targetClanId && attackerParty != null &&
						targetParty != null && !attacker.isInsideZone(L2Character.ZONE_PVP))
				{
					int attackerClannies = 0;
					int targetClannies = 0;
					for (L2PcInstance clanny : attacker.getKnownList().getKnownPlayers().values())
					{
						if (clanny.getClanId() == attackerClanId || clanny.getParty() == attackerParty)
						{
							attackerClannies++;
						}
						else if (clanny.getClanId() == targetClanId || clanny.getParty() == targetParty)
						{
							targetClannies++;
						}
					}

					if (targetClannies > 2 && attackerClannies > targetClannies)
					{
						multiplier *= Math.pow(targetClannies / (double) attackerClannies, 1.15);
						//System.out.println(targetClannies + " " + attackerClannies + " " + multiplier);
					}
				}

				if (attacker.getActingPlayer().isPlayingEvent())
				{
					EventInstance event = attacker.getActingPlayer().getEvent();
					if (event.isType(EventType.Survival) || event.isType(EventType.TeamSurvival))
					{
						long seconds = (System.currentTimeMillis() - event.getStartTime()) / 1000;
						multiplier *= Math.pow(1.001, seconds);
					}
				}

				if (attackerClass.getParent() != null && attackerClass.getParent().getAwakeningClassId() > 0)
				{
					int awakening = attackerClass.getParent().getAwakeningClassId();
					L2Weapon weapon = attacker.getActiveWeaponItem();
					switch (awakening)
					{
						case 139: // Sigel Knight
							multiplier *= 1.2;
							if (weapon != null && (weapon.getItemType() == L2WeaponType.BOW ||
									weapon.getItemType() == L2WeaponType.CROSSBOW))
							{
								multiplier *= 0.3;
							}
							break;
						case 140: // Tyrr Warrior
							multiplier *= 0.95;
							break;
						case 141: // Othell Rogue
							if (weapon != null && (weapon.getItemType() == L2WeaponType.DAGGER ||
									weapon.getItemType() == L2WeaponType.DUALDAGGER))
							{
								multiplier *= 1.2;
							}
							break;
						case 142: // Yul Archer
							multiplier *= 1.05;
							break;
						case 143: // Feoh Wizard
							multiplier *= 1.3;
							break;
						case 144: // Iss Enchanter
							multiplier *= 1.5;
							break;
						case 145: // Wynn Summoner
							multiplier *= 1.7;
							break;
						case 146: // Aeore Healer
							multiplier *= 1.5;
							break;
					}
				}

				switch (attackerClass.getId())
				{
					case 152: // Tyrr Duelist
						multiplier *= 0.9;
						break;
					case 166: // Feoh Archmage
						multiplier *= 1.5;
						break;
					case 167: // Feoh Soultaker
						multiplier *= 0.9;
						break;
					case 168: // Feoh Mystic Muse
						multiplier *= 1.3;
						break;
					case 169: // Feoh Storm Screamer
						multiplier *= 0.9;
						break;
					case 188: // Eviscerator
						multiplier *= 1.2;
						break;
					case 189: // Sayha's Seer
						multiplier *= 1.2;
						break;
				}

				PlayerClass targetClass = target.getActingPlayer().getCurrentClass();
				if (targetClass.getParent() != null && targetClass.getParent().getAwakeningClassId() > 0)
				{
					int awakening = targetClass.getParent().getAwakeningClassId();
					switch (awakening)
					{
						case 139: // Sigel Knight
							multiplier *= 0.8;
							break;
						case 140: // Tyrr Warrior
							multiplier *= 1.1;
							break;
						case 141: // Othell Rogue
							multiplier *= 0.95;
							break;
						case 142: // Yul Archer
							//multiplier *= 1.4;
							break;
						case 143: // Feoh Wizard
							multiplier *= 0.85;
							break;
						case 144: // Iss Enchanter
							//multiplier *= 1.8;
							break;
						case 145: // Wynn Summoner
							multiplier *= 0.85;
							break;
						case 146: // Aeore Healer
							//multiplier *= 2.1;
							break;
					}
				}

				switch (targetClass.getId())
				{
					case 166: // Feoh Archmage
						multiplier *= 0.8;
						break;
					case 167: // Feoh Soultaker
						multiplier *= 1.2;
						break;
					case 188: // Eviscerator
						//multiplier *= 0.8;
						break;
					case 189: // Sayha's Seer
						//multiplier *= 1.4;
						break;
				}
			}
			else
			// PvE Damage
			{
				if (Config.isServer(Config.TENKAI) && target.getInstanceId() == 0 &&
						!Config.isServer(Config.TENKAI_ESTHUS))
				{
					multiplier *= 3.0f;
				}

				if (Config.isServer(Config.TENKAI_ESTHUS) && !target.isRaid())
				{
					multiplier *= 2.0f;
				}

				if (target.isRaid())
				{
					return multiplier;
				}

				if (attackerClass.getParent() != null && attackerClass.getParent().getAwakeningClassId() > 0)
				{
					int awakening = attackerClass.getParent().getAwakeningClassId();
					@SuppressWarnings("unused") L2Weapon weapon = attacker.getActiveWeaponItem();
					switch (awakening)
					{
						case 139: // Sigel Knight
							multiplier *= 1.6;
							break;
						case 140: // Tyrr Warrior
							multiplier *= 0.9;
							break;
						case 141: //Othell Rogue
							multiplier *= 1.5;
							break;
						case 142: //Yul Archer
							multiplier *= 1.2;
							break;
						case 143: //Feoh Wizard
							multiplier *= 1.5;
							break;
						case 144: // Iss Enchanter
							multiplier *= 1.5;
							break;
						case 145: // Wynn Summoner
							multiplier *= 1.5;
							break;
						case 146: // Aeore Healer
							multiplier *= 2.3;
							break;
					}
				}

				switch (attackerClass.getId())
				{
					case 166: // Feoh Archmage
						multiplier *= 1.4;
						break;
					case 167: // Feoh Soultaker
						multiplier *= 0.9;
						break;
					case 188: // Eviscerator
						multiplier *= 1.0;
						break;
					case 189: // Sayha's Seer
						multiplier *= 1.3;
						break;
				}
			}
		}

		return multiplier;
	}
}
