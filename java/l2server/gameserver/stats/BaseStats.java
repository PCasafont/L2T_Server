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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import l2server.Config;
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.util.Util;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

/**
 * 
 * @author DS
 *
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
	
	private BaseStats(BaseStat s)
	{
		_stat = s;
	}
	
	public final double calcBonus(L2Character actor)
	{
		if (actor != null)
			return _stat.calcBonus(actor);
		
		return 1;
	}
	
	public static final BaseStats valueOfXml(String name)
	{
		name = name.intern();
		for (BaseStats s : values())
		{
			if (s.getValue().equalsIgnoreCase(name))
				return s;
		}
		
		throw new NoSuchElementException("Unknown name '" + name + "' for enum BaseStats");
	}
	
	private interface BaseStat
	{
		public double calcBonus(L2Character actor);
	}
	
	private static final class STR implements BaseStat
	{
		public final double calcBonus(L2Character actor)
		{
			return STRbonus[actor.getSTR()];
		}
	}
	
	private static final class INT implements BaseStat
	{
		public final double calcBonus(L2Character actor)
		{
			return INTbonus[actor.getINT()];
		}
	}
	
	private static final class DEX implements BaseStat
	{
		public final double calcBonus(L2Character actor)
		{
			return DEXbonus[actor.getDEX()];
		}
	}
	
	private static final class WIT implements BaseStat
	{
		public final double calcBonus(L2Character actor)
		{
			return WITbonus[actor.getWIT()];
		}
	}
	
	private static final class CON implements BaseStat
	{
		public final double calcBonus(L2Character actor)
		{
			return CONbonus[actor.getCON()];
		}
	}
	
	private static final class MEN implements BaseStat
	{
		public final double calcBonus(L2Character actor)
		{
			return MENbonus[actor.getMEN()];
		}
	}
	
	private static final class LUC implements BaseStat
	{
		public final double calcBonus(L2Character actor)
		{
			return LUCbonus[actor.getLUC()];
		}
	}
	
	private static final class CHA implements BaseStat
	{
		public final double calcBonus(L2Character actor)
		{
			return CHAbonus[actor.getCHA()];
		}
	}
	
	static
	{
		if (Config.IS_CLASSIC)
		{
			for (int i = 0; i < MAX_STAT_VALUE; i++)
			{
				STRbonus[i] = Math.pow(1.036, i - 34.845);
				INTbonus[i] = Math.pow(1.02, i - 31.375);
				CONbonus[i] = Math.pow(1.03, i - 27.632);
				MENbonus[i] = Math.pow(1.01, i + 0.06);
				DEXbonus[i] = Math.pow(1.009, i - 19.36);
				WITbonus[i] = Math.pow(1.05, i - 20);
				LUCbonus[i] = 1.0;
				CHAbonus[i] = 1.0;
			}
		}
		else
		{
			for (int i = 0; i < MAX_STAT_VALUE; i++)
			{
				STRbonus[i] = Math.pow(1.013, i * 1.036 - 68);
				INTbonus[i] = Math.pow(1.01, i * 1.005 - 55.4);
				CONbonus[i] = Math.pow(1.012, i * 1.01 - 37.3);
				MENbonus[i] = Math.pow(1.004, i);
				DEXbonus[i] = Math.pow(1.007, i * 0.62 - 12.2);
				WITbonus[i] = Math.pow(1.0096, i * 1.193 - 80.9) * 1.28;
				LUCbonus[i] = Math.pow(1.05, i - 20);
				CHAbonus[i] = Math.pow(1.002, i - 40);
			}
		}

		try
		{
			Map<Integer, float[]> hpz = new HashMap<Integer, float[]>();
			Map<Integer, float[]> mpz = new HashMap<Integer, float[]>();
			Map<Integer, float[]> cpz = new HashMap<Integer, float[]>();
			File dir = new File("C:\\Users\\Pere\\Desktop\\class_data");
			for (File file : dir.listFiles())
			{
				if (!file.getName().contains("xml") || !file.exists())
					continue;
				
				XmlDocument doc = new XmlDocument(file);
				for (XmlNode node : doc.getFirstChild().getChildren())
				{
					if (!node.getName().equals("class_data"))
						continue;
	
					for (XmlNode dataNode : node.getChildren())
					{
						if (!dataNode.getName().equals("hp_mp_cp_data"))
							continue;
	
						float[] hp = new float[99];
						float[] cp = new float[99];
						float[] mp = new float[99];
						for (XmlNode hpcpmpNode : dataNode.getChildren())
						{
							if (!hpcpmpNode.getName().equals("hp_mp_cp"))
								continue;
	
							hp[hpcpmpNode.getInt("lvl") - 1] = hpcpmpNode.getFloat("hp");
							mp[hpcpmpNode.getInt("lvl") - 1] = hpcpmpNode.getFloat("mp");
							cp[hpcpmpNode.getInt("lvl") - 1] = hpcpmpNode.getFloat("cp");
						}
	
						hpz.put(node.getInt("class_id"), hp);
						mpz.put(node.getInt("class_id"), mp);
						cpz.put(node.getInt("class_id"), cp);
					}
				}
			}
			
			List<List<Integer>> equivalencies = new ArrayList<List<Integer>>();
			for (int classId : hpz.keySet())
			{
				boolean found = false;
				for (List<Integer> equivalents : equivalencies)
				{
					int eqClass = equivalents.get(0);
					boolean equals = true;
					for (int lvl = 0; lvl < 99; lvl++)
					{
						if (hpz.get(classId)[lvl] != hpz.get(eqClass)[lvl]
								|| mpz.get(classId)[lvl] != mpz.get(eqClass)[lvl]
								|| cpz.get(classId)[lvl] != cpz.get(eqClass)[lvl])
						{
							equals = false;
							break;
						}
					}
					
					if (equals)
					{
						found = true;
						equivalents.add(classId);
						break;
					}
				}
				
				if (!found)
				{
					List<Integer> equivalents = new ArrayList<Integer>();
					equivalents.add(classId);
					equivalencies.add(equivalents);
				}
			}
			
			String xmlContent = "<list>\r\n";
			for (List<Integer> equivalents : equivalencies)
			{
				int classId = equivalents.get(0);
				String classContent = "\t<class id=\"";
				for (int eqClass : equivalents)
					classContent += eqClass + ",";
				
				classContent = classContent.substring(0, classContent.length() - 1);
				classContent += "\"> <!-- ";
				for (int eqClass : equivalents)
					classContent += PlayerClassTable.getInstance().getClassById(eqClass).getName() + ", ";
				
				classContent = classContent.substring(0, classContent.length() - 2) + " -->\r\n";
				for (int lvl = 1; lvl <= 99; lvl++)
					classContent += "\t\t<statData level=\"" + lvl + "\" hp=\"" + hpz.get(classId)[lvl - 1] + "\" mp=\"" + mpz.get(classId)[lvl - 1] + "\" cp=\"" + cpz.get(classId)[lvl - 1] + "\" />\r\n";
				for (int lvl = 100; lvl <= 120; lvl++)
					classContent += "\t\t<statData level=\"" + lvl
							+ "\" hp=\"" + String.format("%.1f", (hpz.get(classId)[98] + (hpz.get(classId)[98] - hpz.get(classId)[97]) * (lvl - 99)))
							+ "\" mp=\"" + String.format("%.2f", (mpz.get(classId)[98] + (mpz.get(classId)[98] - mpz.get(classId)[97]) * (lvl - 99)))
							+ "\" cp=\"" + String.format("%.2f", (cpz.get(classId)[98] + (cpz.get(classId)[98] - cpz.get(classId)[97]) * (lvl - 99))) + "\" />\r\n";
				classContent += "\t</class>\r\n";
				xmlContent += classContent;
			}
			xmlContent += "</list>\r\n";
			Util.writeFile("C:\\Users\\Pere\\Desktop\\ztatz.xml", xmlContent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}