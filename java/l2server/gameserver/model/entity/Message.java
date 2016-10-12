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

	private final int _messageId, _senderId, _receiverId;
	private final long _expiration;
	private String _senderName = null;
	private String _receiverName = null;
	private final String _subject, _content;
	private boolean _unread, _returned;
	private int _sendBySystem;
	private boolean _deletedBySender;
	private boolean _deletedByReceiver;
	private long _reqAdena;
	private boolean _hasAttachments;
	private Mail _attachments = null;
	private ScheduledFuture<?> _unloadTask = null;
	private int _systemMessage1 = 0;
	private int _systemMessage2 = 0;

	public enum SendBySystem
	{
		PLAYER, NEWS, NONE, ALEGRIA, UNK1, SYSTEM, MENTORING, PRESENT
	}

	/*
	 * Constructor for restoring from DB.
	 */
	public Message(ResultSet rset) throws SQLException
	{
		_messageId = rset.getInt("messageId");
		_senderId = rset.getInt("senderId");
		_receiverId = rset.getInt("receiverId");
		_subject = rset.getString("subject");
		_content = rset.getString("content");
		_expiration = rset.getLong("expiration");
		_reqAdena = rset.getLong("reqAdena");
		_hasAttachments = rset.getBoolean("hasAttachments");
		_unread = rset.getBoolean("isUnread");
		_deletedBySender = rset.getBoolean("isDeletedBySender");
		_deletedByReceiver = rset.getBoolean("isDeletedByReceiver");
		_sendBySystem = rset.getInt("sendBySystem");
		_returned = rset.getBoolean("isReturned");
		_systemMessage1 = rset.getInt("systemMessage1");
		_systemMessage2 = rset.getInt("systemMessage2");
	}

	/*
	 * This constructor used for creating new message.
	 */
	public Message(int senderId, int receiverId, boolean isCod, String subject, String text, long reqAdena)
	{
		_messageId = IdFactory.getInstance().getNextId();
		_senderId = senderId;
		_receiverId = receiverId;
		_subject = subject;
		_content = text;
		_expiration = isCod ? System.currentTimeMillis() + COD_EXPIRATION * 3600000 :
				System.currentTimeMillis() + EXPIRATION * 3600000;
		_hasAttachments = false;
		_unread = true;
		_deletedBySender = false;
		_deletedByReceiver = false;
		_reqAdena = reqAdena;
	}

	/*
	 * This constructor used for System Mails
	 */
	public Message(int receiverId, String subject, String content, SendBySystem sendBySystem)
	{
		_messageId = IdFactory.getInstance().getNextId();
		_senderId = -1;
		_receiverId = receiverId;
		_subject = subject;
		_content = content;
		_expiration = System.currentTimeMillis() + EXPIRATION * 3600000;
		_reqAdena = 0;
		_hasAttachments = false;
		_unread = true;
		_deletedBySender = true;
		_deletedByReceiver = false;
		_sendBySystem = sendBySystem.ordinal();
		_returned = false;
	}

	/*
	 * This constructor used for System Mails
	 */
	public Message(int receiverId, String subject, String content, int systemMessage1, int systemMessage2)
	{
		_messageId = IdFactory.getInstance().getNextId();
		_senderId = -1;
		_receiverId = receiverId;
		_subject = subject;
		_content = content;
		_expiration = System.currentTimeMillis() + EXPIRATION * 3600000;
		_reqAdena = 0;
		_hasAttachments = false;
		_unread = true;
		_deletedBySender = true;
		_deletedByReceiver = false;
		_sendBySystem = 5;
		_returned = false;
		_systemMessage1 = systemMessage1;
		_systemMessage2 = systemMessage2;
	}

	/*
	 * This constructor used for auto-generation of the "return attachments" message
	 */
	public Message(Message msg)
	{
		_messageId = IdFactory.getInstance().getNextId();
		_senderId = msg.getSenderId();
		_receiverId = msg.getSenderId();
		_subject = "";
		_content = "";
		_expiration = System.currentTimeMillis() + EXPIRATION * 3600000;
		_unread = true;
		_deletedBySender = true;
		_deletedByReceiver = false;
		_sendBySystem = SendBySystem.NONE.ordinal();
		_returned = true;
		_reqAdena = 0;
		_hasAttachments = true;
		_attachments = msg.getAttachments();
		msg.removeAttachments();
		_attachments.setNewMessageId(_messageId);
		_unloadTask = ThreadPoolManager.getInstance().scheduleGeneral(new AttachmentsUnloadTask(this),
				UNLOAD_ATTACHMENTS_INTERVAL + Rnd.get(UNLOAD_ATTACHMENTS_INTERVAL));
	}

	public static PreparedStatement getStatement(Message msg, Connection con) throws SQLException
	{
		PreparedStatement stmt = con.prepareStatement(
				"INSERT INTO messages (messageId, senderId, receiverId, subject, content, expiration, reqAdena, hasAttachments, isUnread, isDeletedBySender, isDeletedByReceiver, sendBySystem, isReturned, systemMessage1, systemMessage2) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		stmt.setInt(1, msg._messageId);
		stmt.setInt(2, msg._senderId);
		stmt.setInt(3, msg._receiverId);
		stmt.setString(4, msg._subject);
		stmt.setString(5, msg._content);
		stmt.setLong(6, msg._expiration);
		stmt.setLong(7, msg._reqAdena);
		stmt.setString(8, String.valueOf(msg._hasAttachments));
		stmt.setString(9, String.valueOf(msg._unread));
		stmt.setString(10, String.valueOf(msg._deletedBySender));
		stmt.setString(11, String.valueOf(msg._deletedByReceiver));
		stmt.setInt(12, msg._sendBySystem);
		stmt.setString(13, String.valueOf(msg._returned));
		stmt.setInt(14, msg._systemMessage1);
		stmt.setInt(15, msg._systemMessage2);

		return stmt;
	}

	public final int getId()
	{
		return _messageId;
	}

	public final int getSenderId()
	{
		return _senderId;
	}

	public final int getReceiverId()
	{
		return _receiverId;
	}

	public final String getSenderName()
	{
		if (_senderName == null)
		{
			if (_sendBySystem != 0)
			{
				return "****";
			}

			_senderName = CharNameTable.getInstance().getNameById(_senderId);
			if (_senderName == null)
			{
				_senderName = "";
			}
		}
		return _senderName;
	}

	public final String getReceiverName()
	{
		if (_receiverName == null)
		{
			_receiverName = CharNameTable.getInstance().getNameById(_receiverId);
			if (_receiverName == null)
			{
				_receiverName = "";
			}
		}
		return _receiverName;
	}

	public final String getSubject()
	{
		return _subject;
	}

	public final String getContent()
	{
		return _content;
	}

	public final boolean isLocked()
	{
		return _reqAdena > 0;
	}

	public final long getExpiration()
	{
		return _expiration;
	}

	public final int getExpirationSeconds()
	{
		return (int) (_expiration / 1000);
	}

	public final boolean isUnread()
	{
		return _unread;
	}

	public final void markAsRead()
	{
		if (_unread)
		{
			_unread = false;
			MailManager.getInstance().markAsReadInDb(_messageId);
		}
	}

	public final boolean isDeletedBySender()
	{
		return _deletedBySender;
	}

	public final void setDeletedBySender()
	{
		if (!_deletedBySender)
		{
			_deletedBySender = true;
			if (_deletedByReceiver)
			{
				MailManager.getInstance().deleteMessageInDb(_messageId);
			}
			else
			{
				MailManager.getInstance().markAsDeletedBySenderInDb(_messageId);
			}
		}
	}

	public final boolean isDeletedByReceiver()
	{
		return _deletedByReceiver;
	}

	public final void setDeletedByReceiver()
	{
		if (!_deletedByReceiver)
		{
			_deletedByReceiver = true;
			if (_deletedBySender)
			{
				MailManager.getInstance().deleteMessageInDb(_messageId);
			}
			else
			{
				MailManager.getInstance().markAsDeletedByReceiverInDb(_messageId);
			}
		}
	}

	public final int getSendBySystem()
	{
		return _sendBySystem;
	}

	public final int getSystemMessage1()
	{
		return _systemMessage1;
	}

	public final int getSystemMessage2()
	{
		return _systemMessage2;
	}

	public final boolean isReturned()
	{
		return _returned;
	}

	public final void setIsReturned(boolean val)
	{
		_returned = val;
	}

	public final long getReqAdena()
	{
		return _reqAdena;
	}

	public final synchronized Mail getAttachments()
	{
		if (!_hasAttachments)
		{
			return null;
		}

		if (_attachments == null)
		{
			int objId = _senderId;
			if (objId < 0)
			{
				objId = _receiverId;
			}
			_attachments = new Mail(objId, _messageId);
			_attachments.restore();
			_unloadTask = ThreadPoolManager.getInstance().scheduleGeneral(new AttachmentsUnloadTask(this),
					UNLOAD_ATTACHMENTS_INTERVAL + Rnd.get(UNLOAD_ATTACHMENTS_INTERVAL));
		}
		return _attachments;
	}

	public final boolean hasAttachments()
	{
		return _hasAttachments;
	}

	public final synchronized void removeAttachments()
	{
		if (_attachments != null)
		{
			_attachments = null;
			_hasAttachments = false;
			MailManager.getInstance().removeAttachmentsInDb(_messageId);
			if (_unloadTask != null)
			{
				_unloadTask.cancel(false);
			}
		}
	}

	public final synchronized Mail createAttachments()
	{
		if (_hasAttachments || _attachments != null)
		{
			return null;
		}

		int objId = _senderId;
		if (objId < 0)
		{
			objId = _receiverId;
		}
		_attachments = new Mail(objId, _messageId);
		_hasAttachments = true;
		_unloadTask = ThreadPoolManager.getInstance().scheduleGeneral(new AttachmentsUnloadTask(this),
				UNLOAD_ATTACHMENTS_INTERVAL + Rnd.get(UNLOAD_ATTACHMENTS_INTERVAL));
		return _attachments;
	}

	protected final synchronized void unloadAttachments()
	{
		if (_attachments != null)
		{
			_attachments.deleteMe();
			_attachments = null;
		}
	}

	static class AttachmentsUnloadTask implements Runnable
	{
		private Message _msg;

		AttachmentsUnloadTask(Message msg)
		{
			_msg = msg;
		}

		@Override
		public void run()
		{
			if (_msg != null)
			{
				_msg.unloadAttachments();
				_msg = null;
			}
		}
	}
}
