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

package l2server.gameserver.model.base;

import l2server.Config;
import l2server.log.Log;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.logging.Level;

/**

 *
 */
public class Experience
{
	private static long LEVEL[];

	static
	{
		reload();
	}

	public static void reload()
	{
		Map<Integer, Long> levels = new HashMap<>();
		int maxLevel = 0;
		LineNumberReader lnr = null;
		try
		{
			File data = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/stats/experience.dat");
			if (!data.exists())
			{
				data = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "stats/experience.dat");
			}
			lnr = new LineNumberReader(new BufferedReader(new FileReader(data)));

			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().length() == 0 || line.startsWith("#"))
				{
					continue;
				}

				if (line.indexOf("#") > 0)
				{
					line = line.substring(0, line.indexOf("#"));
				}

				StringTokenizer st = new StringTokenizer(line, ",");
				int level = Integer.parseInt(st.nextToken().trim());
				long exp = Long.parseLong(st.nextToken().trim());
				levels.put(level, exp);
				if (level > maxLevel)
				{
					maxLevel = level;
				}
			}
		}
		catch (FileNotFoundException e)
		{
			Log.warning("stats/experience.dat is missing in data folder");
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Error while loading Experience table " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				lnr.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		if (levels.size() < maxLevel)
		{
			Log.warning("Experience table: some level entry is missing!");
		}

		LEVEL = new long[levels.size() + 1];
		LEVEL[0] = -1; // Unreachable
		for (Entry<Integer, Long> level : levels.entrySet())
		{
			LEVEL[level.getKey()] = level.getValue();
		}
	}

	public static long getAbsoluteExp(int lvl)
	{
		if (lvl < LEVEL.length)
		{
			return LEVEL[lvl];
		}

		return getAbsoluteExp(lvl - 1) * 3;
	}

	public static long getLevelExp(int lvl)
	{
		return getAbsoluteExp(lvl + 1) - getAbsoluteExp(lvl);
	}

	public static double getExpPercent(int lvl, long exp)
	{
		return (exp - getAbsoluteExp(lvl)) / (double) getLevelExp(lvl);
	}
}
