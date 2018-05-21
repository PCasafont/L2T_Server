package l2server.gameserver.api

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import l2server.gameserver.model.World
import org.slf4j.LoggerFactory

/**
 * @author Pere
 * @since 2018/05/21
 */
private val log = LoggerFactory.getLogger("publicApi")

fun Routing.publicApi() {
	get("/population") {
		data class PlayerClassPopulationDto(val classId: Int, val className: String, val count: Int)
		val classDtos = World.getInstance().allPlayers.values.groupBy { it.currentClass }.map {
			val playerClass = it.key
			PlayerClassPopulationDto(playerClass.id, playerClass.name, it.value.size)
		}
		call.respond(classDtos)
	}
}
