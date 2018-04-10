package l2server.gameserver.network.clientpackets;

/**
 * @author MegaParzor!
 */
public class RequestBRBuyProduct extends L2GameClientPacket {
	@SuppressWarnings("unused")
	private int productId;
	@SuppressWarnings("unused")
	private int count;

	@Override
	public void readImpl() {
		productId = readD();
		count = readD();
	}

	@Override
	public void runImpl() {
		// TODO
		log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
