/*
 * $Header: Util.java, 14-Jul-2005 03:27:51 luisantonioa Exp $
 *
 * $Author: luisantonioa $ $Date: 14-Jul-2005 03:27:51 $ $Revision: 1 $ $Log:
 * Util.java,v $ Revision 1 14-Jul-2005 03:27:51 luisantonioa Added copyright
 * notice
 *
 *
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

package l2server.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class Util
{
	public static boolean isInternalIP(String ipAddress)
	{
		java.net.InetAddress addr = null;
		try
		{
			addr = java.net.InetAddress.getByName(ipAddress);
			return addr.isSiteLocalAddress() || addr.isLoopbackAddress();
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public static String printData(byte[] data, int len)
	{
		final StringBuilder result = new StringBuilder(len * 4);

		int counter = 0;

		for (int i = 0; i < len; i++)
		{
			if (counter % 16 == 0)
			{
				result.append(fillHex(i, 4) + ": ");
			}

			result.append(fillHex(data[i] & 0xff, 2) + " ");
			counter++;
			if (counter == 16)
			{
				result.append("   ");

				int charpoint = i - 15;
				for (int a = 0; a < 16; a++)
				{
					int t1 = 0xFF & data[charpoint++];
					if (t1 > 0x1f && t1 < 0x80)
					{
						result.append((char) t1);
					}
					else
					{
						result.append('.');
					}
				}

				result.append("\n");
				counter = 0;
			}
		}

		int rest = data.length % 16;
		if (rest > 0)
		{
			for (int i = 0; i < 17 - rest; i++)
			{
				result.append("   ");
			}

			int charpoint = data.length - rest;
			for (int a = 0; a < rest; a++)
			{
				int t1 = 0xFF & data[charpoint++];
				if (t1 > 0x1f && t1 < 0x80)
				{
					result.append((char) t1);
				}
				else
				{
					result.append('.');
				}
			}

			result.append("\n");
		}

		return result.toString();
	}

	public static String fillHex(int data, int digits)
	{
		String number = Integer.toHexString(data);

		for (int i = number.length(); i < digits; i++)
		{
			number = "0" + number;
		}

		return number;
	}

	/**
	 * @param raw
	 * @return
	 */
	public static String printData(byte[] raw)
	{
		return printData(raw, raw.length);
	}

	public static String printData(ByteBuffer buf)
	{
		byte[] data = new byte[buf.remaining()];
		buf.get(data);
		String hex = Util.printData(data, data.length);
		buf.position(buf.position() - data.length);
		return hex;
	}

	public static byte[] generateHex(int size)
	{
		byte[] array = new byte[size];
		Rnd.nextBytes(array);
		return array;
	}

	public static String getStackTrace(Throwable t)
	{
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public static byte[] toByteArray(String hex)
	{
		int len = (hex.length() + 1) / 3;
		byte[] data = new byte[len];
		for (int i = 0; i < len; i++)
		{
			data[i] =
					(byte) ((Character.digit(hex.charAt(i * 3), 16) << 4) + Character.digit(hex.charAt(i * 3 + 1), 16));
		}
		return data;
	}
}
