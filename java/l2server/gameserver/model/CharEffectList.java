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

	private AtomicBoolean _queueLock = new AtomicBoolean(true);

	private CopyOnWriteArrayList<L2Abnormal> _buffs;
	private CopyOnWriteArrayList<L2Abnormal> _debuffs;

	// The table containing the List of all stacked effect in progress for each Stack group Identifier
	private Map<String, List<L2Abnormal>> _stackedEffects;

	private volatile boolean _hasBuffsRemovedOnAction = false;
	private volatile boolean _hasBuffsRemovedOnDamage = false;
	private volatile boolean _hasDebuffsRemovedOnDamage = false;
	private volatile boolean _hasBuffsRemovedOnDebuffBlock = false;

	private boolean _queuesInitialized = false;
	private LinkedBlockingQueue<L2Abnormal> _addQueue;
	private LinkedBlockingQueue<L2Abnormal> _removeQueue;
	private long _effectFlags;

	// only party icons need to be updated
	private boolean _partyOnly = false;

	// Owner of this list
	private L2Character _owner;

	public CharEffectList(L2Character owner)
	{
		_owner = owner;
	}

	/**
	 * Returns all effects affecting stored in this CharEffectList
	 *
	 * @return
	 */
	public final L2Abnormal[] getAllEffects()
	{
		// If no effect is active, return EMPTY_EFFECTS
		if ((_buffs == null || _buffs.isEmpty()) && (_debuffs == null || _debuffs.isEmpty()))
		{
			return EMPTY_EFFECTS;
		}

		// Create a copy of the effects
		ArrayList<L2Abnormal> temp = new ArrayList<>();

		// Add all buffs and all debuffs
		if (_buffs != null)
		{
			//synchronized (_buffs)
			{
				if (!_buffs.isEmpty())
				{
					temp.addAll(_buffs);
				}
			}
		}
		if (_debuffs != null)
		{
			//synchronized (_debuffs)
			{
				if (!_debuffs.isEmpty())
				{
					temp.addAll(_debuffs);
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
		if (_debuffs == null || _debuffs.isEmpty())
		{
			return EMPTY_EFFECTS;
		}

		// Create a copy of the effects
		ArrayList<L2Abnormal> temp = new ArrayList<>();

		if (_debuffs != null)
		{
			if (!_debuffs.isEmpty())
			{
				temp.addAll(_debuffs);
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

		if (_buffs != null)
		{
			//synchronized (_buffs)
			{
				if (!_buffs.isEmpty())
				{
					for (L2Abnormal e : _buffs)
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
		if (effectNotInUse == null && _debuffs != null)
		{
			//synchronized (_debuffs)
			{
				if (!_debuffs.isEmpty())
				{
					for (L2Abnormal e : _debuffs)
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
			if (_debuffs == null)
			{
				return null;
			}

			//synchronized (_debuffs)
			{
				if (_debuffs.isEmpty())
				{
					return null;
				}

				for (L2Abnormal e : _debuffs)
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
			if (_buffs == null)
			{
				return null;
			}

			//synchronized (_buffs)
			{
				if (_buffs.isEmpty())
				{
					return null;
				}

				for (L2Abnormal e : _buffs)
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

		if (_buffs != null)
		{
			//synchronized (_buffs)
			{
				if (!_buffs.isEmpty())
				{
					for (L2Abnormal e : _buffs)
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

		if (effectNotInUse == null && _debuffs != null)
		{
			//synchronized (_debuffs)
			{
				if (!_debuffs.isEmpty())
				{
					for (L2Abnormal e : _debuffs)
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
		if (_buffs != null)
		{
			//synchronized (_buffs)
			{
				if (!_buffs.isEmpty())
				{
					for (L2Abnormal e : _buffs)
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

		if (effectNotInUse == null && _debuffs != null)
		{
			//synchronized (_debuffs)
			{
				if (!_debuffs.isEmpty())
				{
					for (L2Abnormal e : _debuffs)
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

		if (_buffs != null)
		{
			//synchronized (_buffs)
			{
				if (!_buffs.isEmpty())
				{
					for (L2Abnormal e : _buffs)
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

		if (effectNotInUse == null && _debuffs != null)
		{
			//synchronized (_debuffs)
			{
				if (!_debuffs.isEmpty())
				{
					for (L2Abnormal e : _debuffs)
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
		if (_buffs == null)
		{
			return 0;
		}
		int buffCount = 0;

		//synchronized(_buffs)
		{
			if (_buffs.isEmpty())
			{
				return 0;
			}

			for (L2Abnormal e : _buffs)
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
		if (_buffs == null)
		{
			return 0;
		}
		int danceCount = 0;

		//synchronized(_buffs)
		{
			if (_buffs.isEmpty())
			{
				return 0;
			}

			for (L2Abnormal e : _buffs)
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
		if (_buffs == null)
		{
			return 0;
		}
		int danceCount = 0;

		//synchronized(_buffs)
		{
			if (_buffs.isEmpty())
			{
				return 0;
			}

			for (L2Abnormal e : _buffs)
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
		if (_buffs != null)
		{
			//synchronized (_buffs)
			{
				if (!_buffs.isEmpty())
				{
					_buffs.stream().filter(e -> e != null && e.getSkill().isToggle()).forEachOrdered(L2Abnormal::exit);
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
		if (_buffs != null)
		{
			//synchronized (_buffs)
			{
				if (!_buffs.isEmpty())
				{
					temp.addAll(
							_buffs.stream().filter(e -> e != null && e.getType() == type).collect(Collectors.toList()));
				}
			}
		}
		if (_debuffs != null)
		{
			//synchronized (_debuffs)
			{
				if (!_debuffs.isEmpty())
				{
					temp.addAll(_debuffs.stream().filter(e -> e != null && e.getType() == type)
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
		if (_buffs != null)
		{
			//synchronized (_buffs)
			{
				if (!_buffs.isEmpty())
				{
					temp.addAll(_buffs.stream().filter(e -> e != null && (e.getEffectMask() & type.getMask()) > 0)
							.collect(Collectors.toList()));
				}
			}
		}
		if (_debuffs != null)
		{
			//synchronized (_debuffs)
			{
				if (!_debuffs.isEmpty())
				{
					temp.addAll(_debuffs.stream().filter(e -> e != null && (e.getEffectMask() & type.getMask()) > 0)
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
		if (_buffs != null)
		{
			//synchronized (_buffs)
			{
				if (!_buffs.isEmpty())
				{
					temp.addAll(_buffs.stream().filter(e -> e != null && e.getSkill().getId() == skillId)
							.collect(Collectors.toList()));
				}
			}
		}
		if (_debuffs != null)
		{
			//synchronized (_debuffs)
			{
				if (!_debuffs.isEmpty())
				{
					temp.addAll(_debuffs.stream().filter(e -> e != null && e.getSkill().getId() == skillId)
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
		if (_hasBuffsRemovedOnAction && _buffs != null && !_buffs.isEmpty())
		{
			for (L2Abnormal e : _buffs)
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
		if (_hasBuffsRemovedOnDamage && _buffs != null && !_buffs.isEmpty())
		{
			for (L2Abnormal e : _buffs)
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

		if (_hasDebuffsRemovedOnDamage && _debuffs != null && !_debuffs.isEmpty())
		{
			for (L2Abnormal e : _debuffs)
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
		if (_hasBuffsRemovedOnDebuffBlock && _buffs != null && !_buffs.isEmpty())
		{
			_buffs.stream().filter(e -> e != null).filter(e -> e.isRemovedOnDebuffBlock(true)).forEachOrdered(e ->
			{
				e.exit(true);
			});
		}
	}

	public void updateEffectIcons(boolean partyOnly)
	{
		if (_buffs == null && _debuffs == null)
		{
			return;
		}

		if (partyOnly)
		{
			_partyOnly = true;
		}

		queueRunner();
	}

	public void queueEffect(L2Abnormal effect, boolean remove)
	{
		if (effect == null)
		{
			return;
		}

		if (!_queuesInitialized)
		{
			init();
		}

		if (remove)
		{
			_removeQueue.offer(effect);
		}
		else
		{
			_addQueue.offer(effect);
		}

		queueRunner();
	}

	private synchronized void init()
	{
		if (_queuesInitialized)
		{
			return;
		}

		_addQueue = new LinkedBlockingQueue<>();
		_removeQueue = new LinkedBlockingQueue<>();
		_queuesInitialized = true;
	}

	private void queueRunner()
	{
		if (!_queueLock.compareAndSet(true, false))
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
				while ((effect = _removeQueue.poll()) != null)
				{
					removeEffectFromQueue(effect);
					_partyOnly = false;
				}

				if ((effect = _addQueue.poll()) != null)
				{
					addEffectFromQueue(effect);
					_partyOnly = false;
				}
			}
			while (!_addQueue.isEmpty() || !_removeQueue.isEmpty());

			computeEffectFlags();
			updateEffectIcons();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			_queueLock.set(true);
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
			if (_debuffs == null)
			{
				return;
			}
			effectList = _debuffs;
		}
		else
		{
			if (_buffs == null)
			{
				return;
			}
			effectList = _buffs;
		}

		if (effect.getStackType().length == 0)
		{
			// Remove Func added by this effect from the L2Character Calculator
			_owner.removeStatsOwner(effect);
		}
		else
		{
			if (_stackedEffects == null)
			{
				return;
			}

			for (String stackType : effect.getStackType())
			{
				// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
				List<L2Abnormal> stackQueue = _stackedEffects.get(stackType);

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
						_owner.removeStatsOwner(effect);

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
									_owner.addStatFuncs(newStackedEffect.getStatFuncs());
								}
							}
						}
					}
					if (stackQueue.isEmpty())
					{
						_stackedEffects.remove(stackType);
					}
					else
					{
						// Update the Stack Group table _stackedEffects of the L2Character
						_stackedEffects.put(stackType, stackQueue);
					}
				}
			}
		}

		// Remove the active skill L2effect from _effects of the L2Character
		boolean removed = effectList.remove(effect);
		if (removed && _owner instanceof L2PcInstance && effect.getShowIcon())
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
			_owner.sendPacket(sm);
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
			if (_debuffs == null)
			{
				_debuffs = new CopyOnWriteArrayList<>();
			}

			for (L2Abnormal e : _debuffs)
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
			_debuffs.add(newEffect);
		}
		else
		{
			if (_buffs == null)
			{
				_buffs = new CopyOnWriteArrayList<>();
			}

			for (L2Abnormal e : _buffs)
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
			if (newEffect.isHerbEffect() && getBuffCount() >= _owner.getMaxBuffCount())
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
					for (L2Abnormal e : _buffs)
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
					for (L2Abnormal e : _buffs)
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
				effectsToRemove = getBuffCount() - _owner.getMaxBuffCount();
				if (effectsToRemove >= 0)
				{
					switch (newSkill.getSkillType())
					{
						case BUFF:
						case HEAL_PERCENT:
						case MANAHEAL_PERCENT:
							for (L2Abnormal e : _buffs)
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
				_buffs.add(newEffect);
			}
			else
			{
				int pos = 0;
				if (newSkill.isDance())
				{
					// toggle skill - before all dances
					for (L2Abnormal e : _buffs)
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
					for (L2Abnormal e : _buffs)
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
					for (L2Abnormal e : _buffs)
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
				_buffs.add(pos, newEffect);
			}
		}

		// Check if a stack group is defined for this effect
		if (newEffect.getStackType().length == 0)
		{
			// Set this L2Effect to In Use
			if (newEffect.setInUse(true))
			// Add Funcs of this effect to the Calculator set of the L2Character
			{
				_owner.addStatFuncs(newEffect.getStatFuncs());
			}
			else
			{
				if (newEffect.getSkill().isDebuff())
				{
					_debuffs.remove(newEffect);
				}
				else
				{
					_buffs.remove(newEffect);
				}
			}

			return;
		}

		if (_stackedEffects == null)
		{
			_stackedEffects = new HashMap<>();
		}

		Set<L2Abnormal> effectsToAdd = new HashSet<>();
		Set<L2Abnormal> effectsToRemove = new HashSet<>();
		Set<L2Abnormal> removed = new HashSet<>();
		for (String stackType : newEffect.getStackType())
		{
			L2Abnormal effectToAdd = null;
			L2Abnormal effectToRemove = null;
			// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
			List<L2Abnormal> stackQueue = _stackedEffects.get(stackType);
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
								_debuffs.remove(toRemove);
							}
							else
							{
								_buffs.remove(toRemove);
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

			// Update the Stack Group table _stackedEffects of the L2Character
			_stackedEffects.put(stackType, stackQueue);

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
				if (_stackedEffects.get(stackType).get(0) != a)
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
					_owner.addStatFuncs(a.getStatFuncs());
				}
				else
				{
					if (a.getSkill().isDebuff())
					{
						_debuffs.remove(a);
					}
					else
					{
						_buffs.remove(a);
					}
				}
			}
			else
			{
				// Remove it from the stack
				for (String stackType : a.getStackType())
				{
					_stackedEffects.get(stackType).remove(a);
				}
			}
		}

		for (L2Abnormal a : effectsToRemove)
		{
			// Remove all Func objects corresponding to this stacked effect from the Calculator set of the L2Character
			_owner.removeStatsOwner(a);
		}
	}

	protected void updateEffectIcons()
	{
		if (_owner == null)
		{
			return;
		}

		_owner.broadcastAbnormalStatusUpdate();

		if (!(_owner instanceof L2Playable))
		{
			updateEffectFlags();
			return;
		}

		AbnormalStatusUpdate mi = null;
		PartySpelled ps = null;
		ExOlympiadSpelledInfo os = null;

		if (_owner instanceof L2PcInstance)
		{
			if (_partyOnly)
			{
				_partyOnly = false;
			}
			else
			{
				mi = new AbnormalStatusUpdate();
			}

			if (_owner.isInParty())
			{
				ps = new PartySpelled(_owner);
			}

			if (((L2PcInstance) _owner).isInOlympiadMode() && ((L2PcInstance) _owner).isOlympiadStart())
			{
				os = new ExOlympiadSpelledInfo((L2PcInstance) _owner);
			}
		}
		else if (_owner instanceof L2Summon)
		{
			ps = new PartySpelled(_owner);
		}

		boolean foundRemovedOnAction = false;
		boolean foundRemovedOnDamage = false;
		boolean foundRemovedOnDebuffBlock = false;

		if (_buffs != null && !_buffs.isEmpty())
		{
			//synchronized (_buffs)
			{
				for (L2Abnormal e : _buffs)
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

		_hasBuffsRemovedOnAction = foundRemovedOnAction;
		_hasBuffsRemovedOnDamage = foundRemovedOnDamage;
		_hasBuffsRemovedOnDebuffBlock = foundRemovedOnDebuffBlock;
		foundRemovedOnDamage = false;

		if (_debuffs != null && !_debuffs.isEmpty())
		{
			//synchronized (_debuffs)
			{
				for (L2Abnormal e : _debuffs)
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

		_hasDebuffsRemovedOnDamage = foundRemovedOnDamage;

		if (mi != null)
		{
			_owner.sendPacket(mi);
		}

		if (ps != null)
		{
			if (_owner instanceof L2Summon)
			{
				L2PcInstance summonOwner = ((L2Summon) _owner).getOwner();

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
			else if (_owner instanceof L2PcInstance && _owner.isInParty())
			{
				_owner.getParty().broadcastToPartyMembers(ps);
			}
		}

		if (os != null)
		{
			final OlympiadGameTask game =
					OlympiadGameManager.getInstance().getOlympiadTask(((L2PcInstance) _owner).getOlympiadGameId());
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

		if (_buffs != null && !_buffs.isEmpty())
		{
			//synchronized (_buffs)
			{
				for (L2Abnormal e : _buffs)
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
		_hasBuffsRemovedOnAction = foundRemovedOnAction;
		_hasBuffsRemovedOnDamage = foundRemovedOnDamage;
		_hasBuffsRemovedOnDebuffBlock = foundRemovedOnDebuffBlock;
		foundRemovedOnDamage = false;

		if (_debuffs != null && !_debuffs.isEmpty())
		{
			//synchronized (_debuffs)
			{
				for (L2Abnormal e : _debuffs)
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
		_hasDebuffsRemovedOnDamage = foundRemovedOnDamage;
	}

	/**
	 * Returns effect if contains in _buffs or _debuffs and null if not found
	 *
	 * @param effect
	 * @return
	 */
	private L2Abnormal listsContains(L2Abnormal effect)
	{
		if (_buffs != null && !_buffs.isEmpty() && _buffs.contains(effect))
		{
			return effect;
		}
		if (_debuffs != null && !_debuffs.isEmpty() && _debuffs.contains(effect))
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

		if (_buffs != null)
		{
			for (L2Abnormal e : _buffs)
			{
				if (e == null)
				{
					continue;
				}
				flags |= e.getEffectMask();
			}
		}

		if (_debuffs != null)
		{
			for (L2Abnormal e : _debuffs)
			{
				if (e == null)
				{
					continue;
				}
				flags |= e.getEffectMask();
			}
		}

		_effectFlags = flags;
	}

	/**
	 * Check if target is affected with special buff
	 *
	 * @param bitFlag flag of special buff
	 * @return boolean true if affected
	 */
	public boolean isAffected(long bitFlag)
	{
		return (_effectFlags & bitFlag) != 0;
	}

	/**
	 * Clear and null all queues and lists
	 * Use only during delete character from the world.
	 */
	public void clear()
	{
		try
		{
			if (_addQueue != null)
			{
				_addQueue.clear();
				_addQueue = null;
			}
			if (_removeQueue != null)
			{
				_removeQueue.clear();
				_removeQueue = null;
			}
			_queuesInitialized = false;

			if (_buffs != null)
			{
				_buffs.clear();
				_buffs = null;
			}
			if (_debuffs != null)
			{
				_debuffs.clear();
				_debuffs = null;
			}

			if (_stackedEffects != null)
			{
				_stackedEffects.clear();
				_stackedEffects = null;
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "", e);
		}
	}
}
