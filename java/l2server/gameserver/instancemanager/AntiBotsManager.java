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

package l2server.gameserver.instancemanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.GmListTable;
import l2server.gameserver.Server;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.util.Util;
import l2server.log.Log;

/**
 * @author Zakaxx
 */
public class AntiBotsManager
{
    public class ClientProcess
    {
        public String _productName;
        public String _filePath;
        public String _fileName;
        public String _fileDescription;
        public long _firstSeenAt;
        public long _lastSeenAt;
        public boolean _isRunning;

        private ClientProcess(String productName, String filePath, String fileName, String fileDescription, long firstSeenAt)
        {
            _productName = productName;
            _filePath = filePath;
            _fileName = fileName;
            _fileDescription = fileDescription;
            _firstSeenAt = firstSeenAt;
        }

        private String getFileName()
        {
            return _fileName;
        }

        private void setIsRunning(boolean b)
        {
            _isRunning = b;
        }

        private void updateLastSeenAt()
        {
            _lastSeenAt = System.currentTimeMillis();
        }

        private String getFilePath()
        {
            return _filePath;
        }

        private boolean isRunning()
        {
            return _isRunning;
        }

        private String getProductName()
        {
            return _productName;
        }
    }

    public class ClientInfo
    {
        private final ArrayList<ClientProcess> _processes = new ArrayList<ClientProcess>();
        private String _ip;
        private String _localIp;
        private String _windowsUser;
        private String _hardwareId;
        private String _version;
        private long _lastUpdateTime;

        private ClientInfo(String windowsUser, String hardwareId, String ip, String localIp)
        {
            _windowsUser = windowsUser;
            _hardwareId = hardwareId;
            _ip = ip;
            _localIp = localIp;
        }

        public final String getIp()
        {
            return _ip;
        }

        @SuppressWarnings("unused")
        private final String getLocalIp()
        {
            return _localIp;
        }

        public final String getWindowsUser()
        {
            return _windowsUser;
        }

        public final String getHardwareId()
        {
            return _hardwareId;
        }

        public final String getVersion()
        {
            return _version;
        }

        private void setVersion(String b)
        {
            _version = b;
        }

        public final long getLastUpdateTime()
        {
            return _lastUpdateTime;
        }

        private void setLastUpdateTime(long b)
        {
            _lastUpdateTime = b;
        }

        private final void updateProcesses(final String[] processes)
        {
            for (String processe : processes)
            {
                if (processe.equals(""))
                {
                    continue;
                }

                final String[] processData = processe.split("\\|", -1);
                if (processData.length < 4)
                {
                    //System.out.println(processes[i]);
                    continue;
                }

                String filePath = null;
                try
                {
                    filePath = processData[1]; // TODO Pass, retrieve and use ProcessId instead of the File Path.
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                ArrayList<ClientProcess> fileProcesses = getProcessesByFilePath(filePath);

                ClientProcess process = null;
                if (fileProcesses.size() == 0)
                {
                    String productName = processData[0];
                    String processFilePath = processData[1];
                    String fileName = processData[2];
                    String fileDescription = processData[3];
                    long firstSeenAt = System.currentTimeMillis();

                    if (fileName.isEmpty())
                    {
                        fileName = processFilePath;
                        while (fileName.contains("\\"))
                        {
                            fileName = fileName.substring(fileName.indexOf("\\") + 1);
                        }
                    }
                    process = new ClientProcess(productName, processFilePath, fileName, fileDescription, firstSeenAt);
                    _processes.add(process);
                }
                else
                {
                    process = fileProcesses.get(0);
                }

                process.updateLastSeenAt();
            }

            // Go through known processes and check the ones that are no longer running.
            for (ClientProcess process : _processes)
            {
                if (!isProcessRunningIn(processes, process.getFileName()))
                {
                    process.setIsRunning(false);
                }
                else
                {
                    process.setIsRunning(true);
                }
            }
        }

        public final ArrayList<ClientProcess> getProcesses()
        {
            return _processes;
        }

        private final ArrayList<ClientProcess> getProcessesByFilePath(final String filePath)
        {
            ArrayList<ClientProcess> result = new ArrayList<ClientProcess>();
            for (ClientProcess process : _processes)
            {
                if (!process.getFilePath().equals(filePath))
                {
                    continue;
                }

                result.add(process);
            }
            return result;
        }

        private final boolean isProcessRunningIn(final String[] processes, final String fileName)
        {
            for (String process : processes)
            {
                if (process.equals(""))
                {
                    continue;
                }

                final String[] processData = process.split("\\|", -1);
                if (processData.length < 4)
                {
                    continue;
                }

                String name = processData[2];
                if (name.isEmpty())
                {
                    name = processData[1];
                    while (name.contains("\\"))
                    {
                        name = name.substring(name.indexOf("\\") + 1);
                    }
                }

                if (!name.equals(fileName))
                {
                    continue;
                }

                return true;
            }
            return false;
        }
    }

    private class PlayerBanTrace
    {
        private final long _banTime;
        private final int _characterBanDuration;
        private final int _hardwareBanDuration;
        private final String _reason;
        private final String _accountName;
        private final String _hardwareId;

        private PlayerBanTrace(final long banTime, final int characterBanDuration, final int hardwareBanDuration, final String reason, final String accountName, final String hardwareId)
        {
            _banTime = banTime;
            _characterBanDuration = characterBanDuration;
            _hardwareBanDuration = hardwareBanDuration;
            _reason = reason;
            _accountName = accountName;
            _hardwareId = hardwareId;
        }

        @SuppressWarnings("unused")
        private final long getBanTime()
        {
            return _banTime;
        }

        private final int getCharacterBanDuration()
        {
            return _characterBanDuration;
        }

        private final int getHardwareBanDuration()
        {
            return _hardwareBanDuration;
        }

        private final String getReason()
        {
            return _reason;
        }

        private final String getAccountName()
        {
            return _accountName;
        }

        private final String getHardwareId()
        {
            return _hardwareId;
        }
    }

    //Trace the users by his forum IP AKA users using ping tools
    private static final Map<String, String> _conflictiveUserIps = new HashMap<String, String>();

    private static final String SELECT_AUTH_DATA_FOR_IP =
            "SELECT localIp, hardwareId, windowsUser, version, processes, lastUpdateTime FROM " + Config.LOGIN_DB_NAME +
                    ".auth_info WHERE userIp = ? ORDER BY lastUpdateTime ASC";
    private static final String SELECT_AUTH_DATA_FOR_HWID =
            "SELECT userIp, localIp, windowsUser, version, processes, lastUpdateTime FROM " + Config.LOGIN_DB_NAME +
                    ".auth_info WHERE hardwareId = ? ORDER BY lastUpdateTime ASC";

    private static final String SELECT_FORUM_IP_FOR_ACCOUNT_NAME =
            "SELECT ip_address from " + Config.FORUM_DB_NAME + ".core_sessions where member_name IN (" +
                    "SELECT name from " + Config.FORUM_DB_NAME + ".core_members WHERE name IN (" +
                    "SELECT forum FROM " + Config.WEB_DB_NAME + ".accounts WHERE game = ?));";
    private static final String WEBSITE_URL = "http://antibots.l2tenkai.com/AB-CHECK.html";

    private static final String _latestVersion = "2.0.1";
    private static int _failedWebHitTimes = 0;

    // Key: IP
    public static final Map<String, ClientInfo> _clients = new HashMap<String, ClientInfo>();
    public static final ArrayList<PlayerBanTrace> _pendingBans = new ArrayList<PlayerBanTrace>();
    public static final ArrayList<String> _hardwareBans = new ArrayList<String>();

    private static final String[] PROHIBITED_PROCESSES_NAMES = {
            "l2tower", //http://www.forum.l2tower.eu
            "zranger", //http://zranger.net
            "z-ranger", "l2net", "l2divine", //http://www.l2divine.com
            "l2ph", //http://l2ph.coderx.ru/arhive
            "l2packethack", "hlapex", "ntracker", //http://www.l2tracker.net
            "l2walker", //http://www.towalker.com
            "adrenaline", //http://l2bot.eu/
            "l2control", //http://www.l2control.com/
            "la2robot", "la2util", "autoit" //https://www.autoitscript.com
    };

    public AntiBotsManager()
    {
        init();
    }

    private final void init()
    {
        ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AntibotsTask(), 300000, 300000);
    }

    public class AntibotsTask implements Runnable
    {
        public AntibotsTask()
        {

        }

        @Override
        public void run()
        {
            boolean isWebReachable = getWebResponse();
            if (!isWebReachable)
            {
                _failedWebHitTimes++;
                Log.info("AntiBotsManager: Couldn't hit on our website (" + _failedWebHitTimes +
                        " times in a row). Aborting any check on this execution. (" + Util.getCurrentDate() + ")");
                return;
            }

            _failedWebHitTimes = 0;

            final Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers().values();
            for (final L2PcInstance player : allPlayers)
            {
                if (player == null) // Just logged out...
                {
                    continue;
                }

                final L2GameClient playerClient = player.getClient();
                if (playerClient == null)
                {
                    continue;
                }

                if (playerClient.isDetached())
                {
                    continue;
                }

                String ipAddress = getProperPlayerIP(player.getExternalIP());
                List<ClientInfo> foundHardware = getClientsInfoByIp(ipAddress);
                if (foundHardware.size() == 0)
                {
                    GmListTable.broadcastMessageToGMs("===============================");
                    GmListTable.broadcastMessageToGMs(
                            player.getName() + " [" + ipAddress + "] did not send any antibots data yet...");

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - player.getLastAccess());
                    if (minutes <= 5)
                    {
                        GmListTable.broadcastMessageToGMs("But " + player.getName() + " just logged " + minutes +
                                " minutes ago, let's see on the next check!");
                        continue;
                    }

                    removeConflictiveIp(player.getExternalIP());

                    GmListTable.broadcastMessageToGMs("Checking forum ip for: " + player.getName() + ".");

                    //check forum
                    String forumIpAddress = getForumPlayerIp(player.getAccountName());
                    if (forumIpAddress != null)
                    {
                        GmListTable.broadcastMessageToGMs("Found his forum IP: " + forumIpAddress + "!");
                        foundHardware = getClientsInfoByIp(forumIpAddress);
                        //if we get any result we add the ip?
                        if (foundHardware.size() != 0)
                        {
                            addConflictiveIp(player.getExternalIP(), forumIpAddress);
                            GmListTable.broadcastMessageToGMs("And his hardware!");
                        }
                        else
                        {
                            GmListTable.broadcastMessageToGMs("But not his hardware!");
                        }
                    }
                    else
                    {
                        GmListTable.broadcastMessageToGMs("Can't locate his forum IP!");
                    }
                }

                if (foundHardware.size() == 0)
                {
                    if (!removeConflictiveIp(player.getExternalIP()))
                    {
                        sendAntiBotMessage(player);

                        if (Config.ANTI_BOTS_KICK_IF_NO_DATA_RECEIVED)
                        {
                            ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    GmListTable.broadcastMessageToGMs(
                                            player.getName() + " has been kicked. His system was outdated.");
                                    if (!player.isGM())
                                    {
                                        player.logout();
                                    }
                                }
                            }, 20000);
                        }
                    }
                    continue;
                }

                boolean hasRecentlySentData = false;
                // Go through each of the PCs found for this player...
                String hardwareId = "";
                for (ClientInfo clientInfo : foundHardware)
                {
                    if (clientInfo == null)
                    {
                        continue;
                    }

                    if (player.getClient() != null && player.getClient().getHWId() != null)
                    {
                        hardwareId = player.getClient().getHWId();
                    }

                    // If the hardwareId is unknown, for now, we only get the hardware that last gave us informations coming from this toon IP.
                    if (hardwareId.equals(""))
                    {
                        hardwareId = clientInfo.getHardwareId();
                        player.getClient().setHWId(hardwareId);
                    }

                    if (clientInfo.getVersion() != null && !clientInfo.getVersion().equals(_latestVersion))
                    {
                        GmListTable.broadcastMessageToGMs(
                                player.getName() + " is running on Client[" + clientInfo.getVersion() +
                                        "], latest is " + _latestVersion + ".");
                    }

                    if (clientInfo.getLastUpdateTime() + 600000 < System.currentTimeMillis())
                    {
                        continue;
                    }
                    else
                    {
                        hasRecentlySentData = true;
                    }

                    // Finally, we check if this character is running any illegal process...
                    List<ClientProcess> illegalProcesses = getIllegalProcesses(clientInfo);

                    // Certain processes are illegal, but, may not have been used on the current character...
                    // Depending on the configurations, we remove them so that no sanction happens.
                    for (Iterator<ClientProcess> processesIterator = illegalProcesses.iterator();
                         processesIterator.hasNext(); )
                    {
                        ClientProcess process = processesIterator.next();
                        if (Config.ANTI_BOTS_SANCTION_ONLY_IF_TOOL_STILL_RUNNING && !process.isRunning())
                        {
                            processesIterator.remove();
                        }
                    }

					/*if (illegalProcesses.size() != 0)
                    {
						final String accountName = player.getClient().getAccountName();

						if (isHardwareInPendingBanList(clientInfo.getHardwareId()) && isAccountInPendingBanList(accountName))
							continue;

						int randomDelay = Rnd.get(1, 5);
						int minBannedHours = 48;

						GmListTable.broadcastMessageToGMs("Scheduling ban for Account[" + accountName + "] in " + randomDelay + " minutes, " + player.getName() + " was found using...:");

						// Log to file...
						Util.logToFile("Scheduling ban for Account[" + accountName + "], Hardware[" + clientInfo.getHardwareId() + "] in " + randomDelay + " minutes, " + player.getName() + " was found using...:", "Antibots", true);

						//Check how many times he has been banned
						int timesBanned = getBannedTimes(clientInfo.getHardwareId());
						if (timesBanned == 1)
							minBannedHours = 3;
						else if (timesBanned >= 3)	//3 times max
							minBannedHours = -1;

						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.HACKING_TOOL));

						for (ClientProcess process : illegalProcesses)
						{
							GmListTable.broadcastMessageToGMs("- Product Name:" + process.getProductName() + ", File Exe: " + process.getFileName() + ", File Path: " + process.getFilePath());

							// Log to file...
							Util.logToFile("- Product Name:" + process.getProductName() + ", File Exe: " + process.getFileName() + ", File Path: " + process.getFilePath(), "Antibots", true);
						}

						Util.logToFile("He/She has been banned already " + timesBanned + " times.", "Antibots", true);

						addToPendingBanLists(
							randomDelay * 1000 * 60,
							minBannedHours, // break for the character...
							minBannedHours, // break from the server...
							"The use of third party software to automate (even just parts of) gameplay is prohibited.", // Reason...
							player.getClient().getAccountName(),  // We ban the account...
							clientInfo.getHardwareId()); // and the hardware.
					}*/
                }

                // If the Anti Bots did not send any information to us for over 10 minutes, it was likely closed.
                if (!hasRecentlySentData)
                {
                    if (!removeConflictiveIp(player.getExternalIP()))
                    {
                        sendAntiBotMessage(player);

                        if (Config.ANTI_BOTS_KICK_IF_NO_DATA_RECEIVED)
                        {
                            GmListTable.broadcastMessageToGMs(
                                    player.getName() + " did not provide any client info for over 10 minutes.");

                            if (!player.isGM())
                            {
                                playerClient.setDetached(true);
                            }
                        }
                    }
                }
            }

            // We're now going to look if there's any player that should gtfo...
			/*if (_pendingBans.size() != 0)
			{
				final long currentTime = System.currentTimeMillis();
				PlayerBanTrace toBan = null;
				for (PlayerBanTrace playerBanTrace : _pendingBans)
				{
					if (playerBanTrace.getBanTime() > currentTime)
						continue;

					toBan = playerBanTrace;
					break;
				}

				if (toBan != null)
					banPlayer(toBan);
			}*/
        }
    }

    private void sendAntiBotMessage(L2PcInstance player)
    {
        return;
		/*
		if (player == null)
			return;
		System.out.println("sending to " + player.getName());
		if (player.getName().equalsIgnoreCase("AceKilla") || player.getName().equalsIgnoreCase("Elerni"))
			return;

		NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
		String antiBotInfo = HtmCache.getInstance().getHtm(null, "AntiBot.html");
		if (antiBotInfo != null)
			htmlMsg.setHtml(antiBotInfo);

		player.sendPacket(htmlMsg);*/
    }

    @SuppressWarnings("unused")
    private int getBannedTimes(String hwId)
    {
        int bannedTimes = 0;
        try
        {
            File antibotFile = new File("./Antibots.txt");
            if (!antibotFile.exists())
            {
                return 0;
            }

            LineNumberReader readFile = new LineNumberReader(new BufferedReader(new FileReader(antibotFile)));

            String line = null;
            String date = "";
            while ((line = readFile.readLine()) != null)
            {
                if (line.isEmpty())
                {
                    continue;
                }

                if (line.contains(hwId))
                {
                    String dateInfo = line.split("@")[0];
                    if (!date.equalsIgnoreCase(dateInfo))
                    {
                        date = dateInfo;
                        bannedTimes++;
                    }
                }
            }
            readFile.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return bannedTimes;
    }

    private String getForumPlayerIp(String accountName)
    {
        String forumIp = null;
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;

            ResultSet rs = null;
            statement = con.prepareStatement(SELECT_FORUM_IP_FOR_ACCOUNT_NAME); //Maybe check time too (last_visit)
            statement.setString(1, accountName);
            rs = statement.executeQuery();

            if (rs.next())
            {
                forumIp = rs.getString("ip_address");
            }

            rs.close();
            statement.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            L2DatabaseFactory.close(con);
        }

        return forumIp;
    }

    @SuppressWarnings("unused")
    private final void addToPendingBanLists(final int delay, final int characterBanDuration, final int hardwareBanDuration, final String reason, final String accountName, final String hardwareId)
    {
        _pendingBans
                .add(new PlayerBanTrace(System.currentTimeMillis() + delay, characterBanDuration, hardwareBanDuration,
                        reason, accountName, hardwareId));
    }

    @SuppressWarnings("unused")
    private final void banPlayer(final PlayerBanTrace playerBanTrace)
    {
        final String hardwareId = playerBanTrace.getHardwareId();

        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            PreparedStatement statement = con.prepareStatement(
                    "REPLACE INTO ban_timers (identity, timer, author, reason) VALUES (?, ?, ?, ?);");

            statement.setString(1, playerBanTrace.getAccountName());
            statement.setLong(2, playerBanTrace.getCharacterBanDuration() > 0 ?
                    System.currentTimeMillis() / 1000 + playerBanTrace.getCharacterBanDuration() * 3600 : -1);
            statement.setString(3, "Anti Bots");
            statement.setString(4, playerBanTrace.getReason());
            statement.execute();
            statement.close();

            if (hardwareId != null && hardwareId.length() > 0)
            {
                statement = con.prepareStatement(
                        "REPLACE INTO ban_timers (identity, timer, author, reason) VALUES (?, ?, ?, ?);");

                statement.setString(1, hardwareId);
                statement.setLong(2, playerBanTrace.getHardwareBanDuration() > 0 ?
                        System.currentTimeMillis() / 1000 + playerBanTrace.getHardwareBanDuration() * 3600 : -1);
                statement.setString(3, "Anti Bots");
                statement.setString(4, playerBanTrace.getReason());
                statement.execute();
                statement.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }
        }

        if (Config.ANTI_BOTS_BAN_OFFENDERS_RELIED_CHARACTERS)
        {
            // We kick out any logged in character of this player.
            for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
            {
                final L2GameClient playerClient = player.getClient();
                // Not the same hardware...
                if (playerClient == null)
                {
                    continue;
                }

                final String playerHardwareId = playerClient.getHWId();

                if (hardwareId == null || playerHardwareId == null)
                {
                    continue;
                }
                else if (!playerHardwareId.equals(hardwareId))
                {
                    continue;
                }

                if (playerClient.getActiveChar() != null)
                {
                    playerClient.getActiveChar().logout(true);
                }
                else
                {
                    playerClient.closeNow();
                }
            }

            _pendingBans.remove(playerBanTrace);

            // TODO
            // Maybe ban his other characters accounts? :$
        }
    }

    private List<ClientProcess> getIllegalProcesses(final ClientInfo clientInfo)
    {
        List<ClientProcess> result = new ArrayList<ClientProcess>();
        for (ClientProcess process : clientInfo.getProcesses())
        {
            if (!isIllegalProcess(process))
            {
                continue;
            }

            result.add(process);
        }

        return result;
    }

    public final boolean isIllegalProcess(final ClientProcess process)
    {
        final String productName = process.getProductName().toLowerCase();
        final String fileExeName = process.getFileName().toLowerCase();
        final String filePath = process.getFilePath().toLowerCase();

        if (process.getProductName().equalsIgnoreCase("AlienAdrenaline.GameModeProcessor"))
        {
            return false;
        }

        if (filePath.contains("lineage ii dreams"))
        {
            return false;
        }

        for (String element : PROHIBITED_PROCESSES_NAMES)
        {
            if (productName.contains(element) || fileExeName.contains(element) || filePath.contains(element))
            {
                return true;
            }
        }

        return false;
    }

    public final List<ClientInfo> getClientsInfoByIp(final String ip)
    {
        List<ClientInfo> result = new ArrayList<ClientInfo>();

        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;

            ResultSet rs = null;

            statement = con.prepareStatement(SELECT_AUTH_DATA_FOR_IP);
            statement.setString(1, ip);

            rs = statement.executeQuery();
            while (rs.next())
            {
                final String localIp = rs.getString("localIp");
                final String hardwareId = rs.getString("hardwareId");
                final String userId = ip + "-" + hardwareId;
                final long lastUpdateTime = rs.getLong("lastUpdateTime");

                ClientInfo clientInfo = _clients.get(userId);
                if (clientInfo == null)
                {
                    // If this client did not login since the previous restart, discard it
                    if (lastUpdateTime < Server.dateTimeServerStarted.getTimeInMillis())
                    {
                        continue;
                    }

                    clientInfo = new ClientInfo(rs.getString("windowsUser"), hardwareId, ip, localIp);
                    _clients.put(userId, clientInfo);
                }

                final String version = rs.getString("version");

                // Check if this client received any update...
                if (clientInfo.getVersion() != null && !clientInfo.getVersion().equals(version))
                {
                    clientInfo.setVersion(rs.getString("version"));
                }

                // Set the time at which the client last sent us info...
                clientInfo.setLastUpdateTime(lastUpdateTime);

                // Update the running processes...
                clientInfo.updateProcesses(rs.getString("processes").split(";"));
                result.add(clientInfo);
            }

            rs.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "AntibotsManager: Couldn't retrieve ClientInfo for IP[" + ip + "]... ", e);
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
                Log.log(Level.WARNING, "", e);
            }
        }

        return result;
    }

    public final ClientInfo getClientInfoByHardwareId(final String hardwareId)
    {
        ClientInfo result = null;

        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;

            ResultSet rs = null;

            statement = con.prepareStatement(SELECT_AUTH_DATA_FOR_HWID);
            statement.setString(1, hardwareId);

            rs = statement.executeQuery();

            while (rs.next())
            {
                final String userIp = rs.getString("userIp");

                final String userId = userIp + "-" + hardwareId;
                if (_clients.containsKey(userId))
                {
                    result = _clients.get(userId);
                }
            }

            rs.close();
            statement.close();
        }
        catch (Exception e)
        {
            Log.log(Level.WARNING, "AntibotsManager: Couldn't retrieve ClientInfo for HWID[" + hardwareId + "]... ", e);
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
                Log.log(Level.WARNING, "", e);
            }
        }

        return result;
    }

    public final String getHardwareIdFor(final String ipAddress)
    {
        List<ClientInfo> foundHardwares = getClientsInfoByIp(ipAddress);
        if (foundHardwares.size() == 0)
        {
            return null;
        }

        // We get the last hardware we got an update from...
        return foundHardwares.get(0).getHardwareId();
    }

    public final boolean isAccountInPendingBanList(final String accountName)
    {
        for (PlayerBanTrace playerBanTrace : _pendingBans)
        {
            if (!playerBanTrace.getAccountName().equals(accountName))
            {
                continue;
            }

            return true;
        }

        return false;
    }

    public final boolean isHardwareInPendingBanList(final String hardwareId)
    {
        for (PlayerBanTrace playerBanTrace : _pendingBans)
        {
            if (!playerBanTrace.getHardwareId().equals(hardwareId))
            {
                continue;
            }

            return true;
        }

        return false;
    }

    private boolean getWebResponse()
    {
        String result = "";

        URL url;
        HttpURLConnection connection = null;
        try
        {
            url = new URL(WEBSITE_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "AB-CHECK");
            connection.setInstanceFollowRedirects(false);

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));

            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null)
            {
                response.append(line);
                response.append('\r');
            }
            result = response.toString();

            is.close();
            rd.close();
        }
        catch (Exception e)
        {
            result = null;
            e.printStackTrace();
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }

        return result != null && result.contains("Website is available!");
    }

    private static AntiBotsManager _instance;

    public static AntiBotsManager getInstance()
    {
        if (_instance == null)
        {
            _instance = new AntiBotsManager();
        }

        return _instance;
    }

    private void addConflictiveIp(String fakeIp, String realIp)
    {
        synchronized (_conflictiveUserIps)
        {
            _conflictiveUserIps.put(fakeIp, realIp);
        }
    }

    private boolean removeConflictiveIp(String ip)
    {
        synchronized (_conflictiveUserIps)
        {
            if (_conflictiveUserIps.containsKey(ip))
            {
                return _conflictiveUserIps.remove(ip) != null;
            }
        }

        return false;
    }

    public String getProperPlayerIP(String ip)
    {
        synchronized (_conflictiveUserIps)
        {
            if (_conflictiveUserIps.containsKey(ip))
            {
                //	System.out.println(ip + " " + _conflictiveUserIps.get(ip));
                return _conflictiveUserIps.get(ip);
            }
        }
        return ip;
    }
}
