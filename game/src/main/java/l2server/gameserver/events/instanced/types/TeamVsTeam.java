package l2server.gameserver.events.instanced.types;

import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.events.instanced.EventConfig;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventTeam;
import l2server.gameserver.events.instanced.EventTeleporter;
import l2server.gameserver.instancemanager.PlayerAssistsManager;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;

import java.util.List;

/**
 * @author Pere
 */
public class TeamVsTeam extends EventInstance {
	public TeamVsTeam(int id, EventConfig config) {
		super(id, config);
	}

	@Override
	public void calculateRewards() {
		EventTeam team;
		if (config.getLocation().getTeamCount() != 4) {
			if (teams[0].getPoints() == teams[1].getPoints()) {
				// Check if one of the teams have no more players left
				if (teams[0].getParticipatedPlayerCount() == 0 || teams[1].getParticipatedPlayerCount() == 0) {
					// set state to rewarding
					setState(EventState.REWARDING);
					// return here, the Fight can't be completed
					Announcements.getInstance().announceToAll("The event has ended. No team won due to inactivity!");
					return;
				}

				// Both teams have equals points
				if (Config.INSTANCED_EVENT_REWARD_TEAM_TIE) {
					rewardTeams(-1);
				}
				Announcements.getInstance().announceToAll("The event has ended in a tie");
				return;
			}

			// Set state REWARDING so nobody can point anymore
			setState(EventState.REWARDING);

			// Get team which has more points
			team = teams[teams[0].getPoints() > teams[1].getPoints() ? 0 : 1];

			if (team == teams[0]) {
				rewardTeams(0);
			} else {
				rewardTeams(1);
			}
		} else {
			// Set state REWARDING so nobody can point anymore
			setState(EventState.REWARDING);
			if (teams[0].getPoints() > teams[1].getPoints() && teams[0].getPoints() > teams[2].getPoints() &&
					teams[0].getPoints() > teams[3].getPoints()) {
				rewardTeams(0);
				team = teams[0];
			} else if (teams[1].getPoints() > teams[0].getPoints() && teams[1].getPoints() > teams[2].getPoints() &&
					teams[1].getPoints() > teams[3].getPoints()) {
				rewardTeams(1);
				team = teams[1];
			} else if (teams[2].getPoints() > teams[0].getPoints() && teams[2].getPoints() > teams[1].getPoints() &&
					teams[2].getPoints() > teams[3].getPoints()) {
				rewardTeams(2);
				team = teams[2];
			} else if (teams[3].getPoints() > teams[0].getPoints() && teams[3].getPoints() > teams[1].getPoints() &&
					teams[3].getPoints() > teams[2].getPoints()) {
				rewardTeams(3);
				team = teams[3];
			} else {
				Announcements.getInstance().announceToAll("The event has ended in a tie");
				return;
			}
		}

		Announcements.getInstance().announceToAll("The event has ended. Team " + team.getName() + " won with " + team.getPoints() + " kill points.");
	}

	@Override
	public String getRunningInfo(Player player) {
		String html = "";
		for (EventTeam team : teams) {
			if (team.getParticipatedPlayerCount() > 0) {
				html += "Team " + team.getName() + " kills: " + team.getPoints() + "<br>";
			}
		}
		if (html.length() > 4) {
			html = html.substring(0, html.length() - 4);
		}
		return html;
	}

	@Override
	public void onKill(Creature killerCharacter, Player killedPlayerInstance) {
		if (killedPlayerInstance == null || !isState(EventState.STARTED)) {
			return;
		}

		byte killedTeamId = getParticipantTeamId(killedPlayerInstance.getObjectId());

		if (killedTeamId == -1) {
			return;
		}

		new EventTeleporter(killedPlayerInstance, teams[killedTeamId].getCoords(), false, false);

		if (killerCharacter == null) {
			return;
		}

		Player killerPlayerInstance = null;

		if (killerCharacter instanceof PetInstance || killerCharacter instanceof SummonInstance) {
			killerPlayerInstance = ((Summon) killerCharacter).getOwner();

			if (killerPlayerInstance == null) {
				return;
			}
		} else if (killerCharacter instanceof Player) {
			killerPlayerInstance = (Player) killerCharacter;
		} else {
			return;
		}

		byte killerTeamId = getParticipantTeamId(killerPlayerInstance.getObjectId());

		boolean friendlyDeath = killerTeamId == killedTeamId;
		if (killerTeamId != -1 && !friendlyDeath) {
			EventTeam killerTeam = teams[killerTeamId];

			killerTeam.increasePoints();
			onContribution(killerPlayerInstance, 1);

			CreatureSay cs = new CreatureSay(killerPlayerInstance.getObjectId(),
					Say2.TELL,
					killerPlayerInstance.getName(),
					"I have killed " + killedPlayerInstance.getName() + "!");
			for (Player playerInstance : teams[killerTeamId].getParticipatedPlayers().values()) {
				if (playerInstance != null) {
					playerInstance.sendPacket(cs);
				}
			}

			killerPlayerInstance.addEventPoints(3);
			List<Player> assistants = PlayerAssistsManager.getInstance().getAssistants(killerPlayerInstance, killedPlayerInstance, true);
			for (Player assistant : assistants) {
				assistant.addEventPoints(1);
			}
		}
	}
}
