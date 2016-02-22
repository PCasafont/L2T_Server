
package l2server.gameserver.network.serverpackets;

/**
 * @author MegaParzor!
 */
public class ExMoveToTargetInAirShip extends L2GameServerPacket
{
	private int _objID;
	private int _x;
	private int _y;
	private int _z;
	private int _vehicleid;
	
	public ExMoveToTargetInAirShip(int objID, int x, int y, int z, int vehicleid)
	{
		_objID = objID;
		_x = x;
		_y = y;
		_z = z;
		_vehicleid = vehicleid;
	}
	
	@Override
	public void writeImpl()
	{
		writeD(0x00); // heading1
		writeD(_objID);
		writeD(_x);
		writeD(0x00); // heading2
		writeD(_y);
		writeD(_z);
		writeD(_vehicleid);
	}
}
