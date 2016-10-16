package l2server.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Pere
 */
public class TestPacket extends L2GameServerPacket
{
	private ByteBuffer buffer = ByteBuffer.allocate(10000).order(ByteOrder.LITTLE_ENDIAN);

	public void writeChar(int x)
	{
		this.buffer.put((byte) x);
	}

	public void writeShort(int x)
	{
		this.buffer.putShort((short) x);
	}

	public void writeInt(int x)
	{
		this.buffer.putInt(x);
	}

	@Override
	protected final void writeImpl()
	{
		this.buffer.flip();
		while (this.buffer.position() < this.buffer.limit())
		{
			writeC(this.buffer.get());
		}
	}
}
