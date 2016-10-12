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

package l2server.gameserver.ai;

import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Vehicle;

public abstract class L2VehicleAI extends L2CharacterAI
{
	/**
	 * @author DS
	 * Simple AI for vehicles
	 */
	public L2VehicleAI(L2Vehicle.AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	protected void onIntentionAttack(L2Character target)
	{
	}

	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
	}

	@Override
	protected void onIntentionFollow(L2Character target)
	{
	}

	@Override
	protected void onIntentionPickUp(L2Object item)
	{
	}

	@Override
	protected void onIntentionInteract(L2Object object)
	{
	}

	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
	}

	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
	}

	@Override
	protected void onEvtStunned(L2Character attacker)
	{
	}

	@Override
	protected void onEvtSleeping(L2Character attacker)
	{
	}

	@Override
	protected void onEvtRooted(L2Character attacker)
	{
	}

	@Override
	protected void onEvtForgetObject(L2Object object)
	{
	}

	@Override
	protected void onEvtCancel()
	{
	}

	@Override
	protected void onEvtDead()
	{
	}

	@Override
	protected void onEvtFakeDeath()
	{
	}

	@Override
	protected void onEvtFinishCasting()
	{
	}

	@Override
	protected void clientActionFailed()
	{
	}

	@Override
	protected void moveToPawn(L2Object pawn, int offset)
	{
	}

	@Override
	protected void clientStoppedMoving()
	{
	}
}
