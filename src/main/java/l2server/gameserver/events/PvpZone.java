package l2server.gameserver.events;

import l2server.L2DatabaseFactory;
import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.instancemanager.PlayerAssistsManager;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.util.Broadcast;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

/**
 * @author Inia
 *
 */
public class PvpZone
{

    public static enum State{INACTIVE,REGISTER,LOADING,FIGHT}
    public static State state = State.INACTIVE;
    public static Vector<L2PcInstance> players = new Vector<>();
    public static Vector<L2PcInstance> fighters = new Vector<>();
    public static Vector<Integer> _fight = new Vector<>();

    protected void openRegistrations()
    {
        state=State.LOADING;
        Broadcast.announceToOnlinePlayers("PvP Zone will open in 30 seconds!");
        ThreadPoolManager.getInstance().scheduleGeneral(new fight(), 30000 );
    }

    protected void checkItem()
    {
        if (state != State.FIGHT)
            return;


        for (L2PcInstance player : players)
        {
            if (player == null)
            {
                continue;
            }
            if (player.isInParty())
            {
                player.leaveParty();
            }
            player.checkItemRestriction();
            player.broadcastUserInfo();
        }


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

    public void onKillPvpZone(L2PcInstance killed, L2PcInstance killer)
    {
        int killerCurrentPoints = getRankedPoints(killer);
        int killedCurrentPoints = getRankedPoints(killed);

        int totalPoints = ((killedCurrentPoints + 1) / (killerCurrentPoints + 1 )) + 2;
        if (totalPoints > 5)
            totalPoints = 5 + (int) Math.pow(totalPoints - 5, 0.35);

        int amount = totalPoints;
        if (amount > 3)
            amount = 3 + (int) Math.pow(amount - 3, 0.45);

        setRankedPoints(killer, getRankedPoints(killer) + totalPoints);
        setRankedPoints(killed, getRankedPoints(killed) - (totalPoints / 3));
        if (getRankedPoints(killed) < 0)
        {
            setRankedPoints(killed, 0);
        }

        killer.addItem("", 5899, amount,killer, true);

        killer.sendMessage("You won " + totalPoints + " points!");
        killer.sendMessage("Current points: " + getRankedPoints(killer));
        killed.sendMessage("You lost " + totalPoints / 3 + " points");
        killed.sendMessage("Current points: " + getRankedPoints(killed));


        List<L2PcInstance> assistants =
                PlayerAssistsManager.getInstance().getAssistants(killer, killed, true);
        for (L2PcInstance assistant : assistants)
        {
            int assistantCurrentPoints = getRankedPoints(killer);


            totalPoints = ((killedCurrentPoints + 1) / (killerCurrentPoints + 1 )) + 2;
            if (totalPoints > 5)
                totalPoints = 5 + (int) Math.pow(totalPoints - 5, 0.35);

            amount = totalPoints;
            if (amount > 3)
                amount = 3 + (int) Math.pow(amount - 3, 0.45);

            setRankedPoints(assistant, getRankedPoints(assistant) + totalPoints / 3);
            assistant.addItem("", 5899, amount / 3, assistant, true);

            assistant.sendMessage("You won " + totalPoints / 3 + " points for the assistance on " + killed.getName());
            assistant.sendMessage("Current points : " + getRankedPoints(killer));
        }

        reviveKilled(killed);
    }

    public void reviveKilled(L2PcInstance player)
    {
        player.sendMessage("You will revive in 10 seconds!");
        //Pause of 10 Seconds
        try {
            Thread.sleep(10000);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();}

        player.doRevive();
        player.heal();
        player.setPvpFlag(1);
    }

    protected void startFight()
    {
        state = State.FIGHT;
        Announcements.getInstance().announceToAll("PvP Zone will close in 10 minutes!");
        Broadcast.announceToOnlinePlayers("Write .pvpzone to teleport!");
        ThreadPoolManager.getInstance().scheduleGeneral(new checkLast(), 60000 * 10 );
    }

    protected void lastCheck()
    {
        if(state == State.FIGHT)
        {
            Announcements.getInstance().announceToAll("PvP Zone has closed.");
            players.clear();
            fighters.clear();
            _fight.clear();
            revert();
            clean();
        }
    }

    public static void revert()
    {
        if(!players.isEmpty())
            for(L2PcInstance p : players)
            {
                if(p == null)
                    continue;

                if(p.isDead())
                    p.doRevive();

                p.setPvpFlag(0);
                p.setCurrentHp(p.getMaxHp());
                p.setCurrentCp(p.getMaxCp());
                p.setCurrentMp(p.getMaxMp());
                p.broadcastUserInfo();
                p.teleToLocation(-114435,253417,-1546);

            }
    }

    public static void clean()
    {

        if(state == State.FIGHT)
            for(L2PcInstance p : players)
                p.setTeam(0);


        players.clear();
        _fight.clear();
        state = State.INACTIVE;

    }

    protected PvpZone()
    {
        ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new checki(), 100 , 100);
        ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Event(), 60000 * 33 , 60000 * 33);
    }

    public static PvpZone getInstance()
    {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder
    {
        protected static final PvpZone _instance = new PvpZone();
    }



    protected class Event implements Runnable
    {
        @Override
        public void run()
        {
            if(state == State.INACTIVE)
                openRegistrations();
        }

    }

    protected class fight implements Runnable
    {

        @Override
        public void run()
        {
            startFight();
        }

    }

    protected class checki implements Runnable
    {
        @Override
        public  void run() {checkItem();}
    }

    protected class checkLast implements Runnable
    {
        @Override
        public void run()
        {
            lastCheck();
        }

    }
}