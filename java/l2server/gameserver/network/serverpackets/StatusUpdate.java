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

import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.L2Character;

import java.util.ArrayList;

/**
 * 01				// Packet Identifier <BR>
 * c6 37 50 40	   // ObjectId <BR><BR>
 * <p>
 * 01 00			 // Number of Attribute Trame of the Packet <BR><BR>
 * <p>
 * c6 37 50 40	   // Attribute Identifier : 01-Level, 02-Experience, 03-STR, 04-DEX, 05-CON, 06-INT, 07-WIT, 08-MEN, 09-Current HP, 0a, Max HP...<BR>
 * cd 09 00 00	   // Attribute Value <BR>
 * <p>
 * format   d d(dd)
 *
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/27 15:29:39 $
 */
public final class StatusUpdate extends L2GameServerPacket
{
	//private static final int HP_MOD = 10000000;

	public static final int LEVEL = 0x01;
	public static final int EXP = 0x02;
	public static final int STR = 0x03;
	public static final int DEX = 0x04;
	public static final int CON = 0x05;
	public static final int INT = 0x06;
	public static final int WIT = 0x07;
	public static final int MEN = 0x08;

	public static final int CUR_HP = 0x09;
	public static final int MAX_HP = 0x0a;
	public static final int CUR_MP = 0x0b;
	public static final int MAX_MP = 0x0c;

	public static final int SP = 0x0d;
	public static final int CUR_LOAD = 0x0e;
	public static final int MAX_LOAD = 0x0f;

	public static final int P_ATK = 0x11;
	public static final int ATK_SPD = 0x12;
	public static final int P_DEF = 0x13;
	public static final int EVASION = 0x14;
	public static final int ACCURACY = 0x15;
	public static final int CRITICAL = 0x16;
	public static final int M_ATK = 0x17;
	public static final int CAST_SPD = 0x18;
	public static final int M_DEF = 0x19;
	public static final int PVP_FLAG = 0x1a;
	public static final int REPUTATION = 0x1b;

	public static final int CUR_CP = 0x21;
	public static final int MAX_CP = 0x22;

	private int _objectId;
	private int _causerId = 0;
	private int _display = 0;
	//private int _maxHp = -1;
	private ArrayList<Attribute> _attributes;

	static class Attribute
	{
		/**
		 * id values
		 * 09 - current health
		 * 0a - max health
		 * 0b - current mana
		 * 0c - max mana
		 */
		public int id;
		public int value;

		Attribute(int pId, int pValue)
		{
			id = pId;
			value = pValue;
		}
	}

	public enum StatusUpdateDisplay
	{
		NONE, NORMAL, SHOW, MANA, UNK2, UNK3, UNK4, UNK5, UNK6, UNK7, DOT,
	}

	/**
	 * Create {@link StatusUpdate} packet for given {@link L2Object}.
	 *
	 * @param object
	 */
	public StatusUpdate(L2Object object)
	{
		_attributes = new ArrayList<>();
		_objectId = object.getObjectId();
		/*if (object instanceof L2Attackable || object instanceof L2Playable
                && getClient() != null && getClient().getActiveChar() != null
				&& getClient().getActiveChar().getObjectId() != _objectId)
			_maxHp = ((L2Character) object).getMaxVisibleHp();*/
	}

	public StatusUpdate(L2Object object, L2Character causer, StatusUpdateDisplay display)
	{
		this(object);
		_causerId = causer != null ? causer.getObjectId() : 0;
		_display = display.ordinal();
	}

	public void addAttribute(int id, int level)
	{
        /*if (_maxHp != -1)
		{
			if (id == CUR_HP)
			{
				level = (int)((level / (float)_maxHp) * HP_MOD);
				_attributes.add(new Attribute(MAX_HP, HP_MOD)); // This is a ninja fix
			}
			else if (id == MAX_HP)
				level = HP_MOD;
		}*/
		_attributes.add(new Attribute(id, level));
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_causerId);
		writeC(_display);
		writeC(_attributes.size());

		for (Attribute temp : _attributes)
		{
			writeC(temp.id);
			writeD(temp.value);
		}
	}
}
