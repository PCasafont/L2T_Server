package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExEventMatchFirecracker extends L2GameServerPacket
{
	private int _fireCrackerId;

	public ExEventMatchFirecracker(int fireCrackerId)
	{
		_fireCrackerId = fireCrackerId;
	}

	@Override
	public void writeImpl()
	{
		writeD(_fireCrackerId);
	}
}
