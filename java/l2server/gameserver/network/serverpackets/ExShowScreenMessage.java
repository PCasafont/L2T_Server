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
		type = 1;
		sysMessageId = -1;
		unk1 = 0;
		unk2 = 0;
		unk3 = 0;
		unk4 = 0;
		position = 0x02;
		npcStringId = -1;
		this.text = text;
		this.time = time;
		size = 0;
		effect = true;
	}

	public ExShowScreenMessage(int npcStringId, int time)
	{
		type = 1;
		sysMessageId = -1;
		unk1 = 0;
		unk2 = 0;
		unk3 = 0;
		unk4 = 0;
		position = 0x02;
		this.npcStringId = npcStringId;
		text = "";
		this.time = time;
		size = 0;
		effect = false;
	}

	public ExShowScreenMessage(int npcStringId, int unk, boolean effect, int time)
	{
		type = 1;
		sysMessageId = -1;
		unk1 = 0;
		unk2 = 0;
		unk3 = unk;
		unk4 = 0;
		position = 0x02;
		this.npcStringId = npcStringId;
		text = "";
		this.time = time;
		size = 0;
		this.effect = effect;
	}

	public ExShowScreenMessage(int npcStringId, int pos, int time)
	{
		type = 1;
		sysMessageId = -1;
		unk1 = 0;
		unk2 = 0;
		unk3 = 0;
		unk4 = 0;
		position = pos;
		this.npcStringId = npcStringId;
		text = "";
		this.time = time;
		size = 0;
		effect = false;
	}

	public ExShowScreenMessage(int type, int messageId, int position, int unk1, int size, int unk2, int unk3, boolean showEffect, int time, int unk4, String text)
	{
		this.type = type;
		sysMessageId = messageId;
		this.unk1 = unk1;
		this.unk2 = unk2;
		this.unk3 = unk3;
		this.unk4 = unk4;
		this.position = position;
		npcStringId = -1;
		this.text = text;
		this.time = time;
		this.size = size;
		effect = showEffect;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(type); // 0 - system messages, 1 - your defined text
		writeD(sysMessageId); // system message id (this.type must be 0 otherwise no effect)
		writeD(position); // message position
		writeD(unk1); // ?
		writeD(size); // font size 0 - normal, 1 - small
		writeD(unk2); // ?
		writeD(unk3); // ?
		writeD(effect ? 1 :
				0); // upper effect (0 - disabled, 1 enabled) - this.position must be 2 (center) otherwise no effect
		writeD(time); // time
		writeD(unk4); // ?
		writeD(npcStringId);
		writeS(text); // your text (this.type must be 1, otherwise no effect)
	}
}
