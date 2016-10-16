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

package l2server.gameserver.templates.item;

import l2server.Config;
import l2server.gameserver.stats.SkillHolder;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.skills.L2SkillType;

import java.util.ArrayList;
import java.util.List;

/**
 * This class ...
 *
 * @version $Revision$ $Date$
 */
public class L2Henna
{
	private final int symbolId;
	private final int dye;
	private final String name;
	private final long price;
	private final int INT;
	private final int STR;
	private final int CON;
	private final int MEN;
	private final int DEX;
	private final int WIT;
	private final int LUC;
	private final int CHA;
	private final int elemId;
	private final int elemVal;

	//Temp dyes values
	private final long maxTime;
	private long expiryTime;
	private final boolean isFourthSlot;
	private List<SkillHolder> skills;

	public L2Henna(StatsSet set)
	{
		symbolId = set.getInteger("symbolId");
		dye = set.getInteger("dyeId");
		name = set.getString("name");
		long p = set.getLong("price", 0);
		if (Config.isServer(Config.TENKAI_ESTHUS))
		{
			p = (int) Math.sqrt(p);
		}
		price = p;
		STR = set.getInteger("STR", 0);
		CON = set.getInteger("CON", 0);
		DEX = set.getInteger("DEX", 0);
		INT = set.getInteger("INT", 0);
		WIT = set.getInteger("WIT", 0);
		MEN = set.getInteger("MEN", 0);
		LUC = set.getInteger("LUC", 0);
		CHA = set.getInteger("CHA", 0);
		elemId = set.getInteger("elemId", 0);
		elemVal = set.getInteger("elemVal", 0);
		maxTime = set.getLong("time", 0);
		isFourthSlot = set.getBool("fourthSlot", false);
		if (set.getString("skills", null) != null)
		{
			skills = new ArrayList<>();

			String[] skillsParse = set.getString("skills").split(";");
			for (String element : skillsParse)
			{
				String[] skInfo = element.split(",");

				SkillHolder sk = new SkillHolder(Integer.valueOf(skInfo[0]), Integer.valueOf(skInfo[1]));
				if (sk.getSkill().getSkillType() == L2SkillType.NOTDONE)
				{
					System.out.println(sk.getSkillId() + " is not done!");
				}
				skills.add(sk);
			}
		}
	}

	public int getSymbolId()
	{
		return symbolId;
	}

	/**
	 * @return
	 */
	public int getDyeId()
	{
		return dye;
	}

	/**
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return
	 */
	public long getPrice()
	{
		return price;
	}

	/**
	 * @return
	 */
	public int getAmountDyeRequire()
	{
		if (isFourthSlot())
		{
			return 1;
		}

		return 10;
	}

	/**
	 * @return
	 */
	public int getStatINT()
	{
		return INT;
	}

	/**
	 * @return
	 */
	public int getStatSTR()
	{
		return STR;
	}

	/**
	 * @return
	 */
	public int getStatCON()
	{
		return CON;
	}

	/**
	 * @return
	 */
	public int getStatMEM()
	{
		return MEN;
	}

	/**
	 * @return
	 */
	public int getStatDEX()
	{
		return DEX;
	}

	/**
	 * @return
	 */
	public int getStatWIT()
	{
		return WIT;
	}

	/**
	 * @return
	 */
	public int getStatLUC()
	{
		return LUC;
	}

	/**
	 * @return
	 */
	public int getStatCHA()
	{
		return CHA;
	}

	/**
	 * @return
	 */
	public int getStatElemId()
	{
		return elemId;
	}

	/**
	 * @return
	 */
	public int getStatElemVal()
	{
		return elemVal;
	}

	public long getMaxTime()
	{
		return maxTime;
	}

	public boolean isFourthSlot()
	{
		return isFourthSlot;
	}

	public List<SkillHolder> getSkills()
	{
		return skills;
	}

	public void setExpiryTime(long time)
	{
		expiryTime = time;
	}

	public long getExpiryTime()
	{
		return expiryTime;
	}
}
