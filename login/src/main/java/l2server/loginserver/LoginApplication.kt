package l2server.loginserver

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
	// FIXME I'M DIRTY!
	L2LoginServer()

	// Run ktor application
	val server = embeddedServer(Netty, port = 8088) {
		routing {
			get("/") {
				call.respondText("Ktor UP!")
			}
		}
	}
	server.start(wait = true)
}
