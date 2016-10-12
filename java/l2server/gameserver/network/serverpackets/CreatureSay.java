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

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public final class CreatureSay extends L2GameServerPacket
{
	// ddSS
	private int _objectId;
	private int _textType;
	private String _charName = null;
	private int _charId = 0;
	private String _text = null;
	private int _msgId = -1;
	private byte _level = 0;

	/**
	 */
	public CreatureSay(int objectId, int messageType, String charName, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_charName = charName;
		_text = text;
	}

	public CreatureSay(L2Character activeChar, int messageType, String charName, String text)
	{
		_objectId = activeChar.getObjectId();
		_textType = messageType;
		_charName = charName;
		_text = text;
		_level = (byte) activeChar.getLevel();
	}

	public CreatureSay(int objectId, int messageType, int charId, int msgId)
	{
		_objectId = objectId;
		_textType = messageType;
		_charId = charId;
		_msgId = msgId;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_textType);
		if (_charName != null)
		{
			writeS(_charName);
		}
		else
		{
			writeD(_charId);
		}
		writeD(_msgId);
		if (_text != null)
		{
			writeS(_text);
		}
		writeC(0x00);
		writeC(_level);
	}

	@Override
	public final void runImpl()
	{
		L2PcInstance _pci = getClient().getActiveChar();
		if (_pci != null)
		{
			_pci.broadcastSnoop(_textType, _charName, _text);
		}
	}
}
