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

package l2server.gameserver.model;

/**
 * Stats definition for each pet level
 *
 * @author JIV
 */
public class L2PetLevelData
{
	private int ownerExpTaken;
	private long petMaxExp;
	private int petMaxHP;
	private int petMaxMP;
	private int petPAtk;
	private int petPDef;
	private int petMAtk;
	private int petMDef;
	private int petMaxFeed;
	private int petFeedBattle;
	private int petFeedNormal;
	private int petRegenHP;
	private int petRegenMP;
	private short petSoulShot;
	private short petSpiritShot;

	//  Max Exp
	public long getPetMaxExp()
	{
		return this.petMaxExp;
	}

	public void setPetMaxExp(long pPetMaxExp)
	{
		this.petMaxExp = pPetMaxExp;
	}

	public int getOwnerExpTaken()
	{
		return this.ownerExpTaken;
	}

	public void setOwnerExpTaken(int pOwnerExpTaken)
	{
		this.ownerExpTaken = pOwnerExpTaken;
	}

	//  Max HP
	public int getPetMaxHP()
	{
		return this.petMaxHP;
	}

	public void setPetMaxHP(int pPetMaxHP)
	{
		this.petMaxHP = pPetMaxHP;
	}

	//  Max Mp
	public int getPetMaxMP()
	{
		return this.petMaxMP;
	}

	public void setPetMaxMP(int pPetMaxMP)
	{
		this.petMaxMP = pPetMaxMP;
	}

	//  PAtk
	public int getPetPAtk()
	{
		return this.petPAtk;
	}

	public void setPetPAtk(int pPetPAtk)
	{
		this.petPAtk = pPetPAtk;
	}

	//  PDef
	public int getPetPDef()
	{
		return this.petPDef;
	}

	public void setPetPDef(int pPetPDef)
	{
		this.petPDef = pPetPDef;
	}

	//  MAtk
	public int getPetMAtk()
	{
		return this.petMAtk;
	}

	public void setPetMAtk(int pPetMAtk)
	{
		this.petMAtk = pPetMAtk;
	}

	//  MDef
	public int getPetMDef()
	{
		return this.petMDef;
	}

	public void setPetMDef(int pPetMDef)
	{
		this.petMDef = pPetMDef;
	}

	//  MaxFeed
	public int getPetMaxFeed()
	{
		return this.petMaxFeed;
	}

	public void setPetMaxFeed(int pPetMaxFeed)
	{
		this.petMaxFeed = pPetMaxFeed;
	}

	//  Normal Feed
	public int getPetFeedNormal()
	{
		return this.petFeedNormal;
	}

	public void setPetFeedNormal(int pPetFeedNormal)
	{
		this.petFeedNormal = pPetFeedNormal;
	}

	//  Battle Feed
	public int getPetFeedBattle()
	{
		return this.petFeedBattle;
	}

	public void setPetFeedBattle(int pPetFeedBattle)
	{
		this.petFeedBattle = pPetFeedBattle;
	}

	//  Regen HP
	public int getPetRegenHP()
	{
		return this.petRegenHP;
	}

	public void setPetRegenHP(int pPetRegenHP)
	{
		this.petRegenHP = pPetRegenHP;
	}

	//  Regen MP
	public int getPetRegenMP()
	{
		return this.petRegenMP;
	}

	public void setPetRegenMP(int pPetRegenMP)
	{
		this.petRegenMP = pPetRegenMP;
	}

	/**
	 * @return the _petSoulShot
	 */
	public short getPetSoulShot()
	{
		return this.petSoulShot;
	}

	/**
	 * @param soulShot the this.petSoulShot to set
	 */
	public void setPetSoulShot(short soulShot)
	{
		this.petSoulShot = soulShot;
	}

	/**
	 * @return the _petSpiritShot
	 */
	public short getPetSpiritShot()
	{
		return this.petSpiritShot;
	}

	/**
	 * @param spiritShot the this.petSpiritShot to set
	 */
	public void setPetSpiritShot(short spiritShot)
	{
		this.petSpiritShot = spiritShot;
	}
}
