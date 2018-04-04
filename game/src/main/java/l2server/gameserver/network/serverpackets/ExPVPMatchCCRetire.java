package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExPVPMatchCCRetire extends L2GameServerPacket {
	private int unk;
	
	public ExPVPMatchCCRetire(int unk) {
		this.unk = unk;
	}
	
	@Override
	public void writeImpl() {
		writeD(unk);
	}
}
