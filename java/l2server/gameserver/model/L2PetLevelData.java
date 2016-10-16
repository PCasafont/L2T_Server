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

import lombok.Getter;
/**
 * Stats definition for each pet level
 *
 * @author JIV
 */
public class L2PetLevelData
{
	@Getter private int ownerExpTaken;
	@Getter private long petMaxExp;
	@Getter private int petMaxHP;
	@Getter private int petMaxMP;
	@Getter private int petPAtk;
	@Getter private int petPDef;
	@Getter private int petMAtk;
	@Getter private int petMDef;
	@Getter private int petMaxFeed;
	@Getter private int petFeedBattle;
	@Getter private int petFeedNormal;
	@Getter private int petRegenHP;
	@Getter private int petRegenMP;
	@Getter private short petSoulShot;
	@Getter private short petSpiritShot;

	//  Max Exp

	public void setPetMaxExp(long pPetMaxExp)
	{
		petMaxExp = pPetMaxExp;
	}


	public void setOwnerExpTaken(int pOwnerExpTaken)
	{
		ownerExpTaken = pOwnerExpTaken;
	}

	//  Max HP

	public void setPetMaxHP(int pPetMaxHP)
	{
		petMaxHP = pPetMaxHP;
	}

	//  Max Mp

	public void setPetMaxMP(int pPetMaxMP)
	{
		petMaxMP = pPetMaxMP;
	}

	//  PAtk

	public void setPetPAtk(int pPetPAtk)
	{
		petPAtk = pPetPAtk;
	}

	//  PDef

	public void setPetPDef(int pPetPDef)
	{
		petPDef = pPetPDef;
	}

	//  MAtk

	public void setPetMAtk(int pPetMAtk)
	{
		petMAtk = pPetMAtk;
	}

	//  MDef

	public void setPetMDef(int pPetMDef)
	{
		petMDef = pPetMDef;
	}

	//  MaxFeed

	public void setPetMaxFeed(int pPetMaxFeed)
	{
		petMaxFeed = pPetMaxFeed;
	}

	//  Normal Feed

	public void setPetFeedNormal(int pPetFeedNormal)
	{
		petFeedNormal = pPetFeedNormal;
	}

	//  Battle Feed

	public void setPetFeedBattle(int pPetFeedBattle)
	{
		petFeedBattle = pPetFeedBattle;
	}

	//  Regen HP

	public void setPetRegenHP(int pPetRegenHP)
	{
		petRegenHP = pPetRegenHP;
	}

	//  Regen MP

	public void setPetRegenMP(int pPetRegenMP)
	{
		petRegenMP = pPetRegenMP;
	}

	/**
	 * @param soulShot the this.petSoulShot to set
	 */
	public void setPetSoulShot(short soulShot)
	{
		petSoulShot = soulShot;
	}

	/**
	 * @param spiritShot the this.petSpiritShot to set
	 */
	public void setPetSpiritShot(short spiritShot)
	{
		petSpiritShot = spiritShot;
	}
}
