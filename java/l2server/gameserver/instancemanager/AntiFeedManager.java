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

package l2server.gameserver.instancemanager;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectProcedure;
import l2server.Config;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.L2GameClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AntiFeedManager
{
	public static final int GAME_ID = 0;
	public static final int OLYMPIAD_ID = 1;
	public static final int TVT_ID = 2;

	private Map<Integer, Long> _lastDeathTimes;
	private TIntObjectHashMap<Map<Integer, Connections>> _eventIPs;

	public static AntiFeedManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private AntiFeedManager()
	{
		_lastDeathTimes = new ConcurrentHashMap<>();
		_eventIPs = new TIntObjectHashMap<>();
	}

	/**
	 * Set time of the last player's death to current
	 *
	 * @param objectId Player's objectId
	 */
	public final void setLastDeathTime(int objectId)
	{
		_lastDeathTimes.put(objectId, System.currentTimeMillis());
	}

	/**
	 * Check if current kill should be counted as non-feeded.
	 *
	 * @param attacker Attacker character
	 * @param target   Target character
	 * @return True if kill is non-feeded.
	 */
	public final boolean check(L2Character attacker, L2Character target)
	{
		if (!Config.L2JMOD_ANTIFEED_ENABLE)
		{
			return true;
		}

		if (target == null)
		{
			return false;
		}

		final L2PcInstance targetPlayer = target.getActingPlayer();
		if (targetPlayer == null)
		{
			return false;
		}

		if (attacker == null)
		{
			return false;
		}

		final L2PcInstance attackerPlayer = attacker.getActingPlayer();

		if (attackerPlayer == null)
		{
			return false;
		}

		if (attackerPlayer.getClient() == null || targetPlayer.getClient() == null)
		{
			return false;
		}

		//External IP check
		if (attackerPlayer.getExternalIP().equalsIgnoreCase(targetPlayer.getExternalIP()))
		{
			if (attackerPlayer.getInternalIP().equalsIgnoreCase(targetPlayer.getInternalIP()))
			{
				return false;
			}
		}

		//Level check
		if (attackerPlayer.getLevel() - 3 > targetPlayer.getLevel())
		{
			return false;
		}

		//Target defense
		if (targetPlayer.getPDef(target) < 800)
		{
			return false;
		}

		//Target atk spd
		if (targetPlayer.isMageClass())
		{
			if (targetPlayer.getMAtkSpd() < 800)
			{
				return false;
			}
		}
		else if (targetPlayer.getPAtkSpd() < 600)
		{
			return false;
		}

		//Clan check
		return !(attackerPlayer.getClan() != null && targetPlayer.getClan() != null &&
				attackerPlayer.getClanId() == targetPlayer.getClanId());

	}

	/**
	 * Clears all timestamps
	 */
	public final void clear()
	{
		_lastDeathTimes.clear();
	}

	/**
	 * Register new event for dualbox check.
	 * Should be called only once.
	 *
	 * @param eventId
	 */
	public final void registerEvent(int eventId)
	{
		if (!_eventIPs.containsKey(eventId))
		{
			_eventIPs.put(eventId, new HashMap<>());
		}
	}

	/**
	 * If number of all simultaneous connections from player's IP address lower than max
	 * then increment connection count and return true.
	 * Returns false if number of all simultaneous connections from player's IP address
	 * higher than max.
	 *
	 * @param eventId
	 * @param player
	 * @param max
	 * @return
	 */
	public final boolean tryAddPlayer(int eventId, L2PcInstance player, int max)
	{
		return tryAddClient(eventId, player.getClient(), max);
	}

	/**
	 * If number of all simultaneous connections from player's IP address lower than max
	 * then increment connection count and return true.
	 * Returns false if number of all simultaneous connections from player's IP address
	 * higher than max.
	 *
	 * @param eventId
	 * @param max
	 * @return
	 */
	public final boolean tryAddClient(int eventId, L2GameClient client, int max)
	{
		return true;
		/*
        if (client == null)
			return false; // unable to determine IP address

		final Map<Integer, Connections> event = _eventIPs.get(eventId);
		if (event == null)
			return false; // no such event registered

		final Integer addrHash = Integer.valueOf(client.getConnectionAddress().hashCode());
		int limit = Config.L2JMOD_DUALBOX_CHECK_WHITELIST.get(addrHash);
		limit = limit < 0 ? Integer.MAX_VALUE : limit + max;

		Connections conns;
		synchronized (event)
		{
			conns = event.get(addrHash);
			if (conns == null)
			{
				conns = new Connections();
				event.put(addrHash, conns);
			}
		}

		return conns.testAndIncrement(limit);*/
	}

	/**
	 * Decreasing number of active connection from player's IP address
	 * Returns true if success and false if any problem detected.
	 *
	 * @param eventId
	 * @param player
	 * @return
	 */
	public final boolean removePlayer(int eventId, L2PcInstance player)
	{
		final L2GameClient client = player.getClient();
		if (client == null)
		{
			return false; // unable to determine IP address
		}

		final Map<Integer, Connections> event = _eventIPs.get(eventId);
		if (event == null)
		{
			return false; // no such event registered
		}

		final Integer addrHash = client.getConnectionAddress().hashCode();
		Connections conns = event.get(addrHash);
		if (conns == null)
		{
			return false; // address not registered
		}

		synchronized (event)
		{
			if (conns.testAndDecrement())
			{
				event.remove(addrHash);
			}
		}

		return true;
	}

	/**
	 * Remove player connection IP address from all registered events lists.
	 */
	public final void onDisconnect(L2GameClient client)
	{
		if (client == null)
		{
			return;
		}

		final Integer addrHash = client.getConnectionAddress().hashCode();
		_eventIPs.forEachValue(new DisconnectProcedure(addrHash));
	}

	/**
	 * Clear all entries for this eventId.
	 *
	 * @param eventId
	 */
	public final void clear(int eventId)
	{
		final Map<Integer, Connections> event = _eventIPs.get(eventId);
		if (event != null)
		{
			event.clear();
		}
	}

	/**
	 * Returns maximum number of allowed connections (whitelist + max)
	 *
	 * @param player
	 * @param max
	 * @return
	 */
	public final int getLimit(L2PcInstance player, int max)
	{
		return getLimit(player.getClient(), max);
	}

	/**
	 * Returns maximum number of allowed connections (whitelist + max)
	 *
	 * @param client
	 * @param max
	 * @return
	 */
	public final int getLimit(L2GameClient client, int max)
	{
		if (client == null)
		{
			return max;
		}

		final Integer addrHash = client.getConnectionAddress().hashCode();
		final int limit = Config.L2JMOD_DUALBOX_CHECK_WHITELIST.get(addrHash);
		return limit < 0 ? 0 : limit + max;
	}

	private static final class Connections
	{
		private int _num = 0;

		/**
		 * Returns true if successfully incremented number of connections
		 * and false if maximum number is reached.
		 */
		@SuppressWarnings("unused")
		public final synchronized boolean testAndIncrement(int max)
		{
			if (_num < max)
			{
				_num++;
				return true;
			}
			return false;
		}

		/**
		 * Returns true if all connections are removed
		 */
		public final synchronized boolean testAndDecrement()
		{
			if (_num > 0)
			{
				_num--;
			}

			return _num == 0;
		}
	}

	private static final class DisconnectProcedure implements TObjectProcedure<Map<Integer, Connections>>
	{
		private final Integer _addrHash;

		public DisconnectProcedure(Integer addrHash)
		{
			_addrHash = addrHash;
		}

		@Override
		public final boolean execute(Map<Integer, Connections> event)
		{
			final Connections conns = event.get(_addrHash);
			if (conns != null)
			{
				synchronized (event)
				{
					if (conns.testAndDecrement())
					{
						event.remove(_addrHash);
					}
				}
			}
			return true;
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AntiFeedManager _instance = new AntiFeedManager();
	}
}
