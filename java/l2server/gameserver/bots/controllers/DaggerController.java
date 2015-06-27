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

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.bots.DamageType;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.item.L2Armor;
import l2server.gameserver.templates.item.L2ArmorType;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
public class DaggerController extends FighterController
{
	protected static final int BACKSTAB_ID = 30;
	
	protected static final int ESCAPE_SHACKLE_ID = 453;
	
	protected static final int SAND_BOMB_ID = 412;
	
	public DaggerController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected int pickSpecialSkill(final L2Character target)
	{
		// If we're behind the target and Backstab is available... Backstab!
		if (Util.calculateDistance(_player.getX(), _player.getY(), target.getX(), target.getY()) < 100 && _player.isBehind(target) && isSkillAvailable(BACKSTAB_ID))
			return BACKSTAB_ID;
		
		// If we're rooted and Escape Shackle is available... well, let's go.
		if (_player.isRooted() && isSkillAvailable(ESCAPE_SHACKLE_ID))
			return ESCAPE_SHACKLE_ID;
		
		// If Sand Bomb is available and nearby targets not debuffed with Sand Bomb attacked us with regular attacks, let's sand bomb... from time to time...
		if (Rnd.get(0, 2) == 0 && isSkillAvailable(SAND_BOMB_ID) && getAmountOfTargetsInRangeByDamageType(200, DamageType.PHYSICAL_ATTACK, SAND_BOMB_ID) != 0)
			return SAND_BOMB_ID;
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	protected final int getGeneralAttackRate()
	{
		return 15;
	}
	
	@Override
	protected final boolean isOkToEquip(L2Weapon weaponItem)
	{
		return weaponItem.getItemType() == L2WeaponType.DAGGER;
	}
	
	@Override
	protected boolean maybeMoveToBestPosition(final L2Character targetedCharacter)
	{
		return (!_player.isBehind(targetedCharacter) && (targetedCharacter.isStunned() || targetedCharacter.getTarget() == null)) ? true : Rnd.get(0, 10) == 0;
	}
	
	@Override
	// And the best position is... BEHIND!
	protected void moveToBestPosition(final L2Character targetedCharacter)
	{
		float headingAngle = (float) (targetedCharacter.getHeading() * Math.PI) / Short.MAX_VALUE;
		
		float x = targetedCharacter.getX() - 50 * (float) Math.cos(headingAngle);
		float y = targetedCharacter.getY() - 50 * (float) Math.sin(headingAngle);
		float z = targetedCharacter.getZ() + 1;
		
		_player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition((int) x, (int) y, (int) z, targetedCharacter.getHeading()));
		
		_refreshRate = 500;
	}
	
	@Override
	protected final boolean isOkToEquip(L2Armor armorItem)
	{
		return armorItem.getItemType() == L2ArmorType.LIGHT;
	}
}
