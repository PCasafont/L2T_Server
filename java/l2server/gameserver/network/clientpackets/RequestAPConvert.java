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

package l2server.gameserver.network.clientpackets;

import l2server.gameserver.datatables.AbilityTable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ExAcquireAPSkillList;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.UserInfo;

/**
 * @author Pere
 */
public final class RequestAPConvert extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	/**
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		if (player.getAbilityPoints() >= 16)
		{
			sendPacket(new ExAcquireAPSkillList(getClient().getActiveChar(), false));
			return;
		}

		long requiredSp = AbilityTable.getInstance().getSpCostPerPoint(player.getAbilityPoints());
		if (player.getSp() < requiredSp)
		{
			sendPacket(new ExAcquireAPSkillList(getClient().getActiveChar(), false));
			return;
		}

		player.setSp(player.getSp() - requiredSp);
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.SP, (int) player.getSp());
		player.sendPacket(su);
		player.sendPacket(new UserInfo(player));

		player.setAbilityPoints(player.getAbilityPoints() + 1);
		sendPacket(new ExAcquireAPSkillList(getClient().getActiveChar(), true));
	}
}
