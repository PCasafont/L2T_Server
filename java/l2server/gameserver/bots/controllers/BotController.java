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
package l2server.gameserver.bots.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import l2server.gameserver.GeoData;
import l2server.gameserver.GeoEngine;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.bots.BotMode;
import l2server.gameserver.bots.BotsManager;
import l2server.gameserver.bots.DamageType;
import l2server.gameserver.bots.KnownAttacker;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.handler.ItemHandler;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2NpcBufferInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2RaidBossInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.pathfinding.AbstractNodeLoc;
import l2server.gameserver.pathfinding.PathFinding;
import l2server.gameserver.stats.skills.L2SkillSummon;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.gameserver.templates.item.L2Armor;
import l2server.gameserver.templates.item.L2EtcItem;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */

public abstract class BotController
{
	// The player instance this controller controls.
	protected final L2PcInstance _player;
	
	// The task handling updates of this controller.
	private ScheduledFuture<?> _updateTask;
	
	// The rate at which we should look for the next action.
	public int _refreshRate = 1000;
	
	// A map containing known players.
	protected Map<Integer, KnownAttacker> _knownAttackers = new HashMap<Integer, KnownAttacker>();
	
	// The time till which we'll keep focusing on our current target.
	protected long _lockTargetTill;
	
	// Used for BotMode.HUNTING, the time at which we'll be logging out.
	protected long _logoutAtTime;
	
	// Used for BotMode.HUNTING, the hunting ground for which this character was spawned.
	protected int _huntingGroundId;

	// The instance of the currently equipped weapon.
	protected L2ItemInstance _equippedWeapon;
	
	// The soulshots and blessed spiritshots item ids to use on the current weapon.
	private int[] _soulShotsToUse = null;
	
	// The last time we landed a hit on current target.
	protected long _lastTargetHitTime = 0;
	
	// The target we're currently focusing on. (always an enemy)
	protected L2Character _focusedTarget = null;
	
	// The target we're currently assisting. (always an amigo)
	protected L2Character _assistedTarget = null;
	
	// Disallows any action while running till destination is reached.
	protected boolean _actionLockTillDestinationReached = false;
	
	// Sometimes, the bot should just chill a little and do nothing.
	protected long _lockActionsTill;
	
	// Sometimes, we're going to decide to simply use general attacks for a few seconds.
	protected long _lockAttacksTill;
	
	// The time at which we'll next check for essential buffs.
	private long _nextCheckForEssentialActions = 0;
	
	// A record of the longest debuff cast range.
	protected int _maxDebuffCastRange = 0;
	
	// A record of the longest AoE reach range.
	private int _maxAoeReachRange = 0;
	
	// The time at which we last spawned a summon.
	protected long _summonSpawnTime;
	
	private BotMode _mode;
	
	private boolean _isHumanBehind;
	
	protected int _selectedLogoutEvent;
	
	protected static final int JUST_LOGOUT = 1;
	protected static final int GO_TO_NEAREST_AND_LOGOUT = 2;
	protected static final int DEFAULT_KITE_RATE = 10;
	
	/**
	 * [Skills Related Data]
	 */
	private final int[] AOE_ATTACK_SKILL_IDS = new int[0];
	private final int[] AOE_DEBUFF_SKILL_IDS = new int[0];
	private final int[] ATTACK_SKILL_IDS = new int[0];
	private final int[][] AREA_OF_EFFECT_SKILLS = new int[0][];
	private final int[] ESSENTIAL_BUFF_SKILL_IDS = new int[0];
	private final int[] DEBUFF_SKILL_IDS = new int[0];
	private final int[] SUMMON_SKILL_IDS = new int[0];
	private final int[] COMBAT_TOGGLE_IDS = new int[0];
	
	private static final int[] SOULSHOT_ITEMS_ID =
	{
		1835, // Soulshot - No Grade
		1463, // Soulshot - D Grade
		1464, // Soulshot - C Grade
		1465, // Soulshot - B Grade
		1466, // Soulshot - A Grade
		1467, // Soulshot - S Grade
		17754, // Soulshot - R Grade
	};
	
	private static final int[] BLESSED_SPIRITSHOT_ITEMS_ID =
	{
		3947, // Blessed Spiritshot - No Grade
		3948, // Blessed Spiritshot - D Grade
		3949, // Blessed Spiritshot - C Grade
		3950, // Blessed Spiritshot - B Grade
		3951, // Blessed Spiritshot - A Grade
		3952, // Blessed Spiritshot - S Grade
		19442, // Blessed Spiritshot - R Grade
	};
	
	private static final int BEAST_SOULSHOT_ID = 6645;
	private static final int BEAST_BLESSED_SPIRITSHOT_ID = 6647;
	
	// That just need to GTFO
	public static final int[] HEADING_ANGLES =
	{
		0, // Front
		2500, // Front 30
		-2500, 5000, -5000, // Front 330
		7500, -7500, 10000, // Front 60
		-10000, // Front 300
		12500, 15000, // Right Side
		-12500, -15000, // Left Side
	};
	
	public BotController(final L2PcInstance player)
	{
		_player = player;
	}
	
	private final void launchUpdateTask(final Runnable updateTask)
	{
		_updateTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(updateTask, _refreshRate, _refreshRate);
	}
	
	private class UpdateTask implements Runnable
	{
		private final BotController _fakePlayerAI;
		
		protected UpdateTask(BotController fakePlayerAI)
		{
			_fakePlayerAI = fakePlayerAI;
		}
		
		public void run()
		{
			try
			{
				_fakePlayerAI.lookForNextAction();
			}
			catch (Exception e)
			{
				Log.warning("An exception occured while trying to update " + _fakePlayerAI._player.getName() + " Controller...");
				
				e.printStackTrace();
			}
		}
	}
	
	public void onEnterWorld(final boolean isHumanBehind)
	{
		_isHumanBehind = isHumanBehind;
		
		if (_player.isDead())
			_player.doRevive(100);
		
		if (!isHumanBehind)
		{
			_player.spawnMe();
		}
		
		_maxDebuffCastRange = getMaxDebuffCastRange();
		_maxAoeReachRange = getMaxAoeReachRange(getLowRangeAttackSkillIds());
		
		int range = getMaxAoeReachRange(getAoeDebuffSkillIds());
		if (range > _maxAoeReachRange)
			_maxAoeReachRange = range;
		
		if (_mode == BotMode.HUNTING || _mode == BotMode.SPAWNED_BY_GM)
		{
			if (_mode == BotMode.SPAWNED_BY_GM)
				_player.broadcastTitleInfo();
			else
				_logoutAtTime = System.currentTimeMillis() + (60000 * Rnd.get(5, 10));
		}
		else if (_mode == BotMode.WARZONE)
			L2NpcBufferInstance.giveBasicBuffs(_player);

		checkIfIsProperlyEquipped();
		/*if (!Config.IS_CLASSIC)
		{
			_soulShotsToUse = new int[]
			{
				getSoulShotsToUse(), getBlessedSpiritShotsToUse()
			};
		}*/
		
		launchUpdateTask(new UpdateTask(this));
	}
	
	public final boolean onLogout()
	{
		// We just logout.
		if (_player.isCastingNow() || _player.isAttackingNow() || _player.isInCombat() || AttackStanceTaskManager.getInstance().getAttackStanceTask(_player) || _knownAttackers.size() != 0)
			return false;
		
		if (_selectedLogoutEvent == 0)
		{
			if (Rnd.nextBoolean())
				_selectedLogoutEvent = JUST_LOGOUT;
			else
				_selectedLogoutEvent = GO_TO_NEAREST_AND_LOGOUT;
		}
		
		if (_selectedLogoutEvent == GO_TO_NEAREST_AND_LOGOUT)
		{
		}
		
		onExitWorld();
		return true;
	}
	
	public void stopController()
	{
		if (_updateTask != null)
		{
			_updateTask.cancel(true);
			_updateTask = null;
		}
	}
	
	public void startController()
	{
		launchUpdateTask(new UpdateTask(this));
	}
	
	public final void onExitWorld()
	{
		stopController();
		
		if (!_isHumanBehind)
			_player.deleteMe();
		
		BotsManager.getInstance().logOutPlayer(_player);
	}
	
	public final void onDeath(final L2Character killer)
	{
		_knownAttackers.clear();
		
		_player.setTarget(null);
		
		if (_mode == BotMode.HUNTING)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				public void run()
				{
					Location loc = MapRegionTable.getInstance().getTeleToLocation(_player, MapRegionTable.TeleportWhereType.Town);
					
					if (loc != null)
					{
						_player.teleToLocation(loc, true);
						_player.doRevive(70);
					}
				}
			}, Rnd.get(2000, 10000));
		}
		else if (_mode == BotMode.SPAWNED_BY_GM)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				public void run()
				{
					if (_player.isDead())
						_player.doRevive(100);
				}
			}, 5000);
		}
	}
	
	public final void onTeleport()
	{
		_player.onTeleported();
		
		// Simulate client loading by prompting the bot to do any action shortly after teleportation...
		_lockActionsTill = System.currentTimeMillis() + Rnd.get(2500, 5000);
	}
	
	public final void onDamageReceived(final L2Character attacker, final DamageType damageType, final int damages)
	{
		KnownAttacker knownAttacker = null;
		if (!_knownAttackers.containsKey(attacker.getObjectId()))
			knownAttacker = addKnownAttacker(attacker);
		else
			knownAttacker = _knownAttackers.get(attacker.getObjectId());
		
		knownAttacker.increaseRecentDamages(damageType, damages);
	}
	
	public final void onDamagesDealt(final L2Character character)
	{
		if (character != _focusedTarget)
			return;
		
		_lastTargetHitTime = System.currentTimeMillis();
		
		//broadcastDebugMessage("Last hit on target NOW." + _lastTargetHitTime);
	}
	
	public void onSummonSpawn(final L2Summon summon)
	{
		_summonSpawnTime = System.currentTimeMillis();
		
		// Activate Beast Soulshots...
		if (summon.getChargedSoulShot() == L2ItemInstance.CHARGED_NONE)
		{
			final L2ItemInstance item = getItem(BEAST_SOULSHOT_ID);
			if (item != null)
			{
				useItem(item);
				
				_player.addAutoSoulShot(item.getItemId());
			}
		}
		
		// Activate Beast Spiritshots...
		if (summon.getChargedSpiritShot() == L2ItemInstance.CHARGED_NONE)
		{
			final L2ItemInstance item = getItem(BEAST_BLESSED_SPIRITSHOT_ID);
			if (item != null)
			{
				useItem(item);
				
				_player.addAutoSoulShot(item.getItemId());
			}
		}
	}
	
	public final void checkIfIsProperlyEquipped()
	{
		final PcInventory playerInventory = _player.getInventory();
		for (L2ItemInstance item : playerInventory.getItems())
		{
			if (!item.isEquipable() || item.isEquipped())
				continue;
			
			int bodyPart = item.getItem().getBodyPart();
			L2ItemInstance curItem = playerInventory.getPaperdollItem(PcInventory.getPaperdollIndex(bodyPart));

			if (item.isWeapon() && !isOkToEquip(item.getWeaponItem()))
				continue;
			else if (item.isArmor() && (bodyPart == L2Item.SLOT_CHEST || bodyPart == L2Item.SLOT_LEGS
					|| bodyPart == L2Item.SLOT_FULL_ARMOR) && !isOkToEquip(item.getArmorItem()))
				continue;
			
			// Higher reference price uses to mean better item
			if (curItem == null || item.getItem().getReferencePrice() > curItem.getItem().getReferencePrice())
				playerInventory.equipItem(item);
		}
	}
	
	public boolean checkIfIsReadyToFight()
	{
		final L2Weapon activeWeapon = _player.getActiveWeaponItem();
		// If the player do not have a weapon equipped, find an appropriate one and equip it.
		if (activeWeapon == null)
		{
			if (!_player.isDead() && !_player.isDisarmed() && !_player.isStunned() && !_player.isSleeping() && !_player.isAfraid() && !_player.isInLove())
			{
				if (_equippedWeapon != null)
					_player.useEquippableItem(_equippedWeapon, true);
			}
		}
		
		final long currentTime = System.currentTimeMillis();
		
		// Make sure the essential buffs are always up.
		if (_nextCheckForEssentialActions == 0 || _nextCheckForEssentialActions < currentTime)
		{
			int missingBuffId = pickSkill(getEssentialBuffSkillIds(), true);
			if (missingBuffId != -1)
			{
				useSkill(missingBuffId);
				
				_nextCheckForEssentialActions = currentTime + 500;
			}
			else
				_nextCheckForEssentialActions = currentTime + Rnd.get(3000, 6000);
			
			// Always make sure the player has enough soulshots/spiritshots and that they're auto-activated.
			if (_soulShotsToUse != null)
			{
				int itemIdToUse = 0;
				for (int i = 0; i < _soulShotsToUse.length; i++)
				{
					itemIdToUse = _soulShotsToUse[i];
					
					final L2ItemInstance item = getItem(itemIdToUse);
					if (item != null)
					{
						useItem(item);
						
						if (_player.getAutoSoulShot().contains(itemIdToUse))
							continue;
						
						_player.addAutoSoulShot(itemIdToUse);
					}
				}
			}
			
			// If this toon has summons, make sure a summon is around.
			if (getSummonSkillIds().length != 0 && (isAllowedToSummonInCombat() || !_player.isInCombat()))
			{
				final List<L2SummonInstance> summons = _player.getSummons();
				if (summons.size() == 0)
					spawnMySummon();
				// If the summon is around for at least 3seconds...
				else if (isSummonAllowedToAssist())
				{
					for (L2SummonInstance summon : summons)
					{
						final L2Object summonTarget = summon.getTarget();
						// Update the summon target to match the one of its owner.
						if (summonTarget != _focusedTarget)
						{
							summon.setTarget(_focusedTarget);
							summon.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _focusedTarget);
						}
					}
				}
			}
		}
		
		return true;
	}
	
	public void lookForNextAction()
	{
		if (!_player.isRunning())
			_player.setRunning();
		
		boolean isDisabled = false;
		
		final long currentTime = System.currentTimeMillis();
		
		if (isDisabled || currentTime < _lockActionsTill)
			return;
		
		// Slightly randomize the refresh rate.
		_refreshRate = Rnd.get(200, 400);
		
		// No need to look for a next action while dead or not ready to fight.
		if (_player.isDead() || !checkIfIsReadyToFight())
			return;
		
		L2Object target = _player.getTarget();
		
		// From time to time, we may want to switch target between summon and its owner.
		if (_lockTargetTill < currentTime)
		{
			if (_focusedTarget instanceof L2Summon)
			{
				final L2Summon summon = (L2Summon) _focusedTarget;
				final L2PcInstance summonOwner = summon.getOwner();
				
				if (!summonOwner.isDead() && Util.checkIfInRange(900, _player, summonOwner, false) && GeoEngine.getInstance().canSeeTarget(_player, summonOwner))
					setTarget(summonOwner);
			}
			else if (_focusedTarget instanceof L2PcInstance)
			{
				final L2PcInstance targetedPlayer = (L2PcInstance) _focusedTarget;
				final L2Summon targetedPlayerSummon = targetedPlayer.getPet();
				
				if (targetedPlayerSummon != null && !targetedPlayerSummon.isDead() && Util.checkIfInRange(900, _player, targetedPlayerSummon, false) && GeoEngine.getInstance().canSeeTarget(_player, targetedPlayerSummon))
					setTarget(targetedPlayerSummon);
			}
		}
		
		// If the player was previously casting a self-skill, and is done, set back the target we previously had.
		if (target == _player && !_player.isCastingNow())
			_player.setTarget(_focusedTarget);
		
		boolean allowLookingForTarget = true;
		
		// Make sure the target is up to date since we may just have modified it...
		target = _player.getTarget();
		
		if (_player.getAI() == null)
			return;
		
		// From time to time, run around the target.
		final CtrlIntention playerIntention = _player.getAI().getIntention();
		
		if (playerIntention == CtrlIntention.AI_INTENTION_PICK_UP)
		{
			// Randomly learn all skills here
			_player.giveAvailableSkills(false);
			return;
		}
		
		// We consider changing our target from time to time.
		if ((allowLookingForTarget && _lockTargetTill < currentTime) || (target instanceof L2PcInstance && ((L2PcInstance) target).getAppearance().getInvisible()))
		{
			maybeReconsiderTarget();
		}
		
		if (playerIntention == CtrlIntention.AI_INTENTION_CAST && target instanceof L2Character && !_player.isCastingNow() && Rnd.get(0, 3) == 0)
		{
			float headingAngle = (float) ((_player.getHeading() + Rnd.get(-7500, 7500)) * Math.PI) / Short.MAX_VALUE;
			
			float x = target.getX() + Rnd.get(-100, 100) * (float) Math.cos(headingAngle);
			float y = target.getY() + Rnd.get(-100, 100) * (float) Math.sin(headingAngle);
			float z = target.getZ() + 1;
			
			_player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition((int) x, (int) y, (int) z, _player.getHeading()));
			
		}
		
		// Under certain situations, we have to run... no matter what.
		boolean shouldRunNoMatterWhat = (_player.isMageClass() && _player.isMuted()) || _player.isDisarmed();
		
		int walkRate = shouldRunNoMatterWhat ? 25 : -1;
		
		// If we're not forced to run and got a target, let's jump it.
		if (!shouldRunNoMatterWhat && target != null)
		{
			if (!(target instanceof L2Character))
			{
				// Should never happen? gotta check...
				return;
			}
			
			final L2Character targetedCharacter = (L2Character) target;
			if (maybeMoveToBestPosition(targetedCharacter))
			{
				moveToBestPosition(targetedCharacter);
				return;
			}
			
			boolean shouldTryCastingSkill = true;
			switch (playerIntention)
			{
				case AI_INTENTION_IDLE:
					_actionLockTillDestinationReached = false;
					break;
				case AI_INTENTION_CAST:
					// Maintain current skill cast as is unless we haven't managed to hit the target in 5 seconds.
					if (_lastTargetHitTime + 5000 > System.currentTimeMillis())
						shouldTryCastingSkill = false;
					break;
			/*
			case AI_INTENTION_MOVE_TO:
			{
			_actionLockTillDestinationReached = true;
			_player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(target.getX() + Rnd.get(-50, 50), target.getY() + Rnd.get(-50, 50), target.getZ(), 0));
			broadcastDebugMessage("Moving to target rnd");
			break;
			}*/
			}
			
			if (_actionLockTillDestinationReached)
			{
				if (_player.isRooted() || !_player.isMoving())
				{
					_actionLockTillDestinationReached = false;
					
					shouldTryCastingSkill = true;
				}
				else
					shouldTryCastingSkill = false;
			}
			
			if (shouldTryCastingSkill)
			{
				if (!GeoEngine.getInstance().canSeeTarget(_player, targetedCharacter))
				{
					// Cant see target, let's try moving closer...
					_player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(target.getX() + Rnd.get(-50, 50), target.getY() + Rnd.get(-50, 50), target.getZ(), 0));
				}
				else if (shouldUseGeneralAttacks())
				{
					if (!(this instanceof MageController) && !_player.isAttackingNow())
					{
						_player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					}
				}
				else
				{
					int skillToUseNext = getSkillToUseNext(targetedCharacter);
					// No skill was selected... let's just attack then
					if (skillToUseNext == -1)
					{
						if (!(this instanceof MageController) && !_player.isAttackingNow())
							_player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					}
					else
						useSkill(skillToUseNext);
				}
			}
		}
		else if (!_player.isMoving())
		{
			if (shouldRunNoMatterWhat && Rnd.get(0, Rnd.get(5, 7)) != 0)
				return;
			
			// If the player isn't supposed to always run... maybe he could run a little... now...
			if (walkRate == -1)
			{
				walkRate = 50;
				if (Rnd.get(100) > walkRate)
					return;
			}
			
			//broadcastDebugMessage("RRRR");
			
			L2CharPosition bestPosition = getBestPositionToMoveNext(true, true, null);
			if (bestPosition != null)
			{
				_player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, bestPosition);
			
				//Randomize a bit the walk
				if (_mode != BotMode.AFKER)
				{
					if (!_player.isMoving())
					{
						float headingAngle = (float) ((_player.getHeading() + Rnd.get(-15000, 15000)) * Math.PI) / Short.MAX_VALUE;
						float x = _player.getX() - Rnd.get(100, 200) * (float) Math.cos(headingAngle);
						float y = _player.getY() - Rnd.get(100, 200) * (float) Math.sin(headingAngle);
						float z = GeoEngine.getInstance().getHeight((int) x, (int) y, _player.getZ());
						
						bestPosition = new L2CharPosition((int) x, (int) y, (int) z, 0);
						
						_player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, bestPosition);
					}
				}
			}
		}
	}
	
	private final int getSkillToUseNext(final L2Character targetedCharacter)
	{
		int selectedSkillId = -1;
		
		// We start by enabling/disabling combat toggles.
		// TODO Move that under add/remove KnownAttacker.
		int[] pickFromSkills = getCombatToggles();
		if (pickFromSkills.length != 0)
		{
			L2Abnormal effect = null;
			for (int toggleSkillId : pickFromSkills)
			{
				if (!isSkillAvailable(toggleSkillId))
					continue;
				
				effect = _player.getFirstEffect(toggleSkillId);
				
				if (_knownAttackers.size() == 0)
				{
					// We stop the toggle if there's no more known attackers.
					if (effect != null)
						effect.exit();
				}
				else
				{
					// We activate the toggle if we're under attack.
					if (effect == null)
					{
						return toggleSkillId;
					}
				}
			}
		}
		
		selectedSkillId = pickSpecialSkill(targetedCharacter);
		if (selectedSkillId != -1)
			return selectedSkillId;
		
		final int distanceToTarget = targetedCharacter == null ? 0 : (int) Util.calculateDistance(_player.getX(), _player.getY(), targetedCharacter.getX(), targetedCharacter.getY());
		
		int[][] areaOfEffectSkills = getAreaOfEffectSkills();
		if (areaOfEffectSkills.length != 0)
		{
			for (int i = 0; i < areaOfEffectSkills.length; i++)
			{
				final int[] skillData = areaOfEffectSkills[i];
				
				final int skillId = skillData[0];
				final int skillChances = skillData[1];
				final int minEnemiesIfTargetClose = skillData[2];
				final int minEnemiesIfTargetNotClose=  skillData[3];
				
				if (!isSkillAvailable(skillId))
					continue;
				
				int skillRadius = getSkillRadius(skillId);
				
				int enemiesNearby = getEnemiesAmountNearby(skillRadius);
				if (enemiesNearby > 0)
				{
					// If the target is within AoE range, we require less surrounding enemies to cast the skill.
					if (enemiesNearby > (skillRadius > distanceToTarget ? minEnemiesIfTargetClose : minEnemiesIfTargetNotClose))
						return skillId;
					else if (skillChances > Rnd.get(100))
						return skillId;
				}
			}
		}
		
		// We start by checking if we are in attack range to the target.
		// If not, we may want to use certain skills to catch up.
		/*final int attackRange = getMinimumRangeToUseCatchupSkill();
		if (distanceToTarget > attackRange)
		{
			pickFromSkills = getOnTargetSkillIds();
			
			if (pickFromSkills.length != 0)
			{
				selectedSkillId = pickSkill(pickFromSkills, false);
				
				if (selectedSkillId != -1)
					return selectedSkillId;
			}
		}*/
		
		/*if (isAllowedToUseEmergencySkills())
		{
			pickFromSkills = getEmergencySkillIds();
			
			if (pickFromSkills.length != 0)
			{
				selectedSkillId = pickSkill(pickFromSkills, false);
				
				if (selectedSkillId != -1)
					return selectedSkillId;
			}
		}*/
		
		if (shouldUseDebuff(distanceToTarget))
		{
			pickFromSkills = getDebuffSkillIds();
			
			if (pickFromSkills.length != 0)
			{
				selectedSkillId = pickDebuffSkill(pickFromSkills, targetedCharacter);
				
				if (selectedSkillId != -1)
					return selectedSkillId;
			}
		}
		
		pickFromSkills = getAttackSkillIds();
		if (pickFromSkills.length != 0)
		{
			selectedSkillId = pickSkill(pickFromSkills, false);
			
			if (selectedSkillId != -1)
				return selectedSkillId;
		}
		
		if (pickFromSkills.length == 0)
		{
			ArrayList<Integer> skills = new ArrayList<Integer>();
			for (L2Skill sk : _player.getAllSkills())
			{
				if (sk.isOffensive())
					skills.add(sk.getId());
			}
			
			pickFromSkills = new int[skills.size()];
			for (int i = 0; i < skills.size(); i++)
				pickFromSkills[i] = skills.get(i);
			selectedSkillId = pickSkill(pickFromSkills, false);
			if (selectedSkillId != -1)
				return selectedSkillId;
		}
		
		/*if (distanceToTarget < _maxAoeReachRange + 40)
		{
			pickFromSkills = getAoeDebuffSkillIds();
			
			if (pickFromSkills.length != 0 && Rnd.nextBoolean())
			{
				selectedSkillId = pickDebuffSkill(pickFromSkills, targetedCharacter);
				
				if (selectedSkillId != -1)
					return selectedSkillId;
			}
			
			pickFromSkills = getLowRangeAttackSkillIds();
			if (pickFromSkills.length != 0 && Rnd.nextBoolean())
			{
				selectedSkillId = pickDebuffSkill(pickFromSkills, targetedCharacter);
				
				if (selectedSkillId != -1)
					return selectedSkillId;
			}
		}*/
		
		return -1;
	}
	
	protected L2CharPosition getBestPositionToMoveNext(final boolean prefersForward, boolean allowOppositeRetry, Location loc)
	{
		float x = 0;
		float y = 0;
		float z = 0;
		
		L2CharPosition bestPosition = null;
		L2CharPosition bestPositionFromPreviousAngle = null;
		int bestDistance = 0;
		
		List<AbstractNodeLoc> path = null;
		for (int i = 0; i < HEADING_ANGLES.length; i++)
		{
			// Keep a trace of the previous angle best position in case we end up giving a...
			// yuck fou to the current angle best position.
			bestPositionFromPreviousAngle = bestPosition;
			L2CharPosition bestPositionFromCurrentAngle = null;
			
			int headingModifier = HEADING_ANGLES[i];
			if ((headingModifier >= 20000 || headingModifier <= -20000) && bestPosition != null)
				break;
			
			for (int i2 = 0; i2 < 5; i2++)
			{
				float headingAngle = (float) ((_player.getHeading() + headingModifier) * Math.PI) / Short.MAX_VALUE;
				
				if (loc != null)
				{
					//Specific path
					x = loc.getX() + (200 * i2) * (float) Math.cos(headingAngle);
					y = loc.getY() + (200 * i2) * (float) Math.sin(headingAngle);
				}
				else
				{	
					//Randomly
					x = _player.getX() + (200 * i2) * (float) Math.cos(headingAngle);
					y = _player.getY() + (200 * i2) * (float) Math.sin(headingAngle);
				}
				
				z = GeoEngine.getInstance().getHeight((int) x, (int) y, _player.getZ());
				
				if (z + 500 < _player.getZ() || _player.getZ() + 500 < z)
				{
					if (bestPositionFromCurrentAngle != null)
					{
						if (bestPositionFromPreviousAngle != null)
						{
							int distanceBetweenPreviousAndNextLoc = (int) Util.calculateDistance(bestPositionFromPreviousAngle.x, bestPositionFromPreviousAngle.y, (int) x, (int) y);
							int distanceBetweenCurrentAndNextLoc = (int) Util.calculateDistance(bestPositionFromCurrentAngle.x, bestPositionFromCurrentAngle.y, (int) x, (int) y);
							if (distanceBetweenPreviousAndNextLoc < distanceBetweenCurrentAndNextLoc)
								bestPosition = bestPositionFromCurrentAngle;
						}
					}
					else
						bestPosition = bestPositionFromPreviousAngle;
					
					break;
				}
				
				if (!GeoEngine.getInstance().canMoveFromToTarget(_player.getX(), _player.getY(), _player.getZ(), (int) x, (int) y, (int) z, _player.getInstanceId()))
				{
					path = PathFinding.getInstance().findPath(_player.getX(), _player.getY(), _player.getZ(), (int) x, (int) y, (int) z, _player.getInstanceId(), false);
					if (path == null || path.size() == 0)
						break;
				}
				
				int distance = (int) Util.calculateDistance(_player.getX(), _player.getY(), (int) x, (int) y);
				if (bestPosition == null || distance > bestDistance)
				{
					bestPosition = new L2CharPosition((int) x, (int) y, (int) z, 0);
					bestDistance = distance;
					bestPositionFromCurrentAngle = bestPosition;
				}
			}
		}
		
		int distance = 0;
		if (path != null && path.size() > 1)
		{
			final AbstractNodeLoc endPoint = path.get(path.size() - 1);
			
			//bestPosition = new L2CharPosition(endPoint.getX(), endPoint.getY(), endPoint.getZ(), 0);
			
			_player.sendMessage("Calculating distance betweenz " + endPoint.getX() + ", " + endPoint.getY() + " : " + _player.getX() + ", " + _player.getY());
		}
		
		if (bestPosition != null)
			distance = (int) Util.calculateDistance(_player.getX(), _player.getY(), bestPosition.x, bestPosition.y);
		
		if (bestPosition == null || distance < 200)
		{
			float headingAngle = (float) ((_player.getHeading() + Rnd.get(-15000, 15000)) * Math.PI) / Short.MAX_VALUE;
			
			x = _player.getX() - Rnd.get(100, 200) * (float) Math.cos(headingAngle);
			y = _player.getY() - Rnd.get(100, 200) * (float) Math.sin(headingAngle);
			
			z = GeoEngine.getInstance().getHeight((int) x, (int) y, _player.getZ());
			
			bestPosition = new L2CharPosition((int) x, (int) y, (int) z, 0);
		}
		
		return bestPosition;
	}
	
	protected final boolean isTargetGettingCloser()
	{
		final L2Object target = _player.getTarget();
		
		if (!(target instanceof L2PcInstance))
			return false;
		
		final L2PcInstance targetedPlayer = (L2PcInstance) target;
		
		final int[] movementTrace = targetedPlayer.getMovementTrace();
		final int[] previousMovementTrace = targetedPlayer.getPreviousMovementTrace();
		
		final int distanceAfterMove = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), movementTrace[0], movementTrace[1], movementTrace[2], false);
		final int distanceBeforeMove = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), previousMovementTrace[0], previousMovementTrace[1], previousMovementTrace[2], false);
		
		if (distanceBeforeMove > distanceAfterMove)
			return true;
		
		return false;
	}
	
	protected final int getDistanceTo(final L2Character target)
	{
		return (int) Util.calculateDistance(_player.getX(), _player.getY(), target.getX(), target.getY());
	}
	
	protected boolean maybeMoveToBestPosition(final L2Character targetedCharacter)
	{
		return false;
	}
	
	protected void moveToBestPosition(final L2Character targetedCharacter)
	{
		
	}
	
	protected int getMinimumRangeToKite(final L2Character targetedCharacter)
	{
		return Rnd.get(150, 200);
	}
	
	protected int getKiteRate(final L2Character targetedCharacter)
	{
		int result = DEFAULT_KITE_RATE;
		
		// Kite less if you're targeting self (healing cases here)
		if (targetedCharacter == _player)
			result /= 2;
		
		return result;
	}
	
	protected int getEnemiesAmountNearby(final int range)
	{
		int result = 0;
		for (L2PcInstance player : _player.getKnownList().getKnownPlayersInRadius(range))
		{
			if (!isTargetReachable(player, false))
				continue;
			
			result++;
		}
		
		return result;
	}
	
	protected boolean maybeKite(final L2Character targetedCharacter)
	{
		if (_actionLockTillDestinationReached)
			return false;
		
		int kiteRate = getKiteRate(targetedCharacter);
		
		if (Rnd.get(100) > kiteRate)
			return false;
		
		boolean result = false;
		
		if (targetedCharacter instanceof L2PcInstance)
		{
			final L2PcInstance targetedPlayer = (L2PcInstance) targetedCharacter;
			
			final int[] movementTrace = targetedPlayer.getMovementTrace();
			final int[] previousMovementTrace = targetedPlayer.getPreviousMovementTrace();
			
			final int distanceAfterMove = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), movementTrace[0], movementTrace[1], movementTrace[2], false);
			final int distanceBeforeMove = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), previousMovementTrace[0], previousMovementTrace[1], previousMovementTrace[2], false);
			
			int minimumRangeToRun = getMinimumRangeToKite(targetedCharacter);
			
			// If the target is too close, we kite.
			if (Util.checkIfInRange(minimumRangeToRun, _player, targetedCharacter, false))
				result = true;
			// If he isn't close, but is getting closer, sometimes, we kite.
			else if (Rnd.get(0, 5) == 0 && distanceBeforeMove > distanceAfterMove)
				result = true;
		}
		
		return result;
	}
	
	protected void kiteToBestPosition(final L2Character targetedCharacter, final boolean lockActions, final int minRange, final int maxRange)
	{
		if (targetedCharacter instanceof L2PcInstance)
		{
			// The character is getting closer to us here - so he's running in our direction.
			// We're going to look for a position in front of the targeted character...
			float headingAngle = (float) ((targetedCharacter.getHeading() + Rnd.get(-15000, 15000)) * Math.PI) / Short.MAX_VALUE;
			
			final int range = minRange != maxRange ? Rnd.get(minRange, maxRange) : minRange;
			
			float x = _player.getX() + range * (float) Math.cos(headingAngle);
			float y = _player.getY() + range * (float) Math.sin(headingAngle);
			float z = _player.getZ() + 1;
			
			boolean isDestinationReachable = GeoEngine.getInstance().canSeeTarget(_player.getX(), _player.getY(), _player.getZ(), (int) x, (int) y, (int) z);
			
			final int geoZ = GeoEngine.getInstance().getHeight((int) x, (int) y, (int) z);
			
			if (geoZ + 300 < _player.getZ() || _player.getZ() + 300 < geoZ)
				isDestinationReachable = false;
				
			boolean forceTowardRun = Rnd.get(0, 25) == 0;
			// If we can run forward, run forward.
			if (isDestinationReachable && !forceTowardRun)
				_player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition((int) x, (int) y, (int) z, targetedCharacter.getHeading()));
			// Otherwise, wait for the targeted character to get in range to us, then, just run behind him.
			else if (forceTowardRun || Util.checkIfInRange(300, _player, targetedCharacter, false))
			{
				x = _player.getX() - range * (float) Math.cos(headingAngle);
				y = _player.getY() - range * (float) Math.sin(headingAngle);
				z = _player.getZ() + 1;
				
				_player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition((int) x, (int) y, (int) z, targetedCharacter.getHeading()));
			}
			
			// Lock actions till the destination is reached?
			if (lockActions)
				_actionLockTillDestinationReached = true;
		}
	}
	
	protected final void maybeReconsiderTarget()
	{
		// We start by making sure known attackers are... up to date.
		Iterator<Entry<Integer, KnownAttacker>> it = _knownAttackers.entrySet().iterator();
		
		final long currentTime = System.currentTimeMillis();
		
		while (it.hasNext())
		{
			Entry<Integer, KnownAttacker> entry = it.next();
			
			final KnownAttacker knownAttacker = entry.getValue();
			if (knownAttacker == null)
				continue;
			
			final L2Character character = knownAttacker.getCharacter();
			
			final long lastAttackTime = knownAttacker.getLastAttackTime();
			
			int z = GeoEngine.getInstance().getHeight(character.getX(), character.getY(), _player.getZ());
			// Forget the known attacker if he's no longer visible, dead, didn't attack for 30seconds...
			boolean shouldForgetTarget = !_player.isInsideRadius(character, 1600, false, false) || character.getInstanceId() != _player.getInstanceId() || !character.isVisible() || (character instanceof L2PcInstance && ((L2PcInstance) character).getAppearance().getInvisible()) || character.isDead() || (lastAttackTime + 30000 < currentTime && _lastTargetHitTime + 10000 < currentTime)
					|| ((lastAttackTime + 15000 < currentTime && _lastTargetHitTime + 5000 < currentTime && // ... or didn't attack for 15seconds and cannot be reached by walking.
					!GeoEngine.getInstance().canMoveFromToTarget(_player.getX(), _player.getY(), z, character.getX(), character.getY(), z, _player.getInstanceId())));
			
			if (shouldForgetTarget)
			{
				knownAttacker.onForget();
				
				it.remove();
				
				if (character == _focusedTarget)
				{
					_focusedTarget = null;
					
					broadcastDebugMessage("Lost focus on target " + character.getName());
				}
				
				_player.setTarget(null);
				
				broadcastDebugMessage("Forgot " + character.getName());
				
				final L2Summon summon = _player.getPet();
				if (summon != null)
				{
					summon.setTarget(null);
					summon.abortAttack();
				}
			}
		}
		
		L2Object currentTarget = _player.getTarget();
		L2Character newTarget = null;
		
		if (currentTarget instanceof L2Character)
		{
			L2Character targetedCharacter = (L2Character) currentTarget;
			if (!GeoEngine.getInstance().canSeeTarget(_player, targetedCharacter))
			{
				if (_knownAttackers.containsKey(targetedCharacter.getObjectId()))
				{
					final KnownAttacker knownAttacker = _knownAttackers.get(targetedCharacter.getObjectId());
					
					knownAttacker.setLastUnreachabilityTime(currentTime);
					
					_player.setTarget(null);
					
					if (targetedCharacter == _focusedTarget)
						_focusedTarget = null;
					
					broadcastDebugMessage("FU� " + targetedCharacter.getName());
					targetedCharacter = null;
				}
			}
			
			if (targetedCharacter == null || targetedCharacter.isDead() || !targetedCharacter.isVisible())
			{
				if (targetedCharacter != null)
					removeTarget(targetedCharacter);
				
				newTarget = getNewTarget(true);
				if (newTarget == null)
					considerChoosingAnotherPlace();
			}
			else if (targetedCharacter.isInvul(_player))
			{
				// If the target is invulnerable... lets fuck it and pick another.
				newTarget = getNewTarget(false);
			}
			else if (_knownAttackers.size() != 0)
			{
				// TODO IMPROVE ME
				// When we just picked the target and are still running to it etc...
				if (_lastTargetHitTime + 5000 < System.currentTimeMillis())
				{
					double distance = Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), currentTarget.getX(), currentTarget.getY(), currentTarget.getZ(), false);
					
					newTarget = getMostDamagingKnownAttacker();
					
					if (newTarget == null && distance > 300)
						newTarget = getClosestKnownAttacker(_player.getX(), _player.getY(), _player.getZ());
					
					broadcastDebugMessage("Fed up of cTarget");
				}
			}
		}
		else
		{
			if (_knownAttackers.size() != 0)
				newTarget = Rnd.nextBoolean() ? getMostDamagingKnownAttacker() : getClosestKnownAttacker(_player.getX(), _player.getY(), _player.getZ());
			
			if (newTarget == null)
				newTarget = getNewTarget(false);
		}
		
		if (newTarget != null && newTarget != currentTarget)
		{
			boolean canPickTarget = true;
			if (_knownAttackers.containsKey(newTarget.getObjectId()))
			{
				final KnownAttacker knownAttacker = _knownAttackers.get(newTarget.getObjectId());
				
				if (knownAttacker.getLastUnreachabilityTime() + 10000 > currentTime && knownAttacker.getLastAttackTime() + 5000 < currentTime)
					canPickTarget = false;
			}
			
			if (canPickTarget)
				setTarget(newTarget);
		}
	}
	
	protected boolean isAllowedToUseEmergencySkills()
	{
		return false;
	}
	
	protected boolean isSummonAllowedToAssist()
	{
		return false;
	}
	
	protected boolean isAllowedToSummonInCombat()
	{
		return false;
	}
	
	public final boolean isSkillAvailable(final int skillId)
	{
		final L2Skill skill = _player.getKnownSkill(skillId);
		
		if (skill == null)
			return false;
		
		return !_player.isSkillDisabled(skill);
	}
	
	public final boolean isSkillAvailable(final L2Skill skill)
	{
		return !_player.isSkillDisabled(skill);
	}
	
	public final boolean isEffectActive(final int skillId)
	{
		return _player.getFirstEffect(skillId) != null;
	}
	
	protected boolean shouldUseDebuff(final int distanceToTarget)
	{
		return distanceToTarget < _maxDebuffCastRange + 40 && Rnd.nextBoolean();
	}
	
	protected int getGeneralAttackRate()
	{
		return 10;
	}
	
	protected boolean shouldUseGeneralAttacks()
	{
		if (_player.isPhysicalAttackMuted())
			return false;
		
		if (_player.isFighter())
		{
			// Player is unable to use skills at this time.
			if (_player.isPhysicalMuted())
				return true;
			
			final long currentTime = System.currentTimeMillis();	
			// We recently decided to attack, so lets keep this up.
			if (_lockAttacksTill > currentTime)
				return true;
			
			boolean shouldUseGeneralAttacks = getGeneralAttackRate() > Rnd.get(100);
			// If we recently locked attacks, don't lock again.
			if (_lockAttacksTill != 0 && _lockAttacksTill + 10000 > currentTime)
				shouldUseGeneralAttacks = false;
			
			if (shouldUseGeneralAttacks)
			{
				broadcastDebugMessage("Locked attacks");
				
				// We lock the use of skills for a few seconds.
				_lockAttacksTill = System.currentTimeMillis() + Rnd.get(1000, 3000);
				return true;
			}
		}
		else
		{
			if (_player.isMuted())
				return true;
		}
		
		return false;
	}
	
	protected final int pickSkill(final int[] skillIds, final boolean isBuff)
	{
		int result = -1;
		for (int i = skillIds.length; i-- > 0;)
		{
			int skillId = skillIds[i];
			
			L2Skill skill = _player.getKnownSkill(skillId);
			if (skill == null)
				continue;
			
			// Feoh's Stance Handling...
			if (skill.isStanceSwitch())
			{
				int stance = _player.getElementalStance();
				if (stance == 0)
					continue;
				
				if (stance > 4)
					stance = 5;
				
				L2Skill tempSkill = SkillTable.getInstance().getInfo(skillId + stance, skill.getLevelHash());
				if (tempSkill == null)
					continue;
				else if (!isSkillAvailable(tempSkill))
					continue;
			}
			
			if (!isSkillAvailable(skill))
				continue;
			else
			{
				if (skill.getNumCharges() > _player.getCharges())
					continue;
				else if (isBuff)
				{
					// If we already have this effect, fuck it.
					if (_player.getFirstEffect(skillId) != null)
						continue;
					
					final String firstEffectStacktype = skill.getFirstEffectStack();
					
					// If we already have an effect having the same stack type, fuck it.
					if (!firstEffectStacktype.equals("") && _player.getFirstEffect(firstEffectStacktype) != null)
						continue;
					
					// If we already have this cubic, fuck it.
					if (skill instanceof L2SkillSummon && ((L2SkillSummon) skill).isCubic())
					{
						int mastery = _player.getSkillLevelHash(143); // Cubic Mastery
						
						if (_player.getCubics().size() > mastery || _player.getCubics().containsKey(((L2SkillSummon) skill).getNpcId()))
							continue;
					}
				}
			}
			
			result = skillId;
			break;
		}
		
		return result;
	}
	
	protected int pickSpecialSkill(final L2Character target)
	{
		return -1;
	}
	
	protected final int pickDebuffSkill(final int[] skillIds, final L2Character target)
	{
		int result = -1;
		for (int i = skillIds.length; i-- > 0;)
		{
			int skillId = skillIds[i];
			
			final L2Skill skill = _player.getKnownSkill(skillId);
			if (skill == null)
				continue;
			else if (_player.isSkillDisabled(skill))
				continue;
			else if (target.getFirstEffect(skillId) != null)
				continue;
			
			result = skillId;
			break;
		}
		
		return result;
	}
	
	protected final void useSkill(final int skillId)
	{
		L2Skill skill = _player.getKnownSkill(skillId);
		if (skill == null)
		{
			broadcastDebugMessage("Skill " + skillId + " not found...");
			return;
		}
		
		// Feoh's Stance Handling...
		if (skill.isStanceSwitch())
		{
			int stance = _player.getElementalStance();
			if (stance == 0)
				return;
			
			if (stance > 4)
				stance = 5;
			
			skill = SkillTable.getInstance().getInfo(skillId + stance, skill.getLevelHash());
			if (skill == null)
			{
				broadcastDebugMessage("Skill " + skillId + " not found...");
				return;
			}
		}
		
		L2Object target = _player.getTarget();
		// Target self if this is a friendly skill... unless we're targeting our own summon.
		if (!skill.isOffensive())
		{
			if (target != null && target != _player.getPet() && target != _assistedTarget)
				setTarget(_player);
		}
		else
		{
			if (target != null && (target == _player || target == _player.getPet() || target == _assistedTarget))
				setTarget(_focusedTarget);
		}
		
		target = _player.getTarget();
		
		if (target != null)
			broadcastDebugMessage("Using " + skill.getName() + " on " + target.getName());
		
		_player.useMagic(skill, true, false);
		
		final long currentTime = System.currentTimeMillis();
		if (_player.getAI().getIntention() == CtrlIntention.AI_INTENTION_CAST && !_player.isCastingNow() && !_player.isMoving() && _lastTargetHitTime + 5000 < currentTime)
		{
			if (target instanceof L2Character)
			{
				if (_knownAttackers.containsKey(target.getObjectId()))
				{
					final KnownAttacker knownAttacker = _knownAttackers.get(target.getObjectId());
					if (knownAttacker.getLastAttackTime() + 5000 < currentTime)
					{
						knownAttacker.setLastUnreachabilityTime(System.currentTimeMillis());
						
						_player.setTarget(null);
						
						if (target == _focusedTarget)
							_focusedTarget = null;
						
						broadcastDebugMessage("FU " + target.getName());
					}
				}
			}
		}
	}
	
	protected final L2ItemInstance getItem(final int itemId)
	{
		L2ItemInstance item = _player.getInventory().getItemByItemId(itemId);
		if (item == null || item.getCount() < 2000)
		{
			_player.addItem("Admin", itemId, 2000, null, true);
			
			item = _player.getInventory().getItemByItemId(itemId);
		}
		return item;
	}
	
	protected final void useItem(final L2ItemInstance item)
	{
		if (!(item.getItem() instanceof L2EtcItem))
		{
			Log.warning(_player.getName() +  " tried to use a non-useable item... (" + item.getName() + ")");
			return;
		}
		
		IItemHandler handler = ItemHandler.getInstance().getItemHandler((L2EtcItem) item.getItem());
		if (handler != null)
			handler.useItem(_player, item, false);
		/**
		else
		{
			if (item.getItem() instanceof L2EtcItem)
			{
				_player.useEtcItem((L2EtcItem) item.getItem());
			}
			else
			{
				_player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}*/
	}
	
	protected void spawnMySummon()
	{
		int summonSkillId = pickSkill(getSummonSkillIds(), false);
		if (summonSkillId > 0)
			useSkill(summonSkillId);
	}
	
	protected ArrayList<L2PcInstance> getTargetsNeedingHelp(final int range, int healthPercent, int manaPercent)
	{
		final ArrayList<L2PcInstance> result = new ArrayList<L2PcInstance>();
		for (L2PcInstance player : _player.getKnownList().getKnownPlayersInRadius(range))
		{
			if (!isTargetReachable(player, true))
				continue;
			
			boolean requiresHeals = healthPercent != 0 && getHealthPercent(player) <= healthPercent;
			boolean requiresManaHeals = manaPercent != 0 && getManaPercent(player) <= manaPercent;
			if (!requiresHeals && !requiresManaHeals)
				continue;
			
			result.add(player);
		}
		
		return result;
	}
	
	static Set<L2MonsterInstance> _mobsUnderAttack = new HashSet<L2MonsterInstance>();
	
	private final L2Character getNewTarget(final boolean fromKnownAttackers)
	{
		if (fromKnownAttackers)
			return getMostDamagingKnownAttacker();
		
		L2Character target = null;
		if (_mode == BotMode.SPAWNED_BY_GM || _mode == BotMode.WARZONE)
		{
			final int maxWatchDistance = _player.getKnownList().getDistanceToWatchObject(null);
			
			target = getFavoriteTarget(_player.getKnownList().getKnownPlayersInRadius((int) ((maxWatchDistance * 1.25) - maxWatchDistance)), true, false);
			if (target == null)
				target = getFavoriteTarget(_player.getKnownList().getKnownPlayersInRadius((int) ((maxWatchDistance * 1.50) - maxWatchDistance)), true, false);
			
			if (target == null)
				target = getFavoriteTarget(_player.getKnownList().getKnownPlayersInRadius((int) ((maxWatchDistance * 1.75) - maxWatchDistance)), true, false);
			
			if (target == null)
				target = getFavoriteTarget(_player.getKnownList().getKnownPlayersInRadius(maxWatchDistance), true, false);
		}
		else if (_mode == BotMode.HUNTING)
		{
			List<L2Character> knownCharacters = new ArrayList<L2Character>(_player.getKnownList().getKnownCharacters());
			Collections.shuffle(knownCharacters);
			
			for (L2Character character : knownCharacters)
			{
				if (character.isDead())
					continue;
				else if (!GeoEngine.getInstance().canSeeTarget(_player, character))
					continue;
				else if (character instanceof L2Playable && ((L2Playable)character).getReputation() >= 0 && !feelsLikePvping())
					continue;
				else if (character instanceof L2RaidBossInstance || !(character instanceof L2MonsterInstance))
					continue;
				else if (Math.abs(character.getLevel() - _player.getLevel()) > 3)
					continue;
				else if (target != null)
				{
					double dist1 = Util.calculateDistance(_player, target, false);
					double dist2 = Util.calculateDistance(_player, character, false);
					if ((character.getLevel() > _player.getLevel() || character.getLevel() <= target.getLevel()) && dist2 * 1.3 > dist1)
						continue;
				}
				
				synchronized (_mobsUnderAttack)
				{
					if (!_mobsUnderAttack.contains(character))
						target = character;
				}
			}
		}
		
		if (target instanceof L2MonsterInstance)
		{
			synchronized (_mobsUnderAttack)
			{
				_mobsUnderAttack.add((L2MonsterInstance)target);
			}
		}
		return target;
	}
	
	protected final boolean isTargetReachable(final L2Character target, final boolean lookForFriendlyTarget)
	{
		if (target.isDead())
			return false;
		
		if (target instanceof L2Playable)
		{
			final L2PcInstance targetedPlayer = target.getActingPlayer();
			
			if (targetedPlayer.getAppearance().getInvisible())
				return false;
			
			if (_mode == BotMode.WARZONE && targetedPlayer.getTeam() == _player.getTeam())
				return false;
				
			if (_mode == BotMode.SPAWNED_BY_GM)
				return true;
			
			if (lookForFriendlyTarget)
				return false;
		}
		else if (lookForFriendlyTarget)
			return false;
		
		if (!GeoEngine.getInstance().canSeeTarget(_player, target)
				&& !GeoEngine.getInstance().canMoveFromToTarget(_player.getX(), _player.getY(), _player.getZ(), target.getX(), target.getY(), target.getZ(), _player.getInstanceId()))
			return false;
		else if (target.isInvul(_player))
			return false;
		
		final long currentTime = System.currentTimeMillis();
		
		if (_knownAttackers.containsKey(target.getObjectId()))
		{
			final KnownAttacker knownAttacker = _knownAttackers.get(target.getObjectId());
			if (knownAttacker.getLastUnreachabilityTime() + 10000 > currentTime && knownAttacker.getLastAttackTime() + 5000 < currentTime)
				return false;
		}
		
		return true;
	}
	
	private final L2PcInstance getFavoriteTarget(Collection<L2PcInstance> collection, boolean shuffle, final boolean lookForFriendlyTarget)
	{
		if (collection.size() == 0)
			return null;
		
		// Randomize the order of players to avoid plenty of bots picking up on the same target...
		if (shuffle)
			Collections.shuffle((List<L2PcInstance>) collection);
		
		L2PcInstance result = null;
		for (L2PcInstance player : collection)
		{
			if (!isTargetReachable(player, lookForFriendlyTarget))
				continue;
			
			result = player;
			break;
		}
		
		return result;
	}
	
	protected final void setTarget(final L2Character target)
	{
		_player.setTarget(target);
		
		if (target != _player && target != _player.getPet())
		{
			_focusedTarget = target;
			
			if (!_knownAttackers.containsKey(target.getObjectId()))
				addKnownAttacker(target);
		}
		
		final long currentTime = System.currentTimeMillis();
		
		// TODO
		// Rename this var to _targetLockedTill
		_lockTargetTill = currentTime + Rnd.get(3000, 6000);
		
		// We also set this to.. right now, to avoid canceling new target right away because...
		// ... we end up thinking the target have been unreachable.
		_lastTargetHitTime = currentTime;
	}
	
	protected final void removeTarget(final L2Character target)
	{
		_knownAttackers.remove(target.getObjectId());
		
		if (_player.getTarget() == target)
			_player.setTarget(null);
		
		if (target instanceof L2MonsterInstance)
		{
			synchronized (_mobsUnderAttack)
			{
				_mobsUnderAttack.remove(target);
			}
		}
	}
	
	protected final KnownAttacker addKnownAttacker(final L2Character attacker)
	{
		final KnownAttacker knownAttacker = new KnownAttacker(attacker);
		
		_knownAttackers.put(attacker.getObjectId(), knownAttacker);
		
		return knownAttacker;
	}
	
	protected final boolean isKnownAttacker(final int objectId)
	{
		return _knownAttackers.containsKey(objectId);
	}
	
	protected final L2Character getMostDamagingKnownAttacker()
	{
		L2Character result = null;
		
		double mostDamages = 0;
		
		final long currentTime = System.currentTimeMillis();
		for (KnownAttacker ka : _knownAttackers.values())
		{
			if (ka == null)
				continue;
			
			if (ka.getLastUnreachabilityTime() + 10000 > currentTime && ka.getLastAttackTime() + 5000 < currentTime)
				continue;
			else if (mostDamages != 0 && mostDamages > ka.getTotalDamages())
				continue;
			else if (!isTargetReachable(ka.getCharacter(), false))
				continue;
			
			mostDamages = ka.getTotalDamages();
			result = ka.getCharacter();
		}
		
		return result;
	}
	
	protected final long getLastDamagesReceivedTime()
	{
		long result = 0;
		for (KnownAttacker ka : _knownAttackers.values())
		{
			long lastAttackTime = ka.getLastAttackTime();
			
			if (lastAttackTime < result)
				continue;
			
			result = lastAttackTime;
		}
		return result;
	}
	
	protected final L2Character getClosestKnownAttacker(final int playerX, final int playerY, final int playerZ)
	{
		L2Character result = null;
		double currentClosest = 0;
		
		final long currentTime = System.currentTimeMillis();
		for (KnownAttacker ka : _knownAttackers.values())
		{
			if (ka == null)
				continue;
			
			if (ka.getLastUnreachabilityTime() + 10000 > currentTime && ka.getLastAttackTime() + 5000 < currentTime)
				continue;
			
			final L2Character character = ka.getCharacter();
			double distance = Util.calculateDistance(playerX, playerY, playerZ, character.getX(), character.getY(), character.getZ(), false);
			
			if (currentClosest != 0 && distance > currentClosest)
				continue;
			
			currentClosest = distance;
			result = character;
		}
		
		return result;
	}
	
	public final void considerChoosingAnotherPlace()
	{
		L2MonsterInstance closestMob = null;
		double bestDist = 0.0;
		for (L2Object obj : L2World.getInstance().getAllVisibleObjects().values())
		{
			if (!(obj instanceof L2MonsterInstance))
				continue;
			
			L2MonsterInstance mob = (L2MonsterInstance)obj;
			if (mob.isRaid() || mob.getLevel() > _player.getLevel() || mob.getLevel() < _player.getLevel() - 2)
				continue;

			double dist = Util.calculateDistance(_player,  mob, true);
			if (closestMob == null)
			{
				closestMob = mob;
				bestDist = dist;
				continue;
			}
			
			if (dist < bestDist)
			{
				closestMob = mob;
				bestDist = dist;
			}
		}
		
		if (closestMob == null)
			return;
		
		/*if (bestDist < 5000)
		{
			// try to move there
		}
		else*/
		{
			// Teleport there
			int x = closestMob.getX() + Rnd.get(2000) - 1000;
			int y = closestMob.getY() + Rnd.get(2000) - 1000;
			int z = GeoData.getInstance().getHeight(x, y, closestMob.getZ());
			_player.teleToLocation(x, y, z);
		}
	}
	
	public final int getAmountOfTargetsInRangeByDamageType(final int range, final DamageType damageType, final int ignoreEffectedById)
	{
		int result = 0;
		
		for (KnownAttacker ka : _knownAttackers.values())
		{
			final L2Character character = ka.getCharacter();
			
			if (Util.calculateDistance(_player.getX(), _player.getY(), character.getX(), character.getY()) > range)
				continue;
			else if (ignoreEffectedById != 0 && character.getFirstEffect(ignoreEffectedById) != null)
				continue;
			else if (ka.getTotalDamagesByType(damageType) == 0)
				continue;
			
			result++;
		}
		
		return result;
	}
	
	protected final int getTotalDamagesByType(final DamageType damageType)
	{
		int totalDamages = 0;
		
		for (KnownAttacker ka : _knownAttackers.values())
		{
			int damages = ka.getTotalDamagesByType(damageType);
			
			if (damages == 0)
				continue;
			
			totalDamages += damages;
		}
		
		return totalDamages;
	}
	
	public final int getPlayerHealthPercent()
	{
		return (int) (_player.getCurrentHp() * 100 / _player.getMaxHp());
	}
	
	public final int getHealthPercent(L2Character character)
	{
		return (int) (character.getCurrentHp() * 100 / character.getMaxHp());
	}
	
	public final int getManaPercent(L2Character character)
	{
		return (int) (character.getCurrentMp() * 100 / character.getMaxMp());
	}
	
	public final int getTargetHealthPercent()
	{
		final L2Object target = _player.getTarget();
		if (!(target instanceof L2Character))
			return -1;
		
		final L2Character targetedCharacter = (L2Character) target;
		
		return (int) (targetedCharacter.getCurrentHp() * 100 / targetedCharacter.getMaxHp());
	}
	
	public final int getPlayerManaPercent()
	{
		return (int) (_player.getCurrentMp() * 100 / _player.getMaxMp());
	}
	
	protected int[] getAttackSkillIds()
	{
		return ATTACK_SKILL_IDS;
	}
	
	public int[][] getAreaOfEffectSkills()
	{
		return AREA_OF_EFFECT_SKILLS;
	}
	
	protected int[] getEssentialBuffSkillIds()
	{
		return ESSENTIAL_BUFF_SKILL_IDS;
	}
	
	protected int[] getDebuffSkillIds()
	{
		return DEBUFF_SKILL_IDS;
	}
	
	protected int[] getSummonSkillIds()
	{
		return SUMMON_SKILL_IDS;
	}
	
	protected int[] getCombatToggles()
	{
		return COMBAT_TOGGLE_IDS;
	}
	
	protected int getAttackRange()
	{
		return _player.getTemplate().baseAtkRange;
	}
	
	protected int getMinimumRangeToUseCatchupSkill()
	{
		return getAttackRange() * 5;
	}
	
	private final int getMaxDebuffCastRange()
	{
		L2Skill skill = null;
		
		int[] debuffSkills = getDebuffSkillIds();
		int maxCastRange = 0;
		for (int i = debuffSkills.length; i-- > 0;)
		{
			final int skillId = debuffSkills[i];
			
			skill = _player.getKnownSkill(skillId);
			
			if (skill == null)
				continue;
			
			if (maxCastRange != 0 && skill.getCastRange() < maxCastRange)
				continue;
			
			maxCastRange = skill.getCastRange();
		}
		
		return maxCastRange;
	}
	
	private final int getMaxAoeReachRange(int[] skillIds)
	{
		L2Skill skill = null;
		
		int maxRadius = 0;
		for (int i = skillIds.length; i-- > 0;)
		{
			final int skillId = skillIds[i];
			
			skill = _player.getKnownSkill(skillId);
			
			if (skill == null)
				continue;
			
			if (maxRadius != 0 && skill.getSkillRadius() < maxRadius)
				continue;
			
			maxRadius = skill.getCastRange();
		}
		return maxRadius;
	}
	
	protected final int getSkillRadius(final int skillId)
	{
		final L2Skill skill = _player.getKnownSkill(skillId);
		if (skill == null)
			return 0;
		
		return skill.getSkillRadius() / 2;
	}
	
	protected boolean isOkToEquip(L2Armor armorItem)
	{
		return true;
	}
	
	protected boolean isOkToEquip(L2Weapon weaponItem)
	{
		return true;
	}
	
	public void setCurrentWeapon (L2ItemInstance weapon)
	{
		_equippedWeapon = weapon;
	}
	
	@SuppressWarnings("unused")
	private final int getSoulShotsToUse()
	{
		final L2ItemInstance activeWeapon = _player.getActiveWeaponInstance();
		if (activeWeapon == null)
			return SOULSHOT_ITEMS_ID[0];
		
		switch (activeWeapon.getItem().getCrystalType())
		{
			case L2Item.CRYSTAL_D:
				return SOULSHOT_ITEMS_ID[1];
			case L2Item.CRYSTAL_C:
				return SOULSHOT_ITEMS_ID[2];
			case L2Item.CRYSTAL_B:
				return SOULSHOT_ITEMS_ID[3];
			case L2Item.CRYSTAL_A:
				return SOULSHOT_ITEMS_ID[4];
			case L2Item.CRYSTAL_S:
			case L2Item.CRYSTAL_S80:
			case L2Item.CRYSTAL_S84:
				return SOULSHOT_ITEMS_ID[5];
			case L2Item.CRYSTAL_R:
			case L2Item.CRYSTAL_R95:
			case L2Item.CRYSTAL_R99:
				return SOULSHOT_ITEMS_ID[6];
		}
		return SOULSHOT_ITEMS_ID[0];
	}
	
	@SuppressWarnings("unused")
	private final int getBlessedSpiritShotsToUse()
	{
		final L2ItemInstance activeWeapon = _player.getActiveWeaponInstance();
		if (activeWeapon == null)
			return BLESSED_SPIRITSHOT_ITEMS_ID[0];
		
		switch (activeWeapon.getItem().getCrystalType())
		{
			case L2Item.CRYSTAL_D:
				return BLESSED_SPIRITSHOT_ITEMS_ID[1];
			case L2Item.CRYSTAL_C:
				return BLESSED_SPIRITSHOT_ITEMS_ID[2];
			case L2Item.CRYSTAL_B:
				return BLESSED_SPIRITSHOT_ITEMS_ID[3];
			case L2Item.CRYSTAL_A:
				return BLESSED_SPIRITSHOT_ITEMS_ID[4];
			case L2Item.CRYSTAL_S:
			case L2Item.CRYSTAL_S80:
			case L2Item.CRYSTAL_S84:
				return BLESSED_SPIRITSHOT_ITEMS_ID[5];
			case L2Item.CRYSTAL_R:
			case L2Item.CRYSTAL_R95:
			case L2Item.CRYSTAL_R99:
				return BLESSED_SPIRITSHOT_ITEMS_ID[6];
		}
		return BLESSED_SPIRITSHOT_ITEMS_ID[0];
	}
	
	public final void broadcastDebugMessage(final String text)
	{
		//Log.info(text);
		//_player.broadcastPacket(new CreatureSay(_player.getObjectId(), Say2.ALL, _player.getName(), text));
	}
	
	private final boolean feelsLikePvping()
	{
		return false;
	}
	
	public final void setMode(final BotMode mode)
	{
		_mode = mode;
	}
	
	public final BotMode getMode()
	{
		return _mode;
	}
	
	public final boolean isHumanBehind()
	{
		return _isHumanBehind;
	}
	
	public final void setHuntingGroundId(final int huntingGroundId)
	{
		_huntingGroundId = huntingGroundId;
	}
	
	public final int getHuntingGroundId()
	{
		return _huntingGroundId;
	}
	
	protected int[] getAoeDebuffSkillIds()
	{
		return AOE_DEBUFF_SKILL_IDS;
	}
	
	protected int[] getLowRangeAttackSkillIds()
	{
		return AOE_ATTACK_SKILL_IDS;
	}
	
	public final void lockActionsTill(final long time)
	{
		_lockActionsTill = time;
	}
	
	public L2PcInstance getPlayer()
	{
		return _player;
	}
}