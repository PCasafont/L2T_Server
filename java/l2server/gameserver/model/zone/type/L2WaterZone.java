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
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.serverpackets.NpcInfo;
import l2server.gameserver.network.serverpackets.ServerObjectInfo;

import java.util.Collection;

public class L2WaterZone extends L2ZoneType
{
	public L2WaterZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_WATER, true);

		if (character instanceof L2PcInstance)
		{
			if (character.isTransformed() && !((L2PcInstance) character).isCursedWeaponEquipped())
			{
				character.stopTransformation(true);
				//((L2PcInstance) character).untransform();
			}
			// TODO: update to only send speed status when that packet is known
			else
			{
				((L2PcInstance) character).broadcastUserInfo();
			}
		}
		else if (character instanceof L2Npc)
		{
			Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
			//synchronized (character.getKnownList().getKnownPlayers())
			{
				for (L2PcInstance player : plrs)
				{
					if (character.getRunSpeed() == 0)
					{
						player.sendPacket(new ServerObjectInfo((L2Npc) character, player));
					}
					else
					{
						player.sendPacket(new NpcInfo((L2Npc) character, player));
					}
				}
			}
		}

		/*
		 * if (character instanceof L2PcInstance) {
		 * ((L2PcInstance)character).sendMessage("You entered water!"); }
		 */
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_WATER, false);

		/*if (character instanceof L2PcInstance)
		{
			((L2PcInstance)character).sendMessage("You exited water!");
		}*/

		// TODO: update to only send speed status when that packet is known
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance) character).broadcastUserInfo();
		}
		else if (character instanceof L2Npc)
		{
			Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
			//synchronized (character.getKnownList().getKnownPlayers())
			{
				for (L2PcInstance player : plrs)
				{
					if (character.getRunSpeed() == 0)
					{
						player.sendPacket(new ServerObjectInfo((L2Npc) character, player));
					}
					else
					{
						player.sendPacket(new NpcInfo((L2Npc) character, player));
					}
				}
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

	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}
