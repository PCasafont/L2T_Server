package l2server.util.crypt

import java.security.MessageDigest

/**
 * @author Pere
 * @since 2018/05/21
 */
object PasswordCrypt {

	fun encryptPassword(username: String, password: String): ByteArray {
		val md = MessageDigest.getInstance("SHA-512")
		val raw = (password.toLowerCase() + "XjCSl+n/mpc4" + username.toLowerCase()).toByteArray(charset("UTF-8"))
		return md.digest(raw)
	}
}
