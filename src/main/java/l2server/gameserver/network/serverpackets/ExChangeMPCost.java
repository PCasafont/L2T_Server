package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExChangeMPCost extends L2GameServerPacket
{
	@Override
	public void writeImpl()
	{
		writeF(0.0f); // unk2
		writeD(0x00); // unk1
	}
}
