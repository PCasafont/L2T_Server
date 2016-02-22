
package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExTodoListHtml extends L2GameServerPacket
{
	private int _unk;
	
	public ExTodoListHtml(int unk)
	{
		_unk = unk;
	}
	
	@Override
	public void writeImpl()
	{
		writeH(_unk);
	}
}
