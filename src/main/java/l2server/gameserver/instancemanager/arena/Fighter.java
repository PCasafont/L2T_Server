package l2server.gameserver.instancemanager.arena;

import l2server.gameserver.instancemanager.ArenaManager;
import l2server.gameserver.model.actor.instance.L2PcInstance;

public class Fighter {

	private int arenaPoints;
	private int charId;
	private Fight fight;
	private L2PcInstance playerInstance;
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

	public L2PcInstance getPlayerInstance() {
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
	public void onDie(L2PcInstance killer) {

	}

	//LINKED
	public void onKill(L2PcInstance victim) {
		Fighter fVictim = ArenaManager.getInstance().getFighter(victim);
		fight.fighterKilled(this, fVictim);
	}

	//LINKED
	public void onHit(L2PcInstance victim) {

	}

	//LINKED
	public void onDisconnect() {

		fight.playerLeft(this);
	}

	public void setFight(Fight fight) {
		this.fight = fight;
	}

	public Fighter(Fight fight, L2PcInstance player, int teamId) {
		this.fight = fight;
		this.playerInstance = player;
		this.charId = player.getObjectId();
		this.teamId = teamId;
	}

	public Fight getFight() {
		return fight;
	}
}
