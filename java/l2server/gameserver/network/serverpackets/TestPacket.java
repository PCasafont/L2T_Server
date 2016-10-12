package l2server.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Pere
 */
public class TestPacket extends L2GameServerPacket
{
	private ByteBuffer _buffer = ByteBuffer.allocate(10000).order(ByteOrder.LITTLE_ENDIAN);

	public void writeChar(int x)
	{
		_buffer.put((byte) x);
	}

	public void writeShort(int x)
	{
		_buffer.putShort((short) x);
	}

	public void writeInt(int x)
	{
		_buffer.putInt(x);
	}

	@Override
	protected final void writeImpl()
	{
		_buffer.flip();
		while (_buffer.position() < _buffer.limit())
		{
			writeC(_buffer.get());
		}
	}
}
