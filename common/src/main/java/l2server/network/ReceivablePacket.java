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

import java.nio.ByteBuffer;

/**
 * @param <T>
 * @author KenM
 */
public abstract class ReceivablePacket<T extends MMOClient<?>> extends AbstractPacket<T> implements Runnable {
	NioNetStringBuffer sbuf;
	
	protected ReceivablePacket() {
	
	}
	
	protected abstract boolean read();
	
	@Override
	public abstract void run();
	
	/**
	 * Reads <B>byte[]</B> from the buffer. <BR>
	 * Reads as many bytes as the length of the array.
	 *
	 * @param dst : the byte array which will be filled with the data.
	 */
	protected final void readB(final byte[] dst) {
		buf.get(dst);
	}
	
	/**
	 * Reads <B>byte[]</B> from the buffer. <BR>
	 * Reads as many bytes as the given length (len). Starts to fill the
	 * byte array from the given offset to <B>offset</B> + <B>len</B>.
	 *
	 * @param dst    : the byte array which will be filled with the data.
	 * @param offset : starts to fill the byte array from the given offset.
	 * @param len    : the given length of bytes to be read.
	 */
	protected final void readB(final byte[] dst, final int offset, final int len) {
		buf.get(dst, offset, len);
	}
	
	/**
	 * Reads <B>byte</B> from the buffer. <BR>
	 * 8bit integer (00)
	 *
	 */
	protected final int readC() {
		return buf.get() & 0xFF;
	}
	
	/**
	 * Reads <B>short</B> from the buffer. <BR>
	 * 16bit integer (00 00)
	 *
	 */
	protected final int readH() {
		return buf.getShort() & 0xFFFF;
	}
	
	/**
	 * Reads <B>int</B> from the buffer. <BR>
	 * 32bit integer (00 00 00 00)
	 *
	 */
	protected final int readD() {
		return buf.getInt();
	}
	
	/**
	 * Reads <B>long</B> from the buffer. <BR>
	 * 64bit integer (00 00 00 00 00 00 00 00)
	 *
	 */
	protected final long readQ() {
		return buf.getLong();
	}
	
	/**
	 * Reads <B>double</B> from the buffer. <BR>
	 * 64bit double precision float (00 00 00 00 00 00 00 00)
	 *
	 */
	protected final double readF() {
		return buf.getDouble();
	}
	
	/**
	 * Reads <B>String</B> from the buffer.
	 *
	 */
	protected final String readS() {
		sbuf.clear();
		
		char ch;
		while ((ch = buf.getChar()) != 0) {
			sbuf.append(ch);
		}
		
		return sbuf.toString();
	}
	
	/**
	 * packet forge purpose
	 *
	 */
	public void setBuffers(ByteBuffer data, T client, NioNetStringBuffer sBuffer) {
		buf = data;
		this.client = client;
		sbuf = sBuffer;
	}
}
