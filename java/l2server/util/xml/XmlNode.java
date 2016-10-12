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

package l2server.util.xml;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Pere
 */
public class XmlNode
{
	private String _name;
	private Map<String, String> _attributes = new HashMap<>();
	private List<XmlNode> _children = new ArrayList<>();
	private String _text = null;

	public XmlNode(Node base)
	{
		_name = base.getNodeName();
		for (int i = 0; i < base.getAttributes().getLength(); i++)
		{
			String name = base.getAttributes().item(i).getNodeName();
			String value = base.getAttributes().item(i).getNodeValue();
			_attributes.put(name, value);
		}

		for (Node baseSubNode = base.getFirstChild(); baseSubNode != null; baseSubNode = baseSubNode.getNextSibling())
		{
			if (baseSubNode.getNodeType() == Node.ELEMENT_NODE)
			{
				_children.add(new XmlNode(baseSubNode));
			}
		}

		if (base.getFirstChild() != null)
		{
			_text = base.getFirstChild().getNodeValue();
		}
	}

	public String getName()
	{
		return _name;
	}

	public boolean hasAttributes()
	{
		return !_attributes.isEmpty();
	}

	public boolean hasAttribute(String name)
	{
		return _attributes.containsKey(name);
	}

	public boolean getBool(String name)
	{
		Object val = _attributes.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException(
					"Boolean value required for \"" + name + "\", but not specified.\r\nNode: " + this);
		}
		if (val instanceof Boolean)
		{
			return (Boolean) val;
		}
		try
		{
			return Boolean.parseBoolean((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Boolean value required for \"" + name + "\", but found: " + val);
		}
	}

	public boolean getBool(String name, boolean deflt)
	{
		Object val = _attributes.get(name);
		if (val == null)
		{
			return deflt;
		}
		if (val instanceof Boolean)
		{
			return (Boolean) val;
		}
		try
		{
			return Boolean.parseBoolean((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Boolean value required for \"" + name + "\", but found: " + val);
		}
	}

	public int getInt(String name)
	{
		Object val = _attributes.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException(
					"Integer value required for \"" + name + "\", but not specified.\r\nNode: " + this);
		}
		if (val instanceof Number)
		{
			return ((Number) val).intValue();
		}
		try
		{
			return Integer.parseInt((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required for \"" + name + "\", but found: " + val);
		}
	}

	public int getInt(String name, int deflt)
	{
		Object val = _attributes.get(name);
		if (val == null)
		{
			return deflt;
		}
		if (val instanceof Number)
		{
			return ((Number) val).intValue();
		}
		try
		{
			return Integer.parseInt((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required for \"" + name + "\", but found: " + val);
		}
	}

	public long getLong(String name)
	{
		Object val = _attributes.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException(
					"Integer value required for \"" + name + "\", but not specified.\r\nNode: " + this);
		}
		if (val instanceof Number)
		{
			return ((Number) val).longValue();
		}
		try
		{
			return Long.parseLong((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required for \"" + name + "\", but found: " + val);
		}
	}

	public long getLong(String name, long deflt)
	{
		Object val = _attributes.get(name);
		if (val == null)
		{
			return deflt;
		}
		if (val instanceof Number)
		{
			return ((Number) val).longValue();
		}
		try
		{
			return Long.parseLong((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Long value required for \"" + name + "\", but found: " + val);
		}
	}

	public float getFloat(String name)
	{
		Object val = _attributes.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException(
					"Float value required for \"" + name + "\", but not specified.\r\nNode: " + this);
		}
		if (val instanceof Number)
		{
			return ((Number) val).floatValue();
		}
		try
		{
			return (float) Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required for \"" + name + "\", but found: " + val);
		}
	}

	public float getFloat(String name, float deflt)
	{
		Object val = _attributes.get(name);
		if (val == null)
		{
			return deflt;
		}
		if (val instanceof Number)
		{
			return ((Number) val).floatValue();
		}
		try
		{
			return (float) Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required for \"" + name + "\", but found: " + val);
		}
	}

	public double getDouble(String name)
	{
		Object val = _attributes.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException(
					"Float value required for \"" + name + "\", but not specified.\r\nNode: " + this);
		}
		if (val instanceof Number)
		{
			return ((Number) val).doubleValue();
		}
		try
		{
			return Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required for \"" + name + "\", but found: " + val);
		}
	}

	public double getDouble(String name, double deflt)
	{
		Object val = _attributes.get(name);
		if (val == null)
		{
			return deflt;
		}
		if (val instanceof Number)
		{
			return ((Number) val).doubleValue();
		}
		try
		{
			return Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required for \"" + name + "\", but found: " + val);
		}
	}

	public String getString(String name)
	{
		Object val = _attributes.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException(
					"String value required for \"" + name + "\", but not specified.\r\nNode: " + this);
		}
		return String.valueOf(val);
	}

	public String getString(String name, String deflt)
	{
		Object val = _attributes.get(name);
		if (val == null)
		{
			return deflt;
		}
		return String.valueOf(val);
	}

	public Map<String, String> getAttributes()
	{
		return _attributes;
	}

	public XmlNode getFirstChild()
	{
		if (_children.isEmpty())
		{
			return null;
		}

		return _children.get(0);
	}

	public List<XmlNode> getChildren()
	{
		return _children;
	}

	public String getText()
	{
		return _text;
	}

	@Override
	public String toString()
	{
		return toString(0);
	}

	private String toString(int tabDepth)
	{
		String tabs = "";
		for (int i = 0; i < tabDepth; i++)
		{
			tabs += "\t";
		}

		String result = tabs + "<" + _name;
		for (Entry<String, String> attr : _attributes.entrySet())
		{
			result += " " + attr.getKey() + "=\"" + attr.getValue() + "\"";
		}

		if (!_children.isEmpty() || _text != null && _text.length() > 0)
		{
			result += ">\r\n";
			for (XmlNode child : _children)
			{
				result += child.toString(tabDepth + 1) + "\r\n";
			}

			result += tabs + "<" + _name + ">\r\n";
		}
		else
		{
			result += " />";
		}

		return result;
	}
}
