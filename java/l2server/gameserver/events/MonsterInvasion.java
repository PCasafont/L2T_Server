package l2server.gameserver.events;

import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2ArmyMonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.util.Rnd;

import java.util.Calendar;

/**
 * @author Pere
 */
public class MonsterInvasion
{
	public static MonsterInvasion _instance = null;

	private StartTask _task;

	public int type = 0;

	private int _eventTown = 9;
	private String eventTownName = "Giran";
	private boolean invasionFightStarted = false;
	private L2Spawn _armyCommanderSpawn;
	private L2Spawn[] _armySpawns = new L2Spawn[1000];

	public static MonsterInvasion getInstance()
	{
		if (_instance == null)
		{
			_instance = new MonsterInvasion();
		}
		return _instance;
	}

	public void initialize()
	{
		int town = Rnd.get(4);
		switch (town)
		{
			case 0:
				_eventTown = 8;
				break;
			case 1:
				_eventTown = 9;
				break;
			case 2:
				_eventTown = 10;
				break;
			case 3:
				_eventTown = 15;
		}

		eventTownName = MapRegionTable.getInstance().getTownName(_eventTown);
	}

	public void start()
	{
		Announcements.getInstance().announceToAll("A monster army is trying to invade " + eventTownName + "!");
		Announcements.getInstance().announceToAll("No monster can survive! Charge!");

		int nMobs = L2World.getInstance().getAllPlayersCount() / 5;
		int rows = (int) Math.round(Math.sqrt(nMobs) / 1.6);
		int columns = (int) Math.round(Math.sqrt(nMobs) / 1.6 * 2.5);
		nMobs = rows * columns;

		int x;
		int iniY;
		int iniZ;
		int cDespY;
		int cDespZ;
		int fDespY;
		int fDespZ;
		int despX;
		int despY;
		int heading;

		switch (_eventTown)
		{
			case 8:
				x = 148620;
				iniY = 77700;
				iniZ = -3595;
				cDespY = 80750;
				cDespZ = -3465;
				fDespY = 81350;
				fDespZ = -3470;
				despX = 1100;
				despY = 440;
				heading = 0;
				break;
			case 9:
				x = 54280;
				iniY = 78030;
				iniZ = -1913;
				cDespY = 79776;
				cDespZ = -1561;
				fDespY = 80400;
				fDespZ = -1561;
				despX = 550;
				despY = 250;
				heading = 0;
				break;
			case 10:
				x = 147455;
				iniY = 30162;
				iniZ = -2462;
				cDespY = 28640;
				cDespZ = -2270;
				fDespY = 28000;
				fDespZ = -2270;
				despX = 1350;
				despY = 500;
				heading = 49152;
				break;
			default:
				x = 147716;
				iniY = -59350;
				iniZ = -2980;
				cDespY = -56850;
				cDespZ = -2800;
				fDespY = -56100;
				fDespZ = -2800;
				despX = 830;
				despY = 350;
				heading = 16384;
				break;
		}

		int interX = despX / columns;
		int interY = despY / rows;

		try
		{
			L2NpcTemplate[] tmpls = new L2NpcTemplate[5];
			int race = Rnd.get(8) + 1;
			int types = 2;
			switch (race)
			{
				case 1:
					types = 3;
					break;
				case 2:
					types = 4;
					break;
				case 3:
					types = 4;
					break;
				case 4:
					types = 4;
					break;
				case 5:
					types = 3;
					break;
				case 6:
					types = 4;
					break;
				case 7:
					types = 3;
					break;
				case 8:
					types = 2;
					break;
			}
			for (int i = 0; i < types; i++)
			{
				tmpls[i] = NpcTable.getInstance().getTemplate(44000 + 10 * race + i);
			}

			_armyCommanderSpawn = new L2Spawn(tmpls[0]);

			if (heading == 0 || heading == 32768)
			{
				_armyCommanderSpawn.setX(iniY);
				_armyCommanderSpawn.setY(x);
			}
			else
			{
				_armyCommanderSpawn.setX(x);
				_armyCommanderSpawn.setY(iniY);
			}
			_armyCommanderSpawn.setZ(iniZ + 50);
			_armyCommanderSpawn.setHeading(heading);

			_armyCommanderSpawn.stopRespawn();
			_armyCommanderSpawn.doSpawn();

			int pRow = 2;
			for (int i = 0; i < nMobs; i++)
			{
				_armySpawns[i] = new L2Spawn(tmpls[(int) Math.floor(i * (types - 1) / nMobs) + 1]);

				if (heading == 0)
				{
					_armySpawns[i].setX(iniY - interY * pRow);
					_armySpawns[i].setY((int) Math.round(x + 20 * (i % 2 - 0.5)));
				}
				else if (heading == 16384)
				{
					_armySpawns[i].setX((int) Math.round(x + 20 * (i % 2 - 0.5)));
					_armySpawns[i].setY(iniY - interY * pRow);
				}
				else if (heading == 32768)
				{
					_armySpawns[i].setX(iniY + interY * pRow);
					_armySpawns[i].setY((int) Math.round(x - 20 * (i % 2 + 0.5)));
				}
				else
				{
					_armySpawns[i].setX((int) Math.round(x - 20 * (i % 2 + 0.5)));
					_armySpawns[i].setY(iniY + interY * pRow);
				}
				_armySpawns[i].setZ(iniZ + 100);
				_armySpawns[i].setHeading(heading);

				_armySpawns[i].stopRespawn();
				_armySpawns[i].doSpawn();

				if (i % 2 == 1)
				{
					pRow++;
				}
			}
		}
		catch (Exception e)
		{
			//Logozo.warning("Error spawning ivasion army:");
			e.printStackTrace();
		}

		try
		{
			L2ArmyMonsterInstance mob;

			mob = (L2ArmyMonsterInstance) _armyCommanderSpawn.getNpc();
			if (heading == 0 || heading == 32768)
			{
				mob.move(cDespY, x, cDespZ);
			}
			else
			{
				mob.move(x, cDespY, cDespZ);
			}
			for (int i = 0; i < nMobs; i++)
			{
				mob = (L2ArmyMonsterInstance) _armySpawns[i].getNpc();
				if (heading == 0)
				{
					mob.move(cDespY, (int) Math.round(x + 20 * (i % 2 - 0.5)), cDespZ);
				}
				else if (heading == 16384)
				{
					mob.move((int) Math.round(x + 20 * (i % 2 - 0.5)), cDespY, cDespZ);
				}
				else if (heading == 32768)
				{
					mob.move(cDespY, (int) Math.round(x - 20 * (i % 2 + 0.5)), cDespZ);
				}
				else
				{
					mob.move((int) Math.round(x - 20 * (i % 2 + 0.5)), cDespY, cDespZ);
				}
			}

			mob = (L2ArmyMonsterInstance) _armyCommanderSpawn.getNpc();
			if (heading == 0 || heading == 32768)
			{
				mob.move(fDespY + 100, x, fDespZ);
			}
			else if (heading == 16384)
			{
				mob.move(x, fDespY + 100, fDespZ);
			}
			else if (heading == 32768)
			{
				mob.move(fDespY - 100, x, fDespZ);
			}
			else
			{
				mob.move(x, fDespY - 100, fDespZ);
			}
			for (int i = 0; i < nMobs; i++)
			{
				mob = (L2ArmyMonsterInstance) _armySpawns[i].getNpc();
				if (heading == 0)
				{
					mob.move(fDespY - (i - i % columns) / columns * interY,
							x - columns * interX / 2 + i % columns * interX, fDespZ);
				}
				else if (heading == 16384)
				{
					mob.move(x - columns * interX / 2 + i % columns * interX,
							fDespY - (int) Math.floor(i / columns) * interY, fDespZ);
				}
				else if (heading == 32768)
				{
					mob.move(fDespY + (int) Math.floor(i / columns) * interY,
							x + columns * interX / 2 - i % columns * interX, fDespZ);
				}
				else
				{
					mob.move(x + columns * interX / 2 - i % columns * interX,
							fDespY + (int) Math.floor(i / columns) * interY, fDespZ);
				}
			}
			mob.setIsTheLastMob(true);
		}
		catch (Exception e)
		{
			for (int i = 0; i < nMobs; i++)
			{
				if (_armySpawns[i] != null && _armySpawns[i].getNpc() != null)
				{
					_armySpawns[i].getNpc().setIsInvul(false);
				}
			}
			if (_armyCommanderSpawn != null && _armyCommanderSpawn.getNpc() != null)
			{
				_armyCommanderSpawn.getNpc().setIsInvul(false);
			}
			//Logozo.warning("Error moving ivasion army:");
			e.printStackTrace();
		}
	}

	private void stop()
	{
		invasionFightStarted = false;
		_eventTown = -1;
		for (int i = 0; i < _armySpawns.length; i++)
		{
			_armySpawns[i] = null;
		}

		Announcements.getInstance().announceToAll("The invading monster army has been defeated!");
	}

	public void startInvasionFight()
	{
		if (!invasionFightStarted)
		{
			for (L2Spawn _armySpawn : _armySpawns)
			{
				if (_armySpawn != null && _armySpawn.getNpc() != null)
				{
					_armySpawn.getNpc().setIsInvul(false);
				}
			}
			if (_armyCommanderSpawn != null && _armyCommanderSpawn.getNpc() != null)
			{
				L2ArmyMonsterInstance commander = (L2ArmyMonsterInstance) _armyCommanderSpawn.getNpc();
				commander.setIsInvul(false);
				commander.shout("ATTACK!");
			}
			invasionFightStarted = true;
		}
	}

	public void onCommanderDeath()
	{
		for (L2Spawn _armySpawn : _armySpawns)
		{
			if (_armySpawn != null && _armySpawn.getNpc() != null)
			{
				_armySpawn.getNpc().doDie(_armySpawn.getNpc());
			}
		}

		stop();
	}

	public int getAttackedTown()
	{
		return -1;//_eventTown;
	}

	public void scheduleEventStart()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = Calendar.getInstance();
			nextStartTime.setLenient(true);
			int hour = Rnd.get(5) + 18;
			int minute = Rnd.get(60);
			nextStartTime.set(Calendar.HOUR_OF_DAY, hour);
			nextStartTime.set(Calendar.MINUTE, minute);
			nextStartTime.set(Calendar.SECOND, 0);
			// If the date is in the past, make it the next day (Example: Checking for "1:00", when the time is 23:57.)
			if (nextStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
			{
				nextStartTime.add(Calendar.DAY_OF_MONTH, 1);
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
			time = "tomorrow";
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
				"<html>" + "<title>Event</title>" + "<body>" + "<center><br><tr><td>Monster Invasion</td></tr><br>" +
						"<br>" + "The next invasion will be " + time + ".<br>";
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
				start();

				scheduleEventStart();
			}
		}
	}
}
