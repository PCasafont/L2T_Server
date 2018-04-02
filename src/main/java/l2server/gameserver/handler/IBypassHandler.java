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

package l2server.gameserver.handler;

import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nBd
 */
public interface IBypassHandler {
	Logger log = LoggerFactory.getLogger(IBypassHandler.class.getName());

	/**
	 * this is the worker method that is called when someone uses an bypass command
	 *
	 * @return success
	 */
	boolean useBypass(String command, Player activeChar, Npc target);

	/**
	 * this method is called at initialization to register all bypasses automatically
	 *
	 * @return all known bypasses
	 */
	String[] getBypassList();
}
