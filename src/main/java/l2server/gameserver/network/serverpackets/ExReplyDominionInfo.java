package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExReplyDominionInfo extends L2GameServerPacket {
	private int dominonCount;
	
	public ExReplyDominionInfo(int dominonCount) {
		this.dominonCount = dominonCount;
	}
	
	@Override
	public void writeImpl() {
		writeD(dominonCount);
	}
}

