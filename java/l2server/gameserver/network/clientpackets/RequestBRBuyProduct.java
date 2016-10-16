package l2server.gameserver.network.clientpackets;

import l2server.log.Log;

/**
 * @author MegaParzor!
 */
public class RequestBRBuyProduct extends L2GameClientPacket
{
	@SuppressWarnings("unused") private int productId;
	@SuppressWarnings("unused") private int count;

	@Override
	public void readImpl()
	{
		productId = readD();
		count = readD();
	}

	@Override
	public void runImpl()
	{
		// TODO
		Log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
