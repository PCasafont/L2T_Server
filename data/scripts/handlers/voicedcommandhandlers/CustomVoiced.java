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

import java.text.DecimalFormat;
import java.util.Calendar;

import l2server.Config;
import l2server.gameserver.communitybbs.Manager.CustomCommunityBoard;
import l2server.gameserver.events.HiddenChests;
import l2server.gameserver.handler.IVoicedCommandHandler;
import l2server.gameserver.instancemanager.CustomOfflineBuffersManager;
import l2server.gameserver.instancemanager.DiscussionManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.CharInfo;
import l2server.gameserver.network.serverpackets.ExVoteSystemInfo;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.UserInfo;
import l2server.gameserver.stats.BaseStats;
import l2server.gameserver.stats.Stats;

public class CustomVoiced implements IVoicedCommandHandler
{
    private static final String[] VOICED_COMMANDS = {
            "itemid", // shows item ID of an item
            "event", // shows information about the event
            "noexp", // ignores XP/SP gain when hunting
            "time", // shows server current time (clock)
            "vote", // to vote in a poll created by an admin
            "remember", // allows the player to remember 3rd class skills
            "landrates", // shows chance of landing skills inflicted and received
            "myhiddenstats", // opens the hidden stats information window
            "refusebuff", // cannot receive buffs until restart or re-type of the command
            "blockrequests", // blocks all kind of requests, party, trade, clan etc
            "blockpms", // blocks the incoming pms
            "refusekillinfo", // blocks the kill info pop up
            "disableweaponglow", // disable the weapon enchant glow
            "disablearmorglow", // disable the armor enchant glow
            "disablenicknamewings", // disable the nickname wings
            "hidecloak", // hide the cloak
            "mammon", // shows blacksmith and merchant of mammon locations
            "stabs", // shows stab angle
            "unrec", // deletes recommendations
            "treasure", // opens the hidden chests event information window
            "clones", // opens the clones event information window
            "offlinebuffer"
    };

    /**
     * @see l2server.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, l2server.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
     */
    @Override
    public boolean useVoicedCommand(String command, L2PcInstance player, String params)
    {
        if (command.equalsIgnoreCase("itemid"))
        {
            player.setIsItemId(true);
            player.sendMessage("Double click on the item you want to know its itemid.");
        }
        else if (command.equalsIgnoreCase("offlinebuffer"))
        {
            CustomOfflineBuffersManager.getInstance().offlineBuffPannel(player);
        }
        else if (command.equalsIgnoreCase("event"))
        {
            if (Config.INSTANCED_EVENT_ENABLED)
            {
                CustomCommunityBoard.getInstance().parseCmd("_bbscustom;currentEvent", player);
            }
        }
        else if (command.equalsIgnoreCase("remember"))
        {
            if (!player.isInsideZone(L2Character.ZONE_TOWN))
            {
                player.sendMessage("You cannot use the remember mode outside of town!");
                return true;
            }

            player.setRememberSkills(!player.isRememberSkills());
            if (player.isRememberSkills())
            {
                player.sendMessage(
                        "Now you will be able to remember old skills until you type again .remember or log out.");
            }
            else
            {
                player.sendMessage("Remember Mode turned OFF");
            }
        }
        else if (command.equalsIgnoreCase("landrates"))
        {
            player.setLandRates(!player.isLandRates());
            if (player.isLandRates())
            {
                player.sendMessage(
                        "Now you will see all the skill land rates until you type again .landrates or log out.");
            }
            else
            {
                player.sendMessage("Land Rates Mode turned OFF");
            }
        }
        else if (command.equalsIgnoreCase("blockrequests"))
        {
            player.setIsRefusingRequests(!player.getIsRefusingRequests());
            if (player.getIsRefusingRequests())
            {
                player.sendMessage("Now you won't receive any request!");
            }
            else
            {
                player.sendMessage("Refuse requests turned OFF");
            }
        }
        else if (command.equalsIgnoreCase("blockpms"))
        {
            player.setMessageRefusal(!player.getMessageRefusal());
            if (player.getMessageRefusal())
            {
                player.sendMessage("Now you won't receive any PM!");
            }
            else
            {
                player.sendMessage("Refuse PM turned OFF");
            }
        }
        else if (command.equalsIgnoreCase("refusekillinfo"))
        {
            player.setIsRefusalKillInfo(!player.getIsRefusalKillInfo());
            if (player.getIsRefusalKillInfo())
            {
                player.sendMessage("Now you won't receive any kill info pop-up window!");
            }
            else
            {
                player.sendMessage("Refuse Kill Info turned OFF");
            }
        }
        else if (command.equalsIgnoreCase("disableweaponglow"))
        {
            player.setIsWeaponGlowDisabled(!player.getIsWeaponGlowDisabled());
            if (player.getIsWeaponGlowDisabled())
            {
                player.sendMessage("Now you won't see your own Weapon Enchant Glow!");
            }
            else
            {
                player.sendMessage("Disabled Weapon Enchant glow turned OFF");
            }
        }
        else if (command.equalsIgnoreCase("disablearmorglow"))
        {
            player.setIsArmorGlowDisabled(!player.getIsArmorGlowDisabled());
            if (player.getIsArmorGlowDisabled())
            {
                player.sendMessage("Now you won't see your own Armor Enchant Glow!");
            }
            else
            {
                player.sendMessage("Disabled Armor Enchant glow turned OFF");
            }
        }
        else if (command.equalsIgnoreCase("disablenicknamewings"))
        {
            player.setNickNameWingsDisabled(!player.isNickNameWingsDisabled());

            for (L2PcInstance pl : player.getKnownList().getKnownPlayers().values())
            {
                if (pl == null)
                {
                    continue;
                }
                player.sendPacket(new CharInfo(pl));
            }

            if (player.isNickNameWingsDisabled())
            {
                player.sendMessage("Now you won't see the nickname wings!");
            }
            else
            {
                player.sendMessage("Disable Nick Wings turned OFF");
            }
        }
        else if (command.equalsIgnoreCase("hidecloak"))
        {
            player.setCloakHidden(!player.isCloakHidden());

            if (player.isCloakHidden())
            {
                player.sendMessage("Your cloak has been hidden!");
            }
            else
            {
                player.sendMessage("Disable Cloak turned OFF");
            }
        }
        else if (command.equalsIgnoreCase("noexp"))
        {
            player.setNoExp(!player.isNoExp());
            if (player.isNoExp())
            {
                player.sendMessage("Now you won't receive experience until you type again .noexp or log out.");
            }
            else
            {
                player.sendMessage("No Exp Mode turned OFF");
            }
        }
        else if (command.equalsIgnoreCase("myhiddenstats"))
        {
            sendHiddenStats(player);
        }
        else if (command.equalsIgnoreCase("time"))
        {
            Calendar currentTime = Calendar.getInstance();
            String day = String.valueOf(currentTime.get(Calendar.DAY_OF_MONTH));
            if (day.length() == 1)
            {
                day = "0" + day;
            }
            String month = String.valueOf(currentTime.get(Calendar.MONTH) + 1); // January = 0
            if (month.length() == 1)
            {
                month = "0" + month;
            }
            String year = String.valueOf(currentTime.get(Calendar.YEAR));
            String hour = String.valueOf(currentTime.get(Calendar.HOUR_OF_DAY));
            if (hour.length() == 1)
            {
                hour = "0" + hour;
            }
            String minute = String.valueOf(currentTime.get(Calendar.MINUTE));
            if (minute.length() == 1)
            {
                minute = "0" + minute;
            }
            String time = day + "/" + month + "/" + year + ", " + hour + ":" + minute;
            player.sendMessage("The current time is " + time + ".");
        }
        else if (command.equalsIgnoreCase("vote"))
        {
            try
            {
                if (!DiscussionManager.getInstance().areVotesEnabled())
                {
                    player.sendMessage("You can't vote for anything right now.");
                    return false;
                }
                byte option = Byte.parseByte(params);
                if (DiscussionManager.getInstance().vote(player.getObjectId(), option))
                {
                    player.sendMessage("You have voted for the option " + option + ".");
                }
                else
                {
                    player.sendMessage("You have voted already!");
                    return false;
                }
            }
            catch (Exception e)
            {
                player.sendMessage("You must specify a numeric option after the command!");
                return false;
            }
        }
        else if (command.equalsIgnoreCase("refusebuff"))
        {
            player.setRefuseBuffs(!player.isRefusingBuffs());

            if (player.isRefusingBuffs())
            {
                player.sendMessage(
                        "Now you won't receive buffs from out of party anymore until you type again .refusebuff or log out.");
            }
            else
            {
                player.sendMessage("You are not refusing buffs from out of party anymore");
            }
        }
        else if (command.equalsIgnoreCase("stabs"))
        {
            player.setShowStabs(!player.isShowingStabs());

            if (player.isShowingStabs())
            {
                player.sendMessage("Stab direction display activated.");
            }
            if (!player.isShowingStabs())
            {
                player.sendMessage("Stab direction display deactivated.");
            }
        }
        else if (command.equalsIgnoreCase("unrec"))
        {
            player.setRecomHave(0);
            player.broadcastUserInfo();
            player.sendPacket(new UserInfo(player));
            player.sendPacket(new ExVoteSystemInfo(player));
            player.sendMessage("Recommendation points deleted.");
        }
        else if (command.equalsIgnoreCase("treasure"))
        {
            HiddenChests.getInstance().showInfo(player);
        }
        else if (command.equalsIgnoreCase("clones"))
        {
            //CloneInvasion.getInstance().showInfo(player);
        }

        return true;
    }

    private void sendHiddenStats(L2PcInstance player)
    {
        String html = "<html>" + "<body>" +
                "<center><table><tr><td><img src=icon.etc_alphabet_h_i00 width=32 height=32></td><td><img src=icon.etc_alphabet_i_i00 width=32 height=32></td><td><img src=icon.etc_alphabet_d_i00 width=32 height=32></td><td><img src=icon.etc_alphabet_d_i00 width=32 height=32></td><td><img src=icon.etc_alphabet_e_i00 width=32 height=32></td><td><img src=icon.etc_alphabet_n_i00 width=32 height=32></td></tr></table></center>" +
                "<center><table><tr><td><img src=icon.etc_alphabet_s_i00 width=32 height=32></td><td><img src=icon.etc_alphabet_t_i00 width=32 height=32></td><td><img src=icon.etc_alphabet_a_i00 width=32 height=32></td><td><img src=icon.etc_alphabet_t_i00 width=32 height=32></td><td><img src=icon.etc_alphabet_s_i00 width=32 height=32></td></tr></table></center><br><br>" +
                "<center><button value=\"Reload!\" width=103 height=24 action=\"bypass -h voice .myhiddenstats\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over></center>" +
                "<center><table width=300><tr><td FIXWIDTH=150>Stat</td><td FIXWIDTH=150>Value</td</tr></table><img src=\"L2UI.Squaregray\" width=300 height=1>";

        html += getStatHtm("Skill Mastery", player.getSkillMastery(), 0, false, "%");
        html += getStatHtm("Shield Def", player.getShldDef(), 0, false, "");
        html += getStatHtm("Shield Def Angle", player.calcStat(Stats.SHIELD_DEFENCE_ANGLE, 120, null, null), 120, false,
                "");
        html += getStatHtm("Dmg Absorbed", player.calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null), 0, false, "%");
        html += getStatHtm("Heals received", player.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Heals given", player.calcStat(Stats.HEAL_PROFICIENCY, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Momentum Power", player.calcStat(Stats.MOMENTUM_POWER, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Momentum Critical Rate", player.calcStat(Stats.MOMENTUM_CRIT_RATE, 100, null, null) - 100,
                0, true, "%");
        html += getStatHtm("Max Targets", player.calcStat(Stats.ATTACK_COUNT_MAX, 1, null, null) - 1, 0, true, "");
        html += getStatHtm("P. Skill Power", player.calcStat(Stats.PHYSICAL_SKILL_POWER, 100, null, null) - 100, 0,
                true, "%");
        html += getStatHtm("P. Crit Dmg", player.calcStat(Stats.CRITICAL_DAMAGE, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("P. Crit Dmg Recvd", player.calcStat(Stats.CRIT_VULN, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("P. Skill Crit Rate", player.calcStat(Stats.PCRITICAL_RATE, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("P. Skill Crit Dmg", player.calcStat(Stats.PSKILL_CRIT_DMG, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Additional DEX P. Skill Rate",
                player.calcStat(Stats.PSKILL_CRIT_PER_DEX, 0, null, null) * (BaseStats.DEX.calcBonus(player) - 1) * 100,
                0, true, "%");
        html += getStatHtm("M. Skill Power", player.calcStat(Stats.MAGIC_SKILL_POWER, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("M. Crit Dmg", player.calcStat(Stats.MAGIC_CRIT_DMG, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("M. Crit Dmg Recvd", player.calcStat(Stats.MAGIC_CRIT_VULN, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Fixed P. Crit Dmg", player.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, null, null), 0, true, "");
        html += getStatHtm("Fixed P. Crit Dmg Res", -player.calcStat(Stats.CRIT_ADD_VULN, 0, null, null), 0, true, "");
        html += getStatHtm("Fixed M. Crit Dmg", player.calcStat(Stats.MAGIC_CRIT_DMG_ADD, 0, null, null), 0, true, "");
        html += getStatHtm("Front Critical Rate", player.calcStat(Stats.CRITICAL_RATE_FRONT, 100, null, null) - 100, 0,
                true, "%");
        html += getStatHtm("Front Critical Dmg", player.calcStat(Stats.CRITICAL_DMG_FRONT, 100, null, null) - 100, 0,
                true, "%");
        html += getStatHtm("Behind Critical Rate", player.calcStat(Stats.CRITICAL_RATE_BEHIND, 100, null, null) - 100,
                0, true, "%");
        html += getStatHtm("Behind Critical Dmg", player.calcStat(Stats.CRITICAL_DMG_BEHIND, 100, null, null) - 100, 0,
                true, "%");
        html += getStatHtm("Side Critical Rate", player.calcStat(Stats.CRITICAL_RATE_SIDE, 100, null, null) - 100, 0,
                true, "%");
        html += getStatHtm("Side Critical Dmg", player.calcStat(Stats.CRITICAL_DMG_SIDE, 100, null, null) - 100, 0,
                true, "%");
        html += getStatHtm("Reflected Debuff", player.calcStat(Stats.REFLECT_DEBUFFS, 0, null, null), 0, true, "%");
        html += getStatHtm("Reflected Dmg", player.calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, null, null), 0, true, "%");
        html += getStatHtm("Reflected Res", 100 - player.calcStat(Stats.REFLECT_VULN, 100, null, null), 0, true, "%");
        html += getStatHtm("M. MP Consume Rate", player.calcStat(Stats.MAGICAL_MP_CONSUME_RATE, 100, null, null), 0,
                false, "%");
        html += getStatHtm("P. MP Consume Rate", player.calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, 100, null, null), 0,
                false, "%");
        html += getStatHtm("P. Skill Evasion Rate", player.calcStat(Stats.P_SKILL_EVASION, 100, null, null) - 100, 0,
                false, "%");
        html += getStatHtm("M. Skill Evasion Rate", player.calcStat(Stats.M_SKILL_EVASION, 0, null, null), 0, false,
                "%");
        html += getStatHtm("PvP P. Dmg", player.calcStat(Stats.PVP_PHYSICAL_DMG, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("PvP P. Skill Dmg", player.calcStat(Stats.PVP_PHYS_SKILL_DMG, 100, null, null) - 100, 0,
                true, "%");
        html += getStatHtm("PvP M. Dmg", player.calcStat(Stats.PVP_MAGICAL_DMG, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("PvP P. Dmg Res", player.calcStat(Stats.PVP_PHYSICAL_DEF, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("PvP P. Skill Dmg Res", player.calcStat(Stats.PVP_PHYS_SKILL_DEF, 100, null, null) - 100, 0,
                true, "%");
        html += getStatHtm("PvP M. Dmg Res", player.calcStat(Stats.PVP_MAGICAL_DEF, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("PvE P. Dmg", player.calcStat(Stats.PVE_PHYSICAL_DMG, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("PvE P. Skill Dmg", player.calcStat(Stats.PVE_PHYS_SKILL_DMG, 100, null, null) - 100, 0,
                true, "%");
        html += getStatHtm("PvE M. Dmg", player.calcStat(Stats.PVE_MAGICAL_DMG, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("PvE P. Dmg Res", player.calcStat(Stats.PVE_PHYSICAL_DEF, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("PvE P. Skill Dmg Res", player.calcStat(Stats.PVE_PHYS_SKILL_DEF, 100, null, null) - 100, 0,
                true, "%");
        html += getStatHtm("PvE M. Dmg Res", player.calcStat(Stats.PVE_MAGICAL_DEF, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Debuff Res", player.calcStat(Stats.DEBUFF_RES, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Physical Debuff Res", player.calcStat(Stats.PHYS_DEBUFF_RES, 100, null, null) - 100, 0,
                true, "%");
        html += getStatHtm("Magical Debuff Res", player.calcStat(Stats.MENTAL_DEBUFF_RES, 100, null, null) - 100, 0,
                true, "%");
        html += getStatHtm("Stun Res", player.calcStat(Stats.STUN_RES, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Paralysis Res", player.calcStat(Stats.PARALYSIS_RES, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Petrify Res", player.calcStat(Stats.PETRIFY_RES, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Hold Res", player.calcStat(Stats.HOLD_RES, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Sleep Res", player.calcStat(Stats.SLEEP_RES, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Knock Down Res", player.calcStat(Stats.KNOCK_DOWN_RES, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Knock Back Res", player.calcStat(Stats.KNOCK_BACK_RES, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Pull Res", player.calcStat(Stats.PULL_RES, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Aerial Yoke Res", player.calcStat(Stats.AERIAL_YOKE_RES, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Mental Res", player.calcStat(Stats.DERANGEMENT_RES, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Poison Res", player.calcStat(Stats.POISON_RES, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Bleed Res", player.calcStat(Stats.BLEED_RES, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Cancel Res", player.calcStat(Stats.CANCEL_RES, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Debuff Prof", player.calcStat(Stats.DEBUFF_PROF, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Physical Debuff Prof", player.calcStat(Stats.PHYS_DEBUFF_PROF, 100, null, null) - 100, 0,
                true, "%");
        html += getStatHtm("Magical Debuff Prof", player.calcStat(Stats.MENTAL_DEBUFF_PROF, 100, null, null) - 100, 0,
                true, "%");
        html += getStatHtm("Stun Prof", player.calcStat(Stats.STUN_PROF, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Paralysis Prof", player.calcStat(Stats.PARALYSIS_PROF, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Petrify Prof", player.calcStat(Stats.PETRIFY_PROF, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Hold Prof", player.calcStat(Stats.HOLD_PROF, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Sleep Prof", player.calcStat(Stats.SLEEP_PROF, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Knock Down Prof", player.calcStat(Stats.KNOCK_DOWN_PROF, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Knock Back Prof", player.calcStat(Stats.KNOCK_BACK_PROF, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Pull Prof", player.calcStat(Stats.PULL_PROF, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Aerial Yoke Prof", player.calcStat(Stats.AERIAL_YOKE_PROF, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Mental Prof", player.calcStat(Stats.DERANGEMENT_PROF, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Poison Prof", player.calcStat(Stats.POISON_PROF, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Bleed Prof", player.calcStat(Stats.BLEED_PROF, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Cancel Prof", player.calcStat(Stats.CANCEL_PROF, 100, null, null) - 100, 0, true, "%");

        //Sharing stats
        html += getStatHtm("Sharing P.Atk", player.calcStat(Stats.OWNER_PATK, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Sharing M.Atk", player.calcStat(Stats.OWNER_MATK, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Sharing P. Spd", player.calcStat(Stats.OWNER_PATKSPD, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Sharing M. Spd", player.calcStat(Stats.OWNER_MATKSPD, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Sharing P.Def", player.calcStat(Stats.OWNER_PDEF, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Sharing M.Def", player.calcStat(Stats.OWNER_MDEF, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Sharing HP", player.calcStat(Stats.OWNER_MAXHP, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Sharing MP", player.calcStat(Stats.OWNER_MAXMP, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Sharing Crit", player.calcStat(Stats.OWNER_CRIT, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Sharing Crit Dmg", player.calcStat(Stats.OWNER_CRIT_DMG, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Sharing PvP & PvE", player.calcStat(Stats.OWNER_PVP_PVE, 100, null, null) - 100, 0, true,
                "%");

        //Rates
        html += getStatHtm("XP Gain", player.calcStat(Stats.EXP_RATE, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("SP Gain", player.calcStat(Stats.SP_RATE, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Drop Gain", player.calcStat(Stats.DROP_RATE, 100, null, null) - 100, 0, true, "%");

        //Weapon stuff
        html += getStatHtm("Sword Vulnerability", player.calcStat(Stats.SWORD_WPN_VULN, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Big Sword Vulnerability", player.calcStat(Stats.BIGSWORD_WPN_VULN, 100, null, null) - 100,
                0, true, "%");
        html += getStatHtm("Blunt Vulnerability", player.calcStat(Stats.BLUNT_WPN_VULN, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Big Blunt Vulnerability", player.calcStat(Stats.BIGBLUNT_WPN_VULN, 100, null, null) - 100,
                0, true, "%");
        html += getStatHtm("Dagger Vulnerability", player.calcStat(Stats.DAGGER_WPN_VULN, 100, null, null) - 100, 0,
                true, "%");
        html += getStatHtm("Dual Dagger Vulnerability",
                player.calcStat(Stats.DUALDAGGER_WPN_VULN, 100, null, null) - 100, 0, true, "%");
        html += getStatHtm("Bow Vulnerability", player.calcStat(Stats.BOW_WPN_VULN, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Crossbow Vulnerability", player.calcStat(Stats.CROSSBOW_WPN_VULN, 100, null, null) - 100, 0,
                true, "%");
        html += getStatHtm("Pole Vulnerability", player.calcStat(Stats.POLE_WPN_VULN, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Dual Vulnerability", player.calcStat(Stats.DUAL_WPN_VULN, 100, null, null) - 100, 0, true,
                "%");
        html += getStatHtm("Fists Vulnerability", player.calcStat(Stats.DUALFIST_WPN_VULN, 100, null, null) - 100, 0,
                true, "%");

        html += "</table><br>" + "</body></html>";
        player.sendPacket(new NpcHtmlMessage(0, html));
    }

    private String getStatHtm(String statName, double statVal, double statDefault, boolean plusIfPositive, String suffix)
    {
        if (statVal == statDefault)
        {
            return "";
        }

        return "<table width=300 border=0><tr><td FIXWIDTH=150>" + statName + ":</td><td FIXWIDTH=150>" +
                (plusIfPositive && statVal >= 0 ? "+" : "") + new DecimalFormat("#0.##").format(statVal) + suffix +
                "</td></tr></table><img src=\"L2UI.Squaregray\" width=300 height=1>";
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
