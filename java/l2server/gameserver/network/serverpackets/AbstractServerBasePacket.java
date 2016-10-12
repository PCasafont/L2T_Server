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
 * This class makes runImpl() and writeImpl() abstract for custom classes outside of this package
 *
 * @author galun
 * @version $Revision: $ $Date: $
 */
public abstract class AbstractServerBasePacket extends L2GameServerPacket
{

	/**
	 */
	@Override
	abstract public void runImpl();

	/**
	 */
	@Override
	abstract protected void writeImpl();
}
