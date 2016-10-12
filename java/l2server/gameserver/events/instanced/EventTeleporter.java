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
	private L2PcInstance _playerInstance = null;
	private Point3D _coordinates = null;
	private boolean _restore = false;
	private boolean _heal = true;

	public EventTeleporter(L2PcInstance playerInstance, Point3D coordinates, boolean fastSchedule, boolean restore)
	{
		_playerInstance = playerInstance;
		_coordinates = coordinates;
		_restore = restore;

		long delay = (playerInstance.getEvent() == null || playerInstance.getEvent().isState(EventState.STARTED) ?
				Config.INSTANCED_EVENT_RESPAWN_TELEPORT_DELAY : Config.INSTANCED_EVENT_START_LEAVE_TELEPORT_DELAY) *
				1000;

		ThreadPoolManager.getInstance().scheduleGeneral(this, fastSchedule ? 0 : delay);
	}

	public EventTeleporter(L2PcInstance playerInstance, Point3D coordinates, boolean fastSchedule, boolean restore, boolean heal)
	{
		_playerInstance = playerInstance;
		_coordinates = coordinates;
		_restore = restore;
		_heal = heal;

		long delay = (playerInstance.getEvent() == null || playerInstance.getEvent().isState(EventState.STARTED) ?
				Config.INSTANCED_EVENT_RESPAWN_TELEPORT_DELAY : Config.INSTANCED_EVENT_START_LEAVE_TELEPORT_DELAY) *
				1000;

		ThreadPoolManager.getInstance().scheduleGeneral(this, fastSchedule ? 0 : delay);
	}

	@Override
	public void run()
	{
		if (_playerInstance == null)
		{
			return;
		}

		EventInstance event = _playerInstance.getEvent();
		if (event == null)
		{
			return;
		}

		try
		{
			for (L2Abnormal effect : _playerInstance.getAllEffects())
			{
				if (effect != null)
				{
					effect.exit();
				}
			}

			L2PetInstance pet = _playerInstance.getPet();
			if (pet != null)
			{
				// In LC, SS and SS2, players don't need summons and summons are even able to attack during event so better unsummon
				if (pet.isMountable() || event.isType(EventType.LuckyChests) ||
						event.isType(EventType.StalkedSalkers) || event.isType(EventType.SimonSays))
				{
					pet.unSummon(_playerInstance);
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
			for (L2SummonInstance summon : _playerInstance.getSummons())
			{
				// In LC, SS and SS2, players don't need summons and summons are even able to attack during event so better unsummon
				if (summon.isMountable() || event.isType(EventType.LuckyChests) ||
						event.isType(EventType.StalkedSalkers) || event.isType(EventType.SimonSays))
				{
					summon.unSummon(_playerInstance);
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
				_playerInstance.leaveParty();
			}

			for (L2SummonInstance summon : _playerInstance.getSummons())
			{
				// In LC, SS and SS2, players don't need summons and summons are even able to attack during event so better unsummon
				if (event.isType(EventType.LuckyChests) || event.isType(EventType.StalkedSalkers) ||
						event.isType(EventType.SimonSays))
				{
					summon.unSummon(_playerInstance);
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

			if (_playerInstance.isDead())
			{
				_playerInstance.restoreExp(100.0);
				_playerInstance.doRevive();
			}

			if (_heal)
			{
				_playerInstance.setCurrentCp(_playerInstance.getMaxCp());
				_playerInstance.setCurrentHp(_playerInstance.getMaxHp());
				_playerInstance.setCurrentMp(_playerInstance.getMaxMp());
			}

			int x = 0, y = 0, z = 0;
			if (event.isState(EventState.STARTED) && !_restore)
			{
				_playerInstance.setInstanceId(event.getInstanceId());
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
					x = Math.round((float) Math.cos(r1 / 1000 * 2 * Math.PI) * r2 + _coordinates.getX());
					y = Math.round((float) Math.sin(r1 / 1000 * 2 * Math.PI) * r2 + _coordinates.getY());
					z = GeoData.getInstance().getHeight(x, y, _coordinates.getZ());
				}
			}
			else
			{
				_playerInstance.setInstanceId(0);
				_playerInstance.setEvent(null);
				_playerInstance.returnedFromEvent();
				if (_playerInstance.getEventSavedPosition().getX() == 0 &&
						_playerInstance.getEventSavedPosition().getY() == 0 &&
						_playerInstance.getEventSavedPosition().getZ() == 0)
				{
					x = _coordinates.getX();
					y = _coordinates.getY();
					z = GeoData.getInstance().getHeight(_coordinates.getX(), _coordinates.getY(), _coordinates.getZ());
				}
				else
				{
					x = _playerInstance.getEventSavedPosition().getX();
					y = _playerInstance.getEventSavedPosition().getY();
					z = _playerInstance.getEventSavedPosition().getZ();
				}

				// Remove all skills' reuse when the event ends
				_playerInstance.removeSkillReuse(true);
			}
			_playerInstance.teleToLocation(x, y, z, false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
