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
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * @author LasTravel
 */

public class ScrollOfPkReduce implements IItemHandler {
	/**
	 * @see l2server.gameserver.handler.IItemHandler#useItem(Playable, Item, boolean)
	 */
	@Override
	public void useItem(Playable playable, Item item, boolean forceUse) {
		if (!(playable instanceof Player)) {
			return;
		}

		Player activeChar = (Player) playable;

		Skill itemSkill = item.getItem().getSkills()[0].getSkill();

		int power = (int) itemSkill.getPower();

		if (activeChar.getPkKills() < power) {
			return;
		}

		activeChar.setPkKills(activeChar.getPkKills() - power);

		Skill skill = SkillTable.getInstance().getInfo(itemSkill.getId(), itemSkill.getLevel());
		activeChar.useMagic(skill, true, true);

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(item);
		activeChar.sendPacket(sm);

		activeChar.broadcastUserInfo();

		activeChar.sendMessage("PK count reduced by: " + power);
	}
}
