package l2server.gameserver.instancemanager.arena;

import java.util.Vector;

public class Rule
{
	private int teamNumbers;
	private int[] teamMembersAmount;
	private enum SpecialRules {D, C, B, A, S, S80, S84, R, R94, R99};
	private Vector<SpecialRules> rules = new Vector<SpecialRules>();

	public Rule(int teamNumbers){
		this.teamNumbers = teamNumbers;
	}

	public void addSpecialRules(SpecialRules special){
		if (rules.contains(special))
			return;
		rules.add(special);
	}

	public int getTeamNumbers()
	{
		return teamNumbers;
	}

	public int[] getTeamMembersAmount()
	{
		return teamMembersAmount;
	}

	public void setTeamsNumbers(int number){
		teamNumbers = number;
	}

	public void setTeamMemberAmount(int teamId, int amount){
		teamMembersAmount[teamId] = amount;
	}
}
