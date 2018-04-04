package l2server

import l2server.gameserver.GameApplication
import l2server.gameserver.GameThreadPools
import l2server.util.concurrent.ThreadPool
import org.slf4j.LoggerFactory


/**
 * @author Pere
 */
open class LoadTest {

	protected val log = LoggerFactory.getLogger(GameApplication::class.java.name)

	protected fun initializeServer() {
        ServerMode.serverMode = ServerMode.MODE_GAMESERVER

        // Initialize config
        Config.load()
        ThreadPool.initThreadPools(GameThreadPools())
    }
}
