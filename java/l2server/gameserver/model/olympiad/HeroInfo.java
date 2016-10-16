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

import lombok.Getter;
import lombok.Setter;

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

	@Getter private final int id;
	@Getter private final String name;
	@Getter private final int classId;

	@Getter @Setter private int victories;
	@Getter @Setter private int defeats;
	@Getter @Setter private int draws;

	@Getter private int count = 1;
	@Getter @Setter private boolean played = true;
	@Getter @Setter private String clanName = "";
	@Getter @Setter private int clanCrest = 0;
	@Getter @Setter private String allyName = "";
	@Getter @Setter private int allyCrest = 0;

	@Getter @Setter private String message;
	@Getter private List<DiaryEntry> diary = new ArrayList<>();
	@Getter private List<FightInfo> fights = new ArrayList<>();

	public HeroInfo(OlympiadNobleInfo nobleInfo)
	{
		id = nobleInfo.getId();
		name = nobleInfo.getName();
		classId = nobleInfo.getClassId();
		victories = nobleInfo.getVictories();
		defeats = nobleInfo.getDefeats();
		draws = nobleInfo.getDraws();
	}

	public HeroInfo(int id, String name, int classId, int count, boolean played)
	{
		this.id = id;
		this.name = name;
		this.classId = classId;
		this.count = count;
		this.played = played;
	}











	public void increaseCount()
	{
		count++;
	}













	public void addDiaryEntry(DiaryEntry entry)
	{
		diary.add(entry);
	}


	public void addFight(FightInfo fight)
	{
		fights.add(fight);
	}

}
