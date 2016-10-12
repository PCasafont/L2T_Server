package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExTodoListInzone extends L2GameServerPacket
{
	private int _unk;

	public ExTodoListInzone(int unk)
	{
		_unk = unk;
	}

	@Override
	public void writeImpl()
	{
		writeH(_unk);
	}
}
