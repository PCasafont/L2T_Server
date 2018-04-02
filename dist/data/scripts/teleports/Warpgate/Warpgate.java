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

package teleports.Warpgate;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.zone.ZoneType;

public class Warpgate extends Quest {
	private static final int ZONE = 40101;

	private static final int[] WARPGATES = {32314, 32315, 32316, 32317, 32318, 32319, 33900};

	private static boolean canEnter(Player player) {
		if (player.isFlying()) {
			return false;
		}

		if (player.getLevel() < 99) {
			return false;
		}

		return true;
	}

	@Override
	public final String onFirstTalk(Npc npc, Player player) {
		return npc.getNpcId() + ".html";
	}

	@Override
	public final String onTalk(Npc npc, Player player) {
		if (!canEnter(player)) {
			return "warpgate-no.html";
		}

		player.teleToLocation(-28930, 256438, -2194, true);

		return null;
	}

	@Override
	public final String onEnterZone(Creature character, ZoneType zone) {
		if (character instanceof Player) {
			if (!canEnter((Player) character) && !character.isGM()) {
				ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(character), 1000);
			}
		}
		return null;
	}

	private static final class Teleport implements Runnable {
		private final Creature cha;

		public Teleport(Creature c) {
			cha = c;
		}

		@Override
		public void run() {
			try {
				cha.teleToLocation(-16555, 209375, -3670, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Warpgate(int questId, String name, String descr) {
		super(questId, name, descr);
		for (int id : WARPGATES) {
			addStartNpc(id);
			addFirstTalkId(id);
			addTalkId(id);
		}
		addEnterZoneId(ZONE);
	}

	public static void main(String[] args) {
		new Warpgate(-1, "Warpgate", "teleports");
	}
}
