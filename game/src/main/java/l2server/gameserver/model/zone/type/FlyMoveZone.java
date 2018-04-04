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

package l2server.gameserver.model.zone.type;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.model.L2FlyMove;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.zone.ZoneType;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExNotifyFlyMoveStart;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Pere
 */
public class FlyMoveZone extends ZoneType {
	private L2FlyMove flyMove;

	public FlyMoveZone(int id) {
		super(id + 70500);
	}

	public void setFlyMove(L2FlyMove move) {
		flyMove = move;
	}

	@Override
	protected void onEnter(Creature character) {
		if (!(character instanceof Player)) {
			return;
		}

		Player player = (Player) character;

		if (PlayerClassTable.getInstance().getClassById(player.getBaseClass()).getLevel() < 85 || player.getReputation() < 0 || player.isMounted() ||
				player.isTransformed()) {
			return;
		}

		if (!player.getSummons().isEmpty()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_USE_SAYUNE_WITH_PET));
			return;
		}

		player.setFlyMove(flyMove);

		ThreadPoolManager.getInstance().scheduleGeneral(new FlyMoveStartSendTask((Player) character), 10L);
	}

	@Override
	protected void onExit(Creature character) {
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
	}

	@Override
	public void onReviveInside(Creature character) {
	}

	private class FlyMoveStartSendTask implements Runnable {
		Player player;

		public FlyMoveStartSendTask(Player player) {
			this.player = player;
		}

		@Override
		public void run() {
			if (!isCharacterInZone(player)) {
				return;
			}

			if (!(player.isPerformingFlyMove() && player.isChoosingFlyMove())) {
				player.sendPacket(new ExNotifyFlyMoveStart());
			}

			ThreadPoolManager.getInstance().scheduleGeneral(this, 1000L);
		}
	}
}
