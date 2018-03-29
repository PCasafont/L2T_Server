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

package l2server.gameserver.geoeditorcon;

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.log.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Luno, Dezmond
 */
public class GeoEditorThread extends Thread
{

	private boolean working = false;

	private int mode = 0; // 0 - don't send coords, 1 - send each

	// validateposition from client, 2 - send in
	// intervals of sendDelay ms.
	private int sendDelay = 1000; // default - once in second

	private Socket geSocket;

	private OutputStream out;

	private List<L2PcInstance> gms;

	public GeoEditorThread(Socket ge)
	{
		geSocket = ge;
		working = true;
		gms = new ArrayList<>();
	}

	@Override
	public void interrupt()
	{
		try
		{
			geSocket.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		super.interrupt();
	}

	@Override
	public void run()
	{
		try
		{
			out = geSocket.getOutputStream();
			int timer = 0;

			while (working)
			{
				if (!isConnected())
				{
					working = false;
				}

				if (mode == 2 && timer > sendDelay)
				{
					for (L2PcInstance gm : gms)
					{
						if (!gm.getClient().getConnection().isClosed())
						{
							sendGmPosition(gm);
						}
						else
						{
							gms.remove(gm);
						}
					}
					timer = 0;
				}

				try
				{
					sleep(100);
					if (mode == 2)
					{
						timer += 100;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "GeoEditor disconnected. " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				geSocket.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			working = false;
		}
	}

	public void sendGmPosition(int gx, int gy, short z)
	{
		if (!isConnected())
		{
			return;
		}
		try
		{
			synchronized (out)
			{
				writeC(0x0b); // length 11 bytes!
				writeC(0x01); // Cmd = save cell;
				writeD(gx); // Global coord X;
				writeD(gy); // Global coord Y;
				writeH(z); // Coord Z;
				out.flush();
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "GeoEditor disconnected. " + e.getMessage(), e);
			working = false;
		}
		finally
		{
			try
			{
				geSocket.close();
			}
			catch (Exception ignored)
			{
			}
			working = false;
		}
	}

	public void sendGmPosition(L2PcInstance gm)
	{
		sendGmPosition(gm.getX(), gm.getY(), (short) gm.getZ());
	}

	public void sendPing()
	{
		if (!isConnected())
		{
			return;
		}
		try
		{
			synchronized (out)
			{
				writeC(0x01); // length 1 byte!
				writeC(0x02); // Cmd = ping (dummy packet for connection
				// test);
				out.flush();
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "GeoEditor disconnected. " + e.getMessage(), e);
			working = false;
		}
		finally
		{
			try
			{
				geSocket.close();
			}
			catch (Exception ignored)
			{
			}
			working = false;
		}
	}

	private void writeD(int value) throws IOException
	{
		out.write(value & 0xff);
		out.write(value >> 8 & 0xff);
		out.write(value >> 16 & 0xff);
		out.write(value >> 24 & 0xff);
	}

	private void writeH(int value) throws IOException
	{
		out.write(value & 0xff);
		out.write(value >> 8 & 0xff);
	}

	private void writeC(int value) throws IOException
	{
		out.write(value & 0xff);
	}

	public void setMode(int value)
	{
		mode = value;
	}

	public void setTimer(int value)
	{
		if (value < 500)
		{
			sendDelay = 500; // maximum - 2 times per second!
		}
		else if (value > 60000)
		{
			sendDelay = 60000; // Minimum - 1 time per minute.
		}
		else
		{
			sendDelay = value;
		}
	}

	public void addGM(L2PcInstance gm)
	{
		if (!gms.contains(gm))
		{
			gms.add(gm);
		}
	}

	public void removeGM(L2PcInstance gm)
	{
		if (gms.contains(gm))
		{
			gms.remove(gm);
		}
	}

	public boolean isSend(L2PcInstance gm)
	{
		return mode == 1 && gms.contains(gm);
	}

	private boolean isConnected()
	{
		return geSocket.isConnected() && !geSocket.isClosed();
	}

	public boolean isWorking()
	{
		sendPing();
		return working;
	}

	public int getMode()
	{
		return mode;
	}
}
