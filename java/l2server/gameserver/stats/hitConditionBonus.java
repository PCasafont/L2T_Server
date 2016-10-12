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
import l2server.gameserver.TimeController;
import l2server.gameserver.model.actor.L2Character;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.logging.Level;

/**
 * @author Nik
 */
public class hitConditionBonus
{

	private static int frontBonus = 0;
	private static int sideBonus = 0;
	private static int backBonus = 0;
	private static int highBonus = 0;
	private static int lowBonus = 0;
	private static int darkBonus = 0;

	//private static int rainBonus = 0;

	protected static double getConditionBonus(L2Character attacker, L2Character target)
	{
		double mod = 100;
		// Get high or low bonus
		if (attacker.getZ() - target.getZ() > 50)
		{
			mod += hitConditionBonus.highBonus;
		}
		else if (attacker.getZ() - target.getZ() < -50)
		{
			mod += hitConditionBonus.lowBonus;
		}

		// Get weather bonus
		if (TimeController.getInstance().isNowNight())
		{
			mod += hitConditionBonus.darkBonus;
		}
		//else if () No rain support yet.
		//chance += hitConditionBonus.rainBonus;

		// Get side bonus
		if (attacker.isBehindTarget())
		{
			mod += hitConditionBonus.backBonus;
		}
		else if (attacker.isInFrontOfTarget())
		{
			mod += hitConditionBonus.frontBonus;
		}
		else
		{
			mod += hitConditionBonus.sideBonus;
		}

		// If (mod / 10) is less than 0, return 0, because we cant lower more than 100%.
		return Math.max(mod / 100, 0);
	}

	static
	{
		final File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "stats/hitConditionBonus.xml");

		if (file.exists())
		{
			XmlDocument doc = new XmlDocument(file);
			String name;
			for (XmlNode list : doc.getChildren())
			{
				if (list.getName().equalsIgnoreCase("hitConditionBonus") || list.getName().equalsIgnoreCase("list"))
				{
					for (XmlNode cond : list.getChildren())
					{
						int bonus = 0;
						name = cond.getName();
						try
						{
							if (cond.hasAttributes())
							{
								bonus = cond.getInt("val");
							}
						}
						catch (Exception e)
						{
							Log.log(Level.WARNING, "[hitConditionBonus] Could not parse condition: " + e.getMessage(),
									e);
						}
						finally
						{
							if ("front".equals(name))
							{
								frontBonus = bonus;
							}
							else if ("side".equals(name))
							{
								sideBonus = bonus;
							}
							else if ("back".equals(name))
							{
								backBonus = bonus;
							}
							else if ("high".equals(name))
							{
								highBonus = bonus;
							}
							else if ("low".equals(name))
							{
								lowBonus = bonus;
							}
							else if ("dark".equals(name))
							{
								darkBonus = bonus;
							}
							//else if ("rain".equals(name))
							//rainBonus = bonus;
						}
					}
				}
			}
		}
		else
		{
			throw new Error("[hitConditionBonus] File not found: " + file.getName());
		}
	}
}
