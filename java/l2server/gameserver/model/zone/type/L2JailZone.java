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
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * A jail zone
 *
 * @author durgus
 */
public class L2JailZone extends L2ZoneType
{
	public L2JailZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_JAIL, true);
			character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, true);
			if (Config.JAIL_IS_PVP)
			{
				character.setInsideZone(L2Character.ZONE_PVP, true);
				character
						.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
			}
			if (Config.JAIL_DISABLE_TRANSACTION)
			{
				character.setInsideZone(L2Character.ZONE_NOSTORE, true);
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_JAIL, false);
			character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, false);
			if (Config.JAIL_IS_PVP)
			{
				character.setInsideZone(L2Character.ZONE_PVP, false);
				character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
			}
			if (((L2PcInstance) character).isInJail())
			{
				// when a player wants to exit jail even if he is still jailed, teleport him back to jail
				ThreadPoolManager.getInstance().scheduleGeneral(new BackToJail(character), 2000);
				character.sendMessage(
						"You cannot cheat your way out of here. You must wait until your jail time is over.");
			}
			if (Config.JAIL_DISABLE_TRANSACTION)
			{
				character.setInsideZone(L2Character.ZONE_NOSTORE, false);
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

	static class BackToJail implements Runnable
	{
		private L2PcInstance _activeChar;

		BackToJail(L2Character character)
		{
			_activeChar = (L2PcInstance) character;
		}

		@Override
		public void run()
		{
			_activeChar.teleToLocation(-114356, -249645, -2984); // Jail
		}
	}
}
