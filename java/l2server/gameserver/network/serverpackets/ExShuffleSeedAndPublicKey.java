package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExShuffleSeedAndPublicKey extends L2GameServerPacket
{
	private int _publicKeySize;

	public ExShuffleSeedAndPublicKey(int publicKeySize)
	{
		_publicKeySize = publicKeySize;
	}

	@Override
	public void writeImpl()
	{
		writeB(new byte[1]); // seed (TODO: check size)
		writeH(_publicKeySize);
	}
}
