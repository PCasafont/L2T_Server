/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */

package l2server.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;

/**
 * @param <T>
 * @author KenM
 */
public class MMOConnection<T extends MMOClient<?>>
{
	private final Core<T> selectorThread;

	private final Socket socket;

	private final InetAddress address;

	private final ReadableByteChannel readableByteChannel;

	private final WritableByteChannel writableByteChannel;

	private final int port;

	private final NioNetStackList<SendablePacket<T>> sendQueue;

	private final SelectionKey selectionKey;

	// private SendablePacket<T> closePacket;

	private ByteBuffer readBuffer;

	private ByteBuffer primaryWriteBuffer;

	private ByteBuffer secondaryWriteBuffer;

	private volatile boolean pendingClose;

	private T client;

	public MMOConnection(final Core<T> selectorThread, final Socket socket, final SelectionKey key, boolean tcpNoDelay)
	{
		this.selectorThread = selectorThread;
		this.socket = socket;
		this.address = socket.getInetAddress();
		this.readableByteChannel = socket.getChannel();
		this.writableByteChannel = socket.getChannel();
		this.port = socket.getPort();
		this.selectionKey = key;

		this.sendQueue = new NioNetStackList<>();

		try
		{
			this.socket.setTcpNoDelay(tcpNoDelay);
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
	}

	final void setClient(final T client)
	{
		this.client = client;
	}

	public final T getClient()
	{
		return this.client;
	}

	public final void sendPacket(final SendablePacket<T> sp)
	{
		sp.client = this.client;

		if (this.pendingClose)
		{
			return;
		}

		synchronized (getSendQueue())
		{
			this.sendQueue.addLast(sp);
		}

		if (!this.sendQueue.isEmpty())
		{
			try
			{
				this.selectionKey.interestOps(this.selectionKey.interestOps() | SelectionKey.OP_WRITE);
			}
			catch (CancelledKeyException e)
			{
				// ignore
			}
		}
	}

	final SelectionKey getSelectionKey()
	{
		return this.selectionKey;
	}

	public final InetAddress getInetAddress()
	{
		return this.address;
	}

	public final int getPort()
	{
		return this.port;
	}

	final void close() throws IOException
	{
		this.socket.close();
	}

	final int read(final ByteBuffer buf) throws IOException
	{
		return this.readableByteChannel.read(buf);
	}

	final int write(final ByteBuffer buf) throws IOException
	{
		return this.writableByteChannel.write(buf);
	}

	final void createWriteBuffer(final ByteBuffer buf)
	{
		if (this.primaryWriteBuffer == null)
		{
			this.primaryWriteBuffer = this.selectorThread.getPooledBuffer();
			this.primaryWriteBuffer.put(buf);
		}
		else
		{
			final ByteBuffer temp = this.selectorThread.getPooledBuffer();
			temp.put(buf);

			final int remaining = temp.remaining();
			this.primaryWriteBuffer.flip();
			final int limit = this.primaryWriteBuffer.limit();

			if (remaining >= this.primaryWriteBuffer.remaining())
			{
				temp.put(this.primaryWriteBuffer);
				this.selectorThread.recycleBuffer(this.primaryWriteBuffer);
				this.primaryWriteBuffer = temp;
			}
			else
			{
				this.primaryWriteBuffer.limit(remaining);
				temp.put(this.primaryWriteBuffer);
				this.primaryWriteBuffer.limit(limit);
				this.primaryWriteBuffer.compact();
				this.secondaryWriteBuffer = this.primaryWriteBuffer;
				this.primaryWriteBuffer = temp;
			}
		}
	}

	final boolean hasPendingWriteBuffer()
	{
		return this.primaryWriteBuffer != null;
	}

	final void movePendingWriteBufferTo(final ByteBuffer dest)
	{
		this.primaryWriteBuffer.flip();
		dest.put(this.primaryWriteBuffer);
		this.selectorThread.recycleBuffer(this.primaryWriteBuffer);
		this.primaryWriteBuffer = this.secondaryWriteBuffer;
		this.secondaryWriteBuffer = null;
	}

	final void setReadBuffer(final ByteBuffer buf)
	{
		this.readBuffer = buf;
	}

	final ByteBuffer getReadBuffer()
	{
		return this.readBuffer;
	}

	public final boolean isClosed()
	{
		return this.pendingClose;
	}

	final NioNetStackList<SendablePacket<T>> getSendQueue()
	{
		return this.sendQueue;
	}

	/*
	 * final SendablePacket<T> getClosePacket() { return this.closePacket; }
	 */

	@SuppressWarnings("unchecked")
	public final void close(final SendablePacket<T> sp)
	{

		close(new SendablePacket[]{sp});
	}

	public final void close(final SendablePacket<T>[] closeList)
	{
		if (this.pendingClose)
		{
			return;
		}

		synchronized (getSendQueue())
		{
			if (!this.pendingClose)
			{
				this.pendingClose = true;
				this.sendQueue.clear();
				for (SendablePacket<T> sp : closeList)
				{
					this.sendQueue.addLast(sp);
				}
			}
		}

		try
		{
			this.selectionKey.interestOps(this.selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
		}
		catch (CancelledKeyException e)
		{
			// ignore
		}

		// this.closePacket = sp;
		this.selectorThread.closeConnection(this);
	}

	final void releaseBuffers()
	{
		if (this.primaryWriteBuffer != null)
		{
			this.selectorThread.recycleBuffer(this.primaryWriteBuffer);
			this.primaryWriteBuffer = null;

			if (this.secondaryWriteBuffer != null)
			{
				this.selectorThread.recycleBuffer(this.secondaryWriteBuffer);
				this.secondaryWriteBuffer = null;
			}
		}

		if (this.readBuffer != null)
		{
			this.selectorThread.recycleBuffer(this.readBuffer);
			this.readBuffer = null;
		}
	}
}
