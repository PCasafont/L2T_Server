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
	private int _ownerExpTaken;
	private long _petMaxExp;
	private int _petMaxHP;
	private int _petMaxMP;
	private int _petPAtk;
	private int _petPDef;
	private int _petMAtk;
	private int _petMDef;
	private int _petMaxFeed;
	private int _petFeedBattle;
	private int _petFeedNormal;
	private int _petRegenHP;
	private int _petRegenMP;
	private short _petSoulShot;
	private short _petSpiritShot;

	//  Max Exp
	public long getPetMaxExp()
	{
		return _petMaxExp;
	}

	public void setPetMaxExp(long pPetMaxExp)
	{
		_petMaxExp = pPetMaxExp;
	}

	public int getOwnerExpTaken()
	{
		return _ownerExpTaken;
	}

	public void setOwnerExpTaken(int pOwnerExpTaken)
	{
		_ownerExpTaken = pOwnerExpTaken;
	}

	//  Max HP
	public int getPetMaxHP()
	{
		return _petMaxHP;
	}

	public void setPetMaxHP(int pPetMaxHP)
	{
		_petMaxHP = pPetMaxHP;
	}

	//  Max Mp
	public int getPetMaxMP()
	{
		return _petMaxMP;
	}

	public void setPetMaxMP(int pPetMaxMP)
	{
		_petMaxMP = pPetMaxMP;
	}

	//  PAtk
	public int getPetPAtk()
	{
		return _petPAtk;
	}

	public void setPetPAtk(int pPetPAtk)
	{
		_petPAtk = pPetPAtk;
	}

	//  PDef
	public int getPetPDef()
	{
		return _petPDef;
	}

	public void setPetPDef(int pPetPDef)
	{
		_petPDef = pPetPDef;
	}

	//  MAtk
	public int getPetMAtk()
	{
		return _petMAtk;
	}

	public void setPetMAtk(int pPetMAtk)
	{
		_petMAtk = pPetMAtk;
	}

	//  MDef
	public int getPetMDef()
	{
		return _petMDef;
	}

	public void setPetMDef(int pPetMDef)
	{
		_petMDef = pPetMDef;
	}

	//  MaxFeed
	public int getPetMaxFeed()
	{
		return _petMaxFeed;
	}

	public void setPetMaxFeed(int pPetMaxFeed)
	{
		_petMaxFeed = pPetMaxFeed;
	}

	//  Normal Feed
	public int getPetFeedNormal()
	{
		return _petFeedNormal;
	}

	public void setPetFeedNormal(int pPetFeedNormal)
	{
		_petFeedNormal = pPetFeedNormal;
	}

	//  Battle Feed
	public int getPetFeedBattle()
	{
		return _petFeedBattle;
	}

	public void setPetFeedBattle(int pPetFeedBattle)
	{
		_petFeedBattle = pPetFeedBattle;
	}

	//  Regen HP
	public int getPetRegenHP()
	{
		return _petRegenHP;
	}

	public void setPetRegenHP(int pPetRegenHP)
	{
		_petRegenHP = pPetRegenHP;
	}

	//  Regen MP
	public int getPetRegenMP()
	{
		return _petRegenMP;
	}

	public void setPetRegenMP(int pPetRegenMP)
	{
		_petRegenMP = pPetRegenMP;
	}

	/**
	 * @return the _petSoulShot
	 */
	public short getPetSoulShot()
	{
		return _petSoulShot;
	}

	/**
	 * @param soulShot the _petSoulShot to set
	 */
	public void setPetSoulShot(short soulShot)
	{
		_petSoulShot = soulShot;
	}

	/**
	 * @return the _petSpiritShot
	 */
	public short getPetSpiritShot()
	{
		return _petSpiritShot;
	}

	/**
	 * @param spiritShot the _petSpiritShot to set
	 */
	public void setPetSpiritShot(short spiritShot)
	{
		_petSpiritShot = spiritShot;
	}
}
