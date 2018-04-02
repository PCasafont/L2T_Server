package l2server

import l2server.gameserver.GameApplication
import l2server.gameserver.GameThreadPools
import l2server.gameserver.ThreadPoolManager
import l2server.gameserver.idfactory.IdFactory
import l2server.log.Log
import l2server.util.concurrent.ThreadPool
import l2server.util.loader.Loader
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.util.logging.LogManager
import kotlin.system.measureTimeMillis

/**
 * @author Pere
 */
class LoadTest {

    private fun initializeServer() {
        ServerMode.serverMode = ServerMode.MODE_GAMESERVER
        // Local Constants
        val LOG_FOLDER = "log" // Name of folder for log file
        val LOG_NAME = "./log.cfg" // Name of log file

        /* Main */
        // Create log folder
        val logFolder = File(Config.DATAPACK_ROOT, LOG_FOLDER)
        logFolder.mkdir()

        // Create input stream for log file -- or store file data into memory
        val inputStream = FileInputStream(File(LOG_NAME))
        LogManager.getLogManager().readConfiguration(inputStream)
        inputStream.close()

        // Initialize config
        Config.load()

        ThreadPool.initThreadPools(GameThreadPools())

        L2DatabaseFactory.getInstance()

        val idFactory = IdFactory.getInstance()

        if (!idFactory.isInitialized()) {
            Log.severe("Could not read object IDs from DB. Please Check Your Data.")
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
