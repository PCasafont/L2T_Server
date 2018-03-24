package events.DCEvent;

import com.sun.corba.se.impl.oa.toa.TOA;
import events.DCEvent.DCEvent;
import l2server.gameserver.Announcements;
import l2server.gameserver.GmListTable;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.events.RankingKillInfo;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.PlayerClass;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestTimer;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.NpcStringId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.gameserver.util.Broadcast;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Inia
 *
 */

public class DCEvent extends Quest
{
    //Config
    private static final boolean _exChangeOnly = false;
    private static int _timeToEndInvasion = 1; //Minutes
    private static final int _npcId = 92000;
    private static final int[] _invaderIds = {92011, 92010};
    private static final int _boosId = 92011;

    //Vars
    private static Long _nextInvasion;
    private static L2PcInstance _player;
    private static boolean _isUnderInvasion = false;
    private Map<Integer, invaderInfo> _attackInfo = new HashMap<Integer, invaderInfo>();
    private ArrayList<L2Character> _invaders = new ArrayList<L2Character>();
    private ArrayList<L2Character> _registered = new ArrayList<L2Character>();
    private Map<L2Character, Integer> _testPlayer = new HashMap<L2Character, Integer>();

    public DCEvent(int id, String name, String descr)
    {
        super(id, name, descr);

        addStartNpc(_npcId);
        addTalkId(_npcId);
        addFirstTalkId(_npcId);

        for (int mob : _invaderIds)
        {
            addAttackId(mob);
            addKillId(mob);
        }

    }

    private class invaderInfo
    {
        private Long _attackedTime;
        private int _playerId;
        private String _externalIP;
        private String _internalIP;

        private invaderInfo(int playerId, String externalIP, String internalIP)
        {
            _playerId = playerId;
            _externalIP = externalIP;
            _internalIP = internalIP;
            setAttackedTime();
        }

        private long getAttackedTime()
        {
            return _attackedTime;
        }

        private void setAttackedTime()
        {
            _attackedTime = System.currentTimeMillis();
        }

        private int getPlayerId()
        {
            return _playerId;
        }

        private String getExternalIP()
        {
            return _externalIP;
        }

        private String getInternalIP()
        {
            return _internalIP;
        }

        private void updateInfo(int playerId, String externalIP, String internalIP)
        {
            _playerId = playerId;
            _externalIP = externalIP;
            _internalIP = internalIP;
            setAttackedTime();
        }
    }



    @Override
    public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
    {


        L2Npc inv = addSpawn(_invaderIds[Rnd.get(_invaderIds.length)], npc.getX() + Rnd.get(100),
                npc.getY() + Rnd.get(100), npc.getZ(), 0, false, 0);
        _invaders.add(inv);
        int points = _testPlayer.get(player);
        int toAdd = 1;


        _testPlayer.replace(player, points + toAdd);

        return "";
    }

    @Override
    public String onDieZone(L2Character character, L2Character killer, L2ZoneType zone)
    {
        if (_isUnderInvasion)
        {
            L2PcInstance player = killer.getActingPlayer();
            if (player != null)
            {
                player.increasePvpKills(character);
                player.addItem("coin", 4037, 1, player, true);
            }
        }
        return null;
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        if (_isUnderInvasion)
        {
            StringBuilder tb = new StringBuilder();
            tb.append("<html><center><font color=\"3D81A8\">Melonis!</font></center><br1>Hi " +
                    player.getName() +
                    "<br> The event is already started.<br><Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h Quest DCEvent teleport\">Teleport to event !</Button>");
            if (player.isGM())
            {
                tb.append("<html><center> <br> GM Panel<br><Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h Quest DCEvent end_invasion\">Stop event</Button>");
            }
            NpcHtmlMessage msg = new NpcHtmlMessage(_npcId);
            msg.setHtml(tb.toString());
            player.sendPacket(msg);
            return ("");
        }

        return "DCEventShop.html";
    }

    @SuppressWarnings("unused")
    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        if (event.startsWith("trySpawnBoss"))
        {
            _timeToEndInvasion = Integer.valueOf(event.split(" ")[1]);

            int _price = 0;
            if (_timeToEndInvasion == 5)
            {
                _price = 5;
            }
            else if (_timeToEndInvasion == 10)
            {
                _price = 8;
            }
            else if (_timeToEndInvasion == 15)
            {
                _price = 12;
            }
            if (!player.destroyItemByItemId("coin", 4037, _price, player, true))
            {
                StringBuilder tb = new StringBuilder();
                tb.append("<html><center><font color=\"3D81A8\">Melonis!</font></center><br1>Sorry " +
                        player.getName() + " but I need  <font color=LEVEL>" + _price + "</font> Coins of luck<br>");
                NpcHtmlMessage msg = new NpcHtmlMessage(_npcId);
                msg.setHtml(tb.toString());
                player.sendPacket(msg);
                return ("");
            }
            Broadcast.toAllOnlinePlayers(new ExShowScreenMessage(
                    player.getName() + " started the dragon claw event for  " + _timeToEndInvasion + " minutes !", 7000));

            startQuestTimer("start_invasion", 1, null, null);
        }
        else if (event.equalsIgnoreCase("teleport"))
        {
            player.teleToLocation(-54481, -69402, -3416, true);

            _testPlayer.put(player, 0);
            _registered.add(player);
        }
        else if (event.equalsIgnoreCase("teleport_back"))
        {
            player.teleToLocation(-114435, 253417, -1546, true);
        }
        else if (event.equalsIgnoreCase("eventMusic"))
        {
            int rnd = Rnd.get(4) + 1;
            player.sendPacket(new PlaySound(1, "CC_0" + rnd, 0, 0, 0, 0, 0));
        }
        else if (event.startsWith("start_talk"))
        {
            Announcements.getInstance().announceToAll("1 2 3!");
        }
        else if (event.startsWith("start_invasion"))
        {
            if (_isUnderInvasion)
            {
                return "";
            }

            _isUnderInvasion = true;

            addSpawn(_npcId, -114358, 253164, -1541, 24266, false, _timeToEndInvasion);

            int minX = -53749;
            int maxX = -55287;
            int minY = -68835;
            int maxY = -70258;
            int radius = 500;

            for (int a = 0; a < 3; a++)
            {
                for (int i = 0; i < 50; i++)
                {
                    int x = Rnd.get(minX, maxX) + 1;
                    int y = Rnd.get(minY, maxY) + 1;
                    int x2 = (int) (radius * Math.cos(i * 0.618));
                    int y2 = (int) (radius * Math.sin(i * 0.618));

                    L2Npc inv =
                            addSpawn(_invaderIds[Rnd.get(_invaderIds.length)], x, y, -3416 + 20, -1,
                                    false, 0, false, 0);

                    _invaders.add(inv);
                }
                radius += 300;
            }

            startQuestTimer(event.equalsIgnoreCase("start_invasion") ? "end_invasion" : "end_invasion_gm", _timeToEndInvasion * 60000, null, null);
        }
        else if (event.startsWith("spawn_boss"))
        {



            startQuestTimer("delete_boss", 60000, null, null);
            L2Npc boss = addSpawn(_boosId, -54481, -69402, -3416, 0, false, 0, true);
            _invaders.add(boss);
        }
        else if (event.startsWith("delete_boss"))
        {
            for (L2Character delete : _invaders)
            {
                if (delete == null)
                {
                    continue;
                }
                delete.deleteMe();
            }

            for (L2Character test : _registered)
            {
                if (test == null)
                {
                    continue;
                }
                test.teleToLocation(-114435, 253417, -1546, true);
            }
            _invaders.clear();
            _registered.clear();
            _attackInfo.clear();
            Announcements.getInstance().announceToAll("FINISHED");
            _isUnderInvasion = false;
        }
        else if (event.startsWith("end_invasion"))
        {
            _isUnderInvasion = false;

            if (event.equalsIgnoreCase("end_invasion_gm_force"))
            {
                QuestTimer timer = getQuestTimer("end_invasion_gm", null, null);
                if (timer != null)
                {
                    timer.cancel();
                }
            }

            for (L2Character chara : _invaders)
            {
                if (chara == null)
                {
                    continue;
                }
                chara.deleteMe();
            }

            for (Map.Entry<L2Character, Integer> ontest : _testPlayer.entrySet())
            {
                if (ontest == null)
                {
                    continue;
                }
                L2Character toTp = ontest.getKey();
                int totalPoints = ontest.getValue();

                L2PcInstance _oui = (L2PcInstance) ontest.getKey();
                Announcements.getInstance().announceToAll("Player : " + toTp.getName() + " Points 1" + totalPoints);
                _oui.addItem("coin", 36414, totalPoints, _oui, true);
                toTp.teleToLocation(-114435, 253417, -1546, true);
            }



            _registered.clear();
            _invaders.clear();
            _attackInfo.clear();
            _isUnderInvasion = false;
            Announcements.getInstance().announceToAll("Event finished !");

            //Only schedule the next invasion if is not started by a GM

        }
        return "";
    }


    public static void main(String[] args)
    {
        new DCEvent(-1, "DCEvent", "events");
    }
}