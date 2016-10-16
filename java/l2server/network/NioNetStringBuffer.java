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
		this.buf = new char[size];
		this.size = size;
		this.len = 0;
	}

	public final void clear()
	{
		this.len = 0;
	}

	public final void append(final char c)
	{
		if (this.len < this.size)
		{
			this.buf[this.len++] = c;
		}
		else
		{
			throw new BufferOverflowException();
		}
	}

	@Override
	public final String toString()
	{
		return new String(this.buf, 0, this.len);
	}
}
