package l2server.gameserver.events.instanced.types;

import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.events.instanced.EventConfig;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventTeleporter;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class KingOfTheHill extends EventInstance {
	public KingOfTheHill(int id, EventConfig config) {
		super(id, config);
	}

	@Override
	public boolean startFight() {
		if (!super.startFight()) {
			return false;
		}

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
			@Override
			public void run() {
				if (!KingOfTheHill.this.isState(EventState.STARTED)) {
					return;
				}

				Player highest = null;
				for (Player player : teams[0].getParticipatedPlayers().values()) {
					if (player == null) {
						continue;
					}

					if (highest == null || player.getZ() > highest.getZ()) {
						highest = player;
					}
				}

				if (highest != null) {
					highest.addEventPoints(1);
				}
				ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
			}
		}, 10000);

		return true;
	}

	@Override
	public void calculateRewards() {
		List<Player> sorted = new ArrayList<>();
		for (Player playerInstance : teams[0].getParticipatedPlayers().values()) {
			boolean added = false;
			int index = 0;
			for (Player listed : sorted) {
				if (playerInstance.getEventPoints() > listed.getEventPoints()) {
					sorted.add(index, playerInstance);
					added = true;
					break;
				}
				index++;
			}
			if (!added) {
				sorted.add(playerInstance);
			}
		}

		rewardPlayers(sorted);
		Announcements.getInstance()
				.announceToAll(
						"The event has ended. The player " + sorted.get(0).getName() + " won being on the highest place during the most time!");
	}

	@Override
	public String getRunningInfo(Player player) {
		String html = "";
		if (teams[0].getParticipatedPlayerCount() > 0) {
			html += "Participant heights:<br>";
			for (Player participant : teams[0].getParticipatedPlayers().values()) {
				if (participant != null) {
					html += EventsManager.getInstance().getPlayerString(participant, player) + ": " +
							(participant.getZ() - config.getLocation().getGlobalZ()) + "<br>";
				}
			}
			if (html.length() > 4) {
				html = html.substring(0, html.length() - 4);
			}
		}
		return html;
	}

	@Override
	public void onKill(Creature killerCharacter, Player killedPlayerInstance) {
		if (killedPlayerInstance == null || !isState(EventState.STARTED)) {
			return;
		}

		new EventTeleporter(killedPlayerInstance, teams[0].getCoords(), false, false);
	}
}
