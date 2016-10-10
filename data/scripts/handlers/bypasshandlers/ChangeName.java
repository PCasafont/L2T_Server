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
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.PartySmallWindowAll;
import l2server.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;

public class ChangeName implements IBypassHandler
{
    private static final String[] COMMANDS = {"ChangeCharName", "ChangeClanName"};

    @Override
    public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
    {
        if (target == null || !Config.isServer(Config.TENKAI))
        {
            return false;
        }

        if (command.startsWith("ChangeCharName"))
        {
            if (command.equalsIgnoreCase("ChangeCharName"))
            {
                String html = "<html>" + "<title>Tenkai</title>" + "<body>" +
                        "<center><br><tr><td>Change Name</tr></td><br>" + "<br>" +
                        "Tired of your current character name? Have in mind that this is a very big privilege, the name change has always been denied by the administrators!<br>" +
                        "But I can help you. This is not a recommended option, I would suggest you to create another character, but if you insist that much... it will be 10 coins of luck.<br>" +
                        "<center><tr><td><edit var=text width=130 height=11 length=26><br>" +
                        "<button value=\"Done\" action=\"bypass -h npc_%objectId%_ChangeCharName $text\" back=\"l2ui_ct1.button_df\" width=65 height=20 fore=\"l2ui_ct1.button_df\"></td></tr><br>" +
                        "</center></body></html>";
                NpcHtmlMessage packet = new NpcHtmlMessage(target.getObjectId());
                packet.setHtml(html);
                packet.replace("%objectId%", String.valueOf(target.getObjectId()));
                activeChar.sendPacket(packet);
            }
            else
            {
                try
                {
                    String newName = command.substring(15);
                    if (!Util.isAlphaNumeric(newName) || newName.length() < 2)
                    {
                        activeChar.sendMessage("Incorrect name.");
                        return false;
                    }
                    if (newName.length() > 16)
                    {
                        activeChar.sendMessage("Too long name.");
                        return false;
                    }
                    if (CharNameTable.getInstance().getIdByName(newName) > 0)
                    {
                        activeChar.sendMessage("Player " + newName + " already exists.");
                        return false;
                    }
                    if (!activeChar.destroyItemByItemId("Change Char Name", 4037, 10, target, true))
                    {
                        activeChar.sendPacket(ActionFailed.STATIC_PACKET);
                        return false;
                    }
                    activeChar.setName(newName);
                    activeChar.store();
                    CharNameTable.getInstance().addName(activeChar);

                    activeChar.sendMessage("Your name has been changed.");
                    activeChar.broadcastUserInfo();

                    if (activeChar.isInParty())
                    {
                        // Delete party window for other party members
                        activeChar.getParty().broadcastToPartyMembers(activeChar, new PartySmallWindowDeleteAll());
                        for (L2PcInstance member : activeChar.getParty().getPartyMembers())
                        {
                            // And re-add
                            if (member != activeChar)
                            {
                                member.sendPacket(new PartySmallWindowAll(member, activeChar.getParty()));
                            }
                        }
                    }
                    if (activeChar.getClan() != null)
                    {
                        activeChar.getClan().broadcastClanStatus();
                    }
                }
                catch (StringIndexOutOfBoundsException e)
                {
                    activeChar.sendMessage("You must specify a name!");
                }
            }
        }
        else if (command.startsWith("ChangeClanName"))
        {
            if (command.equalsIgnoreCase("ChangeClanName"))
            {
                String html = "<html>" + "<title>Tenkai</title>" + "<body>" +
                        "<center><br><tr><td>Change Clan Name</tr></td><br>" + "<br>" +
                        "Do you want to change your current clan name? Have in mind that this is a very big privilege, any name change has always been denied by the administrators!<br>" +
                        "But I can help you. This is not a recommended option, I would suggest you to create another clan, but if you insist that much... it will be 25 coins of luck.<br>" +
                        "<center><tr><td><edit var=text width=130 height=11 length=26><br>" +
                        "<button value=\"Done\" action=\"bypass -h npc_%objectId%_ChangeClanName $text\" back=\"l2ui_ct1.button_df\" width=65 height=20 fore=\"l2ui_ct1.button_df\"></td></tr><br>" +
                        "</center></body></html>";
                NpcHtmlMessage packet = new NpcHtmlMessage(target.getObjectId());
                packet.setHtml(html);
                packet.replace("%objectId%", String.valueOf(target.getObjectId()));
                activeChar.sendPacket(packet);
            }
            else
            {
                try
                {
                    if (activeChar.getClan() == null)
                    {
                        activeChar.sendMessage("You don't have a clan!");
                        return false;
                    }
                    String newName = command.substring(15);
                    if (!Util.isAlphaNumeric(newName) || newName.length() < 2)
                    {
                        activeChar.sendMessage("Incorrect name.");
                        return false;
                    }
                    if (newName.length() > 16)
                    {
                        activeChar.sendMessage("Too long name.");
                        return false;
                    }
                    if (ClanTable.getInstance().getClanByName(newName) != null)
                    {
                        // clan name is already taken
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS);
                        sm.addString(newName);
                        activeChar.sendPacket(sm);
                        return false;
                    }
                    if (!activeChar.destroyItemByItemId("Change Clan Name", 4037, 25, target, true))
                    {
                        activeChar.sendPacket(ActionFailed.STATIC_PACKET);
                        return false;
                    }
                    activeChar.getClan().setName(newName);
                    activeChar.getClan().updateClanInDB();

                    activeChar.sendMessage("Your clan name has been changed.");
                    activeChar.getClan().broadcastClanStatus();
                }
                catch (StringIndexOutOfBoundsException e)
                {
                    activeChar.sendMessage("You must specify a name!");
                }
            }
        }

        return true;
    }

    @Override
    public String[] getBypassList()
    {
        return COMMANDS;
    }
}
