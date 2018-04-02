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

/*
  @author FBIagent
 */

package handlers.itemhandlers;

import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ConfirmDlg;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.chars.NpcTemplate;

public class MobSummonItems implements IItemHandler {
	/**
	 */
	@Override
	public void useItem(Playable playable, Item item, boolean forcedUse) {
		if (!(playable instanceof Player)) {
			return;
		}

		Player activeChar = (Player) playable;

		if (activeChar.getEvent() != null && !activeChar.getEvent().onItemSummon(activeChar.getObjectId())) {
			return;
		}

		if (activeChar.isMobSummonRequest()) {
			return;
		}

		if (!activeChar.getFloodProtectors().getItemPetSummon().tryPerformAction("summon pet item")) {
			return;
		}

		if (activeChar.isSitting()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_MOVE_SITTING));
			return;
		}

		int mobId = item.getMobId();
		String confirmText = "";

		if (mobId == 0) {
			confirmText = "This CokeBall is empty. Do you want to use it to catch a monster?";
		} else {
			NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(mobId);
			if (npcTemplate != null) {
				confirmText = "This CokeBall contains a " + npcTemplate.getName() + ". Do you want to call it?";
			}
		}
		activeChar.setMobSummonRequest(true, item);
		ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1.getId()).addString(confirmText);
		activeChar.sendPacket(dlg);
	}
}
