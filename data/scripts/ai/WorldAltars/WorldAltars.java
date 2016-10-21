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

package ai.WorldAltars;

import l2server.gameserver.instancemanager.CustomWorldAltars;
import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LasTravel
 */

public class WorldAltars extends Quest
{
	private static final String _qn = "WorldAltars";
	private static final boolean _debug = false;

	private static final int[] _altarIds = {143, 144, 145, 146};
	private static final int[] _bossIds = {80351, 80352, 80353, 80354};

	public WorldAltars(int questId, String name, String descr)
	{
		super(questId, name, descr);
		for (int i : _altarIds)
		{
			addTalkId(i);
			addStartNpc(i);
			addFirstTalkId(i);
		}

		for (int i : _bossIds)
		{
			addKillId(i);
		}
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "WorldAltars.html";
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.startsWith("trySpawnBoss"))
		{
			if (!_debug)
			{
				L2Party party = player.getParty();
				if (party == null)
				{
					player.sendMessage("You can't beat such challenge alone!");
					return "";
				}
				if (party.getLeader() != player)
				{
					player.sendMessage("You aren't the team group leader!");
					return "";
				}
				if (party.getMemberCount() < 4)
				{
					player.sendMessage("You will need at least 4 different players!");
					return "";
				}
				//Check if have at least 4 different players
				Map<String, String> pIps = new HashMap<String, String>();
				for (L2PcInstance pMember : party.getPartyMembers())
				{
					if (pMember == null)
					{
						continue;
					}

					if (pIps.size() >= 4)
					{
						break;
					}

					if (!pMember.isInsideRadius(npc, 1000, false, false))
					{
						player.sendMessage("World Altars: " + pMember.getName() + " is far!");
						return "";
					}

					if (pIps.containsKey(pMember.getExternalIP()))
					{
						if (pIps.get(pMember.getExternalIP()).equalsIgnoreCase(pMember.getInternalIP()))
						{
							continue;
						}
					}
					pIps.put(pMember.getExternalIP(), pMember.getInternalIP());
				}

				if (pIps.size() >= 4)
				{
					if (!CustomWorldAltars.getInstance().notifyTrySpawnBosss(npc))
					{
						return "WorldAltars-no.html";
					}
				}
				else
				{
					player.sendMessage("You don't have enough different players!");
					return "";
				}
			}
			else
			{
				if (!CustomWorldAltars.getInstance().notifyTrySpawnBosss(npc))
				{
					return "WorldAltars-no.html";
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		CustomWorldAltars.getInstance().notifyBossKilled(npc);

		return super.onKill(npc, player, isPet);
	}

	public static void main(String[] args)
	{
		new WorldAltars(-1, _qn, "ai");
	}
}
