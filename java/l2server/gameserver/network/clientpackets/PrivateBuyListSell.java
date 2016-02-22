
package l2server.gameserver.network.clientpackets;

import l2server.log.Log;

/**
 * @author MegaParzor!
 */
public class PrivateBuyListSell extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _adena;
	@SuppressWarnings("unused")
	private int _isPackageSale;
	@SuppressWarnings("unused")
	private int _listSize;
	@SuppressWarnings("unused")
	private int _objectId;
	
	@Override
	public void readImpl()
	{
		_adena = readD();
		_isPackageSale = readD();
		_listSize = readD();
		_objectId = readD();
	}
	
	@Override
	public void runImpl()
	{
		// TODO
		Log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
