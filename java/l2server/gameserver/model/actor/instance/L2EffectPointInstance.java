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

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.templates.chars.L2NpcTemplate;

public class L2EffectPointInstance extends L2Npc
{
	private final L2PcInstance _owner;

	public L2EffectPointInstance(int objectId, L2NpcTemplate template, L2Character owner)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2EffectPointInstance);
		setIsInvul(false);
		_owner = owner == null ? null : owner.getActingPlayer();
		setInstanceId(owner.getInstanceId());
	}

	@Override
	public L2PcInstance getActingPlayer()
	{
		return _owner;
	}

	/**
	 * this is called when a player interacts with this NPC
	 *
	 * @param player
	 */
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onActionShift(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
