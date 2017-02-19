package l2server.gameserver.events;

import l2server.L2DatabaseFactory;
import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.instancemanager.MailManager;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Message;
import l2server.gameserver.model.itemcontainer.Mail;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.logging.Level;


/**
 * @author Inia
 *
 */
public class TopRanked
{
    private StartTask _task;


    public int getRankedPoints(int id)
    {
        Connection get = null;

        try
        {
            get = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = get.prepareStatement(
                    "SELECT rankedPoints FROM characters WHERE charId = ?");
            statement.setInt(1, id);
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

    public void setRankedPoints(int id, int amount)
    {
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            PreparedStatement statement =
                    con.prepareStatement("UPDATE characters SET rankedPoints=? WHERE charId=?");
            statement.setInt(1, amount);
            statement.setInt(2, id);

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
    public void test()
    {
        int position = 1;
        Connection get = null;
        int amount = 0;
        int currentPoints = 0;
        Message msg;
        Announcements.getInstance().announceToAll("Rewards of the current season delivered !");
        try
        {
            get = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = get.prepareStatement(
                    "SELECT rankedPoints,charId FROM characters WHERE rankedPoints>0 order by rankedPoints desc limit 10");
            ResultSet rset = statement.executeQuery();
            while (rset.next())
            {

                String id =  rset.getString("charId");
                Integer x = Integer.valueOf(id);
                currentPoints = getRankedPoints(x);

                amount =  (int)(Math.pow(currentPoints, 1.05))*(5/position);
                if (amount > 70)
                 amount =  (int)(70 + (Math.pow(amount - 70, 0.55)));

                 msg = new Message(-1, x, false, "Ranked System", "Congrats for youre ranking ! You ended the season at position " + position + " with " + currentPoints + " points !", 0);

                Mail attachments = msg.createAttachments();
                attachments.addItem("Ranked System", 5899, amount, null, null);
                MailManager.getInstance().sendMessage(msg);



                setRankedPoints(x, (10 / position) * currentPoints / 12);

                position++;
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
    }




    protected TopRanked()
    {

    }


    public void scheduleEventStart()
    {
        try
        {
            Calendar currentTime = Calendar.getInstance();
            Calendar nextStartTime = Calendar.getInstance();
            nextStartTime.setLenient(true);
            int hour = 2;//Rnd.get(5);
            int minute = 1;//Rnd.get(60);
            nextStartTime.set(Calendar.HOUR_OF_DAY, hour);
            nextStartTime.set(Calendar.MINUTE, minute);
            nextStartTime.set(Calendar.SECOND, 0);
            // If the date is in the past, make it the next day (Example: Checking for "1:00", when the time is 23:57.)
            if (nextStartTime.getTimeInMillis() - 10000 < currentTime.getTimeInMillis())
            {
                nextStartTime.add(Calendar.DAY_OF_MONTH, 7);
            }
            _task = new StartTask(nextStartTime.getTimeInMillis());
            ThreadPoolManager.getInstance().executeTask(_task);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public StartTask getStartTask()
    {
        return _task;
    }

    public void showInfo(L2PcInstance activeChar)
    {
        Calendar now = Calendar.getInstance();
        Calendar startTime = Calendar.getInstance();
        startTime.setTimeInMillis(_task.getStartTime());
        String time;
        if (now.get(Calendar.DAY_OF_MONTH) == startTime.get(Calendar.DAY_OF_MONTH))
        {
            time = "today";
        }
        else
        {
            time = "many days";
        }
        time += " at " + startTime.get(Calendar.HOUR_OF_DAY) + ":" + startTime.get(Calendar.MINUTE);
        long toStart = _task.getStartTime() - System.currentTimeMillis();
        int hours = (int) (toStart / 3600000);
        int minutes = (int) (toStart / 60000) % 60;
        if (hours > 0 || minutes > 0)
        {
            time += ", in ";
            if (hours > 0)
            {
                time += hours + " hour" + (hours == 1 ? "" : "s") + " and ";
            }
            time += minutes + " minute" + (minutes == 1 ? "" : "s");
        }
        String html =
                "<html>" + "<title>Event</title>" + "<body>" + "<center><br><tr><td>Ranked</td></tr><br>" +
                        "<br>" + "The next season will be " + time + ".<br>";
        html += "</body></html>";
        activeChar.sendPacket(new NpcHtmlMessage(0, html));
    }

    class StartTask implements Runnable
    {
        private long _startTime;

        public StartTask(long startTime)
        {
            _startTime = startTime;
        }

        public long getStartTime()
        {
            return _startTime;
        }

        @Override
        public void run()
        {
            int delay = (int) Math.round((_startTime - System.currentTimeMillis()) / 1000.0);

            if (delay > 0)
            {
                ThreadPoolManager.getInstance().scheduleGeneral(this, delay * 1000);
            }
            else
            {
                test();

                scheduleEventStart();
            }
        }
    }

    public static TopRanked getInstance()
    {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder
    {
        protected static final TopRanked _instance = new TopRanked();
    }



    protected class Oui implements Runnable
    {
        @Override
        public void run()
        {
            test();
        }

    }






}