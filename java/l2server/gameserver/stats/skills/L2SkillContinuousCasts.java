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

package l2server.gameserver.stats.skills;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.handler.SkillHandler;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.MagicSkillLaunched;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.templates.StatsSet;
import l2server.util.Point3D;

public class L2SkillContinuousCasts extends L2Skill
{
	private final int _skillId;
	private final int _skillLvl;
	private final int _skillEnchantRoute;
	private final int _skillEnchantLvl;
	private final int _castAmount;

	public L2SkillContinuousCasts(StatsSet set)
	{
		super(set);

		_skillId = set.getInteger("castId", 0);
		_skillLvl = set.getInteger("castLvl", 0);
		_skillEnchantRoute = set.getInteger("castEnchantRoute", 0);
		_skillEnchantLvl = set.getInteger("castEnchantLvl", 0);
		_castAmount = set.getInteger("castAmount", 0);
	}

	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		Point3D position = activeChar.getSkillCastPosition();
		ThreadPoolManager.getInstance().scheduleEffect(new CastTask(activeChar, position), 1000);
	}

	private class CastTask implements Runnable
	{
		private int _count = _castAmount;
		L2Character _activeChar;
		Point3D _position;

		public CastTask(L2Character activeChar, Point3D position)
		{
			_activeChar = activeChar;
			_position = position;
		}

		@Override
		public void run()
		{
			if (!cast(_activeChar, _position))
			{
				return;
			}

			if (_count > 0)
			{
				ThreadPoolManager.getInstance().scheduleEffect(this, 1000);
			}

			_count--;
		}
	}

	public boolean cast(L2Character activeChar, Point3D position)
	{
		if (activeChar.isAlikeDead() || !activeChar.isCastingNow() || activeChar.getLastSkillCast() != this)
		{
			return false;
		}

		L2Object[] targets = getTargetList(activeChar, false, activeChar);
		activeChar.broadcastPacket(new MagicSkillLaunched(activeChar, getDisplayId(), getLevelHash(), targets));
		for (L2Object targetObj : targets)
		{
			if (!(targetObj instanceof L2Character))
			{
				continue;
			}

			L2Character target = (L2Character) targetObj;
			if (target.isDead())
			{
				continue;
			}

			int level = _skillLvl;
			if (level == 0)
			{
				level = 1;
				for (L2Abnormal e : target.getAllEffects())
				{
					if (e == null)
					{
						continue;
					}

					if (e.getSkill().getId() == _skillId)
					{
						int maxLevel = SkillTable.getInstance().getMaxLevel(e.getSkill().getId());
						if (e.getSkill().getLevel() < maxLevel)
						{
							level = e.getSkill().getLevel() + 1;
						}
						else
						{
							level = maxLevel;
						}
					}
				}
			}

			L2Skill skillToCast =
					SkillTable.getInstance().getInfo(_skillId, level, _skillEnchantRoute, _skillEnchantLvl);
			//System.out.println(targetObj + " " + level);
			if (!skillToCast.checkCondition(activeChar, target, false))
			{
				activeChar.sendMessage("Can't reach " + target);
				continue;
			}

			if (activeChar instanceof L2PcInstance && !((L2PcInstance) activeChar).checkPvpSkill(target, skillToCast))
			{
				continue;
			}

			L2Object[] subTargets = skillToCast.getTargetList(activeChar, false, target);
			if (subTargets.length == 0)
			{
				activeChar.sendMessage("No target found");
				continue;
			}

			L2Character firstTarget = (L2Character) subTargets[0];
			ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skillToCast.getSkillType());
			activeChar.broadcastPacket(
					new MagicSkillUse(activeChar, firstTarget, skillToCast.getDisplayId(), skillToCast.getLevelHash(),
							0, 0, 0));

			// Launch the magic skill and calculate its effects
			// TODO: once core will support all possible effects, use effects (not handler)
			if (handler != null)
			{
				handler.useSkill(activeChar, skillToCast, subTargets);
			}
			else
			{
				skillToCast.useSkill(activeChar, subTargets);
			}
		}

		activeChar.broadcastPacket(
				new MagicSkillLaunched(activeChar, getDisplayId(), getLevelHash(), new L2Character[]{activeChar}));

		if (activeChar instanceof L2PcInstance)
		{
			((L2PcInstance) activeChar).rechargeAutoSoulShot(false, true, false);
		}

		return true;
	}
}
