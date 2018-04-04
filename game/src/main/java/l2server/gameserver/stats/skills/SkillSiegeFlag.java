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

package l2server.gameserver.stats.skills;

import l2server.Config;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.CastleSiegeManager;
import l2server.gameserver.instancemanager.FortManager;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SiegeFlagInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.entity.Fort;
import l2server.gameserver.templates.StatsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkillSiegeFlag extends Skill {
	private static Logger log = LoggerFactory.getLogger(SkillSiegeFlag.class.getName());


	private final boolean isAdvanced;
	private final boolean isOutpost;

	public SkillSiegeFlag(StatsSet set) {
		super(set);
		isAdvanced = set.getBool("isAdvanced", false);
		isOutpost = set.getBool("isOutpost", false);
	}

	/**
	 * @see Skill#useSkill(Creature, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, WorldObject[] targets) {
		if (!(activeChar instanceof Player)) {
			return;
		}

		Player player = (Player) activeChar;

		if (player.getClan() == null || player.getClan().getLeaderId() != player.getObjectId()) {
			return;
		}

		if (!checkIfOkToPlaceFlag(player, true, isOutpost)) {
			return;
		}

		// Fortress/Castle siege
		try {
			// Spawn a new flag
			SiegeFlagInstance flag = new SiegeFlagInstance(player,
					IdFactory.getInstance().getNextId(),
					NpcTable.getInstance().getTemplate(35062),
					isAdvanced,
					false);
			flag.setTitle(player.getClan().getName());
			flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
			flag.setHeading(player.getHeading());
			flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
			Castle castle = CastleManager.getInstance().getCastle(activeChar);
			Fort fort = FortManager.getInstance().getFort(activeChar);
			if (castle != null) {
				castle.getSiege().getFlag(player.getClan()).add(flag);
			} else if (fort != null) {
				fort.getSiege().getFlag(player.getClan()).add(flag);
			}
		} catch (Exception e) {
			player.sendMessage("Error placing flag:" + e);
			log.warn("Error placing flag: " + e.getMessage(), e);
		}
	}

	/**
	 * Return true if character clan place a flag<BR><BR>
	 *
	 * @param activeChar  The Creature of the character placing the flag
	 * @param isCheckOnly if false, it will send a notification to the player telling him
	 *                    why it failed
	 */
	public static boolean checkIfOkToPlaceFlag(Creature activeChar, boolean isCheckOnly, boolean isOutPost) {
		if (isOutPost) {
			return false;
		}

		Castle castle = CastleManager.getInstance().getCastle(activeChar);
		Fort fort = FortManager.getInstance().getFort(activeChar);

		if (castle != null) {
			return checkIfOkToPlaceFlag(activeChar, castle, isCheckOnly);
		} else if (fort != null) {
			return checkIfOkToPlaceFlag(activeChar, fort, isCheckOnly);
		}
		return false;
	}

	public static boolean checkIfOkToPlaceFlag(Creature activeChar, Castle castle, boolean isCheckOnly) {
		if (!(activeChar instanceof Player)) {
			return false;
		}

		String text = "";
		Player player = (Player) activeChar;

		if (castle == null || castle.getCastleId() <= 0) {
			text = "You must be on castle ground to place a flag.";
		} else if (!castle.getSiege().getIsInProgress()) {
			text = "You can only place a flag during a siege.";
		} else if (castle.getSiege().getAttackerClan(player.getClan()) == null) {
			text = "You must be an attacker to place a flag.";
		} else if (player.getClan() == null || !player.isClanLeader()) {
			text = "You must be a clan leader to place a flag.";
		} else if (castle.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= CastleSiegeManager.getInstance().getFlagMaxCount()) {
			text = "You have already placed the maximum number of flags possible.";
		} else if (player.isInsideZone(Creature.ZONE_NOHQ)) {
			text = "You cannot place flag here.";
		} else {
			return true;
		}

		if (!isCheckOnly) {
			player.sendMessage(text);
		}
		return false;
	}

	public static boolean checkIfOkToPlaceFlag(Creature activeChar, Fort fort, boolean isCheckOnly) {
		if (!(activeChar instanceof Player)) {
			return false;
		}

		String text = "";
		Player player = (Player) activeChar;

		if (fort == null || fort.getFortId() <= 0) {
			text = "You must be on fort ground to place a flag.";
		} else if (!fort.getSiege().getIsInProgress()) {
			text = "You can only place a flag during a siege.";
		} else if (fort.getSiege().getAttackerClan(player.getClan()) == null) {
			text = "You must be an attacker to place a flag.";
		} else if (player.getClan() == null || !player.isClanLeader()) {
			text = "You must be a clan leader to place a flag.";
		} else if (fort.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= Config.FS_MAX_FLAGS) {
			text = "You have already placed the maximum number of flags possible.";
		} else if (player.isInsideZone(Creature.ZONE_NOHQ)) {
			text = "You cannot place flag here.";
		} else {
			return true;
		}

		if (!isCheckOnly) {
			player.sendMessage(text);
		}
		return false;
	}

	/**
	 * Return true if character clan place a flag<BR><BR>
	 *
	 * @param activeChar  The Creature of the character placing the flag
	 * @param isCheckOnly if false, it will send a notification to the player telling him
	 *                    why it failed
	 */
	public static boolean checkIfOkToPlaceHQ(Creature activeChar, boolean isCheckOnly, boolean isOutPost) {
		Castle castle = CastleManager.getInstance().getCastle(activeChar);
		Fort fort = FortManager.getInstance().getFort(activeChar);

		if (castle == null && fort == null) {
			return false;
		}

		String text = "";
		Player player = (Player) activeChar;

		if (fort != null && fort.getFortId() == 0 || castle != null && castle.getCastleId() == 0) {
			text = "You must be on fort or castle ground to construct an outpost or flag.";
		} else if (fort != null && !fort.getZone().isActive() || castle != null && !castle.getZone().isActive()) {
			text = "You can only construct an outpost or flag on siege field.";
		} else if (player.getClan() == null || !player.isClanLeader()) {
			text = "You must be a clan leader to construct an outpost or flag.";
		} else if (player.isInsideZone(Creature.ZONE_NOHQ)) {
			text = "You cannot construct outpost or flag here.";
		} else {
			return true;
		}

		if (!isCheckOnly) {
			player.sendMessage(text);
		}
		return false;
	}
}
