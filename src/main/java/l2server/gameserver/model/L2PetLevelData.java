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
		return petMaxExp;
	}

	public void setPetMaxExp(long pPetMaxExp)
	{
		petMaxExp = pPetMaxExp;
	}

	public int getOwnerExpTaken()
	{
		return ownerExpTaken;
	}

	public void setOwnerExpTaken(int pOwnerExpTaken)
	{
		ownerExpTaken = pOwnerExpTaken;
	}

	//  Max HP
	public int getPetMaxHP()
	{
		return petMaxHP;
	}

	public void setPetMaxHP(int pPetMaxHP)
	{
		petMaxHP = pPetMaxHP;
	}

	//  Max Mp
	public int getPetMaxMP()
	{
		return petMaxMP;
	}

	public void setPetMaxMP(int pPetMaxMP)
	{
		petMaxMP = pPetMaxMP;
	}

	//  PAtk
	public int getPetPAtk()
	{
		return petPAtk;
	}

	public void setPetPAtk(int pPetPAtk)
	{
		petPAtk = pPetPAtk;
	}

	//  PDef
	public int getPetPDef()
	{
		return petPDef;
	}

	public void setPetPDef(int pPetPDef)
	{
		petPDef = pPetPDef;
	}

	//  MAtk
	public int getPetMAtk()
	{
		return petMAtk;
	}

	public void setPetMAtk(int pPetMAtk)
	{
		petMAtk = pPetMAtk;
	}

	//  MDef
	public int getPetMDef()
	{
		return petMDef;
	}

	public void setPetMDef(int pPetMDef)
	{
		petMDef = pPetMDef;
	}

	//  MaxFeed
	public int getPetMaxFeed()
	{
		return petMaxFeed;
	}

	public void setPetMaxFeed(int pPetMaxFeed)
	{
		petMaxFeed = pPetMaxFeed;
	}

	//  Normal Feed
	public int getPetFeedNormal()
	{
		return petFeedNormal;
	}

	public void setPetFeedNormal(int pPetFeedNormal)
	{
		petFeedNormal = pPetFeedNormal;
	}

	//  Battle Feed
	public int getPetFeedBattle()
	{
		return petFeedBattle;
	}

	public void setPetFeedBattle(int pPetFeedBattle)
	{
		petFeedBattle = pPetFeedBattle;
	}

	//  Regen HP
	public int getPetRegenHP()
	{
		return petRegenHP;
	}

	public void setPetRegenHP(int pPetRegenHP)
	{
		petRegenHP = pPetRegenHP;
	}

	//  Regen MP
	public int getPetRegenMP()
	{
		return petRegenMP;
	}

	public void setPetRegenMP(int pPetRegenMP)
	{
		petRegenMP = pPetRegenMP;
	}

	/**
	 * @return the petSoulShot
	 */
	public short getPetSoulShot()
	{
		return petSoulShot;
	}

	/**
	 * @param soulShot the petSoulShot to set
	 */
	public void setPetSoulShot(short soulShot)
	{
		petSoulShot = soulShot;
	}

	/**
	 * @return the petSpiritShot
	 */
	public short getPetSpiritShot()
	{
		return petSpiritShot;
	}

	/**
	 * @param spiritShot the petSpiritShot to set
	 */
	public void setPetSpiritShot(short spiritShot)
	{
		petSpiritShot = spiritShot;
	}
}
