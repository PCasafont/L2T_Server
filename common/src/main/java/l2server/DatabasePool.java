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

package l2server;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabasePool {
	private static Logger log = LoggerFactory.getLogger(DatabasePool.class.getName());
	
	public enum ProviderType {
		MySql,
		MsSql
	}
	
	private static class SingletonHolder {
		private static final DatabasePool INSTANCE = new DatabasePool();
	}
	
	// =========================================================
	// Data Field
	private ProviderType providerType;
	private BoneCP database;
	
	private final int PARTITION_COUNT = 4;
	
	// =========================================================
	// Constructor
	private DatabasePool() {
		BoneCPConfig config = null;
		
		try {
			if (Config.DATABASE_MAX_CONNECTIONS < 2) {
				Config.DATABASE_MAX_CONNECTIONS = 2;
				log.warn("A minimum of " + Config.DATABASE_MAX_CONNECTIONS + " db connections are required.");
			}
			
			config = new BoneCPConfig();
			config.setLazyInit(true);
			config.setPartitionCount(PARTITION_COUNT);
			config.setMinConnectionsPerPartition(5);
			config.setMaxConnectionsPerPartition(Math.max(10, Config.DATABASE_MAX_CONNECTIONS / PARTITION_COUNT));
			config.setAcquireRetryAttempts(5); // How often BoneCp tries to acquire a new connection after a failed try
			config.setAcquireRetryDelayInMs(3000); // Waiting time before trying to acquire connection again, too short delay might slow down the db
			config.setConnectionTimeoutInMs(0); // 0 = wait indefinitely for new connection
			config.setAcquireIncrement(5); // if pool is exhausted, get 5 more connections at a time
			config.setIdleMaxAgeInSeconds(Config.DATABASE_MAX_IDLE_TIME); // 0 = idle connections never expire
			config.setStatementsCacheSize(20); // L2J put a fantasy value (100) here with comment "SURE?", let's try with less since default is 0
			config.setJdbcUrl(Config.DATABASE_URL);
			config.setUsername(Config.DATABASE_LOGIN);
			config.setPassword(Config.DATABASE_PASSWORD);
			config.setTransactionRecoveryEnabled(true);
			
			/*
			 *  This is bonecp's counterpart to what L2J does manually by scheduling a helper thread which after a while checks if a requested connection has been closed again.
			 *  It's ONLY for debugging purpose, thus deactivated for Tenkai live servers. Quote from BoneCp documentation:
			 *  "This is for debugging purposes only and will create a new thread for each call to getConnection().
			 *  Enabling this option will have a big negative impact on pool performance."
			 */
			//config.setCloseConnectionWatch(true);
			//config.setCloseConnectionWatchTimeout(300000);
			
			database = new BoneCP(config);
			
			// Test the connection
			database.getConnection().close();
			
			if (Config.DEBUG) {
				log.debug("Database Connection Working");
			}
			
			if (Config.DATABASE_DRIVER.toLowerCase().contains("microsoft")) {
				providerType = ProviderType.MsSql;
			} else {
				providerType = ProviderType.MySql;
			}
		} catch (Exception e) {
			if (Config.DEBUG) {
				log.debug("Database Connection FAILED");
			}
		}
	}
	
	// =========================================================
	// Method - Public
	public final String prepQuerySelect(String[] fields, String tableName, String whereClause, boolean returnOnlyTopRecord) {
		String msSqlTop1 = "";
		String mySqlTop1 = "";
		if (returnOnlyTopRecord) {
			if (getProviderType() == ProviderType.MsSql) {
				msSqlTop1 = " Top 1 ";
			}
			if (getProviderType() == ProviderType.MySql) {
				mySqlTop1 = " Limit 1 ";
			}
		}
		return "SELECT " + msSqlTop1 + safetyString(fields) + " FROM " + tableName + " WHERE " + whereClause + mySqlTop1;
	}
	
	public void shutdown() {
		log.info("During this session the connection pool initialized " + database.getTotalCreatedConnections() + " connections.");
		if (database.getTotalLeased() > 0) {
			log.info(database.getTotalLeased() + " of them are still in use by the application at this moment.");
		}
		log.info("Shutting down pool...");
		
		try {
			database.close();
		} catch (Exception e) {
			log.info("", e);
		}
		
		database = null;
	}
	
	public final String safetyString(String... whatToCheck) {
		// NOTE: Use brace as a safety precaution just in case name is a reserved word
		final char braceLeft;
		final char braceRight;
		
		if (getProviderType() == ProviderType.MsSql) {
			braceLeft = '[';
			braceRight = ']';
		} else {
			braceLeft = '`';
			braceRight = '`';
		}
		
		int length = 0;
		
		for (String word : whatToCheck) {
			length += word.length() + 4;
		}
		
		final StringBuilder sbResult = new StringBuilder(length);
		
		for (String word : whatToCheck) {
			if (sbResult.length() > 0) {
				sbResult.append(", ");
			}
			
			sbResult.append(braceLeft);
			sbResult.append(word);
			sbResult.append(braceRight);
		}
		
		return sbResult.toString();
	}
	
	// =========================================================
	// Property - Public
	public static DatabasePool getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public Connection getConnection() {
		Connection con = null;
		while (con == null) {
			try {
				con = database.getConnection();
			} catch (SQLException e) {
				log.warn("DatabasePool: getConnection() failed for GameDatabase, trying again " + e.getMessage(), e);
			}
		}
		
		return con;
	}
	
	public static void close(Connection con) {
		if (con == null) {
			return;
		}
		
		try {
			con.close();
		} catch (SQLException e) {
			log.warn("Failed to close database connection!", e);
		}
	}
	
	public int getBusyConnectionCount() {
		return database.getTotalLeased();
	}
	
	public int getIdleConnectionCount() {
		return database.getTotalFree();
	}
	
	public final ProviderType getProviderType() {
		return providerType;
	}
}
