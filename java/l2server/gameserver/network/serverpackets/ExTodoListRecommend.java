package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExTodoListRecommend extends L2GameServerPacket
{
	private int unk;

	public ExTodoListRecommend(int unk)
	{
		this.unk = unk;
	}

	@Override
	public void writeImpl()
	{
		writeH(this.unk);
	}
}
