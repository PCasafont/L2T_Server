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

import l2tserver.gameserver.ai.CtrlIntention;
import l2tserver.gameserver.datatables.SpawnTable;
import l2tserver.gameserver.model.L2CharPosition;
import l2tserver.gameserver.model.L2Skill;
import l2tserver.gameserver.model.L2Spawn;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.util.Rnd;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 * 
 * Nerva Orc Merchant
 * 
 * Source:
 * 			- http://l2wiki.com/Raiders_Crossroads
 */

public class NervaOrcMerchant extends L2AttackableAIScript
{
	private static final int	_merchant	= 23320;

	public NervaOrcMerchant(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addAttackId(_merchant);
		addSpawnId(_merchant);
		
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
		{
			if (spawn == null)
				continue;
			
			if (spawn.getNpcId() == _merchant)
				this.notifySpawn(spawn.getNpc());
		}
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.disableCoreAI(true);
		
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		if (!npc.isMoving())
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(npc.getX() + 1000, npc.getY() + (Rnd.get(10) > 8 ? -1000 : 1000), npc.getZ(), 0));
		
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}
	
	public static void main(String[] args)
	{
		new NervaOrcMerchant(-1, "NervaOrcMerchant", "ai");
	}
}
