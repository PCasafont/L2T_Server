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
import l2server.gameserver.model.zone.L2SpawnZone;

/**
 * @author UnAfraid
 */
public class L2ConditionZone extends L2SpawnZone
{
	private boolean NO_ITEM_DROP = false;
	private boolean NO_BOOKMARK = false;

	public L2ConditionZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equalsIgnoreCase("NoBookmark"))
		{
			NO_BOOKMARK = Boolean.parseBoolean(value);
		}
		else if (name.equalsIgnoreCase("NoItemDrop"))
		{
			NO_ITEM_DROP = Boolean.parseBoolean(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			if (NO_BOOKMARK)
			{
				character.setInsideZone(L2Character.ZONE_NOBOOKMARK, true);
			}
			if (NO_ITEM_DROP)
			{
				character.setInsideZone(L2Character.ZONE_NOITEMDROP, true);
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{

		if (character instanceof L2PcInstance)
		{
			if (NO_BOOKMARK)
			{
				character.setInsideZone(L2Character.ZONE_NOBOOKMARK, false);
			}
			if (NO_ITEM_DROP)
			{
				character.setInsideZone(L2Character.ZONE_NOITEMDROP, false);
			}
		}
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
