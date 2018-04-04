package l2server.datatables

import l2server.LoadTest
import l2server.gameserver.datatables.SkillTable
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
