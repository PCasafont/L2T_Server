package l2server.gameserver.network.serverpackets;

import l2server.log.Log;

/**
 * @author Pere
 */
public class TestPacket extends L2GameServerPacket
{
	//private int _type;
	private int[] _args;
	private int _argsLenght;
	
	public TestPacket(int type, int[] args, int argsLenght)
	{
		//_type = type;
		_args = args;
		_argsLenght = argsLenght;
		Log.info("TestPacket:");
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xce);
		Log.info("WriteC(0xce)");
		//writeH(_type);
		//Log.info("WriteH(0x" + Integer.toHexString(_type) + ")");
		for (int i = 0; i < _argsLenght; i++)
		{
			writeD(_args[i]);
			Log.info("WriteD(0x" + Integer.toHexString(_args[i]) + ")");
		}
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		String type = "WriteC(0xce)\n"/* +
				"WriteH(" + _type + ")"*/;
		for (int i = 0; i < _argsLenght; i++)
		{
			type += "WriteD(" + _args[i] + ")\n";
		}
		return type;
	}
}
