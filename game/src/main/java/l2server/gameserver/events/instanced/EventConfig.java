package l2server.gameserver.events.instanced;

import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.events.instanced.types.*;
import l2server.util.Rnd;

/**
 * @author Pere
 */
public class EventConfig {
	private EventType type = EventType.TVT;
	private EventLocation location = null;
	private String[] teamNames = new String[4];
	
	public EventConfig() {
		selectEvent();
		
		if (location.getTeamCount() == 4) {
			while (selectTeamNames(Rnd.get(4))) {
			}
		} else {
			while (!selectTeamNames(Rnd.get(4))) {
			}
		}
	}
	
	public EventConfig(boolean pvp) {
		selectEvent();
		while (pvp != isPvp()) {
			selectEvent();
		}
		
		if (location.getTeamCount() == 4) {
			while (selectTeamNames(Rnd.get(4))) {
			}
		} else {
			while (!selectTeamNames(Rnd.get(4))) {
			}
		}
	}
	
	public boolean isType(EventType type) {
		return type == type;
	}
	
	public EventType getType() {
		return type;
	}
	
	public boolean isAllVsAll() {
		return type == EventType.Survival || type == EventType.DeathMatch || type == EventType.KingOfTheHill || type == EventType.CursedBattle ||
				type == EventType.StalkedSalkers || type == EventType.SimonSays;
	}
	
	public boolean needsClosedArena() {
		return type == EventType.CaptureTheFlag || type == EventType.VIP || type == EventType.Survival || type == EventType.TeamSurvival ||
				type == EventType.CursedBattle || type == EventType.StalkedSalkers;
	}
	
	public boolean spawnsPlayersRandomly() {
		return type == EventType.Survival || type == EventType.DeathMatch || type == EventType.CursedBattle || type == EventType.StalkedSalkers ||
				type == EventType.SimonSays;
	}
	
	public boolean needsRandomCoords() {
		return spawnsPlayersRandomly() || type == EventType.LuckyChests;
	}
	
	public boolean hasNoLevelLimits() {
		return type == EventType.LuckyChests || type == EventType.StalkedSalkers || type == EventType.SimonSays;
	}
	
	public boolean isPvp() {
		return type == EventType.TVT || type == EventType.CaptureTheFlag || type == EventType.VIP || type == EventType.Survival ||
				type == EventType.DeathMatch || type == EventType.TeamSurvival || type == EventType.CursedBattle;
	}
	
	public String getTeamName(int id) {
		return teamNames[id];
	}
	
	public boolean selectTeamNames(int name) {
		boolean dual = false;
		switch (name) {
			case 0:
				teamNames[0] = "Blue";
				teamNames[1] = "Red";
				dual = true;
				break;
			case 1:
				teamNames[0] = "Water";
				teamNames[1] = "Fire";
				teamNames[2] = "Earth";
				teamNames[3] = "Wind";
				break;
			case 2:
				teamNames[0] = "Winter";
				teamNames[1] = "Autumn";
				teamNames[2] = "Summer";
				teamNames[3] = "Spring";
				break;
			case 3:
				teamNames[0] = "Blue";
				teamNames[1] = "Red";
				teamNames[2] = "Yellow";
				teamNames[3] = "Green";
		}
		
		return dual;
	}
	
	private void selectEvent() {
		double[] chances = new double[]{25.0, // TvT
				4.0, // Capture the Flag
				5.0, // VIP TvT
				9.0, // Survival
				11.0, // Death Match
				2.0, // King of the Hill
				0.0, // Lucky Chests
				18.0, // Team Survival
				0.0, // Cursed Battle
				5.0, // Destroy the Golem
				2.0, // Field Domination
				2.0, // Stalked Stalkers
				6.0 // Simon Says
		};
		
		double total = 0;
		for (double chance : chances) {
			total += chance;
		}
		
		double type = Rnd.get() * total;
		for (int i = 0; i < chances.length; i++) {
			type -= chances[i];
			if (type < 0.0) {
				this.type = EventType.values()[i];
				break;
			}
		}
		
		selectLocation();
	}
	
	public EventLocation getLocation() {
		return location;
	}
	
	public void setLocation(EventLocation location) {
		this.location = location;
	}
	
	public void selectLocation() {
		location = EventsManager.getInstance().getRandomLocation();
		
		if (needsClosedArena() || needsRandomCoords()) {
			while (location.getZone() == null) {
				location = EventsManager.getInstance().getRandomLocation();
			}
		} else if (isType(EventType.KingOfTheHill)) {
			while (!location.isHill()) {
				location = EventsManager.getInstance().getRandomLocation();
			}
		}
	}
	
	public int getMaxTeamPlayers() {
		return isAllVsAll() ? location.getMaxPlayers() : location.getMaxTeamPlayers();
	}
	
	public int getMinPlayers() {
		return isAllVsAll() ? 2 : location.getTeamCount();
	}
	
	public EventInstance createInstance(int id) {
		switch (type) {
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
	
	public String getEventName() {
		if (location == null) {
			return "No event";
		}
		
		switch (type) {
			case CaptureTheFlag:
				if (location.getTeamCount() == 4) {
					return "Four teams Capture the Flag";
				}
				
				return "Capture the Flag";
			
			case VIP:
				if (location.getTeamCount() == 4) {
					return "VIP TvTvTvT";
				}
				
				return "VIP TvT";
			
			case Survival:
				return "Survival";
			
			case DeathMatch:
				return "Death Match";
			
			case LuckyChests:
				if (location.getTeamCount() == 4) {
					return "Four teams Lucky Chests";
				}
				
				return "Lucky Chests";
			
			case KingOfTheHill:
				return "King of The Hill";
			
			case TeamSurvival:
				if (location.getTeamCount() == 4) {
					return "Team Survival";
				}
				
				return "Team Survival";
			
			case CursedBattle:
				return "Cursed Battle";
			
			case DestroyTheGolem:
				if (location.getTeamCount() == 4) {
					return "Four teams Destroy the Golem";
				}
				
				return "Destroy the Golem";
			
			case FieldDomination:
				if (location.getTeamCount() == 4) {
					return "Four teams Field Domination";
				}
				
				return "Field Domination";
			
			case StalkedSalkers:
				return "Stalked Stalkers";
			
			case SimonSays:
				return "Simon Says";
			
			default:
				if (location.getTeamCount() == 4) {
					return "TvTvTvT";
				}
				
				return "Team vs Team";
		}
	}
	
	public String getEventLocationName() {
		if (location == null) {
			return "No event";
		}
		
		return location.getName();
	}
	
	public int getEventImageId() {
		switch (type) {
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
	
	public String getEventString() {
		if (location == null) {
			return "No event";
		}
		
		String eventString;
		switch (type) {
			case CaptureTheFlag:
				eventString = "Capture the Flag";
				if (location.getTeamCount() == 4) {
					eventString = "Four teams Capture the Flag";
				}
				break;
			case VIP:
				eventString = "VIP TvT";
				if (location.getTeamCount() == 4) {
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
				if (location.getTeamCount() == 4) {
					eventString = "Four teams Lucky Chests";
				}
				break;
			case KingOfTheHill:
				eventString = "King of The Hill";
				break;
			case TeamSurvival:
				eventString = "Team Survival";
				if (location.getTeamCount() == 4) {
					eventString = "Four Teams Survival";
				}
				break;
			case CursedBattle:
				eventString = "Cursed Battle";
				break;
			case DestroyTheGolem:
				eventString = "Destroy the Golem";
				if (location.getTeamCount() == 4) {
					eventString = "Four teams Destroy the Golem";
				}
				break;
			case FieldDomination:
				eventString = "Field Domination";
				if (location.getTeamCount() == 4) {
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
				if (location.getTeamCount() == 4) {
					eventString = "TvTvTvT";
				}
		}
		
		eventString += " at " + location.getName();
		
		return eventString;
	}
}
