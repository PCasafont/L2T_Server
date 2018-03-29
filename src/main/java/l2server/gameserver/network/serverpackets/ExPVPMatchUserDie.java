package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExPVPMatchUserDie extends L2GameServerPacket
{
	private int team1kills;
	private int team2kills;

	public ExPVPMatchUserDie(int team1kills, int team2kills)
	{
		this.team1kills = team1kills;
		this.team2kills = team2kills;
	}

	@Override
	public void writeImpl()
	{
		writeD(team1kills);
		writeD(team2kills);
	}
}
