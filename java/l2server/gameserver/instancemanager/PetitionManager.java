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

package l2server.gameserver.instancemanager;

import l2server.Config;
import l2server.gameserver.GmListTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;
import l2server.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Petition Manager
 *
 * @author Tempy
 */
public final class PetitionManager
{

	private Map<Integer, Petition> _pendingPetitions;
	private Map<Integer, Petition> _completedPetitions;

	private enum PetitionState
	{
		Pending,
		Responder_Cancel,
		Responder_Missing,
		Responder_Reject,
		Responder_Complete,
		Petitioner_Cancel,
		Petitioner_Missing,
		In_Process,
		Completed
	}

	private enum PetitionType
	{
		Immobility,
		Recovery_Related,
		Bug_Report,
		Quest_Related,
		Bad_User,
		Suggestions,
		Game_Tip,
		Operation_Related,
		Other
	}

	public static PetitionManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private class Petition
	{
		private long _submitTime = System.currentTimeMillis();

		private int _id;
		private PetitionType _type;
		private PetitionState _state = PetitionState.Pending;
		private String _content;

		private List<CreatureSay> _messageLog = new ArrayList<>();

		private L2PcInstance _petitioner;
		private L2PcInstance _responder;

		public Petition(L2PcInstance petitioner, String petitionText, int petitionType)
		{
			petitionType--;
			_id = IdFactory.getInstance().getNextId();
			if (petitionType >= PetitionType.values().length)
			{
				Log.warning(
						"PetitionManager:Petition : invalid petition type (received type was +1) : " + petitionType);
			}
			_type = PetitionType.values()[petitionType];
			_content = petitionText;

			_petitioner = petitioner;
		}

		protected boolean addLogMessage(CreatureSay cs)
		{
			return _messageLog.add(cs);
		}

		protected List<CreatureSay> getLogMessages()
		{
			return _messageLog;
		}

		public boolean endPetitionConsultation(PetitionState endState)
		{
			setState(endState);

			if (getResponder() != null && getResponder().isOnline())
			{
				if (endState == PetitionState.Responder_Reject)
				{
					getPetitioner().sendMessage("Your petition was rejected. Please try again later.");
				}
				else
				{
					// Ending petition consultation with <Player>.
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PETITION_ENDED_WITH_C1);
					sm.addString(getPetitioner().getName());
					getResponder().sendPacket(sm);

					if (endState == PetitionState.Petitioner_Cancel)
					{
						// Receipt No. <ID> petition cancelled.
						sm = SystemMessage.getSystemMessage(SystemMessageId.RECENT_NO_S1_CANCELED);
						sm.addNumber(getId());
						getResponder().sendPacket(sm);
					}
				}
			}

			// End petition consultation and inform them, if they are still online.
			if (getPetitioner() != null && getPetitioner().isOnline())
			{
				getPetitioner().sendPacket(
						SystemMessage.getSystemMessage(SystemMessageId.THIS_END_THE_PETITION_PLEASE_PROVIDE_FEEDBACK));
			}

			getCompletedPetitions().put(getId(), this);
			return getPendingPetitions().remove(getId()) != null;
		}

		public String getContent()
		{
			return _content;
		}

		public int getId()
		{
			return _id;
		}

		public L2PcInstance getPetitioner()
		{
			return _petitioner;
		}

		public L2PcInstance getResponder()
		{
			return _responder;
		}

		public long getSubmitTime()
		{
			return _submitTime;
		}

		public PetitionState getState()
		{
			return _state;
		}

		public String getTypeAsString()
		{
			return _type.toString().replace("_", " ");
		}

		public void sendPetitionerPacket(L2GameServerPacket responsePacket)
		{
			if (getPetitioner() == null || !getPetitioner().isOnline())
			{
				// Allows petitioners to see the results of their petition when
				// they log back into the game.

				//endPetitionConsultation(PetitionState.Petitioner_Missing);
				return;
			}

			getPetitioner().sendPacket(responsePacket);
		}

		public void sendResponderPacket(L2GameServerPacket responsePacket)
		{
			if (getResponder() == null || !getResponder().isOnline())
			{
				endPetitionConsultation(PetitionState.Responder_Missing);
				return;
			}

			getResponder().sendPacket(responsePacket);
		}

		public void setState(PetitionState state)
		{
			_state = state;
		}

		public void setResponder(L2PcInstance respondingAdmin)
		{
			if (getResponder() != null)
			{
				return;
			}

			_responder = respondingAdmin;
		}
	}

	private PetitionManager()
	{
		Log.info("Initializing PetitionManager");
		_pendingPetitions = new HashMap<>();
		_completedPetitions = new HashMap<>();
	}

	public void clearCompletedPetitions()
	{
		int numPetitions = getPendingPetitionCount();

		getCompletedPetitions().clear();
		Log.info("PetitionManager: Completed petition data cleared. " + numPetitions + " petition(s) removed.");
	}

	public void clearPendingPetitions()
	{
		int numPetitions = getPendingPetitionCount();

		getPendingPetitions().clear();
		Log.info("PetitionManager: Pending petition queue cleared. " + numPetitions + " petition(s) removed.");
	}

	public boolean acceptPetition(L2PcInstance respondingAdmin, int petitionId)
	{
		if (!isValidPetition(petitionId))
		{
			return false;
		}

		Petition currPetition = getPendingPetitions().get(petitionId);

		if (currPetition.getResponder() != null)
		{
			return false;
		}

		currPetition.setResponder(respondingAdmin);
		currPetition.setState(PetitionState.In_Process);

		// Petition application accepted. (Send to Petitioner)
		currPetition.sendPetitionerPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_APP_ACCEPTED));

		// Petition application accepted. Reciept No. is <ID>
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PETITION_ACCEPTED_RECENT_NO_S1);
		sm.addNumber(currPetition.getId());
		currPetition.sendResponderPacket(sm);

		// Petition consultation with <Player> underway.
		sm = SystemMessage.getSystemMessage(SystemMessageId.STARTING_PETITION_WITH_C1);
		sm.addString(currPetition.getPetitioner().getName());
		currPetition.sendResponderPacket(sm);
		return true;
	}

	public boolean cancelActivePetition(L2PcInstance player)
	{
		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition.getPetitioner() != null &&
					currPetition.getPetitioner().getObjectId() == player.getObjectId())
			{
				return currPetition.endPetitionConsultation(PetitionState.Petitioner_Cancel);
			}

			if (currPetition.getResponder() != null &&
					currPetition.getResponder().getObjectId() == player.getObjectId())
			{
				return currPetition.endPetitionConsultation(PetitionState.Responder_Cancel);
			}
		}

		return false;
	}

	public void checkPetitionMessages(L2PcInstance petitioner)
	{
		if (petitioner != null)
		{
			for (Petition currPetition : getPendingPetitions().values())
			{
				if (currPetition == null)
				{
					continue;
				}

				if (currPetition.getPetitioner() != null &&
						currPetition.getPetitioner().getObjectId() == petitioner.getObjectId())
				{
					for (CreatureSay logMessage : currPetition.getLogMessages())
					{
						petitioner.sendPacket(logMessage);
					}

					return;
				}
			}
		}
	}

	public boolean endActivePetition(L2PcInstance player)
	{
		if (!player.isGM())
		{
			return false;
		}

		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition == null)
			{
				continue;
			}

			if (currPetition.getResponder() != null &&
					currPetition.getResponder().getObjectId() == player.getObjectId())
			{
				return currPetition.endPetitionConsultation(PetitionState.Completed);
			}
		}

		return false;
	}

	protected Map<Integer, Petition> getCompletedPetitions()
	{
		return _completedPetitions;
	}

	protected Map<Integer, Petition> getPendingPetitions()
	{
		return _pendingPetitions;
	}

	public int getPendingPetitionCount()
	{
		return getPendingPetitions().size();
	}

	public int getPlayerTotalPetitionCount(L2PcInstance player)
	{
		if (player == null)
		{
			return 0;
		}

		int petitionCount = 0;

		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition == null)
			{
				continue;
			}

			if (currPetition.getPetitioner() != null &&
					currPetition.getPetitioner().getObjectId() == player.getObjectId())
			{
				petitionCount++;
			}
		}

		for (Petition currPetition : getCompletedPetitions().values())
		{
			if (currPetition == null)
			{
				continue;
			}

			if (currPetition.getPetitioner() != null &&
					currPetition.getPetitioner().getObjectId() == player.getObjectId())
			{
				petitionCount++;
			}
		}

		return petitionCount;
	}

	public boolean isPetitionInProcess()
	{
		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition == null)
			{
				continue;
			}

			if (currPetition.getState() == PetitionState.In_Process)
			{
				return true;
			}
		}

		return false;
	}

	public boolean isPetitionInProcess(int petitionId)
	{
		if (!isValidPetition(petitionId))
		{
			return false;
		}

		Petition currPetition = getPendingPetitions().get(petitionId);
		return currPetition.getState() == PetitionState.In_Process;
	}

	public boolean isPlayerInConsultation(L2PcInstance player)
	{
		if (player != null)
		{
			for (Petition currPetition : getPendingPetitions().values())
			{
				if (currPetition == null)
				{
					continue;
				}

				if (currPetition.getState() != PetitionState.In_Process)
				{
					continue;
				}

				if (currPetition.getPetitioner() != null &&
						currPetition.getPetitioner().getObjectId() == player.getObjectId() ||
						currPetition.getResponder() != null &&
								currPetition.getResponder().getObjectId() == player.getObjectId())
				{
					return true;
				}
			}
		}

		return false;
	}

	public boolean isPetitioningAllowed()
	{
		return Config.PETITIONING_ALLOWED;
	}

	public boolean isPlayerPetitionPending(L2PcInstance petitioner)
	{
		if (petitioner != null)
		{
			for (Petition currPetition : getPendingPetitions().values())
			{
				if (currPetition == null)
				{
					continue;
				}

				if (currPetition.getPetitioner() != null &&
						currPetition.getPetitioner().getObjectId() == petitioner.getObjectId())
				{
					return true;
				}
			}
		}

		return false;
	}

	private boolean isValidPetition(int petitionId)
	{
		return getPendingPetitions().containsKey(petitionId);
	}

	public boolean rejectPetition(L2PcInstance respondingAdmin, int petitionId)
	{
		if (!isValidPetition(petitionId))
		{
			return false;
		}

		Petition currPetition = getPendingPetitions().get(petitionId);

		if (currPetition.getResponder() != null)
		{
			return false;
		}

		currPetition.setResponder(respondingAdmin);
		return currPetition.endPetitionConsultation(PetitionState.Responder_Reject);
	}

	public boolean sendActivePetitionMessage(L2PcInstance player, String messageText)
	{
		//if (!isPlayerInConsultation(player))
		//return false;

		CreatureSay cs;

		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition == null)
			{
				continue;
			}

			if (currPetition.getPetitioner() != null &&
					currPetition.getPetitioner().getObjectId() == player.getObjectId())
			{
				cs = new CreatureSay(player.getObjectId(), Say2.PETITION_PLAYER, player.getName(), messageText);
				currPetition.addLogMessage(cs);

				currPetition.sendResponderPacket(cs);
				currPetition.sendPetitionerPacket(cs);
				return true;
			}

			if (currPetition.getResponder() != null &&
					currPetition.getResponder().getObjectId() == player.getObjectId())
			{
				cs = new CreatureSay(player.getObjectId(), Say2.PETITION_GM, player.getName(), messageText);
				currPetition.addLogMessage(cs);

				currPetition.sendResponderPacket(cs);
				currPetition.sendPetitionerPacket(cs);
				return true;
			}
		}

		return false;
	}

	public void sendPendingPetitionList(L2PcInstance activeChar)
	{
		final StringBuilder htmlContent = StringUtil.startAppend(600 + getPendingPetitionCount() * 300,
				"<html><body><center><table width=270><tr>" +
						"<td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" +
						"<td width=180><center>Petition Menu</center></td>" +
						"<td width=45><button value=\"Back\" action=\"bypass -h admin_admin7\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br>" +
						"<table width=\"270\">" +
						"<tr><td><table width=\"270\"><tr><td><button value=\"Reset\" action=\"bypass -h admin_reset_petitions\" width=\"80\" height=\"21\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" +
						"<td align=right><button value=\"Refresh\" action=\"bypass -h admin_view_petitions\" width=\"80\" height=\"21\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br></td></tr>");

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		if (getPendingPetitionCount() == 0)
		{
			htmlContent.append("<tr><td>There are no currently pending petitions.</td></tr>");
		}
		else
		{
			htmlContent.append("<tr><td><font color=\"LEVEL\">Current Petitions:</font><br></td></tr>");
		}

		boolean color = true;
		int petcount = 0;
		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition == null)
			{
				continue;
			}

			StringUtil.append(htmlContent, "<tr><td width=\"270\"><table width=\"270\" cellpadding=\"2\" bgcolor=",
					color ? "131210" : "444444", "><tr><td width=\"130\">",
					dateFormat.format(new Date(currPetition.getSubmitTime())));
			StringUtil.append(htmlContent, "</td><td width=\"140\" align=right><font color=\"",
					currPetition.getPetitioner().isOnline() ? "00FF00" : "999999", "\">",
					currPetition.getPetitioner().getName(), "</font></td></tr>");
			StringUtil.append(htmlContent, "<tr><td width=\"130\">");
			if (currPetition.getState() != PetitionState.In_Process)
			{
				StringUtil.append(htmlContent, "<table width=\"130\" cellpadding=\"2\"><tr>" +
								"<td><button value=\"View\" action=\"bypass -h admin_view_petition ",
						String.valueOf(currPetition.getId()),
						"\" width=\"50\" height=\"21\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" +
								"<td><button value=\"Reject\" action=\"bypass -h admin_reject_petition ",
						String.valueOf(currPetition.getId()),
						"\" width=\"50\" height=\"21\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");
			}
			else
			{
				htmlContent.append("<font color=\"" + (currPetition.getResponder().isOnline() ? "00FF00" : "999999") +
						"\">" + currPetition.getResponder().getName() + "</font>");
			}
			StringUtil.append(htmlContent, "</td>", currPetition.getTypeAsString(), "<td width=\"140\" align=right>",
					currPetition.getTypeAsString(), "</td></tr></table></td></tr>");
			color = !color;
			petcount++;
			if (petcount > 10)
			{
				htmlContent
						.append("<tr><td><font color=\"LEVEL\">There is more pending petition...</font><br></td></tr>");
				break;
			}
		}

		htmlContent.append("</table></center></body></html>");

		NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
		htmlMsg.setHtml(htmlContent.toString());
		activeChar.sendPacket(htmlMsg);
	}

	public int submitPetition(L2PcInstance petitioner, String petitionText, int petitionType)
	{
		// Create a new petition instance and add it to the list of pending petitions.
		Petition newPetition = new Petition(petitioner, petitionText, petitionType);
		int newPetitionId = newPetition.getId();
		getPendingPetitions().put(newPetitionId, newPetition);

		// Notify all GMs that a new petition has been submitted.
		String msgContent = petitioner.getName() + " has submitted a new petition."; //(ID: " + newPetitionId + ").";
		GmListTable.broadcastToGMs(
				new CreatureSay(petitioner.getObjectId(), Say2.HERO_VOICE, "Petition System", msgContent));

		return newPetitionId;
	}

	public void viewPetition(L2PcInstance activeChar, int petitionId)
	{
		if (!activeChar.isGM())
		{
			return;
		}

		if (!isValidPetition(petitionId))
		{
			return;
		}

		Petition currPetition = getPendingPetitions().get(petitionId);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(activeChar.getHtmlPrefix(), "admin/petition.htm");
		html.replace("%petition%", String.valueOf(currPetition.getId()));
		html.replace("%time%", dateFormat.format(new Date(currPetition.getSubmitTime())));
		html.replace("%type%", currPetition.getTypeAsString());
		html.replace("%petitioner%", currPetition.getPetitioner().getName());
		html.replace("%online%", currPetition.getPetitioner().isOnline() ? "00FF00" : "999999");
		html.replace("%text%", currPetition.getContent());

		activeChar.sendPacket(html);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final PetitionManager _instance = new PetitionManager();
	}
}
