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

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.olympiad.OlympiadParticipant;

/**
 * This class ...
 *
 * @author godson
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class ExOlympiadUserInfo extends L2GameServerPacket
{
	// chcdSddddd
	private L2PcInstance player;
	private OlympiadParticipant par = null;
	private int curHp;
	private int maxHp;
	private int curCp;
	private int maxCp;

	public ExOlympiadUserInfo(L2PcInstance player)
	{
		this.player = player;
		if (this.player != null)
		{
			this.curHp = (int) this.player.getCurrentHp();
			this.maxHp = this.player.getMaxVisibleHp();
			this.curCp = (int) this.player.getCurrentCp();
			this.maxCp = this.player.getMaxCp();
		}
		else
		{
			this.curHp = 0;
			this.maxHp = 100;
			this.curCp = 0;
			this.maxCp = 100;
		}
	}

	public ExOlympiadUserInfo(OlympiadParticipant par)
	{
		this.par = par;
		this.player = par.player;
		if (this.player != null)
		{
			this.curHp = (int) this.player.getCurrentHp();
			this.maxHp = this.player.getMaxVisibleHp();
			this.curCp = (int) this.player.getCurrentCp();
			this.maxCp = this.player.getMaxCp();
		}
		else
		{
			this.curHp = 0;
			this.maxHp = 100;
			this.curCp = 0;
			this.maxCp = 100;
		}
	}

	@Override
	protected final void writeImpl()
	{
		if (this.player != null)
		{
			writeC(this.player.getOlympiadSide());
			writeD(this.player.getObjectId());
			writeS(this.player.getName());
			writeD(this.player.getCurrentClass().getId());
		}
		else
		{
			writeC(this.par.side);
			writeD(this.par.objectId);
			writeS(this.par.name);
			writeD(this.par.baseClass);
		}

		writeD(this.curHp);
		writeD(this.maxHp);
		writeD(this.curCp);
		writeD(this.maxCp);
	}
}
