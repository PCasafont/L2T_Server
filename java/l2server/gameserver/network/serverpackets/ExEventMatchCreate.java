package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExEventMatchCreate extends L2GameServerPacket
{
	private int unk;

	public ExEventMatchCreate(int unk)
	{
		this.unk = unk;
	}

	@Override
	public void writeImpl()
	{
		writeD(this.unk);
	}
}
