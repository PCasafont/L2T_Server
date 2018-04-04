package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExEventMatchSpelledInfo extends L2GameServerPacket {
	private int numberofeffects;
	private int type;
	private int objectID;
	
	public ExEventMatchSpelledInfo(int numberofeffects, int type, int objectID) {
		this.numberofeffects = numberofeffects;
		this.type = type;
		this.objectID = objectID;
	}
	
	@Override
	public void writeImpl() {
		writeD(numberofeffects);
		writeD(type);
		writeD(objectID);
	}
}
