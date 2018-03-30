package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExPeriodicItemList extends L2GameServerPacket {
	private int unk;
	
	public ExPeriodicItemList(int unk) {
		this.unk = unk;
	}
	
	@Override
	public void writeImpl() {
		writeD(unk);
	}
}
