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

package l2server.gameserver.model;

import l2server.Config;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.olympiad.OlympiadGameManager;
import l2server.gameserver.model.olympiad.OlympiadGameTask;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.AbnormalStatusUpdate;
import l2server.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import l2server.gameserver.network.serverpackets.PartySpelled;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectType;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CharEffectList
{
	private static final L2Abnormal[] EMPTY_EFFECTS = new L2Abnormal[0];

	private AtomicBoolean queueLock = new AtomicBoolean(true);

	private CopyOnWriteArrayList<L2Abnormal> buffs;
	private CopyOnWriteArrayList<L2Abnormal> debuffs;

	// The table containing the List of all stacked effect in progress for each Stack group Identifier
	private Map<String, List<L2Abnormal>> stackedEffects;

	private volatile boolean hasBuffsRemovedOnAction = false;
	private volatile boolean hasBuffsRemovedOnDamage = false;
	private volatile boolean hasDebuffsRemovedOnDamage = false;
	private volatile boolean hasBuffsRemovedOnDebuffBlock = false;

	private boolean queuesInitialized = false;
	private LinkedBlockingQueue<L2Abnormal> addQueue;
	private LinkedBlockingQueue<L2Abnormal> removeQueue;
	private long effectFlags;

	// only party icons need to be updated
	private boolean partyOnly = false;

	// Owner of this list
	private L2Character owner;

	public CharEffectList(L2Character owner)
	{
		this.owner = owner;
	}

	/**
	 * Returns all effects affecting stored in this CharEffectList
	 *
	 * @return
	 */
	public final L2Abnormal[] getAllEffects()
	{
		// If no effect is active, return EMPTY_EFFECTS
		if ((this.buffs == null || this.buffs.isEmpty()) && (this.debuffs == null || this.debuffs.isEmpty()))
		{
			return EMPTY_EFFECTS;
		}

		// Create a copy of the effects
		ArrayList<L2Abnormal> temp = new ArrayList<>();

		// Add all buffs and all debuffs
		if (this.buffs != null)
		{
			//synchronized (this.buffs)
			{
				if (!this.buffs.isEmpty())
				{
					temp.addAll(this.buffs);
				}
			}
		}
		if (this.debuffs != null)
		{
			//synchronized (this.debuffs)
			{
				if (!this.debuffs.isEmpty())
				{
					temp.addAll(this.debuffs);
				}
			}
		}

		// Return all effects in an array
		L2Abnormal[] tempArray = new L2Abnormal[temp.size()];
		temp.toArray(tempArray);
		return tempArray;
	}

	public final L2Abnormal[] getAllDebuffs()
	{
		// If no effect is active, return EMPTY_EFFECTS
		if (this.debuffs == null || this.debuffs.isEmpty())
		{
			return EMPTY_EFFECTS;
		}

		// Create a copy of the effects
		ArrayList<L2Abnormal> temp = new ArrayList<>();

		if (this.debuffs != null)
		{
			if (!this.debuffs.isEmpty())
			{
				temp.addAll(this.debuffs);
			}
		}

		// Return all effects in an array
		L2Abnormal[] tempArray = new L2Abnormal[temp.size()];
		temp.toArray(tempArray);
		return tempArray;
	}

	/**
	 * Returns the first effect matching the given EffectType
	 *
	 * @param tp
	 * @return
	 */
	public final L2Abnormal getFirstEffect(L2AbnormalType tp)
	{
		L2Abnormal effectNotInUse = null;

		if (this.buffs != null)
		{
			//synchronized (this.buffs)
			{
				if (!this.buffs.isEmpty())
				{
					for (L2Abnormal e : this.buffs)
					{
						if (e == null)
						{
							continue;
						}

						if (e.getType() == tp)
						{
							if (e.getInUse())
							{
								return e;
							}
							else
							{
								effectNotInUse = e;
							}
						}
					}
				}
			}
		}
		if (effectNotInUse == null && this.debuffs != null)
		{
			//synchronized (this.debuffs)
			{
				if (!this.debuffs.isEmpty())
				{
					for (L2Abnormal e : this.debuffs)
					{
						if (e == null)
						{
							continue;
						}
						if (e.getType() == tp)
						{
							if (e.getInUse())
							{
								return e;
							}
							else
							{
								effectNotInUse = e;
							}
						}
					}
				}
			}
		}
		return effectNotInUse;
	}

	/**
	 * Returns the first effect matching the given L2Skill
	 *
	 * @param skill
	 * @return
	 */
	public final L2Abnormal getFirstEffect(L2Skill skill)
	{
		L2Abnormal effectNotInUse = null;

		if (skill.isDebuff())
		{
			if (this.debuffs == null)
			{
				return null;
			}

			//synchronized (this.debuffs)
			{
				if (this.debuffs.isEmpty())
				{
					return null;
				}

				for (L2Abnormal e : this.debuffs)
				{
					if (e == null)
					{
						continue;
					}
					if (e.getSkill() == skill)
					{
						if (e.getInUse())
						{
							return e;
						}
						else
						{
							effectNotInUse = e;
						}
					}
				}
			}
			return effectNotInUse;
		}
		else
		{
			if (this.buffs == null)
			{
				return null;
			}

			//synchronized (this.buffs)
			{
				if (this.buffs.isEmpty())
				{
					return null;
				}

				for (L2Abnormal e : this.buffs)
				{
					if (e == null)
					{
						continue;
					}
					if (e.getSkill() == skill)
					{
						if (e.getInUse())
						{
							return e;
						}
						else
						{
							effectNotInUse = e;
						}
					}
				}
			}
			return effectNotInUse;
		}
	}

	/**
	 * Returns the first effect matching the given skillId
	 *
	 * @return
	 */
	public final L2Abnormal getFirstEffect(int skillId)
	{
		L2Abnormal effectNotInUse = null;

		if (this.buffs != null)
		{
			//synchronized (this.buffs)
			{
				if (!this.buffs.isEmpty())
				{
					for (L2Abnormal e : this.buffs)
					{
						if (e == null)
						{
							continue;
						}
						if (e.getSkill().getId() == skillId)
						{
							if (e.getInUse())
							{
								return e;
							}
							else
							{
								effectNotInUse = e;
							}
						}
					}
				}
			}
		}

		if (effectNotInUse == null && this.debuffs != null)
		{
			//synchronized (this.debuffs)
			{
				if (!this.debuffs.isEmpty())
				{
					for (L2Abnormal e : this.debuffs)
					{
						if (e == null)
						{
							continue;
						}
						if (e.getSkill().getId() == skillId)
						{
							if (e.getInUse())
							{
								return e;
							}
							else
							{
								effectNotInUse = e;
							}
						}
					}
				}
			}
		}

		return effectNotInUse;
	}

	public final L2Abnormal getFirstEffectByName(String effectName)
	{
		L2Abnormal effectNotInUse = null;
		if (this.buffs != null)
		{
			//synchronized (this.buffs)
			{
				if (!this.buffs.isEmpty())
				{
					for (L2Abnormal e : this.buffs)
					{
						if (e == null)
						{
							continue;
						}

						for (L2Effect eff : e.getEffects())
						{
							if (eff == null)
							{
								continue;
							}

							if (eff.getTemplate().funcName.equalsIgnoreCase(effectName))
							{
								if (e.getInUse())
								{
									return e;
								}
								else
								{
									effectNotInUse = e;
								}
							}
						}
					}
				}
			}
		}

		if (effectNotInUse == null && this.debuffs != null)
		{
			//synchronized (this.debuffs)
			{
				if (!this.debuffs.isEmpty())
				{
					for (L2Abnormal e : this.debuffs)
					{
						if (e == null)
						{
							continue;
						}

						for (L2Effect eff : e.getEffects())
						{
							if (eff == null)
							{
								continue;
							}

							if (eff.getTemplate().funcName.equalsIgnoreCase(effectName))
							{
								if (e.getInUse())
								{
									return e;
								}
								else
								{
									effectNotInUse = e;
								}
							}
						}
					}
				}
			}
		}
		return effectNotInUse;
	}

	/**
	 * Returns the first effect matching the given skillId
	 *
	 * @return
	 */
	public final L2Abnormal getFirstEffect(final String stackType)
	{
		L2Abnormal effectNotInUse = null;

		if (this.buffs != null)
		{
			//synchronized (this.buffs)
			{
				if (!this.buffs.isEmpty())
				{
					for (L2Abnormal e : this.buffs)
					{
						if (e == null)
						{
							continue;
						}
						if (e.getSkill().getFirstEffectStack().equals(stackType))
						{
							if (e.getInUse())
							{
								return e;
							}
							else
							{
								effectNotInUse = e;
							}
						}
					}
				}
			}
		}

		if (effectNotInUse == null && this.debuffs != null)
		{
			//synchronized (this.debuffs)
			{
				if (!this.debuffs.isEmpty())
				{
					for (L2Abnormal e : this.debuffs)
					{
						if (e == null)
						{
							continue;
						}
						if (e.getSkill().getFirstEffectStack().equals(stackType))
						{
							if (e.getInUse())
							{
								return e;
							}
							else
							{
								effectNotInUse = e;
							}
						}
					}
				}
			}
		}

		return effectNotInUse;
	}

	/**
	 * Return the number of buffs in this CharEffectList not counting Songs/Dances
	 *
	 * @return
	 */
	public int getBuffCount()
	{
		if (this.buffs == null)
		{
			return 0;
		}
		int buffCount = 0;

		//synchronized(this.buffs)
		{
			if (this.buffs.isEmpty())
			{
				return 0;
			}

			for (L2Abnormal e : this.buffs)
			{
				if (e != null && e.getShowIcon() && !e.getSkill().isDance() && !e.getSkill().isToggle() &&
						!e.getSkill().isActivation() && !e.getSkill().is7Signs())
				{
					switch (e.getSkill().getSkillType())
					{
						case BUFF:
						case HEAL_PERCENT:
						case MANAHEAL_PERCENT:
							buffCount++;
					}
				}
			}
		}
		return buffCount;
	}

	/**
	 * Return the number of Songs/Dances in this CharEffectList
	 *
	 * @return
	 */
	public int getDanceCount()
	{
		if (this.buffs == null)
		{
			return 0;
		}
		int danceCount = 0;

		//synchronized(this.buffs)
		{
			if (this.buffs.isEmpty())
			{
				return 0;
			}

			for (L2Abnormal e : this.buffs)
			{
				if (e != null && e.getSkill().isDance() && e.getInUse())
				{
					danceCount++;
				}
			}
		}
		return danceCount;
	}

	/**
	 * Return the number of Activation buffs in this CharEffectList
	 *
	 * @return
	 */
	public int getActivationCount()
	{
		if (this.buffs == null)
		{
			return 0;
		}
		int danceCount = 0;

		//synchronized(this.buffs)
		{
			if (this.buffs.isEmpty())
			{
				return 0;
			}

			for (L2Abnormal e : this.buffs)
			{
				if (e != null && e.getSkill().isActivation() && e.getInUse())
				{
					danceCount++;
				}
			}
		}
		return danceCount;
	}

	/**
	 * Exits all effects in this CharEffectList
	 */
	public final void stopAllEffects()
	{
		// Get all active skills effects from this list
		L2Abnormal[] effects = getAllEffects();

		// Exit them
		for (L2Abnormal e : effects)
		{
			if (e != null)
			{
				e.exit(true);
			}
		}
	}

	/**
	 * Exits all effects in this CharEffectList
	 */
	public final void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		// Get all active skills effects from this list
		L2Abnormal[] effects = getAllEffects();

		// Exit them
		for (L2Abnormal e : effects)
		{
			if (e != null && !e.getSkill().isStayAfterDeath())
			{
				e.exit(true);
			}
		}
	}

	/**
	 * Exit all toggle-type effects
	 */
	public void stopAllToggles()
	{
		if (this.buffs != null)
		{
			//synchronized (this.buffs)
			{
				if (!this.buffs.isEmpty())
				{
					this.buffs.stream().filter(e -> e != null && e.getSkill().isToggle()).forEachOrdered(L2Abnormal::exit);
				}
			}
		}
	}

	/**
	 * Exit all effects having a specified type
	 *
	 * @param type
	 */
	public final void stopEffects(L2AbnormalType type)
	{
		// Go through all active skills effects
		ArrayList<L2Abnormal> temp = new ArrayList<>();
		if (this.buffs != null)
		{
			//synchronized (this.buffs)
			{
				if (!this.buffs.isEmpty())
				{
					temp.addAll(
							this.buffs.stream().filter(e -> e != null && e.getType() == type).collect(Collectors.toList()));
				}
			}
		}
		if (this.debuffs != null)
		{
			//synchronized (this.debuffs)
			{
				if (!this.debuffs.isEmpty())
				{
					temp.addAll(this.debuffs.stream().filter(e -> e != null && e.getType() == type)
							.collect(Collectors.toList()));
				}
			}
		}
		if (!temp.isEmpty())
		{
			temp.stream().filter(e -> e != null).forEachOrdered(L2Abnormal::exit);
		}
	}

	/**
	 * Exit all effects having a specified type
	 *
	 * @param type
	 */
	public final void stopEffects(L2EffectType type)
	{
		// Go through all active skills effects
		ArrayList<L2Abnormal> temp = new ArrayList<>();
		if (this.buffs != null)
		{
			//synchronized (this.buffs)
			{
				if (!this.buffs.isEmpty())
				{
					temp.addAll(this.buffs.stream().filter(e -> e != null && (e.getEffectMask() & type.getMask()) > 0)
							.collect(Collectors.toList()));
				}
			}
		}
		if (this.debuffs != null)
		{
			//synchronized (this.debuffs)
			{
				if (!this.debuffs.isEmpty())
				{
					temp.addAll(this.debuffs.stream().filter(e -> e != null && (e.getEffectMask() & type.getMask()) > 0)
							.collect(Collectors.toList()));
				}
			}
		}
		if (!temp.isEmpty())
		{
			temp.stream().filter(e -> e != null).forEachOrdered(L2Abnormal::exit);
		}
	}

	/**
	 * Exits all effects created by a specific skillId
	 *
	 * @param skillId
	 */
	public final void stopSkillEffects(int skillId)
	{
		// Go through all active skills effects
		ArrayList<L2Abnormal> temp = new ArrayList<>();
		if (this.buffs != null)
		{
			//synchronized (this.buffs)
			{
				if (!this.buffs.isEmpty())
				{
					temp.addAll(this.buffs.stream().filter(e -> e != null && e.getSkill().getId() == skillId)
							.collect(Collectors.toList()));
				}
			}
		}
		if (this.debuffs != null)
		{
			//synchronized (this.debuffs)
			{
				if (!this.debuffs.isEmpty())
				{
					temp.addAll(this.debuffs.stream().filter(e -> e != null && e.getSkill().getId() == skillId)
							.collect(Collectors.toList()));
				}
			}
		}
		if (!temp.isEmpty())
		{
			temp.stream().filter(e -> e != null).forEachOrdered(L2Abnormal::exit);
		}
	}

	/**
	 * Exits all buffs effects of the skills with "removedOnAnyAction" set.
	 * Called on any action except movement (attack, cast).
	 */
	public void stopEffectsOnAction(L2Skill skill)
	{
		boolean friendlyAction = skill != null && (skill.getTargetType() == L2SkillTargetType.TARGET_SELF ||
				skill.getTargetType() == L2SkillTargetType.TARGET_FRIENDS);
		if (this.hasBuffsRemovedOnAction && this.buffs != null && !this.buffs.isEmpty())
		{
			for (L2Abnormal e : this.buffs)
			{
				if (e == null || !e.getSkill().isRemovedOnAction() || friendlyAction)
				{
					continue;
				}

				e.exit(true);
			}
		}
	}

	public void stopEffectsOnDamage(boolean awake, int damage)
	{
		if (this.hasBuffsRemovedOnDamage && this.buffs != null && !this.buffs.isEmpty())
		{
			for (L2Abnormal e : this.buffs)
			{
				if (e != null && awake)
				{
					if (e.getType() == L2AbnormalType.FEAR || e.getType() == L2AbnormalType.SLEEP)
					{
						continue;
					}

					if (e.isRemovedOnDamage(damage))
					{
						e.exit(true);
					}
				}
			}
		}

		if (this.hasDebuffsRemovedOnDamage && this.debuffs != null && !this.debuffs.isEmpty())
		{
			for (L2Abnormal e : this.debuffs)
			{
				if (e != null && awake)
				{
					if (e.isRemovedOnDamage(damage))
					{
						if (e.getSkill().getRemovedOnDamageChance() != 0)
						{
							if (e.getSkill().getRemovedOnDamageChance() >= Rnd.get(100))
							{
								e.exit(true);
							}
							return;
						}
						e.exit(true);
					}
				}
			}
		}
	}

	public void stopEffectsOnDebuffBlock()
	{
		if (this.hasBuffsRemovedOnDebuffBlock && this.buffs != null && !this.buffs.isEmpty())
		{
			this.buffs.stream().filter(e -> e != null).filter(e -> e.isRemovedOnDebuffBlock(true)).forEachOrdered(e ->
			{
				e.exit(true);
			});
		}
	}

	public void updateEffectIcons(boolean partyOnly)
	{
		if (this.buffs == null && this.debuffs == null)
		{
			return;
		}

		if (partyOnly)
		{
			this.partyOnly = true;
		}

		queueRunner();
	}

	public void queueEffect(L2Abnormal effect, boolean remove)
	{
		if (effect == null)
		{
			return;
		}

		if (!this.queuesInitialized)
		{
			init();
		}

		if (remove)
		{
			this.removeQueue.offer(effect);
		}
		else
		{
			this.addQueue.offer(effect);
		}

		queueRunner();
	}

	private synchronized void init()
	{
		if (this.queuesInitialized)
		{
			return;
		}

		this.addQueue = new LinkedBlockingQueue<>();
		this.removeQueue = new LinkedBlockingQueue<>();
		this.queuesInitialized = true;
	}

	private void queueRunner()
	{
		if (!this.queueLock.compareAndSet(true, false))
		{
			return;
		}

		try
		{
			L2Abnormal effect;
			do
			{
				// remove has more priority than add
				// so removing all effects from queue first
				while ((effect = this.removeQueue.poll()) != null)
				{
					removeEffectFromQueue(effect);
					this.partyOnly = false;
				}

				if ((effect = this.addQueue.poll()) != null)
				{
					addEffectFromQueue(effect);
					this.partyOnly = false;
				}
			}
			while (!this.addQueue.isEmpty() || !this.removeQueue.isEmpty());

			computeEffectFlags();
			updateEffectIcons();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			this.queueLock.set(true);
		}
	}

	protected void removeEffectFromQueue(L2Abnormal effect)
	{
		if (effect == null)
		{
			return;
		}

		CopyOnWriteArrayList<L2Abnormal> effectList;

		if (effect.getSkill().isDebuff())
		{
			if (this.debuffs == null)
			{
				return;
			}
			effectList = this.debuffs;
		}
		else
		{
			if (this.buffs == null)
			{
				return;
			}
			effectList = this.buffs;
		}

		if (effect.getStackType().length == 0)
		{
			// Remove Func added by this effect from the L2Character Calculator
			this.owner.removeStatsOwner(effect);
		}
		else
		{
			if (this.stackedEffects == null)
			{
				return;
			}

			for (String stackType : effect.getStackType())
			{
				// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
				List<L2Abnormal> stackQueue = this.stackedEffects.get(stackType);

				if (stackQueue == null || stackQueue.isEmpty())
				{
					continue;
				}

				int index = stackQueue.indexOf(effect);

				// Remove the effect from the stack group
				if (index >= 0)
				{
					stackQueue.remove(effect);
					// Check if the first stacked effect was the effect to remove
					if (index == 0)
					{
						// Remove all its Func objects from the L2Character calculator set
						this.owner.removeStatsOwner(effect);

						// Check if there's another effect in the Stack Group
						if (!stackQueue.isEmpty())
						{
							L2Abnormal newStackedEffect = listsContains(stackQueue.get(0));
							if (newStackedEffect != null)
							{
								// Set the effect to In Use
								if (newStackedEffect.setInUse(true))
								// Add its list of Funcs to the Calculator set of the L2Character
								{
									this.owner.addStatFuncs(newStackedEffect.getStatFuncs());
								}
							}
						}
					}
					if (stackQueue.isEmpty())
					{
						this.stackedEffects.remove(stackType);
					}
					else
					{
						// Update the Stack Group table this.stackedEffects of the L2Character
						this.stackedEffects.put(stackType, stackQueue);
					}
				}
			}
		}

		// Remove the active skill L2effect from this.effects of the L2Character
		boolean removed = effectList.remove(effect);
		if (removed && this.owner instanceof L2PcInstance && effect.getShowIcon())
		{
			SystemMessage sm;
			if (effect.getSkill().isToggle())
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ABORTED);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED);
			}
			sm.addSkillName(effect);
			this.owner.sendPacket(sm);
		}
	}

	protected void addEffectFromQueue(L2Abnormal newEffect)
	{
		if (newEffect == null)
		{
			return;
		}

		L2Skill newSkill = newEffect.getSkill();

		if (newSkill.isDebuff())
		{
			if (this.debuffs == null)
			{
				this.debuffs = new CopyOnWriteArrayList<>();
			}

			for (L2Abnormal e : this.debuffs)
			{
				if (e != null && e.getSkill().getId() == newEffect.getSkill().getId() &&
						e.getType() == newEffect.getType() && e.getStackLvl() == newEffect.getStackLvl() &&
						Arrays.equals(e.getStackType(), newEffect.getStackType()))
				{
					// Started scheduled timer needs to be canceled.
					if (newEffect.getDuration() - newEffect.getTime() < e.getDuration() - e.getTime())
					{
						newEffect.stopEffectTask();
						return;
					}
					else
					{
						e.stopEffectTask();
					}
				}
			}
			this.debuffs.add(newEffect);
		}
		else
		{
			if (this.buffs == null)
			{
				this.buffs = new CopyOnWriteArrayList<>();
			}

			for (L2Abnormal e : this.buffs)
			{
				if (e != null && e.getSkill().getId() == newEffect.getSkill().getId() &&
						e.getType() == newEffect.getType() && e.getStackLvl() == newEffect.getStackLvl())
				{
					boolean sameStackType = e.getStackType().length == newEffect.getStackType().length;
					if (sameStackType)
					{
						for (int i = 0; i < e.getStackType().length; i++)
						{
							if (!e.getStackType()[i].equals(newEffect.getStackType()[i]))
							{
								sameStackType = false;
								break;
							}
						}
					}

					if (sameStackType)
					{
						e.exit(); // exit this
					}
				}
			}

			// if max buffs, no herb effects are used, even if they would replace one old
			if (newEffect.isHerbEffect() && getBuffCount() >= this.owner.getMaxBuffCount())
			{
				newEffect.stopEffectTask();
				return;
			}

			// Remove first buff when buff list is full
			int effectsToRemove;
			if (newSkill.isActivation())
			{
				effectsToRemove = getActivationCount() - 24;
				if (effectsToRemove >= 0)
				{
					for (L2Abnormal e : this.buffs)
					{
						if (e == null || !e.getSkill().isActivation())
						{
							continue;
						}

						// get first dance
						e.exit();
						effectsToRemove--;
						if (effectsToRemove < 0)
						{
							break;
						}
					}
				}
			}
			else if (newSkill.isDance())
			{
				effectsToRemove = getDanceCount() - Config.DANCES_MAX_AMOUNT;
				if (effectsToRemove >= 0)
				{
					for (L2Abnormal e : this.buffs)
					{
						if (e == null || !e.getSkill().isDance())
						{
							continue;
						}

						// get first dance
						e.exit();
						effectsToRemove--;
						if (effectsToRemove < 0)
						{
							break;
						}
					}
				}
			}
			else if (!newSkill.isToggle())
			{
				effectsToRemove = getBuffCount() - this.owner.getMaxBuffCount();
				if (effectsToRemove >= 0)
				{
					switch (newSkill.getSkillType())
					{
						case BUFF:
						case HEAL_PERCENT:
						case MANAHEAL_PERCENT:
							for (L2Abnormal e : this.buffs)
							{
								if (e == null || e.getSkill().isDance())
								{
									continue;
								}

								switch (e.getSkill().getSkillType())
								{
									case BUFF:
									case HEAL_PERCENT:
									case MANAHEAL_PERCENT:
										e.exit();
										effectsToRemove--;
										break; // break switch()
									default:
										continue; // continue for ()
								}
								if (effectsToRemove < 0)
								{
									break; // break for ()
								}
							}
					}
				}
			}

			// Icons order: buffs, 7s, toggles, dances, activation
			if (newSkill.isActivation())
			{
				this.buffs.add(newEffect);
			}
			else
			{
				int pos = 0;
				if (newSkill.isDance())
				{
					// toggle skill - before all dances
					for (L2Abnormal e : this.buffs)
					{
						if (e == null)
						{
							continue;
						}
						if (e.getSkill().isActivation())
						{
							break;
						}
						pos++;
					}
				}
				else if (newSkill.isToggle())
				{
					// toggle skill - before all dances
					for (L2Abnormal e : this.buffs)
					{
						if (e == null)
						{
							continue;
						}
						if (e.getSkill().isDance() || e.getSkill().isActivation())
						{
							break;
						}
						pos++;
					}
				}
				else
				{
					// normal buff - before toggles and 7s and dances
					for (L2Abnormal e : this.buffs)
					{
						if (e == null)
						{
							continue;
						}
						if (e.getSkill().isToggle() || e.getSkill().is7Signs() || e.getSkill().isDance() ||
								e.getSkill().isActivation())
						{
							break;
						}
						pos++;
					}
				}
				this.buffs.add(pos, newEffect);
			}
		}

		// Check if a stack group is defined for this effect
		if (newEffect.getStackType().length == 0)
		{
			// Set this L2Effect to In Use
			if (newEffect.setInUse(true))
			// Add Funcs of this effect to the Calculator set of the L2Character
			{
				this.owner.addStatFuncs(newEffect.getStatFuncs());
			}
			else
			{
				if (newEffect.getSkill().isDebuff())
				{
					this.debuffs.remove(newEffect);
				}
				else
				{
					this.buffs.remove(newEffect);
				}
			}

			return;
		}

		if (this.stackedEffects == null)
		{
			this.stackedEffects = new HashMap<>();
		}

		Set<L2Abnormal> effectsToAdd = new HashSet<>();
		Set<L2Abnormal> effectsToRemove = new HashSet<>();
		Set<L2Abnormal> removed = new HashSet<>();
		for (String stackType : newEffect.getStackType())
		{
			L2Abnormal effectToAdd = null;
			L2Abnormal effectToRemove = null;
			// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
			List<L2Abnormal> stackQueue = this.stackedEffects.get(stackType);
			if (stackQueue != null)
			{
				int pos = 0;
				if (!stackQueue.isEmpty())
				{
					// Get the first stacked effect of the Stack group selected
					effectToRemove = listsContains(stackQueue.get(0));

					// Create an Iterator to go through the list of stacked effects in progress on the L2Character
					Iterator<L2Abnormal> queueIterator = stackQueue.iterator();

					while (queueIterator.hasNext())
					{
						if (newEffect.getStackLvl() < queueIterator.next().getStackLvl())
						{
							pos++;
						}
						else
						{
							break;
						}
					}
					// Add the new effect to the Stack list in function of its position in the Stack group
					stackQueue.add(pos, newEffect);

					// skill.exit() could be used, if the users don't wish to see "effect
					// removed" always when a timer goes off, even if the buff isn't active
					// any more (has been replaced). but then check e.g. npc hold and raid petrification.
					if (Config.EFFECT_CANCELING && !newEffect.isHerbEffect() && stackQueue.size() > 1)
					{
						L2Abnormal toRemove = stackQueue.remove(1);
						if (!removed.contains(toRemove))
						{
							removed.add(toRemove);
							if (newSkill.isDebuff())
							{
								this.debuffs.remove(toRemove);
							}
							else
							{
								this.buffs.remove(toRemove);
							}
							toRemove.exit();
						}
					}
				}
				else
				{
					stackQueue.add(0, newEffect);
				}
			}
			else
			{
				stackQueue = new ArrayList<>();
				stackQueue.add(0, newEffect);
			}

			// Update the Stack Group table this.stackedEffects of the L2Character
			this.stackedEffects.put(stackType, stackQueue);

			// Get the first stacked effect of the Stack group selected
			if (!stackQueue.isEmpty())
			{
				effectToAdd = listsContains(stackQueue.get(0));
			}

			if (effectToRemove != effectToAdd)
			{
				if (effectToRemove != null)
				{
					effectsToRemove.add(effectToRemove);
				}
				if (effectToAdd != null)
				{
					effectsToAdd.add(effectToAdd);
				}
			}
		}

		for (L2Abnormal a : effectsToRemove)
		{
			// Set the L2Effect to Not In Use
			a.setInUse(false);
		}

		for (L2Abnormal a : effectsToAdd)
		{
			// To be added it must be first in all its stack types
			boolean firstInAll = true;
			for (String stackType : a.getStackType())
			{
				if (this.stackedEffects.get(stackType).get(0) != a)
				{
					firstInAll = false;
					break;
				}
			}

			if (firstInAll)
			{
				// Set this L2Effect to In Use
				if (a.setInUse(true))
				// Add all Func objects corresponding to this stacked effect to the Calculator set of the L2Character
				{
					this.owner.addStatFuncs(a.getStatFuncs());
				}
				else
				{
					if (a.getSkill().isDebuff())
					{
						this.debuffs.remove(a);
					}
					else
					{
						this.buffs.remove(a);
					}
				}
			}
			else
			{
				// Remove it from the stack
				for (String stackType : a.getStackType())
				{
					this.stackedEffects.get(stackType).remove(a);
				}
			}
		}

		for (L2Abnormal a : effectsToRemove)
		{
			// Remove all Func objects corresponding to this stacked effect from the Calculator set of the L2Character
			this.owner.removeStatsOwner(a);
		}
	}

	protected void updateEffectIcons()
	{
		if (this.owner == null)
		{
			return;
		}

		this.owner.broadcastAbnormalStatusUpdate();

		if (!(this.owner instanceof L2Playable))
		{
			updateEffectFlags();
			return;
		}

		AbnormalStatusUpdate mi = null;
		PartySpelled ps = null;
		ExOlympiadSpelledInfo os = null;

		if (this.owner instanceof L2PcInstance)
		{
			if (this.partyOnly)
			{
				this.partyOnly = false;
			}
			else
			{
				mi = new AbnormalStatusUpdate();
			}

			if (this.owner.isInParty())
			{
				ps = new PartySpelled(this.owner);
			}

			if (((L2PcInstance) this.owner).isInOlympiadMode() && ((L2PcInstance) this.owner).isOlympiadStart())
			{
				os = new ExOlympiadSpelledInfo((L2PcInstance) this.owner);
			}
		}
		else if (this.owner instanceof L2Summon)
		{
			ps = new PartySpelled(this.owner);
		}

		boolean foundRemovedOnAction = false;
		boolean foundRemovedOnDamage = false;
		boolean foundRemovedOnDebuffBlock = false;

		if (this.buffs != null && !this.buffs.isEmpty())
		{
			//synchronized (this.buffs)
			{
				for (L2Abnormal e : this.buffs)
				{
					if (e == null)
					{
						continue;
					}

					if (e.getSkill().isRemovedOnAction())
					{
						foundRemovedOnAction = true;
					}
					if (e.isRemovedOnDamage(0))
					{
						foundRemovedOnDamage = true;
					}
					if (e.isRemovedOnDebuffBlock(false))
					{
						foundRemovedOnDebuffBlock = true;
					}

					if (!e.getShowIcon())
					{
						continue;
					}

					switch (e.getType())
					{
						case CHARGE: // handled by EtcStatusUpdate
						case SIGNET_GROUND:
							continue;
					}

					if (e.getInUse())
					{
						if (mi != null)
						{
							e.addIcon(mi);
						}

						if (ps != null)
						{
							e.addPartySpelledIcon(ps);
						}

						if (os != null)
						{
							e.addOlympiadSpelledIcon(os);
						}
					}
				}
			}
		}

		this.hasBuffsRemovedOnAction = foundRemovedOnAction;
		this.hasBuffsRemovedOnDamage = foundRemovedOnDamage;
		this.hasBuffsRemovedOnDebuffBlock = foundRemovedOnDebuffBlock;
		foundRemovedOnDamage = false;

		if (this.debuffs != null && !this.debuffs.isEmpty())
		{
			//synchronized (this.debuffs)
			{
				for (L2Abnormal e : this.debuffs)
				{
					if (e == null)
					{
						continue;
					}

					if (e.isRemovedOnDamage(0))
					{
						foundRemovedOnDamage = true;
					}

					if (!e.getShowIcon())
					{
						continue;
					}

					switch (e.getType())
					{
						case SIGNET_GROUND:
							continue;
					}

					if (e.getInUse())
					{
						if (mi != null)
						{
							e.addIcon(mi);
						}

						if (ps != null)
						{
							e.addPartySpelledIcon(ps);
						}

						if (os != null)
						{
							e.addOlympiadSpelledIcon(os);
						}
					}
				}
			}
		}

		this.hasDebuffsRemovedOnDamage = foundRemovedOnDamage;

		if (mi != null)
		{
			this.owner.sendPacket(mi);
		}

		if (ps != null)
		{
			if (this.owner instanceof L2Summon)
			{
				L2PcInstance summonOwner = ((L2Summon) this.owner).getOwner();

				if (summonOwner != null)
				{
					if (summonOwner.isInParty())
					{
						summonOwner.getParty().broadcastToPartyMembers(ps);
					}
					else
					{
						summonOwner.sendPacket(ps);
					}
				}
			}
			else if (this.owner instanceof L2PcInstance && this.owner.isInParty())
			{
				this.owner.getParty().broadcastToPartyMembers(ps);
			}
		}

		if (os != null)
		{
			final OlympiadGameTask game =
					OlympiadGameManager.getInstance().getOlympiadTask(((L2PcInstance) this.owner).getOlympiadGameId());
			if (game != null && game.isBattleStarted())
			{
				game.getZone().broadcastPacketToObservers(os, game.getGame().getGameId());
			}
		}
	}

	protected void updateEffectFlags()
	{
		boolean foundRemovedOnAction = false;
		boolean foundRemovedOnDamage = false;
		boolean foundRemovedOnDebuffBlock = false;

		if (this.buffs != null && !this.buffs.isEmpty())
		{
			//synchronized (this.buffs)
			{
				for (L2Abnormal e : this.buffs)
				{
					if (e == null)
					{
						continue;
					}

					if (e.getSkill().isRemovedOnAction())
					{
						foundRemovedOnAction = true;
					}
					if (e.isRemovedOnDamage(0))
					{
						foundRemovedOnDamage = true;
					}
					if (e.isRemovedOnDebuffBlock(false))
					{
						foundRemovedOnDebuffBlock = true;
					}
				}
			}
		}
		this.hasBuffsRemovedOnAction = foundRemovedOnAction;
		this.hasBuffsRemovedOnDamage = foundRemovedOnDamage;
		this.hasBuffsRemovedOnDebuffBlock = foundRemovedOnDebuffBlock;
		foundRemovedOnDamage = false;

		if (this.debuffs != null && !this.debuffs.isEmpty())
		{
			//synchronized (this.debuffs)
			{
				for (L2Abnormal e : this.debuffs)
				{
					if (e == null)
					{
						continue;
					}

					if (e.isRemovedOnDamage(0))
					{
						foundRemovedOnDamage = true;
					}
				}
			}
		}
		this.hasDebuffsRemovedOnDamage = foundRemovedOnDamage;
	}

	/**
	 * Returns effect if contains in this.buffs or this.debuffs and null if not found
	 *
	 * @param effect
	 * @return
	 */
	private L2Abnormal listsContains(L2Abnormal effect)
	{
		if (this.buffs != null && !this.buffs.isEmpty() && this.buffs.contains(effect))
		{
			return effect;
		}
		if (this.debuffs != null && !this.debuffs.isEmpty() && this.debuffs.contains(effect))
		{
			return effect;
		}
		return null;
	}

	/**
	 * Recalculate effect bits flag.<br>
	 * Please no concurrency access
	 */
	private void computeEffectFlags()
	{
		int flags = 0;

		if (this.buffs != null)
		{
			for (L2Abnormal e : this.buffs)
			{
				if (e == null)
				{
					continue;
				}
				flags |= e.getEffectMask();
			}
		}

		if (this.debuffs != null)
		{
			for (L2Abnormal e : this.debuffs)
			{
				if (e == null)
				{
					continue;
				}
				flags |= e.getEffectMask();
			}
		}

		this.effectFlags = flags;
	}

	/**
	 * Check if target is affected with special buff
	 *
	 * @param bitFlag flag of special buff
	 * @return boolean true if affected
	 */
	public boolean isAffected(long bitFlag)
	{
		return (this.effectFlags & bitFlag) != 0;
	}

	/**
	 * Clear and null all queues and lists
	 * Use only during delete character from the world.
	 */
	public void clear()
	{
		try
		{
			if (this.addQueue != null)
			{
				this.addQueue.clear();
				this.addQueue = null;
			}
			if (this.removeQueue != null)
			{
				this.removeQueue.clear();
				this.removeQueue = null;
			}
			this.queuesInitialized = false;

			if (this.buffs != null)
			{
				this.buffs.clear();
				this.buffs = null;
			}
			if (this.debuffs != null)
			{
				this.debuffs.clear();
				this.debuffs = null;
			}

			if (this.stackedEffects != null)
			{
				this.stackedEffects.clear();
				this.stackedEffects = null;
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "", e);
		}
	}
}
