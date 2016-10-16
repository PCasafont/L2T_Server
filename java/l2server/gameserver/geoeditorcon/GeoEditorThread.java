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
	// intervals of this.sendDelay ms.
	private int sendDelay = 1000; // default - once in second

	private Socket geSocket;

	private OutputStream out;

	private List<L2PcInstance> gms;

	public GeoEditorThread(Socket ge)
	{
		this.geSocket = ge;
		this.working = true;
		this.gms = new ArrayList<>();
	}

	@Override
	public void interrupt()
	{
		try
		{
			this.geSocket.close();
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
			this.out = this.geSocket.getOutputStream();
			int timer = 0;

			while (this.working)
			{
				if (!isConnected())
				{
					this.working = false;
				}

				if (this.mode == 2 && timer > sendDelay)
				{
					for (L2PcInstance gm : this.gms)
					{
						if (!gm.getClient().getConnection().isClosed())
						{
							sendGmPosition(gm);
						}
						else
						{
							this.gms.remove(gm);
						}
					}
					timer = 0;
				}

				try
				{
					sleep(100);
					if (this.mode == 2)
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
				this.geSocket.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			this.working = false;
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
			synchronized (this.out)
			{
				writeC(0x0b); // length 11 bytes!
				writeC(0x01); // Cmd = save cell;
				writeD(gx); // Global coord X;
				writeD(gy); // Global coord Y;
				writeH(z); // Coord Z;
				this.out.flush();
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "GeoEditor disconnected. " + e.getMessage(), e);
			this.working = false;
		}
		finally
		{
			try
			{
				this.geSocket.close();
			}
			catch (Exception ignored)
			{
			}
			this.working = false;
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
			synchronized (this.out)
			{
				writeC(0x01); // length 1 byte!
				writeC(0x02); // Cmd = ping (dummy packet for connection
				// test);
				this.out.flush();
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "GeoEditor disconnected. " + e.getMessage(), e);
			this.working = false;
		}
		finally
		{
			try
			{
				this.geSocket.close();
			}
			catch (Exception ignored)
			{
			}
			this.working = false;
		}
	}

	private void writeD(int value) throws IOException
	{
		this.out.write(value & 0xff);
		this.out.write(value >> 8 & 0xff);
		this.out.write(value >> 16 & 0xff);
		this.out.write(value >> 24 & 0xff);
	}

	private void writeH(int value) throws IOException
	{
		this.out.write(value & 0xff);
		this.out.write(value >> 8 & 0xff);
	}

	private void writeC(int value) throws IOException
	{
		this.out.write(value & 0xff);
	}

	public void setMode(int value)
	{
		this.mode = value;
	}

	public void setTimer(int value)
	{
		if (value < 500)
		{
			this.sendDelay = 500; // maximum - 2 times per second!
		}
		else if (value > 60000)
		{
			this.sendDelay = 60000; // Minimum - 1 time per minute.
		}
		else
		{
			this.sendDelay = value;
		}
	}

	public void addGM(L2PcInstance gm)
	{
		if (!this.gms.contains(gm))
		{
			this.gms.add(gm);
		}
	}

	public void removeGM(L2PcInstance gm)
	{
		if (this.gms.contains(gm))
		{
			this.gms.remove(gm);
		}
	}

	public boolean isSend(L2PcInstance gm)
	{
		return this.mode == 1 && this.gms.contains(gm);
	}

	private boolean isConnected()
	{
		return this.geSocket.isConnected() && !this.geSocket.isClosed();
	}

	public boolean isWorking()
	{
		sendPing();
		return this.working;
	}

	public int getMode()
	{
		return this.mode;
	}
}
