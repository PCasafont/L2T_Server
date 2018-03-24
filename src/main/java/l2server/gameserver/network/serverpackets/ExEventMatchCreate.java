package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExEventMatchCreate extends L2GameServerPacket
{
	private int _unk;

	public ExEventMatchCreate(int unk)
	{
		_unk = unk;
	}

	@Override
	public void writeImpl()
	{
		writeD(_unk);
	}
}
