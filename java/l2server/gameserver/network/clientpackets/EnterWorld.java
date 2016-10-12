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

package l2server.gameserver.network.clientpackets;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.Announcements;
import l2server.gameserver.GmListTable;
import l2server.gameserver.LoginServerThread;
import l2server.gameserver.TaskPriority;
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.communitybbs.CommunityBoard;
import l2server.gameserver.datatables.*;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.instancemanager.*;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.stat.PcStat;
import l2server.gameserver.model.base.PlayerClass;
import l2server.gameserver.model.entity.*;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.util.Util;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.logging.Level;

/**
 * Enter World Packet Handler<p>
 * <p>
 * 0000: 03 <p>
 * packet format rev87 bddddbdcccccccccccccccccccc
 * <p>
 */
public class EnterWorld extends L2GameClientPacket
{

	private int[][] tracert = new int[5][4];

	public TaskPriority getPriority()
	{
		return TaskPriority.PR_URGENT;
	}

	@Override
	protected void readImpl()
	{
		for (int i = 0; i < 5; i++)
		{
			for (int o = 0; o < 4; o++)
			{
				tracert[i][o] = readC();
			}
		}

		readB(new byte[32]); // Unknown Byte Array
		readD(); // Unknown Value 0123
		readD(); // Unknown Value 4567
		readD(); // Unknown Value 89ab
		readD(); // Unknown Value cdef
		readB(new byte[32]); // Unknown Byte Array
		readD(); // Unknown Value 6784
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
		{
			Log.warning("EnterWorld failed! activeChar returned 'null'.");
			getClient().closeNow();
			return;
		}

		String[] adress = new String[5];
		for (int i = 0; i < 5; i++)
		{
			adress[i] = tracert[i][0] + "." + tracert[i][1] + "." + tracert[i][2] + "." + tracert[i][3];
		}

		LoginServerThread.getInstance().sendClientTracert(activeChar.getAccountName(), adress);

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("REPLACE INTO account_gsdata(account_name,var,value) VALUES(?,?,?)");
			statement.setString(1, activeChar.getAccountName());
			statement.setString(2, "lastHWId");
			statement.setString(3, getClient().getHWId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Could not save client's HWId: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		getClient().setClientTracert(tracert);

		// Restore to instanced area if enabled
		if (Config.RESTORE_PLAYER_INSTANCE)
		{
			activeChar.setInstanceId(InstanceManager.getInstance().getPlayerInstance(activeChar.getObjectId()));
		}
		else
		{
			int instanceId = InstanceManager.getInstance().getPlayerInstance(activeChar.getObjectId());
			if (instanceId > 0)
			{
				InstanceManager.getInstance().getInstance(instanceId).removePlayer(activeChar.getObjectId());
			}
		}

		if (L2World.getInstance().findObject(activeChar.getObjectId()) != null)
		{
			if (Config.DEBUG)
			{
				Log.warning("User already exists in Object ID map! User " + activeChar.getName() +
						" is a character clone.");
			}
		}

		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			sendPacket(new ExCastleTendency(castle.getCastleId(), castle.getTendency()));
		}

		for (int i = 0; i < 4; i++)
		{
			sendPacket(new ExAutoSoulShot(0, 1, i));
		}

		activeChar.checkAutoShots();

		// Apply special GM properties to the GM when entering
		if (activeChar.isGM())
		{
			if (Config.GM_STARTUP_INVULNERABLE &&
					AdminCommandAccessRights.getInstance().hasAccess("admin_invul", activeChar.getAccessLevel()))
			{
				activeChar.setIsInvul(true);
			}

			if (Config.GM_STARTUP_INVISIBLE &&
					AdminCommandAccessRights.getInstance().hasAccess("admin_invisible", activeChar.getAccessLevel()))
			{
				activeChar.getAppearance().setInvisible();
			}

			if (Config.GM_STARTUP_SILENCE &&
					AdminCommandAccessRights.getInstance().hasAccess("admin_silence", activeChar.getAccessLevel()))
			{
				activeChar.setSilenceMode(true);
			}

			if (Config.GM_STARTUP_DIET_MODE &&
					AdminCommandAccessRights.getInstance().hasAccess("admin_diet", activeChar.getAccessLevel()))
			{
				activeChar.setDietMode(true);
				activeChar.refreshOverloaded();
			}

			if (Config.GM_STARTUP_AUTO_LIST &&
					AdminCommandAccessRights.getInstance().hasAccess("admin_gmliston", activeChar.getAccessLevel()) &&
					activeChar.getAccessLevel().getLevel() != 127)
			{
				GmListTable.getInstance().addGm(activeChar, false);
			}
			else
			{
				GmListTable.getInstance().addGm(activeChar, true);
			}

			if (Config.GM_GIVE_SPECIAL_SKILLS)
			{
				GMSkillTable.getInstance().addSkills(activeChar);
			}
		}

		// Set dead status if applies
		if (activeChar.getCurrentHp() < 0.5)
		{
			activeChar.setIsDead(true);
		}

		boolean showClanNotice = false;

		// Clan related checks are here
		if (activeChar.getClan() != null)
		{
			activeChar.sendPacket(new PledgeSkillList(activeChar.getClan()));

			notifyClanMembers(activeChar);

			notifySponsorOrApprentice(activeChar);

			ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan());

			if (clanHall != null)
			{
				if (!clanHall.getPaid())
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(
							SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW));
				}
			}

			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
				{
					continue;
				}

				if (siege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
					activeChar.setSiegeSide(siege.getCastle().getCastleId());
				}

				else if (siege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 2);
					activeChar.setSiegeSide(siege.getCastle().getCastleId());
				}
			}

			for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
				{
					continue;
				}

				if (siege.checkIsAttacker(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 1);
					activeChar.setSiegeSide(siege.getFort().getFortId());
				}

				else if (siege.checkIsDefender(activeChar.getClan()))
				{
					activeChar.setSiegeState((byte) 2);
					activeChar.setSiegeSide(siege.getFort().getFortId());
				}
			}

			sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), activeChar));
			sendPacket(new PledgeStatusChanged(activeChar.getClan()));

			// Residential skills support
			if (activeChar.getClan().getHasCastle() > 0)
			{
				CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).giveResidentialSkills(activeChar);
			}

			if (activeChar.getClan().getHasFort() > 0)
			{
				FortManager.getInstance().getFortByOwner(activeChar.getClan()).giveResidentialSkills(activeChar);
			}

			showClanNotice = activeChar.getClan().isNoticeEnabled();

			if (activeChar.getSkillLevelHash(19009) >= 1)
			{
				L2Skill clanAdvent = SkillTable.getInstance().getInfo(19009, 1);
				if (activeChar.isClanLeader())
				{
					for (L2ClanMember member : activeChar.getClan().getMembers())
					{
						if (member.getPlayerInstance() != null && clanAdvent
								.checkCondition(member.getPlayerInstance(), member.getPlayerInstance(), false))
						{
							clanAdvent.getEffects(member.getPlayerInstance(), member.getPlayerInstance());
						}
					}
				}
				else if (activeChar.getClan().getLeader() != null && activeChar.getClan().getLeader().isOnline())
				{
					clanAdvent.getEffects(activeChar, activeChar);
				}
			}
		}

		activeChar.sendPacket(new ExPeriodicHenna());

		activeChar.checkRecoBonusTask();

		activeChar.broadcastUserInfo();

		// Send Macro List
		activeChar.getMacroses().sendUpdate();

		// Send Item List
		sendPacket(new ItemList(activeChar, false));

		// Send GG check
		//activeChar.queryGameGuard();

		// Send Teleport Bookmark List
		sendPacket(new ExGetBookMarkInfoPacket(activeChar));

		// Send Shortcuts
		sendPacket(new ShortCutInit(activeChar));

		// Send Action list
		activeChar.sendPacket(ExBasicActionList.getStaticPacket(activeChar));

		// Send Skill list
		activeChar.sendSkillList();

		// Send Dye Information
		activeChar.sendPacket(new HennaInfo(activeChar));

		Quest.playerEnter(activeChar);

		if (Config.isServer(Config.TENKAI) || Config.IS_CLASSIC)
		{
			ImageTable.getInstance().sendImages(activeChar);
		}

		if (!Config.DISABLE_TUTORIAL)
		{
			loadTutorial(activeChar);
		}
		if (Config.isServer(Config.TENKAI) && activeChar.getLevel() == 1)
		{
			CommunityBoard.getInstance().handleCommands(getClient(), Config.BBS_DEFAULT);
		}

		for (Quest quest : QuestManager.getInstance().getAllManagedScripts())
		{
			if (quest != null && quest.getOnEnterWorld())
			{
				quest.notifyEnterWorld(activeChar);
			}
		}
		activeChar.sendPacket(new QuestList());
		activeChar.sendPacket(new ExBrPremiumState(activeChar.getObjectId(), 0));

		if (Config.PLAYER_SPAWN_PROTECTION > 0)
		{
			activeChar.setProtection(true);
		}

		byte temporaryLevel = activeChar.getTemporaryLevelToApply();
		if (temporaryLevel != 0)
		{
			activeChar.setTemporaryLevel(temporaryLevel);
		}

		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());

		// Wedding Checks
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			engage(activeChar);
			notifyPartner(activeChar, activeChar.getPartnerId());
		}

		if (activeChar.isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().getCursedWeapon(activeChar.getCursedWeaponEquippedId()).cursedOnLogin();
		}

		activeChar.updateEffectIcons();
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));
		activeChar.sendPacket(new MagicAndSkillUse(activeChar));

		//Expand Skill
		activeChar.sendPacket(new ExStorageMaxCount(activeChar));
		sendPacket(new FriendList(activeChar));

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN);
		sm.addString(activeChar.getName());
		for (int id : activeChar.getFriendList())
		{
			L2Object obj = L2World.getInstance().findObject(id);
			if (obj != null)
			{
				obj.sendPacket(sm);
			}
		}

		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WELCOME_TO_LINEAGE));
		/*
        if (activeChar.getServerInstanceId() == 1)
		{
			SystemMessage s = SystemMessage.getSystemMessage(SystemMessageId.RED_CHATBOX_S1);
			s.addString("This new dimension is not yet open. You will now be disconnected.");
			activeChar.sendPacket(s);

			activeChar.sendPacket(new ExShowScreenMessage("This dimension is not yet open. It's going live on the 29 August 2015!", 3600000));

			activeChar.getClient().setDetached(true);
			return;
		}*/

		/*activeChar.sendMessage(getText("VGhpcyBzZXJ2ZXIgdXNlcyBMMkosIGEgcHJvamVjdCBmb3VuZGVkIGJ5IEwyQ2hlZg==\n"));
		activeChar.sendMessage(getText("YW5kIGRldmVsb3BlZCBieSB0aGUgTDJKIERldiBUZWFtIGF0IGwyanNlcnZlci5jb20=\n"));

		if (Config.DISPLAY_SERVER_VERSION)
		{
			if (Config.SERVER_VERSION != null)
				activeChar.sendMessage(getText("TDJKIFNlcnZlciBWZXJzaW9uOg==")+" "+Config.SERVER_VERSION);

			if (Config.DATAPACK_VERSION != null)
				activeChar.sendMessage(getText("TDJKIERhdGFwYWNrIFZlcnNpb246")+" "+Config.DATAPACK_VERSION);
		}
		activeChar.sendMessage(getText("Q29weXJpZ2h0IDIwMDQtMjAxMA==\n"));*/

		Announcements.getInstance().showAnnouncements(activeChar);

		if (!CharacterCreate.isValidName(activeChar.getName()) && !activeChar.getName().contains("Khadia_Gift_"))
		{
			activeChar.setMovieId(1000);
			sendPacket(new NpcHtmlMessage(0, "<html><body><center>" + "Your nickname contains illegal letters!<br>" +
					"Let's decide another nickname for this character:<br>" +
					"<edit var=text width=130 height=11 length=26><br>" +
					"<button value=\"Done\" action=\"bypass NickName $text\" back=\"l2ui_ct1.button_df\" width=65 height=20 fore=\"l2ui_ct1.button_df\">" +
					"</center></body></html>"));
		}
		else if (SurveyManager.getInstance().isActive() &&
				!SurveyManager.getInstance().playerAnswered(activeChar.getObjectId()))
		{
			String html = "<html><title>Survey System</title><body>" + SurveyManager.getInstance().getQuestion() +
					" <button value=\"More Info\" action=\"bypass -h SurveyInfo\" width=70 height=20 fore=\"L2UI_ct1.button_df\"><br>" +
					"<table width=260>";
			for (int answerId : SurveyManager.getInstance().getPossibleAnswerIds())
			{
				html += "<tr><td><button value=\"" + SurveyManager.getInstance().getPossibleAnswer(answerId) +
						"\" action=\"bypass -h SurveyAnswer " + answerId +
						"\" width=250 height=25 fore=\"L2UI_ct1.button_df\"></td></tr>";
			}
			html += "</table></body></html>";
			sendPacket(new NpcHtmlMessage(0, html));
		}
		else if (showClanNotice)
		{
			try
			{
				NpcHtmlMessage notice = new NpcHtmlMessage(1);
				notice.setFile(activeChar.getHtmlPrefix(), "clanNotice.htm");
				notice.replace("%clan_name%", activeChar.getClan().getName());
				notice.replace("%notice_text%", activeChar.getClan().getNotice().replaceAll("\r\n", "<br>"));
				notice.disableValidation();
				sendPacket(notice);
			}
			catch (Exception e)
			{
				activeChar.getClan().setNotice("");
				e.printStackTrace();
			}
		}
		else if (Config.SERVER_NEWS)
		{
			String serverNews = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "servnews.htm");
			if (serverNews != null)
			{
				sendPacket(new NpcHtmlMessage(1, serverNews));
			}
		}

		if (Config.PETITIONING_ALLOWED)
		{
			PetitionManager.getInstance().checkPetitionMessages(activeChar);
		}

		if (activeChar.isAlikeDead()) // dead or fake dead
		{
			// no broadcast needed since the player will already spawn dead to others
			sendPacket(new Die(activeChar));
		}

		checkDonations(activeChar);

		activeChar.onPlayerEnter();

		if (activeChar.getClan() == null || activeChar.isClanLeader() &&
				ClanRecruitManager.getInstance().getRecruitData(activeChar.getClanId()) != null)
		{
			sendPacket(new ExPledgeWaitingListAlarm());
		}

		sendPacket(new ExAdenaInvenCount(activeChar.getAdena(), activeChar.getInventory().getSize(false)));
		sendPacket(new SkillCoolTime(activeChar));
		sendPacket(new ExVoteSystemInfo(activeChar));
		if (!activeChar.getContactList().getAllContacts().isEmpty())
		{
			sendPacket(new ExShowContactList(activeChar));
		}

		sendPacket(new ExUnreadMailCount(MailManager.getInstance().getUnreadInboxSize(activeChar.getObjectId())));

		for (L2ItemInstance i : activeChar.getInventory().getItems())
		{
			if (i.isTimeLimitedItem())
			{
				i.scheduleLifeTimeTask();
			}
			if (i.isShadowItem() && i.isEquipped())
			{
				i.decreaseMana(false);
			}
		}

		for (L2ItemInstance i : activeChar.getWarehouse().getItems())
		{
			if (i.isTimeLimitedItem())
			{
				i.scheduleLifeTimeTask();
			}
		}

		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED));
		}

		// remove combat flag before teleporting
		if (activeChar.getInventory().getItemByItemId(9819) != null)
		{
			Fort fort = FortManager.getInstance().getFort(activeChar);

			if (fort != null)
			{
				FortSiegeManager.getInstance().dropCombatFlag(activeChar, fort.getFortId());
			}
			else
			{
				int slot = activeChar.getInventory().getSlotFromItem(activeChar.getInventory().getItemByItemId(9819));
				activeChar.getInventory().unEquipItemInBodySlot(slot);
				activeChar.destroyItem("CombatFlag", activeChar.getInventory().getItemByItemId(9819), null, true);
			}
		}

		// Attacker or spectator logging in to a siege zone. Actually should be checked for inside castle only?
		if (!activeChar.isGM()
				// inside siege zone
				&& activeChar.isInsideZone(L2Character.ZONE_SIEGE)
				// but non-participant or attacker
				&& (!activeChar.isInSiege() || activeChar.getSiegeState() < 2))
		{
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
		}

		if (Config.ALLOW_MAIL)
		{
			if (MailManager.getInstance().hasUnreadPost(activeChar))
			{
				sendPacket(ExNoticePostArrived.valueOf(false));
			}
		}

		EventsManager.getInstance().onLogin(activeChar);

		if (Config.WELCOME_MESSAGE_ENABLED)
		{
			activeChar.sendPacket(
					new ExShowScreenMessage(Config.WELCOME_MESSAGE_TEXT, Config.WELCOME_MESSAGE_TIME * 1000));
		}

		int birthday = activeChar.checkBirthDay();
		if (birthday == 0)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_BIRTHDAY_GIFT_HAS_ARRIVED));
			activeChar.sendPacket(new ExBirthdayPopup());
		}
		else if (birthday != -1)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.THERE_ARE_S1_DAYS_UNTIL_YOUR_CHARACTERS_BIRTHDAY);
			sm.addString(Integer.toString(birthday));
			activeChar.sendPacket(sm);
		}

		if (!activeChar.getPremiumItemList().isEmpty())
		{
			activeChar.sendPacket(new ExNotifyPremiumItem());
		}

		if (Config.ENABLE_VITALITY && !activeChar.isGM())
		{
			Calendar monday = Calendar.getInstance();
			monday.setLenient(true);
			monday.set(Calendar.HOUR_OF_DAY, 6);
			monday.set(Calendar.MINUTE, 0);
			monday.set(Calendar.SECOND, 0);
			monday.add(Calendar.DAY_OF_YEAR, -6);
			while (monday.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
			{
				monday.add(Calendar.DAY_OF_YEAR, 1);
			}
			if (monday.getTimeInMillis() > activeChar.getLastAccess())
			{
				activeChar.setVitalityPoints(PcStat.MAX_VITALITY_POINTS, true);
			}
		}

		// Fix for retail bug (you need to target yourself when you login to move)
		activeChar.setTarget(activeChar);
		activeChar.setTarget(null);

		if (activeChar.getFriendList().size() > 0)
		{
			for (int objId : activeChar.getFriendList())
			{
				L2PcInstance friend;
				if (L2World.getInstance().getPlayer(objId) != null)
				{
					friend = L2World.getInstance().getPlayer(objId);
					friend.sendPacket(new FriendPacket(true, activeChar.getObjectId(), friend));
					friend.sendPacket(new FriendList(friend));
				}
				sendPacket(new FriendPacket(true, objId, activeChar));
			}
		}

		if (Config.isServer(Config.TENKAI))
		{
			// Give Magic Gem if not in the inventory
			if (activeChar.getInventory().getItemByItemId(1373) == null)
			{
				activeChar.addItem("Magic Gem", 1373, 1, activeChar, true);
			}

			// Give Swimsuit if not in the inventory
			/*int swimsuit1Id = activeChar.getAppearance().getSex() ? 36345 : 36343;
			int swimsuit2Id = activeChar.getAppearance().getSex() ? 36346 : 36344;
			if (activeChar.getInventory().getItemByItemId(swimsuit1Id) == null)
				activeChar.addItem("Pool Party Event", swimsuit1Id, 1, activeChar, true);
			if (activeChar.getInventory().getItemByItemId(swimsuit2Id) == null)
				activeChar.addItem("Pool Party Event", swimsuit2Id, 1, activeChar, true);*/

			CertificateSkillTable.getInstance().checkPlayer(activeChar);
		}

		if (Config.isServer(Config.TENKAI) || Config.IS_CLASSIC)
		{
			// Give Deck if not in the inventory
			if (activeChar.getInventory().getItemByItemId(938) == null)
			{
				activeChar.addItem("Deck", 938, 1, activeChar, true);
			}
		}

		// Auto Party Search
		//activeChar.showWaitingSubstitute();

		// Vitality information
		//activeChar.sendPacket(new ExVitalityEffectInfo(activeChar.getVitalityPoints(), activeChar.getStat().getVitalityLevel(), activeChar.getVitalityItemsUsed()));

		if (activeChar.getLevel() >= 85 && activeChar.getClassId() < 139)
		{
			PlayerClass cl = PlayerClassTable.getInstance().getClassById(activeChar.getClassId());
			if (cl.getAwakeningClassId() != -1)
			{
				activeChar.sendPacket(new ExCallToChangeClass(cl.getId(), false));
			}
		}

		if (activeChar.isMentor())
		{
			activeChar.giveMentorBuff();
		}
		else if (activeChar.isMentee())
		{
			L2PcInstance mentor = L2World.getInstance().getPlayer(activeChar.getMentorId());
			if (mentor != null && mentor.isOnline())
			{
				mentor.giveMentorBuff();
			}
		}

		// Sending user info again for the clan privileges
		activeChar.broadcastUserInfo();

		// The client passed the loading screen!
		activeChar.hasLoaded();

		switch (activeChar.getClassId())
		{
			case 188: // Eviscerator
			case 189: // Sayha Seer
			{
				int cloakId = activeChar.getClassId() == 188 ? 40200 : 40201;

				if (activeChar.getInventory().getItemByItemId(cloakId) == null)
				{
					activeChar.addItem("EnterWorld", cloakId, 1, null, true);
				}

				break;
			}
		}

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"SELECT timer, author, reason FROM ban_timers " + "WHERE identity = ? OR identity = ?");

			String hardwareId = getClient().getHWId() == null || getClient().getHWId().length() < 20 ? "none" :
					getClient().getHWId();
			statement.setString(1, activeChar.getAccountName());
			statement.setString(2, hardwareId);

			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				int timer = rset.getInt("timer");
				if (timer < 0 || timer > System.currentTimeMillis() / 1000)
				{
					String author = rset.getString("author");
					String reason = rset.getString("reason");
					String expiration = "This ban ";
					int remaining = timer - (int) (System.currentTimeMillis() / 1000);
					if (timer > -1)
					{
						expiration +=
								"expires in " + remaining / 3600 + " hours and " + remaining % 3600 / 60 + " minutes.";
					}
					else
					{
						expiration += "is permanent.";
					}

					if (Config.isServer(Config.TENKAI))
					{
						getClient().sendPacket(new ExShowScreenMessage("OH NO! YOU ARE BANNED!!", 1000000));
						getClient().sendPacket(new CreatureSay(0, Say2.CRITICAL_ANNOUNCE, "",
								"You have been banned by " + author + "."));
						getClient()
								.sendPacket(new CreatureSay(0, Say2.CRITICAL_ANNOUNCE, "", "Reason: " + reason + "."));
						getClient().sendPacket(new CreatureSay(0, Say2.CRITICAL_ANNOUNCE, "", expiration));
					}

					getClient().setDetached(true);
					/*Log.info("Player " + activeChar.getName() + " tried to log in but he/she was banned.");
					Log.info("Account: " + activeChar.getAccountName());
					Log.info("HWID: " + activeChar.getHWID());
					Log.info("Ban author: " + author);
					Log.info("Ban reason: " + reason);
					Log.info(expiration);*/
				}
				else
				{
					PreparedStatement statement2 =
							con.prepareStatement("DELETE FROM ban_timers WHERE timer < ? AND timer > 0");
					statement2.setInt(1, (int) (System.currentTimeMillis() / 1000));
					statement2.execute();
					statement2.close();
				}
			}

			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Could not check bans: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		if (activeChar.getInventory().getItemByItemId(1373) == null)
		{
			activeChar.addItem("EnterWorld", 1373, 1, activeChar, true);
		}
		
		/*
		SystemMessage s = SystemMessage.getSystemMessage(SystemMessageId.LIGHT_BLUE_CHATBOX_S1);
		if (activeChar.getServerInstanceId() == 0)
		{
			s.addString("You are playing the main dimension of Phoenix.");
			activeChar.sendPacket(s);
		}*/
	}

	/**
	 * @param cha
	 */
	private void engage(L2PcInstance cha)
	{
		int chaid = cha.getObjectId();

		for (Couple cl : CoupleManager.getInstance().getCouples())
		{
			if (cl.getPlayer1Id() == chaid || cl.getPlayer2Id() == chaid)
			{
				if (cl.getMaried())
				{
					cha.setMarried(true);
				}

				cha.setCoupleId(cl.getId());

				if (cl.getPlayer1Id() == chaid)
				{
					cha.setPartnerId(cl.getPlayer2Id());
				}

				else
				{
					cha.setPartnerId(cl.getPlayer1Id());
				}
			}
		}
	}

	/**
	 * @param cha
	 * @param partnerId
	 */
	private void notifyPartner(L2PcInstance cha, int partnerId)
	{
		if (cha.getPartnerId() != 0)
		{
			int objId = cha.getPartnerId();

			try
			{
				L2PcInstance partner = L2World.getInstance().getPlayer(objId);

				if (partner != null)
				{
					partner.sendMessage("Your Partner has logged in.");
				}

				partner = null;
			}
			catch (ClassCastException cce)
			{
				Log.warning("Wedding Error: ID " + objId + " is now owned by a(n) " +
						L2World.getInstance().findObject(objId).getClass().getSimpleName());
			}
		}
	}

	/**
	 * @param activeChar
	 */
	private void notifyClanMembers(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return;
		}

		L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}

		clan.getClanMember(activeChar.getObjectId()).setPlayerInstance(activeChar);
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN);
		msg.addString(activeChar.getName());
		clan.broadcastToOtherOnlineMembers(msg, activeChar);
		msg = null;
		clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(activeChar), activeChar);
		clan.broadcastToOnlineMembers(new ExPledgeCount(clan.getOnlineMembersCount()));
	}

	/**
	 * @param activeChar
	 */
	private void notifySponsorOrApprentice(L2PcInstance activeChar)
	{
		if (activeChar.getSponsor() != 0)
		{
			L2PcInstance sponsor = L2World.getInstance().getPlayer(activeChar.getSponsor());

			if (sponsor != null)
			{
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				sponsor.sendPacket(msg);
			}
		}
		else if (activeChar.getApprentice() != 0)
		{
			L2PcInstance apprentice = L2World.getInstance().getPlayer(activeChar.getApprentice());

			if (apprentice != null)
			{
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOUR_SPONSOR_C1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				apprentice.sendPacket(msg);
			}
		}
	}

	private void loadTutorial(L2PcInstance player)
	{
		QuestState qs = player.getQuestState("Q255_Tutorial");

		if (qs != null)
		{
			qs.getQuest().notifyEvent("UC", null, player);
		}
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}

	private void checkDonations(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("SELECT * FROM items_to_add WHERE online = 1 OR user_name LIKE ?");
			statement.setString(1, player.getName());
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				player = L2World.getInstance().getPlayer(rset.getString("user_name"));
				if (player == null)
				{
					continue;
				}
				int itemId = rset.getInt("item_id");
				int count = rset.getInt("count");

				//Be sure these items are allowed...
				if (itemId != 4037 && itemId != 4356) //Coin of Luck, Gold Einhasad
				{
					String itemName = String.valueOf(itemId);
					L2Item temp = ItemTable.getInstance().getTemplate(itemId);
					if (temp != null)
					{
						itemName = temp.getName();
					}
					Util.logToFile(
							player.getName() + " has tried to get " + itemName + "(" + count + ") which is illegal!",
							"Donations", true);
					continue;
				}

				PreparedStatement statement2 =
						con.prepareStatement("DELETE FROM items_to_add WHERE user_name LIKE ? AND item_id = ?");
				statement2.setString(1, player.getName());
				statement2.setInt(2, itemId);
				statement2.execute();
				statement2.close();

				PcInventory inv = player.getInventory();
				inv.addItem("item to add", itemId, count, player, player);

				SystemMessage systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				systemMessage.addItemName(itemId);
				systemMessage.addNumber(count);
				player.sendPacket(systemMessage);

				if (itemId == 4037 || itemId == 4356)
				{
					CreatureSay cs1 = new CreatureSay(0, Say2.TELL, "Administration",
							"We have just given you the coins corresponding with your last donation.");
					player.sendPacket(cs1);
					cs1 = new CreatureSay(0, Say2.TELL, "Administration",
							"Thank you very much for this contribution, we hope you to continue enjoying our server.");
					player.sendPacket(cs1);
				}

				Util.logToFile(player.getName() + " received " + count + " " +
						ItemTable.getInstance().getTemplate(itemId).getName(), "Donations", true);
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			Log.warning("Exception checking for items to add: " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}
