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

package l2server.gameserver.network.clientpackets;

import l2server.Config;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExAutoSoulShot;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.0.0.0 $ $Date: 2005/07/11 15:29:30 $
 */
public final class RequestAutoSoulShot extends L2GameClientPacket {

	// format cd
	private int itemId;
	private int enabled; // 1 = on : 0 = off;

	@Override
	protected void readImpl() {
		itemId = readD();
		enabled = readD();
	}

	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}

		if (activeChar.getPrivateStoreType() == 0 && activeChar.getActiveRequester() == null && !activeChar.isDead()) {
			if (Config.DEBUG) {
				log.debug("AutoSoulShot:" + itemId);
			}

			Item item = activeChar.getInventory().getItemByItemId(itemId);
			if (item == null) {
				return;
			}

			if (enabled == 1) {
				if (!activeChar.getInventory().canManipulateWithItemId(item.getItemId())) {
					activeChar.sendMessage("Cannot use this item.");
					return;
				}

				// Fishingshots are not automatic on retail
				if (itemId < 6535 || itemId > 6540) {
					// Attempt to charge first shot on activation
					if (itemId == 6645 || itemId == 6646 || itemId == 6647 || itemId == 20332 || itemId == 20333 || itemId == 20334) {
						boolean hasSummon = false;
						if (activeChar.getPet() != null) {
							if (item.getEtcItem().getHandlerName().equals("BeastSoulShot")) {
								if (activeChar.getPet().getSoulShotsPerHit() > item.getCount()) {
									activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET));
									return;
								}
							} else {
								if (activeChar.getPet().getSpiritShotsPerHit() > item.getCount()) {
									activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET));
									return;
								}
							}
							activeChar.addAutoSoulShot(item);
							activeChar.sendPacket(new ExAutoSoulShot(itemId, enabled, item.getItem().getShotTypeIndex()));

							// start the auto soulshot use
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
							sm.addItemName(item);// Update Message by rocknow
							activeChar.sendPacket(sm);

							activeChar.rechargeAutoSoulShot(true, true, true);
							hasSummon = true;
						}
						for (SummonInstance summon : activeChar.getSummons()) {
							if (summon == null) {
								continue;
							}

							if (item.getEtcItem().getHandlerName().equals("BeastSoulShot")) {
								if (summon.getSoulShotsPerHit() > item.getCount()) {
									activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET));
									continue;
								}
							} else {
								if (summon.getSpiritShotsPerHit() > item.getCount()) {
									activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET));
									continue;
								}
							}
							activeChar.addAutoSoulShot(item);
							activeChar.sendPacket(new ExAutoSoulShot(itemId, enabled, item.getItem().getShotTypeIndex()));

							// start the auto soulshot use
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
							sm.addItemName(item);// Update Message by rocknow
							activeChar.sendPacket(sm);

							activeChar.rechargeAutoSoulShot(true, true, true);
							hasSummon = true;
							break;
						}

						if (!hasSummon) {
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_SERVITOR_CANNOT_AUTOMATE_USE));
						}
					} else {
						if (activeChar.getActiveWeaponItem() != activeChar.getFistsWeaponItem() &&
								item.getItem().getCrystalType() == activeChar.getActiveWeaponItem().getItemGradePlain()) {
							activeChar.addAutoSoulShot(item);
							activeChar.sendPacket(new ExAutoSoulShot(itemId, enabled, item.getItem().getShotTypeIndex()));
						} else {
							if (itemId >= 2509 && itemId <= 2514 || itemId >= 3947 && itemId <= 3952 || itemId == 5790 ||
									itemId >= 22072 && itemId <= 22081) {
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH));
							} else {
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SOULSHOTS_GRADE_MISMATCH));
							}

							activeChar.addAutoSoulShot(item);
							activeChar.sendPacket(new ExAutoSoulShot(itemId, enabled, item.getItem().getShotTypeIndex()));
						}

						// start the auto soulshot use
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
						sm.addItemName(item);// Update Message by rocknow
						activeChar.sendPacket(sm);

						activeChar.rechargeAutoSoulShot(true, true, false);
					}
				}
			} else if (enabled == 0) {
				activeChar.removeAutoSoulShot(item);
				activeChar.sendPacket(new ExAutoSoulShot(itemId, enabled, item.getItem().getShotTypeIndex()));

				// cancel the auto soulshot use
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
				sm.addItemName(item);// Update Message by rocknow
				activeChar.sendPacket(sm);
			}
		}
	}

	@Override
	protected boolean triggersOnActionRequest() {
		return false;
	}
}
