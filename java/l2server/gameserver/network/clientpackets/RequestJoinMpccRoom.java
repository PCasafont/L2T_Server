
package l2server.gameserver.network.clientpackets;

import l2server.log.Log;

/**
 * @author MegaParzor!
 */
public class RequestJoinMpccRoom extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _unk;
	@SuppressWarnings("unused")
	private int _id;
	
	@Override
	public void readImpl()
	{
		_unk = readD();
		_id = readD();
	}
	
	@Override
	public void runImpl()
	{
		// TODO
		Log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
