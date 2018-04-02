package l2server.gameserver.network.clientpackets;


/**
 * @author MegaParzor!
 */
public class RequestBRProductInfo extends L2GameClientPacket {
	@SuppressWarnings("unused")
	private int productId;
	
	@Override
	public void readImpl() {
		productId = readD();
	}
	
	@Override
	public void runImpl() {
		// TODO
		log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
