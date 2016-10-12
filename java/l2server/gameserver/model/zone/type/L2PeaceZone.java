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

import l2server.gameserver.instancemanager.GMEventManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.zone.L2ZoneType;

/**
 * A peaceful zone
 *
 * @author durgus
 */
public class L2PeaceZone extends L2ZoneType
{
	boolean _enabled;

	public L2PeaceZone(int id)
	{
		super(id);

		_enabled = true;
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (!_enabled)
		{
			return;
		}

		if (!GMEventManager.getInstance().onEnterZone(character, this))
		{
			return;
		}

		character.setInsideZone(L2Character.ZONE_PEACE, true);
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_PEACE, false);
	}

	@Override
	public void onDieInside(L2Character character, L2Character killer)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	public boolean isEnabled()
	{
		return _enabled;
	}

	public void setZoneEnabled(boolean val)
	{
		_enabled = val;

		for (L2Character chara : getCharactersInside().values())
		{
			if (chara == null)
			{
				continue;
			}

			if (_enabled)
			{
				onEnter(chara);
			}
			else
			{
				onExit(chara);
			}
		}
	}
}
