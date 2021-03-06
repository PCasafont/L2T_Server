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

package l2server.gameserver.stats.skills;

import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.base.Race;
import l2server.gameserver.templates.StatsSet;

public class SkillAppearance extends Skill {
	private final int faceId;
	private final int hairColorId;
	private final int hairStyleId;

	public SkillAppearance(StatsSet set) {
		super(set);

		faceId = set.getInteger("faceId", -1);
		hairColorId = set.getInteger("hairColorId", -1);
		hairStyleId = set.getInteger("hairStyleId", -1);
	}

	@Override
	public void useSkill(Creature caster, WorldObject[] targets) {
		for (WorldObject target : targets) {
			if (target instanceof Player) {
				Player targetPlayer = (Player) target;

				int maxHairStyle = targetPlayer.getAppearance().getSex() ? 5 : 7;
				int maxHairColor = targetPlayer.getRace() == Race.Kamael || targetPlayer.getRace() == Race.Ertheia ? 2 : 3;
				int maxFace = 3;

				int faceId = this.faceId;
				int hairStyleId = this.hairStyleId;
				int hairColorId = this.hairColorId;

				if (hairStyleId > maxHairStyle) {
					hairStyleId = -1;
				}

				if (hairColorId > maxHairColor) {
					hairColorId = -1;
				}

				if (faceId > maxFace) {
					faceId = -1;
				}

				if (faceId >= 0) {
					targetPlayer.getAppearance().setFace(faceId);
				}
				if (hairColorId >= 0) {
					targetPlayer.getAppearance().setHairColor(hairColorId);
				}
				if (hairStyleId >= 0) {
					targetPlayer.getAppearance().setHairStyle(hairStyleId);
				}

				targetPlayer.broadcastUserInfo();
			}
		}
	}
}
