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

package l2server.gameserver.model.zone.type;

import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.zone.ZoneType;

/**
 * A scripted zone...
 * Creation of such a zone should require somekind
 * of jython script reference which can handle onEnter() / onExit()
 *
 * @author durgus
 */
public class ScriptZone extends ZoneType {
	public ScriptZone(int id) {
		super(id);
	}

	@Override
	protected void onEnter(Creature character) {
		character.setInsideZone(Creature.ZONE_SCRIPT, true);
	}

	@Override
	protected void onExit(Creature character) {
		character.setInsideZone(Creature.ZONE_SCRIPT, false);
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
	}

	@Override
	public void onReviveInside(Creature character) {
	}
}
