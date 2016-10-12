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

package l2server.gameserver.model.actor.knownlist;

import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2GuardInstance;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.log.Log;

public class GuardKnownList extends AttackableKnownList
{

	public GuardKnownList(L2GuardInstance activeChar)
	{
		super(activeChar);
	}

	@Override
	public boolean addKnownObject(L2Object object)
	{
		if (!super.addKnownObject(object))
		{
			return false;
		}

		if (object instanceof L2PcInstance)
		{
			// Check if the object added is a L2PcInstance that owns Karma
			if (((L2PcInstance) object).getReputation() < 0)
			{
				if (Config.DEBUG)
				{
					Log.fine(getActiveChar().getObjectId() + ": PK " + object.getObjectId() + " entered scan range");
				}

				// Set the L2GuardInstance Intention to AI_INTENTION_ACTIVE
				if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				}
			}
		}
		else if (Config.GUARD_ATTACK_AGGRO_MOB && getActiveChar().isInActiveRegion() &&
				object instanceof L2MonsterInstance)
		{
			// Check if the object added is an aggressive L2MonsterInstance
			if (((L2MonsterInstance) object).isAggressive())
			{
				if (Config.DEBUG)
				{
					Log.fine(getActiveChar().getObjectId() + ": Aggressive mob " + object.getObjectId() +
							" entered scan range");
				}

				// Set the L2GuardInstance Intention to AI_INTENTION_ACTIVE
				if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				}
			}
		}
		else if (object instanceof L2Npc && ((L2Npc) object).getClan() != null &&
				((L2Npc) object).getClan().equalsIgnoreCase(getActiveChar().getEnemyClan()))
		{
			if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			{
				getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			}
		}

		return true;
	}

	@Override
	protected boolean removeKnownObject(L2Object object, boolean forget)
	{
		if (!super.removeKnownObject(object, forget))
		{
			return false;
		}

		// Check if the _aggroList of the L2GuardInstance is Empty
		if (getActiveChar().noTarget())
		{
			//removeAllKnownObjects();

			// Set the L2GuardInstance to AI_INTENTION_IDLE
			if (getActiveChar().hasAI())
			{
				getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
			}
		}

		return true;
	}

	@Override
	public final L2GuardInstance getActiveChar()
	{
		return (L2GuardInstance) super.getActiveChar();
	}
}
