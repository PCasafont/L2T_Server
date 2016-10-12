package l2server.gameserver.events.instanced.types;

import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.events.instanced.EventConfig;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventTeleporter;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.util.Point3D;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class SimonSays extends EventInstance
{

	private boolean _someoneFailed = false;
	private int _currentSocialActionId = 2;
	private SimonSaysTask _simonSaysTask;
	private ArrayList<Integer> _actedPlayers = new ArrayList<>();

	List<L2PcInstance> _winners = new ArrayList<>();

	public SimonSays(int id, EventConfig config)
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

		startSimonSaysTask();

		return true;
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
		if (_teams[0].getParticipatedPlayerCount() > 0)
		{
			html += "Players staying:<br>";
			for (L2PcInstance participant : _teams[0].getParticipatedPlayers().values())
			{
				if (participant != null)
				{
					html += EventsManager.getInstance().getPlayerString(participant, player) + ", ";
				}
			}
			html = html.substring(0, html.length() - 2) + ".";
		}
		return html;
	}

	@Override
	public boolean onAction(L2PcInstance playerInstance, int targetedPlayerObjectId)
	{
		return false;
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

	public void onSocialAction(L2PcInstance player, int actionId)
	{
		if (_simonSaysTask.isWaiting() || !isPlayerParticipant(player.getObjectId()))
		{
			return;
		}

		if (actionId == _currentSocialActionId)
		{
			_actedPlayers.add(player.getObjectId());
			player.sendPacket(new CreatureSay(0, Say2.TELL, "Instanced Events", "Ok!"));

			player.addEventPoints(_winners.size() + _teams[0].getParticipatedPlayers().size() - _actedPlayers.size());
		}
		else
		{
			_someoneFailed = true;
			player.sendPacket(
					new CreatureSay(0, Say2.TELL, "Instanced Events", "Ooh, error! You have been disqualified."));
			removeParticipant(player.getObjectId());
			_winners.add(0, player);
			new EventTeleporter(player, new Point3D(0, 0, 0), false, true);
		}
	}

	public void simonSays()
	{
		_currentSocialActionId = Rnd.get(16) + 2;
		if (_currentSocialActionId > 15)
		{
			_currentSocialActionId += 12;
		}

		CreatureSay cs = new CreatureSay(0, Say2.BATTLEFIELD, "Simon", getActionString(_currentSocialActionId));
		for (L2PcInstance playerInstance : _teams[0].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
			{
				playerInstance.sendPacket(cs);
			}
		}
	}

	private String getActionString(int actionId)
	{
		String actionString;
		switch (actionId)
		{
			case 2:
				actionString = "Greet!";
				break;
			case 3:
				actionString = "Victory!";
				break;
			case 4:
				actionString = "Advance!";
				break;
			case 5:
				actionString = "No!";
				break;
			case 6:
				actionString = "Yes!";
				break;
			case 7:
				actionString = "Bow!";
				break;
			case 8:
				actionString = "Unaware!";
				break;
			case 9:
				actionString = "Waiting!";
				break;
			case 10:
				actionString = "Laugh!";
				break;
			case 11:
				actionString = "Applaud!";
				break;
			case 12:
				actionString = "Dance!";
				break;
			case 13:
				actionString = "Sorrow!";
				break;
			case 14:
				actionString = "Charm!";
				break;
			case 15:
				actionString = "Shyness!";
				break;
			case 28:
				actionString = "Propose!";
				break;
			default:
				actionString = "Provoke!";
				break;
		}
		return actionString;
	}

	public void endSimonSaysRound()
	{
		CreatureSay cs = new CreatureSay(0, Say2.TELL, "Instanced Events", "Nice! You passed this round!");
		CreatureSay cs2 = new CreatureSay(0, Say2.TELL, "Instanced Events",
				"You have been disqualified for being the last player acting!");
		CreatureSay cs3 =
				new CreatureSay(0, Say2.TELL, "Instanced Events", "You have been disqualified for not doing anything!");

		List<L2PcInstance> participants = new ArrayList<>(_teams[0].getParticipatedPlayers().values());
		for (L2PcInstance playerInstance : participants)
		{
			if (playerInstance != null && !_actedPlayers.contains(playerInstance.getObjectId()))
			{
				_someoneFailed = true;
				removeParticipant(playerInstance.getObjectId());
				new EventTeleporter(playerInstance, new Point3D(0, 0, 0), false, true);
				playerInstance.sendPacket(cs3);
			}
		}

		if (!_someoneFailed && getParticipatedPlayersCount() > 1)
		{
			L2PcInstance player = _teams[0].getParticipatedPlayers().get(_actedPlayers.get(_actedPlayers.size() - 1));
			if (player != null)
			{
				removeParticipant(player.getObjectId());
				new EventTeleporter(player, new Point3D(0, 0, 0), false, true);
				_winners.add(0, player);
				player.sendPacket(cs2);
			}
		}

		for (L2PcInstance playerInstance : _teams[0].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
			{
				playerInstance.sendPacket(cs);
			}
		}

		if (getParticipatedPlayersCount() <= 1)
		{
			stopFight();
		}

		_someoneFailed = false;
		_actedPlayers.clear();
	}

	class SimonSaysTask implements Runnable
	{
		boolean _stop = false;
		boolean _waiting = true;

		@Override
		public void run()
		{
			if (!_stop && isState(EventState.STARTED))
			{
				int delay;
				if (_waiting)
				{
					simonSays();
					delay = 15000;
				}
				else
				{
					endSimonSaysRound();
					delay = 5000;
				}
				_waiting = !_waiting;

				ThreadPoolManager.getInstance().scheduleGeneral(this, delay);
			}
		}

		public void stop()
		{
			_stop = true;
		}

		public boolean isStopped()
		{
			return _stop;
		}

		public boolean isWaiting()
		{
			return _waiting;
		}
	}

	public void startSimonSaysTask()
	{
		if (_simonSaysTask != null && !_simonSaysTask.isStopped())
		{
			_simonSaysTask.stop();
		}
		_simonSaysTask = new SimonSaysTask();
		ThreadPoolManager.getInstance().scheduleGeneral(_simonSaysTask, 30000);
	}
}
