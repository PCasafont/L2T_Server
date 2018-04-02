package l2server.gameserver.instancemanager.arena;

import l2server.gameserver.instancemanager.ArenaManager;
import l2server.gameserver.model.actor.instance.Player;

public class Fighter {

	private int arenaPoints;
	private int charId;
	private Fight fight;
	private Player playerInstance;
	private int wins;
	private int loses;
	private int teamId;

	public int getWins() {
		return wins;
	}

	public int getLoses() {
		return loses;
	}

	public int getCharId() {
		return charId;
	}

	public Player getPlayerInstance() {
		return playerInstance;
	}

	public int getArenaPoints() {
		return arenaPoints;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}

	//LINKED
	public void onDie(Player killer) {

	}

	//LINKED
	public void onKill(Player victim) {
		Fighter fVictim = ArenaManager.getInstance().getFighter(victim);
		fight.fighterKilled(this, fVictim);
	}

	//LINKED
	public void onHit(Player victim) {

	}

	//LINKED
	public void onDisconnect() {

		fight.playerLeft(this);
	}

	public void setFight(Fight fight) {
		this.fight = fight;
	}

	public Fighter(Fight fight, Player player, int teamId) {
		this.fight = fight;
		this.playerInstance = player;
		this.charId = player.getObjectId();
		this.teamId = teamId;
	}

	public Fight getFight() {
		return fight;
	}
}
