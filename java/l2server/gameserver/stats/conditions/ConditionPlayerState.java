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

package l2server.gameserver.stats.conditions;

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.PlayerState;
import l2server.gameserver.stats.Env;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * The Class ConditionPlayerState.
 *
 * @author mkizub
 */
public class ConditionPlayerState extends Condition
{
	private final PlayerState check;
	private final boolean required;

	/**
	 * Instantiates a new condition player state.
	 *
	 * @param check    the player state to be verified.
	 * @param required the required value.
	 */
	public ConditionPlayerState(PlayerState check, boolean required)
	{
		this.check = check;
		this.required = required;
	}

	/**
	 * @see l2server.gameserver.stats.conditions.Condition#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		final L2Character character = env.player;
		L2PcInstance player = null;
		switch (this.check)
		{
			case RESTING:
				player = character.getActingPlayer();
				if (player != null)
				{
					return player.isSitting() == this.required;
				}
				return !this.required;
			case MOVING:
				return character.isMoving() == this.required;
			case RUNNING:
				return character.isRunning() == this.required;
			case STANDING:
				player = character.getActingPlayer();
				if (player != null)
				{
					return this.required != (player.isSitting() || player.isMoving());
				}
				return this.required != character.isMoving();
			case COMBAT:
				return AttackStanceTaskManager.getInstance().getAttackStanceTask(character);
			case FLYING:
				return character.isFlying() == this.required;
			case BEHIND:
				return character.isBehindTarget() == this.required;
			case FRONT:
				return character.isInFrontOfTarget() == this.required;
			case CHAOTIC:
				player = character.getActingPlayer();
				if (player != null)
				{
					return player.getReputation() < 0 == this.required;
				}
				return !this.required;
			case OLYMPIAD:
				player = character.getActingPlayer();
				if (player != null)
				{
					return player.isInOlympiadMode() == this.required;
				}
				return !this.required;
		}
		return !this.required;
	}
}
