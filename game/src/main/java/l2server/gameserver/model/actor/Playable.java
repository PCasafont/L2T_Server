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

import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.InstanceType;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.instance.MobSummonInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.knownlist.PlayableKnownList;
import l2server.gameserver.model.actor.stat.PlayableStat;
import l2server.gameserver.model.actor.status.PlayableStatus;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.templates.chars.CreatureTemplate;
import l2server.gameserver.templates.skills.EffectType;

/**
 * This class represents all Playable characters in the world.<BR><BR>
 * <p>
 * L2PlayableInstance :<BR><BR>
 * <li>Player</li>
 * <li>Summon</li><BR><BR>
 */

public abstract class Playable extends Creature {
	private Creature lockedTarget = null;
	
	/**
	 * Constructor of L2PlayableInstance (use Creature constructor).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the Creature constructor to create an empty skills slot and link copy basic Calculator set to this L2PlayableInstance </li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The CreatureTemplate to apply to the L2PlayableInstance
	 */
	public Playable(int objectId, CreatureTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2Playable);
		setIsInvul(false);
	}
	
	@Override
	public PlayableKnownList getKnownList() {
		return (PlayableKnownList) super.getKnownList();
	}
	
	@Override
	public PlayableKnownList initialKnownList() {
		return new PlayableKnownList(this);
	}
	
	@Override
	public PlayableStat getStat() {
		return (PlayableStat) super.getStat();
	}
	
	@Override
	public void initCharStat() {
		setStat(new PlayableStat(this));
	}
	
	@Override
	public PlayableStatus getStatus() {
		return (PlayableStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus() {
		setStatus(new PlayableStatus(this));
	}
	
	@Override
	public boolean doDie(Creature killer) {
		// killing is only possible one time
		synchronized (this) {
			if (isDead()) {
				return false;
			}
			// now reset currentHp to zero
			setCurrentHp(0);
			setIsDead(true);
		}
		
		// Set target to null and cancel Attack or Cast
		setTarget(null);
		
		// Stop movement
		stopMove(null);
		
		// Stop HP/MP/CP Regeneration task
		getStatus().stopHpMpRegeneration();
		
		// Stop all active skills effects in progress on the Creature,
		// if the Character isn't affected by Soul of The Phoenix or Salvation
		if (isPhoenixBlessed()) {
			if (getCharmOfLuck()) //remove Lucky Charm if player has SoulOfThePhoenix/Salvation buff
			{
				stopCharmOfLuck(null);
			}
			if (isNoblesseBlessed()) {
				stopNoblesseBlessing(null);
			}
		}
		// Same thing if the Character isn't a Noblesse Blessed L2PlayableInstance
		else if (isNoblesseBlessed()) {
			stopNoblesseBlessing(null);
			
			if (getCharmOfLuck()) //remove Lucky Charm if player have Nobless blessing buff
			{
				stopCharmOfLuck(null);
			}
		} else {
			boolean canStopEffects = !(this instanceof Player) || this instanceof Player;
			
			if (canStopEffects) {
				stopAllEffectsExceptThoseThatLastThroughDeath();
			}
		}
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform
		broadcastStatusUpdate();
		
		if (getWorldRegion() != null) {
			getWorldRegion().onDeath(this, killer);
		}
		
		// Notify Quest of Playable's death
		Player actingPlayer = getActingPlayer();
		if (!actingPlayer.isNotifyQuestOfDeathEmpty()) {
			for (QuestState qs : actingPlayer.getNotifyQuestOfDeath()) {
				qs.getQuest().notifyDeath(killer == null ? this : killer, this, qs);
			}
		}
		
		if (killer != null) {
			Player player = killer.getActingPlayer();
			if (player != null) {
				player.onKillUpdatePvPReputation(this);
			}
		}
		
		// Notify Creature AI
		getAI().notifyEvent(CtrlEvent.EVT_DEAD);
		
		return true;
	}
	
	public boolean checkIfPvP(Creature target) {
		if (target == null) {
			return false; // Target is null
		}
		if (target == this) {
			return false; // Target is self
		}
		if (!(target instanceof Playable)) {
			return false; // Target is not a L2PlayableInstance
		}
		
		Player player = null;
		if (this instanceof Player) {
			player = (Player) this;
		} else if (this instanceof Summon && !(this instanceof MobSummonInstance)) {
			player = ((Summon) this).getOwner();
		}
		
		if (player == null) {
			return false; // Active player is null
		}
		if (player.getReputation() < 0) {
			return false; // Active player has karma
		}
		
		Player targetPlayer = null;
		if (target instanceof Player) {
			targetPlayer = (Player) target;
		} else if (target instanceof Summon) {
			targetPlayer = ((Summon) target).getOwner();
		}
		
		if (targetPlayer == null) {
			return false; // Target player is null
		}
		if (targetPlayer == this) {
			return false; // Target player is self
		}
		if (targetPlayer.getReputation() < 0) {
			return false; // Target player has karma
		}
		if (targetPlayer.getPvpFlag() == 0) {
			return false;
		}
		
		return !(targetPlayer.getPvpFlag() == 0 && !player.isInSameClanWar(targetPlayer));

		/*  Even at war, there should be PvP flag
        if (
				player.getClan() == null ||
				targetPlayer.getClan() == null ||
				(
						!targetPlayer.getClan().isAtWarWith(player.getClanId()) &&
						targetPlayer.getWantsPeace() == 0 &&
						player.getWantsPeace() == 0
				)
			)
		{
			return true;
		}

		return false;
		 */
	}
	
	@Override
	public boolean isAttackable() {
		return true;
	}
	
	// Support for Noblesse Blessing skill, where buffs are retained
	// after resurrect
	public final boolean isNoblesseBlessed() {
		return effects.isAffected(EffectType.NOBLESSE_BLESSING.getMask()) && !getActingPlayer().getIsInsideGMEvent();
	}
	
	public final void stopNoblesseBlessing(Abnormal effect) {
		if (effect == null) {
			stopEffects(EffectType.NOBLESSE_BLESSING);
		} else {
			removeEffect(effect);
		}
		updateAbnormalEffect();
	}
	
	// Support for Soul of the Phoenix and Salvation skills
	public final boolean isPhoenixBlessed() {
		return effects.isAffected(EffectType.PHOENIX_BLESSING.getMask());
	}
	
	public final void stopPhoenixBlessing(Abnormal effect) {
		if (effect == null) {
			stopEffects(EffectType.PHOENIX_BLESSING);
		} else {
			removeEffect(effect);
		}
		
		updateAbnormalEffect();
	}
	
	/**
	 * Return True if the Silent Moving mode is active.<BR><BR>
	 */
	public boolean isSilentMoving() {
		return effects.isAffected(EffectType.SILENT_MOVE.getMask());
	}
	
	// for Newbie Protection Blessing skill, keeps you safe from an attack by a chaotic character >= 10 levels apart from you
	public final boolean getProtectionBlessing() {
		return effects.isAffected(EffectType.PROTECTION_BLESSING.getMask());
	}
	
	public void stopProtectionBlessing(Abnormal effect) {
		if (effect == null) {
			stopEffects(EffectType.PROTECTION_BLESSING);
		} else {
			removeEffect(effect);
		}
		
		updateAbnormalEffect();
	}
	
	//Charm of Luck - During a Raid/Boss war, decreased chance for death penalty
	public final boolean getCharmOfLuck() {
		return effects.isAffected(EffectType.CHARM_OF_LUCK.getMask());
	}
	
	public final void stopCharmOfLuck(Abnormal effect) {
		if (effect == null) {
			stopEffects(EffectType.CHARM_OF_LUCK);
		} else {
			removeEffect(effect);
		}
		
		updateAbnormalEffect();
	}
	
	@Override
	public void updateEffectIcons(boolean partyOnly) {
		effects.updateEffectIcons(partyOnly);
	}
	
	public boolean isLockedTarget() {
		return lockedTarget != null;
	}
	
	public Creature getLockedTarget() {
		return lockedTarget;
	}
	
	public void setLockedTarget(Creature cha) {
		lockedTarget = cha;
	}
	
	Player transferDmgTo;
	
	public void setTransferDamageTo(Player val) {
		transferDmgTo = val;
	}
	
	public Player getTransferingDamageTo() {
		return transferDmgTo;
	}
	
	public abstract int getReputation();
	
	public abstract byte getPvpFlag();
	
	public abstract boolean useMagic(Skill skill, boolean forceUse, boolean dontMove);
}
