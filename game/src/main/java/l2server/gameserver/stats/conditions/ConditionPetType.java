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

package l2server.gameserver.stats.conditions;

import l2server.gameserver.datatables.PetDataTable;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.item.ItemTemplate;

/**
 * @author JIV
 */
public class ConditionPetType extends Condition {
	private int petType;

	public ConditionPetType(int petType) {
		this.petType = petType;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.stats.conditions.Condition#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	boolean testImpl(Env env) {
		if (!(env.player instanceof PetInstance)) {
			return false;
		}

		/*if ((petType & ItemTemplate.ANY_PET) == ItemTemplate.ANY_PET)
			return true;*/

		int npcid = ((Summon) env.player).getNpcId();

		if (PetDataTable.isHatchling(npcid) && (petType & ItemTemplate.HATCHLING) == ItemTemplate.HATCHLING) {
			return true;
		} else if (PetDataTable.isWolf(npcid) && (petType & ItemTemplate.WOLF) == ItemTemplate.WOLF) {
			return true;
		} else if (PetDataTable.isEvolvedWolf(npcid) && (petType & ItemTemplate.GROWN_WOLF) == ItemTemplate.GROWN_WOLF) {
			return true;
		} else if (PetDataTable.isStrider(npcid) && (petType & ItemTemplate.STRIDER) == ItemTemplate.STRIDER) {
			return true;
		} else if (PetDataTable.isBaby(npcid) && (petType & ItemTemplate.BABY) == ItemTemplate.BABY) {
			return true;
		} else if (PetDataTable.isImprovedBaby(npcid) && (petType & ItemTemplate.IMPROVED_BABY) == ItemTemplate.IMPROVED_BABY) {
			return true;
		}

		return false;
	}
}
