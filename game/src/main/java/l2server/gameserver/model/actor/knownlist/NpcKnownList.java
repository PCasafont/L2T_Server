/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.model.actor.knownlist;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.*;
import l2server.gameserver.model.actor.instance.NpcInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.Quest;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

public class NpcKnownList extends CharKnownList {
	// =========================================================
	// Data Field
	private ScheduledFuture<?> trackingTask = null;

	// =========================================================
	// Constructor
	public NpcKnownList(Npc activeChar) {
		super(activeChar);
	}

	// =========================================================
	// Method - Public

	// =========================================================
	// Method - Private

	// =========================================================
	// Property - Public

	@Override
	public boolean addKnownObject(WorldObject object) {
		if (!super.addKnownObject(object)) {
			return false;
		}

		if (getActiveObject() instanceof NpcInstance && object instanceof Player) {
			final Npc npc = (Npc) getActiveObject();

			// Notify to scripts
			if (npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_CREATURE_SEE) != null) {
				for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_CREATURE_SEE)) {
					quest.notifyCreatureSee(npc, (Player) object, object instanceof Summon);
				}
			}
		}
		return true;
	}

	@Override
	public Npc getActiveChar() {
		return (Npc) super.getActiveChar();
	}

	@Override
	public int getDistanceToForgetObject(WorldObject object) {
		return getDistanceToWatchObject(object) + 200;
	}

	@Override
	public int getDistanceToWatchObject(WorldObject object) {
		if (object instanceof NpcInstance || !(object instanceof Creature)) {
			return 0;
		}

		if (object instanceof Playable) {
			return object.getKnownList().getDistanceToWatchObject(getActiveObject());
		}

		return 500;
	}

	//L2Master mod - support for Walking monsters aggro
	public void startTrackingTask() {
		if (trackingTask == null && getActiveChar().getAggroRange() > 0) {
			trackingTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new TrackingTask(), 2000, 2000);
		}
	}

	//L2Master mod - support for Walking monsters aggro
	public void stopTrackingTask() {
		if (trackingTask != null) {
			trackingTask.cancel(true);
			trackingTask = null;
		}
	}

	//L2Master mod - support for Walking monsters aggro
	private class TrackingTask implements Runnable {
		public TrackingTask() {
			//
		}

		@Override
		public void run() {
			if (getActiveChar() instanceof Attackable) {
				final Attackable monster = (Attackable) getActiveChar();
				if (monster.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO) {
					final Collection<Player> players = getKnownPlayers().values();
					if (players != null) {
						for (Player pl : players) {
							if (pl.isInsideRadius(monster, monster.getAggroRange(), true, false) && !pl.isDead() && !pl.isInvul(monster)) {
								monster.addDamageHate(pl, 0, 100);
								monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, pl, null);
								break;
							}
						}
					}
				}
			}
		}
	}
}
