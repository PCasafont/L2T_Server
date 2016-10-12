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
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.zone.type.L2SwampZone;
import l2server.log.Log;

public class PlayableStat extends CharStat
{

	private static final long MAX_SP = 50000000000L;

	public PlayableStat(L2Playable activeChar)
	{
		super(activeChar);
	}

	public boolean addExp(long value)
	{
		long expForMaxLevel = getExpForLevel(getMaxLevel() + 1);
		if (getExp() + value < 0 || value > 0 && getExp() == expForMaxLevel - 1)
		{
			return true;
		}

		if (getExp() + value >= expForMaxLevel)
		{
			value = expForMaxLevel - 1 - getExp();
		}

		setExp(getExp() + value);

		byte minimumLevel = 1;
		if (getActiveChar() instanceof L2PetInstance)
		{
			// get minimum level from L2NpcTemplate
			minimumLevel = (byte) PetDataTable.getInstance()
					.getPetMinLevel(((L2PetInstance) getActiveChar()).getTemplate().NpcId);
		}

		byte level = minimumLevel; // minimum level

		for (byte tmp = level; tmp <= getMaxLevel() + 1; tmp++)
		{
			if (getExp() >= getExpForLevel(tmp))
			{
				continue;
			}

			level = --tmp;
			break;
		}
		if (level != getLevel() && level >= minimumLevel)
		{
			addLevel((byte) (level - getLevel()));
		}

		return true;
	}

	public boolean removeExp(long value)
	{
		if (getExp() - value < 0)
		{
			value = getExp() - 1;
		}

		setExp(getExp() - value);

		byte minimumLevel = 1;
		if (getActiveChar() instanceof L2PetInstance)
		{
			// get minimum level from L2NpcTemplate
			minimumLevel = (byte) PetDataTable.getInstance()
					.getPetMinLevel(((L2PetInstance) getActiveChar()).getTemplate().NpcId);
		}
		byte level = minimumLevel;

		for (byte tmp = level; tmp <= getMaxLevel() + 1; tmp++)
		{
			if (getExp() >= getExpForLevel(tmp))
			{
				continue;
			}
			level = --tmp;
			break;
		}
		if (level != getLevel() && level >= minimumLevel)
		{
			addLevel((byte) (level - getLevel()));
		}
		return true;
	}

	public boolean addExpAndSp(long addToExp, long addToSp)
	{
		boolean expAdded = false;
		boolean spAdded = false;
		if (addToExp >= 0)
		{
			expAdded = addExp(addToExp);
		}
		if (addToSp >= 0)
		{
			spAdded = addSp(addToSp);
		}

		return expAdded || spAdded;
	}

	public boolean removeExpAndSp(long removeExp, long removeSp)
	{
		boolean expRemoved = false;
		boolean spRemoved = false;
		if (removeExp > 0)
		{
			expRemoved = removeExp(removeExp);
		}
		if (removeSp > 0)
		{
			spRemoved = removeSp(removeSp);
		}

		return expRemoved || spRemoved;
	}

	public boolean addLevel(byte value)
	{
		if (getLevel() + value > getMaxLevel())
		{
			if (getLevel() < getMaxLevel())
			{
				value = (byte) (getMaxLevel() - getLevel());
			}
			else
			{
				return false;
			}
		}

		boolean levelIncreased = getLevel() + value > getLevel();
		value += getLevel();
		setLevel(value);

		// Sync up exp with current level
		if (getExp() >= getExpForLevel(getLevel() + 1) || getExpForLevel(getLevel()) > getExp())
		{
			setExp(getExpForLevel(getLevel()));
		}

		if (!levelIncreased && getActiveChar() instanceof L2PcInstance && !getActiveChar().isGM() &&
				Config.DECREASE_SKILL_LEVEL)
		{
			((L2PcInstance) getActiveChar()).checkPlayerSkills();
		}

		if (!levelIncreased)
		{
			return false;
		}

		getActiveChar().getStatus().setCurrentHp(getActiveChar().getStat().getMaxHp());
		getActiveChar().getStatus().setCurrentMp(getActiveChar().getStat().getMaxMp());

		return true;
	}

	public boolean addSp(long value)
	{
		if (value < 0)
		{
			Log.warning("wrong usage");
			return false;
		}

		long currentSp = getSp();
		if (currentSp == MAX_SP)
		{
			return false;
		}

		if (currentSp > MAX_SP - value)
		{
			value = MAX_SP - currentSp;
		}

		setSp(currentSp + value);
		return true;
	}

	public boolean removeSp(long value)
	{
		long currentSp = getSp();
		if (currentSp < value)
		{
			value = currentSp;
		}
		setSp(getSp() - value);
		return true;
	}

	public long getExpForLevel(int level)
	{
		return level;
	}

	@Override
	public int getRunSpeed()
	{
		int val = super.getRunSpeed();
		if (getActiveChar().isInsideZone(L2Character.ZONE_WATER))
		{
			val /= 2;
		}

		if (getActiveChar().isInsideZone(L2Character.ZONE_SWAMP))
		{
			L2SwampZone zone = ZoneManager.getInstance().getZone(getActiveChar(), L2SwampZone.class);
			int bonus = zone == null ? 0 : zone.getMoveBonus();
			double dbonus = bonus / 100.0; //%
			val += val * dbonus;
		}

		return val;
	}

	@Override
	public L2Playable getActiveChar()
	{
		return (L2Playable) super.getActiveChar();
	}

	public int getMaxLevel()
	{
		return Config.MAX_LEVEL;
	}
}
