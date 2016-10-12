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

package l2server.gameserver.network;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.LoginServerThread;
import l2server.gameserver.LoginServerThread.SessionKey;
import l2server.gameserver.Shutdown;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.instancemanager.AntiFeedManager;
import l2server.gameserver.instancemanager.CustomOfflineBuffersManager;
import l2server.gameserver.model.CharSelectInfoPackage;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.ServerClose;
import l2server.gameserver.security.SecondaryPasswordAuth;
import l2server.gameserver.util.FloodProtectors;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.network.MMOClient;
import l2server.network.MMOConnection;
import l2server.network.ReceivablePacket;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Represents a client connected on Game Server
 *
 * @author KenM
 */
public final class L2GameClient extends MMOClient<MMOConnection<L2GameClient>> implements Runnable
{
	protected static final Logger _logAccounting = Logger.getLogger("accounting");

	/**
	 * CONNECTED	- client has just connected
	 * AUTHED		- client has authed but doesnt has character attached to it yet
	 * IN_GAME		- client has selected a char and is in game
	 *
	 * @author KenM
	 */
	public enum GameClientState
	{
		CONNECTED, AUTHED, IN_GAME
	}

	private GameClientState _state;

	// Info
	private final InetAddress _addr;
	private String _accountName;
	private SessionKey _sessionId;
	private L2PcInstance _activeChar;
	private ReentrantLock _activeCharLock = new ReentrantLock();
	private SecondaryPasswordAuth _secondaryAuth;

	private boolean _isAuthedGG;
	private long _connectionStartTime;
	private CharSelectInfoPackage[] _charSlotMapping = null;

	// floodprotectors
	private final FloodProtectors _floodProtectors = new FloodProtectors(this);

	// Task
	protected ScheduledFuture<?> _autoSaveInDB;
	protected ScheduledFuture<?> _cleanupTask = null;

	private L2GameServerPacket _aditionalClosePacket;

	private GameCrypt _crypt;

	private ClientStats _stats;

	private boolean _isDetached = false;

	private boolean _protocolOk;
	private int _protocolVersion;

	private final ArrayBlockingQueue<ReceivablePacket<L2GameClient>> _packetQueue;
	private ReentrantLock _queueLock = new ReentrantLock();

	private int[][] trace;

	private String _hwId;

	private boolean _blockDisconnectTask;

	public L2GameClient(MMOConnection<L2GameClient> con)
	{
		super(con);
		_state = GameClientState.CONNECTED;
		_connectionStartTime = System.currentTimeMillis();
		_crypt = new GameCrypt();
		_stats = new ClientStats();

		_packetQueue = new ArrayBlockingQueue<>(Config.CLIENT_PACKET_QUEUE_SIZE);

		if (Config.CHAR_STORE_INTERVAL > 0)
		{
			_autoSaveInDB = ThreadPoolManager.getInstance()
					.scheduleGeneralAtFixedRate(new AutoSaveTask(), 300000L, Config.CHAR_STORE_INTERVAL * 60000L);
		}
		else
		{
			_autoSaveInDB = null;
		}

		try
		{
			_addr = con != null ? con.getInetAddress() : InetAddress.getLocalHost();
		}
		catch (UnknownHostException e)
		{
			throw new Error("Unable to determine localhost address.");
		}
	}

	public byte[] enableCrypt()
	{
		byte[] key;
		key = BlowFishKeygen.getRandomKey();
		_crypt.setKey(key);
		return key;
	}

	public GameClientState getState()
	{
		return _state;
	}

	public void setState(GameClientState pState)
	{
		if (_state != pState)
		{
			_state = pState;
			_packetQueue.clear();
		}
	}

	public ClientStats getStats()
	{
		return _stats;
	}

	/**
	 * Returns cached connection IP address, for checking detached clients.
	 * For loaded offline traders returns localhost address.
	 */
	public InetAddress getConnectionAddress()
	{
		return _addr;
	}

	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}

	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		_crypt.decrypt(buf.array(), buf.position(), size);
		return true;
	}

	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		_crypt.encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}

	public L2PcInstance getActiveChar()
	{
		return _activeChar;
	}

	public void setActiveChar(L2PcInstance pActiveChar)
	{
		_activeChar = pActiveChar;
		//JIV remove - done on spawn
		/*if (_activeChar != null)
        {
			L2World.getInstance().storeObject(getActiveChar());
		}*/
	}

	public ReentrantLock getActiveCharLock()
	{
		return _activeCharLock;
	}

	public FloodProtectors getFloodProtectors()
	{
		return _floodProtectors;
	}

	public void setGameGuardOk(boolean val)
	{
		_isAuthedGG = val;
	}

	public boolean isAuthedGG()
	{
		return _isAuthedGG;
	}

	public void setAccountName(String pAccountName)
	{
		_accountName = pAccountName;

		if (Config.SECOND_AUTH_ENABLED)
		{
			_secondaryAuth = new SecondaryPasswordAuth(this);
		}
	}

	public String getAccountName()
	{
		return _accountName;
	}

	public void setSessionId(SessionKey sk)
	{
		_sessionId = sk;
	}

	public SessionKey getSessionId()
	{
		return _sessionId;
	}

	public void sendPacket(L2GameServerPacket gsp)
	{
		if (_isDetached) // Temp fix for stuck characters in the loading screen
		{
			return;
		}

		// Packets from invisible chars sends only to GMs
		if (gsp.getInvisibleCharacter() != 0)
		{
			final L2PcInstance activeChar = getActiveChar();
			final L2PcInstance target = L2World.getInstance().getPlayer(gsp.getInvisibleCharacter());

			if (activeChar != null && target != null && !activeChar.isGM() && !activeChar.isInSameParty(target))
			{
				return;
			}
		}

		getConnection().sendPacket(gsp);
		gsp.runImpl();
	}

	public boolean isDetached()
	{
		return _isDetached;
	}

	public void setDetached(boolean b)
	{
		_isDetached = b;
	}

	/**
	 * Method to handle character deletion
	 *
	 * @return a byte:
	 * <li>-1: Error: No char was found for such charslot, caught exception, etc...
	 * <li> 0: character is not member of any clan, proceed with deletion
	 * <li> 1: character is member of a clan, but not clan leader
	 * <li> 2: character is clan leader
	 */
	public byte markToDeleteChar(int charslot)
	{
		int objid = getObjectIdForSlot(charslot);

		if (objid < 0)
		{
			return -1;
		}

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement("SELECT clanId FROM characters WHERE charId=?");
			statement.setInt(1, objid);
			ResultSet rs = statement.executeQuery();

			rs.next();

			int clanId = rs.getInt(1);
			byte answer = 0;
			if (clanId != 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(clanId);

				if (clan == null)
				{
					answer = 0; // jeezes!
				}
				else if (clan.getLeaderId() == objid)
				{
					answer = 2;
				}
				else
				{
					answer = 1;
				}
			}

			rs.close();
			statement.close();

			// Setting delete time
			if (answer == 0)
			{
				if (Config.DELETE_DAYS == 0)
				{
					deleteCharByObjId(objid);
				}
				else
				{
					statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE charId=?");
					statement.setLong(1,
							System.currentTimeMillis() + Config.DELETE_DAYS * 86400000L); // 24*60*60*1000 = 86400000
					statement.setInt(2, objid);
					statement.execute();
					statement.close();
				}

				LogRecord record = new LogRecord(Level.WARNING, "Delete");
				record.setParameters(new Object[]{objid, L2GameClient.this});
				_logAccounting.log(record);
			}

			return answer;
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error updating delete time of character.", e);
			return -1;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Save the L2PcInstance to the database.
	 */
	public void saveCharToDisk()
	{
		try
		{
			L2PcInstance player = L2GameClient.this.getActiveChar();
			if (player != null)
			{
				player.store();
				player.storeRecommendations();
				if (Config.UPDATE_ITEMS_ON_CHAR_STORE)
				{
					player.getInventory().updateDatabase();
					player.getWarehouse().updateDatabase();
				}
			}
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error saving character..", e);
		}
	}

	public void markRestoredChar(int charslot)
	{
		//have to make sure active character must be nulled
        /*if (getActiveChar() != null)
		{
			saveCharToDisk (getActiveChar());
			if (Config.DEBUG) Logozo.fine("active Char saved");
			this.setActiveChar(null);
		}*/

		int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
		{
			return;
		}
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error restoring character.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		LogRecord record = new LogRecord(Level.WARNING, "Restore");
		record.setParameters(new Object[]{objid, L2GameClient.this});
		_logAccounting.log(record);
	}

	public static void deleteCharByObjId(int objid)
	{
		if (objid < 0)
		{
			return;
		}

		CharNameTable.getInstance().removeName(objid);

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("DELETE FROM character_abilities WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_ability_points WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_contacts WHERE charId=? OR contactId=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_friends WHERE charId=? OR friendId=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_hennas WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_instance_time WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_last_summons WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_macroses WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_mentees WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_norestart_zone_time WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_offline_trade WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_offline_trade_items WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_offline_trade_item_prices WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_premium_items WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_quest_global_data WHERE charId=?");
			statement.setInt(1, objid);
			statement.executeUpdate();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_raid_points WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_recipeshoplist WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_reco_bonus WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_skills WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_tpbookmark WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_ui_actions WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_ui_categories WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM heroes WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM item_auction_bid WHERE playerObjId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM olympiad_fights WHERE charOneId=? OR charTwoId=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement(
					"DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement(
					"DELETE FROM item_attributes WHERE itemId IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement(
					"DELETE FROM item_elementals WHERE itemId IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM characters WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error deleting character.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public L2PcInstance loadCharFromDisk(int charslot)
	{
		final int objId = getObjectIdForSlot(charslot);
		if (objId < 0)
		{
			return null;
		}

		L2PcInstance character = L2World.getInstance().getPlayer(objId);
		if (character != null)
		{
			// exploit prevention, should not happens in normal way
			Log.severe("Attempt of double login: " + character.getName() + "(" + objId + ") " + getAccountName());
			if (character.getClient() != null)
			{
				character.getClient().closeNow();
			}
			else
			{
				character.deleteMe();
			}
			return null;
		}

		character = L2PcInstance.load(objId);
		if (character != null)
		{
			// preinit some values for each login
			character.setRunning(); // running is default
			character.standUp(); // standing is default

			character.refreshOverloaded();
			character.refreshExpertisePenalty();
			character.setOnlineStatus(true, false);
		}
		else
		{
			Log.severe("could not restore in slot: " + charslot);
		}

		//setCharacter(character);
		return character;
	}

	/**
	 * @param chars
	 */
	public void setCharSelection(CharSelectInfoPackage[] chars)
	{
		_charSlotMapping = chars;
	}

	public CharSelectInfoPackage getCharSelection(int charslot)
	{
		if (_charSlotMapping == null || charslot < 0 || charslot >= _charSlotMapping.length)
		{
			return null;
		}
		return _charSlotMapping[charslot];
	}

	public SecondaryPasswordAuth getSecondaryAuth()
	{
		return _secondaryAuth;
	}

	public void close(L2GameServerPacket gsp, boolean blockDisconnectTask)
	{
		if (getConnection() == null)
		{
			return; // offline shop
		}

		_blockDisconnectTask = blockDisconnectTask;

		if (blockDisconnectTask)
		{
			cancelAutoSave();
		}

		close(gsp);
	}

	public final void cancelAutoSave()
	{
		// we are going to mannually save the char bellow thus we can force the cancel
		ScheduledFuture<?> future = _autoSaveInDB;
		_autoSaveInDB = null;
		if (future != null)
		{
			future.cancel(false);
		}
	}

	public void close(L2GameServerPacket gsp)
	{
		if (getConnection() == null)
		{
			return; // offline shop
		}
		if (_aditionalClosePacket != null)
		{
			getConnection().close(new L2GameServerPacket[]{_aditionalClosePacket, gsp});
		}
		else
		{
			getConnection().close(gsp);
		}
	}

	public void close(L2GameServerPacket[] gspArray)
	{
		if (getConnection() == null)
		{
			return; // ofline shop
		}
		getConnection().close(gspArray);
	}

	/**
	 * @param charslot
	 * @return
	 */
	private int getObjectIdForSlot(int charslot)
	{
		final CharSelectInfoPackage info = getCharSelection(charslot);
		if (info == null)
		{
			Log.warning(toString() + " tried to delete Character in slot " + charslot +
					" but no characters exits at that slot.");
			return -1;
		}
		return info.getObjectId();
	}

	@Override
	protected void onForcedDisconnection()
	{
		LogRecord record = new LogRecord(Level.WARNING, "Disconnected abnormally");
		record.setParameters(new Object[]{L2GameClient.this});
		_logAccounting.log(record);
	}

	@Override
	protected void onDisconnection()
	{
		if (_blockDisconnectTask)
		{
			cancelAutoSave();
			return;
		}

		// no long running tasks here, do it async
		try
		{
			ThreadPoolManager.getInstance().executeTask(new DisconnectTask());
		}
		catch (RejectedExecutionException e)
		{
			// server is closing
		}
	}

	/**
	 * Close client connection with {@link ServerClose} packet
	 */
	public void closeNow()
	{
		_isDetached = true; // prevents more packets execution
		close(ServerClose.STATIC_PACKET);
		synchronized (this)
		{
			if (_cleanupTask != null)
			{
				cancelCleanup();
			}
			_cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), 0); //instant
		}
	}

	/**
	 * Produces the best possible string representation of this client.
	 */
	@Override
	public String toString()
	{
		try
		{
			InetAddress address = getConnection().getInetAddress();
			switch (getState())
			{
				case CONNECTED:
					return "[IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				case AUTHED:
					return "[Account: " + getAccountName() + " - IP: " +
							(address == null ? "disconnected" : address.getHostAddress()) + "]";
				case IN_GAME:
					return "[Character: " + (getActiveChar() == null ? "disconnected" :
							getActiveChar().getName() + "[" + getActiveChar().getObjectId() + "]") + " - Account: " +
							getAccountName() + " - IP: " +
							(address == null ? "disconnected" : address.getHostAddress()) + "]";
				default:
					throw new IllegalStateException("Missing state on switch");
			}
		}
		catch (NullPointerException e)
		{
			return "[Character read failed due to disconnect]";
		}
	}

	class DisconnectTask implements Runnable
	{
		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			if (_blockDisconnectTask)
			{
				cancelAutoSave();
				return;
			}
			boolean fast = true;
			try
			{
				final L2PcInstance player = getActiveChar();
				if (player != null && !isDetached())
				{
					getActiveChar().storeZoneRestartLimitTime();
					setDetached(true);

					if (offlineModeConditions(getActiveChar()))
					{
						player.leaveParty();
						player.setIsInvul(true);

						if (Config.OFFLINE_SET_NAME_COLOR)
						{
							player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
							player.broadcastUserInfo();
						}

						if (player.getOfflineStartTime() == 0)
						{
							player.setOfflineStartTime(System.currentTimeMillis());
						}

						// Try to reduce the graphical lag from offline shops
						if (player.getSummons() != null)
						{
							for (L2SummonInstance summon : player.getSummons())
							{
								if (summon == null)
								{
									continue;
								}

								summon.unSummon(player);
							}
						}

						if (player.getPet() != null)
						{
							player.getPet().unSummon(player);
						}

						if (player.getAgathionId() != 0)
						{
							player.setAgathionId(0);
							player.broadcastUserInfo();
						}

						//Turn off toggles
						for (L2Abnormal eff : player.getAllEffects())
						{
							if (eff == null || eff.getSkill() == null)
							{
								continue;
							}

							if (eff.getSkill().isToggle())
							{
								eff.exit();
							}
						}

						LogRecord record = new LogRecord(Level.INFO, "Entering offline mode");
						record.setParameters(new Object[]{L2GameClient.this});
						_logAccounting.log(record);
						return;
					}

					if (player.isInCombat() || player.isLocked())
					{
						fast = false;
					}
				}
				cleanMe(fast);
			}
			catch (Exception e1)
			{
				Log.log(Level.WARNING, "Error while disconnecting client.", e1);
			}
		}
	}

	/**
	 * @param player the player to be check.
	 * @return {@code true} if the player is allowed to remain as off-line shop.
	 */
	private boolean offlineModeConditions(L2PcInstance player)
	{
		boolean canSetShop = false;
		if (player.isInOlympiadMode() || player.getInstanceId() != 0 || player.isInJail() ||
				player.getVehicle() != null || EventsManager.getInstance().isPlayerParticipant(player.getObjectId()) ||
				player.getEvent() != null)
		{
			return false;
		}

		if (Config.OFFLINE_TRADE_ENABLE && (player.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL ||
				player.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY ||
				player.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_CUSTOM_SELL))
		{
			canSetShop = true;
		}
		else if (Config.OFFLINE_CRAFT_ENABLE &&
				(player.isInCraftMode() || player.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_MANUFACTURE))
		{
			canSetShop = true;
		}

		if (Config.OFFLINE_MODE_IN_PEACE_ZONE && !player.isInsideZone(L2Character.ZONE_PEACE))
		{
			canSetShop = false;
		}

		if (Config.OFFLINE_BUFFERS_ENABLE && !player.isInStoreMode() && player.isInsideZone(L2Character.ZONE_PEACE) &&
				CustomOfflineBuffersManager.getInstance().setUpOfflineBuffer(player))
		{
			canSetShop = true;
		}

		return canSetShop;
	}

	public void cleanMe(boolean fast)
	{
		try
		{
			synchronized (this)
			{
				if (_cleanupTask == null)
				{
					_cleanupTask =
							ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), fast ? 5 : 15000L);
				}
			}
		}
		catch (Exception e1)
		{
			Log.log(Level.WARNING, "Error during cleanup.", e1);
		}
	}

	class CleanupTask implements Runnable
	{
		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			try
			{
				// we are going to manually save the char below thus we can force the cancel
				if (_autoSaveInDB != null)
				{
					_autoSaveInDB.cancel(true);
					//ThreadPoolManager.getInstance().removeGeneral((Runnable) _autoSaveInDB);
				}

				L2PcInstance player = getActiveChar();
				if (player != null) // this should only happen on connection loss
				{
					if (player.isLocked())
					{
						Log.log(Level.WARNING,
								"Player " + player.getName() + " still performing subclass actions during disconnect.");
					}

					// prevent closing again
					player.setClient(null);

					if (player.isOnline() && !Shutdown.getInstance().isShuttingDown())
					{
						player.deleteMe();
						AntiFeedManager.getInstance().onDisconnect(L2GameClient.this);
					}
				}
				setActiveChar(null);
			}
			catch (Exception e1)
			{
				Log.log(Level.WARNING, "Error while cleanup client.", e1);
			}
			finally
			{
				LoginServerThread.getInstance().sendLogout(getAccountName());
			}
		}
	}

	class AutoSaveTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_autoSaveInDB == null)
			{
				return;
			}

			try
			{
				L2PcInstance player = getActiveChar();
				if (player != null && player.isOnline() &&
						L2World.getInstance().getPlayer(player.getObjectId()) == player) // safety precaution
				{
					saveCharToDisk();
					if (player.getPet() != null)
					{
						player.getPet().store();
					}
				}
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "Error on AutoSaveTask.", e);
			}
		}
	}

	public boolean isProtocolOk()
	{
		return _protocolOk;
	}

	public void setProtocolOk(boolean b)
	{
		_protocolOk = b;
	}

	public int getProtocolVersion()
	{
		return _protocolVersion;
	}

	public void setProtocolVersion(int version)
	{
		_protocolVersion = version;
	}

	public boolean handleCheat(String punishment)
	{
		if (_activeChar != null)
		{
			Util.handleIllegalPlayerAction(_activeChar, toString() + ": " + punishment, Config.DEFAULT_PUNISH);
			return true;
		}

		Logger _logAudit = Logger.getLogger("audit");
		_logAudit.log(Level.INFO, "AUDIT: Client " + toString() + " kicked for reason: " + punishment);
		closeNow();
		return false;
	}

	/**
	 * Returns false if client can receive packets.
	 * True if detached, or flood detected, or queue overflow detected and queue still not empty.
	 */
	public boolean dropPacket()
	{
		if (_isDetached) // detached clients can't receive any packets
		{
			return true;
		}

		// flood protection
		if (getStats().countPacket(_packetQueue.size()))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}

		return getStats().dropPacket();
	}

	/**
	 * Counts buffer underflow exceptions.
	 */
	public void onBufferUnderflow()
	{
		if (getStats().countUnderflowException())
		{
			Log.severe("Client " + toString() + " - Disconnected: Too many buffer underflow exceptions.");
			closeNow();
			return;
		}
		if (_state == GameClientState.CONNECTED) // in CONNECTED state kick client immediately
		{
			if (Config.PACKET_HANDLER_DEBUG)
			{
				Log.severe("Client " + toString() + " - Disconnected, too many buffer underflows in non-authed state.");
			}
			closeNow();
		}
	}

	/**
	 * Counts unknown packets
	 */
	public void onUnknownPacket()
	{
		if (getStats().countUnknownPacket())
		{
			Log.severe("Client " + toString() + " - Disconnected: Too many unknown packets.");
			closeNow();
			return;
		}
		if (_state == GameClientState.CONNECTED) // in CONNECTED state kick client immediately
		{
			if (Config.PACKET_HANDLER_DEBUG)
			{
				Log.severe("Client " + toString() + " - Disconnected, too many unknown packets in non-authed state.");
			}
			closeNow();
		}
	}

	/**
	 * Add packet to the queue and start worker thread if needed
	 */
	public void execute(ReceivablePacket<L2GameClient> packet)
	{
		if (getStats().countFloods())
		{
			Log.severe("Client " + toString() + " - Disconnected, too many floods:" + getStats().longFloods +
					" long and " + getStats().shortFloods + " short.");
			closeNow();
			return;
		}

		if (!_packetQueue.offer(packet))
		{
			if (getStats().countQueueOverflow())
			{
				Log.severe("Client " + toString() + " - Disconnected, too many queue overflows.");
				closeNow();
			}
			else
			{
				sendPacket(ActionFailed.STATIC_PACKET);
			}

			return;
		}

		if (_queueLock.isLocked()) // already processing
		{
			return;
		}

		try
		{
			if (_state == GameClientState.CONNECTED)
			{
				if (getStats().processedPackets > 3)
				{
					if (Config.PACKET_HANDLER_DEBUG)
					{
						Log.severe("Client " + toString() + " - Disconnected, too many packets in non-authed state.");
					}
					closeNow();
					return;
				}

				ThreadPoolManager.getInstance().executeIOPacket(this);
			}
			else
			{
				ThreadPoolManager.getInstance().executePacket(this);
			}
		}
		catch (RejectedExecutionException e)
		{
			// if the server is shutdown we ignore
			if (!ThreadPoolManager.getInstance().isShutdown())
			{
				Log.severe("Failed executing: " + packet.getClass().getSimpleName() + " for Client: " + toString());
			}
		}
	}

	@Override
	public void run()
	{
		if (!_queueLock.tryLock())
		{
			return;
		}

		try
		{
			int count = 0;
			while (true)
			{
				final ReceivablePacket<L2GameClient> packet = _packetQueue.poll();
				if (packet == null) // queue is empty
				{
					return;
				}

				if (_isDetached) // clear queue immediately after detach
				{
					_packetQueue.clear();
					return;
				}

				try
				{
					packet.run();
				}
				catch (Exception e)
				{
					Log.severe("Exception during execution " + packet.getClass().getSimpleName() + ", client: " +
							toString() + "," + e.getMessage());
				}

				count++;
				if (getStats().countBurst(count))
				{
					return;
				}
			}
		}
		finally
		{
			_queueLock.unlock();
		}
	}

	public void setClientTracert(int[][] tracert)
	{
		trace = tracert;
	}

	public int[][] getTrace()
	{
		return trace;
	}

	public void setHWId(String hwId)
	{
		_hwId = hwId;
	}

	public String getHWId()
	{
		return _hwId;
	}

	private boolean cancelCleanup()
	{
		Future<?> task = _cleanupTask;
		if (task != null)
		{
			_cleanupTask = null;
			return task.cancel(true);
		}
		return false;
	}

	public void setAditionalClosePacket(L2GameServerPacket _aditionalClosePacket)
	{
		this._aditionalClosePacket = _aditionalClosePacket;
	}
}
