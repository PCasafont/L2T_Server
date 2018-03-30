package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExEventMatchObserver extends L2GameServerPacket {
	@Override
	public void writeImpl() {
		writeD(0x00); // unk8
		writeD(0x00); // unk6
		writeD(0x00); // unk7
		writeS(""); // unk4
		writeS(""); // unk5
		writeC(0x00); // unk2
		writeC(0x00); // unk3
		writeD(0x00); // unk1
	}
}
