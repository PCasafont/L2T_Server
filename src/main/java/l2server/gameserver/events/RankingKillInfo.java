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

package l2server.gameserver.events;

import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2CommandChannel;
import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LasTravel
 */

public class RankingKillInfo
{
	private static Map<String, KillInfo> specificKillInfo = new HashMap<>();

	public void updateSpecificKillInfo(L2PcInstance killerPlayer, L2PcInstance killedPlayer)
	{
		if (killerPlayer == null || killedPlayer == null)
		{
			return;
		}

		//Killed
		KillInfo map = specificKillInfo.get(killedPlayer.getName());
		if (map != null)
		{
			if (map.getKillingSpree() >= 3)
			{
				map.notifyDead();
				sendRegionalMessage(killedPlayer, " was shut down by " + killerPlayer.getName() + "!");
			}
		}

		//Killer
		map = specificKillInfo.get(killerPlayer.getName());
		if (map != null) // The player have specific info
		{
			map.increaseKills(killerPlayer, killedPlayer.getName());
		}
		else
		{
			KillInfo info = new KillInfo();
			info.increaseKills(killerPlayer, killedPlayer.getName());
			specificKillInfo.put(killerPlayer.getName(), info);
		}

		//Send the info
		if (!killedPlayer.getIsRefusalKillInfo())
		{
			NpcHtmlMessage htmlPage = new NpcHtmlMessage(0);
			htmlPage.setHtml(getBasicKillInfo(killedPlayer, killerPlayer));
			killedPlayer.sendPacket(htmlPage);
		}
	if (killerPlayer.getLevel() - killedPlayer.getLevel() < 5)
			giveKillRewards(killerPlayer, killedPlayer);
	}

	@SuppressWarnings("unused")
	private boolean checkConditions(L2PcInstance killerPlayer, L2PcInstance killedPlayer)
	{
		if (killerPlayer == null || killedPlayer == null)
		{
			return false;
		}

		if (killerPlayer.getPvpFlag() == 0 || killedPlayer.getPvpFlag() == 0)
		{
			return false;
		}

		if (killerPlayer.getLevel() > killedPlayer.getLevel() + 8)
		{
			return false;
		}

		if ((System.currentTimeMillis() - killedPlayer.getCreateTime()) / (24 * 60 * 60 * 1000) < 5)
		{
			return false;
		}

		if (killerPlayer.getClan() == null || killedPlayer.getClan() == null)
		{
			return false;
		}

		if (killerPlayer.getClanId() == killedPlayer.getClanId())
		{
			return false;
		}

		if (killerPlayer.getExternalIP().equalsIgnoreCase(killedPlayer.getExternalIP()) &&
				killerPlayer.getInternalIP().equalsIgnoreCase(killedPlayer.getInternalIP()))
		{
			return false;
		}

		if (killerPlayer.getParty() != null && killedPlayer.getParty() != null &&
				killerPlayer.getParty().getPartyLeaderOID() == killedPlayer.getParty().getPartyLeaderOID())
		{
			return false;
		}

		if (killedPlayer.getPDef(null) < 800 || killedPlayer.getMDef(killedPlayer, null) < 800 ||
				killedPlayer.getPAtkSpd() < 500 || killedPlayer.getMAtkSpd() < 500 || killedPlayer.getPvpKills() < 10)
		{
			return false;
		}



		return (true);

	}

	private void giveKillRewards(L2PcInstance killer, L2PcInstance killed)
	{
		if (killer == null || killed == null)
			return;


		List<String> rewardedPlayers = new ArrayList<String>();
		List<L2PcInstance> allPlayersToBuff = new ArrayList<L2PcInstance>();
		L2Party party = killer.getParty();
		if (party == null)
			allPlayersToBuff.add(killer);
		else
		{
			L2CommandChannel channel = party.getCommandChannel();
			if (channel == null)
				allPlayersToBuff.addAll(party.getPartyMembers());
			else
				allPlayersToBuff.addAll(channel.getMembers());
		}

		if (!allPlayersToBuff.isEmpty())
		{
			for (L2PcInstance pl : allPlayersToBuff)
			{
				if (pl == null || pl.getInstanceId() != pl.getInstanceId() || !Util.checkIfInShortRadius(1600, killer, pl, false))
					continue;

				L2Abnormal currentBuff = pl.getFirstEffect(21365);
				if (currentBuff != null)
				{
					int currentLevel = currentBuff.getLevel();
					if (currentLevel < 5)
					{
						L2Skill buff = SkillTable.getInstance().getInfo(21365, currentLevel + 1);
						if (buff != null)
							buff.getEffects(pl, pl);
					}
					else if (currentLevel == 5)
					{
						//Renew the last buff
						L2Skill buff = SkillTable.getInstance().getInfo(21365, 5);
						if (buff != null)
							buff.getEffects(pl, pl);
					}
				}
				else
				{
					L2Skill buff = SkillTable.getInstance().getInfo(21365, 1);
					if (buff != null)
						buff.getEffects(pl, pl);
				}


					rewardedPlayers.add(pl.getExternalIP());



			}
		}
	}

	private class KillInfo
	{
		@SuppressWarnings("unused")
		private String killerName;
		private int killingSpree;
		private HashMap<String, Integer> killerList;

		private KillInfo()
		{
			killerList = new HashMap<>();
		}

		private int getKillInfo(String name)
		{
			int i = 0;
			if (killerList.containsKey(name))
			{
				return killerList.get(name);
			}
			return i;
		}

		private void increaseKills(L2PcInstance killerPlayer, String killedName)
		{
			if (killerPlayer == null)
			{
				return;
			}

			if (killerList.containsKey(killedName))
			{
				killerList.put(killedName, killerList.get(killedName) + 1);
			}
			else
			{
				killerList.put(killedName, 1);
			}

			killingSpree++;

			String message = null;
			int skillLevel = 0;
			switch (killingSpree)
			{
				case 3:
					message = " is on a Killing Spree!";
					skillLevel = 1;
					break;
				case 5:
					message = " is on a Rampage!";
					skillLevel = 2;
					break;
				case 9:
					message = " is Unstoppable!";
					skillLevel = 3;
					break;
				case 15:
					message = " is Legendary!";
					skillLevel = 4;
					break;
				case 25:
					message = " is Godlike!";
					skillLevel = 5;
					break;
			}

			if (skillLevel > 0)
			{
				L2Skill starSkill = SkillTable.getInstance().getInfo(18363, skillLevel);
				if (starSkill != null)
				{
					starSkill.getEffects(killerPlayer, killerPlayer);
				}
			}

			//Region message only
			if (message != null)
			{
				sendRegionalMessage(killerPlayer, message);
			}
		}

		private int getKillingSpree()
		{
			return killingSpree;
		}

		private void notifyDead()
		{
			killingSpree = 0;
		}
	}

	private void sendRegionalMessage(L2PcInstance player, String message)
	{
		if (player == null)
		{
			return;
		}

		CreatureSay cs = new CreatureSay(-1, Say2.CRITICAL_ANNOUNCE, player.getName(), player.getName() + message);
		int region = MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY());
		Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers().values();
		for (L2PcInstance worldPlayer : pls)
		{
			if (region == MapRegionTable.getInstance().getMapRegion(worldPlayer.getX(), worldPlayer.getY()) &&
					worldPlayer.getEvent() == null && worldPlayer.getInstanceId() == 0)
			{
				worldPlayer.sendPacket(cs);
			}
		}
	}

	private int getSpecificKillsInfo(String killerName, String killedName)
	{
		if (specificKillInfo.get(killerName) != null)
		{
			return specificKillInfo.get(killerName).getKillInfo(killedName);
		}
		return 0;
	}

	private String getRank(int points)
	{
		if (points < 50)
		{
			return "No Grade";
		}
		else if (points >= 50 && points < 100)
		{
			return "D Grade";
		}
		else if (points >= 100 && points < 200)
		{
			return "C Grade";
		}
		else if (points >= 200 && points < 300)
		{
			return "B Grade";
		}
		else if (points >= 300 && points < 400)
		{
			return "A Grade";
		}
		else if (points >= 400 && points < 500)
		{
			return "S Grade";
		}
		else if (points >= 500 && points < 600)
		{
			return "S80 Grade";
		}
		else if (points >= 600 && points < 700)
		{
			return "R Grade";
		}
		else if (points >= 700 && points < 800)
		{
			return "R95 Grade";
		}
		else if (points >= 800 && points < 900)
		{
			return "R99 Grade";
		}
		return "Epic Grade";
	}

	private String getBasicKillInfo(L2PcInstance killed, L2PcInstance killer)
	{
		StringBuilder tb = new StringBuilder();
		String rankName = getRank(killer.getPvpKills());
		if (rankName == null)
		{
			return "";
		}

		tb.append("<html><head><title>" + killer.getName() + " Basic Information</title></head><body>");
		if (killed.getClassId() >= 148)
		{
			tb.append("<center><table width=256 height=180 background=\"L2UI_CT1.HtmlWnd.HtmlWnd_ClassMark_" +
					killed.getClassId() +
					"\"><tr><td FIXWIDTH=300><br><br><br><br><br><br><br></td></tr></table></center>");
		}
		else
		{
			tb.append(
					"<center><table width=256 height=180 background=\"L2UI_CT1.HtmlWnd.HtmlWnd_DF_TextureMansion\"><tr><td FIXWIDTH=300><br><br><br><br><br><br><br></td></tr></table></center>");
		}
		tb.append("<br><table width=300>");
		tb.append(
				"<tr><td FIXWIDTH=100><font color=886531>Ranking Position:</font></td><td>" + rankName + "</td></tr>");
		tb.append("<tr><td FIXWIDTH=100><font color=886531>Name [lvl]:</font></td><td><font color=FF6600>" +
				killer.getName() + " [" + killer.getLevel() + "]</font></td></tr>");
		tb.append(
				"<tr><td><font color=772F2F><font color=886531>Current Class:</font></font></td><td><font color=FF6600>" +
						PlayerClassTable.getInstance().getClassNameById(killer.getClassId()) + "</font></td></tr>");
		tb.append("<tr><td><font color=886531>Hero:</font></td><td><font color=999999>" +
				(killer.isHero() ? "Yes" : "No") + "</font></td></tr>");

		String clanName = killer.getClan() != null ? killer.getClan().getName() : "No";
		tb.append(
				"<tr><td><font color=886531>Clan:</font></td><td><font color=999999>" + clanName + "</font></td></tr>");

		String allyName = killer.getAllyId() > 0 ? killer.getClan().getAllyName() : "No";
		tb.append("<tr><td><font color=886531>Alliance:</font></td><td><font color=999999>" + allyName +
				"</font></td></tr>");
		tb.append("</table>");
		tb.append("<img src=\"L2UI.Squaregray\" width=300 height=1>");
		tb.append("<table width=300>");
		tb.append("<tr><td ><font color=886531>Current CP / Max Cp:</font></td><td FIXWIDTH=100><font color=LEVEL>" +
				(int) killer.getCurrentCp() + " / " + killer.getMaxCp() + "</font></td></tr>");
		tb.append("<tr><td><font color=886531>Current HP / Max HP:</font></td><td><font color=FF0000>" +
				(int) killer.getCurrentHp() + " / " + killer.getMaxHp() + "</font></td></tr>");
		tb.append("<tr><td><font color=886531>Current MP / Max MP:</font></td><td><font color=3366FF>" +
				(int) killer.getCurrentMp() + " / " + killer.getMaxMp() + "</font></td></tr>");
		tb.append("</table>");
		tb.append("<img src=\"L2UI.Squaregray\" width=300 height=1>");
		tb.append("<table width=300>");
		tb.append("<tr><td><font color=886531>Legal PvP Points:</font></td><td FIXWIDTH=70><font color=LEVEL>" +
				killer.getPvpKills() + "</font></td></tr>");
		tb.append(
				"<tr><td><font color=886531>Legal PK Points:</font></td><td><font color=LEVEL>" + killer.getPkKills() +
						"</font></td></tr>");
		tb.append("<tr><td><font color=886531>Times killed me:</font></td><td><font color=LEVEL>" +
				getSpecificKillsInfo(killer.getName(), killed.getName()) + "</font></td></tr>");
		tb.append("<tr><td><font color=886531>Times I killed:</font></td><td><font color=LEVEL>" +
				getSpecificKillsInfo(killed.getName(), killer.getName()) + "</font></td></tr>");
		tb.append("</table>");
		tb.append("<img src=\"L2UI.Squaregray\" width=300 height=1>");
		tb.append("</body></html>");
		return tb.toString();
	}

	public static RankingKillInfo getInstance()
	{
		return SingletonHolder.instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final RankingKillInfo instance = new RankingKillInfo();
	}
}
