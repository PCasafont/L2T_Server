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

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public final class AskJoinPledge extends L2GameServerPacket
{

	private int requestorObjId;
	private String subPledgeName;
	private int pledgeType;
	private String pledgeName;

	public AskJoinPledge(int requestorObjId, String subPledgeName, int pledgeType, String pledgeName)
	{
		this.requestorObjId = requestorObjId;
		this.subPledgeName = subPledgeName;
		this.pledgeType = pledgeType;
		this.pledgeName = pledgeName;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.requestorObjId);
		if (this.subPledgeName != null)
		{
			writeS(this.pledgeType > 0 ? this.subPledgeName : this.pledgeName);
		}
		if (this.pledgeType != 0)
		{
			writeD(this.pledgeType);
		}
		writeS(this.pledgeName);
	}
}
