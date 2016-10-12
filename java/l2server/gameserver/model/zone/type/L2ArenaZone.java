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
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.L2SpawnZone;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * An arena
 *
 * @author durgus
 */
public class L2ArenaZone extends L2SpawnZone
{

	public L2ArenaZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{

		if (!GMEventManager.getInstance().onEnterZone(character, this))
		{
			return;
		}

		character.setInsideZone(L2Character.ZONE_PVP, true);
		character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, true);

		if (character instanceof L2PcInstance)
		{
			if (!character.isInsideZone(L2Character.ZONE_PVP))
			{
				character
						.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_PVP, false);
		character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, false);

		if (character instanceof L2PcInstance)
		{
			if (!character.isInsideZone(L2Character.ZONE_PVP))
			{
				character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
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
