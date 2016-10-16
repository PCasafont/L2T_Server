package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExLightingCandleEvent extends L2GameServerPacket
{
	private int state;

	public ExLightingCandleEvent(int state)
	{
		this.state = state;
	}

	@Override
	public void writeImpl()
	{
		writeH(this.state);
	}
}
