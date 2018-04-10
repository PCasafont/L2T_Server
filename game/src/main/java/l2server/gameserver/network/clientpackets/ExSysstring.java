package l2server.gameserver.network.clientpackets;

/**
 * @author MegaParzor!
 */
public class ExSysstring extends L2GameClientPacket {
	@Override
	public void readImpl() {
		readS(); // unk2
		readD(); // unk1
	}

	@Override
	public void runImpl() {
		// TODO
		log.info(getType() + " packet was received from " + getClient() + ".");
	}
}
