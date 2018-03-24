package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExEventMatchUserInfo extends L2GameServerPacket
{
	@Override
	public void writeImpl()
	{
		writeD(0x00); // unk8
		writeD(0x00); // unk9
		writeD(0x00); // unk6
		writeD(0x00); // unk7
		writeD(0x00); // unk4
		writeD(0x00); // unk5
		writeD(0x00); // unk10
		writeS(""); // unk2
		writeD(0x00); // unk3
		writeD(0x00); // unk1
	}
}
