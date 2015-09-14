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

import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.FlyToLocation;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.gameserver.network.serverpackets.FlyToLocation.FlyType;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.gameserver.util.Util;

/**
 *
 * @author  Didldak
 * Some parts taken from EffectWarp, which cannot be used for this case.
 */
public class InstantJump implements ISkillHandler
{
	
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.INSTANT_JUMP
	};
	
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2Character target = (L2Character) targets[0];
		
		if (Formulas.calcPhysicalSkillEvasion(activeChar, target, skill))
		{
			if (activeChar instanceof L2PcInstance)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DODGES_ATTACK);
				sm.addString(target.getName());
				((L2PcInstance) activeChar).sendPacket(sm);
			}
			if (target instanceof L2PcInstance)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_C1_ATTACK);
				sm.addString(activeChar.getName());
				((L2PcInstance) target).sendPacket(sm);
			}
			return;
		}
		
		int x = 0, y = 0, z = 0;
		
		int px = target.getX();
		int py = target.getY();
		double ph = Util.convertHeadingToDegree(target.getHeading());
		
		ph += 180;
		
		if (ph > 360)
			ph -= 360;
		
		ph = (Math.PI * ph) / 180;
		
		x = (int) (px + (25 * Math.cos(ph)));
		y = (int) (py + (25 * Math.sin(ph)));
		z = target.getZ();
		
		Location loc = new Location(x, y, z);
		
		if (Config.GEODATA > 0)
			loc = GeoData.getInstance().moveCheck(activeChar.getX(), activeChar.getY(), activeChar.getZ(), x, y, z, activeChar.getInstanceId());
		
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		activeChar.broadcastPacket(new FlyToLocation(activeChar, loc.getX(), loc.getY(), loc.getZ(), FlyType.DUMMY));
		activeChar.abortAttack();
		activeChar.abortCast();
		
		activeChar.setXYZ(loc.getX(), loc.getY(), loc.getZ());
		activeChar.broadcastPacket(new ValidateLocation(activeChar));
		
		if (skill.hasEffects())
		{
			if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_EFFECTS)
			{
				//activeChar.stopSkillEffects(skill.getId());
				skill.getEffects(target, activeChar);
				
				//SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
				//sm.addSkillName(skill);
				//activeChar.sendPacket(sm);
			}
			else
			{
				// activate attacked effects, if any
				//target.stopSkillEffects(skill.getId());
				skill.getEffects(activeChar, target);
			}
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
}
