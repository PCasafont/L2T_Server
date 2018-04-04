package l2server

import l2server.gameserver.GameApplication
import l2server.util.loader.Loader
import org.junit.Ignore
import org.junit.Test
import kotlin.system.measureTimeMillis


/**
 * @author Pere
 */
class ServerTest : LoadTest() {

	@Ignore
	@Test
	fun testFullLoad() {
		val executionTime = measureTimeMillis {
			initializeServer()
			Loader.initialize(GameApplication::class.java.`package`.name)
			//Loader.run()
			Loader.runAsync().join()
		}
		println(Loader.getDependencyTreeString())
		println("Loading took $executionTime ms")
	}

	//@Test
	//public void testSkillLoading() {
	//	initializeServer();
	//	SkillTable.getInstance().load();
	//}
	//
	//@Test
	//public void testItemLoading() {
	//	initializeServer();
	//	ItemTable.getInstance().load();
	//}
}
