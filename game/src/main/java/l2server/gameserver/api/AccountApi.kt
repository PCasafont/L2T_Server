package l2server.gameserver.api

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.slf4j.LoggerFactory

/**
 * @author Pere
 * @since 2018/05/21
 */
private val log = LoggerFactory.getLogger("accountApi")

fun Routing.accountApi() {
	authenticate("game") {
		get("/auth") {
			call.respond(HttpStatusCode.OK)
		}
	}
}
