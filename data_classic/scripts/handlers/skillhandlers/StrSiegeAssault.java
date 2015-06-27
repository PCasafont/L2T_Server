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
package handlers.skillhandlers;

import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.FortManager;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.entity.Fort;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.gameserver.templates.skills.L2SkillType;

/**
 * @author _tomciaaa_
 *
 */
public class StrSiegeAssault implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.STRSIEGEASSAULT
	};
	
	/**
	 * 
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Skill, l2server.gameserver.model.L2Object[])
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		
		if (!(activeChar instanceof L2PcInstance))
			return;
		
		L2PcInstance player = (L2PcInstance) activeChar;
		
		if (!player.isRidingStrider())
			return;
		if (!(player.getTarget() instanceof L2DoorInstance))
			return;
		
		Castle castle = CastleManager.getInstance().getCastle(player);
		Fort fort = FortManager.getInstance().getFort(player);
		
		if ((castle == null) && (fort == null))
			return;
		
		if (castle != null)
		{
			if (!player.checkIfOkToUseStriderSiegeAssault(castle))
				return;
		}
		else
		{
			if (!player.checkIfOkToUseStriderSiegeAssault(fort))
				return;
		}
		
		try
		{
			// damage calculation
			int damage = 0;
			
			for (L2Character target: (L2Character[]) targets)
			{
				L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
				if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && ((L2PcInstance)target).isFakeDeath())
				{
					target.stopFakeDeath(true);
				}
				else if (target.isDead())
					continue;
				
				boolean dual = activeChar.isUsingDualWeapon();
				byte shld = Formulas.calcShldUse(activeChar, target, skill);
				boolean crit = Formulas.calcCrit(activeChar.getCriticalHit(target, skill), target);
				double soul = L2ItemInstance.CHARGED_NONE;
				if (weapon != null && weapon.getItemType() != L2WeaponType.DAGGER)
					soul = weapon.getChargedSoulShot();
				
				if (!crit && (skill.getCondition() & L2Skill.COND_CRIT) != 0)
					damage = 0;
				else
					damage = (int) Formulas.calcPhysDam(activeChar, target, skill, shld, crit, dual, soul);
				
				if (damage > 0)
				{
					target.reduceCurrentHp(damage, activeChar, skill);
					if (weapon != null)
						weapon.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
					
					activeChar.sendDamageMessage(target, damage, false, false, false);
					
				}
				else
					activeChar.sendMessage(skill.getName() + " failed.");
			}
		}
		catch (Exception e)
		{
			player.sendMessage("Error using siege assault:" + e);
		}
	}
	
	/**
	 * 
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
	
	public static void main(String[] args)
	{
		new StrSiegeAssault();
	}
	
}
