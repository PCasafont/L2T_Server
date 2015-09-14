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
package ai.individual;

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.util.Rnd;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 * 
 * Mandragora AI
 */

public class Mandragora extends L2AttackableAIScript
{
	private static final int[] _mandragoras 		= {23240, 23241};
	private static final int[] _sumonedMandragoras 	= {23210, 23211};

	public Mandragora(int id, String name, String descr)
	{
		super(id, name, descr);
		
		for (int a : _mandragoras)
		{
			addAttackId(a);
		}
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		npc.deleteMe();
		
		L2Npc mandragora = addSpawn(_sumonedMandragoras[Rnd.get(_sumonedMandragoras.length)], npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 60000);
		
		mandragora.setTarget(attacker);
		
		((L2MonsterInstance)mandragora).addDamageHate(attacker, 500, 99999);
		
		mandragora.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}
	
	public static void main(String[] args)
	{
		new Mandragora(-1, "Mandragora", "ai");
	}
}
