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
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.templates.chars.L2NpcTemplate;

/**
 * This class extends Guard class for quests, that require tracking of onAttack and onKill events from monsters' attacks.
 *
 * @author GKR
 */
public final class L2QuestGuardInstance extends L2GuardInstance
{
	private boolean _isAutoAttackable = true;
	private boolean _isPassive = false;

	public L2QuestGuardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2QuestGuardInstance);
	}

	@Override
	public void addDamage(L2Character attacker, int damage, L2Skill skill)
	{
		super.addDamage(attacker, damage, skill);

		if (attacker instanceof L2Attackable)
		{
			if (getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK) != null)
			{
				for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK))
				{
					quest.notifyAttack(this, null, damage, false, skill);
				}
			}
		}
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		// Kill the L2NpcInstance (the corpse disappeared after 7 seconds)
		if (!super.doDie(killer))
		{
			return false;
		}

		if (killer instanceof L2Attackable)
		{
			if (getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL) != null)
			{
				for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL))
				{
					ThreadPoolManager.getInstance()
							.scheduleEffect(new OnKillNotifyTask(this, quest, null, false), _onKillDelay);
				}
			}
		}

		return true;
	}

	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (!_isPassive && !(attacker instanceof L2PcInstance))
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}

	public void setPassive(boolean state)
	{
		_isPassive = state;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return _isAutoAttackable && !(attacker instanceof L2PcInstance);
	}

	@Override
	public void setAutoAttackable(boolean state)
	{
		_isAutoAttackable = state;
	}

	public boolean isPassive()
	{
		return _isPassive;
	}
}
