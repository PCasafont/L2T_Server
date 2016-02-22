
package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExMpccPartymasterList extends L2GameServerPacket
{
	private int _size;
	
	public ExMpccPartymasterList(int size)
	{
		_size = size;
	}
	
	@Override
	public void writeImpl()
	{
		writeD(_size);
	}
}
