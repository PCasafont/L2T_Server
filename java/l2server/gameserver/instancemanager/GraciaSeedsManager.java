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

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.quest.Quest;
import l2server.log.Log;

import java.util.Calendar;

public class GraciaSeedsManager
{

	public static String qn = "EnergySeeds";

	private static final byte SOITYPE = 2;
	private static final byte SOATYPE = 3;

	// Seed of Destruction
	private static final byte SODTYPE = 1;
	private int SoDTiatKilled = 0;
	private int SoDState = 1;
	private Calendar SoDLastStateChangeDate;

	private GraciaSeedsManager()
	{
		Log.info(getClass().getSimpleName() + ": Initializing");
		this.SoDLastStateChangeDate = Calendar.getInstance();
		loadData();
		handleSodStages();
	}

	public void saveData(byte seedType)
	{
		switch (seedType)
		{
			case SODTYPE:
				// Seed of Destruction
				GlobalVariablesManager.getInstance().storeVariable("SoDState", String.valueOf(this.SoDState));
				GlobalVariablesManager.getInstance().storeVariable("SoDTiatKilled", String.valueOf(this.SoDTiatKilled));
				GlobalVariablesManager.getInstance()
						.storeVariable("SoDLSCDate", String.valueOf(this.SoDLastStateChangeDate.getTimeInMillis()));
				break;
			case SOITYPE:
				// Seed of Infinity
				break;
			case SOATYPE:
				// Seed of Annihilation
				break;
			default:
				Log.warning("GraciaSeedManager: Unknown SeedType in SaveData: " + seedType);
				break;
		}
	}

	public void loadData()
	{
		// Seed of Destruction variables
		if (GlobalVariablesManager.getInstance().isVariableStored("SoDState"))
		{
			this.SoDState = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("SoDState"));
			this.SoDTiatKilled = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("SoDTiatKilled"));
			this.SoDLastStateChangeDate.setTimeInMillis(
					Long.parseLong(GlobalVariablesManager.getInstance().getStoredVariable("SoDLSCDate")));
		}
		else
		{
			// save Initial values
			saveData(SODTYPE);
		}
	}

	private void handleSodStages()
	{
		switch (this.SoDState)
		{
			case 1:
				// do nothing, players should kill Tiat a few times
				break;
			case 2:
				// Conquest Complete state, if too much time is passed than change to defense state
				long timePast = System.currentTimeMillis() - this.SoDLastStateChangeDate.getTimeInMillis();
				if (timePast >= Config.SOD_STAGE_2_LENGTH)
				// change to Attack state because Defend statet is not implemented
				{
					setSoDState(1, true);
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleEffect(() ->
					{
						setSoDState(1, true);
						Quest esQuest = QuestManager.getInstance().getQuest(qn);
						if (esQuest == null)
						{
							Log.warning("GraciaSeedManager: missing EnergySeeds Quest!");
						}
						else
						{
							esQuest.notifyEvent("StopSoDAi", null, null);
						}
					}, Config.SOD_STAGE_2_LENGTH - timePast);
				}
				break;
			case 3:
				// not implemented
				setSoDState(1, true);
				break;
			default:
				Log.warning("GraciaSeedManager: Unknown Seed of Destruction state(" + this.SoDState + ")! ");
		}
	}

	public void increaseSoDTiatKilled()
	{
		if (this.SoDState == 1)
		{
			this.SoDTiatKilled++;
			if (this.SoDTiatKilled >= Config.SOD_TIAT_KILL_COUNT)
			{
				setSoDState(2, false);
			}
			saveData(SODTYPE);
			Quest esQuest = QuestManager.getInstance().getQuest(qn);
			if (esQuest == null)
			{
				Log.warning("GraciaSeedManager: missing EnergySeeds Quest!");
			}
			else
			{
				esQuest.notifyEvent("StartSoDAi", null, null);
			}
		}
	}

	public int getSoDTiatKilled()
	{
		return this.SoDTiatKilled;
	}

	public void setSoDState(int value, boolean doSave)
	{
		Log.info("GraciaSeedManager: New Seed of Destruction state -> " + value + ".");
		this.SoDLastStateChangeDate.setTimeInMillis(System.currentTimeMillis());
		this.SoDState = value;
		// reset number of Tiat kills
		if (this.SoDState == 1)
		{
			this.SoDTiatKilled = 0;
		}

		handleSodStages();

		if (doSave)
		{
			saveData(SODTYPE);
		}
	}

	public long getSoDTimeForNextStateChange()
	{
		switch (this.SoDState)
		{
			case 1:
				return -1;
			case 2:
				return this.SoDLastStateChangeDate.getTimeInMillis() + Config.SOD_STAGE_2_LENGTH -
						System.currentTimeMillis();
			case 3:
				// not implemented yet
				return -1;
			default:
				// this should not happen!
				return -1;
		}
	}

	public Calendar getSoDLastStateChangeDate()
	{
		return this.SoDLastStateChangeDate;
	}

	public int getSoDState()
	{
		return this.SoDState;
	}

	public static GraciaSeedsManager getInstance()
	{
		return SingletonHolder.instance;
	}

	private static class SingletonHolder
	{
		protected static final GraciaSeedsManager instance = new GraciaSeedsManager();
	}
}
