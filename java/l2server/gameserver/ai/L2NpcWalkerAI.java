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
	 * @param accessor The AI accessor of the L2Character
	 */
	public L2NpcWalkerAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
	}

	public void initializeRoute(List<L2NpcWalkerNode> route)
	{
		this.route = route;

		// Here we need 1 second initial delay cause getActor().hasAI() will return null...
		// Constructor of L2NpcWalkerAI is called faster then ai object is attached in L2NpcWalkerInstance
		if (this.route != null)
		{
			walkToLocation();
			this.task = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 400);
		}
		else
		{
			Log.warning(getClass().getSimpleName() + ": Missing route data! Npc: " + this.actor);
		}
	}

	public void initializeRoute(List<L2NpcWalkerNode> route, L2PcInstance guided)
	{
		this.route = route;

		// Here we need 1 second initial delay cause getActor().hasAI() will return null...
		// Constructor of L2NpcWalkerAI is called faster then ai object is attached in L2NpcWalkerInstance
		if (this.route != null)
		{
			this.task = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 200);
		}
		else
		{
			Log.warning(getClass().getSimpleName() + ": Missing route data! Npc: " + this.actor);
		}

		this.guided = guided;
	}

	@Override
	public void run()
	{
		if (this.route == null)
		{
			this.task.cancel(false);
			return;
		}

		if (System.currentTimeMillis() < this.nextMoveTime || this.waitingForQuestResponse)
		{
			return;
		}

		int x = this.route.get(this.currentPos).getMoveX();
		int y = this.route.get(this.currentPos).getMoveY();
		int z = this.route.get(this.currentPos).getMoveZ();

		if (this.isWaiting)
		{
			if (getActor().isInsideRadius(this.guided, this.waitRadius, false, false) &&
					getActor().getTemplate().getEventQuests(Quest.QuestEventType.ON_PLAYER_ARRIVED) != null)
			{
				this.waitingForQuestResponse = true;
				for (Quest quest : getActor().getTemplate().getEventQuests(Quest.QuestEventType.ON_PLAYER_ARRIVED))
				{
					quest.notifyPlayerArrived(this);
				}
			}
			return;
		}

		if (!getActor().isInsideRadius(x, y, z, 40, false, false))
		{
			if (this.nextMoveTime != 0 && System.currentTimeMillis() > nextMoveTime + 10000L)
			{
				int destX = this.route.get(this.currentPos).getMoveX();
				int destY = this.route.get(this.currentPos).getMoveY();
				int destZ = this.route.get(this.currentPos).getMoveZ();
				setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(destX, destY, destZ, 0));
				this.nextMoveTime = System.currentTimeMillis();
			}
			return;
		}

		if (getActor().getTemplate().getEventQuests(Quest.QuestEventType.ON_ARRIVED) != null)
		{
			this.waitingForQuestResponse = true;
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

		int id = this.route.get(this.currentPos).getChatId();
		String chat = null;
		if (id == 0)
		{
			chat = this.route.get(this.currentPos).getChatText();
		}

		if (id > 0 || chat != null && !chat.isEmpty())
		{
			getActor().broadcastChat(chat, id);
		}

		//time in millis
		long delay = this.route.get(this.currentPos).getDelay() * 1000;

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

		this.nextMoveTime = System.currentTimeMillis() + delay;
	}

	/**
	 * If npc can't walk to it's target then just teleport to next point
	 *
	 * @param blocked_at_pos ignoring it
	 */
	@Override
	protected void onEvtArrivedBlocked(L2CharPosition blocked_at_pos)
	{
		Log.warning("Npc Walker ID: " + getActor().getNpcId() + ": Blocked at route position [" + this.currentPos +
				"], coords: " + blocked_at_pos.x + ", " + blocked_at_pos.y + ", " + blocked_at_pos.z +
				". Teleporting to next point");

		int destinationX = this.route.get(this.currentPos).getMoveX();
		int destinationY = this.route.get(this.currentPos).getMoveY();
		int destinationZ = this.route.get(this.currentPos).getMoveZ();

		getActor().teleToLocation(destinationX, destinationY, destinationZ, false);
		super.onEvtArrivedBlocked(blocked_at_pos);
	}

	public void walkToLocation()
	{
		if (this.currentPos < this.route.size() - 1)
		{
			this.currentPos++;
		}
		else
		{
			this.currentPos = 0;
		}

		boolean moveType = this.route.get(this.currentPos).getRunning();

		if (moveType)
		{
			getActor().setRunning();
		}
		else
		{
			getActor().setWalking();
		}

		//now we define destination
		int destinationX = this.route.get(this.currentPos).getMoveX();
		int destinationY = this.route.get(this.currentPos).getMoveY();
		int destinationZ = this.route.get(this.currentPos).getMoveZ();

		setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
				new L2CharPosition(destinationX, destinationY, destinationZ, 0));
	}

	public void walkToGuided(int distance)
	{
		if (this.guided == null)
		{
			return;
		}

		int dx = this.guided.getX() - getActor().getX();
		int dy = this.guided.getY() - getActor().getY();
		int dz = this.guided.getZ() - getActor().getZ();

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
		return this.currentPos;
	}

	public L2PcInstance getGuided()
	{
		return this.guided;
	}

	public boolean isWaiting()
	{
		return this.isWaiting;
	}

	public void setWaiting(boolean waiting)
	{
		this.isWaiting = waiting;
		this.waitingForQuestResponse = false;
	}

	public int getWaitRadius()
	{
		return this.waitRadius;
	}

	public void setWaitRadius(int radius)
	{
		this.waitRadius = radius;
	}

	public void cancelTask()
	{
		this.task.cancel(false);
	}
}
