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

import l2server.gameserver.network.NpcStringId;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kerberos
 */
public final class NpcSay extends L2GameServerPacket
{
	// cddddS
	private int objectId;
	private int textType;
	private int npcId;
	private String text;
	private int npcString;
	private List<String> parameters;

	/**
	 */
	public NpcSay(int objectId, int messageType, int npcId, String text)
	{
		this.objectId = objectId;
		this.textType = messageType;
		this.npcId = 1000000 + npcId;
		this.npcString = -1;
		this.text = text;
	}

	public NpcSay(int objectId, int messageType, int npcId, int npcString)
	{
		this.objectId = objectId;
		this.textType = messageType;
		this.npcId = 1000000 + npcId;
		this.npcString = npcString;
		this.text = null;
	}

	public NpcSay(int objectId, int messageType, int npcId, NpcStringId npcStringId)
	{
		this.objectId = objectId;
		this.textType = messageType;
		this.npcId = 1000000 + npcId;
		this.npcString = npcStringId.getId();
		this.text = null;
	}

	/**
	 * String parameter for argument S1,S2,.. in npcstring-e.dat
	 *
	 * @param text
	 */
	public void addStringParameter(String text)
	{
		if (this.parameters == null)
		{
			this.parameters = new ArrayList<>();
		}
		this.parameters.add(text);
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.objectId);
		writeD(this.textType);
		writeD(this.npcId);
		writeD(this.npcString);
		if (this.npcString == -1)
		{
			writeS(this.text);
		}
		else
		{
			if (this.parameters != null)
			{
				for (String s : this.parameters)
				{
					writeS(s);
				}
			}
		}
	}
}
