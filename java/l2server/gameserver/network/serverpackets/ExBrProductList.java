
package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExBrProductList extends L2GameServerPacket
{
	private long _adena;
	private long _herocoins;
	private byte _type0list1history2fav;
	private int _itemssize;
	
	public ExBrProductList(long adena, long herocoins, byte type0list1history2fav, int itemssize)
	{
		_adena = adena;
		_herocoins = herocoins;
		_type0list1history2fav = type0list1history2fav;
		_itemssize = itemssize;
	}
	
	@Override
	public void writeImpl()
	{
		writeQ(_adena);
		writeQ(_herocoins);
		writeC(_type0list1history2fav);
		writeD(_itemssize);
	}
}
