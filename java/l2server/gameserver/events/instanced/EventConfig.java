package l2server.gameserver.events.instanced;

import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.events.instanced.types.*;
import l2server.util.Rnd;

/**
 * @author Pere
 */
public class EventConfig
{
	private EventType _type = EventType.TVT;
	private EventLocation _location = null;
	private String[] _teamNames = new String[4];

	public EventConfig()
	{
		selectEvent();

		if (_location.getTeamCount() == 4)
		{
			while (selectTeamNames(Rnd.get(4)))
			{
			}
		}
		else
		{
			while (!selectTeamNames(Rnd.get(4)))
			{
			}
		}
	}

	public EventConfig(boolean pvp)
	{
		selectEvent();
		while (pvp != isPvp())
		{
			selectEvent();
		}

		if (_location.getTeamCount() == 4)
		{
			while (selectTeamNames(Rnd.get(4)))
			{
			}
		}
		else
		{
			while (!selectTeamNames(Rnd.get(4)))
			{
			}
		}
	}

	public boolean isType(EventType type)
	{
		return _type == type;
	}

	public EventType getType()
	{
		return _type;
	}

	public boolean isAllVsAll()
	{
		return _type == EventType.Survival || _type == EventType.DeathMatch || _type == EventType.KingOfTheHill ||
				_type == EventType.CursedBattle || _type == EventType.StalkedSalkers || _type == EventType.SimonSays;
	}

	public boolean needsClosedArena()
	{
		return _type == EventType.CaptureTheFlag || _type == EventType.VIP || _type == EventType.Survival ||
				_type == EventType.TeamSurvival || _type == EventType.CursedBattle || _type == EventType.StalkedSalkers;
	}

	public boolean spawnsPlayersRandomly()
	{
		return _type == EventType.Survival || _type == EventType.DeathMatch || _type == EventType.CursedBattle ||
				_type == EventType.StalkedSalkers || _type == EventType.SimonSays;
	}

	public boolean needsRandomCoords()
	{
		return spawnsPlayersRandomly() || _type == EventType.LuckyChests;
	}

	public boolean hasNoLevelLimits()
	{
		return _type == EventType.LuckyChests || _type == EventType.StalkedSalkers || _type == EventType.SimonSays;
	}

	public boolean isPvp()
	{
		return _type == EventType.TVT || _type == EventType.CaptureTheFlag || _type == EventType.VIP ||
				_type == EventType.Survival || _type == EventType.DeathMatch || _type == EventType.TeamSurvival ||
				_type == EventType.CursedBattle;
	}

	public String getTeamName(int id)
	{
		return _teamNames[id];
	}

	public boolean selectTeamNames(int name)
	{
		boolean dual = false;
		switch (name)
		{
			case 0:
				_teamNames[0] = "Blue";
				_teamNames[1] = "Red";
				dual = true;
				break;
			case 1:
				_teamNames[0] = "Water";
				_teamNames[1] = "Fire";
				_teamNames[2] = "Earth";
				_teamNames[3] = "Wind";
				break;
			case 2:
				_teamNames[0] = "Winter";
				_teamNames[1] = "Autumn";
				_teamNames[2] = "Summer";
				_teamNames[3] = "Spring";
				break;
			case 3:
				_teamNames[0] = "Blue";
				_teamNames[1] = "Red";
				_teamNames[2] = "Yellow";
				_teamNames[3] = "Green";
		}

		return dual;
	}

	private void selectEvent()
	{
		double[] chances = new double[]{
				25.0, // TvT
				4.0, // Capture the Flag
				0.0, // VIP TvT
				9.0, // Survival
				11.0, // Death Match
				0.0, // King of the Hill
				0.0, // Lucky Chests
				18.0, // Team Survival
				0.0, // Cursed Battle
				0.0, // Destroy the Golem
				0.0, // Field Domination
				2.0, // Stalked Stalkers
				6.0 // Simon Says
		};

		double total = 0;
		for (double chance : chances)
		{
			total += chance;
		}

		double type = Rnd.get() * total;
		for (int i = 0; i < chances.length; i++)
		{
			type -= chances[i];
			if (type < 0.0)
			{
				_type = EventType.values()[i];
				break;
			}
		}

		selectLocation();
	}

	public EventLocation getLocation()
	{
		return _location;
	}

	public void setLocation(EventLocation location)
	{
		_location = location;
	}

	public void selectLocation()
	{
		_location = EventsManager.getInstance().getRandomLocation();

		if (needsClosedArena() || needsRandomCoords())
		{
			while (_location.getZone() == null)
			{
				_location = EventsManager.getInstance().getRandomLocation();
			}
		}
		else if (isType(EventType.KingOfTheHill))
		{
			while (!_location.isHill())
			{
				_location = EventsManager.getInstance().getRandomLocation();
			}
		}
	}

	public int getMaxTeamPlayers()
	{
		return isAllVsAll() ? _location.getMaxPlayers() : _location.getMaxTeamPlayers();
	}

	public int getMinPlayers()
	{
		return isAllVsAll() ? 2 : _location.getTeamCount();
	}

	public EventInstance createInstance(int id)
	{
		switch (_type)
		{
			case TVT:
				return new TeamVsTeam(id, this);
			case CaptureTheFlag:
				return new CaptureTheFlag(id, this);
			case VIP:
				return new VIPTeamVsTeam(id, this);
			case DeathMatch:
				return new DeathMatch(id, this);
			case Survival:
				return new Survival(id, this);
			case KingOfTheHill:
				return new KingOfTheHill(id, this);
			case LuckyChests:
				return new LuckyChests(id, this);
			case TeamSurvival:
				return new TeamSurvival(id, this);
			case CursedBattle:
				return new CursedBattle(id, this);
			case DestroyTheGolem:
				return new DestroyTheGolem(id, this);
			case FieldDomination:
				return new FieldDomination(id, this);
			case StalkedSalkers:
				return new StalkedStalkers(id, this);
		}

		return new SimonSays(id, this);
	}

	public String getEventName()
	{
		if (_location == null)
		{
			return "No event";
		}

		switch (_type)
		{
			case CaptureTheFlag:
				if (_location.getTeamCount() == 4)
				{
					return "Four teams Capture the Flag";
				}

				return "Capture the Flag";

			case VIP:
				if (_location.getTeamCount() == 4)
				{
					return "VIP TvTvTvT";
				}

				return "VIP TvT";

			case Survival:
				return "Survival";

			case DeathMatch:
				return "Death Match";

			case LuckyChests:
				if (_location.getTeamCount() == 4)
				{
					return "Four teams Lucky Chests";
				}

				return "Lucky Chests";

			case KingOfTheHill:
				return "King of The Hill";

			case TeamSurvival:
				if (_location.getTeamCount() == 4)
				{
					return "Team Survival";
				}

				return "Team Survival";

			case CursedBattle:
				return "Cursed Battle";

			case DestroyTheGolem:
				if (_location.getTeamCount() == 4)
				{
					return "Four teams Destroy the Golem";
				}

				return "Destroy the Golem";

			case FieldDomination:
				if (_location.getTeamCount() == 4)
				{
					return "Four teams Field Domination";
				}

				return "Field Domination";

			case StalkedSalkers:
				return "Stalked Stalkers";

			case SimonSays:
				return "Simon Says";

			default:
				if (_location.getTeamCount() == 4)
				{
					return "TvTvTvT";
				}

				return "Team vs Team";
		}
	}

	public String getEventLocationName()
	{
		if (_location == null)
		{
			return "No event";
		}

		return _location.getName();
	}

	public int getEventImageId()
	{
		switch (_type)
		{
			case TVT:
				return 20012;
			case CaptureTheFlag:
				return 20002;
			case VIP:
				return 20013;
			case Survival:
				return 20004;
			case DeathMatch:
				return 20009;
			case KingOfTheHill:
				return 20008;
			case LuckyChests:
				return 20001;
			case TeamSurvival:
				return 20005;
			case CursedBattle:
				return 20003;
			case DestroyTheGolem:
				return 20007;
			case FieldDomination:
				return 20006;
			case StalkedSalkers:
				return 20010;
			case SimonSays:
				return 20011;
		}

		return 0;
	}

	public String getEventString()
	{
		if (_location == null)
		{
			return "No event";
		}

		String eventString;
		switch (_type)
		{
			case CaptureTheFlag:
				eventString = "Capture the Flag";
				if (_location.getTeamCount() == 4)
				{
					eventString = "Four teams Capture the Flag";
				}
				break;
			case VIP:
				eventString = "VIP TvT";
				if (_location.getTeamCount() == 4)
				{
					eventString = "VIP TvTvTvT";
				}
				break;
			case Survival:
				eventString = "Survival";
				break;
			case DeathMatch:
				eventString = "Death Match";
				break;
			case LuckyChests:
				eventString = "Lucky Chests";
				if (_location.getTeamCount() == 4)
				{
					eventString = "Four teams Lucky Chests";
				}
				break;
			case KingOfTheHill:
				eventString = "King of The Hill";
				break;
			case TeamSurvival:
				eventString = "Team Survival";
				if (_location.getTeamCount() == 4)
				{
					eventString = "Four Teams Survival";
				}
				break;
			case CursedBattle:
				eventString = "Cursed Battle";
				break;
			case DestroyTheGolem:
				eventString = "Destroy the Golem";
				if (_location.getTeamCount() == 4)
				{
					eventString = "Four teams Destroy the Golem";
				}
				break;
			case FieldDomination:
				eventString = "Field Domination";
				if (_location.getTeamCount() == 4)
				{
					eventString = "Four teams Field Domination";
				}
				break;
			case StalkedSalkers:
				eventString = "Stalked Stalkers";
				break;
			case SimonSays:
				eventString = "Simon Says";
				break;
			default:
				eventString = "Team vs Team";
				if (_location.getTeamCount() == 4)
				{
					eventString = "TvTvTvT";
				}
		}

		eventString += " at " + _location.getName();

		return eventString;
	}
}
