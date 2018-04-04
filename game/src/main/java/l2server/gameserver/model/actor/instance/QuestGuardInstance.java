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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.templates.chars.NpcTemplate;

/**
 * This class extends Guard class for quests, that require tracking of onAttack and onKill events from monsters' attacks.
 *
 * @author GKR
 */
public final class QuestGuardInstance extends GuardInstance {
	private boolean isAutoAttackable = true;
	private boolean isPassive = false;
	
	public QuestGuardInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2QuestGuardInstance);
	}
	
	@Override
	public void addDamage(Creature attacker, int damage, Skill skill) {
		super.addDamage(attacker, damage, skill);
		
		if (attacker instanceof Attackable) {
			if (getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK) != null) {
				for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK)) {
					quest.notifyAttack(this, null, damage, false, skill);
				}
			}
		}
	}
	
	@Override
	public boolean doDie(Creature killer) {
		// Kill the NpcInstance (the corpse disappeared after 7 seconds)
		if (!super.doDie(killer)) {
			return false;
		}
		
		if (killer instanceof Attackable) {
			if (getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL) != null) {
				for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL)) {
					ThreadPoolManager.getInstance().scheduleEffect(new OnKillNotifyTask(this, quest, null, false), onKillDelay);
				}
			}
		}
		
		return true;
	}
	
	@Override
	public void addDamageHate(Creature attacker, int damage, int aggro) {
		if (!isPassive && !(attacker instanceof Player)) {
			super.addDamageHate(attacker, damage, aggro);
		}
	}
	
	public void setPassive(boolean state) {
		isPassive = state;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker) {
		return isAutoAttackable && !(attacker instanceof Player);
	}
	
	@Override
	public void setAutoAttackable(boolean state) {
		isAutoAttackable = state;
	}
	
	public boolean isPassive() {
		return isPassive;
	}
}
