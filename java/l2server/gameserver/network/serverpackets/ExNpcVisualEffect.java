package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExNpcVisualEffect extends L2GameServerPacket
{
    private int _objId;

    public ExNpcVisualEffect(int objId)
    {
        _objId = objId;
    }

    @Override
    public void writeImpl()
    {
        writeD(0x00); // unk2
        writeD(_objId);
        writeD(0x00); // unk1
    }
}
