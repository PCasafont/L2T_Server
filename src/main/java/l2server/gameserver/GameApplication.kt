package l2server.gameserver

import l2server.log.Log
import l2server.util.loader.Loader
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GameApplication

fun main(args: Array<String>) {

	Loader.initialize(GameApplication::class.java.`package`.name)
	Loader.run()
	Log.info(Loader.getDependencyTreeString())
	// FIXME I'M DIRTY! Use spring to initialize the server
	Server()
	// Run spring application
	runApplication<GameApplication>(*args)
}
