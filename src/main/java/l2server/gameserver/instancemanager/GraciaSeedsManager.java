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
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.model.quest.Quest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import l2server.util.loader.annotations.Load;

import java.util.Calendar;

public class GraciaSeedsManager {
	private static Logger log = LoggerFactory.getLogger(GraciaSeedsManager.class.getName());



	public static String qn = "EnergySeeds";

	private static final byte SOITYPE = 2;
	private static final byte SOATYPE = 3;

	// Seed of Destruction
	private static final byte SODTYPE = 1;
	private int SoDTiatKilled = 0;
	private int SoDState = 1;
	private Calendar SoDLastStateChangeDate;

	private GraciaSeedsManager() {
	}
	
	@Load
	public void initialize() {
		log.info(getClass().getSimpleName() + ": Initializing");
		SoDLastStateChangeDate = Calendar.getInstance();
		loadData();
		handleSodStages();
	}
	
	public void saveData(byte seedType) {
		switch (seedType) {
			case SODTYPE:
				// Seed of Destruction
				GlobalVariablesManager.getInstance().storeVariable("SoDState", String.valueOf(SoDState));
				GlobalVariablesManager.getInstance().storeVariable("SoDTiatKilled", String.valueOf(SoDTiatKilled));
				GlobalVariablesManager.getInstance().storeVariable("SoDLSCDate", String.valueOf(SoDLastStateChangeDate.getTimeInMillis()));
				break;
			case SOITYPE:
				// Seed of Infinity
				break;
			case SOATYPE:
				// Seed of Annihilation
				break;
			default:
				log.warn("GraciaSeedManager: Unknown SeedType in SaveData: " + seedType);
				break;
		}
	}

	public void loadData() {
		// Seed of Destruction variables
		if (GlobalVariablesManager.getInstance().isVariableStored("SoDState")) {
			SoDState = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("SoDState"));
			SoDTiatKilled = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("SoDTiatKilled"));
			SoDLastStateChangeDate.setTimeInMillis(Long.parseLong(GlobalVariablesManager.getInstance().getStoredVariable("SoDLSCDate")));
		} else {
			// save Initial values
			saveData(SODTYPE);
		}
	}

	private void handleSodStages() {
		switch (SoDState) {
			case 1:
				// do nothing, players should kill Tiat a few times
				break;
			case 2:
				// Conquest Complete state, if too much time is passed than change to defense state
				long timePast = System.currentTimeMillis() - SoDLastStateChangeDate.getTimeInMillis();
				if (timePast >= Config.SOD_STAGE_2_LENGTH)
				// change to Attack state because Defend statet is not implemented
				{
					setSoDState(1, true);
				} else {
					ThreadPoolManager.getInstance().scheduleEffect(() -> {
						setSoDState(1, true);
						Quest esQuest = QuestManager.getInstance().getQuest(qn);
						if (esQuest == null) {
							log.warn("GraciaSeedManager: missing EnergySeeds Quest!");
						} else {
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
				log.warn("GraciaSeedManager: Unknown Seed of Destruction state(" + SoDState + ")! ");
		}
	}

	public void increaseSoDTiatKilled() {
		if (SoDState == 1) {
			SoDTiatKilled++;
			if (SoDTiatKilled >= Config.SOD_TIAT_KILL_COUNT) {
				setSoDState(2, false);
			}
			saveData(SODTYPE);
			Quest esQuest = QuestManager.getInstance().getQuest(qn);
			if (esQuest == null) {
				log.warn("GraciaSeedManager: missing EnergySeeds Quest!");
			} else {
				esQuest.notifyEvent("StartSoDAi", null, null);
			}
		}
	}

	public int getSoDTiatKilled() {
		return SoDTiatKilled;
	}

	public void setSoDState(int value, boolean doSave) {
		log.info("GraciaSeedManager: New Seed of Destruction state -> " + value + ".");
		SoDLastStateChangeDate.setTimeInMillis(System.currentTimeMillis());
		SoDState = value;
		// reset number of Tiat kills
		if (SoDState == 1) {
			SoDTiatKilled = 0;
		}

		handleSodStages();

		if (doSave) {
			saveData(SODTYPE);
		}
	}

	public long getSoDTimeForNextStateChange() {
		switch (SoDState) {
			case 1:
				return -1;
			case 2:
				return SoDLastStateChangeDate.getTimeInMillis() + Config.SOD_STAGE_2_LENGTH - System.currentTimeMillis();
			case 3:
				// not implemented yet
				return -1;
			default:
				// this should not happen!
				return -1;
		}
	}

	public Calendar getSoDLastStateChangeDate() {
		return SoDLastStateChangeDate;
	}

	public int getSoDState() {
		return SoDState;
	}

	public static GraciaSeedsManager getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {
		protected static final GraciaSeedsManager instance = new GraciaSeedsManager();
	}
}
