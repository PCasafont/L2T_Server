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

package handlers.skillhandlers;

import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ConfirmDlg;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.SkillType;
import l2server.gameserver.util.Util;

import java.util.logging.Level;

/**
 * @authors BiTi, Sami
 */
public class SummonFriend implements ISkillHandler {
	//private static Logger log = Logger.getLogger(SummonFriend.class.getName());
	private static final SkillType[] SKILL_IDS = {SkillType.SUMMON_FRIEND};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		if (!(activeChar instanceof Player)) {
			return; // currently not implemented for others
		}
		Player activePlayer = (Player) activeChar;

		if (!Player.checkSummonerStatus(activePlayer)) {
			return;
		}

		try {
			for (Creature target : (Creature[]) targets) {
				if (activeChar == target) {
					continue;
				}

				if (target instanceof Player) {
					Player targetPlayer = (Player) target;

					if (!Player.checkSummonTargetStatus(targetPlayer, activePlayer)) {
						continue;
					}

					if (!Util.checkIfInRange(0, activeChar, target, false)) {
						if (!targetPlayer.teleportRequest(activePlayer, skill)) {
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ALREADY_SUMMONED);
							sm.addString(target.getName());
							activePlayer.sendPacket(sm);
							continue;
						}
						if (skill.getId() == 1403) //summon friend
						{
							// Send message
							ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
							confirm.addCharName(activeChar);
							confirm.addZoneName(activeChar.getX(), activeChar.getY(), activeChar.getZ());
							confirm.addTime(30000);
							confirm.addRequesterId(activePlayer.getObjectId());
							target.sendPacket(confirm);
						} else {
							Player.teleToTarget(targetPlayer, activePlayer, skill);
							targetPlayer.teleportRequest(null, null);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
