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

import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.skills.L2EffectTemplate;

/**
 * @author Ahmed
 *         <p>
 *         This is the Effect support for spoil.
 *         <p>
 *         This was originally done by _drunk_
 */
public class EffectMug extends L2Effect
{
	public EffectMug(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		if (!(getEffector() instanceof L2PcInstance))
		{
			return false;
		}

		if (!(getEffected() instanceof L2MonsterInstance))
		{
			return false;
		}

		L2MonsterInstance target = (L2MonsterInstance) getEffected();

		if (target == null)
		{
			return false;
		}

		if (target.isSpoil())
		{
			getEffector().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_SPOILED));
			return false;
		}

		// SPOIL SYSTEM by Lbaldi
		boolean spoil = false;
		if (!target.isDead())
		{
			spoil = Formulas.calcMagicSuccess(getEffector(), target, getSkill());

			if (spoil)
			{
				target.setSpoil(true);
				target.setIsSpoiledBy(getEffector().getObjectId());
			}
			target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEffector());
		}
		return true;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
