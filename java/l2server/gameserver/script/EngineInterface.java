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

package l2server.gameserver.script;

import l2server.gameserver.Announcements;
import l2server.gameserver.RecipeController;
import l2server.gameserver.TimeController;
import l2server.gameserver.datatables.*;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.L2World;

/**
 * @author Luis Arias
 */
public interface EngineInterface
{
	//*  keep the references of Singletons to prevent garbage collection
	CharNameTable charNametable = CharNameTable.getInstance();

	IdFactory idFactory = IdFactory.getInstance();
	ItemTable itemTable = ItemTable.getInstance();

	SkillTable skillTable = SkillTable.getInstance();

	RecipeController recipeController = RecipeController.getInstance();

	SkillTreeTable skillTreeTable = SkillTreeTable.getInstance();
	CharTemplateTable charTemplates = CharTemplateTable.getInstance();
	ClanTable clanTable = ClanTable.getInstance();

	NpcTable npcTable = NpcTable.getInstance();

	TeleportLocationTable teleTable = TeleportLocationTable.getInstance();
	L2World world = L2World.getInstance();
	SpawnTable spawnTable = SpawnTable.getInstance();
	TimeController gameTimeController = TimeController.getInstance();
	Announcements announcements = Announcements.getInstance();
	MapRegionTable mapRegions = MapRegionTable.getInstance();

	//public ArrayList getAllPlayers();
	//public Player getPlayer(String characterName);
	void addQuestDrop(int npcID, int itemID, int min, int max, int chance, String questID, String[] states);

	void addEventDrop(int[] items, int[] count, double chance, DateRange range);

	void onPlayerLogin(String[] message, DateRange range);
}
