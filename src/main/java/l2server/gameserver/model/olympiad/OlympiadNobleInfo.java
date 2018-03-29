/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.model.olympiad;

/**
 * @author Pere
 */
public class OlympiadNobleInfo
{
	private final int id;
	private final String name;
	private final int classId;

	private int points = Olympiad.DEFAULT_POINTS;
	private int matches = 0;
	private int victories = 0;
	private int defeats = 0;
	private int draws = 0;
	private int classedMatches = 0;
	private int nonClassedMatches = 0;
	private boolean settled = false;

	private boolean toSave = false;

	public OlympiadNobleInfo(int id, String name, int classId)
	{
		this.id = id;
		this.name = name;
		this.classId = classId;
		toSave = true;
	}

	public OlympiadNobleInfo(int id, String name, int classId, int points, int matches, int victories, int defeats, int draws, int classedMatches, int nonClassedMatches, boolean settled)
	{
		this.id = id;
		this.name = name;
		this.classId = classId;

		this.points = points;
		this.matches = matches;
		this.victories = victories;
		this.defeats = defeats;
		this.draws = draws;
		this.classedMatches = classedMatches;
		this.nonClassedMatches = nonClassedMatches;
		this.settled = settled;
	}

	public void addWeeklyPoints(int weeklyPoints)
	{
		points += weeklyPoints;

		// Also reset the competitions that the player could do this week
		classedMatches = 0;
		nonClassedMatches = 0;
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public int getClassId()
	{
		return classId;
	}

	public int getPoints()
	{
		return points;
	}

	public void setPoints(int points)
	{
		this.points = points;
	}

	public void increaseMatches()
	{
		matches++;
	}

	public void increaseVictories()
	{
		victories++;
	}

	public void increaseDefeats()
	{
		defeats++;
	}

	public void increaseDraws()
	{
		draws++;
	}

	public void increaseClassedMatches()
	{
		classedMatches++;
	}

	public void increaseNonClassedMatches()
	{
		nonClassedMatches++;
	}

	public int getMatches()
	{
		return matches;
	}

	public int getVictories()
	{
		return victories;
	}

	public int getDefeats()
	{
		return defeats;
	}

	public int getDraws()
	{
		return draws;
	}

	public int getClassedMatches()
	{
		return classedMatches;
	}

	public int getNonClassedMatches()
	{
		return nonClassedMatches;
	}

	public int getMatchesThisWeek()
	{
		return classedMatches + nonClassedMatches;
	}

	public boolean isSettled()
	{
		return settled;
	}

	public void setSettled(boolean settled)
	{
		this.settled = settled;
	}

	public boolean isToSave()
	{
		return toSave;
	}

	public void setToSave(boolean toSave)
	{
		this.toSave = toSave;
	}
}
