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

/**
 * @param <T>
 * @author KenM
 */
public abstract class SendablePacket<T extends MMOClient<?>> extends AbstractPacket<T>
{
	protected T writeClient;

	public final T getWriteClient()
	{
		return writeClient;
	}

	protected final void putInt(final int value)
	{
		buf.putInt(value);
	}

	protected final void putDouble(final double value)
	{
		buf.putDouble(value);
	}

	protected final void putFloat(final float value)
	{
		buf.putFloat(value);
	}

	/**
	 * Write <B>byte</B> to the buffer. <BR>
	 * 8bit integer (00)
	 *
	 * @param data
	 */
	protected final void writeC(final int data)
	{
		buf.put((byte) data);
	}

	/**
	 * Write <B>double</B> to the buffer. <BR>
	 * 64bit double precision float (00 00 00 00 00 00 00 00)
	 *
	 * @param value
	 */
	protected final void writeF(final double value)
	{
		buf.putDouble(value);
	}

	/**
	 * Write <B>float</B> to the buffer. <BR>
	 * 64bit double precision float (00 00 00 00 00 00 00 00)
	 *
	 * @param value
	 */
	protected final void writeFl(final float value)
	{
		buf.putFloat(value);
	}

	/**
	 * Write <B>short</B> to the buffer. <BR>
	 * 16bit integer (00 00)
	 *
	 * @param value
	 */
	protected final void writeH(final int value)
	{
		buf.putShort((short) value);
	}

	/**
	 * Write <B>int</B> to the buffer. <BR>
	 * 32bit integer (00 00 00 00)
	 *
	 * @param value
	 */
	protected final void writeD(final int value)
	{
		buf.putInt(value);
	}

	/**
	 * Write <B>long</B> to the buffer. <BR>
	 * 64bit integer (00 00 00 00 00 00 00 00)
	 *
	 * @param value
	 */
	protected final void writeQ(final long value)
	{
		buf.putLong(value);
	}

	/**
	 * Write <B>byte[]</B> to the buffer. <BR>
	 * 8bit integer array (00 ...)
	 *
	 * @param data
	 */
	protected final void writeB(final byte[] data)
	{
		buf.put(data);
	}

	/**
	 * Write <B>String</B> to the buffer.
	 *
	 * @param text
	 */
	protected final void writeS(final String text)
	{
		if (text != null)
		{
			final int len = text.length();
			for (int i = 0; i < len; i++)
			{
				buf.putChar(text.charAt(i));
			}
		}

		buf.putChar('\000');
	}

	protected abstract void write();
}
