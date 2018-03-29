package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExDominionWarStart extends L2GameServerPacket
{
	private int size;

	public ExDominionWarStart(int size)
	{
		this.size = size;
	}

	@Override
	public void writeImpl()
	{
		writeD(size);
	}
}

