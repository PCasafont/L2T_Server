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
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2EffectPointInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;

/**
 * @author Forsaiken
 */
public class EffectSignetAntiSummon extends L2Effect
{
	private L2EffectPointInstance _actor;

	public EffectSignetAntiSummon(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.SIGNET_GROUND;
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

		_actor = (L2EffectPointInstance) getEffected();
		return true;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		if (getAbnormal().getCount() == getAbnormal().getTotalCount() - 1)
		{
			return true; // do nothing first time
		}

		int mpConsume = getSkill().getMpConsume();

		L2PcInstance caster = (L2PcInstance) getEffector();

		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (cha == null)
			{
				continue;
			}

			if (cha instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) cha;
				if (!player.isInsideZone(L2Character.ZONE_PVP) && player.getPvpFlag() == 0)
				{
					continue;
				}
			}

			if (cha instanceof L2Playable)
			{
				if (caster.canAttackCharacter(cha))
				{
					L2PcInstance owner = null;
					if (cha instanceof L2Summon)
					{
						owner = ((L2Summon) cha).getOwner();
					}
					else
					{
						owner = (L2PcInstance) cha;
					}

					if (owner != null && (owner.getPet() != null || !owner.getSummons().isEmpty()))
					{
						if (mpConsume > getEffector().getCurrentMp())
						{
							getEffector().sendPacket(
									SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
							return false;
						}
						else
						{
							getEffector().reduceCurrentMp(mpConsume);
						}

						if (owner.getPet() != null)
						{
							owner.getPet().unSummon(owner);
						}
						for (L2SummonInstance summon : owner.getSummons())
						{
							summon.unSummon(owner);
						}
						owner.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEffector());
					}
				}
			}
		}
		return true;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onExit()
	 */
	@Override
	public void onExit()
	{
		if (_actor != null)
		{
			_actor.deleteMe();
		}
	}
}
