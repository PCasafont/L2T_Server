package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExEventMatchTeamInfo extends L2GameServerPacket
{
    @Override
    public void writeImpl()
    {
        writeC(0x00); // unk2
        writeD(0x00); // unk3
        writeD(0x00); // unk1
    }
}
