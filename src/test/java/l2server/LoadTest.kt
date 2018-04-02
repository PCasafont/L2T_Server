package l2server

import l2server.gameserver.GameApplication
import l2server.gameserver.GameThreadPools
import l2server.gameserver.ThreadPoolManager
import l2server.gameserver.idfactory.IdFactory
import l2server.util.concurrent.ThreadPool
import l2server.util.loader.Loader
import org.junit.Test
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.measureTimeMillis


/**
 * @author Pere
 */
class LoadTest {

    private val log = LoggerFactory.getLogger(GameApplication::class.java.name)

    private fun initializeServer() {
        ServerMode.serverMode = ServerMode.MODE_GAMESERVER

        // Initialize config
        Config.load()

        ThreadPool.initThreadPools(GameThreadPools())

        L2DatabaseFactory.getInstance()

        val idFactory = IdFactory.getInstance()

        if (!idFactory.isInitialized()) {
            log.error("Could not read object IDs from DB. Please Check Your Data.")
            throw Exception("Could not initialize the ID factory")
        }

        ThreadPoolManager.getInstance()

        File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "crests").mkdirs()
        File("log/game").mkdirs()

        Loader.initialize(GameApplication::class.java.`package`.name)
    }

    @Test
    fun testFullLoad() {
        val executionTime = measureTimeMillis {
            initializeServer()
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
