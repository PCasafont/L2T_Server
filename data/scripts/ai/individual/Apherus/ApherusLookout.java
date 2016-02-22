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

package ai.individual.Apherus;

import l2server.Config;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import ai.group_template.L2AttackableAIScript;

/**
 * @author LasTravel
 */

public class ApherusLookout extends L2AttackableAIScript
{
	private static final int _apherusLookout = 22964;
	private static final int _apherusPackage = 19001;
	
	public ApherusLookout(int id, String name, String descr)
	{
		super(id, name, descr);
		
		addKillId(_apherusLookout);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (!Config.isServer(Config.DREAMS) && (npc.getNpcId() == _apherusLookout))
		{
			for (int a = 0; a < 3; a++)
			{
				L2Npc aPackage = addSpawn(_apherusPackage, npc.getX(), npc.getY(), npc.getZ(), 0, true, 120000, false);
				aPackage.setIsImmobilized(true);
			}
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
		new ApherusLookout(-1, "ApherusLookout", "ai/individual");
	}
}
