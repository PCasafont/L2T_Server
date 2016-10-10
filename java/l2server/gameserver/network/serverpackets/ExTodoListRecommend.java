package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExTodoListRecommend extends L2GameServerPacket
{
    private int _unk;

    public ExTodoListRecommend(int unk)
    {
        _unk = unk;
    }

    @Override
    public void writeImpl()
    {
        writeH(_unk);
    }
}
