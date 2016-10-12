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

	ScheduledFuture<?> _task = null;
	private List<L2NpcWalkerNode> _route = null;
	private int _currentPos = 0;
	private long _nextMoveTime = 0;

	private L2PcInstance _guided = null;
	private boolean _isWaiting = false;
	private int _waitRadius = 100;
	private boolean _waitingForQuestResponse = false;

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
		_route = route;

		// Here we need 1 second initial delay cause getActor().hasAI() will return null...
		// Constructor of L2NpcWalkerAI is called faster then ai object is attached in L2NpcWalkerInstance
		if (_route != null)
		{
			walkToLocation();
			_task = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 400);
		}
		else
		{
			Log.warning(getClass().getSimpleName() + ": Missing route data! Npc: " + _actor);
		}
	}

	public void initializeRoute(List<L2NpcWalkerNode> route, L2PcInstance guided)
	{
		_route = route;

		// Here we need 1 second initial delay cause getActor().hasAI() will return null...
		// Constructor of L2NpcWalkerAI is called faster then ai object is attached in L2NpcWalkerInstance
		if (_route != null)
		{
			_task = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 200);
		}
		else
		{
			Log.warning(getClass().getSimpleName() + ": Missing route data! Npc: " + _actor);
		}

		_guided = guided;
	}

	@Override
	public void run()
	{
		if (_route == null)
		{
			_task.cancel(false);
			return;
		}

		if (System.currentTimeMillis() < _nextMoveTime || _waitingForQuestResponse)
		{
			return;
		}

		int x = _route.get(_currentPos).getMoveX();
		int y = _route.get(_currentPos).getMoveY();
		int z = _route.get(_currentPos).getMoveZ();

		if (_isWaiting)
		{
			if (getActor().isInsideRadius(_guided, _waitRadius, false, false) &&
					getActor().getTemplate().getEventQuests(Quest.QuestEventType.ON_PLAYER_ARRIVED) != null)
			{
				_waitingForQuestResponse = true;
				for (Quest quest : getActor().getTemplate().getEventQuests(Quest.QuestEventType.ON_PLAYER_ARRIVED))
				{
					quest.notifyPlayerArrived(this);
				}
			}
			return;
		}

		if (!getActor().isInsideRadius(x, y, z, 40, false, false))
		{
			if (_nextMoveTime != 0 && System.currentTimeMillis() > _nextMoveTime + 10000L)
			{
				int destX = _route.get(_currentPos).getMoveX();
				int destY = _route.get(_currentPos).getMoveY();
				int destZ = _route.get(_currentPos).getMoveZ();
				setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(destX, destY, destZ, 0));
				_nextMoveTime = System.currentTimeMillis();
			}
			return;
		}

		if (getActor().getTemplate().getEventQuests(Quest.QuestEventType.ON_ARRIVED) != null)
		{
			_waitingForQuestResponse = true;
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

		int id = _route.get(_currentPos).getChatId();
		String chat = null;
		if (id == 0)
		{
			chat = _route.get(_currentPos).getChatText();
		}

		if (id > 0 || chat != null && !chat.isEmpty())
		{
			getActor().broadcastChat(chat, id);
		}

		//time in millis
		long delay = _route.get(_currentPos).getDelay() * 1000;

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

		_nextMoveTime = System.currentTimeMillis() + delay;
	}

	/**
	 * If npc can't walk to it's target then just teleport to next point
	 *
	 * @param blocked_at_pos ignoring it
	 */
	@Override
	protected void onEvtArrivedBlocked(L2CharPosition blocked_at_pos)
	{
		Log.warning("Npc Walker ID: " + getActor().getNpcId() + ": Blocked at route position [" + _currentPos +
				"], coords: " + blocked_at_pos.x + ", " + blocked_at_pos.y + ", " + blocked_at_pos.z +
				". Teleporting to next point");

		int destinationX = _route.get(_currentPos).getMoveX();
		int destinationY = _route.get(_currentPos).getMoveY();
		int destinationZ = _route.get(_currentPos).getMoveZ();

		getActor().teleToLocation(destinationX, destinationY, destinationZ, false);
		super.onEvtArrivedBlocked(blocked_at_pos);
	}

	public void walkToLocation()
	{
		if (_currentPos < _route.size() - 1)
		{
			_currentPos++;
		}
		else
		{
			_currentPos = 0;
		}

		boolean moveType = _route.get(_currentPos).getRunning();

		if (moveType)
		{
			getActor().setRunning();
		}
		else
		{
			getActor().setWalking();
		}

		//now we define destination
		int destinationX = _route.get(_currentPos).getMoveX();
		int destinationY = _route.get(_currentPos).getMoveY();
		int destinationZ = _route.get(_currentPos).getMoveZ();

		setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
				new L2CharPosition(destinationX, destinationY, destinationZ, 0));
	}

	public void walkToGuided(int distance)
	{
		if (_guided == null)
		{
			return;
		}

		int dx = _guided.getX() - getActor().getX();
		int dy = _guided.getY() - getActor().getY();
		int dz = _guided.getZ() - getActor().getZ();

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
		return _currentPos;
	}

	public L2PcInstance getGuided()
	{
		return _guided;
	}

	public boolean isWaiting()
	{
		return _isWaiting;
	}

	public void setWaiting(boolean waiting)
	{
		_isWaiting = waiting;
		_waitingForQuestResponse = false;
	}

	public int getWaitRadius()
	{
		return _waitRadius;
	}

	public void setWaitRadius(int radius)
	{
		_waitRadius = radius;
	}

	public void cancelTask()
	{
		_task.cancel(false);
	}
}
