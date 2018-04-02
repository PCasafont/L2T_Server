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

import l2server.gameserver.communitybbs.Manager.CustomCommunityBoard;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.Player;

public class MagicVisor implements IItemHandler {
	@Override
	public void useItem(Playable playable, Item visorItem, boolean forcedUse) {
		if (!(playable instanceof Player)) {
			return;
		}

		Player player = (Player) playable;
		WorldObject target = player.getTarget();

		if (target == null || !(target instanceof Npc)) {
			player.sendMessage("You should target a monster to see its drop list!");
			return;
		}

		Npc mob = (Npc) player.getTarget();
		if (mob != null) {
			CustomCommunityBoard.getInstance().sendDropPage(player, mob.getNpcId(), 1, mob);
		}
	}
}
