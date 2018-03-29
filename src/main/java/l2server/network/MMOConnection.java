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
		address = socket.getInetAddress();
		readableByteChannel = socket.getChannel();
		writableByteChannel = socket.getChannel();
		port = socket.getPort();
		selectionKey = key;

		sendQueue = new NioNetStackList<>();

		try
		{
			socket.setTcpNoDelay(tcpNoDelay);
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
		return client;
	}

	public final void sendPacket(final SendablePacket<T> sp)
	{
		sp.client = client;

		if (pendingClose)
		{
			return;
		}

		synchronized (getSendQueue())
		{
			sendQueue.addLast(sp);
		}

		if (!sendQueue.isEmpty())
		{
			try
			{
				selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
			}
			catch (CancelledKeyException e)
			{
				// ignore
			}
		}
	}

	final SelectionKey getSelectionKey()
	{
		return selectionKey;
	}

	public final InetAddress getInetAddress()
	{
		return address;
	}

	public final int getPort()
	{
		return port;
	}

	final void close() throws IOException
	{
		socket.close();
	}

	final int read(final ByteBuffer buf) throws IOException
	{
		return readableByteChannel.read(buf);
	}

	final int write(final ByteBuffer buf) throws IOException
	{
		return writableByteChannel.write(buf);
	}

	final void createWriteBuffer(final ByteBuffer buf)
	{
		if (primaryWriteBuffer == null)
		{
			primaryWriteBuffer = selectorThread.getPooledBuffer();
			primaryWriteBuffer.put(buf);
		}
		else
		{
			final ByteBuffer temp = selectorThread.getPooledBuffer();
			temp.put(buf);

			final int remaining = temp.remaining();
			primaryWriteBuffer.flip();
			final int limit = primaryWriteBuffer.limit();

			if (remaining >= primaryWriteBuffer.remaining())
			{
				temp.put(primaryWriteBuffer);
				selectorThread.recycleBuffer(primaryWriteBuffer);
				primaryWriteBuffer = temp;
			}
			else
			{
				primaryWriteBuffer.limit(remaining);
				temp.put(primaryWriteBuffer);
				primaryWriteBuffer.limit(limit);
				primaryWriteBuffer.compact();
				secondaryWriteBuffer = primaryWriteBuffer;
				primaryWriteBuffer = temp;
			}
		}
	}

	final boolean hasPendingWriteBuffer()
	{
		return primaryWriteBuffer != null;
	}

	final void movePendingWriteBufferTo(final ByteBuffer dest)
	{
		primaryWriteBuffer.flip();
		dest.put(primaryWriteBuffer);
		selectorThread.recycleBuffer(primaryWriteBuffer);
		primaryWriteBuffer = secondaryWriteBuffer;
		secondaryWriteBuffer = null;
	}

	final void setReadBuffer(final ByteBuffer buf)
	{
		readBuffer = buf;
	}

	final ByteBuffer getReadBuffer()
	{
		return readBuffer;
	}

	public final boolean isClosed()
	{
		return pendingClose;
	}

	final NioNetStackList<SendablePacket<T>> getSendQueue()
	{
		return sendQueue;
	}

	/*
	 * final SendablePacket<T> getClosePacket() { return closePacket; }
	 */

	@SuppressWarnings("unchecked")
	public final void close(final SendablePacket<T> sp)
	{

		close(new SendablePacket[]{sp});
	}

	public final void close(final SendablePacket<T>[] closeList)
	{
		if (pendingClose)
		{
			return;
		}

		synchronized (getSendQueue())
		{
			if (!pendingClose)
			{
				pendingClose = true;
				sendQueue.clear();
				for (SendablePacket<T> sp : closeList)
				{
					sendQueue.addLast(sp);
				}
			}
		}

		try
		{
			selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
		}
		catch (CancelledKeyException e)
		{
			// ignore
		}

		// closePacket = sp;
		selectorThread.closeConnection(this);
	}

	final void releaseBuffers()
	{
		if (primaryWriteBuffer != null)
		{
			selectorThread.recycleBuffer(primaryWriteBuffer);
			primaryWriteBuffer = null;

			if (secondaryWriteBuffer != null)
			{
				selectorThread.recycleBuffer(secondaryWriteBuffer);
				secondaryWriteBuffer = null;
			}
		}

		if (readBuffer != null)
		{
			selectorThread.recycleBuffer(readBuffer);
			readBuffer = null;
		}
	}
}
