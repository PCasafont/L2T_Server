package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExPVPMatchRecord extends L2GameServerPacket
{
    private int _kills;
    private int _teamsSize;

    public ExPVPMatchRecord(int kills, int teamsSize)
    {
        _kills = kills;
        _teamsSize = teamsSize;
    }

    @Override
    public void writeImpl()
    {
        writeD(_kills);
        writeD(0x00); // ranking2
        writeD(_teamsSize);
        writeD(0x00); // ranking1
        writeD(0x00); // kills3
    }
}
