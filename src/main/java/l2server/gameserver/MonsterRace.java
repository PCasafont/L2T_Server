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

package l2server.gameserver;

import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;
import l2server.util.Rnd;

import java.lang.reflect.Constructor;
import java.util.logging.Level;

public class MonsterRace
{

	private L2Npc[] monsters;
	private Constructor<?> constructor;
	private int[][] speeds;
	private int[] first, second;

	private MonsterRace()
	{
		monsters = new L2Npc[8];
		speeds = new int[8][20];
		first = new int[2];
		second = new int[2];
	}

	public static MonsterRace getInstance()
	{
		return SingletonHolder.instance;
	}

	public void newRace()
	{
		int random = 0;

		for (int i = 0; i < 8; i++)
		{
			int id = 31003;
			random = Rnd.get(24);
			for (int j = i - 1; j >= 0; j--)
			{
				if (monsters[j].getTemplate().NpcId == id + random)
				{
					random = Rnd.get(24);
				}
			}

			try
			{
				L2NpcTemplate template = NpcTable.getInstance().getTemplate(id + random);
				constructor = Class.forName("l2server.gameserver.model.actor.instance." + template.Type + "Instance")
						.getConstructors()[0];
				int objectId = IdFactory.getInstance().getNextId();
				monsters[i] = (L2Npc) constructor.newInstance(objectId, template);
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, "", e);
			}
			//Logozo.info("Monster "+i+" is id: "+(id+random));
		}
		newSpeeds();
	}

	public void newSpeeds()
	{
		speeds = new int[8][20];
		int total = 0;
		first[1] = 0;
		second[1] = 0;
		for (int i = 0; i < 8; i++)
		{
			total = 0;
			for (int j = 0; j < 20; j++)
			{
				if (j == 19)
				{
					speeds[i][j] = 100;
				}
				else
				{
					speeds[i][j] = Rnd.get(60) + 65;
				}
				total += speeds[i][j];
			}
			if (total >= first[1])
			{
				second[0] = first[0];
				second[1] = first[1];
				first[0] = 8 - i;
				first[1] = total;
			}
			else if (total >= second[1])
			{
				second[0] = 8 - i;
				second[1] = total;
			}
		}
	}

	/**
	 * @return Returns the monsters.
	 */
	public L2Npc[] getMonsters()
	{
		return monsters;
	}

	/**
	 * @return Returns the speeds.
	 */
	public int[][] getSpeeds()
	{
		return speeds;
	}

	public int getFirstPlace()
	{
		return first[0];
	}

	public int getSecondPlace()
	{
		return second[0];
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final MonsterRace instance = new MonsterRace();
	}
}
