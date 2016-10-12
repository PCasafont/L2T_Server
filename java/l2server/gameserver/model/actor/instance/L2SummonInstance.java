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

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.network.serverpackets.SetSummonRemainTime;
import l2server.gameserver.stats.skills.L2SkillSummon;
import l2server.gameserver.taskmanager.DecayTaskManager;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.log.Log;

import java.util.concurrent.Future;
import java.util.logging.Level;

public class L2SummonInstance extends L2Summon
{
	private float _expPenalty = 0; // exp decrease multiplier (i.e. 0.3 (= 30%) for shadow)
	private int _itemConsumeId;
	private int _itemConsumeCount;
	private int _itemConsumeSteps;
	private final int _totalLifeTime;
	private final int _timeLostIdle;
	private final int _timeLostActive;
	private int _timeRemaining;
	private int _nextItemConsumeTime;
	private int _summonSkillId;
	private L2Skill _summonPrice;
	private int _summonPoints;
	public int _lastLifeTimeCheck; // Following FbiAgent's example to avoid sending useless packets

	private Future<?> _summonLifeTask;

	public L2SummonInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill)
	{
		super(objectId, template, owner);
		setInstanceType(InstanceType.L2SummonInstance);
		setShowSummonAnimation(true);

		if (skill != null)
		{
			final L2SkillSummon summonSkill = (L2SkillSummon) skill;
			_itemConsumeId = summonSkill.getItemConsumeIdOT();
			_itemConsumeCount = summonSkill.getItemConsumeOT();
			_itemConsumeSteps = summonSkill.getItemConsumeSteps();
			_totalLifeTime = summonSkill.getTotalLifeTime();
			_timeLostIdle = summonSkill.getTimeLostIdle();
			_timeLostActive = summonSkill.getTimeLostActive();
			int summonPrice = summonSkill.getSummonPrice();
			if (summonPrice != 0)
			{
				_summonPrice = SkillTable.getInstance().getInfo(summonPrice, 1);
			}
			else
			{
				_summonPrice = null;
			}
			_summonPoints = summonSkill.getSummonPoints();
			_summonSkillId = summonSkill.getId();
		}
		else
		{
			// defaults
			_itemConsumeId = 0;
			_itemConsumeCount = 0;
			_itemConsumeSteps = 0;
			_totalLifeTime = -1; // infinite
			_timeLostIdle = 1000;
			_timeLostActive = 1000;
			_summonPrice = null;
			_summonPoints = 0;
			_summonSkillId = 0;
		}

		_timeRemaining = _totalLifeTime;
		_lastLifeTimeCheck = _totalLifeTime;

		if (_itemConsumeId == 0)
		{
			_nextItemConsumeTime = -1; // do not consume
		}
		else if (_itemConsumeSteps == 0)
		{
			_nextItemConsumeTime = -1; // do not consume
		}
		else
		{
			_nextItemConsumeTime = _totalLifeTime - _totalLifeTime / (_itemConsumeSteps + 1);
		}

		// When no item consume is defined task only need to check when summon life time has ended.
		// Otherwise have to destroy items from owner's inventory in order to let summon live.
		int delay = 1000;

		if (Config.DEBUG && _itemConsumeCount != 0)
		{
			Log.warning("L2SummonInstance: Item Consume ID: " + _itemConsumeId + ", Count: " + _itemConsumeCount +
					", Rate: " + _itemConsumeSteps + " times.");
		}
		if (Config.DEBUG)
		{
			Log.warning("L2SummonInstance: Task Delay " + delay / 1000 + " seconds.");
		}

		_summonLifeTask = ThreadPoolManager.getInstance()
				.scheduleGeneralAtFixedRate(new SummonLifetime(getOwner(), this), delay, delay);

		// Restore summon's buffs if there are any stored (Noblesse)
		if (!getOwner().isInOlympiadMode())
		{
			L2Abnormal[] restoreEffects = getOwner().restoreSummonBuffs();
			// Only restore buffs if new summon is same kind as last, else clear stored buffs
			if (restoreEffects != null && getNpcId() == getOwner().getLastSummonId())
			{
				for (L2Abnormal e : restoreEffects)
				{
					if (e.getType() == L2AbnormalType.HIDE)
					{
						continue;
					}

					e.getSkill().getEffects(getOwner(), this);
				}
			}
			else
			{
				getOwner().storeSummonBuffs(null);
			}
		}

		// Give the owner's buffs
		for (L2Abnormal e : getOwner().getAllEffects())
		{
			if (e.getType() == L2AbnormalType.HIDE)
			{
				continue;
			}

			if (e.canBeShared())
			{
				for (L2Abnormal sE : e.getSkill().getEffects(getOwner(), this))
				{
					sE.setFirstTime(e.getTime());
				}
			}
		}
	}

	@Override
	public final int getLevel()
	{
		return getTemplate() != null ? getTemplate().Level : 0;
	}

	@Override
	public int getSummonType()
	{
		return 1;
	}

	public void setExpPenalty(float expPenalty)
	{
		_expPenalty = expPenalty;
	}

	public float getExpPenalty()
	{
		return _expPenalty;
	}

	public int getItemConsumeCount()
	{
		return _itemConsumeCount;
	}

	public int getItemConsumeId()
	{
		return _itemConsumeId;
	}

	public int getItemConsumeSteps()
	{
		return _itemConsumeSteps;
	}

	public int getNextItemConsumeTime()
	{
		return _nextItemConsumeTime;
	}

	public int getTotalLifeTime()
	{
		return _totalLifeTime;
	}

	public int getTimeLostIdle()
	{
		return _timeLostIdle;
	}

	public int getTimeLostActive()
	{
		return _timeLostActive;
	}

	public int getTimeRemaining()
	{
		return _timeRemaining;
	}

	public int getSummonSkillId()
	{
		return _summonSkillId;
	}

	public void setNextItemConsumeTime(int value)
	{
		_nextItemConsumeTime = value;
	}

	public void decNextItemConsumeTime(int value)
	{
		_nextItemConsumeTime -= value;
	}

	public void decTimeRemaining(int value)
	{
		_timeRemaining -= value;
	}

	public void addExpAndSp(int addToExp, int addToSp)
	{
		getOwner().addExpAndSp(addToExp, addToSp);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		// Store buffs of summon when it does while having Noblesse Blessing
		if (getOwner() != null && !getOwner().isInOlympiadMode())
		{
			L2Abnormal[] effects = getAllEffects();
			for (L2Abnormal e : effects)
			{
				if (e.getSkill().getId() == 1323 ||
						e.getSkill().getId() == 7096) // Noblesse Blessing and Master's Blessing of Noblesse
				{
					getOwner().storeSummonBuffs(effects);
				}
			}
		}

		if (!super.doDie(killer))
		{
			return false;
		}

		// To prevent players re-summoning their dead summons endlessly
		if (_summonPoints > 0 && !(getOwner() != null && getOwner().isPlayingEvent() &&
				!getOwner().getEvent().isType(EventType.Survival) &&
				!getOwner().getEvent().isType(EventType.TeamSurvival)))
		{
			DecayTaskManager.getInstance().addDecayTask(this, 5000);
		}

		if (Config.DEBUG)
		{
			Log.warning("L2SummonInstance: " + getTemplate().Name + " (" + getOwner().getName() + ") has been killed.");
		}

		if (_summonLifeTask != null)
		{
			_summonLifeTask.cancel(true);
			_summonLifeTask = null;
		}
		return true;
	}

	/**
	 * Servitors' skills automatically change their level based on the servitor's level.
	 * Until level 70, the servitor gets 1 lv of skill per 10 levels. After that, it is 1
	 * skill level per 5 servitor levels.  If the resulting skill level doesn't exist use
	 * the max that does exist!
	 *
	 * @see l2server.gameserver.model.actor.L2Character#doCast(l2server.gameserver.model.L2Skill)
	 */
	@Override
	public void doCast(L2Skill skill)
	{
		final int petLevel = getLevel();
		int skillLevel = petLevel / 10;
		if (petLevel >= 70)
		{
			skillLevel += (petLevel - 65) / 10;
		}

		// adjust the level for servitors less than lv 10
		if (skillLevel < 1)
		{
			skillLevel = 1;
		}

		L2Skill skillToCast = SkillTable.getInstance().getInfo(skill.getId(), skillLevel);

		if (skillToCast != null)
		{
			super.doCast(skillToCast);
		}
		else
		{
			super.doCast(skill);
		}
	}

	static class SummonLifetime implements Runnable
	{
		private L2PcInstance _activeChar;
		private L2SummonInstance _summon;

		SummonLifetime(L2PcInstance activeChar, L2SummonInstance newpet)
		{
			_activeChar = activeChar;
			_summon = newpet;
		}

		@Override
		public void run()
		{
			if (Config.DEBUG)
			{
				Log.warning("L2SummonInstance: " + _summon.getTemplate().Name + " (" + _activeChar.getName() +
						") run task.");
			}

			try
			{
				double oldTimeRemaining = _summon.getTimeRemaining();
				int maxTime = _summon.getTotalLifeTime();
				double newTimeRemaining;

				// if pet is attacking
				if (_summon.isAttackingNow())
				{
					_summon.decTimeRemaining(_summon.getTimeLostActive());
				}
				else
				{
					_summon.decTimeRemaining(_summon.getTimeLostIdle());
				}
				newTimeRemaining = _summon.getTimeRemaining();
				// check if the summon's lifetime has ran out
				if (maxTime > 0 && newTimeRemaining < 0)
				{
					_summon.unSummon(_activeChar);
				}
				// check if it is time to consume another item
				else if (newTimeRemaining <= _summon.getNextItemConsumeTime() &&
						oldTimeRemaining > _summon.getNextItemConsumeTime())
				{
					_summon.decNextItemConsumeTime(maxTime / (_summon.getItemConsumeSteps() + 1));

					// check if owner has enought itemConsume, if requested
					if (_summon.getItemConsumeCount() > 0 && _summon.getItemConsumeId() != 0 && !_summon.isDead() &&
							!_summon.destroyItemByItemId("Consume", _summon.getItemConsumeId(),
									_summon.getItemConsumeCount(), _activeChar, true))
					{
						_summon.unSummon(_activeChar);
					}
				}

				// prevent useless packet-sending when the difference isn't visible.
				/*if ((_summon._lastShowntimeRemaining - newTimeRemaining) > maxTime / 352)
                {
					_summon.getOwner().sendPacket(new SetSummonRemainTime(maxTime, (int) newTimeRemaining));
					_summon._lastShowntimeRemaining = (int) newTimeRemaining;
					_summon.updateEffectIcons();
				}*/
				if (_summon._lastLifeTimeCheck > 50)
				{
					_summon.getOwner().sendPacket(new SetSummonRemainTime(maxTime, (int) newTimeRemaining));
					_summon._lastLifeTimeCheck = 0;
					if (_summon._summonPrice != null)
					{
						_summon._summonPrice.getEffects(_summon, _activeChar);
					}
					_summon.updateEffectIcons();
				}
				_summon._lastLifeTimeCheck++;
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "Error on player [" + _activeChar.getName() + "] summon item consume task.", e);
			}
		}
	}

	@Override
	public void unSummon(L2PcInstance owner)
	{
		if (Config.DEBUG)
		{
			Log.warning("L2SummonInstance: " + getTemplate().Name + " (" + owner.getName() + ") unsummoned.");
		}

		if (_summonLifeTask != null)
		{
			_summonLifeTask.cancel(true);
			_summonLifeTask = null;

			if (_summonPrice != null)
			{
				for (L2Abnormal e : _owner.getAllEffects())
				{
					if (e.getSkill().getId() == _summonPrice.getId())
					{
						e.exit();
					}
				}
			}
		}

		super.unSummon(owner);
	}

	@Override
	public boolean destroyItem(String process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		return getOwner().destroyItem(process, objectId, count, reference, sendMessage);
	}

	@Override
	public boolean destroyItemByItemId(String process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		if (Config.DEBUG)
		{
			Log.warning("L2SummonInstance: " + getTemplate().Name + " (" + getOwner().getName() + ") consume.");
		}

		return getOwner().destroyItemByItemId(process, itemId, count, reference, sendMessage);
	}

	@Override
	public byte getAttackElement()
	{
		if (getOwner() == null || !getOwner().getCurrentClass().isSummoner())
		{
			return super.getAttackElement();
		}

		return getOwner().getAttackElement();
	}

	@Override
	public int getAttackElementValue(byte attribute)
	{
		if (getOwner() == null || !getOwner().getCurrentClass().isSummoner() ||
				getOwner().getExpertiseWeaponPenalty() > 0)
		{
			return super.getAttackElementValue(attribute);
		}

		return getOwner().getAttackElementValue(attribute);
	}

	@Override
	public int getDefenseElementValue(byte attribute)
	{
		if (getOwner() == null || !getOwner().getCurrentClass().isSummoner())
		{
			return super.getDefenseElementValue(attribute);
		}

		return getOwner().getDefenseElementValue(attribute);
	}

	@Override
	public boolean isMovementDisabled()
	{
		return super.isMovementDisabled() || !getTemplate().getAIData().canMove();
	}

	/**
	 * Tenkai custom
	 *
	 * @return returns true if the owner of this servitor is in Olympiad mode
	 */
	public boolean isInOlympiadMode()
	{
		return _owner != null && _owner.isInOlympiadMode();
	}

	public int getSummonPoints()
	{
		return _summonPoints;
	}
}
