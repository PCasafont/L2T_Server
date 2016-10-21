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

import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;

/**
 * @author LasTravel
 *         <p>
 *         Teleport portal from Angel Waterfall > Magmeld
 */

public class TeleportPortal extends Quest
{
	private static final int _portal = 32910;

	public TeleportPortal(int id, String name, String descr)
	{
		super(id, name, descr);

		addAggroRangeEnterId(_portal);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (!npc.isImmobilized())
		{
			npc.disableCoreAI(true);
			npc.setIsImmobilized(true);
			npc.setIsInvul(true);
		}
		player.teleToLocation(207559, 86429, -1000);

		return super.onAggroRangeEnter(npc, player, isPet);
	}

	public static void main(String[] args)
	{
		new TeleportPortal(-1, "TeleportPortal", "ai");
	}
}
