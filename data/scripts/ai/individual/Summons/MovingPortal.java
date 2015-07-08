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
package ai.individual.Summons;

import l2tserver.gameserver.GeoData;
import l2tserver.gameserver.datatables.SkillTable;
import l2tserver.gameserver.model.L2Object;
import l2tserver.gameserver.model.L2Skill;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 * @author Pere
 * 
 * Summon Moving Portal (skill id: 11361) AI
 */

public class MovingPortal extends L2AttackableAIScript
{
	private static final int		_portalId			= 13426;
	private static final L2Skill	_instantTeleport 	= SkillTable.getInstance().getInfo(11363, 1);
	
	public MovingPortal(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addSkillSeeId(_portalId);
	}

	@Override
	public String onSkillSee (L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (skill.getId() == _instantTeleport.getId())
		{
			if (caster.getEvent() == null && npc.getOwner() == caster && GeoData.getInstance().canSeeTarget(npc, caster) && caster.isInsideRadius(npc, _instantTeleport.getSkillRadius(), true, false))
			{
				caster.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	public static void main(String[] args)
	{
		new MovingPortal(-1, "MovingPortal", "ai/individual");
	}
}
