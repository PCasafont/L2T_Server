package l2server.gameserver.datatables

import l2server.gameserver.LoadTest
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
