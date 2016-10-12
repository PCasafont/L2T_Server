package l2server.gameserver.events.instanced.types;

import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.events.instanced.EventConfig;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventTeam;
import l2server.gameserver.events.instanced.EventTeleporter;
import l2server.gameserver.instancemanager.PlayerAssistsManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;

import java.util.List;

/**
 * @author Pere
 */
public class VIPTeamVsTeam extends EventInstance
{

	public VIPTeamVsTeam(int id, EventConfig config)
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

		// Iterate over all teams
		for (EventTeam team : _teams)
		{
			team.setVIP(team.selectRandomParticipant());
			int antiLock = 0;
			while (team.getVIP() == null && antiLock < 10)
			{
				team.setVIP(team.selectRandomParticipant());
				antiLock++;
			}
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
				"The event has ended. Team " + team.getName() + " won with " + team.getPoints() + " kill points.");
	}

	@Override
	public String getRunningInfo(L2PcInstance player)
	{
		String html = "";
		for (EventTeam team : _teams)
		{
			if (team.getVIP() == null)
			{
				team.setVIP(team.selectRandomParticipant());
				int antiLock = 0;
				while (team.getVIP() == null && antiLock < 10)
				{
					team.setVIP(team.selectRandomParticipant());
					antiLock++;
				}
				setImportant(team.getVIP(), true);
			}
			if (team.getParticipatedPlayerCount() > 0)
			{
				html += "Team " + team.getName() + " VIP: " + team.getVIP().getName() + "; points: " +
						team.getPoints() + "<br>";
			}
		}
		if (html.length() > 4)
		{
			html = html.substring(0, html.length() - 4);
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

		byte killedTeamId = getParticipantTeamId(killedPlayerInstance.getObjectId());

		if (killedTeamId == -1)
		{
			return;
		}

		new EventTeleporter(killedPlayerInstance, _teams[killedTeamId].getCoords(), false, false);

		if (killerCharacter == null ||
				getParticipantTeam(killedPlayerInstance.getObjectId()).getVIP() != killedPlayerInstance)
		{
			return;
		}

		L2PcInstance killerPlayerInstance = null;

		if (killerCharacter instanceof L2PetInstance || killerCharacter instanceof L2SummonInstance)
		{
			killerPlayerInstance = ((L2Summon) killerCharacter).getOwner();

			if (killerPlayerInstance == null)
			{
				return;
			}
		}
		else if (killerCharacter instanceof L2PcInstance)
		{
			killerPlayerInstance = (L2PcInstance) killerCharacter;
		}
		else
		{
			return;
		}

		byte killerTeamId = getParticipantTeamId(killerPlayerInstance.getObjectId());

		boolean friendlyDeath = killerTeamId == killedTeamId;
		if (killerTeamId != -1 && killedTeamId != -1 && !friendlyDeath)
		{
			EventTeam killerTeam = _teams[killerTeamId];

			killerTeam.increasePoints();

			CreatureSay cs =
					new CreatureSay(killerPlayerInstance.getObjectId(), Say2.TELL, killerPlayerInstance.getName(),
							"I have killed " + killedPlayerInstance.getName() + "!");
			for (L2PcInstance playerInstance : _teams[killerTeamId].getParticipatedPlayers().values())
			{
				if (playerInstance != null)
				{
					playerInstance.sendPacket(cs);
				}
			}

			killerPlayerInstance.addEventPoints(3);
			List<L2PcInstance> assistants =
					PlayerAssistsManager.getInstance().getAssistants(killerPlayerInstance, killedPlayerInstance, true);
			for (L2PcInstance assistant : assistants)
			{
				assistant.addEventPoints(1);
			}
		}
	}

	@Override
	public boolean removeParticipant(int playerObjectId)
	{
		if (!super.removeParticipant(playerObjectId))
		{
			return false;
		}

		for (EventTeam team : _teams)
		{
			if (team.getVIP() == null || team.getVIP().getObjectId() == playerObjectId)
			{
				team.setVIP(team.selectRandomParticipant());
				int antiLock = 0;
				while (team.getVIP() == null && antiLock < 10)
				{
					team.setVIP(team.selectRandomParticipant());
					antiLock++;
				}
				setImportant(team.getVIP(), true);
				break;
			}
		}

		return true;
	}
}
