package l2server.gameserver.network.clientpackets;

import l2server.log.Log;

/**
 * @author MegaParzor!
 */
public class RequestJoinMpccRoom extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int unk;
	@SuppressWarnings("unused")
	private int id;

	@Override
	public void readImpl()
	{
		this.unk = readD();
		this.id = readD();
	}

	@Override
	public void runImpl()
	{
		// TODO
		Log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
