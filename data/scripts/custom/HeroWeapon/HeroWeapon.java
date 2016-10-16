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

package custom.HeroWeapon;

import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.util.Util;

public class HeroWeapon extends Quest
{
	private static final int[] npcIds = {31690, 31769, 31770, 31771, 31772};

	private static final int[] weaponIds =
			{30392, 30393, 30394, 30395, 30396, 30397, 30398, 30399, 30400, 30401, 30402, 30403, 30404, 30405};

	public HeroWeapon(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int i : npcIds)
		{
			addStartNpc(i);
			addTalkId(i);
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());

		int weaponId = Integer.valueOf(event);
		if (Util.contains(weaponIds, weaponId))
		{
			st.giveItems(weaponId, 1);
		}

		st.exitQuest(true);
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			newQuestState(player);
		}

		if (player.isHero())
		{
			if (hasHeroWeapon(player))
			{
				htmltext = "already_have_weapon.htm";
				st.exitQuest(true);
			}
			else
			{
				htmltext = "weapon_list.htm";
			}
		}
		else
		{
			htmltext = "no_hero.htm";
			st.exitQuest(true);
		}

		return htmltext;
	}

	private boolean hasHeroWeapon(L2PcInstance player)
	{
		for (int i : weaponIds)
		{
			if (player.getInventory().getItemByItemId(i) != null)
			{
				return true;
			}
		}

		for (int i : weaponIds)
		{
			if (player.getWarehouse().getItemByItemId(i) != null)
			{
				return true;
			}
		}

		return false;
	}

	public static void main(String[] args)
	{
		new HeroWeapon(-1, "HeroWeapon", "custom");
	}
}
