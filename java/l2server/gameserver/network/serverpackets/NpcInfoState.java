package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class NpcInfoState extends L2GameServerPacket
{
	private byte state;
	private int objectId;

	public NpcInfoState(byte state, int objectId)
	{
		this.state = state;
		this.objectId = objectId;
	}

	@Override
	public void writeImpl()
	{
		writeC(state);
		writeD(objectId);
	}
}
