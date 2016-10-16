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

package handlers.bypasshandlers;

import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.DirectEnchantMultiSellList;
import l2server.gameserver.network.serverpackets.DirectEnchantMultiSellList.DirectEnchantMultiSellConfig;
import l2server.gameserver.network.serverpackets.EnchantMultiSellList;

import java.util.StringTokenizer;

public class EnchantMultisell implements IBypassHandler
{
	private static final String[] COMMANDS = {"EnchantMultisell", "DirectEnchantMultisell"};

	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
	{
		if (target == null)
		{
			return false;
		}

		StringTokenizer st = new StringTokenizer(command, " ");
		String com = st.nextToken();
		if (com.equalsIgnoreCase("EnchantMultisell"))
		{
			activeChar.sendPacket(new EnchantMultiSellList(activeChar));
			return true;
		}

		//int shopId = Integer.parseInt(st.nextToken());
		//EnchantMultiSellConfig config = EnchantMultiSellConfig.getConfig(shopId);
		DirectEnchantMultiSellConfig config = DirectEnchantMultiSellConfig.valueOf(st.nextToken());
		if (config == null)
		{
			return false;
		}

		activeChar.sendPacket(new DirectEnchantMultiSellList(activeChar, config));

		return true;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
