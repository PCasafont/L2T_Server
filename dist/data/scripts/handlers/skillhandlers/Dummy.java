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
import l2server.gameserver.instancemanager.HandysBlockCheckerManager;
import l2server.gameserver.instancemanager.HandysBlockCheckerManager.ArenaParticipantsHolder;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.BlockInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.skills.SkillType;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.5.2.4 $ $Date: 2005/04/03 15:55:03 $
 */

public class Dummy implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.DUMMY};
	
	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		if (!(activeChar instanceof Player)) {
			return;
		}
		
		switch (skill.getId()) {
			case 5852:
			case 5853: {
				final WorldObject obj = targets[0];
				if (obj != null) {
					useBlockCheckerSkill((Player) activeChar, skill, obj);
				}
				break;
			}
		}
	}
	
	/**
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}
	
	private final void useBlockCheckerSkill(Player activeChar, Skill skill, WorldObject target) {
		if (!(target instanceof BlockInstance)) {
			return;
		}
		
		BlockInstance block = (BlockInstance) target;
		
		final int arena = activeChar.getBlockCheckerArena();
		if (arena != -1) {
			final ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(arena);
			if (holder == null) {
				return;
			}
			
			final int team = holder.getPlayerTeam(activeChar);
			final int color = block.getColorEffect();
			if (team == 0 && color == 0x00) {
				block.changeColor(activeChar, holder, team);
			} else if (team == 1 && color == 0x53) {
				block.changeColor(activeChar, holder, team);
			}
		}
	}
}
