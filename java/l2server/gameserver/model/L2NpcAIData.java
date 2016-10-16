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
import lombok.Getter;
import lombok.Setter;

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
	@Setter private boolean canMove;
	private int soulshot;
	private int spiritshot;
	private int soulshotchance;
	private int spiritshotchance;
	private int ischaos;
	@Getter private String clan = null;
	@Getter @Setter private int clanRange;
	@Getter private String enemyClan = null;
	@Getter @Setter private int enemyRange;
	//private int baseShldRate;
	//private int baseShldDef;
	@Getter @Setter private int dodge;
	private int longrangeskill;
	private int shortrangeskill;
	private int longrangechance;
	private int shortrangechance;
	private int switchrangechance;
	@Setter private int minSocial1;
	@Setter private int maxSocial1;
	@Setter private int minSocial2;
	@Setter private int maxSocial2;
	@Getter private AIType aiType = AIType.FIGHTER;

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


	public void setEnemyClan(String enemyClan)
	{
		if (enemyClan != null && !enemyClan.equals("") && !enemyClan.equalsIgnoreCase("null"))
		{
			this.enemyClan = enemyClan.intern();
		}
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






	public int getMinSocial(boolean second)
	{
		return !second ? minSocial1 : minSocial2;
	}

	public int getMaxSocial(boolean second)
	{
		return !second ? maxSocial1 : maxSocial2;
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
