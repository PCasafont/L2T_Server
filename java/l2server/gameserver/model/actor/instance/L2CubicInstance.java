/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.model.actor.instance;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.handler.SkillHandler;
import l2server.gameserver.instancemanager.DuelManager;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.skills.L2SkillDrain;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.gameserver.templates.skills.L2SkillBehaviorType;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.gameserver.util.Util;
import l2server.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class L2CubicInstance
{
	// Type of Cubics
	public static final int STORM_CUBIC = 1;
	public static final int VAMPIRIC_CUBIC = 2;
	public static final int LIFE_CUBIC = 3;
	public static final int VIPER_CUBIC = 4;
	public static final int POLTERGEIST_CUBIC = 5;
	public static final int BINDING_CUBIC = 6;
	public static final int AQUA_CUBIC = 7;
	public static final int SPARK_CUBIC = 8;
	public static final int ATTRACT_CUBIC = 9;
	public static final int SMART_CUBIC_EVATEMPLAR = 10;
	public static final int SMART_CUBIC_SHILLIENTEMPLAR = 11;
	public static final int SMART_CUBIC_ARCANALORD = 12;
	public static final int SMART_CUBIC_ELEMENTALMASTER = 13;
	public static final int SMART_CUBIC_SPECTRALMASTER = 14;
	public static final int KNIGHT_CUBIC = 15;
	public static final int AVENGING_CUBIC = 16;
	public static final int FAIRY_OF_LIFE = 17;
	public static final int BUFF_CUBIC = 18;
	public static final int MIND_CUBIC = 19;
	public static final int PHANTOM_CUBIC = 20;
	public static final int HEX_CUBIC = 21;
	public static final int GUARDIAN_CUBIC = 22;

	// Max range of cubic skills
	// TODO: Check/fix the max range
	public static final int MAX_MAGIC_RANGE = 900;

	// Cubic skills
	public static final int SKILL_CUBIC_HEAL = 4051;
	public static final int SKILL_CUBIC_CURE = 5579;
	public static final int SKILL_BUFF_CUBIC_HEAL = 10082;
	public static final int SKILL_MIND_CUBIC_HEAL = 10084;

	protected L2PcInstance _owner;
	protected L2Character _target;

	protected int _id;
	protected int _matk;
	protected int _activationtime;
	protected int _activationchance;
	protected int _maxcount;
	protected int _currentcount;
	protected boolean _active;
	private boolean _givenByOther;

	protected List<L2Skill> _skills = new ArrayList<>();

	private Future<?> _disappearTask;
	private Future<?> _actionTask;

	public L2CubicInstance(L2PcInstance owner, int id, int level, int mAtk, int activationtime, int activationchance, int maxcount, int totallifetime, boolean givenByOther)
	{
		_owner = owner;
		_id = id;
		_matk = mAtk;
		_activationtime = activationtime * 1000;
		_activationchance = activationchance;
		_maxcount = maxcount;
		_currentcount = 0;
		_active = false;
		_givenByOther = givenByOther;

		switch (_id)
		{
			case STORM_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4049, level));
				break;
			case VAMPIRIC_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4050, level));
				break;
			case LIFE_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4051, level));
				doAction();
				break;
			case VIPER_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4052, level));
				break;
			case POLTERGEIST_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4053, level));
				_skills.add(SkillTable.getInstance().getInfo(4054, level));
				_skills.add(SkillTable.getInstance().getInfo(4055, level));
				break;
			case BINDING_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4164, level));
				break;
			case AQUA_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4165, level));
				break;
			case SPARK_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(4166, level));
				break;
			case ATTRACT_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(5115, level));
				_skills.add(SkillTable.getInstance().getInfo(5116, level));
				break;
			case SMART_CUBIC_ARCANALORD:
				// _skills.add(SkillTable.getInstance().getInfo(4049,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				_skills.add(SkillTable.getInstance().getInfo(4051, 7)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				_skills.add(SkillTable.getInstance().getInfo(4165, 9)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case SMART_CUBIC_ELEMENTALMASTER:
				_skills.add(SkillTable.getInstance().getInfo(4049, 8)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4165,9)); no animation
				_skills.add(SkillTable.getInstance().getInfo(4166, 9)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case SMART_CUBIC_SPECTRALMASTER:
				_skills.add(SkillTable.getInstance().getInfo(4049, 8)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				_skills.add(SkillTable.getInstance().getInfo(4052, 6)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4165,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case SMART_CUBIC_EVATEMPLAR:
				// _skills.add(SkillTable.getInstance().getInfo(4049,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				_skills.add(SkillTable.getInstance().getInfo(4053, 8)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				_skills.add(SkillTable.getInstance().getInfo(4165, 9)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case SMART_CUBIC_SHILLIENTEMPLAR:
				_skills.add(SkillTable.getInstance().getInfo(4049, 8)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4165,9)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				_skills.add(SkillTable.getInstance().getInfo(5115, 4)); // have animation
				// _skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// _skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case KNIGHT_CUBIC:
				//_skills.add(SkillTable.getInstance().getInfo(10055, 1));
				_skills.add(SkillTable.getInstance().getInfo(10056, level));
				//_skills.add(SkillTable.getInstance().getInfo(10057, 1));
				doAction();
				break;
			case AVENGING_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(11292, Math.min(level, 8)));
				_skills.add(SkillTable.getInstance().getInfo(11293, Math.min(level, 8)));
				_skills.add(SkillTable.getInstance().getInfo(11294, level));
				doAction();
				break;
			case FAIRY_OF_LIFE:
				_skills.add(SkillTable.getInstance().getInfo(11807, level));
				doAction();
				break;
			case BUFF_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(10082, level));
				doAction();
				break;
			case MIND_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(10084, level));
				doAction();
				break;
			case PHANTOM_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(10085, level));
				doAction();
				break;
			case HEX_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(10086, level));
				doAction();
				break;
			case GUARDIAN_CUBIC:
				_skills.add(SkillTable.getInstance().getInfo(10093, level));
				doAction();
				break;
		}
		_disappearTask = ThreadPoolManager.getInstance().scheduleGeneral(new Disappear(), totallifetime); // disappear
	}

	public synchronized void doAction()
	{
		if (_active)
		{
			return;
		}

		_active = true;

		switch (_id)
		{
			case AQUA_CUBIC:
			case BINDING_CUBIC:
			case SPARK_CUBIC:
			case STORM_CUBIC:
			case POLTERGEIST_CUBIC:
			case VAMPIRIC_CUBIC:
			case VIPER_CUBIC:
			case ATTRACT_CUBIC:
			case SMART_CUBIC_ARCANALORD:
			case SMART_CUBIC_ELEMENTALMASTER:
			case SMART_CUBIC_SPECTRALMASTER:
			case SMART_CUBIC_EVATEMPLAR:
			case SMART_CUBIC_SHILLIENTEMPLAR:
			case KNIGHT_CUBIC:
			case AVENGING_CUBIC:
			case FAIRY_OF_LIFE:
			case HEX_CUBIC:
			case PHANTOM_CUBIC:
			case GUARDIAN_CUBIC:
				_actionTask = ThreadPoolManager.getInstance()
						.scheduleEffectAtFixedRate(new Action(_activationchance), _activationtime / 2, _activationtime);
				break;

			case LIFE_CUBIC:
			case BUFF_CUBIC:
			case MIND_CUBIC:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Heal(), 0, _activationtime);
				break;
		}
	}

	public int getId()
	{
		return _id;
	}

	public L2PcInstance getOwner()
	{
		return _owner;
	}

	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		// TODO: Temporary now mcrit for cubics is the baseMCritRate of its owner
		return _owner.getTemplate().baseMCritRate;
	}

	public int getMAtk()
	{
		return _matk;
	}

	public void stopAction()
	{
		_target = null;

		if (_actionTask != null)
		{
			_actionTask.cancel(true);
			_actionTask = null;
		}

		_active = false;
	}

	public void cancelDisappear()
	{
		if (_disappearTask != null)
		{
			_disappearTask.cancel(true);
			_disappearTask = null;
		}
	}

	/**
	 * this sets the enemy target for a cubic
	 */
	public void getCubicTarget()
	{
		try
		{
			_target = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private class Action implements Runnable
	{
		@SuppressWarnings("unused")
		private int _chance;

		Action(int chance)
		{
			_chance = chance;
			// run task
		}

		@Override
		public void run()
		{
			try
			{
				boolean active = false;
				for (L2CubicInstance cubic : _owner.getCubics().values())
				{
					if (cubic == L2CubicInstance.this)
					{
						active = true;
					}
				}

				if (active)
				{
					active = _active && !_owner.isDead() && _owner.isOnline();
				}

				if (!active)
				{
					stopAction();
					_owner.delCubic(_id);
					_owner.broadcastUserInfo();
					cancelDisappear();
					return;
				}

				boolean hasActiveSummon = false;
				boolean cubicShouldStopIfNoAction = true;

				switch (_id)
				{
					case KNIGHT_CUBIC:
					case AVENGING_CUBIC:
					case HEX_CUBIC:
					case PHANTOM_CUBIC:
					case FAIRY_OF_LIFE:
					case GUARDIAN_CUBIC:
						cubicShouldStopIfNoAction = false;
						break;
				}

				for (L2SummonInstance summon : _owner.getSummons())
				{
					if (AttackStanceTaskManager.getInstance().getAttackStanceTask(summon))
					{
						hasActiveSummon = true;
					}
				}

				if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(_owner) && !hasActiveSummon &&
						cubicShouldStopIfNoAction)
				{
					if (_owner.getPet() != null)
					{
						if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(_owner.getPet()))
						{
							stopAction();
							return;
						}
					}
					else
					{
						stopAction();
						return;
					}
				}
				// The cubic has already reached its limit and it will stay idle until its lifetime ends.
				if (_maxcount > -1 && _currentcount >= _maxcount)
				{
					stopAction();
					return;
				}

				for (L2Skill sk : _skills)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(sk.getId(), sk.getLevel());
					//Broadcast.toGameMasters(skill.getName() + " has a reuse delay of " + skill.getReuseDelay());

					if (skill.getId() == SKILL_CUBIC_HEAL)
					{
						// friendly skill, so we look a target in owner's party
						cubicTargetForHeal();
					}
					else if (skill.getId() == 10056 || skill.getId() == 11292 || skill.getId() == 11807 ||
							skill.getId() == 10093)
					{
						_target = _owner;
					}
					else
					{
						// offensive skill, we look for an enemy target
						if (_owner.getTarget() instanceof L2Character)
						{
							_target = (L2Character) _owner.getTarget();
						}

						if (!_owner.isAbleToCastOnTarget(_target, skill, false))
						{
							_target = null;
						}

						if (_target != null && !Util.checkIfInRange(skill.getCastRange(), _owner, _target, false))
						{
							_target = null;
						}

						if (_target == null)
						{
							break;
						}
					}

					if (_owner.isSkillDisabled(skill.getReuseHashCode()))
					{
						continue;
					}

					int reuseDelay = skill.getReuseDelay();

					if (reuseDelay == 0)
					{
						reuseDelay = 10000;
					}

					_owner.disableSkill(skill, reuseDelay);

					//Broadcast.toGameMasters(_owner.getName() + " Cubic(" + _id + ") is casting " + skill.getName() + " on " + _target.getName() + ".");

					L2Character target = _target; // copy to avoid npe

					if (target != null && !target.isDead())
					{
						if (Config.DEBUG)
						{
							Log.info("L2CubicInstance: Action.run();");
							Log.info("Cubic Id: " + _id + " Target: " + target.getName() + " distance: " +
									Math.sqrt(target.getDistanceSq(_owner.getX(), _owner.getY(), _owner.getZ())));
						}

						_owner.broadcastPacket(
								new MagicSkillUse(_owner, target, skill.getId(), skill.getLevel(), 0, 0, 0));

						ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());

						L2Character[] targets = {target};

						L2SkillType type = skill.getSkillType();

						//Broadcast.toGameMasters("Cubic is using Skill " + skill.getName());
						if (type == L2SkillType.AGGDAMAGE)
						{
							if (Config.DEBUG)
							{
								Log.info("L2CubicInstance: Action.run() handler " + type);
							}

							useCubicDisabler(type, L2CubicInstance.this, skill, targets);
						}
						else if (type == L2SkillType.MDAM || type == L2SkillType.DRAIN)
						{
							if (Config.DEBUG)
							{
								Log.info("L2CubicInstance: Action.run() handler " + type);
							}

							useCubicMdam(L2CubicInstance.this, skill, targets);
						}
						else if (type == L2SkillType.DEBUFF)
						{
							if (Config.DEBUG)
							{
								Log.info("L2CubicInstance: Action.run() handler " + type);
							}

							useCubicContinuous(L2CubicInstance.this, skill, targets);
						}
						else if (type == L2SkillType.DRAIN)
						{
							if (Config.DEBUG)
							{
								Log.info("L2CubicInstance: Action.run() skill " + type);
							}

							((L2SkillDrain) skill).useCubicSkill(L2CubicInstance.this, targets);
						}
						else
						{
							handler.useSkill(_owner, skill, targets);
						}
					}

					// The cubic has done an action, increase the currentcount
					_currentcount++;

					if (skill.getSkillBehavior() != L2SkillBehaviorType.FRIENDLY)
					{
						break;
					}
				}
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "", e);
			}
		}
	}

	public void useCubicContinuous(L2CubicInstance activeCubic, L2Skill skill, L2Object[] targets)
	{
		for (L2Character target : (L2Character[]) targets)
		{
			if (target == null || target.isDead())
			{
				continue;
			}

			if (skill.isOffensive())
			{
				byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);

				boolean acted = Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld);

				if (!acted)
				{
					activeCubic.getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));

					continue;
				}
			}

			// if this is a debuff let the duel manager know about it
			// so the debuff can be removed after the duel
			// (player & target must be in the same duel)
			if (target instanceof L2PcInstance && ((L2PcInstance) target).isInDuel() &&
					skill.getSkillType() == L2SkillType.DEBUFF &&
					activeCubic.getOwner().getDuelId() == ((L2PcInstance) target).getDuelId())
			{
				DuelManager dm = DuelManager.getInstance();

				for (L2Abnormal debuff : skill.getEffects(activeCubic.getOwner(), target))
				{
					if (debuff != null)
					{
						dm.onBuff((L2PcInstance) target, debuff);
					}
				}
			}
			else
			{
				skill.getEffects(activeCubic, target, null);
			}
		}
	}

	public void useCubicMdam(L2CubicInstance activeCubic, L2Skill skill, L2Object[] targets)
	{
		for (L2Character target : (L2Character[]) targets)
		{
			if (target == null)
			{
				continue;
			}

			if (target.isAlikeDead())
			{
				if (target instanceof L2PcInstance)
				{
					target.stopFakeDeath(true);
				}
				else
				{
					continue;
				}
			}

			boolean mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(target, skill));

			byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);

			int damage = (int) Formulas.calcMagicDam(activeCubic, target, skill, mcrit, shld);

			/*
			 *  If target is reflecting the skill then no damage is done
			 *  Ignoring vengance-like reflections
			 */
			if ((Formulas.calcSkillReflect(target, skill) & Formulas.SKILL_REFLECT_EFFECTS) > 0)
			{
				damage = 0;
			}

			if (Config.DEBUG)
			{
				Log.info("L2SkillMdam: useCubicSkill() -> damage = " + damage);
			}

			if (damage > 0)
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();

					target.breakCast();
				}

				activeCubic.getOwner().sendDamageMessage(target, damage, mcrit, false, false);

				if (skill.hasEffects())
				{
					// activate attacked effects, if any
					target.stopSkillEffects(skill.getId());

					if (target.getFirstEffect(skill) != null)
					{
						target.removeEffect(target.getFirstEffect(skill));
					}

					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld))
					{
						skill.getEffects(activeCubic, target, null);
					}
				}

				target.reduceCurrentHp(damage, activeCubic.getOwner(), skill);
			}
		}
	}

	public void useCubicDisabler(L2SkillType type, L2CubicInstance activeCubic, L2Skill skill, L2Object[] targets)
	{
		if (Config.DEBUG)
		{
			Log.info("Disablers: useCubicSkill()");
		}

		for (L2Character target : (L2Character[]) targets)
		{
			if (target == null || target.isDead()) // bypass if target is null or dead
			{
				continue;
			}

			byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);

			switch (type)
			{
				case DEBUFF:
				{
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld))
					{
						// if this is a debuff let the duel manager know about it
						// so the debuff can be removed after the duel
						// (player & target must be in the same duel)
						if (target instanceof L2PcInstance && ((L2PcInstance) target).isInDuel() &&
								skill.getSkillType() == L2SkillType.DEBUFF &&
								activeCubic.getOwner().getDuelId() == ((L2PcInstance) target).getDuelId())
						{
							DuelManager dm = DuelManager.getInstance();

							for (L2Abnormal debuff : skill.getEffects(activeCubic.getOwner(), target))
							{
								if (debuff != null)
								{
									dm.onBuff((L2PcInstance) target, debuff);
								}
							}
						}
						else
						{
							skill.getEffects(activeCubic, target, null);
						}

						if (Config.DEBUG)
						{
							Log.info("Disablers: useCubicSkill() -> success");
						}
					}
					else
					{
						if (Config.DEBUG)
						{
							Log.info("Disablers: useCubicSkill() -> failed");
						}
					}

					break;
				}

				case CANCEL_DEBUFF:
				{
					L2Abnormal[] effects = target.getAllEffects();

					if (effects == null || effects.length == 0)
					{
						break;
					}

					int count = skill.getMaxNegatedEffects() > 0 ? 0 : -2;

					for (L2Abnormal e : effects)
					{
						if (e.getSkill().isDebuff() && count < skill.getMaxNegatedEffects())
						{
							// Do not remove raid curse skills
							if (e.getSkill().getId() != 4215 && e.getSkill().getId() != 4515 &&
									e.getSkill().getId() != 4082)
							{
								e.exit();

								if (count > -1)
								{
									count++;
								}
							}
						}
					}

					break;
				}

				case AGGDAMAGE:
				{
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld))
					{
						if (target instanceof L2Attackable)
						{
							target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeCubic.getOwner(),
									(int) (150 * skill.getPower() / (target.getLevel() + 7)));
						}

						skill.getEffects(activeCubic, target, null);

						if (Config.DEBUG)
						{
							Log.info("Disablers: useCubicSkill() -> success");
						}
					}
					else
					{
						if (Config.DEBUG)
						{
							Log.info("Disablers: useCubicSkill() -> failed");
						}
					}

					break;
				}
			}
		}
	}

	/**
	 * returns true if the target is inside of the owner's max Cubic range
	 */
	public boolean isInCubicRange(L2Character owner, L2Character target)
	{
		if (owner == null || target == null)
		{
			return false;
		}

		int x, y, z;

		// temporary range check until real behavior of cubics is known/coded
		int range = MAX_MAGIC_RANGE;

		x = owner.getX() - target.getX();

		y = owner.getY() - target.getY();

		z = owner.getZ() - target.getZ();

		return x * x + y * y + z * z <= range * range;
	}

	/**
	 * this sets the friendly target for a cubic
	 */
	public void cubicTargetForHeal()
	{
		L2Character target = null;

		double percentleft = 100.0;

		L2Party party = _owner.getParty();

		// if owner is in a duel but not in a party duel, then it is the same as he does not have a
		// party
		if (_owner.isInDuel())
		{
			if (!DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())
			{
				party = null;
			}
		}

		if (party != null && !_owner.isInOlympiadMode())
		{
			// Get all visible objects in a spheric area near the L2Character
			// Get a list of Party Members
			List<L2PcInstance> partyList = party.getPartyMembers();

			for (L2PcInstance partyMember : partyList)
			{
				if (!partyMember.isDead())
				{
					// if party member not dead, check if he is in castrange of heal cubic
					if (isInCubicRange(_owner, partyMember))
					{
						// member is in cubic casting range, check if he need heal and if he have
						// the lowest HP
						if (partyMember.getCurrentHp() < partyMember.getMaxHp())
						{
							if (percentleft > partyMember.getCurrentHp() / partyMember.getMaxHp())
							{
								percentleft = partyMember.getCurrentHp() / partyMember.getMaxHp();

								target = partyMember;
							}
						}
					}
				}

				if (partyMember.getPet() != null)
				{
					if (partyMember.getPet().isDead())
					{
						continue;
					}

					// if party member's pet not dead, check if it is in castrange of heal cubic
					if (!isInCubicRange(_owner, partyMember.getPet()))
					{
						continue;
					}

					// member's pet is in cubic casting range, check if he need heal and if he have
					// the lowest HP
					if (partyMember.getPet().getCurrentHp() < partyMember.getPet().getMaxHp() &&
							percentleft > partyMember.getPet().getCurrentHp() / partyMember.getPet().getMaxHp())
					{
						percentleft = partyMember.getPet().getCurrentHp() / partyMember.getPet().getMaxHp();

						target = partyMember.getPet();
					}
				}

				for (L2SummonInstance summon : partyMember.getSummons())
				{
					if (summon.isDead())
					{
						continue;
					}

					// if party member's pet not dead, check if it is in castrange of heal cubic
					if (!isInCubicRange(_owner, summon))
					{
						continue;
					}

					// member's pet is in cubic casting range, check if he need heal and if he have
					// the lowest HP
					if (summon.getCurrentHp() < summon.getMaxHp() &&
							percentleft > summon.getCurrentHp() / summon.getMaxHp())
					{
						percentleft = summon.getCurrentHp() / summon.getMaxHp();

						target = summon;
					}
				}
			}
		}
		else
		{
			if (_owner.getCurrentHp() < _owner.getMaxHp())
			{
				percentleft = _owner.getCurrentHp() / _owner.getMaxHp();

				target = _owner;
			}

			if (_owner.getPet() != null && !_owner.getPet().isDead() &&
					_owner.getPet().getCurrentHp() < _owner.getPet().getMaxHp() &&
					percentleft > _owner.getPet().getCurrentHp() / _owner.getPet().getMaxHp() &&
					isInCubicRange(_owner, _owner.getPet()))
			{
				percentleft = _owner.getPet().getCurrentHp() / _owner.getPet().getMaxHp();

				target = _owner.getPet();
			}

			for (L2SummonInstance summon : _owner.getSummons())
			{
				if (!summon.isDead() && summon.getCurrentHp() < summon.getMaxHp() &&
						percentleft > summon.getCurrentHp() / summon.getMaxHp() && isInCubicRange(_owner, summon))
				{
					percentleft = summon.getCurrentHp() / summon.getMaxHp();

					target = summon;
				}
			}
		}

		_target = target;
	}

	public boolean givenByOther()
	{
		return _givenByOther;
	}

	private class Heal implements Runnable
	{
		Heal()
		{
			// run task
		}

		@Override
		public void run()
		{
			boolean active = false;
			for (L2CubicInstance cubic : _owner.getCubics().values())
			{
				if (cubic == L2CubicInstance.this)
				{
					active = true;
				}
			}

			if (active)
			{
				active = _active && !_owner.isDead() && _owner.isOnline();
			}

			if (!active)
			{
				stopAction();
				_owner.delCubic(_id);
				_owner.broadcastUserInfo();
				cancelDisappear();
				return;
			}

			try
			{
				L2Skill skill = null;

				for (L2Skill sk : _skills)
				{
					if (sk != null && (sk.getId() == SKILL_CUBIC_HEAL || sk.getId() == SKILL_BUFF_CUBIC_HEAL ||
							sk.getId() == SKILL_MIND_CUBIC_HEAL))
					{
						skill = sk;

						break;
					}
				}

				if (skill != null)
				{
					L2Character target = _target;

					if (_id != BUFF_CUBIC && _id != MIND_CUBIC)
					{
						cubicTargetForHeal();
					}
					else
					{
						target = _owner;
					}

					if (target != null && !target.isDead())
					{
						boolean cubicShouldStopIfNoAction = !(_id == BUFF_CUBIC || _id == MIND_CUBIC);

						if (target.getMaxHp() - target.getCurrentHp() > skill.getPower() || !cubicShouldStopIfNoAction)
						{
							L2Character[] targets = {target};

							ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());

							if (handler != null)
							{
								handler.useSkill(_owner, skill, targets);
							}
							else
							{
								skill.useSkill(_owner, targets);
							}

							MagicSkillUse msu =
									new MagicSkillUse(_owner, target, skill.getId(), skill.getLevel(), 0, 0, 0);

							_owner.broadcastPacket(msu);
						}
					}
				}
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "", e);
			}
		}
	}

	private class Disappear implements Runnable
	{
		Disappear()
		{
			// run task
		}

		@Override
		public void run()
		{
			stopAction();

			_owner.delCubic(_id);

			_owner.broadcastUserInfo();
		}
	}
}
