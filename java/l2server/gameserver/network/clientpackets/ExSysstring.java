
package l2server.gameserver.network.clientpackets;

import l2server.log.Log;

/**
 * @author MegaParzor!
 */
public class ExSysstring extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{
		readS(); // unk2
		readD(); // unk1
	}
	
	@Override
	public void runImpl()
	{
		// TODO
		Log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
