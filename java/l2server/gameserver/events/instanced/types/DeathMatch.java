package l2server.gameserver.events.instanced.types;

import l2server.gameserver.Announcements;
import l2server.gameserver.events.instanced.EventConfig;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventTeleporter;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.instancemanager.PlayerAssistsManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class DeathMatch extends EventInstance
{

	public DeathMatch(int id, EventConfig config)
	{
		super(id, config);
	}

	@Override
	public void calculateRewards()
	{
		List<L2PcInstance> sorted = new ArrayList<>();
		for (L2PcInstance playerInstance : _teams[0].getParticipatedPlayers().values())
		{
			boolean added = false;
			int index = 0;
			for (L2PcInstance listed : sorted)
			{
				if (playerInstance.getEventPoints() > listed.getEventPoints())
				{
					sorted.add(index, playerInstance);
					added = true;
					break;
				}
				index++;
			}
			if (!added)
			{
				sorted.add(playerInstance);
			}
		}

		rewardPlayers(sorted);
		Announcements.getInstance().announceToAll(
				"The event has ended. The player " + sorted.get(0).getName() + " won with " +
						sorted.get(0).getEventPoints() + " kill points");
	}

	@Override
	public String getRunningInfo(L2PcInstance player)
	{
		String html = "";
		if (_teams[0].getParticipatedPlayerCount() > 0)
		{
			html += "Participants' points:<br>";
			for (L2PcInstance participant : _teams[0].getParticipatedPlayers().values())
			{
				if (participant != null)
				{
					html += EventsManager.getInstance().getPlayerString(participant, player) + ": " +
							participant.getEventPoints() + "<br>";
				}
			}
			if (html.length() > 4)
			{
				html = html.substring(0, html.length() - 4);
			}
		}
		return html;
	}

	@Override
	public void onKill(L2Character killerCharacter, L2PcInstance killedPlayer)
	{
		if (killedPlayer == null || !isState(EventState.STARTED))
		{
			return;
		}

		new EventTeleporter(killedPlayer, _teams[0].getCoords(), false, false);

		if (killerCharacter == null)
		{
			return;
		}

		L2PcInstance killerPlayer = killerCharacter.getActingPlayer();
		if (killerPlayer == null)
		{
			return;
		}

		onContribution(killerPlayer, 1);

		CreatureSay cs = new CreatureSay(killerPlayer.getObjectId(), Say2.TELL, killerPlayer.getName(),
				"I have killed " + killedPlayer.getName() + "!");
		killerPlayer.sendPacket(cs);

		killerPlayer.addEventPoints(3);
		List<L2PcInstance> assistants =
				PlayerAssistsManager.getInstance().getAssistants(killerPlayer, killedPlayer, true);
		for (L2PcInstance assistant : assistants)
		{
			assistant.addEventPoints(1);
		}
	}
}
