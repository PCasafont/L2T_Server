package l2server.gameserver.events.instanced.types;

import l2server.gameserver.Announcements;
import l2server.gameserver.events.instanced.EventConfig;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventTeleporter;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.instancemanager.PlayerAssistsManager;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class DeathMatch extends EventInstance {

	public DeathMatch(int id, EventConfig config) {
		super(id, config);
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
				.announceToAll("The event has ended. The player " + sorted.get(0).getName() + " won with " + sorted.get(0).getEventPoints() +
						" kill points");
	}

	@Override
	public String getRunningInfo(Player player) {
		String html = "";
		if (teams[0].getParticipatedPlayerCount() > 0) {
			html += "Participants' points:<br>";
			for (Player participant : teams[0].getParticipatedPlayers().values()) {
				if (participant != null) {
					html += EventsManager.getInstance().getPlayerString(participant, player) + ": " + participant.getEventPoints() + "<br>";
				}
			}
			if (html.length() > 4) {
				html = html.substring(0, html.length() - 4);
			}
		}
		return html;
	}

	@Override
	public void onKill(Creature killerCharacter, Player killedPlayer) {
		if (killedPlayer == null || !isState(EventState.STARTED)) {
			return;
		}

		new EventTeleporter(killedPlayer, teams[0].getCoords(), false, false);

		if (killerCharacter == null) {
			return;
		}

		Player killerPlayer = killerCharacter.getActingPlayer();
		if (killerPlayer == null) {
			return;
		}

		onContribution(killerPlayer, 1);

		CreatureSay cs =
				new CreatureSay(killerPlayer.getObjectId(), Say2.TELL, killerPlayer.getName(), "I have killed " + killedPlayer.getName() + "!");
		killerPlayer.sendPacket(cs);

		killerPlayer.addEventPoints(3);
		List<Player> assistants = PlayerAssistsManager.getInstance().getAssistants(killerPlayer, killedPlayer, true);
		for (Player assistant : assistants) {
			assistant.addEventPoints(1);
		}
	}
}
