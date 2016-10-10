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
package handlers.voicedcommandhandlers;

import java.util.StringTokenizer;

import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.handler.IVoicedCommandHandler;
import l2server.gameserver.model.TradeList;
import l2server.gameserver.model.TradeList.TradeItem;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.PrivateStoreMsgSell;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.gameserver.templates.item.L2Item;

/**
 * @author Pere
 */
public class Sell implements IVoicedCommandHandler
{
    private static final String[] VOICED_COMMANDS = {"sell"};

    /**
     * @see l2server.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, l2server.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
     */
    public boolean useVoicedCommand(String command, L2PcInstance player, String params)
    {
        if (command.equalsIgnoreCase("sell"))
        {
            if (params == null)
            {
                params = "";
            }

            boolean isSelling = player.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_CUSTOM_SELL;

            TradeList list = player.getCustomSellList();

            if (!isSelling)
            {
                if (params.equals("start"))
                {
                    boolean canStart = list.getItemCount() > 0;

                    for (TradeItem item : list.getItems())
                    {
                        if (item.getPriceItems().isEmpty())
                        {
                            player.sendMessage("At least one of the items you're trying to sell lacks price!");
                            canStart = false;
                            break;
                        }
                    }

                    if (!player.getAccessLevel().allowTransaction())
                    {
                        player.sendPacket(SystemMessage
                                .getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
                        canStart = false;
                    }

                    if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player) || player.isInDuel())
                    {
                        player.sendPacket(SystemMessage
                                .getSystemMessage(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT));
                        canStart = false;
                    }

                    if (player.isInsideZone(L2Character.ZONE_NOSTORE))
                    {
                        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_PRIVATE_STORE_HERE));
                        canStart = false;
                    }

                    for (L2Character c : player.getKnownList().getKnownCharactersInRadius(70))
                    {
                        if (!(c instanceof L2PcInstance && ((L2PcInstance) c)
                                .getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_NONE))
                        {
                            player.sendMessage("Try to put your store a little further from " + c
                                    .getName() + ", please.");
                            canStart = false;
                        }
                    }

                    if (canStart)
                    {
                        player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_CUSTOM_SELL);
                        player.broadcastUserInfo();
                        player.broadcastPacket(new PrivateStoreMsgSell(player));
                        player.sitDown();
                        isSelling = true;
                    }
                    else
                    {
                        player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
                        player.sendMessage(
                                "The selling process couldn't be started. Make sure everything's well set up.");
                    }
                }
                else if (params.startsWith("item"))
                {
                    params = params.substring(5);
                    StringTokenizer st = new StringTokenizer(params);

                    int index = Integer.parseInt(st.nextToken());
                    if (index >= list.getItemCount())
                    {
                        return false;
                    }

                    TradeItem item = list.getItems()[index];

                    String html = "<html>" + "<title>Tenkai</title>" + "<body>" +
                            "<center><br><tr><td>Sell %name%</tr></td><br>" + "<br>" + "Maximum amount: %amount%<br>" +
                            "<tr><td><edit var=text width=130 height=11 length=26><br>" +
                            "<button value=\"Set max amount\" action=\"bypass -h voice .sell item " + index +
                            " setAmount $text\" back=\"l2ui_ct1.button_df\" width=135 height=20 fore=\"l2ui_ct1.button_df\"></td></tr><br><br>" +
                            "%prices%<br><br>" + "%addPrice%<br><br>" +
                            "<button value=\"Back\" action=\"bypass -h voice .sell\" back=\"l2ui_ct1.button_df\" width=65 height=20 fore=\"l2ui_ct1.button_df\">" +
                            "</center></body></html>";

                    NpcHtmlMessage packet = new NpcHtmlMessage(0);
                    packet.setHtml(html);

                    String param = "";
                    if (st.hasMoreTokens())
                    {
                        param = st.nextToken();
                    }

                    if (param.equals("setAmount") && st.hasMoreTokens())
                    {
                        try
                        {
                            long amount = 0;

                            String inputAmount = st.nextToken();

                            if (inputAmount.matches("-?\\d+"))
                            {
                                amount = Long.parseLong(inputAmount);
                            }

                            if (amount == 0)
                            {
                                player.sendMessage("Please set a correct amount!");

                                return false;
                            }

                            if (player.getInventory().getItemByObjectId(item.getObjectId()).getCount() >= amount)
                            {
                                item.setCount(amount);
                            }
                            else
                            {
                                player.sendMessage("You don't have enough of that item.");

                                return false;
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else if (param.equals("setPriceAmount"))
                    {
                        try
                        {
                            if (!st.hasMoreTokens())
                            {
                                player.sendMessage("Please set the correct price!");

                                return false;
                            }

                            int priceId = Integer.parseInt(st.nextToken());

                            if (!st.hasMoreTokens())
                            {
                                player.sendMessage("Please set the correct amount!");

                                return false;
                            }

                            long amount = Long.parseLong(st.nextToken());
                            item.getPriceItems().put(ItemTable.getInstance().getTemplate(priceId), amount);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else if (param.equals("addPrice"))
                    {
                        if (st.hasMoreTokens())
                        {
                            int itemId = Integer.parseInt(st.nextToken());
                            L2Item toSell = ItemTable.getInstance().getTemplate(itemId);
                            if (toSell != null && toSell.isTradeable())
                            {
                                item.getPriceItems().put(toSell, 1L);
                            }
                            else
                            {
                                player.sendMessage("You can't trade that item!");
                            }
                        }
                        else
                        {
                            packet.replace("%addPrice%", "Click on the item you want to add.");
                            player.setAddSellPrice(index);
                        }
                    }
                    else if (param.equals("deletePrice") && st.hasMoreTokens())
                    {
                        int itemId = Integer.parseInt(st.nextToken());
                        item.getPriceItems().remove(ItemTable.getInstance().getTemplate(itemId));
                    }

                    String pricesHtm = "";
                    for (L2Item priceItem : item.getPriceItems().keySet())
                    {
                        String itemHtm = priceItem.getName() + " (" + item.getPriceItems()
                                .get(priceItem) + ")<br>" + "<tr>" + "<td><edit var=amt" + priceItem
                                .getItemId() + " width=60 height=11 length=26> " +
                                "<button value=\"Set Amount\" action=\"bypass -h voice .sell item " + index +
                                " setPriceAmount " + priceItem
                                .getItemId() + " $amt" + priceItem
                                .getItemId() +
                                "\" back=\"l2ui_ct1.button_df\" width=85 height=20 fore=\"l2ui_ct1.button_df\"></td>" +
                                "<td><button value=\"Delete\" action=\"bypass -h voice .sell item " + index +
                                " deletePrice " + priceItem
                                .getItemId() +
                                "\" back=\"l2ui_ct1.button_df\" width=65 height=20 fore=\"l2ui_ct1.button_df\"></td>" +
                                "</tr>";
                        ;

                        pricesHtm += itemHtm;
                    }

                    packet.replace("%name%", item.getItem().getName());
                    packet.replace("%amount%", String.valueOf(item.getCount()));
                    packet.replace("%prices%", pricesHtm);
                    packet.replace("%addPrice%",
                            "<button value=\"Add Price Item\" action=\"bypass -h voice .sell item " + index +
                                    " addPrice\" back=\"l2ui_ct1.button_df\" width=115 height=20 fore=\"l2ui_ct1.button_df\">");

                    player.sendPacket(packet);
                    return true;
                }
            }
            else if (params.equals("stop"))
            {
                player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
                player.standUp();
                player.broadcastUserInfo();
                isSelling = false;
            }

            String html =
                    "<html>" + "<title>Tenkai</title>" + "<body>" + "<center><br><tr><td>Sell</tr></td><br>" + "<br>" +
                            "Message: %message%<br>" + "<tr><td><edit var=text width=130 height=11 length=26><br>" +
                            "<button value=\"Set Message\" action=\"bypass -h voice .sell setMessage $text\" back=\"l2ui_ct1.button_df\" width=85 height=20 fore=\"l2ui_ct1.button_df\"></td></tr><br><br>" +
                            "%items%<br><br>" + "%addItem%<br><br>" + "%actionButton%" + "</center></body></html>";

            NpcHtmlMessage packet = new NpcHtmlMessage(0);
            packet.setHtml(html);

            if (!isSelling)
            {
                if (params.startsWith("setMessage") && params.length() > 11)
                {
                    list.setTitle(params.substring(11));
                }
                else if (params.startsWith("addItem"))
                {
                    if (params.length() > 8)
                    {
                        int objId = Integer.parseInt(params.substring(8));
                        list.addItem(objId, 1);
                    }
                    else
                    {
                        packet.replace("%addItem%", "Click on the item you want to add.");
                        player.setIsAddSellItem(true);
                    }
                }
                else if (params.startsWith("deleteItem"))
                {
                    int objId = Integer.parseInt(params.substring(11));
                    list.removeItem(objId, -1, -1);
                }
            }

            String itemsHtm = "";
            for (int i = 0; i < list.getItemCount(); i++)
            {
                TradeItem item = list.getItems()[i];
                String itemHtm = item.getItem().getName() + "<br>" + "<tr>" + "<td>Price items: " + item.getPriceItems()
                        .size() + "</td>" + "<td><button value=\"Edit\" action=\"bypass -h voice .sell item " + i +
                        "\" back=\"l2ui_ct1.button_df\" width=65 height=20 fore=\"l2ui_ct1.button_df\"></td>" +
                        "<td><button value=\"Delete\" action=\"bypass -h voice .sell deleteItem " + item
                        .getObjectId() +
                        "\" back=\"l2ui_ct1.button_df\" width=65 height=20 fore=\"l2ui_ct1.button_df\"></td>" + "</tr>";

                itemsHtm += itemHtm;
            }

            if (list.getTitle() != null)
            {
                packet.replace("%message%", "\"" + list.getTitle() + "\"");
            }
            else
            {
                packet.replace("%message%", "No message");
            }
            packet.replace("%items%", itemsHtm);
            packet.replace("%addItem%",
                    "<button value=\"Add Item\" action=\"bypass -h voice .sell addItem\" back=\"l2ui_ct1.button_df\" width=65 height=20 fore=\"l2ui_ct1.button_df\">");

            if (!isSelling)
            {
                packet.replace("%actionButton%",
                        "<button value=\"Start!\" action=\"bypass -h voice .sell start\" back=\"l2ui_ct1.button_df\" width=85 height=20 fore=\"l2ui_ct1.button_df\">");
            }
            else
            {
                packet.replace("%actionButton%",
                        "<button value=\"Stop\" action=\"bypass -h voice .sell stop\" back=\"l2ui_ct1.button_df\" width=85 height=20 fore=\"l2ui_ct1.button_df\">");
            }

            player.sendPacket(packet);
        }
        return true;
    }

    /**
     * @see l2server.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
     */
    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }
}
