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
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.VisualEffect;
import l2server.gameserver.stats.funcs.Func;
import l2server.gameserver.stats.funcs.FuncTemplate;
import l2server.gameserver.templates.skills.AbnormalTemplate;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectType;
import l2server.gameserver.templates.skills.SkillTargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.1.2.12 $ $Date: 2005/04/11 10:06:07 $
 */
public class Abnormal {
	private static Logger log = LoggerFactory.getLogger(Abnormal.class.getName());



	public enum AbnormalState {
		CREATED,
		ACTING,
		FINISHING
	}

	private static final Func[] emptyFunctionSet = new Func[0];

	//member effector is the instance of Creature that cast/used the spell/skill that is
	//causing this effect.  Do not confuse with the instance of Creature that
	//is being affected by this effect.
	private final Creature effector;

	//member effected is the instance of Creature that was affected
	//by this effect.  Do not confuse with the instance of Creature that
	//casted/used this effect.
	private final Creature effected;

	//the skill that was used.
	private final Skill skill;

	private final boolean isHerbEffect;

	//or the items that was used.
	//private final ItemTemplate item;

	// the current state
	private AbnormalState state;

	// period, seconds
	private final int duration;
	private int periodStartTicks;
	private int periodFirstTime;

	private AbnormalTemplate template;

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

	private final class AbnormalTask implements Runnable {
		@Override
		public void run() {
			try {
				periodFirstTime = 0;
				periodStartTicks = TimeController.getGameTicks();
				scheduleEffect();
			} catch (Exception e) {
				log.error("", e);
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
	public Abnormal(Env env, AbnormalTemplate template, L2Effect[] effects) {
		state = AbnormalState.CREATED;
		skill = env.skill;
		//item = env.item == null ? null : env.item.getItem();
		this.template = template;
		effected = env.target;
		effector = env.player;
		funcTemplates = template.funcTemplates;
		count = template.counter;
		totalCount = count;

		// Support for retail herbs duration when effected has a Summon
		int temp = template.duration;

		if (skill.getId() > 2277 && skill.getId() < 2286 || skill.getId() >= 2512 && skill.getId() <= 2514) {
			if (effected instanceof SummonInstance || effected instanceof Player && !((Player) effected).getSummons().isEmpty()) {
				temp /= 2;
			}
		}

		if (env.skillMastery) {
			temp *= 2;
		}

		duration = temp;
		visualEffect = template.visualEffect;
		stackType = template.stackType;
		stackLvl = template.stackLvl;
		periodStartTicks = TimeController.getGameTicks();
		periodFirstTime = 0;
		icon = template.icon;
		landRate = template.landRate;

		isHerbEffect = skill.getName().contains("Herb");
		comboId = template.comboId;

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
	protected Abnormal(Env env, Abnormal effect) {
		template = effect.template;
		state = AbnormalState.CREATED;
		skill = env.skill;
		effected = env.target;
		effector = env.player;
		funcTemplates = template.funcTemplates;
		count = effect.getCount();
		totalCount = template.counter;
		duration = template.duration;
		visualEffect = template.visualEffect;
		stackType = template.stackType;
		stackLvl = template.stackLvl;
		periodStartTicks = effect.getPeriodStartTicks();
		periodFirstTime = effect.getTime();
		icon = template.icon;

		isHerbEffect = skill.getName().contains("Herb");

		comboId = effect.comboId;

		/*
		 * Commented out by DrHouse:
		 * scheduleEffect can call onStart before effect is completly
		 * initialized on constructor (child classes constructor)
		 */
		//scheduleEffect();
	}

	public int getCount() {
		return count;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setCount(int newcount) {
		count = Math.min(newcount, totalCount); // sanity check
	}

	public void setFirstTime(int newFirstTime) {
		periodFirstTime = Math.min(newFirstTime, duration);
		periodStartTicks -= periodFirstTime * TimeController.TICKS_PER_SECOND;
	}

	public boolean getShowIcon() {
		return icon;
	}

	public int getDuration() {
		return duration;
	}

	public int getTime() {
		return (TimeController.getGameTicks() - periodStartTicks) / TimeController.TICKS_PER_SECOND;
	}

	/**
	 * Returns the elapsed time of the task.
	 *
	 * @return Time in seconds.
	 */
	public int getTaskTime() {
		if (count == totalCount) {
			return 0;
		}
		return Math.abs(count - totalCount + 1) * duration + getTime() + 1;
	}

	public boolean getInUse() {
		return inUse;
	}

	public boolean setInUse(boolean inUse) {
		this.inUse = inUse;
		if (inUse) {
			startConditionsCorrect = onStart();
		} else {
			onExit();
		}

		return startConditionsCorrect;
	}

	public String[] getStackType() {
		return stackType;
	}

	public byte getStackLvl() {
		return stackLvl;
	}

	public final Skill getSkill() {
		return skill;
	}

	public final Creature getEffector() {
		return effector;
	}

	public final Creature getEffected() {
		return effected;
	}

	public boolean isSelfEffect() {
		return isSelfEffect;
	}

	public void setSelfEffect() {
		isSelfEffect = true;
	}

	public boolean isHerbEffect() {
		return isHerbEffect;
	}

	private synchronized void startEffectTask() {
		if (duration > 0) {
			stopEffectTask();
			final int initialDelay = Math.max((duration - periodFirstTime) * 1000, 5);
			if (count > 1) {
				currentFuture = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new AbnormalTask(), initialDelay, duration * 1000);
			} else {
				currentFuture = ThreadPoolManager.getInstance().scheduleEffect(new AbnormalTask(), initialDelay);
			}
		}
		if (state == AbnormalState.ACTING) {
			if (isSelfEffectType()) {
				effector.addEffect(this);
			} else {
				effected.addEffect(this);
			}
		}
	}

	/**
	 * Stop the L2Effect task and send Server->Client update packet.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Cancel the effect in the the abnormal effect map of the Creature </li>
	 * <li>Stop the task of the L2Effect, remove it and update client magic icon </li><BR><BR>
	 */
	public final void exit() {
		exit(false);
	}

	public final void exit(boolean preventUpdate) {
		preventExitUpdate = preventUpdate;
		state = AbnormalState.FINISHING;
		scheduleEffect();
	}

	/**
	 * Stop the task of the L2Effect, remove it and update client magic icon.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Cancel the task </li>
	 * <li>Stop and remove L2Effect from Creature and update client magic icon </li><BR><BR>
	 */
	public final synchronized void stopEffectTask() {
		if (currentFuture != null) {
			// Cancel the task
			currentFuture.cancel(false);
			//ThreadPoolManager.getInstance().removeEffect(currentTask);

			currentFuture = null;

			if (isSelfEffectType() && getEffector() != null) {
				getEffector().removeEffect(this);
			} else if (getEffected() != null) {
				getEffected().removeEffect(this);
			}
		}
	}

	/**
	 * returns effect type
	 */
	public AbnormalType getType() {
		if (getTemplate().effectType != AbnormalType.NONE) {
			return getTemplate().effectType;
		}

		for (L2Effect e : effects) {
			if (e.getAbnormalType() != AbnormalType.NONE) {
				return e.getAbnormalType();
			}
		}

		return AbnormalType.NONE;
	}

	public long getEffectMask() {
		long mask = 0L;
		for (L2Effect e : effects) {
			mask |= e.getEffectMask();
		}

		return mask;
	}

	/**
	 * Notify started
	 */
	public boolean onStart() {
		if (visualEffect != null) {
			for (VisualEffect ve : visualEffect) {
				getEffected().startVisualEffect(ve);
			}
		}

		boolean canStart = true;
		boolean[] started = new boolean[effects.length];
		int i = 0;
		for (L2Effect effect : effects) {
			started[i] = effect.onStart();
			if (!started[i]) {
				canStart = false;
			}

			i++;
		}

		if (!canStart) {
			i = 0;
			for (L2Effect effect : effects) {
				if (started[i]) {
					effect.onExit();
				}

				i++;
			}

			if (visualEffect != null) {
				for (VisualEffect ve : visualEffect) {
					getEffected().stopVisualEffect(ve);
				}
			}
		}

		strikes = 0;
		debuffBlocks = 0;

		return canStart;
	}

	/**
	 * Cancel the effect in the the abnormal effect map of the effected Creature.<BR><BR>
	 */
	public void onExit() {
		for (L2Effect effect : effects) {
			effect.onExit();
		}

		if (visualEffect != null) {
			for (VisualEffect ve : visualEffect) {
				getEffected().stopVisualEffect(ve);
			}
		}
	}

	/**
	 * Cancel the effect in the the abnormal effect map of the effected Creature.<BR><BR>
	 */
	public boolean onActionTime() {
		boolean toReturn = true;
		for (L2Effect effect : effects) {
			if (!effect.onActionTime()) {
				toReturn = false;
			}
		}

		return toReturn;
	}

	public final void scheduleEffect() {
		switch (state) {
			case CREATED: {
				state = AbnormalState.ACTING;

				if (skill.isOffensive() && icon && getEffected() instanceof Player) {
					SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
					smsg.addSkillName(skill);
					getEffected().sendPacket(smsg);
				}

				if (duration != 0) {
					startEffectTask();
					return;
				}
				// effects not having count or period should start
				startConditionsCorrect = onStart();
			}
			case ACTING: {
				if (count > 0) {
					count--;
					if (getInUse()) { // effect has to be in use
						if (onActionTime() && startConditionsCorrect && count > 0) {
							return; // false causes effect to finish right away
						}
					} else if (count > 0) { // do not finish it yet, in case reactivated
						return;
					}
				}
				state = AbnormalState.FINISHING;
			}
			case FINISHING: {
				//If the time left is equal to zero, send the message
				if (count == 0 && icon && getEffected() instanceof Player) {
					SystemMessage smsg3 = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WORN_OFF);
					smsg3.addSkillName(skill);
					getEffected().sendPacket(smsg3);
				}
				// if task is null - stopEffectTask does not remove effect
				if (currentFuture == null && getEffected() != null) {
					getEffected().removeEffect(this);
				}
				// Stop the task of the L2Effect, remove it and update client magic icon
				stopEffectTask();

				// Cancel the effect in the the abnormal effect map of the Creature
				if (getInUse() || !(count > 1 || duration > 0)) {
					if (startConditionsCorrect) {
						onExit();
					}
				}

				if (skill.getAfterEffectId() > 0) {
					Skill skill = SkillTable.getInstance().getInfo(this.skill.getAfterEffectId(), this.skill.getAfterEffectLvl());
					if (skill != null) {
						getEffected().broadcastPacket(new MagicSkillUse(effected, skill.getId(), skill.getLevelHash(), 0, 0));
						getEffected().broadcastPacket(new MagicSkillLaunched(effected, skill.getId(), skill.getLevelHash()));
						skill.getEffects(getEffected(), getEffected());
					}
				}

				if (skill.getId() == 14571 && getEffected() instanceof Player) {
					((Player) getEffected()).decreaseBreathOfShilenDebuffLevel();
				}
				if (skill.getId() == 1570 && getEffected() instanceof Player && ((Player) getEffected()).hasIdentityCrisis()) {
					((Player) getEffected()).setHasIdentityCrisis(false);
				}
			}
		}
	}

	public Func[] getStatFuncs() {
		if (funcTemplates == null) {
			return emptyFunctionSet;
		}
		ArrayList<Func> funcs = new ArrayList<>(funcTemplates.length);

		Env env = new Env();
		env.player = getEffector();
		env.target = getEffected();
		env.skill = getSkill();
		Func f;

		for (FuncTemplate t : funcTemplates) {
			f = t.getFunc(this); // effect is owner
			if (f != null) {
				funcs.add(f);
			}
		}
		if (funcs.isEmpty()) {
			return emptyFunctionSet;
		}

		return funcs.toArray(new Func[funcs.size()]);
	}

	public final void addIcon(AbnormalStatusUpdate mi) {
		if (state != AbnormalState.ACTING) {
			return;
		}

		final ScheduledFuture<?> future = currentFuture;
		final Skill sk = getSkill();

		int levelHash = getLevelHash();
		if (sk.getId() >= 11139 && sk.getId() <= 11145) {
			levelHash = getLevel();
		}
		if (totalCount > 1) {
			mi.addEffect(sk.getId(), levelHash, comboId, (count - 1) * duration * 1000 + (int) future.getDelay(TimeUnit.MILLISECONDS));
		} else if (future != null) {
			mi.addEffect(sk.getId(), levelHash, comboId, (int) future.getDelay(TimeUnit.MILLISECONDS));
		} else if (duration == -1) {
			mi.addEffect(sk.getId(), levelHash, comboId, duration);
		}
	}

	public final void addIcon(AbnormalStatusUpdateFromTarget mi) {
		if (state != AbnormalState.ACTING) {
			return;
		}

		final ScheduledFuture<?> future = currentFuture;
		final Skill sk = getSkill();
		if (sk == null || effector == null || mi == null || future == null) {
			return;
		}

		int levelHash = getLevelHash();
		if (sk.getId() >= 11139 && sk.getId() <= 11145) {
			levelHash = getLevel();
		}
		if (totalCount > 1) {
			mi.addEffect(sk.getId(),
					levelHash,
					comboId,
					(count - 1) * duration * 1000 + (int) future.getDelay(TimeUnit.MILLISECONDS),
					effector.getObjectId());
		} else if (effector != null) {
			mi.addEffect(sk.getId(), levelHash, comboId, (int) future.getDelay(TimeUnit.MILLISECONDS), effector.getObjectId());
		} else if (duration == -1) {
			mi.addEffect(sk.getId(), levelHash, comboId, duration, effector.getObjectId());
		}
	}

	public final void addPartySpelledIcon(PartySpelled ps) {
		if (state != AbnormalState.ACTING) {
			return;
		}

		final ScheduledFuture<?> future = currentFuture;
		final Skill sk = getSkill();
		if (future != null) {
			ps.addPartySpelledEffect(sk.getId(), getLevelHash(), (int) future.getDelay(TimeUnit.MILLISECONDS));
		} else if (duration == -1) {
			ps.addPartySpelledEffect(sk.getId(), getLevelHash(), duration);
		}
	}

	public final void addOlympiadSpelledIcon(ExOlympiadSpelledInfo os) {
		if (state != AbnormalState.ACTING) {
			return;
		}

		final ScheduledFuture<?> future = currentFuture;
		final Skill sk = getSkill();
		if (future != null) {
			os.addEffect(sk.getId(), getLevelHash(), (int) future.getDelay(TimeUnit.MILLISECONDS));
		} else if (duration == -1) {
			os.addEffect(sk.getId(), getLevelHash(), duration);
		}
	}

	public int getLevel() {
		return getSkill().getLevel();
	}

	public int getEnchantRouteId() {
		return getSkill().getEnchantRouteId();
	}

	public int getEnchantLevel() {
		return getSkill().getEnchantLevel();
	}

	public int getLevelHash() {
		return getSkill().getLevelHash();
	}

	public int getPeriodStartTicks() {
		return periodStartTicks;
	}

	public AbnormalTemplate getTemplate() {
		return template;
	}

	public double getLandRate() {
		return landRate;
	}

	public int getComboId() {
		return comboId;
	}

	public L2Effect[] getEffects() {
		return effects;
	}

	public boolean canBeStolen() {
		return !(!effectCanBeStolen() || getType() == AbnormalType.MUTATE || getSkill().isPassive() ||
				getSkill().getTargetType() == SkillTargetType.TARGET_SELF || getSkill().isToggle() || getSkill().isDebuff() ||
				getSkill().isHeroSkill() || getSkill().getTransformId() > 0
				//|| (this.getSkill().isGMSkill() && getEffected().getInstanceId() == 0)
				|| getSkill().isPotion() && getSkill().getId() != 2274 && getSkill().getId() != 2341
				// Hardcode for now :<
				|| isHerbEffect() || !getSkill().canBeDispeled());
	}

	public boolean canBeShared() {
		return !(!effectCanBeStolen() || getType() == AbnormalType.MUTATE || getSkill().isPassive() || getSkill().isToggle() ||
				getSkill().isDebuff()
				//|| (this.getSkill().isGMSkill() && getEffected().getInstanceId() == 0)
				|| !getSkill().canBeDispeled());
	}

	/**
	 * Return true if effect itself can be stolen
	 *
	 * @return
	 */
	protected boolean effectCanBeStolen() {
		for (L2Effect effect : effects) {
			if (!effect.effectCanBeStolen()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return "L2Effect [_skill=" + skill + ", _state=" + state + ", _duration=" + duration + "]";
	}

	public boolean isSelfEffectType() {
		for (L2Effect effect : effects) {
			if (effect.isSelfEffectType()) {
				return true;
			}
		}

		return false;
	}

	public boolean isRemovedOnDamage(int damage) {
		if (damage > 0) {
			strikes++;
			blockedDamage += damage;
		}

		return getSkill().isRemovedOnDamage() || (getEffectMask() & EffectType.SLEEP.getMask()) != 0 ||
				(getEffectMask() & EffectType.FEAR.getMask()) != 0 ||
				getSkill().getStrikesToRemove() > 0 && (damage == 0 || strikes >= getSkill().getStrikesToRemove()) ||
				getSkill().getDamageToRemove() > 0 && (damage == 0 || blockedDamage >= getSkill().getDamageToRemove());
	}

	public boolean isRemovedOnDebuffBlock(boolean onDebuffBlock) {
		if (getSkill().isRemovedOnDebuffBlock()) {
			if (onDebuffBlock && getSkill().getDebuffBlocksToRemove() > 0) {
				debuffBlocks++;
				return debuffBlocks >= getSkill().getDebuffBlocksToRemove();
			}

			return true;
		}

		return false;
	}
}
