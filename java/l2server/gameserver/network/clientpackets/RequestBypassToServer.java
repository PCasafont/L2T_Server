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
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.communitybbs.CommunityBoard;
import l2server.gameserver.datatables.AdminCommandAccessRights;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.events.HiddenChests;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.handler.AdminCommandHandler;
import l2server.gameserver.handler.BypassHandler;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.instancemanager.SurveyManager;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2MerchantSummonInstance;
import l2server.gameserver.model.actor.instance.L2MobSummonInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.olympiad.HeroesManager;
import l2server.gameserver.model.olympiad.OlympiadGameManager;
import l2server.gameserver.model.olympiad.OlympiadGameTask;
import l2server.gameserver.model.olympiad.OlympiadManager;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.util.GMAudit;
import l2server.log.Log;

import java.util.StringTokenizer;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.12.4.5 $ $Date: 2005/04/11 10:06:11 $
 */
public final class RequestBypassToServer extends L2GameClientPacket
{

	// S
	private String _command;

	@Override
	protected void readImpl()
	{
		_command = readS();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		if (!getClient().getFloodProtectors().getServerBypass().tryPerformAction(_command))
		{
			return;
		}

		if (_command.isEmpty())
		{
			Log.info(activeChar.getName() + " send empty requestbypass");
			activeChar.logout();
			return;
		}

		try
		{
			if (_command.startsWith("admin_")) //&& activeChar.getAccessLevel() >= Config.GM_ACCESSLEVEL)
			{
				String command = _command.split(" ")[0];

				IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(command);

				if (ach == null)
				{
					if (activeChar.isGM())
					{
						activeChar.sendMessage("The command " + command.substring(6) + " does not exist!");
					}

					Log.warning("No handler registered for admin command '" + command + "'");
					return;
				}

				if (!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel()))
				{
					activeChar.sendMessage("You don't have the access rights to use this command!");
					Log.warning("Character " + activeChar.getName() + " tried to use admin command " + command +
							", without proper access level!");
					return;
				}

				if (AdminCommandAccessRights.getInstance().requireConfirm(command))
				{
					activeChar.setAdminConfirmCmd(_command);
					ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1);
					dlg.addString("Are you sure you want execute command " + _command.substring(6) + " ?");
					activeChar.sendPacket(dlg);
				}
				else
				{
					if (Config.GMAUDIT)
					{
						GMAudit.auditGMAction(activeChar.getName(), _command,
								activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target");
					}

					ach.useAdminCommand(_command, activeChar);
				}
			}
			else if (_command.equals("come_here") && activeChar.isGM())
			{
				comeHere(activeChar);
			}
			else if (_command.startsWith("npc_"))
			{
				if (!activeChar.validateBypass(_command))
				{
					return;
				}

				int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
				{
					id = _command.substring(4, endOfId);
				}
				else
				{
					id = _command.substring(4);
				}
				try
				{
					L2Object object = L2World.getInstance().findObject(Integer.parseInt(id));

					if (object instanceof L2Npc && endOfId > 0 && activeChar
							.isInsideRadius(object, ((L2Npc) object).getTemplate().InteractionDistance, false, false))
					{
						((L2Npc) object).onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					}

					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				}
				catch (NumberFormatException ignored)
				{
				}
			}
			else if (_command.startsWith("summon_"))
			{
				if (!activeChar.validateBypass(_command))
				{
					return;
				}

				int endOfId = _command.indexOf('_', 8);
				String id;
				if (endOfId > 0)
				{
					id = _command.substring(7, endOfId);
				}
				else
				{
					id = _command.substring(7);
				}
				try
				{
					L2Object object = L2World.getInstance().findObject(Integer.parseInt(id));

					if (object instanceof L2MerchantSummonInstance && endOfId > 0 &&
							activeChar.isInsideRadius(object, L2Npc.DEFAULT_INTERACTION_DISTANCE, false, false))
					{
						((L2MerchantSummonInstance) object)
								.onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					}

					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				}
				catch (NumberFormatException ignored)
				{
				}
			}
			// Navigate through Manor windows
			else if (_command.startsWith("manor_menu_select"))
			{
				final IBypassHandler manor = BypassHandler.getInstance().getBypassHandler("manor_menu_select");
				if (manor != null)
				{
					manor.useBypass(_command, activeChar, null);
				}
			}
			else if (_command.startsWith("_bbs"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.startsWith("_mail"))
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CB_OFFLINE));
			}
			else if (_command.startsWith("_friend"))
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CB_OFFLINE));
			}
			else if (_command.startsWith("Quest "))
			{
				if (!activeChar.validateBypass(_command))
				{
					return;
				}

				L2PcInstance player = getClient().getActiveChar();
				if (player == null)
				{
					return;
				}

				String p = _command.substring(6).trim();
				int idx = p.indexOf(' ');
				if (idx < 0)
				{
					player.processQuestEvent(p, "");
				}
				else
				{
					player.processQuestEvent(p.substring(0, idx), p.substring(idx).trim());
				}
			}
			else if (_command.startsWith("_match"))
			{
				L2PcInstance player = getClient().getActiveChar();
				if (player == null)
				{
					return;
				}

				String params = _command.substring(_command.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = HeroesManager.getInstance().getHeroByClass(heroclass);
				if (heroid > 0)
				{
					HeroesManager.getInstance().showHeroFights(player, heroclass, heroid, heropage);
				}
			}
			else if (_command.startsWith("_diary"))
			{
				L2PcInstance player = getClient().getActiveChar();
				if (player == null)
				{
					return;
				}

				String params = _command.substring(_command.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = HeroesManager.getInstance().getHeroByClass(heroclass);
				if (heroid > 0)
				{
					HeroesManager.getInstance().showHeroDiary(player, heroclass, heroid, heropage);
				}
			}
			else if (_command.startsWith("MobSummon"))
			{
				for (L2Summon summon : activeChar.getSummons())
				{
					if (!(summon instanceof L2MobSummonInstance))
					{
						continue;
					}

					L2MobSummonInstance mobSummon = (L2MobSummonInstance) summon;
					mobSummon.onBypass(activeChar, _command.substring(10));
				}
			}
			else if (_command.startsWith("InstancedEvent"))
			{
				EventsManager.getInstance().handleBypass(activeChar, _command);
			}
			else if (_command.startsWith("WatchDrops"))
			{
				IBypassHandler handler = BypassHandler.getInstance().getBypassHandler(_command);
				if (handler != null)
				{
					handler.useBypass(_command, activeChar, null);
				}
			}
			else if (_command.startsWith("Captcha"))
			{
				String text = "";
				if (_command.length() > 8)
				{
					text = _command.substring(8);
				}
				if (text.equalsIgnoreCase(activeChar.getCaptcha()))
				{
					activeChar.setCaptcha(null);
					activeChar.sendPacket(new CreatureSay(0, Say2.TELL, "Captcha", "Ok"));
					activeChar.sendPacket(new NpcHtmlMessage(0, "<html><body></body><html>"));
				}
				else
				{
					if (activeChar.getBotLevel() % 5 < 4)
					{
						activeChar.increaseBotLevel();
						activeChar.captcha("");
						activeChar.sendPacket(
								new CreatureSay(0, Say2.TELL, "Captcha", "You have typed it wrong. Try again.."));
					}
					else
					{
						activeChar.logout(false);
					}
				}
			}
			else if (_command.startsWith("NickName"))
			{
				String text = "";
				if (_command.length() > 9)
				{
					text = _command.substring(9);
				}
				if (CharNameTable.getInstance().doesCharNameExist(text))
				{
					activeChar.sendPacket(new NpcHtmlMessage(0,
							"<html><body><center>" + "This name already exists!<br>" + "Choose another one:<br>" +
									"<edit var=text width=130 height=11 length=26><br>" +
									"<button value=\"Done\" action=\"bypass NickName $text\" back=\"l2ui_ct1.button_df\" width=65 height=20 fore=\"l2ui_ct1.button_df\">" +
									"</center></body></html>"));
				}
				else if (!CharacterCreate.isValidName(text))
				{
					activeChar.sendPacket(new NpcHtmlMessage(0,
							"<html><body><center>" + "Invalid name!<br>" + "Choose another one:<br>" +
									"<edit var=text width=130 height=11 length=26><br>" +
									"<button value=\"Done\" action=\"bypass NickName $text\" back=\"l2ui_ct1.button_df\" width=65 height=20 fore=\"l2ui_ct1.button_df\">" +
									"</center></body></html>"));
				}
				else
				{
					activeChar.setName(text);
					activeChar.store();
					CharNameTable.getInstance().addName(activeChar);
					activeChar.sendMessage("Name changed!");
					activeChar.broadcastUserInfo();
					activeChar.setMovieId(0);
					activeChar.sendPacket(new NpcHtmlMessage(0, "<html><body></body></html>"));
					activeChar.startCaptchaTask();
				}
			}
			else if (_command.startsWith("Survey"))
			{
				if (_command.length() > 6)
				{
					if (_command.equals("SurveyInfo"))
					{
						String html = "<html><title>Survey System</title><body>" +
								SurveyManager.getInstance().getDescription() +
								"<br><button value=\"Back\" action=\"bypass -h Survey\" width=60 height=20 fore=\"L2UI_ct1.button_df\"><br>" +
								"</body></html>";
						activeChar.sendPacket(new NpcHtmlMessage(0, html));
					}
					else if (_command.startsWith("SurveyAnswer"))
					{
						String message = "Thank you!";
						if (!SurveyManager.getInstance()
								.storeAnswer(activeChar.getObjectId(), Integer.valueOf(_command.substring(13))))
						{
							message = "You already answered!";
						}
						String html = "<html><title>Survey System</title><body>" + message + "</body></html>";
						activeChar.sendPacket(new NpcHtmlMessage(0, html));
					}
				}
				else
				{
					String html =
							"<html><title>Survey System</title><body>" + SurveyManager.getInstance().getQuestion() +
									" <button value=\"More Info\" action=\"bypass -h SurveyInfo\" width=70 height=20 fore=\"L2UI_ct1.button_df\"><br>" +
									"<table width=260>";
					for (int answerId : SurveyManager.getInstance().getPossibleAnswerIds())
					{
						html += "<tr><td><button value=\"" + SurveyManager.getInstance().getPossibleAnswer(answerId) +
								"\" action=\"bypass -h SurveyAnswer " + answerId +
								"\" width=250 height=25 fore=\"L2UI_ct1.button_df\"></td></tr>";
					}
					html += "</table></body></html>";
					activeChar.sendPacket(new NpcHtmlMessage(0, html));
				}
			}
			else if (_command.startsWith("_olympiad"))
			{
				if (_command.substring(18).startsWith("move_op_field"))
				{
					final int arenaId = Integer.parseInt(_command.substring(38));
					final OlympiadGameTask nextArena = OlympiadGameManager.getInstance().getOlympiadTask(arenaId - 1);
					if (nextArena != null && nextArena.getGame() != null && !activeChar.isInOlympiadMode() &&
							!OlympiadManager.getInstance().isRegistered(activeChar) &&
							(activeChar.inObserverMode() || activeChar.getOlympiadGameId() == -1))
					{
						activeChar.enterOlympiadObserverMode(nextArena.getZone().getSpawns().get(8),
								nextArena.getGame().getGameId());
					}
				}
			}
			else if (_command.equals("treasure"))
			{
				HiddenChests.getInstance().showInfo(activeChar);
			}
			else
			{
				final IBypassHandler handler = BypassHandler.getInstance().getBypassHandler(_command);
				if (handler != null)
				{
					handler.useBypass(_command, activeChar, null);
				}
				else
				{
					Log.log(Level.WARNING, getClient() + " sent not handled RequestBypassToServer: [" + _command + "]");
				}
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, getClient() + " sent bad RequestBypassToServer: \"" + _command + "\"", e);
			if (activeChar.isGM())
			{
				StringBuilder sb = new StringBuilder(200);
				sb.append("<html><body>");
				sb.append("Bypass error: " + e + "<br1>");
				sb.append("Bypass command: " + _command + "<br1>");
				sb.append("StackTrace:<br1>");
				for (StackTraceElement ste : e.getStackTrace())
				{
					sb.append(ste.toString() + "<br1>");
				}
				sb.append("</body></html>");
				// item html
				NpcHtmlMessage msg = new NpcHtmlMessage(0, 12807);
				msg.setHtml(sb.toString());
				msg.disableValidation();
				activeChar.sendPacket(msg);
			}
		}
	}

	/**
	 */
	private static void comeHere(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if (obj == null)
		{
			return;
		}
		if (obj instanceof L2Npc)
		{
			L2Npc temp = (L2Npc) obj;
			temp.setTarget(activeChar);
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
					new L2CharPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 0));
		}
	}
}
