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

import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Trap;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.TrapInstance;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.NpcTemplate;

public class SkillTrap extends SkillSummon {
	private int triggerSkillId = 0;
	private int triggerSkillLvl = 0;
	private int trapNpcId = 0;
	protected L2Spawn trapSpawn;
	
	public SkillTrap(StatsSet set) {
		super(set);
		triggerSkillId = set.getInteger("triggerSkillId");
		triggerSkillLvl = set.getInteger("triggerSkillLvl");
		trapNpcId = set.getInteger("trapNpcId");
	}
	
	public int getTriggerSkillId() {
		return triggerSkillId;
	}
	
	/**
	 * @see Skill#useSkill(Creature, WorldObject[])
	 */
	@Override
	public void useSkill(Creature caster, WorldObject[] targets) {
		if (caster.isAlikeDead() || !(caster instanceof Player)) {
			return;
		}
		
		if (trapNpcId == 0) {
			return;
		}
		
		Player activeChar = (Player) caster;
		
		if (activeChar.inObserverMode()) {
			return;
		}
		
		if (activeChar.isMounted()) {
			return;
		}
		
		if (triggerSkillId == 0 || triggerSkillLvl == 0) {
			return;
		}
		
		Trap trap = activeChar.getTrap();
		if (trap != null) {
			trap.unSummon();
		}
		
		Skill skill = SkillTable.getInstance().getInfo(triggerSkillId, triggerSkillLvl);
		
		if (skill == null) {
			return;
		}
		
		NpcTemplate TrapTemplate = NpcTable.getInstance().getTemplate(trapNpcId);
		trap = new TrapInstance(IdFactory.getInstance().getNextId(), TrapTemplate, activeChar, getTotalLifeTime(), skill);
		trap.setCurrentHp(trap.getMaxHp());
		trap.setCurrentMp(trap.getMaxMp());
		trap.setInvul(true);
		trap.setHeading(activeChar.getHeading());
		activeChar.setTrap(trap);
		//World.getInstance().storeObject(trap);
		trap.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}
}
