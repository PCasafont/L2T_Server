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

package l2server.gameserver.model.zone.type;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

/**
 * @author DaRkRaGe
 */
public class L2BossZone extends L2ZoneType
{
	private int _timeInvade;
	private boolean _enabled = true;
	private HashMap<Integer, PlayerEntry> _playerEntries;
	protected CopyOnWriteArrayList<L2Character> _raidList;
	private Location _exitLocation;

	public L2BossZone(int id)
	{
		super(id);

		_playerEntries = new HashMap<>();
		_raidList = new CopyOnWriteArrayList<>();

		GrandBossManager.getInstance().addZone(this);
	}

	private class PlayerEntry
	{
		private int _playerId;
		private long _allowedEntryTime;
		private ScheduledFuture<?> _kickTask;

		private PlayerEntry(int playerId, long allowedEntryTime)
		{
			_playerId = playerId;
			_allowedEntryTime = allowedEntryTime;
		}

		private boolean isEntryAllowed()
		{
			return _allowedEntryTime > System.currentTimeMillis();
		}

		private void setAllowedEntryTime(long b)
		{
			_allowedEntryTime = b;
		}

		private void startKickTask()
		{
			_kickTask = ThreadPoolManager.getInstance().scheduleGeneral(new KickTask(), 300000);
		}

		private void stopKickTask()
		{
			if (_kickTask != null)
			{
				_kickTask.cancel(false);
			}
		}

		private class KickTask implements Runnable
		{
			@Override
			public void run()
			{
				L2PcInstance player = L2World.getInstance().getPlayer(_playerId);
				if (player != null)
				{
					kickPlayerFromEpicZone(player);
				}
			}
		}
	}

	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "InvadeTime":
				_timeInvade = Integer.parseInt(value);
				break;
			case "EnabledByDefault":
				_enabled = Boolean.parseBoolean(value);
				break;
			case "oustLoc":
				_exitLocation =
						new Location(Integer.parseInt(value.split(",")[0]), Integer.parseInt(value.split(",")[1]),
								Integer.parseInt(value.split(",")[2]));
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	/**
	 * Boss zones have special behaviors for player characters. Players are
	 * automatically teleported out when the attempt to enter these zones,
	 * except if the time at which they enter the zone is prior to the entry
	 * expiration time set for that player. Entry expiration times are set by
	 * any one of the following: 1) A player logs out while in a zone
	 * (Expiration gets set to logoutTime + _timeInvade) 2) An external source
	 * (such as a quest or AI of NPC) set up the player for entry.
	 * <p>
	 * There exists one more case in which the player will be allowed to enter.
	 * That is if the server recently rebooted (boot-up time more recent than
	 * currentTime - _timeInvade) AND the player was in the zone prior to reboot.
	 */
	@Override
	protected void onEnter(L2Character character)
	{
		if (_enabled)
		{
			if (!(character instanceof L2Playable))
			{
				return;
			}

			L2PcInstance player = null;
			if (character instanceof L2PcInstance)
			{
				player = (L2PcInstance) character;
			}
			else
			{
				if (character instanceof L2Summon)
				{
					player = ((L2Summon) character).getOwner();
					if (!isInsideZone(player))
					{
						kickPlayerFromEpicZone(player);
					}
					return;
				}
			}

			if (player == null)
			{
				return;
			}

			//GM's and instance players are not checked
			if (player.isGM() || player.getInstanceId() != 0)
			{
				return;
			}

			PlayerEntry playerEntry = _playerEntries.get(player.getObjectId());
			if (playerEntry == null || !playerEntry.isEntryAllowed()) //Illegal entry
			{
				kickPlayerFromEpicZone(player);
				return;
			}

			player.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, true);
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (_enabled)
		{
			if (character instanceof L2PcInstance)
			{
				final L2PcInstance player = (L2PcInstance) character;
				if (player.isGM())
				{
					return;
				}

				PlayerEntry playerEntry = _playerEntries.get(player.getObjectId());
				if (playerEntry == null)
				{
					return;
				}

				player.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, false);

				if (!player.isOnline())
				{
					playerEntry.setAllowedEntryTime(System.currentTimeMillis() + _timeInvade);
				}
			}
		}

		if (character instanceof L2Attackable && character.isRaid() && !character.isDead())
		{
			((L2Attackable) character).returnHome();
		}
	}

	public void setAllowedPlayers(List<Integer> players)
	{
		if (players == null)
		{
			return;
		}

		_playerEntries.clear();

		for (int i : players)
		{
			_playerEntries.put(i, new PlayerEntry(i, System.currentTimeMillis() + 1200000)); //20 min
		}
	}

	public Set<Integer> getAllowedPlayers()
	{
		return _playerEntries.keySet();
	}

	/**
	 * Some GrandBosses send all players in zone to a specific part of the zone,
	 * rather than just removing them all. If this is the case, this command should
	 * be used. If this is no the case, then use oustAllPlayers().
	 *
	 * @param x
	 * @param y
	 * @param z
	 */
	public void movePlayersTo(int x, int y, int z)
	{
		if (_characterList.isEmpty())
		{
			return;
		}

		for (L2Character character : _characterList.values())
		{
			if (character == null)
			{
				continue;
			}

			if (character instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) character;
				if (player.isOnline())
				{
					player.teleToLocation(x, y, z);
				}
			}
		}
	}

	/**
	 * Occasionally, all players need to be sent out of the zone (for example,
	 * if the players are just running around without fighting for too long, or
	 * if all players die, etc). This call sends all online players to town and
	 * marks offline players to be teleported (by clearing their relog
	 * expiration times) when they log back in (no real need for off-line
	 * teleport).
	 */
	@Override
	public void oustAllPlayers()
	{
		if (_characterList.isEmpty())
		{
			return;
		}

		for (L2Character character : _characterList.values())
		{
			if (character == null)
			{
				continue;
			}

			if (character instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) character;
				if (player.isOnline())
				{
					kickPlayerFromEpicZone(player);
				}
			}
		}
		_playerEntries.clear();
	}

	/**
	 * This function is to be used by external sources, such as quests and AI
	 * in order to allow a player for entry into the zone for some time.  Naturally
	 * if the player does not enter within the allowed time, he/she will be
	 * teleported out again...
	 *
	 * @param player:        reference to the player we wish to allow
	 * @param durationInSec: amount of time in seconds during which entry is valid.
	 */
	public void allowPlayerEntry(L2PcInstance player, int durationInSec)
	{
		if (player == null || player.isGM())
		{
			return;
		}

		PlayerEntry playerEntry = _playerEntries.get(player.getObjectId());
		if (playerEntry == null)
		{
			_playerEntries.put(player.getObjectId(),
					new PlayerEntry(player.getObjectId(), System.currentTimeMillis() + durationInSec * 1000));
		}
		else
		{
			playerEntry.setAllowedEntryTime(System.currentTimeMillis() + durationInSec * 1000);
		}
	}

	public void removePlayer(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}

		if (!player.isGM())
		{
			_playerEntries.remove(player.getObjectId());
		}
	}

	@Override
	public void onDieInside(L2Character character, L2Character killer)
	{
		if (!_enabled)
		{
			return;
		}

		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;
			PlayerEntry entryPlayer = _playerEntries.get(player.getObjectId());
			if (entryPlayer == null)
			{
				return;
			}

			entryPlayer.startKickTask();
		}
	}

	@Override
	public void onReviveInside(L2Character character)
	{
		if (!_enabled)
		{
			return;
		}

		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;
			PlayerEntry entryPlayer = _playerEntries.get(player.getObjectId());
			if (entryPlayer == null)
			{
				return;
			}

			entryPlayer.stopKickTask();
		}
	}

	private void kickPlayerFromEpicZone(L2Character chara)
	{
		if (chara == null)
		{
			return;
		}

		chara.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, false);

		if (_exitLocation != null)
		{
			chara.teleToLocation(_exitLocation, true);
		}
		else
		{
			chara.teleToLocation(MapRegionTable.TeleportWhereType.Town);
		}
	}

	public void kickDualBoxes()
	{
		List<L2PcInstance> toBeKicked = new ArrayList<>();
		Map<String, String> allPlayerIps = new HashMap<>();
		for (L2PcInstance player : getPlayersInside())
		{
			if (player == null || !player.isOnline())
			{
				continue;
			}
			if (allPlayerIps.containsKey(player.getExternalIP()))
			{
				if (allPlayerIps.get(player.getExternalIP()).equalsIgnoreCase(player.getInternalIP()))
				{
					toBeKicked.add(player);
				}
			}
			allPlayerIps.put(player.getExternalIP(), player.getInternalIP());
		}

		if (!toBeKicked.isEmpty())
		{
			for (final L2PcInstance player : toBeKicked)
			{
				if (player == null)
				{
					continue;
				}

				player.sendPacket(
						new ExShowScreenMessage("You will be removed from the zone in 60 seconds because of dual box!",
								5000));
				ThreadPoolManager.getInstance().scheduleGeneral(() -> kickPlayerFromEpicZone(player), 60000);
			}
		}
	}
}
