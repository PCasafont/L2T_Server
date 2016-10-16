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
 * @author Kerberos
 */
public class ExShowScreenMessage extends L2GameServerPacket
{
	private int type;
	private int sysMessageId;
	private int unk1;
	private int unk2;
	private int unk3;
	private int unk4;
	private int size;
	private int position;
	private boolean effect;
	private int npcStringId;
	private String text;
	private int time;

	public ExShowScreenMessage(String text, int time)
	{
		this.type = 1;
		this.sysMessageId = -1;
		this.unk1 = 0;
		this.unk2 = 0;
		this.unk3 = 0;
		this.unk4 = 0;
		this.position = 0x02;
		this.npcStringId = -1;
		this.text = text;
		this.time = time;
		this.size = 0;
		this.effect = true;
	}

	public ExShowScreenMessage(int npcStringId, int time)
	{
		this.type = 1;
		this.sysMessageId = -1;
		this.unk1 = 0;
		this.unk2 = 0;
		this.unk3 = 0;
		this.unk4 = 0;
		this.position = 0x02;
		this.npcStringId = npcStringId;
		this.text = "";
		this.time = time;
		this.size = 0;
		this.effect = false;
	}

	public ExShowScreenMessage(int npcStringId, int unk, boolean effect, int time)
	{
		this.type = 1;
		this.sysMessageId = -1;
		this.unk1 = 0;
		this.unk2 = 0;
		this.unk3 = unk;
		this.unk4 = 0;
		this.position = 0x02;
		this.npcStringId = npcStringId;
		this.text = "";
		this.time = time;
		this.size = 0;
		this.effect = effect;
	}

	public ExShowScreenMessage(int npcStringId, int pos, int time)
	{
		this.type = 1;
		this.sysMessageId = -1;
		this.unk1 = 0;
		this.unk2 = 0;
		this.unk3 = 0;
		this.unk4 = 0;
		this.position = pos;
		this.npcStringId = npcStringId;
		this.text = "";
		this.time = time;
		this.size = 0;
		this.effect = false;
	}

	public ExShowScreenMessage(int type, int messageId, int position, int unk1, int size, int unk2, int unk3, boolean showEffect, int time, int unk4, String text)
	{
		this.type = type;
		this.sysMessageId = messageId;
		this.unk1 = unk1;
		this.unk2 = unk2;
		this.unk3 = unk3;
		this.unk4 = unk4;
		this.position = position;
		this.npcStringId = -1;
		this.text = text;
		this.time = time;
		this.size = size;
		this.effect = showEffect;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.type); // 0 - system messages, 1 - your defined text
		writeD(this.sysMessageId); // system message id (this.type must be 0 otherwise no effect)
		writeD(this.position); // message position
		writeD(this.unk1); // ?
		writeD(this.size); // font size 0 - normal, 1 - small
		writeD(this.unk2); // ?
		writeD(this.unk3); // ?
		writeD(this.effect ? 1 :
				0); // upper effect (0 - disabled, 1 enabled) - this.position must be 2 (center) otherwise no effect
		writeD(this.time); // time
		writeD(this.unk4); // ?
		writeD(this.npcStringId);
		writeS(this.text); // your text (this.type must be 1, otherwise no effect)
	}
}
