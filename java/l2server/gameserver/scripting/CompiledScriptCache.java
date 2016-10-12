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

import l2server.Config;
import l2server.log.Log;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache of Compiled Scripts
 *
 * @author KenM
 */
public class CompiledScriptCache implements Serializable
{
	private static final long serialVersionUID = 2L;

	private Map<String, CompiledScriptHolder> _compiledScripts = new HashMap<>();
	private transient boolean _modified = false;

	public CompiledScript loadCompiledScript(ScriptEngine engine, File file) throws FileNotFoundException,
			ScriptException
	{
		int len = L2ScriptEngineManager.SCRIPT_FOLDER.getPath().length() + 1;
		String relativeName = file.getPath().substring(len);

		CompiledScriptHolder csh = _compiledScripts.get(relativeName);
		if (csh != null && csh.matches(file))
		{
			if (Config.DEBUG)
			{
				Log.fine("Reusing cached compiled script: " + file);
			}
			return csh.getCompiledScript();
		}
		else
		{
			if (Config.DEBUG)
			{
				Log.info("Compiling script: " + file);
			}
			Compilable eng = (Compilable) engine;
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

			// TODO lock file
			CompiledScript cs = eng.compile(reader);
			if (cs instanceof Serializable)
			{
				synchronized (_compiledScripts)
				{
					_compiledScripts.put(relativeName, new CompiledScriptHolder(cs, file));
					_modified = true;
				}
			}

			return cs;
		}
	}

	public boolean isModified()
	{
		return _modified;
	}

	public void purge()
	{
		synchronized (_compiledScripts)
		{
			for (String path : _compiledScripts.keySet())
			{
				File file = new File(L2ScriptEngineManager.SCRIPT_FOLDER, path);
				if (!file.isFile())
				{
					_compiledScripts.remove(path);
					_modified = true;
				}
			}
		}
	}

	public void save() throws IOException
	{
		synchronized (_compiledScripts)
		{
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(new File(L2ScriptEngineManager.SCRIPT_FOLDER, "CompiledScripts.cache")));
			oos.writeObject(this);
			oos.close();
			_modified = false;
		}
	}

	public void checkFiles()
	{
		synchronized (_compiledScripts)
		{
			for (String path : _compiledScripts.keySet())
			{
				File file = new File(L2ScriptEngineManager.SCRIPT_FOLDER, path);
				if (!_compiledScripts.get(path).matches(file))
				{
					_compiledScripts.clear();
					return;
				}
			}
		}
	}
}
