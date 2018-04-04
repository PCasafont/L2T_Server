/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.scripting

import com.l2jserver.script.jython.JythonScriptEngine
import l2server.Config
import l2server.gameserver.datatables.DoorTable
import l2server.gameserver.datatables.SpawnTable
import l2server.gameserver.instancemanager.QuestManager
import l2server.gameserver.instancemanager.TransformationManager
import l2server.util.loader.annotations.Load
import org.slf4j.LoggerFactory
import java.io.*
import java.util.*
import javax.script.*

/**
 * Caches script engines and provides funcionality for executing and managing scripts.<BR></BR>
 *
 * @author KenM
 */
object L2ScriptEngineManager {

    private val log = LoggerFactory.getLogger(L2ScriptEngineManager::class.java)

    @JvmStatic
    val SCRIPT_FOLDER = File(Config.DATAPACK_ROOT.absolutePath, Config.DATA_FOLDER + "scripts")

    private val nameEngines = HashMap<String, ScriptEngine>()
    private val extEngines = HashMap<String, ScriptEngine>()
    private val scriptManagers = LinkedList<ScriptManager<*>>()

    var cache: CompiledScriptCache? = null

    /**
     * @return Returns the currentLoadingScript.
     */
    /**
     * @param currentLoadingScript The currentLoadingScript to set.
     */
    var currentLoadingScript: File? = null
        private set

    // Configs
    // TODO move to config file
    /**
     * Informs(logs) the scripts being loaded.<BR></BR>
     * Apply only when executing script from files.<BR></BR>
     */
    private val VERBOSE_LOADING = false

    /**
     * If the script engine supports compilation the script is compiled before execution.<BR></BR>
     */
    private val ATTEMPT_COMPILATION = true

    /**
     * Use Compiled Scripts Cache.<BR></BR>
     * Only works if ATTEMPT_COMPILATION is true.<BR></BR>
     * DISABLED DUE TO ISSUES (if a superclass file changes subclasses are not recompiled while they should)
     */
    private val USE_COMPILED_CACHE = true

    /**
     * Clean an previous error log(if such exists) for the script being loaded before trying to load.<BR></BR>
     * Apply only when executing script from files.<BR></BR>
     */
    private val PURGE_ERROR_LOG = true

    @Load(dependencies = [SpawnTable::class, DoorTable::class])
    fun initialize() {
        val scriptEngineManager = ScriptEngineManager()
        val factories = scriptEngineManager.engineFactories
        if (USE_COMPILED_CACHE) {
            cache = loadCompiledScriptCache()
        } else {
            cache = null
        }
        log.info("Initializing Script Engine Manager")

        for (factory in factories) {
            try {
                val engine = factory.scriptEngine
                var reg = false
                for (name in factory.names) {
                    val existentEngine = nameEngines[name]

                    if (existentEngine != null) {
                        val engineVer = java.lang.Double.parseDouble(factory.engineVersion)
                        val existentEngVer = java.lang.Double.parseDouble(existentEngine.factory.engineVersion)

                        if (engineVer <= existentEngVer) {
                            continue
                        }
                    }

                    reg = true
                    nameEngines[name] = engine
                }

                if (reg) {
                    log.info("Script Engine: " + factory.engineName + " " + factory.engineVersion + " - Language: " +
                            factory.languageName + " - Language Version: " + factory.languageVersion)
                }

                for (ext in factory.extensions) {
                    if (ext != "java" || factory.languageName == "java") {
                        extEngines[ext] = engine
                    }
                }
            } catch (e: Exception) {
                log.warn("Failed initializing factory: " + e.message, e)
            }

        }

        preConfigure()

        try {
            log.info("Loading Server Scripts")
            var scripts = File(Config.DATAPACK_ROOT.toString() + "/" + Config.DATA_FOLDER + "scripts.cfg")
            if (!Config.ALT_DEV_NO_HANDLERS || !Config.ALT_DEV_NO_QUESTS) {
                L2ScriptEngineManager.executeScriptList(scripts)

                scripts = File(Config.DATAPACK_ROOT.toString() + "/data_" + Config.SERVER_NAME + "/scripts.cfg")
                if (scripts.exists()) {
                    L2ScriptEngineManager.executeScriptList(scripts)
                }
            }
        } catch (ioe: IOException) {
            log.error("Failed loading scripts.cfg, no script going to be loaded")
        }

        try {
            val compiledScriptCache = L2ScriptEngineManager.cache
            if (compiledScriptCache == null) {
                log.info("Compiled Scripts Cache is disabled.")
            } else {
                compiledScriptCache.purge()

                if (compiledScriptCache.isModified) {
                    compiledScriptCache.save()
                    log.info("Compiled Scripts Cache was saved.")
                } else {
                    log.info("Compiled Scripts Cache is up-to-date.")
                }
            }
        } catch (e: IOException) {
            log.error("Failed to store Compiled Scripts Cache.", e)
        }

        QuestManager.getInstance().report()
        TransformationManager.getInstance().report()
    }

    private fun preConfigure() {
        // java class path

        // Jython sys.path
        val dataPackDirForwardSlashes = SCRIPT_FOLDER.path.replace("\\\\".toRegex(), "/")
        val configScript = "import sys;sys.path.insert(0,'$dataPackDirForwardSlashes');"
        try {
            this.eval("jython", configScript)
        } catch (e: ScriptException) {
            log.error("Failed preconfiguring jython: " + e.message)
        }

    }

    private fun getEngineByName(name: String): ScriptEngine? {
        return nameEngines[name]
    }

    private fun getEngineByExtension(ext: String): ScriptEngine? {
        return extEngines[ext]
    }

    @Throws(IOException::class)
    fun executeScriptList(list: File) {
        var file: File

        if (!Config.ALT_DEV_NO_HANDLERS && Config.ALT_DEV_NO_QUESTS) {
            file = File(SCRIPT_FOLDER, "handlers/MasterHandler.java")

            try {
                executeScript(file)
                log.info("Handlers loaded, all other scripts skipped")
                return
            } catch (se: ScriptException) {
                log.warn("", se)
            }

        }

        if (Config.ALT_DEV_NO_QUESTS) {
            return
        }

        if (list.isFile) {
            val lnr = LineNumberReader(InputStreamReader(FileInputStream(list)))
            var line = lnr.readLine()
            while (line != null) {
                if (Config.ALT_DEV_NO_HANDLERS && line.contains("MasterHandler.java")) {
                    continue
                }

                val parts = line.trim { it <= ' ' }.split("#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                if (parts.size > 0 && !parts[0].startsWith("#") && parts[0].length > 0) {
                    line = parts[0]

                    if (line.endsWith("/**")) {
                        line = line.substring(0, line.length - 3)
                    } else if (line.endsWith("/*")) {
                        line = line.substring(0, line.length - 2)
                    }

                    file = File(SCRIPT_FOLDER, line)

                    if (file.isDirectory && parts[0].endsWith("/**")) {
                        executeAllScriptsInDirectory(file, true, 32)
                    } else if (file.isDirectory && parts[0].endsWith("/*")) {
                        executeAllScriptsInDirectory(file)
                    } else if (file.isFile) {
                        try {
                            executeScript(file)
                        } catch (e: ScriptException) {
                            reportScriptFileError(file, e)
                        }

                    } else {
                        log.warn("Failed loading: (" + file.canonicalPath + ") @ " + list.name + ":" + lnr.lineNumber +
                                " - Reason: it doesn't exist or it's not a file.")
                    }
                }

                line = lnr.readLine()
            }
            lnr.close()
        } else {
            throw IllegalArgumentException("Argument must be an file containing a list of scripts to be loaded")
        }
    }

    fun executeAllScriptsInDirectory(dir: File) {
        this.executeAllScriptsInDirectory(dir, false, 0)
    }

    fun executeAllScriptsInDirectory(dir: File, recurseDown: Boolean, maxDepth: Int) {
        this.executeAllScriptsInDirectory(dir, recurseDown, maxDepth, 0)
    }

    private fun executeAllScriptsInDirectory(dir: File, recurseDown: Boolean, maxDepth: Int, currentDepth: Int) {
        if (dir.isDirectory) {
            for (file in dir.listFiles()!!) {
                if (file.isDirectory && recurseDown && maxDepth > currentDepth) {
                    if (VERBOSE_LOADING) {
                        log.info("Entering folder: " + file.name)
                    }
                    executeAllScriptsInDirectory(file, recurseDown, maxDepth, currentDepth + 1)
                } else if (file.isFile) {
                    try {
                        val name = file.name
                        val lastIndex = name.lastIndexOf('.')
                        val extension: String
                        if (lastIndex != -1) {
                            extension = name.substring(lastIndex + 1)
                            val engine = getEngineByExtension(extension)
                            if (engine != null) {
                                executeScript(engine, file)
                            }
                        }
                    } catch (e: FileNotFoundException) {
                        // should never happen
                        log.warn("", e)
                    } catch (e: ScriptException) {
                        reportScriptFileError(file, e)
                        //Logozo.warn( "", e);
                    }

                }
            }
        } else {
            throw IllegalArgumentException("The argument directory either doesn't exist or it's not a directory.")
        }
    }

    fun loadCompiledScriptCache(): CompiledScriptCache? {
        if (USE_COMPILED_CACHE) {
            val file = File(SCRIPT_FOLDER, "CompiledScripts.cache")
            if (file.isFile) {
                var ois: ObjectInputStream? = null
                try {
                    ois = ObjectInputStream(FileInputStream(file))
                    val cache = ois.readObject() as CompiledScriptCache
                    cache.checkFiles()
                    return cache
                } catch (e: InvalidClassException) {
                    log.error("Failed loading Compiled Scripts Cache, invalid class (Possibly outdated).", e)
                } catch (e: IOException) {
                    log.error("Failed loading Compiled Scripts Cache from file.", e)
                } catch (e: ClassNotFoundException) {
                    log.error("Failed loading Compiled Scripts Cache, class not found.", e)
                } finally {
                    try {
                        ois!!.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
                return CompiledScriptCache()
            } else {
                return CompiledScriptCache()
            }
        }
        return null
    }

    @Throws(ScriptException::class, FileNotFoundException::class)
    fun executeScript(file: File) {
        val name = file.name
        val lastIndex = name.lastIndexOf('.')
        val extension: String
        if (lastIndex != -1) {
            extension = name.substring(lastIndex + 1)
        } else {
            throw ScriptException("Script file ($name) doesnt has an extension that identifies the ScriptEngine to be used.")
        }

        val engine = getEngineByExtension(extension)
        if (engine == null) {
            throw ScriptException("No engine registered for extension ($extension)")
        } else {
            executeScript(engine, file)
        }
    }

    @Throws(FileNotFoundException::class, ScriptException::class)
    fun executeScript(engineName: String, file: File) {
        val engine = getEngineByName(engineName)
        if (engine == null) {
            throw ScriptException("No engine registered with name ($engineName)")
        } else {
            executeScript(engine, file)
        }
    }

    @Throws(FileNotFoundException::class, ScriptException::class)
    fun executeScript(engine: ScriptEngine, file: File) {
        val reader = BufferedReader(InputStreamReader(FileInputStream(file)))

        if (VERBOSE_LOADING) {
            log.info("Loading Script: " + file.absolutePath)
        }

        if (PURGE_ERROR_LOG) {
            val name = file.absolutePath + ".error.log"
            val errorLog = File(name)
            if (errorLog.isFile) {
                errorLog.delete()
            }
        }

        if (engine is Compilable && ATTEMPT_COMPILATION) {
            val context = SimpleScriptContext()
            context.setAttribute("mainClass", getClassForFile(file)!!.replace('/', '.').replace('\\', '.'), ScriptContext.ENGINE_SCOPE)
            context.setAttribute(ScriptEngine.FILENAME, file.name, ScriptContext.ENGINE_SCOPE)
            context.setAttribute("classpath", SCRIPT_FOLDER.absolutePath, ScriptContext.ENGINE_SCOPE)
            context.setAttribute("sourcepath", SCRIPT_FOLDER.absolutePath, ScriptContext.ENGINE_SCOPE)
            context.setAttribute(JythonScriptEngine.JYTHON_ENGINE_INSTANCE, engine, ScriptContext.ENGINE_SCOPE)

            currentLoadingScript = file
            val ctx = engine.context
            try {
                engine.context = context
                if (USE_COMPILED_CACHE) {
                    val cs = cache!!.loadCompiledScript(engine, file)
                    cs.eval(context)
                } else {
                    val eng = engine as Compilable
                    val cs = eng.compile(reader)
                    cs.eval(context)
                }
            } finally {
                engine.context = ctx
                currentLoadingScript = null
                context.removeAttribute(ScriptEngine.FILENAME, ScriptContext.ENGINE_SCOPE)
                context.removeAttribute("mainClass", ScriptContext.ENGINE_SCOPE)
            }
        } else {
            val context = SimpleScriptContext()
            context.setAttribute("mainClass", getClassForFile(file)!!.replace('/', '.').replace('\\', '.'), ScriptContext.ENGINE_SCOPE)
            context.setAttribute(ScriptEngine.FILENAME, file.name, ScriptContext.ENGINE_SCOPE)
            context.setAttribute("classpath", SCRIPT_FOLDER.absolutePath, ScriptContext.ENGINE_SCOPE)
            context.setAttribute("sourcepath", SCRIPT_FOLDER.absolutePath, ScriptContext.ENGINE_SCOPE)
            currentLoadingScript = file
            try {
                engine.eval(reader, context)
            } finally {
                currentLoadingScript = null
                engine.context.removeAttribute(ScriptEngine.FILENAME, ScriptContext.ENGINE_SCOPE)
                engine.context.removeAttribute("mainClass", ScriptContext.ENGINE_SCOPE)
            }
        }
    }

    fun getScriptContext(engine: ScriptEngine): ScriptContext {
        return engine.context
    }

    fun getScriptContext(engineName: String): ScriptContext {
        val engine = getEngineByName(engineName)
        return if (engine == null) {
            throw IllegalStateException("No engine registered with name ($engineName)")
        } else {
            this.getScriptContext(engine)
        }
    }

    @Throws(ScriptException::class)
    fun eval(engine: ScriptEngine, script: String, context: ScriptContext?): Any? {
        if (engine is Compilable && ATTEMPT_COMPILATION) {
            val eng = engine as Compilable
            val cs = eng.compile(script)
            return if (context != null) cs.eval(context) else cs.eval()
        } else {
            return if (context != null) engine.eval(script, context) else engine.eval(script)
        }
    }

    @Throws(ScriptException::class)
    fun eval(engineName: String, script: String): Any? {
        return this.eval(engineName, script, null)
    }

    @Throws(ScriptException::class)
    fun eval(engineName: String, script: String, context: ScriptContext?): Any? {
        val engine = getEngineByName(engineName)
        return if (engine == null) {
            throw ScriptException("No engine registered with name ($engineName)")
        } else {
            this.eval(engine, script, context)
        }
    }

    @Throws(ScriptException::class)
    fun eval(engine: ScriptEngine, script: String): Any? {
        return this.eval(engine, script, null)
    }

    fun reportScriptFileError(script: File, e: ScriptException) {
        val dir = script.parent
        if (dir != null) {
            val errorHeader = "Error on: " + script.absolutePath + "\r\nLine: " + e.lineNumber + " - Column: " + e.columnNumber + "\r\n\r\n"
            log.warn("Failed executing script: " + script.absolutePath + ":")
            log.warn(errorHeader)
            log.warn(e.message)
        } else {
            log.warn(
                    "Failed executing script: " + script.absolutePath + "\r\n" + e.message +
                            "Additionally failed when trying to write an error report on script directory.",
                    e)
        }
    }

    fun registerScriptManager(manager: ScriptManager<*>) {
        scriptManagers.add(manager)
    }

    fun removeScriptManager(manager: ScriptManager<*>) {
        scriptManagers.remove(manager)
    }

    fun getScriptManagers(): List<ScriptManager<*>> {
        return scriptManagers
    }

    fun getClassForFile(script: File): String? {
        val path = script.absolutePath
        val scpPath = SCRIPT_FOLDER.absolutePath
        if (path.startsWith(scpPath)) {
            val idx = path.lastIndexOf('.')
            return path.substring(scpPath.length + 1, idx)
        }
        return null
    }
}
