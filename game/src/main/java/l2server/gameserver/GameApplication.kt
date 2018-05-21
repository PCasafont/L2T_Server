package l2server.gameserver

import l2server.Config
import l2server.DatabasePool
import l2server.ServerMode
import l2server.gameserver.api.startApiServer
import l2server.gameserver.geoeditorcon.GeoEditorListener
import l2server.gameserver.gui.ServerGui
import l2server.gameserver.idfactory.IdFactory
import l2server.gameserver.network.L2GameClient
import l2server.gameserver.network.L2GamePacketHandler
import l2server.gameserver.pathfinding.PathFinding
import l2server.gameserver.util.DeadLockDetector
import l2server.network.Core
import l2server.network.CoreConfig
import l2server.util.IPv4Filter
import l2server.util.concurrent.ThreadPool
import l2server.util.loader.Loader
import org.slf4j.LoggerFactory
import java.awt.Toolkit
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*
import javax.swing.UIManager

val dateTimeServerStarted = Calendar.getInstance()

lateinit var gui: ServerGui
lateinit var selectorThread: Core<L2GameClient>
private lateinit var gamePacketHandler: L2GamePacketHandler
private lateinit var deadDetectThread: DeadLockDetector
private lateinit var loginThread: LoginServerThread

fun main(args: Array<String>) {
	val serverLoadStart = System.currentTimeMillis()
    val log = LoggerFactory.getLogger("GameApplication")

    ServerMode.serverMode = ServerMode.MODE_GAMESERVER

    // Initialize config
    Config.load()

    ThreadPool.initThreadPools(GameThreadPools())

	DatabasePool.getInstance()

    val idFactory = IdFactory.getInstance()
    if (!idFactory.isInitialized) {
        log.error("Could not read object IDs from DB. Please Check Your Data.")
        throw Exception("Could not initialize the ID factory")
    }

    ThreadPoolManager.getInstance()

    File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "crests").mkdirs()

	Loader.initialize("l2server.gameserver")
	Loader.run()

	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
	gui = ServerGui()
	gui.init()

	GeoData.getInstance()
	if (Config.GEODATA == 2) {
		PathFinding.getInstance()
	}

	if (Config.ACCEPT_GEOEDITOR_CONN) {
		GeoEditorListener.getInstance()
	}

	//Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());

	log.info("Free ObjectID's remaining: " + IdFactory.getInstance().size())

	if (Config.DEADLOCK_DETECTOR) {
		val deadDetectThread = DeadLockDetector()
		deadDetectThread.isDaemon = true
		deadDetectThread.start()
	}

	//LameGuard.getInstance();
	System.gc()
	// maxMemory is the upper limit the jvm can use, totalMemory the size of
	// the current allocation pool, freeMemory the unused memory in the
	// allocation pool
	val freeMem = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / 1048576
	val totalMem = Runtime.getRuntime().maxMemory() / 1048576
	log.info("GameServer Started, free memory $freeMem Mb of $totalMem Mb")
	Toolkit.getDefaultToolkit().beep()

	val sc = CoreConfig()
	sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS
	sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS
	sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME
	sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT

	val gamePacketHandler = L2GamePacketHandler()
	val selectorThread = Core<L2GameClient>(sc, gamePacketHandler, gamePacketHandler, gamePacketHandler, IPv4Filter())

	var bindAddress: InetAddress? = null
	if (Config.GAMESERVER_HOSTNAME != "*") {
		try {
			bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME)
		} catch (e1: UnknownHostException) {
			log.error("WARNING: The GameServer bind address is invalid, using all avaliable IPs. Reason: " + e1.message, e1)
		}

	}

	try {
		selectorThread.openServerSocket(bindAddress, Config.PORT_GAME)
	} catch (e: IOException) {
		log.error("FATAL: Failed to open server socket. Reason: " + e.message, e)
		System.exit(1)
	}

	selectorThread.start()
	log.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS)

	Runtime.getRuntime().addShutdownHook(Shutdown.getInstance())

	startApiServer()

	val serverLoadEnd = System.currentTimeMillis()
	log.info("Server Loaded in " + (serverLoadEnd - serverLoadStart) / 1000 + " seconds")
}
