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

/*
  @author Forsaiken
 */

package l2server.gameserver.stats.effects;

import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2EffectPointInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.MagicSkillLaunched;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.skills.L2SkillSignetCasttime;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.util.Point3D;

import java.util.ArrayList;

public class EffectSignetMDam extends L2Effect
{
	private L2EffectPointInstance _actor;

	public EffectSignetMDam(Env env, L2EffectTemplate template)
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
		L2NpcTemplate template;
		if (getSkill() instanceof L2SkillSignetCasttime)
		{
			template = NpcTable.getInstance().getTemplate(((L2SkillSignetCasttime) getSkill())._effectNpcId);
		}
		else
		{
			return false;
		}

		L2EffectPointInstance effectPoint =
				new L2EffectPointInstance(IdFactory.getInstance().getNextId(), template, getEffector());
		effectPoint.setCurrentHp(effectPoint.getMaxHp());
		effectPoint.setCurrentMp(effectPoint.getMaxMp());
		//L2World.getInstance().storeObject(effectPoint);

		int x = getEffector().getX();
		int y = getEffector().getY();
		int z = getEffector().getZ();

		if (getEffector() instanceof L2PcInstance && getSkill().getTargetType() == L2SkillTargetType.TARGET_GROUND)
		{
			Point3D wordPosition = getEffector().getSkillCastPosition();

			if (wordPosition != null)
			{
				x = wordPosition.getX();
				y = wordPosition.getY();
				z = wordPosition.getZ();
			}
		}
		effectPoint.setIsInvul(true);
		effectPoint.spawnMe(x, y, z);

		_actor = effectPoint;
		return true;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		if (getAbnormal().getCount() >= getAbnormal().getTotalCount() - 2)
		{
			return true; // do nothing first 2 times
		}
		int mpConsume = getSkill().getMpConsume();

		L2PcInstance caster = (L2PcInstance) getEffector();

		double ssMul = L2ItemInstance.CHARGED_NONE;

		L2ItemInstance weaponInst = caster.getActiveWeaponInstance();
		if (weaponInst != null)
		{
			ssMul = weaponInst.getChargedSpiritShot();
			weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
		}

		ArrayList<L2Character> targets = new ArrayList<>();

		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (cha == null || cha == caster)
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

			if (cha instanceof L2Attackable || cha instanceof L2Playable)
			{
				if (cha.isAlikeDead())
				{
					continue;
				}

				if (mpConsume > caster.getCurrentMp())
				{
					caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
					return false;
				}
				else
				{
					caster.reduceCurrentMp(mpConsume);
				}

				if (cha instanceof L2Playable)
				{
					if (caster.canAttackCharacter(cha))
					{
						targets.add(cha);
						caster.updatePvPStatus(cha);
					}
				}
				else
				{
					targets.add(cha);
				}
			}
		}

		if (!targets.isEmpty())
		{
			caster.broadcastPacket(new MagicSkillLaunched(caster, getSkill().getId(), getSkill().getLevelHash(),
					targets.toArray(new L2Character[targets.size()])));
			for (L2Character target : targets)
			{
				boolean mcrit = Formulas.calcMCrit(caster.getMCriticalHit(target, getSkill()));
				byte shld = Formulas.calcShldUse(caster, target, getSkill());
				int mdam = (int) Formulas.calcMagicDam(caster, target, getSkill(), shld, ssMul, mcrit);

				if (target instanceof L2Summon)
				{
					target.broadcastStatusUpdate();
				}

				if (mdam > 0)
				{
					if (!target.isRaid() && Formulas.calcAtkBreak(target, mdam))
					{
						target.breakAttack();
						target.breakCast();
					}
					caster.sendDamageMessage(target, mdam, mcrit, false, false);
					target.reduceCurrentHp(mdam, caster, getSkill());
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, caster);
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
