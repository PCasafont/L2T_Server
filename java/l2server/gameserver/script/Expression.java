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

package l2server.gameserver.script;

import l2server.gameserver.scripting.L2ScriptEngineManager;
import l2server.log.Log;

import javax.script.ScriptContext;
import java.util.logging.Level;

public class Expression
{
	private final ScriptContext _context;
	@SuppressWarnings("unused")
	private final String _lang;
	@SuppressWarnings("unused")
	private final String _code;

	public static Object eval(String lang, String code)
	{
		try
		{
			return L2ScriptEngineManager.getInstance().eval(lang, code);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "", e);
			return null;
		}
	}

	public static Object eval(ScriptContext context, String lang, String code)
	{
		try
		{
			return L2ScriptEngineManager.getInstance().eval(lang, code, context);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "", e);
			return null;
		}
	}

	public static Expression create(ScriptContext context, String lang, String code)
	{
		try
		{
			return new Expression(context, lang, code);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "", e);
			return null;
		}
	}

	private Expression(ScriptContext pContext, String pLang, String pCode)
	{
		_context = pContext;
		_lang = pLang;
		_code = pCode;
	}

	public <T> void addDynamicVariable(String name, T value)
	{
		try
		{
			_context.setAttribute(name, value, ScriptContext.ENGINE_SCOPE);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "", e);
		}
	}

	public void removeDynamicVariable(String name)
	{
		try
		{
			_context.removeAttribute(name, ScriptContext.ENGINE_SCOPE);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "", e);
		}
	}
}
