package l2server.gameserver.events.instanced;

import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.events.instanced.types.*;
import l2server.util.Rnd;

/**
 * @author Pere
 */
public class EventConfig
{
	private EventType type = EventType.TVT;
	private EventLocation location = null;
	private String[] teamNames = new String[4];

	public EventConfig()
	{
		selectEvent();

		if (this.location.getTeamCount() == 4)
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

		if (this.location.getTeamCount() == 4)
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
		return this.type == type;
	}

	public EventType getType()
	{
		return this.type;
	}

	public boolean isAllVsAll()
	{
		return this.type == EventType.Survival || this.type == EventType.DeathMatch || this.type == EventType.KingOfTheHill ||
				this.type == EventType.CursedBattle || this.type == EventType.StalkedSalkers || this.type == EventType.SimonSays;
	}

	public boolean needsClosedArena()
	{
		return this.type == EventType.CaptureTheFlag || this.type == EventType.VIP || this.type == EventType.Survival ||
				this.type == EventType.TeamSurvival || this.type == EventType.CursedBattle || this.type == EventType.StalkedSalkers;
	}

	public boolean spawnsPlayersRandomly()
	{
		return this.type == EventType.Survival || this.type == EventType.DeathMatch || this.type == EventType.CursedBattle ||
				this.type == EventType.StalkedSalkers || this.type == EventType.SimonSays;
	}

	public boolean needsRandomCoords()
	{
		return spawnsPlayersRandomly() || this.type == EventType.LuckyChests;
	}

	public boolean hasNoLevelLimits()
	{
		return this.type == EventType.LuckyChests || this.type == EventType.StalkedSalkers || this.type == EventType.SimonSays;
	}

	public boolean isPvp()
	{
		return this.type == EventType.TVT || this.type == EventType.CaptureTheFlag || this.type == EventType.VIP ||
				this.type == EventType.Survival || this.type == EventType.DeathMatch || this.type == EventType.TeamSurvival ||
				this.type == EventType.CursedBattle;
	}

	public String getTeamName(int id)
	{
		return this.teamNames[id];
	}

	public boolean selectTeamNames(int name)
	{
		boolean dual = false;
		switch (name)
		{
			case 0:
				this.teamNames[0] = "Blue";
				this.teamNames[1] = "Red";
				dual = true;
				break;
			case 1:
				this.teamNames[0] = "Water";
				this.teamNames[1] = "Fire";
				this.teamNames[2] = "Earth";
				this.teamNames[3] = "Wind";
				break;
			case 2:
				this.teamNames[0] = "Winter";
				this.teamNames[1] = "Autumn";
				this.teamNames[2] = "Summer";
				this.teamNames[3] = "Spring";
				break;
			case 3:
				this.teamNames[0] = "Blue";
				this.teamNames[1] = "Red";
				this.teamNames[2] = "Yellow";
				this.teamNames[3] = "Green";
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
				this.type = EventType.values()[i];
				break;
			}
		}

		selectLocation();
	}

	public EventLocation getLocation()
	{
		return this.location;
	}

	public void setLocation(EventLocation location)
	{
		this.location = location;
	}

	public void selectLocation()
	{
		this.location = EventsManager.getInstance().getRandomLocation();

		if (needsClosedArena() || needsRandomCoords())
		{
			while (this.location.getZone() == null)
			{
				this.location = EventsManager.getInstance().getRandomLocation();
			}
		}
		else if (isType(EventType.KingOfTheHill))
		{
			while (!this.location.isHill())
			{
				this.location = EventsManager.getInstance().getRandomLocation();
			}
		}
	}

	public int getMaxTeamPlayers()
	{
		return isAllVsAll() ? this.location.getMaxPlayers() : this.location.getMaxTeamPlayers();
	}

	public int getMinPlayers()
	{
		return isAllVsAll() ? 2 : this.location.getTeamCount();
	}

	public EventInstance createInstance(int id)
	{
		switch (this.type)
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
		if (this.location == null)
		{
			return "No event";
		}

		switch (this.type)
		{
			case CaptureTheFlag:
				if (this.location.getTeamCount() == 4)
				{
					return "Four teams Capture the Flag";
				}

				return "Capture the Flag";

			case VIP:
				if (this.location.getTeamCount() == 4)
				{
					return "VIP TvTvTvT";
				}

				return "VIP TvT";

			case Survival:
				return "Survival";

			case DeathMatch:
				return "Death Match";

			case LuckyChests:
				if (this.location.getTeamCount() == 4)
				{
					return "Four teams Lucky Chests";
				}

				return "Lucky Chests";

			case KingOfTheHill:
				return "King of The Hill";

			case TeamSurvival:
				if (this.location.getTeamCount() == 4)
				{
					return "Team Survival";
				}

				return "Team Survival";

			case CursedBattle:
				return "Cursed Battle";

			case DestroyTheGolem:
				if (this.location.getTeamCount() == 4)
				{
					return "Four teams Destroy the Golem";
				}

				return "Destroy the Golem";

			case FieldDomination:
				if (this.location.getTeamCount() == 4)
				{
					return "Four teams Field Domination";
				}

				return "Field Domination";

			case StalkedSalkers:
				return "Stalked Stalkers";

			case SimonSays:
				return "Simon Says";

			default:
				if (this.location.getTeamCount() == 4)
				{
					return "TvTvTvT";
				}

				return "Team vs Team";
		}
	}

	public String getEventLocationName()
	{
		if (this.location == null)
		{
			return "No event";
		}

		return this.location.getName();
	}

	public int getEventImageId()
	{
		switch (this.type)
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
		if (this.location == null)
		{
			return "No event";
		}

		String eventString;
		switch (this.type)
		{
			case CaptureTheFlag:
				eventString = "Capture the Flag";
				if (this.location.getTeamCount() == 4)
				{
					eventString = "Four teams Capture the Flag";
				}
				break;
			case VIP:
				eventString = "VIP TvT";
				if (this.location.getTeamCount() == 4)
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
				if (this.location.getTeamCount() == 4)
				{
					eventString = "Four teams Lucky Chests";
				}
				break;
			case KingOfTheHill:
				eventString = "King of The Hill";
				break;
			case TeamSurvival:
				eventString = "Team Survival";
				if (this.location.getTeamCount() == 4)
				{
					eventString = "Four Teams Survival";
				}
				break;
			case CursedBattle:
				eventString = "Cursed Battle";
				break;
			case DestroyTheGolem:
				eventString = "Destroy the Golem";
				if (this.location.getTeamCount() == 4)
				{
					eventString = "Four teams Destroy the Golem";
				}
				break;
			case FieldDomination:
				eventString = "Field Domination";
				if (this.location.getTeamCount() == 4)
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
				if (this.location.getTeamCount() == 4)
				{
					eventString = "TvTvTvT";
				}
		}

		eventString += " at " + this.location.getName();

		return eventString;
	}
}
