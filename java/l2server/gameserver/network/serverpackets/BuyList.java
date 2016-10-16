package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class BuyList extends L2GameServerPacket
{
	private int listID;
	private long money;
	private int listsize;

	public BuyList(int listID, long money, int listsize)
	{
		this.listID = listID;
		this.money = money;
		this.listsize = listsize;
	}

	@Override
	public void writeImpl()
	{
		writeD(this.listID);
		writeQ(this.money);
		writeH(this.listsize);
	}
}
