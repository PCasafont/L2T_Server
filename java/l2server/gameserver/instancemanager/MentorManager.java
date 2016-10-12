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

package l2server.gameserver.instancemanager;

import l2server.log.Log;

import java.util.HashMap;

/**
 * @author Erlandys
 */
public class MentorManager
{
	HashMap<Integer, Integer> coinsRewardForLevel;

	public MentorManager()
	{
		load();
	}

	public void load()
	{
		coinsRewardForLevel = new HashMap<>();
		coinsRewardForLevel.put(10, 1);
		coinsRewardForLevel.put(20, 25);
		coinsRewardForLevel.put(30, 30);
		coinsRewardForLevel.put(40, 63);
		coinsRewardForLevel.put(50, 68);
		coinsRewardForLevel.put(51, 16);
		coinsRewardForLevel.put(53, 9);
		coinsRewardForLevel.put(54, 11);
		coinsRewardForLevel.put(55, 13);
		coinsRewardForLevel.put(56, 16);
		coinsRewardForLevel.put(57, 19);
		coinsRewardForLevel.put(58, 23);
		coinsRewardForLevel.put(59, 29);
		coinsRewardForLevel.put(60, 37);
		coinsRewardForLevel.put(61, 51);
		coinsRewardForLevel.put(62, 20);
		coinsRewardForLevel.put(63, 24);
		coinsRewardForLevel.put(64, 24);
		coinsRewardForLevel.put(65, 36);
		coinsRewardForLevel.put(66, 44);
		coinsRewardForLevel.put(67, 55);
		coinsRewardForLevel.put(68, 67);
		coinsRewardForLevel.put(69, 84);
		coinsRewardForLevel.put(70, 107);
		coinsRewardForLevel.put(71, 120);
		coinsRewardForLevel.put(72, 92);
		coinsRewardForLevel.put(73, 114);
		coinsRewardForLevel.put(74, 139);
		coinsRewardForLevel.put(75, 172);
		coinsRewardForLevel.put(76, 213);
		coinsRewardForLevel.put(77, 629);
		coinsRewardForLevel.put(78, 322);
		coinsRewardForLevel.put(79, 413);
		coinsRewardForLevel.put(80, 491);
		coinsRewardForLevel.put(81, 663);
		coinsRewardForLevel.put(82, 746);
		coinsRewardForLevel.put(83, 850);
		coinsRewardForLevel.put(84, 987);
		coinsRewardForLevel.put(85, 1149);
		coinsRewardForLevel.put(86, 2015);
		Log.info("MentorManager: Successfully loaded - " + coinsRewardForLevel.size() +
				" reward to mentor for mentee level.");
	}

	public int getItemsCount(int level)
	{
		if (coinsRewardForLevel.containsKey(level))
		{
			return coinsRewardForLevel.get(level);
		}
		return 0;
	}

	public String getTitle()
	{
		return "Mentee Coin from Mentee Leveling";
	}

	public String getMessage(String playerName, String level)
	{
		return "Your mentee " + playerName + " has reacher level " + level +
				", so you are receiving some Mentee Coin. " +
				"After Mentee Coin has successfully been removed and placed into your inventory please be sure to " +
				"delete this letter. If your mailbox is full when any future letters are sent to you they cannot " +
				"be delivered and you will not receive these items.";
	}

	public static MentorManager getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final MentorManager _instance = new MentorManager();
	}
}
