package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExTodoListInzone extends L2GameServerPacket
{
	private int unk;

	public ExTodoListInzone(int unk)
	{
		this.unk = unk;
	}

	@Override
	public void writeImpl()
	{
		writeH(this.unk);
	}
}
