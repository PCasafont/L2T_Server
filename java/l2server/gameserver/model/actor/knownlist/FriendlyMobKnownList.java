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

import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2FriendlyMobInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;

public class FriendlyMobKnownList extends AttackableKnownList
{
	public FriendlyMobKnownList(L2FriendlyMobInstance activeChar)
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

		if (object instanceof L2PcInstance && getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
		{
			getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
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

		if (!(object instanceof L2Character))
		{
			return true;
		}

		if (getActiveChar().hasAI())
		{
			getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
			if (getActiveChar().getTarget() == object)
			{
				getActiveChar().setTarget(null);
			}
		}

		if (getActiveChar().isVisible() && getKnownPlayers().isEmpty() && getKnownSummons().isEmpty())
		{
			getActiveChar().clearAggroList();
			//removeAllKnownObjects();
			if (getActiveChar().hasAI())
			{
				getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
			}
		}

		return true;
	}

	@Override
	public final L2FriendlyMobInstance getActiveChar()
	{
		return (L2FriendlyMobInstance) super.getActiveChar();
	}
}
