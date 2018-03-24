package l2server.gameserver.network.clientpackets;

import l2server.log.Log;

/**
 * @author MegaParzor!
 */
public class NetPing extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _pingID;

	@Override
	public void readImpl()
	{
		_pingID = readD();
		readD(); // unk2
		readD(); // unk1
	}

	@Override
	public void runImpl()
	{
		// TODO
		Log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
