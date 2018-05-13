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
  @author Migi
 */

package handlers.itemhandlers;

import l2server.Config;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.CreatureZone;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;

public class MagicGem implements IItemHandler {
	@Override
	public void useItem(Playable playable, Item magicGem, boolean forcedUse) {
		if (!(playable instanceof Player)) {
			return;
		}

		Player player = (Player) playable;

		if (!player.getFloodProtectors().getMagicGem().tryPerformAction("Magic Gem")) {
			return;
		}

		if (Config.isServer(Config.TENKAI)) {
			if (!GrandBossManager.getInstance().checkIfInZone(player) && player.getInstanceId() == 0 && !player.isInsideZone(CreatureZone.ZONE_PVP) &&
					(!player.isInsideZone(CreatureZone.ZONE_NOSUMMONFRIEND) || player.isInsideZone(CreatureZone.ZONE_TOWN)) &&
					player.getEvent() == null && !player.isInOlympiadMode() && !AttackStanceTaskManager.getInstance().getAttackStanceTask(player) &&
					InstanceManager.getInstance().getInstance(player.getObjectId()) == null && player.getPvpFlag() == 0) {
				player.spawnServitors();
				player.sendMessage("You use a Magic Gem.");
			} else {
				player.sendMessage("You cannot use a Magic Gem right now.");
			}
		}
	}
}
