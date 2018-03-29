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

package l2server.gameserver.ai;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.L2NpcWalkerNode;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.log.Log;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class L2NpcWalkerAI extends L2CharacterAI implements Runnable
{
	private static final int DEFAULT_MOVE_DELAY = 100;

	ScheduledFuture<?> task = null;
	private List<L2NpcWalkerNode> route = null;
	private int currentPos = 0;
	private long nextMoveTime = 0;

	private L2PcInstance guided = null;
	private boolean isWaiting = false;
	private int waitRadius = 100;
	private boolean waitingForQuestResponse = false;

	/**
	 * Constructor of L2CharacterAI.<BR><BR>
	 *
	 * @param creature
	 */
	public L2NpcWalkerAI(L2Character creature)
	{
		super(creature);
	}

	public void initializeRoute(List<L2NpcWalkerNode> route)
	{
		this.route = route;

		// Here we need 1 second initial delay cause getActor().hasAI() will return null...
		// Constructor of L2NpcWalkerAI is called faster then ai object is attached in L2NpcWalkerInstance
		if (route != null)
		{
			walkToLocation();
			task = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 400);
		}
		else
		{
			Log.warning(getClass().getSimpleName() + ": Missing route data! Npc: " + actor);
		}
	}

	public void initializeRoute(List<L2NpcWalkerNode> route, L2PcInstance guided)
	{
		this.route = route;

		// Here we need 1 second initial delay cause getActor().hasAI() will return null...
		// Constructor of L2NpcWalkerAI is called faster then ai object is attached in L2NpcWalkerInstance
		if (route != null)
		{
			task = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 200);
		}
		else
		{
			Log.warning(getClass().getSimpleName() + ": Missing route data! Npc: " + actor);
		}

		this.guided = guided;
	}

	@Override
	public void run()
	{
		if (route == null)
		{
			task.cancel(false);
			return;
		}

		if (System.currentTimeMillis() < nextMoveTime || waitingForQuestResponse)
		{
			return;
		}

		int x = route.get(currentPos).getMoveX();
		int y = route.get(currentPos).getMoveY();
		int z = route.get(currentPos).getMoveZ();

		if (isWaiting)
		{
			if (getActor().isInsideRadius(guided, waitRadius, false, false) &&
					getActor().getTemplate().getEventQuests(Quest.QuestEventType.ON_PLAYER_ARRIVED) != null)
			{
				waitingForQuestResponse = true;
				for (Quest quest : getActor().getTemplate().getEventQuests(Quest.QuestEventType.ON_PLAYER_ARRIVED))
				{
					quest.notifyPlayerArrived(this);
				}
			}
			return;
		}

		if (!getActor().isInsideRadius(x, y, z, 40, false, false))
		{
			if (nextMoveTime != 0 && System.currentTimeMillis() > nextMoveTime + 10000L)
			{
				int destX = route.get(currentPos).getMoveX();
				int destY = route.get(currentPos).getMoveY();
				int destZ = route.get(currentPos).getMoveZ();
				setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(destX, destY, destZ, 0));
				nextMoveTime = System.currentTimeMillis();
			}
			return;
		}

		if (getActor().getTemplate().getEventQuests(Quest.QuestEventType.ON_ARRIVED) != null)
		{
			waitingForQuestResponse = true;
			for (Quest quest : getActor().getTemplate().getEventQuests(Quest.QuestEventType.ON_ARRIVED))
			{
				quest.notifyArrived(this);
			}
			return;
		}
		else
		{
			walkToLocation();
		}

		int id = route.get(currentPos).getChatId();
		String chat = null;
		if (id == 0)
		{
			chat = route.get(currentPos).getChatText();
		}

		if (id > 0 || chat != null && !chat.isEmpty())
		{
			getActor().broadcastChat(chat, id);
		}

		//time in millis
		long delay = route.get(currentPos).getDelay() * 1000;

		//delay += (getActor().getPosition().getWorldPosition().distanceSquaredTo(new Point3D(x, y, z)) / (float)getActor().getWalkSpeed()) * 1000;

		//sleeps between each move
		if (delay < 0)
		{
			delay = DEFAULT_MOVE_DELAY;
			if (Config.DEVELOPER)
			{
				Log.warning("Wrong Delay Set in Npc Walker Functions = " + delay + " secs, using default delay: " +
						DEFAULT_MOVE_DELAY + " secs instead.");
			}
		}

		nextMoveTime = System.currentTimeMillis() + delay;
	}

	/**
	 * If npc can't walk to it's target then just teleport to next point
	 *
	 * @param blocked_at_pos ignoring it
	 */
	@Override
	protected void onEvtArrivedBlocked(L2CharPosition blocked_at_pos)
	{
		Log.warning("Npc Walker ID: " + getActor().getNpcId() + ": Blocked at route position [" + currentPos +
				"], coords: " + blocked_at_pos.x + ", " + blocked_at_pos.y + ", " + blocked_at_pos.z +
				". Teleporting to next point");

		int destinationX = route.get(currentPos).getMoveX();
		int destinationY = route.get(currentPos).getMoveY();
		int destinationZ = route.get(currentPos).getMoveZ();

		getActor().teleToLocation(destinationX, destinationY, destinationZ, false);
		super.onEvtArrivedBlocked(blocked_at_pos);
	}

	public void walkToLocation()
	{
		if (currentPos < route.size() - 1)
		{
			currentPos++;
		}
		else
		{
			currentPos = 0;
		}

		boolean moveType = route.get(currentPos).getRunning();

		if (moveType)
		{
			getActor().setRunning();
		}
		else
		{
			getActor().setWalking();
		}

		//now we define destination
		int destinationX = route.get(currentPos).getMoveX();
		int destinationY = route.get(currentPos).getMoveY();
		int destinationZ = route.get(currentPos).getMoveZ();

		setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
				new L2CharPosition(destinationX, destinationY, destinationZ, 0));
	}

	public void walkToGuided(int distance)
	{
		if (guided == null)
		{
			return;
		}

		int dx = guided.getX() - getActor().getX();
		int dy = guided.getY() - getActor().getY();
		int dz = guided.getZ() - getActor().getZ();

		double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

		int destinationX = getActor().getX() + (int) Math.round(distance * dx / dist);
		int destinationY = getActor().getY() + (int) Math.round(distance * dy / dist);
		int destinationZ = getActor().getZ() + (int) Math.round(distance * dz / dist);

		setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
				new L2CharPosition(destinationX, destinationY, destinationZ, 0));
	}

	@Override
	public L2Npc getActor()
	{
		return (L2Npc) super.getActor();
	}

	public int getCurrentPos()
	{
		return currentPos;
	}

	public L2PcInstance getGuided()
	{
		return guided;
	}

	public boolean isWaiting()
	{
		return isWaiting;
	}

	public void setWaiting(boolean waiting)
	{
		isWaiting = waiting;
		waitingForQuestResponse = false;
	}

	public int getWaitRadius()
	{
		return waitRadius;
	}

	public void setWaitRadius(int radius)
	{
		waitRadius = radius;
	}

	public void cancelTask()
	{
		task.cancel(false);
	}
}
