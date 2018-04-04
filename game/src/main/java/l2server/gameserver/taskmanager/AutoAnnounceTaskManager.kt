/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.taskmanager

import l2server.Config
import l2server.gameserver.model.World
import l2server.gameserver.util.Broadcast
import l2server.util.concurrent.ThreadPool
import l2server.util.loader.annotations.Load
import l2server.util.xml.XmlDocument
import org.slf4j.LoggerFactory
import java.io.File

/**
 * @author nBd
 */
object AutoAnnounceTaskManager {

    private val log = LoggerFactory.getLogger(AutoAnnounceTaskManager::class.java)

    @Load(dependencies = [World::class])
    private fun load() {
        val file = File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/autoAnnouncements.xml")
        if (!file.exists()) {
            return
        }
        var count = 0
        val doc = XmlDocument(file)
        for (d in doc.getChildren()) {
            if (d.name.equals("announce", ignoreCase = true)) {
                val text = d.getString("text")
                val initial = d.getInt("initial")
                val reuse = d.getInt("reuse")

                ThreadPool.scheduleAtFixedRate({Broadcast.announceToOnlinePlayers(text)}, (initial * 60000).toLong(), (reuse * 60000).toLong())
                count++
            }
        }
        log.info("AutoAnnouncements: Loaded: $count auto announcements!")
    }

    private class AutoAnnouncement private constructor(private val text: String) : Runnable {

        override fun run() {
            Broadcast.announceToOnlinePlayers(text)
        }
    }
}
