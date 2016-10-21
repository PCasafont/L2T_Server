package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExBRGamePoint extends L2GameServerPacket
{
	private int _userId;

	public ExBRGamePoint(int userId)
	{
		_userId = userId;
	}

	@Override
	public void writeImpl()
	{
		writeD(0x00); // unk2
		writeD(0x00); // unk3
		writeD(0x00); // unk1
		writeD(_userId);
	}
}
