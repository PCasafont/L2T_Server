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
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2EffectTemplate;
import l2server.gameserver.util.Util;

public class EffectAuraForces extends L2Effect
{
	public EffectAuraForces(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onStart()
	{
		// Just do check on start if its not force and party solidarity, but its AURA
		if (getSkill().getId() != 1955 && getSkill().getName().contains("Aura"))
		{
			// Do check for players party if party exists give members (and you too) the force, if member is in range
			if (getEffector().getParty() != null)
			{
				for (L2PcInstance member : getEffector().getParty().getPartyMembers())
				{
					if (!Util.checkIfInRange(getSkill().getSkillRadius(), getEffector(), member, false))
					{
						continue;
					}

					int newSkillId = getSkill().getPartyChangeSkill() == -1 ? getSkill().getId() :
							getSkill().getPartyChangeSkill();

					SkillTable.getInstance().getInfo(newSkillId, 1).getEffects(getEffector(), member);

					if (member.getActiveForcesCount() + 1 <= 7)
					{
						member.setActiveForcesCount(member.getActiveForcesCount() + 1);
					}
					else
					{
						member.setActiveForcesCount(7);
					}

					int aurasCount = member.getActiveForcesCount();
					if (aurasCount > 3)
					{
						int level = Math.max(aurasCount - 4, 1);
						SkillTable.getInstance().getInfo(1955, level).getEffects(getEffector(), member);
					}
					member.updateEffectIcons();
				}
			}
			else
			{
				int newSkillId =
						getSkill().getPartyChangeSkill() == -1 ? getSkill().getId() : getSkill().getPartyChangeSkill();
				SkillTable.getInstance().getInfo(newSkillId, 1).getEffects(getEffector(), getEffector());

				if (getEffector().getActingPlayer().getActiveForcesCount() + 1 <= 7)
				{
					getEffector().getActingPlayer()
							.setActiveForcesCount(getEffector().getActingPlayer().getActiveForcesCount() + 1);
				}
				else
				{
					getEffector().getActingPlayer().setActiveForcesCount(7);
				}

				int aurasCount = getEffector().getActingPlayer().getActiveForcesCount();
				if (aurasCount > 3)
				{
					int level = Math.max(aurasCount - 4, 1);
					SkillTable.getInstance().getInfo(1955, level)
							.getEffects(getEffector(), getEffector().getActingPlayer());
				}
				getEffector().getActingPlayer().updateEffectIcons();
			}
		}
		getEffector().updateEffectIcons();
		return super.onStart();
	}

	@Override
	public void onExit()
	{
		// Do the simple check just like in onStart
		if (getSkill().getId() != 1955 && getSkill().getName().contains("Aura"))
		{
			if (getEffector().getParty() != null)
			{
				for (L2PcInstance member : getEffector().getParty().getPartyMembers())
				{
					if (member == null)
					{
						continue;
					}

					int newSkillId = getSkill().getPartyChangeSkill() == -1 ? getSkill().getId() :
							getSkill().getPartyChangeSkill();

					L2Abnormal abn = member.getFirstEffect(1955);

					for (L2Abnormal effect : member.getAllEffects())
					{
						if (effect == null)
						{
							continue;
						}
						if (effect.getSkill().getId() == newSkillId)
						{
							member.removeEffect(effect);
						}
					}

					if (member.getActiveForcesCount() - 1 >= 0)
					{
						member.setActiveForcesCount(member.getActiveForcesCount() - 1);
					}
					else
					{
						member.setActiveForcesCount(0);
					}

					int aurasCount = member.getActiveForcesCount();
					if (aurasCount > 3)
					{
						int level = Math.max(aurasCount - 4, 1);
						if (abn == null || abn.getLevelHash() != level)
						{
							SkillTable.getInstance().getInfo(1955, level).getEffects(getEffector(), member);
						}
					}
					else
					{
						if (abn != null)
						{
							abn.exit();
						}
					}
				}
			}
			else
			{
				int newSkillId =
						getSkill().getPartyChangeSkill() == -1 ? getSkill().getId() : getSkill().getPartyChangeSkill();

				L2Abnormal abn = getEffected().getActingPlayer().getFirstEffect(1955);
				L2Abnormal ab = getEffector().getFirstEffect(newSkillId);
				if (ab != null)
				{
					ab.exit();
				}

				if (getEffected().getActingPlayer().getActiveForcesCount() - 1 >= 0)
				{
					getEffected().getActingPlayer()
							.setActiveForcesCount(getEffected().getActingPlayer().getActiveForcesCount() - 1);
				}
				else
				{
					getEffected().getActingPlayer().setActiveForcesCount(0);
				}

				int aurasCount = getEffected().getActingPlayer().getActiveForcesCount();
				if (aurasCount > 3)
				{
					int level = Math.max(aurasCount - 4, 1);
					if (abn == null || abn.getLevelHash() != level)
					{
						SkillTable.getInstance().getInfo(1955, level)
								.getEffects(getEffector(), getEffected().getActingPlayer());
					}
				}
				else if (abn != null)
				{
					abn.exit();
				}
			}
		}
		else if (getSkill().getName()
				.contains("Force")) // this is needed if player removes Force the force counter must decrease too.
		{
			L2Abnormal abn = getEffected().getActingPlayer().getFirstEffect(1955);

			if (getEffected().getActingPlayer().getActiveForcesCount() - 1 >= 0)
			{
				getEffected().getActingPlayer()
						.setActiveForcesCount(getEffected().getActingPlayer().getActiveForcesCount() - 1);
			}
			else
			{
				getEffected().getActingPlayer().setActiveForcesCount(0);
			}

			int aurasCount = getEffected().getActingPlayer().getActiveForcesCount();
			if (aurasCount > 3)
			{
				int level = Math.max(aurasCount - 4, 1);
				if (abn == null || abn.getLevel() != level)
				{
					SkillTable.getInstance().getInfo(1955, level)
							.getEffects(getEffector(), getEffected().getActingPlayer());
				}
			}
			else if (abn != null)
			{
				abn.exit();
			}
		}
		super.onExit();
	}

	/**
	 */
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
		{
			super.onExit();
			return false;
		}

		// Do check if this skill is Force or Solidarity just do check for range things
		if (getSkill().getId() == 1955 || getSkill().getName().contains("Force"))
		{
			if (getEffector() == null || getEffected().getParty() == null && getEffected() != getEffector())
			{
				return false;
			}

			if (getEffected().getParty() != null)
			{
				if (!getEffected().getParty().getPartyMembers().contains(getEffector()))
				{
					return false;
				}
			}

			return Util.checkIfInRange(getSkill().getSkillRadius(), getEffector(), getEffected(), false);

		}
		else
		{
			double base = calc();
			double consume = (getEffected().getLevel() - 1) / 7.5 * base * getAbnormal().getDuration();

			if (consume > getEffected().getCurrentMp())
			{
				getEffected().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
				return false;
			}

			getEffected().reduceCurrentMp(consume);

			// Then for player who have Aura (the effector) finishes the time (onActionTime), must be check for all party if all of them have Force.
			if (getEffector().getParty() != null)
			{
				for (L2PcInstance member : getEffector().getParty().getPartyMembers())
				{
					if (member.getObjectId() == getEffector().getObjectId())
					{
						continue;
					}
					if (!Util.checkIfInRange(getSkill().getSkillRadius(), getEffector(), member, false))
					{
						continue;
					}
					int newSkillId = getSkill().getPartyChangeSkill() == -1 ? getSkill().getId() :
							getSkill().getPartyChangeSkill();
					if (member.getFirstEffect(newSkillId) == null)
					{
						SkillTable.getInstance().getInfo(newSkillId, 1).getEffects(getEffector(), member);
						if (member.getActiveForcesCount() + 1 <= 7)
						{
							member.setActiveForcesCount(member.getActiveForcesCount() + 1);
						}
						else
						{
							member.setActiveForcesCount(7);
						}
						int aurasCount = member.getActiveForcesCount();
						if (aurasCount > 3)
						{
							int level = Math.max(aurasCount - 4, 1);
							SkillTable.getInstance().getInfo(1955, level).getEffects(getEffector(), member);
						}
						member.updateEffectIcons();
					}
				}
			}
		}
		getEffected().updateEffectIcons();
		return true;
	}
}
