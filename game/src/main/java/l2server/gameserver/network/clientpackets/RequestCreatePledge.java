package l2server.gameserver.network.clientpackets;


/**
 * @author MegaParzor!
 */
public class RequestCreatePledge extends L2GameClientPacket {
	@Override
	public void readImpl() {
	}

	@Override
	public void runImpl() {
		// TODO
		log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
