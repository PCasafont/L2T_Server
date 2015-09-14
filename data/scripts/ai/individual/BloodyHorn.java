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

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.util.Rnd;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 * 
 * Bloody Horns AI
 * 
 * Source:
 * 			- http://l2wiki.com/Land_of_Chaos
 */

public class BloodyHorn extends L2AttackableAIScript
{
	private static final int	_bloodyHorn		= 19463;
	private static final int[] 	_debufSkills	= {15537, 15538, 15539, 15540};

	public BloodyHorn(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addFirstTalkId(_bloodyHorn);
		
		addSpawnId(_bloodyHorn);
		
		addSpellFinishedId(_bloodyHorn);
		
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
		{
			if (spawn == null)
			{
				continue;
			}
			
			if (spawn.getNpcId() == _bloodyHorn)
			{	
				notifySpawn(spawn.getNpc());
			}
		}
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		npc.doDie(null);
		
		return super.onSpellFinished(npc, player, skill);
	}
	
	
	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getTarget() == null)
		{	
			npc.setTarget(player);
			
			int level = Rnd.get(1, 2);
			
			npc.doCast(SkillTable.getInstance().getInfo(_debufSkills[Rnd.get(_debufSkills.length)], level));
			
			player.sendPacket(new ExShowScreenMessage(level == 1 ? 1802313 : 1802306, 2, 3000));
		}	
		
		return "";
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.setIsImmobilized(true);
		
		npc.setIsInvul(true);
		
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new BloodyHorn(-1, "BloodyHorn", "ai");
	}
}
