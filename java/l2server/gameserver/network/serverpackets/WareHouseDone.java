
package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class WareHouseDone extends L2GameServerPacket
{
	private int _unk;
	
	public WareHouseDone(int unk)
	{
		_unk = unk;
	}
	
	@Override
	public void writeImpl()
	{
		writeD(_unk);
	}
}
