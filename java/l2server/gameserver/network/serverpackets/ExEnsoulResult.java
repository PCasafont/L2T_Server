package l2server.gameserver.network.serverpackets;

/**
 * @author Pere
 */
public class ExEnsoulResult extends L2GameServerPacket
{
	public boolean success;

	public ExEnsoulResult(boolean success)
	{
		this.success = success;
	}

	@Override
	public void writeImpl()
	{
		writeD(this.success ? 1 : 0);
	}
}
