package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExNpcVisualEffect extends L2GameServerPacket
{
	private int objId;

	public ExNpcVisualEffect(int objId)
	{
		this.objId = objId;
	}

	@Override
	public void writeImpl()
	{
		writeD(0x00); // unk2
		writeD(this.objId);
		writeD(0x00); // unk1
	}
}
