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

package l2server.gameserver.stats;

import l2server.Config;
import l2server.gameserver.model.actor.L2Character;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.NoSuchElementException;

/**
 * @author DS
 */
public enum BaseStats
{
	STR(new STR()),
	INT(new INT()),
	DEX(new DEX()),
	WIT(new WIT()),
	CON(new CON()),
	MEN(new MEN()),
	LUC(new LUC()),
	CHA(new CHA());

	public static final int MAX_STAT_VALUE = 300;

	private static final double[] STRbonus = new double[MAX_STAT_VALUE];
	private static final double[] INTbonus = new double[MAX_STAT_VALUE];
	private static final double[] DEXbonus = new double[MAX_STAT_VALUE];
	private static final double[] WITbonus = new double[MAX_STAT_VALUE];
	private static final double[] CONbonus = new double[MAX_STAT_VALUE];
	private static final double[] MENbonus = new double[MAX_STAT_VALUE];
	private static final double[] LUCbonus = new double[MAX_STAT_VALUE];
	private static final double[] CHAbonus = new double[MAX_STAT_VALUE];

	private final BaseStat _stat;

	public final String getValue()
	{
		return _stat.getClass().getSimpleName();
	}

	BaseStats(BaseStat s)
	{
		_stat = s;
	}

	public final double calcBonus(L2Character actor)
	{
		if (actor != null)
		{
			return _stat.calcBonus(actor);
		}

		return 1;
	}

	public static BaseStats valueOfXml(String name)
	{
		name = name.intern();
		for (BaseStats s : values())
		{
			if (s.getValue().equalsIgnoreCase(name))
			{
				return s;
			}
		}

		throw new NoSuchElementException("Unknown name '" + name + "' for enum BaseStats");
	}

	private interface BaseStat
	{
		double calcBonus(L2Character actor);
	}

	private static final class STR implements BaseStat
	{
		@Override
		public final double calcBonus(L2Character actor)
		{
			return STRbonus[actor.getSTR() - 1];
		}
	}

	private static final class INT implements BaseStat
	{
		@Override
		public final double calcBonus(L2Character actor)
		{
			return INTbonus[actor.getINT() - 1];
		}
	}

	private static final class DEX implements BaseStat
	{
		@Override
		public final double calcBonus(L2Character actor)
		{
			return DEXbonus[actor.getDEX() - 1];
		}
	}

	private static final class WIT implements BaseStat
	{
		@Override
		public final double calcBonus(L2Character actor)
		{
			return WITbonus[actor.getWIT() - 1];
		}
	}

	private static final class CON implements BaseStat
	{
		@Override
		public final double calcBonus(L2Character actor)
		{
			return CONbonus[actor.getCON() - 1];
		}
	}

	private static final class MEN implements BaseStat
	{
		@Override
		public final double calcBonus(L2Character actor)
		{
			return MENbonus[actor.getMEN() - 1];
		}
	}

	private static final class LUC implements BaseStat
	{
		@Override
		public final double calcBonus(L2Character actor)
		{
			return LUCbonus[actor.getLUC() - 1];
		}
	}

	private static final class CHA implements BaseStat
	{
		@Override
		public final double calcBonus(L2Character actor)
		{
			return CHAbonus[actor.getCHA() - 1];
		}
	}

	static
	{
		for (int i = 0; i < MAX_STAT_VALUE; i++)
		{
			STRbonus[i] = 1.0;
			INTbonus[i] = 1.0;
			CONbonus[i] = 1.0;
			MENbonus[i] = 1.0;
			DEXbonus[i] = 1.0;
			WITbonus[i] = 1.0;
			LUCbonus[i] = 1.0;
			CHAbonus[i] = 1.0;
		}

		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "stats/statBonus.xml");
		XmlDocument doc = new XmlDocument(file);
		for (XmlNode n : doc.getFirstChild().getChildren())
		{
			if (!n.getName().equalsIgnoreCase("stat"))
			{
				continue;
			}

			for (XmlNode bonusNode : n.getChildren())
			{
				if (!bonusNode.getName().equalsIgnoreCase("bonus"))
				{
					continue;
				}

				switch (n.getString("name"))
				{
					case "STR":
						STRbonus[bonusNode.getInt("id") - 1] = bonusNode.getDouble("val");
						break;
					case "INT":
						INTbonus[bonusNode.getInt("id") - 1] = bonusNode.getDouble("val");
						break;
					case "CON":
						CONbonus[bonusNode.getInt("id") - 1] = bonusNode.getDouble("val");
						break;
					case "MEN":
						MENbonus[bonusNode.getInt("id") - 1] = bonusNode.getDouble("val");
						break;
					case "DEX":
						DEXbonus[bonusNode.getInt("id") - 1] = bonusNode.getDouble("val");
						break;
					case "WIT":
						WITbonus[bonusNode.getInt("id") - 1] = bonusNode.getDouble("val");
						break;
					case "LUC":
						LUCbonus[bonusNode.getInt("id") - 1] = bonusNode.getDouble("val");
						break;
					case "CHA":
						CHAbonus[bonusNode.getInt("id") - 1] = bonusNode.getDouble("val");
						break;
				}
			}
		}
	}
}
