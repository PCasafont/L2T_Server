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

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.NewbieHelperAI;
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.handler.BypassHandler;
import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.instancemanager.*;
import l2server.gameserver.instancemanager.MainTownManager.MainTownInfo;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.instance.*;
import l2server.gameserver.model.actor.knownlist.NpcKnownList;
import l2server.gameserver.model.actor.stat.NpcStat;
import l2server.gameserver.model.actor.status.NpcStatus;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.entity.Fort;
import l2server.gameserver.model.olympiad.Olympiad;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.zone.type.TownZone;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.taskmanager.DecayTaskManager;
import l2server.gameserver.templates.skills.AISkillType;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.gameserver.templates.chars.NpcTemplate.AIType;
import l2server.gameserver.templates.item.ItemTemplate;
import l2server.gameserver.templates.item.WeaponTemplate;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.util.Broadcast;
import l2server.util.Rnd;
import l2server.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import static l2server.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

/**
 * This class represents a Non-Player-Character in the world. It can be a monster or a friendly character.
 * It also uses a template to fetch some static values. The templates are hardcoded in the client, so we can rely on them.<BR><BR>
 * <p>
 * Creature :<BR><BR>
 * <li>Attackable</li>
 * <li>L2BoxInstance</li>
 * <li>L2FolkInstance</li>
 *
 * @version $Revision: 1.32.2.7.2.24 $ $Date: 2005/04/11 10:06:09 $
 */
public class Npc extends Creature {
	
	protected static Logger log = LoggerFactory.getLogger(Npc.class.getName());
	
	/**
	 * The interaction distance of the NpcInstance(is used as offset in MovetoLocation method)
	 */
	public static final int DEFAULT_INTERACTION_DISTANCE = 150;
	
	/**
	 * The L2Spawn object that manage this NpcInstance
	 */
	private L2Spawn spawn;
	
	/**
	 * The flag to specify if this NpcInstance is busy
	 */
	private boolean isBusy = false;
	
	/**
	 * The busy message for this NpcInstance
	 */
	private String busyMessage = "";
	
	/**
	 * True if endDecayTask has already been called
	 */
	volatile boolean isDecayed = false;
	
	/**
	 * The castle index in the array of L2Castle this NpcInstance belongs to
	 */
	private int castleIndex = -2;
	
	/**
	 * The fortress index in the array of L2Fort this NpcInstance belongs to
	 */
	private int fortIndex = -2;
	
	private boolean isInTown = false;
	
	/**
	 * True if this Npc is autoattackable
	 **/
	private boolean isAutoAttackable = false;
	
	/**
	 * Time of last social packet broadcast
	 */
	private long lastSocialBroadcast = 0;
	
	/**
	 * Minimum interval between social packets
	 */
	private int minimalSocialInterval = 4000;
	
	private boolean isRndWalk = false;
	
	protected RandomAnimationTask rAniTask = null;
	private int currentLHandId; // normally this shouldn't change from the template, but there exist exceptions
	private int currentRHandId; // normally this shouldn't change from the template, but there exist exceptions
	private int currentEnchant; // normally this shouldn't change from the template, but there exist exceptions
	private double currentCollisionHeight; // used for npc grow effect skills
	private double currentCollisionRadius; // used for npc grow effect skills
	
	public boolean soulshotcharged = false;
	public boolean spiritshotcharged = false;
	private int soulshotamount = 0;
	private int spiritshotamount = 0;
	public boolean ssrecharged = true;
	public boolean spsrecharged = true;
	protected boolean isHideName = false;
	private int displayEffect = 0;
	
	private boolean isInvisible = false;
	private boolean isLethalInmune = false;
	private boolean isDebuffInmune = false;
	
	private Player owner = null;
	
	/**
	 * The Polymorph object that manage this NpcInstance's morph to a PcInstance
	 */
	private Player clonedPlayer;
	
	//AI Recall
	public int getSoulShot() {
		L2NpcAIData AI = getTemplate().getAIData();
		return AI.getSoulShot();
	}
	
	public int getSpiritShot() {
		L2NpcAIData AI = getTemplate().getAIData();
		return AI.getSpiritShot();
	}
	
	public int getSoulShotChance() {
		L2NpcAIData AI = getTemplate().getAIData();
		return AI.getSoulShotChance();
	}
	
	public int getSpiritShotChance() {
		L2NpcAIData AI = getTemplate().getAIData();
		return AI.getSpiritShotChance();
	}
	
	public boolean useSoulShot() {
		if (soulshotcharged) {
			return true;
		}
		if (ssrecharged) {
			soulshotamount = getSoulShot();
			ssrecharged = false;
		} else if (soulshotamount > 0) {
			if (Rnd.get(100) <= getSoulShotChance()) {
				soulshotamount = soulshotamount - 1;
				Broadcast.toSelfAndKnownPlayersInRadius(this, new MagicSkillUse(this, this, 2154, 1, 0, 0, 0), 360000);
				soulshotcharged = true;
			}
		} else {
			return false;
		}
		
		return soulshotcharged;
	}
	
	public boolean useSpiritShot() {
		
		if (spiritshotcharged) {
			return true;
		} else {
			//spiritshotcharged = false;
			if (spsrecharged) {
				spiritshotamount = getSpiritShot();
				spsrecharged = false;
			} else if (spiritshotamount > 0) {
				if (Rnd.get(100) <= getSpiritShotChance()) {
					spiritshotamount = spiritshotamount - 1;
					Broadcast.toSelfAndKnownPlayersInRadius(this, new MagicSkillUse(this, this, 2061, 1, 0, 0, 0), 360000);
					spiritshotcharged = true;
				}
			} else {
				return false;
			}
		}
		
		return spiritshotcharged;
	}
	
	public int getEnemyRange() {
		L2NpcAIData AI = getTemplate().getAIData();
		return AI.getEnemyRange();
	}
	
	public String getEnemyClan() {
		L2NpcAIData AI = getTemplate().getAIData();
		return AI.getEnemyClan();
	}
	
	public int getClanRange() {
		L2NpcAIData AI = getTemplate().getAIData();
		return AI.getClanRange();
	}
	
	public String getClan() {
		if (isChampion()) {
			return null;
		}
		
		L2NpcAIData AI = getTemplate().getAIData();
		return AI.getClan();
	}
	
	// GET THE PRIMARY ATTACK
	public int getPrimaryAttack() {
		L2NpcAIData AI = getTemplate().getAIData();
		return AI.getPrimaryAttack();
	}
	
	public int getSkillChance() {
		L2NpcAIData AI = getTemplate().getAIData();
		return AI.getSkillChance();
	}
	
	public boolean canMove() {
		L2NpcAIData AI = getTemplate().getAIData();
		return AI.canMove();
	}
	
	public int getIsChaos() {
		L2NpcAIData AI = getTemplate().getAIData();
		return AI.getIsChaos();
	}
	
	public int getCanDodge() {
		L2NpcAIData AI = getTemplate().getAIData();
		return AI.getDodge();
	}
	
	public int getSSkillChance() {
		L2NpcAIData AI = getTemplate().getAIData();
		return AI.getShortRangeChance();
	}
	
	public int getLSkillChance() {
		L2NpcAIData AI = getTemplate().getAIData();
		return AI.getLongRangeChance();
	}
	
	public int getSwitchRangeChance() {
		L2NpcAIData AI = getTemplate().getAIData();
		return AI.getSwitchRangeChance();
	}
	
	public boolean hasLSkill() {
		L2NpcAIData AI = getTemplate().getAIData();
		
		return AI.getLongRangeSkill() != 0;
	}
	
	public boolean hasSSkill() {
		L2NpcAIData AI = getTemplate().getAIData();
		
		return AI.getShortRangeSkill() != 0;
	}
	
	public ArrayList<Skill> getLrangeSkill() {
		ArrayList<Skill> skilldata = new ArrayList<>();
		boolean hasLrange = false;
		L2NpcAIData AI = getTemplate().getAIData();
		
		if (AI == null || AI.getLongRangeSkill() == 0) {
			return null;
		}
		
		switch (AI.getLongRangeSkill()) {
			case -1: {
				Skill[] skills = null;
				skills = getAllSkills();
				if (skills != null) {
					for (Skill sk : skills) {
						if (sk == null || sk.isPassive() || sk.getTargetType() == SkillTargetType.TARGET_SELF) {
							continue;
						}
						
						if (sk.getCastRange() >= 200) {
							skilldata.add(sk);
							hasLrange = true;
						}
					}
				}
				break;
			}
			case 1: {
				if (getTemplate().aiSkills[AISkillType.AIST_UNIVERSAL] != null) {
					for (Skill sk : getTemplate().aiSkills[AISkillType.AIST_UNIVERSAL]) {
						if (sk.getCastRange() >= 200) {
							skilldata.add(sk);
							hasLrange = true;
						}
					}
				}
				break;
			}
			default: {
				for (Skill sk : getAllSkills()) {
					if (sk.getId() == AI.getLongRangeSkill()) {
						skilldata.add(sk);
						hasLrange = true;
					}
				}
			}
		}
		
		return hasLrange ? skilldata : null;
	}
	
	public ArrayList<Skill> getSrangeSkill() {
		ArrayList<Skill> skilldata = new ArrayList<>();
		boolean hasSrange = false;
		L2NpcAIData AI = getTemplate().getAIData();
		
		if (AI == null || AI.getShortRangeSkill() == 0) {
			return null;
		}
		
		switch (AI.getShortRangeSkill()) {
			case -1: {
				Skill[] skills = null;
				skills = getAllSkills();
				if (skills != null) {
					for (Skill sk : skills) {
						if (sk == null || sk.isPassive() || sk.getTargetType() == SkillTargetType.TARGET_SELF) {
							continue;
						}
						
						if (sk.getCastRange() <= 200) {
							skilldata.add(sk);
							hasSrange = true;
						}
					}
				}
				break;
			}
			case 1: {
				if (getTemplate().aiSkills[AISkillType.AIST_UNIVERSAL] != null) {
					for (Skill sk : getTemplate().aiSkills[AISkillType.AIST_UNIVERSAL]) {
						if (sk.getCastRange() <= 200) {
							skilldata.add(sk);
							hasSrange = true;
						}
					}
				}
				break;
			}
			default: {
				for (Skill sk : getAllSkills()) {
					if (sk.getId() == AI.getShortRangeSkill()) {
						skilldata.add(sk);
						hasSrange = true;
					}
				}
			}
		}
		
		return hasSrange ? skilldata : null;
	}
	
	/**
	 * Task launching the function onRandomAnimation()
	 */
	protected class RandomAnimationTask implements Runnable {
		private boolean second;
		
		public RandomAnimationTask(boolean second) {
			this.second = second;
		}
		
		@Override
		public void run() {
			try {
				if (this != rAniTask) {
					return; // Shouldn't happen, but who knows... just to make sure every active npc has only one timer.
				}
				if (isMob()) {
					// Cancel further animation timers until intention is changed to ACTIVE again.
					if (getAI().getIntention() != AI_INTENTION_ACTIVE) {
						return;
					}
				} else {
					if (!isInActiveRegion()) // NPCs in inactive region don't run this task
					{
						return;
					}
				}
				
				if (!(isDead() || isStunned() || isSleeping() || isParalyzed())) {
					onRandomAnimation(second ? 3 : 2);
				}
				
				startRandomAnimationTimer();
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
	
	/**
	 * Send a packet SocialAction to all Player in the KnownPlayers of the NpcInstance and create a new RandomAnimation Task.<BR><BR>
	 */
	public void onRandomAnimation(int animationId) {
		// Send a packet SocialAction to all Player in the KnownPlayers of the NpcInstance
		long now = System.currentTimeMillis();
		if (now - lastSocialBroadcast > minimalSocialInterval) {
			lastSocialBroadcast = now;
			broadcastPacket(new SocialAction(getObjectId(), animationId));
		}
	}
	
	/**
	 * Create a RandomAnimation Task that will be launched after the calculated delay.<BR><BR>
	 */
	public void startRandomAnimationTimer() {
		if (!hasRandomAnimation()) {
			return;
		}
		
		boolean particular = getTemplate().getAIData().getMinSocial(false) >= 0 || getTemplate().getAIData().getMinSocial(true) >= 0;
		boolean second = false;
		if (particular) {
			if (getTemplate().getAIData().getMinSocial(true) != 0 && (getTemplate().getAIData().getMinSocial(false) == 0 ||
					Rnd.get(getTemplate().getAIData().getMinSocial(false) + getTemplate().getAIData().getMinSocial(true)) >
							getTemplate().getAIData().getMinSocial(true))) {
				second = true;
			}
		} else {
			second = Rnd.get(1) == 0;
		}
		
		int minWait = particular ? getTemplate().getAIData().getMinSocial(second) : isMob() ? Config.MIN_MONSTER_ANIMATION : Config.MIN_NPC_ANIMATION;
		int maxWait = particular ? getTemplate().getAIData().getMaxSocial(second) : isMob() ? Config.MAX_MONSTER_ANIMATION : Config.MAX_NPC_ANIMATION;
		
		// Calculate the delay before the next animation
		int interval = Rnd.get(minWait, maxWait) * 1000;
		
		// Create a RandomAnimation Task that will be launched after the calculated delay
		rAniTask = new RandomAnimationTask(second);
		ThreadPoolManager.getInstance().scheduleGeneral(rAniTask, interval);
	}
	
	/**
	 * Check if the server allows Random Animation.<BR><BR>
	 */
	public boolean hasRandomAnimation() {
		return Config.MAX_NPC_ANIMATION > 0 && !getAiType().equals(AIType.CORPSE) &&
				(getTemplate().getAIData().getMaxSocial(false) != 0 || getTemplate().getAIData().getMaxSocial(true) != 0);
	}
	
	/**
	 * Constructor of NpcInstance (use Creature constructor).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the Creature constructor to set the template of the Creature (copy skills from template to object and link calculators to NPC_STD_CALCULATOR)  </li>
	 * <li>Set the name of the Creature</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The NpcTemplate to apply to the NPC
	 */
	public Npc(int objectId, NpcTemplate template) {
		// Call the Creature constructor to set the template of the Creature, copy skills from template to object
		// and link calculators to NPC_STD_CALCULATOR
		super(objectId, template);
		setInstanceType(InstanceType.L2Npc);
		initCharStatusUpdateValues();
		
		// initialize the "current" equipment
		currentLHandId = getTemplate().LHand;
		currentRHandId = getTemplate().RHand;
		currentEnchant = Config.ENABLE_RANDOM_ENCHANT_EFFECT ? Rnd.get(4, 21) : getTemplate().EnchantEffect;
		// initialize the "current" collisions
		currentCollisionHeight = getTemplate().getFCollisionHeight();
		currentCollisionRadius = getTemplate().getFCollisionRadius();
		isRndWalk = getTemplate().RandomWalk;
		
		if (template == null) {
			log.error("No template for Npc. Please check your datapack is setup correctly.");
			return;
		}
		
		// Set the name of the Creature
		setName(template.Name);
	}
	
	@Override
	public NpcKnownList getKnownList() {
		return (NpcKnownList) super.getKnownList();
	}
	
	@Override
	public NpcKnownList initialKnownList() {
		return new NpcKnownList(this);
	}
	
	@Override
	public NpcStat getStat() {
		return (NpcStat) super.getStat();
	}
	
	@Override
	public void initCharStat() {
		setStat(new NpcStat(this));
	}
	
	@Override
	public NpcStatus getStatus() {
		return (NpcStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus() {
		setStatus(new NpcStatus(this));
	}
	
	/**
	 * Return the NpcTemplate of the NpcInstance.
	 */
	@Override
	public final NpcTemplate getTemplate() {
		return (NpcTemplate) super.getTemplate();
	}
	
	/**
	 * Return the generic Identifier of this NpcInstance contained in the NpcTemplate.<BR><BR>
	 */
	public int getNpcId() {
		return getTemplate().NpcId;
	}
	
	@Override
	public boolean isAttackable() {
		return Config.ALT_ATTACKABLE_NPCS;
	}
	
	/**
	 * Return the faction Identifier of this NpcInstance contained in the NpcTemplate.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * If a NPC belows to a Faction, other NPC of the faction inside the Faction range will help it if it's attacked<BR><BR>
	 */
	//@Deprecated
	public final String getFactionId() {
		return getClan();
	}
	
	/**
	 * Return the Level of this NpcInstance contained in the NpcTemplate.<BR><BR>
	 */
	@Override
	public final int getLevel() {
		return (int) calcStat(Stats.LEVEL, getTemplate().Level, null, null);
	}
	
	/**
	 * Return True if the NpcInstance is agressive (ex : MonsterInstance in function of aggroRange).<BR><BR>
	 */
	public boolean isAggressive() {
		return false;
	}
	
	/**
	 * Return the Aggro Range of this NpcInstance contained in the NpcTemplate.<BR><BR>
	 */
	public int getAggroRange() {
		return getTemplate().AggroRange;
	}
	
	/**
	 * Return the Faction Range of this NpcInstance contained in the NpcTemplate.<BR><BR>
	 */
	//@Deprecated
	public int getFactionRange() {
		return getClanRange();
	}
	
	/**
	 * Return True if this NpcInstance is undead in function of the NpcTemplate.<BR><BR>
	 */
	@Override
	public boolean isUndead() {
		return getTemplate().isUndead();
	}
	
	/**
	 * Send a packet NpcInfo with state of abnormal effect to all Player in the KnownPlayers of the NpcInstance.<BR><BR>
	 */
	@Override
	public void updateAbnormalEffect() {
		if (getIsInvisible()) {
			return;
		}
		
		// Send a Server->Client packet NpcInfo with state of abnormal effect to all Player in the KnownPlayers of the NpcInstance
		Collection<Player> plrs = getKnownList().getKnownPlayers().values();
		//synchronized (getKnownList().getKnownPlayers())
		{
			for (Player player : plrs) {
				if (player == null) {
					continue;
				}
				if (getRunSpeed() == 0) {
					player.sendPacket(new ServerObjectInfo(this, player));
				} else {
					player.sendPacket(new NpcInfo(this, player));
				}
				player.sendPacket(new ExNpcSpeedInfo(this));
			}
		}
	}
	
	/**
	 * Return the distance under which the object must be add to knownObject in
	 * function of the object type.<BR>
	 * <BR>
	 * <p>
	 * <B><U> Values </U> :</B><BR>
	 * <BR>
	 * <li> object is a L2FolkInstance : 0 (don't remember it) </li>
	 * <li> object is a Creature : 0 (don't remember it) </li>
	 * <li> object is a L2PlayableInstance : 1500 </li>
	 * <li> others : 500 </li>
	 * <BR>
	 * <BR>
	 * <p>
	 * <B><U> Override in </U> :</B><BR>
	 * <BR>
	 * <li> Attackable</li>
	 * <BR>
	 * <BR>
	 *
	 * @param object The Object to add to knownObject
	 */
	public int getDistanceToWatchObject(WorldObject object) {
		if (object instanceof NpcInstance || !(object instanceof Creature)) {
			return 0;
		}
		
		if (object instanceof Playable) {
			return 1500;
		}
		
		return 500;
	}
	
	/**
	 * Return the distance after which the object must be remove from knownObject in function of the object type.<BR><BR>
	 * <p>
	 * <B><U> Values </U> :</B><BR><BR>
	 * <li> object is not a Creature : 0 (don't remember it) </li>
	 * <li> object is a L2FolkInstance : 0 (don't remember it)</li>
	 * <li> object is a L2PlayableInstance : 3000 </li>
	 * <li> others : 1000 </li><BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> Attackable</li><BR><BR>
	 *
	 * @param object The Object to remove from knownObject
	 */
	public int getDistanceToForgetObject(WorldObject object) {
		return getDistanceToWatchObject(object) + 200;
	}
	
	/**
	 * Return False.<BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> MonsterInstance : Check if the attacker is not another MonsterInstance</li>
	 * <li> Player</li><BR><BR>
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker) {
		return isAutoAttackable;
	}
	
	public boolean isAutoAttackable() {
		return isAutoAttackable;
	}
	
	public void setAutoAttackable(boolean flag) {
		isAutoAttackable = flag;
	}
	
	/**
	 * Return the Identifier of the item in the left hand of this NpcInstance contained in the NpcTemplate.<BR><BR>
	 */
	public int getLeftHandItem() {
		return currentLHandId;
	}
	
	/**
	 * Return the Identifier of the item in the right hand of this NpcInstance contained in the NpcTemplate.<BR><BR>
	 */
	public int getRightHandItem() {
		return currentRHandId;
	}
	
	public int getEnchantEffect() {
		return currentEnchant;
	}
	
	/**
	 * Return the busy status of this NpcInstance.<BR><BR>
	 */
	public final boolean isBusy() {
		return isBusy;
	}
	
	/**
	 * Set the busy status of this NpcInstance.<BR><BR>
	 */
	public void setBusy(boolean isBusy) {
		this.isBusy = isBusy;
	}
	
	/**
	 * Return the busy message of this NpcInstance.<BR><BR>
	 */
	public final String getBusyMessage() {
		return busyMessage;
	}
	
	/**
	 * Set the busy message of this NpcInstance.<BR><BR>
	 */
	public void setBusyMessage(String message) {
		busyMessage = message;
	}
	
	/**
	 * Return true if this Npc instance can be warehouse manager.<BR><BR>
	 */
	public boolean isWarehouse() {
		return false;
	}
	
	public boolean canTarget(Player player) {
		if (player.isOutOfControl() || !getTemplate().Targetable) {
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		if (player.isLockedTarget() && player.getLockedTarget() != this) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_CHANGE_TARGET));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		// TODO: More checks...
		
		return true;
	}
	
	public boolean canInteract(Player player) {
		// TODO: NPC busy check etc...
		
		if (player.isCastingNow() || player.isCastingSimultaneouslyNow()) {
			return false;
		}
		if (player.isDead() || player.isFakeDeath()) {
			return false;
		}
		if (player.isSitting()) {
			return false;
		}
		if (player.getPrivateStoreType() != 0) {
			return false;
		}
		if (!isInsideRadius(player, getInteractionDistance(), true, false)) {
			return false;
		}
		return !(player.getInstanceId() != getInstanceId() && getInstanceId() != player.getObjectId() && player.getInstanceId() != -1);
	}
	
	public int getInteractionDistance() {
		return getTemplate().InteractionDistance;
	}
	
	/**
	 * Return the L2Castle this NpcInstance belongs to.
	 */
	public final Castle getCastle() {
		// Get castle this NPC belongs to (excluding Attackable)
		if (castleIndex < 0) {
			TownZone town = TownManager.getTown(getX(), getY(), getZ());
			
			if (town != null) {
				castleIndex = CastleManager.getInstance().getCastleIndex(town.getTaxById());
			}
			
			if (castleIndex < 0) {
				castleIndex = CastleManager.getInstance().findNearestCastleIndex(this);
			} else {
				isInTown = true; // Npc was spawned in town
			}
		}
		
		if (castleIndex < 0) {
			return null;
		}
		
		return CastleManager.getInstance().getCastles().get(castleIndex);
	}
	
	/**
	 * Return closest castle in defined distance
	 *
	 * @param maxDistance long
	 * @return Castle
	 */
	public final Castle getCastle(long maxDistance) {
		int index = CastleManager.getInstance().findNearestCastleIndex(this, maxDistance);
		
		if (index < 0) {
			return null;
		}
		
		return CastleManager.getInstance().getCastles().get(index);
	}
	
	/**
	 * Return the L2Fort this NpcInstance belongs to.
	 */
	public final Fort getFort() {
		// Get Fort this NPC belongs to (excluding Attackable)
		if (fortIndex < 0) {
			Fort fort = FortManager.getInstance().getFort(getX(), getY(), getZ());
			if (fort != null) {
				fortIndex = FortManager.getInstance().getFortIndex(fort.getFortId());
			}
			
			if (fortIndex < 0) {
				fortIndex = FortManager.getInstance().findNearestFortIndex(this);
			}
		}
		
		if (fortIndex < 0) {
			return null;
		}
		
		return FortManager.getInstance().getForts().get(fortIndex);
	}
	
	/**
	 * Return closest Fort in defined distance
	 *
	 * @param maxDistance long
	 * @return Fort
	 */
	public final Fort getFort(long maxDistance) {
		int index = FortManager.getInstance().findNearestFortIndex(this, maxDistance);
		
		if (index < 0) {
			return null;
		}
		
		return FortManager.getInstance().getForts().get(index);
	}
	
	public final boolean getIsInTown() {
		if (castleIndex < 0) {
			getCastle();
		}
		
		return isInTown;
	}
	
	/**
	 * Open a quest or chat window on client with the text of the NpcInstance in function of the command.<BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packet : RequestBypassToServer</li><BR><BR>
	 *
	 * @param command The command string received from client
	 */
	public void onBypassFeedback(Player player, String command) {
		//if (canInteract(player))
		{
			if (isBusy() && getBusyMessage().length() > 0) {
				player.sendPacket(ActionFailed.STATIC_PACKET);
				
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getHtmlPrefix(), "npcbusy.htm");
				html.replace("%busymessage%", getBusyMessage());
				html.replace("%npcname%", getName());
				html.replace("%playername%", player.getName());
				player.sendPacket(html);
			} else {
				IBypassHandler handler = BypassHandler.getInstance().getBypassHandler(command);
				if (handler != null) {
					handler.useBypass(command, player, this);
				} else {
					log.warn("Unknown NPC bypass: \"" + command + "\" NpcId: " + getNpcId());
				}
			}
		}
	}
	
	/**
	 * Return null (regular NPCs don't have weapons).<BR><BR>
	 */
	@Override
	public Item getActiveWeaponInstance() {
		// regular NPCs don't have weapons
		return null;
	}
	
	/**
	 * Return the weapon item equiped in the right hand of the NpcInstance or null.<BR><BR>
	 */
	@Override
	public WeaponTemplate getActiveWeaponItem() {
		// Get the weapon identifier equiped in the right hand of the NpcInstance
		int weaponId = getTemplate().RHand;
		if (weaponId < 1) {
			return null;
		}
		
		// Get the weapon item equiped in the right hand of the NpcInstance
		ItemTemplate item = ItemTable.getInstance().getTemplate(getTemplate().RHand);
		if (!(item instanceof WeaponTemplate)) {
			return null;
		}
		
		return (WeaponTemplate) item;
	}
	
	/**
	 * Return null (regular NPCs don't have weapons instancies).<BR><BR>
	 */
	@Override
	public Item getSecondaryWeaponInstance() {
		// regular NPCs dont have weapons instancies
		return null;
	}
	
	/**
	 * Return the weapon item equiped in the left hand of the NpcInstance or null.<BR><BR>
	 */
	@Override
	public WeaponTemplate getSecondaryWeaponItem() {
		// Get the weapon identifier equiped in the right hand of the NpcInstance
		int weaponId = getTemplate().LHand;
		
		if (weaponId < 1) {
			return null;
		}
		
		// Get the weapon item equiped in the right hand of the NpcInstance
		ItemTemplate item = ItemTable.getInstance().getTemplate(getTemplate().LHand);
		
		if (!(item instanceof WeaponTemplate)) {
			return null;
		}
		
		return (WeaponTemplate) item;
	}
	
	/**
	 * Send a Server->Client packet NpcHtmlMessage to the Player in order to display the message of the NpcInstance.<BR><BR>
	 *
	 * @param player  The Player who talks with the NpcInstance
	 * @param content The text of the L2NpcMessage
	 */
	public void insertObjectIdAndShowChatWindow(Player player, String content, boolean isQuest) {
		// Send a Server->Client packet NpcHtmlMessage to the Player in order to display the message of the NpcInstance
		content = content.replaceAll("%objectId%", String.valueOf(getObjectId()));
		NpcHtmlMessage npcReply = new NpcHtmlMessage(getObjectId());
		npcReply.setHtml(content);
		player.sendPacket(npcReply);
	}
	
	public void insertObjectIdAndShowChatWindow(Player player, String content) {
		insertObjectIdAndShowChatWindow(player, content, false);
	}
	
	public String getHtmlPath(int npcId, String val) {
		if (val.isEmpty()) {
			return getHtmlPath(npcId, 0);
		}
		
		if (val.matches("-?\\d+")) {
			return getHtmlPath(npcId, Integer.parseInt(val));
		}
		
		return getHtmlPathCommon(npcId, val);
	}
	
	public String getHtmlPath(int npcId, int val) {
		return getHtmlPathCommon(npcId, String.valueOf(val));
	}
	
	/**
	 * Return the pathfile of the selected HTML file in function of the npcId and of the page number.<BR><BR>
	 * <p>
	 * <B><U> Format of the pathfile </U> :</B><BR><BR>
	 * <li> if the file exists on the server (page number = 0) : <B>data/html/default/12006.htm</B> (npcId-page number)</li>
	 * <li> if the file exists on the server (page number > 0) : <B>data/html/default/12006-1.htm</B> (npcId-page number)</li>
	 * <li> if the file doesn't exist on the server : <B>data/html/npcdefault.htm</B> (message : "I have nothing to say to you")</li><BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> GuardInstance : Set the pathfile to data/html/guard/12006-1.htm (npcId-page number)</li><BR><BR>
	 *
	 * @param npcId The Identifier of the NpcInstance whose text must be display
	 * @param val   The number of the page to display
	 */
	private String getHtmlPathCommon(int npcId, String val) {
		String pom = "";
		
		if (val.equalsIgnoreCase("0") || val.isEmpty()) {
			pom = "" + npcId;
		} else {
			pom = npcId + "-" + val;
		}
		
		String temp = "default/" + pom + ".htm";
		
		if (!Config.LAZY_CACHE) {
			// If not running lazy cache the file must be in the cache or it doesnt exist
			if (HtmCache.getInstance().contains(temp)) {
				return temp;
			}
		} else {
			if (HtmCache.getInstance().isLoadable(temp)) {
				return temp;
			}
		}
		
		// If the file is not found, the standard message "I have nothing to say to you" is returned
		return "npcdefault.htm";
	}
	
	public void showChatWindow(Player player) {
		showChatWindow(player, "");
	}
	
	/**
	 * Returns true if html exists
	 *
	 * @return boolean
	 */
	private boolean showPkDenyChatWindow(Player player, String type) {
		String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "" + type + "/" + getNpcId() + "-pk.htm");
		
		if (html != null) {
			NpcHtmlMessage pkDenyMsg = new NpcHtmlMessage(getObjectId());
			pkDenyMsg.setHtml(html);
			player.sendPacket(pkDenyMsg);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		
		return false;
	}
	
	public void showChatWindow(Player player, String val) {
		if (val.isEmpty()) {
			showChatWindow(player, 0);
		} else if (val.matches("-?\\d+")) {
			showChatWindow(player, Integer.parseInt(val));
		} else {
			showChatWindowCommon(player, val);
		}
	}
	
	public void showChatWindow(Player player, int val) {
		showChatWindowCommon(player, String.valueOf(val));
	}
	
	/**
	 * Open a chat window on client with the text of the NpcInstance.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the text of the selected HTML file in function of the npcId and of the page number </li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the NpcInstance to the Player </li>
	 * <li>Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet </li><BR>
	 *
	 * @param player The Player that talk with the NpcInstance
	 * @param val    The number of the page of the NpcInstance to display
	 */
	private void showChatWindowCommon(Player player, String val) {
		if (getTemplate().IsNonTalking) {
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.isCursedWeaponEquipped() &&
				(!(player.getTarget() instanceof ClanHallManagerInstance) || !(player.getTarget() instanceof DoormenInstance))) {
			player.setTarget(player);
			return;
		}
		if (player.getReputation() < 0) {
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && this instanceof MerchantInstance) {
				if (showPkDenyChatWindow(player, "merchant")) {
					return;
				}
			} else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && this instanceof TeleporterInstance) {
				if (showPkDenyChatWindow(player, "teleporter")) {
					return;
				}
			} else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && this instanceof WarehouseInstance) {
				if (showPkDenyChatWindow(player, "warehouse")) {
					return;
				}
			} else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && this instanceof FishermanInstance) {
				if (showPkDenyChatWindow(player, "fisherman")) {
					return;
				}
			}
		}
		
		if ("L2Auctioneer".equals(getTemplate().Type) && (val.isEmpty() || val.equalsIgnoreCase("0"))) {
			return;
		}
		
		int npcId = getTemplate().NpcId;
		
		/* For use with Seven Signs implementation */
		String filename;
		
		switch (npcId) {
			case 31688:
				if (player.isNoble()) {
					filename = Olympiad.OLYMPIAD_HTML_PATH + "noble_main.htm";
				} else {
					filename = getHtmlPath(npcId, val);
				}
				break;
			case 31690:
			case 31769:
			case 31770:
			case 31771:
			case 31772:
				if (player.isNoble()) {
					filename = Olympiad.OLYMPIAD_HTML_PATH + "hero_main.htm";
				} else {
					filename = getHtmlPath(npcId, val);
				}
				break;
			case 36402:
				if (player.olyBuff > 0) {
					filename = player.olyBuff == 5 ? Olympiad.OLYMPIAD_HTML_PATH + "olympiad_buffs.htm" :
							Olympiad.OLYMPIAD_HTML_PATH + "olympiad_5buffs.htm";
				} else {
					filename = Olympiad.OLYMPIAD_HTML_PATH + "olympiad_nobuffs.htm";
				}
				break;
			case 30298: // Blacksmith Pinter
				if (player.isAcademyMember()) {
					filename = getHtmlPath(npcId, "1");
				} else {
					filename = getHtmlPath(npcId, "0");
				}
				break;
			default:
				if (npcId >= 31093 && npcId <= 31094 || npcId >= 31172 && npcId <= 31201 || npcId >= 31239 && npcId <= 31254) {
					return;
				}
				// Get the text of the selected HTML file in function of the npcId and of the page number
				filename = getHtmlPath(npcId, val);
				break;
		}
		
		// Send a Server->Client NpcHtmlMessage containing the text of the NpcInstance to the Player
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), filename);
		
		if (this instanceof MerchantInstance) {
			if (Config.LIST_PET_RENT_NPC.contains(npcId)) {
				html.replace("_Quest", "_RentPet\">Rent Pet</a><br><a action=\"bypass -h npc_%objectId%_Quest");
			}
		}
		
		html.replace("%objectId%", String.valueOf(getObjectId()));
		
		html.replace("%serverId%", String.valueOf(Config.SERVER_ID));
		
		if (npcId == 40009) {
			String pker = "";
			int maxPks = 0;
			for (Player onlinePker : World.getInstance().getAllPlayers().values()) {
				if (onlinePker.isOnline() && !onlinePker.isGM() && onlinePker.getPkKills() >= maxPks) {
					maxPks = onlinePker.getPkKills();
					pker = onlinePker.getName();
				}
			}
			html.replace("%pker%", pker);
		} else if (npcId == 40004 && val.equals("main_towns")) {
			html.replace("%nextTowns%", MainTownManager.getInstance().getNextTownsInfo());
		} else if (npcId == 40001) {
			MainTownInfo mainTown = MainTownManager.getInstance().getCurrentMainTown();
			if (mainTown != null) {
				html.replace("%currentMainTown%", "(" + mainTown.getName() + ")");
			} else {
				html.replace("%currentMainTown%", "");
			}
		} else if (npcId == 40017) {
			MainTownInfo mainTown = MainTownManager.getInstance().getCurrentMainTown();
			if (mainTown != null) {
				html.replace("%currentMainTown%", "(" + mainTown.getName() + ")");
			} else {
				html.replace("%currentMainTown%", "");
			}
		} else if (npcId == 80001) // Astrake
		{
			int[] EPIC_BOSSES = {
					//25283, // Lilith
					25286, // Anakim
					29001, // Queen Ant
					29006, // Core
					29014, // Orfen
					//29019, // Antharas???
					29020, // Baium
					29022, // Zaken
					29028, // Valakas
					//29054, // Benom
					29065, // Sailren
					//29066, // Antharas???
					//29067, // Antharas???
					29068, // Antharas
					29240 // Lindvior
			};
			
			String epicInfo = "";
			
			for (int element : EPIC_BOSSES) {
				final NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(element);
				
				epicInfo += "<font color=\"50a2d0\">" + npcTemplate.getName() + "</font><br1>";
				
				if (GrandBossManager.getInstance().getBossStatus(npcTemplate.NpcId) == 0) {
					epicInfo += "Alive.<br>";
					
					continue;
				}
				
				SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE d MMMMMMM");
				
				long bossRespawnTime = System.currentTimeMillis() + GrandBossManager.getInstance().getUnlockTime(npcTemplate.NpcId);
				
				long earliestSpawnTime = 0;
				long latestSpawnTime = 0;
				
				String earliestSpawnTimeDay = "";
				String latestSpawnTimeDay = "";
				
				int displayType = 0; // Rnd.get(0, 2);
				switch (displayType) {
					case 0: {
						// Shows -1 +1
						earliestSpawnTime = bossRespawnTime - 3600000;
						latestSpawnTime = bossRespawnTime + 3600000;
						break;
					}
					case 1: {
						// Shows -2 0
						earliestSpawnTime = bossRespawnTime - 2 * 3600000;
						latestSpawnTime = bossRespawnTime;
						break;
					}
					case 2: {
						// Shows 0 +2
						earliestSpawnTime = bossRespawnTime;
						latestSpawnTime = bossRespawnTime + 2 * 3600000;
						break;
					}
				}
				
				earliestSpawnTimeDay = dateFormatter.format(earliestSpawnTime);
				latestSpawnTimeDay = dateFormatter.format(latestSpawnTime);
				
				dateFormatter = new SimpleDateFormat("k:m:s:");
				
				if (!earliestSpawnTimeDay.equals(latestSpawnTimeDay)) {
					epicInfo += "Spawning between " + earliestSpawnTimeDay + " at " + dateFormatter.format(earliestSpawnTime) + " and the " +
							latestSpawnTimeDay + " at " + dateFormatter.format(latestSpawnTime) + ".<br>";
				} else {
					epicInfo += "Spawning on " + earliestSpawnTimeDay + " between " + dateFormatter.format(earliestSpawnTime) + " and " +
							dateFormatter.format(latestSpawnTime) + ".<br>";
				}
			}
			html.replace("%bossList%", epicInfo);
		}
		
		player.sendPacket(html);
		
		// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Open a chat window on client with the text specified by the given file name and path,<BR>
	 * relative to the datapack root.
	 * <BR><BR>
	 * Added by Tempy
	 *
	 * @param player   The Player that talk with the NpcInstance
	 * @param filename The filename that contains the text to send
	 */
	public void showChatWindowByFileName(Player player, String filename) {
		// Send a Server->Client NpcHtmlMessage containing the text of the NpcInstance to the Player
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
		
		// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Return the Exp Reward of this NpcInstance contained in the NpcTemplate (modified by RATE_XP).<BR><BR>
	 */
	public float getExpReward() {
		return getTemplate().RewardExp * Config.RATE_XP;
	}
	
	/**
	 * Return the SP Reward of this NpcInstance contained in the NpcTemplate (modified by RATE_SP).<BR><BR>
	 */
	public int getSpReward() {
		return (int) (getTemplate().RewardSp * Config.RATE_SP);
	}
	
	/**
	 * Kill the NpcInstance (the corpse disappeared after 7 seconds).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create a DecayTask to remove the corpse of the NpcInstance after 7 seconds </li>
	 * <li>Set target to null and cancel Attack or Cast </li>
	 * <li>Stop movement </li>
	 * <li>Stop HP/MP/CP Regeneration task </li>
	 * <li>Stop all active skills effects in progress on the Creature </li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform </li>
	 * <li>Notify Creature AI </li><BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> Attackable </li><BR><BR>
	 *
	 * @param killer The Creature who killed it
	 */
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer)) {
			return false;
		}
		
		// normally this wouldn't really be needed, but for those few exceptions,
		// we do need to reset the weapons back to the initial templated weapon.
		currentLHandId = getTemplate().LHand;
		currentRHandId = getTemplate().RHand;
		currentCollisionHeight = getTemplate().getFCollisionHeight();
		currentCollisionRadius = getTemplate().getFCollisionRadius();
		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}
	
	/**
	 * Set the spawn of the NpcInstance.<BR><BR>
	 *
	 * @param spawn The L2Spawn that manage the NpcInstance
	 */
	public void setSpawn(L2Spawn spawn) {
		this.spawn = spawn;
	}
	
	@Override
	public void onSpawn() {
		super.onSpawn();
		
		if (getTemplate().getEventQuests(Quest.QuestEventType.ON_SPAWN) != null) {
			for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_SPAWN)) {
				quest.notifySpawn(this);
			}
		}
		
		if (getNpcId() == 33454) {
			setAI(new NewbieHelperAI(this));
		}
	}
	
	/**
	 * Remove the NpcInstance from the world and update its spawn object (for a complete removal use the deleteMe method).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the NpcInstance from the world when the decay task is launched </li>
	 * <li>Decrease its spawn counter </li>
	 * <li>Manage Siege task (killFlag, killCT) </li><BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from allObjects of World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR><BR>
	 */
	@Override
	public void onDecay() {
		if (isDecayed()) {
			return;
		}
		
		setDecayed(true);
		
		// Remove the NpcInstance from the world when the decay task is launched
		super.onDecay();
		
		// Decrease its spawn counter
		if (spawn != null) {
			spawn.onDecay(this);
		}
	}
	
	/**
	 * Remove PROPERLY the NpcInstance from the world.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the NpcInstance from the world and update its spawn object </li>
	 * <li>Remove all WorldObject from knownObjects and knownPlayer of the NpcInstance then cancel Attack or Cast and notify AI </li>
	 * <li>Remove WorldObject object from allObjects of World </li><BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR><BR>
	 */
	@Override
	public void deleteMe() {
		WorldRegion oldRegion = getWorldRegion();
		
		try {
			onDecay();
		} catch (Exception e) {
			log.error("Failed decayMe().", e);
		}
		try {
			if (getFusionSkill() != null || getContinuousDebuffTargets() != null) {
				abortCast();
			}
			
			for (Creature character : getKnownList().getKnownCharacters()) {
				if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this) {
					character.abortCast();
				}
			}
		} catch (Exception e) {
			log.error("deleteMe()", e);
		}
		if (oldRegion != null) {
			oldRegion.removeFromZones(this);
		}
		
		// Remove all WorldObject from knownObjects and knownPlayer of the Creature then cancel Attak or Cast and notify AI
		try {
			getKnownList().removeAllKnownObjects();
		} catch (Exception e) {
			log.error("Failed removing cleaning knownlist.", e);
		}
		
		// Remove WorldObject object from allObjects of World
		World.getInstance().removeObject(this);
		
		super.deleteMe();
	}
	
	/**
	 * Return the L2Spawn object that manage this NpcInstance.<BR><BR>
	 */
	public L2Spawn getSpawn() {
		return spawn;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + getTemplate().Name + "(" + getNpcId() + ")" + "[" + getObjectId() + "]";
	}
	
	public boolean isDecayed() {
		return isDecayed;
	}
	
	public void setDecayed(boolean decayed) {
		isDecayed = decayed;
	}
	
	public void endDecayTask() {
		if (!isDecayed()) {
			DecayTaskManager.getInstance().cancelDecayTask(this);
			onDecay();
		}
	}
	
	public boolean isMob() // rather delete this check
	{
		return false; // This means we use MAX_NPC_ANIMATION instead of MAX_MONSTER_ANIMATION
	}
	
	// Two functions to change the appearance of the equipped weapons on the NPC
	// This is only useful for a few NPCs and is most likely going to be called from AI
	public void setLHandId(int newWeaponId) {
		currentLHandId = newWeaponId;
		updateAbnormalEffect();
	}
	
	public void setRHandId(int newWeaponId) {
		currentRHandId = newWeaponId;
		updateAbnormalEffect();
	}
	
	public void setLRHandId(int newLWeaponId, int newRWeaponId) {
		currentRHandId = newRWeaponId;
		currentLHandId = newLWeaponId;
		updateAbnormalEffect();
	}
	
	public void setEnchant(int newEnchantValue) {
		currentEnchant = newEnchantValue;
		updateAbnormalEffect();
	}
	
	public void setHideName(boolean val) {
		isHideName = val;
	}
	
	public boolean isHideName() {
		return !getTemplate().ShowName || isHideName;
	}
	
	public void setCollisionHeight(double height) {
		currentCollisionHeight = height;
	}
	
	public void setCollisionRadius(double radius) {
		currentCollisionRadius = radius;
	}
	
	public double getCollisionHeight() {
		return currentCollisionHeight;
	}
	
	public double getCollisionRadius() {
		return currentCollisionRadius;
	}
	
	public boolean isRndWalk() {
		return isRndWalk;
	}
	
	public void setIsNoRndWalk(boolean isNoRndWalk) {
		isRndWalk = !isNoRndWalk;
	}
	
	@Override
	public void sendInfo(Player activeChar) {
		if (getIsInvisible()) {
			return;
		}
		
		if (Config.CHECK_KNOWN && activeChar.isGM()) {
			activeChar.sendMessage("Added NPC: " + getName());
		}
		
		if (getRunSpeed() == 0) {
			activeChar.sendPacket(new ServerObjectInfo(this, activeChar));
		} else {
			activeChar.sendPacket(new NpcInfo(this, activeChar));
		}
		activeChar.sendPacket(new ExNpcSpeedInfo(this));
	}
	
	public void showNoTeachHtml(Player player) {
		int npcId = getNpcId();
		String html = "";
		
		if (this instanceof WarehouseInstance) {
			html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "warehouse/" + npcId + "-noteach.htm");
		} else if (this instanceof TrainerInstance) {
			html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "trainer/" + npcId + "-noteach.htm");
		}
		
		if (html == null) {
			log.warn("Npc " + npcId + " missing noTeach html!");
			NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
			final String sb = StringUtil.concat("<html><body>" + "I cannot teach you any skills.<br>You must find your current class teachers.",
					"</body></html>");
			msg.setHtml(sb);
			player.sendPacket(msg);
		} else {
			NpcHtmlMessage noTeachMsg = new NpcHtmlMessage(getObjectId());
			noTeachMsg.setHtml(html);
			noTeachMsg.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(noTeachMsg);
		}
	}
	
	public Player getClonedPlayer() {
		return clonedPlayer;
	}
	
	public void setClonedPlayer(Player clonedPlayer) {
		this.clonedPlayer = clonedPlayer;
	}
	
	public void scheduleDespawn(long delay) {
		ThreadPoolManager.getInstance().scheduleGeneral(this.new DespawnTask(), delay);
	}
	
	private class DespawnTask implements Runnable {
		@Override
		public void run() {
			if (!isDecayed()) {
				deleteMe();
			}
		}
	}
	
	@Override
	protected final void notifyQuestEventSkillFinished(Skill skill, WorldObject target) {
		try {
			if (getTemplate().getEventQuests(Quest.QuestEventType.ON_SPELL_FINISHED) != null) {
				Player player = null;
				if (target != null) {
					player = target.getActingPlayer();
				}
				for (Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_SPELL_FINISHED)) {
					quest.notifySpellFinished(this, player, skill);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.model.actor.Creature#isMovementDisabled()
	 */
	@Override
	public boolean isMovementDisabled() {
		return super.isMovementDisabled() || !canMove() || getAiType().equals(AIType.CORPSE);
	}
	
	public AIType getAiType() {
		return getTemplate().getAIData().getAiType();
	}
	
	public void setDisplayEffect(int val) {
		if (val != displayEffect) {
			displayEffect = val;
			broadcastPacket(new ExChangeNpcState(getObjectId(), val));
		}
	}
	
	public int getDisplayEffect() {
		return displayEffect;
	}
	
	public int getColorEffect() {
		return 0;
	}
	
	/**
	 * Sends a chat to all knowObjects
	 *
	 * @param chat message to say
	 */
	public void broadcastChat(String chat, int id) {
		NpcSay cs;
		if (id == 0) {
			cs = new NpcSay(getObjectId(), Say2.ALL_NOT_RECORDED, getNpcId(), chat);
		} else {
			cs = new NpcSay(getObjectId(), Say2.ALL_NOT_RECORDED, getNpcId(), id);
		}
		Broadcast.toKnownPlayers(this, cs);
	}
	
	public void setIsInvisible(boolean b) {
		isInvisible = b;
		
		L2GameServerPacket toBroadcast;
		if (isInvisible) {
			toBroadcast = new DeleteObject(this);
		} else {
			toBroadcast = new NpcInfo(this, null);
		}
		
		for (Creature chara : getKnownList().getKnownCharacters()) {
			if (chara == null) {
				continue;
			}
			
			chara.broadcastPacket(toBroadcast);
		}
		
		this.broadcastPacket(toBroadcast);
	}
	
	public boolean getIsInvisible() {
		return isInvisible;
	}
	
	public void setIsLethalImmune(boolean b) {
		isLethalInmune = b;
	}
	
	public boolean getIsLethalInmune() {
		return isLethalInmune;
	}
	
	public boolean getIsDebuffInmune() {
		return isDebuffInmune;
	}
	
	public void setIsDebuffInmune(boolean b) {
		isDebuffInmune = b;
	}
	
	public void setOwner(Player owner) {
		this.owner = owner;
	}
	
	public Player getOwner() {
		return owner;
	}
	
	public final void tell(final Player player, final String message) {
		player.sendPacket(new CreatureSay(getObjectId(), Say2.TELL, getName(), message));
	}
	
	public final void tell(final int delay, final Player player, final String message) {
		ThreadPoolManager.getInstance().scheduleGeneral(() -> broadcastPacket(new CreatureSay(getObjectId(), Say2.TELL, getName(), message)), delay);
	}
	
	public final void say(final String message) {
		broadcastPacket(new CreatureSay(getObjectId(), Say2.ALL, getName(), message));
	}
	
	public final void say(final int delay, final String message) {
		ThreadPoolManager.getInstance().scheduleGeneral(() -> broadcastPacket(new CreatureSay(getObjectId(), Say2.ALL, getName(), message)), delay);
	}
}
