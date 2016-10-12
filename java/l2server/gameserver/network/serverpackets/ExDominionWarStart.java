package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExDominionWarStart extends L2GameServerPacket
{
	private int _size;

	public ExDominionWarStart(int size)
	{
		_size = size;
	}

	@Override
	public void writeImpl()
	{
		writeD(_size);
	}
}

