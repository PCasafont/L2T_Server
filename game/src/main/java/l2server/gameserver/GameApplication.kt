package l2server.gameserver

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import l2server.Config
import l2server.DatabasePool
import l2server.ServerMode
import l2server.gameserver.idfactory.IdFactory
import l2server.gameserver.model.World
import l2server.util.concurrent.ThreadPool
import l2server.util.loader.Loader
import org.slf4j.LoggerFactory
import java.io.File

fun main(args: Array<String>) {
	val serverLoadStart = System.currentTimeMillis()
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

	val serverLoadEnd = System.currentTimeMillis()
	log.info("Server Loaded in " + (serverLoadEnd - serverLoadStart) / 1000 + " seconds")

	// Run ktor application
	val server = embeddedServer(Netty, port = 8087) {
		install(DefaultHeaders)
		install(Compression)
		install(CallLogging)
		install(ContentNegotiation) {
			jackson {
				configure(SerializationFeature.INDENT_OUTPUT, true)
				registerModule(JavaTimeModule())
			}
		}
		routing {
			get("/population") {
				data class PlayerClassDto(val classId: Int, val className: String, val count: Int)
				val classDtos = World.getInstance().allPlayers.values.groupBy { it.currentClass }.map {
					val playerClass = it.key
					PlayerClassDto(playerClass.id, playerClass.name, it.value.size)
				}
				call.respond(classDtos)
			}
		}
	}
	server.start(wait = true)
}
