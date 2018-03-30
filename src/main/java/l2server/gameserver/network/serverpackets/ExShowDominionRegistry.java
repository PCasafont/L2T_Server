package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExShowDominionRegistry extends L2GameServerPacket {
	private int currentTime;
	private String territoryOwner;
	private String ownerAlliance;
	private int territoryCount;
	private int territoryId;
	private int mercRequest;
	private int clanRequest;
	private String ownerClan;
	private int warTime;
	
	public ExShowDominionRegistry(int currentTime,
	                              String territoryOwner,
	                              String ownerAlliance,
	                              int territoryCount,
	                              int territoryId,
	                              int mercRequest,
	                              int clanRequest,
	                              String ownerClan,
	                              int warTime) {
		this.currentTime = currentTime;
		this.territoryOwner = territoryOwner;
		this.ownerAlliance = ownerAlliance;
		this.territoryCount = territoryCount;
		this.territoryId = territoryId;
		this.mercRequest = mercRequest;
		this.clanRequest = clanRequest;
		this.ownerClan = ownerClan;
		this.warTime = warTime;
	}
	
	@Override
	public void writeImpl() {
		writeD(currentTime);
		writeS(territoryOwner);
		writeS(ownerAlliance);
		writeD(0x00); // unk2
		writeD(0x00); // unk3
		writeD(territoryCount);
		writeD(territoryId);
		writeD(mercRequest);
		writeD(0x00); // unk1
		writeD(clanRequest);
		writeS(ownerClan);
		writeD(warTime);
	}
}

