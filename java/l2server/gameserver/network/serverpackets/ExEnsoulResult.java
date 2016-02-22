
package l2server.gameserver.network.serverpackets;

/**
 * @author Pere
 */
public class ExEnsoulResult extends L2GameServerPacket
{
	public boolean _success;
	
	public ExEnsoulResult(boolean success)
	{
		_success = success;
	}
	
	@Override
	public void writeImpl()
	{
		writeD(_success ? 1 : 0);
	}
}
