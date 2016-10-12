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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class HeroInfo
{
	public static class DiaryEntry
	{
		public long time;
		public String action;
	}

	public static class FightInfo
	{
		public String opponent;
		public String opponentClass;
		public String duration;
		public String startTime;
		public boolean classed;
		public String result;
	}

	private final int _id;
	private final String _name;
	private final int _classId;

	private int _victories;
	private int _defeats;
	private int _draws;

	private int _count = 1;
	private boolean _played = true;
	private String _clanName = "";
	private int _clanCrest = 0;
	private String _allyName = "";
	private int _allyCrest = 0;

	private String _message;
	private List<DiaryEntry> _diary = new ArrayList<>();
	private List<FightInfo> _fights = new ArrayList<>();

	public HeroInfo(OlympiadNobleInfo nobleInfo)
	{
		_id = nobleInfo.getId();
		_name = nobleInfo.getName();
		_classId = nobleInfo.getClassId();
		_victories = nobleInfo.getVictories();
		_defeats = nobleInfo.getDefeats();
		_draws = nobleInfo.getDraws();
	}

	public HeroInfo(int id, String name, int classId, int count, boolean played)
	{
		_id = id;
		_name = name;
		_classId = classId;
		_count = count;
		_played = played;
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

	public int getVictories()
	{
		return _victories;
	}

	public void setVictories(int victories)
	{
		_victories = victories;
	}

	public int getDefeats()
	{
		return _defeats;
	}

	public void setDefeats(int defeats)
	{
		_defeats = defeats;
	}

	public int getDraws()
	{
		return _draws;
	}

	public void setDraws(int draws)
	{
		_draws = draws;
	}

	public int getCount()
	{
		return _count;
	}

	public void increaseCount()
	{
		_count++;
	}

	public boolean getPlayed()
	{
		return _played;
	}

	public void setPlayed(boolean played)
	{
		_played = played;
	}

	public String getClanName()
	{
		return _clanName;
	}

	public void setClanName(String clanName)
	{
		_clanName = clanName;
	}

	public int getClanCrest()
	{
		return _clanCrest;
	}

	public void setClanCrest(int clanCrest)
	{
		_clanCrest = clanCrest;
	}

	public String getAllyName()
	{
		return _allyName;
	}

	public void setAllyName(String allyName)
	{
		_allyName = allyName;
	}

	public int getAllyCrest()
	{
		return _allyCrest;
	}

	public void setAllyCrest(int allyCrest)
	{
		_allyCrest = allyCrest;
	}

	public String getMessage()
	{
		return _message;
	}

	public void setMessage(String message)
	{
		_message = message;
	}

	public void addDiaryEntry(DiaryEntry entry)
	{
		_diary.add(entry);
	}

	public List<DiaryEntry> getDiary()
	{
		return _diary;
	}

	public void addFight(FightInfo fight)
	{
		_fights.add(fight);
	}

	public List<FightInfo> getFights()
	{
		return _fights;
	}
}
