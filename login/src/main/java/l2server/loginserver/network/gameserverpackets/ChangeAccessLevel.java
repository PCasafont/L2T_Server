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

package l2server.loginserver.network.gameserverpackets;

import l2server.loginserver.GameServerThread;
import l2server.loginserver.LoginController;
import l2server.network.BaseRecievePacket;
import org.slf4j.LoggerFactory;

/**
 * @author -Wooden-
 */
public class ChangeAccessLevel extends BaseRecievePacket {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(ChangeAccessLevel.class.getName());



	public ChangeAccessLevel(byte[] decrypt, GameServerThread server) {
		super(decrypt);
		int level = readD();
		String account = readS();

		LoginController.getInstance().setAccountAccessLevel(account, level);
		log.info("Changed " + account + " access level to " + level);
	}
}
