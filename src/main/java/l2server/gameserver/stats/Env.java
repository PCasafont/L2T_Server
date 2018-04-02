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

import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.CubicInstance;

/**
 * An Env object is just a class to pass parameters to a calculator such as Player, Item, Initial value.
 */

public final class Env {

	public Creature player;
	public CubicInstance cubic;
	public Creature target;
	public Item item;
	public Skill skill;
	public Abnormal effect;
	public double value;
	public double baseValue;
	public boolean skillMastery = false;
	public byte shld = 0;
	public double ssMul = Item.CHARGED_NONE;

	public Env() {

	}

	public Env(byte shd, double ss) {
		shld = shd;
		ssMul = ss;
	}
}
