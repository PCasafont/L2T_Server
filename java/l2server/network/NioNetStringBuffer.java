package l2server.network;

import java.nio.BufferOverflowException;

/**
 * @author Forsaiken
 */
public final class NioNetStringBuffer
{
	private final char[] buf;

	private final int size;

	private int len;

	public NioNetStringBuffer(final int size)
	{
		buf = new char[size];
		this.size = size;
		len = 0;
	}

	public final void clear()
	{
		len = 0;
	}

	public final void append(final char c)
	{
		if (len < size)
		{
			buf[len++] = c;
		}
		else
		{
			throw new BufferOverflowException();
		}
	}

	@Override
	public final String toString()
	{
		return new String(buf, 0, len);
	}
}
