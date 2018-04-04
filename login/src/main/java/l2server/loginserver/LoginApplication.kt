package l2server.loginserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LoginApplication

fun main(args: Array<String>) {
	// FIXME I'M DIRTY! Use spring to initialize the server
	L2LoginServer()
	// Run spring application
	runApplication<LoginApplication>(*args)
}
