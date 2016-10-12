package l2server.gameserver.events.instanced.types;

import l2server.gameserver.Announcements;
import l2server.gameserver.events.instanced.*;
import l2server.gameserver.instancemanager.PlayerAssistsManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.util.Point3D;

import java.util.List;

/**
 * @author Pere
 */
public class TeamSurvival extends EventInstance
{
	public TeamSurvival(int id, EventConfig config)
	{
		super(id, config);
	}

	@Override
	public void calculateRewards()
	{
		// Set state REWARDING so nobody can point anymore
		setState(EventState.REWARDING);

		int maxAlive = 0;
		int teamId = 0;
		for (int i = 0; i < _config.getLocation().getTeamCount(); i++)
		{
			int alive = _teams[i].getAlivePlayerCount();
			if (alive > maxAlive)
			{
				maxAlive = alive;
				teamId = i;
			}
		}

		EventTeam team = _teams[teamId];
		rewardTeams(teamId);

		Announcements.getInstance().announceToAll(
				"The event has ended. Team " + team.getName() + " won with " + team.getPoints() + " kill points.");
	}

	@Override
	public String getRunningInfo(L2PcInstance player)
	{
		String html = "";

		int i = 0;
		int alive = 0;
		for (EventTeam team : _teams)
		{
			if (++i > _config.getLocation().getTeamCount())
			{
				break;
			}

			if (team.getParticipatedPlayerCount() > 0 && team.isAlive())
			{
				html += "Team " + team.getName() + " survivors:<br>";
				for (L2PcInstance participant : team.getParticipatedPlayers().values())
				{
					if (participant != null && !participant.isDead())
					{
						html += EventsManager.getInstance().getPlayerString(participant, player) + ", ";
					}
				}
				html = html.substring(0, html.length() - 2);
				html += ".<br>";
				alive++;
			}
			else
			{
				html += "Team " + team.getName() + " is disqualified.<br>";
			}
		}

		if (html.length() > 4)
		{
			html = html.substring(0, html.length() - 4);
		}

		if (alive <= 1)
		{
			stopFight();
		}

		return html;
	}

	@Override
	public void onKill(L2Character killerCharacter, L2PcInstance killedPlayerInstance)
	{
		if (killedPlayerInstance == null || !isState(EventState.STARTED))
		{
			return;
		}

		L2PcInstance killerPlayer = killerCharacter.getActingPlayer();
		if (killerPlayer == null)
		{
			return;
		}

		byte killedTeamId = getParticipantTeamId(killedPlayerInstance.getObjectId());
		if (killedTeamId == -1)
		{
			return;
		}

		EventTeam team = getParticipantTeam(killedPlayerInstance.getObjectId());
		EventTeam killerTeam = getParticipantTeam(killerPlayer.getObjectId());
		if (killerTeam != null)
		{
			if (killerTeam != team)
			{
				killerTeam.increasePoints();
				killerPlayer.addEventPoints(3);
				List<L2PcInstance> assistants =
						PlayerAssistsManager.getInstance().getAssistants(killerPlayer, killedPlayerInstance, true);
				for (L2PcInstance assistant : assistants)
				{
					assistant.addEventPoints(1);
				}
			}
			else
			{
				killerCharacter.sendPacket(new CreatureSay(0, Say2.TELL, "Instanced Events",
						"You have been disqualified for killing a member of your own team!"));
				removeParticipant(killerPlayer.getObjectId());
				new EventTeleporter(killerPlayer, new Point3D(0, 0, 0), false, true);
			}
		}

		if (!team.isAlive())
		{
			for (L2PcInstance player : team.getParticipatedPlayers().values())
			{
				player.sendMessage(
						"Since there weren't alive participants, your team has been disqualified from the event.");
				new EventTeleporter(player, new Point3D(0, 0, 0), false, true);
			}
			team.cleanMe();
			if (_config.getLocation().getTeamCount() != 4 && (!_teams[0].isAlive() || !_teams[1].isAlive()) ||
					_config.getLocation().getTeamCount() == 4 &&
							(!_teams[0].isAlive() && !_teams[1].isAlive() && !_teams[2].isAlive() ||
									!_teams[0].isAlive() && !_teams[1].isAlive() && !_teams[3].isAlive() ||
									!_teams[0].isAlive() && !_teams[2].isAlive() && !_teams[3].isAlive() ||
									!_teams[1].isAlive() && !_teams[2].isAlive() && !_teams[3].isAlive()))
			{
				stopFight();
			}
		}
	}
}
