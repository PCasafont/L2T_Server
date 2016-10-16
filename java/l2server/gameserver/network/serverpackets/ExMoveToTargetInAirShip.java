package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExMoveToTargetInAirShip extends L2GameServerPacket
{
	private int objID;
	private int x;
	private int y;
	private int z;
	private int vehicleid;

	public ExMoveToTargetInAirShip(int objID, int x, int y, int z, int vehicleid)
	{
		this.objID = objID;
		this.x = x;
		this.y = y;
		this.z = z;
		this.vehicleid = vehicleid;
	}

	@Override
	public void writeImpl()
	{
		writeD(0x00); // heading1
		writeD(objID);
		writeD(x);
		writeD(0x00); // heading2
		writeD(y);
		writeD(z);
		writeD(vehicleid);
	}
}
