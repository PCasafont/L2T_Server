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
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 * 
 * Horn AI
 * 
 * Source:
 * 			- http://l2wiki.com/Land_of_Chaos
 */

public class Horn extends L2AttackableAIScript
{
	private static final int[]	_hornIds	= {19460, 19461, 19462};
	private static final int	_chaosHorn	= 23348;
	private static final int	_poras		= 23335;

	public Horn(int id, String name, String descr)
	{
		super(id, name, descr);
		
		for (int a : _hornIds)
		{
			addKillId(a);
		}
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		switch(npc.getNpcId())
		{
			case 19460:	//Green
				for(L2Character chara : npc.getKnownList().getKnownCharactersInRadius(600))
				{
					if (chara == null || !(chara instanceof L2MonsterInstance))
					{
						continue;
					}
					
					((L2MonsterInstance)chara).setTarget(killer);
					((L2MonsterInstance)chara).addDamageHate(killer, 500, 99999);
					((L2MonsterInstance)chara).getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
				}
				break;
				
			case 19461:	//Blue
				for (int a = 0; a < 5; a++)
				{
					addSpawn(_chaosHorn, npc.getX(), npc.getY(), npc.getZ(), 0, true, 60000, true);
				}
				break;
				
			case 19462:	//Red
				for (int a = 0; a < 8; a++)
				{
					addSpawn(_poras, npc.getX(), npc.getY(), npc.getZ(), 0, true, 60000, false);
				}
				break;
				
		}
		
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public int getOnKillDelay(int npcId)
	{
		return 0;
	}
	
	public static void main(String[] args)
	{
		new Horn(-1, "Horn", "ai");
	}
}
