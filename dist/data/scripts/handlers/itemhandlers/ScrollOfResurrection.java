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

package handlers.itemhandlers;

import l2server.Config;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.7 $ $Date: 2005/04/05 19:41:13 $
 */

public class ScrollOfResurrection implements IItemHandler {
	/**
	 * @see l2server.gameserver.handler.IItemHandler#useItem(Playable, Item, boolean)
	 */
	@Override
	public void useItem(Playable playable, Item item, boolean forceUse) {
		if (!(playable instanceof Player)) {
			return;
		}

		Player activeChar = (Player) playable;
		// Custom, in order to avoid blessed resurrection scroll usages
		if (Config.isServer(Config.TENKAI) && activeChar.getPvpFlag() > 0) {
			switch (item.getItemId()) {
				case 3936: // Blessed Scroll of Resurrection
				case 3959: // L2Day - Blessed Scroll of Resurrection
				case 6387: // Blessed Scroll of Resurrection: For Pets
				case 9157: // Blessed Scroll of Resurrection Event
				case 10150: // Blessed Scroll of Battlefield Resurrection
				case 13259: // Gran Kain's Blessed Scroll of Resurrection
					activeChar.sendMessage("You cannot use blessed scrolls of resurrection while flagged");
					return;
			}
		}

		if (activeChar.getEvent() != null && !activeChar.getEvent().onScrollUse(activeChar.getObjectId())) {
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isSitting()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_MOVE_SITTING));
			return;
		}
		if (activeChar.isMovementDisabled()) {
			return;
		}

		int itemId = item.getItemId();
		//boolean blessedScroll = (itemId != 737);
		boolean petScroll = itemId == 6387;

		// SoR Animation section
		Creature target = (Creature) activeChar.getTarget();

		if (target != null && target.isDead()) {
			Player targetPlayer = null;

			if (target instanceof Player) {
				targetPlayer = (Player) target;
			}

			PetInstance targetPet = null;

			if (target instanceof PetInstance) {
				targetPet = (PetInstance) target;
			}

			if (targetPlayer != null || targetPet != null) {
				boolean condGood = true;

				//check target is not in a active siege zone
				Castle castle = null;

				if (targetPlayer != null) {
					castle = CastleManager.getInstance().getCastle(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
				} else {
					castle = CastleManager.getInstance()
							.getCastle(targetPet.getOwner().getX(), targetPet.getOwner().getY(), targetPet.getOwner().getZ());
				}

				if (castle != null && castle.getSiege().getIsInProgress()) {
					condGood = false;
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE));
				}

				if (targetPet != null) {
					if (targetPet.getOwner() != activeChar) {
						if (targetPet.getOwner().isReviveRequested()) {
							if (targetPet.getOwner().isRevivingPet()) {
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED)); // Resurrection is already been proposed.
							} else {
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_RES_PET2)); // A pet cannot be resurrected while it's owner is in the process of resurrecting.
							}
							condGood = false;
						}
					}
				} else {
					if (targetPlayer.isReviveRequested()) {
						if (targetPlayer.isRevivingPet()) {
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MASTER_CANNOT_RES)); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
						} else {
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED)); // Resurrection is already been proposed.
						}
						condGood = false;
					} else if (petScroll) {
						condGood = false;
						activeChar.sendMessage("You do not have the correct scroll");
					}
				}

				if (condGood) {
					int skillId = 0;
					int skillLevel = 1;

					switch (itemId) {
						case 737:
							skillId = 2014;
							break; // Scroll of Resurrection
						case 3936:
							skillId = 2049;
							break; // Blessed Scroll of Resurrection
						case 3959:
							skillId = 2062;
							break; // L2Day - Blessed Scroll of Resurrection
						case 6387:
							skillId = 2179;
							break; // Blessed Scroll of Resurrection: For Pets
						case 9157:
							skillId = 2321;
							break; // Blessed Scroll of Resurrection Event
						case 10150:
							skillId = 2393;
							break; // Blessed Scroll of Battlefield Resurrection
						case 13259:
							skillId = 2596;
							break; // Gran Kain's Blessed Scroll of Resurrection
					}

					if (skillId != 0) {
						if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false)) {
							return;
						}

						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(item);
						activeChar.sendPacket(sm);

						Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
						activeChar.useMagic(skill, true, true);
					}
				}
			}
		} else {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
	}
}
