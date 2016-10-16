package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExPVPMatchRecord extends L2GameServerPacket
{
	private int kills;
	private int teamsSize;

	public ExPVPMatchRecord(int kills, int teamsSize)
	{
		this.kills = kills;
		this.teamsSize = teamsSize;
	}

	@Override
	public void writeImpl()
	{
		writeD(kills);
		writeD(0x00); // ranking2
		writeD(teamsSize);
		writeD(0x00); // ranking1
		writeD(0x00); // kills3
	}
}
