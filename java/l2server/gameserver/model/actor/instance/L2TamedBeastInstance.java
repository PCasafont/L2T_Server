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
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.skills.L2SkillType;
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
public final class L2TamedBeastInstance extends L2FeedableBeastInstance
{
	private int foodSkillId;
	private static final int MAX_DISTANCE_FROM_HOME = 30000;
	private static final int MAX_DISTANCE_FROM_OWNER = 2000;
	private static final int MAX_DURATION = 1200000; // 20 minutes
	private static final int DURATION_CHECK_INTERVAL = 60000; // 1 minute
	private static final int DURATION_INCREASE_INTERVAL = 20000; // 20 secs (gained upon feeding)
	private static final int BUFF_INTERVAL = 5000; // 5 seconds
	private int remainingTime = MAX_DURATION;
	private int homeX, homeY, homeZ;
	private L2PcInstance owner;
	private Future<?> buffTask = null;
	private Future<?> durationCheckTask = null;
	private static boolean isFreyaBeast;
	private List<L2Skill> beastSkills = null;

	public L2TamedBeastInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2TamedBeastInstance);
		setHome(this);
	}

	public L2TamedBeastInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, int foodSkillId, int x, int y, int z)
	{
		super(objectId, template);
		this.isFreyaBeast = false;
		setInstanceType(InstanceType.L2TamedBeastInstance);
		setCurrentHp(getMaxHp());
		setCurrentMp(getMaxMp());
		setOwner(owner);
		setFoodType(foodSkillId);
		setHome(x, y, z);
		this.spawnMe(x, y, z);
	}

	public L2TamedBeastInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, int food, int x, int y, int z, boolean isFreyaBeast)
	{
		super(objectId, template);
		this.isFreyaBeast = isFreyaBeast;
		setInstanceType(InstanceType.L2TamedBeastInstance);
		setCurrentHp(getMaxHp());
		setCurrentMp(getMaxMp());
		setFoodType(food);
		setHome(x, y, z);
		spawnMe(x, y, z);
		setOwner(owner);
		if (isFreyaBeast)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this.owner);
		}
	}

	public void onReceiveFood()
	{
		// Eating food extends the duration by 20secs, to a max of 20minutes
		this.remainingTime = this.remainingTime + DURATION_INCREASE_INTERVAL;
		if (this.remainingTime > MAX_DURATION)
		{
			this.remainingTime = MAX_DURATION;
		}
	}

	public Point3D getHome()
	{
		return new Point3D(this.homeX, this.homeY, this.homeZ);
	}

	public void setHome(int x, int y, int z)
	{
		this.homeX = x;
		this.homeY = y;
		this.homeZ = z;
	}

	public void setHome(L2Character c)
	{
		setHome(c.getX(), c.getY(), c.getZ());
	}

	public int getRemainingTime()
	{
		return this.remainingTime;
	}

	public void setRemainingTime(int duration)
	{
		this.remainingTime = duration;
	}

	public int getFoodType()
	{
		return this.foodSkillId;
	}

	public void setFoodType(int foodItemId)
	{
		if (foodItemId > 0)
		{
			this.foodSkillId = foodItemId;

			// start the duration checks
			// start the buff tasks
			if (this.durationCheckTask != null)
			{
				this.durationCheckTask.cancel(true);
			}
			this.durationCheckTask = ThreadPoolManager.getInstance()
					.scheduleGeneralAtFixedRate(new CheckDuration(this), DURATION_CHECK_INTERVAL,
							DURATION_CHECK_INTERVAL);
		}
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}

		getAI().stopFollow();
		if (this.buffTask != null)
		{
			this.buffTask.cancel(true);
		}
		if (this.durationCheckTask != null)
		{
			this.durationCheckTask.cancel(true);
		}

		// clean up variables
		if (this.owner != null && this.owner.getTrainedBeasts() != null)
		{
			this.owner.getTrainedBeasts().remove(this);
		}
		this.buffTask = null;
		this.durationCheckTask = null;
		this.owner = null;
		this.foodSkillId = 0;
		this.remainingTime = 0;
		return true;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return !this.isFreyaBeast;
	}

	public boolean isFreyaBeast()
	{
		return this.isFreyaBeast;
	}

	public void addBeastSkill(L2Skill skill)
	{
		if (this.beastSkills == null)
		{
			this.beastSkills = new ArrayList<>();
		}
		this.beastSkills.add(skill);
	}

	public void castBeastSkills()
	{
		if (this.owner == null || this.beastSkills == null)
		{
			return;
		}
		int delay = 100;
		for (L2Skill skill : this.beastSkills)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new buffCast(skill), delay);
			delay += 100 + skill.getHitTime();
		}
		ThreadPoolManager.getInstance().scheduleGeneral(new buffCast(null), delay);
	}

	private class buffCast implements Runnable
	{
		private L2Skill skill;

		public buffCast(L2Skill skill)
		{
			this.skill = skill;
		}

		@Override
		public void run()
		{
			if (this.skill == null)
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner);
			}
			else
			{
				sitCastAndFollow(this.skill, owner);
			}
		}
	}

	@Override
	public L2PcInstance getOwner()
	{
		return this.owner;
	}

	@Override
	public void setOwner(L2PcInstance owner)
	{
		if (owner != null)
		{
			this.owner = owner;
			setTitle(owner.getName());
			// broadcast the new title
			setShowSummonAnimation(true);
			broadcastPacket(new NpcInfo(this, owner));

			owner.addTrainedBeast(this);

			// always and automatically follow the owner.
			getAI().startFollow(this.owner, 100);

			if (!this.isFreyaBeast)
			{
				// instead of calculating this value each time, let's get this now and pass it on
				int totalBuffsAvailable = 0;
				for (L2Skill skill : getTemplate().getSkills().values())
				{
					// if the skill is a buff, check if the owner has it already [  owner.getEffect(L2Skill skill) ]
					if (skill.getSkillType() == L2SkillType.BUFF)
					{
						totalBuffsAvailable++;
					}
				}

				// start the buff tasks
				if (this.buffTask != null)
				{
					this.buffTask.cancel(true);
				}
				this.buffTask = ThreadPoolManager.getInstance()
						.scheduleGeneralAtFixedRate(new CheckOwnerBuffs(this, totalBuffsAvailable), BUFF_INTERVAL,
								BUFF_INTERVAL);
			}
		}
		else
		{
			deleteMe(); // despawn if no owner
		}
	}

	public boolean isTooFarFromHome()
	{
		return !this.isInsideRadius(this.homeX, this.homeY, this.homeZ, MAX_DISTANCE_FROM_HOME, true, true);
	}

	@Override
	public void deleteMe()
	{
		if (this.buffTask != null)
		{
			this.buffTask.cancel(true);
		}
		this.durationCheckTask.cancel(true);
		stopHpMpRegeneration();

		// clean up variables
		if (this.owner != null && this.owner.getTrainedBeasts() != null)
		{
			this.owner.getTrainedBeasts().remove(this);
		}
		setTarget(null);
		this.buffTask = null;
		this.durationCheckTask = null;
		this.owner = null;
		this.foodSkillId = 0;
		this.remainingTime = 0;

		// remove the spawn
		super.deleteMe();
	}

	// notification triggered by the owner when the owner is attacked.
	// tamed mobs will heal/recharge or debuff the enemy according to their skills
	public void onOwnerGotAttacked(L2Character attacker)
	{
		// check if the owner is no longer around...if so, despawn
		if (this.owner == null || !this.owner.isOnline())
		{
			deleteMe();
			return;
		}
		// if the owner is too far away, stop anything else and immediately run towards the owner.
		if (!this.owner.isInsideRadius(this, MAX_DISTANCE_FROM_OWNER, true, true))
		{
			getAI().startFollow(this.owner);
			return;
		}
		// if the owner is dead, do nothing...
		if (this.owner.isDead() || this.isFreyaBeast)
		{
			return;
		}

		// if the tamed beast is currently in the middle of casting, let it complete its skill...
		if (isCastingNow())
		{
			return;
		}

		float HPRatio = (float) this.owner.getCurrentHp() / this.owner.getMaxHp();

		// if the owner has a lot of HP, then debuff the enemy with a random debuff among the available skills
		// use of more than one debuff at this moment is acceptable
		if (HPRatio >= 0.8)
		{
			HashMap<Integer, L2Skill> skills = (HashMap<Integer, L2Skill>) getTemplate().getSkills();

			for (L2Skill skill : skills.values())
			{
				// if the skill is a debuff, check if the attacker has it already [  attacker.getEffect(L2Skill skill) ]
				if (skill.isDebuff() && Rnd.get(3) < 1 && attacker != null && attacker.getFirstEffect(skill) != null)
				{
					sitCastAndFollow(skill, attacker);
				}
			}
		}
		// for HP levels between 80% and 50%, do not react to attack events (so that MP can regenerate a bit)
		// for lower HP ranges, heal or recharge the owner with 1 skill use per attack.
		else if (HPRatio < 0.5)
		{
			int chance = 1;
			if (HPRatio < 0.25)
			{
				chance = 2;
			}

			// if the owner has a lot of HP, then debuff the enemy with a random debuff among the available skills
			HashMap<Integer, L2Skill> skills = (HashMap<Integer, L2Skill>) getTemplate().getSkills();

			for (L2Skill skill : skills.values())
			{
				// if the skill is a buff, check if the owner has it already [  owner.getEffect(L2Skill skill) ]
				if (Rnd.get(5) < chance &&
						(skill.getSkillType() == L2SkillType.HEAL || skill.getSkillType() == L2SkillType.BALANCE_LIFE ||
								skill.getSkillType() == L2SkillType.HEAL_PERCENT ||
								skill.getSkillType() == L2SkillType.HEAL_STATIC ||
								skill.getSkillType() == L2SkillType.COMBATPOINTHEAL ||
								skill.getSkillType() == L2SkillType.MANAHEAL ||
								skill.getSkillType() == L2SkillType.MANA_BY_LEVEL ||
								skill.getSkillType() == L2SkillType.MANAHEAL_PERCENT ||
								skill.getSkillType() == L2SkillType.MANARECHARGE))
				{
					sitCastAndFollow(skill, this.owner);
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
	 * @see l2server.gameserver.model.actor.L2Character#doCast(l2server.gameserver.model.L2Skill)
	 */
	protected void sitCastAndFollow(L2Skill skill, L2Character target)
	{
		stopMove(null);
		broadcastPacket(new StopMove(this));
		getAI().setIntention(AI_INTENTION_IDLE);

		setTarget(target);
		doCast(skill);
		getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this.owner);
	}

	private static class CheckDuration implements Runnable
	{
		private L2TamedBeastInstance tamedBeast;

		CheckDuration(L2TamedBeastInstance tamedBeast)
		{
			this.tamedBeast = tamedBeast;
		}

		@Override
		public void run()
		{
			int foodTypeSkillId = this.tamedBeast.getFoodType();
			L2PcInstance owner = this.tamedBeast.getOwner();

			L2ItemInstance item = null;
			if (isFreyaBeast)
			{
				item = owner.getInventory().getItemByItemId(foodTypeSkillId);
				if (item != null && item.getCount() >= 1)
				{
					owner.destroyItem("BeastMob", item, 1, this.tamedBeast, true);
					this.tamedBeast.broadcastPacket(new SocialAction(this.tamedBeast.getObjectId(), 3));
				}
				else
				{
					this.tamedBeast.deleteMe();
				}
			}
			else
			{
				this.tamedBeast.setRemainingTime(this.tamedBeast.getRemainingTime() - DURATION_CHECK_INTERVAL);
				// I tried to avoid this as much as possible...but it seems I can't avoid hardcoding
				// ids further, except by carrying an additional variable just for these two lines...
				// Find which food item needs to be consumed.
				if (foodTypeSkillId == 2188)
				{
					item = owner.getInventory().getItemByItemId(6643);
				}
				else if (foodTypeSkillId == 2189)
				{
					item = owner.getInventory().getItemByItemId(6644);
				}

				// if the owner has enough food, call the item handler (use the food and triffer all necessary actions)
				if (item != null && item.getCount() >= 1)
				{
					L2Object oldTarget = owner.getTarget();
					owner.setTarget(this.tamedBeast);
					L2Object[] targets = {this.tamedBeast};

					// emulate a call to the owner using food, but bypass all checks for range, etc
					// this also causes a call to the AI tasks handling feeding, which may call onReceiveFood as required.
					owner.callSkill(SkillTable.getInstance().getInfo(foodTypeSkillId, 1), targets);
					owner.setTarget(oldTarget);
				}
				else
				{
					// if the owner has no food, the beast immediately despawns, except when it was only
					// newly spawned.  Newly spawned beasts can last up to 5 minutes
					if (this.tamedBeast.getRemainingTime() < MAX_DURATION - 300000)
					{
						this.tamedBeast.setRemainingTime(-1);
					}
				}
				/* There are too many conflicting reports about whether distance from home should
                 * be taken into consideration.  Disabled for now.
				 *
				if (this.tamedBeast.isTooFarFromHome())
					this.tamedBeast.setRemainingTime(-1);
				 */

				if (this.tamedBeast.getRemainingTime() <= 0)
				{
					this.tamedBeast.deleteMe();
				}
			}
		}
	}

	private class CheckOwnerBuffs implements Runnable
	{
		private L2TamedBeastInstance tamedBeast;
		private int numBuffs;

		CheckOwnerBuffs(L2TamedBeastInstance tamedBeast, int numBuffs)
		{
			this.tamedBeast = tamedBeast;
			this.numBuffs = numBuffs;
		}

		@Override
		public void run()
		{
			L2PcInstance owner = this.tamedBeast.getOwner();

			// check if the owner is no longer around...if so, despawn
			if (owner == null || !owner.isOnline())
			{
				deleteMe();
				return;
			}
			// if the owner is too far away, stop anything else and immediately run towards the owner.
			if (!isInsideRadius(owner, MAX_DISTANCE_FROM_OWNER, true, true))
			{
				getAI().startFollow(owner);
				return;
			}
			// if the owner is dead, do nothing...
			if (owner.isDead())
			{
				return;
			}
			// if the tamed beast is currently casting a spell, do not interfere (do not attempt to cast anything new yet).
			if (isCastingNow())
			{
				return;
			}

			int totalBuffsOnOwner = 0;
			int i = 0;
			int rand = Rnd.get(this.numBuffs);
			L2Skill buffToGive = null;

			// get this npc's skills:  getSkills()
			HashMap<Integer, L2Skill> skills = (HashMap<Integer, L2Skill>) this.tamedBeast.getTemplate().getSkills();

			for (L2Skill skill : skills.values())
			{
				// if the skill is a buff, check if the owner has it already [  owner.getEffect(L2Skill skill) ]
				if (skill.getSkillType() == L2SkillType.BUFF)
				{
					if (i++ == rand)
					{
						buffToGive = skill;
					}
					if (owner.getFirstEffect(skill) != null)
					{
						totalBuffsOnOwner++;
					}
				}
			}
			// if the owner has less than 60% of this beast's available buff, cast a random buff
			if (this.numBuffs * 2 / 3 > totalBuffsOnOwner)
			{
				this.tamedBeast.sitCastAndFollow(buffToGive, owner);
			}
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this.tamedBeast.getOwner());
		}
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (player == null || !canTarget(player))
		{
			return;
		}

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);

			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if (interact)
		{
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100)
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			else
			{
				// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
}
