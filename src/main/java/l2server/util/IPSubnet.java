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

package l2server.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPSubnet
{
	final byte[] addr;
	final byte[] mask;
	final boolean isIPv4;

	public IPSubnet(String input) throws UnknownHostException, NumberFormatException, ArrayIndexOutOfBoundsException
	{
		int idx = input.indexOf("/");
		if (idx > 0)
		{
			addr = InetAddress.getByName(input.substring(0, idx)).getAddress();
			mask = getMask(Integer.parseInt(input.substring(idx + 1)), addr.length);
			isIPv4 = addr.length == 4;

			if (!applyMask(addr))
			{
				throw new UnknownHostException(input);
			}
		}
		else
		{
			addr = InetAddress.getByName(input).getAddress();
			mask = getMask(addr.length * 8, addr.length); // host, no need to check mask
			isIPv4 = addr.length == 4;
		}
	}

	public IPSubnet(InetAddress addr, int mask) throws UnknownHostException
	{
        this.addr = addr.getAddress();
		isIPv4 = this.addr.length == 4;
        this.mask = getMask(mask, this.addr.length);
		if (!applyMask(this.addr))
		{
			throw new UnknownHostException(addr.toString() + "/" + mask);
		}
	}

	public byte[] getAddress()
	{
		return addr;
	}

	public boolean applyMask(byte[] addr)
	{
		// V4 vs V4 or V6 vs V6 checks
		if (isIPv4 == (addr.length == 4))
		{
			for (int i = 0; i < addr.length; i++)
			{
				if ((addr[i] & mask[i]) != addr[i])
				{
					return false;
				}
			}
		}
		else
		{
			// check for embedded v4 in v6 addr (not done !)
			if (isIPv4)
			{
				// my V4 vs V6
				for (int i = 0; i < addr.length; i++)
				{
					if ((addr[i + 12] & mask[i]) != addr[i])
					{
						return false;
					}
				}
			}
			else
			{
				// my V6 vs V4
				for (int i = 0; i < addr.length; i++)
				{
					if ((addr[i] & mask[i + 12]) != addr[i + 12])
					{
						return false;
					}
				}
			}
		}

		return true;
	}

	@Override
	public String toString()
	{
		int size = 0;
		for (byte element : mask)
		{
			size += Integer.bitCount(element & 0xFF);
		}

		try
		{
			return InetAddress.getByAddress(addr).toString() + "/" + size;
		}
		catch (UnknownHostException e)
		{
			return "Invalid";
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof IPSubnet)
		{
			return applyMask(((IPSubnet) o).getAddress());
		}
		else if (o instanceof InetAddress)
		{
			return applyMask(((InetAddress) o).getAddress());
		}

		return false;
	}

	private static byte[] getMask(int n, int maxLength) throws UnknownHostException
	{
		if (n > maxLength << 3 || n < 0)
		{
			throw new UnknownHostException("Invalid netmask: " + n);
		}

		final byte[] result = new byte[maxLength];
		for (int i = 0; i < maxLength; i++)
		{
			result[i] = (byte) 0xFF;
		}

		for (int i = (maxLength << 3) - 1; i >= n; i--)
		{
			result[i >> 3] = (byte) (result[i >> 3] << 1);
		}

		return result;
	}
}
