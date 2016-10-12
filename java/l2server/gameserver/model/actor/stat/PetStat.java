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

package l2server.gameserver.model.actor.stat;

import l2server.Config;
import l2server.gameserver.datatables.PetDataTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SocialAction;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Stats;
import l2server.log.Log;

public class PetStat extends SummonStat
{
	public PetStat(L2PetInstance activeChar)
	{
		super(activeChar);
	}

	public boolean addExp(int value)
	{
		if (!super.addExp(value))
		{
			return false;
		}

		getActiveChar().updateAndBroadcastStatus(1);
		// The PetInfo packet wipes the PartySpelled (list of active  spells' icons).  Re-add them
		getActiveChar().updateEffectIcons(true);

		return true;
	}

	@Override
	public boolean addExpAndSp(long addToExp, long addToSp)
	{
		if (!super.addExpAndSp(addToExp, addToSp))
		{
			return false;
		}

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_EARNED_S1_EXP);
		sm.addItemNumber(addToExp);
		getActiveChar().updateAndBroadcastStatus(1);
		getActiveChar().getOwner().sendPacket(sm);

		return true;
	}

	@Override
	public final boolean addLevel(byte value)
	{
		if (getLevel() + value > getMaxLevel())
		{
			return false;
		}

		boolean levelIncreased = super.addLevel(value);

		// Sync up exp with current level
		//if (getExp() > getExpForLevel(getLevel() + 1) || getExp() < getExpForLevel(getLevel())) setExp(Experience.LEVEL(getLevel()));

		//TODO : proper system msg if is any
		//if (levelIncreased) getActiveChar().getOwner().sendMessage("Your pet has increased it's level.");

		StatusUpdate su = new StatusUpdate(getActiveChar());
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		getActiveChar().broadcastPacket(su);
		if (levelIncreased)
		{
			getActiveChar().broadcastPacket(new SocialAction(getActiveChar().getObjectId(), SocialAction.LEVEL_UP));
		}
		// Send a Server->Client packet PetInfo to the L2PcInstance
		getActiveChar().updateAndBroadcastStatus(1);

		if (getActiveChar().getControlItem() != null)
		{
			getActiveChar().getControlItem().setEnchantLevel(getLevel());
		}

		return levelIncreased;
	}

	@Override
	public final long getExpForLevel(int level)
	{
		try
		{
			return PetDataTable.getInstance().getPetLevelData(getActiveChar().getNpcId(), level).getPetMaxExp();
		}
		catch (NullPointerException e)
		{
			if (getActiveChar() != null)
			{
				Log.warning("Pet objectId:" + getActiveChar().getObjectId() + ", NpcId:" + getActiveChar().getNpcId() +
						", level:" + level + " is missing data from pets_stats table!");
			}
			throw e;
		}
	}

	@Override
	public L2PetInstance getActiveChar()
	{
		return (L2PetInstance) super.getActiveChar();
	}

	public final int getFeedBattle()
	{
		return getActiveChar().getPetLevelData().getPetFeedBattle();
	}

	public final int getFeedNormal()
	{
		return getActiveChar().getPetLevelData().getPetFeedNormal();
	}

	@Override
	public void setLevel(byte value)
	{
		getActiveChar()
				.setPetData(PetDataTable.getInstance().getPetLevelData(getActiveChar().getTemplate().NpcId, value));
		if (getActiveChar().getPetLevelData() == null)
		{
			throw new IllegalArgumentException(
					"No pet data for npc: " + getActiveChar().getTemplate().NpcId + " level: " + value);
		}
		getActiveChar().stopFeed();
		super.setLevel(value);

		getActiveChar().startFeed();

		if (getActiveChar().getControlItem() != null)
		{
			getActiveChar().getControlItem().setEnchantLevel(getLevel());
		}
	}

	public final int getMaxFeed()
	{
		return getActiveChar().getPetLevelData().getPetMaxFeed();
	}

	@Override
	public int getMaxVisibleHp()
	{
		return (int) calcStat(Stats.MAX_HP, getActiveChar().getPetLevelData().getPetMaxHP(), null, null);
	}

	@Override
	public int getMaxMp()
	{
		return (int) calcStat(Stats.MAX_MP, getActiveChar().getPetLevelData().getPetMaxMP(), null, null);
	}

	@Override
	public int getMAtk(L2Character target, L2Skill skill)
	{
		double attack = getActiveChar().getPetLevelData().getPetMAtk();
		if (skill != null)
		{
			attack += skill.getPower();
		}

		return (int) calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
	}

	@Override
	public int getMDef(L2Character target, L2Skill skill)
	{
		double defence = getActiveChar().getPetLevelData().getPetMDef();
		return (int) calcStat(Stats.MAGIC_DEFENSE, defence, target, skill);
	}

	@Override
	public int getPAtk(L2Character target)
	{
		return (int) calcStat(Stats.PHYS_ATTACK, getActiveChar().getPetLevelData().getPetPAtk(), target, null);
	}

	@Override
	public int getPDef(L2Character target)
	{
		return (int) calcStat(Stats.PHYS_DEFENSE, getActiveChar().getPetLevelData().getPetPDef(), target, null);
	}

	@Override
	public int getPAtkSpd()
	{
		int val = super.getPAtkSpd();
		if (getActiveChar().isHungry())
		{
			val = val / 2;
		}
		return val;
	}

	@Override
	public int getMAtkSpd()
	{
		int val = super.getMAtkSpd();
		if (getActiveChar().isHungry())
		{
			val = val / 2;
		}
		return val;
	}

	@Override
	public int getMaxLevel()
	{
		return Config.MAX_PET_LEVEL;
	}
}
