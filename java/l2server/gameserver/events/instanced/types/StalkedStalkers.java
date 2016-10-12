package l2server.gameserver.events.instanced.types;

import l2server.gameserver.Announcements;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.events.instanced.EventConfig;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventTeleporter;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.util.Point3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Pere
 */
public class StalkedStalkers extends EventInstance
{

	private HashMap<Integer, String> _assignedStalkers = new HashMap<>();

	public StalkedStalkers(int id, EventConfig config)
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

		// Iterate over all participated players
		for (L2PcInstance playerInstance : _teams[0].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
			{
				L2PcInstance randomStalkedPlayer = selectRandomParticipant();
				int limit = 0;
				while (randomStalkedPlayer.getObjectId() == playerInstance.getObjectId() && limit < 10)
				{
					randomStalkedPlayer = selectRandomParticipant();
					limit++;
				}
				_assignedStalkers.put(playerInstance.getObjectId(), randomStalkedPlayer.getName());
				stalkerMessage(playerInstance, -1);
				playerInstance.addEventPoints(5);
			}
		}

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
		Announcements.getInstance().announceToAll(
				"The event has ended. The player " + sorted.get(0).getName() + " won with " +
						sorted.get(0).getEventPoints() + " points");
	}

	@Override
	public void stopFight()
	{
		_assignedStalkers.clear();
		super.stopFight();
	}

	@Override
	public String getRunningInfo(L2PcInstance player)
	{
		String html = "";
		if (_teams[0].getParticipatedPlayerCount() > 0)
		{
			html += "Participant points:<br>";
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
	public void onKill(L2Character killerCharacter, L2PcInstance killedPlayerInstance)
	{
		if (killedPlayerInstance == null || !isState(EventState.STARTED))
		{
			return;
		}

		new EventTeleporter(killedPlayerInstance, _teams[0].getCoords(), false, false);

		if (killerCharacter == null)
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

		if (killedPlayerInstance.getName().equalsIgnoreCase(_assignedStalkers.get(killerPlayerInstance.getObjectId())))
		{
			_assignedStalkers.remove(killerPlayerInstance.getObjectId());
			L2PcInstance randomStalkedPlayer = selectRandomParticipant();
			while (randomStalkedPlayer.getObjectId() == killerPlayerInstance.getObjectId())
			{
				randomStalkedPlayer = selectRandomParticipant();
			}
			_assignedStalkers.put(killerPlayerInstance.getObjectId(), randomStalkedPlayer.getName());
			stalkerMessage(killerPlayerInstance, -2);
			killerPlayerInstance.addEventPoints(3);
		}
		else
		{
			killerPlayerInstance.addEventPoints(-1);
			if (killerPlayerInstance.getEventPoints() < 0)
			{
				killerPlayerInstance.sendPacket(new CreatureSay(0, Say2.TELL, "Instanced Events",
						"Your stupidity has tired me. Get out from here!"));
				removeParticipant(killerPlayerInstance.getObjectId());
				new EventTeleporter(killerPlayerInstance, new Point3D(0, 0, 0), false, true);
			}
			else
			{
				stalkerMessage(killerPlayerInstance, 0);
			}
		}
	}

	public void stalkerMessage(L2PcInstance stalker, int level)
	{
		if (stalker == null)
		{
			return;
		}

		L2PcInstance player = null;
		String hint;
		if (level < 0 || level == 5)
		{
			stalker.startStalkerHintsTask();
		}
		if (level > 0)
		{
			player = L2World.getInstance().getPlayer(_assignedStalkers.get(stalker.getObjectId()));
			if (player == null || player.getEvent() != this)
			{
				_assignedStalkers.remove(stalker.getObjectId());
				L2PcInstance randomStalkedPlayer = selectRandomParticipant();
				while (randomStalkedPlayer.getObjectId() == stalker.getObjectId())
				{
					randomStalkedPlayer = selectRandomParticipant();
				}
				_assignedStalkers.put(stalker.getObjectId(), randomStalkedPlayer.getName());
				level = -3;
			}
		}

		switch (level)
		{
			case -3:
				hint = "Oh, your target left the game! You'll have to stalk another one. Let's begin again!";
				break;
			case -2:
				hint = "Mission complete! You have been awarded with 3 points. Ready for the next target?";
				break;
			case -1:
				hint =
						"Ok, let's begin: You have to kill someone. Who? Watch out for the hints that I will be sending to you!";
				break;
			case 0:
				hint = "Damn, his wasn't your target! You lost 1 point. Continue searching...";
				break;
			case 1:
				hint = "Hint 1: Your target is a " + (player.getAppearance().getSex() ? "Woman" : "Man") + ".";
				break;
			case 2:
				hint = "Hint 2: Your target's race is " + player.getVisibleTemplate().race.toString() + ".";
				break;
			case 3:
				if (player.getActiveClass() != player.getBaseClass())
				{
					hint = "Hint 3: Tour target's current class is " +
							PlayerClassTable.getInstance().getClassNameById(player.getActiveClass()) + ", " +
							"and its base class is " +
							PlayerClassTable.getInstance().getClassNameById(player.getBaseClass()) + ".";
				}
				else
				{
					hint = "Hint 3: Tour target's class is " +
							PlayerClassTable.getInstance().getClassNameById(player.getBaseClass()) + ".";
				}
				break;
			case 4:
				hint = "Hint 4: Your target is " + player.getName() + "! Slay him!";
				break;
			default:
				hint = "Too late! How can you be that slow? Let's try with another target...";
		}

		stalker.sendPacket(new CreatureSay(0, Say2.TELL, "Instanced Events", hint));
	}

	@Override
	public boolean removeParticipant(int playerObjectId)
	{
		if (!super.removeParticipant(playerObjectId))
		{
			return false;
		}

		String playerName = CharNameTable.getInstance().getNameById(playerObjectId);

		List<Integer> toIterate = new ArrayList<>(_assignedStalkers.keySet());
		for (int stalkerObjId : toIterate)
		{
			if (_assignedStalkers.get(stalkerObjId).equals(playerName))
			{
				_assignedStalkers.remove(stalkerObjId);
				L2PcInstance randomStalkedPlayer = selectRandomParticipant();
				while (randomStalkedPlayer.getObjectId() == stalkerObjId)
				{
					randomStalkedPlayer = selectRandomParticipant();
				}
				_assignedStalkers.put(stalkerObjId, randomStalkedPlayer.getName());
				stalkerMessage(L2World.getInstance().getAllPlayers().get(stalkerObjId), -3);
			}
		}

		return true;
	}
}
