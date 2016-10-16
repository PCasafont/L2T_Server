/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */

package l2server.gameserver.model;

import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.L2NpcTemplate.AIType;

/**
 * Author: ShanSoft
 * By L2JTW
 */

// This Data is for NPC Attributes and AI relate stuffs...
// Still need to finish...Update later...
public class L2NpcAIData
{
	//Basic AI
	private int _primary_attack;
	private int _skill_chance;
	private boolean canMove;
	private int soulshot;
	private int spiritshot;
	private int soulshotchance;
	private int spiritshotchance;
	private int ischaos;
	private String clan = null;
	private int clanRange;
	private String enemyClan = null;
	private int enemyRange;
	//private int baseShldRate;
	//private int baseShldDef;
	private int dodge;
	private int longrangeskill;
	private int shortrangeskill;
	private int longrangechance;
	private int shortrangechance;
	private int switchrangechance;
	private int minSocial1;
	private int maxSocial1;
	private int minSocial2;
	private int maxSocial2;
	private AIType aiType = AIType.FIGHTER;

	public L2NpcAIData()
	{
	}

	public L2NpcAIData(StatsSet set)
	{
		setAi(set.getString("aiType"));
		setPrimaryAttack(set.getInteger("primaryAttack", 0));
		setSkillChance(set.getInteger("skillChance", 0));
		setCanMove(set.getBool("canMove", true));
		setSoulShot(set.getInteger("soulshots", 0));
		setSpiritShot(set.getInteger("spiritshots", 0));
		setSoulShotChance(set.getInteger("ssChance", 0));
		setSpiritShotChance(set.getInteger("spsChance", 0));
		setIsChaos(set.getInteger("isChaos", 0));
		setShortRangeSkill(set.getInteger("minrangeskill", 0));
		setShortRangeChance(set.getInteger("minrangechance", 0));
		setLongRangeSkill(set.getInteger("maxrangeskill", 0));
		setLongRangeChance(set.getInteger("maxrangechance", 0));
		setClan(set.getString("clan", ""));
		setClanRange(set.getInteger("clanRange", 0));
		setEnemyClan(set.getString("enemyClan", ""));
		setEnemyRange(set.getInteger("enemyRange", 0));
		setDodge(set.getInteger("dodge", 0));
		setMinSocial1(set.getInteger("minSocial1", -1));
		setMaxSocial1(set.getInteger("maxSocial1", -1));
		setMinSocial2(set.getInteger("minSocial2", -1));
		setMaxSocial2(set.getInteger("maxSocial2", -1));
	}

	//--------------------------------------------------------------------------------------------------------------
	//Setting....
	//--------------------------------------------------------------------------------------------------------------
	public void setPrimaryAttack(int primaryattack)
	{
		_primary_attack = primaryattack;
	}

	public void setSkillChance(int skill_chance)
	{
		_skill_chance = skill_chance;
	}

	public void setCanMove(boolean canMove)
	{
		this.canMove = canMove;
	}

	public void setSoulShot(int soulshot)
	{
		this.soulshot = soulshot;
	}

	public void setSpiritShot(int spiritshot)
	{
		this.spiritshot = spiritshot;
	}

	public void setSoulShotChance(int soulshotchance)
	{
		this.soulshotchance = soulshotchance;
	}

	public void setSpiritShotChance(int spiritshotchance)
	{
		this.spiritshotchance = spiritshotchance;
	}

	public void setShortRangeSkill(int shortrangeskill)
	{
		this.shortrangeskill = shortrangeskill;
	}

	public void setShortRangeChance(int shortrangechance)
	{
		this.shortrangechance = shortrangechance;
	}

	public void setLongRangeSkill(int longrangeskill)
	{
		this.longrangeskill = longrangeskill;
	}

	public void setLongRangeChance(int longrangechance)
	{
		shortrangechance = longrangechance;
	}

	public void setSwitchRangeChance(int switchrangechance)
	{
		this.switchrangechance = switchrangechance;
	}

	public void setIsChaos(int ischaos)
	{
		this.ischaos = ischaos;
	}

	public void setClan(String clan)
	{
		if (clan != null && !clan.equals("") && !clan.equalsIgnoreCase("null"))
		{
			this.clan = clan.intern();
		}
	}

	public void setClanRange(int clanRange)
	{
		this.clanRange = clanRange;
	}

	public void setEnemyClan(String enemyClan)
	{
		if (enemyClan != null && !enemyClan.equals("") && !enemyClan.equalsIgnoreCase("null"))
		{
			this.enemyClan = enemyClan.intern();
		}
	}

	public void setEnemyRange(int enemyRange)
	{
		this.enemyRange = enemyRange;
	}

	public void setDodge(int dodge)
	{
		this.dodge = dodge;
	}

	public void setMinSocial1(int minSocial1)
	{
		this.minSocial1 = minSocial1;
	}

	public void setMaxSocial1(int maxSocial1)
	{
		this.maxSocial1 = maxSocial1;
	}

	public void setMinSocial2(int minSocial2)
	{
		this.minSocial2 = minSocial2;
	}

	public void setMaxSocial2(int maxSocial2)
	{
		this.maxSocial2 = maxSocial2;
	}

	public void setAi(String ai)
	{
		if (ai.equalsIgnoreCase("archer"))
		{
			aiType = AIType.ARCHER;
		}
		else if (ai.equalsIgnoreCase("balanced"))
		{
			aiType = AIType.BALANCED;
		}
		else if (ai.equalsIgnoreCase("mage"))
		{
			aiType = AIType.MAGE;
		}
		else if (ai.equalsIgnoreCase("healer"))
		{
			aiType = AIType.HEALER;
		}
		else if (ai.equalsIgnoreCase("corpse"))
		{
			aiType = AIType.CORPSE;
		}
		else
		{
			aiType = AIType.FIGHTER;
		}
	}

	/*

	public void setBaseShldRate (int baseShldRate)
	{
		this.baseShldRate = baseShldRate;
	}

	public void setBaseShldDef (int baseShldDef)
	{
		this.baseShldDef = baseShldDef;
	}
	 */

	//--------------------------------------------------------------------------------------------------------------
	//Data Recall....
	//--------------------------------------------------------------------------------------------------------------
	public int getPrimaryAttack()
	{
		return _primary_attack;
	}

	public int getSkillChance()
	{
		return _skill_chance;
	}

	public boolean canMove()
	{
		return canMove;
	}

	public int getSoulShot()
	{
		return soulshot;
	}

	public int getSpiritShot()
	{
		return spiritshot;
	}

	public int getSoulShotChance()
	{
		return soulshotchance;
	}

	public int getSpiritShotChance()
	{
		return spiritshotchance;
	}

	public int getShortRangeSkill()
	{
		return shortrangeskill;
	}

	public int getShortRangeChance()
	{
		return shortrangechance;
	}

	public int getLongRangeSkill()
	{
		return longrangeskill;
	}

	public int getLongRangeChance()
	{
		return longrangechance;
	}

	public int getSwitchRangeChance()
	{
		return switchrangechance;
	}

	public int getIsChaos()
	{
		return ischaos;
	}

	public String getClan()
	{
		return clan;
	}

	public int getClanRange()
	{
		return clanRange;
	}

	public String getEnemyClan()
	{
		return enemyClan;
	}

	public int getEnemyRange()
	{
		return enemyRange;
	}

	public int getDodge()
	{
		return dodge;
	}

	public int getMinSocial(boolean second)
	{
		return !second ? minSocial1 : minSocial2;
	}

	public int getMaxSocial(boolean second)
	{
		return !second ? maxSocial1 : maxSocial2;
	}

	public AIType getAiType()
	{
		return aiType;
	}

	/*

	public int getBaseShldRate ()
	{
		return this.baseShldRate;
	}

	public int getBaseShldDef ()
	{
		return this.baseShldDef;
	}
	 */
}
