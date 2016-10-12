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

import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.L2SpawnZone;

/**
 * A castle zone
 *
 * @author durgus
 */
public class L2FortZone extends L2SpawnZone
{
	private int _fortId;

	public L2FortZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("fortId"))
		{
			_fortId = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_FORT, true);
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_FORT, false);
	}

	@Override
	public void onDieInside(L2Character character, L2Character killer)
	{

	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	public void updateZoneStatusForCharactersInside()
	{
	}

	/**
	 * Removes all foreigners from the fort
	 *
	 * @param owningClan
	 */
	public void banishForeigners(L2Clan owningClan)
	{
		for (L2Character temp : _characterList.values())
		{
			if (!(temp instanceof L2PcInstance))
			{
				continue;
			}
			if (((L2PcInstance) temp).getClan() == owningClan)
			{
				continue;
			}

			temp.teleToLocation(
					MapRegionTable.TeleportWhereType.Town); // TODO: shouldnt be town, its outside of fort
		}
	}

	public int getFortId()
	{
		return _fortId;
	}
}
