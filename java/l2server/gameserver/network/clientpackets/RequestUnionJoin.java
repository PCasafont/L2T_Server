package l2server.gameserver.network.clientpackets;

import l2server.log.Log;

/**
 * @author MegaParzor!
 */
public class RequestUnionJoin extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int unk;

	@Override
	public void readImpl()
	{
		this.unk = readD();
	}

	@Override
	public void runImpl()
	{
		// TODO
		Log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
