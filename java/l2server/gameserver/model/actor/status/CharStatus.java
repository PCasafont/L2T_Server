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

package l2server.gameserver.model.actor.status;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.stat.CharStat;
import l2server.gameserver.network.serverpackets.StatusUpdate.StatusUpdateDisplay;
import l2server.gameserver.stats.BaseStats;
import l2server.gameserver.stats.Formulas;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class CharStatus
{

	private L2Character _activeChar;

	private double _currentHp = 0; //Current HP of the L2Character
	private double _currentMp = 0; //Current MP of the L2Character

	/**
	 * Array containing all clients that need to be notified about hp/mp updates of the L2Character
	 */
	private Set<L2Character> _statusListener;

	private Future<?> _regTask;

	protected byte _flagsRegenActive = 0;

	protected static final byte REGEN_FLAG_CP = 4;
	private static final byte REGEN_FLAG_HP = 1;
	private static final byte REGEN_FLAG_MP = 2;

	public CharStatus(L2Character activeChar)
	{
		_activeChar = activeChar;
	}

	/**
	 * Add the object to the list of L2Character that must be informed of HP/MP updates of this L2Character.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates.
	 * Players who must be informed are players that target this L2Character.
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.<BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Target a PC or NPC</li><BR><BR>
	 *
	 * @param object L2Character to add to the listener
	 */
	public final void addStatusListener(L2Character object)
	{
		if (object == getActiveChar())
		{
			return;
		}

		getStatusListener().add(object);
	}

	/**
	 * Remove the object from the list of L2Character that must be informed of HP/MP updates of this L2Character.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates.
	 * Players who must be informed are players that target this L2Character.
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.<BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Untarget a PC or NPC</li><BR><BR>
	 *
	 * @param object L2Character to add to the listener
	 */
	public final void removeStatusListener(L2Character object)
	{
		getStatusListener().remove(object);
	}

	/**
	 * Return the list of L2Character that must be informed of HP/MP updates of this L2Character.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates.
	 * Players who must be informed are players that target this L2Character.
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.<BR><BR>
	 *
	 * @return The list of L2Character to inform or null if empty
	 */
	public final Set<L2Character> getStatusListener()
	{
		if (_statusListener == null)
		{
			_statusListener = new CopyOnWriteArraySet<>();
		}
		return _statusListener;
	}

	// place holder, only PcStatus has CP
	public void reduceCp(int value)
	{
	}

	/**
	 * Reduce the current HP of the L2Character and launch the doDie Task if necessary.<BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2Attackable : Set overhit values</li><BR>
	 * <li> L2Npc : Update the attacker AggroInfo of the L2Attackable _aggroList and clear duel status of the attacking players</li><BR><BR>
	 *
	 * @param attacker The L2Character who attacks
	 */
	public void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true, false, false);
	}

	public void reduceHp(double value, L2Character attacker, boolean isHpConsumption)
	{
		reduceHp(value, attacker, true, false, isHpConsumption);
	}

	public void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isHPConsumption)
	{
		if (getActiveChar().isDead())
		{
			return;
		}

		boolean isHide = attacker instanceof L2PcInstance && ((L2PcInstance) attacker).getAppearance().getInvisible();
		if (!isDOT && !isHPConsumption || isHide)
		{
			getActiveChar().stopEffectsOnDamage(awake, (int) value);

			if (getActiveChar().isStunned())
			{
				int baseBreakChance = attacker.getLevel() > 85 ? 5 : 25; // TODO Recheck this
				double breakChance = baseBreakChance * Math.sqrt(BaseStats.CON.calcBonus(getActiveChar()));

				if (value > 2000)
				{
					breakChance *= 4;
				}
				else if (value > 1000)
				{
					breakChance *= 2;
				}
				else if (value > 500)
				{
					breakChance *= 1.5;
				}

				if (value > 100 && Rnd.get(100) < breakChance)
				{
					getActiveChar().stopStunning(true);
				}
			}
		}

		// invul handling
		if (getActiveChar().isInvul(attacker))
		{
			// other chars can't damage
			//if (attacker != getActiveChar())
			//	return;

			// only DOT and HP consumption allowed for damage self
			if (!isDOT && !isHPConsumption)
			{
				return;
			}
		}

		if (attacker instanceof L2Playable)
		{
			final L2PcInstance attackerPlayer = attacker.getActingPlayer();

			if (attackerPlayer.isGM() && !attackerPlayer.getAccessLevel().canGiveDamage())
			{
				return;
			}
		}

		if (attacker instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) attacker;

			if (player.isGM() && !player.getAccessLevel().canGiveDamage())
			{
				return;
			}
		}

		if (isDOT)
		{
			value = Formulas.calcCustomModifier(attacker, getActiveChar(), value);
		}

		StatusUpdateDisplay display = StatusUpdateDisplay.NONE;
		if (isDOT)
		{
			display = StatusUpdateDisplay.DOT;
		}
		if (value > 0) // Reduce Hp if any, and Hp can't be negative
		{
			setCurrentHp(Math.max(getCurrentHp() - value, 0), true, attacker, display);
		}

		if (getActiveChar().getCurrentHp() < 0.5 && getActiveChar().isMortal()) // Die
		{
			getActiveChar().abortAttack();
			getActiveChar().abortCast();

			if (Config.DEBUG)
			{
				Log.fine("char is dead.");
			}

			// Check for onKill skill trigger
			if (attacker.getChanceSkills() != null)
			{
				attacker.getChanceSkills().onKill(getActiveChar());
			}

			getActiveChar().doDie(attacker);
		}
	}

	public void reduceMp(double value)
	{
		setCurrentMp(Math.max(getCurrentMp() - value, 0));
	}

	/**
	 * Start the HP/MP/CP Regeneration task.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate the regen task period </li>
	 * <li>Launch the HP/MP/CP Regeneration task with Medium priority </li><BR><BR>
	 */
	public final synchronized void startHpMpRegeneration()
	{
		if (_regTask == null && !getActiveChar().isDead())
		{
			if (Config.DEBUG)
			{
				Log.fine("HP/MP regen started");
			}

			// Get the Regeneration periode
			int period = Formulas.getRegeneratePeriod(getActiveChar());

			// Create the HP/MP/CP Regeneration task
			_regTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new RegenTask(), period, period);
		}
	}

	/**
	 * Stop the HP/MP/CP Regeneration task.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the RegenActive flag to False </li>
	 * <li>Stop the HP/MP/CP Regeneration task </li><BR><BR>
	 */
	public final synchronized void stopHpMpRegeneration()
	{
		if (_regTask != null)
		{
			if (Config.DEBUG)
			{
				Log.fine("HP/MP regen stop");
			}

			// Stop the HP/MP/CP Regeneration task
			_regTask.cancel(false);
			_regTask = null;

			// Set the RegenActive flag to false
			_flagsRegenActive = 0;
		}
	}

	// place holder, only PcStatus has CP
	public double getCurrentCp()
	{
		return 0;
	}

	// place holder, only PcStatus has CP
	public void setCurrentCp(double newCp)
	{
	}

	public final double getCurrentHp()
	{
		return _currentHp;
	}

	public final void setCurrentHp(double newHp)
	{
		setCurrentHp(newHp, false);
	}

	public void setCurrentHp(double newHp, boolean broadcastPacket)
	{
		setCurrentHp(newHp, broadcastPacket, null, StatusUpdateDisplay.NONE);
	}

	public void setCurrentHp(double newHp, boolean broadcastPacket, L2Character causer, StatusUpdateDisplay display)
	{
		// Get the Max HP of the L2Character
		final double maxHp = getActiveChar().getStat().getMaxHp();

		synchronized (this)
		{
			if (getActiveChar().isDead())
			{
				return;
			}

			if (newHp >= maxHp)
			{
				// Set the RegenActive flag to false
				_currentHp = maxHp;
				_flagsRegenActive &= ~REGEN_FLAG_HP;

				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				// Set the RegenActive flag to true
				_currentHp = newHp;
				_flagsRegenActive |= REGEN_FLAG_HP;

				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		if (broadcastPacket)
		{
			getActiveChar().broadcastStatusUpdate(causer, display);
		}
	}

	public final void setCurrentHpMp(double newHp, double newMp)
	{
		setCurrentHp(newHp, false);
		setCurrentMp(newMp, true); //send the StatusUpdate only once
	}

	public final double getCurrentMp()
	{
		return _currentMp;
	}

	public final void setCurrentMp(double newMp)
	{
		setCurrentMp(newMp, true);
	}

	public final void setCurrentMp(double newMp, boolean broadcastPacket)
	{
		// Get the Max MP of the L2Character
		final int maxMp = getActiveChar().getStat().getMaxMp();

		synchronized (this)
		{
			if (getActiveChar().isDead())
			{
				return;
			}

			if (newMp >= maxMp)
			{
				// Set the RegenActive flag to false
				_currentMp = maxMp;
				_flagsRegenActive &= ~REGEN_FLAG_MP;

				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				// Set the RegenActive flag to true
				_currentMp = newMp;
				_flagsRegenActive |= REGEN_FLAG_MP;

				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		if (broadcastPacket)
		{
			getActiveChar().broadcastStatusUpdate();
		}
	}

	protected void doRegeneration()
	{
		final CharStat charstat = getActiveChar().getStat();

		// Modify the current HP of the L2Character and broadcast Server->Client packet StatusUpdate
		if (getCurrentHp() < charstat.getMaxHp())
		{
			setCurrentHp(getCurrentHp() + Formulas.calcHpRegen(getActiveChar()), false);
		}

		// Modify the current MP of the L2Character and broadcast Server->Client packet StatusUpdate
		if (getCurrentMp() < charstat.getMaxMp())
		{
			setCurrentMp(getCurrentMp() + Formulas.calcMpRegen(getActiveChar()), false);
		}

		if (!getActiveChar().isInActiveRegion())
		{
			// no broadcast necessary for characters that are in inactive regions.
			// stop regeneration for characters who are filled up and in an inactive region.
			if (getCurrentHp() == charstat.getMaxHp() && getCurrentMp() == charstat.getMaxMp())
			{
				stopHpMpRegeneration();
			}
		}
		else
		{
			getActiveChar().broadcastStatusUpdate(); //send the StatusUpdate packet
		}
	}

	/**
	 * Task of HP/MP regeneration
	 */
	class RegenTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				doRegeneration();
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "", e);
			}
		}
	}

	public L2Character getActiveChar()
	{
		return _activeChar;
	}
}
