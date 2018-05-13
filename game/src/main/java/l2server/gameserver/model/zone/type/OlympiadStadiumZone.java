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
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.CreatureZone;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.DoorInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.model.olympiad.Olympiad;
import l2server.gameserver.model.olympiad.OlympiadGameTask;
import l2server.gameserver.model.zone.SpawnZone;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExOlympiadMatchEnd;
import l2server.gameserver.network.serverpackets.ExOlympiadUserInfo;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.SystemMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * An olympiad stadium
 *
 * @author durgus, DS
 */
public class OlympiadStadiumZone extends SpawnZone {
	private final List<OlympiadGameTask> instances;

	public OlympiadStadiumZone(int id) {
		super(id);
		instances = new ArrayList<>(40);
	}

	public final void registerTask(OlympiadGameTask task, int id) {
		instances.add(id / 4, task);
	}

	public final void broadcastStatusUpdate(Player player) {
		final ExOlympiadUserInfo packet = new ExOlympiadUserInfo(player);
		for (Creature character : characterList.values()) {
			if (character instanceof Player && character.getInstanceId() == player.getInstanceId()) {
				if (((Player) character).inObserverMode() || ((Player) character).getOlympiadSide() != player.getOlympiadSide()) {
					character.sendPacket(packet);
				}
			}
		}
	}

	public final void broadcastPacketToObservers(L2GameServerPacket packet, int gameId) {
		for (Creature character : characterList.values()) {
			if (character instanceof Player && ((Player) character).inObserverMode() &&
					character.getInstanceId() - Olympiad.BASE_INSTANCE_ID == gameId) {
				character.sendPacket(packet);
			}
		}
	}

	@Override
	protected final void onEnter(Creature character) {
		character.setInsideZone(CreatureZone.ZONE_NOSUMMONFRIEND, true);

		int instanceIndex = (character.getInstanceId() - Olympiad.BASE_INSTANCE_ID) / 4;
		if (instanceIndex < 0 || instanceIndex >= instances.size()) {
			return;
		}

		OlympiadGameTask task = instances.get(instanceIndex);
		if (task == null) {
			return;
		}

		if (task.isBattleStarted()) {
			character.setInsideZone(CreatureZone.ZONE_PVP, true);
			if (character instanceof Player) {
				character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
				task.getGame().sendOlympiadInfo(character);
			}
		}

		if (character instanceof Playable) {
			final Player player = character.getActingPlayer();
			if (player != null) {
				// only participants, observers and GMs allowed
				if (!player.isGM() && !player.isInOlympiadMode() && !player.inObserverMode()) {
					ThreadPoolManager.getInstance().executeTask(new KickPlayer(player));
				}
			}
		}
	}

	@Override
	protected final void onExit(Creature character) {
		character.setInsideZone(CreatureZone.ZONE_NOSUMMONFRIEND, false);

		int instanceIndex = (character.getInstanceId() - Olympiad.BASE_INSTANCE_ID) / 4;
		if (instanceIndex < 0 || instanceIndex >= instances.size()) {
			return;
		}

		OlympiadGameTask task = instances.get(instanceIndex);
		if (task.isBattleStarted()) {
			character.setInsideZone(CreatureZone.ZONE_PVP, false);
			if (character instanceof Player) {
				character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
				character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
			}
		}

		if (character instanceof DoorInstance) {
			task.getDoors().remove(character);
		}
	}

	public final void updateZoneStatusForCharactersInside(int gameId) {
		int instanceIndex = gameId / 4;
		if (instanceIndex < 0 || instanceIndex >= instances.size()) {
			return;
		}

		OlympiadGameTask task = instances.get(instanceIndex);
		if (task == null) {
			return;
		}

		final boolean battleStarted = task.isBattleStarted();
		final SystemMessage sm;
		if (battleStarted) {
			sm = SystemMessage.getSystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE);
		} else {
			sm = SystemMessage.getSystemMessage(SystemMessageId.LEFT_COMBAT_ZONE);
		}

		for (Creature character : characterList.values()) {
			if (character == null || character.getInstanceId() - Olympiad.BASE_INSTANCE_ID != gameId) {
				continue;
			}

			if (battleStarted) {
				character.setInsideZone(CreatureZone.ZONE_PVP, true);
				if (character instanceof Player) {
					character.sendPacket(sm);
				}
			} else {
				character.setInsideZone(CreatureZone.ZONE_PVP, false);
				if (character instanceof Player) {
					character.sendPacket(sm);
					character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
				}
			}
		}
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
	}

	@Override
	public void onReviveInside(Creature character) {
	}

	private static final class KickPlayer implements Runnable {
		private Player player;

		public KickPlayer(Player player) {
			this.player = player;
		}

		@Override
		public void run() {
			if (player != null) {
				final Summon pet = player.getPet();
				if (pet != null) {
					pet.unSummon(player);
				}
				for (SummonInstance summon : player.getSummons()) {
					summon.unSummon(player);
				}

				player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				player = null;
			}
		}
	}
}
