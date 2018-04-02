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
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.DoorInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.SkillType;

public class NornilsPower implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.NORNILS_POWER};

	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		if (!(activeChar instanceof Player)) {
			return;
		}
		InstanceWorld world = null;

		final int instanceId = activeChar.getInstanceId();
		if (instanceId > 0) {
			world = InstanceManager.getInstance().getPlayerWorld((Player) activeChar);
		}

		if (world != null && world.instanceId == instanceId && world.templateId == 11) {
			if (activeChar.isInsideRadius(-107393, 83677, 100, true)) {
				activeChar.destroyItemByItemId("NornilsPower", 9713, 1, activeChar, true);
				DoorInstance door = InstanceManager.getInstance().getInstance(world.instanceId).getDoor(16200010);
				if (door != null) {
					door.setMeshIndex(1);
					//door.setTargetable(true);
					door.broadcastStatusUpdate();
				}
			} else {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(skill);
				activeChar.sendPacket(sm);
			}
		} else {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOTHING_HAPPENED));
		}
	}

	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
