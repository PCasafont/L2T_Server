package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExMpccRoomMembers extends L2GameServerPacket
{
	private int _unk;
	private int _listsize;

	public ExMpccRoomMembers(int unk, int listsize)
	{
		_unk = unk;
		_listsize = listsize;
	}

	@Override
	public void writeImpl()
	{
		writeD(_unk);
		writeD(_listsize);
	}
}
