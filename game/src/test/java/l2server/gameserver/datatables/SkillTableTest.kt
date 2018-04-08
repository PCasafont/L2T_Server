package l2server.gameserver.datatables

import l2server.gameserver.LoadTest
import org.junit.Test


/**
 * @author Pere
 */
class SkillTableTest : LoadTest() {

	@Test
	fun testLoad() {
		initializeServer()
		SkillTable.getInstance().load()
	}
}
