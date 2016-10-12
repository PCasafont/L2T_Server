package l2server.gameserver.events;

import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.Calendar;

/**
 * @author Pere
 */
public class CloneInvasion
{
	public static CloneInvasion _instance = null;

	private StartTask _task;

	public static CloneInvasion getInstance()
	{
		if (_instance == null)
		{
			_instance = new CloneInvasion();
		}
		return _instance;
	}

	public void start()
	{
		Announcements.getInstance().announceToAll(
				"A lot of clones with your appearance have now appeared! They will charge upon you without any reason!");
		Announcements.getInstance().announceToAll("Prove that there isn't anyone as the original one!");

		spawnNpcPlayers();
	}

	public void spawnNpcPlayers()
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			if (player == null || player.isGM() || player.isInStoreMode())
			{
				continue;
			}
			try
			{
				int pledgeClass = 0;
				if (player.getClan() != null)
				{
					pledgeClass = player.getClan().getClanMember(player.getObjectId()).calculatePledgeClass(player);
				}

				if (player.isNoble() && pledgeClass < 5)
				{
					pledgeClass = 5;
				}

				if (player.isHero() && pledgeClass < 8)
				{
					pledgeClass = 8;
				}

				player.setPledgeClass(pledgeClass);

				StatsSet set = new StatsSet();
				set.set("id", player.getObjectId());
				set.set("level", player.getLevel());
				set.set("type", "L2Monster");
				set.set("name", player.getName());
				set.set("atkRange", player.getPhysicalAttackRange());
				set.set("hpMax", player.getMaxHp() * 20);
				set.set("hpReg", 5);
				set.set("mpMax", player.getMaxMp() * 10);
				set.set("mpReg", 2);
				set.set("STR", player.getSTR());
				set.set("CON", player.getCON());
				set.set("DEX", player.getDEX());
				set.set("INT", player.getINT());
				set.set("MEN", player.getMEN());
				set.set("WIT", player.getWIT());
				set.set("expRate", 5);
				set.set("pAtk", player.getPAtk(null));
				set.set("pDef", player.getPDef(null));
				set.set("mAtk", player.getMAtk(null, null));
				set.set("mDef", player.getMDef(null, null));
				set.set("pAtkSpd", player.getPAtkSpd());
				set.set("mAtkSpd", player.getMAtkSpd());
				set.set("walkSpd", player.getWalkSpeed());
				set.set("runSpd", player.getRunSpeed());
				set.set("aggressive", true);
				set.set("aggroRange", 500);

				L2NpcTemplate tmpl = new L2NpcTemplate(set);

				L2NpcAIData npcAIDat = new L2NpcAIData();
				npcAIDat.setAi(player.getActiveWeaponItem() != null ? player.isMageClass() ? "mage" :
						player.getActiveWeaponItem().getItemType() == L2WeaponType.BOW ? "archer" : "fighter" :
						"balanced");
				npcAIDat.setSkillChance(player.isMageClass() ? 100 : 15);
				npcAIDat.setCanMove(true);
				npcAIDat.setSoulShot(10000);
				npcAIDat.setSpiritShot(10000);
				npcAIDat.setSoulShotChance(100);
				npcAIDat.setSpiritShotChance(100);
				npcAIDat.setClan("clones");
				npcAIDat.setClanRange(500);
				tmpl.setAIData(npcAIDat);

				for (L2Skill skill : player.getAllSkills())
				{
					if (skill.isOffensive() && skill.getMagicLevel() > player.getLevel() - 5)
					{
						tmpl.addSkill(skill);
					}
				}

				L2DropData dd = new L2DropData(4357, 1500, 2500, 100);
				dd.setCustom();
				tmpl.addDropData(dd);

				L2Spawn spawn = new L2Spawn(tmpl);
				spawn.setX(player.getPosition().getX() + Rnd.get(100));
				spawn.setY(player.getPosition().getY() + Rnd.get(100));
				spawn.setZ(player.getPosition().getZ());

				spawn.stopRespawn();
				if (spawn.getNpc() != null && !spawn.getNpc().isInsideZone(L2Character.ZONE_NOLANDING))
				{
					spawn.getNpc().setClonedPlayer(player);
					spawn.doSpawn();
				}
			}
			catch (Exception e)
			{
				Log.warning("Error spawning a player clone: ");
				e.printStackTrace();
			}
		}
	}

	public void scheduleEventStart()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = Calendar.getInstance();
			nextStartTime.setLenient(true);
			int hour = 18 + 1;//Rnd.get(5);
			int minute = 0;//Rnd.get(60);
			nextStartTime.set(Calendar.HOUR_OF_DAY, hour);
			nextStartTime.set(Calendar.MINUTE, minute);
			nextStartTime.set(Calendar.SECOND, 0);
			// If the date is in the past, make it the next day (Example: Checking for "1:00", when the time is 23:57.)
			if (nextStartTime.getTimeInMillis() - 10000 < currentTime.getTimeInMillis())
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
				"<html>" + "<title>Event</title>" + "<body>" + "<center><br><tr><td>Clone Invasion</td></tr><br>" +
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
