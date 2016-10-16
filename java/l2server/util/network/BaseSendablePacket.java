/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.util.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class ...
 *
 * @version $Revision: 1.2.4.1 $ $Date: 2005/03/27 15:30:11 $
 */
public abstract class BaseSendablePacket
{
	ByteArrayOutputStream bao;

	protected BaseSendablePacket()
	{
		this.bao = new ByteArrayOutputStream();
	}

	protected void writeD(int value)
	{
		this.bao.write(value & 0xff);
		this.bao.write(value >> 8 & 0xff);
		this.bao.write(value >> 16 & 0xff);
		this.bao.write(value >> 24 & 0xff);
	}

	protected void writeH(int value)
	{
		this.bao.write(value & 0xff);
		this.bao.write(value >> 8 & 0xff);
	}

	protected void writeC(int value)
	{
		this.bao.write(value & 0xff);
	}

	protected void writeF(double org)
	{
		long value = Double.doubleToRawLongBits(org);
		this.bao.write((int) (value & 0xff));
		this.bao.write((int) (value >> 8 & 0xff));
		this.bao.write((int) (value >> 16 & 0xff));
		this.bao.write((int) (value >> 24 & 0xff));
		this.bao.write((int) (value >> 32 & 0xff));
		this.bao.write((int) (value >> 40 & 0xff));
		this.bao.write((int) (value >> 48 & 0xff));
		this.bao.write((int) (value >> 56 & 0xff));
	}

	protected void writeS(String text)
	{
		try
		{
			if (text != null)
			{
				this.bao.write(text.getBytes("UTF-16LE"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		this.bao.write(0);
		this.bao.write(0);
	}

	protected void writeB(byte[] array)
	{
		try
		{
			this.bao.write(array);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected void writeQ(long value)
	{
		this.bao.write((int) (value & 0xff));
		this.bao.write((int) (value >> 8 & 0xff));
		this.bao.write((int) (value >> 16 & 0xff));
		this.bao.write((int) (value >> 24 & 0xff));
		this.bao.write((int) (value >> 32 & 0xff));
		this.bao.write((int) (value >> 40 & 0xff));
		this.bao.write((int) (value >> 48 & 0xff));
		this.bao.write((int) (value >> 56 & 0xff));
	}

	public int getLength()
	{
		return this.bao.size() + 2;
	}

	public byte[] getBytes()
	{
		//if (this instanceof Init)
		//	writeD(0x00); //reserve for XOR initial key

		writeD(0x00); // reserve for checksum

		int padding = this.bao.size() % 8;
		if (padding != 0)
		{
			for (int i = padding; i < 8; i++)
			{
				writeC(0x00);
			}
		}

		return this.bao.toByteArray();
	}

	public abstract byte[] getContent();
}
