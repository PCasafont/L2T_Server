
package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExTutorialList extends L2GameServerPacket
{
	@Override
	public void writeImpl()
	{
		writeB(new byte[1]); // unk (TODO: check size)
	}
}
