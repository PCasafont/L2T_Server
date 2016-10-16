package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExMpccPartymasterList extends L2GameServerPacket
{
	private int size;

	public ExMpccPartymasterList(int size)
	{
		this.size = size;
	}

	@Override
	public void writeImpl()
	{
		writeD(size);
	}
}
