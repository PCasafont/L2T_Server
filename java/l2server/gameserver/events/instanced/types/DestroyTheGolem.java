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
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2EventGolemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;

import java.util.Arrays;
import java.util.List;

/**
 * @author Pere
 */
public class DestroyTheGolem extends EventInstance
{

	private boolean _golemsSpawned = false;

	public DestroyTheGolem(int id, EventConfig config)
	{
		super(id, config);
	}

	@Override
	public boolean startFight()
	{
		if (!super.startFight())
		{
			return false;
		}

		if (!_golemsSpawned)
		{
			spawnGolems();
		}

		return true;
	}

	@Override
	public void calculateRewards()
	{
		EventTeam team;
		if (_config.getLocation().getTeamCount() != 4)
		{
			if (_teams[0].getPoints() == _teams[1].getPoints())
			{
				// Check if one of the teams have no more players left
				if (_teams[0].getParticipatedPlayerCount() == 0 || _teams[1].getParticipatedPlayerCount() == 0)
				{
					// set state to rewarding
					setState(EventState.REWARDING);
					// return here, the fight can't be completed
					Announcements.getInstance().announceToAll("The event has ended. No team won due to inactivity!");
					return;
				}

				// Both teams have equals points
				if (Config.INSTANCED_EVENT_REWARD_TEAM_TIE)
				{
					rewardTeams(-1);
				}
				Announcements.getInstance().announceToAll("The event has ended in a tie");
				return;
			}

			// Set state REWARDING so nobody can point anymore
			setState(EventState.REWARDING);

			// Get team which has more points
			team = _teams[_teams[0].getPoints() > _teams[1].getPoints() ? 0 : 1];

			if (team == _teams[0])
			{
				rewardTeams(0);
			}
			else
			{
				rewardTeams(1);
			}
		}
		else
		{
			// Set state REWARDING so nobody can point anymore
			setState(EventState.REWARDING);
			if (_teams[0].getPoints() > _teams[1].getPoints() && _teams[0].getPoints() > _teams[2].getPoints() &&
					_teams[0].getPoints() > _teams[3].getPoints())
			{
				rewardTeams(0);
				team = _teams[0];
			}
			else if (_teams[1].getPoints() > _teams[0].getPoints() && _teams[1].getPoints() > _teams[2].getPoints() &&
					_teams[1].getPoints() > _teams[3].getPoints())
			{
				rewardTeams(1);
				team = _teams[1];
			}
			else if (_teams[2].getPoints() > _teams[0].getPoints() && _teams[2].getPoints() > _teams[1].getPoints() &&
					_teams[2].getPoints() > _teams[3].getPoints())
			{
				rewardTeams(2);
				team = _teams[2];
			}
			else if (_teams[3].getPoints() > _teams[0].getPoints() && _teams[3].getPoints() > _teams[1].getPoints() &&
					_teams[3].getPoints() > _teams[2].getPoints())
			{
				rewardTeams(3);
				team = _teams[3];
			}
			else
			{
				Announcements.getInstance().announceToAll("The event has ended in a tie");
				return;
			}
		}

		Announcements.getInstance().announceToAll(
				"The event has ended. Team " + team.getName() + " won with " + team.getPoints() + " points.");
	}

	@Override
	public void stopFight()
	{
		super.stopFight();
		unspawnGolems();
	}

	@Override
	public String getRunningInfo(L2PcInstance player)
	{
		String html = "";
		for (EventTeam team : _teams)
		{
			if (team.getParticipatedPlayerCount() > 0)
			{
				html += "Team " + team.getName() + " points: " + team.getPoints() + "<br>";
			}
		}
		if (html.length() > 4)
		{
			html = html.substring(0, html.length() - 4);
		}
		return html;
	}

	public void onGolemDestroyed(L2PcInstance player, EventTeam team)
	{
		getParticipantTeam(player.getObjectId()).increasePoints();

		sendToAllParticipants(
				getParticipantTeam(player.getObjectId()).getName() + " team's member " + player.getName() +
						" has destroyed the " + team.getName() + " team's golem!");

		CreatureSay cs = new CreatureSay(player.getObjectId(), Say2.TELL, player.getName(),
				"I have destroyed the " + team.getName() + " team's golem!");
		for (L2PcInstance character : getParticipantTeam(player.getObjectId()).getParticipatedPlayers().values())
		{
			if (character != null)
			{
				character.sendPacket(cs);
			}
		}

		player.addEventPoints(20);
	}

	@Override
	public void onKill(L2Character killerCharacter, L2PcInstance killedPlayer)
	{
		if (killedPlayer == null || !isState(EventState.STARTED))
		{
			return;
		}

		byte killedTeamId = getParticipantTeamId(killedPlayer.getObjectId());
		if (killedTeamId == -1)
		{
			return;
		}

		L2PcInstance killerPlayer = killerCharacter.getActingPlayer();
		if (killerPlayer == null)
		{
			return;
		}

		killerPlayer.addEventPoints(3);
		List<L2PcInstance> assistants =
				PlayerAssistsManager.getInstance().getAssistants(killerPlayer, killedPlayer, true);
		for (L2PcInstance assistant : assistants)
		{
			assistant.addEventPoints(1);
		}

		new EventTeleporter(killedPlayer, _teams[killedTeamId].getCoords(), false, false);
	}

	private void spawnGolems()
	{
		spawnGolem(_teams[0]);
		spawnGolem(_teams[1]);
		if (_config.getLocation().getTeamCount() == 4)
		{
			spawnGolem(_teams[2]);
			spawnGolem(_teams[3]);
		}
		_golemsSpawned = true;
	}

	private void unspawnGolems()
	{
		for (EventTeam team : _teams)
		{
			unspawnGolem(team);
		}
		_golemsSpawned = false;
	}

	private void spawnGolem(EventTeam team)
	{
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(team.getGolemId());

		try
		{
			team.setGolemSpawn(new L2Spawn(tmpl));

			int x = 0;
			int y = 0;
			for (int i = 0; i < _config.getLocation().getTeamCount(); i++)
			{
				x += _teams[i].getCoords().getX();
				y += _teams[i].getCoords().getY();
			}
			x /= _config.getLocation().getTeamCount();
			y /= _config.getLocation().getTeamCount();

			int heading = (int) Math
					.round(Math.atan2(y - team.getCoords().getY(), x - team.getCoords().getX()) / Math.PI * 32768);
			if (heading < 0)
			{
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
			L2EventGolemInstance golem = (L2EventGolemInstance) team.getGolemSpawn().getNpc();
			int maxHp = 25 * getParticipatedPlayersCount() / _config.getLocation().getTeamCount();
			golem.setMaxHp(maxHp);
			golem.setCurrentHp(golem.getMaxHp());
			golem.setTeam(team);
			golem.setTitle(team.getName());
			golem.updateAbnormalEffect();
		}
		catch (Exception e)
		{
			Log.warning("Golem Engine[spawnGolem(" + team.getName() + ")]: exception: " +
					Arrays.toString(e.getStackTrace()));
		}
	}

	private void unspawnGolem(EventTeam team)
	{
		if (team.getGolemSpawn() != null)
		{
			team.getGolemSpawn().getNpc().deleteMe();
			team.getGolemSpawn().stopRespawn();
			SpawnTable.getInstance().deleteSpawn(team.getGolemSpawn(), false);
		}
	}

	class UnspawnGolemsTask implements Runnable
	{
		@Override
		@SuppressWarnings("synthetic-access")
		public void run()
		{
			unspawnGolems();
		}
	}
}
