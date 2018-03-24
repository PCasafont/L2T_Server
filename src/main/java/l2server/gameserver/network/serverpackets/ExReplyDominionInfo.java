package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExReplyDominionInfo extends L2GameServerPacket
{
	private int _dominonCount;

	public ExReplyDominionInfo(int dominonCount)
	{
		_dominonCount = dominonCount;
	}

	@Override
	public void writeImpl()
	{
		writeD(_dominonCount);
	}
}

