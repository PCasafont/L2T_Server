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
	private final long expiration;
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
		this.messageId = rset.getInt("messageId");
		this.senderId = rset.getInt("senderId");
		this.receiverId = rset.getInt("receiverId");
		this.subject = rset.getString("subject");
		this.content = rset.getString("content");
		this.expiration = rset.getLong("expiration");
		this.reqAdena = rset.getLong("reqAdena");
		this.hasAttachments = rset.getBoolean("hasAttachments");
		this.unread = rset.getBoolean("isUnread");
		this.deletedBySender = rset.getBoolean("isDeletedBySender");
		this.deletedByReceiver = rset.getBoolean("isDeletedByReceiver");
		this.sendBySystem = rset.getInt("sendBySystem");
		this.returned = rset.getBoolean("isReturned");
		this.systemMessage1 = rset.getInt("systemMessage1");
		this.systemMessage2 = rset.getInt("systemMessage2");
	}

	/*
	 * This constructor used for creating new message.
	 */
	public Message(int senderId, int receiverId, boolean isCod, String subject, String text, long reqAdena)
	{
		this.messageId = IdFactory.getInstance().getNextId();
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.subject = subject;
		this.content = text;
		this.expiration = isCod ? System.currentTimeMillis() + COD_EXPIRATION * 3600000 :
				System.currentTimeMillis() + EXPIRATION * 3600000;
		this.hasAttachments = false;
		this.unread = true;
		this.deletedBySender = false;
		this.deletedByReceiver = false;
		this.reqAdena = reqAdena;
	}

	/*
	 * This constructor used for System Mails
	 */
	public Message(int receiverId, String subject, String content, SendBySystem sendBySystem)
	{
		this.messageId = IdFactory.getInstance().getNextId();
		this.senderId = -1;
		this.receiverId = receiverId;
		this.subject = subject;
		this.content = content;
		this.expiration = System.currentTimeMillis() + EXPIRATION * 3600000;
		this.reqAdena = 0;
		this.hasAttachments = false;
		this.unread = true;
		this.deletedBySender = true;
		this.deletedByReceiver = false;
		this.sendBySystem = sendBySystem.ordinal();
		this.returned = false;
	}

	/*
	 * This constructor used for System Mails
	 */
	public Message(int receiverId, String subject, String content, int systemMessage1, int systemMessage2)
	{
		this.messageId = IdFactory.getInstance().getNextId();
		this.senderId = -1;
		this.receiverId = receiverId;
		this.subject = subject;
		this.content = content;
		this.expiration = System.currentTimeMillis() + EXPIRATION * 3600000;
		this.reqAdena = 0;
		this.hasAttachments = false;
		this.unread = true;
		this.deletedBySender = true;
		this.deletedByReceiver = false;
		this.sendBySystem = 5;
		this.returned = false;
		this.systemMessage1 = systemMessage1;
		this.systemMessage2 = systemMessage2;
	}

	/*
	 * This constructor used for auto-generation of the "return attachments" message
	 */
	public Message(Message msg)
	{
		this.messageId = IdFactory.getInstance().getNextId();
		this.senderId = msg.getSenderId();
		this.receiverId = msg.getSenderId();
		this.subject = "";
		this.content = "";
		this.expiration = System.currentTimeMillis() + EXPIRATION * 3600000;
		this.unread = true;
		this.deletedBySender = true;
		this.deletedByReceiver = false;
		this.sendBySystem = SendBySystem.NONE.ordinal();
		this.returned = true;
		this.reqAdena = 0;
		this.hasAttachments = true;
		this.attachments = msg.getAttachments();
		msg.removeAttachments();
		this.attachments.setNewMessageId(this.messageId);
		this.unloadTask = ThreadPoolManager.getInstance().scheduleGeneral(new AttachmentsUnloadTask(this),
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
		return this.messageId;
	}

	public final int getSenderId()
	{
		return this.senderId;
	}

	public final int getReceiverId()
	{
		return this.receiverId;
	}

	public final String getSenderName()
	{
		if (this.senderName == null)
		{
			if (this.sendBySystem != 0)
			{
				return "****";
			}

			this.senderName = CharNameTable.getInstance().getNameById(this.senderId);
			if (this.senderName == null)
			{
				this.senderName = "";
			}
		}
		return this.senderName;
	}

	public final String getReceiverName()
	{
		if (this.receiverName == null)
		{
			this.receiverName = CharNameTable.getInstance().getNameById(this.receiverId);
			if (this.receiverName == null)
			{
				this.receiverName = "";
			}
		}
		return this.receiverName;
	}

	public final String getSubject()
	{
		return this.subject;
	}

	public final String getContent()
	{
		return this.content;
	}

	public final boolean isLocked()
	{
		return this.reqAdena > 0;
	}

	public final long getExpiration()
	{
		return this.expiration;
	}

	public final int getExpirationSeconds()
	{
		return (int) (this.expiration / 1000);
	}

	public final boolean isUnread()
	{
		return this.unread;
	}

	public final void markAsRead()
	{
		if (this.unread)
		{
			this.unread = false;
			MailManager.getInstance().markAsReadInDb(this.messageId);
		}
	}

	public final boolean isDeletedBySender()
	{
		return this.deletedBySender;
	}

	public final void setDeletedBySender()
	{
		if (!this.deletedBySender)
		{
			this.deletedBySender = true;
			if (this.deletedByReceiver)
			{
				MailManager.getInstance().deleteMessageInDb(this.messageId);
			}
			else
			{
				MailManager.getInstance().markAsDeletedBySenderInDb(this.messageId);
			}
		}
	}

	public final boolean isDeletedByReceiver()
	{
		return this.deletedByReceiver;
	}

	public final void setDeletedByReceiver()
	{
		if (!this.deletedByReceiver)
		{
			this.deletedByReceiver = true;
			if (this.deletedBySender)
			{
				MailManager.getInstance().deleteMessageInDb(this.messageId);
			}
			else
			{
				MailManager.getInstance().markAsDeletedByReceiverInDb(this.messageId);
			}
		}
	}

	public final int getSendBySystem()
	{
		return this.sendBySystem;
	}

	public final int getSystemMessage1()
	{
		return this.systemMessage1;
	}

	public final int getSystemMessage2()
	{
		return this.systemMessage2;
	}

	public final boolean isReturned()
	{
		return this.returned;
	}

	public final void setIsReturned(boolean val)
	{
		this.returned = val;
	}

	public final long getReqAdena()
	{
		return this.reqAdena;
	}

	public final synchronized Mail getAttachments()
	{
		if (!this.hasAttachments)
		{
			return null;
		}

		if (this.attachments == null)
		{
			int objId = this.senderId;
			if (objId < 0)
			{
				objId = this.receiverId;
			}
			this.attachments = new Mail(objId, this.messageId);
			this.attachments.restore();
			this.unloadTask = ThreadPoolManager.getInstance().scheduleGeneral(new AttachmentsUnloadTask(this),
					UNLOAD_ATTACHMENTS_INTERVAL + Rnd.get(UNLOAD_ATTACHMENTS_INTERVAL));
		}
		return this.attachments;
	}

	public final boolean hasAttachments()
	{
		return this.hasAttachments;
	}

	public final synchronized void removeAttachments()
	{
		if (this.attachments != null)
		{
			this.attachments = null;
			this.hasAttachments = false;
			MailManager.getInstance().removeAttachmentsInDb(this.messageId);
			if (this.unloadTask != null)
			{
				this.unloadTask.cancel(false);
			}
		}
	}

	public final synchronized Mail createAttachments()
	{
		if (this.hasAttachments || this.attachments != null)
		{
			return null;
		}

		int objId = this.senderId;
		if (objId < 0)
		{
			objId = this.receiverId;
		}
		this.attachments = new Mail(objId, this.messageId);
		this.hasAttachments = true;
		this.unloadTask = ThreadPoolManager.getInstance().scheduleGeneral(new AttachmentsUnloadTask(this),
				UNLOAD_ATTACHMENTS_INTERVAL + Rnd.get(UNLOAD_ATTACHMENTS_INTERVAL));
		return this.attachments;
	}

	protected final synchronized void unloadAttachments()
	{
		if (this.attachments != null)
		{
			this.attachments.deleteMe();
			this.attachments = null;
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
			if (this.msg != null)
			{
				this.msg.unloadAttachments();
				this.msg = null;
			}
		}
	}
}
