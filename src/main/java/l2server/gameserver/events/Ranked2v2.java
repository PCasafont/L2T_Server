package l2server.gameserver.events;

import l2server.L2DatabaseFactory;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.communitybbs.Manager.CustomCommunityBoard;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.util.NpcUtil;
import l2server.log.Log;
import l2server.util.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;


/**
 * @author Inia
 *
 */
public class Ranked2v2
{

    public static enum State{INACTIVE,REGISTER,LOADING,FIGHT}
    public static State state = State.INACTIVE;
    public static final int timeEach = 1;
    public static Vector<L2PcInstance> players = new Vector<>();
    public static Map<L2PcInstance, Long> fighters = new HashMap<L2PcInstance, Long>();
    public static Vector<L2PcInstance> teamOne = new Vector<>();
    public static Vector<L2PcInstance> teamTwo = new Vector<>();

    public void checkRegistered()
    {
        if (teamTwo.isEmpty() && teamOne.isEmpty())
        {
            if (players.size() < 4)
            {
                return;
            }
        }
        if (!teamOne.isEmpty() && players.size() < 2)
            return;
        if (!teamTwo.isEmpty() && players.size() < 2)
            return;

        state = State.REGISTER;

        if (teamOne.isEmpty())
        {
            int rnd1=Rnd.get(players.size());
            int rnd2=Rnd.get(players.size());



            while(rnd2==rnd1)
                rnd2=Rnd.get(players.size());

            L2PcInstance player1 = players.get(rnd1);
            L2PcInstance player2 = players.get(rnd2);

            players.remove(player1);
            players.remove(player2);

            teamOne.add(player1);
            teamOne.add(player2);
        }

        if (teamTwo.isEmpty())
        {
            int rnd3=Rnd.get(players.size());
            int rnd4=Rnd.get(players.size());

            while(rnd4==rnd3)
                rnd4=Rnd.get(players.size());

            L2PcInstance player3 = players.get(rnd3);
            L2PcInstance player4 = players.get(rnd4);

            players.remove(player3);
            players.remove(player4);

            teamTwo.add(player3);
            teamTwo.add(player4);
        }

        for (L2PcInstance fighter1 : teamTwo)
        {
            if (fighter1 == null)
                continue;
            fighter1.sendMessage("[RANKED] MATCH FOUND !");
            fighter1.sendMessage("You will be teleported in 30 seconds");
        }

        for (L2PcInstance fighter2 : teamOne)
        {
            if (fighter2 == null)
                continue;
            fighter2.sendMessage("[RANKED] MATCH FOUND !");
            fighter2.sendMessage("You will be teleported in 30 seconds");
        }

        ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(), 30000);
    }

    public void teleportFighters()
    {
        if (teamOne.size() < 2 || teamTwo.size() < 2)
        {
            for (L2PcInstance un : teamOne)
            {
                if (un==null)
                    continue;
                un.sendMessage("An enemy have left !");
                un.teleToLocation(-114435,253417,-1546);
            }
            for (L2PcInstance deux : teamTwo)
            {
                if (deux==null)
                    continue;
                deux.sendMessage("An enemy have left !");
                deux.teleToLocation(-114435,253417,-1546);
            }
            teamOne.clear();
            teamTwo.clear();
            state = State.INACTIVE;
            return;
        }
        state = State.FIGHT;

        L2Npc bufferOne = NpcUtil.addSpawn(40002, -88900 + 50, -252849, -3330, 0, false, 15000, false, 0);
        L2Npc bufferTwo = NpcUtil.addSpawn(40002, -87322 + 50, -252849, -3332, 0, false, 15000, false, 0);
        bufferOne.spawnMe();
        bufferTwo.spawnMe();

        int i = 1;

        for (L2PcInstance team1 : teamOne)
        {
            if (team1 == null)
                continue;
            team1.teleToLocation(-88900,-252849,-3330);
            team1.setTeam(1);
            team1.setIsParalyzed(true);
            team1.setPvpFlag(0);
            team1.heal();
            team1.sendMessage("Fight will start in 15 seconds");
        }
        for (L2PcInstance team2 : teamTwo)
        {
            if (team2 == null)
                continue;
            team2.teleToLocation(-88900,-252849,-3330);
            team2.setTeam(1);
            team2.setIsParalyzed(true);
            team2.setPvpFlag(0);
            team2.heal();
            team2.sendMessage("Fight will start in 15 seconds");
        }


        ThreadPoolManager.getInstance().scheduleGeneral(new fight(), 15000);
    }


    protected void checkItem()
    {
        if (state != State.FIGHT)
            return;

        for (Map.Entry<L2PcInstance, Long> fighter : fighters.entrySet())
        {
            if (fighter == null)
            {
                continue;
            }
            fighter.getKey().checkItemRestriction();
            fighter.getKey().broadcastUserInfo();
        }
        return;
    }

    public Long getTime(L2PcInstance player)
    {
        if (!fighters.containsKey(player))
        {
            return null;
        }
        Long Seconds = (System.currentTimeMillis() - fighters.get(player)) / 1000;
        return Seconds;
    }

    public void onKill2v2(L2PcInstance killer, L2PcInstance killed)
    {
        int killerCurrentPoints;
        int killedCurrentPoints;

        killer.setPvpFlag(0);
        killed.setPvpFlag(0);


        killerCurrentPoints = getRankedPoints(killer);
        killedCurrentPoints = getRankedPoints(killed);

        int totalPoints = ((killedCurrentPoints + 1) / (killerCurrentPoints + 1 )) + 2;
        if (totalPoints > 20)
            totalPoints = 20 + (int) Math.pow(totalPoints - 20, 0.55);


        int amount = totalPoints;
        if (amount > 5)
            amount = 5 + (int) Math.pow(amount - 5, 0.55);

        setRankedPoints(killer, getRankedPoints(killer) + totalPoints);
        setRankedPoints(killed, getRankedPoints(killed) - (totalPoints / 2));

        killer.addItem("", 5899, amount,killer, true);


        killer.sendMessage("You won " + totalPoints + " points !");
        killer.sendMessage("Current points : " + getRankedPoints(killer));

        killed.sendMessage("You lost " + totalPoints / 3 + " points");
        killed.sendMessage("Current points : " + getRankedPoints(killed));



        killed.setPvpFlag(0);
        killed.teleToLocation(-114435,253417,-1546);
        killed.doRevive();
        killed.setTeam(0);

        if (teamOne.contains(killed))
            teamOne.remove(killed);
        if (teamTwo.contains(killed))
            teamTwo.remove(killed);
        if (teamTwo.size() == 0 || teamOne.size() == 0)
        {
            if (teamOne.size() > 0)
            {
                for (L2PcInstance team1 : teamOne)
                {
                    if (team1 == null)
                        continue;
                    team1.setPvpFlag(0);
                    if (team1.isDead())
                        team1.doRevive();
                    team1.setTeam(0);
                    team1.teleToLocation(-114435,253417,-1546);
                }
            }
            if (teamTwo.size() > 0)
            {
                for (L2PcInstance team2 : teamTwo)
                {
                    if (team2 == null)
                        continue;
                    team2.setPvpFlag(0);
                    if (team2.isDead())
                        team2.doRevive();
                    team2.setTeam(0);
                    team2.teleToLocation(-114435,253417,-1546);
                }
            }
            teamOne.clear();
            teamTwo.clear();
            state = State.INACTIVE;
        }

    }

    public void runFight()
    {
        if (teamOne.size() < 2 ||teamTwo.size() < 2)
        {
            for (L2PcInstance un : teamOne)
            {
                if (un==null)
                    continue;
                un.sendMessage("An enemy have left !");
                un.teleToLocation(-114435,253417,-1546);
            }
            for (L2PcInstance deux : teamTwo)
            {
                if (deux==null)
                    continue;
                deux.sendMessage("An enemy have left !");
                deux.teleToLocation(-114435,253417,-1546);
            }
            teamOne.clear();
            teamTwo.clear();
            state = State.INACTIVE;
            return;
        }

        state = State.FIGHT;

        for (L2PcInstance team1 : teamOne)
        {
            if (team1==null)
                continue;
            team1.setPvpFlag(1);
            team1.setIsParalyzed(false);
            team1.sendMessage("[RANKED] Fight !");
        }
        for (L2PcInstance team2 : teamTwo)
        {
            if (team2==null)
                continue;
            team2.setPvpFlag(1);
            team2.setIsParalyzed(false);
            team2.sendMessage("[RANKED] Fight !");
        }


        ThreadPoolManager.getInstance().scheduleGeneral(new checkLast(), 180000 );
    }

    public void lastcheck()
    {
        if (state != State.FIGHT)
        {
            return;
        }

        int alive1 = 0;
        int alive2 = 0;

        for (L2PcInstance team1 : teamOne)
        {
            if (team1==null)
                continue;
            if (!team1.isDead())
                alive1++;
        }



        for (L2PcInstance team2 : teamTwo)
        {
            if (team2==null)
                continue;
            if (!team2.isDead())
                alive2++;
        }

        if (alive1 > 0 && alive2 > 0)
        {

            for (L2PcInstance un : teamOne)
            {
                if (un==null)
                    continue;
                un.setPvpFlag(0);
                if (un.isDead())
                    un.doRevive();
                un.heal();
                un.teleToLocation(-114435,253417,-1546);
                un.sendMessage("[RANKED] Tie !");
            }
            for (L2PcInstance deux : teamTwo)
            {
                if (deux==null)
                    continue;
                deux.setPvpFlag(0);
                if (deux.isDead())
                    deux.doRevive();
                deux.heal();
                deux.teleToLocation(-114435,253417,-1546);
                deux.sendMessage("[RANKED] Tie !");
            }

            teamOne.clear();
            teamTwo.clear();
            state = State.INACTIVE;
        }

        return;
    }

    public  void registerpt(L2PcInstance player)
    {
        if (player == null)
        {
            return;
        }
        if (!player.isInParty())
        {
            player.sendMessage("You must be in a party !");
            return;
        }
        if (players.contains(player))
        {
            player.sendMessage("You're already in the queue.");
            return;
        }
        if (player.getParty().getMemberCount() > 2)
        {
            player.sendMessage("You're party have more than 2 members !");
            return;
        }
        List<L2PcInstance> party = player.getParty().getPartyMembers();

        if (teamOne.isEmpty())
        {
            for (L2PcInstance pt : party)
            {
                if (pt == null)
                    continue;
                teamOne.add(pt);
                pt.sendMessage("You joined the queue !");
            }
        }
        else if (teamTwo.isEmpty())
        {
            for (L2PcInstance pt : party)
            {
                if (pt == null)
                    continue;
                teamOne.add(pt);
                pt.sendMessage("You joined the queue !");
            }
        }
        else
        {
            for (L2PcInstance pt : party)
            {
                if (pt == null)
                    continue;
                pt.sendMessage("No places for the moment !");
            }
        }


        CustomCommunityBoard.getInstance().parseCmd("_bbscustom;getTest;0;0", player);
        return;
    }

    public  void register(L2PcInstance player)
    {
        if (player == null)
        {
            return;
        }
        if (players.contains(player))
        {
            player.sendMessage("You're already in the queue.");
            return;
        }
        players.add(player);
        player.sendMessage("You joined the queue !");
        CustomCommunityBoard.getInstance().parseCmd("_bbscustom;getTest;0;0", player);
        return;
    }

    public  void unregister(L2PcInstance player)
    {
        if (player == null)
        {
            return;
        }
        if (!players.contains(player))
        {
            player.sendMessage("You're not in the queue.");
            return;
        }
        player.sendMessage("You left the queue !");
        players.remove(player);
        CustomCommunityBoard.getInstance().parseCmd("_bbscustom;getTest;0;0", player);
        return;
    }


    public int getRankedPoints(L2PcInstance player)
    {
        Connection get = null;

        try
        {
            get = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = get.prepareStatement(
                    "SELECT rankedPoints FROM characters WHERE charId = ?");
            statement.setInt(1, player.getObjectId());
            ResultSet rset = statement.executeQuery();

            if (rset.next())
            {
                int currentPoints = rset.getInt("rankedPoints");
                return (currentPoints);
            }
            rset.close();
            statement.close();
        }

        catch (Exception e)
        {
            Log.log(Level.WARNING, "Couldn't get current ranked points : " + e.getMessage(), e);
        }
        finally
        {
            L2DatabaseFactory.close(get);
        }
        return 0;
    }

    public void setRankedPoints(L2PcInstance player, int amount)
    {
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            PreparedStatement statement =
                    con.prepareStatement("UPDATE characters SET rankedPoints=? WHERE charId=?");
            statement.setInt(1, amount);
            statement.setInt(2, player.getObjectId());

            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.SEVERE, "Failed updating Ranked Points", e);
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }
    }

    public void handleEventCommand(L2PcInstance player, String command)
    {
        if (player == null)
        {
            return;
        }

        StringTokenizer st = new StringTokenizer(command, ";");
        st.nextToken();
        st.nextToken();
        st.nextToken();


        switch (String.valueOf(st.nextToken()))
        {
            case "register":
                register(player);
                break;
            case "registerpt":
                registerpt(player);
                break;
            case "unregister":
                unregister(player);
                break;

        }

    }

    protected Ranked2v2()
    {
        ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Event(), 60000 * timeEach , 60000 * timeEach);
        ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Item(), 0  , 1000);
    }

    public static Ranked2v2 getInstance()
    {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder
    {
        protected static final Ranked2v2 instance = new Ranked2v2();
    }

    protected class Event implements Runnable
    {
        @Override
        public void run()
        {
            if (state != State.FIGHT)
                checkRegistered();
        }

    }


    protected class Item implements Runnable
    {
        @Override
        public void run()
        {
            checkItem();
        }

    }

    protected class fight implements Runnable
    {
        @Override
        public void run()
        {

            runFight();
        }

    }

    protected class checkLast implements Runnable
    {
        @Override
        public void run()
        {

            lastcheck();
        }

    }


    protected class Teleport implements Runnable
    {
        @Override
        public void run()
        {

            teleportFighters();
        }

    }




}
