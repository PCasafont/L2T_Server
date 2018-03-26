package l2server.gameserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GameApplication

fun main(args: Array<String>) {
	// FIXME I'M DIRTY! Use spring to initialize the server
	Server()
	// Run spring application
	runApplication<GameApplication>(*args)
}
