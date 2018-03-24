package l2server.gameserver.instancemanager.arena;

import l2server.gameserver.instancemanager.ArenaManager;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.Vector;

public class Fight
{
	private int id;
	private Vector<Fighter> fighters = new Vector<Fighter>();
	private Rule rules;
	private enum State {LOBBY, PREPARATION, FIGHT, END};
	private State state;
	public void start(){

	}

	public void stop(){

	}

	public void end(int teamId){

		rewardTeam(teamId);
	}

	public boolean addFighter(int teamId, L2PcInstance player){

		if (teamId > rules.getTeamNumbers())
			return false;
		if (getAmountOfFighterInTeam(teamId) >= rules.getTeamMembersAmount()[teamId])
			return false;

		Fighter fighter = new Fighter(this, player, teamId);
		fighters.add(fighter);
		return true;
	}

	public void removeFighter(L2PcInstance player){

		if (ArenaManager.getInstance().isInFight(player)){
			Fighter fighter = ArenaManager.getInstance().getFighter(player);
			if (fighters.contains(fighter)){
				fighters.remove(fighter);
			}
		}
	}

	public int getAmountOfFighterInTeam(int teamId){
		int amount = 0;

		for (Fighter f : fighters){
			if (f == null)
				continue;
			if (f.getTeamId() == teamId)
				amount++;
		}
		return amount;
	}

	public void destructMe(){

	}

	public void playerLeft(Fighter fighter){

	}

	public void fighterKilled(Fighter killer, Fighter killed){

		//Don't forget to check the rules
		if (getAmountOfFighterInTeam(killed.getTeamId()) == 0){
			end(killer.getTeamId());
		}
	}

	public Fight(Rule rules){
		this.rules = rules;
		state = State.LOBBY;
	}

	public void loadRules(Rule rules){}

	public Rule getRules(){return rules;}

	public void rewardTeam(int teamId){

		Vector<Fighter> winners = new Vector<Fighter>();

		for (Fighter f : fighters){
			if (f == null)
				continue;
			if (f.getTeamId() == teamId){
				winners.add(f);
			}
		}

		for (Fighter f : winners){
			if (f == null)
				continue;
		}

	}

	public void interupt(){

	}

	public void launchFight(){

	}

	public void teleportFighters(){

	}

	public void spawnBuffers(){

	}

	public void unspawnBuffers(){

	}

	public void announceToFighters(String s){
		for (Fighter f : fighters){
			if (f == null)
				continue;
			L2PcInstance p  = f.getPlayerInstance();
			if (p != null){
				p.sendMessage(s);
			}
		}
	}

	public Vector<Fighter> getFighters()
	{
		return fighters;
	}

	public int getId()
	{
		return id;
	}
}
