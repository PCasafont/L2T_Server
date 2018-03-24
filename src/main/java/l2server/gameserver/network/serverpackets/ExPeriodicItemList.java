package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExPeriodicItemList extends L2GameServerPacket
{
	private int _unk;

	public ExPeriodicItemList(int unk)
	{
		_unk = unk;
	}

	@Override
	public void writeImpl()
	{
		writeD(_unk);
	}
}
