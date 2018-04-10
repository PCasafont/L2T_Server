package l2server.gameserver.network.clientpackets;

/**
 * @author MegaParzor!
 */
public class PrivateBuyListSell extends L2GameClientPacket {
	@SuppressWarnings("unused")
	private int adena;
	@SuppressWarnings("unused")
	private int isPackageSale;
	@SuppressWarnings("unused")
	private int listSize;
	@SuppressWarnings("unused")
	private int objectId;

	@Override
	public void readImpl() {
		adena = readD();
		isPackageSale = readD();
		listSize = readD();
		objectId = readD();
	}

	@Override
	public void runImpl() {
		// TODO
		log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
