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

package l2server.loginserver.network.serverpackets;

import l2server.loginserver.network.L2LoginClient;
import l2server.network.SendablePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KenM
 */
public abstract class L2LoginServerPacket extends SendablePacket<L2LoginClient> {
	protected static Logger log = LoggerFactory.getLogger(L2LoginServerPacket.class.getName());
	
}
