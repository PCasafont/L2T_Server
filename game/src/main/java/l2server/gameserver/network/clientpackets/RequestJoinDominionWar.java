package l2server.gameserver.network.clientpackets;

/**
 * @author MegaParzor!
 */
public class RequestJoinDominionWar extends L2GameClientPacket {
	@Override
	public void readImpl() {
	}
	
	@Override
	public void runImpl() {
		// TODO
		log.info(getType() + " was received from " + getClient() + ".");
	}
}

