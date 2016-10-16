package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExMpccRoomInfo extends L2GameServerPacket
{
	private int maxLvl;
	private int maxParties;
	private String leaderName;
	private int id;
	private int minLvl;
	private int curParties;

	public ExMpccRoomInfo(int maxLvl, int maxParties, String leaderName, int id, int minLvl, int curParties)
	{
		this.maxLvl = maxLvl;
		this.maxParties = maxParties;
		this.leaderName = leaderName;
		this.id = id;
		this.minLvl = minLvl;
		this.curParties = curParties;
	}

	@Override
	public void writeImpl()
	{
		writeD(this.maxLvl);
		writeD(this.maxParties);
		writeS(this.leaderName);
		writeH(0x00); // unk2
		writeD(0x00); // unk1
		writeD(this.id);
		writeD(this.minLvl);
		writeD(this.curParties);
	}
}
