package l2server

import ch.qos.logback.classic.Level.ERROR
import ch.qos.logback.classic.Level.WARN
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import l2server.gameserver.gui.ConsoleTab.ConsoleFilter.*
import l2server.gameserver.gui.ServerGui

class UILogAppender : AppenderBase<ILoggingEvent>() {

	override fun append(event: ILoggingEvent) {
		ServerGui.getConsoleTab()?.onAppendMessage(when (event.level) {
			ERROR -> Errors
			WARN -> Warnings
			else -> Info
		}, event.message)
	}
}
