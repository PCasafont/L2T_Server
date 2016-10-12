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
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.instance.L2MobSummonInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.knownlist.PlayableKnownList;
import l2server.gameserver.model.actor.stat.PlayableStat;
import l2server.gameserver.model.actor.status.PlayableStatus;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.templates.chars.L2CharTemplate;
import l2server.gameserver.templates.skills.L2EffectType;

/**
 * This class represents all Playable characters in the world.<BR><BR>
 * <p>
 * L2PlayableInstance :<BR><BR>
 * <li>L2PcInstance</li>
 * <li>L2Summon</li><BR><BR>
 */

public abstract class L2Playable extends L2Character
{
	private L2Character _lockedTarget = null;

	/**
	 * Constructor of L2PlayableInstance (use L2Character constructor).<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and link copy basic Calculator set to this L2PlayableInstance </li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2CharTemplate to apply to the L2PlayableInstance
	 */
	public L2Playable(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2Playable);
		setIsInvul(false);
	}

	@Override
	public PlayableKnownList getKnownList()
	{
		return (PlayableKnownList) super.getKnownList();
	}

	@Override
	public void initKnownList()
	{
		setKnownList(new PlayableKnownList(this));
	}

	@Override
	public PlayableStat getStat()
	{
		return (PlayableStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		setStat(new PlayableStat(this));
	}

	@Override
	public PlayableStatus getStatus()
	{
		return (PlayableStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		setStatus(new PlayableStatus(this));
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		// killing is only possible one time
		synchronized (this)
		{
			if (isDead())
			{
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

		// Stop all active skills effects in progress on the L2Character,
		// if the Character isn't affected by Soul of The Phoenix or Salvation
		if (isPhoenixBlessed())
		{
			if (getCharmOfLuck()) //remove Lucky Charm if player has SoulOfThePhoenix/Salvation buff
			{
				stopCharmOfLuck(null);
			}
			if (isNoblesseBlessed())
			{
				stopNoblesseBlessing(null);
			}
		}
		// Same thing if the Character isn't a Noblesse Blessed L2PlayableInstance
		else if (isNoblesseBlessed())
		{
			stopNoblesseBlessing(null);

			if (getCharmOfLuck()) //remove Lucky Charm if player have Nobless blessing buff
			{
				stopCharmOfLuck(null);
			}
		}
		else
		{
			boolean canStopEffects = !(this instanceof L2PcInstance) || this instanceof L2PcInstance;

			if (canStopEffects)
			{
				stopAllEffectsExceptThoseThatLastThroughDeath();
			}
		}

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();

		if (getWorldRegion() != null)
		{
			getWorldRegion().onDeath(this, killer);
		}

		// Notify Quest of L2Playable's death
		L2PcInstance actingPlayer = getActingPlayer();
		if (!actingPlayer.isNotifyQuestOfDeathEmpty())
		{
			for (QuestState qs : actingPlayer.getNotifyQuestOfDeath())
			{
				qs.getQuest().notifyDeath(killer == null ? this : killer, this, qs);
			}
		}

		if (killer != null)
		{
			L2PcInstance player = killer.getActingPlayer();
			if (player != null)
			{
				player.onKillUpdatePvPReputation(this);
			}
		}

		// Notify L2Character AI
		getAI().notifyEvent(CtrlEvent.EVT_DEAD);

		return true;
	}

	public boolean checkIfPvP(L2Character target)
	{
		if (target == null)
		{
			return false; // Target is null
		}
		if (target == this)
		{
			return false; // Target is self
		}
		if (!(target instanceof L2Playable))
		{
			return false; // Target is not a L2PlayableInstance
		}

		L2PcInstance player = null;
		if (this instanceof L2PcInstance)
		{
			player = (L2PcInstance) this;
		}
		else if (this instanceof L2Summon && !(this instanceof L2MobSummonInstance))
		{
			player = ((L2Summon) this).getOwner();
		}

		if (player == null)
		{
			return false; // Active player is null
		}
		if (player.getReputation() < 0)
		{
			return false; // Active player has karma
		}

		L2PcInstance targetPlayer = null;
		if (target instanceof L2PcInstance)
		{
			targetPlayer = (L2PcInstance) target;
		}
		else if (target instanceof L2Summon)
		{
			targetPlayer = ((L2Summon) target).getOwner();
		}

		if (targetPlayer == null)
		{
			return false; // Target player is null
		}
		if (targetPlayer == this)
		{
			return false; // Target player is self
		}
		if (targetPlayer.getReputation() < 0)
		{
			return false; // Target player has karma
		}
		if (targetPlayer.getPvpFlag() == 0)
		{
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
	public boolean isAttackable()
	{
		return true;
	}

	// Support for Noblesse Blessing skill, where buffs are retained
	// after resurrect
	public final boolean isNoblesseBlessed()
	{
		return _effects.isAffected(L2EffectType.NOBLESSE_BLESSING.getMask()) && !getActingPlayer().getIsInsideGMEvent();
	}

	public final void stopNoblesseBlessing(L2Abnormal effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.NOBLESSE_BLESSING);
		}
		else
		{
			removeEffect(effect);
		}
		updateAbnormalEffect();
	}

	// Support for Soul of the Phoenix and Salvation skills
	public final boolean isPhoenixBlessed()
	{
		return _effects.isAffected(L2EffectType.PHOENIX_BLESSING.getMask());
	}

	public final void stopPhoenixBlessing(L2Abnormal effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.PHOENIX_BLESSING);
		}
		else
		{
			removeEffect(effect);
		}

		updateAbnormalEffect();
	}

	/**
	 * Return True if the Silent Moving mode is active.<BR><BR>
	 */
	public boolean isSilentMoving()
	{
		return _effects.isAffected(L2EffectType.SILENT_MOVE.getMask());
	}

	// for Newbie Protection Blessing skill, keeps you safe from an attack by a chaotic character >= 10 levels apart from you
	public final boolean getProtectionBlessing()
	{
		return _effects.isAffected(L2EffectType.PROTECTION_BLESSING.getMask());
	}

	/**
	 */
	public void stopProtectionBlessing(L2Abnormal effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.PROTECTION_BLESSING);
		}
		else
		{
			removeEffect(effect);
		}

		updateAbnormalEffect();
	}

	//Charm of Luck - During a Raid/Boss war, decreased chance for death penalty
	public final boolean getCharmOfLuck()
	{
		return _effects.isAffected(L2EffectType.CHARM_OF_LUCK.getMask());
	}

	public final void stopCharmOfLuck(L2Abnormal effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.CHARM_OF_LUCK);
		}
		else
		{
			removeEffect(effect);
		}

		updateAbnormalEffect();
	}

	@Override
	public void updateEffectIcons(boolean partyOnly)
	{
		_effects.updateEffectIcons(partyOnly);
	}

	public boolean isLockedTarget()
	{
		return _lockedTarget != null;
	}

	public L2Character getLockedTarget()
	{
		return _lockedTarget;
	}

	public void setLockedTarget(L2Character cha)
	{
		_lockedTarget = cha;
	}

	L2PcInstance transferDmgTo;

	public void setTransferDamageTo(L2PcInstance val)
	{
		transferDmgTo = val;
	}

	public L2PcInstance getTransferingDamageTo()
	{
		return transferDmgTo;
	}

	public abstract int getReputation();

	public abstract byte getPvpFlag();

	public abstract boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove);
}
