package l2server.gameserver.api

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.basic
import l2server.Base64
import l2server.DatabasePool
import l2server.util.crypt.PasswordCrypt
import org.slf4j.LoggerFactory
import java.sql.Connection

/**
 * @author Pere
 * @since 2018/05/21
 */
private val log = LoggerFactory.getLogger("auth")

fun Application.installGameAuth() {
	install(Authentication) {
		basic("game") {
			validate {
				var principal: UserIdPrincipal? = null
				var con: Connection? = null
				try {
					con = DatabasePool.getInstance().connection
					val statement = con!!.prepareStatement("SELECT password FROM accounts WHERE login=?")
					statement.setString(1, it.name)
					val rset = statement.executeQuery()
					if (rset.next()) {
						val expected = Base64.decode(rset.getString("password"))
						val actual = PasswordCrypt.encryptPassword(it.name, it.password)
						if (actual.contentEquals(expected)) {
							principal = UserIdPrincipal(it.name)
						}
					}
					rset.close()
					statement.close()
				} catch (e: Exception) {
					log.warn("Could not check authentication ({}): {}", it, e.message, e)
				} finally {
					DatabasePool.close(con);
				}

				principal
			}
		}
	}
}
