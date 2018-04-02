/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.datatables

import l2server.Config
import l2server.L2DatabaseFactory
import l2server.gameserver.LoginServerThread
import l2server.gameserver.Server
import l2server.gameserver.model.L2ManufactureItem
import l2server.gameserver.model.L2ManufactureList
import l2server.gameserver.model.World
import l2server.gameserver.model.actor.instance.Player
import l2server.gameserver.network.L2GameClient
import l2server.gameserver.network.L2GameClient.GameClientState
import l2server.util.loader.annotations.Load
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.util.*

object OfflineTradersTable {

    private val log = LoggerFactory.getLogger(OfflineTradersTable::class.java)

    //SQL DEFINITIONS
    private const val SAVE_OFFLINE_STATUS = "INSERT INTO character_offline_trade (`charId`,`time`,`type`,`title`) VALUES (?,?,?,?)"
    private const val SAVE_ITEMS = "INSERT INTO character_offline_trade_items (`charId`,`item`,`count`,`price`) VALUES (?,?,?,?)"
    private const val SAVE_PRICES = "INSERT INTO character_offline_trade_item_prices (`charId`,`item`,`priceId`,`count`) VALUES (?,?,?,?)"
    private const val CLEAR_OFFLINE_TABLE = "DELETE FROM character_offline_trade"
    private const val CLEAR_OFFLINE_TABLE_ITEMS = "DELETE FROM character_offline_trade_items"
    private const val CLEAR_OFFLINE_TABLE_PRICES = "DELETE FROM character_offline_trade_item_prices"
    private const val LOAD_OFFLINE_STATUS = "SELECT * FROM character_offline_trade"
    private const val LOAD_OFFLINE_ITEMS = "SELECT * FROM character_offline_trade_items WHERE charId = ?"
    private const val LOAD_OFFLINE_PRICES = "SELECT * FROM character_offline_trade_item_prices WHERE charId = ? AND item = ?"

    fun storeOffliners() {
        var con: Connection? = null
        try {
            con = L2DatabaseFactory.getInstance().connection
            var stm = con!!.prepareStatement(CLEAR_OFFLINE_TABLE)
            stm.execute()
            stm.close()
            stm = con.prepareStatement(CLEAR_OFFLINE_TABLE_ITEMS)
            stm.execute()
            stm.close()
            stm = con.prepareStatement(CLEAR_OFFLINE_TABLE_PRICES)
            stm.execute()
            stm.close()

            stm = con.prepareStatement(SAVE_OFFLINE_STATUS)
            val stm_items = con.prepareStatement(SAVE_ITEMS)
            val stm_prices = con.prepareStatement(SAVE_PRICES)

            //StringBuilder items = StringBuilder.newInstance();
            val checkInactiveStores = Config.isServer(Config.TENKAI) && System.currentTimeMillis() - Server.dateTimeServerStarted.timeInMillis > 36000000
            loop@for (pc in World.getInstance().allPlayers.values) {
                try {
                    if (pc.privateStoreType != Player.STORE_PRIVATE_NONE && (pc.client == null || pc.client.isDetached) &&
                            (!checkInactiveStores || pc.hadStoreActivity())) {
                        stm.setInt(1, pc.objectId) //Char Id
                        stm.setLong(2, pc.offlineStartTime)
                        stm.setInt(3, pc.privateStoreType) //store type
                        var title: String? = null

                        when (pc.privateStoreType) {
                            Player.STORE_PRIVATE_BUY -> {
                                if (!Config.OFFLINE_TRADE_ENABLE) {
                                    continue@loop
                                }
                                title = pc.buyList.title
                                for (i in pc.buyList.items) {
                                    stm_items.setInt(1, pc.objectId)
                                    stm_items.setInt(2, i.item.itemId)
                                    stm_items.setLong(3, i.count)
                                    stm_items.setLong(4, i.price)
                                    stm_items.executeUpdate()
                                    stm_items.clearParameters()
                                }
                            }
                            Player.STORE_PRIVATE_SELL, Player.STORE_PRIVATE_PACKAGE_SELL -> {
                                if (!Config.OFFLINE_TRADE_ENABLE) {
                                    continue@loop
                                }
                                title = pc.sellList.title
                                for (i in pc.sellList.items) {
                                    stm_items.setInt(1, pc.objectId)
                                    stm_items.setInt(2, i.objectId)
                                    stm_items.setLong(3, i.count)
                                    stm_items.setLong(4, i.price)
                                    stm_items.executeUpdate()
                                    stm_items.clearParameters()
                                }
                            }
                            Player.STORE_PRIVATE_MANUFACTURE -> {
                                if (!Config.OFFLINE_CRAFT_ENABLE) {
                                    continue@loop
                                }
                                title = pc.createList.storeName
                                for (i in pc.createList.list) {
                                    stm_items.setInt(1, pc.objectId)
                                    stm_items.setInt(2, i.recipeId)
                                    stm_items.setLong(3, 0)
                                    stm_items.setLong(4, i.cost)
                                    stm_items.executeUpdate()
                                    stm_items.clearParameters()
                                }
                            }
                            Player.STORE_PRIVATE_CUSTOM_SELL -> {
                                if (!Config.OFFLINE_TRADE_ENABLE) {
                                    continue@loop
                                }
                                title = pc.customSellList.title
                                for (i in pc.customSellList.items) {
                                    stm_items.setInt(1, pc.objectId)
                                    stm_items.setInt(2, i.objectId)
                                    stm_items.setLong(3, i.count)
                                    stm_items.setLong(4, 0)
                                    stm_items.executeUpdate()
                                    stm_items.clearParameters()

                                    for (priceItem in i.priceItems.keys) {
                                        val count = i.priceItems[priceItem]!!
                                        stm_prices.setInt(1, pc.objectId)
                                        stm_prices.setInt(2, i.objectId)
                                        stm_prices.setInt(3, priceItem.itemId)
                                        stm_prices.setLong(4, count)
                                        stm_prices.executeUpdate()
                                        stm_prices.clearParameters()
                                    }
                                }
                            }
                        }
                        stm.setString(4, title)
                        stm.executeUpdate()
                        stm.clearParameters()
                    }
                } catch (e: Exception) {
                    log.warn(
                            "OfflineTradersTable[storeTradeItems()]: Error while saving offline trader: " + pc.objectId + " " + e,
                            e)
                }

            }
            stm.close()
            stm_items.close()
            stm_prices.close()
            log.info("Offline traders stored.")
        } catch (e: Exception) {
            log.warn("OfflineTradersTable[storeTradeItems()]: Error while saving offline traders: $e", e)
        } finally {
            L2DatabaseFactory.close(con)
        }
    }

    @Load(dependencies = [World::class, ItemTable::class])
    fun restoreOfflineTraders() {
        if (!(((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS))) {
            return
        }

        log.info("Loading offline traders...")

        val restoreThread = Thread {
            var con: Connection? = null
            var nTraders = 0

            val startTime = System.nanoTime()
            try {
                con = L2DatabaseFactory.getInstance().connection
                var stm = con!!.prepareStatement(LOAD_OFFLINE_STATUS)
                val rs = stm.executeQuery()
                while (rs.next()) {
                    val time = rs.getLong("time")
                    if (Config.OFFLINE_MAX_DAYS > 0) {
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = time
                        cal.add(Calendar.DAY_OF_YEAR, Config.OFFLINE_MAX_DAYS)
                        if (cal.timeInMillis <= System.currentTimeMillis()) {
                            continue
                        }
                    }

                    val type = rs.getInt("type")
                    if (type == Player.STORE_PRIVATE_NONE) {
                        continue
                    }

                    var player: Player? = null

                    try {
                        val client = L2GameClient(null)
                        client.isDetached = true
                        player = Player.load(rs.getInt("charId"))
                        client.activeChar = player
                        player!!.setOnlineStatus(true, false)
                        client.accountName = player.accountNamePlayer
                        client.state = GameClientState.IN_GAME
                        player.client = client
                        player.offlineStartTime = time
                        player.spawnMe(player.x, player.y, player.z)
                        LoginServerThread.getInstance().addGameServerLogin(player.accountName, client)
                        val stm_items = con.prepareStatement(LOAD_OFFLINE_ITEMS)
                        stm_items.setInt(1, player.objectId)
                        val items = stm_items.executeQuery()

                        when (type) {
                            Player.STORE_PRIVATE_BUY -> {
                                while (items.next()) {
                                    if (player.buyList.addItemByItemId(items.getInt(2), items.getLong(3), items.getLong(4)) == null) {
                                        throw NullPointerException()
                                    }
                                }
                                player.buyList.title = rs.getString("title")
                            }
                            Player.STORE_PRIVATE_SELL, Player.STORE_PRIVATE_PACKAGE_SELL -> {
                                while (items.next()) {
                                    if (player.sellList.addItem(items.getInt(2), items.getLong(3), items.getLong(4)) == null) {
                                        throw NullPointerException()
                                    }
                                }
                                player.sellList.title = rs.getString("title")
                                player.sellList.isPackaged = type == Player.STORE_PRIVATE_PACKAGE_SELL
                            }
                            Player.STORE_PRIVATE_MANUFACTURE -> {
                                val createList = L2ManufactureList()
                                while (items.next()) {
                                    createList.add(L2ManufactureItem(items.getInt(2), items.getLong(4)))
                                }
                                player.createList = createList
                                player.createList.storeName = rs.getString("title")
                            }
                            Player.STORE_PRIVATE_CUSTOM_SELL -> {
                                while (items.next()) {
                                    val item = player.customSellList.addItem(items.getInt(2), items.getLong(3))
                                            ?: throw NullPointerException()

                                    val stm_prices = con.prepareStatement(LOAD_OFFLINE_PRICES)
                                    stm_prices.setInt(1, player.objectId)
                                    stm_prices.setInt(2, items.getInt(2))
                                    val prices = stm_prices.executeQuery()
                                    while (prices.next()) {
                                        val i = ItemTable.getInstance().getTemplate(prices.getInt("priceId"))
                                                ?: throw NullPointerException()

                                        item.priceItems[i] = prices.getLong("count")
                                    }
                                }
                                player.customSellList.title = rs.getString("title")
                            }
                        }
                        items.close()
                        stm_items.close()

                        player.sitDown()
                        if (Config.OFFLINE_SET_NAME_COLOR) {
                            player.appearance.nameColor = Config.OFFLINE_NAME_COLOR
                        }
                        player.privateStoreType = type
                        player.setOnlineStatus(true, true)
                        player.restoreEffects()
                        player.broadcastUserInfo()

                        player.setIsInvul(true)

                        nTraders++
                    } catch (e: Exception) {
                        log.warn("OfflineTradersTable[loadOffliners()]: Error loading trader: " + player!!, e)
                        player.deleteMe()
                    }

                }
                rs.close()
                stm.close()
                stm = con.prepareStatement(CLEAR_OFFLINE_TABLE)
                stm.execute()
                stm.close()
                stm = con.prepareStatement(CLEAR_OFFLINE_TABLE_ITEMS)
                stm.execute()
                stm.close()
            } catch (e: Exception) {
                log.warn("OfflineTradersTable[loadOffliners()]: Error while loading offline traders: ", e)
                e.printStackTrace()
            } finally {
                L2DatabaseFactory.close(con)
            }

            val finishTime = System.nanoTime()
            log.info("Asynch restoring of " + nTraders + " offline traders took " + (finishTime - startTime) / 1000000 + " ms")
        }
        restoreThread.priority = Thread.MIN_PRIORITY
        restoreThread.name = "restoreOfflineTraders"
        restoreThread.start()
    }
}
