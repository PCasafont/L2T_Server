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
public final class NpcSay extends L2GameServerPacket {
	// cddddS
	private int objectId;
	private int textType;
	private int npcId;
	private String text;
	private int npcString;
	private List<String> parameters;

	/**
	 */
	public NpcSay(int objectId, int messageType, int npcId, String text) {
		this.objectId = objectId;
		textType = messageType;
		this.npcId = 1000000 + npcId;
		npcString = -1;
		this.text = text;
	}

	public NpcSay(int objectId, int messageType, int npcId, int npcString) {
		this.objectId = objectId;
		textType = messageType;
		this.npcId = 1000000 + npcId;
		this.npcString = npcString;
		text = null;
	}

	public NpcSay(int objectId, int messageType, int npcId, NpcStringId npcStringId) {
		this.objectId = objectId;
		textType = messageType;
		this.npcId = 1000000 + npcId;
		npcString = npcStringId.getId();
		text = null;
	}

	/**
	 * String parameter for argument S1,S2,.. in npcstring-e.dat
	 *
	 * @param text
	 */
	public void addStringParameter(String text) {
		if (parameters == null) {
			parameters = new ArrayList<>();
		}
		parameters.add(text);
	}

	@Override
	protected final void writeImpl() {
		writeD(objectId);
		writeD(textType);
		writeD(npcId);
		writeD(npcString);
		if (npcString == -1) {
			writeS(text);
		} else {
			if (parameters != null) {
				for (String s : parameters) {
					writeS(s);
				}
			}
		}
	}
}
