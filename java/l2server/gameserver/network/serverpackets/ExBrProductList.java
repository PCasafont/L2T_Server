package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExBrProductList extends L2GameServerPacket
{
	private long adena;
	private long herocoins;
	private byte type0list1history2fav;
	private int itemssize;

	public ExBrProductList(long adena, long herocoins, byte type0list1history2fav, int itemssize)
	{
		this.adena = adena;
		this.herocoins = herocoins;
		this.type0list1history2fav = type0list1history2fav;
		this.itemssize = itemssize;
	}

	@Override
	public void writeImpl()
	{
		writeQ(this.adena);
		writeQ(this.herocoins);
		writeC(this.type0list1history2fav);
		writeD(this.itemssize);
	}
}
