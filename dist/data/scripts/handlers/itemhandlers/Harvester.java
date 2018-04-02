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

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.instancemanager.CastleManorManager;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * @author l3x
 */
public class Harvester implements IItemHandler {
	Player activeChar;
	MonsterInstance target;

	/**
	 * @see l2server.gameserver.handler.IItemHandler#useItem(Playable, Item, boolean)
	 */
	@Override
	public void useItem(Playable playable, Item item, boolean forceUse) {
		if (!(playable instanceof Player)) {
			return;
		}

		if (CastleManorManager.getInstance().isDisabled()) {
			return;
		}

		activeChar = (Player) playable;

		if (!(activeChar.getTarget() instanceof MonsterInstance)) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		target = (MonsterInstance) activeChar.getTarget();

		if (target == null || !target.isDead()) {
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		Skill skill = SkillTable.getInstance().getInfo(2098, 1); //harvesting skill
		if (skill != null) {
			activeChar.useMagic(skill, false, false);
		}
	}
}
