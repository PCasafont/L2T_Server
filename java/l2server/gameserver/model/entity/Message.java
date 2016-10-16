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

package l2server.gameserver.model.entity;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.instancemanager.MailManager;
import l2server.gameserver.model.itemcontainer.Mail;
import l2server.util.Rnd;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Migi, DS
 */
public class Message
{
	private static final int EXPIRATION = 360; // 15 days
	private static final int COD_EXPIRATION = 12; // 12 hours

	private static final int UNLOAD_ATTACHMENTS_INTERVAL = 900000; // 15-30 mins

	// post state
	public static final int DELETED = 0;
	public static final int READED = 1;
	public static final int REJECTED = 2;

	private final int messageId, senderId, receiverId;
	@Getter private final long expiration;
	private String senderName = null;
	private String receiverName = null;
	private final String subject, content;
	private boolean unread, returned;
	private int sendBySystem;
	private boolean deletedBySender;
	private boolean deletedByReceiver;
	private long reqAdena;
	private boolean hasAttachments;
	private Mail attachments = null;
	private ScheduledFuture<?> unloadTask = null;
	private int systemMessage1 = 0;
	private int systemMessage2 = 0;

	public enum SendBySystem
	{
		PLAYER, NEWS, NONE, ALEGRIA, UNK1, SYSTEM, MENTORING, PRESENT
	}

	/*
	 * Constructor for restoring from DB.
	 */
	public Message(ResultSet rset) throws SQLException
	{
		messageId = rset.getInt("messageId");
		senderId = rset.getInt("senderId");
		receiverId = rset.getInt("receiverId");
		subject = rset.getString("subject");
		content = rset.getString("content");
		expiration = rset.getLong("expiration");
		reqAdena = rset.getLong("reqAdena");
		hasAttachments = rset.getBoolean("hasAttachments");
		unread = rset.getBoolean("isUnread");
		deletedBySender = rset.getBoolean("isDeletedBySender");
		deletedByReceiver = rset.getBoolean("isDeletedByReceiver");
		sendBySystem = rset.getInt("sendBySystem");
		returned = rset.getBoolean("isReturned");
		systemMessage1 = rset.getInt("systemMessage1");
		systemMessage2 = rset.getInt("systemMessage2");
	}

	/*
	 * This constructor used for creating new message.
	 */
	public Message(int senderId, int receiverId, boolean isCod, String subject, String text, long reqAdena)
	{
		messageId = IdFactory.getInstance().getNextId();
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.subject = subject;
		content = text;
		expiration = isCod ? System.currentTimeMillis() + COD_EXPIRATION * 3600000 :
				System.currentTimeMillis() + EXPIRATION * 3600000;
		hasAttachments = false;
		unread = true;
		deletedBySender = false;
		deletedByReceiver = false;
		this.reqAdena = reqAdena;
	}

	/*
	 * This constructor used for System Mails
	 */
	public Message(int receiverId, String subject, String content, SendBySystem sendBySystem)
	{
		messageId = IdFactory.getInstance().getNextId();
		senderId = -1;
		this.receiverId = receiverId;
		this.subject = subject;
		this.content = content;
		expiration = System.currentTimeMillis() + EXPIRATION * 3600000;
		reqAdena = 0;
		hasAttachments = false;
		unread = true;
		deletedBySender = true;
		deletedByReceiver = false;
		this.sendBySystem = sendBySystem.ordinal();
		returned = false;
	}

	/*
	 * This constructor used for System Mails
	 */
	public Message(int receiverId, String subject, String content, int systemMessage1, int systemMessage2)
	{
		messageId = IdFactory.getInstance().getNextId();
		senderId = -1;
		this.receiverId = receiverId;
		this.subject = subject;
		this.content = content;
		expiration = System.currentTimeMillis() + EXPIRATION * 3600000;
		reqAdena = 0;
		hasAttachments = false;
		unread = true;
		deletedBySender = true;
		deletedByReceiver = false;
		sendBySystem = 5;
		returned = false;
		this.systemMessage1 = systemMessage1;
		this.systemMessage2 = systemMessage2;
	}

	/*
	 * This constructor used for auto-generation of the "return attachments" message
	 */
	public Message(Message msg)
	{
		messageId = IdFactory.getInstance().getNextId();
		senderId = msg.getSenderId();
		receiverId = msg.getSenderId();
		subject = "";
		content = "";
		expiration = System.currentTimeMillis() + EXPIRATION * 3600000;
		unread = true;
		deletedBySender = true;
		deletedByReceiver = false;
		sendBySystem = SendBySystem.NONE.ordinal();
		returned = true;
		reqAdena = 0;
		hasAttachments = true;
		attachments = msg.getAttachments();
		msg.removeAttachments();
		attachments.setNewMessageId(messageId);
		unloadTask = ThreadPoolManager.getInstance().scheduleGeneral(new AttachmentsUnloadTask(this),
				UNLOAD_ATTACHMENTS_INTERVAL + Rnd.get(UNLOAD_ATTACHMENTS_INTERVAL));
	}

	public static PreparedStatement getStatement(Message msg, Connection con) throws SQLException
	{
		PreparedStatement stmt = con.prepareStatement(
				"INSERT INTO messages (messageId, senderId, receiverId, subject, content, expiration, reqAdena, hasAttachments, isUnread, isDeletedBySender, isDeletedByReceiver, sendBySystem, isReturned, systemMessage1, systemMessage2) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		stmt.setInt(1, msg.messageId);
		stmt.setInt(2, msg.senderId);
		stmt.setInt(3, msg.receiverId);
		stmt.setString(4, msg.subject);
		stmt.setString(5, msg.content);
		stmt.setLong(6, msg.expiration);
		stmt.setLong(7, msg.reqAdena);
		stmt.setString(8, String.valueOf(msg.hasAttachments));
		stmt.setString(9, String.valueOf(msg.unread));
		stmt.setString(10, String.valueOf(msg.deletedBySender));
		stmt.setString(11, String.valueOf(msg.deletedByReceiver));
		stmt.setInt(12, msg.sendBySystem);
		stmt.setString(13, String.valueOf(msg.returned));
		stmt.setInt(14, msg.systemMessage1);
		stmt.setInt(15, msg.systemMessage2);

		return stmt;
	}

	public final int getId()
	{
		return messageId;
	}

	public final int getSenderId()
	{
		return senderId;
	}

	public final int getReceiverId()
	{
		return receiverId;
	}

	public final String getSenderName()
	{
		if (senderName == null)
		{
			if (sendBySystem != 0)
			{
				return "****";
			}

			senderName = CharNameTable.getInstance().getNameById(senderId);
			if (senderName == null)
			{
				senderName = "";
			}
		}
		return senderName;
	}

	public final String getReceiverName()
	{
		if (receiverName == null)
		{
			receiverName = CharNameTable.getInstance().getNameById(receiverId);
			if (receiverName == null)
			{
				receiverName = "";
			}
		}
		return receiverName;
	}

	public final String getSubject()
	{
		return subject;
	}

	public final String getContent()
	{
		return content;
	}

	public final boolean isLocked()
	{
		return reqAdena > 0;
	}


	public final int getExpirationSeconds()
	{
		return (int) (expiration / 1000);
	}

	public final boolean isUnread()
	{
		return unread;
	}

	public final void markAsRead()
	{
		if (unread)
		{
			unread = false;
			MailManager.getInstance().markAsReadInDb(messageId);
		}
	}

	public final boolean isDeletedBySender()
	{
		return deletedBySender;
	}

	public final void setDeletedBySender()
	{
		if (!deletedBySender)
		{
			deletedBySender = true;
			if (deletedByReceiver)
			{
				MailManager.getInstance().deleteMessageInDb(messageId);
			}
			else
			{
				MailManager.getInstance().markAsDeletedBySenderInDb(messageId);
			}
		}
	}

	public final boolean isDeletedByReceiver()
	{
		return deletedByReceiver;
	}

	public final void setDeletedByReceiver()
	{
		if (!deletedByReceiver)
		{
			deletedByReceiver = true;
			if (deletedBySender)
			{
				MailManager.getInstance().deleteMessageInDb(messageId);
			}
			else
			{
				MailManager.getInstance().markAsDeletedByReceiverInDb(messageId);
			}
		}
	}

	public final int getSendBySystem()
	{
		return sendBySystem;
	}

	public final int getSystemMessage1()
	{
		return systemMessage1;
	}

	public final int getSystemMessage2()
	{
		return systemMessage2;
	}

	public final boolean isReturned()
	{
		return returned;
	}

	public final void setIsReturned(boolean val)
	{
		returned = val;
	}

	public final long getReqAdena()
	{
		return reqAdena;
	}

	public final synchronized Mail getAttachments()
	{
		if (!hasAttachments)
		{
			return null;
		}

		if (attachments == null)
		{
			int objId = senderId;
			if (objId < 0)
			{
				objId = receiverId;
			}
			attachments = new Mail(objId, messageId);
			attachments.restore();
			unloadTask = ThreadPoolManager.getInstance().scheduleGeneral(new AttachmentsUnloadTask(this),
					UNLOAD_ATTACHMENTS_INTERVAL + Rnd.get(UNLOAD_ATTACHMENTS_INTERVAL));
		}
		return attachments;
	}

	public final boolean hasAttachments()
	{
		return hasAttachments;
	}

	public final synchronized void removeAttachments()
	{
		if (attachments != null)
		{
			attachments = null;
			hasAttachments = false;
			MailManager.getInstance().removeAttachmentsInDb(messageId);
			if (unloadTask != null)
			{
				unloadTask.cancel(false);
			}
		}
	}

	public final synchronized Mail createAttachments()
	{
		if (hasAttachments || attachments != null)
		{
			return null;
		}

		int objId = senderId;
		if (objId < 0)
		{
			objId = receiverId;
		}
		attachments = new Mail(objId, messageId);
		hasAttachments = true;
		unloadTask = ThreadPoolManager.getInstance().scheduleGeneral(new AttachmentsUnloadTask(this),
				UNLOAD_ATTACHMENTS_INTERVAL + Rnd.get(UNLOAD_ATTACHMENTS_INTERVAL));
		return attachments;
	}

	protected final synchronized void unloadAttachments()
	{
		if (attachments != null)
		{
			attachments.deleteMe();
			attachments = null;
		}
	}

	static class AttachmentsUnloadTask implements Runnable
	{
		private Message msg;

		AttachmentsUnloadTask(Message msg)
		{
			this.msg = msg;
		}

		@Override
		public void run()
		{
			if (msg != null)
			{
				msg.unloadAttachments();
				msg = null;
			}
		}
	}
}
