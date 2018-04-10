package l2server.gameserver.datatables

import l2server.gameserver.LoadTest
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
