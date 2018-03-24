package l2server.gameserver.network.clientpackets;

import l2server.log.Log;

/**
 * @author MegaParzor!
 */
public class RequestBRProductInfo extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _productId;

	@Override
	public void readImpl()
	{
		_productId = readD();
	}

	@Override
	public void runImpl()
	{
		// TODO
		Log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
