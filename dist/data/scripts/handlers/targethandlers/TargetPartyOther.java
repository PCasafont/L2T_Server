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
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.SkillTargetType;

/**
 * @author nBd
 */
public class TargetPartyOther implements ISkillTargetTypeHandler {
	/**
	 */
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		if (target != null && target != activeChar && activeChar.getParty() != null && target.getParty() != null &&
				activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID()) {
			if (!target.isDead()) {
				if (target instanceof Player) {
					Player player = (Player) target;
					switch (skill.getId()) {
						// FORCE BUFFS may cancel here but there should be a proper condition
						case 426:
							if (!player.isMageClass()) {
								return new Creature[]{target};
							} else {
								return null;
							}
						case 427:
							if (player.isMageClass()) {
								return new Creature[]{target};
							} else {
								return null;
							}
					}
				}
				return new Creature[]{target};
			} else {
				return null;
			}
		} else {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return null;
		}
	}

	/**
	 */
	@Override
	public Enum<SkillTargetType> getTargetType() {
		// TODO Auto-generated method stub
		return SkillTargetType.TARGET_PARTY_OTHER;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetPartyOther());
	}
}
