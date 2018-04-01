package l2server.gameserver

import l2server.Config
import l2server.L2DatabaseFactory
import l2server.ServerMode
import l2server.gameserver.Server.gameServer
import l2server.gameserver.gui.ServerGui
import l2server.gameserver.idfactory.IdFactory
import l2server.gameserver.script.EngineInterface.idFactory
import l2server.log.Log
import l2server.util.loader.Loader
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File
import java.io.FileInputStream
import java.util.logging.LogManager
import javax.swing.UIManager

@SpringBootApplication
class GameApplication

fun main(args: Array<String>) {
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

    L2DatabaseFactory.getInstance()

    //SqlToXml.races();
    //SqlToXml.classes();
    //SqlToXml.shops();
    //SqlToXml.customShops();
    //SqlToXml.enchantSkillGroups();
    //SqlToXml.armorSets();
    //SqlToXml.henna();
    //SqlToXml.fortSpawns();

    val idFactory = IdFactory.getInstance()

    if (!idFactory.isInitialized()) {
        Log.severe("Could not read object IDs from DB. Please Check Your Data.")
        throw Exception("Could not initialize the ID factory")
    }

    ThreadPoolManager.getInstance()

    File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "crests").mkdirs()
    File("log/game").mkdirs()

	Loader.initialize(GameApplication::class.java.`package`.name)
	Loader.run()
	Log.info(Loader.getDependencyTreeString())

	// FIXME I'M DIRTY! Use spring to initialize the server
	Server()
	// Run spring application
	runApplication<GameApplication>(*args)
}
