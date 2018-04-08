/*
 * $Header: Broadcast.java, 18/11/2005 15:33:35 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 18/11/2005 15:33:35 $
 * $Revision: 1 $
 * $Log: Broadcast.java,v $
 * Revision 1  18/11/2005 15:33:35  luisantonioa
 * Added copyright notice
 *
 *
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

package l2server.gameserver.util;

import l2server.Config;
import l2server.gameserver.gui.ConsoleTab;
import l2server.gameserver.gui.ConsoleTab.ConsoleFilter;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CharInfo;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.RelationChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public final class Broadcast {
	
	private static Logger log = LoggerFactory.getLogger(Broadcast.class.getName());
	
	/**
	 * Send a packet to all Player in the KnownPlayers of the Creature that have the Character targetted.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR>
	 * Player in the detection area of the Creature are identified in <B>knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the Creature, server just need to go through knownPlayers to send Server->Client Packet<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this Creature (to do this use method toSelfAndKnownPlayers)</B></FONT><BR><BR>
	 */
	public static void toPlayersTargettingMyself(Creature character, L2GameServerPacket mov) {
		if (Config.DEBUG) {
			log.debug("players to notify:" + character.getKnownList().getKnownPlayers().size() + " packet:" + mov.getType());
		}

		Collection<Player> plrs = character.getKnownList().getKnownPlayers().values();
		// synchronized (character.getKnownList().getKnownPlayers())
		{
			for (Player player : plrs) {
				if (player.getTarget() != character) {
					continue;
				}

				player.sendPacket(mov);
			}
		}
	}

	/**
	 * Send a packet to all Player in the KnownPlayers of the
	 * Creature.<BR>
	 * <BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR>
	 * Player in the detection area of the Creature are identified in
	 * <B>knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the
	 * Creature, server just need to go through knownPlayers to send
	 * Server->Client Packet<BR>
	 * <BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND
	 * Server->Client packet to this Creature (to do this use method
	 * toSelfAndKnownPlayers)</B></FONT><BR>
	 * <BR>
	 */
	public static void toKnownPlayers(Creature character, L2GameServerPacket mov) {
		if (Config.DEBUG) {
			log.debug("players to notify:" + character.getKnownList().getKnownPlayers().size() + " packet:" + mov.getType());
		}

		Collection<Player> plrs = character.getKnownList().getKnownPlayers().values();
		//synchronized (character.getKnownList().getKnownPlayers())
		{
			for (Player player : plrs) {
				if (player == null) {
					continue;
				}
				try {
					player.sendPacket(mov);
					if (mov instanceof CharInfo && character instanceof Player) {
						int relation = ((Player) character).getRelation(player);
						Integer oldrelation = character.getKnownList().getKnownRelations().get(player.getObjectId());
						if (oldrelation != null && oldrelation != relation) {
							player.sendPacket(new RelationChanged((Player) character, relation, character.isAutoAttackable(player)));
							if (((Player) character).getPet() != null) {
								player.sendPacket(new RelationChanged(((Player) character).getPet(),
										relation,
										character.isAutoAttackable(player)));
							}
							for (SummonInstance summon : player.getSummons()) {
								player.sendPacket(new RelationChanged(summon, relation, character.isAutoAttackable(player)));
							}
						}
					}
				} catch (NullPointerException e) {
					log.warn(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Send a packet to all Player in the KnownPlayers (in the specified
	 * radius) of the Creature.<BR>
	 * <BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR>
	 * Player in the detection area of the Creature are identified in
	 * <B>knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the
	 * Creature, server just needs to go through knownPlayers to send
	 * Server->Client Packet and check the distance between the targets.<BR>
	 * <BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND
	 * Server->Client packet to this Creature (to do this use method
	 * toSelfAndKnownPlayers)</B></FONT><BR>
	 * <BR>
	 */
	public static void toKnownPlayersInRadius(Creature character, L2GameServerPacket mov, int radius) {
		if (radius < 0) {
			radius = 1500;
		}

		Collection<Player> plrs = character.getKnownList().getKnownPlayers().values();
		//synchronized (character.getKnownList().getKnownPlayers())
		{
			for (Player player : plrs) {
				if (character.isInsideRadius(player, radius, false, false)) {
					player.sendPacket(mov);
				}
			}
		}
	}

	/**
	 * Send a packet to all Player in the KnownPlayers of the Creature and to the specified character.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR>
	 * Player in the detection area of the Creature are identified in <B>knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the Creature, server just need to go through knownPlayers to send Server->Client Packet<BR><BR>
	 */
	public static void toSelfAndKnownPlayers(Creature character, L2GameServerPacket mov) {
		if (character instanceof Player) {
			character.sendPacket(mov);
		}

		toKnownPlayers(character, mov);
	}

	// To improve performance we are comparing values of radius^2 instead of calculating sqrt all the time
	public static void toSelfAndKnownPlayersInRadius(Creature character, L2GameServerPacket mov, long radiusSq) {
		if (radiusSq < 0) {
			radiusSq = 360000;
		}

		if (character instanceof Player) {
			character.sendPacket(mov);
		}

		Collection<Player> plrs = character.getKnownList().getKnownPlayers().values();
		//synchronized (character.getKnownList().getKnownPlayers())
		{
			for (Player player : plrs) {
				if (player != null && character.getDistanceSq(player) <= radiusSq) {
					player.sendPacket(mov);
				}
			}
		}
	}

	/**
	 * Send a packet to all Player present in the world.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR>
	 * In order to inform other players of state modification on the Creature, server just need to go through allPlayers to send Server->Client Packet<BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this Creature (to do this use method toSelfAndKnownPlayers)</B></FONT><BR><BR>
	 */
	public static void toAllOnlinePlayers(L2GameServerPacket mov, int dimensionId) {
		if (Config.DEBUG) {
			log.debug("Players to notify: " + World.getInstance().getAllPlayersCount() + " (with packet " + mov.getType() + ")");
		}

		Collection<Player> pls = World.getInstance().getAllPlayers().values();
		// synchronized (World.getInstance().getAllPlayers())
		{
			for (Player onlinePlayer : pls) {
				if (onlinePlayer == null) {
					continue;
				}

				if (onlinePlayer.isOnline()) {
					onlinePlayer.sendPacket(mov);
				}
			}
		}
	}

	public static void toAllOnlinePlayers(L2GameServerPacket mov) {
		toAllOnlinePlayers(mov, -1);
	}

	public static void announceToOnlinePlayers(String text) {
		CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, "", text);
		toAllOnlinePlayers(cs);

		ConsoleTab.appendMessage(ConsoleFilter.Announcements, "Announcements: " + text);
	}

	public static void toPlayersInInstance(L2GameServerPacket mov, int instanceId) {
		Collection<Player> pls = World.getInstance().getAllPlayers().values();
		//synchronized (character.getKnownList().getKnownPlayers())
		{
			pls.stream()
					.filter(onlinePlayer -> onlinePlayer != null && onlinePlayer.isOnline() && onlinePlayer.getInstanceId() == instanceId)
					.forEachOrdered(onlinePlayer -> {
						onlinePlayer.sendPacket(mov);
					});
		}
	}

	public static void toGameMasters(String message) {
		Collection<Player> pls = World.getInstance().getAllPlayers().values();
		//synchronized (character.getKnownList().getKnownPlayers())
		{
			pls.stream()
					.filter(onlinePlayer -> onlinePlayer != null && onlinePlayer.isOnline() && onlinePlayer.isGM())
					.forEachOrdered(onlinePlayer -> {
						onlinePlayer.sendMessage(message);
					});
		}
	}
}
