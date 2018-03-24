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

import ai.group_template.L2AttackableAIScript;
import l2server.gameserver.GeoData;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.util.Util;

/**
 * @author LasTravel
 *         <p>
 *         Wisp AI
 *         <p>
 *         Source:
 *         - http://l2wiki.com/Fairy_Settlement
 */

public class Wisps extends L2AttackableAIScript
{
	private static final int _wisp = 32915;
	private static final int _largeWisp = 32916;
	private static final L2Skill _healSkill = SkillTable.getInstance().getInfo(14064, 1);

	public Wisps(int id, String name, String descr)
	{
		super(id, name, descr);

		addSpawnId(_wisp);
		addSpawnId(_largeWisp);

		addAggroRangeEnterId(_wisp);
		addAggroRangeEnterId(_largeWisp);

		addSpellFinishedId(_wisp);
		addSpellFinishedId(_largeWisp);

		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
		{
			if (spawn == null)
			{
				continue;
			}

			if (spawn.getNpcId() == _wisp || spawn.getNpcId() == _largeWisp)
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
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (!Util.checkIfInRange(500, player, npc, false) || !GeoData.getInstance().canSeeTarget(player, npc) ||
				player.isDead() || player.isInvul(npc) || player.getPvpFlag() > 0 || player.isFakeDeath())
		{
			return super.onAggroRangeEnter(npc, player, isPet);
		}

		npc.setTarget(player);
		npc.doCast(_healSkill);

		return super.onAggroRangeEnter(npc, player, isPet);
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
		new Wisps(-1, "Wisps", "ai");
	}
}
