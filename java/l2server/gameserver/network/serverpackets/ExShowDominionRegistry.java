package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExShowDominionRegistry extends L2GameServerPacket
{
	private int _currentTime;
	private String _territoryOwner;
	private String _ownerAlliance;
	private int _territoryCount;
	private int _territoryId;
	private int _mercRequest;
	private int _clanRequest;
	private String _ownerClan;
	private int _warTime;
	
	public ExShowDominionRegistry(int currentTime, String territoryOwner, String ownerAlliance, int territoryCount, int territoryId, int mercRequest, int clanRequest, String ownerClan, int warTime)
	{
		_currentTime = currentTime;
		_territoryOwner = territoryOwner;
		_ownerAlliance = ownerAlliance;
		_territoryCount = territoryCount;
		_territoryId = territoryId;
		_mercRequest = mercRequest;
		_clanRequest = clanRequest;
		_ownerClan = ownerClan;
		_warTime = warTime;
	}
	
	@Override
	public void writeImpl()
	{
		writeD(_currentTime);
		writeS(_territoryOwner);
		writeS(_ownerAlliance);
		writeD(0x00); // unk2
		writeD(0x00); // unk3
		writeD(_territoryCount);
		writeD(_territoryId);
		writeD(_mercRequest);
		writeD(0x00); // unk1
		writeD(_clanRequest);
		writeS(_ownerClan);
		writeD(_warTime);
	}
}

