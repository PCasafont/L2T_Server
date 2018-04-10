package l2server.gameserver.network.clientpackets;

/**
 * @author MegaParzor!
 */
public class NetPing extends L2GameClientPacket {
	@SuppressWarnings("unused")
	private int pingID;

	@Override
	public void readImpl() {
		pingID = readD();
		readD(); // unk2
		readD(); // unk1
	}

	@Override
	public void runImpl() {
		// TODO
		log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
