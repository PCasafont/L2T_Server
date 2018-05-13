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

import l2server.gameserver.model.InstanceType;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.gameserver.templates.skills.SkillType;
import l2server.util.Point3D;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import static l2server.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

// While a tamed beast behaves a lot like a pet (ingame) and does have
// an owner, in all other aspects, it acts like a mob.
// In addition, it can be fed in order to increase its duration.
// This class handles the running tasks, AI, and feed of the mob.
// The (mostly optional) AI on feeding the spawn is handled by the datapack ai script
public final class TamedBeastInstance extends FeedableBeastInstance {
	private int foodSkillId;
	private static final int MAX_DISTANCE_FROM_HOME = 30000;
	private static final int MAX_DISTANCE_FROM_OWNER = 2000;
	private static final int MAX_DURATION = 1200000; // 20 minutes
	private static final int DURATION_CHECK_INTERVAL = 60000; // 1 minute
	private static final int DURATION_INCREASE_INTERVAL = 20000; // 20 secs (gained upon feeding)
	private static final int BUFF_INTERVAL = 5000; // 5 seconds
	private int remainingTime = MAX_DURATION;
	private int homeX, homeY, homeZ;
	private Player owner;
	private Future<?> buffTask = null;
	private Future<?> durationCheckTask = null;
	private static boolean isFreyaBeast;
	private List<Skill> beastSkills = null;
	
	public TamedBeastInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2TamedBeastInstance);
		setHome(this);
	}
	
	public TamedBeastInstance(int objectId, NpcTemplate template, Player owner, int foodSkillId, int x, int y, int z) {
		super(objectId, template);
		isFreyaBeast = false;
		setInstanceType(InstanceType.L2TamedBeastInstance);
		setCurrentHp(getMaxHp());
		setCurrentMp(getMaxMp());
		setOwner(owner);
		setFoodType(foodSkillId);
		setHome(x, y, z);
		this.spawnMe(x, y, z);
	}
	
	public TamedBeastInstance(int objectId, NpcTemplate template, Player owner, int food, int x, int y, int z, boolean isFreyaBeast) {
		super(objectId, template);
		this.isFreyaBeast = isFreyaBeast;
		setInstanceType(InstanceType.L2TamedBeastInstance);
		setCurrentHp(getMaxHp());
		setCurrentMp(getMaxMp());
		setFoodType(food);
		setHome(x, y, z);
		spawnMe(x, y, z);
		setOwner(owner);
		if (isFreyaBeast) {
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner);
		}
	}
	
	public void onReceiveFood() {
		// Eating food extends the duration by 20secs, to a max of 20minutes
		remainingTime = remainingTime + DURATION_INCREASE_INTERVAL;
		if (remainingTime > MAX_DURATION) {
			remainingTime = MAX_DURATION;
		}
	}
	
	public Point3D getHome() {
		return new Point3D(homeX, homeY, homeZ);
	}
	
	public void setHome(int x, int y, int z) {
		homeX = x;
		homeY = y;
		homeZ = z;
	}
	
	public void setHome(Creature c) {
		setHome(c.getX(), c.getY(), c.getZ());
	}
	
	public int getRemainingTime() {
		return remainingTime;
	}
	
	public void setRemainingTime(int duration) {
		remainingTime = duration;
	}
	
	public int getFoodType() {
		return foodSkillId;
	}
	
	public void setFoodType(int foodItemId) {
		if (foodItemId > 0) {
			foodSkillId = foodItemId;
			
			// start the duration checks
			// start the buff tasks
			if (durationCheckTask != null) {
				durationCheckTask.cancel(true);
			}
			durationCheckTask = ThreadPoolManager.getInstance()
					.scheduleGeneralAtFixedRate(new CheckDuration(this), DURATION_CHECK_INTERVAL, DURATION_CHECK_INTERVAL);
		}
	}
	
	@Override
	public boolean doDie(Creature killer) {
		if (!super.doDie(killer)) {
			return false;
		}
		
		getAI().stopFollow();
		if (buffTask != null) {
			buffTask.cancel(true);
		}
		if (durationCheckTask != null) {
			durationCheckTask.cancel(true);
		}
		
		// clean up variables
		if (owner != null && owner.getTrainedBeasts() != null) {
			owner.getTrainedBeasts().remove(this);
		}
		buffTask = null;
		durationCheckTask = null;
		owner = null;
		foodSkillId = 0;
		remainingTime = 0;
		return true;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker) {
		return !isFreyaBeast;
	}
	
	public boolean isFreyaBeast() {
		return isFreyaBeast;
	}
	
	public void addBeastSkill(Skill skill) {
		if (beastSkills == null) {
			beastSkills = new ArrayList<>();
		}
		beastSkills.add(skill);
	}
	
	public void castBeastSkills() {
		if (owner == null || beastSkills == null) {
			return;
		}
		int delay = 100;
		for (Skill skill : beastSkills) {
			ThreadPoolManager.getInstance().scheduleGeneral(new buffCast(skill), delay);
			delay += 100 + skill.getHitTime();
		}
		ThreadPoolManager.getInstance().scheduleGeneral(new buffCast(null), delay);
	}
	
	private class buffCast implements Runnable {
		private Skill skill;
		
		public buffCast(Skill skill) {
			this.skill = skill;
		}
		
		@Override
		public void run() {
			if (skill == null) {
				getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner);
			} else {
				sitCastAndFollow(skill, owner);
			}
		}
	}
	
	@Override
	public Player getOwner() {
		return owner;
	}
	
	@Override
	public void setOwner(Player owner) {
		if (owner != null) {
			this.owner = owner;
			setTitle(owner.getName());
			// broadcast the new title
			setShowSummonAnimation(true);
			broadcastPacket(new NpcInfo(this, owner));
			
			owner.addTrainedBeast(this);
			
			// always and automatically follow the owner.
			getAI().startFollow(owner, 100);
			
			if (!isFreyaBeast) {
				// instead of calculating this value each time, let's get this now and pass it on
				int totalBuffsAvailable = 0;
				for (Skill skill : getTemplate().getSkills().values()) {
					// if the skill is a buff, check if the owner has it already [  owner.getEffect(Skill skill) ]
					if (skill.getSkillType() == SkillType.BUFF) {
						totalBuffsAvailable++;
					}
				}
				
				// start the buff tasks
				if (buffTask != null) {
					buffTask.cancel(true);
				}
				buffTask = ThreadPoolManager.getInstance()
						.scheduleGeneralAtFixedRate(new CheckOwnerBuffs(this, totalBuffsAvailable), BUFF_INTERVAL, BUFF_INTERVAL);
			}
		} else {
			deleteMe(); // despawn if no owner
		}
	}
	
	public boolean isTooFarFromHome() {
		return !this.isInsideRadius(homeX, homeY, homeZ, MAX_DISTANCE_FROM_HOME, true, true);
	}
	
	@Override
	public void deleteMe() {
		if (buffTask != null) {
			buffTask.cancel(true);
		}
		durationCheckTask.cancel(true);
		stopHpMpRegeneration();
		
		// clean up variables
		if (owner != null && owner.getTrainedBeasts() != null) {
			owner.getTrainedBeasts().remove(this);
		}
		setTarget(null);
		buffTask = null;
		durationCheckTask = null;
		owner = null;
		foodSkillId = 0;
		remainingTime = 0;
		
		// remove the spawn
		super.deleteMe();
	}
	
	// notification triggered by the owner when the owner is attacked.
	// tamed mobs will heal/recharge or debuff the enemy according to their skills
	public void onOwnerGotAttacked(Creature attacker) {
		// check if the owner is no longer around...if so, despawn
		if (owner == null || !owner.isOnline()) {
			deleteMe();
			return;
		}
		// if the owner is too far away, stop anything else and immediately run towards the owner.
		if (!owner.isInsideRadius(this, MAX_DISTANCE_FROM_OWNER, true, true)) {
			getAI().startFollow(owner);
			return;
		}
		// if the owner is dead, do nothing...
		if (owner.isDead() || isFreyaBeast) {
			return;
		}
		
		// if the tamed beast is currently in the middle of casting, let it complete its skill...
		if (isCastingNow()) {
			return;
		}
		
		float HPRatio = (float) owner.getCurrentHp() / owner.getMaxHp();
		
		// if the owner has a lot of HP, then debuff the enemy with a random debuff among the available skills
		// use of more than one debuff at this moment is acceptable
		if (HPRatio >= 0.8) {
			HashMap<Integer, Skill> skills = (HashMap<Integer, Skill>) getTemplate().getSkills();
			
			for (Skill skill : skills.values()) {
				// if the skill is a debuff, check if the attacker has it already [  attacker.getEffect(Skill skill) ]
				if (skill.isDebuff() && Rnd.get(3) < 1 && attacker != null && attacker.getFirstEffect(skill) != null) {
					sitCastAndFollow(skill, attacker);
				}
			}
		}
		// for HP levels between 80% and 50%, do not react to attack events (so that MP can regenerate a bit)
		// for lower HP ranges, heal or recharge the owner with 1 skill use per attack.
		else if (HPRatio < 0.5) {
			int chance = 1;
			if (HPRatio < 0.25) {
				chance = 2;
			}
			
			// if the owner has a lot of HP, then debuff the enemy with a random debuff among the available skills
			HashMap<Integer, Skill> skills = (HashMap<Integer, Skill>) getTemplate().getSkills();
			
			for (Skill skill : skills.values()) {
				// if the skill is a buff, check if the owner has it already [  owner.getEffect(Skill skill) ]
				if (Rnd.get(5) < chance && (skill.getSkillType() == SkillType.HEAL || skill.getSkillType() == SkillType.BALANCE_LIFE ||
						skill.getSkillType() == SkillType.HEAL_PERCENT || skill.getSkillType() == SkillType.HEAL_STATIC ||
						skill.getSkillType() == SkillType.COMBATPOINTHEAL || skill.getSkillType() == SkillType.MANAHEAL ||
						skill.getSkillType() == SkillType.MANA_BY_LEVEL || skill.getSkillType() == SkillType.MANAHEAL_PERCENT ||
						skill.getSkillType() == SkillType.MANARECHARGE)) {
					sitCastAndFollow(skill, owner);
					return;
				}
			}
		}
	}
	
	/**
	 * Prepare and cast a skill:
	 * First smoothly prepare the beast for casting, by abandoning other actions
	 * Next, call super.doCast(skill) in order to actually cast the spell
	 * Finally, return to auto-following the owner.
	 *
	 * @see Creature#doCast(Skill)
	 */
	protected void sitCastAndFollow(Skill skill, Creature target) {
		stopMove(null);
		broadcastPacket(new StopMove(this));
		getAI().setIntention(AI_INTENTION_IDLE);
		
		setTarget(target);
		doCast(skill);
		getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner);
	}
	
	private static class CheckDuration implements Runnable {
		private TamedBeastInstance tamedBeast;
		
		CheckDuration(TamedBeastInstance tamedBeast) {
			this.tamedBeast = tamedBeast;
		}
		
		@Override
		public void run() {
			int foodTypeSkillId = tamedBeast.getFoodType();
			Player owner = tamedBeast.getOwner();
			
			Item item = null;
			if (isFreyaBeast) {
				item = owner.getInventory().getItemByItemId(foodTypeSkillId);
				if (item != null && item.getCount() >= 1) {
					owner.destroyItem("BeastMob", item, 1, tamedBeast, true);
					tamedBeast.broadcastPacket(new SocialAction(tamedBeast.getObjectId(), 3));
				} else {
					tamedBeast.deleteMe();
				}
			} else {
				tamedBeast.setRemainingTime(tamedBeast.getRemainingTime() - DURATION_CHECK_INTERVAL);
				// I tried to avoid this as much as possible...but it seems I can't avoid hardcoding
				// ids further, except by carrying an additional variable just for these two lines...
				// Find which food item needs to be consumed.
				if (foodTypeSkillId == 2188) {
					item = owner.getInventory().getItemByItemId(6643);
				} else if (foodTypeSkillId == 2189) {
					item = owner.getInventory().getItemByItemId(6644);
				}
				
				// if the owner has enough food, call the item handler (use the food and triffer all necessary actions)
				if (item != null && item.getCount() >= 1) {
					WorldObject oldTarget = owner.getTarget();
					owner.setTarget(tamedBeast);
					WorldObject[] targets = {tamedBeast};
					
					// emulate a call to the owner using food, but bypass all checks for range, etc
					// this also causes a call to the AI tasks handling feeding, which may call onReceiveFood as required.
					owner.callSkill(SkillTable.getInstance().getInfo(foodTypeSkillId, 1), targets);
					owner.setTarget(oldTarget);
				} else {
					// if the owner has no food, the beast immediately despawns, except when it was only
					// newly spawned.  Newly spawned beasts can last up to 5 minutes
					if (tamedBeast.getRemainingTime() < MAX_DURATION - 300000) {
						tamedBeast.setRemainingTime(-1);
					}
				}
				/* There are too many conflicting reports about whether distance from home should
                 * be taken into consideration.  Disabled for now.
				 *
				if (tamedBeast.isTooFarFromHome())
					tamedBeast.setRemainingTime(-1);
				 */
				
				if (tamedBeast.getRemainingTime() <= 0) {
					tamedBeast.deleteMe();
				}
			}
		}
	}
	
	private class CheckOwnerBuffs implements Runnable {
		private TamedBeastInstance tamedBeast;
		private int numBuffs;
		
		CheckOwnerBuffs(TamedBeastInstance tamedBeast, int numBuffs) {
			this.tamedBeast = tamedBeast;
			this.numBuffs = numBuffs;
		}
		
		@Override
		public void run() {
			Player owner = tamedBeast.getOwner();
			
			// check if the owner is no longer around...if so, despawn
			if (owner == null || !owner.isOnline()) {
				deleteMe();
				return;
			}
			// if the owner is too far away, stop anything else and immediately run towards the owner.
			if (!isInsideRadius(owner, MAX_DISTANCE_FROM_OWNER, true, true)) {
				getAI().startFollow(owner);
				return;
			}
			// if the owner is dead, do nothing...
			if (owner.isDead()) {
				return;
			}
			// if the tamed beast is currently casting a spell, do not interfere (do not attempt to cast anything new yet).
			if (isCastingNow()) {
				return;
			}
			
			int totalBuffsOnOwner = 0;
			int i = 0;
			int rand = Rnd.get(numBuffs);
			Skill buffToGive = null;
			
			// get this npc's skills:  getSkills()
			HashMap<Integer, Skill> skills = (HashMap<Integer, Skill>) tamedBeast.getTemplate().getSkills();
			
			for (Skill skill : skills.values()) {
				// if the skill is a buff, check if the owner has it already [  owner.getEffect(Skill skill) ]
				if (skill.getSkillType() == SkillType.BUFF) {
					if (i++ == rand) {
						buffToGive = skill;
					}
					if (owner.getFirstEffect(skill) != null) {
						totalBuffsOnOwner++;
					}
				}
			}
			// if the owner has less than 60% of this beast's available buff, cast a random buff
			if (numBuffs * 2 / 3 > totalBuffsOnOwner) {
				tamedBeast.sitCastAndFollow(buffToGive, owner);
			}
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, tamedBeast.getOwner());
		}
	}
	
	@Override
	public void onAction(Player player, boolean interact) {
		if (player == null || !canTarget(player)) {
			return;
		}
		
		// Check if the Player already target the NpcInstance
		if (this != player.getTarget()) {
			// Set the target of the Player player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the Player player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			
			// Send a Server->Client packet StatusUpdate of the NpcInstance to the Player to update its HP bar
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
			
			// Send a Server->Client packet ValidateLocation to correct the NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		} else if (interact) {
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100) {
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			} else {
				// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
}
