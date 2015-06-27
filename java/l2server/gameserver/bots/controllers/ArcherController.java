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
package l2server.gameserver.bots.controllers;

import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.item.L2Armor;
import l2server.gameserver.templates.item.L2ArmorType;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
public class ArcherController extends FighterController
{
	private static final int[] ARROW_ITEMS_ID =
	{
		17, // Wooden Arrow (No Grade)
		1341, // Bone Arrow (D Grade)
		1342, // Steel Arrow (C Grade)
		1343, // Silver Arrow (B Grade)
		1344, // Mithril Arrow (A Grade)
		1345, // Shining Arrow (S Grade)
	};
	
	private static final int[] BOLT_ITEMS_ID =
	{
		9632, // Wooden Bolt (No Grade)
		9633, // Bone Bolt (D Grade)
		9634, // Steel Bolt (C Grade)
		9635, // Silver Bolt (B Grade)
		9636, // Mithril Bolt (A Grade)
		9637, // Shining Bolt (S Grade)
	};
	
	public ArcherController(final L2PcInstance player)
	{
		super(player);
	}
	
	private static final int STUN_SHOT_ID = 101;
	
	private int _arrowsToUse = 0;
	
	@Override
	public void onEnterWorld(final boolean isHumanBehind)
	{
		super.onEnterWorld(isHumanBehind);
		
		_arrowsToUse = getArrowsToUse();
	}
	
	@Override
	public boolean checkIfIsReadyToFight()
	{
		getItem(_arrowsToUse); // Just make sure they always have enough arrows...
		
		return super.checkIfIsReadyToFight();
	}
	
	private final int getArrowsToUse()
	{
		final int playerLevel = _player.getLevel();
		final boolean isTrickster = this instanceof TricksterController;
		
		if (playerLevel >= 76)
			return isTrickster ? BOLT_ITEMS_ID[5] : ARROW_ITEMS_ID[5];
		else if (playerLevel >= 61)
			return isTrickster ? BOLT_ITEMS_ID[4] : ARROW_ITEMS_ID[4];
		else if (playerLevel >= 52)
			return isTrickster ? BOLT_ITEMS_ID[3] : ARROW_ITEMS_ID[3];
		else if (playerLevel >= 40)
			return isTrickster ? BOLT_ITEMS_ID[2] : ARROW_ITEMS_ID[2];
		else if (playerLevel >= 20)
			return isTrickster ? BOLT_ITEMS_ID[1] : ARROW_ITEMS_ID[1];
		
		return isTrickster ? BOLT_ITEMS_ID[5] : ARROW_ITEMS_ID[5];
	}
	
	@Override
	protected final int getGeneralAttackRate()
	{
		return 30;
	}
	
	@Override
	protected boolean isOkToEquip(L2Weapon weaponItem)
	{
		return weaponItem.getItemType() == L2WeaponType.BOW;
	}
	
	@Override
	protected int getAttackRange()
	{
		return _player.getTemplate().baseAtkRange;
	}
	
	@Override
	protected int getMinimumRangeToUseCatchupSkill()
	{
		return getAttackRange() * 5;
	}
	
	@Override
	protected int getMinimumRangeToKite(final L2Character targetedCharacter)
	{
		return targetedCharacter.isStunned() ? Rnd.get(400, 600) : super.getMinimumRangeToKite(targetedCharacter);
	}
	
	@Override
	protected int getKiteRate(final L2Character targetedCharacter)
	{
		// Always try to kite if stun shot have just been used.
		if (!isSkillAvailable(STUN_SHOT_ID))
			return 100;
		
		final L2Abnormal dashEffect = _player.getFirstEffect(4);
		// Always kite when dash is active.
		if (dashEffect != null && dashEffect.getTime() > 5)
			return 100;
		
		// Kite from time to time otherwise.
		return 50;
	}
	
	@Override
	protected boolean maybeMoveToBestPosition(final L2Character targetedCharacter)
	{
		return maybeKite(targetedCharacter);
	}
	
	@Override
	protected void moveToBestPosition(final L2Character targetedCharacter)
	{
		kiteToBestPosition(targetedCharacter, true, 200, 600);
	}
	
	@Override
	protected final boolean isOkToEquip(L2Armor armorItem)
	{
		return armorItem.getItemType() == L2ArmorType.LIGHT;
	}
}
