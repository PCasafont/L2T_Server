
package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExPVPMatchUserDie extends L2GameServerPacket
{
	private int _team1kills;
	private int _team2kills;
	
	public ExPVPMatchUserDie(int team1kills, int team2kills)
	{
		_team1kills = team1kills;
		_team2kills = team2kills;
	}
	
	@Override
	public void writeImpl()
	{
		writeD(_team1kills);
		writeD(_team2kills);
	}
}
