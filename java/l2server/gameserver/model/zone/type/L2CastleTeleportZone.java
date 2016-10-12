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

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.util.Rnd;

import java.util.ArrayList;

/**
 * A castle teleporter zone
 * used for Mass Gatekeepers
 *
 * @author Kerberos
 */
public class L2CastleTeleportZone extends L2ZoneType
{
	private int[] _spawnLoc;
	private int _castleId;

	public L2CastleTeleportZone(int id)
	{
		super(id);

		_spawnLoc = new int[5];
	}

	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "castleId":
				_castleId = Integer.parseInt(value);
				break;
			case "spawnMinX":
				_spawnLoc[0] = Integer.parseInt(value);
				break;
			case "spawnMaxX":
				_spawnLoc[1] = Integer.parseInt(value);
				break;
			case "spawnMinY":
				_spawnLoc[2] = Integer.parseInt(value);
				break;
			case "spawnMaxY":
				_spawnLoc[3] = Integer.parseInt(value);
				break;
			case "spawnZ":
				_spawnLoc[4] = Integer.parseInt(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, true);
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, false);
	}

	@Override
	public void onDieInside(L2Character character, L2Character killer)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	/**
	 * Returns all players within this zone
	 *
	 * @return
	 */
	public ArrayList<L2PcInstance> getAllPlayers()
	{
		ArrayList<L2PcInstance> players = new ArrayList<>();

		for (L2Character temp : _characterList.values())
		{
			if (temp instanceof L2PcInstance)
			{
				players.add((L2PcInstance) temp);
			}
		}

		return players;
	}

	@Override
	public void oustAllPlayers()
	{
		if (_characterList == null)
		{
			return;
		}
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
					player.teleToLocation(Rnd.get(_spawnLoc[0], _spawnLoc[1]), Rnd.get(_spawnLoc[2], _spawnLoc[3]),
							_spawnLoc[4]);
				}
			}
		}
	}

	public int getCastleId()
	{
		return _castleId;
	}

	/**
	 * Get the spawn locations
	 *
	 * @return
	 */
	public int[] getSpawn()
	{
		return _spawnLoc;
	}
}
