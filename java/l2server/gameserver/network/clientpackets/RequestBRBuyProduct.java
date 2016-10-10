package l2server.gameserver.network.clientpackets;

import l2server.log.Log;

/**
 * @author MegaParzor!
 */
public class RequestBRBuyProduct extends L2GameClientPacket
{
    @SuppressWarnings("unused")
    private int _productId;
    @SuppressWarnings("unused")
    private int _count;

    @Override
    public void readImpl()
    {
        _productId = readD();
        _count = readD();
    }

    @Override
    public void runImpl()
    {
        // TODO
        Log.info(getType() + " packet was received from " + getClient() + ".");
    }
}
