package l2server.gameserver.events;

import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.instancemanager.TownManager;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.util.Rnd;

import java.util.Calendar;

/**
 * @author Pere
 */
public class Curfew {
	public static Curfew instance = null;

	private CurfewTask ctask;
	private StartTask task;

	private int eventTown = 9;
	private String eventTownName = "Giran";
	public long curfewEnd = 0;

	public static Curfew getInstance() {
		if (instance == null) {
			instance = new Curfew();
		}
		return instance;
	}

	public void initialize() {
		eventTown = Rnd.get(19) + 1;
		if (eventTown == 16) {
			eventTown = 20;
		} else if (eventTown == 18) {
			eventTown = 22;
		}

		eventTownName = MapRegionTable.getInstance().getTownName(eventTown);
	}

	public void start() {
		for (Player player : World.getInstance().getAllPlayers().values()) {
			if (!(player.isInsideZone(Creature.ZONE_PEACE) && TownManager.getClosestTown(player).getTownId() == eventTown)) {
				player.setInsideZone(Creature.ZONE_PVP, true);
			}
		}

		for (Player player : World.getInstance().getAllPlayers().values()) {
			if (player.isInsideZone(Creature.ZONE_PEACE)) {
				player.teleToLocation(player.getX(), player.getY(), player.getZ());
			}
		}

		int minutes = Rnd.get(30) + 30;

		curfewEnd = System.currentTimeMillis() + 60000L * minutes;
		scheduleCurfew();

		Announcements.getInstance().announceToAll("The entire world has become a hell during " + minutes + " minutes!");
		Announcements.getInstance().announceToAll("You will be safe from the assassins only at " + eventTownName + "!");
	}

	private void stop() {
		eventTown = -1;
		for (Player player : World.getInstance().getAllPlayers().values()) {
			if (player != null && !player.isInsideZone(Creature.ZONE_PEACE) &&
					!(player.isInsideZone(Creature.ZONE_NOSUMMONFRIEND) && player.isInsideZone(Creature.ZONE_NOLANDING))) {
				player.teleToLocation(player.getX(), player.getY(), player.getZ());
			}
		}

		Announcements.getInstance().announceToAll("The curfew has ended: the murder is penalized again.");
	}

	public int getOnlyPeaceTown() {
		return -1;//eventTown;
	}

	class CurfewTask implements Runnable {
		private long startTime;

		public CurfewTask(long startTime) {
			this.startTime = startTime;
		}

		@Override
		public void run() {
			int delay = Math.round((startTime - System.currentTimeMillis()) / 1000);

			if (delay > 0) {
				ThreadPoolManager.getInstance().scheduleGeneral(this, delay * 1000);
			} else {
				stop();
			}
		}
	}

	public void scheduleCurfew() {
		try {
			ctask = new CurfewTask(curfewEnd);
			ThreadPoolManager.getInstance().executeTask(ctask);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void scheduleEventStart() {
		try {
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = Calendar.getInstance();
			nextStartTime.setLenient(true);
			int hour = Rnd.get(5) + 18;
			int minute = Rnd.get(60);
			nextStartTime.set(Calendar.HOUR_OF_DAY, hour);
			nextStartTime.set(Calendar.MINUTE, minute);
			nextStartTime.set(Calendar.SECOND, 0);
			// If the date is in the past, make it the next day (Example: Checking for "1:00", when the time is 23:57.)
			if (nextStartTime.getTimeInMillis() < currentTime.getTimeInMillis()) {
				nextStartTime.add(Calendar.DAY_OF_MONTH, 1);
			}
			task = new StartTask(nextStartTime.getTimeInMillis());
			ThreadPoolManager.getInstance().executeTask(task);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public StartTask getStartTask() {
		return task;
	}

	public void showInfo(Player activeChar) {
		Calendar now = Calendar.getInstance();
		Calendar startTime = Calendar.getInstance();
		startTime.setTimeInMillis(task.getStartTime());
		String time;
		if (now.get(Calendar.DAY_OF_MONTH) == startTime.get(Calendar.DAY_OF_MONTH)) {
			time = "today";
		} else {
			time = "tomorrow";
		}
		time += " at " + startTime.get(Calendar.HOUR_OF_DAY) + ":" + startTime.get(Calendar.MINUTE);
		long toStart = task.getStartTime() - System.currentTimeMillis();
		int hours = (int) (toStart / 3600000);
		int minutes = (int) (toStart / 60000) % 60;
		if (hours > 0 || minutes > 0) {
			time += ", in ";
			if (hours > 0) {
				time += hours + " hour" + (hours == 1 ? "" : "s") + " and ";
			}
			time += minutes + " minute" + (minutes == 1 ? "" : "s");
		}
		String html =
				"<html>" + "<title>Event</title>" + "<body>" + "<center><br><tr><td>Curfew</td></tr><br>" + "<br>" + "The next curfew will be " +
						time + ".<br>";
		html += "</body></html>";
		activeChar.sendPacket(new NpcHtmlMessage(0, html));
	}

	class StartTask implements Runnable {
		private long startTime;

		public StartTask(long startTime) {
			this.startTime = startTime;
		}

		public long getStartTime() {
			return startTime;
		}

		@Override
		public void run() {
			int delay = (int) Math.round((startTime - System.currentTimeMillis()) / 1000.0);

			if (delay > 0) {
				ThreadPoolManager.getInstance().scheduleGeneral(this, delay * 1000);
			} else {
				start();

				scheduleEventStart();
			}
		}
	}
}
