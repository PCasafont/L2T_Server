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

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import l2server.Config;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2OlympiadManagerInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.model.olympiad.Olympiad;
import l2server.gameserver.model.olympiad.OlympiadManager;
import l2server.gameserver.model.olympiad.OlympiadNobleInfo;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExHeroList;
import l2server.gameserver.network.serverpackets.ExOlympiadInfoList;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * @author DS
 */
public class OlympiadManagerLink implements IBypassHandler
{
    private static final String[] COMMANDS = {"olympiaddesc", "olympiadnoble", "olybuff", "olympiad"};

    private static final int GATE_PASS = Config.ALT_OLY_COMP_RITEM;

    @Override
    public final boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
    {
        if (!(target instanceof L2OlympiadManagerInstance))
        {
            return false;
        }

        try
        {
            if (command.toLowerCase().startsWith(COMMANDS[0])) // desc
            {
                int val = Integer.parseInt(command.substring(13, 14));
                String suffix = command.substring(14);
                ((L2OlympiadManagerInstance) target).showChatWindow(activeChar, val, suffix);
            }
            else if (command.toLowerCase().startsWith(COMMANDS[1])) // noble
            {
                if (!activeChar.isNoble() || activeChar.isSubClassActive())
                {
                    return false;
                }

                int passes;
                int val = Integer.parseInt(command.substring(14));
                NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());

                switch (val)
                {
                    case 1:
                        OlympiadManager.getInstance().unRegisterNoble(activeChar);
                        break;
                    case 2:
                        if (OlympiadManager.getInstance().isInCompetition(activeChar, true))
                        {
                            return false;
                        }
                        if (!OlympiadManager.getInstance().isRegistered(activeChar))
                        {
                            final int nonClassed = OlympiadManager.getInstance().getRegisteredNonClassBased().size();
                            final Collection<List<Integer>> allClassed =
                                    OlympiadManager.getInstance().getRegisteredClassBased().values();
                            int classed = 0;
                            if (!allClassed.isEmpty())
                            {
                                for (List<Integer> cls : allClassed)
                                {
                                    if (cls != null)
                                    {
                                        classed += cls.size();
                                    }
                                }
                            }
                            html.setFile(activeChar.getHtmlPrefix(),
                                    Olympiad.OLYMPIAD_HTML_PATH + "noble_register.htm");
                            html.replace("%period%", String.valueOf(Olympiad.getInstance().getCurrentCycle())); // TODO
                            html.replace("%rounds%", "?"); // TODO
                            html.replace("%participants%", String.valueOf(classed + nonClassed));
                        }
                        else
                        {
                            html.setFile(activeChar.getHtmlPrefix(),
                                    Olympiad.OLYMPIAD_HTML_PATH + "noble_unregister.htm");
                        }
                        html.replace("%objectId%", String.valueOf(target.getObjectId()));
                        activeChar.sendPacket(html);
                        break;
                    case 3:
                        int points = 0;
                        OlympiadNobleInfo oni = Olympiad.getInstance().getNobleInfo(activeChar.getObjectId());
                        if (oni != null)
                        {
                            points = oni.getPoints();
                        }
                        html.setFile(activeChar.getHtmlPrefix(), Olympiad.OLYMPIAD_HTML_PATH + "noble_points1.htm");
                        html.replace("%points%", String.valueOf(points));
                        html.replace("%objectId%", String.valueOf(target.getObjectId()));
                        activeChar.sendPacket(html);
                        break;
                    case 4:
                        OlympiadManager.getInstance().registerNoble(activeChar);
                        break;
                    case 6:
                        passes = Olympiad.getInstance().getTokensCount(activeChar, false);
                        if (passes > 0)
                        {
                            html.setFile(activeChar.getHtmlPrefix(), Olympiad.OLYMPIAD_HTML_PATH + "noble_settle.htm");
                            html.replace("%objectId%", String.valueOf(target.getObjectId()));
                            activeChar.sendPacket(html);
                        }
                        else
                        {
                            html.setFile(activeChar.getHtmlPrefix(),
                                    Olympiad.OLYMPIAD_HTML_PATH + "noble_nopoints2.htm");
                            html.replace("%objectId%", String.valueOf(target.getObjectId()));
                            activeChar.sendPacket(html);
                        }
                        break;
                    case 9:
                        int point = Olympiad.getInstance().getLastNobleOlympiadPoints(activeChar.getObjectId());
                        html.setFile(activeChar.getHtmlPrefix(), Olympiad.OLYMPIAD_HTML_PATH + "noble_points2.htm");
                        html.replace("%points%", String.valueOf(point));
                        html.replace("%objectId%", String.valueOf(target.getObjectId()));
                        activeChar.sendPacket(html);
                        break;
                    case 10:
                        passes = Olympiad.getInstance().getTokensCount(activeChar, true);
                        if (passes > 0)
                        {
                            L2ItemInstance item = activeChar.getInventory()
                                    .addItem("Olympiad", GATE_PASS, passes, activeChar, target);

                            InventoryUpdate iu = new InventoryUpdate();
                            iu.addModifiedItem(item);
                            activeChar.sendPacket(iu);

                            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
                            sm.addItemName(item);
                            sm.addItemNumber(passes);
                            activeChar.sendPacket(sm);
                        }
                        break;
                    default:
                        _log.warning("Olympiad System: Couldnt send packet for request " + val);
                        break;
                }
            }
            else if (command.toLowerCase().startsWith(COMMANDS[2])) // buff
            {
                if (activeChar.olyBuff <= 0)
                {
                    return false;
                }

                NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());
                String[] params = command.split(" ");

                if (params[1] == null)
                {
                    _log.warning("Olympiad Buffer Warning: npcId = " + target.getNpcId() +
                            " has no buffGroup set in the bypass for the buff selected.");
                    return false;
                }
                int skillId = Integer.parseInt(params[1]);

                L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);

                target.setTarget(activeChar);

                if (activeChar.olyBuff > 0)
                {
                    if (skill != null)
                    {
                        activeChar.olyBuff--;
                        target.broadcastPacket(
                                new MagicSkillUse(target, activeChar, skill.getId(), skill.getLevelHash(), 0, 0, 0));
                        skill.getEffects(activeChar, activeChar);
                        L2PetInstance pet = activeChar.getPet();
                        if (pet != null)
                        {
                            target.broadcastPacket(
                                    new MagicSkillUse(target, pet, skill.getId(), skill.getLevelHash(), 0, 0, 0));
                            skill.getEffects(pet, pet);
                        }
                        for (L2SummonInstance summon : activeChar.getSummons())
                        {
                            target.broadcastPacket(
                                    new MagicSkillUse(target, summon, skill.getId(), skill.getLevelHash(), 0, 0, 0));
                            skill.getEffects(summon, summon);
                        }
                    }
                }

                if (activeChar.olyBuff > 0)
                {
                    String file = "";

                    if (activeChar.hasAwakaned())
                    {
                        file = "olympiad_buffs.htm";
                    }
                    else
                    {
                        file = "olympiad_buffs_notawakened.htm";
                    }

                    html.setFile(activeChar.getHtmlPrefix(), Olympiad.OLYMPIAD_HTML_PATH + file);
                    html.replace("%objectId%", String.valueOf(target.getObjectId()));
                    activeChar.sendPacket(html);
                }
                else
                {
                    html.setFile(activeChar.getHtmlPrefix(), Olympiad.OLYMPIAD_HTML_PATH + "olympiad_nobuffs.htm");
                    html.replace("%objectId%", String.valueOf(target.getObjectId()));
                    activeChar.sendPacket(html);
                    target.deleteMe();
                }
            }
            else if (command.toLowerCase().startsWith(COMMANDS[3])) // olympiad
            {
                int val = Integer.parseInt(command.substring(9, 10));

                NpcHtmlMessage reply = new NpcHtmlMessage(target.getObjectId());

                switch (val)
                {
                    case 1:
                        activeChar.sendPacket(new ExOlympiadInfoList());
                        break;
                    case 2:
                        // for example >> Olympiad 1_88
                        int classId = Integer.parseInt(command.substring(11));
                        if (classId >= 148 && classId <= 181 || classId == 188 || classId == 189 ||
                                classId >= 88 && classId <= 118 || classId >= 131 && classId <= 134 || classId == 136)
                        {
                            List<String> names = Olympiad.getInstance().getClassLeaderBoard(classId);
                            reply.setFile(activeChar.getHtmlPrefix(),
                                    Olympiad.OLYMPIAD_HTML_PATH + "olympiad_ranking.htm");

                            int index = 1;
                            for (String name : names)
                            {
                                reply.replace("%place" + index + "%", String.valueOf(index));
                                reply.replace("%rank" + index + "%", name);
                                index++;
                                if (index > 10)
                                {
                                    break;
                                }
                            }
                            for (; index <= 10; index++)
                            {
                                reply.replace("%place" + index + "%", "");
                                reply.replace("%rank" + index + "%", "");
                            }

                            reply.replace("%objectId%", String.valueOf(target.getObjectId()));
                            activeChar.sendPacket(reply);
                        }
                        break;
                    case 4:
                        activeChar.sendPacket(new ExHeroList());
                        break;
                    default:
                        _log.warning("Olympiad System: Couldnt send packet for request " + val);
                        break;
                }
            }
        }
        catch (Exception e)
        {
            _log.log(Level.INFO, "Exception in " + e.getMessage(), e);
        }

        return true;
    }

    @Override
    public final String[] getBypassList()
    {
        return COMMANDS;
    }
}
