package l2server.gameserver.network.clientpackets;


/**
 * @author MegaParzor!
 */
public class ExOrcMove extends L2GameClientPacket {
	@Override
	public void readImpl() {
		readB(new byte[1]); // unk (TODO: check size)
	}

	@Override
	public void runImpl() {
		// TODO
		log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
