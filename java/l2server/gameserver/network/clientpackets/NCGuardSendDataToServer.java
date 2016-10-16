package l2server.gameserver.network.clientpackets;

/**
 * @author MegaParzor!
 */
public class NCGuardSendDataToServer extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int size;

	@Override
	public void readImpl()
	{
		size = readD();
		readB(new byte[1]); // data (TODO: check size)
	}

	@Override
	public void runImpl()
	{
		// TODO
		//Log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
