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
	private L2PcInstance _player;
	private OlympiadParticipant _par = null;
	private int _curHp;
	private int _maxHp;
	private int _curCp;
	private int _maxCp;

	public ExOlympiadUserInfo(L2PcInstance player)
	{
		_player = player;
		if (_player != null)
		{
			_curHp = (int) _player.getCurrentHp();
			_maxHp = _player.getMaxVisibleHp();
			_curCp = (int) _player.getCurrentCp();
			_maxCp = _player.getMaxCp();
		}
		else
		{
			_curHp = 0;
			_maxHp = 100;
			_curCp = 0;
			_maxCp = 100;
		}
	}

	public ExOlympiadUserInfo(OlympiadParticipant par)
	{
		_par = par;
		_player = par.player;
		if (_player != null)
		{
			_curHp = (int) _player.getCurrentHp();
			_maxHp = _player.getMaxVisibleHp();
			_curCp = (int) _player.getCurrentCp();
			_maxCp = _player.getMaxCp();
		}
		else
		{
			_curHp = 0;
			_maxHp = 100;
			_curCp = 0;
			_maxCp = 100;
		}
	}

	@Override
	protected final void writeImpl()
	{
		if (_player != null)
		{
			writeC(_player.getOlympiadSide());
			writeD(_player.getObjectId());
			writeS(_player.getName());
			writeD(_player.getCurrentClass().getId());
		}
		else
		{
			writeC(_par.side);
			writeD(_par.objectId);
			writeS(_par.name);
			writeD(_par.baseClass);
		}

		writeD(_curHp);
		writeD(_maxHp);
		writeD(_curCp);
		writeD(_maxCp);
	}
}
