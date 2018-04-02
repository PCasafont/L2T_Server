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
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.Quest.TrapAction;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.SkillType;

public class Trap implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.DETECT_TRAP, SkillType.REMOVE_TRAP};
	
	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		if (activeChar == null || skill == null) {
			return;
		}
		
		switch (skill.getSkillType()) {
			case DETECT_TRAP: {
				for (Creature target : activeChar.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius())) {
					if (!(target instanceof l2server.gameserver.model.actor.Trap)) {
						continue;
					}
					
					if (target.isAlikeDead()) {
						continue;
					}
					
					final l2server.gameserver.model.actor.Trap trap = (l2server.gameserver.model.actor.Trap) target;
					
					if (trap.getLevel() <= skill.getPower()) {
						trap.setDetected(activeChar);
					}
				}
				break;
			}
			case REMOVE_TRAP: {
				for (Creature target : (Creature[]) targets) {
					if (!(target instanceof l2server.gameserver.model.actor.Trap)) {
						continue;
					}
					
					if (target.isAlikeDead()) {
						continue;
					}
					
					final l2server.gameserver.model.actor.Trap trap = (l2server.gameserver.model.actor.Trap) target;
					
					if (!trap.canSee(activeChar)) {
						if (activeChar instanceof Player) {
							((Player) activeChar).sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
						}
						continue;
					}
					
					if (trap.getLevel() > skill.getPower()) {
						continue;
					}
					
					if (trap.getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION) != null) {
						for (Quest quest : trap.getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION)) {
							quest.notifyTrapAction(trap, activeChar, TrapAction.TRAP_DISARMED);
						}
					}
					
					trap.unSummon();
					if (activeChar instanceof Player) {
						((Player) activeChar).sendPacket(SystemMessage.getSystemMessage(SystemMessageId.A_TRAP_DEVICE_HAS_BEEN_STOPPED));
					}
				}
			}
			default:
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
