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

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Duel;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.util.Util;

public class SummonStatus extends PlayableStatus
{
	public SummonStatus(L2Summon activeChar)
	{
		super(activeChar);
	}

	@Override
	public void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true, false, false);
	}

	@Override
	public void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isHPConsumption)
	{
		if (attacker == null || getActiveChar().isDead())
		{
			return;
		}

		final L2PcInstance attackerPlayer = attacker.getActingPlayer();
		if (attackerPlayer != null && (getActiveChar().getOwner() == null ||
				getActiveChar().getOwner().getDuelId() != attackerPlayer.getDuelId()))
		{
			attackerPlayer.setDuelState(Duel.DUELSTATE_INTERRUPTED);
		}

		if (getActiveChar().getOwner().getParty() != null)
		{
			final L2PcInstance caster = getActiveChar().getTransferingDamageTo();
			if (caster != null && getActiveChar().getParty() != null &&
					Util.checkIfInRange(1000, getActiveChar(), caster, true) && !caster.isDead() &&
					getActiveChar().getOwner() != caster &&
					getActiveChar().getParty().getPartyMembers().contains(caster))
			{
				int transferDmg = 0;

				transferDmg = (int) value *
						(int) getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_TO_PLAYER, 0, null, null) / 100;
				transferDmg = Math.min((int) caster.getCurrentHp() - 1, transferDmg);
				if (transferDmg > 0 && attacker instanceof L2Playable)
				{
					int membersInRange = 0;
					for (L2PcInstance member : caster.getParty().getPartyMembers())
					{
						if (Util.checkIfInRange(1000, member, caster, false) && member != caster)
						{
							membersInRange++;
						}
					}
					if (caster.getCurrentCp() > 0)
					{
						if (caster.getCurrentCp() > transferDmg)
						{
							reduceCp(transferDmg);
						}
						else
						{
							transferDmg = (int) (transferDmg - caster.getCurrentCp());
							reduceCp((int) caster.getCurrentCp());
						}
					}

					if (membersInRange > 0)
					{
						caster.reduceCurrentHp(transferDmg / membersInRange, attacker, null);
					}
					value -= transferDmg;
				}
			}
		}

		super.reduceHp(value, attacker, awake, isDOT, isHPConsumption);
	}

	@Override
	public L2Summon getActiveChar()
	{
		return (L2Summon) super.getActiveChar();
	}
}
