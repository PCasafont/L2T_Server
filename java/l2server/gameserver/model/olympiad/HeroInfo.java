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

	private final int id;
	private final String name;
	private final int classId;

	private int victories;
	private int defeats;
	private int draws;

	private int count = 1;
	private boolean played = true;
	private String clanName = "";
	private int clanCrest = 0;
	private String allyName = "";
	private int allyCrest = 0;

	private String message;
	private List<DiaryEntry> diary = new ArrayList<>();
	private List<FightInfo> fights = new ArrayList<>();

	public HeroInfo(OlympiadNobleInfo nobleInfo)
	{
		this.id = nobleInfo.getId();
		this.name = nobleInfo.getName();
		this.classId = nobleInfo.getClassId();
		this.victories = nobleInfo.getVictories();
		this.defeats = nobleInfo.getDefeats();
		this.draws = nobleInfo.getDraws();
	}

	public HeroInfo(int id, String name, int classId, int count, boolean played)
	{
		this.id = id;
		this.name = name;
		this.classId = classId;
		this.count = count;
		this.played = played;
	}

	public int getId()
	{
		return this.id;
	}

	public String getName()
	{
		return this.name;
	}

	public int getClassId()
	{
		return this.classId;
	}

	public int getVictories()
	{
		return this.victories;
	}

	public void setVictories(int victories)
	{
		this.victories = victories;
	}

	public int getDefeats()
	{
		return this.defeats;
	}

	public void setDefeats(int defeats)
	{
		this.defeats = defeats;
	}

	public int getDraws()
	{
		return this.draws;
	}

	public void setDraws(int draws)
	{
		this.draws = draws;
	}

	public int getCount()
	{
		return this.count;
	}

	public void increaseCount()
	{
		this.count++;
	}

	public boolean getPlayed()
	{
		return this.played;
	}

	public void setPlayed(boolean played)
	{
		this.played = played;
	}

	public String getClanName()
	{
		return this.clanName;
	}

	public void setClanName(String clanName)
	{
		this.clanName = clanName;
	}

	public int getClanCrest()
	{
		return this.clanCrest;
	}

	public void setClanCrest(int clanCrest)
	{
		this.clanCrest = clanCrest;
	}

	public String getAllyName()
	{
		return this.allyName;
	}

	public void setAllyName(String allyName)
	{
		this.allyName = allyName;
	}

	public int getAllyCrest()
	{
		return this.allyCrest;
	}

	public void setAllyCrest(int allyCrest)
	{
		this.allyCrest = allyCrest;
	}

	public String getMessage()
	{
		return this.message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public void addDiaryEntry(DiaryEntry entry)
	{
		this.diary.add(entry);
	}

	public List<DiaryEntry> getDiary()
	{
		return this.diary;
	}

	public void addFight(FightInfo fight)
	{
		this.fights.add(fight);
	}

	public List<FightInfo> getFights()
	{
		return this.fights;
	}
}
