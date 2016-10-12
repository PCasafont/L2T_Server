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

import l2server.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class XmlDocument
{
	private static DocumentBuilderFactory _factory = DocumentBuilderFactory.newInstance();

	static
	{
		_factory.setValidating(false);
		_factory.setIgnoringComments(true);
	}

	private List<XmlNode> _children = new ArrayList<>();

	public XmlDocument(File file)
	{
		if (file == null || !file.exists())
		{
			Log.warning("The following XML could not be loaded:");
			Log.warning("- " + file.getAbsolutePath());
			return;
		}

		Document doc = null;
		try
		{
			doc = _factory.newDocumentBuilder().parse(file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		for (Node baseNode = doc.getFirstChild(); baseNode != null; baseNode = baseNode.getNextSibling())
		{
			if (baseNode.getNodeType() == Node.ELEMENT_NODE)
			{
				_children.add(new XmlNode(baseNode));
			}
		}
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
}
