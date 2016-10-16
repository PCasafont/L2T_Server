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

package l2server.gameserver.model.zone.type;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.model.L2Object.InstanceType;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.zone.L2ZoneType;

import java.util.Collection;
import java.util.concurrent.Future;

/**
 * A damage zone
 *
 * @author durgus
 */
public class L2DamageZone extends L2ZoneType
{
	private int damageHPPerSec;
	private int damageMPPerSec;
	private Future<?> task;

	private int castleId;
	private Castle castle;

	private int startTask;
	private int reuseTask;

	private boolean enabled;

	public L2DamageZone(int id)
	{
		super(id);

		// Setup default damage
		this.damageHPPerSec = 200;
		this.damageMPPerSec = 0;

		// Setup default start / reuse time
		this.startTask = 10;
		this.reuseTask = 5000;

		// no castle by default
		this.castleId = 0;
		this.castle = null;

		this.enabled = true;

		setTargetType(InstanceType.L2Playable); // default only playabale
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equalsIgnoreCase("dmgHPSec"))
		{
			this.damageHPPerSec = Integer.parseInt(value);
		}
		else if (name.equalsIgnoreCase("dmgMPSec"))
		{
			this.damageMPPerSec = Integer.parseInt(value);
		}
		else if (name.equalsIgnoreCase("castleId"))
		{
			this.castleId = Integer.parseInt(value);
		}
		else if (name.equalsIgnoreCase("initialDelay"))
		{
			this.startTask = Integer.parseInt(value);
		}
		else if (name.equalsIgnoreCase("reuse"))
		{
			this.reuseTask = Integer.parseInt(value);
		}
		else if (name.equalsIgnoreCase("enabled"))
		{
			this.enabled = Boolean.parseBoolean(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (this.task == null && (this.damageHPPerSec != 0 || this.damageMPPerSec != 0))
		{
			L2PcInstance player = character.getActingPlayer();
			if (getCastle() != null) // Castle zone
			{
				if (!(getCastle().getSiege().getIsInProgress() && player != null &&
						player.getSiegeState() != 2)) // Siege and no defender
				{
					return;
				}
			}
			synchronized (this)
			{
				if (this.task == null)
				{
					this.task = ThreadPoolManager.getInstance()
							.scheduleGeneralAtFixedRate(new ApplyDamage(this), this.startTask, this.reuseTask);
				}
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (this.characterList.isEmpty() && this.task != null)
		{
			stopTask();
		}
	}

	protected Collection<L2Character> getCharacterList()
	{
		return this.characterList.values();
	}

	protected int getHPDamagePerSecond()
	{
		return this.damageHPPerSec;
	}

	protected int getMPDamagePerSecond()
	{
		return this.damageMPPerSec;
	}

	protected void stopTask()
	{
		if (this.task != null)
		{
			this.task.cancel(false);
			this.task = null;
		}
	}

	private Castle getCastle()
	{
		if (this.castleId > 0 && this.castle == null)
		{
			this.castle = CastleManager.getInstance().getCastleById(this.castleId);
		}

		return this.castle;
	}

	class ApplyDamage implements Runnable
	{
		private final L2DamageZone dmgZone;
		private final Castle castle;

		ApplyDamage(L2DamageZone zone)
		{
			this.dmgZone = zone;
			this.castle = zone.getCastle();
		}

		@Override
		public void run()
		{
			boolean siege = false;

			if (this.castle != null)
			{
				siege = this.castle.getSiege().getIsInProgress();
				// castle zones active only during siege
				if (!siege)
				{
					this.dmgZone.stopTask();
					return;
				}
			}

			if (!enabled)
			{
				return;
			}

			for (L2Character temp : this.dmgZone.getCharacterList())
			{
				if (temp != null && !temp.isDead())
				{
					if (siege)
					{
						// during siege defenders not affected
						final L2PcInstance player = temp.getActingPlayer();
						if (player != null && player.isInSiege() && player.getSiegeState() == 2)
						{
							continue;
						}
					}

					if (getHPDamagePerSecond() != 0)
					{
						temp.reduceCurrentHp(this.dmgZone.getHPDamagePerSecond(), null, null);
					}
					if (getMPDamagePerSecond() != 0)
					{
						temp.reduceCurrentMp(this.dmgZone.getMPDamagePerSecond());
					}
				}
			}
		}
	}

	public void setEnabled(boolean state)
	{
		this.enabled = state;
	}

	@Override
	public void onDieInside(L2Character character, L2Character killer)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}
}
