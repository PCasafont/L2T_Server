/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.script.faenor;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptContext;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.script.DateRange;
import l2server.gameserver.script.IntList;
import l2server.gameserver.script.Parser;
import l2server.gameserver.script.ParserFactory;
import l2server.gameserver.script.ScriptEngine;
import l2server.log.Log;

import org.w3c.dom.Node;

/**
 * @author Luis Arias
 *
 */
public class FaenorEventParser extends FaenorParser
{
	static Logger _log = Logger.getLogger(FaenorEventParser.class.getName());
	private DateRange _eventDates = null;
	
	@Override
	public void parseScript(final Node eventNode, ScriptContext context)
	{
		String ID = attribute(eventNode, "ID");
		
		if (DEBUG)
			Log.fine("Parsing Event \"" + ID + "\"");
		
		_eventDates = DateRange.parse(attribute(eventNode, "Active"), DATE_FORMAT);
		
		Date currentDate = new Date();
		if (_eventDates.getEndDate().before(currentDate))
		{
			Log.info("Event ID: (" + ID + ") has passed... Ignored.");
			return;
		}
		
		if (_eventDates.getStartDate().after(currentDate))
		{
			Log.info("Event ID: (" + ID + ") is not active yet... Ignored.");
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					parseEventDropAndMessage(eventNode);
				}
			}, _eventDates.getStartDate().getTime() - currentDate.getTime());
			return;
		}
		
		parseEventDropAndMessage(eventNode);
	}
	
	protected void parseEventDropAndMessage(Node eventNode)
	{
		
		for (Node node = eventNode.getFirstChild(); node != null; node = node.getNextSibling())
		{
			
			if (isNodeName(node, "DropList"))
			{
				parseEventDropList(node);
			}
			else if (isNodeName(node, "Message"))
			{
				parseEventMessage(node);
			}
		}
	}
	
	private void parseEventMessage(Node sysMsg)
	{
		if (DEBUG)
			Log.fine("Parsing Event Message.");
		
		try
		{
			String type = attribute(sysMsg, "Type");
			String[] message = attribute(sysMsg, "Msg").split("\n");
			
			if (type.equalsIgnoreCase("OnJoin"))
			{
				_bridge.onPlayerLogin(message, _eventDates);
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Error in event parser: " + e.getMessage(), e);
		}
	}
	
	private void parseEventDropList(Node dropList)
	{
		if (DEBUG)
			Log.fine("Parsing Droplist.");
		
		for (Node node = dropList.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if (isNodeName(node, "AllDrop"))
			{
				parseEventDrop(node);
			}
		}
	}
	
	private void parseEventDrop(Node drop)
	{
		if (DEBUG)
			Log.fine("Parsing Drop.");
		
		try
		{
			int[] items = IntList.parse(attribute(drop, "Items"));
			int[] count = IntList.parse(attribute(drop, "Count"));
			double chance = getPercent(attribute(drop, "Chance"));
			
			_bridge.addEventDrop(items, count, chance, _eventDates);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "ERROR(parseEventDrop):" + e.getMessage(), e);
		}
	}
	
	static class FaenorEventParserFactory extends ParserFactory
	{
		@Override
		public Parser create()
		{
			return (new FaenorEventParser());
		}
	}
	
	static
	{
		ScriptEngine.parserFactories.put(getParserName("Event"), new FaenorEventParserFactory());
	}
}
