package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class BuyList extends L2GameServerPacket
{
	private int _listID;
	private long _money;
	private int _listsize;

	public BuyList(int listID, long money, int listsize)
	{
		_listID = listID;
		_money = money;
		_listsize = listsize;
	}

	@Override
	public void writeImpl()
	{
		writeD(_listID);
		writeQ(_money);
		writeH(_listsize);
	}
}
