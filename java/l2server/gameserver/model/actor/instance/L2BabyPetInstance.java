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

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.PetDataTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2PetData.L2PetSkillLearn;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.SkillHolder;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

/**
 * This class ...
 *
 * @version $Revision: 1.15.2.10.2.16 $ $Date: 2005/04/06 16:13:40 $
 */
public final class L2BabyPetInstance extends L2PetInstance
{
	private static final int BUFF_CONTROL = 5771;
	private static final int AWAKENING = 5753;

	private ArrayList<SkillHolder> _buffs = null;
	private SkillHolder _majorHeal = null;
	private SkillHolder _minorHeal = null;
	private SkillHolder _recharge = null;

	private Future<?> _castTask;

	private boolean _bufferMode = true;

	public L2BabyPetInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control)
	{
		super(objectId, template, owner, control);
		setInstanceType(InstanceType.L2BabyPetInstance);
	}

	public L2BabyPetInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control, byte level)
	{
		super(objectId, template, owner, control, level);
		setInstanceType(InstanceType.L2BabyPetInstance);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		L2Skill skill;
		double healPower = 0;
		for (L2PetSkillLearn psl : PetDataTable.getInstance().getPetData(getNpcId()).getAvailableSkills())
		{
			int id = psl.getId();
			int lvl = PetDataTable.getInstance().getPetData(getNpcId()).getAvailableLevel(id, getLevel());
			if (lvl == 0) // not enough pet lvl
			{
				continue;
			}
			skill = SkillTable.getInstance().getInfo(id, lvl);
			if (skill != null)
			{
				if (skill.getId() == BUFF_CONTROL || skill.getId() == AWAKENING)
				{
					continue;
				}

				switch (skill.getSkillType())
				{
					case HEAL:
						if (healPower == 0)
						{
							// set both heal types to the same skill
							_majorHeal = new SkillHolder(skill);
							_minorHeal = _majorHeal;
							healPower = skill.getPower();
						}
						else
						{
							// another heal skill found - search for most powerful
							if (skill.getPower() > healPower)
							{
								_majorHeal = new SkillHolder(skill);
							}
							else
							{
								_minorHeal = new SkillHolder(skill);
							}
						}
						break;
					case BUFF:
						if (_buffs == null)
						{
							_buffs = new ArrayList<>();
						}
						_buffs.add(new SkillHolder(skill));
						break;
					case MANAHEAL:
					case MANARECHARGE:
					case MANA_BY_LEVEL:
						_recharge = new SkillHolder(skill);
						break;
				}
			}
		}
		startCastTask();
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		stopCastTask();
		abortCast();
		return true;
	}

	@Override
	public synchronized void unSummon(L2PcInstance owner)
	{
		stopCastTask();
		abortCast();
		super.unSummon(owner);
	}

	@Override
	public void doRevive()
	{
		super.doRevive();
		startCastTask();
	}

	@Override
	public void onDecay()
	{
		super.onDecay();

		if (_buffs != null)
		{
			_buffs.clear();
		}
	}

	private void startCastTask()
	{
		if ((_majorHeal != null || _buffs != null || _recharge != null) && _castTask == null &&
				!isDead()) // cast task is not yet started and not dead (will start on revive)
		{
			_castTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new CastTask(this), 3000, 1000);
		}
	}

	public void switchMode()
	{
		_bufferMode = !_bufferMode;
	}

	private void stopCastTask()
	{
		if (_castTask != null)
		{
			_castTask.cancel(false);
			_castTask = null;
		}
	}

	protected void castSkill(L2Skill skill)
	{
		// casting automatically stops any other action (such as autofollow or a move-to).
		// We need to gather the necessary info to restore the previous state.
		final boolean previousFollowStatus = getFollowStatus();

		// pet not following and owner outside cast range
		if (!previousFollowStatus && !isInsideRadius(getOwner(), skill.getCastRange(), true, true))
		{
			return;
		}

		setTarget(getOwner());
		useMagic(skill, false, false);

		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1);
		msg.addSkillName(skill);
		getOwner().sendPacket(msg);

		// calling useMagic changes the follow status, if the babypet actually casts
		// (as opposed to failing due some factors, such as too low MP, etc).
		// if the status has actually been changed, revert it.  Else, allow the pet to
		// continue whatever it was trying to do.
		// NOTE: This is important since the pet may have been told to attack a target.
		// reverting the follow status will abort this attack!  While aborting the attack
		// in order to heal is natural, it is not acceptable to abort the attack on its own,
		// merely because the timer stroke and without taking any other action...
		if (previousFollowStatus != getFollowStatus())
		{
			setFollowStatus(previousFollowStatus);
		}
	}

	private class CastTask implements Runnable
	{
		private final L2BabyPetInstance _baby;
		private List<L2Skill> _currentBuffs = new ArrayList<>();

		public CastTask(L2BabyPetInstance baby)
		{
			_baby = baby;
		}

		@Override
		public void run()
		{
			L2PcInstance owner = _baby.getOwner();

			// if the owner is dead, merely wait for the owner to be resurrected
			// if the pet is still casting from the previous iteration, allow the cast to complete...
			if (owner != null && !owner.isDead() && !owner.isInvul() && !_baby.isCastingNow() && !_baby.isBetrayed() &&
					!_baby.isMuted() && !_baby.isOutOfControl() && _bufferMode &&
					_baby.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST)
			{
				L2Skill skill = null;

				if (_majorHeal != null)
				{
					/*
                      if the owner's HP is more than 80%, do nothing.
                      if the owner's HP is very low (less than 20%) have a high chance for strong heal
                      otherwise, have a low chance for weak heal
                     */
					final double hpPercent = owner.getCurrentHp() / owner.getMaxHp();
					final boolean isImprovedBaby = PetDataTable.isImprovedBaby(getNpcId());
					if (isImprovedBaby && hpPercent < 0.3 || !isImprovedBaby && hpPercent < 0.15)
					{
						skill = _majorHeal.getSkill();
						if (!_baby.isSkillDisabled(skill) && Rnd.get(100) <= 75)
						{
							if (_baby.getCurrentMp() >= skill.getMpConsume())
							{
								castSkill(skill);
								return;
							}
						}
					}
					else if (_majorHeal.getSkill() != _minorHeal.getSkill() &&
							(isImprovedBaby && hpPercent < 0.7 || !isImprovedBaby && hpPercent < 0.8))
					{
						//Cast _minorHeal only if it's different than _majorHeal, then pet has two heals available.
						skill = _minorHeal.getSkill();
						if (!_baby.isSkillDisabled(skill) && Rnd.get(100) <= 25)
						{
							if (_baby.getCurrentMp() >= skill.getMpConsume())
							{
								castSkill(skill);
								return;
							}
						}
					}
				}

				if (_baby.getFirstEffect(BUFF_CONTROL) == null) // Buff Control is not active
				{
					// searching for usable buffs
					if (_buffs != null && !_buffs.isEmpty())
					{
						for (SkillHolder i : _buffs)
						{
							if (i == null)
							{
								continue;
							}

							skill = i.getSkill();

							if (skill.getTargetType() == L2SkillTargetType.TARGET_SELF &&
									_baby.getFirstEffect(skill) != null)
							{
								continue;
							}

							if (_baby.isSkillDisabled(skill))
							{
								continue;
							}
							if (_baby.getCurrentMp() >= skill.getMpConsume())
							{
								_currentBuffs.add(skill);
							}
						}
					}

					// buffs found, checking owner buffs
					if (!_currentBuffs.isEmpty())
					{
						L2Abnormal[] effects = owner.getAllEffects();
						Iterator<L2Skill> iter;
						L2Skill currentSkill;
						for (L2Abnormal e : effects)
						{
							if (e == null)
							{
								continue;
							}

							currentSkill = e.getSkill();
							// skipping debuffs, passives, toggles
							if (currentSkill.isDebuff() || currentSkill.isPassive() || currentSkill.isToggle())
							{
								continue;
							}

							// if buff does not need to be casted - remove it from list
							iter = _currentBuffs.iterator();
							while (iter.hasNext())
							{
								skill = iter.next();
								if (currentSkill.getId() == skill.getId() &&
										currentSkill.getLevel() >= skill.getLevel())
								{
									iter.remove();
								}
								else
								{
									// effect with same stacktype and greater or equal stackorder
									if (skill.hasEffects() && e.getStackLvl() >= skill.getEffectTemplates()[0].stackLvl)
									{
										for (String stackType : skill.getEffectTemplates()[0].stackType)
										{
											boolean found = false;
											if (!stackType.equals("none"))
											{
												for (String stackType2 : e.getStackType())
												{
													if (stackType.equals(stackType2))
													{
														iter.remove();
														found = true;
														break;
													}
												}
											}

											if (found)
											{
												break;
											}
										}
									}
								}
							}
							// no more buffs in list
							if (_currentBuffs.isEmpty())
							{
								break;
							}
						}
						// buffs list ready, casting random
						if (!_currentBuffs.isEmpty())
						{
							castSkill(_currentBuffs.get(Rnd.get(_currentBuffs.size())));
							_currentBuffs.clear();
							return;
						}
					}
				}

				// buffs/heal not casted, trying recharge, if exist
				if (_recharge != null && owner.isInCombat() // recharge casted only if owner in combat stance
						&& owner.getCurrentMp() / owner.getMaxMp() < 0.6 && Rnd.get(100) <= 60)
				{
					skill = _recharge.getSkill();
					if (!_baby.isSkillDisabled(skill) && _baby.getCurrentMp() >= skill.getMpConsume())
					{
						castSkill(skill);
					}
				}
			}
		}
	}
}
