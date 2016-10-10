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

import l2server.gameserver.datatables.CertificateSkillTable;
import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.SubClass;

public class Certificate implements IBypassHandler
{
    private static final String[] COMMANDS = {
            "CertificateSub",
            "LearnSubCertSkills",
            "ResetSubCertificates",
            "CertificateDual",
            "LearnDualCertSkills",
            "ResetDualCertificates"
    };

    @Override
    public boolean useBypass(String command, L2PcInstance player, L2Npc target)
    {
        if (target == null)
        {
            return false;
        }

        if (player.getTemporaryLevel() != 0)
        {
            player.sendMessage("You can't do this while on a temporary level.");
            return false;
        }

        if (command.equalsIgnoreCase("CertificateSub"))
        {
            if (player.getClassIndex() == 0)
            {
                // TODO: Proper message/HTML
                player.sendMessage("To receive certificates you must be on your subclass");
                return false;
            }

            SubClass sub = player.getSubClasses().get(player.getClassIndex());
            int maxCerts = 0;
            if (sub.getLevel() >= 65)
            {
                maxCerts++;
            }
            if (sub.getLevel() >= 70)
            {
                maxCerts++;
            }
            if (sub.getLevel() >= 75)
            {
                maxCerts++;
            }
            if (sub.getLevel() >= 80)
            {
                maxCerts++;
            }

            int subCerts = sub.getCertificates();
            if (subCerts >= maxCerts)
            {
                // TODO: Proper message/HTML
                player.sendMessage("You can't receive more certificates");
                return false;
            }

            sub.setCertificates(maxCerts);
            player.addItem("Certifications", CertificateSkillTable.SUBCLASS_CERTIFICATE, maxCerts - subCerts, player,
                    true);
        }
        else if (command.equalsIgnoreCase("LearnSubCertSkills"))
        {
            if (player.getClassIndex() != 0)
            {
                // TODO: Proper message/HTML
                player.sendMessage("To learn skills you must be on your base class");
                return false;
            }

            L2ItemInstance certsItem = player.getInventory()
                    .getItemByItemId(CertificateSkillTable.SUBCLASS_CERTIFICATE);
            if (certsItem == null)
            {
                // TODO: Proper message/HTML
                player.sendMessage("You don't have certificates");
                return false;
            }

            CertificateSkillTable.getInstance().sendSubClassSkillList(player);
        }
        else if (command.equalsIgnoreCase("ResetSubCertificates"))
        {
            if (player.getClassIndex() != 0)
            {
                // TODO: Proper message/HTML
                player.sendMessage("Your must be on your main class to do this.");
                return false;
            }

            CertificateSkillTable.getInstance().resetSubClassCertificates(player);
            CertificateSkillTable.getInstance().resetDualClassCertificates(player);
            player.resetCertificationSkills();
            // TODO: Proper message/HTML
            player.sendMessage("Subclass certifications reset");
        }
        else if (command.equalsIgnoreCase("CertificateDual"))
        {
            if (player.getClassIndex() != 0)
            {
                // TODO: Proper message/HTML
                player.sendMessage("You must be on your main class to receive Dual Certificates.");
                return false;
            }

            int highestDualClassLevel = 0;

            SubClass dualClass = null;
            for (SubClass s : player.getSubClasses().values())
            {
                if (s.isDual())
                {
                    if (s.getLevel() > highestDualClassLevel)
                    {
                        highestDualClassLevel = s.getLevel();
                    }

                    if (dualClass == null || dualClass.getCertificates() != 0)
                    {
                        dualClass = s;
                    }
                }
            }

            if (highestDualClassLevel == 0)
            {
                player.sendMessage("You do not have any dual class.");
                return false;
            }

            int dualCertificates = CertificateSkillTable.getInstance().getDualClassCertificatesAmount(player);

            if (dualCertificates >= 4)
            {
                player.sendMessage("You already have your 4 dual class certificates.");
                return false;
            }

            if (highestDualClassLevel < 85)
            {
                player.sendMessage("Your dual class should be at least level 86.");
                return false;
            }

            int maxCerts = 0;
            if (highestDualClassLevel >= 85)
            {
                maxCerts++;
            }
            if (highestDualClassLevel >= 90)
            {
                maxCerts++;
            }
            if (highestDualClassLevel >= 95)
            {
                maxCerts++;
            }
            if (highestDualClassLevel >= 99)
            {
                maxCerts++;
            }

            int certificatesCount = maxCerts - dualCertificates;

            //dualClass.setCertificates(maxCerts);
            player.addItem("Certifications", CertificateSkillTable.DUALCLASS_CERTIFICATE, certificatesCount, player,
                    true);
        }
        else if (command.equalsIgnoreCase("LearnDualCertSkills"))
        {
            if (player.getClassIndex() != 0)
            {
                // TODO: Proper message/HTML
                player.sendMessage("To learn skills you must be on your base class");
                return false;
            }

            L2ItemInstance certsItem = player.getInventory()
                    .getItemByItemId(CertificateSkillTable.DUALCLASS_CERTIFICATE);
            if (certsItem == null)
            {
                // TODO: Proper message/HTML
                player.sendMessage("You don't have certificates");
                return false;
            }

            CertificateSkillTable.getInstance().sendDualClassSkillList(player);
        }
        else if (command.equalsIgnoreCase("ResetDualCertificates"))
        {
            if (player.getClassIndex() != 0)
            {
                // TODO: Proper message/HTML
                player.sendMessage("Your must be on your main class to do this.");
                return false;
            }

            CertificateSkillTable.getInstance().resetDualClassCertificates(player);
            player.resetCertificationSkills();
            player.sendMessage("Dual class certifications reset");
        }

        return true;
    }

    @Override
    public String[] getBypassList()
    {
        return COMMANDS;
    }
}
