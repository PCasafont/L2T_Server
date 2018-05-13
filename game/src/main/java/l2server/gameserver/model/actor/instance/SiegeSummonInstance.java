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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.model.InstanceType;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.CreatureZone;
import l2server.gameserver.templates.chars.NpcTemplate;

public class SiegeSummonInstance extends SummonInstance {
	public SiegeSummonInstance(int objectId, NpcTemplate template, Player owner, Skill skill) {
		super(objectId, template, owner, skill);
		setInstanceType(InstanceType.L2SiegeSummonInstance);
	}
	
	@Override
	public void onSpawn() {
		super.onSpawn();
		if (!getOwner().isGM() && !isInsideZone(CreatureZone.ZONE_SIEGE)) {
			unSummon(getOwner());
			getOwner().sendMessage("Summon was unsummoned because it exited siege zone");
		}
	}
}
