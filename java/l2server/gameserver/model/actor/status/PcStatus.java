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

package l2server.gameserver.model.actor.status;

import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.instancemanager.DuelManager;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2MobSummonInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.model.actor.stat.PcStat;
import l2server.gameserver.model.entity.Duel;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.StatusUpdate.StatusUpdateDisplay;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.BaseStats;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

public class PcStatus extends PlayableStatus
{
	private double _currentCp = 0; //Current CP of the L2PcInstance

	public PcStatus(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	@Override
	public final void reduceCp(int value)
	{
		if (getCurrentCp() > value)
		{
			setCurrentCp(getCurrentCp() - value);
		}
		else
		{
			setCurrentCp(0);
		}
	}

	@Override
	public final void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true, false, false, false, false);
	}

	@Override
	public final void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isHPConsumption)
	{
		reduceHp(value, attacker, awake, isDOT, isHPConsumption, false, false);
	}

	public final void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isHPConsumption, boolean ignoreCP, boolean ignoreInvul)
	{
		if (getActiveChar().isDead())
		{
			return;
		}

		boolean isHide = getActiveChar().getAppearance().getInvisible();
		if (!isHPConsumption || isHide)
		{
			if (getActiveChar().isSitting())
			{
				getActiveChar().standUp();
			}

			if (!isDOT || isHide)
			{
				getActiveChar().stopEffectsOnDamage(awake, (int) value);

				if (getActiveChar().isStunned())
				{
					int baseBreakChance = attacker.getLevel() > 85 ? 5 : 25; // TODO Recheck this
					double breakChance = baseBreakChance * Math.sqrt(BaseStats.CON.calcBonus(getActiveChar()));

					if (value > 2000)
					{
						breakChance *= 4;
					}
					else if (value > 1000)
					{
						breakChance *= 2;
					}
					else if (value > 500)
					{
						breakChance *= 1.5;
					}

					if (value > 100 && Rnd.get(100) < breakChance)
					{
						getActiveChar().stopStunning(true);
					}
				}
			}
		}

		if (getActiveChar().isInvul(attacker))
		{
			//if (attacker == getActiveChar())
			{
				if (!isDOT && !isHPConsumption && !ignoreInvul)
				{
					return;
				}
			}
			//else
			//	return;
		}

		if (getActiveChar().getFaceoffTarget() != null && attacker != getActiveChar().getFaceoffTarget() &&
				attacker != getActiveChar())
		{
			return;
		}

		int fullValue = (int) value;
		int tDmg = 0;
		int mpDam = 0;

		if (attacker != null && attacker != getActiveChar())
		{
			final L2PcInstance attackerPlayer = attacker.getActingPlayer();

			if (attackerPlayer != null)
			{
				if (attackerPlayer.isGM() && !attackerPlayer.getAccessLevel().canGiveDamage())
				{
					return;
				}

				if (getActiveChar().isInDuel())
				{
					if (getActiveChar().getDuelState() == Duel.DUELSTATE_DEAD)
					{
						return;
					}
					else if (getActiveChar().getDuelState() == Duel.DUELSTATE_WINNER)
					{
						return;
					}

					// cancel duel if player got hit by another player, that is not part of the duel
					if (attackerPlayer.getDuelId() != getActiveChar().getDuelId())
					{
						getActiveChar().setDuelState(Duel.DUELSTATE_INTERRUPTED);
					}
				}
			}

			int dmgCap = (int) getActiveChar().getStat().calcStat(Stats.DAMAGE_CAP, 0, null, null);
			if (dmgCap > 0 && value > dmgCap)
			{
				value = dmgCap;
				fullValue = dmgCap;
			}

			// Check and calculate transfered damage
			L2SummonInstance summon = getActiveChar().getSummon(0);
			if (summon != null && !(summon instanceof L2MobSummonInstance) &&
					Util.checkIfInRange(1000, getActiveChar(), summon, true))
			{
				tDmg = (int) value *
						(int) getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null) / 100;

				// Only transfer dmg up to current HP, it should not be killed
				tDmg = Math.min((int) summon.getCurrentHp() - 1, tDmg);
				if (tDmg > 0)
				{
					summon.reduceCurrentHp(tDmg, attacker, null);
					value -= tDmg;
					fullValue =
							(int) value; // reduce the announced value here as player will get a message about summon damage
				}
			}

			boolean manaBlock = false;
			int manaShield = (int) getActiveChar().getStat().calcStat(Stats.MANA_SHIELD_PERCENT, 0, null, null);
			if (manaShield > 100)
			{
				manaShield -= 100;
				manaBlock = true;
			}
			mpDam = (int) value * manaShield / 100;

			if (mpDam > 0)
			{
				if (mpDam > getActiveChar().getCurrentMp())
				{
					getActiveChar().sendPacket(
							SystemMessage.getSystemMessage(SystemMessageId.MP_BECAME_0_ARCANE_SHIELD_DISAPPEARING));
					L2Abnormal effect = getActiveChar().getFirstEffect(1556);
					if (effect != null)
					{
						effect.stopEffectTask();
					}
					else
					{
						getActiveChar().getFirstEffect(11065).stopEffectTask();
					}

					value -= getActiveChar().getCurrentMp();
					getActiveChar().setCurrentMp(0);
				}
				else
				{
					SystemMessage smsg = SystemMessage
							.getSystemMessage(SystemMessageId.ARCANE_SHIELD_DECREASED_YOUR_MP_BY_S1_INSTEAD_OF_HP);
					smsg.addNumber(mpDam);
					getActiveChar().sendPacket(smsg);
					value -= mpDam;
					getActiveChar().reduceCurrentMp(mpDam);
				}

				if (manaBlock)
				{
					value = 0;
				}

				fullValue = (int) value;
			}

			final L2PcInstance caster = getActiveChar().getTransferingDamageTo();
			if (caster != null && getActiveChar().getParty() != null &&
					Util.checkIfInRange(1000, getActiveChar(), caster, true) && !caster.isDead() &&
					getActiveChar() != caster && getActiveChar().getParty().getPartyMembers().contains(caster))
			{
				int transferDmg = 0;

				transferDmg = (int) value *
						(int) getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_TO_PLAYER, 0, null, null) / 100;
				transferDmg = Math.min((int) caster.getCurrentHp() - 1, transferDmg);
				if (transferDmg > 0)
				{
					if (attacker instanceof L2Playable && caster.getCurrentCp() > 0)
					{
						if (caster.getCurrentCp() > transferDmg)
						{
							caster.getStatus().reduceCp(transferDmg);
						}
						else
						{
							value -= caster.getCurrentCp();
							transferDmg -= caster.getCurrentCp();
							caster.getStatus().reduceCp((int) caster.getCurrentCp());
						}
					}

					caster.reduceCurrentHp(transferDmg, attacker, null);
					value -= transferDmg;
					fullValue = (int) value;
				}
			}

			if (!ignoreCP && attacker instanceof L2Playable)
			{
				if (getCurrentCp() >= value)
				{
					setCurrentCp(getCurrentCp() - value); // Set Cp to diff of Cp vs value
					value = 0; // No need to subtract anything from Hp
				}
				else
				{
					value -= getCurrentCp(); // Get diff from value vs Cp; will apply diff to Hp
					setCurrentCp(0, false); // Set Cp to 0
				}
			}

			if (fullValue > 0 && !isDOT)
			{
				SystemMessage smsg;
				// Send a System Message to the L2PcInstance
				smsg = SystemMessage.getSystemMessage(SystemMessageId.C1_RECEIVED_DAMAGE_OF_S3_FROM_C2);
				smsg.addCharName(getActiveChar());
				smsg.addCharName(attacker);
				smsg.addNumber(fullValue);
				smsg.addHpChange(getActiveChar().getObjectId(), attacker.getObjectId(), -fullValue);
				getActiveChar().sendPacket(smsg);

				if (tDmg > 0)
				{
					smsg = SystemMessage.getSystemMessage(SystemMessageId.C1_RECEIVED_DAMAGE_OF_S3_FROM_C2);
					smsg.addString(getActiveChar().getSummon(0).getName());
					smsg.addCharName(attacker);
					smsg.addNumber(tDmg);
					smsg.addHpChange(getActiveChar().getSummon(0).getObjectId(), attacker.getObjectId(), -tDmg);
					getActiveChar().sendPacket(smsg);

					if (attackerPlayer != null)
					{
						smsg = SystemMessage.getSystemMessage(
								SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR);
						smsg.addNumber(fullValue);
						smsg.addNumber(tDmg);
						smsg.addHpChange(getActiveChar().getObjectId(), attacker.getObjectId(), -fullValue);
						attackerPlayer.sendPacket(smsg);
					}
				}
			}

			if (attackerPlayer != null && getActiveChar().isInOlympiadMode())
			{
				attackerPlayer.setOlyGivenDmg(attackerPlayer.getOlyGivenDmg() + fullValue);
			}
		}

		StatusUpdateDisplay display = StatusUpdateDisplay.NONE;
		if (isDOT)
		{
			display = StatusUpdateDisplay.DOT;
		}

		if (value > 0)
		{
			value = getCurrentHp() - value;
			if (value <= 0)
			{
				if (getActiveChar().isInDuel())
				{
					getActiveChar().disableAllSkills();
					stopHpMpRegeneration();
					attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					attacker.sendPacket(ActionFailed.STATIC_PACKET);

					// let the DuelManager know of his defeat
					DuelManager.getInstance().onPlayerDefeat(getActiveChar());
					value = 1;
				}
				else if (getActiveChar().isMortal())
				{
					value = 0;
				}
				else
				{
					value = 1;
				}
			}

			setCurrentHp(value, true, attacker, display);
		}

		if (getActiveChar().getCurrentHp() < 0.5)
		{
			getActiveChar().abortAttack();
			getActiveChar().abortCast();

			if (getActiveChar().isInOlympiadMode())
			{
				stopHpMpRegeneration();
				getActiveChar().setIsDead(true);
				getActiveChar().setIsPendingRevive(true);
				if (getActiveChar().getPet() != null)
				{
					getActiveChar().getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
				}
				for (L2SummonInstance summon : getActiveChar().getSummons())
				{
					summon.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
				}
				return;
			}

			getActiveChar().doDie(attacker);
			if (!Config.DISABLE_TUTORIAL)
			{
				QuestState qs = getActiveChar().getQuestState("Q255_Tutorial");
				if (qs != null)
				{
					qs.getQuest().notifyEvent("CE30", null, getActiveChar());
				}
			}
		}
	}

	@Override
	public final void setCurrentHp(double newHp, boolean broadcastPacket)
	{
		super.setCurrentHp(newHp, broadcastPacket);

		if (!Config.DISABLE_TUTORIAL && getCurrentHp() <= getActiveChar().getStat().getMaxHp() * .3)
		{
			QuestState qs = getActiveChar().getQuestState("Q255_Tutorial");
			if (qs != null && qs.getQuest() != null)
			{
				qs.getQuest().notifyEvent("CE45", null, getActiveChar());
			}
		}
	}

	@Override
	public final double getCurrentCp()
	{
		return _currentCp;
	}

	@Override
	public final void setCurrentCp(double newCp)
	{
		setCurrentCp(newCp, true);
	}

	public final void setCurrentCp(double newCp, boolean broadcastPacket)
	{
		// Get the Max CP of the L2Character
		int maxCp = getActiveChar().getStat().getMaxCp();

		synchronized (this)
		{
			if (getActiveChar().isDead())
			{
				return;
			}

			if (newCp < 0)
			{
				newCp = 0;
			}

			if (newCp >= maxCp)
			{
				// Set the RegenActive flag to false
				_currentCp = maxCp;
				_flagsRegenActive &= ~REGEN_FLAG_CP;

				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				// Set the RegenActive flag to true
				_currentCp = newCp;
				_flagsRegenActive |= REGEN_FLAG_CP;

				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		if (broadcastPacket)
		{
			getActiveChar().broadcastStatusUpdate();
		}
	}

	@Override
	protected void doRegeneration()
	{
		final PcStat charstat = getActiveChar().getStat();

		// Modify the current CP of the L2Character and broadcast Server->Client packet StatusUpdate
		if (getCurrentCp() < charstat.getMaxCp())
		{
			setCurrentCp(getCurrentCp() + Formulas.calcCpRegen(getActiveChar()), false);
		}

		// Modify the current HP of the L2Character and broadcast Server->Client packet StatusUpdate
		if (getCurrentHp() < charstat.getMaxHp())
		{
			setCurrentHp(getCurrentHp() + Formulas.calcHpRegen(getActiveChar()), false);
		}

		// Modify the current MP of the L2Character and broadcast Server->Client packet StatusUpdate
		if (getCurrentMp() < charstat.getMaxMp())
		{
			setCurrentMp(getCurrentMp() + Formulas.calcMpRegen(getActiveChar()), false);
		}

		getActiveChar().broadcastStatusUpdate(); //send the StatusUpdate packet
	}

	@Override
	public L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}
}
