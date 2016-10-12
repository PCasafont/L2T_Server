package l2server.gameserver.model.actor.instance;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.TimeController;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.events.instanced.EventTeam;
import l2server.gameserver.events.instanced.types.CaptureTheFlag;
import l2server.gameserver.events.instanced.types.FieldDomination;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author Pere
 */
public class L2EventFlagInstance extends L2NpcInstance
{
	private boolean _toDelete = false;
	private EventInstance _event = null;
	private EventTeam _team = null;
	private EventType _type;

	public L2EventFlagInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public void setEvent(EventInstance event)
	{
		_event = event;
		if (_event.isType(EventType.CaptureTheFlag))
		{
			_type = EventType.CaptureTheFlag;
		}
		else
		{
			_type = EventType.FieldDomination;
		}
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (_toDelete || !_event.isType(_type))
		{
			deleteMe();
			SpawnTable.getInstance().deleteSpawn(getSpawn(), true);
		}
		else if (this != player.getTarget())
		{
			player.setTarget(this);
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			player.sendPacket(new ValidateLocation(this));
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				if (player.getEvent() != null &&
						(_event.isType(EventType.CaptureTheFlag) || _event.isType(EventType.FieldDomination)))
				{
					if (_event.isType(EventType.CaptureTheFlag) && player.getEvent() == _event)
					{
						((CaptureTheFlag) player.getEvent()).onFlagTouched(player, getTeam());
					}
					else if (_event.isType(EventType.FieldDomination) && !player.isCastingNow() &&
							player.getEvent() != null &&
							player.getEvent().getParticipantTeam(player.getObjectId()).getFlagId() != getNpcId())
					{
						player.stopMove(null, false);

						int castingMillis = player.isMageClass() ? 5000 : 7000;
						player.broadcastPacket(new MagicSkillUse(player, 1050, 1, castingMillis, 0));
						player.sendPacket(new SetupGauge(0, castingMillis));
						player.sendMessage("Converting flag...");

						player.setLastSkillCast(SkillTable.getInstance().getInfo(1050, 1));
						FlagCastFinalizer fcf = new FlagCastFinalizer(player);
						player.setSkillCast(ThreadPoolManager.getInstance().scheduleEffect(fcf, castingMillis));
						player.forceIsCasting(
								TimeController.getGameTicks() + castingMillis / TimeController.MILLIS_IN_TICK);
					}
				}
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public EventTeam getTeam()
	{
		return _team;
	}

	public void setTeam(EventTeam team)
	{
		_team = team;
	}

	class FlagCastFinalizer implements Runnable
	{
		private L2PcInstance _player;

		FlagCastFinalizer(L2PcInstance player)
		{
			_player = player;
		}

		@Override
		public void run()
		{
			if (_player.isCastingNow())
			{
				_player.sendPacket(new MagicSkillLaunched(_player, 2046, 1));
				_player.setIsCastingNow(false);

				if (_player.getEvent() != null && _player.getEvent() instanceof FieldDomination && !isToDelete())
				{
					if (getTeam() == null)
					{
						((FieldDomination) _player.getEvent()).convertFlag(L2EventFlagInstance.this,
								_player.getEvent().getParticipantTeam(_player.getObjectId()), _player);
					}
					else
					{
						((FieldDomination) _player.getEvent()).convertFlag(L2EventFlagInstance.this, null, _player);
					}
				}
			}
		}
	}

	public void shouldBeDeleted()
	{
		_toDelete = true;
	}

	public boolean isToDelete()
	{
		return _toDelete;
	}
}
