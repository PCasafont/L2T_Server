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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.templates.chars.L2NpcTemplate;

public final class L2FlyTerrainObjectInstance extends L2Npc
{

	public L2FlyTerrainObjectInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2FlyTerrainObjectInstance);
		_isHideName = true;
	}

	@Override
	public void onSpawn()
	{
		setIsFlying(true);
		super.onSpawn();
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onActionShift(L2PcInstance player)
	{
		if (player.isGM())
		{
			super.onActionShift(player);
		}
		else
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
}
