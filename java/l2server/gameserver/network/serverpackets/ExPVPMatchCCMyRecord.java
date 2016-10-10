package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExPVPMatchCCMyRecord extends L2GameServerPacket
{
    private int _unk;

    public ExPVPMatchCCMyRecord(int unk)
    {
        _unk = unk;
    }

    @Override
    public void writeImpl()
    {
        writeD(_unk);
    }
}
