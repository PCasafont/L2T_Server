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

import l2server.Config;
import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.Race;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;

public class CustomBypass implements IBypassHandler
{
    private static final String[] COMMANDS = {"titlecolor", "clanrep", "changesex"};

    public boolean useBypass(String command, L2PcInstance player, L2Npc target)
    {
        if (target == null || !Config.isServer(Config.TENKAI))
        {
            return false;
        }

        if (command.startsWith("titlecolor"))
        {
            String val = command.substring(11);
            PcInventory inv = player.getInventory();
            int medals = 10;
            if (!val.equalsIgnoreCase("FFFF77") && inv.getItemByItemId(6393) != null && inv.getItemByItemId(6393)
                    .getCount() >= medals)
            {
                player.destroyItemByItemId("Change Title Color", 6393, medals, player, true);
                player.setTitleColor(val);
                player.broadcastUserInfo();
                player.sendPacket(new CreatureSay(target.getObjectId(), Say2.TELL, target
                        .getName(), "Title color changed!"));
            }
            else if (val.equalsIgnoreCase("FFFF77"))
            {
                player.setTitleColor(val);
                player.broadcastUserInfo();
                player.sendPacket(new CreatureSay(target.getObjectId(), Say2.TELL, target
                        .getName(), "Title color changed!"));
            }
            else
            {
                player.sendPacket(new CreatureSay(target.getObjectId(), Say2.TELL, target
                        .getName(), "You have not enough medals..."));
            }
        }
        else if (command.startsWith("clanrep"))
        {
            int val = Integer.valueOf(command.substring(8));
            PcInventory inv = player.getInventory();
            if (player.getClan() != null && inv.getItemByItemId(6393) != null && inv.getItemByItemId(6393)
                    .getCount() >= 1)
            {
                inv.destroyItemByItemId("Clan Reputation", 6393, 1, player, this);
                player.getClan().addReputationScore(val, true);
                CreatureSay cs = new CreatureSay(target.getObjectId(), Say2.CLAN, target
                        .getName(), "The clan member " + player
                        .getName() + " has contributed with 1 Glittering Medal to add " + val +
                        " reputation points to the clan!");
                player.getClan().broadcastCSToOnlineMembers(cs, player);
            }
            else
            {
                player.sendPacket(new CreatureSay(target.getObjectId(), Say2.TELL, target
                        .getName(), "You don't have clan or enough medals."));
            }
        }
        else if (command.equalsIgnoreCase("changesex"))
        {
            if (player.getRace() == Race.Kamael)
            {
                player.sendMessage("Sorry but we can't change the sex from kamaels!");
                return false;
            }

            Inventory inv = player.getInventory();
            if (inv.getItemByItemId(4037) == null || inv.getItemByItemId(4037).getCount() < 10)
            {
                player.sendMessage("Sorry but you don't have enough Coins of Luck(10)!");
                return false;
            }

            player.destroyItemByItemId("", 4037, 10, player, true);

            player.getAppearance().setSex(player.getAppearance().getSex() ? false : true);
            player.broadcastUserInfo();
            player.decayMe();
            player.spawnMe(player.getX(), player.getY(), player.getZ());
        }
        return true;
    }

    public String[] getBypassList()
    {
        return COMMANDS;
    }
}