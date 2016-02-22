
package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExLightingCandleEvent extends L2GameServerPacket
{
	private int _state;
	
	public ExLightingCandleEvent(int state)
	{
		_state = state;
	}
	
	@Override
	public void writeImpl()
	{
		writeH(_state);
	}
}
