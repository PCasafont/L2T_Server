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

import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.ItemTemplate;
import l2server.gameserver.templates.item.WeaponTemplate;
import l2server.gameserver.templates.item.WeaponType;
import l2server.gameserver.util.Broadcast;

/**
 * @author -Nemesiss-
 */
public class FishShots implements IItemHandler {
	private static final int[] SKILL_IDS = {2181, 2182, 2183, 2184, 2185, 2186};

	/**
	 * @see l2server.gameserver.handler.IItemHandler#useItem(Playable, Item, boolean)
	 */
	@Override
	public void useItem(Playable playable, Item item, boolean forceUse) {
		if (!(playable instanceof Player)) {
			return;
		}

		Player activeChar = (Player) playable;
		Item weaponInst = activeChar.getActiveWeaponInstance();
		WeaponTemplate weaponItem = activeChar.getActiveWeaponItem();

		if (weaponInst == null || weaponItem.getItemType() != WeaponType.FISHINGROD) {
			return;
		}

		if (weaponInst.getChargedFishshot())
		// spirit shot is already active
		{
			return;
		}

		int FishshotId = item.getItemId();
		int grade = weaponItem.getCrystalType();
		long count = item.getCount();

		if (grade == ItemTemplate.CRYSTAL_NONE && FishshotId != 6535 || grade == ItemTemplate.CRYSTAL_D && FishshotId != 6536 ||
				grade == ItemTemplate.CRYSTAL_C && FishshotId != 6537 || grade == ItemTemplate.CRYSTAL_B && FishshotId != 6538 ||
				grade == ItemTemplate.CRYSTAL_A && FishshotId != 6539 || FishshotId != 6540 && grade == ItemTemplate.CRYSTAL_S) {
			//1479 - This fishing shot is not fit for the fishing pole crystal.
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WRONG_FISHINGSHOT_GRADE));
			return;
		}

		if (count < 1) {
			return;
		}

		weaponInst.setChargedFishshot(true);
		activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false);
		WorldObject oldTarget = activeChar.getTarget();
		activeChar.setTarget(activeChar);

		Broadcast.toSelfAndKnownPlayers(activeChar, new MagicSkillUse(activeChar, SKILL_IDS[grade], 1, 0, 0));
		activeChar.setTarget(oldTarget);
	}
}
