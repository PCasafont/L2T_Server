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

package l2server.gameserver.ai;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.HelperBuffTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.templates.L2HelperBuff;
import l2server.gameserver.templates.skills.L2SkillType;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manages AI of L2Attackable.<BR><BR>
 */
public class L2NewbieHelperAI extends L2CharacterAI implements Runnable
{
	private List<Integer> _alreadyBuffed = new ArrayList<>();

	public L2NewbieHelperAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 20000, 2000);
	}

	@Override
	public void run()
	{
		final L2Character npc = getActor();
		if (npc == null)
		{
			return;
		}

		if (npc.getKnownList().getKnownPlayersInRadius(200).isEmpty())
		{
			return;
		}

		for (L2Character activeChar : npc.getKnownList().getKnownCharacters())
		{
			if (activeChar == null || !(activeChar instanceof L2Playable) ||
					_alreadyBuffed.contains(activeChar.getObjectId()) && activeChar.getAllEffects().length > 0)
			{
				continue;
			}

			final L2Playable playable = (L2Playable) activeChar;

			if (!playable.isInsideRadius(npc, 200, true, false) || playable.getActingPlayer().isCursedWeaponEquipped())
			{
				continue;
			}

			int player_level = playable.getLevel();
			int lowestLevel = 0;
			int highestLevel = 0;

			npc.setTarget(playable);

			if (playable instanceof L2Summon)
			{
				lowestLevel = HelperBuffTable.getInstance().getServitorLowestLevel();
				highestLevel = HelperBuffTable.getInstance().getServitorHighestLevel();
			}
			else
			{
				// 	Calculate the min and max level between which the player must be to obtain buff
				if (playable.getActingPlayer().isMageClass())
				{
					lowestLevel = HelperBuffTable.getInstance().getMagicClassLowestLevel();
					highestLevel = HelperBuffTable.getInstance().getMagicClassHighestLevel();
				}
				else
				{
					lowestLevel = HelperBuffTable.getInstance().getPhysicClassLowestLevel();
					highestLevel = HelperBuffTable.getInstance().getPhysicClassHighestLevel();
				}
			}

			if (player_level > highestLevel || player_level < lowestLevel)
			{
				continue;
			}

			L2Skill skill = null;
			if (playable instanceof L2Summon)
			{
				for (L2HelperBuff helperBuffItem : HelperBuffTable.getInstance().getHelperBuffTable())
				{
					if (helperBuffItem.isForSummon())
					{
						skill = SkillTable.getInstance()
								.getInfo(helperBuffItem.getSkillID(), helperBuffItem.getSkillLevel());
						if (skill != null)
						{
							npc.doCast(skill);
						}
					}
				}
			}
			else
			{
				// 	Go through the Helper Buff list define in sql table helper_buff_list and cast skill
				for (L2HelperBuff helperBuffItem : HelperBuffTable.getInstance().getHelperBuffTable())
				{
					if (helperBuffItem.isMagicClassBuff() == playable.getActingPlayer().isMageClass())
					{
						if (player_level >= helperBuffItem.getLowerLevel() &&
								player_level <= helperBuffItem.getUpperLevel())
						{
							skill = SkillTable.getInstance()
									.getInfo(helperBuffItem.getSkillID(), helperBuffItem.getSkillLevel());
							if (skill.getSkillType() == L2SkillType.SUMMON)
							{
								playable.doSimultaneousCast(skill);
							}
							else
							{
								npc.doCast(skill);
							}
						}
					}
				}
			}

			_alreadyBuffed.add(playable.getObjectId());
			ThreadPoolManager.getInstance().scheduleAi(new Runnable()
			{
				@Override
				public void run()
				{
					if (playable.getActingPlayer() == null || !playable.getActingPlayer().isOnline())
					{
						return;
					}

					if (playable.isInsideRadius(npc, 200, true, false))
					{
						ThreadPoolManager.getInstance().scheduleAi(this, 300000L);
						return;
					}

					_alreadyBuffed.remove((Integer) playable.getObjectId());
				}
			}, 600000L);
		}
	}
}
