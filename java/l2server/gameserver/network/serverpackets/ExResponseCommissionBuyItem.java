package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExResponseCommissionBuyItem extends L2GameServerPacket
{
	private int _unk;

	public ExResponseCommissionBuyItem(int unk)
	{
		_unk = unk;
	}

	@Override
	public void writeImpl()
	{
		writeD(_unk);
	}
}
