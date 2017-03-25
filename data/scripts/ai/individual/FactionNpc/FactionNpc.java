package ai.individual.FactionNpc;

import l2server.gameserver.events.Faction.FactionManager;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Inia
 */

public class FactionNpc extends Quest
{
    private static final int factionNpcId = 99999;
    private static final String _qn = "FactionNpc";

    public FactionNpc(int questId, String name, String descr)
    {
        super(questId, name, descr);

        //addSpawn(factionNpcId, -119771, 246318, -1237, 843, false, 0);


        addFirstTalkId(factionNpcId);
        addStartNpc(factionNpcId);
        addTalkId(factionNpcId);


    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        StringBuilder tb = new StringBuilder();
        if (player.getFactionId() == 0)
        {
            tb.append("<html><center><font color=\"3D81A8\">Faction npc:</font><br1>Hello " + player.getName() +
                    " are you ready to join a  <font color=LEVEL>Faction</font> ?<br>");
          tb.append(
                    "<table><tr><td><button ICON=\"QUEST\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over a action=\"bypass -h Quest FactionNpc joinFaction 1\" width=140 height=20 value=\" " +
                            FactionManager.getInstance().getFactionName(1) + " \"  />");
            tb.append("<br>");
            tb.append(
                    "<table><tr><td><button ICON=\"QUEST\" fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over a action=\"bypass -h Quest FactionNpc joinFaction 2\" width=140 height=20 value=\" " +
                            FactionManager.getInstance().getFactionName(2) + " \"  />");
            tb.append("<br>");
            if (player.getFactionId() != 0)
                tb.append(
                        "<button fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over a action=\"bypass -h Quest FactionNpc leaveFaction\" width=140 height=20 value=\"Leave " +
                                FactionManager.getInstance().getFactionName(player.getFactionId()) + " \"  /><br1>");
            tb.append("<br></center>");

            tb.append("</body></html>");
        }
        else
        {
            int factionId = player.getFactionId();
            tb.append("<html><center><font color=\"3D81A8\">Faction " + FactionManager.getInstance().getFactionName(factionId) + "</font><br1> ");
            tb.append("<br>Members: " + FactionManager.getInstance().getFactionMembers(factionId) + " <br>");
            tb.append("Points: " + FactionManager.getInstance().getFactionPoints(factionId) + " <br>");
            tb.append("Kills: " + FactionManager.getInstance().getFactionKills(factionId) + " Deaths: " + FactionManager.getInstance().getFactionDeaths(factionId) + " Team Kill: " + FactionManager.getInstance().getFactionTeamKills(factionId) + "<br>");
            tb.append("Description:  <br>");
            tb.append("" + FactionManager.getInstance().getFactionDesc(factionId) + "<br>");
            tb.append("Your points: " + player.getFactionPoints() + "<br>");

                tb.append(
                        "<button fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over a action=\"bypass -h Quest FactionNpc leaveFaction\" width=140 height=20 value=\"Leave " +
                                FactionManager.getInstance().getFactionName(player.getFactionId()) + " \"  /><br1>");
            tb.append("<br></center>");

            tb.append("</body></html>");
        }
        NpcHtmlMessage msg = new NpcHtmlMessage(factionNpcId);
        msg.setHtml(tb.toString());
        player.sendPacket(msg);
        return "";
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (event.startsWith("joinFaction"))
        {
            int factionId = Integer.valueOf(event.split(" ")[1]);

            StringBuilder tb = new StringBuilder();

            if (player.getFactionId() == 0)
            {
                tb.append("<html><center><font color=\"3D81A8\">Faction " + FactionManager.getInstance().getFactionName(factionId) + "</font><br1> ");
                tb.append("<br>Members: " + FactionManager.getInstance().getFactionMembers(factionId) + " <br>");
                tb.append("Points: " + FactionManager.getInstance().getFactionPoints(factionId) + " <br>");
                tb.append("Kills: " + FactionManager.getInstance().getFactionKills(factionId) + " Deaths: " + FactionManager.getInstance().getFactionDeaths(factionId) + " Team Kill: " + FactionManager.getInstance().getFactionTeamKills(factionId) + "<br>");
                tb.append("Description:  <br>");
                tb.append("" + FactionManager.getInstance().getFactionDesc(factionId) + "<br>");
                tb.append("Level minimum: " + FactionManager.getInstance().getFactionMinLvl(factionId) + "<br>");
                tb.append("PvP minimum: " + FactionManager.getInstance().getFactionMinPvp(factionId) + "<br>");
                tb.append("<button  fore=L2UI_CT1.Button_DF_Calculator back=L2UI_CT1.Button_DF_Calculator_Over a action=\"bypass -h Quest FactionNpc reqJoin " + factionId + "\" width=70 height=20 value=\"Join!\" />");
                tb.append("</body></html>");
            }

            NpcHtmlMessage msg = new NpcHtmlMessage(factionNpcId);
            msg.setHtml(tb.toString());
            player.sendPacket(msg);
            return "";


        }
        else if (event.startsWith("reqJoin"))
        {
            int factionId = Integer.valueOf(event.split(" ")[1]);
            if (player.getFactionId() != 0)
            {
                if (player.getFactionId() == factionId)
                {
                    player.sendMessage("You're already in " + FactionManager.getInstance().getFactionName(factionId) + " faction ");
                    return "";
                }
                player.sendMessage("You already have a faction!");
                player.sendMessage("Current faction: " + FactionManager.getInstance().getFactionName(player.getFactionId()));
                return "";
            }
            player.setFactionId(factionId);
            StringBuilder tb = new StringBuilder();

            tb.append("<html><center><font color=\"3D81A8\">Faction " + FactionManager.getInstance().getFactionName(factionId) + "</font><br1> ");
            tb.append("You joined the faction: " + FactionManager.getInstance().getFactionName(factionId) + "!");
            tb.append("</body></html>");

            NpcHtmlMessage msg = new NpcHtmlMessage(factionNpcId);
            msg.setHtml(tb.toString());
            player.sendPacket(msg);
            return "";
        }
        else if (event.startsWith("leaveFaction"))
        {
            if (player.getFactionId() == 0)
            {
                player.sendMessage("You're not in any faction!");
                return "";
            }
            player.sendMessage("You left " + FactionManager.getInstance().getFactionName(player.getFactionId()) + "!");
            player.setFactionId(0);
            return "";
        }
        else if (event.startsWith("descFaction"))
        {
            int factionId = Integer.valueOf(event.split(" ")[1]);

            StringBuilder tb = new StringBuilder();
            tb.append("<html><center><font color=\"3D81A8\">Faction npc:</font></center><body> ");
            tb.append(" o: " + FactionManager.getInstance().getFactionDesc(factionId));

            tb.append("</body></html>");

            NpcHtmlMessage msg = new NpcHtmlMessage(factionNpcId);
            msg.setHtml(tb.toString());
            player.sendPacket(msg);
            return "";

        }

        return super.onAdvEvent(event, npc, player);
    }


    public static void main(String[] args)
    {
        new FactionNpc(-1, _qn, "ai/individual");
    }
}
