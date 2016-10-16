package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExBRProductInfo extends L2GameServerPacket
{
	private int productId;
	private int price;
	private int count;

	public ExBRProductInfo(int productId, int price, int count)
	{
		this.productId = productId;
		this.price = price;
		this.count = count;
	}

	@Override
	public void writeImpl()
	{
		writeD(this.productId);
		writeD(this.price);
		writeD(this.count);
	}
}
