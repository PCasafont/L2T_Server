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

public class ExSendUIEvent extends L2GameServerPacket
{
	private int _uiType;
	private int _isIncrease;
	private int _startTime;
	private int _endTime;
	private String _text;
	private int _npcString;

	//UI Types
	public static int TYPE_COUNT_DOWN = 0;
	public static int TYPE_REMOVE = 1;
	public static int TYPE_ISTINA = 2;
	public static int TYPE_NORNIL = 5;
	public static int TYPE_DRACO_INCUBATION_1 = 6;
	public static int TYPE_DRACO_INCUBATION_2 = 7;

	/**
	 * Used for cold down types.
	 *
	 * @param uiType
	 * @param isIncrease
	 * @param startTime
	 * @param endTime
	 * @param text
	 */
	public ExSendUIEvent(int uiType, int isIncrease, int startTime, int endTime, String text)
	{
		_uiType = uiType;
		_isIncrease = isIncrease;
		_startTime = startTime;
		_endTime = endTime;
		_text = text;
		_npcString = -1;
	}

	/**
	 * Used for cold down types.
	 *
	 * @param uiType
	 * @param isIncrease
	 * @param startTime
	 * @param endTime
	 * @param npcStringId
	 */
	public ExSendUIEvent(int uiType, int isIncrease, int startTime, int endTime, int npcStringId)
	{
		_uiType = uiType;
		_isIncrease = isIncrease;
		_startTime = startTime;
		_endTime = endTime;
		_text = "";
		_npcString = npcStringId;
	}

	/**
	 * Used for BAR UIs
	 *
	 * @param uiType
	 * @param currentPoints
	 * @param maxPoints
	 * @param npcStringId
	 */
	public ExSendUIEvent(int uiType, int currentPoints, int maxPoints, int npcStringId)
	{
		_uiType = uiType;
		_isIncrease = -1;
		_startTime = currentPoints;
		_endTime = maxPoints;
		_text = "";
		_npcString = npcStringId;
	}

	@Override
	protected final void writeImpl()
	{
		if (getClient() == null || getClient().getActiveChar() == null)
		{
			return;
		}

		writeD(getClient().getActiveChar().getObjectId());
		writeD(_uiType);
		writeD(0x00); // unknown
		writeD(0x00); // unknown

		switch (_uiType)
		{
			case 2:
				writeS(String.valueOf(_startTime)); // Seconds
				writeS(String.valueOf(_endTime)); // % done
				writeS(String.valueOf(0)); // Should be 0
				writeD(0x00); // % symbol on the bar
				writeD(_npcString); //npcString
				writeD(122520);

				break;

			case 5:
				writeS(String.valueOf(_isIncrease));
				writeS(String.valueOf(_startTime));
				writeS(String.valueOf(_endTime));
				writeD(0x00);
				//	writeD(0x00);
				writeD(_npcString);

				break;

			default:
				writeS(String.valueOf(_isIncrease)); // "0": count negative, "1": count positive
				writeS(String.valueOf(_startTime / 60)); // timer starting minute(s)
				writeS(String.valueOf(_startTime % 60)); // timer starting second(s)
				writeD(0x00); // unknown
				writeD(_npcString); // TODO: npcString
				writeS(_text); // text above timer
				writeS(String.valueOf(
						_endTime / 60)); // timer length minute(s) (timer will disappear 10 seconds before it ends)
				writeS(String.valueOf(
						_endTime % 60)); // timer length second(s) (timer will disappear 10 seconds before it ends)

				break;
		}
	}
}
