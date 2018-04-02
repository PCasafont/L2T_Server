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

import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.olympiad.OlympiadParticipant;

/**
 * This class ...
 *
 * @author godson
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class ExOlympiadUserInfo extends L2GameServerPacket {
	// chcdSddddd
	private Player player;
	private OlympiadParticipant par = null;
	private int curHp;
	private int maxHp;
	private int curCp;
	private int maxCp;
	
	public ExOlympiadUserInfo(Player player) {
		this.player = player;
		if (player != null) {
			curHp = (int) player.getCurrentHp();
			maxHp = player.getMaxVisibleHp();
			curCp = (int) player.getCurrentCp();
			maxCp = player.getMaxCp();
		} else {
			curHp = 0;
			maxHp = 100;
			curCp = 0;
			maxCp = 100;
		}
	}
	
	public ExOlympiadUserInfo(OlympiadParticipant par) {
		this.par = par;
		player = par.player;
		if (player != null) {
			curHp = (int) player.getCurrentHp();
			maxHp = player.getMaxVisibleHp();
			curCp = (int) player.getCurrentCp();
			maxCp = player.getMaxCp();
		} else {
			curHp = 0;
			maxHp = 100;
			curCp = 0;
			maxCp = 100;
		}
	}
	
	@Override
	protected final void writeImpl() {
		if (player != null) {
			writeC(player.getOlympiadSide());
			writeD(player.getObjectId());
			writeS(player.getName());
			writeD(player.getCurrentClass().getId());
		} else {
			writeC(par.side);
			writeD(par.objectId);
			writeS(par.name);
			writeD(par.baseClass);
		}
		
		writeD(curHp);
		writeD(maxHp);
		writeD(curCp);
		writeD(maxCp);
	}
}
