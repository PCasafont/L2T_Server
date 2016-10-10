package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExBRProductInfo extends L2GameServerPacket
{
    private int _productId;
    private int _price;
    private int _count;

    public ExBRProductInfo(int productId, int price, int count)
    {
        _productId = productId;
        _price = price;
        _count = count;
    }

    @Override
    public void writeImpl()
    {
        writeD(_productId);
        writeD(_price);
        writeD(_count);
    }
}
