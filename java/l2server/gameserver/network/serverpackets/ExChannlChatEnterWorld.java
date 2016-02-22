
package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExChannlChatEnterWorld extends L2GameServerPacket
{
	@Override
	public void writeImpl()
	{
		writeD(0x00); // unk4
		writeH(0x00); // unk5
		writeD(0x00); // unk2
		writeD(0x00); // unk3
		writeD(0x00); // unk1
	}
}
