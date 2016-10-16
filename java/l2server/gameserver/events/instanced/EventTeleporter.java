package l2server.gameserver.events.instanced;

import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.events.instanced.EventInstance.EventState;
import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.util.Point3D;
import l2server.util.Rnd;

/**
 * @author Pere
 */
public class EventTeleporter implements Runnable
{
	private L2PcInstance playerInstance = null;
	private Point3D coordinates = null;
	private boolean restore = false;
	private boolean heal = true;

	public EventTeleporter(L2PcInstance playerInstance, Point3D coordinates, boolean fastSchedule, boolean restore)
	{
		this.playerInstance = playerInstance;
		this.coordinates = coordinates;
		this.restore = restore;

		long delay = (playerInstance.getEvent() == null || playerInstance.getEvent().isState(EventState.STARTED) ?
				Config.INSTANCED_EVENT_RESPAWN_TELEPORT_DELAY : Config.INSTANCED_EVENT_START_LEAVE_TELEPORT_DELAY) *
				1000;

		ThreadPoolManager.getInstance().scheduleGeneral(this, fastSchedule ? 0 : delay);
	}

	public EventTeleporter(L2PcInstance playerInstance, Point3D coordinates, boolean fastSchedule, boolean restore, boolean heal)
	{
		this.playerInstance = playerInstance;
		this.coordinates = coordinates;
		this.restore = restore;
		this.heal = heal;

		long delay = (playerInstance.getEvent() == null || playerInstance.getEvent().isState(EventState.STARTED) ?
				Config.INSTANCED_EVENT_RESPAWN_TELEPORT_DELAY : Config.INSTANCED_EVENT_START_LEAVE_TELEPORT_DELAY) *
				1000;

		ThreadPoolManager.getInstance().scheduleGeneral(this, fastSchedule ? 0 : delay);
	}

	@Override
	public void run()
	{
		if (playerInstance == null)
		{
			return;
		}

		EventInstance event = playerInstance.getEvent();
		if (event == null)
		{
			return;
		}

		try
		{
			for (L2Abnormal effect : playerInstance.getAllEffects())
			{
				if (effect != null)
				{
					effect.exit();
				}
			}

			L2PetInstance pet = playerInstance.getPet();
			if (pet != null)
			{
				// In LC, SS and SS2, players don't need summons and summons are even able to attack during event so better unsummon
				if (pet.isMountable() || event.isType(EventType.LuckyChests) ||
						event.isType(EventType.StalkedSalkers) || event.isType(EventType.SimonSays))
				{
					pet.unSummon(playerInstance);
				}
				else
				{
					for (L2Abnormal effect : pet.getAllEffects())
					{
						if (effect != null)
						{
							effect.exit();
						}
					}
				}
			}
			for (L2SummonInstance summon : playerInstance.getSummons())
			{
				// In LC, SS and SS2, players don't need summons and summons are even able to attack during event so better unsummon
				if (summon.isMountable() || event.isType(EventType.LuckyChests) ||
						event.isType(EventType.StalkedSalkers) || event.isType(EventType.SimonSays))
				{
					summon.unSummon(playerInstance);
				}
				else
				{
					for (L2Abnormal effect : summon.getAllEffects())
					{
						if (effect != null)
						{
							effect.exit();
						}
					}
				}
			}

			if (event.getConfig().isAllVsAll())
			{
				playerInstance.leaveParty();
			}

			for (L2SummonInstance summon : playerInstance.getSummons())
			{
				// In LC, SS and SS2, players don't need summons and summons are even able to attack during event so better unsummon
				if (event.isType(EventType.LuckyChests) || event.isType(EventType.StalkedSalkers) ||
						event.isType(EventType.SimonSays))
				{
					summon.unSummon(playerInstance);
				}
				else
				{
					for (L2Abnormal effect : summon.getAllEffects())
					{
						if (effect != null)
						{
							effect.exit();
						}
					}
				}
			}

			if (playerInstance.isDead())
			{
				playerInstance.restoreExp(100.0);
				playerInstance.doRevive();
			}

			if (heal)
			{
				playerInstance.setCurrentCp(playerInstance.getMaxCp());
				playerInstance.setCurrentHp(playerInstance.getMaxHp());
				playerInstance.setCurrentMp(playerInstance.getMaxMp());
			}

			int x = 0, y = 0, z = 0;
			if (event.isState(EventState.STARTED) && !restore)
			{
				playerInstance.setInstanceId(event.getInstanceId());
				if (event.getConfig().spawnsPlayersRandomly())
				{
					EventLocation location = event.getConfig().getLocation();
					int[] pos = location.getZone().getZone().getRandomPoint();
					x = pos[0];
					y = pos[1];
					z = GeoData.getInstance().getHeight(pos[0], pos[1], pos[2]);
				}
				else
				{
					float r1 = Rnd.get(1000);
					int r2 = Rnd.get(100);
					x = Math.round((float) Math.cos(r1 / 1000 * 2 * Math.PI) * r2 + coordinates.getX());
					y = Math.round((float) Math.sin(r1 / 1000 * 2 * Math.PI) * r2 + coordinates.getY());
					z = GeoData.getInstance().getHeight(x, y, coordinates.getZ());
				}
			}
			else
			{
				playerInstance.setInstanceId(0);
				playerInstance.setEvent(null);
				playerInstance.returnedFromEvent();
				if (playerInstance.getEventSavedPosition().getX() == 0 &&
						playerInstance.getEventSavedPosition().getY() == 0 &&
						playerInstance.getEventSavedPosition().getZ() == 0)
				{
					x = coordinates.getX();
					y = coordinates.getY();
					z = GeoData.getInstance().getHeight(coordinates.getX(), coordinates.getY(), coordinates.getZ());
				}
				else
				{
					x = playerInstance.getEventSavedPosition().getX();
					y = playerInstance.getEventSavedPosition().getY();
					z = playerInstance.getEventSavedPosition().getZ();
				}

				// Remove all skills' reuse when the event ends
				playerInstance.removeSkillReuse(true);
			}
			playerInstance.teleToLocation(x, y, z, false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
