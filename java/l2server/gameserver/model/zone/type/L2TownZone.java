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

import l2server.Config;
import l2server.gameserver.events.Curfew;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.L2SpawnZone;
import l2server.gameserver.network.serverpackets.PlaySound;
import l2server.util.Rnd;

/**
 * A Town zone
 *
 * @author durgus
 */
public class L2TownZone extends L2SpawnZone
{
	private int _townId;
	private int _taxById;
	private boolean _isPeaceZone;

	public L2TownZone(int id)
	{
		super(id);

		_taxById = 0;

		// Default not peace zone
		_isPeaceZone = false;
	}

	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "townId":
				_townId = Integer.parseInt(value);
				break;
			case "taxById":
				_taxById = Integer.parseInt(value);
				break;
			case "isPeaceZone":
				_isPeaceZone = Boolean.parseBoolean(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			// PVP possible during siege, now for siege participants only
			// Could also check if this town is in siege, or if any siege is going on
			if (((L2PcInstance) character).getSiegeState() != 0 && Config.ZONE_TOWN == 1)
			{
				return;
			}

			//((L2PcInstance)character).sendMessage("You entered "+_townName);

			/*if (Config.isServer(Config.TENKAI) && Curfew.getInstance().getOnlyPeaceTown() == -1 && isInHostileTown((L2PcInstance)character))
			{
				((L2PcInstance)character).setHostileZone(true);
				((L2PcInstance)character).broadcastReputation();
				((L2PcInstance)character).sendMessage(40063);
			}*/

			//ThreadPoolManager.getInstance().scheduleGeneral(new MusicTask((L2PcInstance)character), 2000);
		}

		if (_isPeaceZone && Config.ZONE_TOWN != 2 &&
				(Curfew.getInstance().getOnlyPeaceTown() == -1 || Curfew.getInstance().getOnlyPeaceTown() == _townId))
		{
			character.setInsideZone(L2Character.ZONE_PEACE, true);
		}

		character.setInsideZone(L2Character.ZONE_TOWN, true);
	}

	@Override
	protected void onExit(L2Character character)
	{
		// TODO: there should be no exit if there was possibly no enter
		if (_isPeaceZone)
		{
			character.setInsideZone(L2Character.ZONE_PEACE, false);
		}

		character.setInsideZone(L2Character.ZONE_TOWN, false);

		// if (character instanceof L2PcInstance)
		//((L2PcInstance)character).sendMessage("You left "+_townName);

		/*if (character instanceof L2PcInstance)
		{
			((L2PcInstance)character).setHostileZone(false);
			((L2PcInstance)character).broadcastReputation();
		}*/
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
	 * Returns this zones town id (if any)
	 *
	 * @return
	 */
	public int getTownId()
	{
		return _townId;
	}

	/**
	 * Returns this town zones castle id
	 *
	 * @return
	 */
	public final int getTaxById()
	{
		return _taxById;
	}

	public final boolean isPeaceZone()
	{
		return _isPeaceZone;
	}

	@SuppressWarnings("unused")
	private boolean isInHostileTown(L2PcInstance player)
	{
		switch (_townId)
		{
			case 7:
				return player.isAtWarWithCastle(1);
			case 8:
				return player.isAtWarWithCastle(2);
			case 9:
				return player.isAtWarWithCastle(3);
			case 10:
				return player.isAtWarWithCastle(4);
			case 12:
				return player.isAtWarWithCastle(5);
			case 15:
				return player.isAtWarWithCastle(6);
			case 13:
				return player.isAtWarWithCastle(7);
			case 14:
				return player.isAtWarWithCastle(8);
			case 17:
				return player.isAtWarWithCastle(9);
			default:
				return false;
		}
	}

	class MusicTask implements Runnable
	{
		private L2PcInstance _player;

		public MusicTask(L2PcInstance player)
		{
			_player = player;
		}

		@Override
		public void run()
		{
			int rnd = Rnd.get(4) + 1;
			_player.sendPacket(new PlaySound(1, "CC_0" + rnd, 0, 0, 0, 0, 0));
		}
	}
}
