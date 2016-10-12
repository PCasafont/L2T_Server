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

package custom.VoteNpc;

import l2server.Config;
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.util.Util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author LasTravel
 */
public class VoteNpc extends Quest
{
    private static final boolean _debug = false;
    private static final String _qn = "VoteNpc";
    private static final int _voteNpcId = 40004;
    @SuppressWarnings("unused")
    private static final String SELECT_VOTE_IP =
            "SELECT * from " + Config.WEB_DB_NAME + ".server_voted_ips WHERE ip = ?";

    //private static Map<Integer, Rewards> _voteRewards = new HashMap<Integer, Rewards>();

    public VoteNpc(int questId, String name, String descr)
    {
        super(questId, name, descr);

        addTalkId(_voteNpcId);
        addStartNpc(_voteNpcId);

        //load();
    }

	/*private void load()
    {
		File file = new File(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "scripts/custom/VoteNpc/voteRewards.xml");
		if (!file.exists())
			return;

		XmlDocument doc = new XmlDocument(file);
		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("reward"))
					{
						boolean isItem = d.hasAttribute("itemId");

						int rewardId = isItem ? d.getInt("itemId") : d.getInt("skillId");
						String rewardName = "";
						String rewardIcon = "";
						int maxSkillLevel = 0;
						if (isItem)
						{
							L2Item temp = ItemTable.getInstance().getTemplate(rewardId);
							if (temp == null)
								continue;
							rewardName = temp.getName();
							rewardIcon = temp.getIcon();
						}
						else
						{
							L2Skill temp = SkillTable.getInstance().getInfo(rewardId, 1);
							if (temp == null)
								continue;
							rewardName = temp.getName();

							if (rewardId == 1372)
								rewardIcon = "icon.skill0332";
							else if (rewardId == 1371)
								rewardIcon = "icon.skill0333";
							else if (rewardId == 19229)
								rewardIcon = "icon.skill19222";
							else
								rewardIcon = "icon.skill" + rewardId;

							maxSkillLevel = SkillTable.getInstance().getMaxLevel(rewardId);
						}

						String description = d.getString("description");
						long count = d.getLong("amount", 1);

						_voteRewards.put(rewardId, new Rewards(rewardId, rewardName, rewardIcon, count, description, isItem, maxSkillLevel));
					}
				}
			}
		}
		Log.info("VoteNpc: Loaded " + _voteRewards.size() + " rewards!");
	}

	private class Rewards
	{
		private String _rewardName;
		private int _rewardId;
		private long _amount;
		private String _description;
		private String _rewardIcon;
		private boolean _isItem;
		private int _maxSkillLevel;

		private Rewards(int rewardId, String rewardName, String rewardIcon, long amount, String description, boolean isItem, int maxSkillLevel)
		{
			_rewardId = rewardId;
			_amount = amount;
			_description = description;
			_rewardName = rewardName;
			_rewardIcon = rewardIcon;
			_isItem = isItem;
			_maxSkillLevel = maxSkillLevel;
		}

		private boolean isItem()
		{
			return _isItem;
		}

		private int getMaxSkillLevel()
		{
			return _maxSkillLevel;
		}

		private int getRewardId()
		{
			return _rewardId;
		}

		private long getAmount()
		{
			return _amount;
		}

		private String getDescription()
		{
			return _description;
		}

		private String getName()
		{
			return _rewardName;
		}

		private String getIcon()
		{
			return _rewardIcon;
		}
	}*/

    @Override
    public String onTalk(L2Npc npc, L2PcInstance player)
    {
        return super.onTalk(npc, player);
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        /*if (!_debug && ((player.getHWID() == null) || player.getHWID().equalsIgnoreCase("")))
		{
			player.sendMessage("You can't claim items right now!");
			return "";
		}*/

        long currentTime = System.currentTimeMillis();

        long accountReuse = 0;
        long ipReuse = 0;
        //long hwIdReuse = 0;

        String accountValue = loadGlobalQuestVar(player.getAccountName());
        String externalIpValue = loadGlobalQuestVar(player.getExternalIP());
        //String hwIdValue = loadGlobalQuestVar(player.getHWID().substring(10));

        if (!accountValue.equalsIgnoreCase(""))
        {
            accountReuse = Long.parseLong(accountValue);
        }

        if (!externalIpValue.equalsIgnoreCase(""))
        {
            ipReuse = Long.parseLong(externalIpValue);
        }

        //if (!hwIdValue.equalsIgnoreCase(""))
        //	hwIdReuse = Long.parseLong(hwIdValue);

        long reuse = Collections.max(Arrays.asList(accountReuse, ipReuse));//, hwIdReuse));
        if (currentTime > reuse)
        {
            if (canGetReward(player))
            {
                if (!_debug)
                {
                    String varReuse = Long.toString(System.currentTimeMillis() + 12 * 3600000);
                    saveGlobalQuestVar(player.getAccountName(), varReuse);
                    saveGlobalQuestVar(player.getExternalIP(), varReuse);
                    //saveGlobalQuestVar(player.getHWID().substring(10), varReuse);
                }

                player.addItem(_qn, 4356, 70, npc, true);

                Util.logToFile(player.getName() + "(" + player.getExternalIP() + ") received a vote reward",
                        "VoteSystem", true);

                return "thanks.html";
            }
            else
            {
                player.sendMessage("It looks like you didn't vote!");
                return "";
            }
        }
        else
        {
            //Calc the left time
            long _remaining_time = (reuse - currentTime) / 1000;
            int hours = (int) _remaining_time / 3600;
            int minutes = (int) _remaining_time % 3600 / 60;
            String reuseString =
                    "<font color=\"LEVEL\">Come back again in: " + hours + "Hours " + minutes + "minutes</font>";
            return HtmCache.getInstance().getHtm(null, Config.DATA_FOLDER + "scripts/custom/VoteNpc/reuse.html")
                    .replace("%reuse%", reuseString);
        }
		
		/*if (_debug)
			System.out.println(getName() + ": onAdvEvent: " + event);

		if (event.startsWith("show_rewards_"))
		{
			int pageToShow = Integer.valueOf(event.split("_")[2]);
			int maxItemsPerPage = 15;
			int bossSize = _voteRewards.size();
			int maxPages = bossSize / maxItemsPerPage;
			if (bossSize > (maxItemsPerPage * maxPages))
				maxPages++;
			if (pageToShow > maxPages)
				pageToShow = maxPages;
			int pageStart = maxItemsPerPage * pageToShow;
			int pageEnd = bossSize;
			if ((pageEnd - pageStart) > maxItemsPerPage)
				pageEnd = pageStart + maxItemsPerPage;

			StringBuilder sb = new StringBuilder();

			if (maxPages > 0)
				sb.append("<center>" + CustomCommunityBoard.getInstance().createPages(pageToShow, maxPages, "-h Quest VoteNpc show_rewards_", " ") + "</center>");

			sb.append("<table>");

			Object[] data = _voteRewards.values().toArray();
			for (int i = pageStart; i < pageEnd; i++)
			{
				Rewards reward = (Rewards) data[i];
				if (reward == null)
					continue;

				String rewardName = reward.getName();

				//Skills case
				//We should show the proper skill level to learn on the skill name, so if the player have the Lv. 1 we will show Lv. 2 on the skill name
				if (!reward.isItem())
				{
					int skillLevelToLearn = getProperSkillLevel(player.getSkillLevelHash(reward.getRewardId()), reward.getMaxSkillLevel());
					if (skillLevelToLearn != -1)
						rewardName = rewardName + "(Lv. " + skillLevelToLearn + ")";
					else
						continue;
				}
				else
					rewardName = rewardName + "(" + reward.getAmount() + ")";

				sb.append("<tr><td width=40><img src=" + reward.getIcon() + " width=32 height=32></td><td><table><tr><td><a action=\"bypass -h Quest VoteNpc claim_" + reward.getRewardId() + "\"><font color=00FFFF>" + rewardName + "</font></a></td></tr><tr><td FIXWIDTH=280><font color=ae9977>" + reward.getDescription() + "</font></td></tr></table></td></tr>");
				sb.append("<tr><td><br></td></tr>");
			}
			sb.append("</table>");

			return HtmCache.getInstance().getHtm(null, Config.DATA_FOLDER + "scripts/custom/VoteNpc/rewards.html").replace("%rewards%", sb.toString());
		}
		else if (event.startsWith("claim_"))
		{
			if (!_debug && ((player.getHWID() == null) || player.getHWID().equalsIgnoreCase("")))
			{
				player.sendMessage("You can't claim items right now!");
				return "";
			}

			long currentTime = System.currentTimeMillis();

			long accountReuse = 0;
			long ipReuse = 0;
			long hwIdReuse = 0;

			String accountValue = loadGlobalQuestVar(player.getAccountName());
			String externalIpValue = loadGlobalQuestVar(player.getExternalIP());
			String hwIdValue = loadGlobalQuestVar(player.getHWID().substring(10));

			if (!accountValue.equalsIgnoreCase(""))
				accountReuse = Long.parseLong(accountValue);

			if (!externalIpValue.equalsIgnoreCase(""))
				ipReuse = Long.parseLong(externalIpValue);

			if (!hwIdValue.equalsIgnoreCase(""))
				hwIdReuse = Long.parseLong(hwIdValue);

			long reuse = Collections.max(Arrays.asList(accountReuse, ipReuse, hwIdReuse));
			if (currentTime > reuse)
			{
				int rewardId = Integer.valueOf(event.replace("claim_", ""));
				Rewards reward = _voteRewards.get(rewardId);
				if (reward == null)
				{
					Log.info("VoteNpc: null reward for id " + rewardId);
					return "";
				}

				if (canGetReward(player))
				{
					if (!_debug)
					{
						String varReuse = Long.toString(System.currentTimeMillis() + (12 * 3600000));
						saveGlobalQuestVar(player.getAccountName(), varReuse);
						saveGlobalQuestVar(player.getExternalIP(), varReuse);
						saveGlobalQuestVar(player.getHWID().substring(10), varReuse);
					}

					if (reward.isItem())
						player.addItem(_qn, rewardId, reward.getAmount(), npc, true);
					else
					{
						int skillLevelToLearn = getProperSkillLevel(player.getSkillLevelHash(reward.getRewardId()), reward.getMaxSkillLevel());
						if (skillLevelToLearn != 0)
						{
							L2Skill rewardSkill = SkillTable.getInstance().getInfo(reward.getRewardId(), skillLevelToLearn);
							player.addSkill(rewardSkill, true);
							player.sendSkillList();
						}
					}

					Util.logToFile(player.getName() + "(" + player.getExternalIP() + ") received " + reward.getName() + "(" + reward.getAmount() + ")", "VoteSystem", true);

					return "thanks.html";
				}
				else
					player.sendMessage("It looks like you didn't vote!");
			}
			else
			{
				//Calc the left time
				long _remaining_time = (reuse - currentTime) / 1000;
				int hours = (int) _remaining_time / 3600;
				int minutes = ((int) _remaining_time % 3600) / 60;
				String reuseString = "<font color=\"LEVEL\">Come back again in: " + hours + "Hours " + minutes + "minutes</font>";
				return HtmCache.getInstance().getHtm(null, Config.DATA_FOLDER + "scripts/custom/VoteNpc/reuse.html").replace("%reuse%", reuseString);
			}
		}
		return super.onAdvEvent(event, npc, player);*/
    }

    @SuppressWarnings("unused")
    private int getProperSkillLevel(int currentPlayerSkillLevel, int maxSkillLevel)
    {
        int skillLevelToLearn = -1;
        int currentPlayerLevel = currentPlayerSkillLevel;
        if (currentPlayerLevel == -1)
        {
            skillLevelToLearn = 1;
        }
        else
        {
            if (currentPlayerLevel < maxSkillLevel)
            {
                skillLevelToLearn = currentPlayerLevel + 1;
            }
        }
        return skillLevelToLearn;
    }

    private boolean canGetReward(L2PcInstance player)
    {
        if (player == null)
        {
            return true;
        }

        if (_debug)
        {
            return true;
        }

        String result = "";

        HttpURLConnection connection = null;
        try
        {
            URL url =
                    new URL("http://l2topzone.com/api.php?API_KEY=e659e120d32c1780e566c98590737045&SERVER_ID=12059&IP=" +
                            player.getClient().getConnectionAddress().getHostAddress());
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));

            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null)
            {
                response.append(line);
                response.append('\r');
            }
            result = response.toString();

            is.close();
            rd.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }

        return result != null && result.contains("TRUE");
		
		/*Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement stm = con.prepareStatement(SELECT_VOTE_IP);
			//We will get the proper player external IP, the antibots already check if the player use a ping tool and store his real ip
			stm.setString(1, Config.ANTI_BOTS_ENABLED ? AntiBotsManager.getInstance().getProperPlayerIP(player.getExternalIP()) : player.getExternalIP());
			ResultSet rs = stm.executeQuery();
			if (rs.next())
			{
				long currTime = System.currentTimeMillis() / 1000;
				long expireTime = rs.getLong("expiry_time");
				if (expireTime > currTime)
					return true;
			}
			rs.close();
			stm.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return false;*/
    }

    public static void main(String[] args)
    {
        new VoteNpc(-1, _qn, "custom");
    }
}
