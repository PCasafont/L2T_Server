package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class AllianceInfo extends L2GameServerPacket
{
	private String leadclan;
	private int online;
	private int clanscount;
	private String clanLeader;
	private int allymembers;
	private String allyName;

	public AllianceInfo(String leadclan, int online, int clanscount, String clanLeader, int allymembers, String allyName)
	{
		this.leadclan = leadclan;
		this.online = online;
		this.clanscount = clanscount;
		this.clanLeader = clanLeader;
		this.allymembers = allymembers;
		this.allyName = allyName;
	}

	@Override
	public void writeImpl()
	{
		writeS(this.leadclan);
		writeD(this.online);
		writeD(this.clanscount);
		writeS(this.clanLeader);
		writeD(this.allymembers);
		writeS(this.allyName);
	}
}
