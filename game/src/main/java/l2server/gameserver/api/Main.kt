package l2server.gameserver.api

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

/**
 * @author Pere
 * @since 2018/05/21
 */
fun startApiServer() {
	// Run ktor application
	embeddedServer(Netty, port = 8087) {
		install(DefaultHeaders)
		install(Compression)
		install(CallLogging)
		install(CORS) {
			anyHost()
		}
		install(ContentNegotiation) {
			jackson {
				configure(SerializationFeature.INDENT_OUTPUT, true)
				registerModule(JavaTimeModule())
			}
		}

		installGameAuth()

		routing {
			publicApi()
			accountApi()
		}
	}.start(wait = true)
}
