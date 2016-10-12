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

import l2server.log.Log;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * @author Noctarius
 */

public final class L2Properties extends Properties
{
	private static final long serialVersionUID = 1L;

	public L2Properties()
	{
	}

	public L2Properties(String name) throws IOException
	{
		load(new FileInputStream(name));
	}

	public L2Properties(File file) throws IOException
	{
		load(new FileInputStream(file));
	}

	public L2Properties(InputStream inStream) throws IOException
	{
		load(inStream);
	}

	public L2Properties(Reader reader) throws IOException
	{
		load(reader);
	}

	public void load(String name) throws IOException
	{
		load(new FileInputStream(name));
	}

	public void load(File file) throws IOException
	{
		load(new FileInputStream(file));
	}

	@Override
	public void load(InputStream inStream) throws IOException
	{
		InputStreamReader reader = null;
		try
		{
			reader = new InputStreamReader(inStream, Charset.defaultCharset());
			super.load(reader);
		}
		finally
		{
			inStream.close();
			if (reader != null)
			{
				reader.close();
			}
		}
	}

	@Override
	public void load(Reader reader) throws IOException
	{
		try
		{
			super.load(reader);
		}
		finally
		{
			reader.close();
		}
	}

	@Override
	public String getProperty(String key)
	{
		String property = super.getProperty(key);

		if (property == null)
		{
			//Log.info("L2Properties: Missing property for key - " + key);

			return null;
		}

		return property.trim();
	}

	@Override
	public String getProperty(String key, String defaultValue)
	{
		String property = super.getProperty(key, defaultValue);

		if (property == null)
		{
			Log.warning("L2Properties: Missing defaultValue for key - " + key);

			return null;
		}

		return property.trim();
	}
}
