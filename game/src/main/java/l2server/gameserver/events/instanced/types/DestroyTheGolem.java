package l2server.gameserver.events.instanced.types;

import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.events.instanced.EventConfig;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventTeam;
import l2server.gameserver.events.instanced.EventTeleporter;
import l2server.gameserver.instancemanager.PlayerAssistsManager;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.EventGolemInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.templates.chars.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author Pere
 */
public class DestroyTheGolem extends EventInstance {
	private static Logger log = LoggerFactory.getLogger(DestroyTheGolem.class.getName());

	private boolean golemsSpawned = false;

	public DestroyTheGolem(int id, EventConfig config) {
		super(id, config);
	}

	@Override
	public boolean startFight() {
		if (!super.startFight()) {
			return false;
		}

		if (!golemsSpawned) {
			spawnGolems();
		}

		return true;
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

		Announcements.getInstance().announceToAll("The event has ended. Team " + team.getName() + " won with " + team.getPoints() + " points.");
	}

	@Override
	public void stopFight() {
		super.stopFight();
		unspawnGolems();
	}

	@Override
	public String getRunningInfo(Player player) {
		String html = "";
		for (EventTeam team : teams) {
			if (team.getParticipatedPlayerCount() > 0) {
				html += "Team " + team.getName() + " points: " + team.getPoints() + "<br>";
			}
		}
		if (html.length() > 4) {
			html = html.substring(0, html.length() - 4);
		}
		return html;
	}

	public void onGolemDestroyed(Player player, EventTeam team) {
		getParticipantTeam(player.getObjectId()).increasePoints();

		sendToAllParticipants(
				getParticipantTeam(player.getObjectId()).getName() + " team's member " + player.getName() + " has destroyed the " + team.getName() +
						" team's golem!");

		CreatureSay cs =
				new CreatureSay(player.getObjectId(), Say2.TELL, player.getName(), "I have destroyed the " + team.getName() + " team's golem!");
		for (Player character : getParticipantTeam(player.getObjectId()).getParticipatedPlayers().values()) {
			if (character != null) {
				character.sendPacket(cs);
			}
		}

		player.addEventPoints(20);
	}

	@Override
	public void onKill(Creature killerCharacter, Player killedPlayer) {
		if (killedPlayer == null || !isState(EventState.STARTED)) {
			return;
		}

		byte killedTeamId = getParticipantTeamId(killedPlayer.getObjectId());
		if (killedTeamId == -1) {
			return;
		}

		Player killerPlayer = killerCharacter.getActingPlayer();
		if (killerPlayer == null) {
			return;
		}

		killerPlayer.addEventPoints(3);
		List<Player> assistants = PlayerAssistsManager.getInstance().getAssistants(killerPlayer, killedPlayer, true);
		for (Player assistant : assistants) {
			assistant.addEventPoints(1);
		}

		new EventTeleporter(killedPlayer, teams[killedTeamId].getCoords(), false, false);
	}

	private void spawnGolems() {
		spawnGolem(teams[0]);
		spawnGolem(teams[1]);
		if (config.getLocation().getTeamCount() == 4) {
			spawnGolem(teams[2]);
			spawnGolem(teams[3]);
		}
		golemsSpawned = true;
	}

	private void unspawnGolems() {
		for (EventTeam team : teams) {
			unspawnGolem(team);
		}
		golemsSpawned = false;
	}

	private void spawnGolem(EventTeam team) {
		NpcTemplate tmpl = NpcTable.getInstance().getTemplate(team.getGolemId());

		try {
			team.setGolemSpawn(new L2Spawn(tmpl));

			int x = 0;
			int y = 0;
			for (int i = 0; i < config.getLocation().getTeamCount(); i++) {
				x += teams[i].getCoords().getX();
				y += teams[i].getCoords().getY();
			}
			x /= config.getLocation().getTeamCount();
			y /= config.getLocation().getTeamCount();

			int heading = (int) Math.round(Math.atan2(y - team.getCoords().getY(), x - team.getCoords().getX()) / Math.PI * 32768);
			if (heading < 0) {
				heading = 65535 + heading;
			}

			team.getGolemSpawn().setX(team.getCoords().getX());
			team.getGolemSpawn().setY(team.getCoords().getY());
			team.getGolemSpawn().setZ(team.getCoords().getZ());
			team.getGolemSpawn().setHeading(heading);
			team.getGolemSpawn().setRespawnDelay(20);
			team.getGolemSpawn().setInstanceId(getInstanceId());

			SpawnTable.getInstance().addNewSpawn(team.getGolemSpawn(), false);

			team.getGolemSpawn().startRespawn();
			team.getGolemSpawn().doSpawn();
			EventGolemInstance golem = (EventGolemInstance) team.getGolemSpawn().getNpc();
			int maxHp = 25 * getParticipatedPlayersCount() / config.getLocation().getTeamCount();
			golem.setMaxHp(maxHp);
			golem.setCurrentHp(golem.getMaxHp());
			golem.setTeam(team);
			golem.setTitle(team.getName());
			golem.updateAbnormalEffect();
		} catch (Exception e) {
			log.warn("Golem Engine[spawnGolem(" + team.getName() + ")]: exception: " + Arrays.toString(e.getStackTrace()));
		}
	}

	private void unspawnGolem(EventTeam team) {
		if (team.getGolemSpawn() != null) {
			team.getGolemSpawn().getNpc().deleteMe();
			team.getGolemSpawn().stopRespawn();
			SpawnTable.getInstance().deleteSpawn(team.getGolemSpawn(), false);
		}
	}

	class UnspawnGolemsTask implements Runnable {
		@Override
		@SuppressWarnings("synthetic-access")
		public void run() {
			unspawnGolems();
		}
	}
}
