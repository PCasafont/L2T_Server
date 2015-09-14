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
package l2server.gameserver.network.serverpackets;

import java.util.logging.Level;

import l2server.gameserver.network.L2GameClient;
import l2server.log.Log;
import l2server.network.SendablePacket;

/**
 *
 * @author  KenM
 */
public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{
	protected boolean _invisible = false;
	
	/**
	 * 
	 * @return True if packet originated from invisible character.
	 */
	public boolean isInvisible()
	{
		return _invisible;
	}
	
	/**
	 * Set "invisible" boolean flag in the packet.
	 * Packets from invisible characters will not be broadcasted to players.
	 * @param b
	 */
	public void setInvisible(boolean b)
	{
		_invisible = b;
	}
	
	/**
	 * @see l2server.mmocore.network.SendablePacket#write()
	 */
	@Override
	protected void write()
	{
		try
		{
			/*if (getClient() != null && getClient().getAccountName() != null
					&& getClient().getAccountName().equalsIgnoreCase("pere"))
			{
				Log.info("S: " + getType());
				//if (!(this instanceof UserInfo))
				//	return;
			}*/
			
			writeImpl();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Client: " + getClient().toString() + " - Failed writing: " + getType() + " ; " + e.getMessage(), e);
		}
	}
	
	public void runImpl()
	{
		
	}
	
	protected abstract void writeImpl();
	
	/**
	 * @return A String with this packet name for debuging purposes
	 */
	public abstract String getType();
}
