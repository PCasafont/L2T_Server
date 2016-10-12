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

package l2server.loginserver.network.clientpackets;

import l2server.log.Log;
import l2server.loginserver.network.L2LoginClient;
import l2server.network.ReceivablePacket;

import java.util.logging.Level;

/**
 * @author KenM
 */
public abstract class L2LoginClientPacket extends ReceivablePacket<L2LoginClient>
{

	/**
	 */
	@Override
	protected final boolean read()
	{
		try
		{
			return readImpl();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "ERROR READING: " + this.getClass().getSimpleName() + ": " + e.getMessage(), e);
			return false;
		}
	}

	protected abstract boolean readImpl();
}
