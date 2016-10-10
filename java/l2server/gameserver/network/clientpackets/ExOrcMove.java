package l2server.gameserver.network.clientpackets;

import l2server.log.Log;

/**
 * @author MegaParzor!
 */
public class ExOrcMove extends L2GameClientPacket
{
    @Override
    public void readImpl()
    {
        readB(new byte[1]); // unk (TODO: check size)
    }

    @Override
    public void runImpl()
    {
        // TODO
        Log.info(getType() + " packet was received from " + getClient() + ".");
    }
}
