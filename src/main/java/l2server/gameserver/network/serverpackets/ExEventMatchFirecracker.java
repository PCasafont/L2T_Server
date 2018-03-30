package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExEventMatchFirecracker extends L2GameServerPacket {
	private int fireCrackerId;
	
	public ExEventMatchFirecracker(int fireCrackerId) {
		this.fireCrackerId = fireCrackerId;
	}
	
	@Override
	public void writeImpl() {
		writeD(fireCrackerId);
	}
}
