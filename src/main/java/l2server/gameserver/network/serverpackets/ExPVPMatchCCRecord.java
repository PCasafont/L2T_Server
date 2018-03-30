package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExPVPMatchCCRecord extends L2GameServerPacket {
	private int unk;
	
	public ExPVPMatchCCRecord(int unk) {
		this.unk = unk;
	}
	
	@Override
	public void writeImpl() {
		writeD(unk);
	}
}
