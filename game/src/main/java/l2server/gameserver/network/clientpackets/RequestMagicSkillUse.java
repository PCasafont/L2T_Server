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

package l2server.gameserver.network.clientpackets;

import l2server.Config;
import l2server.gameserver.datatables.ComboSkillTable;
import l2server.gameserver.datatables.ComboSkillTable.Combo;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.templates.skills.SkillType;
import l2server.gameserver.util.GMAudit;

import java.util.Map.Entry;

/**
 * This class ...
 *
 * @version $Revision: 1.7.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestMagicSkillUse extends L2GameClientPacket {
	
	private int magicId;
	private boolean ctrlPressed;
	private boolean shiftPressed;
	
	@Override
	protected void readImpl() {
		magicId = readD(); // Identifier of the used skill
		ctrlPressed = readD() != 0; // True if it's a ForceAttack : Ctrl pressed
		shiftPressed = readC() != 0; // True if Shift pressed
	}
	
	@Override
	protected void runImpl() {
		// Get the current Player of the player
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}
		
		if (activeChar.getCaptcha() != null && !activeChar.onActionCaptcha(true)) {
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.hasIdentityCrisis()) {
			activeChar.sendMessage("You cannot use any skill while having identity crisis.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isEventDisarmed()) {
			activeChar.sendMessage("You cannot use any skill while playing this event.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Get the level of the used skill
		int level = activeChar.getSkillLevelHash(magicId);
		
		// Check combo
		if (activeChar.getTarget() instanceof Creature) {
			for (Abnormal ab : ((Creature) activeChar.getTarget()).getAllEffects()) {
				if (ab.getComboId() != 0) {
					Combo combo = ComboSkillTable.getInstance().getCombo(ab.getComboId());
					if (combo.skills.containsKey(magicId)) {
						magicId = combo.skills.get(magicId);
						level = 1;
						break;
					}
					
					for (Entry<Integer, Integer> comboSkill : combo.skills.entrySet()) {
						if (comboSkill.getValue() == magicId && activeChar.getSkillLevelHash(comboSkill.getKey()) > 0) {
							level = 1;
							break;
						}
					}
				}
			}
		}
		
		if (level <= 0) {
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Get the Skill template corresponding to the skillID received from the client
		Skill skill = SkillTable.getInstance().getInfo(magicId, level);
		
		// Check the validity of the skill
		if (skill != null) {
			if (skill.getSkillType() != SkillType.STRSIEGEASSAULT && activeChar.isMounted()) {
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (skill.getSkillType() != SkillType.TRANSFORMDISPEL && (activeChar.isTransformed() || activeChar.isInStance()) &&
					(!activeChar.containsAllowedTransformSkill(skill.getId()) ||
							activeChar.getLastSkillCast() != null && activeChar.getLastSkillCast().getSkillType() == SkillType.TRANSFORMDISPEL)) {
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			// log.debug("	skill:"+skill.getName() + " level:"+skill.getLevel() + " passive:"+skill.isPassive());
			// log.debug("	range:"+skill.getCastRange()+" targettype:"+skill.getTargetType()+" optype:"+skill.getOperateType()+" power:"+skill.getPower());
			// log.debug("	reusedelay:"+skill.getReuseDelay()+" hittime:"+skill.getHitTime());
			// log.debug("	currentState:"+activeChar.getCurrentState());	//for debug
			
			// If Alternate rule Karma punishment is set to true, forbid skill Return to player with Karma
			if (skill.getSkillType() == SkillType.RECALL && !Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && activeChar.getReputation() < 0) {
				return;
			}
			
			// players mounted on pets cannot use any toggle skills
			if (skill.isToggle() && activeChar.isMounted()) {
				return;
			}
			
			if (activeChar.isGM()) {
				GMAudit.auditGMAction(activeChar.getName(),
						"Use skill: " + skill.getName(),
						activeChar.getTarget() != null ? activeChar.getTarget().getName() : "No Target");
			}
			
			if (skill.isStanceSwitch()) {
				int offset = activeChar.getElementalStance();
				if (offset > 4) {
					offset = 5;
				}
				
				if (skill.getId() == 11177) {
					offset = offset < 5 ? 0 : 12;
				}
				
				Skill magic = SkillTable.getInstance().getInfo(skill.getId() + offset, skill.getLevelHash());
				activeChar.useMagic(magic, ctrlPressed, shiftPressed);
				return;
			}
			
			if (activeChar.getQueuedSkill() != null && activeChar.getQueuedSkill().getSkillId() == 30001 &&
					skill.getId() != activeChar.getQueuedSkill().getSkillId()) {
				activeChar.setQueuedSkill(null, ctrlPressed, shiftPressed);
			}
			
			// activeChar.stopMove();
			activeChar.useMagic(skill, ctrlPressed, shiftPressed);
		} else {
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			log.warn("No skill found with id " + magicId + " and level " + level + " !!");
		}
	}
}
