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

package l2server.gameserver

import l2server.Config
import l2server.L2DatabaseFactory
import l2server.gameserver.datatables.ItemTable
import l2server.gameserver.datatables.NpcTable
import l2server.gameserver.model.L2TradeList
import l2server.gameserver.model.L2TradeList.L2TradeItem
import l2server.util.loader.annotations.Load
import l2server.util.loader.annotations.Reload
import l2server.util.xml.XmlDocument
import org.slf4j.LoggerFactory
import java.io.File
import java.sql.Connection
import java.util.*

object TradeController {

    private val log = LoggerFactory.getLogger(TradeController::class.java)

    private val lists = HashMap<Int, L2TradeList>()

    @Reload("shops")
    @Load(dependencies = [NpcTable::class])
    fun load() {
        lists.clear()
        load(Config.DATAPACK_ROOT.toString() + "/data_" + Config.SERVER_NAME + "/shops/")
        load(Config.DATAPACK_ROOT.toString() + "/" + Config.DATA_FOLDER + "/shops/")
        loadItemCounts()
    }

    fun load(path: String) {
        val dir = File(path)
        if (!dir.exists()) {
            return
        }

        for (file in dir.listFiles()!!) {
            if (!file.name.endsWith(".xml")) {
                continue
            }

            val doc = XmlDocument(file)
            for (d in doc.getChildren()) {
                if (d.name.equals("shop", ignoreCase = true)) {
                    val id = d.getInt("id")
                    val npcId = d.getInt("npcId")

                    if (lists.containsKey(id)) {
                        continue
                    }

                    val buy = L2TradeList(id)

                    val npcTemplate = NpcTable.getInstance().getTemplate(npcId)
                    if (npcTemplate == null) {
                        if (npcId != -1) {
                            log.warn("No template found for NpcId $npcId")
                        }

                        continue
                    }

                    for (shopNode in d.getChildren()) {
                        if (shopNode.name.equals("item", ignoreCase = true)) {
                            val itemId = shopNode.getInt("id")

                            val itemTemplate = ItemTable.getInstance().getTemplate(itemId)
                            if (itemTemplate == null) {
                                log.warn("Skipping itemId: $itemId on buylistId: $id, missing data for that item.")
                                continue
                            }

                            val item = L2TradeItem(id, itemId)
                            var price = shopNode.getLong("price", -1)
                            val count = shopNode.getInt("count", -1)
                            val time = shopNode.getInt("time", 0)
                            if (price <= -1) {
                                price = itemTemplate.referencePrice.toLong()
                                if (price == 0L && npcId != -1) {
                                    log.warn("ItemId: $itemId on buylistId: $id has price = 0!")
                                }
                            }

                            if (Config.DEBUG) {
                                // debug
                                val diff = price.toDouble() / ItemTable.getInstance().getTemplate(itemId)!!.referencePrice
                                if (diff < 0.8 || diff > 1.2) {
                                    log.error("PRICING DEBUG: TradeListId: " + id + " -  ItemId: " + itemId + " (" +
                                            ItemTable.getInstance().getTemplate(itemId)!!.name + ") diff: " + diff + " - Price: " + price +
                                            " - Reference: " + ItemTable.getInstance().getTemplate(itemId)!!.referencePrice)
                                }
                            }

                            item.price = price

                            item.setRestoreDelay(time)
                            item.maxCount = count

                            buy.addItem(item)

                            itemTemplate.salePrice = 0
                        }
                    }

                    buy.npcId = npcId
                    lists[id] = buy
                }
            }
        }

        log.info("TradeController: Loaded " + lists.size + " Buylists.")
    }

    private fun loadItemCounts() {
        var con: Connection? = null
        try {
            con = L2DatabaseFactory.getInstance().connection
            val statement = con!!.prepareStatement("SELECT * FROM shop_item_counts")
            val rset = statement.executeQuery()

            while (rset.next()) {
                val shopId = rset.getInt("shop_id")
                val itemId = rset.getInt("item_id")
                val currentCount = rset.getInt("count")
                val savedTime = rset.getLong("time")

                val tradeList = lists[shopId] ?: continue

                val item = tradeList.getItemById(itemId)

                if (currentCount > -1) {
                    item.currentCount = currentCount.toLong()
                } else {
                    item.currentCount = item.maxCount.toLong()
                    val st = con.prepareStatement("DELETE FROM shop_item_counts WHERE shop_id = ? AND item_id = ?")
                    st.setInt(1, shopId)
                    st.setInt(2, itemId)
                    st.executeUpdate()
                    continue
                }

                item.setNextRestoreTime(savedTime)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                con!!.close()
            } catch (e2: Exception) {
                e2.printStackTrace()
            }

        }
    }

    fun getBuyList(listId: Int): L2TradeList? {
        return lists[listId]
    }

    fun getBuyListByNpcId(npcId: Int): List<L2TradeList> {
        val lists = ArrayList<L2TradeList>()
        val values = this.lists.values

        for (list in values) {
            val tradeNpcId = list.npcId
            if (tradeNpcId == -1) {
                continue
            }
            if (npcId == tradeNpcId) {
                lists.add(list)
            }
        }
        return lists
    }

    fun dataCountStore() {
        var con: Connection? = null
        var listId: Int

        try {
            con = L2DatabaseFactory.getInstance().connection
            val statement = con!!.prepareStatement("UPDATE shop_item_counts SET count = ? WHERE shop_id = ? AND item_id = ?")
            for (list in lists.values) {
                if (list.hasLimitedStockItem()) {
                    listId = list.listId

                    for (item in list.items) {
                        val currentCount = item.currentCount
                        if (item.hasLimitedStock() && currentCount < item.maxCount) {
                            statement.setLong(1, currentCount)
                            statement.setInt(2, listId)
                            statement.setInt(3, item.itemId)
                            statement.executeUpdate()
                            statement.clearParameters()
                        }
                    }
                }
            }
            statement.close()
        } catch (e: Exception) {
            log.error("TradeController: Could not store Count Item: " + e.message, e)
        } finally {
            L2DatabaseFactory.close(con)
        }
    }
}
