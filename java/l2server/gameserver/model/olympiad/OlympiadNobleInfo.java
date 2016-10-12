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
	private final int _id;
	private final String _name;
	private final int _classId;

	private int _points = Olympiad.DEFAULT_POINTS;
	private int _matches = 0;
	private int _victories = 0;
	private int _defeats = 0;
	private int _draws = 0;
	private int _classedMatches = 0;
	private int _nonClassedMatches = 0;
	private boolean _settled = false;

	private boolean _toSave = false;

	public OlympiadNobleInfo(int id, String name, int classId)
	{
		_id = id;
		_name = name;
		_classId = classId;
		_toSave = true;
	}

	public OlympiadNobleInfo(int id, String name, int classId, int points, int matches, int victories, int defeats, int draws, int classedMatches, int nonClassedMatches, boolean settled)
	{
		_id = id;
		_name = name;
		_classId = classId;

		_points = points;
		_matches = matches;
		_victories = victories;
		_defeats = defeats;
		_draws = draws;
		_classedMatches = classedMatches;
		_nonClassedMatches = nonClassedMatches;
		_settled = settled;
	}

	public void addWeeklyPoints(int weeklyPoints)
	{
		_points += weeklyPoints;

		// Also reset the competitions that the player could do this week
		_classedMatches = 0;
		_nonClassedMatches = 0;
	}

	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public int getClassId()
	{
		return _classId;
	}

	public int getPoints()
	{
		return _points;
	}

	public void setPoints(int points)
	{
		_points = points;
	}

	public void increaseMatches()
	{
		_matches++;
	}

	public void increaseVictories()
	{
		_victories++;
	}

	public void increaseDefeats()
	{
		_defeats++;
	}

	public void increaseDraws()
	{
		_draws++;
	}

	public void increaseClassedMatches()
	{
		_classedMatches++;
	}

	public void increaseNonClassedMatches()
	{
		_nonClassedMatches++;
	}

	public int getMatches()
	{
		return _matches;
	}

	public int getVictories()
	{
		return _victories;
	}

	public int getDefeats()
	{
		return _defeats;
	}

	public int getDraws()
	{
		return _draws;
	}

	public int getClassedMatches()
	{
		return _classedMatches;
	}

	public int getNonClassedMatches()
	{
		return _nonClassedMatches;
	}

	public int getMatchesThisWeek()
	{
		return _classedMatches + _nonClassedMatches;
	}

	public boolean isSettled()
	{
		return _settled;
	}

	public void setSettled(boolean settled)
	{
		_settled = settled;
	}

	public boolean isToSave()
	{
		return _toSave;
	}

	public void setToSave(boolean toSave)
	{
		_toSave = toSave;
	}
}
