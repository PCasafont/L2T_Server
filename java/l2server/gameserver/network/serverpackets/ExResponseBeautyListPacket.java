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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.datatables.BeautyTable;
import l2server.gameserver.datatables.BeautyTable.BeautyInfo;
import l2server.gameserver.datatables.BeautyTable.BeautyTemplate;

/**
 * @author Pere
 */
public final class ExResponseBeautyListPacket extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeD(0);
		BeautyTemplate template = BeautyTable.getInstance().getTemplate(0);
		writeD(template.getHairStyles().size());
		for (BeautyInfo info : template.getHairStyles().values())
		{
			writeD(info.getParentId());
			writeD(info.getId());
			writeD(info.getUnk()); // ???
			writeD(info.getAdenaPrice());
			writeD(info.getTicketPrice());
			writeD(99999999); // Remaining units
		}

		writeD(1);
		writeD(template.getFaceStyles().size());
		for (BeautyInfo info : template.getFaceStyles().values())
		{
			writeD(info.getParentId());
			writeD(info.getId());
			writeD(info.getUnk()); // ???
			writeD(info.getAdenaPrice());
			writeD(info.getTicketPrice());
			writeD(99999999); // Remaining units
		}

		writeD(2);
		writeD(template.getHairColors().size());
		for (BeautyInfo info : template.getHairColors().values())
		{
			writeD(info.getParentId());
			writeD(info.getId());
			writeD(info.getUnk()); // ???
			writeD(info.getAdenaPrice());
			writeD(info.getTicketPrice());
			writeD(99999999); // Remaining units
		}
	}
}
