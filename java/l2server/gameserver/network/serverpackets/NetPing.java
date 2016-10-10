package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class NetPing extends L2GameServerPacket
{
    private int _pingid;

    public NetPing(int pingid)
    {
        _pingid = pingid;
    }

    @Override
    public void writeImpl()
    {
        writeD(_pingid);
    }
}
