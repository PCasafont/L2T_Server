package l2server.gameserver

import l2server.Config
import l2server.L2DatabaseFactory
import l2server.ServerMode
import l2server.gameserver.idfactory.IdFactory
import l2server.util.concurrent.ThreadPool
import l2server.util.loader.Loader
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File

@SpringBootApplication
class GameApplication

fun main(args: Array<String>) {
    val log = LoggerFactory.getLogger(GameApplication::class.java)

    ServerMode.serverMode = ServerMode.MODE_GAMESERVER

    // Initialize config
    Config.load()

    ThreadPool.initThreadPools(GameThreadPools())

    L2DatabaseFactory.getInstance()

    val idFactory = IdFactory.getInstance()

    if (!idFactory.isInitialized()) {
        log.error("Could not read object IDs from DB. Please Check Your Data.")
        throw Exception("Could not initialize the ID factory")
    }

    ThreadPoolManager.getInstance()

    File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "crests").mkdirs()
    File("log/game").mkdirs()

	Loader.initialize(GameApplication::class.java.`package`.name)
	Loader.run()
	log.info(Loader.getDependencyTreeString())

	// FIXME I'M DIRTY! Use spring to initialize the server
	Server()
	// Run spring application
	runApplication<GameApplication>(*args)
}
