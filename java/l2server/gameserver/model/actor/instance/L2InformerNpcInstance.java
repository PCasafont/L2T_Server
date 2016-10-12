package l2server.gameserver.model.actor.instance;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.LoginServerThread;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Pere
 */
public class L2InformerNpcInstance extends L2NpcInstance
{
	public L2InformerNpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void showChatWindow(L2PcInstance playerInstance, int val)
	{
		if (playerInstance == null)
		{
			return;
		}

		String htmContent = "<html><title>Event</title><body>";
		if (val == 0)
		{
			htmContent += "<br><center>Features</center><br>" +
					"Tenkai is simply different, but not changing the meaning of this great game, Lineage 2.<br>" +
					"We have tons of adjustments, additions and improvements but they are here as a plus, not substitutive.<br>" +
					"Here you can check our differences from retail:<br>" + "%subLinks%<br>";
			if (Config.isServer(Config.TENKAI) && !playerInstance.isNoble())
			{
				htmContent +=
						"<center><button value=\"I want to be Noble! (10 Glittering Medals)\" action=\"bypass -h npc_%objectId%_Noblesse\" width=150 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center><br>";
			}
		}
		else
		{
			htmContent += getContent(val) + "<br>";
		}
		htmContent += "</body></html>";

		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

		npcHtmlMessage.setHtml(htmContent);
		npcHtmlMessage.replace("%subLinks%", getSubLinks(val));
		npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
		playerInstance.sendPacket(npcHtmlMessage);
		playerInstance.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private String getContent(int val)
	{
		String content = "";
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("SELECT name, html, parent_id FROM l2tenkai_web.features WHERE id = ?");

			statement.setInt(1, val);
			ResultSet rset = statement.executeQuery();

			if (rset.next())
			{
				content += "<br><center>" + rset.getString("name") + "</center><br>" + rset.getString("html") + "<br>" +
						"<center>%subLinks%<br>" + "<button value=\"Back\" action=\"bypass -h npc_%objectId%_Chat " +
						rset.getInt("parent_id") +
						"\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center><br>";
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			Log.warning("Failed to close Statement/ResultSet in Informer NPC: " + e.getMessage());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return content;
	}

	private String getSubLinks(int val)
	{
		String content = "";
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"SELECT id, name FROM l2tenkai_web.features WHERE parent_id = ? AND exclude_" +
							LoginServerThread.getInstance().getServerName() + " = 0");

			statement.setInt(1, val);
			ResultSet rset = statement.executeQuery();

			boolean first = true;
			content += "<table>";
			while (rset.next())
			{
				if (first)
				{
					content += "<tr>";
				}
				content +=
						"<td><button value=\"" + rset.getString("name") + "\" action=\"bypass -h npc_%objectId%_Chat " +
								rset.getInt("id") +
								"\" width=135 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>";
				if (!first)
				{
					content += "</tr>";
				}
				first = !first;
			}
			if (!first)
			{
				content += "</tr>";
			}
			content += "</table>";

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			//Logozo.log(Level.SEVERE, "Error with Informer NPC: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return content;
	}
}
