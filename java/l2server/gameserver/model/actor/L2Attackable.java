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

package l2server.gameserver.model.actor;

import gnu.trove.TIntIntHashMap;
import l2server.Config;
import l2server.gameserver.ItemsAutoDestroy;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.TimeController;
import l2server.gameserver.ai.*;
import l2server.gameserver.datatables.*;
import l2server.gameserver.datatables.EventDroplist.DateDrop;
import l2server.gameserver.datatables.GlobalDropTable.GlobalDropCategory;
import l2server.gameserver.instancemanager.CursedWeaponsManager;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.instance.*;
import l2server.gameserver.model.actor.knownlist.AttackableKnownList;
import l2server.gameserver.model.actor.status.AttackableStatus;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.item.L2EtcItemType;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class L2Attackable extends L2Npc
{
	private boolean _isRaid = false;
	private boolean _isRaidMinion = false;
	private boolean _champion = false;

	/**
	 * This class contains all AggroInfo of the L2Attackable against the attacker L2Character.
	 * <p>
	 * Data:
	 * attacker : The attacker L2Character concerned by this AggroInfo of this L2Attackable
	 * hate : Hate level of this L2Attackable against the attacker L2Character (hate = damage)
	 * damage : Number of damages that the attacker L2Character gave to this L2Attackable
	 */
	public static final class AggroInfo
	{
		private final L2Character _attacker;
		private int _hate = 0;
		private int _damage = 0;

		public AggroInfo(L2Character pAttacker)
		{
			_attacker = pAttacker;
		}

		public final L2Character getAttacker()
		{
			return _attacker;
		}

		public final int getHate()
		{
			return _hate;
		}

		public final int checkHate(L2Attackable owner)
		{
			if (_attacker.isAlikeDead() || !_attacker.isVisible() || !owner.getKnownList().knowsObject(_attacker))
			{
				_hate = 0;
			}

			return _hate;
		}

		public final void addHate(int value)
		{
			_hate = (int) Math.min(_hate + (long) value, 999999999);
		}

		public final void stopHate()
		{
			_hate = 0;
		}

		public final int getDamage()
		{
			return _damage;
		}

		public final void addDamage(int value)
		{
			_damage = (int) Math.min(_damage + (long) value, 999999999);
		}

		@Override
		public final boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}

			if (obj instanceof AggroInfo)
			{
				return ((AggroInfo) obj).getAttacker() == _attacker;
			}

			return false;
		}

		@Override
		public final int hashCode()
		{
			return _attacker.getObjectId();
		}
	}

	/**
	 * This class contains all RewardInfo of the L2Attackable against the any attacker L2Character, based on amount of damage done.
	 * <p>
	 * Data:
	 * attacker : The attacker L2Character concerned by this RewardInfo of this L2Attackable
	 * dmg : Total amount of damage done by the attacker to this L2Attackable (summon + own)
	 */
	protected static final class RewardInfo
	{
		protected L2Character _attacker;

		protected int _dmg = 0;

		public RewardInfo(L2Character pAttacker, int pDmg)
		{
			_attacker = pAttacker;
			_dmg = pDmg;
		}

		public void addDamage(int pDmg)
		{
			_dmg += pDmg;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}

			if (obj instanceof RewardInfo)
			{
				return ((RewardInfo) obj)._attacker == _attacker;
			}

			return false;
		}

		@Override
		public int hashCode()
		{
			return _attacker.getObjectId();
		}
	}

	/**
	 * This class contains all AbsorberInfo of the L2Attackable against the absorber L2Character.
	 * <p>
	 * Data:
	 * absorber : The attacker L2Character concerned by this AbsorberInfo of this L2Attackable
	 */
	public static final class AbsorberInfo
	{
		public int _objId;
		public double _absorbedHP;

		AbsorberInfo(int objId, double pAbsorbedHP)
		{
			_objId = objId;
			_absorbedHP = pAbsorbedHP;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}

			if (obj instanceof AbsorberInfo)
			{
				return ((AbsorberInfo) obj)._objId == _objId;
			}

			return false;
		}

		@Override
		public int hashCode()
		{
			return _objId;
		}
	}

	public static final class RewardItem
	{
		protected int _itemId;

		protected long _count;

		public RewardItem(int itemId, long count)
		{
			_itemId = itemId;
			_count = count;
		}

		public int getItemId()
		{
			return _itemId;
		}

		public long getCount()
		{
			return _count;
		}
	}

	private ConcurrentHashMap<L2Character, AggroInfo> _aggroList = new ConcurrentHashMap<>();

	public final ConcurrentHashMap<L2Character, AggroInfo> getAggroList()
	{
		return _aggroList;
	}

	private boolean _isReturningToSpawnPoint = false;

	public final boolean isReturningToSpawnPoint()
	{
		return _isReturningToSpawnPoint;
	}

	public final void setisReturningToSpawnPoint(boolean value)
	{
		_isReturningToSpawnPoint = value;
	}

	private boolean _canReturnToSpawnPoint = true;

	public final boolean canReturnToSpawnPoint()
	{
		return _canReturnToSpawnPoint;
	}

	public final void setCanReturnToSpawnPoint(boolean value)
	{
		_canReturnToSpawnPoint = value;
	}

	public boolean canSeeThroughSilentMove()
	{
		return getTemplate().CanSeeThroughSilentMove;
	}

	public void setCanSeeThroughSilentMove(boolean val)
	{
		if (!getTemplate().CanSeeThroughSilentMove)
		{
			getTemplate().CanSeeThroughSilentMove = true;
		}
	}

	private RewardItem[] _sweepItems;

	private RewardItem[] _harvestItems;
	private boolean _seeded;
	private int _seedType = 0;
	private int _seederObjId = 0;

	private boolean _overhit;

	private double _overhitDamage;

	private L2Character _overhitAttacker;

	private L2CommandChannel _firstCommandChannelAttacked = null;
	private CommandChannelTimer _commandChannelTimer = null;
	private long _commandChannelLastAttack = 0;

	private boolean _absorbed;

	private ConcurrentHashMap<Integer, AbsorberInfo> _absorbersList = new ConcurrentHashMap<>();

	private boolean _mustGiveExpSp;

	/**
	 * True if a Dwarf has used Spoil on this L2NpcInstance
	 */
	private boolean _isSpoil = false;

	private int _isSpoiledBy = 0;

	protected int _onKillDelay = 3000;

	/**
	 * Constructor of L2Attackable (use L2Character and L2NpcInstance constructor).
	 * <p>
	 * Actions:
	 * Call the L2Character constructor to set the _template of the L2Attackable (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)
	 * Set the name of the L2Attackable
	 * Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it
	 *
	 * @param objectId Identifier of the object to initialized
	 */
	public L2Attackable(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2Attackable);
		setIsInvul(false);
		_mustGiveExpSp = true;
	}

	@Override
	public AttackableKnownList getKnownList()
	{
		return (AttackableKnownList) super.getKnownList();
	}

	@Override
	public void initKnownList()
	{
		setKnownList(new AttackableKnownList(this));
	}

	@Override
	public AttackableStatus getStatus()
	{
		return (AttackableStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		setStatus(new AttackableStatus(this));
	}

	/**
	 * Return the L2Character AI of the L2Attackable and if its null create a new one.
	 */
	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai;

		if (ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new L2AttackableAI(new AIAccessor());
				}

				return _ai;
			}
		}
		return ai;
	}

	/**
	 * Not used.
	 * get condition to hate, actually isAggressive() is checked by monster and karma by guards in motheds that overwrite this one.
	 *
	 * @deprecated
	 */
	@Deprecated
	public boolean getCondition2(L2Character target)
	{
		if (target instanceof L2NpcInstance || target instanceof L2DoorInstance)
		{
			return false;
		}

		if (target.isAlikeDead() || !isInsideRadius(target, getAggroRange(), false, false) ||
				Math.abs(getZ() - target.getZ()) > 100)
		{
			return false;
		}

		return !target.isInvul();
	}

	public void useMagic(L2Skill skill)
	{
		if (skill == null || isAlikeDead())
		{
			return;
		}

		if (skill.isPassive())
		{
			return;
		}

		if (isCastingNow())
		{
			return;
		}

		if (isSkillDisabled(skill))
		{
			return;
		}

		if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			return;
		}

		if (getCurrentHp() <= skill.getHpConsume())
		{
			return;
		}

		if (skill.isMagic())
		{
			if (isMuted())
			{
				return;
			}
		}
		else
		{
			if (isPhysicalMuted())
			{
				return;
			}
		}

		L2Object target = skill.getFirstOfTargetList(this);
		if (target == null)
		{
			return;
		}

		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}

	/**
	 * Reduce the current HP of the L2Attackable.
	 *
	 * @param damage   The HP decrease value
	 * @param attacker The L2Character who attacks
	 */
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(damage, attacker, true, false, skill);
	}

	/**
	 * Reduce the current HP of the L2Attackable, update its _aggroList and launch the doDie Task if necessary.
	 *
	 * @param attacker The L2Character who attacks
	 * @param awake    The awake state (If True : stop sleeping)
	 */
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (isRaid() && !isMinion() && attacker != null && attacker.getParty() != null &&
				attacker.getParty().isInCommandChannel() &&
				attacker.getParty().getCommandChannel().meetRaidWarCondition(this))
		{
			if (_firstCommandChannelAttacked == null) //looting right isn't set
			{
				synchronized (this)
				{
					if (_firstCommandChannelAttacked == null)
					{
						_firstCommandChannelAttacked = attacker.getParty().getCommandChannel();
						if (_firstCommandChannelAttacked != null)
						{
							_commandChannelTimer = new CommandChannelTimer(this);
							_commandChannelLastAttack = System.currentTimeMillis();
							ThreadPoolManager.getInstance()
									.scheduleGeneral(_commandChannelTimer, 10000); // check for last attack
							_firstCommandChannelAttacked.broadcastToChannelMembers(
									new CreatureSay(0, Say2.PARTYROOM_ALL, "",
											"You have looting rights!")); //TODO: retail msg
						}
					}
				}
			}
			else if (attacker.getParty().getCommandChannel().equals(_firstCommandChannelAttacked)) //is in same channel
			{
				_commandChannelLastAttack = System.currentTimeMillis(); // update last attack time
			}
		}

		if (this instanceof L2GuardInstance && attacker instanceof L2Playable && isInvul(attacker))
		{
			return;
		}

		// Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList
		if (attacker != null)
		{
			addDamage(attacker, (int) damage, skill);
		}

		// If this L2Attackable is a L2MonsterInstance and it has spawned minions, call its minions to battle
		if (this instanceof L2MonsterInstance)
		{
			L2MonsterInstance master = (L2MonsterInstance) this;

			if (master.hasMinions())
			{
				master.getMinionList().onAssist(this, attacker);
			}

			master = master.getLeader();
			if (master != null && master.hasMinions())
			{
				master.getMinionList().onAssist(this, attacker);
			}
		}
		// Reduce the current HP of the L2Attackable and launch the doDie Task if necessary
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}

	public void setMustRewardExpSp(boolean value)
	{
		_mustGiveExpSp = value;
	}

	public boolean getMustRewardExpSP()
	{
		return _mustGiveExpSp;
	}

	/**
	 * Kill the L2Attackable (the corpse disappeared after 7 seconds), distribute rewards (EXP, SP, Drops...) and notify Quest Engine.
	 * <p>
	 * Actions:
	 * Distribute Exp and SP rewards to L2PcInstance (including Summon owner) that hit the L2Attackable and to their Party members
	 * Notify the Quest Engine of the L2Attackable death if necessary
	 * Kill the L2NpcInstance (the corpse disappeared after 7 seconds)
	 * <p>
	 * Caution: This method DOESN'T GIVE rewards to L2PetInstance
	 *
	 * @param killer The L2Character that has killed the L2Attackable
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		// Kill the L2NpcInstance (the corpse disappeared after 7 seconds)
		if (!super.doDie(killer))
		{
			return false;
		}

		if (killer instanceof L2Playable)
		{
			final L2PcInstance player = killer.getActingPlayer();

			if (player.getHatersAmount() != 0)
			{
				player.setHatersAmount(player.getHatersAmount() - 1);
			}
		}

		// Notify the Quest Engine of the L2Attackable death if necessary
		try
		{
			L2PcInstance player = null;
			if (killer != null)
			{
				player = killer.getActingPlayer();
			}

			if (player != null)
			{
				if (getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL) != null)
				{
					for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL))
					{
						ThreadPoolManager.getInstance()
								.scheduleEffect(new OnKillNotifyTask(this, quest, player, killer instanceof L2Summon),
										Math.min(_onKillDelay, quest.getOnKillDelay(getNpcId())));
					}
				}
			}
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "", e);
		}

		setCanBeSweeped(true);
		return true;
	}

	protected static class OnKillNotifyTask implements Runnable
	{
		private L2Attackable _attackable;
		private Quest _quest;
		private L2PcInstance _killer;
		private boolean _isPet;

		public OnKillNotifyTask(L2Attackable attackable, Quest quest, L2PcInstance killer, boolean isPet)
		{
			_attackable = attackable;
			_quest = quest;
			_killer = killer;
			_isPet = isPet;
		}

		@Override
		public void run()
		{
			_quest.notifyKill(_attackable, _killer, _isPet);
		}
	}

	/**
	 * Distribute Exp and SP rewards to L2PcInstance (including Summon owner) that hit the L2Attackable and to their Party members.
	 * <p>
	 * Actions:
	 * Get the L2PcInstance owner of the L2SummonInstance (if necessary) and L2Party in progress
	 * Calculate the Experience and SP rewards in function of the level difference
	 * Add Exp and SP rewards to L2PcInstance (including Summon penalty) and to Party members in the known area of the last attacker
	 * <p>
	 * Caution : This method DOESN'T GIVE rewards to L2PetInstance
	 *
	 * @param lastAttacker The L2Character that has killed the L2Attackable
	 */
	@Override
	protected void calculateRewards(L2Character lastAttacker)
	{
		// Creates an empty list of rewards
		HashMap<L2Character, RewardInfo> rewards = new HashMap<>();
		try
		{
			if (getAggroList().isEmpty())
			{
				return;
			}

			int damage, bestDmg = 0;
			L2Character attacker, ddealer, bestDmgDealer = null;
			RewardInfo reward;

			// While Interating over This Map Removing Object is Not Allowed
			//synchronized (getAggroList())
			{
				// Go through the _aggroList of the L2Attackable
				for (AggroInfo info : getAggroList().values())
				{
					if (info == null)
					{
						continue;
					}

					// Get the L2Character corresponding to this attacker
					attacker = info.getAttacker();
					if (attacker instanceof L2Npc)
					{
						continue;
					}

					// Get damages done by this attacker
					damage = info.getDamage();

					// Prevent unwanted behavior
					if (damage <= 1)
					{
						continue;
					}

					if (attacker instanceof L2SummonInstance || attacker instanceof L2PetInstance &&
							((L2PetInstance) attacker).getPetLevelData().getOwnerExpTaken() > 0)
					{
						ddealer = ((L2Summon) attacker).getOwner();
					}
					else
					{
						ddealer = info.getAttacker();
					}

					// Check if ddealer isn't too far from this (killed monster)
					if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, ddealer, true))
					{
						continue;
					}

					// Calculate real damages (Summoners should get own damage plus summon's damage)
					reward = rewards.get(ddealer);

					if (reward == null)
					{
						reward = new RewardInfo(ddealer, damage);
						rewards.put(ddealer, reward);
					}
					else
					{
						reward.addDamage(damage);
					}

					if (ddealer.isInParty() &&
							Util.checkIfInRange(Config.ALT_PARTY_RANGE, ddealer, ddealer.getParty().getLeader(), true))
					{
						damage = 0;
						for (L2PcInstance member : ddealer.getParty().getPartyMembers())
						{
							if (rewards.containsKey(member))
							{
								damage += rewards.get(member)._dmg;
							}
						}

						ddealer = ddealer.getParty().getLeader();
					}

					if (damage > bestDmg)
					{
						bestDmg = damage;
						bestDmgDealer = ddealer;
					}
				}
			}

			// Manage Base, Quests and Sweep drops of the L2Attackable
			//doItemDrop(lastAttacker);
			doItemDrop(bestDmgDealer != null ? bestDmgDealer : lastAttacker);

			// Manage drop of Special Events created by GM for a defined period
			doEventDrop(lastAttacker);

			if (!getMustRewardExpSP())
			{
				return;
			}

			if (!rewards.isEmpty())
			{
				L2Party attackerParty;
				long exp;
				int partyDmg, partyLvl, sp;
				float partyMul, penalty;
				RewardInfo reward2;
				long[] tmp;

				List<L2Character> toRemove = new ArrayList<>();
				for (Entry<L2Character, RewardInfo> entry : rewards.entrySet())
				{
					if (entry == null || toRemove.contains(entry.getKey()))
					{
						continue;
					}

					reward = entry.getValue();

					if (reward == null)
					{
						continue;
					}

					// Penalty applied to the attacker's XP
					penalty = 0;

					// Attacker to be rewarded
					attacker = reward._attacker;

					// Total amount of damage done
					damage = reward._dmg;

					// If the attacker is a Pet, get the party of the owner
					if (attacker instanceof L2PetInstance)
					{
						attackerParty = attacker.getParty();
					}
					else if (attacker instanceof L2PcInstance)
					{
						attackerParty = attacker.getParty();
					}
					else
					{
						return;
					}

					// If this attacker is a L2PcInstance with a summoned L2SummonInstance, get Exp Penalty applied for the current summoned L2SummonInstance
					if (attacker instanceof L2PcInstance && !((L2PcInstance) attacker).getSummons().isEmpty())
					{
						penalty = 0;
						for (L2SummonInstance summon : ((L2PcInstance) attacker).getSummons())
						{
							penalty += summon.getExpPenalty();
						}
					}

					// We must avoid "over damage", if any
					if (damage > getMaxHp())
					{
						damage = getMaxHp();
					}

					// If there's NO party in progress
					if (attackerParty == null)
					{
						// Calculate Exp and SP rewards
						if (attacker.getKnownList().knowsObject(this))
						{
							tmp = calculateExpAndSp(attacker.getLevel(), getLevel(), damage);
							exp = tmp[0];
							exp *= 1 - penalty;
							sp = (int) tmp[1];

							if (Config.L2JMOD_CHAMPION_ENABLE && isChampion())
							{
								exp *= Config.L2JMOD_CHAMPION_REWARDS;
								sp *= Config.L2JMOD_CHAMPION_REWARDS;
							}

							// Check for an over-hit enabled strike
							if (attacker instanceof L2PcInstance)
							{
								L2PcInstance player = (L2PcInstance) attacker;
								if (isOverhit() && attacker == getOverhitAttacker())
								{
									player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OVER_HIT));
									exp += calculateOverhitExp(exp);
								}
							}

							// Distribute the Exp and SP between the L2PcInstance and its L2Summon
							if (!attacker.isDead())
							{
								long addexp = Math.round(attacker.calcStat(Stats.EXP_RATE, exp, null, null));
								int addsp = (int) attacker.calcStat(Stats.SP_RATE, sp, null, null);

								if (attacker instanceof L2PcInstance)
								{
									L2PcInstance pcAttacker = (L2PcInstance) attacker;
									if (pcAttacker.getSkillLevelHash(467) > 0)
									{
										L2Skill skill = SkillTable.getInstance()
												.getInfo(467, attacker.getSkillLevelHash(467));

										if (skill.getExpNeeded() <= addexp)
										{
											pcAttacker.absorbSoul(skill, this);
										}
									}
									if (pcAttacker.getLevel() >= getLevel() &&
											pcAttacker.getLevel() - getLevel() < 11 ||
											pcAttacker.getLevel() < getLevel() &&
													getLevel() - pcAttacker.getLevel() < 11)
									{
										if (pcAttacker.getReputation() < 0)
										{
											pcAttacker.updateReputationForHunting(addexp, addsp);
										}

										pcAttacker.addExpAndSp(addexp, addsp, useVitalityRate());
										if (addexp > 0)
										{
											pcAttacker.updateVitalityPoints(getVitalityPoints(damage), true, false);
										}
									}
								}
								else
								{
									attacker.addExpAndSp(addexp, addsp);
								}
							}
						}
					}
					else
					{
						//share with party members
						partyDmg = 0;
						partyMul = 1.f;
						partyLvl = 0;

						// Get all L2Character that can be rewarded in the party
						List<L2Playable> rewardedMembers = new ArrayList<>();
						// Go through all L2PcInstance in the party
						List<L2PcInstance> groupMembers;

						if (attackerParty.isInCommandChannel())
						{
							groupMembers = attackerParty.getCommandChannel().getMembers();
						}
						else
						{
							groupMembers = attackerParty.getPartyMembers();
						}

						for (L2PcInstance pl : groupMembers)
						{
							if (pl == null || pl.isDead())
							{
								continue;
							}

							// Get the RewardInfo of this L2PcInstance from L2Attackable rewards
							reward2 = rewards.get(pl);

							// If the L2PcInstance is in the L2Attackable rewards add its damages to party damages
							if (reward2 != null)
							{
								if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true))
								{
									partyDmg += reward2._dmg; // Add L2PcInstance damages to party damages
									rewardedMembers.add(pl);

									if (pl.getLevel() > partyLvl)
									{
										if (attackerParty.isInCommandChannel())
										{
											partyLvl = attackerParty.getCommandChannel().getLevel();
										}
										else
										{
											partyLvl = pl.getLevel();
										}
									}
								}

								toRemove.add(pl); // Remove the L2PcInstance from the L2Attackable rewards
							}
							else
							{
								// Add L2PcInstance of the party (that have attacked or not) to members that can be rewarded
								// and in range of the monster.
								if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true))
								{
									rewardedMembers.add(pl);
									if (pl.getLevel() > partyLvl)
									{
										if (attackerParty.isInCommandChannel())
										{
											partyLvl = attackerParty.getCommandChannel().getLevel();
										}
										else
										{
											partyLvl = pl.getLevel();
										}
									}
								}
							}
							L2Playable summon = pl.getPet();

							if (summon != null && summon instanceof L2PetInstance)
							{
								reward2 = rewards.get(summon);

								if (reward2 != null) // Pets are only added if they have done damage
								{
									if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, summon, true))
									{
										partyDmg += reward2._dmg; // Add summon damages to party damages
										rewardedMembers.add(summon);

										if (summon.getLevel() > partyLvl)
										{
											partyLvl = summon.getLevel();
										}
									}
									toRemove.add(summon); // Remove the summon from the L2Attackable rewards
								}
							}
						}

						// If the party didn't killed this L2Attackable alone
						if (partyDmg < getMaxHp())
						{
							partyMul = (float) partyDmg / (float) getMaxHp();
						}

						// Avoid "over damage"
						if (partyDmg > getMaxHp())
						{
							partyDmg = getMaxHp();
						}

						// Calculate Exp and SP rewards
						tmp = calculateExpAndSp(partyLvl, getLevel(), partyDmg);
						exp = tmp[0];
						sp = (int) tmp[1];

						if (Config.L2JMOD_CHAMPION_ENABLE && isChampion())
						{
							exp *= Config.L2JMOD_CHAMPION_REWARDS;
							sp *= Config.L2JMOD_CHAMPION_REWARDS;
						}

						exp *= partyMul;
						sp *= partyMul;

						// Check for an over-hit enabled strike
						// (When in party, the over-hit exp bonus is given to the whole party and splitted proportionally through the party members)
						if (attacker instanceof L2PcInstance)
						{
							L2PcInstance player = (L2PcInstance) attacker;

							if (isOverhit() && attacker == getOverhitAttacker())
							{
								player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OVER_HIT));
								exp += calculateOverhitExp(exp);
							}
						}
						// Distribute Experience and SP rewards to L2PcInstance Party members in the known area of the last attacker
						if (partyDmg > 0)
						{
							attackerParty.distributeXpAndSp(exp, sp, rewardedMembers, partyLvl, partyDmg, this);
						}
					}
				}
			}
			rewards = null;
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "", e);
		}
	}

	/**
	 * @see l2server.gameserver.model.actor.L2Character#addAttackerToAttackByList(l2server.gameserver.model.actor.L2Character)
	 */
	@Override
	public void addAttackerToAttackByList(L2Character player)
	{
		if (player == null || player == this || getAttackByList().contains(player))
		{
			return;
		}

		if (this instanceof L2GuardInstance && player instanceof L2Playable && isInvul(player))
		{
			return;
		}

		getAttackByList().add(player);
	}

	/**
	 * Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList.
	 *
	 * @param attacker The L2Character that gave damages to this L2Attackable
	 * @param damage   The number of damages given by the attacker L2Character
	 */
	public void addDamage(L2Character attacker, int damage, L2Skill skill)
	{
		if (attacker == null)
		{
			return;
		}

		if (this instanceof L2GuardInstance && attacker instanceof L2Playable && isInvul(attacker))
		{
			return;
		}

		// Notify the L2Attackable AI with EVT_ATTACKED
		if (!isDead())
		{
			try
			{
				L2PcInstance player = attacker.getActingPlayer();
				if (player != null)
				{
					if (getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK) != null)
					{
						for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK))
						{
							quest.notifyAttack(this, player, damage, attacker instanceof L2Summon, skill);
						}
					}
				}
				// for now hard code damage hate caused by an L2Attackable
				else
				{
					getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
					addDamageHate(attacker, damage, damage * 100 / (getLevel() + 7));
				}
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "", e);
			}
		}
	}

	/**
	 * Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList.
	 *
	 * @param attacker The L2Character that gave damages to this L2Attackable
	 * @param damage   The number of damages given by the attacker L2Character
	 * @param aggro    The hate (=damage) given by the attacker L2Character
	 */
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (attacker == null || attacker == this)
		{
			return;
		}

		if (this instanceof L2GuardInstance && attacker instanceof L2Playable && isInvul(attacker))
		{
			return;
		}

		// Modify the aggro with the attacker's aggression damage modifier
		aggro = (int) attacker.calcStat(Stats.AGGRESSION_DMG, aggro, this, null);

		int maxDist = 10000;
		if (isRaid() || isMinion())
		{
			maxDist = 3000;
		}
		if (!(this instanceof L2GrandBossInstance) && !isCastingNow() && getInstanceId() == 0 && getSpawn() != null &&
				getSpawn().getGroup() == null &&
				!isInsideRadius(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), maxDist, true, false))
		{
			escape("I have gone too far from my home... Sorry, but I must return.");
			return;
		}

		L2PcInstance targetPlayer = attacker.getActingPlayer();
		// Get the AggroInfo of the attacker L2Character from the _aggroList of the L2Attackable
		AggroInfo ai = getAggroList().get(attacker);

		if (ai == null)
		{
			// Before adding aggro info to this attackable, check how many other attackables hate it
			if (targetPlayer != null && !isRaid() && !isMinion() && getInstanceId() == 0)
			{
				int haters = 0;
				for (L2Character c : targetPlayer.getKnownList().getKnownCharacters())
				{
					if (c instanceof L2Attackable && ((L2Attackable) c).getMostHated() == targetPlayer && !c.isMinion())
					{
						haters++;
						// If there are more than 20 attackables hating this player already, don't add another one
						if (haters > 34)
						{
							return;
						}
					}
				}

				targetPlayer.setHatersAmount(haters);
			}
			ai = new AggroInfo(attacker);
			getAggroList().put(attacker, ai);
		}
		ai.addDamage(damage);
		// traps does not cause aggro
		// making this hack because not possible to determine if damage made by trap
		// so just check for triggered trap here
		if (targetPlayer == null || targetPlayer.getTrap() == null || !targetPlayer.getTrap().isTriggered())
		{
			ai.addHate(aggro);
		}

		if (targetPlayer != null && aggro == 0)
		{
			if (getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER) != null)
			{
				for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER))
				{
					quest.notifyAggroRangeEnter(this, targetPlayer, attacker instanceof L2Summon);
				}
			}
		}
		else if (targetPlayer == null && aggro == 0)
		{
			aggro = 1;
			ai.addHate(1);
		}

		// Set the intention to the L2Attackable to AI_INTENTION_ACTIVE
		if (aggro > 0 && getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}

	public void reduceHate(L2Character target, int amount)
	{
		if (getAI() instanceof L2SiegeGuardAI || getAI() instanceof L2FortSiegeGuardAI)
		{
			// TODO: this just prevents error until siege guards are handled properly
			stopHating(target);
			setTarget(null);
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			return;
		}

		if (target == null) // whole aggrolist
		{
			L2Character mostHated = getMostHated();
			if (mostHated == null) // makes target passive for a moment more
			{
				if (getAI() instanceof L2AttackableAI)
				{
					((L2AttackableAI) getAI()).setGlobalAggro(-25);
				}
				return;
			}
			else
			{
				for (L2Character aggroed : getAggroList().keySet())
				{
					AggroInfo ai = getAggroList().get(aggroed);
					if (ai == null)
					{
						return;
					}
					ai.addHate(-amount);
				}
			}

			amount = getHating(mostHated);

			if (amount <= 0 && getAI() instanceof L2AttackableAI)
			{
				((L2AttackableAI) getAI()).setGlobalAggro(-25);
				clearAggroList();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				setWalking();
			}
			return;
		}
		AggroInfo ai = getAggroList().get(target);

		if (ai == null)
		{
			return;
		}
		ai.addHate(-amount);

		if (ai.getHate() <= 0)
		{
			if (getMostHated() == null)
			{
				((L2AttackableAI) getAI()).setGlobalAggro(-25);
				clearAggroList();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				setWalking();
			}
		}
	}

	/**
	 * Clears _aggroList hate of the L2Character without removing from the list.
	 */
	public void stopHating(L2Character target)
	{
		if (target == null)
		{
			return;
		}
		AggroInfo ai = getAggroList().get(target);
		if (ai != null)
		{
			ai.stopHate();
		}
	}

	/**
	 * Return the most hated L2Character of the L2Attackable _aggroList.
	 */
	public L2Character getMostHated()
	{
		if (getAggroList().isEmpty() || isAlikeDead())
		{
			return null;
		}

		L2Character mostHated = null;
		int maxHate = 0;

		// While Interating over This Map Removing Object is Not Allowed
		//synchronized (getAggroList())
		{
			// Go through the aggroList of the L2Attackable
			for (AggroInfo ai : getAggroList().values())
			{
				if (ai == null)
				{
					continue;
				}

				if (ai.checkHate(this) > maxHate)
				{
					mostHated = ai.getAttacker();
					maxHate = ai.getHate();
				}
			}
		}
		return mostHated;
	}

	/**
	 * Return the 2 most hated L2Character of the L2Attackable _aggroList.
	 */
	public List<L2Character> get2MostHated()
	{
		if (getAggroList().isEmpty() || isAlikeDead())
		{
			return null;
		}

		L2Character mostHated = null;
		L2Character secondMostHated = null;
		int maxHate = 0;
		List<L2Character> result = new ArrayList<>();

		// While iterating over this map removing objects is not allowed
		//synchronized (getAggroList())
		{
			// Go through the aggroList of the L2Attackable
			for (AggroInfo ai : getAggroList().values())
			{
				if (ai == null)
				{
					continue;
				}

				if (ai.checkHate(this) > maxHate)
				{
					secondMostHated = mostHated;
					mostHated = ai.getAttacker();
					maxHate = ai.getHate();
				}
			}
		}
		result.add(mostHated);

		if (getAttackByList().contains(secondMostHated))
		{
			result.add(secondMostHated);
		}
		else
		{
			result.add(null);
		}
		return result;
	}

	public List<L2Character> getHateList()
	{
		if (getAggroList().isEmpty() || isAlikeDead())
		{
			return null;
		}
		List<L2Character> result = new ArrayList<>();

		//synchronized (getAggroList())
		{
			for (AggroInfo ai : getAggroList().values())
			{
				if (ai == null)
				{
					continue;
				}
				ai.checkHate(this);

				result.add(ai.getAttacker());
			}
		}

		return result;
	}

	/**
	 * Return the hate level of the L2Attackable against this L2Character contained in _aggroList.
	 *
	 * @param target The L2Character whose hate level must be returned
	 */
	public int getHating(final L2Character target)
	{
		if (getAggroList().isEmpty() || target == null)
		{
			return 0;
		}

		AggroInfo ai = getAggroList().get(target);

		if (ai == null)
		{
			return 0;
		}

		if (ai.getAttacker() instanceof L2PcInstance)
		{
			L2PcInstance act = (L2PcInstance) ai.getAttacker();
			if (act.getAppearance().getInvisible() /*|| ai.getAttacker().isInvul()*/ || act.isSpawnProtected())
			{
				//Remove Object Should Use This Method and Can be Blocked While Interating
				getAggroList().remove(target);
				return 0;
			}
		}

		if (!ai.getAttacker().isVisible())
		{
			getAggroList().remove(target);
			return 0;
		}

		if (ai.getAttacker().isAlikeDead())
		{
			ai.stopHate();
			return 0;
		}
		return ai.getHate();
	}

	/**
	 * Calculates quantity of items for specific drop acording to current situation
	 *
	 * @param drop          The L2DropData count is being calculated for
	 * @param lastAttacker  The L2PcInstance that has killed the L2Attackable
	 * @param levelModifier level modifier in %'s (will be subtracted from drop chance)
	 */
	private RewardItem calculateRewardItem(L2PcInstance lastAttacker, L2DropData drop, int levelModifier, boolean isSweep)
	{
		// Get default drop chance
		float dropChance = drop.getChance();

		int deepBlueDrop = 1;

		if (!isRaid() && Config.DEEPBLUE_DROP_RULES || isRaid() && Config.DEEPBLUE_DROP_RULES_RAID)
		{
			if (levelModifier > 0)
			{
				// We should multiply by the server's drop rate, so we always get a low chance of drop for deep blue mobs.
				// NOTE: This is valid only for adena drops! Others drops will still obey server's rate
				deepBlueDrop = 3;
				if (drop.getItemId() == 57)
				{
					deepBlueDrop *= isRaid() && !isRaidMinion() ? (int) Config.RATE_DROP_ITEMS_BY_RAID :
							(int) Config.RATE_DROP_ITEMS;
				}
			}
		}

		// Avoid dividing by 0
		if (deepBlueDrop == 0)
		{
			deepBlueDrop = 1;
		}

		// Check if we should apply our maths so deep blue mobs will not drop that easy
		if (!isRaid() && Config.DEEPBLUE_DROP_RULES || isRaid() && Config.DEEPBLUE_DROP_RULES_RAID)
		{
			dropChance = (drop.getChance() - drop.getChance() * levelModifier / 100) / deepBlueDrop;
		}

		// Applies Drop rates
		if (!drop.isCustom())
		{
			if (Config.RATE_DROP_ITEMS_ID.get(drop.getItemId()) != 0)
			{
				dropChance *= Config.RATE_DROP_ITEMS_ID.get(drop.getItemId());
			}
			else if (isSweep)
			{
				dropChance *= Config.RATE_DROP_SPOIL;
			}
			else
			{
				dropChance *= isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
			}
		}

		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion())
		{
			dropChance *= Config.L2JMOD_CHAMPION_REWARDS;
		}

		dropChance = (float) lastAttacker.calcStat(Stats.DROP_RATE, dropChance, this, null);

		// Set our limits for chance of drop
		if (dropChance < 0.00001F)
		{
			dropChance = 0.00001F;
		}

		// Get min and max Item quantity that can be dropped in one time
		int minCount = drop.getMinDrop();
		int maxCount = drop.getMaxDrop();
		long itemCount = 0;

		// Check if the Item must be dropped
		int random = Rnd.get(L2DropData.MAX_CHANCE * 100000);
		while (random < dropChance * 100000)
		{
			// Get the item quantity dropped
			if (minCount < maxCount)
			{
				itemCount += Rnd.get(minCount, maxCount);
			}
			else if (minCount == maxCount)
			{
				itemCount += minCount;
			}
			else
			{
				itemCount++;
			}

			// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
			dropChance -= L2DropData.MAX_CHANCE;
		}

		if (Config.L2JMOD_CHAMPION_ENABLE &&
				(drop.getItemId() == 57 || drop.getItemId() >= 6360 && drop.getItemId() <= 6362) && isChampion())
		{
			itemCount *= Config.L2JMOD_CHAMPION_ADENAS_REWARDS;
		}

		if (itemCount > 0)
		{
			return new RewardItem(drop.getItemId(), itemCount);
		}
		else if (itemCount == 0 && Config.DEBUG)
		{
			Log.fine("Roll produced no drops.");
		}

		return null;
	}

	/**
	 * Calculates quantity of items for specific drop CATEGORY according to current situation
	 * Only a max of ONE item from a category is allowed to be dropped.
	 *
	 * @param lastAttacker  The L2PcInstance that has killed the L2Attackable
	 * @param levelModifier level modifier in %'s (will be subtracted from drop chance)
	 */
	private RewardItem[] calculateCategorizedRewardItems(L2PcInstance lastAttacker, L2DropCategory categoryDrops, int levelModifier)
	{
		if (categoryDrops == null)
		{
			return null;
		}

		// Get default drop chance for the category (that's the sum of chances for all items in the category)
		// keep track of the base category chance as it'll be used later, if an item is drop from the category.
		// for everything else, use the total "categoryDropChance"
		float dropRate = isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
		if (categoryDrops.isCustom())
		{
			dropRate = 1;
		}

		dropRate = (float) lastAttacker.calcStat(Stats.DROP_RATE, dropRate, this, null);
		float categoryDropChance = categoryDrops.getChance() * dropRate;

		int deepBlueDrop = 1;

		if (!isRaid() && Config.DEEPBLUE_DROP_RULES || isRaid() && Config.DEEPBLUE_DROP_RULES_RAID)
		{
			// We should multiply by the server's drop rate, so we always get a low chance of drop for deep blue mobs.
			// NOTE: This is valid only for adena drops! Others drops will still obey server's rate
			if (levelModifier > 0)
			{
				deepBlueDrop = 3;
			}
		}

		// Avoid dividing by 0
		if (deepBlueDrop == 0)
		{
			deepBlueDrop = 1;
		}

		// Check if we should apply our maths so deep blue mobs will not drop that easy
		if (!isRaid() && Config.DEEPBLUE_DROP_RULES || isRaid() && Config.DEEPBLUE_DROP_RULES_RAID)
		{
			categoryDropChance = (categoryDropChance - categoryDropChance * levelModifier / 100) / deepBlueDrop;
		}

		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion())
		{
			categoryDropChance *= Config.L2JMOD_CHAMPION_REWARDS;
		}

		// Set our limits for chance of drop
		if (categoryDropChance < 0.00001F)
		{
			categoryDropChance = 0.00001F;
		}

		TIntIntHashMap rewardsMap = new TIntIntHashMap();
		// Check if an Item from this category must be dropped
		int random = Rnd.get(L2DropData.MAX_CHANCE * 100000);
		while (random < categoryDropChance * 100000)
		{
			L2DropData drop = categoryDrops.dropOne();
			if (drop == null)
			{
				categoryDropChance -= L2DropData.MAX_CHANCE;
				continue;
			}

			int itemCount = Rnd.get(drop.getMinDrop(), drop.getMaxDrop());
			if (itemCount > 0)
			{
				if (rewardsMap.contains(drop.getItemId()))
				{
					itemCount += rewardsMap.get(drop.getItemId());
				}
				rewardsMap.put(drop.getItemId(), itemCount);
			}
			categoryDropChance -= L2DropData.MAX_CHANCE;
		}

		RewardItem[] rewards = new RewardItem[rewardsMap.size()];
		int index = 0;
		for (int itemId : rewardsMap.keys())
		{
			int itemCount = rewardsMap.get(itemId);
			if (Config.RATE_DROP_ITEMS_ID.get(itemId) != 0)
			{
				itemCount *= Config.RATE_DROP_ITEMS_ID.get(itemId) / dropRate;
			}

			if (Config.L2JMOD_CHAMPION_ENABLE && (itemId == 57 || itemId >= 6360 && itemId <= 6362) && isChampion())
			{
				itemCount *= Config.L2JMOD_CHAMPION_ADENAS_REWARDS;
			}

			if (!Config.MULTIPLE_ITEM_DROP && !ItemTable.getInstance().getTemplate(itemId).isStackable() &&
					itemCount > 1)
			{
				itemCount = 1;
			}
			if (itemCount < 1)
			{
				itemCount = 1;
			}
			rewards[index] = new RewardItem(itemId, itemCount);
			index++;
		}
		return rewards;
	}

	/**
	 * Calculates the level modifier for drop
	 *
	 * @param lastAttacker The L2PcInstance that has killed the L2Attackable
	 */
	private int calculateLevelModifierForDrop(L2PcInstance lastAttacker)
	{
		if (!isRaid() && Config.DEEPBLUE_DROP_RULES || isRaid() && Config.DEEPBLUE_DROP_RULES_RAID)
		{
			int highestLevel = lastAttacker.getLevel();

			// Check to prevent very high level player to nearly kill mob and let low level player do the last hit.
			/*if (!getAttackByList().isEmpty())
            {
				for (L2Character atkChar: getAttackByList())
					if (atkChar != null && atkChar.getLevel() > highestLevel)
						highestLevel = atkChar.getLevel();
			}*/

			// According to official data (Prima), deep blue mobs are 9 or more levels below players
			if (highestLevel - 9 >= getLevel())
			{
				return (highestLevel - (getLevel() + 8)) * 9;
			}
		}
		return 0;
	}

	private RewardItem calculateCategorizedExtraItem(L2PcInstance lastAttacker, L2DropCategory categoryDrops)
	{
		if (categoryDrops == null)
		{
			return null;
		}

		// Get default drop chance for the category (that's the sum of chances for all items in the category)
		// keep track of the base category chance as it'll be used later, if an item is drop from the category.
		// for everything else, use the total "categoryDropChance"
		float categoryDropChance = categoryDrops.getChance();

		// Set our limits for chance of drop
		if (categoryDropChance < 0.00001F)
		{
			categoryDropChance = 0.00001F;
		}

		// Check if an Item from this category must be dropped
		if (Rnd.get(L2DropData.MAX_CHANCE * 10000) < categoryDropChance * 10000)
		{
			L2DropData drop = categoryDrops.dropOne();

			if (drop == null)
			{
				return null;
			}

			int itemCount = Rnd.get(drop.getMinDrop(), drop.getMaxDrop());

			if (!Config.MULTIPLE_ITEM_DROP && !ItemTable.getInstance().getTemplate(drop.getItemId()).isStackable() &&
					itemCount > 1)
			{
				itemCount = 1;
			}

			if (this instanceof L2ArmyMonsterInstance)
			{
				itemCount = 1;
			}

			if (itemCount > 0)
			{
				return new RewardItem(drop.getItemId(), itemCount);
			}
			else if (itemCount == 0 && Config.DEBUG)
			{
				Log.fine("Roll produced no drops.");
			}
		}
		return null;
	}

	public void doItemDrop(L2Character lastAttacker)
	{
		doItemDrop(getTemplate(), lastAttacker);
	}

	/**
	 * Manage Base, Quests and Special Events drops of L2Attackable (called by calculateRewards).
	 * <p>
	 * Concept:
	 * During a Special Event all L2Attackable can drop extra Items.
	 * Those extra Items are defined in the table allNpcDateDrops of the EventDroplist.
	 * Each Special Event has a start and end date to stop to drop extra Items automaticaly.
	 * <p>
	 * Actions:
	 * Manage drop of Special Events created by GM for a defined period
	 * Get all possible drops of this L2Attackable from L2NpcTemplate and add it Quest drops
	 * For each possible drops (base + quests), calculate which one must be dropped (random)
	 * Get each Item quantity dropped (random)
	 * Create this or these L2ItemInstance corresponding to each Item Identifier dropped
	 * If the autoLoot mode is actif and if the L2Character that has killed the L2Attackable is a L2PcInstance, Give the item(s) to the L2PcInstance that has killed the L2Attackable
	 * If the autoLoot mode isn't actif or if the L2Character that has killed the L2Attackable is not a L2PcInstance, add this or these item(s) in the world as a visible object at the position where mob was last
	 *
	 * @param lastAttacker The L2Character that has killed the L2Attackable
	 */
	public void doItemDrop(L2NpcTemplate npcTemplate, L2Character lastAttacker)
	{
		if (lastAttacker == null)
		{
			return;
		}

		// Don't drop anything if the last attacker or owner isn't L2PcInstance
		final L2PcInstance player = lastAttacker.getActingPlayer();
		if (player == null)
		{
			return;
		}

		// level modifier in %'s (will be subtracted from drop chance)
		int levelModifier = calculateLevelModifierForDrop(player);

		CursedWeaponsManager.getInstance().checkDrop(this, player);

		// now throw all categorized drops and handle spoil.
		if (isSpoil())
		{
			RewardItem item = null;
			ArrayList<RewardItem> sweepList = new ArrayList<>();

			for (L2DropData dd : npcTemplate.getSpoilData())
			{
				item = calculateRewardItem(player, dd, levelModifier, true);
				if (item == null)
				{
					continue;
				}

				if (Config.DEBUG)
				{
					Log.fine("Item id to spoil: " + item.getItemId() + " amount: " + item.getCount());
				}
				sweepList.add(item);
			}
			// Set the table _sweepItems of this L2Attackable
			if (!sweepList.isEmpty())
			{
				_sweepItems = sweepList.toArray(new RewardItem[sweepList.size()]);
			}
		}

		for (L2DropData dd : npcTemplate.getDropData())
		{
			RewardItem item = calculateRewardItem(player, dd, levelModifier, false);
			if (item == null)
			{
				continue;
			}

			if (Config.DEBUG)
			{
				Log.fine("Item id to drop: " + item.getItemId() + " amount: " + item.getCount());
			}

			// Check if the autoLoot mode is active
			if ((isFlying() || !isRaid() && Config.AUTO_LOOT || isRaid() && Config.AUTO_LOOT_RAIDS) &&
					!(this instanceof L2ArmyMonsterInstance && !this.isAggressive()))
			{
				player.doAutoLoot(this, item); // Give the item(s) to the L2PcInstance that has killed the L2Attackable
			}
			else if (Config.isServer(Config.TENKAI) && isRaid())
			{
				long packs = 10;
				if (this instanceof L2GrandBossInstance)
				{
					packs = 100;
				}
				if (packs > item.getCount())
				{
					packs = item.getCount();
				}
				for (int i = 0; i < packs; i++)
				{
					dropItem(player, new RewardItem(item.getItemId(), item.getCount() / packs));
				}
				dropItem(player, new RewardItem(item.getItemId(), item.getCount() % packs));
			}
			else
			{
				dropItem(player, item); // drop the item on the ground
			}

			// Broadcast message if RaidBoss was defeated
			if (isRaid() && !isRaidMinion())
			{
				SystemMessage sm;
				sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DIED_DROPPED_S3_S2);
				sm.addCharName(this);
				sm.addItemName(item.getItemId());
				sm.addItemNumber(item.getCount());
				broadcastPacket(sm);
			}
		}

		RewardItem[] items;
		for (L2DropCategory cat : npcTemplate.getMultiDropData())
		{
			if (isSeeded())
			{
				L2DropData drop = cat.dropSeedAllowedDropsOnly();
				if (drop == null)
				{
					continue;
				}

				items = new RewardItem[]{calculateRewardItem(player, drop, levelModifier, false)};
			}
			else
			{
				items = calculateCategorizedRewardItems(player, cat, levelModifier);
			}

			for (RewardItem item : items)
			{
				if (item == null)
				{
					continue;
				}
				if (Config.DEBUG)
				{
					Log.fine("Item id to drop: " + item.getItemId() + " amount: " + item.getCount());
				}

				// Check if the autoLoot mode is active
				if ((isFlying() || !isRaid() && Config.AUTO_LOOT || isRaid() && Config.AUTO_LOOT_RAIDS) &&
						!(this instanceof L2ArmyMonsterInstance && !this.isAggressive()))
				{
					player.doAutoLoot(this,
							item); // Give the item(s) to the L2PcInstance that has killed the L2Attackable
				}
				else
				{
					dropItem(player, item); // drop the item on the ground
				}

				// Broadcast message if RaidBoss was defeated
				if (isRaid() && !isRaidMinion())
				{
					SystemMessage sm;
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DIED_DROPPED_S3_S2);
					sm.addCharName(this);
					sm.addItemName(item.getItemId());
					sm.addItemNumber(item.getCount());
					broadcastPacket(sm);
				}
			}
		}

		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			if (isSpoil() && _sweepItems != null && player.getCurrentClass().getLevel() >= 85)
			{
				final L2PcInstance spoiler = L2World.getInstance().getPlayer(getIsSpoiledBy());
				if (spoiler != null && Util.checkIfInRange(900, L2Attackable.this, spoiler, false))
				{
					setSpoil(false);
					for (RewardItem ritem : _sweepItems)
					{
						if (spoiler.isInParty())
						{
							spoiler.getParty().distributeItem(spoiler, ritem, true, L2Attackable.this);
						}
						else
						{
							spoiler.addItem("Sweep", ritem.getItemId(), ritem.getCount(), spoiler, true);
						}
					}

					endDecayTask();
				}
			}
		}, 500);

		// Apply Special Item drop with random(rnd) quantity(qty) for champions.
		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion() && (Config.L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE > 0 ||
				Config.L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE > 0))
		{
			RewardItem item = null;
			int champqty = Rnd.get(Config.L2JMOD_CHAMPION_REWARD_QTY);
			item = new RewardItem(Config.L2JMOD_CHAMPION_REWARD_ID, ++champqty);

			if (player.getLevel() <= getLevel() && Rnd.get(100) < Config.L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE)
			{
				if (Config.AUTO_LOOT || isFlying())
				{
					player.addItem("ChampionLoot", item.getItemId(), item.getCount(), this,
							true); // Give the item(s) to the L2PcInstance that has killed the L2Attackable
				}
				else
				{
					dropItem(player, item);
				}
			}
			else if (player.getLevel() >= getLevel() &&
					Rnd.get(100) < Config.L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE)
			{
				if (Config.AUTO_LOOT || isFlying())
				{
					player.addItem("ChampionLoot", item.getItemId(), item.getCount(), this,
							true); // Give the item(s) to the L2PcInstance that has killed the L2Attackable
				}
				else
				{
					dropItem(player, item);
				}
			}
		}

		//Instant Item Drop :>
		if (getTemplate().ExtraDropGroup > 0 && !Config.isServer(Config.TENKAI))
		{
			RewardItem item = null;
			for (L2DropCategory cat : ExtraDropTable.getInstance().getExtraDroplist(getTemplate().ExtraDropGroup))
			{
				item = calculateCategorizedExtraItem(player, cat);
				if (item != null)
				{
					// more than one herb cant be auto looted!
					long count = item.getCount();
					if (count > 1)
					{
						item._count = 1;
						for (int i = 0; i < count; i++)
						{
							dropItem(player, item);
						}
					}
					else if (isFlying() || Config.AUTO_LOOT_HERBS)
					{
						player.addItem("Loot", item.getItemId(), count, this, true);
					}
					else
					{
						dropItem(player, item);
					}
				}
			}
		}

		for (GlobalDropCategory drop : GlobalDropTable.getInstance().getGlobalDropCategories())
		{
			if (getNpcId() == 50101)
			{
				break;
			}

			RewardItem item = null;
			int random = Rnd.get(100000);
			if (getLevel() + 5 < player.getLevel())
			{
				random *= player.getLevel() - (getLevel() + 4);
			}
			if (Config.L2JMOD_CHAMPION_ENABLE && isChampion())
			{
				random *= Config.L2JMOD_CHAMPION_REWARDS;
			}

			if (drop.isRaidOnly() && !isRaid())
			{
				continue;
			}

			/*if (isRaid() && !isRaidMinion())
				random /= 100;
			if (this instanceof L2GrandBossInstance)
				random /= 10;*/

			if (random < drop.getChance() && drop.canLootNow(player) &&
					(drop.getMobId() == 0 || getNpcId() == drop.getMobId()) && getLevel() >= drop.getMinLevel() &&
					getLevel() <= drop.getMaxLevel())
			{
				drop.increaseCountForPlayer(player);
				item = new RewardItem(drop.getRandomReward(),
						drop.getMinAmount() + Rnd.get(drop.getMaxAmount() - drop.getMinAmount()));
				player.doAutoLoot(this, item);
				/*if (isFlying())
					player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
				else
					dropItem(player, item);*/
			}
		}
	}

	/**
	 * Manage Special Events drops created by GM for a defined period.
	 * <p>
	 * Concept:
	 * During a Special Event all L2Attackable can drop extra Items.
	 * Those extra Items are defined in the table allNpcDateDrops of the EventDroplist.
	 * Each Special Event has a start and end date to stop to drop extra Items automaticaly.
	 * <p>
	 * Actions: <I>If an extra drop must be generated</I>
	 * Get an Item Identifier (random) from the DateDrop Item table of this Event
	 * Get the Item quantity dropped (random)
	 * Create this or these L2ItemInstance corresponding to this Item Identifier
	 * If the autoLoot mode is actif and if the L2Character that has killed the L2Attackable is a L2PcInstance, Give the item(s) to the L2PcInstance that has killed the L2Attackable
	 * If the autoLoot mode isn't actif or if the L2Character that has killed the L2Attackable is not a L2PcInstance, add this or these item(s) in the world as a visible object at the position where mob was last
	 *
	 * @param lastAttacker The L2Character that has killed the L2Attackable
	 */
	public void doEventDrop(L2Character lastAttacker)
	{
		if (lastAttacker == null)
		{
			return;
		}

		L2PcInstance player = lastAttacker.getActingPlayer();

		// Don't drop anything if the last attacker or owner isn't L2PcInstance
		if (player == null)
		{
			return;
		}

		if (player.getLevel() - getLevel() > 9)
		{
			return;
		}

		// Go through DateDrop of EventDroplist allNpcDateDrops within the date range
		for (DateDrop drop : EventDroplist.getInstance().getAllDrops())
		{
			if (Rnd.get(L2DropData.MAX_CHANCE) < drop.chance)
			{
				RewardItem item = new RewardItem(drop.items[Rnd.get(drop.items.length)], Rnd.get(drop.min, drop.max));

				if (Config.AUTO_LOOT || isFlying())
				{
					player.doAutoLoot(this,
							item); // Give the item(s) to the L2PcInstance that has killed the L2Attackable
				}
				else
				{
					dropItem(player, item); // drop the item on the ground
				}
			}
		}
	}

	/**
	 * Drop reward item.
	 */
	public L2ItemInstance dropItem(L2PcInstance lastAttacker, RewardItem item)
	{
		int randDropLim = 70;

		L2ItemInstance ditem = null;
		for (int i = 0; i < item.getCount(); i++)
		{
			// Randomize drop position
			int newX = getX() + Rnd.get(randDropLim * 2 + 1) - randDropLim;
			int newY = getY() + Rnd.get(randDropLim * 2 + 1) - randDropLim;
			int newZ = Math.max(getZ(), lastAttacker.getZ()) +
					20; // TODO: temp hack, do somethign nicer when we have geodatas

			if (ItemTable.getInstance().getTemplate(item.getItemId()) != null)
			{
				// Init the dropped L2ItemInstance and add it in the world as a visible object at the position where mob was last
				ditem = ItemTable.getInstance()
						.createItem("Loot", item.getItemId(), item.getCount(), lastAttacker, this);

				ditem.dropMe(this, newX, newY, newZ);

				// Add drop to auto destroy item task
				if (!Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
				{
					if (Config.AUTODESTROY_ITEM_AFTER * 1000 > 0 && ditem.getItemType() != L2EtcItemType.HERB ||
							Config.HERB_AUTO_DESTROY_TIME * 1000 > 0 && ditem.getItemType() == L2EtcItemType.HERB)
					{
						ItemsAutoDestroy.getInstance().addItem(ditem);
					}
				}
				ditem.setProtected(false);

				// If stackable, end loop as entire count is included in 1 instance of item
				if (ditem.isStackable() || !Config.MULTIPLE_ITEM_DROP)
				{
					break;
				}
			}
			else
			{
				Log.log(Level.SEVERE, "Item doesn't exist so cannot be dropped. Item ID: " + item.getItemId());
			}
		}
		return ditem;
	}

	public L2ItemInstance dropItem(L2PcInstance lastAttacker, int itemId, int itemCount)
	{
		return dropItem(lastAttacker, new RewardItem(itemId, itemCount));
	}

	/**
	 * Return the active weapon of this L2Attackable (= null).
	 */
	public L2ItemInstance getActiveWeapon()
	{
		return null;
	}

	/**
	 * Return True if the _aggroList of this L2Attackable is Empty.
	 */
	public boolean noTarget()
	{
		return getAggroList().isEmpty();
	}

	/**
	 * Return True if the _aggroList of this L2Attackable contains the L2Character.
	 *
	 * @param player The L2Character searched in the _aggroList of the L2Attackable
	 */
	public boolean containsTarget(L2Character player)
	{
		return getAggroList().containsKey(player);
	}

	/**
	 * Clear the _aggroList of the L2Attackable.
	 */
	public void clearAggroList()
	{
		getAggroList().clear();

		// clear overhit values
		_overhit = false;
		_overhitDamage = 0;
		_overhitAttacker = null;
	}

	/**
	 * Return True if a Dwarf use Sweep on the L2Attackable and if item can be spoiled.
	 */
	public boolean isSweepActive()
	{
		return _sweepItems != null;
	}

	/**
	 * Return table containing all L2ItemInstance that can be spoiled.
	 */
	public synchronized RewardItem[] takeSweep()
	{
		RewardItem[] sweep = _sweepItems;
		_sweepItems = null;
		return sweep;
	}

	/**
	 * Return table containing all L2ItemInstance that can be harvested.
	 */
	public synchronized RewardItem[] takeHarvest()
	{
		RewardItem[] harvest = _harvestItems;
		_harvestItems = null;
		return harvest;
	}

	/**
	 * Set the over-hit flag on the L2Attackable.
	 *
	 * @param status The status of the over-hit flag
	 */
	public void overhitEnabled(boolean status)
	{
		_overhit = status;
	}

	/**
	 * Set the over-hit values like the attacker who did the strike and the amount of damage done by the skill.
	 *
	 * @param attacker The L2Character who hit on the L2Attackable using the over-hit enabled skill
	 * @param damage   The ammount of damage done by the over-hit enabled skill on the L2Attackable
	 */
	public void setOverhitValues(L2Character attacker, double damage)
	{
		// Calculate the over-hit damage
		// Ex: mob had 10 HP left, over-hit skill did 50 damage total, over-hit damage is 40
		double overhitDmg = (getCurrentHp() - damage) * -1;
		if (overhitDmg < 0)
		{
			// we didn't killed the mob with the over-hit strike. (it wasn't really an over-hit strike)
			// let's just clear all the over-hit related values
			overhitEnabled(false);
			_overhitDamage = 0;
			_overhitAttacker = null;
			return;
		}
		overhitEnabled(true);
		_overhitDamage = overhitDmg;
		_overhitAttacker = attacker;
	}

	/**
	 * Return the L2Character who hit on the L2Attackable using an over-hit enabled skill.
	 *
	 * @return L2Character attacker
	 */
	public L2Character getOverhitAttacker()
	{
		return _overhitAttacker;
	}

	/**
	 * Return the ammount of damage done on the L2Attackable using an over-hit enabled skill.
	 *
	 * @return double damage
	 */
	public double getOverhitDamage()
	{
		return _overhitDamage;
	}

	/**
	 * Return True if the L2Attackable was hit by an over-hit enabled skill.
	 */
	public boolean isOverhit()
	{
		return _overhit;
	}

	/**
	 * Activate the absorbed soul condition on the L2Attackable.
	 */
	public void absorbSoul()
	{
		_absorbed = true;
	}

	/**
	 * Return True if the L2Attackable had his soul absorbed.
	 */
	public boolean isAbsorbed()
	{
		return _absorbed;
	}

	/**
	 * Adds an attacker that successfully absorbed the soul of this L2Attackable into the _absorbersList.
	 * <p>
	 * Params:
	 * attacker - a valid L2PcInstance
	 * condition - an integer indicating the event when mob dies. This should be:
	 * = 0 - "the crystal scatters";
	 * = 1 - "the crystal failed to absorb. nothing happens";
	 * = 2 - "the crystal resonates because you got more than 1 crystal on you";
	 * = 3 - "the crystal cannot absorb the soul because the mob level is too low";
	 * = 4 - "the crystal successfuly absorbed the soul";
	 */
	public void addAbsorber(L2PcInstance attacker)
	{
		// If we have no _absorbersList initiated, do it
		AbsorberInfo ai = _absorbersList.get(attacker.getObjectId());

		// If the L2Character attacker isn't already in the _absorbersList of this L2Attackable, add it
		if (ai == null)
		{
			ai = new AbsorberInfo(attacker.getObjectId(), getCurrentHp());
			_absorbersList.put(attacker.getObjectId(), ai);
		}
		else
		{
			ai._objId = attacker.getObjectId();
			ai._absorbedHP = getCurrentHp();
		}

		// Set this L2Attackable as absorbed
		absorbSoul();
	}

	public void resetAbsorbList()
	{
		_absorbed = false;
		_absorbersList.clear();
	}

	public ConcurrentHashMap<Integer, AbsorberInfo> getAbsorbersList()
	{
		return _absorbersList;
	}

	/**
	 * Calculate the Experience and SP to distribute to attacker (L2PcInstance, L2SummonInstance or L2Party) of the L2Attackable.
	 *
	 * @param damage The damages given by the attacker (L2PcInstance, L2SummonInstance or L2Party)
	 */
	private long[] calculateExpAndSp(int attackerLevel, int monsterLevel, int damage)
	{
		double xp;
		double sp;

		// Calculate the difference of level between this attacker (L2PcInstance or L2SummonInstance owner) and the L2Attackable
		// mob = 24, atk = 10, diff = -14 (full xp)
		// mob = 24, atk = 28, diff = 4 (some xp)
		// mob = 24, atk = 50, diff = 26 (no xp)
		int diff = attackerLevel - monsterLevel;

		if (diff < -5)
		{
			diff = -5; // makes possible to use ALT_GAME_EXPONENT configuration
		}

		double expReward = getExpReward();
		double multiplier = 0;

		if (multiplier != 0)
		{
			expReward *= multiplier;
		}

		xp = expReward * damage / getMaxHp();

		if (Config.ALT_GAME_EXPONENT_XP != 0)
		{
			xp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_XP);
		}

		double spReward = getSpReward();

		if (multiplier != 0)
		{
			spReward *= multiplier;
		}

		sp = spReward * damage / getMaxHp();

		if (Config.ALT_GAME_EXPONENT_SP != 0)
		{
			sp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_SP);
		}

		if (Config.ALT_GAME_EXPONENT_XP == 0 && Config.ALT_GAME_EXPONENT_SP == 0)
		{
			if (diff > 5) // formula revised May 07
			{
				double pow = Math.pow((double) 5 / 6, diff - 5);
				xp = xp * pow;
				sp = sp * pow;
			}

			if (xp <= 0)
			{
				xp = 0;
				sp = 0;
			}
			else if (sp <= 0)
			{
				sp = 0;
			}
		}
		return new long[]{(long) xp, (long) sp};
	}

	public long calculateOverhitExp(long normalExp)
	{
		// Get the percentage based on the total of extra (over-hit) damage done relative to the total (maximum) ammount of HP on the L2Attackable
		double overhitPercentage = getOverhitDamage() * 100 / getMaxHp();

		// Over-hit damage percentages are limited to 25% max
		if (overhitPercentage > 25)
		{
			overhitPercentage = 25;
		}

		// Get the overhit exp bonus according to the above over-hit damage percentage
		// (1/1 basis - 13% of over-hit damage, 13% of extra exp is given, and so on...)
		double overhitExp = overhitPercentage / 100 * normalExp;

		// Return the rounded ammount of exp points to be added to the player's normal exp reward
		return Math.round(overhitExp);
	}

	/**
	 * Return True.
	 */
	@Override
	public boolean isAttackable()
	{
		return true;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		// Clear mob spoil, seed
		setSpoil(false);
		// Clear all aggro char from list
		clearAggroList();
		// Clear Harvester Rewrard List
		_harvestItems = null;
		// Clear mod Seeded stat
		_seeded = false;
		_seedType = 0;
		_seederObjId = 0;
		// Clear overhit value
		overhitEnabled(false);

		_sweepItems = null;
		resetAbsorbList();

		setWalking();

		// check the region where this mob is, do not activate the AI if region is inactive.
		if (!isInActiveRegion())
		{
			if (hasAI())
			{
				getAI().stopAITask();
			}
		}
	}

	/**
	 * Return True if this L2NpcInstance has drops that can be sweeped.<BR><BR>
	 */
	public boolean isSpoil()
	{
		return _isSpoil;
	}

	/**
	 * Set the spoil state of this L2NpcInstance.<BR><BR>
	 */
	public void setSpoil(boolean isSpoil)
	{
		_isSpoil = isSpoil;
	}

	public final int getIsSpoiledBy()
	{
		return _isSpoiledBy;
	}

	public final void setIsSpoiledBy(int value)
	{
		_isSpoiledBy = value;
	}

	private boolean _canBeSweeped = true;

	public final boolean canBeSweeped()
	{
		return _canBeSweeped;
	}

	public final void setCanBeSweeped(final boolean canBeSweeped)
	{
		_canBeSweeped = canBeSweeped;
	}

	/**
	 * Sets state of the mob to seeded. Paramets needed to be set before.
	 */
	public void setSeeded(L2PcInstance seeder)
	{
		if (_seedType != 0 && _seederObjId == seeder.getObjectId())
		{
			setSeeded(_seedType, seeder.getLevel());
		}
	}

	/**
	 * Sets the seed parameters, but not the seed state
	 *
	 * @param id     - id of the seed
	 * @param seeder - player who is sowind the seed
	 */
	public void setSeeded(int id, L2PcInstance seeder)
	{
		if (!_seeded)
		{
			_seedType = id;
			_seederObjId = seeder.getObjectId();
		}
	}

	private void setSeeded(int id, int seederLvl)
	{
		_seeded = true;
		_seedType = id;
		int count = 1;

		Map<Integer, L2Skill> skills = getTemplate().getSkills();

		if (skills != null)
		{
			for (int skillId : skills.keySet())
			{
				switch (skillId)
				{
					case 4303: //Strong type x2
						count *= 2;
						break;
					case 4304: //Strong type x3
						count *= 3;
						break;
					case 4305: //Strong type x4
						count *= 4;
						break;
					case 4306: //Strong type x5
						count *= 5;
						break;
					case 4307: //Strong type x6
						count *= 6;
						break;
					case 4308: //Strong type x7
						count *= 7;
						break;
					case 4309: //Strong type x8
						count *= 8;
						break;
					case 4310: //Strong type x9
						count *= 9;
						break;
				}
			}
		}

		int diff = getLevel() - (L2Manor.getInstance().getSeedLevel(_seedType) - 5);

		// hi-lvl mobs bonus
		if (diff > 0)
		{
			count += diff;
		}

		ArrayList<RewardItem> harvested = new ArrayList<>();

		harvested.add(new RewardItem(L2Manor.getInstance().getCropType(_seedType), count * Config.RATE_DROP_MANOR));

		_harvestItems = harvested.toArray(new RewardItem[harvested.size()]);
	}

	public int getSeederId()
	{
		return _seederObjId;
	}

	public int getSeedType()
	{
		return _seedType;
	}

	public boolean isSeeded()
	{
		return _seeded;
	}

	/**
	 * Set delay for onKill() call, in ms
	 * Default: 5000 ms
	 *
	 * @param delay
	 */
	public final void setOnKillDelay(int delay)
	{
		_onKillDelay = delay;
	}

	/**
	 * Check if the server allows Random Animation.
	 */
	// This is located here because L2Monster and L2FriendlyMob both extend this class. The other non-pc instances extend either L2NpcInstance or L2MonsterInstance.
	@Override
	public boolean hasRandomAnimation()
	{
		return super.hasRandomAnimation() && !(this instanceof L2GrandBossInstance);
	}

	@Override
	public boolean isMob()
	{
		return true; // This means we use MAX_MONSTER_ANIMATION instead of MAX_NPC_ANIMATION
	}

	protected void setCommandChannelTimer(CommandChannelTimer commandChannelTimer)
	{
		_commandChannelTimer = commandChannelTimer;
	}

	public CommandChannelTimer getCommandChannelTimer()
	{
		return _commandChannelTimer;
	}

	public L2CommandChannel getFirstCommandChannelAttacked()
	{
		return _firstCommandChannelAttacked;
	}

	public void setFirstCommandChannelAttacked(L2CommandChannel firstCommandChannelAttacked)
	{
		_firstCommandChannelAttacked = firstCommandChannelAttacked;
	}

	/**
	 * @return the _commandChannelLastAttack
	 */
	public long getCommandChannelLastAttack()
	{
		return _commandChannelLastAttack;
	}

	/**
	 * @param channelLastAttack the _commandChannelLastAttack to set
	 */
	public void setCommandChannelLastAttack(long channelLastAttack)
	{
		_commandChannelLastAttack = channelLastAttack;
	}

	private static class CommandChannelTimer implements Runnable
	{
		private L2Attackable _monster;

		public CommandChannelTimer(L2Attackable monster)
		{
			_monster = monster;
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			if (System.currentTimeMillis() - _monster.getCommandChannelLastAttack() >
					Config.LOOT_RAIDS_PRIVILEGE_INTERVAL * 1000)
			{
				_monster.setCommandChannelTimer(null);
				_monster.setFirstCommandChannelAttacked(null);
				_monster.setCommandChannelLastAttack(0);
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(this, 10000); // 10sec
			}
		}
	}

	public void returnHome()
	{
		clearAggroList();

		if (!isRaid())
		{
			setCurrentHp(getMaxHp());
		}

		if (hasAI() && getSpawn() != null)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
					new L2CharPosition(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), 0));
		}
	}

	/*
	 * Return vitality points decrease (if positive)
	 * or increase (if negative) based on damage.
	 * Maximum for damage = maxHp.
	 */
	public float getVitalityPoints(int damage)
	{
		// sanity check
		if (damage <= 0)
		{
			return 0;
		}

		float xpRates = Config.RATE_XP;
		final float divider = getTemplate().BaseVitalityDivider * xpRates;
		if (divider == 0)
		{
			return 0;
		}

		// negative value - vitality will be consumed
		return -Math.min(damage, getMaxHp()) / divider;
	}

	/*
	 * True if vitality rate for exp and sp should be applied
	 */
	public boolean useVitalityRate()
	{
		return !(isChampion() && !Config.L2JMOD_CHAMPION_ENABLE_VITALITY);

	}

	/**
	 * Return True if the L2Character is RaidBoss or his minion.
	 */
	@Override
	public boolean isRaid()
	{
		return _isRaid;
	}

	/**
	 * Set this Npc as a Raid instance.<BR><BR>
	 *
	 * @param isRaid
	 */
	public void setIsRaid(boolean isRaid)
	{
		_isRaid = isRaid;
	}

	/**
	 * Set this Npc as a Minion instance.<BR><BR>
	 *
	 * @param val
	 */
	public void setIsRaidMinion(boolean val)
	{
		_isRaid = val;
		_isRaidMinion = val;
	}

	@Override
	public boolean isRaidMinion()
	{
		return _isRaidMinion;
	}

	@Override
	public boolean isMinion()
	{
		return getLeader() != null;
	}

	/**
	 * Return leader of this minion or null.
	 */
	public L2Attackable getLeader()
	{
		return null;
	}

	public void setChampion(boolean champ)
	{
		_champion = champ;
	}

	@Override
	public boolean isChampion()
	{
		return _champion;
	}

	public void escape(String message)
	{
		setIsInvul(true);
		setTarget(this);
		setIsCastingNow(true);
		disableAllSkills();

		int unstuckTimer = 4000;

		forceIsCasting(TimeController.getGameTicks() + unstuckTimer / TimeController.MILLIS_IN_TICK);

		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		MagicSkillUse msk = new MagicSkillUse(this, 1050, 1, unstuckTimer, 0);

		broadcastPacket(msk);

		EscapeFinalizer ef = new EscapeFinalizer(this);

		setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, unstuckTimer));

		if (message != null)
		{
			broadcastPacket(new CreatureSay(getObjectId(), Say2.ALL_NOT_RECORDED, getName(), message));
		}
	}

	private static class EscapeFinalizer implements Runnable
	{
		private L2Attackable _mob;

		EscapeFinalizer(L2Attackable mob)
		{
			_mob = mob;
		}

		@Override
		public void run()
		{
			_mob.enableAllSkills();
			_mob.setIsCastingNow(false);
			if (!_mob.isRaid())
			{
				_mob.setCurrentHpMp(_mob.getMaxHp(), _mob.getMaxMp());
			}
			_mob.setIsInvul(false);
			_mob.teleToLocation(_mob.getSpawn().getX(), _mob.getSpawn().getY(), _mob.getSpawn().getZ());
		}
	}

	@SuppressWarnings("unused")
	private int getHighestLevelAttacker(final L2Attackable attackable, L2PcInstance player)
	{
		int highestLevel = player.getLevel();

		// Check to prevent very high level player to nearly kill mob and let low level player do the last hit.
		if (attackable.getAttackByList() != null && !attackable.getAttackByList().isEmpty())
		{
			for (L2Character atkChar : attackable.getAttackByList())
			{
				if (atkChar != null && atkChar.getLevel() > highestLevel)
				{
					highestLevel = atkChar.getLevel();
				}
			}
		}
		return highestLevel;
	}
}
