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

package l2server.gameserver.scripting;

import com.l2jserver.script.jython.JythonScriptEngine;
import l2server.Config;
import l2server.log.Log;

import javax.script.*;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Caches script engines and provides funcionality for executing and managing scripts.<BR>
 *
 * @author KenM
 */
public final class L2ScriptEngineManager
{
	public static final File SCRIPT_FOLDER =
			new File(Config.DATAPACK_ROOT.getAbsolutePath(), Config.DATA_FOLDER + "scripts");

	public static L2ScriptEngineManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private final Map<String, ScriptEngine> _nameEngines = new HashMap<>();
	private final Map<String, ScriptEngine> _extEngines = new HashMap<>();
	private final List<ScriptManager<?>> _scriptManagers = new LinkedList<>();

	private final CompiledScriptCache _cache;

	private File _currentLoadingScript;

	// Configs
	// TODO move to config file
	/**
	 * Informs(logs) the scripts being loaded.<BR>
	 * Apply only when executing script from files.<BR>
	 */
	private final boolean VERBOSE_LOADING = false;

	/**
	 * If the script engine supports compilation the script is compiled before execution.<BR>
	 */
	private final boolean ATTEMPT_COMPILATION = true;

	/**
	 * Use Compiled Scripts Cache.<BR>
	 * Only works if ATTEMPT_COMPILATION is true.<BR>
	 * DISABLED DUE TO ISSUES (if a superclass file changes subclasses are not recompiled while they should)
	 */
	private final boolean USE_COMPILED_CACHE = true;

	/**
	 * Clean an previous error log(if such exists) for the script being loaded before trying to load.<BR>
	 * Apply only when executing script from files.<BR>
	 */
	private final boolean PURGE_ERROR_LOG = true;

	private L2ScriptEngineManager()
	{
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		List<ScriptEngineFactory> factories = scriptEngineManager.getEngineFactories();
		if (USE_COMPILED_CACHE)
		{
			_cache = loadCompiledScriptCache();
		}
		else
		{
			_cache = null;
		}
		Log.info("Initializing Script Engine Manager");

		for (ScriptEngineFactory factory : factories)
		{
			try
			{
				ScriptEngine engine = factory.getScriptEngine();
				boolean reg = false;
				for (String name : factory.getNames())
				{
					ScriptEngine existentEngine = _nameEngines.get(name);

					if (existentEngine != null)
					{
						double engineVer = Double.parseDouble(factory.getEngineVersion());
						double existentEngVer = Double.parseDouble(existentEngine.getFactory().getEngineVersion());

						if (engineVer <= existentEngVer)
						{
							continue;
						}
					}

					reg = true;
					_nameEngines.put(name, engine);
				}

				if (reg)
				{
					Log.info("Script Engine: " + factory.getEngineName() + " " + factory.getEngineVersion() +
							" - Language: " + factory.getLanguageName() + " - Language Version: " +
							factory.getLanguageVersion());
				}

				for (String ext : factory.getExtensions())
				{
					if (!ext.equals("java") || factory.getLanguageName().equals("java"))
					{
						_extEngines.put(ext, engine);
					}
				}
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, "Failed initializing factory: " + e.getMessage(), e);
			}
		}

		preConfigure();
	}

	private void preConfigure()
	{
		// java class path

		// Jython sys.path
		String dataPackDirForwardSlashes = SCRIPT_FOLDER.getPath().replaceAll("\\\\", "/");
		String configScript = "import sys;sys.path.insert(0,'" + dataPackDirForwardSlashes + "');";
		try
		{
			this.eval("jython", configScript);
		}
		catch (ScriptException e)
		{
			Log.severe("Failed preconfiguring jython: " + e.getMessage());
		}
	}

	private ScriptEngine getEngineByName(String name)
	{
		return _nameEngines.get(name);
	}

	private ScriptEngine getEngineByExtension(String ext)
	{
		return _extEngines.get(ext);
	}

	public void executeScriptList(File list) throws IOException
	{
		File file;

		if (!Config.ALT_DEV_NO_HANDLERS && Config.ALT_DEV_NO_QUESTS)
		{
			file = new File(SCRIPT_FOLDER, "handlers/MasterHandler.java");

			try
			{
				executeScript(file);
				Log.info("Handlers loaded, all other scripts skipped");
				return;
			}
			catch (ScriptException se)
			{
				Log.log(Level.WARNING, "", se);
			}
		}

		if (Config.ALT_DEV_NO_QUESTS)
		{
			return;
		}

		if (list.isFile())
		{
			LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(list)));
			String line;

			while ((line = lnr.readLine()) != null)
			{
				if (Config.ALT_DEV_NO_HANDLERS && line.contains("MasterHandler.java"))
				{
					continue;
				}

				String[] parts = line.trim().split("#");

				if (parts.length > 0 && !parts[0].startsWith("#") && parts[0].length() > 0)
				{
					line = parts[0];

					if (line.endsWith("/**"))
					{
						line = line.substring(0, line.length() - 3);
					}
					else if (line.endsWith("/*"))
					{
						line = line.substring(0, line.length() - 2);
					}

					file = new File(SCRIPT_FOLDER, line);

					if (file.isDirectory() && parts[0].endsWith("/**"))
					{
						executeAllScriptsInDirectory(file, true, 32);
					}
					else if (file.isDirectory() && parts[0].endsWith("/*"))
					{
						executeAllScriptsInDirectory(file);
					}
					else if (file.isFile())
					{
						try
						{
							executeScript(file);
						}
						catch (ScriptException e)
						{
							reportScriptFileError(file, e);
						}
					}
					else
					{
						Log.warning("Failed loading: (" + file.getCanonicalPath() + ") @ " + list.getName() + ":" +
								lnr.getLineNumber() + " - Reason: doesnt exists or is not a file.");
					}
				}
			}
			lnr.close();
		}
		else
		{
			throw new IllegalArgumentException("Argument must be an file containing a list of scripts to be loaded");
		}
	}

	public void executeAllScriptsInDirectory(File dir)
	{
		this.executeAllScriptsInDirectory(dir, false, 0);
	}

	public void executeAllScriptsInDirectory(File dir, boolean recurseDown, int maxDepth)
	{
		this.executeAllScriptsInDirectory(dir, recurseDown, maxDepth, 0);
	}

	private void executeAllScriptsInDirectory(File dir, boolean recurseDown, int maxDepth, int currentDepth)
	{
		if (dir.isDirectory())
		{
			for (File file : dir.listFiles())
			{
				if (file.isDirectory() && recurseDown && maxDepth > currentDepth)
				{
					if (VERBOSE_LOADING)
					{
						Log.info("Entering folder: " + file.getName());
					}
					executeAllScriptsInDirectory(file, recurseDown, maxDepth, currentDepth + 1);
				}
				else if (file.isFile())
				{
					try
					{
						String name = file.getName();
						int lastIndex = name.lastIndexOf('.');
						String extension;
						if (lastIndex != -1)
						{
							extension = name.substring(lastIndex + 1);
							ScriptEngine engine = getEngineByExtension(extension);
							if (engine != null)
							{
								executeScript(engine, file);
							}
						}
					}
					catch (FileNotFoundException e)
					{
						// should never happen
						Log.log(Level.WARNING, "", e);
					}
					catch (ScriptException e)
					{
						reportScriptFileError(file, e);
						//Logozo.log(Level.WARNING, "", e);
					}
				}
			}
		}
		else
		{
			throw new IllegalArgumentException("The argument directory either doesnt exists or is not an directory.");
		}
	}

	public CompiledScriptCache getCompiledScriptCache()
	{
		return _cache;
	}

	public CompiledScriptCache loadCompiledScriptCache()
	{
		if (USE_COMPILED_CACHE)
		{
			File file = new File(SCRIPT_FOLDER, "CompiledScripts.cache");
			if (file.isFile())
			{
				ObjectInputStream ois = null;
				try
				{
					ois = new ObjectInputStream(new FileInputStream(file));
					CompiledScriptCache cache = (CompiledScriptCache) ois.readObject();
					cache.checkFiles();
					return cache;
				}
				catch (InvalidClassException e)
				{
					Log.log(Level.SEVERE, "Failed loading Compiled Scripts Cache, invalid class (Possibly outdated).",
							e);
				}
				catch (IOException e)
				{
					Log.log(Level.SEVERE, "Failed loading Compiled Scripts Cache from file.", e);
				}
				catch (ClassNotFoundException e)
				{
					Log.log(Level.SEVERE, "Failed loading Compiled Scripts Cache, class not found.", e);
				}
				finally
				{
					try
					{
						ois.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				return new CompiledScriptCache();
			}
			else
			{
				return new CompiledScriptCache();
			}
		}
		return null;
	}

	public void executeScript(File file) throws ScriptException, FileNotFoundException
	{
		String name = file.getName();
		int lastIndex = name.lastIndexOf('.');
		String extension;
		if (lastIndex != -1)
		{
			extension = name.substring(lastIndex + 1);
		}
		else
		{
			throw new ScriptException(
					"Script file (" + name + ") doesnt has an extension that identifies the ScriptEngine to be used.");
		}

		ScriptEngine engine = getEngineByExtension(extension);
		if (engine == null)
		{
			throw new ScriptException("No engine registered for extension (" + extension + ")");
		}
		else
		{
			executeScript(engine, file);
		}
	}

	public void executeScript(String engineName, File file) throws FileNotFoundException, ScriptException
	{
		ScriptEngine engine = getEngineByName(engineName);
		if (engine == null)
		{
			throw new ScriptException("No engine registered with name (" + engineName + ")");
		}
		else
		{
			executeScript(engine, file);
		}
	}

	public void executeScript(ScriptEngine engine, File file) throws FileNotFoundException, ScriptException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

		if (VERBOSE_LOADING)
		{
			Log.info("Loading Script: " + file.getAbsolutePath());
		}

		if (PURGE_ERROR_LOG)
		{
			String name = file.getAbsolutePath() + ".error.log";
			File errorLog = new File(name);
			if (errorLog.isFile())
			{
				errorLog.delete();
			}
		}

		if (engine instanceof Compilable && ATTEMPT_COMPILATION)
		{
			ScriptContext context = new SimpleScriptContext();
			context.setAttribute("mainClass", getClassForFile(file).replace('/', '.').replace('\\', '.'),
					ScriptContext.ENGINE_SCOPE);
			context.setAttribute(ScriptEngine.FILENAME, file.getName(), ScriptContext.ENGINE_SCOPE);
			context.setAttribute("classpath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
			context.setAttribute("sourcepath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
			context.setAttribute(JythonScriptEngine.JYTHON_ENGINE_INSTANCE, engine, ScriptContext.ENGINE_SCOPE);

			setCurrentLoadingScript(file);
			ScriptContext ctx = engine.getContext();
			try
			{
				engine.setContext(context);
				if (USE_COMPILED_CACHE)
				{
					CompiledScript cs = _cache.loadCompiledScript(engine, file);
					cs.eval(context);
				}
				else
				{
					Compilable eng = (Compilable) engine;
					CompiledScript cs = eng.compile(reader);
					cs.eval(context);
				}
			}
			finally
			{
				engine.setContext(ctx);
				setCurrentLoadingScript(null);
				context.removeAttribute(ScriptEngine.FILENAME, ScriptContext.ENGINE_SCOPE);
				context.removeAttribute("mainClass", ScriptContext.ENGINE_SCOPE);
			}
		}
		else
		{
			ScriptContext context = new SimpleScriptContext();
			context.setAttribute("mainClass", getClassForFile(file).replace('/', '.').replace('\\', '.'),
					ScriptContext.ENGINE_SCOPE);
			context.setAttribute(ScriptEngine.FILENAME, file.getName(), ScriptContext.ENGINE_SCOPE);
			context.setAttribute("classpath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
			context.setAttribute("sourcepath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
			setCurrentLoadingScript(file);
			try
			{
				engine.eval(reader, context);
			}
			finally
			{
				setCurrentLoadingScript(null);
				engine.getContext().removeAttribute(ScriptEngine.FILENAME, ScriptContext.ENGINE_SCOPE);
				engine.getContext().removeAttribute("mainClass", ScriptContext.ENGINE_SCOPE);
			}
		}
	}

	public static String getClassForFile(File script)
	{
		String path = script.getAbsolutePath();
		String scpPath = SCRIPT_FOLDER.getAbsolutePath();
		if (path.startsWith(scpPath))
		{
			int idx = path.lastIndexOf('.');
			return path.substring(scpPath.length() + 1, idx);
		}
		return null;
	}

	public ScriptContext getScriptContext(ScriptEngine engine)
	{
		return engine.getContext();
	}

	public ScriptContext getScriptContext(String engineName)
	{
		ScriptEngine engine = getEngineByName(engineName);
		if (engine == null)
		{
			throw new IllegalStateException("No engine registered with name (" + engineName + ")");
		}
		else
		{
			return this.getScriptContext(engine);
		}
	}

	public Object eval(ScriptEngine engine, String script, ScriptContext context) throws ScriptException
	{
		if (engine instanceof Compilable && ATTEMPT_COMPILATION)
		{
			Compilable eng = (Compilable) engine;
			CompiledScript cs = eng.compile(script);
			return context != null ? cs.eval(context) : cs.eval();
		}
		else
		{
			return context != null ? engine.eval(script, context) : engine.eval(script);
		}
	}

	public Object eval(String engineName, String script) throws ScriptException
	{
		return this.eval(engineName, script, null);
	}

	public Object eval(String engineName, String script, ScriptContext context) throws ScriptException
	{
		ScriptEngine engine = getEngineByName(engineName);
		if (engine == null)
		{
			throw new ScriptException("No engine registered with name (" + engineName + ")");
		}
		else
		{
			return this.eval(engine, script, context);
		}
	}

	public Object eval(ScriptEngine engine, String script) throws ScriptException
	{
		return this.eval(engine, script, null);
	}

	public void reportScriptFileError(File script, ScriptException e)
	{
		String dir = script.getParent();
		if (dir != null)
		{
			String errorHeader =
					"Error on: " + script.getAbsolutePath() + "\r\nLine: " + e.getLineNumber() + " - Column: " +
							e.getColumnNumber() + "\r\n\r\n";
			Log.warning("Failed executing script: " + script.getAbsolutePath() + ":");
			Log.warning(errorHeader);
			Log.warning(e.getMessage());
		}
		else
		{
			Log.log(Level.WARNING, "Failed executing script: " + script.getAbsolutePath() + "\r\n" + e.getMessage() +
					"Additionally failed when trying to write an error report on script directory.", e);
		}
	}

	public void registerScriptManager(ScriptManager<?> manager)
	{
		_scriptManagers.add(manager);
	}

	public void removeScriptManager(ScriptManager<?> manager)
	{
		_scriptManagers.remove(manager);
	}

	public List<ScriptManager<?>> getScriptManagers()
	{
		return _scriptManagers;
	}

	/**
	 * @param currentLoadingScript The currentLoadingScript to set.
	 */
	protected void setCurrentLoadingScript(File currentLoadingScript)
	{
		_currentLoadingScript = currentLoadingScript;
	}

	/**
	 * @return Returns the currentLoadingScript.
	 */
	protected File getCurrentLoadingScript()
	{
		return _currentLoadingScript;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final L2ScriptEngineManager _instance = new L2ScriptEngineManager();
	}
}
