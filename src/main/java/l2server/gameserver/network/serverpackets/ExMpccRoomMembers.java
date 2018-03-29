package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExMpccRoomMembers extends L2GameServerPacket
{
	private int unk;
	private int listsize;

	public ExMpccRoomMembers(int unk, int listsize)
	{
		this.unk = unk;
		this.listsize = listsize;
	}

	@Override
	public void writeImpl()
	{
		writeD(unk);
		writeD(listsize);
	}
}
