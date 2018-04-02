package l2server.datatables

import l2server.LoadTest
import l2server.gameserver.datatables.ItemTable
import org.junit.Test


/**
 * @author Pere
 */
class ItemTableTest : LoadTest() {

	@Test
	fun testLoad() {
		initializeServer()
		ItemTable.getInstance().load()
	}
}
