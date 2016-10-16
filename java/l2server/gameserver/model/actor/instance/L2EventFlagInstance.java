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
import lombok.Getter;
import lombok.Setter;

/**
 * @author Pere
 */
public class L2EventFlagInstance extends L2NpcInstance
{
	private boolean toDelete = false;
	private EventInstance event = null;
	@Getter @Setter private EventTeam team = null;
	private EventType type;

	public L2EventFlagInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public void setEvent(EventInstance event)
	{
		this.event = event;
		if (this.event.isType(EventType.CaptureTheFlag))
		{
			type = EventType.CaptureTheFlag;
		}
		else
		{
			type = EventType.FieldDomination;
		}
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (toDelete || !event.isType(type))
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
						(event.isType(EventType.CaptureTheFlag) || event.isType(EventType.FieldDomination)))
				{
					if (event.isType(EventType.CaptureTheFlag) && player.getEvent() == event)
					{
						((CaptureTheFlag) player.getEvent()).onFlagTouched(player, getTeam());
					}
					else if (event.isType(EventType.FieldDomination) && !player.isCastingNow() &&
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



	class FlagCastFinalizer implements Runnable
	{
		private L2PcInstance player;

		FlagCastFinalizer(L2PcInstance player)
		{
			this.player = player;
		}

		@Override
		public void run()
		{
			if (player.isCastingNow())
			{
				player.sendPacket(new MagicSkillLaunched(player, 2046, 1));
				player.setIsCastingNow(false);

				if (player.getEvent() != null && player.getEvent() instanceof FieldDomination && !isToDelete())
				{
					if (getTeam() == null)
					{
						((FieldDomination) player.getEvent()).convertFlag(L2EventFlagInstance.this,
								player.getEvent().getParticipantTeam(player.getObjectId()), player);
					}
					else
					{
						((FieldDomination) player.getEvent()).convertFlag(L2EventFlagInstance.this, null, player);
					}
				}
			}
		}
	}

	public void shouldBeDeleted()
	{
		toDelete = true;
	}

	public boolean isToDelete()
	{
		return toDelete;
	}
}
