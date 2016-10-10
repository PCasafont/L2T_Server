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

import java.util.Map.Entry;
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
import l2server.gameserver.util.Util;

/**
 * @author Pere
 */
public class DreamsSell implements IVoicedCommandHandler
{
    private static final String[] VOICED_COMMANDS = {"sell"};

    /**
     * @see l2server.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, l2server.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
     */
    @Override
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
                        player.sendPacket(
                                SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
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
                        if (!(c instanceof L2PcInstance &&
                                ((L2PcInstance) c).getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_NONE))
                        {
                            player.sendMessage(
                                    "Try to put your store a little further from " + c.getName() + ", please.");
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

                        String log = player.getName() + " (" + list.getTitle() + ")\n";
                        for (TradeItem item : list.getItems())
                        {
                            log += "\t" + item.getItem().getName() + " (max " + item.getCount() + ")\n";
                            for (Entry<L2Item, Long> priceItem : item.getPriceItems().entrySet())
                            {
                                log += "\t\t" + priceItem.getKey().getName() + " (" + priceItem.getValue() + ")\n";
                            }
                        }
                        Util.logToFile(log, "sellLog", true);
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

                    String html =
                            "<html><head><title>Magic Gem</title><body>Unknown:<br>Here, you can manage the ingredients that will be required in order to purchase your %name%.<br><center><tr><td>Your are editing <font color=\"6ab3dd\">%name%</font>.</tr></td>" +
                                    "<br>" + "Amount For Sale: %amount%<br>" +
                                    "<tr><td><edit var=text type=number width=130 height=11 length=26><br>" +
                                    "<button value=\"Modify Amount\" action=\"bypass -h voice .sell item " + index +
                                    " setAmount $text\" back=\"l2ui_ct1.button_df\" width=100 height=20 fore=\"l2ui_ct1.button_df\"></td></tr><br><br>" +
                                    "<br><center><font color=\"6ab3dd\">Ingredients</font></center><br>%prices%<br><br>" +
                                    "%addPrice%<br1>" +
                                    "<a action=\"bypass -h voice .sell\"><font color=\"acd6ed\">Back to the previous page.</font></a><br1>" +
                                    "</center></body></html>";

                    NpcHtmlMessage packet = new NpcHtmlMessage(0, 1);
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

                            if (inputAmount.matches("\\d+"))
                            {
                                amount = Long.parseLong(inputAmount);
                            }

                            if (amount == 0)
                            {
                                player.sendMessage("Please set a correct amount!");

                                return false;
                            }

                            if (player.checkItemManipulation(item.getObjectId(), amount, "Custom Sell") != null)
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

                            String amountString = st.nextToken();
                            if (!Util.isDigit(amountString))
                            {
                                player.sendMessage("Please insert only a number!");
                                return false;
                            }

                            long amount = Long.parseLong(amountString);
                            if (amount < 1)
                            {
                                player.sendMessage("Please insert a positive amount!");
                                return false;
                            }

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
                            packet.replace("%addPrice%", "Choose the ingredient to add from your inventory.");
                            player.setAddSellPrice(index);

                            final SystemMessage s =
                                    SystemMessage.getSystemMessage(SystemMessageId.LIGHT_BLUE_UPSTAIRS_S1);
                            s.addString("Choose the ingredient to add from your inventory.");

                            player.sendPacket(s);
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
                        String itemHtm =
                                "<table><tr><td width=40><table bgcolor=000000 width=24><tr><td><button action=\"\" value=\" \" width=32 height=32 back=\"" +
                                        priceItem.getIcon() + "\" fore=\"" + priceItem.getIcon() +
                                        "\"></td></tr></table></td><td width=220><table bgcolor=131210 width=220><tr><td>[<font color=FFFFFF><a action=\"bypass -h voice .sell item " +
                                        index + " deletePrice " + priceItem.getItemId() +
                                        "\" value=\" \" width=32 height=32\">X</a></font>] <font color=FFFFFF><a action=\"\" value=\" \" width=32 height=32\">" +
                                        priceItem.getName() + "</a></font></td></tr><tr><td>" +
                                        item.getPriceItems().get(priceItem) + " " + priceItem.getName() +
                                        " will be required.</td></tr></table></td></tr></table><br>" +
                                        "<br><center><font color=\"6ab3dd\">Amount:</font></center><br1>" +
                                        "<edit var=amt" + priceItem.getItemId() +
                                        " type=number width=100 height=11 length=26><br> " +
                                        "<button value=\"Apply\" action=\"bypass -h voice .sell item " + index +
                                        " setPriceAmount " + priceItem.getItemId() + " $amt" + priceItem.getItemId() +
                                        "\" back=\"l2ui_ct1.button_df\" width=85 height=20 fore=\"l2ui_ct1.button_df\">" +
                                        "<button value=\"Delete\" action=\"bypass -h voice .sell item " + index +
                                        " deletePrice " + priceItem.getItemId() +
                                        "\" back=\"l2ui_ct1.button_df\" width=85 height=20 fore=\"l2ui_ct1.button_df\"><br><br>";

                        pricesHtm += itemHtm;
                    }

                    packet.replace("%name%", item.getItem().getName());
                    packet.replace("%amount%", String.valueOf(item.getCount()));
                    packet.replace("%prices%", pricesHtm);
                    packet.replace("%addPrice%", "<a action=\"bypass -h voice .sell item " + index +
                            " addPrice\"><font color=\"c2dceb\">Add another Ingredient.</font></a><br1>");

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
                    "<html><head><title>Magic Gem</title><body><center><font color=\"6ab3dd\">Private Store</font></center><br><center>[%message%]</center><br><center><font color=\"6ab3dd\">Items For Sale</font></center><br>%items%<center><font color=\"6ab3dd\">Modify Title</font></center><br1><center><edit var=text width=130 height=11 length=26><br><a action=\"bypass -h voice .sell setMessage $text\"><font color=\"c2dceb\">Apply.</font></a></center><br><center><font color=\"6ab3dd\">Others</font></center><br1>%addItem%%actionButton%</body></html>";

            NpcHtmlMessage packet = new NpcHtmlMessage(0, 1);
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
                        packet.replace("%addItem%",
                                "<center>Choose the item to sell from your inventory.</center><br1>");
                        player.setIsAddSellItem(true);

                        final SystemMessage s = SystemMessage.getSystemMessage(SystemMessageId.LIGHT_BLUE_UPSTAIRS_S1);
                        s.addString("Choose the item to sell from your inventory.");

                        player.sendPacket(s);
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
                L2Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItem().getItemId());
                String itemIcon = itemTemplate.getIcon();
                String newItemHtm =
                        "<table><tr><td width=40><table bgcolor=000000 width=24><tr><td><button action=\"bypass -h voice .sell item " +
                                i + "\" value=\" \" width=32 height=32 back=\"" + itemIcon + "\" fore=\"" + itemIcon +
                                "\"></td></tr></table></td><td width=220><table bgcolor=131210 width=220><tr><td>[<font color=FFFFFF><a action=\"bypass -h voice .sell deleteItem " +
                                item.getObjectId() +
                                "\" value=\" \" width=32 height=32\">X</a></font>] <font color=FFFFFF><a action=\"bypass -h voice .sell item " +
                                i + "\" value=\" \" width=32 height=32\">" + itemTemplate.getName() +
                                "</a></font></td></tr><tr><td>Click me to set-up ingredients.</td></tr></table></td></tr></table><br>";
                @SuppressWarnings("unused") String itemHtm =
                        item.getItem().getName() + "<br>" + "<tr>" + "<td>Price items: " + item.getPriceItems().size() +
                                "</td>" + "<td><button value=\"Edit\" action=\"bypass -h voice .sell item " + i +
                                "\" back=\"l2ui_ct1.button_df\" width=65 height=20 fore=\"l2ui_ct1.button_df\"></td>" +
                                "<td><button value=\"Delete\" action=\"bypass -h voice .sell deleteItem " +
                                item.getObjectId() +
                                "\" back=\"l2ui_ct1.button_df\" width=65 height=20 fore=\"l2ui_ct1.button_df\"></td>" +
                                "</tr>";

                itemsHtm += newItemHtm;
            }

            if (itemsHtm.equals(""))
            {
                itemsHtm = "<center>None yet. Add one!</center><br>";
            }

            if (list.getTitle() != null)
            {
                packet.replace("%message%", list.getTitle());
            }
            else
            {
                packet.replace("%message%", "No Title Yet");
            }

            packet.replace("%items%", itemsHtm);
            packet.replace("%addItem%",
                    "<a action=\"bypass -h voice .sell addItem\"><font color=\"c2dceb\">Add another Item.</font></a><br1>");

            if (!isSelling)
            {
                packet.replace("%actionButton%",
                        "<a action=\"bypass -h voice .sell start\"><font color=\"acd6ed\">Set up the store NOW.</font></a>");
            }
            else
            {
                packet.replace("%actionButton%",
                        "<a action=\"bypass -h voice .sell stop\"><font color=\"acd6ed\">Stop the store NOW.</font></a>");
            }

            player.sendPacket(packet);
        }
        return true;
    }

    /**
     * @see l2server.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
     */
    @Override
    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }
}
