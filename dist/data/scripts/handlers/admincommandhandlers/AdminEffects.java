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

package handlers.admincommandhandlers;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.ChestInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.stats.VisualEffect;
import l2server.gameserver.util.Broadcast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Level;

/**
 * This class handles following admin commands:
 * <li> invis/invisible/vis/visible = makes yourself invisible or visible
 * <li> earthquake = causes an earthquake of a given intensity and duration around you
 * <li> bighead/shrinkhead = changes head size
 * <li> gmspeed = temporary Super Haste effect.
 * <li> para/unpara = paralyze/remove paralysis from target
 * <li> para_all/unpara_all = same as para/unpara, affects the whole world.
 * <li> polyself/unpolyself = makes you look as a specified mob.
 * <li> changename = temporary change name
 * <li> clearteams/setteam_close/setteam = team related commands
 * <li> social = forces an Creature instance to broadcast social action packets.
 * <li> effect = forces an Creature instance to broadcast MSU packets.
 * <li> abnormal = force changes over an Creature instance's abnormal state.
 * <li> setPoints = change ranked points of the Player.
 * <li> play_sound/play_sounds = Music broadcasting related commands
 * <li> atmosphere = sky change related commands.
 */
public class AdminEffects implements IAdminCommandHandler {
	private static final String[] ADMIN_COMMANDS =
			{"admin_invis", "admin_invisible", "admin_vis", "admin_visible", "admin_invis_menu", "admin_earthquake", "admin_earthquake_menu",
					"admin_bighead", "admin_shrinkhead", "admin_gmspeed", "admin_gmspeed_menu", "admin_unpara_all", "admin_para_all", "admin_unpara",
					"admin_para", "admin_unpara_all_menu", "admin_para_all_menu", "admin_unpara_menu", "admin_para_menu", "admin_polyself",
					"admin_unpolyself", "admin_polyself_menu", "admin_unpolyself_menu", "admin_clearteams", "admin_setteam_close", "admin_setteam",
					"admin_social", "admin_effect", "admin_social_menu", "admin_effect_menu", "admin_abnormal", "admin_abnormal_menu",
					"admin_play_sounds", "admin_play_sound", "admin_setPoints", "admin_atmosphere", "admin_atmosphere_menu",
					"admin_set_displayeffect", "admin_set_displayeffect_menu"};

	@Override
	public boolean useAdminCommand(String command, Player activeChar) {
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();

		if (command.equals("admin_invis_menu")) {
			if (!activeChar.getAppearance().getInvisible()) {
				activeChar.getAppearance().setInvisible();
				activeChar.broadcastUserInfo();
				activeChar.decayMe();
				activeChar.spawnMe();
			} else {
				activeChar.getAppearance().setVisible();
				activeChar.broadcastUserInfo();
			}
			command = "";
			AdminHelpPage.showHelpPage(activeChar, "gm_menu.htm");
		} else if (command.startsWith("admin_invis")) {
			activeChar.getAppearance().setInvisible();
			activeChar.broadcastUserInfo();
			activeChar.decayMe();
			activeChar.spawnMe();
		} else if (command.startsWith("admin_vis")) {
			activeChar.getAppearance().setVisible();
			activeChar.broadcastUserInfo();
		} else if (command.startsWith("admin_earthquake")) {
			try {
				String val1 = st.nextToken();
				int intensity = Integer.parseInt(val1);
				String val2 = st.nextToken();
				int duration = Integer.parseInt(val2);
				Earthquake eq = new Earthquake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), intensity, duration);
				//activeChar.broadcastPacket(eq);
				for (Player player : World.getInstance().getAllPlayers().values()) {
					player.sendPacket(eq);
				}
			} catch (Exception e) {
				activeChar.sendMessage("Usage: //earthquake <intensity> <duration>");
			}
		} else if (command.startsWith("admin_atmosphere")) {
			try {
				String type = st.nextToken();
				String state = st.nextToken();
				int duration = Integer.parseInt(st.nextToken());
				adminAtmosphere(type, state, duration, activeChar);
			} catch (Exception ex) {
				activeChar.sendMessage("Usage: //atmosphere <signsky dawn|dusk>|<sky day|night|red>");
			}
		} else if (command.equals("admin_play_sounds")) {
			AdminHelpPage.showHelpPage(activeChar, "songs/songs.htm");
		} else if (command.startsWith("admin_play_sounds")) {
			try {
				AdminHelpPage.showHelpPage(activeChar, "songs/songs" + command.substring(18) + ".htm");
			} catch (StringIndexOutOfBoundsException e) {
				activeChar.sendMessage("Usage: //play_sounds <pagenumber>");
			}
		} else if (command.startsWith("admin_play_sound")) {
			try {
				playAdminSound(activeChar, command.substring(17));
			} catch (StringIndexOutOfBoundsException e) {
				activeChar.sendMessage("Usage: //play_sound <soundname>");
			}
		} else if (command.startsWith("admin_setPoints")) {
			String name = st.nextToken();
			int Points = Integer.parseInt(st.nextToken());

			Connection coni = null;
			try {
				coni = L2DatabaseFactory.getInstance().getConnection();

				PreparedStatement statement = coni.prepareStatement("UPDATE characters SET rankedPoints=? WHERE char_name=?");
				statement.setInt(1, Points);
				statement.setString(2, name);

				statement.execute();
				statement.close();
				activeChar.sendMessage("You changed " + name + "'s points to " + Points);
			} catch (Exception e) {
				log.error("Failed updating Ranked Points", e);
			} finally {
				L2DatabaseFactory.close(coni);
			}
		} else if (command.equals("admin_para_all")) {
			try {
				Collection<Player> plrs = activeChar.getKnownList().getKnownPlayers().values();
				//synchronized (activeChar.getKnownList().getKnownPlayers())
				{
					for (Player player : plrs) {
						if (!player.isGM()) {
							player.startVisualEffect(VisualEffect.HOLD_1);
							player.setIsParalyzed(true);
							player.startParalyze();
						}
					}
				}
			} catch (Exception e) {
			}
		} else if (command.equals("admin_unpara_all")) {
			try {
				Collection<Player> plrs = activeChar.getKnownList().getKnownPlayers().values();
				//synchronized (activeChar.getKnownList().getKnownPlayers())
				{
					for (Player player : plrs) {
						player.stopVisualEffect(VisualEffect.HOLD_1);
						player.setIsParalyzed(false);
						player.stopParalyze(false);
					}
				}
			} catch (Exception e) {
			}
		} else if (command.startsWith("admin_para")) // || command.startsWith("admin_para_menu"))
		{
			if (st.hasMoreTokens()) {
				try {
					int radius = Integer.parseInt(st.nextToken());
					for (Player player : activeChar.getKnownList().getKnownPlayersInRadius(radius)) {
						player.startVisualEffect(VisualEffect.HOLD_1);
						player.setIsParalyzed(true);
						StopMove sm = new StopMove(player);
						player.sendPacket(sm);
						player.broadcastPacket(sm);
					}

					activeChar.sendMessage("Paralized all the players within a " + radius + " unit radius.");
					return true;
				} catch (NumberFormatException e) {
					activeChar.sendMessage("Invalid radius.");
					return false;
				}
			} else {
				WorldObject target = activeChar.getTarget();
				Creature player = null;
				if (target instanceof Creature) {
					player = (Creature) target;
					player.startVisualEffect(VisualEffect.HOLD_1);
					player.setIsParalyzed(true);
					player.startParalyze();
				}
			}
		} else if (command.startsWith("admin_unpara")) // || command.startsWith("admin_unpara_menu"))
		{
			if (st.hasMoreTokens()) {
				try {
					int radius = Integer.parseInt(st.nextToken());
					for (Player player : activeChar.getKnownList().getKnownPlayersInRadius(radius)) {
						player.stopVisualEffect(VisualEffect.HOLD_1);
						player.setIsParalyzed(false);
					}

					activeChar.sendMessage("Unparalized all the players within a " + radius + " unit radius.");
					return true;
				} catch (NumberFormatException e) {
					activeChar.sendMessage("Invalid radius.");
					return false;
				}
			} else {
				WorldObject target = activeChar.getTarget();
				Creature player = null;
				if (target instanceof Creature) {
					player = (Creature) target;
					player.stopVisualEffect(VisualEffect.HOLD_1);
					player.setIsParalyzed(false);
					player.stopParalyze(false);
				}
			}
		} else if (command.startsWith("admin_bighead")) {
			try {
				WorldObject target = activeChar.getTarget();
				Creature player = null;
				if (target instanceof Creature) {
					player = (Creature) target;
					player.startVisualEffect(VisualEffect.BIG_HEAD);
				}
			} catch (Exception e) {
			}
		} else if (command.startsWith("admin_shrinkhead")) {
			try {
				WorldObject target = activeChar.getTarget();
				Creature player = null;
				if (target instanceof Creature) {
					player = (Creature) target;
					player.stopVisualEffect(VisualEffect.BIG_HEAD);
				}
			} catch (Exception e) {
			}
		} else if (command.startsWith("admin_gmspeed")) {
			try {
				int val = Integer.parseInt(st.nextToken());
				boolean sendMessage = activeChar.getFirstEffect(7029) != null;
				activeChar.stopSkillEffects(7029);
				if (val == 0 && sendMessage) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED).addSkillName(7029));
				} else if (val >= 1 && val <= 10) {
					Skill gmSpeedSkill = SkillTable.getInstance().getInfo(7029, val);
					activeChar.doSimultaneousCast(gmSpeedSkill);
				}
			} catch (Exception e) {
				activeChar.sendMessage("Usage: //gmspeed <value> (0=off...4=max)");
			}
			if (command.contains("_menu")) {
				command = "";
				AdminHelpPage.showHelpPage(activeChar, "gm_menu.htm");
			}
		} else if (command.startsWith("admin_polyself")) {
			try {
				String id = st.nextToken();
				activeChar.getPoly().setPolyInfo("npc", id);
				activeChar.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false);
				CharInfo info1 = new CharInfo(activeChar);
				activeChar.broadcastPacket(info1);
				UserInfo info2 = new UserInfo(activeChar);
				activeChar.sendPacket(info2);
			} catch (Exception e) {
				activeChar.sendMessage("Usage: //polyself <npcId>");
			}
		} else if (command.startsWith("admin_unpolyself")) {
			activeChar.getPoly().setPolyInfo(null, "1");
			activeChar.decayMe();
			activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			CharInfo info1 = new CharInfo(activeChar);
			activeChar.broadcastPacket(info1);
			UserInfo info2 = new UserInfo(activeChar);
			activeChar.sendPacket(info2);
		} else if (command.equals("admin_clearteams")) {
			try {
				Collection<Player> plrs = activeChar.getKnownList().getKnownPlayers().values();
				//synchronized (activeChar.getKnownList().getKnownPlayers())
				{
					for (Player player : plrs) {
						player.setTeam(0);
						player.broadcastUserInfo();
					}
				}
			} catch (Exception e) {
			}
		} else if (command.startsWith("admin_setteam_close")) {
			try {
				String val = st.nextToken();
				int teamVal = Integer.parseInt(val);
				Collection<Player> plrs = activeChar.getKnownList().getKnownPlayers().values();
				//synchronized (activeChar.getKnownList().getKnownPlayers())
				{
					for (Player player : plrs) {
						if (activeChar.isInsideRadius(player, 400, false, true)) {
							player.setTeam(teamVal);
							if (teamVal != 0) {
								player.sendMessage("You have joined team " + teamVal);
							}
							player.broadcastUserInfo();
						}
					}
				}
			} catch (Exception e) {
				activeChar.sendMessage("Usage: //setteam_close <teamId>");
			}
		} else if (command.startsWith("admin_setteam")) {
			try {
				String val = st.nextToken();
				int teamVal = Integer.parseInt(val);
				WorldObject target = activeChar.getTarget();
				Player player = null;
				if (target instanceof Player) {
					player = (Player) target;
				} else {
					return false;
				}
				player.setTeam(teamVal);
				if (teamVal != 0) {
					player.sendMessage("You have joined team " + teamVal);
				}
				player.broadcastUserInfo();
			} catch (Exception e) {
				activeChar.sendMessage("Usage: //setteam <teamId>");
			}
		} else if (command.startsWith("admin_social")) {
			try {
				String target = null;
				WorldObject obj = activeChar.getTarget();
				if (st.countTokens() == 2) {
					int social = Integer.parseInt(st.nextToken());
					target = st.nextToken();
					if (target != null) {
						Player player = World.getInstance().getPlayer(target);
						if (player != null) {
							if (performSocial(social, player, activeChar)) {
								activeChar.sendMessage(player.getName() + " was affected by your request.");
							}
						} else {
							try {
								int radius = Integer.parseInt(target);
								Collection<WorldObject> objs = activeChar.getKnownList().getKnownObjects().values();
								//synchronized (activeChar.getKnownList().getKnownObjects())
								{
									for (WorldObject object : objs) {
										if (activeChar.isInsideRadius(object, radius, false, false)) {
											performSocial(social, object, activeChar);
										}
									}
								}
								activeChar.sendMessage(radius + " units radius affected by your request.");
							} catch (NumberFormatException nbe) {
								activeChar.sendMessage("Incorrect parameter");
							}
						}
					}
				} else if (st.countTokens() == 1) {
					int social = Integer.parseInt(st.nextToken());
					if (obj == null) {
						obj = activeChar;
					}

					if (performSocial(social, obj, activeChar)) {
						activeChar.sendMessage(obj.getName() + " was affected by your request.");
					} else {
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOTHING_HAPPENED));
					}
				} else if (!command.contains("menu")) {
					activeChar.sendMessage("Usage: //social <social_id> [player_name|radius]");
				}
			} catch (Exception e) {
				if (Config.DEBUG) {
					e.printStackTrace();
				}
			}
		} else if (command.startsWith("admin_abnormal")) {
			try {
				String target = null;
				WorldObject obj = activeChar.getTarget();
				if (st.countTokens() > 0) {
					String parm = st.nextToken();
					int abnormal = Integer.parseInt(parm);
					target = obj.getName();
					if (st.hasMoreTokens()) {
						target = st.nextToken();
					}
					if (target != null) {
						Player player = World.getInstance().getPlayer(target);
						if (player != null) {
							if (performAbnormal(abnormal, player)) {
								activeChar.sendMessage(player.getName() + "'s abnormal status was affected by your request.");
							} else {
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOTHING_HAPPENED));
							}
						} else {
							try {
								int radius = Integer.parseInt(target);
								Collection<WorldObject> objs = activeChar.getKnownList().getKnownObjects().values();

								for (WorldObject object : objs) {
									if (activeChar.isInsideRadius(object, radius, false, false)) {
										performAbnormal(abnormal, object);
									}
								}
								activeChar.sendMessage(radius + " units radius affected by your request.");
							} catch (NumberFormatException nbe) {
								activeChar.sendMessage("Usage: //abnormal <hex_abnormal_mask> [player|radius]");
							}
						}
					}
				} else if (st.countTokens() == 2) {
					int abnormal = Integer.parseInt(st.nextToken());
					if (obj == null) {
						obj = activeChar;
					}

					if (performAbnormal(abnormal, obj)) {
						activeChar.sendMessage(obj.getName() + "'s abnormal status was affected by your request.");
					} else {
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOTHING_HAPPENED));
					}
				} else if (!command.contains("menu")) {
					activeChar.sendMessage("Usage: //abnormal <abnormal_mask> [player_name|radius]");
				}
			} catch (Exception e) {
				if (Config.DEBUG) {
					e.printStackTrace();
				}
			}
		} else if (command.startsWith("admin_effect")) {
			try {
				WorldObject obj = activeChar.getTarget();
				int level = 1, hittime = 1;
				int skill = Integer.parseInt(st.nextToken());
				if (st.hasMoreTokens()) {
					level = Integer.parseInt(st.nextToken());
				}
				if (st.hasMoreTokens()) {
					hittime = Integer.parseInt(st.nextToken());
				}
				if (obj == null) {
					obj = activeChar;
				}
				if (!(obj instanceof Creature)) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
				} else {
					Creature target = (Creature) obj;
					target.broadcastPacket(new MagicSkillUse(target, activeChar, skill, level, hittime, 0, 0));
					activeChar.sendMessage(obj.getName() + " performs MSU " + skill + "/" + level + " by your request.");
				}
			} catch (Exception e) {
				activeChar.sendMessage("Usage: //effect skill [level | level hittime]");
			}
		} else if (command.startsWith("admin_set_displayeffect")) {
			WorldObject target = activeChar.getTarget();
			if (!(target instanceof Npc)) {
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			Npc npc = (Npc) target;
			try {
				String type = st.nextToken();
				int diplayeffect = Integer.parseInt(type);
				npc.setDisplayEffect(diplayeffect);
			} catch (Exception e) {
				activeChar.sendMessage("Usage: //set_displayeffect <id>");
			}
		}
		if (command.contains("menu")) {
			showMainPage(activeChar, command);
		}
		return true;
	}

	/**
	 * @param action bitmask that should be applied over target's abnormal
	 * @param target
	 * @return <i>true</i> if target's abnormal state was affected , <i>false</i> otherwise.
	 */
	private boolean performAbnormal(int action, WorldObject target) {
		if (target instanceof Creature) {
			Creature character = (Creature) target;
			if (character.getAbnormalEffect().contains(action)) {
				character.stopVisualEffect(action);
			} else {
				character.startVisualEffect(action);
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean performSocial(int action, WorldObject target, Player activeChar) {
		try {
			if (target instanceof Creature) {
				if (target instanceof ChestInstance) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOTHING_HAPPENED));
					return false;
				}
				if (target instanceof Npc && (action < 1 || action > 10)) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOTHING_HAPPENED));
					return false;
				}
				if (target instanceof Player && (action < 2 || action > 34 || action == 19) && action != SocialAction.LEVEL_UP) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOTHING_HAPPENED));
					return false;
				}
				Creature character = (Creature) target;
				character.broadcastPacket(new SocialAction(character.getObjectId(), action));
			} else {
				return false;
			}
		} catch (Exception e) {
		}
		return true;
	}

	/**
	 * @param type     - atmosphere type (signssky,sky)
	 * @param state    - atmosphere state(night,day)
	 * @param duration
	 */
	private void adminAtmosphere(String type, String state, int duration, Player activeChar) {
		L2GameServerPacket packet = null;

		if (type.equals("signsky")) {
			if (state.equals("dawn")) {
				packet = new SSQInfo(2);
			} else if (state.equals("dusk")) {
				packet = new SSQInfo(1);
			}
		} else if (type.equals("sky")) {
			if (state.equals("night")) {
				packet = new SunSet();
			} else if (state.equals("day")) {
				packet = new SunRise();
			} else if (state.equals("red")) {
				if (duration != 0) {
					packet = new ExRedSky(duration);
				} else {
					packet = new ExRedSky(10);
				}
			}
		} else {
			activeChar.sendMessage("Usage: //atmosphere <signsky dawn|dusk>|<sky day|night|red>");
		}
		if (packet != null) {
			Broadcast.toAllOnlinePlayers(packet);
		}
	}

	private void playAdminSound(Player activeChar, String sound) {
		PlaySound snd = new PlaySound(1, sound, 0, 0, 0, 0, 0);
		activeChar.sendPacket(snd);
		activeChar.broadcastPacket(snd);
		activeChar.sendMessage("Playing " + sound + ".");
	}

	@Override
	public String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}

	private void showMainPage(Player activeChar, String command) {
		String filename = "effects_menu";
		if (command.contains("abnormal")) {
			filename = "abnormal";
			StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 2) {
				st.nextToken();
				st.nextToken();
				int page = Integer.parseInt(st.nextToken());
				if (page > 1) {
					filename += page;
				}
			}
		} else if (command.contains("social")) {
			filename = "social";
			StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() >= 2) {
				st.nextToken();
				st.nextToken();
				int page = Integer.parseInt(st.nextToken());
				if (page > 1) {
					filename += page;
				}
			}
		}
		AdminHelpPage.showHelpPage(activeChar, filename + ".htm");
	}
}
