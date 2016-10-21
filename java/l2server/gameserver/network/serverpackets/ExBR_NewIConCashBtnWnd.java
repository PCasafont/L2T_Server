package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExBR_NewIConCashBtnWnd extends L2GameServerPacket
{
	private int _unk;

	public ExBR_NewIConCashBtnWnd(int unk)
	{
		_unk = unk;
	}

	@Override
	public void writeImpl()
	{
		writeH(_unk);
	}
}
