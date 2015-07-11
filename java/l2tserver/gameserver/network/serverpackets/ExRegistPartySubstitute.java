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
package l2tserver.gameserver.network.serverpackets;

/**
 * @author Erlandys
 *
 */
public class ExRegistPartySubstitute extends L2GameServerPacket
{
	private static final String _S__FE_105_EXREGISTPARTYSUBSTITUTE = "[S] FE:105 ExRegistPartySubstitute";

	int k[];

	public ExRegistPartySubstitute(int l[])
	{
		k = l;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x106);
		writeD(k[0]);
		writeD(k[1]);
	}

	@Override
	public String getType()
	{
		return _S__FE_105_EXREGISTPARTYSUBSTITUTE;
	}
}
