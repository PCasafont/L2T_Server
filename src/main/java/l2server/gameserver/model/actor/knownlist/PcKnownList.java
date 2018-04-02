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

package l2server.gameserver.model.actor.knownlist;

import l2server.Config;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.Vehicle;
import l2server.gameserver.model.actor.instance.AirShipInstance;
import l2server.gameserver.model.actor.instance.GuardInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.DeleteObject;
import l2server.gameserver.network.serverpackets.SpawnItem;

public class PcKnownList extends PlayableKnownList {
	public PcKnownList(Player activeChar) {
		super(activeChar);
	}

	/**
	 * Add a visible WorldObject to Player knownObjects and knownPlayer (if necessary) and send Server-Client Packets needed to inform the Player of its state and actions in progress.<BR><BR>
	 * <p>
	 * <B><U> object is a Item </U> :</B><BR><BR>
	 * <li> Send Server-Client Packet DropItem/SpawnItem to the Player </li><BR><BR>
	 * <p>
	 * <B><U> object is a DoorInstance </U> :</B><BR><BR>
	 * <li> Send Server-Client Packets DoorInfo and DoorStatusUpdate to the Player </li>
	 * <li> Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the Player </li><BR><BR>
	 * <p>
	 * <B><U> object is a NpcInstance </U> :</B><BR><BR>
	 * <li> Send Server-Client Packet NpcInfo to the Player </li>
	 * <li> Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the Player </li><BR><BR>
	 * <p>
	 * <B><U> object is a Summon </U> :</B><BR><BR>
	 * <li> Send Server-Client Packet NpcInfo/PetItemList (if the Player is the owner) to the Player </li>
	 * <li> Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the Player </li><BR><BR>
	 * <p>
	 * <B><U> object is a Player </U> :</B><BR><BR>
	 * <li> Send Server-Client Packet CharInfo to the Player </li>
	 * <li> If the object has a private store, Send Server-Client Packet PrivateStoreMsgSell to the Player </li>
	 * <li> Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the Player </li><BR><BR>
	 *
	 * @param object The WorldObject to add to knownObjects and knownPlayer
	 */
	@Override
	public boolean addKnownObject(WorldObject object) {
		if (object instanceof GuardInstance && ((GuardInstance) object).isDecayed()) {
			return false;
		}

		if (!super.addKnownObject(object)) {
			return false;
		}

		if (object.getPoly().isMorphed() && object.getPoly().getPolyType().equals("item")) {
			//if (object.getPolytype().equals("item"))
			getActiveChar().sendPacket(new SpawnItem(object));
			//else if (object.getPolytype().equals("npc"))
			//	sendPacket(new NpcInfoPoly(object, this));
		} else {
			object.sendInfo(getActiveChar());

			if (object instanceof Creature) {
				// Update the state of the Creature object client side by sending Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the Player
				Creature obj = (Creature) object;
				if (obj.hasAI()) {
					obj.getAI().describeStateToPlayer(getActiveChar());
				}
			}
		}

		return true;
	}

	/**
	 * Remove a WorldObject from Player knownObjects and knownPlayer (if necessary) and send Server-Client Packet DeleteObject to the Player.<BR><BR>
	 *
	 * @param object The WorldObject to remove from knownObjects and knownPlayer
	 */
	@Override
	protected boolean removeKnownObject(WorldObject object, boolean forget) {
		if (!super.removeKnownObject(object, forget)) {
			return false;
		}

		if (object instanceof AirShipInstance) {
			if (((AirShipInstance) object).getCaptainId() != 0 && ((AirShipInstance) object).getCaptainId() != getActiveChar().getObjectId()) {
				getActiveChar().sendPacket(new DeleteObject(((AirShipInstance) object).getCaptainId()));
			}
			if (((AirShipInstance) object).getHelmObjectId() != 0) {
				getActiveChar().sendPacket(new DeleteObject(((AirShipInstance) object).getHelmObjectId()));
			}
		}

		// Send Server-Client Packet DeleteObject to the Player
		getActiveChar().sendPacket(new DeleteObject(object));

		if (Config.CHECK_KNOWN && object instanceof Npc && getActiveChar().isGM()) {
			getActiveChar().sendMessage("Removed NPC: " + object.getName());
		}

		return true;
	}

	@Override
	public final Player getActiveChar() {
		return (Player) super.getActiveChar();
	}

	@Override
	public int getDistanceToForgetObject(WorldObject object) {
		if (object instanceof Vehicle) {
			return 10000;
		}

		if (getActiveChar().isPerformingFlyMove()) {
			return 1500;
		}

		if (object instanceof Summon && ((Summon) object).getOwner() == getActiveChar()) {
			return 45000;
		}

		// when knownlist grows, the distance to forget should be at least
		// the same as the previous watch range, or it becomes possible that
		// extra charinfo packets are being sent (watch-forget-watch-forget)
		final int knownlistSize = getKnownObjects().size();
		if (knownlistSize <= 25) {
			return 4400;
		}
		if (knownlistSize <= 35) {
			return 3800;
		}
		if (knownlistSize <= 70) {
			return 3200;
		} else {
			return 2700;
		}
	}

	@Override
	public int getDistanceToWatchObject(WorldObject object) {
		if (object instanceof Vehicle) {
			return 8000;
		}

		if (getActiveChar().isPerformingFlyMove()) {
			return 1000;
		}

		final int knownlistSize = getKnownObjects().size();
		if (knownlistSize <= 25) {
			return 4000; // empty field
		}
		if (knownlistSize <= 35) {
			return 3500;
		}
		if (knownlistSize <= 70) {
			return 3000;
		} else {
			return 2500; // Siege, TOI, city
		}
	}
}
