package l2server.gameserver

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import l2server.Config
import l2server.DatabasePool
import l2server.ServerMode
import l2server.gameserver.idfactory.IdFactory
import l2server.util.concurrent.ThreadPool
import l2server.util.loader.Loader
import org.slf4j.LoggerFactory
import java.io.File

fun main(args: Array<String>) {
    val log = LoggerFactory.getLogger("GameApplication")

    ServerMode.serverMode = ServerMode.MODE_GAMESERVER

    // Initialize config
    Config.load()

    ThreadPool.initThreadPools(GameThreadPools())

	DatabasePool.getInstance()

    val idFactory = IdFactory.getInstance()
    if (!idFactory.isInitialized) {
        log.error("Could not read object IDs from DB. Please Check Your Data.")
        throw Exception("Could not initialize the ID factory")
    }

    ThreadPoolManager.getInstance()

    File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "crests").mkdirs()

	Loader.initialize("l2server.gameserver")
	Loader.run()

	// FIXME I'M DIRTY!
	Server()

	// Run ktor application
	val server = embeddedServer(Netty, port = 8087) {
		routing {
			get("/") {
				call.respondText("Ktor UP!")
			}
		}
	}
	server.start(wait = true)
}
