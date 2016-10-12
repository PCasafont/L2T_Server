package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExMpccRoomInfo extends L2GameServerPacket
{
	private int _maxLvl;
	private int _maxParties;
	private String _leaderName;
	private int _id;
	private int _minLvl;
	private int _curParties;

	public ExMpccRoomInfo(int maxLvl, int maxParties, String leaderName, int id, int minLvl, int curParties)
	{
		_maxLvl = maxLvl;
		_maxParties = maxParties;
		_leaderName = leaderName;
		_id = id;
		_minLvl = minLvl;
		_curParties = curParties;
	}

	@Override
	public void writeImpl()
	{
		writeD(_maxLvl);
		writeD(_maxParties);
		writeS(_leaderName);
		writeH(0x00); // unk2
		writeD(0x00); // unk1
		writeD(_id);
		writeD(_minLvl);
		writeD(_curParties);
	}
}
