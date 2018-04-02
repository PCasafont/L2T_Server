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

package handlers.targethandlers;

import l2server.gameserver.handler.ISkillTargetTypeHandler;
import l2server.gameserver.handler.SkillTargetTypeHandler;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.skills.SkillTargetType;

import java.util.ArrayList;

/**
 * Used by all herb skills.
 *
 * @author ZaKaX.
 */
public class HerbTarget implements ISkillTargetTypeHandler {
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		Player aPlayer = null;
		Summon aSummon = null;
		
		final ArrayList<Creature> aResult = new ArrayList<Creature>();
		
		if (activeChar instanceof Player) {
			aPlayer = (Player) activeChar;
			aSummon = aPlayer.getPet();
		} else if (activeChar instanceof Summon) {
			aSummon = (Summon) activeChar;
		} else if (activeChar instanceof PetInstance) {
			aSummon = (PetInstance) activeChar;
		}
		
		// If it's a player that picked up the herb...
		if (aPlayer != null) {
			// Affect the player.
			aResult.add(aPlayer);
			
			// As well as his summon, if it's a summon and NOT a pet.
			if (aSummon != null && !(aSummon instanceof PetInstance)) {
				aResult.add(aSummon);
			}
		} else {
			// Otherwise, a summon picked it up. Only the summon is affected in this case.
			aResult.add(aSummon);
		}
		
		return aResult.toArray(new Creature[aResult.size()]);
	}
	
	@Override
	public Enum<SkillTargetType> getTargetType() {
		return SkillTargetType.HERB_TARGET;
	}
	
	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new HerbTarget());
	}
}
