package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class WareHouseDone extends L2GameServerPacket
{
	private int unk;

	public WareHouseDone(int unk)
	{
		this.unk = unk;
	}

	@Override
	public void writeImpl()
	{
		writeD(this.unk);
	}
}
