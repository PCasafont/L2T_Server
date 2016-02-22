
package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class NpcInfoState extends L2GameServerPacket
{
	private byte _state;
	private int _objectId;
	
	public NpcInfoState(byte state, int objectId)
	{
		_state = state;
		_objectId = objectId;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(_state);
		writeD(_objectId);
	}
}
