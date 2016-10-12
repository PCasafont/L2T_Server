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

package l2server.gameserver.model.olympiad;

import l2server.Config;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.instancemanager.AntiFeedManager;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;

import java.util.*;

/**
 * @author DS
 */
public class OlympiadManager
{
	private List<Integer> _nonClassBasedRegisters;
	private Map<Integer, List<Integer>> _classBasedRegisters;

	private OlympiadManager()
	{
		_nonClassBasedRegisters = new ArrayList<>();
		_classBasedRegisters = new LinkedHashMap<>();
	}

	public static OlympiadManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public final List<Integer> getRegisteredNonClassBased()
	{
		return _nonClassBasedRegisters;
	}

	public final Map<Integer, List<Integer>> getRegisteredClassBased()
	{
		return _classBasedRegisters;
	}

	protected final List<List<Integer>> hasEnoughRegisteredClassed()
	{
		List<List<Integer>> result = null;
		for (Map.Entry<Integer, List<Integer>> classList : _classBasedRegisters.entrySet())
		{
			if (classList.getValue() != null && classList.getValue().size() >= Config.ALT_OLY_CLASSED)
			{
				if (result == null)
				{
					result = new ArrayList<>();
				}

				result.add(classList.getValue());
			}
		}
		return result;
	}

	protected final boolean hasEnoughRegisteredNonClassed()
	{
		return _nonClassBasedRegisters.size() >= Config.ALT_OLY_NONCLASSED;
	}

	protected final void clearRegistered()
	{
		_nonClassBasedRegisters.clear();
		_classBasedRegisters.clear();
		AntiFeedManager.getInstance().clear(AntiFeedManager.OLYMPIAD_ID);
	}

	public final boolean isRegistered(L2PcInstance player)
	{
		return isRegistered(player, false);
	}

	private boolean isRegistered(L2PcInstance player, boolean showMessage)
	{
		final Integer objId = player.getObjectId();
		// party may be already dispersed

		if (_nonClassBasedRegisters.contains(objId))
		{
			if (showMessage)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(
						SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_NON_CLASS_LIMITED_MATCH_WAITING_LIST);
				sm.addPcName(player);
				player.sendPacket(sm);
			}
			return true;
		}

		if (player.getCurrentClass().getParent() == null)
		{
			return false;
		}

		final List<Integer> classed =
				_classBasedRegisters.get(player.getCurrentClass().getParent().getAwakeningClassId());
		if (classed != null && classed.contains(objId))
		{
			if (showMessage)
			{
				final SystemMessage sm = SystemMessage
						.getSystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_CLASS_MATCH_WAITING_LIST);
				sm.addPcName(player);
				player.sendPacket(sm);
			}
			return true;
		}

		return false;
	}

	public final boolean isRegisteredInComp(L2PcInstance player)
	{
		return isRegistered(player, false) || isInCompetition(player, false);
	}

	public final boolean isInCompetition(L2PcInstance player, boolean showMessage)
	{
		if (!Olympiad._inCompPeriod)
		{
			return false;
		}

		AbstractOlympiadGame game;
		for (int i = OlympiadGameManager.getInstance().getNumberOfStadiums(); --i >= 0; )
		{
			game = OlympiadGameManager.getInstance().getOlympiadTask(i).getGame();
			if (game == null)
			{
				continue;
			}

			if (game.containsParticipant(player.getObjectId()))
			{
				if (!showMessage)
				{
					return true;
				}

				switch (game.getType())
				{
					case CLASSED:
					{
						final SystemMessage sm = SystemMessage.getSystemMessage(
								SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_CLASS_MATCH_WAITING_LIST);
						sm.addPcName(player);
						player.sendPacket(sm);
						break;
					}
					case NON_CLASSED:
					{
						final SystemMessage sm = SystemMessage.getSystemMessage(
								SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_NON_CLASS_LIMITED_MATCH_WAITING_LIST);
						sm.addPcName(player);
						player.sendPacket(sm);
						break;
					}
				}
				return true;
			}
		}
		return false;
	}

	public final boolean registerNoble(L2PcInstance player)
	{
		CompetitionType type = CompetitionType.CLASSED;

		if (Config.isServer(Config.TENKAI_ESTHUS))
		{
			if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY)
			{
				type = CompetitionType.NON_CLASSED;
			}
		}
		else
		{
			if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) > 7)
			{
				type = CompetitionType.NON_CLASSED;
			}
		}

		SystemMessage sm;
		if (!Olympiad._inCompPeriod)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
			player.sendPacket(sm);
			return false;
		}

		if (Olympiad.getInstance().getMillisToCompEnd() < 600000)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.GAME_REQUEST_CANNOT_BE_MADE);
			player.sendPacket(sm);
			return false;
		}

		if (player.isCursedWeaponEquipped())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_REGISTER_PROCESSING_CURSED_WEAPON);
			player.sendPacket(sm);
			return false;
		}

		OlympiadNobleInfo nobleInfo = checkNoble(player);
		if (nobleInfo == null)
		{
			return false;
		}

		// TODO: Apply retail error messages
		if (nobleInfo.getMatchesThisWeek() >= Olympiad.MAX_WEEKLY_MATCHES)
		{
			player.sendMessage("Cannot join more matches.");
			return false;
		}

		switch (type)
		{
			case CLASSED:
			{
				int classId = player.getCurrentClass().getParent().getAwakeningClassId();
				List<Integer> classed = _classBasedRegisters.get(classId);
				if (classed != null)
				{
					addPlayer(classed, nobleInfo);
				}
				else
				{
					classed = new ArrayList<>();
					classed.add(player.getObjectId());
					_classBasedRegisters.put(classId, classed);
				}

				sm = SystemMessage.getSystemMessage(
						SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES);
				player.sendPacket(sm);
				break;
			}
			case NON_CLASSED:
			{

				addPlayer(_nonClassBasedRegisters, nobleInfo);
				sm = SystemMessage
						.getSystemMessage(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES);
				player.sendPacket(sm);
				break;
			}
		}
		return true;
	}

	public final boolean unRegisterNoble(L2PcInstance player)
	{
		SystemMessage sm;
		if (!Olympiad._inCompPeriod)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
			player.sendPacket(sm);
			return false;
		}

		if (!player.isNoble())
		{
			sm = SystemMessage.getSystemMessage(
					SystemMessageId.C1_DOES_NOT_MEET_REQUIREMENTS_ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
			sm.addString(player.getName());
			player.sendPacket(sm);
			return false;
		}

		if (!isRegistered(player, false))
		{
			sm = SystemMessage
					.getSystemMessage(SystemMessageId.YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME);
			player.sendPacket(sm);
			return false;
		}

		if (isInCompetition(player, false))
		{
			return false;
		}

		sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);
		Integer objId = player.getObjectId();
		if (_nonClassBasedRegisters.remove(objId))
		{
			if (Config.L2JMOD_DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP > 0)
			{
				AntiFeedManager.getInstance().removePlayer(AntiFeedManager.OLYMPIAD_ID, player);
			}

			player.sendPacket(sm);
			return true;
		}

		int classId = player.getCurrentClass().getParent().getAwakeningClassId();
		final List<Integer> classed = _classBasedRegisters.get(classId);
		if (classed != null && classed.remove(objId))
		{
			_classBasedRegisters.remove(classId);
			_classBasedRegisters.put(classId, classed);

			if (Config.L2JMOD_DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP > 0)
			{
				AntiFeedManager.getInstance().removePlayer(AntiFeedManager.OLYMPIAD_ID, player);
			}

			player.sendPacket(sm);
			return true;
		}
		return false;
	}

	public final void removeDisconnectedCompetitor(L2PcInstance player)
	{
		final OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(player.getOlympiadGameId());
		if (task != null && task.isGameStarted())
		{
			task.getGame().handleDisconnect(player);
		}

		final Integer objId = player.getObjectId();
		if (_nonClassBasedRegisters.remove(objId))
		{
			return;
		}

		if (player.getCurrentClass().getParent() == null)
		{
			return;
		}

		final List<Integer> classed =
				_classBasedRegisters.get(player.getCurrentClass().getParent().getAwakeningClassId());
		if (classed != null && classed.remove(objId))
		{
		}
	}

	/**
	 * @param player - messages will be sent to this L2PcInstance
	 * @return true if all requirements are met
	 */
	// TODO: move to the bypass handler after reworking points system
	private OlympiadNobleInfo checkNoble(L2PcInstance player)
	{
		SystemMessage sm;
		if (!player.isNoble())
		{
			sm = SystemMessage.getSystemMessage(
					SystemMessageId.C1_DOES_NOT_MEET_REQUIREMENTS_ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
			sm.addPcName(player);
			player.sendPacket(sm);
			return null;
		}

		if (player.getCurrentClass().getLevel() < 85 || player.getCurrentClass().getParent() == null)
		{
			//TODO correct system message
			//sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_CLASS_CHARACTER);
			//sm.addPcName(noble);
			//player.sendPacket(sm);
			player.sendMessage("Only awakened characters can participate in the Grand Olympiad.");
			return null;
		}

		if (player.isSubClassActive())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_CLASS_CHARACTER);
			sm.addPcName(player);
			player.sendPacket(sm);
			return null;
		}

		if (player.isCursedWeaponEquipped())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_JOIN_OLYMPIAD_POSSESSING_S2);
			sm.addPcName(player);
			sm.addItemName(player.getCursedWeaponEquippedId());
			player.sendPacket(sm);
			return null;
		}

		if (!player.isInventoryUnder90(true))
		{
			// TODO: Fix this message!
			// If SystemMessage will be YOU_CAN_PROCEED_WHEN_WHEIGHT_BELOW_80_AND_QUANTITY_90 then you can't add noble name
			// but if C1_CANNOT_PARTICIPATE_IN_OLYMPIAD_INVENTORY_SLOT_EXCEEDS_80_PERCENT I don't think it will be new and good...
			sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_CAN_PROCEED_WHEN_WHEIGHT_BELOW_80_AND_QUANTITY_90);
			//sm.addPcName(noble);
			player.sendPacket(sm);
			return null;
		}

		if (EventsManager.getInstance().isPlayerParticipant(player.getObjectId()))
		{
			player.sendMessage("You can't join olympiad while participating on event.");
			return null;
		}

		//If we have access to the player HWID we will use it to check dualbox otherwise we will use the external ip and the internal ip
		/*for (L2PcInstance pl : L2World.getInstance().getAllOlympiadPlayers())
        {
			if (pl == null)
				continue;
			if ((player.getHWID() != null) && (pl.getHWID() != null))
			{
				if (pl.getHWID().equalsIgnoreCase(player.getHWID()) && pl.getInternalIP().equalsIgnoreCase(player.getInternalIP()))
				{
					player.sendMessage("Dual Box is not allowed on the Olympiad Games!");
					return null;
				}
			}
			else
			{
				if (pl.getExternalIP().equalsIgnoreCase(player.getExternalIP()) && pl.getInternalIP().equalsIgnoreCase(player.getInternalIP()))
				{
					player.sendMessage("Dual Box is not allowed on the Olympiad Games!");
					return null;
				}
			}
		}*/

		if (isRegistered(player, true))
		{
			return null;
		}

		if (isInCompetition(player, true))
		{
			return null;
		}

		OlympiadNobleInfo nobleInfo = Olympiad.getInstance().getNobleInfo(player.getObjectId());
		if (nobleInfo == null)
		{
			nobleInfo = new OlympiadNobleInfo(player.getObjectId(), player.getName(), player.getClassId());
			Olympiad.getInstance().addNoble(player.getObjectId(), nobleInfo);
		}

		if (nobleInfo.getPoints() <= 0)
		{
			NpcHtmlMessage message = new NpcHtmlMessage(0);
			message.setFile(player.getHtmlPrefix(), "olympiad/noble_nopoints1.htm");
			player.sendPacket(message);
			return null;
		}

		if (Config.L2JMOD_DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP > 0 && !AntiFeedManager.getInstance()
				.tryAddPlayer(AntiFeedManager.OLYMPIAD_ID, player,
						Config.L2JMOD_DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP))
		{
			NpcHtmlMessage message = new NpcHtmlMessage(0);
			message.setFile(player.getHtmlPrefix(), "mods/OlympiadIPRestriction.htm");
			message.replace("%max%", String.valueOf(AntiFeedManager.getInstance()
					.getLimit(player, Config.L2JMOD_DUALBOX_CHECK_MAX_OLYMPIAD_PARTICIPANTS_PER_IP)));
			player.sendPacket(message);
			return null;
		}

		return nobleInfo;
	}

	private void addPlayer(List<Integer> list, OlympiadNobleInfo nobleInfo)
	{
		int points = nobleInfo.getPoints();
		for (int i = 0; i < list.size(); i++)
		{
			OlympiadNobleInfo oni = Olympiad.getInstance().getNobleInfo(list.get(i));
			if (points < oni.getPoints())
			{
				list.add(i, nobleInfo.getId());
				return;
			}
		}
		list.add(nobleInfo.getId());
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final OlympiadManager _instance = new OlympiadManager();
	}
}
