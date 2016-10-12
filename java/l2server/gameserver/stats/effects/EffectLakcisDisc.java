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

package l2server.gameserver.stats.effects;

import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.StatusUpdate.StatusUpdateDisplay;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;

public class EffectLakcisDisc extends L2Effect
{
	public EffectLakcisDisc(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.BUFF;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead() || !(getEffected() instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance player = (L2PcInstance) getEffected();

		double amount = calc();
		for (L2Character target : player.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (target.isAutoAttackable(player))
			{
				target.reduceCurrentHpByDOT(amount, getEffector(), getSkill());
			}
			else if (target.getActingPlayer() != null)
			{
				L2PcInstance targetPlayer = target.getActingPlayer();
				if (targetPlayer.isInParty() && targetPlayer.getParty() == player.getParty() ||
						target.getActingPlayer().getClanId() != 0 &&
								target.getActingPlayer().getClanId() == player.getClanId() ||
						target.getActingPlayer().getAllyId() != 0 &&
								target.getActingPlayer().getAllyId() == player.getAllyId())
				{
					double hp = target.getCurrentHp();
					double maxhp = target.getMaxHp();
					hp += amount;
					if (hp > maxhp)
					{
						hp = maxhp;
					}

					target.setCurrentHp(hp);
					StatusUpdate suhp = new StatusUpdate(target, getEffector(), StatusUpdateDisplay.NORMAL);
					suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
					target.sendPacket(suhp);
				}
			}
		}

		return true;
	}
}
