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

import l2server.gameserver.model.InstanceType;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.templates.chars.NpcTemplate;

public class EffectPointInstance extends Npc {
	private final Player owner;
	
	public EffectPointInstance(int objectId, NpcTemplate template, Creature owner) {
		super(objectId, template);
		setInstanceType(InstanceType.L2EffectPointInstance);
		setInvul(false);
		this.owner = owner == null ? null : owner.getActingPlayer();
		setInstanceId(this.owner.getInstanceId());
	}
	
	@Override
	public Player getActingPlayer() {
		return owner;
	}
	
	/**
	 * this is called when a player interacts with this NPC
	 *
	 */
	@Override
	public void onAction(Player player, boolean interact) {
		// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onActionShift(Player player) {
		if (player == null) {
			return;
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
