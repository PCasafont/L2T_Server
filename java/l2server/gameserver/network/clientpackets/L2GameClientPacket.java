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
package l2server.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import java.util.logging.Level;

import l2server.Config;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.log.Log;
import l2server.network.ReceivablePacket;

/**
 * Packets received by the game server from clients
 * @author  KenM
 */
public abstract class L2GameClientPacket extends ReceivablePacket<L2GameClient>
{
	@Override
	public boolean read()
	{
		/*if (getClient() != null && getClient().getAccountName() != null
				&& getClient().getAccountName().equalsIgnoreCase("pere"))
			Log.info("C: " + this.getType());*/
		try
		{
			readImpl();
			return true;
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Client: " + getClient().toString() + " - Failed reading: " + getType() + " ; " + e.getMessage(), e);
			
			if (e instanceof BufferUnderflowException) // only one allowed per client per minute
				getClient().onBufferUnderflow();
		}
		return false;
	}
	
	protected abstract void readImpl();
	
	@Override
	public void run()
	{
		try
		{
			/* Removes onspawn protection - player has faster computer than average
			 * Since GE: True for all packets
			 * except RequestItemList and UseItem (in case the item is a Scroll of Escape (736)
			 */
			if (triggersOnActionRequest())
			{
				final L2PcInstance actor = getClient().getActiveChar();
				if (actor != null)
				{
					if (actor.isSpawnProtected() || actor.isInvul())
					{
						actor.onActionRequest();
						if (Config.DEBUG)
							Log.info("Spawn protection for player " + actor.getName() + " removed by packet: " + getType());
					}
					
					actor.setHasMoved(true);
				}
			}
			
			runImpl();
			cleanUp();
		}
		catch (Throwable t)
		{
			Log.log(Level.SEVERE, "Client: " + getClient().toString() + " - Failed running: " + getType() + " ; " + t.getMessage(), t);
			// in case of EnterWorld error kick player from game
			if (this instanceof EnterWorld)
				getClient().closeNow();
		}
	}
	
	protected abstract void runImpl();
	
	protected final void sendPacket(L2GameServerPacket gsp)
	{
		getClient().sendPacket(gsp);
	}
	
	/**
	 * @return A String with this packet name for debuging purposes
	 */
	public abstract String getType();
	
	protected boolean triggersOnActionRequest()
	{
		return true;
	}
	
	protected void cleanUp()
	{}
}
