package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExResponseCommissionBuyItem extends L2GameServerPacket
{
	private int unk;

	public ExResponseCommissionBuyItem(int unk)
	{
		this.unk = unk;
	}

	@Override
	public void writeImpl()
	{
		writeD(unk);
	}
}
