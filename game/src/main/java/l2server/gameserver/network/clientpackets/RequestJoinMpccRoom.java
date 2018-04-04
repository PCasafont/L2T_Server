package l2server.gameserver.network.clientpackets;

/**
 * @author MegaParzor!
 */
public class RequestJoinMpccRoom extends L2GameClientPacket {
	@SuppressWarnings("unused")
	private int unk;
	@SuppressWarnings("unused")
	private int id;

	@Override
	public void readImpl() {
		unk = readD();
		id = readD();
	}

	@Override
	public void runImpl() {
		// TODO
		log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
