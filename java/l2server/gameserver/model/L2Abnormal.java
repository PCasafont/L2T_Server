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

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.TimeController;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.VisualEffect;
import l2server.gameserver.stats.funcs.Func;
import l2server.gameserver.stats.funcs.FuncTemplate;
import l2server.gameserver.templates.skills.L2AbnormalTemplate;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectType;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.log.Log;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.1.2.12 $ $Date: 2005/04/11 10:06:07 $
 */
public class L2Abnormal
{

	public enum AbnormalState
	{
		CREATED, ACTING, FINISHING
	}

	private static final Func[] emptyFunctionSet = new Func[0];

	//member this.effector is the instance of L2Character that cast/used the spell/skill that is
	//causing this effect.  Do not confuse with the instance of L2Character that
	//is being affected by this effect.
	private final L2Character effector;

	//member this.effected is the instance of L2Character that was affected
	//by this effect.  Do not confuse with the instance of L2Character that
	//casted/used this effect.
	private final L2Character effected;

	//the skill that was used.
	private final L2Skill skill;

	private final boolean isHerbEffect;

	//or the items that was used.
	//private final L2Item item;

	// the current state
	private AbnormalState state;

	// period, seconds
	private final int duration;
	private int periodStartTicks;
	private int periodFirstTime;

	private L2AbnormalTemplate template;

	// function templates
	private final FuncTemplate[] funcTemplates;

	//initial count
	private int totalCount;
	// counter
	private int count;

	// visual effect
	private VisualEffect[] visualEffect;
	// show icon
	private boolean icon;
	// is selfeffect ?
	private boolean isSelfEffect = false;
	// skill combo id
	private int comboId = 0;

	public boolean preventExitUpdate;
	private int strikes = 0;
	private int blockedDamage = 0;
	private int debuffBlocks = 0;

	private final class AbnormalTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				periodFirstTime = 0;
				periodStartTicks = TimeController.getGameTicks();
				scheduleEffect();
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "", e);
			}
		}
	}

	private ScheduledFuture<?> currentFuture;

	/**
	 * The Identifier of the stack group
	 */
	private final String[] stackType;

	/**
	 * The position of the effect in the stack group
	 */
	private final byte stackLvl;

	private boolean inUse = false;
	private boolean startConditionsCorrect = true;

	private double landRate;

	private L2Effect[] effects;

	/**
	 * <font color="FF0000"><b>WARNING: scheduleEffect nolonger inside constructor</b></font><br>
	 * So you must call it explicitly
	 */
	public L2Abnormal(Env env, L2AbnormalTemplate template, L2Effect[] effects)
	{
		this.state = AbnormalState.CREATED;
		this.skill = env.skill;
		//_item = env.item == null ? null : env.item.getItem();
		this.template = template;
		this.effected = env.target;
		this.effector = env.player;
		this.funcTemplates = template.funcTemplates;
		this.count = template.counter;
		this.totalCount = this.count;

		// Support for retail herbs duration when this.effected has a Summon
		int temp = template.duration;

		if (this.skill.getId() > 2277 && this.skill.getId() < 2286 || this.skill.getId() >= 2512 && this.skill.getId() <= 2514)
		{
			if (this.effected instanceof L2SummonInstance ||
					this.effected instanceof L2PcInstance && !((L2PcInstance) this.effected).getSummons().isEmpty())
			{
				temp /= 2;
			}
		}

		if (env.skillMastery)
		{
			temp *= 2;
		}

		this.duration = temp;
		this.visualEffect = template.visualEffect;
		this.stackType = template.stackType;
		this.stackLvl = template.stackLvl;
		this.periodStartTicks = TimeController.getGameTicks();
		this.periodFirstTime = 0;
		this.icon = template.icon;
		this.landRate = template.landRate;

		this.isHerbEffect = this.skill.getName().contains("Herb");
		this.comboId = template.comboId;

		this.effects = effects;
	}

	/**
	 * Special constructor to "steal" buffs. Must be implemented on
	 * every child class that can be stolen.<br><br>
	 * <p>
	 * <font color="FF0000"><b>WARNING: scheduleEffect nolonger inside constructor</b></font>
	 * <br>So you must call it explicitly
	 *
	 * @param env
	 * @param effect
	 */
	protected L2Abnormal(Env env, L2Abnormal effect)
	{
		this.template = effect.template;
		this.state = AbnormalState.CREATED;
		this.skill = env.skill;
		this.effected = env.target;
		this.effector = env.player;
		this.funcTemplates = this.template.funcTemplates;
		this.count = effect.getCount();
		this.totalCount = this.template.counter;
		this.duration = this.template.duration;
		this.visualEffect = this.template.visualEffect;
		this.stackType = this.template.stackType;
		this.stackLvl = this.template.stackLvl;
		this.periodStartTicks = effect.getPeriodStartTicks();
		this.periodFirstTime = effect.getTime();
		this.icon = this.template.icon;

		this.isHerbEffect = this.skill.getName().contains("Herb");

		this.comboId = effect.comboId;

		/*
		 * Commented out by DrHouse:
		 * scheduleEffect can call onStart before effect is completly
		 * initialized on constructor (child classes constructor)
		 */
		//scheduleEffect();
	}

	public int getCount()
	{
		return this.count;
	}

	public int getTotalCount()
	{
		return this.totalCount;
	}

	public void setCount(int newcount)
	{
		this.count = Math.min(newcount, this.totalCount); // sanity check
	}

	public void setFirstTime(int newFirstTime)
	{
		this.periodFirstTime = Math.min(newFirstTime, this.duration);
		this.periodStartTicks -= this.periodFirstTime * TimeController.TICKS_PER_SECOND;
	}

	public boolean getShowIcon()
	{
		return this.icon;
	}

	public int getDuration()
	{
		return this.duration;
	}

	public int getTime()
	{
		return (TimeController.getGameTicks() - this.periodStartTicks) / TimeController.TICKS_PER_SECOND;
	}

	/**
	 * Returns the elapsed time of the task.
	 *
	 * @return Time in seconds.
	 */
	public int getTaskTime()
	{
		if (this.count == this.totalCount)
		{
			return 0;
		}
		return Math.abs(this.count - this.totalCount + 1) * this.duration + getTime() + 1;
	}

	public boolean getInUse()
	{
		return this.inUse;
	}

	public boolean setInUse(boolean inUse)
	{
		this.inUse = inUse;
		if (this.inUse)
		{
			this.startConditionsCorrect = onStart();
		}
		else
		{
			onExit();
		}

		return this.startConditionsCorrect;
	}

	public String[] getStackType()
	{
		return this.stackType;
	}

	public byte getStackLvl()
	{
		return this.stackLvl;
	}

	public final L2Skill getSkill()
	{
		return this.skill;
	}

	public final L2Character getEffector()
	{
		return this.effector;
	}

	public final L2Character getEffected()
	{
		return this.effected;
	}

	public boolean isSelfEffect()
	{
		return this.isSelfEffect;
	}

	public void setSelfEffect()
	{
		this.isSelfEffect = true;
	}

	public boolean isHerbEffect()
	{
		return this.isHerbEffect;
	}

	private synchronized void startEffectTask()
	{
		if (this.duration > 0)
		{
			stopEffectTask();
			final int initialDelay = Math.max((this.duration - this.periodFirstTime) * 1000, 5);
			if (this.count > 1)
			{
				this.currentFuture = ThreadPoolManager.getInstance()
						.scheduleEffectAtFixedRate(new AbnormalTask(), initialDelay, this.duration * 1000);
			}
			else
			{
				this.currentFuture = ThreadPoolManager.getInstance().scheduleEffect(new AbnormalTask(), initialDelay);
			}
		}
		if (this.state == AbnormalState.ACTING)
		{
			if (isSelfEffectType())
			{
				this.effector.addEffect(this);
			}
			else
			{
				this.effected.addEffect(this);
			}
		}
	}

	/**
	 * Stop the L2Effect task and send Server->Client update packet.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Cancel the effect in the the abnormal effect map of the L2Character </li>
	 * <li>Stop the task of the L2Effect, remove it and update client magic icon </li><BR><BR>
	 */
	public final void exit()
	{
		exit(false);
	}

	public final void exit(boolean preventUpdate)
	{
		preventExitUpdate = preventUpdate;
		this.state = AbnormalState.FINISHING;
		scheduleEffect();
	}

	/**
	 * Stop the task of the L2Effect, remove it and update client magic icon.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Cancel the task </li>
	 * <li>Stop and remove L2Effect from L2Character and update client magic icon </li><BR><BR>
	 */
	public final synchronized void stopEffectTask()
	{
		if (this.currentFuture != null)
		{
			// Cancel the task
			this.currentFuture.cancel(false);
			//ThreadPoolManager.getInstance().removeEffect(this.currentTask);

			this.currentFuture = null;

			if (isSelfEffectType() && getEffector() != null)
			{
				getEffector().removeEffect(this);
			}
			else if (getEffected() != null)
			{
				getEffected().removeEffect(this);
			}
		}
	}

	/**
	 * returns effect type
	 */
	public L2AbnormalType getType()
	{
		if (getTemplate().effectType != L2AbnormalType.NONE)
		{
			return getTemplate().effectType;
		}

		for (L2Effect e : this.effects)
		{
			if (e.getAbnormalType() != L2AbnormalType.NONE)
			{
				return e.getAbnormalType();
			}
		}

		return L2AbnormalType.NONE;
	}

	public long getEffectMask()
	{
		long mask = 0L;
		for (L2Effect e : this.effects)
		{
			mask |= e.getEffectMask();
		}

		return mask;
	}

	/**
	 * Notify started
	 */
	public boolean onStart()
	{
		if (this.visualEffect != null)
		{
			for (VisualEffect ve : this.visualEffect)
			{
				getEffected().startVisualEffect(ve);
			}
		}

		boolean canStart = true;
		boolean[] started = new boolean[this.effects.length];
		int i = 0;
		for (L2Effect effect : this.effects)
		{
			started[i] = effect.onStart();
			if (!started[i])
			{
				canStart = false;
			}

			i++;
		}

		if (!canStart)
		{
			i = 0;
			for (L2Effect effect : this.effects)
			{
				if (started[i])
				{
					effect.onExit();
				}

				i++;
			}

			if (this.visualEffect != null)
			{
				for (VisualEffect ve : this.visualEffect)
				{
					getEffected().stopVisualEffect(ve);
				}
			}
		}

		this.strikes = 0;
		this.debuffBlocks = 0;

		return canStart;
	}

	/**
	 * Cancel the effect in the the abnormal effect map of the effected L2Character.<BR><BR>
	 */
	public void onExit()
	{
		for (L2Effect effect : this.effects)
		{
			effect.onExit();
		}

		if (this.visualEffect != null)
		{
			for (VisualEffect ve : this.visualEffect)
			{
				getEffected().stopVisualEffect(ve);
			}
		}
	}

	/**
	 * Cancel the effect in the the abnormal effect map of the effected L2Character.<BR><BR>
	 */
	public boolean onActionTime()
	{
		boolean toReturn = true;
		for (L2Effect effect : this.effects)
		{
			if (!effect.onActionTime())
			{
				toReturn = false;
			}
		}

		return toReturn;
	}

	public final void scheduleEffect()
	{
		switch (this.state)
		{
			case CREATED:
			{
				this.state = AbnormalState.ACTING;

				if (this.skill.isOffensive() && this.icon && getEffected() instanceof L2PcInstance)
				{
					SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
					smsg.addSkillName(this.skill);
					getEffected().sendPacket(smsg);
				}

				if (this.duration != 0)
				{
					startEffectTask();
					return;
				}
				// effects not having count or period should start
				this.startConditionsCorrect = onStart();
			}
			case ACTING:
			{
				if (this.count > 0)
				{
					this.count--;
					if (getInUse())
					{ // effect has to be in use
						if (onActionTime() && this.startConditionsCorrect && this.count > 0)
						{
							return; // false causes effect to finish right away
						}
					}
					else if (this.count > 0)
					{ // do not finish it yet, in case reactivated
						return;
					}
				}
				this.state = AbnormalState.FINISHING;
			}
			case FINISHING:
			{
				//If the time left is equal to zero, send the message
				if (this.count == 0 && this.icon && getEffected() instanceof L2PcInstance)
				{
					SystemMessage smsg3 = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WORN_OFF);
					smsg3.addSkillName(this.skill);
					getEffected().sendPacket(smsg3);
				}
				// if task is null - stopEffectTask does not remove effect
				if (this.currentFuture == null && getEffected() != null)
				{
					getEffected().removeEffect(this);
				}
				// Stop the task of the L2Effect, remove it and update client magic icon
				stopEffectTask();

				// Cancel the effect in the the abnormal effect map of the L2Character
				if (getInUse() || !(this.count > 1 || this.duration > 0))
				{
					if (this.startConditionsCorrect)
					{
						onExit();
					}
				}

				if (this.skill.getAfterEffectId() > 0)
				{
					L2Skill skill =
							SkillTable.getInstance().getInfo(this.skill.getAfterEffectId(), this.skill.getAfterEffectLvl());
					if (skill != null)
					{
						getEffected().broadcastPacket(
								new MagicSkillUse(this.effected, skill.getId(), skill.getLevelHash(), 0, 0));
						getEffected().broadcastPacket(
								new MagicSkillLaunched(this.effected, skill.getId(), skill.getLevelHash()));
						skill.getEffects(getEffected(), getEffected());
					}
				}

				if (this.skill.getId() == 14571 && getEffected() instanceof L2PcInstance)
				{
					((L2PcInstance) getEffected()).decreaseBreathOfShilenDebuffLevel();
				}
				if (this.skill.getId() == 1570 && getEffected() instanceof L2PcInstance &&
						((L2PcInstance) getEffected()).hasIdentityCrisis())
				{
					((L2PcInstance) getEffected()).setHasIdentityCrisis(false);
				}
			}
		}
	}

	public Func[] getStatFuncs()
	{
		if (this.funcTemplates == null)
		{
			return this.emptyFunctionSet;
		}
		ArrayList<Func> funcs = new ArrayList<>(this.funcTemplates.length);

		Env env = new Env();
		env.player = getEffector();
		env.target = getEffected();
		env.skill = getSkill();
		Func f;

		for (FuncTemplate t : this.funcTemplates)
		{
			f = t.getFunc(this); // effect is owner
			if (f != null)
			{
				funcs.add(f);
			}
		}
		if (funcs.isEmpty())
		{
			return this.emptyFunctionSet;
		}

		return funcs.toArray(new Func[funcs.size()]);
	}

	public final void addIcon(AbnormalStatusUpdate mi)
	{
		if (this.state != AbnormalState.ACTING)
		{
			return;
		}

		final ScheduledFuture<?> future = this.currentFuture;
		final L2Skill sk = getSkill();

		int levelHash = getLevelHash();
		if (sk.getId() >= 11139 && sk.getId() <= 11145)
		{
			levelHash = getLevel();
		}
		if (this.totalCount > 1)
		{
			mi.addEffect(sk.getId(), levelHash, this.comboId,
					(this.count - 1) * this.duration * 1000 + (int) future.getDelay(TimeUnit.MILLISECONDS));
		}
		else if (future != null)
		{
			mi.addEffect(sk.getId(), levelHash, this.comboId, (int) future.getDelay(TimeUnit.MILLISECONDS));
		}
		else if (this.duration == -1)
		{
			mi.addEffect(sk.getId(), levelHash, this.comboId, this.duration);
		}
	}

	public final void addIcon(AbnormalStatusUpdateFromTarget mi)
	{
		if (this.state != AbnormalState.ACTING)
		{
			return;
		}

		final ScheduledFuture<?> future = this.currentFuture;
		final L2Skill sk = getSkill();
		if (sk == null || this.effector == null || mi == null || future == null)
		{
			return;
		}

		int levelHash = getLevelHash();
		if (sk.getId() >= 11139 && sk.getId() <= 11145)
		{
			levelHash = getLevel();
		}
		if (this.totalCount > 1)
		{
			mi.addEffect(sk.getId(), levelHash, this.comboId,
					(this.count - 1) * this.duration * 1000 + (int) future.getDelay(TimeUnit.MILLISECONDS),
					this.effector.getObjectId());
		}
		else if (this.effector != null)
		{
			mi.addEffect(sk.getId(), levelHash, this.comboId, (int) future.getDelay(TimeUnit.MILLISECONDS),
					this.effector.getObjectId());
		}
		else if (this.duration == -1)
		{
			mi.addEffect(sk.getId(), levelHash, this.comboId, this.duration, this.effector.getObjectId());
		}
	}

	public final void addPartySpelledIcon(PartySpelled ps)
	{
		if (this.state != AbnormalState.ACTING)
		{
			return;
		}

		final ScheduledFuture<?> future = this.currentFuture;
		final L2Skill sk = getSkill();
		if (future != null)
		{
			ps.addPartySpelledEffect(sk.getId(), getLevelHash(), (int) future.getDelay(TimeUnit.MILLISECONDS));
		}
		else if (this.duration == -1)
		{
			ps.addPartySpelledEffect(sk.getId(), getLevelHash(), this.duration);
		}
	}

	public final void addOlympiadSpelledIcon(ExOlympiadSpelledInfo os)
	{
		if (this.state != AbnormalState.ACTING)
		{
			return;
		}

		final ScheduledFuture<?> future = this.currentFuture;
		final L2Skill sk = getSkill();
		if (future != null)
		{
			os.addEffect(sk.getId(), getLevelHash(), (int) future.getDelay(TimeUnit.MILLISECONDS));
		}
		else if (this.duration == -1)
		{
			os.addEffect(sk.getId(), getLevelHash(), this.duration);
		}
	}

	public int getLevel()
	{
		return getSkill().getLevel();
	}

	public int getEnchantRouteId()
	{
		return getSkill().getEnchantRouteId();
	}

	public int getEnchantLevel()
	{
		return getSkill().getEnchantLevel();
	}

	public int getLevelHash()
	{
		return getSkill().getLevelHash();
	}

	public int getPeriodStartTicks()
	{
		return this.periodStartTicks;
	}

	public L2AbnormalTemplate getTemplate()
	{
		return this.template;
	}

	public double getLandRate()
	{
		return this.landRate;
	}

	public int getComboId()
	{
		return this.comboId;
	}

	public L2Effect[] getEffects()
	{
		return this.effects;
	}

	public boolean canBeStolen()
	{
		return !(!effectCanBeStolen() || getType() == L2AbnormalType.MUTATE || getSkill().isPassive() ||
				getSkill().getTargetType() == L2SkillTargetType.TARGET_SELF || getSkill().isToggle() ||
				getSkill().isDebuff() || getSkill().isHeroSkill() || getSkill().getTransformId() > 0
				//|| (this.getSkill().isGMSkill() && getEffected().getInstanceId() == 0)
				|| getSkill().isPotion() && getSkill().getId() != 2274 && getSkill().getId() != 2341
				// Hardcode for now :<
				|| isHerbEffect() || !getSkill().canBeDispeled());
	}

	public boolean canBeShared()
	{
		return !(!effectCanBeStolen() || getType() == L2AbnormalType.MUTATE || getSkill().isPassive() ||
				getSkill().isToggle() || getSkill().isDebuff()
				//|| (this.getSkill().isGMSkill() && getEffected().getInstanceId() == 0)
				|| !getSkill().canBeDispeled());
	}

	/**
	 * Return true if effect itself can be stolen
	 *
	 * @return
	 */
	protected boolean effectCanBeStolen()
	{
		for (L2Effect effect : this.effects)
		{
			if (!effect.effectCanBeStolen())
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString()
	{
		return "L2Effect [_skill=" + this.skill + ", _state=" + this.state + ", _duration=" + this.duration + "]";
	}

	public boolean isSelfEffectType()
	{
		for (L2Effect effect : this.effects)
		{
			if (effect.isSelfEffectType())
			{
				return true;
			}
		}

		return false;
	}

	public boolean isRemovedOnDamage(int damage)
	{
		if (damage > 0)
		{
			this.strikes++;
			this.blockedDamage += damage;
		}

		return getSkill().isRemovedOnDamage() || (getEffectMask() & L2EffectType.SLEEP.getMask()) != 0 ||
				(getEffectMask() & L2EffectType.FEAR.getMask()) != 0 ||
				getSkill().getStrikesToRemove() > 0 && (damage == 0 || this.strikes >= getSkill().getStrikesToRemove()) ||
				getSkill().getDamageToRemove() > 0 && (damage == 0 || this.blockedDamage >= getSkill().getDamageToRemove());
	}

	public boolean isRemovedOnDebuffBlock(boolean onDebuffBlock)
	{
		if (getSkill().isRemovedOnDebuffBlock())
		{
			if (onDebuffBlock && getSkill().getDebuffBlocksToRemove() > 0)
			{
				this.debuffBlocks++;
				return this.debuffBlocks >= getSkill().getDebuffBlocksToRemove();
			}

			return true;
		}

		return false;
	}
}
