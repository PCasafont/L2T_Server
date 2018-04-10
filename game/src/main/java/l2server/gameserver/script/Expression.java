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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptContext;

public class Expression {
	private static Logger log = LoggerFactory.getLogger(Expression.class.getName());

	private final ScriptContext context;
	@SuppressWarnings("unused")
	private final String lang;
	@SuppressWarnings("unused")
	private final String code;

	public static Object eval(String lang, String code) {
		try {
			return L2ScriptEngineManager.INSTANCE.eval(lang, code);
		} catch (Exception e) {
			log.warn("", e);
			return null;
		}
	}

	public static Object eval(ScriptContext context, String lang, String code) {
		try {
			return L2ScriptEngineManager.INSTANCE.eval(lang, code, context);
		} catch (Exception e) {
			log.warn("", e);
			return null;
		}
	}

	public static Expression create(ScriptContext context, String lang, String code) {
		try {
			return new Expression(context, lang, code);
		} catch (Exception e) {
			log.warn("", e);
			return null;
		}
	}

	private Expression(ScriptContext pContext, String pLang, String pCode) {
		context = pContext;
		lang = pLang;
		code = pCode;
	}

	public <T> void addDynamicVariable(String name, T value) {
		try {
			context.setAttribute(name, value, ScriptContext.ENGINE_SCOPE);
		} catch (Exception e) {
			log.warn("", e);
		}
	}

	public void removeDynamicVariable(String name) {
		try {
			context.removeAttribute(name, ScriptContext.ENGINE_SCOPE);
		} catch (Exception e) {
			log.warn("", e);
		}
	}
}
