package l2server.gameserver.events.instanced.types;

import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.events.instanced.EventConfig;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventTeleporter;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class KingOfTheHill extends EventInstance
{
	public KingOfTheHill(int id, EventConfig config)
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

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				if (!KingOfTheHill.this.isState(EventState.STARTED))
				{
					return;
				}

				L2PcInstance highest = null;
				for (L2PcInstance player : _teams[0].getParticipatedPlayers().values())
				{
					if (player == null)
					{
						continue;
					}

					if (highest == null || player.getZ() > highest.getZ())
					{
						highest = player;
					}
				}

				if (highest != null)
				{
					highest.addEventPoints(1);
				}
				ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
			}
		}, 10000);

		return true;
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
		Announcements.getInstance().announceToAll("The event has ended. The player " + sorted.get(0).getName() +
				" won being on the highest place during the most time!");
	}

	@Override
	public String getRunningInfo(L2PcInstance player)
	{
		String html = "";
		if (_teams[0].getParticipatedPlayerCount() > 0)
		{
			html += "Participant heights:<br>";
			for (L2PcInstance participant : _teams[0].getParticipatedPlayers().values())
			{
				if (participant != null)
				{
					html += EventsManager.getInstance().getPlayerString(participant, player) + ": " +
							(participant.getZ() - _config.getLocation().getGlobalZ()) + "<br>";
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
	public void onKill(L2Character killerCharacter, L2PcInstance killedPlayerInstance)
	{
		if (killedPlayerInstance == null || !isState(EventState.STARTED))
		{
			return;
		}

		new EventTeleporter(killedPlayerInstance, _teams[0].getCoords(), false, false);
	}
}
