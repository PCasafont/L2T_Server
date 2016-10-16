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

package l2server.gameserver.script.faenor;

import l2server.Config;
import l2server.gameserver.script.*;
import l2server.gameserver.scripting.L2ScriptEngineManager;
import l2server.log.Log;
import org.w3c.dom.Node;

import javax.script.ScriptContext;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * @author Luis Arias
 */
public class FaenorScriptEngine extends ScriptEngine
{
	static Logger _log = Logger.getLogger(FaenorScriptEngine.class.getName());
	public static final String PACKAGE_DIRECTORY = Config.DATA_FOLDER + "faenor/";
	public static final boolean DEBUG = true;

	private LinkedList<ScriptDocument> _scripts;

	public static FaenorScriptEngine getInstance()
	{
		return SingletonHolder._instance;
	}

	private FaenorScriptEngine()
	{
		_scripts = new LinkedList<>();
		loadPackages();
		parsePackages();
	}

	public void reloadPackages()
	{
		_scripts = new LinkedList<>();
		parsePackages();
	}

	private void loadPackages()
	{
		File packDirectory =
				new File(Config.DATAPACK_ROOT, PACKAGE_DIRECTORY);//Logozo.sss(packDirectory.getAbsolutePath());

		FileFilter fileFilter = file -> file.getName().endsWith(".zip");

		File[] files = packDirectory.listFiles(fileFilter);
		if (files == null)
		{
			return;
		}
		ZipFile zipPack;

		for (File file : files)
		{
			try
			{
				zipPack = new ZipFile(file);
			}
			catch (ZipException e)
			{
				Log.log(Level.WARNING, "", e);
				continue;
			}
			catch (IOException e)
			{
				Log.log(Level.WARNING, "", e);
				continue;
			}

			ScriptPackage module = new ScriptPackage(zipPack);

			List<ScriptDocument> scrpts = module.getScriptFiles();
			for (ScriptDocument script : scrpts)
			{
				_scripts.add(script);
			}
			try
			{
				zipPack.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		/*for (ScriptDocument script : scripts)
         {
		 Logozo.sss("Script: "+script);
		 }
		 Logozo.sss("Sorting");
		 orderScripts();
		 for (ScriptDocument script : scripts)
		 {
		 Logozo.sss("Script: "+script);
		 }*/
	}

	public void orderScripts()
	{
		if (_scripts.size() > 1)
		{
			//ScriptDocument npcInfo = null;

			for (int i = 0; i < _scripts.size(); )
			{
				if (_scripts.get(i).getName().contains("NpcStatData"))
				{
					_scripts.addFirst(_scripts.remove(i));
					//scripts.set(i, scripts.get(0));
					//scripts.set(0, npcInfo);
				}
				else
				{
					i++;
				}
			}
		}
	}

	public void parsePackages()
	{
		L2ScriptEngineManager sem = L2ScriptEngineManager.getInstance();
		ScriptContext context = sem.getScriptContext("beanshell");
		try
		{
			sem.eval("beanshell", "double log1p(double d) { return Math.log1p(d); }");
			sem.eval("beanshell", "double pow(double d, double p) { return Math.pow(d,p); }");

			for (ScriptDocument script : _scripts)
			{
				parseScript(script, context);
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "", e);
		}
	}

	public void parseScript(ScriptDocument script, ScriptContext context)
	{
		if (DEBUG)
		{
			Log.fine("Parsing Script: " + script.getName());
		}

		Node node = script.getDocument().getFirstChild();
		String parserClass = "faenor.Faenor" + node.getNodeName() + "Parser";

		Parser parser = null;
		try
		{
			parser = createParser(parserClass);
		}
		catch (ParserNotCreatedException e)
		{
			Log.log(Level.WARNING, "ERROR: No parser registered for Script: " + parserClass + ": " + e.getMessage(), e);
		}

		if (parser == null)
		{
			Log.warning("Unknown Script Type: " + script.getName());
			return;
		}

		try
		{
			parser.parseScript(node, context);
			Log.fine(script.getName() + "Script Sucessfullty Parsed.");
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Script Parsing Failed: " + e.getMessage(), e);
		}
	}

	@Override
	public String toString()
	{
		if (_scripts.isEmpty())
		{
			return "No Packages Loaded.";
		}

		String out = "Script Packages currently loaded:\n";

		for (ScriptDocument script : _scripts)
		{
			out += script;
		}
		return out;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final FaenorScriptEngine _instance = new FaenorScriptEngine();
	}
}
