package l2server.gameserver.network.clientpackets;

/**
 * @author MegaParzor!
 */
public class RequestUnionJoin extends L2GameClientPacket {
	@SuppressWarnings("unused")
	private int unk;

	@Override
	public void readImpl() {
		unk = readD();
	}

	@Override
	public void runImpl() {
		// TODO
		log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
