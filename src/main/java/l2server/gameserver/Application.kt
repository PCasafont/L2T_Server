package l2server.gameserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

fun main(args: Array<String>) {
	// FIXME I'M DIRTY! Use spring to initialize the server
	Server()
	// Run spring application
	runApplication<Application>(*args)
}
