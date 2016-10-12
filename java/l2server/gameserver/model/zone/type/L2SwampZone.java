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

import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.serverpackets.EventTrigger;

/**
 * another type of zone where your speed is changed
 *
 * @author kerberos
 */
public class L2SwampZone extends L2ZoneType
{
	private int _move_bonus;
	private int _castleId;
	private int _eventId;
	private Castle _castle;

	public L2SwampZone(int id)
	{
		super(id);

		// Setup default speed reduce (in %)
		_move_bonus = -50;
		_castleId = 0;
		_eventId = 0;
		_castle = null;
	}

	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "move_bonus":
				_move_bonus = Integer.parseInt(value);
				break;
			case "castleId":
				_castleId = Integer.parseInt(value);
				break;
			case "eventId":
				_eventId = Integer.parseInt(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	private Castle getCastle()
	{
		if (_castleId > 0 && _castle == null)
		{
			_castle = CastleManager.getInstance().getCastleById(_castleId);
		}

		return _castle;
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (getCastle() != null)
		{
			// castle zones active only during siege
			if (!getCastle().getSiege().getIsInProgress())
			{
				return;
			}
			boolean isTrapActive = getCastle().getSiege().isTrapsActive();
			final L2PcInstance player = character.getActingPlayer();
			if (player != null)
			{
				//Send it to all, even defenders
				if (_eventId > 0)
				{
					player.sendPacket(new EventTrigger(_eventId, isTrapActive));
				}

				if (!isTrapActive)
				{
					return;
				}

				// defenders not affected
				if (player.isInSiege() && player.getSiegeState() == 2)
				{
					return;
				}
			}
		}

		character.setInsideZone(L2Character.ZONE_SWAMP, true);
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance) character).broadcastUserInfo();
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		// don't broadcast info if not needed
		if (character.isInsideZone(L2Character.ZONE_SWAMP))
		{
			character.setInsideZone(L2Character.ZONE_SWAMP, false);
			if (character instanceof L2PcInstance)
			{
				((L2PcInstance) character).broadcastUserInfo();
			}
		}
	}

	public int getMoveBonus()
	{
		return _move_bonus;
	}

	@Override
	public void onDieInside(L2Character character, L2Character killer)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}
}
