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
import l2server.gameserver.stats.VisualEffect;
import l2server.gameserver.templates.skills.L2EffectTemplate;
import l2server.gameserver.util.Util;

/**
 * @author Erlandys
 */

public class EffectPartyMpConsumePerLevel extends L2Effect
{
	public EffectPartyMpConsumePerLevel(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onStart()
	{
		if (getEffector() == getEffected())
		{
			if (getEffector().getParty() != null)
			{
				for (L2PcInstance member : getEffector().getParty().getPartyMembers())
				{
					if (!Util.checkIfInRange(700, getEffector(), member, false))
					{
						continue;
					}
					if (member.getObjectId() != getEffector().getObjectId())
					{
						int skillId = getSkill().getId();
						int skillLvl = 1;
						int skillEnchantRoute = 0;
						int skillEnchantLvl = 0;
						if (getSkill().getPartyChangeSkill() != -1)
						{
							skillId = getSkill().getPartyChangeSkill();
							skillLvl = getSkill().getPartyChangeSkillLevel();
							skillEnchantRoute = getSkill().getPartyChangeSkillEnchantRoute();
							skillEnchantLvl = getSkill().getPartyChangeSkillEnchantLevel();
						}

						SkillTable.getInstance().getInfo(skillId, skillLvl, skillEnchantRoute, skillEnchantLvl)
								.getEffects(getEffector(), member);
					}
					member.updateEffectIcons();
				}
			}
			/*else
            {
				if (getEffector() instanceof L2PcInstance)
				{
					for (L2SummonInstance summon : ((L2PcInstance)getEffector()).getSummons())
					{
						if (summon != null)
						{
							int newSkillId = getSkill().getPartyChangeSkill() == -1 ? getSkill().getId() : getSkill().getPartyChangeSkill();

							SkillTable.getInstance().getInfo(newSkillId, 1).getEffects(summon, summon);

							summon.updateEffectIcons();
						}
					}
				}
			}*/
		}
		getEffector().updateEffectIcons();
		return super.onStart();
	}

	@Override
	public void onExit()
	{
		if (getEffector() == getEffected())
		{
			if (getEffector().getParty() != null)
			{
				for (L2PcInstance member : getEffector().getParty().getPartyMembers())
				{
					if (member == null)
					{
						continue;
					}

					if (member.getObjectId() != getEffector().getObjectId())
					{
						int newSkillId = getSkill().getPartyChangeSkill() == -1 ? getSkill().getId() :
								getSkill().getPartyChangeSkill();
						L2Abnormal removingEffect = member.getFirstEffect(newSkillId);

						if (removingEffect == null)
						{
							continue;
						}

						if (removingEffect.getTemplate().visualEffect != null)
						{
							for (VisualEffect ve : removingEffect.getTemplate().visualEffect)
							{
								member.stopVisualEffect(ve);
							}
						}
						member.removeEffect(removingEffect);
					}
				}
			}
		}
		super.onExit();
	}

	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
		{
			super.onExit();
			return false;
		}

		if (getEffector() != getEffected())
		{
			if (getEffector() == null || getEffected().getParty() == null)
			{
				return false;
			}

			if (!getEffected().getParty().getPartyMembers().contains(getEffector()))
			{
				return false;
			}

			return Util.checkIfInRange(700, getEffector(), getEffected(), false);

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

			// To check if party member have toggle.
			if (getEffector() == getEffected())
			{
				if (getEffector().getParty() != null)
				{
					for (L2PcInstance member : getEffector().getParty().getPartyMembers())
					{
						if (member.getObjectId() == getEffector().getObjectId())
						{
							continue;
						}

						if (!Util.checkIfInRange(700, getEffector(), member, false))
						{
							continue;
						}

						int skillId = getSkill().getId();
						int skillLvl = 1;
						int skillEnchantRoute = 0;
						int skillEnchantLvl = 0;
						if (getSkill().getPartyChangeSkill() != -1)
						{
							skillId = getSkill().getPartyChangeSkill();
							skillLvl = getSkill().getPartyChangeSkillLevel();
							skillEnchantRoute = getSkill().getPartyChangeSkillEnchantRoute();
							skillEnchantLvl = getSkill().getPartyChangeSkillEnchantLevel();
						}

						if (member.getFirstEffect(skillId) == null)
						{
							SkillTable.getInstance().getInfo(skillId, skillLvl, skillEnchantRoute, skillEnchantLvl)
									.getEffects(getEffector(), member);
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
