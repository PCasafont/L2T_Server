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
package l2tserver.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import l2tserver.Config;
import l2tserver.gameserver.Shutdown;
import l2tserver.gameserver.datatables.CharNameTable;
import l2tserver.gameserver.instancemanager.AntiBotsManager;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.L2GameClient.GameClientState;
import l2tserver.gameserver.network.serverpackets.CharSelected;
import l2tserver.gameserver.network.serverpackets.ExSubjobInfo;
import l2tserver.log.Log;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class CharacterSelect extends L2GameClientPacket
{
	private static final String _C__0D_CHARACTERSELECT = "[C] 0D CharacterSelect";
	
	protected static final Logger _logAccounting = Logger.getLogger("accounting");
	
	// cd
	private int _charSlot;
	
	@SuppressWarnings("unused")
	private int _unk1; 	// new in C4
	@SuppressWarnings("unused")
	private int _unk2;	// new in C4
	@SuppressWarnings("unused")
	private int _unk3;	// new in C4
	@SuppressWarnings("unused")
	private int _unk4;	// new in C4
	
	@Override
	protected void readImpl()
	{
		_charSlot = readD();
		_unk1 = readH();
		_unk2 = readD();
		_unk3 = readD();
		_unk4 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (!getClient().getFloodProtectors().getCharacterSelect().tryPerformAction("CharacterSelect")
				|| Shutdown.getInstance().isShuttingDown())
			return;
		
		if (Config.SECOND_AUTH_ENABLED && !getClient().getSecondaryAuth().isAuthed())
		{
			getClient().getSecondaryAuth().openDialog();
			return;
		}
		
		// we should always be able to acquire the lock
		// but if we can't lock then nothing should be done (ie repeated packet)
		if (this.getClient().getActiveCharLock().tryLock())
		{
			try
			{
				// should always be null
				// but if not then this is repeated packet and nothing should be done here
				if (this.getClient().getActiveChar() == null)
				{
					// The L2PcInstance must be created here, so that it can be attached to the L2GameClient
					if (Config.DEBUG)
					{
						Log.fine("selected slot:" + _charSlot);
					}
					
					//load up character from disk
					L2PcInstance cha = getClient().loadCharFromDisk(_charSlot);
					if (cha == null)
						return; // handled in L2GameClient
					
					if (cha.getAccessLevel().getLevel() < 0)
					{
						cha.logout();
						return;
					}
					
					CharNameTable.getInstance().addName(cha);
					
					cha.setClient(this.getClient());
					getClient().setActiveChar(cha);
					cha.setOnlineStatus(true, true);
					
					if (Config.ANTI_BOTS_ENABLED)
					{
						final String hardwareId = AntiBotsManager.getInstance().getHardwareIdFor(cha.getClient().getConnectionAddress().toString().replace("/", ""));
						
						if (hardwareId != null)
							getClient().setHWId(hardwareId);
					}
					
					//sendPacket(new SSQInfo());
					sendPacket(new ExSubjobInfo(cha));
					
					this.getClient().setState(GameClientState.IN_GAME);
					CharSelected cs = new CharSelected(cha, getClient().getSessionId().playOkID1);
					sendPacket(cs);
				}
			}
			finally
			{
				this.getClient().getActiveCharLock().unlock();
			}
			
			LogRecord record = new LogRecord(Level.INFO, "Logged in");
			record.setParameters(new Object[]{this.getClient()});
			_logAccounting.log(record);
		}
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__0D_CHARACTERSELECT;
	}
}
