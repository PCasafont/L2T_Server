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
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.gameserver.templates.item.EtcItemType;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class Attackable extends Npc {
	private static Logger log = LoggerFactory.getLogger(Attackable.class.getName());
	
	private boolean isRaid = false;
	private boolean isRaidMinion = false;
	private boolean champion = false;
	
	/**
	 * This class contains all AggroInfo of the Attackable against the attacker Creature.
	 * <p>
	 * Data:
	 * attacker : The attacker Creature concerned by this AggroInfo of this Attackable
	 * hate : Hate level of this Attackable against the attacker Creature (hate = damage)
	 * damage : Number of damages that the attacker Creature gave to this Attackable
	 */
	public static final class AggroInfo {
		private final Creature attacker;
		private int hate = 0;
		private int damage = 0;
		
		public AggroInfo(Creature pAttacker) {
			attacker = pAttacker;
		}
		
		public final Creature getAttacker() {
			return attacker;
		}
		
		public final int getHate() {
			return hate;
		}
		
		public final int checkHate(Attackable owner) {
			if (attacker.isAlikeDead() || !attacker.isVisible() || !owner.getKnownList().knowsObject(attacker)) {
				hate = 0;
			}
			
			return hate;
		}
		
		public final void addHate(int value) {
			hate = (int) Math.min(hate + (long) value, 999999999);
		}
		
		public final void stopHate() {
			hate = 0;
		}
		
		public final int getDamage() {
			return damage;
		}
		
		public final void addDamage(int value) {
			damage = (int) Math.min(damage + (long) value, 999999999);
		}
		
		@Override
		public final boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			
			if (obj instanceof AggroInfo) {
				return ((AggroInfo) obj).getAttacker() == attacker;
			}
			
			return false;
		}
		
		@Override
		public final int hashCode() {
			return attacker.getObjectId();
		}
	}
	
	/**
	 * This class contains all RewardInfo of the Attackable against the any attacker Creature, based on amount of damage done.
	 * <p>
	 * Data:
	 * attacker : The attacker Creature concerned by this RewardInfo of this Attackable
	 * dmg : Total amount of damage done by the attacker to this Attackable (summon + own)
	 */
	protected static final class RewardInfo {
		protected Creature attacker;
		
		protected int dmg = 0;
		
		public RewardInfo(Creature pAttacker, int pDmg) {
			attacker = pAttacker;
			dmg = pDmg;
		}
		
		public void addDamage(int pDmg) {
			dmg += pDmg;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			
			if (obj instanceof RewardInfo) {
				return ((RewardInfo) obj).attacker == attacker;
			}
			
			return false;
		}
		
		@Override
		public int hashCode() {
			return attacker.getObjectId();
		}
	}
	
	/**
	 * This class contains all AbsorberInfo of the Attackable against the absorber Creature.
	 * <p>
	 * Data:
	 * absorber : The attacker Creature concerned by this AbsorberInfo of this Attackable
	 */
	public static final class AbsorberInfo {
		public int objId;
		public double absorbedHP;
		
		AbsorberInfo(int objId, double pAbsorbedHP) {
			this.objId = objId;
			absorbedHP = pAbsorbedHP;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			
			if (obj instanceof AbsorberInfo) {
				return ((AbsorberInfo) obj).objId == objId;
			}
			
			return false;
		}
		
		@Override
		public int hashCode() {
			return objId;
		}
	}
	
	public static final class RewardItem {
		protected int itemId;
		
		protected long count;
		
		public RewardItem(int itemId, long count) {
			this.itemId = itemId;
			this.count = count;
		}
		
		public int getItemId() {
			return itemId;
		}
		
		public long getCount() {
			return count;
		}
	}
	
	private ConcurrentHashMap<Creature, AggroInfo> aggroList = new ConcurrentHashMap<>();
	
	public final ConcurrentHashMap<Creature, AggroInfo> getAggroList() {
		return aggroList;
	}
	
	private boolean isReturningToSpawnPoint = false;
	
	public final boolean isReturningToSpawnPoint() {
		return isReturningToSpawnPoint;
	}
	
	public final void setisReturningToSpawnPoint(boolean value) {
		isReturningToSpawnPoint = value;
	}
	
	private boolean canReturnToSpawnPoint = true;
	
	public final boolean canReturnToSpawnPoint() {
		return canReturnToSpawnPoint;
	}
	
	public final void setCanReturnToSpawnPoint(boolean value) {
		canReturnToSpawnPoint = value;
	}
	
	public boolean canSeeThroughSilentMove() {
		return getTemplate().CanSeeThroughSilentMove;
	}
	
	public void setCanSeeThroughSilentMove(boolean val) {
		if (!getTemplate().CanSeeThroughSilentMove) {
			getTemplate().CanSeeThroughSilentMove = true;
		}
	}
	
	private RewardItem[] sweepItems;
	
	private RewardItem[] harvestItems;
	private boolean seeded;
	private int seedType = 0;
	private int seederObjId = 0;
	
	private boolean overhit;
	
	private double overhitDamage;
	
	private Creature overhitAttacker;
	
	private L2CommandChannel firstCommandChannelAttacked = null;
	private CommandChannelTimer commandChannelTimer = null;
	private long commandChannelLastAttack = 0;
	
	private boolean absorbed;
	
	private ConcurrentHashMap<Integer, AbsorberInfo> absorbersList = new ConcurrentHashMap<>();
	
	private boolean mustGiveExpSp;
	
	/**
	 * True if a Dwarf has used Spoil on this NpcInstance
	 */
	private boolean isSpoil = false;
	
	private int isSpoiledBy = 0;
	
	protected int onKillDelay = 3000;
	
	/**
	 * Constructor of Attackable (use Creature and NpcInstance constructor).
	 * <p>
	 * Actions:
	 * Call the Creature constructor to set the template of the Attackable (copy skills from template to object and link calculators to NPC_STD_CALCULATOR)
	 * Set the name of the Attackable
	 * Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it
	 *
	 * @param objectId Identifier of the object to initialized
	 */
	public Attackable(int objectId, NpcTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2Attackable);
		setIsInvul(false);
		mustGiveExpSp = true;
	}
	
	@Override
	public AttackableKnownList getKnownList() {
		return (AttackableKnownList) super.getKnownList();
	}
	
	@Override
	public AttackableKnownList initialKnownList() {
		return new AttackableKnownList(this);
	}
	
	@Override
	public AttackableStatus getStatus() {
		return (AttackableStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus() {
		setStatus(new AttackableStatus(this));
	}
	
	@Override
	protected CreatureAI initAI() {
		return new AttackableAI(this);
	}
	
	/**
	 * Not used.
	 * get condition to hate, actually isAggressive() is checked by monster and karma by guards in motheds that overwrite this one.
	 *
	 * @deprecated
	 */
	@Deprecated
	public boolean getCondition2(Creature target) {
		if (target instanceof NpcInstance || target instanceof DoorInstance) {
			return false;
		}
		
		if (target.isAlikeDead() || !isInsideRadius(target, getAggroRange(), false, false) || Math.abs(getZ() - target.getZ()) > 100) {
			return false;
		}
		
		return !target.isInvul();
	}
	
	public void useMagic(Skill skill) {
		if (skill == null || isAlikeDead()) {
			return;
		}
		
		if (skill.isPassive()) {
			return;
		}
		
		if (isCastingNow()) {
			return;
		}
		
		if (isSkillDisabled(skill)) {
			return;
		}
		
		if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill)) {
			return;
		}
		
		if (getCurrentHp() <= skill.getHpConsume()) {
			return;
		}
		
		if (skill.isMagic()) {
			if (isMuted()) {
				return;
			}
		} else {
			if (isPhysicalMuted()) {
				return;
			}
		}
		
		WorldObject target = skill.getFirstOfTargetList(this);
		if (target == null) {
			return;
		}
		
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}
	
	/**
	 * Reduce the current HP of the Attackable.
	 *
	 * @param damage   The HP decrease value
	 * @param attacker The Creature who attacks
	 */
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill) {
		reduceCurrentHp(damage, attacker, true, false, skill);
	}
	
	/**
	 * Reduce the current HP of the Attackable, update its aggroList and launch the doDie Task if necessary.
	 *
	 * @param attacker The Creature who attacks
	 * @param awake    The awake state (If True : stop sleeping)
	 */
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill) {
		if (isRaid() && !isMinion() && attacker != null && attacker.getParty() != null && attacker.getParty().isInCommandChannel() &&
				attacker.getParty().getCommandChannel().meetRaidWarCondition(this)) {
			if (firstCommandChannelAttacked == null) //looting right isn't set
			{
				synchronized (this) {
					if (firstCommandChannelAttacked == null) {
						firstCommandChannelAttacked = attacker.getParty().getCommandChannel();
						if (firstCommandChannelAttacked != null) {
							commandChannelTimer = new CommandChannelTimer(this);
							commandChannelLastAttack = System.currentTimeMillis();
							ThreadPoolManager.getInstance().scheduleGeneral(commandChannelTimer, 10000); // check for last attack
							firstCommandChannelAttacked.broadcastToChannelMembers(new CreatureSay(0,
									Say2.PARTYROOM_ALL,
									"",
									"You have looting rights!")); //TODO: retail msg
						}
					}
				}
			} else if (attacker.getParty().getCommandChannel().equals(firstCommandChannelAttacked)) //is in same channel
			{
				commandChannelLastAttack = System.currentTimeMillis(); // update last attack time
			}
		}
		
		if (this instanceof GuardInstance && attacker instanceof Playable && isInvul(attacker)) {
			return;
		}
		
		// Add damage and hate to the attacker AggroInfo of the Attackable aggroList
		if (attacker != null) {
			addDamage(attacker, (int) damage, skill);
		}
		
		// If this Attackable is a MonsterInstance and it has spawned minions, call its minions to battle
		if (this instanceof MonsterInstance) {
			MonsterInstance master = (MonsterInstance) this;
			
			if (master.hasMinions()) {
				master.getMinionList().onAssist(this, attacker);
			}
			
			master = master.getLeader();
			if (master != null && master.hasMinions()) {
				master.getMinionList().onAssist(this, attacker);
			}
		}
		// Reduce the current HP of the Attackable and launch the doDie Task if necessary
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}
	
	public void setMustRewardExpSp(boolean value) {
		mustGiveExpSp = value;
	}
	
	public boolean getMustRewardExpSP() {
		return mustGiveExpSp;
	}
	
	/**
	 * Kill the Attackable (the corpse disappeared after 7 seconds), distribute rewards (EXP, SP, Drops...) and notify Quest Engine.
	 * <p>
	 * Actions:
	 * Distribute Exp and SP rewards to Player (including Summon owner) that hit the Attackable and to their Party members
	 * Notify the Quest Engine of the Attackable death if necessary
	 * Kill the NpcInstance (the corpse disappeared after 7 seconds)
	 * <p>
	 * Caution: This method DOESN'T GIVE rewards to PetInstance
	 *
	 * @param killer The Creature that has killed the Attackable
	 */
	@Override
	public boolean doDie(Creature killer) {
		// Kill the NpcInstance (the corpse disappeared after 7 seconds)
		if (!super.doDie(killer)) {
			return false;
		}
		
		if (killer instanceof Playable) {
			final Player player = killer.getActingPlayer();
			
			if (player.getHatersAmount() != 0) {
				player.setHatersAmount(player.getHatersAmount() - 1);
			}
		}
		
		// Notify the Quest Engine of the Attackable death if necessary
		try {
			Player player = null;
			if (killer != null) {
				player = killer.getActingPlayer();
			}
			
			if (player != null) {
				if (getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL) != null) {
					for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL)) {
						ThreadPoolManager.getInstance()
								.scheduleEffect(new OnKillNotifyTask(this, quest, player, killer instanceof Summon),
										Math.min(onKillDelay, quest.getOnKillDelay(getNpcId())));
					}
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		
		setCanBeSweeped(true);
		return true;
	}
	
	protected static class OnKillNotifyTask implements Runnable {
		private Attackable attackable;
		private Quest quest;
		private Player killer;
		private boolean isPet;
		
		public OnKillNotifyTask(Attackable attackable, Quest quest, Player killer, boolean isPet) {
			this.attackable = attackable;
			this.quest = quest;
			this.killer = killer;
			this.isPet = isPet;
		}
		
		@Override
		public void run() {
			quest.notifyKill(attackable, killer, isPet);
		}
	}
	
	/**
	 * Distribute Exp and SP rewards to Player (including Summon owner) that hit the Attackable and to their Party members.
	 * <p>
	 * Actions:
	 * Get the Player owner of the SummonInstance (if necessary) and L2Party in progress
	 * Calculate the Experience and SP rewards in function of the level difference
	 * Add Exp and SP rewards to Player (including Summon penalty) and to Party members in the known area of the last attacker
	 * <p>
	 * Caution : This method DOESN'T GIVE rewards to PetInstance
	 *
	 * @param lastAttacker The Creature that has killed the Attackable
	 */
	@Override
	protected void calculateRewards(Creature lastAttacker) {
		// Creates an empty list of rewards
		HashMap<Creature, RewardInfo> rewards = new HashMap<>();
		try {
			if (getAggroList().isEmpty()) {
				return;
			}
			
			int damage, bestDmg = 0;
			Creature attacker, ddealer, bestDmgDealer = null;
			RewardInfo reward;
			
			// While Interating over This Map Removing Object is Not Allowed
			//synchronized (getAggroList())
			{
				// Go through the aggroList of the Attackable
				for (AggroInfo info : getAggroList().values()) {
					if (info == null) {
						continue;
					}
					
					// Get the Creature corresponding to this attacker
					attacker = info.getAttacker();
					if (attacker instanceof Npc) {
						continue;
					}
					
					// Get damages done by this attacker
					damage = info.getDamage();
					
					// Prevent unwanted behavior
					if (damage <= 1) {
						continue;
					}
					
					if (attacker instanceof SummonInstance ||
							attacker instanceof PetInstance && ((PetInstance) attacker).getPetLevelData().getOwnerExpTaken() > 0) {
						ddealer = ((Summon) attacker).getOwner();
					} else {
						ddealer = info.getAttacker();
					}
					
					// Check if ddealer isn't too far from this (killed monster)
					if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, ddealer, true)) {
						continue;
					}
					
					// Calculate real damages (Summoners should get own damage plus summon's damage)
					reward = rewards.get(ddealer);
					
					if (reward == null) {
						reward = new RewardInfo(ddealer, damage);
						rewards.put(ddealer, reward);
					} else {
						reward.addDamage(damage);
					}
					
					if (ddealer.isInParty() && Util.checkIfInRange(Config.ALT_PARTY_RANGE, ddealer, ddealer.getParty().getLeader(), true)) {
						damage = 0;
						for (Player member : ddealer.getParty().getPartyMembers()) {
							if (rewards.containsKey(member)) {
								damage += rewards.get(member).dmg;
							}
						}
						
						ddealer = ddealer.getParty().getLeader();
					}
					
					if (damage > bestDmg) {
						bestDmg = damage;
						bestDmgDealer = ddealer;
					}
				}
			}
			
			// Manage Base, Quests and Sweep drops of the Attackable
			//doItemDrop(lastAttacker);
			doItemDrop(bestDmgDealer != null ? bestDmgDealer : lastAttacker);
			
			// Manage drop of Special Events created by GM for a defined period
			doEventDrop(lastAttacker);
			
			if (!getMustRewardExpSP()) {
				return;
			}
			
			if (!rewards.isEmpty()) {
				L2Party attackerParty;
				long exp;
				int partyDmg, partyLvl, sp;
				float partyMul, penalty;
				RewardInfo reward2;
				long[] tmp;
				
				List<Creature> toRemove = new ArrayList<>();
				for (Entry<Creature, RewardInfo> entry : rewards.entrySet()) {
					if (entry == null || toRemove.contains(entry.getKey())) {
						continue;
					}
					
					reward = entry.getValue();
					
					if (reward == null) {
						continue;
					}
					
					// Penalty applied to the attacker's XP
					penalty = 0;
					
					// Attacker to be rewarded
					attacker = reward.attacker;
					
					// Total amount of damage done
					damage = reward.dmg;
					
					// If the attacker is a Pet, get the party of the owner
					if (attacker instanceof PetInstance) {
						attackerParty = attacker.getParty();
					} else if (attacker instanceof Player) {
						attackerParty = attacker.getParty();
					} else {
						return;
					}
					
					// If this attacker is a Player with a summoned SummonInstance, get Exp Penalty applied for the current summoned SummonInstance
					if (attacker instanceof Player && !((Player) attacker).getSummons().isEmpty()) {
						penalty = 0;
						for (SummonInstance summon : ((Player) attacker).getSummons()) {
							penalty += summon.getExpPenalty();
						}
					}
					
					// We must avoid "over damage", if any
					if (damage > getMaxHp()) {
						damage = getMaxHp();
					}
					
					// If there's NO party in progress
					if (attackerParty == null) {
						// Calculate Exp and SP rewards
						if (attacker.getKnownList().knowsObject(this)) {
							tmp = calculateExpAndSp(attacker.getLevel(), getLevel(), damage);
							exp = tmp[0];
							exp *= 1 - penalty;
							sp = (int) tmp[1];
							
							if (Config.L2JMOD_CHAMPION_ENABLE && isChampion()) {
								exp *= Config.L2JMOD_CHAMPION_REWARDS;
								sp *= Config.L2JMOD_CHAMPION_REWARDS;
							}
							
							// Check for an over-hit enabled strike
							if (attacker instanceof Player) {
								Player player = (Player) attacker;
								if (isOverhit() && attacker == getOverhitAttacker()) {
									player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OVER_HIT));
									exp += calculateOverhitExp(exp);
								}
							}
							
							// Distribute the Exp and SP between the Player and its Summon
							if (!attacker.isDead()) {
								long addexp = Math.round(attacker.calcStat(Stats.EXP_RATE, exp, null, null));
								int addsp = (int) attacker.calcStat(Stats.SP_RATE, sp, null, null);
								
								if (attacker instanceof Player) {
									Player pcAttacker = (Player) attacker;
									if (pcAttacker.getSkillLevelHash(467) > 0) {
										Skill skill = SkillTable.getInstance().getInfo(467, attacker.getSkillLevelHash(467));
										
										if (skill.getExpNeeded() <= addexp) {
											pcAttacker.absorbSoul(skill, this);
										}
									}
									if (pcAttacker.getLevel() >= getLevel() && pcAttacker.getLevel() - getLevel() < 11 ||
											pcAttacker.getLevel() < getLevel() && getLevel() - pcAttacker.getLevel() < 11) {
										if (pcAttacker.getReputation() < 0) {
											pcAttacker.updateReputationForHunting(addexp, addsp);
										}
										
										pcAttacker.addExpAndSp(addexp, addsp, useVitalityRate());
										if (addexp > 0) {
											pcAttacker.updateVitalityPoints(getVitalityPoints(damage), true, false);
										}
									}
								} else {
									attacker.addExpAndSp(addexp, addsp);
								}
							}
						}
					} else {
						//share with party members
						partyDmg = 0;
						partyMul = 1.f;
						partyLvl = 0;
						
						// Get all Creature that can be rewarded in the party
						List<Playable> rewardedMembers = new ArrayList<>();
						// Go through all Player in the party
						List<Player> groupMembers;
						
						if (attackerParty.isInCommandChannel()) {
							groupMembers = attackerParty.getCommandChannel().getMembers();
						} else {
							groupMembers = attackerParty.getPartyMembers();
						}
						
						for (Player pl : groupMembers) {
							if (pl == null || pl.isDead()) {
								continue;
							}
							
							// Get the RewardInfo of this Player from Attackable rewards
							reward2 = rewards.get(pl);
							
							// If the Player is in the Attackable rewards add its damages to party damages
							if (reward2 != null) {
								if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true)) {
									partyDmg += reward2.dmg; // Add Player damages to party damages
									rewardedMembers.add(pl);
									
									if (pl.getLevel() > partyLvl) {
										if (attackerParty.isInCommandChannel()) {
											partyLvl = attackerParty.getCommandChannel().getLevel();
										} else {
											partyLvl = pl.getLevel();
										}
									}
								}
								
								toRemove.add(pl); // Remove the Player from the Attackable rewards
							} else {
								// Add Player of the party (that have attacked or not) to members that can be rewarded
								// and in range of the monster.
								if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true)) {
									rewardedMembers.add(pl);
									if (pl.getLevel() > partyLvl) {
										if (attackerParty.isInCommandChannel()) {
											partyLvl = attackerParty.getCommandChannel().getLevel();
										} else {
											partyLvl = pl.getLevel();
										}
									}
								}
							}
							Playable summon = pl.getPet();
							
							if (summon != null && summon instanceof PetInstance) {
								reward2 = rewards.get(summon);
								
								if (reward2 != null) // Pets are only added if they have done damage
								{
									if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, summon, true)) {
										partyDmg += reward2.dmg; // Add summon damages to party damages
										rewardedMembers.add(summon);
										
										if (summon.getLevel() > partyLvl) {
											partyLvl = summon.getLevel();
										}
									}
									toRemove.add(summon); // Remove the summon from the Attackable rewards
								}
							}
						}
						
						// If the party didn't killed this Attackable alone
						if (partyDmg < getMaxHp()) {
							partyMul = (float) partyDmg / (float) getMaxHp();
						}
						
						// Avoid "over damage"
						if (partyDmg > getMaxHp()) {
							partyDmg = getMaxHp();
						}
						
						// Calculate Exp and SP rewards
						tmp = calculateExpAndSp(partyLvl, getLevel(), partyDmg);
						exp = tmp[0];
						sp = (int) tmp[1];
						
						if (Config.L2JMOD_CHAMPION_ENABLE && isChampion()) {
							exp *= Config.L2JMOD_CHAMPION_REWARDS;
							sp *= Config.L2JMOD_CHAMPION_REWARDS;
						}
						
						exp *= partyMul;
						sp *= partyMul;
						
						// Check for an over-hit enabled strike
						// (When in party, the over-hit exp bonus is given to the whole party and splitted proportionally through the party members)
						if (attacker instanceof Player) {
							Player player = (Player) attacker;
							
							if (isOverhit() && attacker == getOverhitAttacker()) {
								player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OVER_HIT));
								exp += calculateOverhitExp(exp);
							}
						}
						// Distribute Experience and SP rewards to Player Party members in the known area of the last attacker
						if (partyDmg > 0) {
							attackerParty.distributeXpAndSp(exp, sp, rewardedMembers, partyLvl, partyDmg, this);
						}
					}
				}
			}
			rewards = null;
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	/**
	 * @see Creature#addAttackerToAttackByList(Creature)
	 */
	@Override
	public void addAttackerToAttackByList(Creature player) {
		if (player == null || player == this || getAttackByList().contains(player)) {
			return;
		}
		
		if (this instanceof GuardInstance && player instanceof Playable && isInvul(player)) {
			return;
		}
		
		getAttackByList().add(player);
	}
	
	/**
	 * Add damage and hate to the attacker AggroInfo of the Attackable aggroList.
	 *
	 * @param attacker The Creature that gave damages to this Attackable
	 * @param damage   The number of damages given by the attacker Creature
	 */
	public void addDamage(Creature attacker, int damage, Skill skill) {
		if (attacker == null) {
			return;
		}
		
		if (this instanceof GuardInstance && attacker instanceof Playable && isInvul(attacker)) {
			return;
		}
		
		// Notify the Attackable AI with EVT_ATTACKED
		if (!isDead()) {
			try {
				Player player = attacker.getActingPlayer();
				if (player != null) {
					if (getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK) != null) {
						for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK)) {
							quest.notifyAttack(this, player, damage, attacker instanceof Summon, skill);
						}
					}
				}
				// for now hard code damage hate caused by an Attackable
				else {
					getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
					addDamageHate(attacker, damage, damage * 100 / (getLevel() + 7));
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
	
	/**
	 * Add damage and hate to the attacker AggroInfo of the Attackable aggroList.
	 *
	 * @param attacker The Creature that gave damages to this Attackable
	 * @param damage   The number of damages given by the attacker Creature
	 * @param aggro    The hate (=damage) given by the attacker Creature
	 */
	public void addDamageHate(Creature attacker, int damage, int aggro) {
		if (attacker == null || attacker == this) {
			return;
		}
		
		if (this instanceof GuardInstance && attacker instanceof Playable && isInvul(attacker)) {
			return;
		}
		
		// Modify the aggro with the attacker's aggression damage modifier
		aggro = (int) attacker.calcStat(Stats.AGGRESSION_DMG, aggro, this, null);
		
		int maxDist = 10000;
		if (isRaid() || isMinion()) {
			maxDist = 3000;
		}
		if (!(this instanceof GrandBossInstance) && !isCastingNow() && getInstanceId() == 0 && getSpawn() != null && getSpawn().getGroup() == null &&
				!isInsideRadius(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), maxDist, true, false)) {
			escape("I have gone too far from my home... Sorry, but I must return.");
			return;
		}
		
		Player targetPlayer = attacker.getActingPlayer();
		// Get the AggroInfo of the attacker Creature from the aggroList of the Attackable
		AggroInfo ai = getAggroList().get(attacker);
		
		if (ai == null) {
			// Before adding aggro info to this attackable, check how many other attackables hate it
			if (targetPlayer != null && !isRaid() && !isMinion() && getInstanceId() == 0) {
				int haters = 0;
				for (Creature c : targetPlayer.getKnownList().getKnownCharacters()) {
					if (c instanceof Attackable && ((Attackable) c).getMostHated() == targetPlayer && !c.isMinion()) {
						haters++;
						// If there are more than 20 attackables hating this player already, don't add another one
						if (haters > 34) {
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
		if (targetPlayer == null || targetPlayer.getTrap() == null || !targetPlayer.getTrap().isTriggered()) {
			ai.addHate(aggro);
		}
		
		if (targetPlayer != null && aggro == 0) {
			if (getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER) != null) {
				for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER)) {
					quest.notifyAggroRangeEnter(this, targetPlayer, attacker instanceof Summon);
				}
			}
		} else if (targetPlayer == null && aggro == 0) {
			aggro = 1;
			ai.addHate(1);
		}
		
		// Set the intention to the Attackable to AI_INTENTION_ACTIVE
		if (aggro > 0 && getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) {
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}
	
	public void reduceHate(Creature target, int amount) {
		if (getAI() instanceof SiegeGuardAI || getAI() instanceof FortSiegeGuardAI) {
			// TODO: this just prevents error until siege guards are handled properly
			stopHating(target);
			setTarget(null);
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			return;
		}
		
		if (target == null) // whole aggrolist
		{
			Creature mostHated = getMostHated();
			if (mostHated == null) // makes target passive for a moment more
			{
				if (getAI() instanceof AttackableAI) {
					((AttackableAI) getAI()).setGlobalAggro(-25);
				}
				return;
			} else {
				for (Creature aggroed : getAggroList().keySet()) {
					AggroInfo ai = getAggroList().get(aggroed);
					if (ai == null) {
						return;
					}
					ai.addHate(-amount);
				}
			}
			
			amount = getHating(mostHated);
			
			if (amount <= 0 && getAI() instanceof AttackableAI) {
				((AttackableAI) getAI()).setGlobalAggro(-25);
				clearAggroList();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				setWalking();
			}
			return;
		}
		AggroInfo ai = getAggroList().get(target);
		
		if (ai == null) {
			return;
		}
		ai.addHate(-amount);
		
		if (ai.getHate() <= 0) {
			if (getMostHated() == null) {
				((AttackableAI) getAI()).setGlobalAggro(-25);
				clearAggroList();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				setWalking();
			}
		}
	}
	
	/**
	 * Clears aggroList hate of the Creature without removing from the list.
	 */
	public void stopHating(Creature target) {
		if (target == null) {
			return;
		}
		AggroInfo ai = getAggroList().get(target);
		if (ai != null) {
			ai.stopHate();
		}
	}
	
	/**
	 * Return the most hated Creature of the Attackable aggroList.
	 */
	public Creature getMostHated() {
		if (getAggroList().isEmpty() || isAlikeDead()) {
			return null;
		}
		
		Creature mostHated = null;
		int maxHate = 0;
		
		// While Interating over This Map Removing Object is Not Allowed
		//synchronized (getAggroList())
		{
			// Go through the aggroList of the Attackable
			for (AggroInfo ai : getAggroList().values()) {
				if (ai == null) {
					continue;
				}
				
				if (ai.checkHate(this) > maxHate) {
					mostHated = ai.getAttacker();
					maxHate = ai.getHate();
				}
			}
		}
		return mostHated;
	}
	
	/**
	 * Return the 2 most hated Creature of the Attackable aggroList.
	 */
	public List<Creature> get2MostHated() {
		if (getAggroList().isEmpty() || isAlikeDead()) {
			return null;
		}
		
		Creature mostHated = null;
		Creature secondMostHated = null;
		int maxHate = 0;
		List<Creature> result = new ArrayList<>();
		
		// While iterating over this map removing objects is not allowed
		//synchronized (getAggroList())
		{
			// Go through the aggroList of the Attackable
			for (AggroInfo ai : getAggroList().values()) {
				if (ai == null) {
					continue;
				}
				
				if (ai.checkHate(this) > maxHate) {
					secondMostHated = mostHated;
					mostHated = ai.getAttacker();
					maxHate = ai.getHate();
				}
			}
		}
		result.add(mostHated);
		
		if (getAttackByList().contains(secondMostHated)) {
			result.add(secondMostHated);
		} else {
			result.add(null);
		}
		return result;
	}
	
	public List<Creature> getHateList() {
		if (getAggroList().isEmpty() || isAlikeDead()) {
			return null;
		}
		List<Creature> result = new ArrayList<>();
		
		//synchronized (getAggroList())
		{
			for (AggroInfo ai : getAggroList().values()) {
				if (ai == null) {
					continue;
				}
				ai.checkHate(this);
				
				result.add(ai.getAttacker());
			}
		}
		
		return result;
	}
	
	/**
	 * Return the hate level of the Attackable against this Creature contained in aggroList.
	 *
	 * @param target The Creature whose hate level must be returned
	 */
	public int getHating(final Creature target) {
		if (getAggroList().isEmpty() || target == null) {
			return 0;
		}
		
		AggroInfo ai = getAggroList().get(target);
		
		if (ai == null) {
			return 0;
		}
		
		if (ai.getAttacker() instanceof Player) {
			Player act = (Player) ai.getAttacker();
			if (act.getAppearance().getInvisible() /*|| ai.getAttacker().isInvul()*/ || act.isSpawnProtected()) {
				//Remove Object Should Use This Method and Can be Blocked While Interating
				getAggroList().remove(target);
				return 0;
			}
		}
		
		if (!ai.getAttacker().isVisible()) {
			getAggroList().remove(target);
			return 0;
		}
		
		if (ai.getAttacker().isAlikeDead()) {
			ai.stopHate();
			return 0;
		}
		return ai.getHate();
	}
	
	/**
	 * Calculates quantity of items for specific drop acording to current situation
	 *
	 * @param drop          The L2DropData count is being calculated for
	 * @param lastAttacker  The Player that has killed the Attackable
	 * @param levelModifier level modifier in %'s (will be subtracted from drop chance)
	 */
	private RewardItem calculateRewardItem(Player lastAttacker, L2DropData drop, int levelModifier, boolean isSweep) {
		// Get default drop chance
		float dropChance = drop.getChance();
		
		int deepBlueDrop = 1;
		
		if (!isRaid() && Config.DEEPBLUE_DROP_RULES || isRaid() && Config.DEEPBLUE_DROP_RULES_RAID) {
			if (levelModifier > 0) {
				// We should multiply by the server's drop rate, so we always get a low chance of drop for deep blue mobs.
				// NOTE: This is valid only for adena drops! Others drops will still obey server's rate
				deepBlueDrop = 3;
				if (drop.getItemId() == 57) {
					deepBlueDrop *= isRaid() && !isRaidMinion() ? (int) Config.RATE_DROP_ITEMS_BY_RAID : (int) Config.RATE_DROP_ITEMS;
				}
			}
		}
		
		// Avoid dividing by 0
		if (deepBlueDrop == 0) {
			deepBlueDrop = 1;
		}
		
		// Check if we should apply our maths so deep blue mobs will not drop that easy
		if (!isRaid() && Config.DEEPBLUE_DROP_RULES || isRaid() && Config.DEEPBLUE_DROP_RULES_RAID) {
			dropChance = (drop.getChance() - drop.getChance() * levelModifier / 100) / deepBlueDrop;
		}
		
		// Applies Drop rates
		if (!drop.isCustom()) {
			if (Config.RATE_DROP_ITEMS_ID.get(drop.getItemId()) != 0) {
				dropChance *= Config.RATE_DROP_ITEMS_ID.get(drop.getItemId());
			} else if (isSweep) {
				dropChance *= Config.RATE_DROP_SPOIL;
			} else {
				dropChance *= isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
			}
		}
		
		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion()) {
			dropChance *= Config.L2JMOD_CHAMPION_REWARDS;
		}
		
		dropChance = (float) lastAttacker.calcStat(Stats.DROP_RATE, dropChance, this, null);
		
		// Set our limits for chance of drop
		if (dropChance < 0.00001F) {
			dropChance = 0.00001F;
		}
		
		// Get min and max Item quantity that can be dropped in one time
		int minCount = drop.getMinDrop();
		int maxCount = drop.getMaxDrop();
		long itemCount = 0;
		
		// Check if the Item must be dropped
		int random = Rnd.get(L2DropData.MAX_CHANCE * 100000);
		while (random < dropChance * 100000) {
			// Get the item quantity dropped
			if (minCount < maxCount) {
				itemCount += Rnd.get(minCount, maxCount);
			} else if (minCount == maxCount) {
				itemCount += minCount;
			} else {
				itemCount++;
			}
			
			// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
			dropChance -= L2DropData.MAX_CHANCE;
		}
		
		if (Config.L2JMOD_CHAMPION_ENABLE && (drop.getItemId() == 57 || drop.getItemId() >= 6360 && drop.getItemId() <= 6362) && isChampion()) {
			itemCount *= Config.L2JMOD_CHAMPION_ADENAS_REWARDS;
		}
		
		if (itemCount > 0) {
			return new RewardItem(drop.getItemId(), itemCount);
		} else if (itemCount == 0 && Config.DEBUG) {
			log.debug("Roll produced no drops.");
		}
		
		return null;
	}
	
	/**
	 * Calculates quantity of items for specific drop CATEGORY according to current situation
	 * Only a max of ONE item from a category is allowed to be dropped.
	 *
	 * @param lastAttacker  The Player that has killed the Attackable
	 * @param levelModifier level modifier in %'s (will be subtracted from drop chance)
	 */
	private RewardItem[] calculateCategorizedRewardItems(Player lastAttacker, L2DropCategory categoryDrops, int levelModifier) {
		if (categoryDrops == null) {
			return null;
		}
		
		// Get default drop chance for the category (that's the sum of chances for all items in the category)
		// keep track of the base category chance as it'll be used later, if an item is drop from the category.
		// for everything else, use the total "categoryDropChance"
		float dropRate = isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
		if (categoryDrops.isCustom()) {
			dropRate = 1;
		}
		
		dropRate = (float) lastAttacker.calcStat(Stats.DROP_RATE, dropRate, this, null);
		float categoryDropChance = categoryDrops.getChance() * dropRate;
		
		int deepBlueDrop = 1;
		
		if (!isRaid() && Config.DEEPBLUE_DROP_RULES || isRaid() && Config.DEEPBLUE_DROP_RULES_RAID) {
			// We should multiply by the server's drop rate, so we always get a low chance of drop for deep blue mobs.
			// NOTE: This is valid only for adena drops! Others drops will still obey server's rate
			if (levelModifier > 0) {
				deepBlueDrop = 3;
			}
		}
		
		// Avoid dividing by 0
		if (deepBlueDrop == 0) {
			deepBlueDrop = 1;
		}
		
		// Check if we should apply our maths so deep blue mobs will not drop that easy
		if (!isRaid() && Config.DEEPBLUE_DROP_RULES || isRaid() && Config.DEEPBLUE_DROP_RULES_RAID) {
			categoryDropChance = (categoryDropChance - categoryDropChance * levelModifier / 100) / deepBlueDrop;
		}
		
		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion()) {
			categoryDropChance *= Config.L2JMOD_CHAMPION_REWARDS;
		}
		
		// Set our limits for chance of drop
		if (categoryDropChance < 0.00001F) {
			categoryDropChance = 0.00001F;
		}
		
		TIntIntHashMap rewardsMap = new TIntIntHashMap();
		// Check if an Item from this category must be dropped
		int random = Rnd.get(L2DropData.MAX_CHANCE * 100000);
		while (random < categoryDropChance * 100000) {
			L2DropData drop = categoryDrops.dropOne();
			if (drop == null) {
				categoryDropChance -= L2DropData.MAX_CHANCE;
				continue;
			}
			
			int itemCount = Rnd.get(drop.getMinDrop(), drop.getMaxDrop());
			if (itemCount > 0) {
				if (rewardsMap.contains(drop.getItemId())) {
					itemCount += rewardsMap.get(drop.getItemId());
				}
				rewardsMap.put(drop.getItemId(), itemCount);
			}
			categoryDropChance -= L2DropData.MAX_CHANCE;
		}
		
		RewardItem[] rewards = new RewardItem[rewardsMap.size()];
		int index = 0;
		for (int itemId : rewardsMap.keys()) {
			int itemCount = rewardsMap.get(itemId);
			if (Config.RATE_DROP_ITEMS_ID.get(itemId) != 0) {
				itemCount *= Config.RATE_DROP_ITEMS_ID.get(itemId) / dropRate;
			}
			
			if (Config.L2JMOD_CHAMPION_ENABLE && (itemId == 57 || itemId >= 6360 && itemId <= 6362) && isChampion()) {
				itemCount *= Config.L2JMOD_CHAMPION_ADENAS_REWARDS;
			}
			
			if (!Config.MULTIPLE_ITEM_DROP && !ItemTable.getInstance().getTemplate(itemId).isStackable() && itemCount > 1) {
				itemCount = 1;
			}
			if (itemCount < 1) {
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
	 * @param lastAttacker The Player that has killed the Attackable
	 */
	private int calculateLevelModifierForDrop(Player lastAttacker) {
		if (!isRaid() && Config.DEEPBLUE_DROP_RULES || isRaid() && Config.DEEPBLUE_DROP_RULES_RAID) {
			int highestLevel = lastAttacker.getLevel();
			
			// Check to prevent very high level player to nearly kill mob and let low level player do the last hit.
			/*if (!getAttackByList().isEmpty())
            {
				for (Creature atkChar: getAttackByList())
					if (atkChar != null && atkChar.getLevel() > highestLevel)
						highestLevel = atkChar.getLevel();
			}*/
			
			// According to official data (Prima), deep blue mobs are 9 or more levels below players
			if (highestLevel - 9 >= getLevel()) {
				return (highestLevel - (getLevel() + 8)) * 9;
			}
		}
		return 0;
	}
	
	private RewardItem calculateCategorizedExtraItem(Player lastAttacker, L2DropCategory categoryDrops) {
		if (categoryDrops == null) {
			return null;
		}
		
		// Get default drop chance for the category (that's the sum of chances for all items in the category)
		// keep track of the base category chance as it'll be used later, if an item is drop from the category.
		// for everything else, use the total "categoryDropChance"
		float categoryDropChance = categoryDrops.getChance();
		
		// Set our limits for chance of drop
		if (categoryDropChance < 0.00001F) {
			categoryDropChance = 0.00001F;
		}
		
		// Check if an Item from this category must be dropped
		if (Rnd.get(L2DropData.MAX_CHANCE * 10000) < categoryDropChance * 10000) {
			L2DropData drop = categoryDrops.dropOne();
			
			if (drop == null) {
				return null;
			}
			
			int itemCount = Rnd.get(drop.getMinDrop(), drop.getMaxDrop());
			
			if (!Config.MULTIPLE_ITEM_DROP && !ItemTable.getInstance().getTemplate(drop.getItemId()).isStackable() && itemCount > 1) {
				itemCount = 1;
			}
			
			if (this instanceof ArmyMonsterInstance) {
				itemCount = 1;
			}
			
			if (itemCount > 0) {
				return new RewardItem(drop.getItemId(), itemCount);
			} else if (itemCount == 0 && Config.DEBUG) {
				log.debug("Roll produced no drops.");
			}
		}
		return null;
	}
	
	public void doItemDrop(Creature lastAttacker) {
		doItemDrop(getTemplate(), lastAttacker);
	}
	
	/**
	 * Manage Base, Quests and Special Events drops of Attackable (called by calculateRewards).
	 * <p>
	 * Concept:
	 * During a Special Event all Attackable can drop extra Items.
	 * Those extra Items are defined in the table allNpcDateDrops of the EventDroplist.
	 * Each Special Event has a start and end date to stop to drop extra Items automaticaly.
	 * <p>
	 * Actions:
	 * Manage drop of Special Events created by GM for a defined period
	 * Get all possible drops of this Attackable from NpcTemplate and add it Quest drops
	 * For each possible drops (base + quests), calculate which one must be dropped (random)
	 * Get each Item quantity dropped (random)
	 * Create this or these Item corresponding to each Item Identifier dropped
	 * If the autoLoot mode is actif and if the Creature that has killed the Attackable is a Player, Give the item(s) to the Player that has killed the Attackable
	 * If the autoLoot mode isn't actif or if the Creature that has killed the Attackable is not a Player, add this or these item(s) in the world as a visible object at the position where mob was last
	 *
	 * @param lastAttacker The Creature that has killed the Attackable
	 */
	public void doItemDrop(NpcTemplate npcTemplate, Creature lastAttacker) {
		if (lastAttacker == null) {
			return;
		}
		
		// Don't drop anything if the last attacker or owner isn't Player
		final Player player = lastAttacker.getActingPlayer();
		if (player == null) {
			return;
		}
		
		// level modifier in %'s (will be subtracted from drop chance)
		int levelModifier = calculateLevelModifierForDrop(player);
		
		CursedWeaponsManager.getInstance().checkDrop(this, player);
		
		// now throw all categorized drops and handle spoil.
		if (isSpoil()) {
			RewardItem item = null;
			ArrayList<RewardItem> sweepList = new ArrayList<>();
			
			for (L2DropData dd : npcTemplate.getSpoilData()) {
				item = calculateRewardItem(player, dd, levelModifier, true);
				if (item == null) {
					continue;
				}
				
				if (Config.DEBUG) {
					log.debug("Item id to spoil: " + item.getItemId() + " amount: " + item.getCount());
				}
				sweepList.add(item);
			}
			// Set the table sweepItems of this Attackable
			if (!sweepList.isEmpty()) {
				sweepItems = sweepList.toArray(new RewardItem[sweepList.size()]);
			}
		}
		
		for (L2DropData dd : npcTemplate.getDropData()) {
			RewardItem item = calculateRewardItem(player, dd, levelModifier, false);
			if (item == null) {
				continue;
			}
			
			if (Config.DEBUG) {
				log.debug("Item id to drop: " + item.getItemId() + " amount: " + item.getCount());
			}
			
			// Check if the autoLoot mode is active
			if ((isFlying() || !isRaid() && Config.AUTO_LOOT || isRaid() && Config.AUTO_LOOT_RAIDS) &&
					!(this instanceof ArmyMonsterInstance && !this.isAggressive())) {
				player.doAutoLoot(this, item); // Give the item(s) to the Player that has killed the Attackable
			} else if (Config.isServer(Config.TENKAI) && isRaid()) {
				long packs = 10;
				if (this instanceof GrandBossInstance) {
					packs = 100;
				}
				if (packs > item.getCount()) {
					packs = item.getCount();
				}
				for (int i = 0; i < packs; i++) {
					dropItem(player, new RewardItem(item.getItemId(), item.getCount() / packs));
				}
				dropItem(player, new RewardItem(item.getItemId(), item.getCount() % packs));
			} else {
				dropItem(player, item); // drop the item on the ground
			}
			
			// Broadcast message if RaidBoss was defeated
			if (isRaid() && !isRaidMinion()) {
				SystemMessage sm;
				sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DIED_DROPPED_S3_S2);
				sm.addCharName(this);
				sm.addItemName(item.getItemId());
				sm.addItemNumber(item.getCount());
				broadcastPacket(sm);
			}
		}
		
		RewardItem[] items;
		for (L2DropCategory cat : npcTemplate.getMultiDropData()) {
			if (isSeeded()) {
				L2DropData drop = cat.dropSeedAllowedDropsOnly();
				if (drop == null) {
					continue;
				}
				
				items = new RewardItem[]{calculateRewardItem(player, drop, levelModifier, false)};
			} else {
				items = calculateCategorizedRewardItems(player, cat, levelModifier);
			}
			
			for (RewardItem item : items) {
				if (item == null) {
					continue;
				}
				if (Config.DEBUG) {
					log.debug("Item id to drop: " + item.getItemId() + " amount: " + item.getCount());
				}
				
				// Check if the autoLoot mode is active
				if ((isFlying() || !isRaid() && Config.AUTO_LOOT || isRaid() && Config.AUTO_LOOT_RAIDS) &&
						!(this instanceof ArmyMonsterInstance && !this.isAggressive())) {
					player.doAutoLoot(this, item); // Give the item(s) to the Player that has killed the Attackable
				} else {
					dropItem(player, item); // drop the item on the ground
				}
				
				// Broadcast message if RaidBoss was defeated
				if (isRaid() && !isRaidMinion()) {
					SystemMessage sm;
					sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DIED_DROPPED_S3_S2);
					sm.addCharName(this);
					sm.addItemName(item.getItemId());
					sm.addItemNumber(item.getCount());
					broadcastPacket(sm);
				}
			}
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			if (isSpoil() && sweepItems != null && player.getCurrentClass().getLevel() >= 85) {
				final Player spoiler = World.getInstance().getPlayer(getIsSpoiledBy());
				if (spoiler != null && Util.checkIfInRange(900, Attackable.this, spoiler, false)) {
					setSpoil(false);
					for (RewardItem ritem : sweepItems) {
						if (spoiler.isInParty()) {
							spoiler.getParty().distributeItem(spoiler, ritem, true, Attackable.this);
						} else {
							spoiler.addItem("Sweep", ritem.getItemId(), ritem.getCount(), spoiler, true);
						}
					}
					
					endDecayTask();
				}
			}
		}, 500);
		
		// Apply Special Item drop with random(rnd) quantity(qty) for champions.
		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion() &&
				(Config.L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE > 0 || Config.L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE > 0)) {
			RewardItem item = null;
			int champqty = Rnd.get(Config.L2JMOD_CHAMPION_REWARD_QTY);
			item = new RewardItem(Config.L2JMOD_CHAMPION_REWARD_ID, ++champqty);
			
			if (player.getLevel() <= getLevel() && Rnd.get(100) < Config.L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE) {
				if (Config.AUTO_LOOT || isFlying()) {
					player.addItem("ChampionLoot",
							item.getItemId(),
							item.getCount(),
							this,
							true); // Give the item(s) to the Player that has killed the Attackable
				} else {
					dropItem(player, item);
				}
			} else if (player.getLevel() >= getLevel() && Rnd.get(100) < Config.L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE) {
				if (Config.AUTO_LOOT || isFlying()) {
					player.addItem("ChampionLoot",
							item.getItemId(),
							item.getCount(),
							this,
							true); // Give the item(s) to the Player that has killed the Attackable
				} else {
					dropItem(player, item);
				}
			}
		}
		
		//Instant Item Drop :>
		if (getTemplate().ExtraDropGroup > 0 && !Config.isServer(Config.TENKAI)) {
			RewardItem item = null;
			for (L2DropCategory cat : ExtraDropTable.getInstance().getExtraDroplist(getTemplate().ExtraDropGroup)) {
				item = calculateCategorizedExtraItem(player, cat);
				if (item != null) {
					// more than one herb cant be auto looted!
					long count = item.getCount();
					if (count > 1) {
						item.count = 1;
						for (int i = 0; i < count; i++) {
							dropItem(player, item);
						}
					} else if (isFlying() || Config.AUTO_LOOT_HERBS) {
						player.addItem("Loot", item.getItemId(), count, this, true);
					} else {
						dropItem(player, item);
					}
				}
			}
		}
		
		for (GlobalDropCategory drop : GlobalDropTable.getInstance().getGlobalDropCategories()) {
			
			RewardItem item = null;
			int random = Rnd.get(100000);
			if (getLevel() + 5 < player.getLevel()) {
				random *= player.getLevel() - (getLevel() + 4);
			}
			if (Config.L2JMOD_CHAMPION_ENABLE && isChampion()) {
				random *= Config.L2JMOD_CHAMPION_REWARDS;
			}
			
			if (drop.isRaidOnly() && !isRaid()) {
				continue;
			}

			/*if (isRaid() && !isRaidMinion())
				random /= 100;
			if (this instanceof GrandBossInstance)
				random /= 10;*/
			
			if (random < drop.getChance() && drop.canLootNow(player) && (drop.getMobId() == 0 || getNpcId() == drop.getMobId()) &&
					getLevel() >= drop.getMinLevel() && getLevel() <= drop.getMaxLevel()) {
				drop.increaseCountForPlayer(player);
				item = new RewardItem(drop.getRandomReward(), drop.getMinAmount() + Rnd.get(drop.getMaxAmount() - drop.getMinAmount()));
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
	 * During a Special Event all Attackable can drop extra Items.
	 * Those extra Items are defined in the table allNpcDateDrops of the EventDroplist.
	 * Each Special Event has a start and end date to stop to drop extra Items automaticaly.
	 * <p>
	 * Actions: <I>If an extra drop must be generated</I>
	 * Get an Item Identifier (random) from the DateDrop Item table of this Event
	 * Get the Item quantity dropped (random)
	 * Create this or these Item corresponding to this Item Identifier
	 * If the autoLoot mode is actif and if the Creature that has killed the Attackable is a Player, Give the item(s) to the Player that has killed the Attackable
	 * If the autoLoot mode isn't actif or if the Creature that has killed the Attackable is not a Player, add this or these item(s) in the world as a visible object at the position where mob was last
	 *
	 * @param lastAttacker The Creature that has killed the Attackable
	 */
	public void doEventDrop(Creature lastAttacker) {
		if (lastAttacker == null) {
			return;
		}
		
		Player player = lastAttacker.getActingPlayer();
		
		// Don't drop anything if the last attacker or owner isn't Player
		if (player == null) {
			return;
		}
		
		if (player.getLevel() - getLevel() > 9) {
			return;
		}
		
		// Go through DateDrop of EventDroplist allNpcDateDrops within the date range
		for (DateDrop drop : EventDroplist.getInstance().getAllDrops()) {
			if (Rnd.get(L2DropData.MAX_CHANCE) < drop.chance) {
				RewardItem item = new RewardItem(drop.items[Rnd.get(drop.items.length)], Rnd.get(drop.min, drop.max));
				
				if (Config.AUTO_LOOT || isFlying()) {
					player.doAutoLoot(this, item); // Give the item(s) to the Player that has killed the Attackable
				} else {
					dropItem(player, item); // drop the item on the ground
				}
			}
		}
	}
	
	/**
	 * Drop reward item.
	 */
	public Item dropItem(Player lastAttacker, RewardItem item) {
		int randDropLim = 70;
		
		Item ditem = null;
		for (int i = 0; i < item.getCount(); i++) {
			// Randomize drop position
			int newX = getX() + Rnd.get(randDropLim * 2 + 1) - randDropLim;
			int newY = getY() + Rnd.get(randDropLim * 2 + 1) - randDropLim;
			int newZ = Math.max(getZ(), lastAttacker.getZ()) + 20; // TODO: temp hack, do somethign nicer when we have geodatas
			
			if (ItemTable.getInstance().getTemplate(item.getItemId()) != null) {
				// Init the dropped Item and add it in the world as a visible object at the position where mob was last
				ditem = ItemTable.getInstance().createItem("Loot", item.getItemId(), item.getCount(), lastAttacker, this);
				
				ditem.dropMe(this, newX, newY, newZ);
				
				// Add drop to auto destroy item task
				if (!Config.LIST_PROTECTED_ITEMS.contains(item.getItemId())) {
					if (Config.AUTODESTROY_ITEM_AFTER * 1000 > 0 && ditem.getItemType() != EtcItemType.HERB ||
							Config.HERB_AUTO_DESTROY_TIME * 1000 > 0 && ditem.getItemType() == EtcItemType.HERB) {
						ItemsAutoDestroy.getInstance().addItem(ditem);
					}
				}
				ditem.setProtected(false);
				
				// If stackable, end loop as entire count is included in 1 instance of item
				if (ditem.isStackable() || !Config.MULTIPLE_ITEM_DROP) {
					break;
				}
			} else {
				log.error("Item doesn't exist so cannot be dropped. Item ID: " + item.getItemId());
			}
		}
		return ditem;
	}
	
	public Item dropItem(Player lastAttacker, int itemId, int itemCount) {
		return dropItem(lastAttacker, new RewardItem(itemId, itemCount));
	}
	
	/**
	 * Return the active weapon of this Attackable (= null).
	 */
	public Item getActiveWeapon() {
		return null;
	}
	
	/**
	 * Return True if the aggroList of this Attackable is Empty.
	 */
	public boolean noTarget() {
		return getAggroList().isEmpty();
	}
	
	/**
	 * Return True if the aggroList of this Attackable contains the Creature.
	 *
	 * @param player The Creature searched in the aggroList of the Attackable
	 */
	public boolean containsTarget(Creature player) {
		return getAggroList().containsKey(player);
	}
	
	/**
	 * Clear the aggroList of the Attackable.
	 */
	public void clearAggroList() {
		getAggroList().clear();
		
		// clear overhit values
		overhit = false;
		overhitDamage = 0;
		overhitAttacker = null;
	}
	
	/**
	 * Return True if a Dwarf use Sweep on the Attackable and if item can be spoiled.
	 */
	public boolean isSweepActive() {
		return sweepItems != null;
	}
	
	/**
	 * Return table containing all Item that can be spoiled.
	 */
	public synchronized RewardItem[] takeSweep() {
		RewardItem[] sweep = sweepItems;
		sweepItems = null;
		return sweep;
	}
	
	/**
	 * Return table containing all Item that can be harvested.
	 */
	public synchronized RewardItem[] takeHarvest() {
		RewardItem[] harvest = harvestItems;
		harvestItems = null;
		return harvest;
	}
	
	/**
	 * Set the over-hit flag on the Attackable.
	 *
	 * @param status The status of the over-hit flag
	 */
	public void overhitEnabled(boolean status) {
		overhit = status;
	}
	
	/**
	 * Set the over-hit values like the attacker who did the strike and the amount of damage done by the skill.
	 *
	 * @param attacker The Creature who hit on the Attackable using the over-hit enabled skill
	 * @param damage   The ammount of damage done by the over-hit enabled skill on the Attackable
	 */
	public void setOverhitValues(Creature attacker, double damage) {
		// Calculate the over-hit damage
		// Ex: mob had 10 HP left, over-hit skill did 50 damage total, over-hit damage is 40
		double overhitDmg = (getCurrentHp() - damage) * -1;
		if (overhitDmg < 0) {
			// we didn't killed the mob with the over-hit strike. (it wasn't really an over-hit strike)
			// let's just clear all the over-hit related values
			overhitEnabled(false);
			overhitDamage = 0;
			overhitAttacker = null;
			return;
		}
		overhitEnabled(true);
		overhitDamage = overhitDmg;
		overhitAttacker = attacker;
	}
	
	/**
	 * Return the Creature who hit on the Attackable using an over-hit enabled skill.
	 *
	 * @return Creature attacker
	 */
	public Creature getOverhitAttacker() {
		return overhitAttacker;
	}
	
	/**
	 * Return the ammount of damage done on the Attackable using an over-hit enabled skill.
	 *
	 * @return double damage
	 */
	public double getOverhitDamage() {
		return overhitDamage;
	}
	
	/**
	 * Return True if the Attackable was hit by an over-hit enabled skill.
	 */
	public boolean isOverhit() {
		return overhit;
	}
	
	/**
	 * Activate the absorbed soul condition on the Attackable.
	 */
	public void absorbSoul() {
		absorbed = true;
	}
	
	/**
	 * Return True if the Attackable had his soul absorbed.
	 */
	public boolean isAbsorbed() {
		return absorbed;
	}
	
	/**
	 * Adds an attacker that successfully absorbed the soul of this Attackable into the absorbersList.
	 * <p>
	 * Params:
	 * attacker - a valid Player
	 * condition - an integer indicating the event when mob dies. This should be:
	 * = 0 - "the crystal scatters";
	 * = 1 - "the crystal failed to absorb. nothing happens";
	 * = 2 - "the crystal resonates because you got more than 1 crystal on you";
	 * = 3 - "the crystal cannot absorb the soul because the mob level is too low";
	 * = 4 - "the crystal successfuly absorbed the soul";
	 */
	public void addAbsorber(Player attacker) {
		// If we have no absorbersList initiated, do it
		AbsorberInfo ai = absorbersList.get(attacker.getObjectId());
		
		// If the Creature attacker isn't already in the absorbersList of this Attackable, add it
		if (ai == null) {
			ai = new AbsorberInfo(attacker.getObjectId(), getCurrentHp());
			absorbersList.put(attacker.getObjectId(), ai);
		} else {
			ai.objId = attacker.getObjectId();
			ai.absorbedHP = getCurrentHp();
		}
		
		// Set this Attackable as absorbed
		absorbSoul();
	}
	
	public void resetAbsorbList() {
		absorbed = false;
		absorbersList.clear();
	}
	
	public ConcurrentHashMap<Integer, AbsorberInfo> getAbsorbersList() {
		return absorbersList;
	}
	
	/**
	 * Calculate the Experience and SP to distribute to attacker (Player, SummonInstance or L2Party) of the Attackable.
	 *
	 * @param damage The damages given by the attacker (Player, SummonInstance or L2Party)
	 */
	private long[] calculateExpAndSp(int attackerLevel, int monsterLevel, int damage) {
		double xp;
		double sp;
		
		// Calculate the difference of level between this attacker (Player or SummonInstance owner) and the Attackable
		// mob = 24, atk = 10, diff = -14 (full xp)
		// mob = 24, atk = 28, diff = 4 (some xp)
		// mob = 24, atk = 50, diff = 26 (no xp)
		int diff = attackerLevel - monsterLevel;
		
		if (diff < -5) {
			diff = -5; // makes possible to use ALT_GAME_EXPONENT configuration
		}
		
		double expReward = getExpReward();
		double multiplier = 0;
		
		if (multiplier != 0) {
			expReward *= multiplier;
		}
		
		xp = expReward * damage / getMaxHp();
		
		if (Config.ALT_GAME_EXPONENT_XP != 0) {
			xp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_XP);
		}
		
		double spReward = getSpReward();
		
		if (multiplier != 0) {
			spReward *= multiplier;
		}
		
		sp = spReward * damage / getMaxHp();
		
		if (Config.ALT_GAME_EXPONENT_SP != 0) {
			sp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_SP);
		}
		
		if (Config.ALT_GAME_EXPONENT_XP == 0 && Config.ALT_GAME_EXPONENT_SP == 0) {
			if (diff > 5) // formula revised May 07
			{
				double pow = Math.pow((double) 5 / 6, diff - 5);
				xp = xp * pow;
				sp = sp * pow;
			}
			
			if (xp <= 0) {
				xp = 0;
				sp = 0;
			} else if (sp <= 0) {
				sp = 0;
			}
		}
		return new long[]{(long) xp, (long) sp};
	}
	
	public long calculateOverhitExp(long normalExp) {
		// Get the percentage based on the total of extra (over-hit) damage done relative to the total (maximum) ammount of HP on the Attackable
		double overhitPercentage = getOverhitDamage() * 100 / getMaxHp();
		
		// Over-hit damage percentages are limited to 25% max
		if (overhitPercentage > 25) {
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
	public boolean isAttackable() {
		return true;
	}
	
	@Override
	public void onSpawn() {
		super.onSpawn();
		// Clear mob spoil, seed
		setSpoil(false);
		// Clear all aggro char from list
		clearAggroList();
		// Clear Harvester Rewrard List
		harvestItems = null;
		// Clear mod Seeded stat
		seeded = false;
		seedType = 0;
		seederObjId = 0;
		// Clear overhit value
		overhitEnabled(false);
		
		sweepItems = null;
		resetAbsorbList();
		
		setWalking();
		
		// check the region where this mob is, do not activate the AI if region is inactive.
		if (!isInActiveRegion()) {
			if (hasAI()) {
				getAI().stopAITask();
			}
		}
	}
	
	/**
	 * Return True if this NpcInstance has drops that can be sweeped.<BR><BR>
	 */
	public boolean isSpoil() {
		return isSpoil;
	}
	
	/**
	 * Set the spoil state of this NpcInstance.<BR><BR>
	 */
	public void setSpoil(boolean isSpoil) {
		this.isSpoil = isSpoil;
	}
	
	public final int getIsSpoiledBy() {
		return isSpoiledBy;
	}
	
	public final void setIsSpoiledBy(int value) {
		isSpoiledBy = value;
	}
	
	private boolean canBeSweeped = true;
	
	public final boolean canBeSweeped() {
		return canBeSweeped;
	}
	
	public final void setCanBeSweeped(final boolean canBeSweeped) {
		this.canBeSweeped = canBeSweeped;
	}
	
	/**
	 * Sets state of the mob to seeded. Paramets needed to be set before.
	 */
	public void setSeeded(Player seeder) {
		if (seedType != 0 && seederObjId == seeder.getObjectId()) {
			setSeeded(seedType, seeder.getLevel());
		}
	}
	
	/**
	 * Sets the seed parameters, but not the seed state
	 *
	 * @param id     - id of the seed
	 * @param seeder - player who is sowind the seed
	 */
	public void setSeeded(int id, Player seeder) {
		if (!seeded) {
			seedType = id;
			seederObjId = seeder.getObjectId();
		}
	}
	
	private void setSeeded(int id, int seederLvl) {
		seeded = true;
		seedType = id;
		int count = 1;
		
		Map<Integer, Skill> skills = getTemplate().getSkills();
		
		if (skills != null) {
			for (int skillId : skills.keySet()) {
				switch (skillId) {
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
		
		int diff = getLevel() - (L2Manor.getInstance().getSeedLevel(seedType) - 5);
		
		// hi-lvl mobs bonus
		if (diff > 0) {
			count += diff;
		}
		
		ArrayList<RewardItem> harvested = new ArrayList<>();
		
		harvested.add(new RewardItem(L2Manor.getInstance().getCropType(seedType), count * Config.RATE_DROP_MANOR));
		
		harvestItems = harvested.toArray(new RewardItem[harvested.size()]);
	}
	
	public int getSeederId() {
		return seederObjId;
	}
	
	public int getSeedType() {
		return seedType;
	}
	
	public boolean isSeeded() {
		return seeded;
	}
	
	/**
	 * Set delay for onKill() call, in ms
	 * Default: 5000 ms
	 */
	public final void setOnKillDelay(int delay) {
		onKillDelay = delay;
	}
	
	/**
	 * Check if the server allows Random Animation.
	 */
	// This is located here because L2Monster and L2FriendlyMob both extend this class. The other non-pc instances extend either NpcInstance or MonsterInstance.
	@Override
	public boolean hasRandomAnimation() {
		return super.hasRandomAnimation() && !(this instanceof GrandBossInstance);
	}
	
	@Override
	public boolean isMob() {
		return true; // This means we use MAX_MONSTER_ANIMATION instead of MAX_NPC_ANIMATION
	}
	
	protected void setCommandChannelTimer(CommandChannelTimer commandChannelTimer) {
		this.commandChannelTimer = commandChannelTimer;
	}
	
	public CommandChannelTimer getCommandChannelTimer() {
		return commandChannelTimer;
	}
	
	public L2CommandChannel getFirstCommandChannelAttacked() {
		return firstCommandChannelAttacked;
	}
	
	public void setFirstCommandChannelAttacked(L2CommandChannel firstCommandChannelAttacked) {
		this.firstCommandChannelAttacked = firstCommandChannelAttacked;
	}
	
	/**
	 * @return the commandChannelLastAttack
	 */
	public long getCommandChannelLastAttack() {
		return commandChannelLastAttack;
	}
	
	/**
	 * @param channelLastAttack the commandChannelLastAttack to set
	 */
	public void setCommandChannelLastAttack(long channelLastAttack) {
		commandChannelLastAttack = channelLastAttack;
	}
	
	private static class CommandChannelTimer implements Runnable {
		private Attackable monster;
		
		public CommandChannelTimer(Attackable monster) {
			this.monster = monster;
		}
		
		/**
		 * @see Runnable#run()
		 */
		@Override
		public void run() {
			if (System.currentTimeMillis() - monster.getCommandChannelLastAttack() > Config.LOOT_RAIDS_PRIVILEGE_INTERVAL * 1000) {
				monster.setCommandChannelTimer(null);
				monster.setFirstCommandChannelAttacked(null);
				monster.setCommandChannelLastAttack(0);
			} else {
				ThreadPoolManager.getInstance().scheduleGeneral(this, 10000); // 10sec
			}
		}
	}
	
	public void returnHome() {
		clearAggroList();
		
		if (!isRaid()) {
			setCurrentHp(getMaxHp());
		}
		
		if (hasAI() && getSpawn() != null) {
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), 0));
		}
	}
	
	/*
	 * Return vitality points decrease (if positive)
	 * or increase (if negative) based on damage.
	 * Maximum for damage = maxHp.
	 */
	public float getVitalityPoints(int damage) {
		// sanity check
		if (damage <= 0) {
			return 0;
		}
		
		float xpRates = Config.RATE_XP;
		final float divider = getTemplate().BaseVitalityDivider * xpRates;
		if (divider == 0) {
			return 0;
		}
		
		// negative value - vitality will be consumed
		return -Math.min(damage, getMaxHp()) / divider;
	}
	
	/*
	 * True if vitality rate for exp and sp should be applied
	 */
	public boolean useVitalityRate() {
		return !(isChampion() && !Config.L2JMOD_CHAMPION_ENABLE_VITALITY);
	}
	
	/**
	 * Return True if the Creature is RaidBoss or his minion.
	 */
	@Override
	public boolean isRaid() {
		return isRaid;
	}
	
	/**
	 * Set this Npc as a Raid instance.<BR><BR>
	 */
	public void setIsRaid(boolean isRaid) {
		this.isRaid = isRaid;
	}
	
	/**
	 * Set this Npc as a Minion instance.<BR><BR>
	 */
	public void setIsRaidMinion(boolean val) {
		isRaid = val;
		isRaidMinion = val;
	}
	
	@Override
	public boolean isRaidMinion() {
		return isRaidMinion;
	}
	
	@Override
	public boolean isMinion() {
		return getLeader() != null;
	}
	
	/**
	 * Return leader of this minion or null.
	 */
	public Attackable getLeader() {
		return null;
	}
	
	public void setChampion(boolean champ) {
		champion = champ;
	}
	
	@Override
	public boolean isChampion() {
		return champion;
	}
	
	public void escape(String message) {
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
		
		if (message != null) {
			broadcastPacket(new CreatureSay(getObjectId(), Say2.ALL_NOT_RECORDED, getName(), message));
		}
	}
	
	private static class EscapeFinalizer implements Runnable {
		private Attackable mob;
		
		EscapeFinalizer(Attackable mob) {
			this.mob = mob;
		}
		
		@Override
		public void run() {
			mob.enableAllSkills();
			mob.setIsCastingNow(false);
			if (!mob.isRaid()) {
				mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());
			}
			mob.setIsInvul(false);
			mob.teleToLocation(mob.getSpawn().getX(), mob.getSpawn().getY(), mob.getSpawn().getZ());
		}
	}
	
	@SuppressWarnings("unused")
	private int getHighestLevelAttacker(final Attackable attackable, Player player) {
		int highestLevel = player.getLevel();
		
		// Check to prevent very high level player to nearly kill mob and let low level player do the last hit.
		if (attackable.getAttackByList() != null && !attackable.getAttackByList().isEmpty()) {
			for (Creature atkChar : attackable.getAttackByList()) {
				if (atkChar != null && atkChar.getLevel() > highestLevel) {
					highestLevel = atkChar.getLevel();
				}
			}
		}
		return highestLevel;
	}
}
