package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class NetPing extends L2GameServerPacket {
	private int pingid;

	public NetPing(int pingid) {
		this.pingid = pingid;
	}

	@Override
	public void writeImpl() {
		writeD(pingid);
	}
}
