package l2server.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Pere
 * @since 2018/05/21
 */
fun Any.logger(): Lazy<Logger> {
	return lazy { LoggerFactory.getLogger(this.javaClass) }
}
