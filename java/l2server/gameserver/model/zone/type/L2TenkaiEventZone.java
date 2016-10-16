package l2server.gameserver.model.zone.type;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventInstance.EventState;
import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;

/**
 * @author Pere
 */
public class L2TenkaiEventZone extends L2ZoneType
{
	public static int BASE_ID = 80000;

	public L2TenkaiEventZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;
			EventInstance event = player.getEvent();
			if (event != null && event.getConfig().needsClosedArena() &&
					player.getEvent().isState(EventState.STARTED) &&
					event.getConfig().getLocation().getId() == getId() - BASE_ID && (!event.isType(EventType.VIP) ||
					event.getParticipantTeam(player.getObjectId()).getVIP().getObjectId() == player.getObjectId()) &&
					(!event.isType(EventType.CaptureTheFlag) || player.getCtfFlag() != null))
			{
				ThreadPoolManager.getInstance().executeTask(new OutOfEventZoneTask(player));
			}
		}
	}

	/**
	 */
	@Override
	public void onDieInside(L2Character character, L2Character killer)
	{
	}

	/**
	 */
	@Override
	public void onReviveInside(L2Character character)
	{
	}

	class OutOfEventZoneTask implements Runnable
	{
		private L2PcInstance player;
		private int delay = 10;
		private boolean warned = false;

		public OutOfEventZoneTask(L2PcInstance player)
		{
			this.player = player;
		}

		@Override
		public void run()
		{
			if (!isInsideZone(player) && player.isPlayingEvent())
			{
				if (getDistanceToZone(player) > 500 || getZone().getHighZ() < player.getZ() ||
						getZone().getLowZ() > player.getZ())
				{
					if (delay > 0)
					{
						if (!warned)
						{
							player.sendPacket(new CreatureSay(0, Say2.TELL, "Instanced Events",
									"You left the event zone. If you don't return in 10 seconds your character will die!"));
							warned = true;
						}
						else if (delay <= 5)
						{
							player.sendPacket(
									new CreatureSay(0, Say2.TELL, "Instanced Events", delay + " seconds to return."));
						}

						delay--;
						ThreadPoolManager.getInstance().scheduleGeneral(this, 1000L);
					}
					else
					{
						if (player.getEvent().isType(EventType.VIP))
						{
							player.getEvent().getParticipantTeam(player.getObjectId()).decreasePoints();
						}
						player.doDie(player);
					}
				}
				else
				{
					delay = 10;
					ThreadPoolManager.getInstance().scheduleGeneral(this, 1000L);
				}
			}
		}
	}
}
