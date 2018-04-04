package l2server.datatables

import l2server.LoadTest
import l2server.gameserver.datatables.CompoundTable
import org.junit.Test


/**
 * @author Pere
 */
class CompoundTableTest : LoadTest() {

	@Test
	fun testLoad() {
		initializeServer()
		CompoundTable.getInstance().load()
	}
}
