package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExBR_NewIConCashBtnWnd extends L2GameServerPacket {
	private int unk;
	
	public ExBR_NewIConCashBtnWnd(int unk) {
		this.unk = unk;
	}
	
	@Override
	public void writeImpl() {
		writeH(unk);
	}
}
