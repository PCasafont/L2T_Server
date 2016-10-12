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

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2EffectPointInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.skills.L2SkillSignet;
import l2server.gameserver.stats.skills.L2SkillSignetCasttime;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;

import java.util.ArrayList;

/**
 * @authors Forsaiken, Sami
 */
public class EffectSignet extends L2Effect
{
	private L2Skill _skill;
	private L2EffectPointInstance _actor;
	private boolean _srcInArena;

	public EffectSignet(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.SIGNET_EFFECT;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		if (getSkill() instanceof L2SkillSignet)
		{
			_skill = SkillTable.getInstance()
					.getInfo(((L2SkillSignet) getSkill())._effectId, ((L2SkillSignet) getSkill())._effectLevel);
		}
		else if (getSkill() instanceof L2SkillSignetCasttime)
		{
			_skill = SkillTable.getInstance().getInfo(((L2SkillSignetCasttime) getSkill()).effectId, getLevel());
		}
		_actor = (L2EffectPointInstance) getEffected();
		_srcInArena =
				getEffector().isInsideZone(L2Character.ZONE_PVP) && !getEffector().isInsideZone(L2Character.ZONE_SIEGE);
		return true;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		if (_skill == null)
		{
			return true;
		}

		int mpConsume = _skill.getMpConsume();

		if (mpConsume > getEffector().getCurrentMp())
		{
			getEffector().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
			return false;
		}
		else
		{
			getEffector().reduceCurrentMp(mpConsume);
		}

		boolean signetActor = calc() != 0;

		final ArrayList<L2Character> targets = new ArrayList<>();
		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (cha == null)
			{
				continue;
			}

			if (_skill.isOffensive() && !L2Skill.checkForAreaOffensiveSkills(getEffector(), cha, _skill, _srcInArena))
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

			// there doesn't seem to be a visible effect with MagicSkillLaunched packet...
			if (!signetActor)
			{
				_actor.broadcastPacket(new MagicSkillUse(_actor, cha, _skill.getId(), _skill.getLevelHash(), 0, 0, 0));
			}
			targets.add(cha);
		}

		if (signetActor)
		{
			//_actor.broadcastPacket(new TargetSelected(_actor.getObjectId(), _actor.getObjectId(), _actor.getX(), _actor.getY(), _actor.getZ()));
			_actor.broadcastPacket(new MagicSkillUse(_actor, _skill.getId(), _skill.getLevelHash(), 0, 0));
			//_actor.broadcastPacket(new MagicSkillLaunched(_actor, _skill.getId(), _skill.getLevel(), targets.toArray(new L2Character[targets.size()])));
		}

		if (!targets.isEmpty())
		{
			if (!signetActor)
			{
				getEffector().callSkill(_skill, targets.toArray(new L2Character[targets.size()]));
			}
			else
			{
				_actor.callSkill(_skill, targets.toArray(new L2Character[targets.size()]));
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
