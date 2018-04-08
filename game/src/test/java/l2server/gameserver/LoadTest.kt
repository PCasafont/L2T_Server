package l2server.gameserver

import l2server.Config
import l2server.ServerMode
import l2server.util.concurrent.ThreadPool
import org.slf4j.LoggerFactory


/**
 * @author Pere
 */
open class LoadTest {

	protected val log = LoggerFactory.getLogger(LoadTest::class.java.name)

	protected fun initializeServer() {
        ServerMode.serverMode = ServerMode.MODE_GAMESERVER

        // Initialize config
		Config.load()
        ThreadPool.initThreadPools(GameThreadPools())
    }
}
