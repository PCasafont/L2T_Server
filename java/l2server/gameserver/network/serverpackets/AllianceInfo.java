
package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class AllianceInfo extends L2GameServerPacket
{
	private String _leadclan;
	private int _online;
	private int _clanscount;
	private String _clanLeader;
	private int _allymembers;
	private String _allyName;
	
	public AllianceInfo(String leadclan, int online, int clanscount, String clanLeader, int allymembers, String allyName)
	{
		_leadclan = leadclan;
		_online = online;
		_clanscount = clanscount;
		_clanLeader = clanLeader;
		_allymembers = allymembers;
		_allyName = allyName;
	}
	
	@Override
	public void writeImpl()
	{
		writeS(_leadclan);
		writeD(_online);
		writeD(_clanscount);
		writeS(_clanLeader);
		writeD(_allymembers);
		writeS(_allyName);
	}
}
