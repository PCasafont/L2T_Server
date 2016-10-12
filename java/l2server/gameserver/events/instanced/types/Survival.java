package l2server.gameserver.events.instanced.types;

import l2server.gameserver.Announcements;
import l2server.gameserver.events.instanced.EventConfig;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventTeleporter;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.instancemanager.PlayerAssistsManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.util.Point3D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class Survival extends EventInstance
{
	List<L2PcInstance> _winners = new ArrayList<>();

	public Survival(int id, EventConfig config)
	{
		super(id, config);
	}

	@Override
	public void calculateRewards()
	{
		L2PcInstance winner = null;
		if (_teams[0].getParticipatedPlayerCount() != 1)
		{
			Announcements.getInstance().announceToAll("The event has ended in a tie");
			return;
		}

		for (L2PcInstance playerInstance : _teams[0].getParticipatedPlayers().values())
		{
			winner = playerInstance;
		}

		if (winner != null)
		{
			_winners.add(0, winner);
		}

		if (!_winners.isEmpty())
		{
			int extraPoints = _winners.size() * 3;
			for (L2PcInstance player : _winners)
			{
				player.addEventPoints(extraPoints);
				extraPoints /= 2;
			}
			rewardPlayers(_winners);
			Announcements.getInstance().announceToAll("The event has ended. The player " + _winners.get(0).getName() +
					" has won being the last one standing!");
		}
		else
		{
			Announcements.getInstance()
					.announceToAll("The event has ended in a tie due to the fact there wasn't anyone left");
		}
	}

	@Override
	public String getRunningInfo(L2PcInstance player)
	{
		String html = "";
		int alive = 0;
		if (_teams[0].getParticipatedPlayerCount() > 0)
		{
			html += "Survivors:<br>";
			for (L2PcInstance participant : _teams[0].getParticipatedPlayers().values())
			{
				if (participant != null)
				{
					html += EventsManager.getInstance().getPlayerString(participant, player) + ", ";
					alive++;
				}
			}
			html = html.substring(0, html.length() - 2) + ".";
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

		removeParticipant(killedPlayerInstance.getObjectId());
		killedPlayerInstance.sendMessage("You have been killed and disqualified from the event.");
		_winners.add(0, killedPlayerInstance);
		new EventTeleporter(killedPlayerInstance, new Point3D(0, 0, 0), false, true);

		killerPlayer.addEventPoints(3);
		List<L2PcInstance> assistants =
				PlayerAssistsManager.getInstance().getAssistants(killerPlayer, killedPlayerInstance, true);
		for (L2PcInstance assistant : assistants)
		{
			assistant.addEventPoints(1);
		}

		if (_teams[0].getParticipatedPlayerCount() <= 1)
		{
			stopFight();
		}
	}
}
