package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExEventMatchSpelledInfo extends L2GameServerPacket
{
	private int _numberofeffects;
	private int _type;
	private int _objectID;

	public ExEventMatchSpelledInfo(int numberofeffects, int type, int objectID)
	{
		_numberofeffects = numberofeffects;
		_type = type;
		_objectID = objectID;
	}

	@Override
	public void writeImpl()
	{
		writeD(_numberofeffects);
		writeD(_type);
		writeD(_objectID);
	}
}
