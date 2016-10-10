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

package handlers.bypasshandlers;

import java.util.StringTokenizer;

import l2server.gameserver.datatables.MultiSell;
import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.TerritoryWarManager;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2MercenaryManagerInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.ExShowDominionRegistry;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;

public class TerritoryWar implements IBypassHandler
{
    private static final String[] COMMANDS =
            {"Territory", "TW_Multisell", "TW_Buy_List", "TW_Buy", "TW_Buy_Elite", "CalcRewards", "ReceiveRewards"};

    @Override
    public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
    {
        if (target == null)
        {
            return false;
        }

        try
        {
            StringTokenizer st = new StringTokenizer(command, " ");
            String actualCommand = st.nextToken(); // Get actual command

            if (actualCommand.equalsIgnoreCase("Territory"))
            {
                if (st.countTokens() < 1)
                {
                    return false;
                }

                int castleId = Integer.parseInt(st.nextToken());
                activeChar.sendPacket(new ExShowDominionRegistry(castleId, activeChar));
            }
            else if (!(target instanceof L2MercenaryManagerInstance))
            {
                return false;
            }

            L2MercenaryManagerInstance mercman = (L2MercenaryManagerInstance) target;
            if (actualCommand.equalsIgnoreCase("TW_Multisell"))
            {
                if (st.countTokens() < 1)
                {
                    return false;
                }
                int territoryItemId = Integer.parseInt(st.nextToken());
                if (activeChar.getInventory().getItemByItemId(territoryItemId) == null)
                {
                    mercman.showChatWindow(activeChar, 1);
                    return true;
                }

                String val = st.nextToken();
                MultiSell.getInstance().separateAndSend(val, activeChar, mercman, false);
            }
            else if (actualCommand.equalsIgnoreCase("TW_Buy_List"))
            {
                if (st.countTokens() < 1)
                {
                    return false;
                }

                String itemId = st.nextToken();
                NpcHtmlMessage html = new NpcHtmlMessage(mercman.getObjectId());
                html.setFile(activeChar.getHtmlPrefix(), "mercmanager/" + st.nextToken());
                html.replace("%itemId%", itemId);
                html.replace("%noblessBadge%", String.valueOf(TerritoryWarManager.MINTWBADGEFORNOBLESS));
                html.replace("%striderBadge%", String.valueOf(TerritoryWarManager.MINTWBADGEFORSTRIDERS));
                html.replace("%gstriderBadge%", String.valueOf(TerritoryWarManager.MINTWBADGEFORBIGSTRIDER));
                html.replace("%objectId%", String.valueOf(mercman.getObjectId()));
                activeChar.sendPacket(html);
                activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            }
            else if (actualCommand.equalsIgnoreCase("TW_Buy"))
            {
                int itemId = Integer.parseInt(st.nextToken());
                int count = Integer.parseInt(st.nextToken());
                int type = Integer.parseInt(st.nextToken());
                if (activeChar.getInventory().getItemByItemId(itemId) != null)
                {
                    long playerItemCount = activeChar.getInventory().getItemByItemId(itemId).getCount();
                    if (count <= playerItemCount)
                    {
                        int boughtId = 0;
                        switch (type)
                        {
                            case 0:
                                if (activeChar.isNoble())
                                {
                                    return false;
                                }
                                boughtId = 7694;
                                activeChar.setNoble(true);
                                activeChar.broadcastUserInfo();
                                //activeChar.sendPacket(new ExUserInfo(activeChar));
                                //activeChar.sendPacket(new ExBrExtraUserInfo(activeChar));
                                break;
                            case 1:
                                boughtId = 4422;
                                break;
                            case 2:
                                boughtId = 4423;
                                break;
                            case 3:
                                boughtId = 4424;
                                break;
                            case 4:
                                boughtId = 14819;
                                break;
                            default:
                                _log.warning("TerritoryWar buy: not handled type: " + type);
                                return false;
                        }
                        activeChar.destroyItemByItemId("QUEST", itemId, count, mercman, true);
                        activeChar.addItem("QUEST", boughtId, 1, mercman, false);
                        mercman.showChatWindow(activeChar, 7);
                        return true;
                    }
                }
                mercman.showChatWindow(activeChar, 6);
            }
            else if (actualCommand.equalsIgnoreCase("TW_Buy_Elite"))
            {
                if (activeChar.getInventory().getItemByItemId(13767) != null)
                {
                    int _castleid = mercman.getCastle().getCastleId();
                    if (_castleid > 0)
                    {
                        MultiSell.getInstance().separateAndSend("" + (_castleid + 676), activeChar, mercman, false);
                    }
                }
                else
                {
                    NpcHtmlMessage html = new NpcHtmlMessage(mercman.getObjectId());
                    html.setFile(activeChar.getHtmlPrefix(), "mercmanager/nocert.htm");
                    activeChar.sendPacket(html);
                    activeChar.sendPacket(ActionFailed.STATIC_PACKET);
                }
            }
            else if (actualCommand.equalsIgnoreCase("CalcRewards"))
            {
                int territoryId = Integer.parseInt(st.nextToken());
                int[] reward = TerritoryWarManager.getInstance().calcReward(activeChar);
                NpcHtmlMessage html = new NpcHtmlMessage(mercman.getObjectId());
                if (TerritoryWarManager.getInstance().isTWInProgress() || reward[0] == 0)
                {
                    html.setFile(activeChar.getHtmlPrefix(), "mercmanager/reward-0a.htm");
                }
                else if (reward[0] != territoryId)
                {
                    html.setFile(activeChar.getHtmlPrefix(), "mercmanager/reward-0b.htm");
                    html.replace("%castle%", CastleManager.getInstance().getCastleById(reward[0] - 80).getName());
                }
                else if (reward[1] == 0)
                {
                    html.setFile(activeChar.getHtmlPrefix(), "mercmanager/reward-0a.htm");
                }
                else
                {
                    html.setFile(activeChar.getHtmlPrefix(), "mercmanager/reward-1.htm");
                    html.replace("%castle%", CastleManager.getInstance().getCastleById(reward[0] - 80).getName());
                    html.replace("%badge%", String.valueOf(reward[1]));
                    html.replace("%adena%", String.valueOf(reward[1] * 5000));
                }
                html.replace("%territoryId%", String.valueOf(territoryId));
                html.replace("%objectId%", String.valueOf(mercman.getObjectId()));
                activeChar.sendPacket(html);
                activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            }
            else if (actualCommand.equalsIgnoreCase("ReceiveRewards"))
            {
                int territoryId = Integer.parseInt(st.nextToken());
                int badgeId = 57;
                if (TerritoryWarManager.getInstance().TERRITORY_ITEM_IDS.containsKey(territoryId))
                {
                    badgeId = TerritoryWarManager.getInstance().TERRITORY_ITEM_IDS.get(territoryId);
                }
                int[] reward = TerritoryWarManager.getInstance().calcReward(activeChar);
                NpcHtmlMessage html = new NpcHtmlMessage(mercman.getObjectId());
                if (TerritoryWarManager.getInstance().isTWInProgress() || reward[0] == 0)
                {
                    html.setFile(activeChar.getHtmlPrefix(), "mercmanager/reward-0a.htm");
                }
                else if (reward[0] != territoryId)
                {
                    html.setFile(activeChar.getHtmlPrefix(), "mercmanager/reward-0b.htm");
                    html.replace("%castle%", CastleManager.getInstance().getCastleById(reward[0] - 80).getName());
                }
                else if (reward[1] == 0)
                {
                    html.setFile(activeChar.getHtmlPrefix(), "mercmanager/reward-0a.htm");
                }
                else
                {
                    html.setFile(activeChar.getHtmlPrefix(), "mercmanager/reward-2.htm");
                    activeChar.addItem("QUEST", badgeId, reward[1], mercman, true);
                    activeChar.addAdena("QUEST", reward[1] * 5000, mercman, true);
                    TerritoryWarManager.getInstance().resetReward(activeChar);
                }

                html.replace("%objectId%", String.valueOf(mercman.getObjectId()));
                activeChar.sendPacket(html);
                activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            }
            return true;
        }
        catch (Exception e)
        {
            _log.info("Exception in " + getClass().getSimpleName());
        }
        return false;
    }

    @Override
    public String[] getBypassList()
    {
        return COMMANDS;
    }
}
