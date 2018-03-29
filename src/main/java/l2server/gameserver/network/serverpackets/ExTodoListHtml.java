package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExTodoListHtml extends L2GameServerPacket
{
	private int unk;

	public ExTodoListHtml(int unk)
	{
		this.unk = unk;
	}

	@Override
	public void writeImpl()
	{
		writeH(unk);
	}
}
