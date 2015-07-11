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
package l2tserver.gameserver.network.clientpackets;

import l2tserver.log.Log;

/**
 * Format: (ch) S
 * @author  -Wooden-
 * TODO: GodKratos: This packet is wrong in Gracia Final!!
 */
public final class RequestPCCafeCouponUse extends L2GameClientPacket
{
	private static final String _C__D0_20_REQUESTPCCAFECOUPONUSE = "[C] D0:?? RequestPCCafeCouponUse";
	private String _str;
	
	@Override
	protected void readImpl()
	{
		_str = readS();
	}
	
	@Override
	protected void runImpl()
	{
		Log.info("C5: RequestPCCafeCouponUse: S: " + _str);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_20_REQUESTPCCAFECOUPONUSE;
	}
	
}
