package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExPVPMatchCCMyRecord extends L2GameServerPacket {
	private int unk;
	
	public ExPVPMatchCCMyRecord(int unk) {
		this.unk = unk;
	}
	
	@Override
	public void writeImpl() {
		writeD(unk);
	}
}
