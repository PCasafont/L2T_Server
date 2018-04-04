package l2server.gameserver.model.zone.type;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.events.instanced.EventInstance;
import l2server.gameserver.events.instanced.EventInstance.EventState;
import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.zone.ZoneType;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;

/**
 * @author Pere
 */
public class TenkaiEventZone extends ZoneType {
	public static int BASE_ID = 80000;

	public TenkaiEventZone(int id) {
		super(id);
	}

	@Override
	protected void onEnter(Creature character) {
	}

	@Override
	protected void onExit(Creature character) {
		if (character instanceof Player) {
			Player player = (Player) character;
			EventInstance event = player.getEvent();
			if (event != null && event.getConfig().needsClosedArena() && player.getEvent().isState(EventState.STARTED) &&
					event.getConfig().getLocation().getId() == getId() - BASE_ID &&
					(!event.isType(EventType.VIP) || event.getParticipantTeam(player.getObjectId()).getVIP().getObjectId() == player.getObjectId()) &&
					(!event.isType(EventType.CaptureTheFlag) || player.getCtfFlag() != null)) {
				ThreadPoolManager.getInstance().executeTask(new OutOfEventZoneTask(player));
			}
		}
	}

	@Override
	public void onDieInside(Creature character, Creature killer) {
	}

	@Override
	public void onReviveInside(Creature character) {
	}

	class OutOfEventZoneTask implements Runnable {
		private Player player;
		private int delay = 10;
		private boolean warned = false;

		public OutOfEventZoneTask(Player player) {
			this.player = player;
		}

		@Override
		public void run() {
			if (!isInsideZone(player) && player.isPlayingEvent()) {
				if (getDistanceToZone(player) > 500 || getZone().getHighZ() < player.getZ() || getZone().getLowZ() > player.getZ()) {
					if (delay > 0) {
						if (!warned) {
							player.sendPacket(new CreatureSay(0,
									Say2.TELL,
									"Instanced Events",
									"You left the event zone. If you don't return in 10 seconds your character will die!"));
							warned = true;
						} else if (delay <= 5) {
							player.sendPacket(new CreatureSay(0, Say2.TELL, "Instanced Events", delay + " seconds to return."));
						}

						delay--;
						ThreadPoolManager.getInstance().scheduleGeneral(this, 1000L);
					} else {
						if (player.getEvent().isType(EventType.VIP)) {
							player.getEvent().getParticipantTeam(player.getObjectId()).decreasePoints();
						}
						player.doDie(player);
					}
				} else {
					delay = 10;
					ThreadPoolManager.getInstance().scheduleGeneral(this, 1000L);
				}
			}
		}
	}
}
