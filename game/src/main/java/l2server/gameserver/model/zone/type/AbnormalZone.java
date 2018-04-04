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
import l2server.gameserver.stats.VisualEffect;

/**
 * AbnormalZone zones give entering players abnormal effects
 * Default effect is big head
 *
 * @author durgus
 */
public class AbnormalZone extends ZoneType {
	private int abnormal = VisualEffect.BIG_HEAD.getId();

	public AbnormalZone(int id) {
		super(id);
	}

	@Override
	public void setParameter(String name, String value) {
		switch (name) {
			case "AbnormalMask":
				abnormal = Integer.parseInt(value);
				break;
			case "SpecialMask":
				abnormal = Integer.parseInt(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	@Override
	protected void onEnter(Creature character) {
		character.startVisualEffect(abnormal);
	}

	@Override
	protected void onExit(Creature character) {
		character.stopVisualEffect(abnormal);
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
		onExit(character);
	}

	@Override
	public void onReviveInside(Creature character) {
		onEnter(character);
	}
}
